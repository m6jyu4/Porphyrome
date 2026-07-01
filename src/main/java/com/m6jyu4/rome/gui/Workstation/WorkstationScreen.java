package com.m6jyu4.rome.gui.Workstation;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.gui.CustomEditBox;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.registry.ModRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    /* TEXTURE details */
    static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_workstation.png");
    static final int textureWidth  = 512;
    static final int textureHeight = 256;
    static final int guiWidth  = 292;
    static final int guiHeight = 219;

    private final WorkstationInputHandler inputHandler;
    private final WorkstationUI workstationUI;

    int currentPage = 0;
    List<RecipeHolder<WorkstationRecipe>> allRecipes = new ArrayList<>();
    List<RecipeHolder<WorkstationRecipe>> filteredRecipes = new ArrayList<>();

    private EditBox searchBox;

    public WorkstationScreen(WorkstationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth  = guiWidth;
        imageHeight = guiHeight;

        this.inputHandler = new WorkstationInputHandler(this);
        this.workstationUI = new WorkstationUI(this);
    }

        @Override
    protected void init() {
        super.init();
        loadRecipes();
        addSearchBar();
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.menu.getSelectedRecipe() != null && this.minecraft != null && this.minecraft.player != null) {

            int currentState = this.minecraft.player.getInventory().getTimesChanged();

            if (this.menu.lastInventoryState != currentState) {
                this.menu.updateOutputSlot();
                this.menu.lastInventoryState = currentState;
            }
        }
    }

    private void loadRecipes() {
        this.allRecipes = this.minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipe.WORKSTATION_TYPE.get());
        this.filteredRecipes = new ArrayList<>(this.allRecipes);
    }

    private void addSearchBar() {
        this.searchBox = new CustomEditBox(Minecraft.getInstance().font, leftPos + 26, topPos + 16, 78, 14, Component.translatable("gui.rome.search"));
        searchBox.setMaxLength(50);
        this.searchBox.setResponder(workstationUI::updateSearch);
        addRenderableWidget(this.searchBox);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        this.inputHandler.handleMouseClick(mouseX, mouseY, mouseButton);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick); // and renderLabels
        workstationUI.renderUI(guiGraphics);
        this.renderTooltip(guiGraphics, mouseX, mouseY);
    }

    @Override
    public void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, leftPos, topPos, 0, 0, 0, guiWidth, guiHeight, textureWidth, textureHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int INVENTORY_LABEL_Y_OFFSET = - 12;
        guiGraphics.drawString(this.font, this.playerInventoryTitle, WorkstationUI.INVENTORY_SLOT_X, WorkstationUI.INVENTORY_SLOT_Y + INVENTORY_LABEL_Y_OFFSET, 4210752, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderTooltip(guiGraphics, mouseX, mouseY);
        workstationUI.renderTooltips(guiGraphics, mouseX, mouseY);
    }

}