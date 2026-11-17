package com.neolxsky.CBCAddition.client;

import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * 客户端初始化：注册 GUI 屏幕。
 */
public final class ClientSetup {

    private ClientSetup() {
    }

    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenus.ANDESITE_FORGE.get(), AndesiteForgeScreen::new);
            MenuScreens.register(ModMenus.BRASS_FORGE.get(), BrassForgeScreen::new);
        });
    }
}
