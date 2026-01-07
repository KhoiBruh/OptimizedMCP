package net.optifine.shaders.gui;

import net.minecraft.client.gui.GuiSlot;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.resources.I18n;
import net.minecraft.src.Config;
import net.optifine.Lang;
import net.optifine.shaders.IShaderPack;
import net.optifine.shaders.Shaders;
import net.optifine.util.ResUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Properties;

class GuiSlotShaders extends GuiSlot {
    final GuiShaders shadersGui;
    private ArrayList<String> shaderslist;
    private int selectedIndex;
    private long lastClickedCached = 0L;

    public GuiSlotShaders(GuiShaders par1GuiShaders, int width, int height, int top, int bottom, int slotHeight) {
        super(par1GuiShaders.getMc(), width, height, top, bottom, slotHeight);
        shadersGui = par1GuiShaders;
        updateList();
        amountScrolled = 0.0F;
        int i = selectedIndex * slotHeight;
        int j = (bottom - top) / 2;

        if (i > j) {
            scrollBy(i - j);
        }
    }

    public int getListWidth() {
        return width - 20;
    }

    public void updateList() {
        shaderslist = Shaders.listOfShaders();
        selectedIndex = 0;
        int i = 0;

        for (int j = shaderslist.size(); i < j; ++i) {
            if (shaderslist.get(i).equals(Shaders.currentShaderName)) {
                selectedIndex = i;
                break;
            }
        }
    }

    protected int getSize() {
        return shaderslist.size();
    }

    protected void elementClicked(int index, boolean doubleClicked, int mouseX, int mouseY) {
        if (index != selectedIndex || lastClicked != lastClickedCached) {
            String s = shaderslist.get(index);
            IShaderPack ishaderpack = Shaders.getShaderPack(s);

            if (checkCompatible(ishaderpack, index)) {
                selectIndex(index);
            }
        }
    }

    private void selectIndex(int index) {
        selectedIndex = index;
        lastClickedCached = lastClicked;
        Shaders.setShaderPack(shaderslist.get(index));
        Shaders.uninit();
        shadersGui.updateButtons();
    }

    private boolean checkCompatible(IShaderPack sp, final int index) {
        if (sp == null) {
            return true;
        } else {
            InputStream inputstream = sp.getResourceAsStream("/shaders/shaders.properties");
            Properties properties = ResUtils.readProperties(inputstream, "Shaders");

            if (properties == null) {
                return true;
            } else {
                String s = "version.1.8.9";
                String s1 = properties.getProperty(s);

                if (s1 == null) {
                    return true;
                } else {
                    s1 = s1.trim();
                    String s2 = "M6_pre2";
                    int i = Config.compareRelease(s2, s1);

                    if (i >= 0) {
                        return true;
                    } else {
                        String s3 = ("HD_U_" + s1).replace('_', ' ');
                        String s4 = I18n.format("of.message.shaders.nv1", s3);
                        String s5 = I18n.format("of.message.shaders.nv2");
                        GuiYesNoCallback guiyesnocallback = (result, id) -> {
                            if (result) {
                                selectIndex(index);
                            }

                            mc.displayGuiScreen(shadersGui);
                        };
                        GuiYesNo guiyesno = new GuiYesNo(guiyesnocallback, s4, s5, 0);
                        mc.displayGuiScreen(guiyesno);
                        return false;
                    }
                }
            }
        }
    }

    protected boolean isSelected(int index) {
        return index == selectedIndex;
    }

    protected int getScrollBarX() {
        return width - 6;
    }

    protected int getContentHeight() {
        return getSize() * 18;
    }

    protected void drawBackground() {
    }

    protected void drawSlot(int index, int posX, int posY, int contentY, int mouseX, int mouseY) {
        String s = shaderslist.get(index);

        if (s.equals("OFF")) {
            s = Lang.get("of.options.shaders.packNone");
        } else if (s.equals("(internal)")) {
            s = Lang.get("of.options.shaders.packDefault");
        }

        shadersGui.drawCenteredString(s, width / 2, posY + 1, 14737632);
    }

}
