package com.m6jyu4.rome.network;

import com.m6jyu4.rome.util.Constant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record WorkstationCreativeCloneMessage(ItemStack stack) implements CustomPacketPayload {

    public static final Type<WorkstationCreativeCloneMessage> TYPE = new Type<>(Constant.rl("creative_clone"));

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationCreativeCloneMessage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, WorkstationCreativeCloneMessage::stack,
            WorkstationCreativeCloneMessage::new
    );

    public static void handle(WorkstationCreativeCloneMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            // Security check: Only allow this if the player is actually in creative mode
            if (player != null && player.isCreative()) {
                // Set the item to the cursor (carried slot) of their currently open menu
                player.containerMenu.setCarried(payload.stack());
            }
        });
    }
}
