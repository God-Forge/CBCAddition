package com.neolxsky.CBCAddition.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

/**
 * 三台机器的公共方块基类：朝向属性、放置朝向、右键 GUI 与快捷交互。
 * 子类只需提供方块实体创建与 ticker。
 */
public abstract class AbstractForgeBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    protected AbstractForgeBlock(MapColor mapColor) {
        super(BlockBehaviour.Properties.of()
                .mapColor(mapColor)
                .strength(3.5F)
                .requiresCorrectToolForDrops());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AbstractForgeBlockEntity machine) {
                // 快速交互：潜行取产物 / 手持原料直接投料；未消费再打开 GUI
                if (machine.interact(player, player.getItemInHand(hand))) {
                    return InteractionResult.CONSUME;
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, machine, buf -> buf.writeBlockPos(pos));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
