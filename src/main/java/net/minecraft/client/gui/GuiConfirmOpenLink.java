package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;

import java.io.IOException;

public class GuiConfirmOpenLink extends GuiYesNo {
    private final String openLinkWarning;
    private final String copyLinkButtonText;
    private final String linkText;
    private boolean showSecurityWarning = true;

    public GuiConfirmOpenLink(GuiYesNoCallback p_i1084_1_, String linkTextIn, int p_i1084_3_, boolean p_i1084_4_) {
        super(p_i1084_1_, I18n.format(p_i1084_4_ ? "chat.link.confirmTrusted" : "chat.link.confirm"), linkTextIn, p_i1084_3_);
        confirmButtonText = I18n.format(p_i1084_4_ ? "chat.link.open" : "gui.yes");
        cancelButtonText = I18n.format(p_i1084_4_ ? "gui.cancel" : "gui.no");
        copyLinkButtonText = I18n.format("chat.copy");
        openLinkWarning = I18n.format("chat.link.warning");
        linkText = linkTextIn;
    }

    public void initGui() {
        super.initGui();
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 50 - 105, height / 6 + 96, 100, 20, confirmButtonText));
        buttonList.add(new GuiButton(2, width / 2 - 50, height / 6 + 96, 100, 20, copyLinkButtonText));
        buttonList.add(new GuiButton(1, width / 2 - 50 + 105, height / 6 + 96, 100, 20, cancelButtonText));
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.id == 2) {
            copyLinkToClipboard();
        }

        parentScreen.confirmClicked(button.id == 0, parentButtonClickedId);
    }

    public void copyLinkToClipboard() {
        setClipboardString(linkText);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);

        if (showSecurityWarning) {
            drawCenteredString(fontRendererObj, openLinkWarning, width / 2, 110, 16764108);
        }
    }

    public void disableSecurityWarning() {
        showSecurityWarning = false;
    }
}
