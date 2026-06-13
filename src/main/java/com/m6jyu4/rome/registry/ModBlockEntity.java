package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.block.WorkstationBlockEntity;
import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.block.FarmBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.MOD_ID);

    public static final Supplier<BlockEntityType<FarmBlockEntity>> FARM_BE =
            BLOCK_ENTITIES.register("farm_be", () -> BlockEntityType.Builder.of(
                    FarmBlockEntity::new, ModBlock.FARM.get()).build(null));

    public static final Supplier<BlockEntityType<WorkstationBlockEntity>> WORKSTATION_BE =
            BLOCK_ENTITIES.register("workstation_be", () -> BlockEntityType.Builder.of(
                    WorkstationBlockEntity::new, ModBlock.WORKSTATION.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

