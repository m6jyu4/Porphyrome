package com.m6jyu4.rome.gui.workstation;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.network.WorkstationCreativeCloneMessage;
import com.m6jyu4.rome.network.WorkstationRecipeMessage;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

class WorkstationInputHandler {

    private final WorkstationScreen screen;

    WorkstationInputHandler(WorkstationScreen screen) {
        this.screen = screen;
    }

    void handleMouseClick(double mouseX, double mouseY, int mouseButton){
        if (mouseButton == 2) handleMiddleClick(mouseX, mouseY);
        if (mouseButton == 0) handleLeftClick(mouseX, mouseY);
    }

    /* MIDDLE CLICK */
    private void handleMiddleClick(double mouseX, double mouseY) {
        if (!(this.screen.getMinecraft().player.isCreative())) return;

        if (tryCloneRecipe(mouseX, mouseY)) return;

        WorkstationRecipe selectedRecipe = this.screen.getMenu().getSelectedRecipe();
        if (selectedRecipe == null) return;

        tryCloneOutput(selectedRecipe, mouseX, mouseY);
        tryCloneIngredient(selectedRecipe, mouseX, mouseY);
    }

    private boolean tryCloneRecipe(double mouseX, double mouseY) {
        int startIndex = this.screen.currentPage * WorkstationUI.recipePerPage;
        int endIndex = Math.min(startIndex + WorkstationUI.recipePerPage, this.screen.filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            if(!isMouseOverRecipe(i, startIndex, mouseX, mouseY)) continue;

            RecipeHolder<WorkstationRecipe> clickedRecipe = this.screen.filteredRecipes.get(i);
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
        if (this.screen.getMinecraft().player == null) return;

        if (!this.screen.getMenu().getCarried().isEmpty()) return;

        itemStack.setCount(itemStack.getMaxStackSize());

        this.screen.getMenu().setCarried(itemStack);
        PacketDistributor.sendToServer(new WorkstationCreativeCloneMessage(itemStack));
    }

    /* LEFT CLICK */
    private void handleLeftClick(double mouseX, double mouseY) {
        if (this.screen.searchBox != null && !this.screen.searchBox.isMouseOver(mouseX, mouseY)) {
            this.screen.searchBox.setFocused(false);
        }

        if(trySelectRecipe(mouseX, mouseY)) return;
        if(tryTurningPage(mouseX, mouseY)) return;


        deselectAction(mouseX, mouseY);
    }

    private boolean trySelectRecipe(double mouseX, double mouseY) {
        int startIndex = this.screen.currentPage * WorkstationUI.recipePerPage;
        int endIndex = Math.min(startIndex + WorkstationUI.recipePerPage, this.screen.filteredRecipes.size());

        for (int i = startIndex; i < endIndex; i++) {
            if(!isMouseOverRecipe(i, startIndex, mouseX, mouseY)) continue;

            RecipeHolder<WorkstationRecipe> clickedRecipe = this.screen.filteredRecipes.get(i);
            this.screen.getMenu().setSelectedRecipe(clickedRecipe.value());
            PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.screen.getMenu().containerId, clickedRecipe.id()));

            return true;
        }

