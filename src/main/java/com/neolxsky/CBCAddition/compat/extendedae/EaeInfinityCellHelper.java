package com.neolxsky.CBCAddition.compat.extendedae;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.lang.reflect.Method;

/**
 * 通过反射对接 ExtendedAE（expatternprovider）的无限元件。
 *
 * <p>不硬依赖 AE2 / ExtendedAE：未安装时返回空栈，物品本身仍可正常使用。</p>
 */
public final class EaeInfinityCellHelper {

    private static final String INFINITY_CELL_ID = "expatternprovider:infinity_cell";
    private static final String INFINITY_CELL_CLASS = "com.glodblock.github.extendedae.common.items.InfinityCell";
    private static final String AE_KEY_CLASS = "appeng.api.stacks.AEKey";
    private static final String AE_ITEM_KEY_CLASS = "appeng.api.stacks.AEItemKey";
    private static final String AE_FLUID_KEY_CLASS = "appeng.api.stacks.AEFluidKey";

    private EaeInfinityCellHelper() {
    }

    /**
     * 根据样品（普通物品或流体容器）生成对应的无限元件。
     *
     * @return 生成的无限元件；缺少依赖或样品无法解析时返回 {@link ItemStack#EMPTY}
     */
    public static ItemStack createInfinityCell(ItemStack sample) {
        if (sample.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Item cell = BuiltInRegistries.ITEM.get(new ResourceLocation(INFINITY_CELL_ID));
        if (cell == Items.AIR) {
            return ItemStack.EMPTY;
        }
        try {
            Class<?> cellClass = Class.forName(INFINITY_CELL_CLASS);
            if (!cellClass.isInstance(cell)) {
                return ItemStack.EMPTY;
            }
            Object key = resolveKey(sample);
            if (key == null) {
                return ItemStack.EMPTY;
            }
            Method getRecordCell = cellClass.getMethod("getRecordCell", Class.forName(AE_KEY_CLASS));
            Object result = getRecordCell.invoke(cell, key);
            return result instanceof ItemStack stack ? stack : ItemStack.EMPTY;
        } catch (Throwable ignored) {
            return ItemStack.EMPTY;
        }
    }

    private static Object resolveKey(ItemStack sample) {
        try {
            FluidStack fluid = FluidUtil.getFluidContained(sample).orElse(FluidStack.EMPTY);
            if (!fluid.isEmpty()) {
                Class<?> fluidKeyClass = Class.forName(AE_FLUID_KEY_CLASS);
                Method of = fluidKeyClass.getMethod("of", FluidStack.class);
                return of.invoke(null, fluid);
            }
            Class<?> itemKeyClass = Class.forName(AE_ITEM_KEY_CLASS);
            Method of = itemKeyClass.getMethod("of", ItemStack.class);
            return of.invoke(null, sample.copyWithCount(1));
        } catch (Throwable ignored) {
            return null;
        }
    }

    /** 当前环境是否具备制造无限元件的能力（AE2 + ExtendedAE 都在）。 */
    public static boolean isAvailable() {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(INFINITY_CELL_ID)) != Items.AIR;
    }
}
