package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.MechanicalCoreAssemblerBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 机械核心装配机的容器菜单：A / B 两个输入槽 + 产物槽，以及玩家背包。
 */
public class MechanicalCoreAssemblerMenu extends AbstractContainerMenu {

    private final MechanicalCoreAssemblerBlockEntity blockEntity;

    public MechanicalCoreAssemblerMenu(int id, Inventory playerInventory, MechanicalCoreAssemblerBlockEntity blockEntity) {
        super(ModMenus.MECHANICAL_CORE_ASSEMBLER.get(), id);
        this.blockEntity = blockEntity;

        // 机器槽位：A / B 横向并排 → C
        addSlot(new Slot(blockEntity.container(), MechanicalCoreAssemblerBlockEntity.SLOT_A, 32, 36));
        addSlot(new Slot(blockEntity.container(), MechanicalCoreAssemblerBlockEntity.SLOT_B, 56, 36));
        addSlot(new Slot(blockEntity.container(), MechanicalCoreAssemblerBlockEntity.SLOT_OUTPUT, 112, 36) {
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

    public MechanicalCoreAssemblerMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf));
    }

    private static MechanicalCoreAssemblerBlockEntity readBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        return be instanceof MechanicalCoreAssemblerBlockEntity forge ? forge : null;
    }

    public MechanicalCoreAssemblerBlockEntity getBlockEntity() {
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
