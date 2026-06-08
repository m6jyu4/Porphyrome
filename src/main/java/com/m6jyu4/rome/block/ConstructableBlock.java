package com.m6jyu4.rome.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.piston.MovingPistonBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class ConstructableBlock extends Block {
    private static final int MIN_CONSTRUCTION = 0;
    private static final int MAX_CONSTRUCTION = 3;
    public static final IntegerProperty CONSTRUCTION_PROGRESS = IntegerProperty.create("construction_progress", MIN_CONSTRUCTION, MAX_CONSTRUCTION);

    public ConstructableBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CONSTRUCTION_PROGRESS, MIN_CONSTRUCTION));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CONSTRUCTION_PROGRESS);
    }

    public boolean onHammerSwing(ServerLevel level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return false;

        int constructionProcess = state.getValue(CONSTRUCTION_PROGRESS);
        if (constructionProcess >= MAX_CONSTRUCTION) return false;

        increaseConstructionProgress(level, pos, state, constructionProcess);
        playConstructionParticle(level, pos);

        return true;
    }

    private void playConstructionParticle(ServerLevel level, BlockPos pos){
        level.sendParticles(ParticleTypes.FLASH,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                1, 0, 0.5, 0, 0);
    }

    private void increaseConstructionProgress(ServerLevel level, BlockPos pos, BlockState state, int constructionProcess){
        level.setBlock(pos, state.setValue(CONSTRUCTION_PROGRESS, constructionProcess + 1), 3);
    }

    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !this.canSurvive(state, level, pos) ? Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }
}
