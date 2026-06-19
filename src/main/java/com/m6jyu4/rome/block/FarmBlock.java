package com.m6jyu4.rome.block;

import com.m6jyu4.rome.blockentity.FarmBlockEntity;
import com.m6jyu4.rome.capability.CropType;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class FarmBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<CropType> CROP_TYPE = EnumProperty.create("crop_type", CropType.class);

    public FarmBlock(Properties properties) { super(properties); }
    @Override public MapCodec<? extends BaseEntityBlock> codec() { return simpleCodec(FarmBlock::new); }
    @Override public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new FarmBlockEntity(pos, state); }
    @Override public void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING, CROP_TYPE); }

    @Override
    public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            if (level.getBlockEntity(pos) instanceof FarmBlockEntity farm && farm.getCropType() == CropType.EMPTY) {
                CropType type = CropType.CROP_MAPPINGS.get(player.getItemInHand(hand).getItem());
                if (type != null) {
                    farm.setCropType(type);
                    if (!player.hasInfiniteMaterials()) player.getItemInHand(hand).shrink(1);
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moved) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            if (level.getBlockEntity(pos) instanceof FarmBlockEntity farm) farm.dismantle();
        }
        super.onRemove(state, level, pos, newState, moved);
    }

}
