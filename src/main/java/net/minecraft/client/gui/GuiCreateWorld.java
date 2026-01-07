package net.minecraft.client.gui;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldType;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Random;

public class GuiCreateWorld extends GuiScreen {
    private static final String[] disallowedFilenames = new String[]{"CON", "COM", "PRN", "AUX", "CLOCK$", "NUL", "COM1", "COM2", "COM3", "COM4", "COM5", "COM6", "COM7", "COM8", "COM9", "LPT1", "LPT2", "LPT3", "LPT4", "LPT5", "LPT6", "LPT7", "LPT8", "LPT9"};
    public String chunkProviderSettingsJson = "";
    private final GuiScreen parentScreen;
    private GuiTextField worldNameField;
    private GuiTextField worldSeedField;
    private String saveDirName;
    private String gameMode = "survival";
    private String savedGameMode;
    private boolean generateStructuresEnabled = true;
    private boolean allowCheats;
    private boolean allowCheatsWasSetByUser;
    private boolean bonusChestEnabled;
    private boolean hardCoreMode;
    private boolean alreadyGenerated;
    private boolean inMoreWorldOptionsDisplay;
    private GuiButton btnGameMode;
    private GuiButton btnMoreOptions;
    private GuiButton btnMapFeatures;
    private GuiButton btnBonusItems;
    private GuiButton btnMapType;
    private GuiButton btnAllowCommands;
    private GuiButton btnCustomizeType;
    private String gameModeDesc1;
    private String gameModeDesc2;
    private String worldSeed;
    private String worldName;
    private int selectedIndex;

    public GuiCreateWorld(GuiScreen p_i46320_1_) {
        parentScreen = p_i46320_1_;
        worldSeed = "";
        worldName = I18n.format("selectWorld.newWorld");
    }

    public static String getUncollidingSaveDirName(ISaveFormat saveLoader, String name) {

        StringBuilder nameBuilder = new StringBuilder(name.replaceAll("[\\./\"]", "_"));
        for (String s : disallowedFilenames) {
            if (nameBuilder.toString().equalsIgnoreCase(s)) {
                nameBuilder = new StringBuilder("_" + nameBuilder + "_");
            }
        }
        name = nameBuilder.toString();

        while (saveLoader.getWorldInfo(name) != null) {
            name = name + "-";
        }

        return name;
    }

