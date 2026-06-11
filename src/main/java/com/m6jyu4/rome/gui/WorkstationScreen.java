package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.recipe.WorkstationRecipeMessage;
import com.m6jyu4.rome.registry.ModRecipe;

import com.m6jyu4.rome.util.Constant;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_workstation.png");

    // Recipe Lists
    private List<RecipeHolder<WorkstationRecipe>> allRecipes = new ArrayList<>();
    private List<RecipeHolder<WorkstationRecipe>> filteredRecipes = new ArrayList<>();

    // UI Widgets & Pagination
    private EditBox searchBox;
    private Button prevButton;
    private Button nextButton;
    private int currentPage = 0;

    // Grid configuration
    private static final int COLUMNS = 4;
    private static final int ROWS = 3;
    private static final int RECIPES_PER_PAGE = COLUMNS * ROWS; // 12 recipes per page
    private static final int ICON_SIZE = 18;
    private static final int GRID_START_X = 20;
    private static final int GRID_START_Y = 24; // Moved down slightly to fit the search bar

    private static final int ING_START_X = 95;
    private static final int ING_START_Y = 60;

    public WorkstationScreen(WorkstationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void init() {
        super.init();
//        this.titleLabelX = 1000; // Move vanilla title off-screen to save space for our search bar

        // 1. Load Recipes
        if (this.minecraft != null && this.minecraft.level != null) {
            this.allRecipes = this.minecraft.level.getRecipeManager()
                    .getAllRecipesFor(ModRecipe.WORKSTATION_TYPE.get());
            this.filteredRecipes = new ArrayList<>(this.allRecipes);
        }

//        // 2. Add Search Bar
//        this.searchBox = new EditBox(this.font, this.leftPos + GRID_START_X, this.topPos + 6, 54, 12, Component.literal("Search"));
//        this.searchBox.setResponder(this::updateSearch); // Calls updateSearch() when text changes
//        this.addRenderableWidget(this.searchBox);
//
//        // 3. Add Pagination Buttons
//        this.prevButton = Button.builder(Component.literal("<"), b -> changePage(-1))
//                .bounds(this.leftPos + 76, this.topPos + 4, 12, 16).build();
//        this.nextButton = Button.builder(Component.literal(">"), b -> changePage(1))
//                .bounds(this.leftPos + 89, this.topPos + 4, 12, 16).build();
//
//        this.addRenderableWidget(this.prevButton);
//        this.addRenderableWidget(this.nextButton);
//
//        updatePageButtons();
    }

    // --- Search & Pagination Logic ---

    private void updateSearch(String query) {
        this.filteredRecipes.clear();
        String lowerQuery = query.toLowerCase();

        for (RecipeHolder<WorkstationRecipe> recipe : allRecipes) {
            // Check if the output item's name contains the search text
            String itemName = recipe.value().output().getHoverName().getString().toLowerCase();
            if (itemName.contains(lowerQuery)) {
                this.filteredRecipes.add(recipe);
            }
        }
        this.currentPage = 0; // Reset to page 1 on new search
        updatePageButtons();
    }

    private void changePage(int direction) {
        this.currentPage += direction;
        updatePageButtons();
    }

    private void updatePageButtons() {
        int maxPages = (int) Math.ceil((double) filteredRecipes.size() / RECIPES_PER_PAGE);
        this.prevButton.active = this.currentPage > 0;
        this.nextButton.active = this.currentPage < maxPages - 1;
    }


    // --- Rendering ---

    // Coordinates of your output slot relative to the top-left of the GUI (Match your WorkstationMenu constructor!)
    private static final int OUTPUT_SLOT_X = 120;
    private static final int OUTPUT_SLOT_Y = 35;

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);

        // This is the cleaned-up, correct line:
        super.render(graphics, mouseX, mouseY, partialTick); // Draws background & standard slots

        this.renderTooltip(graphics, mouseX, mouseY);

        renderGridTooltips(graphics, mouseX, mouseY);
        renderIngredientTooltips(graphics, mouseX, mouseY);


    }


    @Override
    protected void containerTick() {
        super.containerTick();

        // Every tick, force the menu to update the output slot and recalculate
        // whether the player has enough ingredients in their inventory.
        if (this.menu.getSelectedRecipe() != null) {
            this.menu.updateOutputSlot();
        }
    }


    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);

        // Draw visible grid items for current page
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int gridIndex = i - startIndex; // 0 to 11
            int col = gridIndex % COLUMNS;
            int row = gridIndex / COLUMNS;

            int drawX = this.leftPos + GRID_START_X + (col * ICON_SIZE);
            int drawY = this.topPos + GRID_START_Y + (row * ICON_SIZE);

            WorkstationRecipe recipe = filteredRecipes.get(i).value();
            graphics.renderFakeItem(recipe.output(), drawX, drawY);

            // Highlight selected
            if (this.menu.getSelectedRecipe() != null && this.menu.getSelectedRecipe() == recipe) {
                graphics.pose().pushPose();
                graphics.pose().translate(0, 0, 200);
                graphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x8000FF00);
                graphics.pose().popPose();

            }
        }
        WorkstationRecipe selected = this.menu.getSelectedRecipe();

        if (selected != null && this.menu.getSlot(0).getItem().isEmpty()) {
            int ghostX = this.leftPos + OUTPUT_SLOT_X;
            int ghostY = this.topPos + OUTPUT_SLOT_Y;

            graphics.renderFakeItem(selected.output(), ghostX, ghostY);
            graphics.fill(RenderType.guiGhostRecipeOverlay(), ghostX, ghostY, ghostX + 16, ghostY + 16, 0x30_ffffff);
        }



    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, this.inventoryLabelX, this.inventoryLabelY, 4210752, false);
        drawRecipeIngredients(graphics);
    }

    // --- Clicks and Interactions ---

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startIndex = currentPage * RECIPES_PER_PAGE;
            int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

            // 1. Check if clicking a recipe
            for (int i = startIndex; i < endIndex; i++) {
                int gridIndex = i - startIndex;
                int col = gridIndex % COLUMNS;
                int row = gridIndex / COLUMNS;

                int drawX = this.leftPos + GRID_START_X + (col * ICON_SIZE);
                int drawY = this.topPos + GRID_START_Y + (row * ICON_SIZE);

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    RecipeHolder<WorkstationRecipe> clickedRecipe = filteredRecipes.get(i);
                    this.menu.setSelectedRecipe(clickedRecipe.value());
                    PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, clickedRecipe.id()));
                    return true;
                }
            }

            // 2. If hovering over inventory slot or interacting with SearchBox/Buttons, skip deselection
