package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.BrassForgeMenu;
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
 * 黄铜合金炉：1 铜锭 + 1 金锭 → 2 黄铜锭。
 */
public class BrassForgeBlockEntity extends AbstractForgeBlockEntity {

    public static final int SLOT_COPPER = SLOT_A;
    public static final int SLOT_GOLD = SLOT_B;

    public static final int COPPER_COST = 1;
    public static final int GOLD_COST = 1;
    public static final int OUTPUT_AMOUNT = 2;

    public BrassForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BRASS_FORGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, BrassForgeBlockEntity forge) {
        forge.tickServer();
    }

    @Override
    protected boolean isIngredient(int slot, Item item) {
        return slot == SLOT_A ? item == Items.COPPER_INGOT : item == Items.GOLD_INGOT;
    }

    @Override
    protected @Nullable RecipeResult matchRecipe() {
        if (getStack(SLOT_A).is(Items.COPPER_INGOT) && getStack(SLOT_B).is(Items.GOLD_INGOT)) {
            return new RecipeResult(COPPER_COST, GOLD_COST, OUTPUT_AMOUNT);
        }
        return null;
    }

    @Override
    protected Item outputItem() {
        return BuiltInRegistries.ITEM.get(new ResourceLocation("create", "brass_ingot"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.brass_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BrassForgeMenu(id, playerInventory, this);
    }
}
