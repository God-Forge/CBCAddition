package com.neolxsky.CBCAddition.compat.jei;

import net.minecraft.world.item.ItemStack;

/**
 * JEI 展示用的合金炉配方：两种原料 + 一个产物。
 *
 * @param first  第一个输入槽的物品
 * @param second 第二个输入槽的物品
 * @param output 产物槽的物品
 */
public record ForgeRecipe(ItemStack first, ItemStack second, ItemStack output) {
}
