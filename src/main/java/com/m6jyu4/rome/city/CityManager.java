package com.m6jyu4.rome.city;

import com.m6jyu4.rome.block.ConstructableBlock;
import com.m6jyu4.rome.gui.forum.ForumMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
        cities.put(pos, new City(name, pos, 1, 4));
        setDirty(); // Marks data to be saved to disk
    }

    // Creates the city and saves it
    public City foundNewCity(String name, BlockPos pos, @Nullable java.util.UUID ownerUUID) {
        City newCity = new City(name, pos, 1, 4); // Tier 1, Radius 4
        if (ownerUUID != null) {
            newCity.setOwnerUUID(ownerUUID);
        }
        this.cities.put(pos, newCity);
        this.setDirty();
        return newCity;
    }

    // Scans the world and returns how many buildings it found
    public int annexPreExistingBuildings(City city, ServerLevel serverLevel) {
        int r = city.getRadius();
        int buildingsFound = 0;
        BlockPos pos = city.getForumPos();

        int minY = Math.max(serverLevel.getMinBuildHeight(), pos.getY() - 64);
        int maxY = Math.min(serverLevel.getMaxBuildHeight() - 1, pos.getY() + 64);

        BlockPos minPos = pos.offset(-r, 0, -r).atY(minY);
        BlockPos maxPos = pos.offset(r, 0, r).atY(maxY);

        for (BlockPos scanPos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState scanState = serverLevel.getBlockState(scanPos);
            if (scanState.getBlock() instanceof ConstructableBlock) {
                if (this.getCityOwningBuilding(scanPos) == null) {
                    city.addBuilding(scanPos.immutable());
                    buildingsFound++;
                }
            }
        }

        if (buildingsFound > 0) this.setDirty();

        return buildingsFound; // Pass the number back to the Block
    }

    // In CityManager.java
    public List<ForumMenu.BuildingEntry> getMenuBuildingEntries(City city, ServerLevel serverLevel) {
        List<ForumMenu.BuildingEntry> displayBuildings = new ArrayList<>();
        int r = city.getRadius() + 1; // +1 to include border-touching buildings
        BlockPos pos = city.getForumPos();

        int minY = Math.max(serverLevel.getMinBuildHeight(), pos.getY() - 32);
        int maxY = Math.min(serverLevel.getMaxBuildHeight() - 1, pos.getY() + 32);

        BlockPos minPos = pos.offset(-r, 0, -r).atY(minY);
        BlockPos maxPos = pos.offset(r, 0, r).atY(maxY);

        for (BlockPos scanPos : BlockPos.betweenClosed(minPos, maxPos)) {
            BlockState bState = serverLevel.getBlockState(scanPos);
            if (bState.getBlock() instanceof ConstructableBlock) {
                BlockPos immutablePos = scanPos.immutable();
                City ownerCity = this.getCityOwningBuilding(immutablePos);

                ItemStack iconStack = new ItemStack(bState.getBlock());
                if (iconStack.isEmpty()) {
                    iconStack = new ItemStack(net.minecraft.world.item.Items.BRICKS);
                }

                if (ownerCity != null) {
                    boolean isOwned = ownerCity.getForumPos().equals(city.getForumPos());
                    displayBuildings.add(new ForumMenu.BuildingEntry(
                            immutablePos, iconStack, isOwned, ownerCity.getColour(),
                            ownerCity.getForumPos(), ownerCity.isAllowAnnex()
                    ));
                } else {
                    // Neutral / Unowned border building
                    displayBuildings.add(new ForumMenu.BuildingEntry(
                            immutablePos, iconStack, false, 0x888888,
                            city.getForumPos(), true
                    ));
                }
            }
        }
        return displayBuildings;
    }

    public void removeCity(BlockPos pos) {
        City fallenCity = cities.remove(pos);

        if (fallenCity != null) {
            for (BlockPos buildingPos : fallenCity.getBuildings()) {
                City closestCity = null;
                double closestDistanceSqr = Double.MAX_VALUE;

                for (City otherCity : cities.values()) {
                    if (otherCity.isInside(buildingPos)) {
                        double distanceSqr = buildingPos.distSqr(otherCity.getForumPos());
                        if (distanceSqr < closestDistanceSqr) {
                            closestDistanceSqr = distanceSqr;
                            closestCity = otherCity;
                        }
                    }
                }

                // 4. If we found a valid neighboring city, transfer the building to them!
                if (closestCity != null) {
                    closestCity.addBuilding(buildingPos);

                    // --- NEW: LIVE SYNC THE COLLAPSE TRANSFER ---
                    // This tells all open GUIs to instantly change the building's color
                    // and ownership to the new surviving empire!
                    net.neoforged.neoforge.network.PacketDistributor.sendToAllPlayers(
                            new com.m6jyu4.rome.network.SyncBuildingOwnerMessage(buildingPos, closestCity.getForumPos(), closestCity.getColour())
                    );
                }
            }
            setDirty();
        }
    }

//    public void removeCity(BlockPos pos) {
//        // 1. Remove the city from the map, but keep a reference to it
//        City fallenCity = cities.remove(pos);
//
//        if (fallenCity != null) {
//            // 2. Loop through all the buildings the fallen city used to own
//            for (BlockPos buildingPos : fallenCity.getBuildings()) {
//
//                City closestCity = null;
//                double closestDistanceSqr = Double.MAX_VALUE;
//
//                // 3. Check every remaining city in the world
//                for (City otherCity : cities.values()) {
//                    // Is the building inside this other city's borders?
//                    if (otherCity.isInside(buildingPos)) {
//                        // Calculate the distance from the building to the new city's Forum
//                        double distanceSqr = buildingPos.distSqr(otherCity.getForumPos());
//
//                        // If it's the closest one we've found so far, save it
//                        if (distanceSqr < closestDistanceSqr) {
//                            closestDistanceSqr = distanceSqr;
//                            closestCity = otherCity;
//                        }
//                    }
//                }
//
//                // 4. If we found a valid neighboring city, transfer the building to them!
//                if (closestCity != null) {
//                    closestCity.addBuilding(buildingPos);
//                }
//            }
//
//            // 5. Save the data to disk so the transfers are permanent
//            setDirty();
//        }
//    }

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
