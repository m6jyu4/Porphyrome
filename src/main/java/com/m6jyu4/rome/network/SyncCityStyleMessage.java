package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncCityStyleMessage(BlockPos forumPos, String newName, int color) implements CustomPacketPayload {
    public static final Type<SyncCityStyleMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "sync_city_style"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCityStyleMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncCityStyleMessage::forumPos,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, SyncCityStyleMessage::newName,
            net.minecraft.network.codec.ByteBufCodecs.INT, SyncCityStyleMessage::color,
            SyncCityStyleMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncCityStyleMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // This runs on the Client!
            // We check if the player currently has the Forum screen open.
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.m6jyu4.rome.gui.forum.ForumScreen forumScreen) {
                // Tell the screen to instantly repaint the map with the new colors!
                forumScreen.updateForeignCityStyle(payload.forumPos(), payload.newName(), payload.color());
            }
        });
    }
}
