package com.neolxsky.CBCAddition;

import com.mojang.logging.LogUtils;
import com.neolxsky.CBCAddition.registry.ModBlockEntities;
import com.neolxsky.CBCAddition.registry.ModBlocks;
import com.neolxsky.CBCAddition.registry.ModItems;
import com.neolxsky.CBCAddition.registry.ModMenus;
import com.neolxsky.CBCAddition.network.ModNetwork;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

/**
 * CBCAddition —— 黄铜协奏曲（Create: The Brass Concerto）的私货附属模组。
 */
@Mod(CBCAddition.MODID)
public class CBCAddition {

    /** 模组的命名空间（必须小写，Minecraft 资源命名规则）。 */
    public static final String MODID = "cbcaddition";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CBCAddition() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册物品 / 方块 / 方块实体 / 菜单
        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenus.register(modEventBus);

        // 网络通道
        ModNetwork.register();

        // 客户端屏幕注册（仅客户端加载，避免专用服务器崩）
        if (net.minecraftforge.fml.loading.FMLEnvironment.dist == net.minecraftforge.api.distmarker.Dist.CLIENT) {
            modEventBus.addListener(com.neolxsky.CBCAddition.client.ClientSetup::onClientSetup);
        }

        MinecraftForge.EVENT_BUS.register(this);

        LOGGER.info("CBCAddition 已加载 —— 让材料制作飞起来！");
    }
}
