package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.recipe.WorkstationRecipeMessage;
import com.m6jyu4.rome.registry.ModRecipe;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("rome", "textures/gui/container/my_machine.png");

    private List<RecipeHolder<WorkstationRecipe>> availableRecipes;

    // Grid configuration constants
    private static final int COLUMNS = 4;
    private static final int ICON_SIZE = 18;
    private static final int GRID_START_X = 20;
    private static final int GRID_START_Y = 20;

    // Ingredient Display constants (RELATIVE coordinates to top-left of the GUI)
    private static final int ING_START_X = 95;
    private static final int ING_START_Y = 60;

    public WorkstationScreen(WorkstationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        if (this.minecraft != null && this.minecraft.level != null) {
            this.availableRecipes = this.minecraft.level.getRecipeManager()
                    .getAllRecipesFor(ModRecipe.WORKSTATION_TYPE.get());
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        // Render tooltips for grid items
        if (availableRecipes != null && !availableRecipes.isEmpty()) {
            for (int i = 0; i < availableRecipes.size(); i++) {
                int col = i % COLUMNS;
                int row = i / COLUMNS;

                int drawX = this.leftPos + GRID_START_X + (col * ICON_SIZE);
                int drawY = this.topPos + GRID_START_Y + (row * ICON_SIZE);

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    graphics.renderTooltip(this.font, availableRecipes.get(i).value().output(), mouseX, mouseY);
                }
            }
        }

        // Render tooltips for the required ingredients (uses absolute screen space coordinates)
        renderIngredientTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        // Draw Background Texture
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int startX = this.leftPos;
        int startY = this.topPos;
        graphics.blit(TEXTURE, startX, startY, 0, 0, this.imageWidth, this.imageHeight);

        // Draw Recipe Grid
        if (availableRecipes != null && !availableRecipes.isEmpty()) {
            for (int i = 0; i < availableRecipes.size(); i++) {
                int col = i % COLUMNS;
                int row = i / COLUMNS;

                int drawX = startX + GRID_START_X + (col * ICON_SIZE);
                int drawY = startY + GRID_START_Y + (row * ICON_SIZE);

                WorkstationRecipe recipe = availableRecipes.get(i).value();

                graphics.renderFakeItem(recipe.output(), drawX, drawY);

                if (this.menu.getSelectedRecipe() != null && this.menu.getSelectedRecipe() == recipe) {
                    graphics.pose().pushPose();
                    graphics.pose().translate(0, 0, 200);
                    graphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x8000FF00);
                    graphics.pose().popPose();
                }
            }
        }
    }

    // 1. Draw foreground labels, custom text, and custom items here!
    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw the default titles (Workstation and Inventory)
        graphics.drawString(this.font, this.title, this.titleLabelX, this.titleLabelY, 4210752, false);
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);

        // Draw the selected recipe's ingredients over the top of the GUI
        drawRecipeIngredients(graphics);
    }

    private void drawRecipeIngredients(GuiGraphics graphics) {
        WorkstationRecipe selected = this.menu.getSelectedRecipe();

        // Add this temporary line:
        if (selected == null) {
            System.out.println("DEBUG: Screen thinks no recipe is selected!");
        } else {
            System.out.println("DEBUG: Drawing ingredients for " + selected.output().getHoverName().getString());
        }

        if (selected != null && this.minecraft != null && this.minecraft.player != null) {

            List<SizedIngredient> ingredients = selected.sizedIngredients();
            Inventory playerInv = this.minecraft.player.getInventory();

            for (int i = 0; i < ingredients.size(); i++) {
                SizedIngredient sized = ingredients.get(i);
                int required = sized.count();

                // Count how many of this item the player has in their inventory
                int countInInv = 0;
                for (int j = 0; j < playerInv.getContainerSize(); j++) {
                    ItemStack stack = playerInv.getItem(j);
                    if (sized.ingredient().test(stack)) {
                        countInInv += stack.getCount();
                    }
                }

                boolean hasEnough = countInInv >= required;
                int color = hasEnough ? 0x55FF55 : 0xFF5555; // Green / Red

                ItemStack[] items = sized.ingredient().getItems();
                if (items.length > 0) {
                    int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                    ItemStack displayStack = items[cycleIndex];

                    // RELATIVE coordinates (no leftPos or topPos needed here)
                    int drawX = ING_START_X + (i * 32);
                    int drawY = ING_START_Y;

                    graphics.renderFakeItem(displayStack, drawX, drawY);

                    // Draw required count text with drop shadow
                    graphics.drawString(this.font, "x" + required, drawX + 16, drawY + 8, color, true);
                }
            }
        }
    }

    private void renderIngredientTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        WorkstationRecipe selected = this.menu.getSelectedRecipe();
        if (selected != null) {
            List<SizedIngredient> ingredients = selected.sizedIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
                // Must use absolute coordinates for screen mouse hover
                int drawX = this.leftPos + ING_START_X + (i * 32);
                int drawY = this.topPos + ING_START_Y;

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    ItemStack[] items = ingredients.get(i).ingredient().getItems();
                    if (items.length > 0) {
                        int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                        graphics.renderTooltip(this.font, items[cycleIndex], mouseX, mouseY);
                    }
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (availableRecipes != null && button == 0) {
            // 1. Check if they clicked an actual recipe icon first
            for (int i = 0; i < availableRecipes.size(); i++) {
                int col = i % COLUMNS;
                int row = i / COLUMNS;

                int drawX = this.leftPos + GRID_START_X + (col * ICON_SIZE);
                int drawY = this.topPos + GRID_START_Y + (row * ICON_SIZE);

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    RecipeHolder<WorkstationRecipe> clickedRecipe = availableRecipes.get(i);
                    WorkstationRecipe recipe = clickedRecipe.value();

                    this.menu.setSelectedRecipe(recipe);
                    PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, clickedRecipe.id()));
                    return true;
                }
            }

            // --- SAFETY CHECK FOR SLOTS ---
            // If the player is hovering over ANY slot (inventory or output),
            // completely skip deselection so they can place/take items safely!
            if (this.hoveredSlot != null) {
                return super.mouseClicked(mouseX, mouseY, button);
            }

            // --- DESELECTION LOGIC ---
            // Only clear the recipe if they clicked empty space in the grid
            // and NOT on any slot.
            int gridMinX = this.leftPos + GRID_START_X;
            int gridMinY = this.topPos + GRID_START_Y;
            int gridMaxX = gridMinX + (COLUMNS * ICON_SIZE);
            int gridMaxY = gridMinY + (5 * ICON_SIZE);

            if (mouseX >= gridMinX && mouseX < gridMaxX && mouseY >= gridMinY && mouseY < gridMaxY) {
                this.menu.setSelectedRecipe(null);

                ResourceLocation clearId = ResourceLocation.fromNamespaceAndPath("rome", "clear");
                PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, clearId));

                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }



}
