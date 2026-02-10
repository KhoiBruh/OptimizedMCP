package net.minecraft.client.gui;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.MathHelper;

public class GuiTextField extends Gui {
    private final int id;
    private final FontRenderer fontRendererInstance;
    private final int width;
    private final int height;
    public int xPosition;
    public int yPosition;
    private String text = "";
    private int maxStringLength = 32;
    private int cursorCounter;
    private boolean enableBackgroundDrawing = true;
    private boolean canLoseFocus = true;
    private boolean isFocused;
    private boolean isEnabled = true;
    private int lineScrollOffset;
    private int cursorPosition;
    private int selectionEnd;
    private int enabledColor = 14737632;
    private int disabledColor = 7368816;
    private boolean visible = true;
    private GuiPageButtonList.GuiResponder field_175210_x;
    private Predicate<String> validator = Predicates.alwaysTrue();

    public GuiTextField(int componentId, FontRenderer fontrendererObj, int x, int y, int par5Width, int par6Height) {
        id = componentId;
        fontRendererInstance = fontrendererObj;
        xPosition = x;
        yPosition = y;
        width = par5Width;
        height = par6Height;
    }

    public void func_175207_a(GuiPageButtonList.GuiResponder p_175207_1_) {
        field_175210_x = p_175207_1_;
    }

    public void updateCursorCounter() {
        ++cursorCounter;
    }

    public String getText() {
        return text;
    }

    public void setText(String p_146180_1_) {
        if (validator.apply(p_146180_1_)) {
            if (p_146180_1_.length() > maxStringLength) {
                text = p_146180_1_.substring(0, maxStringLength);
            } else {
                text = p_146180_1_;
            }

            setCursorPositionEnd();
        }
    }

    public String getSelectedText() {
        int i = Math.min(cursorPosition, selectionEnd);
        int j = Math.max(cursorPosition, selectionEnd);
        return text.substring(i, j);
    }

    public void setValidator(Predicate<String> theValidator) {
        validator = theValidator;
    }

    public void writeText(String p_146191_1_) {
        String s = "";
        String s1 = ChatAllowedCharacters.filterAllowedCharacters(p_146191_1_);
        int i = Math.min(cursorPosition, selectionEnd);
        int j = Math.max(cursorPosition, selectionEnd);
        int k = maxStringLength - text.length() - (i - j);
        int l;

        if (!text.isEmpty()) {
            s = s + text.substring(0, i);
        }

        if (k < s1.length()) {
            s = s + s1.substring(0, k);
            l = k;
        } else {
            s = s + s1;
            l = s1.length();
        }

        if (!text.isEmpty() && j < text.length()) {
            s = s + text.substring(j);
        }

        if (validator.apply(s)) {
            text = s;
            moveCursorBy(i - selectionEnd + l);

            if (field_175210_x != null) {
                field_175210_x.func_175319_a(id, text);
            }
        }
    }

    public void deleteWords(int p_146177_1_) {
        if (!text.isEmpty()) {
            if (selectionEnd != cursorPosition) {
                writeText("");
            } else {
                deleteFromCursor(getNthWordFromCursor(p_146177_1_) - cursorPosition);
            }
        }
    }

    public void deleteFromCursor(int p_146175_1_) {
        if (!text.isEmpty()) {
            if (selectionEnd != cursorPosition) {
                writeText("");
            } else {
                boolean flag = p_146175_1_ < 0;
                int i = flag ? cursorPosition + p_146175_1_ : cursorPosition;
                int j = flag ? cursorPosition : cursorPosition + p_146175_1_;
                String s = "";

                if (i >= 0) {
                    s = text.substring(0, i);
                }

                if (j < text.length()) {
                    s = s + text.substring(j);
                }

                if (validator.apply(s)) {
                    text = s;

                    if (flag) {
                        moveCursorBy(p_146175_1_);
                    }

                    if (field_175210_x != null) {
                        field_175210_x.func_175319_a(id, text);
                    }
                }
            }
        }
    }

    public int getId() {
        return id;
    }

    public int getNthWordFromCursor(int p_146187_1_) {
        return getNthWordFromPos(p_146187_1_, cursorPosition);
    }

    public int getNthWordFromPos(int p_146183_1_, int p_146183_2_) {
        return func_146197_a(p_146183_1_, p_146183_2_, true);
    }

    public int func_146197_a(int p_146197_1_, int p_146197_2_, boolean p_146197_3_) {
        int i = p_146197_2_;
        boolean flag = p_146197_1_ < 0;
        int j = Math.abs(p_146197_1_);

        for (int k = 0; k < j; ++k) {
            if (!flag) {
                int l = text.length();
                i = text.indexOf(32, i);

                if (i == -1) {
                    i = l;
                } else {
                    while (p_146197_3_ && i < l && text.charAt(i) == 32) {
                        ++i;
                    }
                }
            } else {
                while (p_146197_3_ && i > 0 && text.charAt(i - 1) == 32) {
                    --i;
                }

                while (i > 0 && text.charAt(i - 1) != 32) {
                    --i;
                }
            }
        }

        return i;
    }

