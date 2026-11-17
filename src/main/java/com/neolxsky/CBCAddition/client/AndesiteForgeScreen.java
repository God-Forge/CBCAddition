package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.menu.AndesiteForgeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 安山合金炉 GUI（绘制逻辑由 {@link AbstractForgeScreen} 统一提供）。
 */
public class AndesiteForgeScreen extends AbstractForgeScreen<AndesiteForgeMenu> {

    public AndesiteForgeScreen(AndesiteForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
