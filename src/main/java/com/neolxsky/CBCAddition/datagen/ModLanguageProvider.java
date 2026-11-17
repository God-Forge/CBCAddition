package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.registry.ModBlocks;
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
        } else {
            add(ModBlocks.ANDESITE_FORGE.get(), "Andesite Alloy Forge");
            add(ModBlocks.BRASS_FORGE.get(), "Brass Alloy Forge");
        }
    }
}
