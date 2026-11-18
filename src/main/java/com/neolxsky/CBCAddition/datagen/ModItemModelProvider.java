package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.CBCAddition;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.ItemModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

/**
 * 物品模型生成：方块物品直接继承对应方块模型。
 */
public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, CBCAddition.MODID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        withExistingParent("andesite_forge", modLoc("block/andesite_forge"));
        withExistingParent("brass_forge", modLoc("block/brass_forge"));
        withExistingParent("mechanical_core_assembler", modLoc("block/mechanical_core_assembler"));

        getBuilder("infinity_cell_configurator")
                .parent(getExistingFile(mcLoc("item/generated")))
                .texture("layer0", modLoc("item/infinity_cell_configurator"));
    }
}
