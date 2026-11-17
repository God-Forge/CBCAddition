package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.menu.BrassForgeMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 黄铜合金炉 GUI（绘制逻辑由 {@link AbstractForgeScreen} 统一提供）。
 */
public class BrassForgeScreen extends AbstractForgeScreen<BrassForgeMenu> {

    public BrassForgeScreen(BrassForgeMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
