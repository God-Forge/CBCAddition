package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.AndesiteForgeBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * 安山合金炉的容器菜单：A / B → C 槽位与玩家背包（布局由基类统一提供）。
 */
public class AndesiteForgeMenu extends AbstractForgeMenu {

    public AndesiteForgeMenu(int id, Inventory playerInventory, AndesiteForgeBlockEntity blockEntity) {
        super(ModMenus.ANDESITE_FORGE.get(), id, playerInventory, blockEntity);
    }

    public AndesiteForgeMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf, AndesiteForgeBlockEntity.class));
    }
}