    public void moveCursorBy(int p_146182_1_) {
        setCursorPosition(selectionEnd + p_146182_1_);
    }

    public void setCursorPositionZero() {
        setCursorPosition(0);
    }

    public void setCursorPositionEnd() {
        setCursorPosition(text.length());
    }

    public boolean textboxKeyTyped(char p_146201_1_, int p_146201_2_) {
        if (!isFocused) {
            return false;
        } else if (GuiScreen.isKeyComboCtrlA(p_146201_2_)) {
            setCursorPositionEnd();
            setSelectionPos(0);
            return true;
        } else if (GuiScreen.isKeyComboCtrlC(p_146201_2_)) {
            GuiScreen.setClipboardString(getSelectedText());
            return true;
        } else if (GuiScreen.isKeyComboCtrlV(p_146201_2_)) {
            if (isEnabled) {
                writeText(GuiScreen.getClipboardString());
            }

            return true;
        } else if (GuiScreen.isKeyComboCtrlX(p_146201_2_)) {
            GuiScreen.setClipboardString(getSelectedText());

            if (isEnabled) {
                writeText("");
            }

            return true;
        } else {
            switch (p_146201_2_) {
                case 14:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (isEnabled) {
                            deleteWords(-1);
                        }
                    } else if (isEnabled) {
                        deleteFromCursor(-1);
                    }

                    return true;

                case 199:
                    if (GuiScreen.isShiftKeyDown()) {
                        setSelectionPos(0);
                    } else {
                        setCursorPositionZero();
                    }

                    return true;

                case 203:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            setSelectionPos(getNthWordFromPos(-1, selectionEnd));
                        } else {
                            setSelectionPos(selectionEnd - 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        setCursorPosition(getNthWordFromCursor(-1));
                    } else {
                        moveCursorBy(-1);
                    }

                    return true;

                case 205:
                    if (GuiScreen.isShiftKeyDown()) {
                        if (GuiScreen.isCtrlKeyDown()) {
                            setSelectionPos(getNthWordFromPos(1, selectionEnd));
                        } else {
                            setSelectionPos(selectionEnd + 1);
                        }
                    } else if (GuiScreen.isCtrlKeyDown()) {
                        setCursorPosition(getNthWordFromCursor(1));
                    } else {
                        moveCursorBy(1);
                    }

                    return true;

                case 207:
                    if (GuiScreen.isShiftKeyDown()) {
                        setSelectionPos(text.length());
                    } else {
                        setCursorPositionEnd();
                    }

                    return true;

                case 211:
                    if (GuiScreen.isCtrlKeyDown()) {
                        if (isEnabled) {
                            deleteWords(1);
                        }
                    } else if (isEnabled) {
                        deleteFromCursor(1);
                    }

                    return true;

                default:
                    if (ChatAllowedCharacters.isAllowedCharacter(p_146201_1_)) {
                        if (isEnabled) {
                            writeText(Character.toString(p_146201_1_));
                        }

                        return true;
                    } else {
                        return false;
                    }
            }
        }
    }

    public void mouseClicked(int p_146192_1_, int p_146192_2_, int p_146192_3_) {
        boolean flag = p_146192_1_ >= xPosition && p_146192_1_ < xPosition + width && p_146192_2_ >= yPosition && p_146192_2_ < yPosition + height;

        if (canLoseFocus) {
            setFocused(flag);
        }

        if (isFocused && flag && p_146192_3_ == 0) {
            int i = p_146192_1_ - xPosition;

            if (enableBackgroundDrawing) {
                i -= 4;
            }

            String s = fontRendererInstance.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
            setCursorPosition(fontRendererInstance.trimStringToWidth(s, i).length() + lineScrollOffset);
        }
    }

    public void drawTextBox() {
        if (visible) {
            if (enableBackgroundDrawing) {
                drawRect(xPosition - 1, yPosition - 1, xPosition + width + 1, yPosition + height + 1, -6250336);
                drawRect(xPosition, yPosition, xPosition + width, yPosition + height, -16777216);
            }

            int i = isEnabled ? enabledColor : disabledColor;
            int j = cursorPosition - lineScrollOffset;
            int k = selectionEnd - lineScrollOffset;
            String s = fontRendererInstance.trimStringToWidth(text.substring(lineScrollOffset), getWidth());
            boolean flag = j >= 0 && j <= s.length();
            boolean flag1 = isFocused && cursorCounter / 6 % 2 == 0 && flag;
            int l = enableBackgroundDrawing ? xPosition + 4 : xPosition;
            int i1 = enableBackgroundDrawing ? yPosition + (height - 8) / 2 : yPosition;
            int j1 = l;

            if (k > s.length()) {
                k = s.length();
            }

            if (!s.isEmpty()) {
                String s1 = flag ? s.substring(0, j) : s;
                j1 = fontRendererInstance.drawStringWithShadow(s1, (float) l, (float) i1, i);
            }

            boolean flag2 = cursorPosition < text.length() || text.length() >= maxStringLength;
            int k1 = j1;

            if (!flag) {
                k1 = j > 0 ? l + width : l;
            } else if (flag2) {
                k1 = j1 - 1;
                --j1;
            }

            if (!s.isEmpty() && flag && j < s.length()) {
                fontRendererInstance.drawStringWithShadow(s.substring(j), (float) j1, (float) i1, i);
            }

            if (flag1) {
                if (flag2) {
                    Gui.drawRect(k1, i1 - 1, k1 + 1, i1 + 1 + fontRendererInstance.FONT_HEIGHT, -3092272);
                } else {
                    fontRendererInstance.drawStringWithShadow("_", (float) k1, (float) i1, i);
                }
            }

            if (k != j) {
                int l1 = l + fontRendererInstance.getStringWidth(s.substring(0, k));
                drawCursorVertical(k1, i1 - 1, l1 - 1, i1 + 1 + fontRendererInstance.FONT_HEIGHT);
            }
        }
    }

    private void drawCursorVertical(int p_146188_1_, int p_146188_2_, int p_146188_3_, int p_146188_4_) {
        if (p_146188_1_ < p_146188_3_) {
            int i = p_146188_1_;
            p_146188_1_ = p_146188_3_;
            p_146188_3_ = i;
        }

        if (p_146188_2_ < p_146188_4_) {
            int j = p_146188_2_;
            p_146188_2_ = p_146188_4_;
            p_146188_4_ = j;
        }

        if (p_146188_3_ > xPosition + width) {
            p_146188_3_ = xPosition + width;
        }

        if (p_146188_1_ > xPosition + width) {
            p_146188_1_ = xPosition + width;
        }

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        GlStateManager.color(0.0F, 0.0F, 255.0F, 255.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.enableColorLogic();
        GlStateManager.colorLogicOp(5387);
        worldrenderer.begin(7, DefaultVertexFormats.POSITION);
        worldrenderer.pos(p_146188_1_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_4_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_3_, p_146188_2_, 0.0D).endVertex();
        worldrenderer.pos(p_146188_1_, p_146188_2_, 0.0D).endVertex();
        tessellator.draw();
        GlStateManager.disableColorLogic();
        GlStateManager.enableTexture2D();
    }

    public int getMaxStringLength() {
        return maxStringLength;
    }

    public void setMaxStringLength(int p_146203_1_) {
        maxStringLength = p_146203_1_;

        if (text.length() > p_146203_1_) {
            text = text.substring(0, p_146203_1_);
        }
    }

    public int getCursorPosition() {
        return cursorPosition;
    }

    public void setCursorPosition(int p_146190_1_) {
        cursorPosition = p_146190_1_;
        int i = text.length();
        cursorPosition = MathHelper.clamp(cursorPosition, 0, i);
        setSelectionPos(cursorPosition);
    }

    public boolean getEnableBackgroundDrawing() {
        return enableBackgroundDrawing;
    }

    public void setEnableBackgroundDrawing(boolean p_146185_1_) {
        enableBackgroundDrawing = p_146185_1_;
    }

    public void setTextColor(int p_146193_1_) {
        enabledColor = p_146193_1_;
    }

    public void setDisabledTextColour(int p_146204_1_) {
        disabledColor = p_146204_1_;
    }

    public boolean isFocused() {
        return isFocused;
    }

    public void setFocused(boolean p_146195_1_) {
        if (p_146195_1_ && !isFocused) {
            cursorCounter = 0;
        }

        isFocused = p_146195_1_;
    }

    public void setEnabled(boolean p_146184_1_) {
        isEnabled = p_146184_1_;
    }

    public int getSelectionEnd() {
        return selectionEnd;
    }

    public int getWidth() {
        return enableBackgroundDrawing ? width - 8 : width;
    }

    public void setSelectionPos(int p_146199_1_) {
        int i = text.length();

        if (p_146199_1_ > i) {
            p_146199_1_ = i;
        }

        if (p_146199_1_ < 0) {
            p_146199_1_ = 0;
        }

        selectionEnd = p_146199_1_;

        if (fontRendererInstance != null) {
            if (lineScrollOffset > i) {
                lineScrollOffset = i;
            }

            int j = getWidth();
            String s = fontRendererInstance.trimStringToWidth(text.substring(lineScrollOffset), j);
            int k = s.length() + lineScrollOffset;

            if (p_146199_1_ == lineScrollOffset) {
                lineScrollOffset -= fontRendererInstance.trimStringToWidth(text, j, true).length();
            }

            if (p_146199_1_ > k) {
                lineScrollOffset += p_146199_1_ - k;
            } else if (p_146199_1_ <= lineScrollOffset) {
                lineScrollOffset -= lineScrollOffset - p_146199_1_;
            }

            lineScrollOffset = MathHelper.clamp(lineScrollOffset, 0, i);
        }
    }

    public void setCanLoseFocus(boolean p_146205_1_) {
        canLoseFocus = p_146205_1_;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setVisible(boolean p_146189_1_) {
        visible = p_146189_1_;
    }
}
