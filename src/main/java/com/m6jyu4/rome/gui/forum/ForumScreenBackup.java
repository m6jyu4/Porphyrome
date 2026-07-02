package com.m6jyu4.rome.gui.forum;

import com.m6jyu4.rome.network.OpenBuildingMessage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class ForumScreenBackup extends AbstractContainerScreen<ForumMenu> {

    // You can use a custom texture, or temporarily use vanilla's generic background
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/dispenser.png");

    public ForumScreenBackup(ForumMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageHeight = 166;
        this.imageWidth = 176;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);

        int startX = this.leftPos + 20;
        int startY = this.topPos + 95;
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            int col = i % 8;
            int row = i / 8;
            int drawX = startX + (col * 18);
            int drawY = startY + (row * 18);

            if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                ForumMenu.BuildingEntry entry = this.menu.buildings.get(i);
                graphics.renderTooltip(this.font, entry.icon(), mouseX, mouseY);
            }
        }
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
        // Draw the City Name as the title
        graphics.drawString(this.font, this.menu.cityName, this.titleLabelX, 6, 0x404040, false);

        // Draw the Statistics
        graphics.drawString(this.font, "Settlement Tier: " + this.menu.tier, 20, 30, 0x000000, false);
        graphics.drawString(this.font, "Border Radius: " + this.menu.radius + " blocks", 20, 45, 0x000000, false);
        graphics.drawString(this.font, "Total Area: " + (this.menu.radius * 2) + "x" + (this.menu.radius * 2), 20, 60, 0x000000, false);

        // You can add requirements for the next tier here later!
        graphics.drawString(this.font, "Next Upgrade: Tier " + (this.menu.tier + 1), 20, 90, 0x555555, false);

        // Draw Buildings Grid
        graphics.drawString(this.font, "City Buildings:", 20, 80, 0x404040, false);

        int startX = 20;
        int startY = 95;
        for (int i = 0; i < this.menu.buildings.size(); i++) {
            int col = i % 8; // 8 buildings per row
            int row = i / 8;

            int drawX = startX + (col * 18);
            int drawY = startY + (row * 18);

            ForumMenu.BuildingEntry entry = this.menu.buildings.get(i);
            graphics.renderFakeItem(entry.icon(), drawX, drawY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int startX = this.leftPos + 20;
            int startY = this.topPos + 95;

            for (int i = 0; i < this.menu.buildings.size(); i++) {
                int col = i % 8;
                int row = i / 8;

                int drawX = startX + (col * 18);
                int drawY = startY + (row * 18);

                // If they clicked the building icon
                if (mouseX >= drawX && mouseX < drawX + 16 && mouseY >= drawY && mouseY < drawY + 16) {
                    BlockPos targetPos = this.menu.buildings.get(i).pos();

                    // SEND PACKET TO OPEN THE BUILDING REMOTE
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(new OpenBuildingMessage(targetPos));

                    // Play a click sound
                    this.minecraft.player.playSound(net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK.value(), 1.0F, 1.0F);
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
}
