package com.m6jyu4.rome.block;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.capability.CropType;
import com.m6jyu4.rome.capability.INetworkNode;
import com.m6jyu4.rome.registry.ModBlockEntity;
import com.m6jyu4.rome.registry.ModCapability;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class FarmBlockEntity extends BlockEntity implements INetworkNode {
    private CropType cropType = CropType.EMPTY;
    private BlockPos parentPos = null;

    public FarmBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntity.FARM_BE.get(), pos, state); }

    public CropType getCropType() { return cropType; }

    public void setCropType(CropType type) {
        this.cropType = type;
        if (level != null) level.setBlockAndUpdate(worldPosition, getBlockState().setValue(FarmBlock.CROP_TYPE, type));
        setChanged();
        onChildUpdated();
    }

    // --- Interfaces ---
    @Override public int getTier() { return 0; }
    @Override public BlockPos getParentPos() { return parentPos; }
    @Override public void setParentPos(BlockPos pos) { this.parentPos = pos; setChanged(); }
//    @Override public int getFoodValue() { return switch (cropType) { case WHEAT, POTATO, CARROT -> 1; default -> 0; }; }
//    @Override public int getPietyValue() { return cropType == CropType.BEETROOT ? 1 : 0; }

    @Override
    public void onChildUpdated() {
        if (level != null && parentPos != null) {
            INetworkNode parent = level.getCapability(ModCapability.NETWORK_NODE, parentPos, null);
            if (parent != null) parent.onChildUpdated(); // Push update to House
        }
    }

    @Override
    public void dismantle() {
        onChildUpdated(); // Tell parent we are dying so it recalculates without us
    }

    // --- NBT ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.saveAdditional(tag, reg);
        tag.putString(Constant.MOD_ID + ".farm.crop", cropType.name());
        if (parentPos != null) tag.putLong(Constant.MOD_ID + ".farm.parent", parentPos.asLong());
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.@NotNull Provider reg) {
        super.loadAdditional(tag, reg);
        this.cropType = CropType.valueOf(tag.getString(Constant.MOD_ID + ".farm.crop"));
        this.parentPos = tag.contains(Constant.MOD_ID + ".farm.parent") ? BlockPos.of(tag.getLong(Constant.MOD_ID + ".farm.parent")) : null;
    }
}

