package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record ToggleAnnexPermissionMessage(BlockPos forumPos, boolean allowAnnex) implements CustomPacketPayload {
    public static final Type<ToggleAnnexPermissionMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "toggle_annex"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ToggleAnnexPermissionMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, ToggleAnnexPermissionMessage::forumPos,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, ToggleAnnexPermissionMessage::allowAnnex,
            ToggleAnnexPermissionMessage::new
    );
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(ToggleAnnexPermissionMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.m6jyu4.rome.city.City city = com.m6jyu4.rome.city.CityManager.get(player.serverLevel()).getCityAt(payload.forumPos());

                if (city != null && player.getUUID().equals(city.getOwnerUUID())) {
                    city.setAllowAnnex(payload.allowAnnex); // (or setAllowsAnnexation depending on your exact naming)
                    com.m6jyu4.rome.city.CityManager.get(player.serverLevel()).setDirty();

                    // --- NEW: BROADCAST THE POLICY CHANGE ---
                    net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                            new SyncAnnexPermissionMessage(payload.forumPos(), payload.allowAnnex)
                    );
                }
            }
        });
    }
}
