package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.BrassForgeMenu;
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
 * 黄铜合金炉的方块实体：
 * 槽位 0 = 铜锭，槽位 1 = 金锭，槽位 2 = 产物（黄铜锭）。
 *
 * <p>每 20 tick 检查一次：1 铜锭 + 1 金锭 → 2 黄铜锭。</p>
 */
public class BrassForgeBlockEntity extends BlockEntity implements MenuProvider, IOConfigurable {

    public static final int SLOT_COPPER = 0;
    public static final int SLOT_GOLD = 1;
    public static final int SLOT_OUTPUT = 2;

    public static final int COPPER_COST = 1;
    public static final int GOLD_COST = 1;
    public static final int OUTPUT_AMOUNT = 2;
    public static final int COOLDOWN_TICKS = 20;

    private final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case SLOT_COPPER -> stack.is(Items.COPPER_INGOT);
                case SLOT_GOLD -> stack.is(Items.GOLD_INGOT);
                default -> false;
            };
        }
    };

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
            BrassForgeBlockEntity.this.setChanged();
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

    public BrassForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRASS_FORGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrassForgeBlockEntity forge) {
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
                    case 0 -> SLOT_COPPER;
                    case 1 -> SLOT_GOLD;
                    default -> SLOT_OUTPUT;
                };
            }
            return slot == 0 ? SLOT_COPPER : SLOT_GOLD;
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

        ItemStack copper = inventory.getStackInSlot(SLOT_COPPER);
        ItemStack gold = inventory.getStackInSlot(SLOT_GOLD);
        if (copper.getCount() < COPPER_COST || gold.getCount() < GOLD_COST) {
            return;
        }

        Item brass = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "brass_ingot"));
        if (brass == Items.AIR) {
            return;
        }

        ItemStack output = inventory.getStackInSlot(SLOT_OUTPUT);
        if (!output.isEmpty()
                && (!output.is(brass) || output.getCount() > output.getMaxStackSize() - OUTPUT_AMOUNT)) {
            return;
        }

        inventory.extractItem(SLOT_COPPER, COPPER_COST, false);
        inventory.extractItem(SLOT_GOLD, GOLD_COST, false);
        if (output.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, new ItemStack(brass, OUTPUT_AMOUNT));
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
            if (held.is(Items.COPPER_INGOT)) {
                ItemStack rest = inventory.insertItem(SLOT_COPPER, held, false);
                player.setItemInHand(InteractionHand.MAIN_HAND, rest);
                return true;
            }
            if (held.is(Items.GOLD_INGOT)) {
                ItemStack rest = inventory.insertItem(SLOT_GOLD, held, false);
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

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.brass_forge");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BrassForgeMenu(id, playerInventory, this);
    }

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
