package com.m6jyu4.rome.network;

import com.m6jyu4.rome.gui.WorkstationMenu;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.Constant;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

public record WorkstationRecipeMessage(int containerId, ResourceLocation recipeId) implements CustomPacketPayload {

    public static final Type<WorkstationRecipeMessage> TYPE = new Type<>(Constant.rl("workstation_select_recipe"));

    public static final StreamCodec<RegistryFriendlyByteBuf, WorkstationRecipeMessage> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.INT, WorkstationRecipeMessage::containerId,
                    ResourceLocation.STREAM_CODEC, WorkstationRecipeMessage::recipeId,
                    WorkstationRecipeMessage::new
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    // This runs on the SERVER when it receives the packet from the CLIENT
    public static void handle(WorkstationRecipeMessage payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer player) {
                if (player.containerMenu instanceof WorkstationMenu menu && menu.containerId == payload.containerId()) {

                    // --- NEW DESELECTION CHECK ---
                    if (payload.recipeId().getPath().equals("clear")) {
                        menu.setSelectedRecipe(null);
                        return; // Stop processing, we just cleared it!
                    }

                    // Otherwise, find the recipe as normal
                    RecipeManager recipeManager = player.server.getRecipeManager();
                    Optional<RecipeHolder<?>> optionalRecipe = recipeManager.byKey(payload.recipeId());

                    if (optionalRecipe.isPresent() && optionalRecipe.get().value() instanceof WorkstationRecipe recipe) {
                        menu.setSelectedRecipe(recipe);
                    }
                }
            }
        });
    }


}
