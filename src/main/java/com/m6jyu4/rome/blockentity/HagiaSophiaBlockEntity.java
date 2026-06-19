package com.m6jyu4.rome.blockentity;

import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class HagiaSophiaBlockEntity extends BlockEntity {
    public HagiaSophiaBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.HAGIA_SOPHIA_BE.get(), pos, blockState);
    }
}
