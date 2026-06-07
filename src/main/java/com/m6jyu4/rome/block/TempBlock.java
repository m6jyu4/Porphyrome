package com.m6jyu4.rome.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class TempBlock extends Block {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public TempBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, false));
    }

    // Direct method called by the hammer
    public void onHammerSwing(ServerLevel level, BlockPos pos, BlockState state, Player player) {
        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());

        if (distance > 16) return; // 16 blocks squared

        // YOUR BLOCK LOGIC HERE
        level.setBlock(pos, state.setValue(POWERED, true), 3);

        // Spawn particles at the block
        level.sendParticles(ParticleTypes.FLASH,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                1, 0, 0.5, 0, 0);

        // Play sound at block
        level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.0F, 1.0F);

        // Schedule reset after 2 seconds
        level.scheduleTick(pos, this, 40);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(POWERED, false), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}
