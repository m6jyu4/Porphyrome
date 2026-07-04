package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBuildingRemovedMessage(BlockPos pos) implements CustomPacketPayload {
    public static final Type<SyncBuildingRemovedMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "sync_building_removed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBuildingRemovedMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncBuildingRemovedMessage::pos,
            SyncBuildingRemovedMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncBuildingRemovedMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.m6jyu4.rome.gui.forum.ForumScreen forumScreen) {
                forumScreen.removeBuildingLive(payload.pos());
            }
        });
    }
}
