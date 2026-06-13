package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.Constant;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TAB = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Constant.MOD_ID);

    public static final Supplier<CreativeModeTab> ROME_TAB = CREATIVE_MODE_TAB.register("rome_tab",
            () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItem.HAMMER.get()))
                    .title(Component.translatable("tab." + Constant.MOD_ID))
                    .displayItems((itemDisplayParameters, output) -> {

                        ///------------------------------///
                        output.accept(Items.BEETROOT_SEEDS);
                        output.accept(Items.BEETROOT);
                        output.accept(Items.BEETROOT_SOUP);
                        output.accept(ModBlock.TEMP.get());
                        output.accept(ModItem.HAMMER.get());
                        output.accept(ModBlock.WORKSTATION.get());
                        ///------------------------------///

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TAB.register(eventBus);
    }

}
