package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.recipe.WorkstationRecipeMessage;
import com.m6jyu4.rome.util.Constant;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworking {
    public void registerPayloads(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(Constant.MOD_ID);
        registrar.playToServer(
                WorkstationRecipeMessage.TYPE,
                WorkstationRecipeMessage.STREAM_CODEC,
                WorkstationRecipeMessage::handle
        );
    }
}
