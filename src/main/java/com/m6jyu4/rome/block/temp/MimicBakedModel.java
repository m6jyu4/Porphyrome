package com.m6jyu4.rome.block.temp;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.data.ModelData;

import javax.annotation.Nullable;
import java.util.List;

public class MimicBakedModel implements BakedModel {

    private final BakedModel originalModel; // Fallback model (the JSON-defined look)

    public MimicBakedModel(BakedModel originalModel) {
        this.originalModel = originalModel;
    }

    // --- NeoForge Extended Methods (These do the actual mimicking) ---

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType) {
        BlockState mimicState = extraData.get(MimicBlockEntity.MIMIC_PROPERTY);

        if (mimicState != null && !mimicState.isAir()) {
            System.out.println("[MimicBlock Debug] Successfully found block below: " + mimicState);
            BakedModel mimicModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(mimicState);
            return mimicModel.getQuads(mimicState, side, rand, extraData, renderType);
        }

        System.out.println("[MimicBlock Debug] No block below found, falling back to original model.");
        return originalModel.getQuads(state, side, rand, extraData, renderType);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState mimicState = data.get(MimicBlockEntity.MIMIC_PROPERTY);

        if (mimicState != null && !mimicState.isAir()) {
            return Minecraft.getInstance().getBlockRenderer().getBlockModel(mimicState).getParticleIcon(data);
        }

        return originalModel.getParticleIcon(data);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        BlockState mimicState = data.get(MimicBlockEntity.MIMIC_PROPERTY);

        if (mimicState != null && !mimicState.isAir()) {
            BakedModel mimicModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(mimicState);
            return mimicModel.getRenderTypes(mimicState, rand, data);
        }

        return originalModel.getRenderTypes(state, rand, data);
    }

    // --- Vanilla Methods (CRITICAL: Do not return null or empty lists here) ---

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState blockState, @Nullable Direction direction, RandomSource randomSource) {
        // Provide the fallback model's quads so it doesn't render invisibly in menus/inventory
        return originalModel.getQuads(blockState, direction, randomSource);
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        // Returning null here WILL crash the game when sprinting on or breaking the block!
        return originalModel.getParticleIcon();
    }

    // --- Standard Delegates ---

    @Override
    public boolean useAmbientOcclusion() { return originalModel.useAmbientOcclusion(); }

    @Override
    public boolean isGui3d() { return originalModel.isGui3d(); }

    @Override
    public boolean usesBlockLight() { return originalModel.usesBlockLight(); }

    @Override
    public boolean isCustomRenderer() { return originalModel.isCustomRenderer(); }

    @Override
    public ItemOverrides getOverrides() { return originalModel.getOverrides(); }
}
