package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.AbstractForgeBlockEntity;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * 三台机器的公共容器菜单：A / B → C 槽位与玩家背包。
 */
public abstract class AbstractForgeMenu extends AbstractContainerMenu {

    protected final AbstractForgeBlockEntity blockEntity;

    protected AbstractForgeMenu(MenuType<?> menuType, int id, Inventory playerInventory,
                                 AbstractForgeBlockEntity blockEntity) {
        super(menuType, id);
        this.blockEntity = blockEntity;

        addSlot(new Slot(blockEntity.container(), AbstractForgeBlockEntity.SLOT_A, 32, 36));
        addSlot(new Slot(blockEntity.container(), AbstractForgeBlockEntity.SLOT_B, 56, 36));
        addSlot(new Slot(blockEntity.container(), AbstractForgeBlockEntity.SLOT_OUTPUT, 112, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    public AbstractForgeBlockEntity getBlockEntity() {
        return blockEntity;
    }

    /** 从网络包读取方块实体（客户端构造用）。 */
    protected static <T extends AbstractForgeBlockEntity> T readBlockEntity(Inventory playerInventory,
                                                                            FriendlyByteBuf buf, Class<T> type) {
        var pos = buf.readBlockPos();
        BlockEntity be = playerInventory.player.level().getBlockEntity(pos);
        return type.isInstance(be) ? type.cast(be) : null;
    }

    @Override
    public boolean stillValid(Player player) {
        if (blockEntity == null || blockEntity.getLevel() == null) {
            return false;
        }
        return player.distanceToSqr(blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();

            if (index < 3) {
                if (!moveItemStackTo(stack, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (stack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, stack);
        }
        return result;
    }
}
