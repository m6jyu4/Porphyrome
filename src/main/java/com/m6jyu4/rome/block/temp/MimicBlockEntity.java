package com.m6jyu4.rome.block.temp;

import com.m6jyu4.rome.registry.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

public class MimicBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockState> MIMIC_PROPERTY = new ModelProperty<>();

    public MimicBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MIMIC_BE.get(), pos, state);
    }

    @Override
    public ModelData getModelData() {
        if (level == null) {
            return ModelData.EMPTY;
        }

        // Dynamic lookup: Find the state directly below this block
        BlockState stateBelow = level.getBlockState(worldPosition.below());

        // Prevent infinite rendering loops if another mimic block is below
        if (stateBelow.getBlock() instanceof MimicBlock) {
            stateBelow = Blocks.AIR.defaultBlockState();
        }

        return ModelData.builder()
                .with(MIMIC_PROPERTY, stateBelow)
                .build();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        // Collect the block entity data to send to clients
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        // Generate a sync packet for block updates
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet, HolderLookup.Provider registries) {
        // 1. Process the incoming server packet (loads any updated tags)
        super.onDataPacket(connection, packet, registries);

        // 2. CRITICAL: Force the client rendering engine to clear its cache
        // and re-evaluate our block's geometry and texture
        if (this.level != null && this.level.isClientSide) {
            this.requestModelDataUpdate();
            this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        }
    }
}
