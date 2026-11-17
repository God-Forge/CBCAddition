package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.BrassForgeBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * 黄铜合金炉的容器菜单：A / B → C 槽位与玩家背包（布局由基类统一提供）。
 */
public class BrassForgeMenu extends AbstractForgeMenu {

    public BrassForgeMenu(int id, Inventory playerInventory, BrassForgeBlockEntity blockEntity) {
        super(ModMenus.BRASS_FORGE.get(), id, playerInventory, blockEntity);
    }

    public BrassForgeMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf, BrassForgeBlockEntity.class));
    }
}
