package com.m6jyu4.rome.registry;

import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.Constant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModRecipe {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, Constant.MOD_ID);

    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, Constant.MOD_ID);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }

    // --- RECIPE ---
    public static final Supplier<RecipeSerializer<WorkstationRecipe>> WORKSTATION_SERIALIZER =
            SERIALIZERS.register("workstation", () -> new RecipeSerializer<WorkstationRecipe>() {
                @Override
                public com.mojang.serialization.MapCodec<WorkstationRecipe> codec() {
                    return WorkstationRecipe.CODEC;
                }

                @Override
                public net.minecraft.network.codec.StreamCodec<net.minecraft.network.RegistryFriendlyByteBuf, WorkstationRecipe> streamCodec() {
                    return WorkstationRecipe.STREAM_CODEC;
                }
            });

    // --- TYPE ---
    public static final Supplier<RecipeType<WorkstationRecipe>> WORKSTATION_TYPE =
            TYPES.register("workstation", () -> new RecipeType<WorkstationRecipe>() {
                @Override
                public String toString() {
                    return Constant.MOD_ID + ":workstation";
                }
            });
}
