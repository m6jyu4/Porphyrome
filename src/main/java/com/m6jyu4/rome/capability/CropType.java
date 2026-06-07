package com.m6jyu4.rome.capability;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.Map;

public enum CropType implements StringRepresentable {

    EMPTY(0),
    WHEAT(1),
    POTATO(2),
    CARROT(3),
    BEETROOT(4);

    private final int foodValue;

    CropType(int foodValue) {
        this.foodValue = foodValue;
    }

    public static final Map<Item, CropType> CROP_MAPPINGS = Map.ofEntries(
            Map.entry(Items.WHEAT_SEEDS,    CropType.WHEAT),
            Map.entry(Items.POTATO,         CropType.POTATO),
            Map.entry(Items.CARROT,         CropType.CARROT),
            Map.entry(Items.BEETROOT_SEEDS, CropType.BEETROOT)
    );

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase();
    }

}
