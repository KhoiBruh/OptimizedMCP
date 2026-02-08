package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.EnumDifficulty;

public class GuiOptions extends GuiScreen implements GuiYesNoCallback {
    private static final GameSettings.Options[] field_146440_f = new GameSettings.Options[]{GameSettings.Options.FOV};
    private final GuiScreen screen;
    private final GameSettings gameSettings;
    protected String field_146442_a = "Options";
    private GuiButton button;
    private GuiLockIconButton lockIconButton;

    public GuiOptions(GuiScreen screen, GameSettings settings) {
        this.screen = screen;
        gameSettings = settings;
    }

    public void initGui() {
        int i = 0;
        field_146442_a = I18n.format("options.title");

        for (GameSettings.Options gamesettings$options : field_146440_f) {
            if (gamesettings$options.getEnumFloat()) {
                buttonList.add(new GuiOptionSlider(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options));
            } else {
                GuiOptionButton guioptionbutton = new GuiOptionButton(gamesettings$options.returnEnumOrdinal(), width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), gamesettings$options, gameSettings.getKeyBinding(gamesettings$options));
                buttonList.add(guioptionbutton);
            }

            ++i;
        }

        if (mc.theWorld != null) {
            EnumDifficulty enumdifficulty = mc.theWorld.getDifficulty();
            button = new GuiButton(108, width / 2 - 155 + i % 2 * 160, height / 6 - 12 + 24 * (i >> 1), 150, 20, func_175355_a(enumdifficulty));
            buttonList.add(button);

            if (mc.isSingleplayer() && !mc.theWorld.getWorldInfo().isHardcoreModeEnabled()) {
                button.setWidth(button.getButtonWidth() - 20);
                lockIconButton = new GuiLockIconButton(109, button.xPosition + button.getButtonWidth(), button.yPosition);
                buttonList.add(lockIconButton);
                lockIconButton.func_175229_b(mc.theWorld.getWorldInfo().isDifficultyLocked());
                lockIconButton.enabled = !lockIconButton.func_175230_c();
                button.enabled = !lockIconButton.func_175230_c();
            } else button.enabled = false;
        }

        buttonList.add(new GuiButton(101, width / 2 - 155, height / 6 + 40, 150, 20, I18n.format("options.skinCustomisation")));
        buttonList.add(new GuiButton(102, width / 2 - 155, height / 6 + 65, 150, 20, I18n.format("options.sounds")));
        buttonList.add(new GuiButton(103, width / 2 - 155, height / 6 + 90, 150, 20, I18n.format("options.video")));
        buttonList.add(new GuiButton(104, width / 2 - 155, height / 6 + 115, 150, 20, I18n.format("options.resourcepack")));

        buttonList.add(new GuiButton(105, width / 2 + 5, height / 6 + 40, 150, 20, I18n.format("options.language")));
        buttonList.add(new GuiButton(106, width / 2 + 5, height / 6 + 65, 150, 20, I18n.format("options.controls")));
        buttonList.add(new GuiButton(107, width / 2 + 5, height / 6 + 90, 150, 20, I18n.format("options.chat.title")));

        buttonList.add(new GuiButton(108, width / 2 - 100, height / 6 + 170, I18n.format("gui.done")));
    }

    public String func_175355_a(EnumDifficulty p_175355_1_) {
        IChatComponent ichatcomponent = new ChatComponentText("");
        ichatcomponent.appendSibling(new ChatComponentTranslation("options.difficulty"));
        ichatcomponent.appendText(": ");
        ichatcomponent.appendSibling(new ChatComponentTranslation(p_175355_1_.getDifficultyResourceKey()));
        return ichatcomponent.getFormattedText();
    }

    public void confirmClicked(boolean result, int id) {
        mc.displayGuiScreen(this);

        if (id == 109 && result && mc.theWorld != null) {
            mc.theWorld.getWorldInfo().setDifficultyLocked(true);
            lockIconButton.func_175229_b(true);
            lockIconButton.enabled = false;
            button.enabled = false;
        }
    }

    protected void actionPerformed(GuiButton button) {
        if (!button.enabled) return;

        if (button.id < 100 && button instanceof GuiOptionButton) {
            GameSettings.Options options = ((GuiOptionButton) button).returnEnumOptions();
            gameSettings.setOptionValue(options, 1);
            button.displayString = gameSettings.getKeyBinding(GameSettings.Options.getEnumOptions(button.id));
        }

        switch (button.id) {
            case 101 -> mc.displayGuiScreen(new GuiCustomizeSkin(this));
            case 102 -> mc.displayGuiScreen(new GuiScreenOptionsSounds(this, gameSettings));
            case 103 -> mc.displayGuiScreen(new GuiVideoSettings(this, gameSettings));
            case 104 -> mc.displayGuiScreen(new GuiScreenResourcePacks(this));

            case 105 -> mc.displayGuiScreen(new GuiLanguage(this, gameSettings, mc.getLanguageManager()));
            case 106 -> mc.displayGuiScreen(new GuiControls(this, gameSettings));
            case 107 -> mc.displayGuiScreen(new ScreenChatOptions(this, gameSettings));

            case 108 -> mc.displayGuiScreen(screen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, field_146442_a, width / 2, 15, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
