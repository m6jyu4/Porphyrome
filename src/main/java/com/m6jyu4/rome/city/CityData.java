package com.m6jyu4.rome.city;

import com.m6jyu4.rome.gui.forum.ForumMenu.BuildingEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record CityData(
        String cityName,
        BlockPos forumPos,
        int tier,
        int radius,
        boolean allowAnnex,
        UUID ownerUUID,
        List<BuildingEntry> buildings
) {

    public static final StreamCodec<RegistryFriendlyByteBuf, CityData> STREAM_CODEC = StreamCodec.of(
            CityData::write,
            CityData::read
    );

    public static void write(RegistryFriendlyByteBuf buf, CityData data) {
        buf.writeUtf(data.cityName());
        buf.writeBlockPos(data.forumPos());
        buf.writeInt(data.tier());
        buf.writeInt(data.radius());
        buf.writeBoolean(data.allowAnnex());

        if (data.ownerUUID() != null) {
            buf.writeBoolean(true);
            buf.writeUUID(data.ownerUUID());
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(data.buildings().size());
        for (BuildingEntry entry : data.buildings()) {
            buf.writeBlockPos(entry.pos());
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, entry.icon());
            buf.writeBoolean(entry.isOwned());
            buf.writeInt(entry.color());
            buf.writeBlockPos(entry.ownerForumPos());
            buf.writeBoolean(entry.allowAnnex());
        }
    }

    public static CityData read(RegistryFriendlyByteBuf buf) {
        String cityName = buf.readUtf();
        BlockPos forumPos = buf.readBlockPos();
        int tier = buf.readInt();
        int radius = buf.readInt();
        boolean allowAnnex = buf.readBoolean();
        UUID ownerUUID = buf.readBoolean() ? buf.readUUID() : null;

        int size = buf.readInt();
        List<BuildingEntry> buildings = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            buildings.add(new BuildingEntry(
                    buf.readBlockPos(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf),
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readBlockPos(),
                    buf.readBoolean()
            ));
        }

        return new CityData(cityName, forumPos, tier, radius, allowAnnex, ownerUUID, buildings);
    }

    public static CityData from(City city, List<BuildingEntry> buildings) {
        return new CityData(
                city.getName(),
                city.getForumPos(),
                city.getTier(),
                city.getRadius(),
                city.isAllowAnnex(),
                city.getOwnerUUID(),
                buildings
        );
    }
}