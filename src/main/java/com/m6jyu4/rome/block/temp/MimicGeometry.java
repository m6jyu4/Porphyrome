package com.m6jyu4.rome.block.temp;

import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Function;

public class MimicGeometry implements IUnbakedGeometry<MimicGeometry> {
    private final BlockModel fallbackModel;

    public MimicGeometry(BlockModel fallbackModel) {
        this.fallbackModel = fallbackModel;
    }

    @Override
    public BakedModel bake(
            IGeometryBakingContext context,
            ModelBaker baker,
            Function<Material, TextureAtlasSprite> spriteGetter,
            ModelState modelState,
            ItemOverrides overrides
    ) {
        // Bake the unbaked fallback BlockModel using the standard baker
        BakedModel bakedFallback = fallbackModel.bake(baker, fallbackModel, spriteGetter, modelState, context.useBlockLight());

        // Return your custom baked model wrapped around the fallback model
        return new MimicBakedModel(bakedFallback);
    }
}