package com.m6jyu4.rome.blockentity;

import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CathedralBlockEntity extends BlockEntity {
    public CathedralBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.CATHEDRAL_BE.get(), pos, blockState);
    }
}
