package com.m6jyu4.rome.city;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.HashSet;
import java.util.Set;

public class City {
    private String name; // Make sure this is not 'final' so we can change it!
    private int colour = 0xAA00AA; // Default: Minecraft Gold
    private final BlockPos forumPos;
    private int tier;
    private int radius; // Block radius (e.g., 50 blocks out from the center)

    private java.util.UUID ownerUUID;
    private boolean allowAnnex = true;

    private final Set<BlockPos> buildings = new HashSet<>();

    public City(String name, BlockPos forumPos, int tier, int radius) {
        this.name = name;
        this.forumPos = forumPos;
        this.tier = tier;
        this.radius = radius;
    }

    // Quick mathematical check to see if a position is inside the square city limits
    public boolean isInside(BlockPos pos) {
        int dx = Math.abs(pos.getX() - forumPos.getX());
        int dz = Math.abs(pos.getZ() - forumPos.getZ());
        return dx <= radius && dz <= radius;
    }

    // Upgrading the city expands its square borders (centuriation expansion)
    public void upgrade() {
        this.tier++;
        this.radius += 25; // Expands the grid by 25 blocks in every direction
    }

    public void addBuilding(BlockPos pos) {
        this.buildings.add(pos);
    }

    public void removeBuilding(BlockPos pos) {
        this.buildings.remove(pos);
    }

    public Set<BlockPos> getBuildings() {
        return this.buildings;
    }

    // NBT Serialization for saving/loading
    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putString("Name", name);
        tag.putLong("ForumPos", forumPos.asLong());
        tag.putInt("Tier", tier);
        tag.putInt("Radius", radius);

        ListTag buildingList = new ListTag();
        for (BlockPos pos : buildings) {
            CompoundTag posTag = new CompoundTag();
            posTag.putLong("Pos", pos.asLong());
            buildingList.add(posTag);
        }
        tag.put("Buildings", buildingList);
        tag.putInt("Colour", colour);

        if (ownerUUID != null) tag.putUUID("OwnerUUID", ownerUUID);
        tag.putBoolean("AllowAnnex", allowAnnex);

        return tag;
    }

    // UPDATE: Load the buildings
    public static City load(CompoundTag tag) {
        City city = new City(
                tag.getString("Name"),
                BlockPos.of(tag.getLong("ForumPos")),
                tag.getInt("Tier"),
                tag.getInt("Radius")
        );
        if (tag.contains("Colour")) {
            city.setColour(tag.getInt("Colour"));
        }
        if (tag.contains("Buildings", Tag.TAG_LIST)) {
            ListTag buildingList = tag.getList("Buildings", Tag.TAG_COMPOUND);
            for (int i = 0; i < buildingList.size(); i++) {
                city.addBuilding(BlockPos.of(buildingList.getCompound(i).getLong("Pos")));
            }
        }

        if (tag.hasUUID("OwnerUUID")) city.setOwnerUUID(tag.getUUID("OwnerUUID"));
        if (tag.contains("AllowsAnnexation")) city.setAllowAnnex(tag.getBoolean("AllowsAnnexation"));
        return city;
    }

    // Getters

    public void setOwnerUUID(java.util.UUID uuid) { this.ownerUUID = uuid; }
    public java.util.UUID getOwnerUUID() { return this.ownerUUID; }
    public void setAllowAnnex(boolean allows) { this.allowAnnex = allows; }
    public boolean isAllowAnnex() { return this.allowAnnex; }


    public void setName(String name) {
        this.name = name;
    }
    public void setColour(int colour) { this.colour = colour; }
    public int getColour() { return this.colour; }

    public String getName() { return name; }
    public BlockPos getForumPos() { return forumPos; }
    public int getTier() { return tier; }
    public int getRadius() { return radius; }

}
