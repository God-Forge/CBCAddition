package com.neolxsky.CBCAddition.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.function.Consumer;

/**
 * 简化配方生成：铁块 4 锭、铁粒 1 锭 9 粒。
 */
public class ModRecipeProvider extends RecipeProvider {

    public ModRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.IRON_BLOCK)
                .requires(Items.IRON_INGOT, 4)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(consumer, "cbcaddition:iron_block_from_4_ingots");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.IRON_NUGGET, 9)
                .requires(Items.IRON_INGOT)
                .unlockedBy("has_iron_ingot", has(Items.IRON_INGOT))
                .save(consumer, "cbcaddition:iron_nugget_from_ingot");
    }
}
