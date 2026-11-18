package com.neolxsky.CBCAddition.item;

import com.neolxsky.CBCAddition.menu.InfinityCellConfiguratorMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

/**
 * 无限元件配置器：手持右键打开 GUI，用 1 个 16k 存储组件 + 样品
 * 制造出对应物品 / 流体的 ExtendedAE 无限元件。
 */
public class InfinityCellConfiguratorItem extends Item {

    public InfinityCellConfiguratorItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer,
                    new SimpleMenuProvider((id, inventory, p) -> new InfinityCellConfiguratorMenu(id, inventory),
                            Component.translatable("item.cbcaddition.infinity_cell_configurator")));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
