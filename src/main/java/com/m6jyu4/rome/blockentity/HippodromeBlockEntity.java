package com.m6jyu4.rome.blockentity;

import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HippodromeBlockEntity extends BlockEntity {
    public HippodromeBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.HIPPODROME_BE.get(), pos, state);
    }
}
