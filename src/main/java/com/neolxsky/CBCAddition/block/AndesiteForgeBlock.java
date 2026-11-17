package com.neolxsky.CBCAddition.block;

import com.neolxsky.CBCAddition.registry.ModBlockEntities;
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
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
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
 * 安山合金炉 —— 熔炉外观（上下两面为安山合金块材质）的自动化机器。
 *
 * <p>放入 1 个铁锭 + 9 个安山岩，自动产出 10 个安山合金。</p>
 * <p>右键打开 GUI：三槽位、熔炉风格、六面 IO 可配置、输出面自动弹出产物。</p>
 */
public class AndesiteForgeBlock extends BaseEntityBlock {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public AndesiteForgeBlock() {
        super(BlockBehaviour.Properties.of()
                .mapColor(MapColor.STONE)
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AndesiteForgeBlockEntity(pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.ANDESITE_FORGE.get(), AndesiteForgeBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AndesiteForgeBlockEntity forge) {
                // 快速交互：潜行取产物 / 手持对应原料直接投料；未消费再打开 GUI
                if (forge.interact(player, player.getItemInHand(hand))) {
                    return InteractionResult.CONSUME;
                }
                if (player instanceof ServerPlayer serverPlayer) {
                    NetworkHooks.openScreen(serverPlayer, forge, buf -> buf.writeBlockPos(pos));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
