package com.m6jyu4.rome.network;

import com.m6jyu4.rome.block.ConstructableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record OpenBuildingMessage(BlockPos pos) implements CustomPacketPayload {

    public static final Type<OpenBuildingMessage> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath("rome", "open_building"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenBuildingMessage> STREAM_CODEC =
            StreamCodec.composite(
                    BlockPos.STREAM_CODEC, OpenBuildingMessage::pos,
                    OpenBuildingMessage::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() { return TYPE; }

    public static void handle(OpenBuildingMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                BlockState state = player.serverLevel().getBlockState(payload.pos());

                // Verify the block is still there and is a building
                if (state.getBlock() instanceof ConstructableBlock) {
                    // Open the remote building's menu!
                    MenuProvider menuProvider = state.getMenuProvider(player.serverLevel(), payload.pos());
                    if (menuProvider != null) {
                        player.openMenu(menuProvider, payload.pos());
                    }
                }
            }
        });
    }
}
