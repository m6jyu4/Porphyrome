package com.m6jyu4.rome.network;

import com.m6jyu4.rome.city.City;
import com.m6jyu4.rome.city.CityManager;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record RenameCityMessage(BlockPos forumPos, String newName) implements CustomPacketPayload {

    public static final Type<RenameCityMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "rename_city"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RenameCityMessage> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, RenameCityMessage::forumPos,
                    ByteBufCodecs.STRING_UTF8, RenameCityMessage::newName,
                    RenameCityMessage::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(RenameCityMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                CityManager manager = CityManager.get(player.serverLevel());
                City city = manager.getCityAt(payload.forumPos());

                // Security check: Make sure the city exists and they are renaming the right one
                if (city != null && city.getForumPos().equals(payload.forumPos())) {
                    city.setName(payload.newName());
                    manager.setDirty(); // Save the new name to disk
                }
            }
        });
    }
}
