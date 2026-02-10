package net.optifine.shaders.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.MathHelper;
import net.optifine.shaders.config.ShaderOption;

public class GuiSliderShaderOption extends GuiButtonShaderOption {
    public boolean dragging;
    private float sliderValue;
    private final ShaderOption shaderOption;

    public GuiSliderShaderOption(int buttonId, int x, int y, int w, int h, ShaderOption shaderOption, String text) {
        super(buttonId, x, y, w, h, shaderOption, text);
        this.shaderOption = shaderOption;
        sliderValue = shaderOption.getIndexNormalized();
        displayString = GuiShaderOptions.getButtonText(shaderOption, width);
    }

    protected int getHoverState(boolean mouseOver) {
        return 0;
    }

    protected void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
        if (visible) {
            if (dragging && !GuiScreen.isShiftKeyDown()) {
                sliderValue = (float) (mouseX - (xPosition + 4)) / (float) (width - 8);
                sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
                shaderOption.setIndexNormalized(sliderValue);
                sliderValue = shaderOption.getIndexNormalized();
                displayString = GuiShaderOptions.getButtonText(shaderOption, width);
            }

            mc.getTextureManager().bindTexture(buttonTextures);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)), yPosition, 0, 66, 4, 20);
            drawTexturedModalRect(xPosition + (int) (sliderValue * (float) (width - 8)) + 4, yPosition, 196, 66, 4, 20);
        }
    }

    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (super.mousePressed(mc, mouseX, mouseY)) {
            sliderValue = (float) (mouseX - (xPosition + 4)) / (float) (width - 8);
            sliderValue = MathHelper.clamp(sliderValue, 0.0F, 1.0F);
            shaderOption.setIndexNormalized(sliderValue);
            displayString = GuiShaderOptions.getButtonText(shaderOption, width);
            dragging = true;
            return true;
        } else {
            return false;
        }
    }

    public void mouseReleased(int mouseX, int mouseY) {
        dragging = false;
    }

    public void valueChanged() {
        sliderValue = shaderOption.getIndexNormalized();
    }

    public boolean isSwitchable() {
        return false;
    }
}
