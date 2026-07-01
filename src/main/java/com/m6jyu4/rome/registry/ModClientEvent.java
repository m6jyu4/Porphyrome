package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.block.temp.MimicGeometryLoader;
import com.m6jyu4.rome.gui.Workstation.WorkstationScreen;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModClientEvent {
    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuType.WORKSTATION_MENU.get(), WorkstationScreen::new);
    }
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(
                ResourceLocation.fromNamespaceAndPath("rome", "mimic_loader"),
                MimicGeometryLoader.INSTANCE
        );
    }
}