package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.blockentity.WorkstationBlockEntity;
import com.m6jyu4.rome.recipe.RecipeResultSlot;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.network.WorkstationRecipeMessage;
import com.m6jyu4.rome.registry.ModBlock;
import com.m6jyu4.rome.registry.ModMenuType;
import com.m6jyu4.rome.recipe.InventoryCraftingUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class WorkstationMenu extends BaseMenu {
    private final ResultContainer resultContainer = new ResultContainer();
    private final Player player;
    private WorkstationRecipe selectedRecipe;
    private int lastInventoryState = -1;

    /**
     * server-side menu constructor
     * called by {@link WorkstationBlockEntity#createMenu}
     *   when the player interacts with the workstation block.
     */
    public WorkstationMenu(int id, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuType.WORKSTATION_MENU.get(), id, access);
        this.player = playerInventory.player;

        this.addSlot(new RecipeResultSlot(this, resultContainer, 0, WorkstationScreen.OUTPUT_SLOT_X, WorkstationScreen.OUTPUT_SLOT_Y));
        addStandardInventorySlots(playerInventory, WorkstationScreen.INVENTORY_START_X, WorkstationScreen.INVENTORY_START_Y);
    }

    /**
     * client-side menu constructor
     * called by {@link ModMenuType#WORKSTATION_MENU}
     *   when the server tells the client to open the screen.
     */
    public WorkstationMenu(int id, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(id, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    /**
     * called by {@link WorkstationRecipeMessage#handle}
     *   when player clicks a recipe in the GUI.
     */
    public void setSelectedRecipe(WorkstationRecipe recipe) {
        this.selectedRecipe = recipe;
        updateOutputSlot();
    }

    public WorkstationRecipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    /**
     * looped by {@link WorkstationScreen#containerTick()}
     *   runs 20 times per second when player have the gui opened.
     */
    public void updateOutputSlot() {
        if (selectedRecipe != null && InventoryCraftingUtil.hasIngredients(player.getInventory(), selectedRecipe.sizedIngredients())) {
            this.resultContainer.setItem(0, selectedRecipe.output().copy());
        } else {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
        }
    }

    @Override
    public void slotsChanged(Container container) {
        super.slotsChanged(container);
    }

    @Override
    public void broadcastChanges() {
        if (this.player != null) {
            int currentState = this.player.getInventory().getTimesChanged();

            if (this.lastInventoryState != currentState) {
                this.updateOutputSlot();
                this.lastInventoryState = currentState;
            }
        }

        super.broadcastChanges();
    }

    public Block getBlock() {
        return ModBlock.WORKSTATION.get();
    }
}
