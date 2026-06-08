package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.block.WorkstationBlock;
import com.m6jyu4.rome.util.Constant;
import com.m6jyu4.rome.block.ConstructableBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Constant.MOD_ID);

    ///------------------------------///
    public static final DeferredBlock<Block> FARM = registerBlock(
            "farm",
            () -> new FarmBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .sound(SoundType.AMETHYST)
                            .pushReaction(PushReaction.DESTROY)
                            .requiresCorrectToolForDrops()
                            .strength(1.5F)
            )
    );
    public static final DeferredBlock<Block> TEMP = registerBlock(
            "temp",
            () -> new ConstructableBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .sound(SoundType.AMETHYST)
                            .pushReaction(PushReaction.DESTROY)
                            .requiresCorrectToolForDrops()
                            .strength(1.5F)
            )
    );
    public static final DeferredBlock<Block> WORKSTATION = registerBlock(
            "workstation",
            () -> new WorkstationBlock(
                    BlockBehaviour.Properties.of()
                            .mapColor(MapColor.COLOR_BLUE)
                            .sound(SoundType.AMETHYST)
                            .pushReaction(PushReaction.DESTROY)
                            .requiresCorrectToolForDrops()
                            .strength(1.5F)
            )
    );
//    public static final DeferredBlock<Block> NEXUS = registerBlock(
//            "nexus",
//            () -> new MineralBlock(
//                    BlockBehaviour.Properties.of()
//                            .randomTicks()
//                            .mapColor(MapColor.GOLD)
//                            .sound(SoundType.AMETHYST)
//                            .strength(1.5F)
//            )
//    );
    ///------------------------------///

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItem.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
