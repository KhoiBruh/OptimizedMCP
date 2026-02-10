package net.optifine.shaders.gui;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.src.Config;
import net.minecraft.util.MathHelper;
import net.optifine.Lang;
import net.optifine.gui.GuiScreenOF;
import net.optifine.gui.TooltipManager;
import net.optifine.gui.TooltipProviderShaderOptions;
import net.optifine.shaders.Shaders;
import net.optifine.shaders.config.ShaderOption;
import net.optifine.shaders.config.ShaderOptionProfile;
import net.optifine.shaders.config.ShaderOptionScreen;

public class GuiShaderOptions extends GuiScreenOF {
    protected String title;
    private final GuiScreen prevScreen;
    private final GameSettings settings;
    private final TooltipManager tooltipManager;
    private String screenName;
    private String screenText;
    private boolean changed;

    public GuiShaderOptions(GuiScreen guiscreen, GameSettings gamesettings) {
        tooltipManager = new TooltipManager(this, new TooltipProviderShaderOptions());
        screenName = null;
        screenText = null;
        changed = false;
        title = "Shader Options";
        prevScreen = guiscreen;
        settings = gamesettings;
    }

    public GuiShaderOptions(GuiScreen guiscreen, GameSettings gamesettings, String screenName) {
        this(guiscreen, gamesettings);
        this.screenName = screenName;

        if (screenName != null) {
            screenText = Shaders.translate("screen." + screenName, screenName);
        }
    }

    public static String getButtonText(ShaderOption so, int btnWidth) {
        String s = so.getNameText();

        if (so instanceof ShaderOptionScreen shaderoptionscreen) {
            return s + "...";
        } else {
            FontRenderer fontrenderer = Config.getMinecraft().fontRendererObj;

            for (int i = fontrenderer.getStringWidth(": " + Lang.getOff()) + 5; fontrenderer.getStringWidth(s) + i >= btnWidth && !s.isEmpty(); s = s.substring(0, s.length() - 1)) {
            }

            String s1 = so.isChanged() ? so.getValueColor(so.getValue()) : "";
            String s2 = so.getValueText(so.getValue());
            return s + ": " + s1 + s2;
        }
    }

    public void initGui() {
        title = I18n.format("of.options.shaderOptionsTitle");
        int i = 100;
        int j;
        int k = 30;
        int l = 20;
        int i1 = 120;
        int j1 = 20;
        int k1 = Shaders.getShaderPackColumns(screenName, 2);
        ShaderOption[] ashaderoption = Shaders.getShaderPackOptions(screenName);

        if (ashaderoption != null) {
            int l1 = MathHelper.ceil((double) ashaderoption.length / 9.0D);

            if (k1 < l1) {
                k1 = l1;
            }

            for (int i2 = 0; i2 < ashaderoption.length; ++i2) {
                ShaderOption shaderoption = ashaderoption[i2];

                if (shaderoption != null && shaderoption.isVisible()) {
                    int j2 = i2 % k1;
                    int k2 = i2 / k1;
                    int l2 = Math.min(width / k1, 200);
                    j = (width - l2 * k1) / 2;
                    int i3 = j2 * l2 + 5 + j;
                    int j3 = k + k2 * l;
                    int k3 = l2 - 10;
                    String s = getButtonText(shaderoption, k3);
                    GuiButtonShaderOption guibuttonshaderoption;

                    if (Shaders.isShaderPackOptionSlider(shaderoption.getName())) {
                        guibuttonshaderoption = new GuiSliderShaderOption(i + i2, i3, j3, k3, j1, shaderoption, s);
                    } else {
                        guibuttonshaderoption = new GuiButtonShaderOption(i + i2, i3, j3, k3, j1, shaderoption, s);
                    }

                    guibuttonshaderoption.enabled = shaderoption.isEnabled();
                    buttonList.add(guibuttonshaderoption);
                }
            }
        }

        buttonList.add(new GuiButton(201, width / 2 - i1 - 20, height / 6 + 168 + 11, i1, j1, I18n.format("controls.reset")));
        buttonList.add(new GuiButton(200, width / 2 + 20, height / 6 + 168 + 11, i1, j1, I18n.format("gui.done")));
    }

    protected void actionPerformed(GuiButton guibutton) {
        if (guibutton.enabled) {
            if (guibutton.id < 200 && guibutton instanceof GuiButtonShaderOption guibuttonshaderoption) {
                ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

                if (shaderoption instanceof ShaderOptionScreen) {
                    String s = shaderoption.getName();
                    GuiShaderOptions guishaderoptions = new GuiShaderOptions(this, settings, s);
                    mc.displayGuiScreen(guishaderoptions);
                    return;
                }

                if (isShiftKeyDown()) {
                    shaderoption.resetValue();
                } else if (guibuttonshaderoption.isSwitchable()) {
                    shaderoption.nextValue();
                }

                updateAllButtons();
                changed = true;
            }

            if (guibutton.id == 201) {
                ShaderOption[] ashaderoption = Shaders.getChangedOptions(Shaders.getShaderPackOptions());

                for (ShaderOption shaderoption1 : ashaderoption) {
                    shaderoption1.resetValue();
                    changed = true;
                }

                updateAllButtons();
            }

            if (guibutton.id == 200) {
                if (changed) {
                    Shaders.saveShaderPackOptions();
                    changed = false;
                    Shaders.uninit();
                }

                mc.displayGuiScreen(prevScreen);
            }
        }
    }

    protected void actionPerformedRightClick(GuiButton btn) {
        if (btn instanceof GuiButtonShaderOption guibuttonshaderoption) {
            ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

            if (isShiftKeyDown()) {
                shaderoption.resetValue();
            } else if (guibuttonshaderoption.isSwitchable()) {
                shaderoption.prevValue();
            }

            updateAllButtons();
            changed = true;
        }
    }

    public void onGuiClosed() {
        super.onGuiClosed();

        if (changed) {
            Shaders.saveShaderPackOptions();
            changed = false;
            Shaders.uninit();
        }
    }

    private void updateAllButtons() {
        for (GuiButton guibutton : buttonList) {
            if (guibutton instanceof GuiButtonShaderOption guibuttonshaderoption) {
                ShaderOption shaderoption = guibuttonshaderoption.getShaderOption();

                if (shaderoption instanceof ShaderOptionProfile shaderoptionprofile) {
                    shaderoptionprofile.updateProfile();
                }

                guibuttonshaderoption.displayString = getButtonText(shaderoption, guibuttonshaderoption.getButtonWidth());
                guibuttonshaderoption.valueChanged();
            }
        }
    }

    public void drawScreen(int x, int y, float f) {
        drawDefaultBackground();

        if (screenText != null) {
            drawCenteredString(fontRendererObj, screenText, width / 2, 15, 16777215);
        } else {
            drawCenteredString(fontRendererObj, title, width / 2, 15, 16777215);
        }

        super.drawScreen(x, y, f);
        tooltipManager.drawTooltips(x, y, buttonList);
    }
}
