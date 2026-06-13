package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.item.HammerItem;
import com.m6jyu4.rome.Constant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItem {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Constant.MOD_ID);

    ///------------------------------///
    public static final DeferredItem<Item> HAMMER = ITEMS.register("hammer",
            () -> new HammerItem((new Item.Properties()).rarity(Rarity.RARE).stacksTo(1)));

//    public static final DeferredItem<Item> GOLDEN_BEETROOT = ITEMS.register("golden_beetroot",
//            () -> new Item((new Item.Properties()).rarity(Rarity.RARE).food(Foods.GOLDEN_APPLE)));
//    public static final DeferredItem<Item> CHORUS_BEETROOT_SEEDS = ITEMS.register("chorus_beetroot_seeds",
//            () -> new Item((new Item.Properties())));
//    public static final DeferredItem<Item> CHORUS_BEETROOT = ITEMS.register("chorus_beetroot",
//            () -> new TestItem(new Item.Properties().food(ModFood.CHORUS_BEETROOT)));
    ///------------------------------///

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}