        return false;
    }

    private boolean tryTurningPage(double mouseX, double mouseY){
        if(turnNextPage(mouseX, mouseY)) return true;
        return turnPreviousPage(mouseX, mouseY);
    }

    private boolean turnNextPage(double mouseX, double mouseY) {
        if (!isMouseOverRightArrow(mouseX, mouseY)) return false;

        int maxPages = (int) Math.ceil((double) this.screen.filteredRecipes.size() / WorkstationUI.recipePerPage);

        if (this.screen.currentPage + 1 < maxPages) {
            this.screen.currentPage++;
            return true;
        }
        return false;
    }

    private boolean turnPreviousPage(double mouseX, double mouseY) {
        if (!isMouseOverLeftArrow(mouseX, mouseY)) return false;

        if (this.screen.currentPage > 0) {
            this.screen.currentPage--;
            return true;
        }
        return false;
    }

    private void deselectAction(double mouseX, double mouseY){




        deselectRecipe(mouseX, mouseY);
    }

    private void deselectRecipe(double mouseX, double mouseY) {
        if (!isMouseOverRecipeGrid(mouseX, mouseY)) return;

        this.screen.getMenu().setSelectedRecipe(null);
        PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.screen.getMenu().containerId, Constant.rl("workstation.recipe_clear")));
    }

    /* HELPER METHOD */
    private boolean isOutputSlotEmpty() {
        return this.screen.getMenu().getSlot(0).getItem().isEmpty();
    }

    private boolean isIngredientSlotEmpty(ItemStack[] items){
        return items.length == 0;
    }

    private boolean isMouseOverOutputSlot(double mouseX, double mouseY){
        int drawX = this.screen.getGuiLeft() + WorkstationUI.OUTPUT_SLOT_X;
        int drawY = this.screen.getGuiTop()  + WorkstationUI.OUTPUT_SLOT_Y;
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverIngredientSlot(int i, double mouseX, double mouseY){
        int drawX = this.screen.getGuiLeft() + WorkstationUI.INGREDIENT_GRID_X + (i * WorkstationUI.ingredientIconGap);
        int drawY = this.screen.getGuiTop()  + WorkstationUI.INGREDIENT_GRID_Y;
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverRecipe(int i, int startIndex, double mouseX, double mouseY){
        int gridIndex = i - startIndex;
        int col = gridIndex % WorkstationUI.recipeBookColumns;
        int row = gridIndex / WorkstationUI.recipeBookColumns;

        int drawX = this.screen.getGuiLeft() + WorkstationUI.RECIPE_GRID_X + (col * WorkstationUI.recipeIconGap);
        int drawY = this.screen.getGuiTop() + WorkstationUI.RECIPE_GRID_Y + (row * WorkstationUI.recipeIconGap);
        return isMouseInBounds(mouseX, mouseY, drawX, drawY, 16, 16);
    }

    private boolean isMouseOverRecipeGrid(double mouseX, double mouseY){
        int gridMinX = this.screen.getGuiLeft() + WorkstationUI.RECIPE_GRID_X;
        int gridMinY = this.screen.getGuiTop() + WorkstationUI.RECIPE_GRID_Y;
        int gridMaxX = gridMinX + (WorkstationUI.recipeBookColumns * WorkstationUI.recipeIconGap);
        int gridMaxY = gridMinY + (WorkstationUI.recipeBookRows * WorkstationUI.recipeIconGap);

        return mouseX >= gridMinX && mouseX < gridMaxX && mouseY >= gridMinY && mouseY < gridMaxY;
    }

    boolean isMouseOverLeftArrow(double mouseX, double mouseY) {
        int localX = (int) (mouseX - this.screen.getGuiLeft());
        int localY = (int) (mouseY - this.screen.getGuiTop());

        if (localX < 28 || localX > 38 || localY < 191 || localY > 207) {
            return false;
        }

        if (localX >= 36 && localX <= 38) {
            return localY >= 191 && localY <= 207;
        }

        return switch (localX) {
            case 35 -> localY >= 192 && localY <= 207;
            case 34 -> localY >= 193 && localY <= 206;
            case 33 -> localY >= 194 && localY <= 205;
            case 32 -> localY >= 195 && localY <= 204;
            case 31 -> localY >= 196 && localY <= 203;
            case 30 -> localY >= 197 && localY <= 202;
            case 29 -> localY >= 198 && localY <= 201;
            case 28 -> localY >= 199 && localY <= 200;
            default -> false;
        };
    }

    boolean isMouseOverRightArrow(double mouseX, double mouseY) {
        int localX = (int) (mouseX - this.screen.getGuiLeft());
        int localY = (int) (mouseY - this.screen.getGuiTop());

        if (localX < 83 || localX > 93 || localY < 191 || localY > 207) {
            return false;
        }

        if (localX >= 83 && localX <= 85) {
            return localY >= 191 && localY <= 207;
        }

        return switch (localX) {
            case 86 -> localY >= 192 && localY <= 207;
            case 87 -> localY >= 193 && localY <= 206;
            case 88 -> localY >= 194 && localY <= 205;
            case 89 -> localY >= 195 && localY <= 204;
            case 90 -> localY >= 196 && localY <= 203;
            case 91 -> localY >= 197 && localY <= 202;
            case 92 -> localY >= 198 && localY <= 201;
            case 93 -> localY >= 199 && localY <= 200;
            default -> false;
        };
    }

    private boolean isMouseInBounds(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

}

