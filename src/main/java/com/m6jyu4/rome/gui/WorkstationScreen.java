package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.network.WorkstationCreativeCloneMessage;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.network.WorkstationRecipeMessage;
import com.m6jyu4.rome.registry.ModClientEvent;
import com.m6jyu4.rome.registry.ModRecipe;

import com.m6jyu4.rome.Constant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

// TODO: clean up whatever this is

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_workstation.png");

    public static final int OUTPUT_SLOT_X = 194 + 2;
    public static final int OUTPUT_SLOT_Y = 33 + 2;
    public static final int INVENTORY_START_X = 124;
    public static final int INVENTORY_START_Y = 137;

    /**
     * client-side screen constructor
     * Called by {@link ModClientEvent#registerScreens(RegisterMenuScreensEvent)}
     *   when the workstation GUI is to be displayed.
     */
    public WorkstationScreen(WorkstationMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        imageWidth  = 292;
        imageHeight = 219;
    }

    /**
     * client-side UI initialization
     *   called by {@link Screen#init(Minecraft, int, int)} after screen centring.
     *   loads recipes and creates search bar & UI widgets.
     */
    @Override
    protected void init() {
        super.init();
        loadRecipes();
        addSearchBar();
    }

    private List<RecipeHolder<WorkstationRecipe>> allRecipes      = new ArrayList<>();
    private List<RecipeHolder<WorkstationRecipe>> filteredRecipes = new ArrayList<>();

    private void loadRecipes() {
        this.allRecipes = this.minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipe.WORKSTATION_TYPE.get());
        this.filteredRecipes = new ArrayList<>(this.allRecipes);
    }

    private EditBox searchBox;

    private void addSearchBar() {
        this.searchBox = new CustomEditBox(this.font, this.leftPos + 26, this.topPos + 16, 78, 14, Component.translatable("gui.rome.search"));
        searchBox.setMaxLength(50);
        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(this.searchBox);
    }

    private int currentPage = 0;

    private static final int COLUMNS = 4;
    private static final int ROWS    = 6;
    private static final int RECIPES_PER_PAGE = COLUMNS * ROWS;

    private static final int RECIPE_ICON_GAP = 25;
    private static final int GRID_START_X = 13 + 2;
    private static final int GRID_START_Y = 37 + 2;

    private static final int INGREDIENT_ICON_GAP = 26;
    private static final int INGREDIENT_START_X = 142 + 2;
    private static final int INGREDIENT_START_Y = 83 + 2;





    private void updateSearch(String query) {
        this.filteredRecipes.clear();
        String lowerQuery = query.toLowerCase();

        for (RecipeHolder<WorkstationRecipe> recipe : allRecipes) {
            String itemName = recipe.value().output().getHoverName().getString().toLowerCase();
            if (itemName.contains(lowerQuery)) {
                this.filteredRecipes.add(recipe);
            }
        }
        this.currentPage = 0;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        this.renderTooltip(graphics, mouseX, mouseY);
        renderRecipeTooltip(graphics, mouseX, mouseY);
        renderIngredientTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        guiGraphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, 0, this.imageWidth, this.imageHeight, 512, 256);

        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int gridIndex = i - startIndex;
            int col = gridIndex % COLUMNS;
            int row = gridIndex / COLUMNS;

            int drawX = this.leftPos + GRID_START_X + (col * RECIPE_ICON_GAP);
            int drawY = this.topPos + GRID_START_Y + (row * RECIPE_ICON_GAP);

            WorkstationRecipe recipe = filteredRecipes.get(i).value();
            guiGraphics.renderFakeItem(recipe.output(), drawX, drawY);

            if (this.menu.getSelectedRecipe() != null && this.menu.getSelectedRecipe() == recipe) {
                guiGraphics.blit(TEXTURE, drawX -4, drawY -4, 0, 316, 0, 24, 24, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            }
        }
        WorkstationRecipe selected = this.menu.getSelectedRecipe();

        if (selected != null && this.menu.getSlot(0).getItem().isEmpty()) {
            int ghostX = this.leftPos + OUTPUT_SLOT_X;
            int ghostY = this.topPos + OUTPUT_SLOT_Y;

            guiGraphics.fill(ghostX, ghostY, ghostX + 16, ghostY + 16, 0x30_ff0000);
            guiGraphics.renderFakeItem(selected.output(), ghostX, ghostY);
            guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), ghostX, ghostY, ghostX + 16, ghostY + 16, 0x30_ffffff);
        }
        renderPageArrows(guiGraphics);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, INVENTORY_START_X, INVENTORY_START_Y - 12, 4210752, false);
        drawRecipeIngredients(graphics);
    }

    private int lastInventoryState = -1;


    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.menu.getSelectedRecipe() != null && this.minecraft != null && this.minecraft.player != null) {
            // Get the current state ID of the player's inventory
            int currentState = this.minecraft.player.getInventory().getTimesChanged();

            // Only update the slot if the inventory has actually changed since the last tick!
            if (this.lastInventoryState != currentState) {
                this.menu.updateOutputSlot();
                this.lastInventoryState = currentState; // Update our tracker
            }
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
        if (mouseButton == 2) handleMiddleClick(mouseX, mouseY);
        if (mouseButton == 0) handleLeftClick(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    //
    // middle click
    private void handleMiddleClick(double mouseX, double mouseY) {
        if (!(this.minecraft.player.isCreative())) return;

        if (tryCloneRecipe(mouseX, mouseY)) return;

        WorkstationRecipe selectedRecipe = menu.getSelectedRecipe();
        if (selectedRecipe == null) return;

        tryCloneOutput(selectedRecipe, mouseX, mouseY);
        tryCloneIngredient(selectedRecipe, mouseX, mouseY);
    }

    private boolean tryCloneRecipe(double mouseX, double mouseY) {
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            if(!isMouseOverRecipe(i, startIndex, mouseX, mouseY)) continue;

            RecipeHolder<WorkstationRecipe> clickedRecipe = filteredRecipes.get(i);
            ItemStack recipe = clickedRecipe.value().output().copy();
            creativeCloneItem(recipe);
            return true;
        }

        return false;
    }

    private void tryCloneOutput(WorkstationRecipe selectedRecipe, double mouseX, double mouseY) {
        if (!isMouseOverOutputSlot(mouseX, mouseY) || !isOutputSlotEmpty()) return;

        ItemStack output = selectedRecipe.output().copy();
        creativeCloneItem(output);
    }


    private void tryCloneIngredient(WorkstationRecipe selectedRecipe, double mouseX, double mouseY) {
        List<SizedIngredient> ingredients = selectedRecipe.sizedIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            if (!isMouseOverIngredientSlot(i, mouseX, mouseY)) continue;

            ItemStack[] items = ingredients.get(i).ingredient().getItems();
            if (isIngredientSlotEmpty(items)) continue;

            int ingredientIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
            ItemStack clickedStack = items[ingredientIndex].copy();
            creativeCloneItem(clickedStack);
        }

    }

    private void creativeCloneItem(ItemStack itemStack) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        if (!this.menu.getCarried().isEmpty()) return;

        itemStack.setCount(itemStack.getMaxStackSize());

        this.menu.setCarried(itemStack);
        PacketDistributor.sendToServer(new WorkstationCreativeCloneMessage(itemStack));
    }

    //
    // left click
    private void handleLeftClick(double mouseX, double mouseY) {
        if (trySelectRecipe(mouseX, mouseY)) return;

        deselectRecipe(mouseX, mouseY);
    }

    private boolean trySelectRecipe(double mouseX, double mouseY) {
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            if(!isMouseOverRecipe(i, startIndex, mouseX, mouseY)) continue;

            RecipeHolder<WorkstationRecipe> clickedRecipe = filteredRecipes.get(i);
            this.menu.setSelectedRecipe(clickedRecipe.value());
            PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, clickedRecipe.id()));

            return true;
        }

        return false;
    }

    private void deselectRecipe(double mouseX, double mouseY) {
        if (!isMouseOverRecipeGrid(mouseX, mouseY)) return;

        this.menu.setSelectedRecipe(null);
        PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId,Constant.rl("workstation.recipe_clear")));
    }

    //
    // click helper method
    private boolean isOutputSlotEmpty() {
        return this.menu.getSlot(0).getItem().isEmpty();
    }

    private boolean isIngredientSlotEmpty(ItemStack[] items) {
        return items.length == 0;
    }

    private boolean isMouseOverOutputSlot(double mouseX, double mouseY) {
        int drawX = this.leftPos + OUTPUT_SLOT_X;
        int drawY = this.topPos  + OUTPUT_SLOT_Y;
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverIngredientSlot(int i, double mouseX, double mouseY) {
        int drawX = this.leftPos + INGREDIENT_START_X + (i * INGREDIENT_ICON_GAP);
        int drawY = this.topPos  + INGREDIENT_START_Y;
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverRecipe(int i, int startIndex, double mouseX, double mouseY) {
        int gridIndex = i - startIndex;
        int col = gridIndex % COLUMNS;
        int row = gridIndex / COLUMNS;

        int drawX = this.leftPos + GRID_START_X + (col * RECIPE_ICON_GAP);
        int drawY = this.topPos + GRID_START_Y + (row * RECIPE_ICON_GAP);
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverRecipeGrid(double mouseX, double mouseY) {
        int gridMinX = this.leftPos + GRID_START_X;
        int gridMinY = this.topPos + GRID_START_Y;
        int gridMaxX = gridMinX + (COLUMNS * RECIPE_ICON_GAP);
        int gridMaxY = gridMinY + (ROWS * RECIPE_ICON_GAP);

        return mouseX >= gridMinX && mouseX < gridMaxX && mouseY >= gridMinY && mouseY < gridMaxY;
    }

    private boolean isMouseInBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    //
    // render helper method
    private void renderRecipeTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
        int startIndex = currentPage * RECIPES_PER_PAGE;
        int endIndex   = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int gridIndex = i - startIndex;
            int col = gridIndex % COLUMNS;
            int row = gridIndex / COLUMNS;
            int drawX = this.leftPos + GRID_START_X + (col * RECIPE_ICON_GAP);
            int drawY = this.topPos  + GRID_START_Y + (row * RECIPE_ICON_GAP);

            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                graphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x80FFFFFF);

                graphics.renderTooltip(this.font, filteredRecipes.get(i).value().output(), mouseX, mouseY);
            }

        }

    }

    private void renderIngredientTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        WorkstationRecipe selected = this.menu.getSelectedRecipe();
        if (selected == null) return;

        List<SizedIngredient> ingredients = selected.sizedIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            int drawX = this.leftPos + INGREDIENT_START_X + (i * INGREDIENT_ICON_GAP);
            int drawY = this.topPos  + INGREDIENT_START_Y;

            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                ItemStack[] items = ingredients.get(i).ingredient().getItems();

                if (items.length == 0) return;

                int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                graphics.renderTooltip(this.font, items[cycleIndex], mouseX, mouseY);

            }
        }

    }

    private void renderPageArrows(GuiGraphics guiGraphics) {
        int maxPages = (int) Math.ceil((double) filteredRecipes.size() / RECIPES_PER_PAGE);


//        if ()

        int x = leftPos + 60 + 1;
        int y = topPos + 211 - font.lineHeight * 2 - 1;

        String text = maxPages == 0 ? "0/0" : "%d/%d".formatted(currentPage + 1, maxPages);
        guiGraphics.drawString(font, text, (int) (x - font.width(text) / 2f), y + 4, 0xffffff);

        if (currentPage > 1) renderLeftArrow(guiGraphics, x, y);
        if (currentPage + 1 < maxPages) renderRightArrow(guiGraphics, x, y);


    }

    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;


    private void renderLeftArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(TEXTURE,
                x - 22 - 11 - 1, y -1,  // Adjust spacing for larger arrow width (11px)
                0,                    // z-level
                292, 41,             // u, v (texture start)
                12, 17,              // width=11, height=16 (your actual arrow size)
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
    }

    private void renderRightArrow(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.blit(TEXTURE,
                x + 22, y -1,           // draw position
                0,                    // z-level
                292, 24,             // u, v (texture start)
                12, 17,              // width=11, height=16 (your actual arrow size)
                TEXTURE_WIDTH, TEXTURE_HEIGHT
        );
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
                    int drawX = INGREDIENT_START_X + (i * INGREDIENT_ICON_GAP);
                    int drawY = INGREDIENT_START_Y;

                    String text = "x" + required;

                    graphics.renderFakeItem(items[cycleIndex], drawX, drawY);
                    graphics.drawString(this.font, text, drawX + 19 - font.width(text), drawY + 21, color, true);


                    if (countInInv < required) {
                        graphics.blit(TEXTURE, drawX -4, drawY -4, 0, 292, 0, 24, 24, TEXTURE_WIDTH, TEXTURE_HEIGHT);
                    }
                }
            }
        }
    }

}