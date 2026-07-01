package com.m6jyu4.rome.gui.Workstation;

import com.m6jyu4.rome.recipe.WorkstationRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

import java.util.List;

class WorkstationUI {

    private final WorkstationScreen screen;

    /* SLOT details */
    static final int OUTPUT_SLOT_X = 194 + 2;
    static final int OUTPUT_SLOT_Y = 33 + 2;
    static final int INVENTORY_SLOT_X = 124;
    static final int INVENTORY_SLOT_Y = 137;
    static final int RECIPE_GRID_X = 13 + 2;
    static final int RECIPE_GRID_Y = 37 + 2;
    static final int INGREDIENT_GRID_X = 142 + 2;
    static final int INGREDIENT_GRID_Y = 83 + 2;

    static final int recipeIconGap = 25;
    static final int ingredientIconGap = 26;

    private static final int redBox_X_Offset = 292;
    private static final int yellowBox_X_Offset = 316;
    private static final int boxSize = 24;

    static final int recipeBookColumns = 4;
    static final int recipeBookRows = 6;
    static final int recipePerPage = recipeBookColumns * recipeBookRows;

    WorkstationUI(WorkstationScreen screen) {
        this.screen = screen;
    }

    void renderUI(GuiGraphics guiGraphics){
        renderRecipes(guiGraphics);
        renderOutputSlot(guiGraphics);
        renderIngredients(guiGraphics);
    }
    
    void renderTooltips(GuiGraphics guiGraphics, int mouseX, int mouseY){
        renderRecipeTooltip(guiGraphics, mouseX, mouseY);
        renderIngredientTooltip(guiGraphics, mouseX, mouseY);
    }

    private WorkstationRecipe getSelectedRecipe() {
        return this.screen.getMenu().getSelectedRecipe();
    }

    /* OUTPUT SLOT */
    private void renderOutputSlot(GuiGraphics guiGraphics){
        boolean cannotCraft = getSelectedRecipe() != null && this.screen.getMenu().getSlot(0).getItem().isEmpty();
        if(!cannotCraft) return;

        int ghostX = screen.getGuiLeft() + OUTPUT_SLOT_X;
        int ghostY = screen.getGuiTop() + OUTPUT_SLOT_Y;

        guiGraphics.fill(ghostX, ghostY, ghostX + 16, ghostY + 16, 0x30_ff0000);
        guiGraphics.renderFakeItem(getSelectedRecipe().output(), ghostX, ghostY);
        guiGraphics.fill(RenderType.guiGhostRecipeOverlay(), ghostX, ghostY, ghostX + 16, ghostY + 16, 0x30_ffffff);
    }

    /* RECIPE BOOK */
    private void renderRecipes(GuiGraphics guiGraphics){
        forEachRecipeSlot((i, drawX, drawY, recipe) -> {
            renderRecipeIcon(guiGraphics, drawX, drawY, recipe);
            renderRecipeSelectionBox(guiGraphics, drawX, drawY, recipe);
        });
    }

    private void renderRecipeIcon(GuiGraphics guiGraphics, int drawX, int drawY, WorkstationRecipe recipe){
        guiGraphics.renderFakeItem(recipe.output(), drawX, drawY);
    }

    private void renderRecipeSelectionBox(GuiGraphics guiGraphics, int drawX, int drawY, WorkstationRecipe recipe){
        boolean isSelected = getSelectedRecipe() != null && getSelectedRecipe() == recipe;
        if(isSelected) guiGraphics.blit(WorkstationScreen.TEXTURE, drawX -4, drawY -4, 0, yellowBox_X_Offset, 0, boxSize, boxSize, WorkstationScreen.textureWidth, WorkstationScreen.textureHeight);
    }