//            if (this.hoveredSlot != null || searchBox.isMouseOver(mouseX, mouseY) || prevButton.isMouseOver(mouseX, mouseY) || nextButton.isMouseOver(mouseX, mouseY)) {
//                return super.mouseClicked(mouseX, mouseY, button);
//            }

            // 3. Deselection Logic (Clicking empty grid space)
            int gridMinX = this.leftPos + GRID_START_X;
            int gridMinY = this.topPos + GRID_START_Y;
            int gridMaxX = gridMinX + (COLUMNS * ICON_SIZE);
            int gridMaxY = gridMinY + (ROWS * ICON_SIZE);

            if (mouseX >= gridMinX && mouseX < gridMaxX && mouseY >= gridMinY && mouseY < gridMaxY) {
                this.menu.setSelectedRecipe(null);
                PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, ResourceLocation.fromNamespaceAndPath("rome", "clear")));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    // --- Helper Render Methods ---

    private void renderGridTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int gridIndex = i - startIndex;
            int col = gridIndex % COLUMNS;
            int row = gridIndex / COLUMNS;
            int drawX = this.leftPos + GRID_START_X + (col * ICON_SIZE);
            int drawY = this.topPos + GRID_START_Y + (row * ICON_SIZE);

            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                graphics.renderTooltip(this.font, filteredRecipes.get(i).value().output(), mouseX, mouseY);
            }
        }
    }

    private void renderIngredientTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        WorkstationRecipe selected = this.menu.getSelectedRecipe();
        if (selected != null) {
            List<SizedIngredient> ingredients = selected.sizedIngredients();
            for (int i = 0; i < ingredients.size(); i++) {
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

    private void drawRecipeIngredients(GuiGraphics graphics) {
        WorkstationRecipe selected = this.menu.getSelectedRecipe();
        if (selected != null && this.minecraft != null && this.minecraft.player != null) {
            List<SizedIngredient> ingredients = selected.sizedIngredients();
            Inventory playerInv = this.minecraft.player.getInventory();

            for (int i = 0; i < ingredients.size(); i++) {
                SizedIngredient sized = ingredients.get(i);
                int required = sized.count();

                int countInInv = 0;
                for (int j = 0; j < playerInv.getContainerSize(); j++) {
                    ItemStack stack = playerInv.getItem(j);
                    if (sized.ingredient().test(stack)) { countInInv += stack.getCount(); }
                }

                int color = (countInInv >= required) ? 0x55FF55 : 0xFF5555;
                ItemStack[] items = sized.ingredient().getItems();

                if (items.length > 0) {
                    int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                    int drawX = ING_START_X + (i * 32);
                    int drawY = ING_START_Y;

                    graphics.renderFakeItem(items[cycleIndex], drawX, drawY);
                    graphics.drawString(this.font, "x" + required, drawX + 16, drawY + 8, color, true);
                }
            }
        }
    }
}
