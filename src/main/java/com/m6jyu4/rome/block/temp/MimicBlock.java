package com.m6jyu4.rome.block.temp;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

public class MimicBlock extends Block implements EntityBlock {
    public MimicBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MimicBlockEntity(pos, state);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean isMoving) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, isMoving);

        // Check if the change happened directly below our block
        boolean isBelow = neighborPos.equals(pos.below());

        System.out.println("[MimicBlock Debug] neighborChanged fired! " +
                "| ClientSide: " + level.isClientSide +
                " | Changed Pos: " + neighborPos +
                " | Is Directly Below: " + isBelow);

        if (isBelow) {
            if (level.getBlockEntity(pos) instanceof MimicBlockEntity mimicBe) {
                if (level.isClientSide) {
                    System.out.println("[MimicBlock Debug] Client requesting model data update...");
                    mimicBe.requestModelDataUpdate();
                } else {
                    System.out.println("[MimicBlock Debug] Server broadcasting sendBlockUpdated packet...");
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                }
            } else {
                System.out.println("[MimicBlock Debug] Warning: BlockEntity at " + pos + " is NOT an instance of MimicBlockEntity!");
            }
        }
    }

}
