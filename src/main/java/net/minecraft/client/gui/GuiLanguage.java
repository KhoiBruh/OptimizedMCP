package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Language;
import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.settings.GameSettings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class GuiLanguage extends GuiScreen {
    private final GameSettings game_settings_3;
    private final LanguageManager languageManager;
    protected GuiScreen parentScreen;
    private GuiLanguage.List list;
    private GuiOptionButton forceUnicodeFontBtn;
    private GuiOptionButton confirmSettingsBtn;

    public GuiLanguage(GuiScreen screen, GameSettings gameSettingsObj, LanguageManager manager) {
        parentScreen = screen;
        game_settings_3 = gameSettingsObj;
        languageManager = manager;
    }

    public void initGui() {
        buttonList.add(forceUnicodeFontBtn = new GuiOptionButton(100, width / 2 - 155, height - 38, GameSettings.Options.FORCE_UNICODE_FONT, game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT)));
        buttonList.add(confirmSettingsBtn = new GuiOptionButton(6, width / 2 - 155 + 160, height - 38, I18n.format("gui.done")));
        list = new GuiLanguage.List(mc);
        list.registerScrollButtons(7, 8);
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        list.handleMouseInput();
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            switch (button.id) {
                case 5:
                    break;

                case 6:
                    mc.displayGuiScreen(parentScreen);
                    break;

                case 100:
                    if (button instanceof GuiOptionButton) {
                        game_settings_3.setOptionValue(((GuiOptionButton) button).returnEnumOptions(), 1);
                        button.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
                        ScaledResolution scaledresolution = new ScaledResolution(mc);
                        int i = scaledresolution.getScaledWidth();
                        int j = scaledresolution.getScaledHeight();
                        setWorldAndResolution(mc, i, j);
                    }

                    break;

                default:
                    list.actionPerformed(button);
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        list.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, I18n.format("options.language"), width / 2, 16, 16777215);
        drawCenteredString(fontRendererObj, "(" + I18n.format("options.languageWarning") + ")", width / 2, height - 56, 8421504);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot {
        private final java.util.List<String> langCodeList = new ArrayList<>();
        private final Map<String, Language> languageMap = new HashMap<>();

        public List(Minecraft mcIn) {
            super(mcIn, GuiLanguage.this.width, GuiLanguage.this.height, 32, GuiLanguage.this.height - 65 + 4, 18);

            for (Language language : languageManager.getLanguages()) {
                languageMap.put(language.getLanguageCode(), language);
                langCodeList.add(language.getLanguageCode());
            }
        }

        protected int getSize() {
            return langCodeList.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            Language language = languageMap.get(langCodeList.get(slotIndex));
            languageManager.setCurrentLanguage(language);
            game_settings_3.language = language.getLanguageCode();
            mc.refreshResources();
            fontRendererObj.setUnicodeFlag(languageManager.isCurrentLocaleUnicode() || game_settings_3.forceUnicodeFont);
            fontRendererObj.setBidiFlag(languageManager.isCurrentLanguageBidirectional());
            confirmSettingsBtn.displayString = I18n.format("gui.done");
            forceUnicodeFontBtn.displayString = game_settings_3.getKeyBinding(GameSettings.Options.FORCE_UNICODE_FONT);
            game_settings_3.saveOptions();
        }

        protected boolean isSelected(int slotIndex) {
            return langCodeList.get(slotIndex).equals(languageManager.getCurrentLanguage().getLanguageCode());
        }

        protected int getContentHeight() {
            return getSize() * 18;
        }

        protected void drawBackground() {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            fontRendererObj.setBidiFlag(true);
            drawCenteredString(fontRendererObj, languageMap.get(langCodeList.get(entryID)).toString(), width / 2, p_180791_3_ + 1, 16777215);
            fontRendererObj.setBidiFlag(languageManager.getCurrentLanguage().isBidirectional());
        }
    }
}
