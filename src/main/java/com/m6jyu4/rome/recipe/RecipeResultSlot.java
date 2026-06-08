package com.m6jyu4.rome.recipe;

import com.m6jyu4.rome.gui.WorkstationMenu;
import com.m6jyu4.rome.util.InventoryCraftingUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

public class RecipeResultSlot extends Slot {
    private final WorkstationMenu menu;

    public RecipeResultSlot(WorkstationMenu menu, Container container, int slot, int x, int y) {
        super(container, slot, x, y);
        this.menu = menu;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        WorkstationRecipe recipe = menu.getSelectedRecipe();
        if (recipe == null) return false;

        return InventoryCraftingUtil.hasIngredients(player.getInventory(), recipe.sizedIngredients());
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        if (!player.level().isClientSide) {
            WorkstationRecipe recipe = menu.getSelectedRecipe();
            if (recipe != null) {
                InventoryCraftingUtil.consumeIngredients(player.getInventory(), recipe.sizedIngredients());

                player.getInventory().setChanged();
                menu.updateOutputSlot();
            }
        }
        super.onTake(player, stack);
    }

}

