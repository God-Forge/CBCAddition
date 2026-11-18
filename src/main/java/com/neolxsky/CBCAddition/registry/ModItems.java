package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.item.InfinityCellConfiguratorItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 物品注册表。
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CBCAddition.MODID);

    /** 无限元件配置器（手持右键打开 GUI）。 */
    public static final RegistryObject<Item> INFINITY_CELL_CONFIGURATOR =
            ITEMS.register("infinity_cell_configurator", InfinityCellConfiguratorItem::new);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
