package com.m6jyu4.rome;

import com.m6jyu4.rome.city.CityBorderTracker;
import com.m6jyu4.rome.network.ModNetworking;
import com.m6jyu4.rome.registry.*;
import com.m6jyu4.rome.registry.ModClientEvent;
import com.m6jyu4.rome.util.Constant;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(Constant.MOD_ID)
public class RomeMod {

    public RomeMod(IEventBus modEventBus) {

        ModBlock.register(modEventBus);
        ModItem.register(modEventBus);
        ModBlockEntity.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        modEventBus.addListener(ModCapability::registerCaps);

        ModMenuType.MENUS.register(modEventBus);
        ModRecipe.register(modEventBus);

        if (FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(ModClientEvent::registerScreens);
            modEventBus.addListener(ModClientEvent::registerGeometryLoaders);
        }

        ModNetworking networking = new ModNetworking();
        modEventBus.addListener(networking::registerPayloads);

        NeoForge.EVENT_BUS.register(CityBorderTracker.class);
    }

}