    public void updateScreen() {
        worldNameField.updateCursorCounter();
        worldSeedField.updateCursorCounter();
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 155, height - 28, 150, 20, I18n.format("selectWorld.create")));
        buttonList.add(new GuiButton(1, width / 2 + 5, height - 28, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(btnGameMode = new GuiButton(2, width / 2 - 75, 115, 150, 20, I18n.format("selectWorld.gameMode")));
        buttonList.add(btnMoreOptions = new GuiButton(3, width / 2 - 75, 187, 150, 20, I18n.format("selectWorld.moreWorldOptions")));
        buttonList.add(btnMapFeatures = new GuiButton(4, width / 2 - 155, 100, 150, 20, I18n.format("selectWorld.mapFeatures")));
        btnMapFeatures.visible = false;
        buttonList.add(btnBonusItems = new GuiButton(7, width / 2 + 5, 151, 150, 20, I18n.format("selectWorld.bonusItems")));
        btnBonusItems.visible = false;
        buttonList.add(btnMapType = new GuiButton(5, width / 2 + 5, 100, 150, 20, I18n.format("selectWorld.mapType")));
        btnMapType.visible = false;
        buttonList.add(btnAllowCommands = new GuiButton(6, width / 2 - 155, 151, 150, 20, I18n.format("selectWorld.allowCommands")));
        btnAllowCommands.visible = false;
        buttonList.add(btnCustomizeType = new GuiButton(8, width / 2 + 5, 120, 150, 20, I18n.format("selectWorld.customizeType")));
        btnCustomizeType.visible = false;
        worldNameField = new GuiTextField(9, fontRendererObj, width / 2 - 100, 60, 200, 20);
        worldNameField.setFocused(true);
        worldNameField.setText(worldName);
        worldSeedField = new GuiTextField(10, fontRendererObj, width / 2 - 100, 60, 200, 20);
        worldSeedField.setText(worldSeed);
        showMoreWorldOptions(inMoreWorldOptionsDisplay);
        calcSaveDirName();
        updateDisplayState();
    }

    private void calcSaveDirName() {
        saveDirName = worldNameField.getText().trim();

        for (char c0 : ChatAllowedCharacters.allowedCharactersArray) {
            saveDirName = saveDirName.replace(c0, '_');
        }

        if (StringUtils.isEmpty(saveDirName)) {
            saveDirName = "World";
        }

        saveDirName = getUncollidingSaveDirName(mc.getSaveLoader(), saveDirName);
    }

    private void updateDisplayState() {
        btnGameMode.displayString = I18n.format("selectWorld.gameMode") + ": " + I18n.format("selectWorld.gameMode." + gameMode);
        gameModeDesc1 = I18n.format("selectWorld.gameMode." + gameMode + ".line1");
        gameModeDesc2 = I18n.format("selectWorld.gameMode." + gameMode + ".line2");
        btnMapFeatures.displayString = I18n.format("selectWorld.mapFeatures") + " ";

        if (generateStructuresEnabled) {
            btnMapFeatures.displayString = btnMapFeatures.displayString + I18n.format("options.on");
        } else {
            btnMapFeatures.displayString = btnMapFeatures.displayString + I18n.format("options.off");
        }

        btnBonusItems.displayString = I18n.format("selectWorld.bonusItems") + " ";

        if (bonusChestEnabled && !hardCoreMode) {
            btnBonusItems.displayString = btnBonusItems.displayString + I18n.format("options.on");
        } else {
            btnBonusItems.displayString = btnBonusItems.displayString + I18n.format("options.off");
        }

        btnMapType.displayString = I18n.format("selectWorld.mapType") + " " + I18n.format(WorldType.worldTypes[selectedIndex].getTranslateName());
        btnAllowCommands.displayString = I18n.format("selectWorld.allowCommands") + " ";

        if (allowCheats && !hardCoreMode) {
            btnAllowCommands.displayString = btnAllowCommands.displayString + I18n.format("options.on");
        } else {
            btnAllowCommands.displayString = btnAllowCommands.displayString + I18n.format("options.off");
        }
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                mc.displayGuiScreen(parentScreen);
            } else if (button.id == 0) {
                mc.displayGuiScreen(null);

                if (alreadyGenerated) {
                    return;
                }

                alreadyGenerated = true;
                long i = (new Random()).nextLong();
                String s = worldSeedField.getText();

                if (!StringUtils.isEmpty(s)) {
                    try {
                        long j = Long.parseLong(s);

                        if (j != 0L) {
                            i = j;
                        }
                    } catch (NumberFormatException var7) {
                        i = s.hashCode();
                    }
                }

                WorldSettings.GameType worldsettings$gametype = WorldSettings.GameType.getByName(gameMode);
                WorldSettings worldsettings = new WorldSettings(i, worldsettings$gametype, generateStructuresEnabled, hardCoreMode, WorldType.worldTypes[selectedIndex]);
                worldsettings.setWorldName(chunkProviderSettingsJson);

                if (bonusChestEnabled && !hardCoreMode) {
                    worldsettings.enableBonusChest();
                }

                if (allowCheats && !hardCoreMode) {
                    worldsettings.enableCommands();
                }

                mc.launchIntegratedServer(saveDirName, worldNameField.getText().trim(), worldsettings);
            } else if (button.id == 3) {
                toggleMoreWorldOptions();
            } else if (button.id == 2) {
                if (gameMode.equals("survival")) {
                    if (!allowCheatsWasSetByUser) {
                        allowCheats = false;
                    }

                    hardCoreMode = false;
                    gameMode = "hardcore";
                    hardCoreMode = true;
                    btnAllowCommands.enabled = false;
                    btnBonusItems.enabled = false;
                    updateDisplayState();
                } else if (gameMode.equals("hardcore")) {
                    if (!allowCheatsWasSetByUser) {
                        allowCheats = true;
                    }

                    hardCoreMode = false;
                    gameMode = "creative";
                    updateDisplayState();
                    hardCoreMode = false;
                    btnAllowCommands.enabled = true;
                    btnBonusItems.enabled = true;
                } else {
                    if (!allowCheatsWasSetByUser) {
                        allowCheats = false;
                    }

                    gameMode = "survival";
                    updateDisplayState();
                    btnAllowCommands.enabled = true;
                    btnBonusItems.enabled = true;
                    hardCoreMode = false;
                }

                updateDisplayState();
            } else if (button.id == 4) {
                generateStructuresEnabled = !generateStructuresEnabled;
                updateDisplayState();
            } else if (button.id == 7) {
                bonusChestEnabled = !bonusChestEnabled;
                updateDisplayState();
            } else if (button.id == 5) {
                ++selectedIndex;

                if (selectedIndex >= WorldType.worldTypes.length) {
                    selectedIndex = 0;
                }

                while (!canSelectCurWorldType()) {
                    ++selectedIndex;

                    if (selectedIndex >= WorldType.worldTypes.length) {
                        selectedIndex = 0;
                    }
                }

                chunkProviderSettingsJson = "";
                updateDisplayState();
                showMoreWorldOptions(inMoreWorldOptionsDisplay);
            } else if (button.id == 6) {
                allowCheatsWasSetByUser = true;
                allowCheats = !allowCheats;
                updateDisplayState();
            } else if (button.id == 8) {
                if (WorldType.worldTypes[selectedIndex] == WorldType.FLAT) {
                    mc.displayGuiScreen(new GuiCreateFlatWorld(this, chunkProviderSettingsJson));
                } else {
                    mc.displayGuiScreen(new GuiCustomizeWorldScreen(this, chunkProviderSettingsJson));
                }
            }
        }
    }

    private boolean canSelectCurWorldType() {
        WorldType worldtype = WorldType.worldTypes[selectedIndex];
        return worldtype != null && worldtype.getCanBeCreated() && (worldtype != WorldType.DEBUG_WORLD || isShiftKeyDown());
    }

    private void toggleMoreWorldOptions() {
        showMoreWorldOptions(!inMoreWorldOptionsDisplay);
    }

    private void showMoreWorldOptions(boolean toggle) {
        inMoreWorldOptionsDisplay = toggle;

        if (WorldType.worldTypes[selectedIndex] == WorldType.DEBUG_WORLD) {
            btnGameMode.visible = !inMoreWorldOptionsDisplay;
            btnGameMode.enabled = false;

            if (savedGameMode == null) {
                savedGameMode = gameMode;
            }

            gameMode = "spectator";
            btnMapFeatures.visible = false;
            btnBonusItems.visible = false;
            btnMapType.visible = inMoreWorldOptionsDisplay;
            btnAllowCommands.visible = false;
            btnCustomizeType.visible = false;
        } else {
            btnGameMode.visible = !inMoreWorldOptionsDisplay;
            btnGameMode.enabled = true;

            if (savedGameMode != null) {
                gameMode = savedGameMode;
                savedGameMode = null;
            }

            btnMapFeatures.visible = inMoreWorldOptionsDisplay && WorldType.worldTypes[selectedIndex] != WorldType.CUSTOMIZED;
            btnBonusItems.visible = inMoreWorldOptionsDisplay;
            btnMapType.visible = inMoreWorldOptionsDisplay;
            btnAllowCommands.visible = inMoreWorldOptionsDisplay;
            btnCustomizeType.visible = inMoreWorldOptionsDisplay && (WorldType.worldTypes[selectedIndex] == WorldType.FLAT || WorldType.worldTypes[selectedIndex] == WorldType.CUSTOMIZED);
        }

        updateDisplayState();

        if (inMoreWorldOptionsDisplay) {
            btnMoreOptions.displayString = I18n.format("gui.done");
        } else {
            btnMoreOptions.displayString = I18n.format("selectWorld.moreWorldOptions");
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (worldNameField.isFocused() && !inMoreWorldOptionsDisplay) {
            worldNameField.textboxKeyTyped(typedChar, keyCode);
            worldName = worldNameField.getText();
        } else if (worldSeedField.isFocused() && inMoreWorldOptionsDisplay) {
            worldSeedField.textboxKeyTyped(typedChar, keyCode);
            worldSeed = worldSeedField.getText();
        }

        if (keyCode == 28 || keyCode == 156) {
            actionPerformed((GuiButton) buttonList.get(0));
        }

        buttonList.get(0).enabled = !worldNameField.getText().isEmpty();
        calcSaveDirName();
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (inMoreWorldOptionsDisplay) {
            worldSeedField.mouseClicked(mouseX, mouseY, mouseButton);
        } else {
            worldNameField.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("selectWorld.create"), width / 2, 20, -1);

        if (inMoreWorldOptionsDisplay) {
            drawString(fontRendererObj, I18n.format("selectWorld.enterSeed"), width / 2 - 100, 47, -6250336);
            drawString(fontRendererObj, I18n.format("selectWorld.seedInfo"), width / 2 - 100, 85, -6250336);

            if (btnMapFeatures.visible) {
                drawString(fontRendererObj, I18n.format("selectWorld.mapFeatures.info"), width / 2 - 150, 122, -6250336);
            }

            if (btnAllowCommands.visible) {
                drawString(fontRendererObj, I18n.format("selectWorld.allowCommands.info"), width / 2 - 150, 172, -6250336);
            }

            worldSeedField.drawTextBox();

            if (WorldType.worldTypes[selectedIndex].showWorldInfoNotice()) {
                fontRendererObj.drawSplitString(I18n.format(WorldType.worldTypes[selectedIndex].getTranslatedInfo()), btnMapType.xPosition + 2, btnMapType.yPosition + 22, btnMapType.getButtonWidth(), 10526880);
            }
        } else {
            drawString(fontRendererObj, I18n.format("selectWorld.enterName"), width / 2 - 100, 47, -6250336);
            drawString(fontRendererObj, I18n.format("selectWorld.resultFolder") + " " + saveDirName, width / 2 - 100, 85, -6250336);
            worldNameField.drawTextBox();
            drawString(fontRendererObj, gameModeDesc1, width / 2 - 100, 137, -6250336);
            drawString(fontRendererObj, gameModeDesc2, width / 2 - 100, 149, -6250336);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void recreateFromExistingWorld(WorldInfo original) {
        worldName = I18n.format("selectWorld.newWorld.copyOf", original.getWorldName());
        worldSeed = original.getSeed() + "";
        selectedIndex = original.getTerrainType().getWorldTypeID();
        chunkProviderSettingsJson = original.getGeneratorOptions();
        generateStructuresEnabled = original.isMapFeaturesEnabled();
        allowCheats = original.areCommandsAllowed();

        if (original.isHardcoreModeEnabled()) {
            gameMode = "hardcore";
        } else if (original.getGameType().isSurvivalOrAdventure()) {
            gameMode = "survival";
        } else if (original.getGameType().isCreative()) {
            gameMode = "creative";
        }
    }
}
