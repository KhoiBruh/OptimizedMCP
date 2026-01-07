package net.minecraft.client.gui;

import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.SaveFormatComparator;
import net.minecraft.world.storage.WorldInfo;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;

public class GuiSelectWorld extends GuiScreen implements GuiYesNoCallback {
    private static final Logger logger = LogManager.getLogger();
    private final DateFormat field_146633_h = new SimpleDateFormat();
    protected GuiScreen parentScreen;
    protected String screenTitle = "Select world";
    private boolean field_146634_i;
    private int selectedIndex;
    private java.util.List<SaveFormatComparator> field_146639_s;
    private GuiSelectWorld.List availableWorlds;
    private String field_146637_u;
    private String field_146636_v;
    private final String[] field_146635_w = new String[4];
    private boolean confirmingDelete;
    private GuiButton deleteButton;
    private GuiButton selectButton;
    private GuiButton renameButton;
    private GuiButton recreateButton;

    public GuiSelectWorld(GuiScreen parentScreenIn) {
        parentScreen = parentScreenIn;
    }

    public static GuiYesNo makeDeleteWorldYesNo(GuiYesNoCallback selectWorld, String name, int id) {
        String s = I18n.format("selectWorld.deleteQuestion");
        String s1 = "'" + name + "' " + I18n.format("selectWorld.deleteWarning");
        String s2 = I18n.format("selectWorld.deleteButton");
        String s3 = I18n.format("gui.cancel");
        return new GuiYesNo(selectWorld, s, s1, s2, s3, id);
    }

    public void initGui() {
        screenTitle = I18n.format("selectWorld.title");

        try {
            loadLevelList();
        } catch (AnvilConverterException anvilconverterexception) {
            logger.error("Couldn't load level list", anvilconverterexception);
            mc.displayGuiScreen(new GuiErrorScreen("Unable to load worlds", anvilconverterexception.getMessage()));
            return;
        }

        field_146637_u = I18n.format("selectWorld.world");
        field_146636_v = I18n.format("selectWorld.conversion");
        field_146635_w[WorldSettings.GameType.SURVIVAL.getID()] = I18n.format("gameMode.survival");
        field_146635_w[WorldSettings.GameType.CREATIVE.getID()] = I18n.format("gameMode.creative");
        field_146635_w[WorldSettings.GameType.ADVENTURE.getID()] = I18n.format("gameMode.adventure");
        field_146635_w[WorldSettings.GameType.SPECTATOR.getID()] = I18n.format("gameMode.spectator");
        availableWorlds = new GuiSelectWorld.List(mc);
        availableWorlds.registerScrollButtons(4, 5);
        addWorldSelectionButtons();
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        availableWorlds.handleMouseInput();
    }

    private void loadLevelList() throws AnvilConverterException {
        ISaveFormat isaveformat = mc.getSaveLoader();
        field_146639_s = isaveformat.getSaveList();
        Collections.sort(field_146639_s);
        selectedIndex = -1;
    }

    protected String func_146621_a(int p_146621_1_) {
        return field_146639_s.get(p_146621_1_).getFileName();
    }

    protected String func_146614_d(int p_146614_1_) {
        String s = field_146639_s.get(p_146614_1_).getDisplayName();

        if (StringUtils.isEmpty(s)) {
            s = I18n.format("selectWorld.world") + " " + (p_146614_1_ + 1);
        }

        return s;
    }

