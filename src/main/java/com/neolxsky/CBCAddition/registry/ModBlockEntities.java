package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.block.AndesiteForgeBlockEntity;
import com.neolxsky.CBCAddition.block.BrassForgeBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 方块实体注册表。
 */
public final class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, CBCAddition.MODID);

    public static final RegistryObject<BlockEntityType<AndesiteForgeBlockEntity>> ANDESITE_FORGE =
            BLOCK_ENTITIES.register("andesite_forge",
                    () -> BlockEntityType.Builder.of(AndesiteForgeBlockEntity::new, ModBlocks.ANDESITE_FORGE.get()).build(null));

    public static final RegistryObject<BlockEntityType<BrassForgeBlockEntity>> BRASS_FORGE =
            BLOCK_ENTITIES.register("brass_forge",
                    () -> BlockEntityType.Builder.of(BrassForgeBlockEntity::new, ModBlocks.BRASS_FORGE.get()).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
