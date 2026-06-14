package com.m6jyu4.rome.block;

import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class ManorBlockEntity extends BlockEntity {

    public ManorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MANOR_BE.get(), pos, state);
    }
}
