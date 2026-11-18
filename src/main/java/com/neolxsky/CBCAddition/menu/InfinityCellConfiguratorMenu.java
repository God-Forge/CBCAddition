package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.compat.extendedae.EaeInfinityCellHelper;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * 无限元件配置器的容器菜单：样品槽 + 产物槽 + 玩家背包。
 */
public class InfinityCellConfiguratorMenu extends AbstractContainerMenu {

    private final Container container = new SimpleContainer(2);

    public InfinityCellConfiguratorMenu(int id, Inventory playerInventory) {
        super(ModMenus.INFINITY_CELL_CONFIGURATOR.get(), id);

        // 槽位 0 = 样品（物品或流体容器）；槽位 1 = 产物
        addSlot(new Slot(container, 0, 44, 35));
        addSlot(new Slot(container, 1, 116, 35) {
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

    public InfinityCellConfiguratorMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory);
    }

    /** 服务端执行制造：读取样品 → 生成无限元件 → 消耗 1 个 16k 组件。 */
    public void tryCraft(Player player) {
        ItemStack sample = container.getItem(0);
        if (sample.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cbcaddition.configurator.no_sample"), true);
            return;
        }
        ItemStack result = EaeInfinityCellHelper.createInfinityCell(sample);
        if (result.isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cbcaddition.configurator.unavailable"), true);
            return;
        }
        if (!container.getItem(1).isEmpty()) {
            player.displayClientMessage(Component.translatable("message.cbcaddition.configurator.output_full"), true);
            return;
        }
        if (!consumeComponent(player)) {
            player.displayClientMessage(Component.translatable("message.cbcaddition.configurator.no_component"), true);
            return;
        }
        container.setItem(1, result);
    }

    private static boolean consumeComponent(Player player) {
        Item component = BuiltInRegistries.ITEM.get(new ResourceLocation("ae2", "cell_component_16k"));
        if (component == Items.AIR) {
            return false;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(component)) {
                stack.shrink(1);
                return true;
            }
        }
        return false;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < 2) {
                if (!moveItemStackTo(stack, 2, slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!moveItemStackTo(stack, 0, 1, false)) {
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

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        for (int i = 0; i < container.getContainerSize(); i++) {
            ItemStack stack = container.removeItemNoUpdate(i);
            if (!stack.isEmpty()) {
                player.getInventory().placeItemBackInInventory(stack);
            }
        }
    }
}
