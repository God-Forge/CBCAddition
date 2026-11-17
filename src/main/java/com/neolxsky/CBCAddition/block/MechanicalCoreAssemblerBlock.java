package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import org.jetbrains.annotations.Nullable;

/**
 * 机械核心装配机方块。
 */
public class MechanicalCoreAssemblerBlock extends AbstractForgeBlock {

    public MechanicalCoreAssemblerBlock() {
        super(MapColor.COLOR_ORANGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MechanicalCoreAssemblerBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MECHANICAL_CORE_ASSEMBLER.get(),
                MechanicalCoreAssemblerBlockEntity::serverTick);
    }
}
