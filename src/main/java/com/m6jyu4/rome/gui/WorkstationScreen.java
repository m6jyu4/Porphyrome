package com.m6jyu4.rome.gui;

import com.m6jyu4.rome.network.WorkstationCreativeCloneMessage;
import com.m6jyu4.rome.recipe.WorkstationRecipe;
import com.m6jyu4.rome.network.WorkstationRecipeMessage;
import com.m6jyu4.rome.registry.ModClientEvent;
import com.m6jyu4.rome.registry.ModRecipe;

import com.m6jyu4.rome.Constant;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
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

public class WorkstationScreen extends AbstractContainerScreen<WorkstationMenu> {

    private static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_workstation1.png");

    private List<RecipeHolder<WorkstationRecipe>> allRecipes      = new ArrayList<>();
    private List<RecipeHolder<WorkstationRecipe>> filteredRecipes = new ArrayList<>();

    private EditBox searchBox;
    private Button prevButton;
    private Button nextButton;
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
        this.titleLabelX = 1000;

        // load recipes
        if (this.minecraft != null && this.minecraft.level != null) {
            this.allRecipes = this.minecraft.level.getRecipeManager().getAllRecipesFor(ModRecipe.WORKSTATION_TYPE.get());
            this.filteredRecipes = new ArrayList<>(this.allRecipes);
        }

        // add search Bar
        this.searchBox = new CustomEditBox(this.font, this.leftPos + 26, this.topPos + 16, 78, 14, Component.translatable("gui.rome.search"));
        searchBox.setMaxLength(50);

        this.searchBox.setResponder(this::updateSearch);
        this.addRenderableWidget(this.searchBox);

        // UI widgets
//        this.prevButton = Button.builder(Component.literal("<"), b -> changePage(-1)).bounds(this.leftPos + 76, this.topPos + 4, 12, 16).build();
//        this.nextButton = Button.builder(Component.literal(">"), b -> changePage( 1)).bounds(this.leftPos + 89, this.topPos + 4, 12, 16).build();

//        this.addRenderableWidget(this.prevButton);
//        this.addRenderableWidget(this.nextButton);

        //updatePageButtons();
    }

    // --- search ---
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
        //updatePageButtons();
    }

    private void changePage(int direction) {
        this.currentPage += direction;
        //updatePageButtons();
    }

