package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.block.temp.MimicBlockEntity;
import com.m6jyu4.rome.blockentity.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Constant.MOD_ID);

    public static final Supplier<BlockEntityType<MimicBlockEntity>> MIMIC_BE =
            BLOCK_ENTITIES.register("mimic_be", () -> BlockEntityType.Builder.of(
                    MimicBlockEntity::new, ModBlock.MIMIC.get()).build(null));

    public static final Supplier<BlockEntityType<WorkstationBlockEntity>> WORKSTATION_BE =
            BLOCK_ENTITIES.register("workstation_be", () -> BlockEntityType.Builder.of(
                    WorkstationBlockEntity::new, ModBlock.WORKSTATION.get()).build(null));

    public static final Supplier<BlockEntityType<ForumBlockEntity>> FORUM_BE =
            BLOCK_ENTITIES.register("forum_be", () -> BlockEntityType.Builder.of(
                    ForumBlockEntity::new, ModBlock.FORUM.get()).build(null));

    public static final Supplier<BlockEntityType<PalaceBlockEntity>> PALACE_BE =
            BLOCK_ENTITIES.register("palace_be", () -> BlockEntityType.Builder.of(
                    PalaceBlockEntity::new, ModBlock.PALACE.get()).build(null));

    public static final Supplier<BlockEntityType<ManorBlockEntity>> MANOR_BE =
            BLOCK_ENTITIES.register("manor_be", () -> BlockEntityType.Builder.of(
                    ManorBlockEntity::new, ModBlock.MANOR.get()).build(null));

    public static final Supplier<BlockEntityType<HippodromeBlockEntity>> HIPPODROME_BE =
            BLOCK_ENTITIES.register("hippodrome_be", () -> BlockEntityType.Builder.of(
                    HippodromeBlockEntity::new, ModBlock.HIPPODROME.get()).build(null));

    public static final Supplier<BlockEntityType<CathedralBlockEntity>> CATHEDRAL_BE =
            BLOCK_ENTITIES.register("hagia_sophia_be", () -> BlockEntityType.Builder.of(
                    CathedralBlockEntity::new, ModBlock.CATHEDRAL.get()).build(null));

    public static final Supplier<BlockEntityType<FarmBlockEntity>> FARM_BE =
            BLOCK_ENTITIES.register("farm_be", () -> BlockEntityType.Builder.of(
                    FarmBlockEntity::new, ModBlock.FARM.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}

