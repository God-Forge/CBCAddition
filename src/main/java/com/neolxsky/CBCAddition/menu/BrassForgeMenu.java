package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.BrassForgeBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 黄铜合金炉的容器菜单：3 个机器槽位 + 玩家背包。
 */
public class BrassForgeMenu extends AbstractContainerMenu {

    private final BrassForgeBlockEntity blockEntity;

    public BrassForgeMenu(int id, Inventory playerInventory, BrassForgeBlockEntity blockEntity) {
        super(ModMenus.BRASS_FORGE.get(), id);
        this.blockEntity = blockEntity;

        addSlot(new Slot(blockEntity.container(), BrassForgeBlockEntity.SLOT_COPPER, 56, 17));
        addSlot(new Slot(blockEntity.container(), BrassForgeBlockEntity.SLOT_GOLD, 56, 53));
        addSlot(new Slot(blockEntity.container(), BrassForgeBlockEntity.SLOT_OUTPUT, 116, 31) {
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

    public BrassForgeMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf));
    }

    private static BrassForgeBlockEntity readBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        return be instanceof BrassForgeBlockEntity forge ? forge : null;
    }

    public BrassForgeBlockEntity getBlockEntity() {
        return blockEntity;
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
