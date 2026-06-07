package com.m6jyu4.rome;

import com.m6jyu4.rome.registry.*;
import com.m6jyu4.rome.util.Constant;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constant.MOD_ID)
public class RomeMod {

    public RomeMod(IEventBus modEventBus) {

        ModBlock.register(modEventBus);
        ModItem.register(modEventBus);
        ModBlockEntity.register(modEventBus);
        ModCreativeTab.register(modEventBus);

        modEventBus.addListener(ModCapability::registerCaps);

//        ModMenuType.register(modEventBus);
//        ModRecipe.register(modEventBus);
//        ModPayload.register(modEventBus);

    }

}
