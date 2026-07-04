package com.m6jyu4.rome.gui.forum;

import com.m6jyu4.rome.registry.ModMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ForumMenu extends AbstractContainerMenu {

    // NEW: Added boolean isOwned to track if it belongs to this city or a neighbor
    public record BuildingEntry(BlockPos pos, ItemStack icon, boolean isOwned, int color, BlockPos ownerForumPos, boolean allowAnnex) {}

    public final String cityName;
    public final BlockPos forumPos;
    public final int tier;
    public final int radius;
    public final List<BuildingEntry> buildings = new ArrayList<>();
    public final java.util.UUID ownerUUID;
    public boolean allowAnnex;

    // CLIENT-SIDE (Reads the data sent by the server)
    public ForumMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(ModMenuType.FORUM_MENU.get(), id);
        this.cityName = buf.readUtf();
        this.forumPos = buf.readBlockPos();
        this.tier = buf.readInt();
        this.radius = buf.readInt();
        this.allowAnnex = buf.readBoolean();
        this.ownerUUID = buf.readBoolean() ? buf.readUUID() : null;

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.buildings.add(new BuildingEntry(
                    buf.readBlockPos(),
                    ItemStack.OPTIONAL_STREAM_CODEC.decode(buf), // MUST BE OPTIONAL!
                    buf.readBoolean(),
                    buf.readInt(),
                    buf.readBlockPos(),
                    buf.readBoolean()
            ));
        }
    }

    // SERVER-SIDE
    public ForumMenu(int id, Inventory inv, com.m6jyu4.rome.city.City city, List<BuildingEntry> activeBuildings) {
        super(ModMenuType.FORUM_MENU.get(), id);
        this.cityName = city.getName();
        this.forumPos = city.getForumPos();
        this.tier = city.getTier();
        this.radius = city.getRadius();
        this.buildings.addAll(activeBuildings);
        this.allowAnnex = city.isAllowAnnex(); // Assumes this getter is in City.java
        this.ownerUUID = city.getOwnerUUID();
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
