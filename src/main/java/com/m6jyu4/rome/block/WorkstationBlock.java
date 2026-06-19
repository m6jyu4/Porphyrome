package com.m6jyu4.rome.block;

import com.m6jyu4.rome.blockentity.WorkstationBlockEntity;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public class WorkstationBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public WorkstationBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return simpleCodec(WorkstationBlock::new);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WorkstationBlockEntity(pos, state);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof WorkstationBlockEntity blockEntity) {
                ((ServerPlayer) player).openMenu(blockEntity, pos);
            }
        }
        return InteractionResult.SUCCESS;
    }

    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return switch ((Direction) pState.getValue(FACING)) {
            case NORTH, SOUTH -> SHAPE1;
            case EAST, WEST -> SHAPE0;
            default -> SHAPE1;
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @NotNull
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirrorIn) {
        return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
    }

    /* SHAPE */

    protected static final VoxelShape TABLE0 = Block.box(2.0, 5.0, 0.0, 14.0, 8.0, 16.0);
    protected static final VoxelShape TABLE1 = Block.box(0.0, 5.0, 2.0, 16.0, 8.0, 14.0);

    protected static final VoxelShape LEG00 = Block.box(2.0, 0.0, 2.0, 4, 5.0, 5);
    protected static final VoxelShape LEG01 = Block.box(2.0, 0.0, 11, 4, 5.0, 14);
    protected static final VoxelShape LEG02 = Block.box(12, 0.0, 2.0, 14, 5.0, 5);
    protected static final VoxelShape LEG03 = Block.box(12, 0.0, 11, 14, 5.0, 14);

    protected static final VoxelShape LEG10 = Block.box(2.0, 0.0, 2.0, 5.0, 5.0, 4.0);
    protected static final VoxelShape LEG11 = Block.box(2.0, 0.0, 12, 5.0, 5.0, 14);
    protected static final VoxelShape LEG12 = Block.box(11.0, 0.0, 2.0, 14.0, 5.0, 4.0);
    protected static final VoxelShape LEG13 = Block.box(11.0, 0.0, 12, 14.0, 5.0, 14);

    protected static final VoxelShape LEGS0 = Shapes.or(LEG00, LEG01, LEG02, LEG03);
    protected static final VoxelShape LEGS1 = Shapes.or(LEG10, LEG11, LEG12, LEG13);
    protected static final VoxelShape SHAPE0 = Shapes.or(LEGS0, TABLE0);
    protected static final VoxelShape SHAPE1 = Shapes.or(LEGS1, TABLE1);
}
