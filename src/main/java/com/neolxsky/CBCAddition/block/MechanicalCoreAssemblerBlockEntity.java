package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.MechanicalCoreAssemblerMenu;
import com.neolxsky.CBCAddition.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 机械核心装配机：两套无序配方。
 *
 * <ul>
 *   <li>1 安山机壳 + 1 大齿轮 → 3 机械核心</li>
 *   <li>1 工业镀铁块 + 1 大型工业齿轮 → 10 机械核心</li>
 * </ul>
 */
public class MechanicalCoreAssemblerBlockEntity extends AbstractForgeBlockEntity {

    public static final int SLOT_INPUT_A = SLOT_A;
    public static final int SLOT_INPUT_B = SLOT_B;

    public static final int CORE_PER_ANDESITE = 3;
    public static final int CORE_PER_INDUSTRIAL = 10;

    public MechanicalCoreAssemblerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MECHANICAL_CORE_ASSEMBLER.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, MechanicalCoreAssemblerBlockEntity forge) {
        forge.tickServer();
    }

    @Override
    protected boolean isIngredient(int slot, Item item) {
        return (slot == SLOT_A || slot == SLOT_B) && isIngredientItem(item);
    }

    @Override
    protected @Nullable RecipeResult matchRecipe() {
        Item a = getStack(SLOT_A).getItem();
        Item b = getStack(SLOT_B).getItem();
        if (a == Items.AIR || b == Items.AIR) {
            return null;
        }
        if (isPair(a, b, item("create", "andesite_casing"), item("create", "large_cogwheel"))) {
            return new RecipeResult(1, 1, CORE_PER_ANDESITE);
        }
        if (isPair(a, b, item("design_decor", "industrial_plating_block"),
                item("design_decor", "industrial_gear_large"))) {
            return new RecipeResult(1, 1, CORE_PER_INDUSTRIAL);
        }
        return null;
    }

    @Override
    protected Item outputItem() {
        return item("kubejs", "mechanical_core");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.mechanical_core_assembler");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MechanicalCoreAssemblerMenu(id, playerInventory, this);
    }

    // ------------------------------------------------------------------
    // 配方辅助
    // ------------------------------------------------------------------

    private static Item item(String namespace, String path) {
        return BuiltInRegistries.ITEM.get(new ResourceLocation(namespace, path));
    }

    private static boolean isIngredientItem(Item item) {
        return item == item("create", "andesite_casing")
                || item == item("create", "large_cogwheel")
                || item == item("design_decor", "industrial_plating_block")
                || item == item("design_decor", "industrial_gear_large");
    }

    private static boolean isPair(Item a, Item b, Item x, Item y) {
        if (x == Items.AIR || y == Items.AIR) {
            return false;
        }
        return (a == x && b == y) || (a == y && b == x);
    }
}
