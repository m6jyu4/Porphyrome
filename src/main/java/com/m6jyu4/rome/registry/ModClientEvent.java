package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.gui.WorkstationScreen;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ModClientEvent {
    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuType.WORKSTATION_MENU.get(), WorkstationScreen::new);
    }
}