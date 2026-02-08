package net.minecraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GuiNewChat extends Gui {
    private static final Logger logger = LogManager.getLogger();
    private final Minecraft mc;
    private final List<String> sentMessages = new ArrayList<>();
    private final List<ChatLine> chatLines = new ArrayList<>();
    private final List<ChatLine> drawnChatLines = new ArrayList<>();
    private int scrollPos;
    private boolean isScrolled;

    public GuiNewChat(Minecraft mcIn) {
        mc = mcIn;
    }

    public static int calculateChatboxWidth(float scale) {
        int i = 320;
        int j = 40;
        return MathHelper.floor_float(scale * (float) (i - j) + (float) j);
    }

    public static int calculateChatboxHeight(float scale) {
        int i = 180;
        int j = 20;
        return MathHelper.floor_float(scale * (float) (i - j) + (float) j);
    }

    public void drawChat(int updateCounter) {
        if (mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = getLineCount();
            boolean flag = false;
            int j = 0;
            int k = drawnChatLines.size();
            float f = mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0) {
                if (getChatOpen()) {
                    flag = true;
                }

                float f1 = getChatScale();
                int l = MathHelper.ceiling_float_int((float) getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + scrollPos < drawnChatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = drawnChatLines.get(i1 + scrollPos);

                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int) (255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++j;

                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = -i1 * 9;
                                drawRect(i2, j2 - 9, i2 + l + 4, j2, l1 / 2 << 24);
                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                mc.fontRendererObj.drawStringWithShadow(s, (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                if (flag) {
                    int k2 = mc.fontRendererObj.FONT_HEIGHT;
                    GlStateManager.translate(-3.0F, 0.0F, 0.0F);
                    int l2 = k * k2 + k;
                    int i3 = j * k2 + j;
                    int j3 = scrollPos * i3 / k;
                    int k1 = i3 * i3 / l2;

                    if (l2 != i3) {
                        int k3 = j3 > 0 ? 170 : 96;
                        int l3 = isScrolled ? 13382451 : 3355562;
                        drawRect(0, -j3, 2, -j3 - k1, l3 + (k3 << 24));
                        drawRect(2, -j3, 1, -j3 - k1, 13421772 + (k3 << 24));
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    public void clearChatMessages() {
        drawnChatLines.clear();
        chatLines.clear();
        sentMessages.clear();
    }

    public void printChatMessage(IChatComponent chatComponent) {
        printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
        setChatLine(chatComponent, chatLineId, mc.ingameGUI.getUpdateCounter(), false);
        logger.info("[CHAT] {}", chatComponent.getUnformattedText());
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor_float((float) getChatWidth() / getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.splitText(chatComponent, i, mc.fontRendererObj, false, false);
        boolean flag = getChatOpen();

        for (IChatComponent ichatcomponent : list) {
            if (flag && scrollPos > 0) {
                isScrolled = true;
                scroll(1);
            }

            drawnChatLines.addFirst(new ChatLine(updateCounter, ichatcomponent, chatLineId));
        }

        while (drawnChatLines.size() > 100) {
            drawnChatLines.removeLast();
        }

        if (!displayOnly) {
            chatLines.addFirst(new ChatLine(updateCounter, chatComponent, chatLineId));

            while (chatLines.size() > 100) {
                chatLines.removeLast();
            }
        }
    }

    public void refreshChat() {
        drawnChatLines.clear();
        resetScroll();

        for (int i = chatLines.size() - 1; i >= 0; --i) {
            ChatLine chatline = chatLines.get(i);
            setChatLine(chatline.getChatComponent(), chatline.getChatLineID(), chatline.getUpdatedCounter(), true);
        }
    }

    public List<String> getSentMessages() {
        return sentMessages;
    }

    public void addToSentMessages(String message) {
        if (sentMessages.isEmpty() || !sentMessages.getLast().equals(message)) {
            sentMessages.add(message);
        }
    }

    public void resetScroll() {
        scrollPos = 0;
        isScrolled = false;
    }

    public void scroll(int amount) {
        scrollPos += amount;
        int i = drawnChatLines.size();

        if (scrollPos > i - getLineCount()) {
            scrollPos = i - getLineCount();
        }

        if (scrollPos <= 0) {
            scrollPos = 0;
            isScrolled = false;
        }
    }

    public IChatComponent getChatComponent(int mouseX, int mouseY) {
        if (!getChatOpen()) {
            return null;
        } else {
            ScaledResolution scaledresolution = new ScaledResolution(mc);
            int i = scaledresolution.getScaleFactor();
            float f = getChatScale();
            int j = mouseX / i - 3;
            int k = mouseY / i - 27;
            j = MathHelper.floor_float((float) j / f);
            k = MathHelper.floor_float((float) k / f);

            if (j >= 0 && k >= 0) {
                int l = Math.min(getLineCount(), drawnChatLines.size());

                if (j <= MathHelper.floor_float((float) getChatWidth() / getChatScale()) && k < mc.fontRendererObj.FONT_HEIGHT * l + l) {
                    int i1 = k / mc.fontRendererObj.FONT_HEIGHT + scrollPos;

                    if (i1 >= 0 && i1 < drawnChatLines.size()) {
                        ChatLine chatline = drawnChatLines.get(i1);
                        int j1 = 0;

                        for (IChatComponent ichatcomponent : chatline.getChatComponent()) {
                            if (ichatcomponent instanceof ChatComponentText) {
                                j1 += mc.fontRendererObj.getStringWidth(GuiUtilRenderComponents.func_178909_a(((ChatComponentText) ichatcomponent).getChatComponentText_TextValue(), false));

                                if (j1 > j) {
                                    return ichatcomponent;
                                }
                            }
                        }
                    }

                    return null;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    public boolean getChatOpen() {
        return mc.currentScreen instanceof GuiChat;
    }

    public void deleteChatLine(int id) {
        Iterator<ChatLine> iterator = drawnChatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline = iterator.next();

            if (chatline.getChatLineID() == id) {
                iterator.remove();
            }
        }

        iterator = chatLines.iterator();

        while (iterator.hasNext()) {
            ChatLine chatline1 = iterator.next();

            if (chatline1.getChatLineID() == id) {
                iterator.remove();
                break;
            }
        }
    }

    public int getChatWidth() {
        return calculateChatboxWidth(mc.gameSettings.chatWidth);
    }

    public int getChatHeight() {
        return calculateChatboxHeight(getChatOpen() ? mc.gameSettings.chatHeightFocused : mc.gameSettings.chatHeightUnfocused);
    }

    public float getChatScale() {
        return mc.gameSettings.chatScale;
    }

    public int getLineCount() {
        return getChatHeight() / 9;
    }
}
