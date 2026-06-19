package com.m6jyu4.rome.block.temp;

import com.google.gson.JsonObject;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;

public class MimicGeometryLoader implements IGeometryLoader<MimicGeometry> {
    public static final MimicGeometryLoader INSTANCE = new MimicGeometryLoader();

    private MimicGeometryLoader() {}

    @Override
    public MimicGeometry read(JsonObject jsonObject, JsonDeserializationContext deserializationContext) throws JsonParseException {
        // Remove the custom loader tag to prevent infinite recursion loop
        jsonObject.remove("loader");

        // Safely deserialize the standard block properties
        BlockModel fallbackModel = deserializationContext.deserialize(jsonObject, BlockModel.class);

        return new MimicGeometry(fallbackModel);
    }
}
