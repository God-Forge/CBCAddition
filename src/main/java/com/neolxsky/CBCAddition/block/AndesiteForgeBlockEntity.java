package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.AndesiteForgeMenu;
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
 * 安山合金炉的方块实体：
 * 槽位 0 = 铁锭，槽位 1 = 安山岩，槽位 2 = 产物（安山合金）。
 *
 * <p>每 20 tick 检查一次：1 铁锭 + 9 安山岩 → 10 安山合金。</p>
 *
 * <p>六面 IO：每个面可设置为禁用 / 输入 / 输出。
 * 输出面会把产物自动弹出到相邻物品容器。</p>
 */
public class AndesiteForgeBlockEntity extends BlockEntity implements MenuProvider, IOConfigurable {

    public static final int SLOT_IRON = 0;
    public static final int SLOT_ANDESITE = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final int ANDESITE_COST = 9;
    public static final int OUTPUT_AMOUNT = 10;
    public static final int COOLDOWN_TICKS = 20;

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_IRON -> stack.is(Items.IRON_INGOT);
                case SLOT_ANDESITE -> stack.is(Items.ANDESITE);
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
            AndesiteForgeBlockEntity.this.setChanged();
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

    /** 六个面的 IO 配置（默认全输入，方便漏斗）。 */
    private final FaceMode[] faceModes = new FaceMode[]{
            FaceMode.INPUT, FaceMode.INPUT, FaceMode.INPUT,
            FaceMode.INPUT, FaceMode.INPUT, FaceMode.INPUT
    };

    private int cooldown = 0;

    public AndesiteForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANDESITE_FORGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AndesiteForgeBlockEntity forge) {
        forge.tick();
    }

    public Container container() {
        return container;
    }

    // ------------------------------------------------------------------
    // 六面 IO
    // ------------------------------------------------------------------

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
                    case 0 -> SLOT_IRON;
                    case 1 -> SLOT_ANDESITE;
                    default -> SLOT_OUTPUT;
                };
            }
            return slot == 0 ? SLOT_IRON : SLOT_ANDESITE;
        }

        @Override
        public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
            if (!mode.canInput()) {
                return stack;
            }
            if (mode == FaceMode.BOTH && slot == 2) {
                return stack; // BOTH 模式下输出槽不可插入
            }
            return inventory.insertItem(toInternal(slot), stack, simulate);
        }

        @Override
        public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!mode.canOutput()) {
                return ItemStack.EMPTY;
            }
            if (mode == FaceMode.BOTH && slot != 2) {
                return ItemStack.EMPTY; // BOTH 模式下仅输出槽可提取
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

        // 自动弹出（输出面 / 双向面）
        for (Direction direction : Direction.values()) {
            if (getFaceMode(direction).canOutput()) {
                pushOutput(direction);
            }
        }

        if (cooldown > 0) {
            cooldown--;
            return;
        }

        ItemStack iron = inventory.getStackInSlot(SLOT_IRON);
        ItemStack andesite = inventory.getStackInSlot(SLOT_ANDESITE);
        if (iron.getCount() < 1 || andesite.getCount() < ANDESITE_COST) {
            return;
        }

        Item andesiteAlloy = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "andesite_alloy"));
        if (andesiteAlloy == Items.AIR) {
            return;
        }

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()
                && (!output.is(andesiteAlloy) || output.getCount() > output.getMaxStackSize() - OUTPUT_AMOUNT)) {
            return;
        }

        inventory.extractItem(SLOT_IRON, 1, false);
        inventory.extractItem(SLOT_ANDESITE, ANDESITE_COST, false);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(andesiteAlloy, OUTPUT_AMOUNT));
        } else {
            output.grow(OUTPUT_AMOUNT);
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
    // 右键（无 GUI 时的快速交互 / 潜行取出）
    // ------------------------------------------------------------------

    /**
     * 快速交互：潜行右键取出产物；手持对应原料右键直接投料。
     *
     * @return {@code true} 表示本次交互已被消费，调用方不应再打开 GUI
     */
    public boolean interact(Player player, ItemStack held) {
        if (player.isShiftKeyDown()) {
            takeOutput(player);
            return true;
        }
        if (!held.isEmpty()) {
            if (held.is(Items.IRON_INGOT)) {
                ItemStack rest = inventory.insertItem(SLOT_IRON, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, rest);
                return true;
            }
            if (held.is(Items.ANDESITE)) {
                ItemStack rest = inventory.insertItem(SLOT_ANDESITE, held, false);
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
    // MenuProvider
    // ------------------------------------------------------------------

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.andesite_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AndesiteForgeMenu(id, playerInventory, this);
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