    public void addWorldSelectionButtons() {
        buttonList.add(selectButton = new GuiButton(1, width / 2 - 154, height - 52, 150, 20, I18n.format("selectWorld.select")));
        buttonList.add(new GuiButton(3, width / 2 + 4, height - 52, 150, 20, I18n.format("selectWorld.create")));
        buttonList.add(renameButton = new GuiButton(6, width / 2 - 154, height - 28, 72, 20, I18n.format("selectWorld.rename")));
        buttonList.add(deleteButton = new GuiButton(2, width / 2 - 76, height - 28, 72, 20, I18n.format("selectWorld.delete")));
        buttonList.add(recreateButton = new GuiButton(7, width / 2 + 4, height - 28, 72, 20, I18n.format("selectWorld.recreate")));
        buttonList.add(new GuiButton(0, width / 2 + 82, height - 28, 72, 20, I18n.format("gui.cancel")));
        selectButton.enabled = false;
        deleteButton.enabled = false;
        renameButton.enabled = false;
        recreateButton.enabled = false;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 2) {
                String s = func_146614_d(selectedIndex);

                if (s != null) {
                    confirmingDelete = true;
                    GuiYesNo guiyesno = makeDeleteWorldYesNo(this, s, selectedIndex);
                    mc.displayGuiScreen(guiyesno);
                }
            } else if (button.id == 1) {
                func_146615_e(selectedIndex);
            } else if (button.id == 3) {
                mc.displayGuiScreen(new GuiCreateWorld(this));
            } else if (button.id == 6) {
                mc.displayGuiScreen(new GuiRenameWorld(this, func_146621_a(selectedIndex)));
            } else if (button.id == 0) {
                mc.displayGuiScreen(parentScreen);
            } else if (button.id == 7) {
                GuiCreateWorld guicreateworld = new GuiCreateWorld(this);
                ISaveHandler isavehandler = mc.getSaveLoader().getSaveLoader(func_146621_a(selectedIndex), false);
                WorldInfo worldinfo = isavehandler.loadWorldInfo();
                isavehandler.flush();
                guicreateworld.recreateFromExistingWorld(worldinfo);
                mc.displayGuiScreen(guicreateworld);
            } else {
                availableWorlds.actionPerformed(button);
            }
        }
    }

    public void func_146615_e(int p_146615_1_) {
        mc.displayGuiScreen(null);

        if (!field_146634_i) {
            field_146634_i = true;
            String s = func_146621_a(p_146615_1_);

            if (s == null) {
                s = "World" + p_146615_1_;
            }

            String s1 = func_146614_d(p_146615_1_);

            if (s1 == null) {
                s1 = "World" + p_146615_1_;
            }

            if (mc.getSaveLoader().canLoadWorld(s)) {
                mc.launchIntegratedServer(s, s1, null);
            }
        }
    }

    public void confirmClicked(boolean result, int id) {
        if (confirmingDelete) {
            confirmingDelete = false;

            if (result) {
                ISaveFormat isaveformat = mc.getSaveLoader();
                isaveformat.flushCache();
                isaveformat.deleteWorldDirectory(func_146621_a(id));

                try {
                    loadLevelList();
                } catch (AnvilConverterException anvilconverterexception) {
                    logger.error("Couldn't load level list", anvilconverterexception);
                }
            }

            mc.displayGuiScreen(this);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        availableWorlds.drawScreen(mouseX, mouseY, partialTicks);
        drawCenteredString(fontRendererObj, screenTitle, width / 2, 20, 16777215);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    class List extends GuiSlot {
        public List(Minecraft mcIn) {
            super(mcIn, GuiSelectWorld.this.width, GuiSelectWorld.this.height, 32, GuiSelectWorld.this.height - 64, 36);
        }

        protected int getSize() {
            return field_146639_s.size();
        }

        protected void elementClicked(int slotIndex, boolean isDoubleClick, int mouseX, int mouseY) {
            selectedIndex = slotIndex;
            boolean flag = selectedIndex >= 0 && selectedIndex < getSize();
            selectButton.enabled = flag;
            deleteButton.enabled = flag;
            renameButton.enabled = flag;
            recreateButton.enabled = flag;

            if (isDoubleClick && flag) {
                func_146615_e(slotIndex);
            }
        }

        protected boolean isSelected(int slotIndex) {
            return slotIndex == selectedIndex;
        }

        protected int getContentHeight() {
            return field_146639_s.size() * 36;
        }

        protected void drawBackground() {
            drawDefaultBackground();
        }

        protected void drawSlot(int entryID, int p_180791_2_, int p_180791_3_, int p_180791_4_, int mouseXIn, int mouseYIn) {
            SaveFormatComparator saveformatcomparator = field_146639_s.get(entryID);
            String s = saveformatcomparator.getDisplayName();

            if (StringUtils.isEmpty(s)) {
                s = field_146637_u + " " + (entryID + 1);
            }

            String s1 = saveformatcomparator.getFileName();
            s1 = s1 + " (" + field_146633_h.format(new Date(saveformatcomparator.getLastTimePlayed()));
            s1 = s1 + ")";
            String s2 = "";

            if (saveformatcomparator.requiresConversion()) {
                s2 = field_146636_v + " " + s2;
            } else {
                s2 = field_146635_w[saveformatcomparator.getEnumGameType().getID()];

                if (saveformatcomparator.isHardcoreModeEnabled()) {
                    s2 = EnumChatFormatting.DARK_RED + I18n.format("gameMode.hardcore", new Object[0]) + EnumChatFormatting.RESET;
                }

                if (saveformatcomparator.getCheatsEnabled()) {
                    s2 = s2 + ", " + I18n.format("selectWorld.cheats");
                }
            }

            drawString(fontRendererObj, s, p_180791_2_ + 2, p_180791_3_ + 1, 16777215);
            drawString(fontRendererObj, s1, p_180791_2_ + 2, p_180791_3_ + 12, 8421504);
            drawString(fontRendererObj, s2, p_180791_2_ + 2, p_180791_3_ + 12 + 10, 8421504);
        }
    }
}
