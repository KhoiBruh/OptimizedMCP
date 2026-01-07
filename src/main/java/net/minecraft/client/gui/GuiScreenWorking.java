package net.minecraft.client.gui;

import net.minecraft.util.IProgressUpdate;
import net.optifine.CustomLoadingScreen;
import net.optifine.CustomLoadingScreens;

public class GuiScreenWorking extends GuiScreen implements IProgressUpdate {
    private String field_146591_a = "";
    private String field_146589_f = "";
    private int progress;
    private boolean doneWorking;
    private final CustomLoadingScreen customLoadingScreen = CustomLoadingScreens.getCustomLoadingScreen();

    public void displaySavingString(String message) {
        resetProgressAndMessage(message);
    }

    public void resetProgressAndMessage(String message) {
        field_146591_a = message;
        displayLoadingString("Working...");
    }

    public void displayLoadingString(String message) {
        field_146589_f = message;
        progress = 0;
    }

    public void setLoadingProgress(int progress) {
        this.progress = progress;
    }

    public void setDoneWorking() {
        doneWorking = true;
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (doneWorking) {
            mc.displayGuiScreen(null);
        } else {
            if (customLoadingScreen != null && mc.theWorld == null) {
                customLoadingScreen.drawBackground(width, height);
            } else {
                drawDefaultBackground();
            }

            if (progress > 0) {
                drawCenteredString(fontRendererObj, field_146591_a, width / 2, 70, 16777215);
                drawCenteredString(fontRendererObj, field_146589_f + " " + progress + "%", width / 2, 90, 16777215);
            }

            super.drawScreen(mouseX, mouseY, partialTicks);
        }
    }
}
