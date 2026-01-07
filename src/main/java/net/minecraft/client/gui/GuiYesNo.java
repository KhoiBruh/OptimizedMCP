package net.minecraft.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.resources.I18n;

import java.io.IOException;
import java.util.List;

public class GuiYesNo extends GuiScreen {
    private final List<String> field_175298_s = Lists.newArrayList();
    protected GuiYesNoCallback parentScreen;
    protected String messageLine1;
    protected String confirmButtonText;
    protected String cancelButtonText;
    protected int parentButtonClickedId;
    private final String messageLine2;
    private int ticksUntilEnable;

    public GuiYesNo(GuiYesNoCallback p_i1082_1_, String p_i1082_2_, String p_i1082_3_, int p_i1082_4_) {
        parentScreen = p_i1082_1_;
        messageLine1 = p_i1082_2_;
        messageLine2 = p_i1082_3_;
        parentButtonClickedId = p_i1082_4_;
        confirmButtonText = I18n.format("gui.yes");
        cancelButtonText = I18n.format("gui.no");
    }

    public GuiYesNo(GuiYesNoCallback p_i1083_1_, String p_i1083_2_, String p_i1083_3_, String p_i1083_4_, String p_i1083_5_, int p_i1083_6_) {
        parentScreen = p_i1083_1_;
        messageLine1 = p_i1083_2_;
        messageLine2 = p_i1083_3_;
        confirmButtonText = p_i1083_4_;
        cancelButtonText = p_i1083_5_;
        parentButtonClickedId = p_i1083_6_;
    }

    public void initGui() {
        buttonList.add(new GuiOptionButton(0, width / 2 - 155, height / 6 + 96, confirmButtonText));
        buttonList.add(new GuiOptionButton(1, width / 2 - 155 + 160, height / 6 + 96, cancelButtonText));
        field_175298_s.clear();
        field_175298_s.addAll(fontRendererObj.listFormattedStringToWidth(messageLine2, width - 50));
    }

    protected void actionPerformed(GuiButton button) {
        parentScreen.confirmClicked(button.id == 0, parentButtonClickedId);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, messageLine1, width / 2, 70, 16777215);
        int i = 90;

        for (String s : field_175298_s) {
            drawCenteredString(fontRendererObj, s, width / 2, i, 16777215);
            i += fontRendererObj.FONT_HEIGHT;
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void setButtonDelay(int p_146350_1_) {
        ticksUntilEnable = p_146350_1_;

        for (GuiButton guibutton : buttonList) {
            guibutton.enabled = false;
        }
    }

    public void updateScreen() {
        super.updateScreen();

        if (--ticksUntilEnable == 0) {
            for (GuiButton guibutton : buttonList) {
                guibutton.enabled = true;
            }
        }
    }
}
