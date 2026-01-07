package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.IChatComponent;

import java.io.IOException;
import java.util.List;

public class GuiDisconnected extends GuiScreen {
    private final GuiScreen parentScreen;
    private final String reason;
    private final IChatComponent message;
    private List<String> multilineMessage;
    private int field_175353_i;

    public GuiDisconnected(GuiScreen screen, String reasonLocalizationKey, IChatComponent chatComp) {
        parentScreen = screen;
        reason = I18n.format(reasonLocalizationKey);
        message = chatComp;
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    public void initGui() {
        buttonList.clear();
        multilineMessage = fontRendererObj.listFormattedStringToWidth(message.getFormattedText(), width - 50);
        field_175353_i = multilineMessage.size() * fontRendererObj.FONT_HEIGHT;
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 2 + field_175353_i / 2 + fontRendererObj.FONT_HEIGHT, I18n.format("gui.toMenu")));
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            mc.displayGuiScreen(parentScreen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, reason, width / 2, height / 2 - field_175353_i / 2 - fontRendererObj.FONT_HEIGHT * 2, 11184810);
        int i = height / 2 - field_175353_i / 2;

        if (multilineMessage != null) {
            for (String s : multilineMessage) {
                drawCenteredString(fontRendererObj, s, width / 2, i, 16777215);
                i += fontRendererObj.FONT_HEIGHT;
            }
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
