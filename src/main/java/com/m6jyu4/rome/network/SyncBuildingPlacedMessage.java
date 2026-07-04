package com.m6jyu4.rome.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record SyncBuildingPlacedMessage(BlockPos pos, ItemStack icon, boolean isOwned, int color, BlockPos ownerForumPos, boolean allowsAnnexation) implements CustomPacketPayload {
    public static final Type<SyncBuildingPlacedMessage> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "sync_building_placed"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncBuildingPlacedMessage> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, SyncBuildingPlacedMessage::pos,
            ItemStack.OPTIONAL_STREAM_CODEC, SyncBuildingPlacedMessage::icon,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, SyncBuildingPlacedMessage::isOwned,
            net.minecraft.network.codec.ByteBufCodecs.INT, SyncBuildingPlacedMessage::color,
            BlockPos.STREAM_CODEC, SyncBuildingPlacedMessage::ownerForumPos,
            net.minecraft.network.codec.ByteBufCodecs.BOOL, SyncBuildingPlacedMessage::allowsAnnexation,
            SyncBuildingPlacedMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(SyncBuildingPlacedMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.screen instanceof com.m6jyu4.rome.gui.forum.ForumScreen forumScreen) {
                forumScreen.addBuildingLive(new com.m6jyu4.rome.gui.forum.ForumMenu.BuildingEntry(
                        payload.pos(), payload.icon(), payload.isOwned(), payload.color(), payload.ownerForumPos(), payload.allowsAnnexation()
                ));
            }
        });
    }
}
