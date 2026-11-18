package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 把 CBCAdd 的物品加入创造模式标签页。
 *
 * <p>Forge 1.20.1 中 mod 物品默认不会出现在创造物品栏，
 * 必须通过 BuildCreativeModeTabContentsEvent 显式添加。</p>
 */
@Mod.EventBusSubscriber(modid = CBCAddition.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class ModCreativeTabs {

    private ModCreativeTabs() {
    }

    @SubscribeEvent
    public static void buildContents(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ModBlocks.ANDESITE_FORGE_ITEM.get());
            event.accept(ModBlocks.BRASS_FORGE_ITEM.get());
            event.accept(ModBlocks.MECHANICAL_CORE_ASSEMBLER_ITEM.get());
            event.accept(ModItems.INFINITY_CELL_CONFIGURATOR.get());
        }
        // 搜索标签：保证在创造模式搜索里能找到
        if (event.getTabKey() == CreativeModeTabs.SEARCH) {
            event.accept(ModBlocks.ANDESITE_FORGE_ITEM.get());
            event.accept(ModBlocks.BRASS_FORGE_ITEM.get());
            event.accept(ModBlocks.MECHANICAL_CORE_ASSEMBLER_ITEM.get());
            event.accept(ModItems.INFINITY_CELL_CONFIGURATOR.get());
        }
    }
}
