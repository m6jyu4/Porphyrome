package com.m6jyu4.rome.gui.forum;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.network.OpenBuildingMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;

public class ForumScreen extends AbstractContainerScreen<ForumMenu> {

    static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_forum.png");

    private boolean isMapView = true;
    private Button toggleViewButton;

    // --- NEW: Panning & Zooming State Variables ---
    private double zoomLevel = 15.0; // Pixels per block. Higher = Zoomed in.
    private double panX = 0.0;
    private double panY = 0.0;

    public ForumScreen(ForumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        this.toggleViewButton = Button.builder(Component.literal("Toggle View"), b -> {
            this.isMapView = !this.isMapView;
        }).bounds(this.leftPos + 105, this.topPos + 4, 65, 16).build();

        this.addRenderableWidget(this.toggleViewButton);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
        renderBuildingTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(this.font, this.menu.cityName, this.titleLabelX - 20, 8, 0x404040, false);

        if (isMapView) {
            drawMapView(graphics);
        } else {
            drawListView(graphics);
        }
    }

    private void drawMapView(GuiGraphics graphics) {
        Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;
        String facingName = facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);
        graphics.drawString(this.font, "City Map (Facing: " + facingName + ")", 20, 22, 0x000000, false);

        // Incorporate panning into the center offset
        int mapCenterX = (this.imageWidth / 2) + (int) panX;
        int mapCenterY = (this.imageHeight / 2) + 10 + (int) panY;

        // --- NEW: CLIPPING (Scissor) ---
        // This ensures the map items do not draw outside the GUI bounds when you drag them!
        int clipMinX = this.leftPos + 7;
        int clipMinY = this.topPos + 35; // Start below the title text
        int clipMaxX = this.leftPos + this.imageWidth - 7;
        int clipMaxY = this.topPos + this.imageHeight - 7;
        graphics.enableScissor(clipMinX, clipMinY, clipMaxX, clipMaxY);

        // 1. Draw boundary background
        int bgSize = (int) (this.menu.radius * zoomLevel * 2);
        graphics.fill(mapCenterX - bgSize/2, mapCenterY - bgSize/2, mapCenterX + bgSize/2, mapCenterY + bgSize/2, 0x20000000);

        // 2. Draw Forum center
        graphics.fill(mapCenterX - 1, mapCenterY - 1, mapCenterX + 1, mapCenterY + 1, 0xFFFF0000);
        graphics.drawString(this.font, "Forum", mapCenterX - 14, mapCenterY - 18, 0x555555, false);

        // 3. Draw Buildings
        for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
            int relX = entry.pos().getX() - this.menu.forumPos.getX();
            int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

            int rotX = relX, rotZ = relZ;
            if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
            else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
            else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

            int drawX = mapCenterX + (int)(rotX * zoomLevel) - 8;
            int drawY = mapCenterY + (int)(rotZ * zoomLevel) - 8;

            graphics.renderFakeItem(entry.icon(), drawX, drawY);
        }

        // Disable clipping so normal UI elements can draw again
        graphics.disableScissor();
    }

    private void drawListView(GuiGraphics graphics) {
        graphics.drawString(this.font, "Settlement Tier: " + this.menu.tier, 20, 30, 0x000000, false);
        graphics.drawString(this.font, "Total Area: " + (this.menu.radius * 2) + "x" + (this.menu.radius * 2), 20, 45, 0x000000, false);
        graphics.drawString(this.font, "Building Roster:", 20, 75, 0x404040, false);

        for (int i = 0; i < this.menu.buildings.size(); i++) {
            int col = i % 8;
            int row = i / 8;
            int drawX = 20 + (col * 18);
            int drawY = 90 + (row * 18);
            graphics.renderFakeItem(this.menu.buildings.get(i).icon(), drawX, drawY);
        }
    }

    // --- NEW: Mouse Panning (Dragging) ---
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMapView && button == 0) { // 0 = Left click drag
            // Adjust the pan offset when the mouse is dragged
            this.panX += dragX;
            this.panY += dragY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    // --- NEW: Mouse Zooming (Scrolling) ---
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMapView) {
            // Increase or decrease zoom level (scrollY is usually 1.0 or -1.0)
            this.zoomLevel += scrollY * 3.0;

            // Clamp the zoom so they can't zoom in forever or zoom out to a single pixel
            this.zoomLevel = Math.max(2.0, Math.min(this.zoomLevel, 60.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    // --- Interaction ---
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !this.toggleViewButton.isMouseOver(mouseX, mouseY)) {
            if (isMapView) {
                Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;

                // Add the Pan offset to the absolute center calculation
                int mapCenterX = this.leftPos + (this.imageWidth / 2) + (int) panX;
                int mapCenterY = this.topPos + (this.imageHeight / 2) + 10 + (int) panY;

                for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                    int relX = entry.pos().getX() - this.menu.forumPos.getX();
                    int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

                    int rotX = relX, rotZ = relZ;
                    if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
                    else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
                    else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

                    int drawX = mapCenterX + (int)(rotX * zoomLevel) - 8;
                    int drawY = mapCenterY + (int)(rotZ * zoomLevel) - 8;

                    // Ensure they aren't clicking a building that is clipped outside the map area!
                    if (mouseY >= this.topPos + 35 && mouseY <= this.topPos + this.imageHeight - 7) {
                        if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                            openRemoteBuilding(entry.pos());
                            return true;
                        }
                    }
                }
            } else {
                int startX = this.leftPos + 20;
                int startY = this.topPos + 90;

                for (int i = 0; i < this.menu.buildings.size(); i++) {
                    int col = i % 8;
                    int row = i / 8;
                    int drawX = startX + (col * 18);
                    int drawY = startY + (row * 18);

                    if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                        openRemoteBuilding(this.menu.buildings.get(i).pos());
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void openRemoteBuilding(net.minecraft.core.BlockPos pos) {
        PacketDistributor.sendToServer(new OpenBuildingMessage(pos));
        this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
    }

    // --- Tooltips ---
    private void renderBuildingTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isMapView) {
            // Only show tooltips if the mouse is inside the clipped map boundary
            if (mouseY < this.topPos + 35 || mouseY > this.topPos + this.imageHeight - 7) return;

            Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;

            // Add the Pan offset
            int mapCenterX = this.leftPos + (this.imageWidth / 2) + (int) panX;
            int mapCenterY = this.topPos + (this.imageHeight / 2) + 10 + (int) panY;

            for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                int relX = entry.pos().getX() - this.menu.forumPos.getX();
                int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

                int rotX = relX, rotZ = relZ;
                if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
                else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
                else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

                int drawX = mapCenterX + (int)(rotX * zoomLevel) - 8;
                int drawY = mapCenterY + (int)(rotZ * zoomLevel) - 8;

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    graphics.renderTooltip(this.font, entry.icon(), mouseX, mouseY);
                }
            }
        } else {
            int startX = this.leftPos + 20;
            int startY = this.topPos + 90;
            for (int i = 0; i < this.menu.buildings.size(); i++) {
                int col = i % 8;
                int row = i / 8;
                int drawX = startX + (col * 18);
                int drawY = startY + (row * 18);

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    graphics.renderTooltip(this.font, this.menu.buildings.get(i).icon(), mouseX, mouseY);
                }
            }
        }
    }
}
