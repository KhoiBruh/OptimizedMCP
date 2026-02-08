package net.minecraft.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatFormat;

public class GuiGameOver extends GuiScreen implements GuiYesNoCallback {
    private int enableButtonsTimer;
    private final boolean field_146346_f = false;

    public void initGui() {
        buttonList.clear();

        if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
            if (mc.isIntegratedServerRunning()) {
                buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.deleteWorld")));
            } else {
                buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.leaveServer")));
            }
        } else {
            buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 72, I18n.format("deathScreen.respawn")));
            buttonList.add(new GuiButton(1, width / 2 - 100, height / 4 + 96, I18n.format("deathScreen.titleScreen")));

            if (mc.getSession() == null) {
                buttonList.get(1).enabled = false;
            }
        }

        for (GuiButton guibutton : buttonList) {
            guibutton.enabled = false;
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0:
                mc.thePlayer.respawnPlayer();
                mc.displayGuiScreen(null);
                break;

            case 1:
                if (mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                    mc.displayGuiScreen(new GuiMainMenu());
                } else {
                    GuiYesNo guiyesno = new GuiYesNo(this, I18n.format("deathScreen.quit.confirm"), "", I18n.format("deathScreen.titleScreen"), I18n.format("deathScreen.respawn"), 0);
                    mc.displayGuiScreen(guiyesno);
                    guiyesno.setButtonDelay(20);
                }
        }
    }

    public void confirmClicked(boolean result, int id) {
        if (result) {
            mc.theWorld.sendQuittingDisconnectingPacket();
            mc.loadWorld(null);
            mc.displayGuiScreen(new GuiMainMenu());
        } else {
            mc.thePlayer.respawnPlayer();
            mc.displayGuiScreen(null);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawGradientRect(0, 0, width, height, 1615855616, -1602211792);
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0F, 2.0F, 2.0F);
        boolean flag = mc.theWorld.getWorldInfo().isHardcoreModeEnabled();
        String s = flag ? I18n.format("deathScreen.title.hardcore") : I18n.format("deathScreen.title");
        drawCenteredString(fontRendererObj, s, width / 2 / 2, 30, 16777215);
        GlStateManager.popMatrix();

        if (flag) {
            drawCenteredString(fontRendererObj, I18n.format("deathScreen.hardcoreInfo"), width / 2, 144, 16777215);
        }

        drawCenteredString(fontRendererObj, I18n.format("deathScreen.score") + ": " + ChatFormat.YELLOW + mc.thePlayer.getScore(), width / 2, 100, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void updateScreen() {
        super.updateScreen();
        ++enableButtonsTimer;

        if (enableButtonsTimer == 20) {
            for (GuiButton guibutton : buttonList) {
                guibutton.enabled = true;
            }
        }
    }
}
