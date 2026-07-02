package com.m6jyu4.rome.city;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class CityManager extends SavedData {
    private final Map<BlockPos, City> cities = new HashMap<>();

    // Standard factory constructor
    public static CityManager get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(
                new SavedData.Factory<>(
                        CityManager::new,
                        CityManager::load,
                        null // Optional: DataFixerType
                ),
                "rome_cities"
        );
    }

    public void registerCity(String name, BlockPos pos) {
        // Tier 1 city starts with a 32-block radius (approx. 2 chunks in each direction)
        cities.put(pos, new City(name, pos, 1, 32));
        setDirty(); // Marks data to be saved to disk
    }

    public void removeCity(BlockPos pos) {
        cities.remove(pos);
        setDirty();
    }

    @Nullable
    public City getCityAt(BlockPos pos) {
        for (City city : cities.values()) {
            if (city.isInside(pos)) {
                return city;
            }
        }
        return null;
    }

    @Nullable
    public City getCityOwningBuilding(BlockPos pos) {
        for (City city : cities.values()) {
            // Check if this specific city has registered this exact block position
            if (city.getBuildings().contains(pos)) {
                return city;
            }
        }
        return null;
    }

    // --- Loading and Saving ---

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        ListTag list = new ListTag();
        for (City city : cities.values()) {
            list.add(city.save());
        }
        tag.put("Cities", list);
        return tag;
    }

    public static CityManager load(CompoundTag tag, HolderLookup.Provider registries) {
        CityManager manager = new CityManager();
        if (tag.contains("Cities", Tag.TAG_LIST)) {
            ListTag list = tag.getList("Cities", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                City city = City.load(list.getCompound(i));
                manager.cities.put(city.getForumPos(), city);
            }
        }
        return manager;
    }
}