    private void renderRecipeTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        forEachRecipeSlot((i, drawX, drawY, recipe) -> {
            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                guiGraphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x80FFFFFF);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, recipe.output(), mouseX, mouseY);
            }
        });
    }

    private void forEachRecipeSlot(RecipeAction action){
        int startIndex = this.screen.currentPage * recipePerPage;
        int endIndex = Math.min(startIndex + recipePerPage, screen.filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            int gridIndex = i - startIndex;
            int col = gridIndex % recipeBookColumns;
            int row = gridIndex / recipeBookColumns;
            int drawX = this.screen.getGuiLeft() + RECIPE_GRID_X + (col * recipeIconGap);
            int drawY = this.screen.getGuiTop() + RECIPE_GRID_Y + (row * recipeIconGap);

            WorkstationRecipe recipe = screen.filteredRecipes.get(i).value();
            action.accept(i, drawX, drawY, recipe);
        }
    }

    @FunctionalInterface
    private interface RecipeAction {
        void accept(int index, int drawX, int drawY, WorkstationRecipe recipe);
    }

    /* INGREDIENTS */
    private void renderIngredients(GuiGraphics guiGraphics){
        if (getSelectedRecipe() == null || Minecraft.getInstance().player == null) return;
        
        List<SizedIngredient> ingredients = getSelectedRecipe().sizedIngredients();
        Inventory playerInv = Minecraft.getInstance().player.getInventory();

        for (int i = 0; i < ingredients.size(); i++) {
            SizedIngredient sized = ingredients.get(i);
            int required = sized.count();

            int ingredientInInventory = 0;
            for (int j = 0; j < playerInv.getContainerSize(); j++) {
                ItemStack stack = playerInv.getItem(j);
                if (sized.ingredient().test(stack)) { ingredientInInventory += stack.getCount(); }
            }

            int color = (ingredientInInventory >= required) ? 0x55FF55 : 0xFF5555;
            ItemStack[] items = sized.ingredient().getItems();

            if (items.length > 0) {
                int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                int drawX = this.screen.getGuiLeft() + INGREDIENT_GRID_X + (i * ingredientIconGap);
                int drawY = this.screen.getGuiTop() + INGREDIENT_GRID_Y;

                String text = "x" + required;

                guiGraphics.renderFakeItem(items[cycleIndex], drawX, drawY);
                guiGraphics.drawString(Minecraft.getInstance().font, text, drawX + 19 - Minecraft.getInstance().font.width(text), drawY + 21, color, true);


                if (ingredientInInventory < required) renderInsufficientIngredientBox(guiGraphics, drawX, drawY);
            }
        }
    }

    private void renderInsufficientIngredientBox(GuiGraphics guiGraphics, int drawX, int drawY){
        guiGraphics.blit(WorkstationScreen.TEXTURE, drawX -4, drawY -4, 0, redBox_X_Offset, 0, boxSize, boxSize, WorkstationScreen.textureWidth, WorkstationScreen.textureHeight);
    }

    private void renderIngredientTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY){
        if (getSelectedRecipe() == null || Minecraft.getInstance().player == null) return;

        List<SizedIngredient> ingredients = getSelectedRecipe().sizedIngredients();
        for (int i = 0; i < ingredients.size(); i++) {
            int drawX = this.screen.getGuiLeft() + INGREDIENT_GRID_X + (i * ingredientIconGap);
            int drawY = this.screen.getGuiTop()  + INGREDIENT_GRID_Y;

            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                ItemStack[] items = ingredients.get(i).ingredient().getItems();

                if (items.length == 0) return;

                int cycleIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                guiGraphics.renderTooltip(Minecraft.getInstance().font, items[cycleIndex], mouseX, mouseY);
            }
        }
    }

    /* SEARCH */
    void updateSearch(String query) {
        this.screen.filteredRecipes.clear();
        String lowerQuery = query.toLowerCase();

        for (RecipeHolder<WorkstationRecipe> recipe : screen.allRecipes) {
            String itemName = recipe.value().output().getHoverName().getString().toLowerCase();
            if (itemName.contains(lowerQuery)) {
                this.screen.filteredRecipes.add(recipe);
            }
        }
        this.screen.currentPage = 0;
    }

}
