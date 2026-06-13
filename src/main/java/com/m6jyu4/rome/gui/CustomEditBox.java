package com.m6jyu4.rome.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class CustomEditBox extends EditBox {

    private Component hint = CommonComponents.EMPTY;
    private final Font font;
    private boolean isBackgroundDisabled = false;

    public CustomEditBox(Font font, int x, int y, int width, int height, Component message) {
        super(font, x, y, width, height, message);
        setBordered(true); //redundant, only for sanity
        this.font = font;
    }

    public void setTextHint(Component hint) {
        this.hint = hint;
    }

    @Override
    public void setBordered(boolean enableBackgroundDrawing) {
        // do nothing, as the **bordered** boolean field should always be true
    }

    @Override
    public int getInnerWidth() {
        return width - 8;
    }

    @Override
    public boolean isBordered() {
        return !isBackgroundDisabled;
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!isFocused() && getValue().isEmpty()) {
            // Draw your custom text hint when the edit box is unfocused and empty
            guiGraphics.drawString(font, hint, getX() + 4, getY() + (height - 8) / 2, 0xff_ffffff);
        } else {
            // Otherwise, render the normal EditBox widget with your background disable flag
            isBackgroundDisabled = true;
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
            isBackgroundDisabled = false;
        }
    }

}
