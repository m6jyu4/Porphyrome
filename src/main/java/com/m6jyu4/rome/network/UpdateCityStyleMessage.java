package com.m6jyu4.rome.network;

import com.m6jyu4.rome.city.City;
import com.m6jyu4.rome.city.CityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record UpdateCityStyleMessage(BlockPos forumPos, String newName, int color) implements CustomPacketPayload {
    public static final Type<UpdateCityStyleMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "update_city_style"));

    public static final StreamCodec<RegistryFriendlyByteBuf, UpdateCityStyleMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, UpdateCityStyleMessage::forumPos,
            net.minecraft.network.codec.ByteBufCodecs.STRING_UTF8, UpdateCityStyleMessage::newName,
            net.minecraft.network.codec.ByteBufCodecs.INT, UpdateCityStyleMessage::color,
            UpdateCityStyleMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(UpdateCityStyleMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.m6jyu4.rome.city.CityManager manager = com.m6jyu4.rome.city.CityManager.get(player.serverLevel());
                com.m6jyu4.rome.city.City city = manager.getCityAt(payload.forumPos());

                if (city != null && city.getForumPos().equals(payload.forumPos())) {
                    city.setName(payload.newName());
                    city.setColour(payload.color());
                    manager.setDirty(); // Save to disk

                    // --- NEW: LIVE SYNC BROADCAST ---
                    // Tell ALL online players about the color/name change instantly!
                    net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                            new SyncCityStyleMessage(payload.forumPos(), payload.newName(), payload.color())
                    );
                }
            }
        });
    }
}
