package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.network.OpenBuildingMessage;
import com.m6jyu4.rome.network.WorkstationCreativeCloneMessage;
import com.m6jyu4.rome.network.WorkstationRecipeMessage;
import com.m6jyu4.rome.Constant;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constant.MOD_ID);
        registrar.playToServer(
                WorkstationRecipeMessage.TYPE,
                WorkstationRecipeMessage.STREAM_CODEC,
                WorkstationRecipeMessage::handle
        );
        registrar.playToServer(
                WorkstationCreativeCloneMessage.TYPE,
                WorkstationCreativeCloneMessage.STREAM_CODEC,
                ModNetworking::handleCreativePick
        );
        registrar.playToServer(
                OpenBuildingMessage.TYPE,
                OpenBuildingMessage.STREAM_CODEC,
                OpenBuildingMessage::handle
        );
    }
    public static void handleCreativePick(WorkstationCreativeCloneMessage payload, IPayloadContext context) {
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
