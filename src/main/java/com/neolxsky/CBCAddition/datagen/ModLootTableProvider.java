package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Set;

/**
 * 方块掉落表生成：保证生存模式破坏机器能掉落本体。
 */
public class ModLootTableProvider extends LootTableProvider {

    public ModLootTableProvider(PackOutput output) {
        super(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(ModBlockLoot::new, LootContextParamSets.BLOCK)));
    }

    private static class ModBlockLoot extends BlockLootSubProvider {

        protected ModBlockLoot() {
            super(Set.of(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        protected void generate() {
            dropSelf(ModBlocks.ANDESITE_FORGE.get());
            dropSelf(ModBlocks.BRASS_FORGE.get());
            dropSelf(ModBlocks.MECHANICAL_CORE_ASSEMBLER.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            // 只校验本模组的方块，否则原版方块（如 stone）也会被要求提供掉落表
            return List.of(ModBlocks.ANDESITE_FORGE.get(), ModBlocks.BRASS_FORGE.get(),
                    ModBlocks.MECHANICAL_CORE_ASSEMBLER.get());
        }
    }
}
