package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.MechanicalCoreAssemblerMenu;
import com.neolxsky.CBCAddition.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
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
 * 机械核心装配机的方块实体：
 * 槽位 0 = A，槽位 1 = B，槽位 2 = 产物（机械核心）。
 *
 * <p>两种配方，输入顺序不敏感：</p>
 * <ul>
 *   <li>1 安山机壳 + 1 大齿轮 → 3 机械核心</li>
 *   <li>1 工业镀铁块 + 1 大型工业齿轮 → 10 机械核心</li>
 * </ul>
 *
 * <p>每 20 tick 合成一次；六面 IO 与自动弹出逻辑与另外两台机器一致。</p>
 */
public class MechanicalCoreAssemblerBlockEntity extends BlockEntity implements MenuProvider, IOConfigurable {

    public static final int SLOT_A = 0;
    public static final int SLOT_B = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final int COOLDOWN_TICKS = 20;

    /** 匹配到的配方：只需要知道产量。 */
    private record Match(int amount) {
    }

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_A, SLOT_B -> isIngredient(stack.getItem());
                default -> false;
            };
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
            MechanicalCoreAssemblerBlockEntity.this.setChanged();
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

    public MechanicalCoreAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MECHANICAL_CORE_ASSEMBLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MechanicalCoreAssemblerBlockEntity forge) {
        forge.tick();
    }

    public Container container() {
        return container;
    }

    public FaceMode getFaceMode(Direction direction) {
        return faceModes[direction.get3DDataValue()];
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        faceModes[direction.get3DDataValue()] = mode;
        setChanged();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER && side != null) {
            FaceMode mode = getFaceMode(side);
            if (mode != FaceMode.DISABLED) {
                return LazyOptional.of(() -> new FaceHandler(side, mode)).cast();
            }
        }
        return super.getCapability(cap, side);
    }

    /** 暴露给某一面的 IItemHandler：输入面只允许插入，输出面只允许提取。 */
    private final class FaceHandler implements IItemHandler {
        private final Direction face;
        private final FaceMode mode;

        private FaceHandler(Direction face, FaceMode mode) {
            this.face = face;
            this.mode = mode;
        }

        @Override
        public int getSlots() {
            return switch (mode) {
                case OUTPUT -> 1;
                case BOTH -> 3;
                default -> 2;
            };
        }

        @Override
        public @NotNull ItemStack getStackInSlot(int slot) {
            return inventory.getStackInSlot(toInternal(slot));
        }

        private int toInternal(int slot) {
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
    // 逻辑
    // ------------------------------------------------------------------

    private void tick() {
        if (level == null || level.isClientSide) {
            return;
        }

        for (Direction direction : Direction.values()) {
            if (getFaceMode(direction).canOutput()) {
                pushOutput(direction);
            }
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        Item core = mechanicalCore();
        if (core == Items.AIR) {
            return;
        }

        Match match = matchRecipe();
        if (match == null) {
            return;
        }

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()
                && (!output.is(core) || output.getCount() > output.getMaxStackSize() - match.amount())) {
            return;
        }

        inventory.extractItem(SLOT_A, 1, false);
        inventory.extractItem(SLOT_B, 1, false);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(core, match.amount()));
        } else {
            output.grow(match.amount());
            inventory.setStackInSlot(SLOT_OUTPUT, output);
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

    // ------------------------------------------------------------------
    // 快捷交互
    // ------------------------------------------------------------------

    /**
     * 潜行右键取出产物；手持任一配方原料则依次填入 A / B 空槽。
     *
     * @return {@code true} 表示本次交互已被消费，调用方不应再打开 GUI
     */
    public boolean interact(Player player, ItemStack held) {
        if (player.isShiftKeyDown()) {
            takeOutput(player);
            return true;
        }
        if (!held.isEmpty() && isIngredient(held.getItem())) {
            if (inventory.getStackInSlot(SLOT_A).isEmpty()) {
                ItemStack rest = inventory.insertItem(SLOT_A, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, rest);
                return true;
            }
            if (inventory.getStackInSlot(SLOT_B).isEmpty()) {
                ItemStack rest = inventory.insertItem(SLOT_B, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, rest);
                return true;
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
    // 配方
    // ------------------------------------------------------------------

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(namespace, path));
    }

    private static Item mechanicalCore() {
        return item("kubejs", "mechanical_core");
    }

    private static boolean isIngredient(Item item) {
        return item == item("create", "andesite_casing")
                || item == item("create", "large_cogwheel")
                || item == item("design_decor", "industrial_plating_block")
                || item == item("design_decor", "industrial_gear_large");
    }

    private Match matchRecipe() {
        Item a = inventory.getStackInSlot(SLOT_A).getItem();
        Item b = inventory.getStackInSlot(SLOT_B).getItem();
        if (a == Items.AIR || b == Items.AIR) {
            return null;
        }
        if (isPair(a, b, item("create", "andesite_casing"), item("create", "large_cogwheel"))) {
            return new Match(3);
        }
        if (isPair(a, b, item("design_decor", "industrial_plating_block"),
                item("design_decor", "industrial_gear_large"))) {
            return new Match(10);
        }
        return null;
    }

    private static boolean isPair(Item a, Item b, Item x, Item y) {
        if (x == Items.AIR || y == Items.AIR) {
            return false;
        }
        return (a == x && b == y) || (a == y && b == x);
    }

    // ------------------------------------------------------------------
    // MenuProvider
    // ------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.mechanical_core_assembler");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MechanicalCoreAssemblerMenu(id, playerInventory, this);
    }

    // ------------------------------------------------------------------
    // NBT
    // ------------------------------------------------------------------

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.put("Inventory", inventory.serializeNBT());
        tag.putInt("Cooldown", cooldown);
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
        for (int i = 0; i < faceModes.length; i++) {
            if (tag.contains("Face" + i)) {
                int ordinal = tag.getByte("Face" + i);
                faceModes[i] = FaceMode.values()[Math.max(0, Math.min(ordinal, FaceMode.values().length - 1))];
            }
        }
    }
}
