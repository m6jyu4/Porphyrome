package com.m6jyu4.rome.network;

import com.m6jyu4.rome.Constant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.item.ItemStack;

public record WorkstationCreativeCloneMessage(ItemStack stack) implements CustomPacketPayload {

    public static final Type<WorkstationCreativeCloneMessage> TYPE = new Type<>(Constant.rl("creative_clone"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationCreativeCloneMessage> STREAM_CODEC = StreamCodec.composite(
            ItemStack.STREAM_CODEC, WorkstationCreativeCloneMessage::stack,
            WorkstationCreativeCloneMessage::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }


}
