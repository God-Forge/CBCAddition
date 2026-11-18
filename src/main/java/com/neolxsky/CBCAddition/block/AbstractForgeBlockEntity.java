package com.neolxsky.CBCAddition.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 三台合金炉类机器的公共方块实体基类。
 *
 * <p>统一承担：三槽位库存、容器适配、六面 IO、输出自动弹出、冷却与合成循环、
 * 快捷交互以及 NBT 持久化。具体机器只需实现：</p>
 * <ul>
 *   <li>{@link #isIngredient(int, Item)}：输入槽接受哪些物品</li>
 *   <li>{@link #matchRecipe()}：当前输入匹配到的配方（消耗与产量）</li>
 *   <li>{@link #outputItem()}：产物物品（依赖模组缺失时返回 {@link Items#AIR}）</li>
 *   <li>{@link #getDisplayName()} / {@link #createMenu(int, Inventory, Player)}：界面接入</li>
 * </ul>
 */
public abstract class AbstractForgeBlockEntity extends BlockEntity implements MenuProvider, IOConfigurable {

    /** 输入槽 A（0）。 */
    public static final int SLOT_A = 0;
    /** 输入槽 B（1）。 */
    public static final int SLOT_B = 1;
    /** 产物槽（2）。 */
    public static final int SLOT_OUTPUT = 2;

    public static final int COOLDOWN_TICKS = 20;

    /** 匹配结果：两个输入槽各消耗多少、产出多少。 */
    protected record RecipeResult(int consumeA, int consumeB, int outputCount) {
    }

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == SLOT_A || slot == SLOT_B) {
                return AbstractForgeBlockEntity.this.isIngredient(slot, stack.getItem());
            }
            return false;
        }
    };

    /** 供 Menu 使用的 Container 适配器。 */
    private final Container container = new Container() {
        @Override
        public int getContainerSize() {
            return inventory.getSlots();
        }

        @Override
        public boolean isEmpty() {
            for (int i = 0; i < inventory.getSlots(); i++) {
                if (!inventory.getStackInSlot(i).isEmpty()) {
                    return false;
                }
            }
            return true;
        }

        @Override
        public ItemStack getItem(int slot) {
            return inventory.getStackInSlot(slot);
        }

        @Override
        public ItemStack removeItem(int slot, int amount) {
            return inventory.extractItem(slot, amount, false);
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            ItemStack stack = inventory.getStackInSlot(slot);
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
            return stack;
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            inventory.setStackInSlot(slot, stack);
        }

        @Override
        public void setChanged() {
            AbstractForgeBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }

        @Override
        public boolean canPlaceItem(int slot, ItemStack stack) {
            return inventory.isItemValid(slot, stack);
        }

        @Override
        public void clearContent() {
            for (int i = 0; i < inventory.getSlots(); i++) {
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    };

    private final FaceMode[] faceModes = new FaceMode[]{
            FaceMode.INPUT, FaceMode.INPUT, FaceMode.INPUT,
            FaceMode.INPUT, FaceMode.INPUT, FaceMode.INPUT
    };

    private int cooldown = 0;

    /** 自动弹出：开启后每 tick 从可输出面中随机挑一个弹射产物。 */
    private boolean autoEject = true;

    /** 每个方向缓存一个能力实例：AE2 等模组会缓存 handler，不能每次新建。 */
    private final LazyOptional<IItemHandler>[] faceHandlers = new LazyOptional[6];

    protected AbstractForgeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        for (int i = 0; i < faceHandlers.length; i++) {
            final Direction face = Direction.from3DDataValue(i);
            faceHandlers[i] = LazyOptional.of(() -> new FaceHandler(face));
        }
    }

    // ------------------------------------------------------------------
    // 子类钩子
    // ------------------------------------------------------------------

    /** 指定的输入槽是否接受该物品。 */
    protected abstract boolean isIngredient(int slot, Item item);

    /** 当前输入匹配到的配方；{@code null} 表示不匹配。 */
    @Nullable
    protected abstract RecipeResult matchRecipe();

    /** 产物物品；依赖模组缺失时应返回 {@link Items#AIR}。 */
    protected abstract Item outputItem();

    // ------------------------------------------------------------------
    // 供子类 / 外部使用的公共访问
    // ------------------------------------------------------------------

    /** 读取槽位内容（只读使用；修改请走容器或交互）。 */
    protected ItemStack getStack(int slot) {
        return inventory.getStackInSlot(slot);
    }

    public Container container() {
        return container;
    }

    // ------------------------------------------------------------------
    // 六面 IO
    // ------------------------------------------------------------------

    @Override
    public FaceMode getFaceMode(Direction direction) {
        return faceModes[direction.get3DDataValue()];
    }

    @Override
    public void setFaceMode(Direction direction, FaceMode mode) {
        faceModes[direction.get3DDataValue()] = mode;
        setChanged();
    }

    public boolean getAutoEject() {
        return autoEject;
    }

    public void setAutoEject(boolean autoEject) {
        this.autoEject = autoEject;
        setChanged();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            return faceHandlers[side.get3DDataValue()].cast();
        }
        return super.getCapability(cap, side);
    }

    /** 暴露给某一面的 IItemHandler：输入面只允许插入，输出面只允许提取。 */
    private final class FaceHandler implements IItemHandler {
        private final Direction face;

        private FaceHandler(Direction face) {
            this.face = face;
        }

        /** 动态读取当前模式，保证 IO 面板修改后立即生效（AE2 等会缓存 handler）。 */
        private FaceMode mode() {
            return getFaceMode(face);
        }

        @Override
        public int getSlots() {
            return switch (mode()) {
                case OUTPUT -> 1;
                case BOTH -> 3;
                case DISABLED -> 0;
                default -> 2;
            };
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(toInternal(slot));
        }

        private int toInternal(int slot) {
            FaceMode mode = mode();
            if (mode == FaceMode.OUTPUT) {
                return SLOT_OUTPUT;
            }
            if (mode == FaceMode.BOTH) {
                return switch (slot) {
                    case 0 -> SLOT_A;
                    case 1 -> SLOT_B;
                    default -> SLOT_OUTPUT;
                };
            }
            return slot == 0 ? SLOT_A : SLOT_B;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            FaceMode mode = mode();
            if (!mode.canInput()) {
                return stack;
            }
            if (mode == FaceMode.BOTH && slot == 2) {
                return stack;
            }
            return inventory.insertItem(toInternal(slot), stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            FaceMode mode = mode();
            if (!mode.canOutput()) {
                return ItemStack.EMPTY;
            }
            if (mode == FaceMode.BOTH && slot != 2) {
                return ItemStack.EMPTY;
            }
            return inventory.extractItem(SLOT_OUTPUT, amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            FaceMode mode = mode();
            if (!mode.canInput()) {
                return false;
            }
            if (mode == FaceMode.BOTH && slot == 2) {
                return false;
            }
            return inventory.isItemValid(toInternal(slot), stack);
        }
    }

    // ------------------------------------------------------------------
    // 合成循环
    // ------------------------------------------------------------------

    /** 由方块 ticker 每 tick 调用一次。 */
    protected void tickServer() {
        if (level == null || level.isClientSide) {
            return;
        }

        if (autoEject) {
            ejectToRandomFace();
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Item output = outputItem();
        if (output == Items.AIR) {
            return;
        }

        RecipeResult result = matchRecipe();
        if (result == null || result.outputCount() <= 0) {
            return;
        }

        if (inventory.getStackInSlot(SLOT_A).getCount() < result.consumeA()
                || inventory.getStackInSlot(SLOT_B).getCount() < result.consumeB()) {
            return;
        }

        ItemStack outputSlot = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!outputSlot.isEmpty()
                && (!outputSlot.is(output) || outputSlot.getCount() > outputSlot.getMaxStackSize() - result.outputCount())) {
            return;
        }

        inventory.extractItem(SLOT_A, result.consumeA(), false);
        inventory.extractItem(SLOT_B, result.consumeB(), false);
        if (outputSlot.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(output, result.outputCount()));
        } else {
            outputSlot.grow(result.outputCount());
            inventory.setStackInSlot(SLOT_OUTPUT, outputSlot);
        }
        cooldown = COOLDOWN_TICKS;
        setChanged();
    }

    private void pushOutput(Direction direction) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (output.isEmpty()) {
            return;
        }
        BlockEntity targetEntity = level.getBlockEntity(worldPosition.relative(direction));
        if (targetEntity == null) {
            return;
        }
        IItemHandler target = targetEntity.getCapability(ForgeCapabilities.ITEM_HANDLER,
                direction.getOpposite()).orElse(null);
        if (target == null) {
            return;
        }
        ItemStack rest = ItemHandlerHelper.insertItem(target, output.copy(), false);
        if (rest.getCount() != output.getCount()) {
            inventory.setStackInSlot(SLOT_OUTPUT, rest);
        }
    }

    /** 从所有可输出面中随机挑一个弹射产物；没有可输出面时什么都不做。 */
    private void ejectToRandomFace() {
        Direction[] outputs = new Direction[6];
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (getFaceMode(direction).canOutput()) {
                outputs[count++] = direction;
            }
        }
        if (count == 0) {
            return;
        }
        pushOutput(outputs[level.random.nextInt(count)]);
    }

    // ------------------------------------------------------------------
    // 快捷交互
    // ------------------------------------------------------------------

    /**
     * 潜行右键取出产物；手持任一原料则填入第一个可放入的输入槽。
     *
     * @return {@code true} 表示本次交互已被消费，调用方不应再打开 GUI
     */
    public boolean interact(Player player, ItemStack held) {
        if (player.isShiftKeyDown()) {
            takeOutput(player);
            return true;
        }
        if (!held.isEmpty()) {
            for (int slot : new int[]{SLOT_A, SLOT_B}) {
                if (inventory.getStackInSlot(slot).isEmpty() && inventory.isItemValid(slot, held)) {
                    ItemStack rest = inventory.insertItem(slot, held, false);
                    player.setItemInHand(InteractionHand.MAIN_HAND, rest);
                    return true;
                }
            }
        }
        return false;
    }

    private void takeOutput(Player player) {
        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()) {
            player.getInventory().placeItemBackInInventory(output.copy());
            inventory.setStackInSlot(SLOT_OUTPUT, ItemStack.EMPTY);
        }
    }

    // ------------------------------------------------------------------
    // 朝向与 NBT
    // ------------------------------------------------------------------

    /** 当前方块朝向（供 GUI 的六面面板使用）。 */
    public Direction getFacing() {
        BlockState state = getBlockState();
        if (state.hasProperty(AbstractForgeBlock.FACING)) {
            return state.getValue(AbstractForgeBlock.FACING);
        }
        return Direction.NORTH;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Cooldown", cooldown);
        tag.putBoolean("AutoEject", autoEject);
        for (int i = 0; i < faceModes.length; i++) {
            tag.putByte("Face" + i, (byte) faceModes[i].ordinal());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(tag.getCompound("Inventory"));
        }
        cooldown = tag.getInt("Cooldown");
        if (tag.contains("AutoEject")) {
            autoEject = tag.getBoolean("AutoEject");
        }
        for (int i = 0; i < faceModes.length; i++) {
            if (tag.contains("Face" + i)) {
                int ordinal = tag.getByte("Face" + i);
                faceModes[i] = FaceMode.values()[Math.max(0, Math.min(ordinal, FaceMode.values().length - 1))];
            }
        }
    }
}
