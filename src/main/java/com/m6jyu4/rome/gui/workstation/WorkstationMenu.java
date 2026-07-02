package com.m6jyu4.rome.gui.workstation;

import com.m6jyu4.rome.blockentity.WorkstationBlockEntity;
import com.m6jyu4.rome.gui.BaseMenu;
import com.m6jyu4.rome.recipe.RecipeResultSlot;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
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
    int lastInventoryState = -1;

    /**
     * client-side menu constructor
     * called by {@link ModMenuType#WORKSTATION_MENU}
     *   when the server tells the client to open the screen.
     *
     * @param containerId     The container ID assigned by the server
     * @param playerInventory The player's inventory
     * @param buf             Network buffer containing the block position
     */
    public WorkstationMenu(int containerId, Inventory playerInventory, RegistryFriendlyByteBuf buf) {
        this(containerId, playerInventory, ContainerLevelAccess.create(playerInventory.player.level(), buf.readBlockPos()));
    }

    /**
     * server-side menu constructor
     * called by {@link WorkstationBlockEntity#createMenu}
     *   when the player interacts with the workstation block.
     *
     * @param containerId     The container ID assigned by the server
     * @param playerInventory The player's inventory
     * @param access          Access to the workstation block in the world
     */
    public WorkstationMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access) {
        super(ModMenuType.WORKSTATION_MENU.get(), containerId, access);
        this.player = playerInventory.player;

        initSlots();
    }

    private void initSlots(){
        this.addSlot(new RecipeResultSlot(this, resultContainer, 0, WorkstationUI.OUTPUT_SLOT_X, WorkstationUI.OUTPUT_SLOT_Y));
        addInventorySlots(player.getInventory(), WorkstationUI.INVENTORY_SLOT_X, WorkstationUI.INVENTORY_SLOT_Y);
    }

    public WorkstationRecipe getSelectedRecipe() {
        return this.selectedRecipe;
    }

    public void setSelectedRecipe(WorkstationRecipe recipe) {
        this.selectedRecipe = recipe;
        updateOutputSlot();
    }

    public void updateOutputSlot() {
        if (selectedRecipe == null) {
            this.resultContainer.setItem(0, ItemStack.EMPTY);
            return;
        }

        boolean hasIngredients = InventoryCraftingUtil.hasIngredients(player.getInventory(), selectedRecipe.sizedIngredients());
        if (hasIngredients) {
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
