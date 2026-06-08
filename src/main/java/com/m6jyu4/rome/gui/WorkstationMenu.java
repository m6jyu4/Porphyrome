package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.recipe.RecipeResultSlot;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.registry.ModMenuType;
import com.m6jyu4.rome.util.InventoryCraftingUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class WorkstationMenu extends AbstractContainerMenu {

    private final ResultContainer outputContainer = new ResultContainer();
    private final Player player;
    private WorkstationRecipe selectedRecipe;

    public WorkstationMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory);
    }

    public WorkstationMenu(int id, Inventory playerInv) {
        super(ModMenuType.WORKSTATION_MENU.get(), id);
        this.player = playerInv.player;

        // Output Slot
        this.addSlot(new RecipeResultSlot(this, outputContainer, 0, 120, 35));

        // Player Inventory Slots
        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 9; ++j) {
                this.addSlot(new Slot(playerInv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int k = 0; k < 9; ++k) {
            this.addSlot(new Slot(playerInv, k, 8 + k * 18, 142));
        }
    }

    // Called via Network Packet when player clicks a recipe in the GUI
    public void setSelectedRecipe(WorkstationRecipe recipe) {
        this.selectedRecipe = recipe;
        updateOutputSlot();
    }

    public WorkstationRecipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    // Evaluates if we should show the ghost item in the output slot
    // Evaluates if we should show the ghost item in the output slot
    public void updateOutputSlot() {
        // Change selectedRecipe.getIngredients() to selectedRecipe.sizedIngredients()
        if (selectedRecipe != null && InventoryCraftingUtil.hasIngredients(player.getInventory(), selectedRecipe.sizedIngredients())) {
            // Show the result!
            outputContainer.setItem(0, selectedRecipe.output().copy());
        } else {
            // Clear the slot if they don't have the items or no recipe is selected
            outputContainer.setItem(0, ItemStack.EMPTY);
        }
        this.broadcastChanges();
    }


    @Override
    public ItemStack quickMoveStack(Player player, int i) {
        return null;
    }

    @Override
    public void slotsChanged(Container container) {
        // If player moves items in their inventory, re-evaluate the output slot
        updateOutputSlot();
        super.slotsChanged(container);
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
