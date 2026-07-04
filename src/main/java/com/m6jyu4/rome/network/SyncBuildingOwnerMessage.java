package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBuildingOwnerMessage(BlockPos buildingPos, BlockPos newOwnerForumPos, int newColor) implements CustomPacketPayload {
    public static final Type<SyncBuildingOwnerMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "sync_building_owner"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBuildingOwnerMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncBuildingOwnerMessage::buildingPos,
            BlockPos.STREAM_CODEC, SyncBuildingOwnerMessage::newOwnerForumPos,
            net.minecraft.network.codec.ByteBufCodecs.INT, SyncBuildingOwnerMessage::newColor,
            SyncBuildingOwnerMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncBuildingOwnerMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Run on Client: update open screens
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.m6jyu4.rome.gui.forum.ForumScreen forumScreen) {
                forumScreen.updateBuildingOwnerLive(payload.buildingPos(), payload.newOwnerForumPos(), payload.newColor());
            }
        });
    }
}
