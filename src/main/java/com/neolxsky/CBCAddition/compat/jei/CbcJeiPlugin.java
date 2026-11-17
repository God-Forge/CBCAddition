package com.neolxsky.CBCAddition.compat.jei;

import com.mojang.logging.LogUtils;
import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.block.AndesiteForgeBlockEntity;
import com.neolxsky.CBCAddition.block.BrassForgeBlockEntity;
import com.neolxsky.CBCAddition.registry.ModBlocks;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * JEI 插件：展示三台机器的配方。
 *
 * <p>机器逻辑在各自方块实体里（每 20 tick 合成一次），这里按同样的物品 ID / 常量
 * 构造只读配方供 JEI 查询；对应模组未安装时该配方不注册。</p>
 */
@JeiPlugin
public class CbcJeiPlugin implements IModPlugin {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static final RecipeType<ForgeRecipe> ANDESITE_FORGE =
            RecipeType.create(CBCAddition.MODID, "andesite_forge", ForgeRecipe.class);

    public static final RecipeType<ForgeRecipe> BRASS_FORGE =
            RecipeType.create(CBCAddition.MODID, "brass_forge", ForgeRecipe.class);

    public static final RecipeType<ForgeRecipe> MECHANICAL_CORE_ASSEMBLER =
            RecipeType.create(CBCAddition.MODID, "mechanical_core_assembler", ForgeRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(CBCAddition.MODID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper guiHelper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
                new ForgeRecipeCategory(guiHelper, ANDESITE_FORGE,
                        Component.translatable("block.cbcaddition.andesite_forge"),
                        new ItemStack(ModBlocks.ANDESITE_FORGE.get())),
                new ForgeRecipeCategory(guiHelper, BRASS_FORGE,
                        Component.translatable("block.cbcaddition.brass_forge"),
                        new ItemStack(ModBlocks.BRASS_FORGE.get())),
                new ForgeRecipeCategory(guiHelper, MECHANICAL_CORE_ASSEMBLER,
                        Component.translatable("block.cbcaddition.mechanical_core_assembler"),
                        new ItemStack(ModBlocks.MECHANICAL_CORE_ASSEMBLER.get())));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Item andesiteAlloy = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "andesite_alloy"));
        if (andesiteAlloy != Items.AIR) {
            registration.addRecipes(ANDESITE_FORGE, List.of(new ForgeRecipe(
                    new ItemStack(Items.IRON_INGOT, 1),
                    new ItemStack(Items.ANDESITE, AndesiteForgeBlockEntity.ANDESITE_COST),
                    new ItemStack(andesiteAlloy, AndesiteForgeBlockEntity.OUTPUT_AMOUNT))));
        }

        Item brassIngot = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "brass_ingot"));
        if (brassIngot != Items.AIR) {
            registration.addRecipes(BRASS_FORGE, List.of(new ForgeRecipe(
                    new ItemStack(Items.COPPER_INGOT, BrassForgeBlockEntity.COPPER_COST),
                    new ItemStack(Items.GOLD_INGOT, BrassForgeBlockEntity.GOLD_COST),
                    new ItemStack(brassIngot, BrassForgeBlockEntity.OUTPUT_AMOUNT))));
        }

        // 机械核心装配机：两套无序配方
        Item core = BuiltInRegistries.ITEM.get(new ResourceLocation("kubejs", "mechanical_core"));
        Item andesiteCasing = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "andesite_casing"));
        Item largeCogwheel = BuiltInRegistries.ITEM.get(new ResourceLocation("create", "large_cogwheel"));
        Item industrialPlating = BuiltInRegistries.ITEM.get(new ResourceLocation("design_decor", "industrial_plating_block"));
        Item largeIndustrialGear = BuiltInRegistries.ITEM.get(new ResourceLocation("design_decor", "industrial_gear_large"));

        List<ForgeRecipe> coreRecipes = new ArrayList<>();
        if (core != Items.AIR) {
            if (andesiteCasing != Items.AIR && largeCogwheel != Items.AIR) {
                coreRecipes.add(new ForgeRecipe(
                        new ItemStack(andesiteCasing, 1),
                        new ItemStack(largeCogwheel, 1),
                        new ItemStack(core, 3)));
            }
            if (industrialPlating != Items.AIR && largeIndustrialGear != Items.AIR) {
                coreRecipes.add(new ForgeRecipe(
                        new ItemStack(industrialPlating, 1),
                        new ItemStack(largeIndustrialGear, 1),
                        new ItemStack(core, 10)));
            }
        }
        if (!coreRecipes.isEmpty()) {
            registration.addRecipes(MECHANICAL_CORE_ASSEMBLER, coreRecipes);
        }

        LOGGER.info("CBCAddition JEI 插件已注册：安山合金炉 = {}，黄铜合金炉 = {}，机械核心装配机 = {}",
                andesiteAlloy != Items.AIR, brassIngot != Items.AIR, !coreRecipes.isEmpty());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.ANDESITE_FORGE.get()), ANDESITE_FORGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.BRASS_FORGE.get()), BRASS_FORGE);
        registration.addRecipeCatalyst(new ItemStack(ModBlocks.MECHANICAL_CORE_ASSEMBLER.get()), MECHANICAL_CORE_ASSEMBLER);
    }
}
