package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 方块状态与方块模型生成：熔炉外观 + 自家贴图。
 */
public class ModBlockStateProvider extends BlockStateProvider {

    public ModBlockStateProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CBCAddition.MODID, existingFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        horizontalBlock(ModBlocks.ANDESITE_FORGE.get(),
                models().orientableWithBottom("andesite_forge",
                        modLoc("block/furnace_side"),
                        modLoc("block/furnace_front"),
                        modLoc("block/andesite_block"),
                        modLoc("block/andesite_block")));

        horizontalBlock(ModBlocks.BRASS_FORGE.get(),
                models().orientableWithBottom("brass_forge",
                        modLoc("block/brass_casing"),
                        modLoc("block/furnace_front"),
                        modLoc("block/brass_casing"),
                        modLoc("block/brass_casing")));
    }
}
