package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record AnnexBuildingMessage(BlockPos buildingPos, BlockPos annexingForumPos) implements CustomPacketPayload {
    public static final Type<AnnexBuildingMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "annex_building"));

    public static final StreamCodec<RegistryFriendlyByteBuf, AnnexBuildingMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, AnnexBuildingMessage::buildingPos,
            BlockPos.STREAM_CODEC, AnnexBuildingMessage::annexingForumPos,
            AnnexBuildingMessage::new
    );

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(AnnexBuildingMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof net.minecraft.server.level.ServerPlayer player) {
                com.m6jyu4.rome.city.CityManager manager = com.m6jyu4.rome.city.CityManager.get(player.serverLevel());
                com.m6jyu4.rome.city.City oldCity = manager.getCityOwningBuilding(payload.buildingPos());
                com.m6jyu4.rome.city.City newCity = manager.getCityAt(payload.annexingForumPos());

                // 1. SECURITY CHECK: Ensure the building is actually within the annexing city's true radius!
                if (newCity != null && newCity.isInside(payload.buildingPos())) {

                    // 2. DIPLOMACY CHECK: Is it unowned? Or if owned, are their borders open?
                    boolean canAnnex = (oldCity == null) || oldCity.isAllowAnnex();

                    if (canAnnex) {
                        if (oldCity != null) {
                            oldCity.removeBuilding(payload.buildingPos());
                        }

                        newCity.addBuilding(payload.buildingPos());
                        manager.setDirty();

                        // Broadcast the live transfer
                        net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                                new SyncBuildingOwnerMessage(payload.buildingPos(), newCity.getForumPos(), newCity.getColour())
                        );
                    }
                }
            }
        });
    }
}
