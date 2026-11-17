package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.AndesiteForgeBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * 安山合金炉的容器菜单：3 个机器槽位 + 玩家背包。
 */
public class AndesiteForgeMenu extends AbstractContainerMenu {

    private final AndesiteForgeBlockEntity blockEntity;

    public AndesiteForgeMenu(int id, Inventory playerInventory, AndesiteForgeBlockEntity blockEntity) {
        super(ModMenus.ANDESITE_FORGE.get(), id);
        this.blockEntity = blockEntity;

        // 机器槽位：A / B 横向并排 → C（箭头材质沿用原版熔炉）
        addSlot(new Slot(blockEntity.container(), AndesiteForgeBlockEntity.SLOT_IRON, 32, 36));
        addSlot(new Slot(blockEntity.container(), AndesiteForgeBlockEntity.SLOT_ANDESITE, 56, 36));
        addSlot(new Slot(blockEntity.container(), AndesiteForgeBlockEntity.SLOT_OUTPUT, 112, 36) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });

        // 玩家背包 3x9
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        // 快捷栏
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    /** 客户端构造：从网络包读取方块位置。 */
    public AndesiteForgeMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf));
    }

    private static AndesiteForgeBlockEntity readBlockEntity(Inventory playerInventory, FriendlyByteBuf buf) {
        var pos = buf.readBlockPos();
        var be = playerInventory.player.level().getBlockEntity(pos);
        return be instanceof AndesiteForgeBlockEntity forge ? forge : null;
    }

    public AndesiteForgeBlockEntity getBlockEntity() {
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
                // 机器槽 → 玩家背包
                if (!moveItemStackTo(stack, 3, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // 玩家背包 → 机器槽
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
