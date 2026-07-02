package com.m6jyu4.rome.gui.forum;

import com.m6jyu4.rome.city.City;
import com.m6jyu4.rome.registry.ModMenuType; // Make sure you register FORUM_MENU!

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ForumMenu extends AbstractContainerMenu {

    public record BuildingEntry(BlockPos pos, ItemStack icon) {}

    public final String cityName;
    public final BlockPos forumPos; // NEW
    public final int tier;
    public final int radius;
    public final List<BuildingEntry> buildings = new ArrayList<>();

    // CLIENT-SIDE
    public ForumMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
        super(ModMenuType.FORUM_MENU.get(), id);
        this.cityName = buf.readUtf();
        this.forumPos = buf.readBlockPos(); // MUST BE SECOND!
        this.tier = buf.readInt();          // MUST BE THIRD!
        this.radius = buf.readInt();        // MUST BE FOURTH!

        int size = buf.readInt();
        for (int i = 0; i < size; i++) {
            this.buildings.add(new BuildingEntry(buf.readBlockPos(), ItemStack.STREAM_CODEC.decode(buf)));
        }
    }

    // CLIENT-SIDE
//    public ForumMenu(int id, Inventory inv, RegistryFriendlyByteBuf buf) {
//        super(ModMenuType.FORUM_MENU.get(), id);
//        this.cityName = buf.readUtf();
//        this.forumPos = buf.readBlockPos(); // NEW
//        this.tier = buf.readInt();
//        this.radius = buf.readInt();
//
//        int size = buf.readInt();
//        for (int i = 0; i < size; i++) {
//            this.buildings.add(new BuildingEntry(buf.readBlockPos(), ItemStack.STREAM_CODEC.decode(buf)));
//        }
//    }

    // SERVER-SIDE
    public ForumMenu(int id, Inventory inv, City city, List<BuildingEntry> activeBuildings) {
        super(ModMenuType.FORUM_MENU.get(), id);
        this.cityName = city.getName();
        this.forumPos = city.getForumPos(); // NEW
        this.tier = city.getTier();
        this.radius = city.getRadius();
        this.buildings.addAll(activeBuildings);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // No slots to shift-click
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}