//    private void updatePageButtons() {
//        int maxPages = (int) Math.ceil((double) filteredRecipes.size() / RECIPES_PER_PAGE);
//        this.prevButton.active = this.currentPage > 0;
//        this.nextButton.active = this.currentPage < maxPages - 1;
//    }


    // --- rendering ---
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
            int gridIndex = i - startIndex; // 0 to 11
            int col = gridIndex % COLUMNS;
            int row = gridIndex / COLUMNS;

            int drawX = this.leftPos + GRID_START_X + (col * RECIPE_ICON_GAP);
            int drawY = this.topPos + GRID_START_Y + (row * RECIPE_ICON_GAP);

            WorkstationRecipe recipe = filteredRecipes.get(i).value();
            guiGraphics.renderFakeItem(recipe.output(), drawX, drawY);

            // Highlight selected
            if (this.menu.getSelectedRecipe() != null && this.menu.getSelectedRecipe() == recipe) {
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, 200);
                guiGraphics.fill(drawX, drawY, drawX + 16, drawY + 16, 0x8000FF00);
                guiGraphics.pose().popPose();

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

    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.playerInventoryTitle, INVENTORY_START_X, INVENTORY_START_Y - 12, 4210752, false);
        drawRecipeIngredients(graphics);
    }






    @Override
    protected void containerTick() {
        super.containerTick();
        if (this.menu.getSelectedRecipe() != null) {
            this.menu.updateOutputSlot();
        }
    }

    private void triggerCreativePickItem(ItemStack stack) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        // 1. Create a copy and set it to a full stack size
        ItemStack fullStack = stack.copy();
        fullStack.setCount(fullStack.getMaxStackSize());

        // 2. Immediately set the carried stack on the client side for visual responsiveness
        this.menu.setCarried(fullStack);

        // 3. Send our custom packet to tell the server to update the cursor item
        PacketDistributor.sendToServer(new WorkstationCreativeCloneMessage(fullStack));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {

        // creative mode item clone
        if (mouseButton == 2) {
            if (this.minecraft != null && this.minecraft.player != null && this.minecraft.player.isCreative()) {
                WorkstationRecipe selected = this.menu.getSelectedRecipe();
                if (selected == null) return true;

                List<SizedIngredient> ingredients = selected.sizedIngredients();
                for (int i = 0; i < ingredients.size(); i++) {
                    int drawX = this.leftPos + INGREDIENT_START_X + (i * INGREDIENT_ICON_GAP);
                    int drawY = this.topPos  + INGREDIENT_START_Y;

                    boolean onIngredientIconPos = mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16;
                    if (!onIngredientIconPos) return true;

                    ItemStack[] items = ingredients.get(i).ingredient().getItems();
                    if (items.length == 0) return true;

                    int ingredientIndex = (int) ((System.currentTimeMillis() / 1000) % items.length);
                    ItemStack clickedStack = items[ingredientIndex].copy();
                    clickedStack.setCount(clickedStack.getMaxStackSize());

                    triggerCreativePickItem(clickedStack);
                    return true;

                }
            }
        }

        if (mouseButton == 0) {
            int startIndex = currentPage * RECIPES_PER_PAGE;
            int endIndex = Math.min(startIndex + RECIPES_PER_PAGE, filteredRecipes.size());

            // 1. Check if clicking a recipe
            for (int i = startIndex; i < endIndex; i++) {
                int gridIndex = i - startIndex;
                int col = gridIndex % COLUMNS;
                int row = gridIndex / COLUMNS;

                int drawX = this.leftPos + GRID_START_X + (col * RECIPE_ICON_GAP);
                int drawY = this.topPos + GRID_START_Y + (row * RECIPE_ICON_GAP);

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    RecipeHolder<WorkstationRecipe> clickedRecipe = filteredRecipes.get(i);
                    this.menu.setSelectedRecipe(clickedRecipe.value());
                    PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, clickedRecipe.id()));
                    return true;
                }
            }

//             2. If hovering over inventory slot or interacting with SearchBox/Buttons, skip deselection
//            if (this.hoveredSlot != null || searchBox.isMouseOver(mouseX, mouseY) || prevButton.isMouseOver(mouseX, mouseY) || nextButton.isMouseOver(mouseX, mouseY)) {
//                return super.mouseClicked(mouseX, mouseY, button);
//            }

//            if (this.hoveredSlot != null || searchBox.isMouseOver(mouseX, mouseY) || nextButton.isMouseOver(mouseX, mouseY)) {
//                return super.mouseClicked(mouseX, mouseY, button);
//            }

            if (this.hoveredSlot != null || searchBox.isMouseOver(mouseX, mouseY)) {
                return super.mouseClicked(mouseX, mouseY, mouseButton);
            }

            // 3. Deselection Logic (Clicking empty grid space)
            int gridMinX = this.leftPos + GRID_START_X;
            int gridMinY = this.topPos + GRID_START_Y;
            int gridMaxX = gridMinX + (COLUMNS * RECIPE_ICON_GAP);
            int gridMaxY = gridMinY + (ROWS * RECIPE_ICON_GAP);

            if (mouseX >= gridMinX && mouseX < gridMaxX && mouseY >= gridMinY && mouseY < gridMaxY) {
                this.menu.setSelectedRecipe(null);
                PacketDistributor.sendToServer(new WorkstationRecipeMessage(this.menu.containerId, ResourceLocation.fromNamespaceAndPath("rome", "clear")));
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, mouseButton);

    }

    // --- Helper Render Methods ---

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
                    //graphics.drawString(font, text, drawX + 16 + 4 - font.width(text), drawy + 16 + 4 + 1, isSufficientCount ? ColorStyles.TEXT_SUCCESS : ColorStyles.TEXT_ERROR);
                }
            }
        }
    }
}
