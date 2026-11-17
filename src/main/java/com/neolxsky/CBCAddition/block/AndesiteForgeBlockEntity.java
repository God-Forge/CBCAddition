package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.menu.AndesiteForgeMenu;
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
 * 安山合金炉：1 铁锭 + 9 安山岩 → 10 安山合金。
 *
 * <p>槽位、IO、合成循环与 NBT 均由 {@link AbstractForgeBlockEntity} 提供，
 * 这里只描述“什么算原料”和“配方怎么匹配”。</p>
 */
public class AndesiteForgeBlockEntity extends AbstractForgeBlockEntity {

    public static final int SLOT_IRON = SLOT_A;
    public static final int SLOT_ANDESITE = SLOT_B;

    public static final int ANDESITE_COST = 9;
    public static final int OUTPUT_AMOUNT = 10;

    public AndesiteForgeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANDESITE_FORGE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, AndesiteForgeBlockEntity forge) {
        forge.tickServer();
    }

    @Override
    protected boolean isIngredient(int slot, Item item) {
        return slot == SLOT_A ? item == Items.IRON_INGOT : item == Items.ANDESITE;
    }

    @Override
    protected @Nullable RecipeResult matchRecipe() {
        if (getStack(SLOT_A).is(Items.IRON_INGOT) && getStack(SLOT_B).is(Items.ANDESITE)) {
            return new RecipeResult(1, ANDESITE_COST, OUTPUT_AMOUNT);
        }
        return null;
    }

    @Override
    protected Item outputItem() {
        return BuiltInRegistries.ITEM.get(new ResourceLocation("create", "andesite_alloy"));
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.cbcaddition.andesite_forge");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new AndesiteForgeMenu(id, playerInventory, this);
    }
}
