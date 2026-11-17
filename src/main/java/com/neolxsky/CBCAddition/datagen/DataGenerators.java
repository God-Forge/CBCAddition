package com.neolxsky.CBCAddition.datagen;

import com.neolxsky.CBCAddition.CBCAddition;
import net.minecraft.data.DataGenerator;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 数据生成入口。
 *
 * <p>运行 {@code gradlew runData} 会把配方、模型、语言文件、掉落表生成到
 * {@code src/generated/resources}，避免再手写 JSON 时漏字段。</p>
 */
@Mod.EventBusSubscriber(modid = CBCAddition.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public final class DataGenerators {

    private DataGenerators() {
    }

    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        ExistingFileHelper existingFileHelper = event.getExistingFileHelper();

        generator.addProvider(event.includeServer(), new ModRecipeProvider(generator.getPackOutput()));
        generator.addProvider(event.includeServer(), new ModLootTableProvider(generator.getPackOutput()));
        generator.addProvider(event.includeClient(), new ModBlockStateProvider(generator.getPackOutput(), existingFileHelper));
        generator.addProvider(event.includeClient(), new ModItemModelProvider(generator.getPackOutput(), existingFileHelper));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(generator.getPackOutput(), "en_us"));
        generator.addProvider(event.includeClient(), new ModLanguageProvider(generator.getPackOutput(), "zh_cn"));
    }
}
