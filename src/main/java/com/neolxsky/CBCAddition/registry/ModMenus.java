package com.neolxsky.CBCAddition.registry;

import com.neolxsky.CBCAddition.CBCAddition;
import com.neolxsky.CBCAddition.menu.AndesiteForgeMenu;
import com.neolxsky.CBCAddition.menu.BrassForgeMenu;
import com.neolxsky.CBCAddition.menu.MechanicalCoreAssemblerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 菜单（GUI 容器类型）注册表。
 */
public final class ModMenus {

    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, CBCAddition.MODID);

    public static final RegistryObject<MenuType<AndesiteForgeMenu>> ANDESITE_FORGE =
            MENUS.register("andesite_forge",
                    () -> IForgeMenuType.create(AndesiteForgeMenu::new));

    public static final RegistryObject<MenuType<BrassForgeMenu>> BRASS_FORGE =
            MENUS.register("brass_forge",
                    () -> IForgeMenuType.create(BrassForgeMenu::new));

    public static final RegistryObject<MenuType<MechanicalCoreAssemblerMenu>> MECHANICAL_CORE_ASSEMBLER =
            MENUS.register("mechanical_core_assembler",
                    () -> IForgeMenuType.create(MechanicalCoreAssemblerMenu::new));

    private ModMenus() {
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
