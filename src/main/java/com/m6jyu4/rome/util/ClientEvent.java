package com.m6jyu4.rome.util;

import com.m6jyu4.rome.gui.WorkstationScreen;
import com.m6jyu4.rome.registry.ModMenuType;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public class ClientEvent {
    public void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuType.WORKSTATION_MENU.get(), WorkstationScreen::new);
    }
}