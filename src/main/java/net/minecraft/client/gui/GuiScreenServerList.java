package net.minecraft.client.gui;

import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiScreenServerList extends GuiScreen {
    private final GuiScreen field_146303_a;
    private final ServerData field_146301_f;
    private GuiTextField field_146302_g;

    public GuiScreenServerList(GuiScreen p_i1031_1_, ServerData p_i1031_2_) {
        field_146303_a = p_i1031_1_;
        field_146301_f = p_i1031_2_;
    }

    public void updateScreen() {
        field_146302_g.updateCursorCounter();
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 96 + 12, I18n.format("selectServer.select")));
        buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
        field_146302_g = new GuiTextField(2, fontRendererObj, width / 2 - 100, 116, 200, 20);
        field_146302_g.setMaxStringLength(128);
        field_146302_g.setFocused(true);
        field_146302_g.setText(mc.gameSettings.lastServer);
        buttonList.getFirst().enabled = !field_146302_g.getText().isEmpty() && field_146302_g.getText().split(":").length > 0;
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        mc.gameSettings.lastServer = field_146302_g.getText();
        mc.gameSettings.saveOptions();
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                field_146303_a.confirmClicked(false, 0);
            } else if (button.id == 0) {
                field_146301_f.serverIP = field_146302_g.getText();
                field_146303_a.confirmClicked(true, 0);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (field_146302_g.textboxKeyTyped(typedChar, keyCode)) {
            buttonList.getFirst().enabled = !field_146302_g.getText().isEmpty() && field_146302_g.getText().split(":").length > 0;
        } else if (keyCode == 28 || keyCode == 156) {
            actionPerformed((GuiButton) buttonList.getFirst());
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        field_146302_g.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("selectServer.direct"), width / 2, 20, 16777215);
        drawString(fontRendererObj, I18n.format("addServer.enterIp"), width / 2 - 100, 100, 10526880);
        field_146302_g.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
