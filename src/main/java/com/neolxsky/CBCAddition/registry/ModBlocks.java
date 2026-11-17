package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.block.AndesiteForgeBlock;
import com.neolxsky.CBCAddition.block.BrassForgeBlock;
import com.neolxsky.CBCAddition.block.MechanicalCoreAssemblerBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块注册表。
 */
public final class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, CBCAddition.MODID);

    public static final DeferredRegister<Item> BLOCK_ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, CBCAddition.MODID);

    public static final RegistryObject<Block> ANDESITE_FORGE =
            BLOCKS.register("andesite_forge", AndesiteForgeBlock::new);

    public static final RegistryObject<Item> ANDESITE_FORGE_ITEM =
            BLOCK_ITEMS.register("andesite_forge",
                    () -> new BlockItem(ANDESITE_FORGE.get(), new Item.Properties()));

    public static final RegistryObject<Block> BRASS_FORGE =
            BLOCKS.register("brass_forge", BrassForgeBlock::new);

    public static final RegistryObject<Item> BRASS_FORGE_ITEM =
            BLOCK_ITEMS.register("brass_forge",
                    () -> new BlockItem(BRASS_FORGE.get(), new Item.Properties()));

    public static final RegistryObject<Block> MECHANICAL_CORE_ASSEMBLER =
            BLOCKS.register("mechanical_core_assembler", MechanicalCoreAssemblerBlock::new);

    public static final RegistryObject<Item> MECHANICAL_CORE_ASSEMBLER_ITEM =
            BLOCK_ITEMS.register("mechanical_core_assembler",
                    () -> new BlockItem(MECHANICAL_CORE_ASSEMBLER.get(), new Item.Properties()));

    private ModBlocks() {
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
        BLOCK_ITEMS.register(eventBus);
    }
}
