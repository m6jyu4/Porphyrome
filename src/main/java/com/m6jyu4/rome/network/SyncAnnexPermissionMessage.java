package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncAnnexPermissionMessage(BlockPos ownerForumPos, boolean allowAnnex) implements CustomPacketPayload {
    public static final Type<SyncAnnexPermissionMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "sync_annex_permission"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAnnexPermissionMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncAnnexPermissionMessage::ownerForumPos,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, SyncAnnexPermissionMessage::allowAnnex,
            SyncAnnexPermissionMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncAnnexPermissionMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            // Run on Client
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.m6jyu4.rome.gui.forum.ForumScreen forumScreen) {
                forumScreen.updateAnnexPermissionLive(payload.ownerForumPos(), payload.allowAnnex());
            }
        });
    }
}
