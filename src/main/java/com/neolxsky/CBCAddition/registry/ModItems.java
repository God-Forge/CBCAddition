package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * 物品注册表。
 *
 * <p>如果暂时不需要自定义物品，可以保持为空；
 * 需要时在这里用 DeferredRegister 注册即可。</p>
 */
public final class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CBCAddition.MODID);

    private ModItems() {
    }

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
