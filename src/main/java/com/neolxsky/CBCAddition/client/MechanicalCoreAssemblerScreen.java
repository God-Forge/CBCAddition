package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.menu.MechanicalCoreAssemblerMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * 机械核心装配机 GUI（绘制逻辑由 {@link AbstractForgeScreen} 统一提供）。
 */
public class MechanicalCoreAssemblerScreen extends AbstractForgeScreen<MechanicalCoreAssemblerMenu> {

    public MechanicalCoreAssemblerScreen(MechanicalCoreAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }
}
