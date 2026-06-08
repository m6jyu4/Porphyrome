package com.m6jyu4.rome.recipe;

import com.m6jyu4.rome.registry.ModRecipe;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

public record WorkstationRecipe(List<SizedIngredient> sizedIngredients, ItemStack output) implements Recipe<RecipeInput> {

    public static final MapCodec<WorkstationRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.listOf().fieldOf("ingredients").forGetter(WorkstationRecipe::sizedIngredients),
            ItemStack.CODEC.fieldOf("result").forGetter(WorkstationRecipe::output)
    ).apply(inst, WorkstationRecipe::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipe> STREAM_CODEC = StreamCodec.composite(
            SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), WorkstationRecipe::sizedIngredients,
            ItemStack.STREAM_CODEC, WorkstationRecipe::output,
            WorkstationRecipe::new
    );

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        for (SizedIngredient sized : sizedIngredients) {
            list.add(sized.ingredient());
        }
        return list;
    }

    @Override
    public boolean matches(RecipeInput input, Level level) {
        return true;
    }

    @Override
    public ItemStack assemble(RecipeInput input, HolderLookup.Provider registries) {
        return this.output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return this.output;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRecipe.WORKSTATION_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRecipe.WORKSTATION_TYPE.get();
    }
}
