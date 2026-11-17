package com.neolxsky.CBCAddition.menu;

import com.neolxsky.CBCAddition.block.MechanicalCoreAssemblerBlockEntity;
import com.neolxsky.CBCAddition.registry.ModMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;

/**
 * 机械核心装配机的容器菜单：A / B → C 槽位与玩家背包（布局由基类统一提供）。
 */
public class MechanicalCoreAssemblerMenu extends AbstractForgeMenu {

    public MechanicalCoreAssemblerMenu(int id, Inventory playerInventory, MechanicalCoreAssemblerBlockEntity blockEntity) {
        super(ModMenus.MECHANICAL_CORE_ASSEMBLER.get(), id, playerInventory, blockEntity);
    }

    public MechanicalCoreAssemblerMenu(int id, Inventory playerInventory, FriendlyByteBuf buf) {
        this(id, playerInventory, readBlockEntity(playerInventory, buf, MechanicalCoreAssemblerBlockEntity.class));
    }
}
