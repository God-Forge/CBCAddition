package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.registry.ModBlocks;
import com.neolxsky.CBCAddition.registry.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraftforge.common.data.LanguageProvider;

/**
 * 语言文件生成：方块名与物品名（英文 / 简体中文）。
 */
public class ModLanguageProvider extends LanguageProvider {

    private final String locale;

    public ModLanguageProvider(PackOutput output, String locale) {
        super(output, CBCAddition.MODID, locale);
        this.locale = locale;
    }

    @Override
    protected void addTranslations() {
        if ("zh_cn".equals(locale)) {
            add(ModBlocks.ANDESITE_FORGE.get(), "安山合金炉");
            add(ModBlocks.BRASS_FORGE.get(), "黄铜合金炉");
            add(ModBlocks.MECHANICAL_CORE_ASSEMBLER.get(), "机械核心装配机");
            addItem(ModItems.INFINITY_CELL_CONFIGURATOR, "无限元件配置器");
            add("message.cbcaddition.configurator.no_sample", "请先在样品槽放入物品或流体容器");
            add("message.cbcaddition.configurator.unavailable", "需要安装 AE2 与 ExtendedAE 才能制造无限元件");
            add("message.cbcaddition.configurator.output_full", "产物槽已有物品");
            add("message.cbcaddition.configurator.no_component", "需要 1 个 16k 存储组件");
        } else {
            add(ModBlocks.ANDESITE_FORGE.get(), "Andesite Alloy Forge");
            add(ModBlocks.BRASS_FORGE.get(), "Brass Alloy Forge");
            add(ModBlocks.MECHANICAL_CORE_ASSEMBLER.get(), "Mechanical Core Assembler");
            addItem(ModItems.INFINITY_CELL_CONFIGURATOR, "Infinity Cell Configurator");
            add("message.cbcaddition.configurator.no_sample", "Put an item or fluid container in the sample slot");
            add("message.cbcaddition.configurator.unavailable", "AE2 and ExtendedAE are required to craft infinity cells");
            add("message.cbcaddition.configurator.output_full", "The output slot is occupied");
            add("message.cbcaddition.configurator.no_component", "Requires one 16k storage component");
        }
    }
}
