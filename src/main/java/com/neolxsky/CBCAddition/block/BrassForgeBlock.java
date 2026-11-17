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
 * 黄铜合金炉方块。
 */
public class BrassForgeBlock extends AbstractForgeBlock {

    public BrassForgeBlock() {
        super(MapColor.COLOR_ORANGE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrassForgeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BRASS_FORGE.get(), BrassForgeBlockEntity::serverTick);
    }
}
