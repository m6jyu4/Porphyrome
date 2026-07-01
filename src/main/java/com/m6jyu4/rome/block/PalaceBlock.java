package com.m6jyu4.rome.block;

import com.m6jyu4.rome.blockentity.PalaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class PalaceBlock extends ConstructableBlock {

    public PalaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new PalaceBlockEntity(blockPos, blockState);
    }


}
