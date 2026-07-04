package com.m6jyu4.rome.gui.forum;

import com.m6jyu4.rome.Constant;
import com.m6jyu4.rome.gui.CustomEditBox;
import com.m6jyu4.rome.network.AnnexBuildingMessage;
import com.m6jyu4.rome.network.OpenBuildingMessage;
import com.m6jyu4.rome.network.ToggleAnnexPermissionMessage;
import com.m6jyu4.rome.network.UpdateCityStyleMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ForumScreen extends AbstractContainerScreen<ForumMenu> {

    static final ResourceLocation TEXTURE = Constant.rl("textures/gui/menu_forum.png");

    private boolean isMapView = true;
    private Button toggleViewButton;
    private CustomEditBox nameBox;
    private Button colorButton;

    private double zoomLevel = 15.0;
    private double panX = 0.0;
    private double panY = 0.0;

    private Button toggleAnnexButton;
    private ForumMenu.BuildingEntry selectedForeignBuilding = null; // Tracks our pop-up


    // A predefined palette of historical/aesthetic colors for empires
    private static final int[] PALETTE = {
            0xFFAA00, // Gold
            0xAA0000, // Roman Red
            0x55FF55, // Emerald
            0x00AAAA, // Cyan
            0x5555FF, // Azure
            0xAA00AA, // Imperial Purple
            0xFFFFFF  // Marble White
    };
    private int currentColorIndex = 0;

    // Helper record for the Grid Outline algorithm
    private record GridPoint(int x, int z) {}

    public ForumScreen(ForumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;

        // Find our initial color index from the menu's buildings
        for (ForumMenu.BuildingEntry entry : menu.buildings) {
            if (entry.isOwned()) {
                for (int i = 0; i < PALETTE.length; i++) {
                    if (PALETTE[i] == entry.color()) this.currentColorIndex = i;
                }
                break;
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        // 1. City Name EditBox
        this.nameBox = new CustomEditBox(this.font, this.leftPos + 7, this.topPos + 6, 85, 12, Component.literal("City Name"));
        this.nameBox.setValue(this.menu.cityName);
        this.addRenderableWidget(this.nameBox);

        // 2. Color Picker Button
        this.colorButton = Button.builder(Component.empty(), b -> {
            this.currentColorIndex = (this.currentColorIndex + 1) % PALETTE.length;
            sendStyleUpdate();
        }).bounds(this.leftPos + 95, this.topPos + 4, 12, 16).build();
        this.addRenderableWidget(this.colorButton);

        // 3. Toggle View Button
        this.toggleViewButton = Button.builder(Component.literal("View"), b -> {
            this.isMapView = !this.isMapView;
        }).bounds(this.leftPos + 110, this.topPos + 4, 55, 16).build();
        this.addRenderableWidget(this.toggleViewButton);

        if (this.minecraft.player != null && this.minecraft.player.getUUID().equals(this.menu.ownerUUID)) {
            String btnText = this.menu.allowAnnex ? "Borders: OPEN" : "Borders: CLOSED";
            this.toggleAnnexButton = Button.builder(Component.literal(btnText), b -> {
                this.menu.allowAnnex = !this.menu.allowAnnex;
                b.setMessage(Component.literal(this.menu.allowAnnex ? "Borders: OPEN" : "Borders: CLOSED"));
                PacketDistributor.sendToServer(new ToggleAnnexPermissionMessage(this.menu.forumPos, this.menu.allowAnnex));
            }).bounds(this.leftPos + 7, this.topPos + 145, 90, 16).build();
            this.addRenderableWidget(this.toggleAnnexButton);
        }
    }

    private void sendStyleUpdate() {
        int newColor = PALETTE[this.currentColorIndex];
        PacketDistributor.sendToServer(new UpdateCityStyleMessage(this.menu.forumPos, this.nameBox.getValue(), newColor));

        // Update local buildings instantly for smooth UI
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            ForumMenu.BuildingEntry e = this.menu.buildings.get(i);
            if (e.isOwned()) {
                this.menu.buildings.set(i, new ForumMenu.BuildingEntry(e.pos(), e.icon(), e.isOwned(), newColor, e.ownerForumPos(), e.allowAnnex()));
            }
        }
    }



    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        // If the player is actively typing inside the name box, feed the characters directly to it
        if (this.nameBox != null && this.nameBox.isFocused()) {
            return this.nameBox.charTyped(codePoint, modifiers);
        }
        return super.charTyped(codePoint, modifiers);
    }

    // --- Rendering ---
    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);

        // Draw color on the picker button
        graphics.fill(this.colorButton.getX() + 2, this.colorButton.getY() + 2, this.colorButton.getX() + 10, this.colorButton.getY() + 14, 0xFF000000 | PALETTE[this.currentColorIndex]);

        this.renderTooltip(graphics, mouseX, mouseY);
        renderBuildingTooltips(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        graphics.blit(TEXTURE, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isMapView) drawMapView(graphics);
        else drawListView(graphics);
    }

    private record CityClusterKey(BlockPos ownerForumPos, int color) {}

    private void drawMapView(GuiGraphics graphics) {
        Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;
        String facingName = facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);
        graphics.drawString(this.font, "Map (Facing: " + facingName + ")", 7, 24, 0x404040, false);

        int mapCenterX = (this.imageWidth / 2) + (int) panX;
        int mapCenterY = (this.imageHeight / 2) + 12 + (int) panY;

        graphics.enableScissor(this.leftPos + 7, this.topPos + 35, this.leftPos + this.imageWidth - 7, this.topPos + this.imageHeight - 7);

        // Group buildings by UNIQUE CITY (ownerForumPos + color combo) to prevent identical colors from merging
        Map<CityClusterKey, Set<GridPoint>> cityClusters = new HashMap<>();
        Map<GridPoint, Boolean> ownershipMap = new HashMap<>(); // Track if a rendered point is owned

        for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
            int relX = entry.pos().getX() - this.menu.forumPos.getX();
            int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

            int rotX = relX, rotZ = relZ;
            if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
            else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
            else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

            GridPoint point = new GridPoint(rotX, rotZ);
            CityClusterKey key = new CityClusterKey(entry.ownerForumPos(), entry.color());

            cityClusters.computeIfAbsent(key, k -> new HashSet<>()).add(point);
            ownershipMap.put(point, entry.isOwned());

            // Draw Icon
            int drawX = mapCenterX + (int)(rotX * zoomLevel) - 8;
            int drawY = mapCenterY + (int)(rotZ * zoomLevel) - 8;
            graphics.renderFakeItem(entry.icon(), drawX, drawY);

            // Draw Translucent Overlay for foreign buildings
            if (!entry.isOwned()) {
                graphics.fill(net.minecraft.client.renderer.RenderType.guiGhostRecipeOverlay(), drawX, drawY, drawX + 16, drawY + 16, 0x90_ffffff);
            }
        }

        // --- WATERTIGHT BORDER OUTLINE ALGORITHM ---
        for (Map.Entry<CityClusterKey, Set<GridPoint>> cluster : cityClusters.entrySet()) {
            int rawColor = cluster.getKey().color();
            Set<GridPoint> points = cluster.getValue();

            for (GridPoint p : points) {
                // Determine if this cell is owned by us to apply shading to the border line
                boolean isOwned = ownershipMap.getOrDefault(p, true);
                int argbColor;

                if (isOwned) {
                    argbColor = 0xFF000000 | rawColor; // Normal solid border
                } else {
                    // Apply the 0x90_ffffff shading tint directly onto the foreign border lines!
                    int r = Math.min(255, (int) (((rawColor >> 16) & 0xFF) * 0.55 + 255 * 0.45));
                    int g = Math.min(255, (int) (((rawColor >> 8) & 0xFF) * 0.55 + 255 * 0.45));
                    int b = Math.min(255, (int) ((rawColor & 0xFF) * 0.55 + 255 * 0.45));
                    argbColor = 0xFF000000 | (r << 16) | (g << 8) | b;
                }

                // Grid cell borders based on zoomLevel
                int cellLeft   = mapCenterX + (int)((p.x - 0.5) * zoomLevel);
                int cellTop    = mapCenterY + (int)((p.z - 0.5) * zoomLevel);
                int cellRight  = mapCenterX + (int)((p.x + 0.5) * zoomLevel);
                int cellBottom = mapCenterY + (int)((p.z + 0.5) * zoomLevel);

                int t = 2; // Line thickness

                // Check Cardinally Adjacent Neighbors (Do we need an edge line?)
                boolean hasTop    = !points.contains(new GridPoint(p.x, p.z - 1));
                boolean hasBottom = !points.contains(new GridPoint(p.x, p.z + 1));
                boolean hasLeft   = !points.contains(new GridPoint(p.x - 1, p.z));
                boolean hasRight  = !points.contains(new GridPoint(p.x + 1, p.z));

                // 1. Draw Outer Edges (Strictly inside the cell bounds to prevent corner poke-outs!)
                if (hasTop)    graphics.fill(cellLeft, cellTop, cellRight, cellTop + t, argbColor);
                if (hasBottom) graphics.fill(cellLeft, cellBottom - t, cellRight, cellBottom, argbColor);
                if (hasLeft)   graphics.fill(cellLeft, cellTop, cellLeft + t, cellBottom, argbColor);
                if (hasRight)  graphics.fill(cellRight - t, cellTop, cellRight, cellBottom, argbColor);

                // 2. Draw Inner Notches
                // If we have blocks next to us (e.g., Top & Left), but the diagonal block is missing,
                // it creates a tiny inner gap. We draw a small t-by-t square to plug that exact gap!
                if (!hasTop && !hasLeft && !points.contains(new GridPoint(p.x - 1, p.z - 1))) {
                    graphics.fill(cellLeft, cellTop, cellLeft + t, cellTop + t, argbColor); // Top-Left Notch
                }
                if (!hasTop && !hasRight && !points.contains(new GridPoint(p.x + 1, p.z - 1))) {
                    graphics.fill(cellRight - t, cellTop, cellRight, cellTop + t, argbColor); // Top-Right Notch
                }
                if (!hasBottom && !hasLeft && !points.contains(new GridPoint(p.x - 1, p.z + 1))) {
                    graphics.fill(cellLeft, cellBottom - t, cellLeft + t, cellBottom, argbColor); // Bottom-Left Notch
                }
                if (!hasBottom && !hasRight && !points.contains(new GridPoint(p.x + 1, p.z + 1))) {
                    graphics.fill(cellRight - t, cellBottom - t, cellRight, cellBottom, argbColor); // Bottom-Right Notch
                }
            }
        }

        graphics.disableScissor();

        if (selectedForeignBuilding != null) {
            int relX = selectedForeignBuilding.pos().getX() - this.menu.forumPos.getX();
            int relZ = selectedForeignBuilding.pos().getZ() - this.menu.forumPos.getZ();

            // Calculate if it's within our strict city radius
            boolean inRange = Math.abs(relX) <= this.menu.radius && Math.abs(relZ) <= this.menu.radius;

            int rotX = relX, rotZ = relZ;
            if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
            else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
            else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

            int popX = mapCenterX + (int)(rotX * zoomLevel) + 8; // Shift to the right of the icon
            int popY = mapCenterY + (int)(rotZ * zoomLevel) - 8;

            // PUSH POSE: Move rendering layer to the very front so it floats above icons!
            graphics.pose().pushPose();
            graphics.pose().translate(0, 0, 300);

            // Draw a stylish dark pop-up box
            graphics.fill(popX, popY, popX + 50, popY + 18, 0xDD000000); // Background
            graphics.fill(popX, popY, popX + 50, popY + 1, 0xFF555555);  // Top border

            // Context Menu Logic
            if (!inRange) {
                graphics.drawString(this.font, "Too Far", popX + 7, popY + 5, 0xFF5555, false); // Red text
            } else if (selectedForeignBuilding.allowAnnex()) {
                graphics.drawString(this.font, "Annex", popX + 12, popY + 5, 0x55FF55, false); // Green text
            } else {
                graphics.drawString(this.font, "Closed", popX + 9, popY + 5, 0xFF5555, false); // Red text
            }

            // POP POSE: Restore original depth buffer
            graphics.pose().popPose();
        }
    }

    private void drawListView(GuiGraphics graphics) {
        graphics.drawString(this.font, "Settlement Tier: " + this.menu.tier, 20, 30, 0x000000, false);
        graphics.drawString(this.font, "Building Roster:", 20, 50, 0x404040, false);

        int index = 0;
        for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
            if (!entry.isOwned()) continue; // Only show owned buildings in list view
            int drawX = 20 + ((index % 8) * 18);
            int drawY = 65 + ((index / 8) * 18);
            graphics.renderFakeItem(entry.icon(), drawX, drawY);
            index++;
        }
    }

    // --- Key Input (Deselect EditBox & Save) ---
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // If the text box is focused
        if (this.nameBox != null && this.nameBox.isFocused()) {
            // 1. Submit on Enter
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                this.nameBox.setFocused(false);
                sendStyleUpdate();
                this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.0F);
                return true;
            }

            // 2. Un-focus on Escape so they can use normal hotkeys to close the GUI
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                this.nameBox.setFocused(false);
                return true;
            }

            // 3. Prevent standard hotkeys (like 'E') from closing the menu while they are typing letters!
            return this.nameBox.keyPressed(keyCode, scanCode, modifiers);
        }

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    // --- Mouse Interaction ---
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (isMapView && button == 0) {
            this.panX += dragX;
            this.panY += dragY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (isMapView) {
            this.zoomLevel = Math.max(5.0, Math.min(this.zoomLevel + (scrollY * 3.0), 60.0));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            Direction facing = this.minecraft.player.getDirection();
            int mapCenterX = this.leftPos + (this.imageWidth / 2) + (int) panX;
            int mapCenterY = this.topPos + (this.imageHeight / 2) + 12 + (int) panY;

            // 1. Check Context Menu Pop-Up Clicks FIRST
            if (isMapView && selectedForeignBuilding != null) {
                int relX = selectedForeignBuilding.pos().getX() - this.menu.forumPos.getX();
                int relZ = selectedForeignBuilding.pos().getZ() - this.menu.forumPos.getZ();
                int rotX = relX, rotZ = relZ;
                if (facing == Direction.EAST) { rotX = relZ; rotZ = -relX; }
                else if (facing == Direction.SOUTH) { rotX = -relX; rotZ = -relZ; }
                else if (facing == Direction.WEST) { rotX = -relZ; rotZ = relX; }

                int popX = mapCenterX + (int)(rotX * zoomLevel) + 8;
                int popY = mapCenterY + (int)(rotZ * zoomLevel) - 8;

                if (mouseX >= popX && mouseX <= popX + 50 && mouseY >= popY && mouseY <= popY + 18) {
                    // Check Range
                    int dx = Math.abs(selectedForeignBuilding.pos().getX() - this.menu.forumPos.getX());
                    int dz = Math.abs(selectedForeignBuilding.pos().getZ() - this.menu.forumPos.getZ());
                    boolean inRange = dx <= this.menu.radius && dz <= this.menu.radius;

                    if (!inRange) {
                        this.minecraft.player.displayClientMessage(Component.literal("§cOut of Range: You can only annex buildings inside your city radius."), true);
                    } else if (selectedForeignBuilding.allowAnnex()) {
                        PacketDistributor.sendToServer(new AnnexBuildingMessage(selectedForeignBuilding.pos(), this.menu.forumPos));
                        // The UI_CARTOGRAPHY_TABLE_TAKE_RESULT sound value might need .value() depending on your NF version
                        this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, 1.0F, 1.0F);
                        selectedForeignBuilding = null;
                    } else {
                        this.minecraft.player.displayClientMessage(Component.literal("§cDiplomacy Error: This city's borders are closed to annexation."), true);
                    }
                    return true;
                }

                // Clicked outside the pop-up -> Dismiss it
                System.out.println("DEBUG: Clicked outside the pop-up, dismissing context menu.");
                selectedForeignBuilding = null;
            }

            // 2. Check Map View Building Clicks
            if (isMapView && mouseY >= this.topPos + 35 && mouseY <= this.topPos + this.imageHeight - 7) {
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
                        System.out.println("DEBUG: Clicked building on map at " + entry.pos().toShortString() + " (Is Owned: " + entry.isOwned() + ")");
                        if (entry.isOwned()) {
                            PacketDistributor.sendToServer(new OpenBuildingMessage(entry.pos()));
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                        } else {
                            // Select foreign building
                            this.selectedForeignBuilding = entry;
                            System.out.println("DEBUG: Selected Foreign Building. Allows Annexation: " + entry.allowAnnex());
                            this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 0.5F, 1.5F);
                        }
                        return true;
                    }
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    public void updateForeignCityStyle(BlockPos ownerForumPos, String newName, int newColor) {
        // Find every building on our current map that is owned by the city that just updated
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            ForumMenu.BuildingEntry e = this.menu.buildings.get(i);

            if (e.ownerForumPos().equals(ownerForumPos)) {
                // Replace the entry with the exact same data, but inject the NEW color!
                this.menu.buildings.set(i, new ForumMenu.BuildingEntry(e.pos(), e.icon(), e.isOwned(), newColor, e.ownerForumPos(), e.allowAnnex()));
            }
        }

        // (Optional) If we were editing our own city from a different terminal, update our local name box
        if (this.menu.forumPos.equals(ownerForumPos) && !this.nameBox.isFocused()) {
            this.nameBox.setValue(newName);
        }
    }


    // --- Tooltips ---
    private void renderBuildingTooltips(GuiGraphics graphics, int mouseX, int mouseY) {
        if (isMapView) {
            if (mouseY < this.topPos + 35 || mouseY > this.topPos + this.imageHeight - 7) return;

            Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;
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
                    Component tooltipName = entry.isOwned() ?
                            entry.icon().getHoverName() :
                            Component.literal(entry.icon().getHoverName().getString() + " §7(Foreign/Neighbor)");

                    graphics.renderTooltip(this.font, tooltipName, mouseX, mouseY);
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

    public void updateBuildingOwnerLive(BlockPos buildingPos, BlockPos newOwnerForumPos, int newColor) {
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            ForumMenu.BuildingEntry e = this.menu.buildings.get(i);

            if (e.pos().equals(buildingPos)) {
                // Determine if this screen's forum now owns the building
                boolean isNowOwnedByUs = this.menu.forumPos.equals(newOwnerForumPos);

                // Swap the owner, color, and ownership status!
                this.menu.buildings.set(i, new ForumMenu.BuildingEntry(
                        e.pos(),
                        e.icon(),
                        isNowOwnedByUs,
                        newColor,
                        newOwnerForumPos,
                        e.allowAnnex()
                ));
                break;
            }
        }
    }

    public void updateAnnexPermissionLive(BlockPos ownerForumPos, boolean allowAnnex) {
        // 1. Update the background data for all foreign buildings
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            ForumMenu.BuildingEntry e = this.menu.buildings.get(i);
            if (e.ownerForumPos().equals(ownerForumPos)) {
                this.menu.buildings.set(i, new ForumMenu.BuildingEntry(e.pos(), e.icon(), e.isOwned(), e.color(), e.ownerForumPos(), allowAnnex));
            }
        }

        // 2. If it's our own city, visually update the button text!
        if (this.menu.forumPos.equals(ownerForumPos)) {
            this.menu.allowAnnex = allowAnnex;
            if (this.toggleAnnexButton != null) {
                this.toggleAnnexButton.setMessage(Component.literal(allowAnnex ? "Borders: OPEN" : "Borders: CLOSED"));
            }
        }

        // 3. If a player currently has the pop-up open for a city that just closed its borders...
        if (this.selectedForeignBuilding != null && this.selectedForeignBuilding.ownerForumPos().equals(ownerForumPos)) {
            if (!allowAnnex) {
                this.selectedForeignBuilding = null; // Auto-dismiss the pop-up to prevent cheating!
            } else {
                // Update the pop-up reference so the red "Closed" text flips to green "Annex" live!
                this.selectedForeignBuilding = new ForumMenu.BuildingEntry(
                        this.selectedForeignBuilding.pos(), this.selectedForeignBuilding.icon(),
                        this.selectedForeignBuilding.isOwned(), this.selectedForeignBuilding.color(),
                        this.selectedForeignBuilding.ownerForumPos(), allowAnnex
                );
            }
        }
    }

    public void addBuildingLive(ForumMenu.BuildingEntry entry) {
        // Only register this building if it is actually within our currently opened city limits!
        int maxDist = this.menu.radius + 1;

        // Quick radial boundary check
        if (Math.abs(entry.pos().getX() - this.menu.forumPos.getX()) <= maxDist &&
                Math.abs(entry.pos().getZ() - this.menu.forumPos.getZ()) <= maxDist) {

            // Check if this building belongs to our city (isOwned)
            boolean isOwnedByUs = this.menu.forumPos.equals(entry.ownerForumPos());

            // Re-create the entry with our current UI context and append it
            this.menu.buildings.add(new ForumMenu.BuildingEntry(
                    entry.pos(),
                    entry.icon(),
                    isOwnedByUs,
                    entry.color(),
                    entry.ownerForumPos(),
                    entry.allowAnnex()
            ));
        }
    }

    // Called live when a player breaks a building
    public void removeBuildingLive(BlockPos pos) {
        // If we are currently holding a pop-up context menu for this broken building, close it!
        if (this.selectedForeignBuilding != null && this.selectedForeignBuilding.pos().equals(pos)) {
            this.selectedForeignBuilding = null;
        }

        // Find the broken building in our list and remove it instantly
        this.menu.buildings.removeIf(entry -> entry.pos().equals(pos));
    }
}
