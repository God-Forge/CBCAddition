package com.neolxsky.CBCAddition.compat.jei;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

/**
 * JEI 配方分类：借用原版熔炉 GUI 的输入 / 产物槽区域作为背景。
 */
public class ForgeRecipeCategory implements IRecipeCategory<ForgeRecipe> {

    /** 原版熔炉 GUI 贴图，截取输入槽与产物槽之间的区域。 */
    private static final ResourceLocation BACKGROUND =
            new ResourceLocation("minecraft", "textures/gui/container/furnace.png");

    private static final int BACKGROUND_X = 54;
    private static final int BACKGROUND_Y = 14;
    private static final int BACKGROUND_WIDTH = 82;
    private static final int BACKGROUND_HEIGHT = 58;

    private final RecipeType<ForgeRecipe> recipeType;
    private final Component title;
    private final IDrawable background;
    private final IDrawable icon;

    public ForgeRecipeCategory(IGuiHelper guiHelper, RecipeType<ForgeRecipe> recipeType,
                               Component title, ItemStack catalyst) {
        this.recipeType = recipeType;
        this.title = title;
        this.background = guiHelper.createDrawable(BACKGROUND,
                BACKGROUND_X, BACKGROUND_Y, BACKGROUND_WIDTH, BACKGROUND_HEIGHT);
        this.icon = guiHelper.createDrawableItemStack(catalyst);
    }

    @Override
    public RecipeType<ForgeRecipe> getRecipeType() {
        return recipeType;
    }

    @Override
    public Component getTitle() {
        return title;
    }

    @Override
    public IDrawable getBackground() {
        return background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ForgeRecipe recipe, IFocusGroup focuses) {
        // 槽位坐标相对背景左上角：输入 (2,3) / 输入 (2,39) / 产物 (62,17)
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 3).addItemStack(recipe.first());
        builder.addSlot(RecipeIngredientRole.INPUT, 2, 39).addItemStack(recipe.second());
        builder.addSlot(RecipeIngredientRole.OUTPUT, 62, 17).addItemStack(recipe.output());
    }

    @Override
    public void draw(ForgeRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics,
                     double mouseX, double mouseY) {
        // 槽位外观由背景贴图负责，正文无需额外绘制
    }
}
