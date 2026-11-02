package com.m6jyu4.rome.network;

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
        registrar.playToServer(
                WorkstationCreativeCloneMessage.TYPE,
                WorkstationCreativeCloneMessage.STREAM_CODEC,
                WorkstationCreativeCloneMessage::handle
        );
        registrar.playToServer(
                OpenBuildingMessage.TYPE,
                OpenBuildingMessage.STREAM_CODEC,
                OpenBuildingMessage::handle
        );
        registrar.playToServer(
                RenameCityMessage.TYPE,
                RenameCityMessage.STREAM_CODEC,
                RenameCityMessage::handle
        );
        registrar.playToServer(
                UpdateCityStyleMessage.TYPE,
                UpdateCityStyleMessage.STREAM_CODEC,
                UpdateCityStyleMessage::handle
        );
        registrar.playToClient(
                SyncCityStyleMessage.TYPE,
                SyncCityStyleMessage.STREAM_CODEC,
                SyncCityStyleMessage::handle
        );
        registrar.playToServer(
                AnnexBuildingMessage.TYPE,
                AnnexBuildingMessage.STREAM_CODEC,
                AnnexBuildingMessage::handle
        );        registrar.playToServer(
                ToggleAnnexPermissionMessage.TYPE,
                ToggleAnnexPermissionMessage.STREAM_CODEC,
                ToggleAnnexPermissionMessage::handle
        );
        registrar.playToClient(
                SyncBuildingOwnerMessage.TYPE,
                SyncBuildingOwnerMessage.STREAM_CODEC,
                SyncBuildingOwnerMessage::handle
        );
        registrar.playToClient(
                SyncAnnexPermissionMessage.TYPE,
                SyncAnnexPermissionMessage.STREAM_CODEC,
                SyncAnnexPermissionMessage::handle
        );
        registrar.playToClient(
                SyncBuildingPlacedMessage.TYPE,
                SyncBuildingPlacedMessage.STREAM_CODEC,
                SyncBuildingPlacedMessage::handle
        );
        registrar.playToClient(
                SyncBuildingRemovedMessage.TYPE,
                SyncBuildingRemovedMessage.STREAM_CODEC,
                SyncBuildingRemovedMessage::handle
        );

    }

}
