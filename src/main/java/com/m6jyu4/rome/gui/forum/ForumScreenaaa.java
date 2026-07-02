package com.m6jyu4.rome.gui.forum;

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

public class ForumScreenaaa extends AbstractContainerScreen<ForumMenu> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    private boolean isMapView = true; // Default to Map View
    private Button toggleViewButton;

    public ForumScreenaaa(ForumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;

        // Add a button to toggle between Map and List view
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

        // Draw Tooltips for the buildings
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

        // Capitalize the first letter of the facing direction for the title
        String facingName = facing.getName().substring(0, 1).toUpperCase() + facing.getName().substring(1);
        graphics.drawString(this.font, "City Map (Facing: " + facingName + ")", 20, 22, 0x000000, false);

        int mapCenterX = this.imageWidth / 2;
        int mapCenterY = (this.imageHeight / 2) + 10;

        int maxOffset = 1;
        for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
            int relX = Math.abs(entry.pos().getX() - this.menu.forumPos.getX());
            int relZ = Math.abs(entry.pos().getZ() - this.menu.forumPos.getZ());
            maxOffset = Math.max(maxOffset, Math.max(relX, relZ));
        }

        double scale = Math.min(20.0, 45.0 / maxOffset);

        int bgSize = (int) (this.menu.radius * scale * 2);
        bgSize = Math.min(130, bgSize);
        graphics.fill(mapCenterX - bgSize/2, mapCenterY - bgSize/2, mapCenterX + bgSize/2, mapCenterY + bgSize/2, 0x20000000);

        graphics.fill(mapCenterX - 1, mapCenterY - 1, mapCenterX + 1, mapCenterY + 1, 0xFFFF0000);
        graphics.drawString(this.font, "Forum", mapCenterX - 14, mapCenterY - 18, 0x555555, false);

        for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
            int relX = entry.pos().getX() - this.menu.forumPos.getX();
            int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

            // --- ROTATION MATH ---
            int rotX = relX;
            int rotZ = relZ;
            if (facing == Direction.EAST) {
                rotX = relZ;
                rotZ = -relX;
            } else if (facing == Direction.SOUTH) {
                rotX = -relX;
                rotZ = -relZ;
            } else if (facing == Direction.WEST) {
                rotX = -relZ;
                rotZ = relX;
            }

            int drawX = mapCenterX + (int)(rotX * scale) - 8;
            int drawY = mapCenterY + (int)(rotZ * scale) - 8;

            graphics.renderFakeItem(entry.icon(), drawX, drawY);
        }
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

    // --- Interaction ---


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !this.toggleViewButton.isMouseOver(mouseX, mouseY)) {

            if (isMapView) {
                Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;
                int mapCenterX = this.leftPos + (this.imageWidth / 2);
                int mapCenterY = this.topPos + (this.imageHeight / 2) + 10;

                int maxOffset = 1;
                for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                    int relX = Math.abs(entry.pos().getX() - this.menu.forumPos.getX());
                    int relZ = Math.abs(entry.pos().getZ() - this.menu.forumPos.getZ());
                    maxOffset = Math.max(maxOffset, Math.max(relX, relZ));
                }
                double scale = Math.min(20.0, 45.0 / maxOffset);

                for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                    int relX = entry.pos().getX() - this.menu.forumPos.getX();
                    int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

                    // --- ROTATION MATH ---
                    int rotX = relX;
                    int rotZ = relZ;
                    if (facing == Direction.EAST) {
                        rotX = relZ; rotZ = -relX;
                    } else if (facing == Direction.SOUTH) {
                        rotX = -relX; rotZ = -relZ;
                    } else if (facing == Direction.WEST) {
                        rotX = -relZ; rotZ = relX;
                    }

                    int drawX = mapCenterX + (int)(rotX * scale) - 8;
                    int drawY = mapCenterY + (int)(rotZ * scale) - 8;

                    if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                        openRemoteBuilding(entry.pos());
                        return true;
                    }
                }
            } else {
                // List View Click Logic
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
            Direction facing = this.minecraft.player != null ? this.minecraft.player.getDirection() : Direction.NORTH;
            int mapCenterX = this.leftPos + (this.imageWidth / 2);
            int mapCenterY = this.topPos + (this.imageHeight / 2) + 10;

            int maxOffset = 1;
            for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                int relX = Math.abs(entry.pos().getX() - this.menu.forumPos.getX());
                int relZ = Math.abs(entry.pos().getZ() - this.menu.forumPos.getZ());
                maxOffset = Math.max(maxOffset, Math.max(relX, relZ));
            }
            double scale = Math.min(20.0, 45.0 / maxOffset);

            for (ForumMenu.BuildingEntry entry : this.menu.buildings) {
                int relX = entry.pos().getX() - this.menu.forumPos.getX();
                int relZ = entry.pos().getZ() - this.menu.forumPos.getZ();

                // --- ROTATION MATH ---
                int rotX = relX;
                int rotZ = relZ;
                if (facing == Direction.EAST) {
                    rotX = relZ; rotZ = -relX;
                } else if (facing == Direction.SOUTH) {
                    rotX = -relX; rotZ = -relZ;
                } else if (facing == Direction.WEST) {
                    rotX = -relZ; rotZ = relX;
                }

                int drawX = mapCenterX + (int)(rotX * scale) - 8;
                int drawY = mapCenterY + (int)(rotZ * scale) - 8;

                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    graphics.renderTooltip(this.font, entry.icon(), mouseX, mouseY);
                }
            }
        } else {
            // List View Tooltip Logic
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
