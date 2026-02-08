package net.minecraft.client.gui;

import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.util.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiChat extends GuiScreen {
    private static final Logger logger = LogManager.getLogger();
    protected GuiTextField inputField;
    private String historyBuffer = "";
    private int sentHistoryCursor = -1;
    private boolean playerNamesFound;
    private boolean waitingOnAutocomplete;
    private int autocompleteIndex;
    private final List<String> foundPlayerNames = new ArrayList<>();
    private String defaultInputFieldText = "";

    public GuiChat() {
    }

    public GuiChat(String defaultText) {
        defaultInputFieldText = defaultText;
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        sentHistoryCursor = mc.ingameGUI.getChatGUI().getSentMessages().size();
        inputField = new GuiTextField(0, fontRendererObj, 4, height - 12, width - 4, 12);
        inputField.setMaxStringLength(100);
        inputField.setEnableBackgroundDrawing(false);
        inputField.setFocused(true);
        inputField.setText(defaultInputFieldText);
        inputField.setCanLoseFocus(false);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        mc.ingameGUI.getChatGUI().resetScroll();
    }

    public void updateScreen() {
        inputField.updateCursorCounter();
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        waitingOnAutocomplete = false;

        if (keyCode == 15) {
            autocompletePlayerNames();
        } else {
            playerNamesFound = false;
        }

        if (keyCode == 1) {
            mc.displayGuiScreen(null);
        } else if (keyCode != 28 && keyCode != 156) {
            if (keyCode == 200) {
                getSentHistory(-1);
            } else if (keyCode == 208) {
                getSentHistory(1);
            } else if (keyCode == 201) {
                mc.ingameGUI.getChatGUI().scroll(mc.ingameGUI.getChatGUI().getLineCount() - 1);
            } else if (keyCode == 209) {
                mc.ingameGUI.getChatGUI().scroll(-mc.ingameGUI.getChatGUI().getLineCount() + 1);
            } else {
                inputField.textboxKeyTyped(typedChar, keyCode);
            }
        } else {
            String s = inputField.getText().trim();

            if (!s.isEmpty()) {
                sendChatMessage(s);
            }

            mc.displayGuiScreen(null);
        }
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int i = Mouse.getEventDWheel();

        if (i != 0) {
            if (i > 1) {
                i = 1;
            }

            if (i < -1) {
                i = -1;
            }

            if (!isShiftKeyDown()) {
                i *= 7;
            }

            mc.ingameGUI.getChatGUI().scroll(i);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            IChatComponent ichatcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

            if (handleComponentClick(ichatcomponent)) {
                return;
            }
        }

        inputField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    protected void setText(String newChatText, boolean shouldOverwrite) {
        if (shouldOverwrite) {
            inputField.setText(newChatText);
        } else {
            inputField.writeText(newChatText);
        }
    }

    public void autocompletePlayerNames() {
        if (playerNamesFound) {
            inputField.deleteFromCursor(inputField.func_146197_a(-1, inputField.getCursorPosition(), false) - inputField.getCursorPosition());

            if (autocompleteIndex >= foundPlayerNames.size()) {
                autocompleteIndex = 0;
            }
        } else {
            int i = inputField.func_146197_a(-1, inputField.getCursorPosition(), false);
            foundPlayerNames.clear();
            autocompleteIndex = 0;
            String s = inputField.getText().substring(i).toLowerCase();
            String s1 = inputField.getText().substring(0, inputField.getCursorPosition());
            sendAutocompleteRequest(s1, s);

            if (foundPlayerNames.isEmpty()) {
                return;
            }

            playerNamesFound = true;
            inputField.deleteFromCursor(i - inputField.getCursorPosition());
        }

        if (foundPlayerNames.size() > 1) {
            StringBuilder stringbuilder = new StringBuilder();

            for (String s2 : foundPlayerNames) {
                if (!stringbuilder.isEmpty()) {
                    stringbuilder.append(", ");
                }

                stringbuilder.append(s2);
            }

            mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new ChatComponentText(stringbuilder.toString()), 1);
        }

        inputField.writeText(foundPlayerNames.get(autocompleteIndex++));
    }

    private void sendAutocompleteRequest(String p_146405_1_, String p_146405_2_) {
        if (!p_146405_1_.isEmpty()) {
            BlockPos blockpos = null;

            if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                blockpos = mc.objectMouseOver.getBlockPos();
            }

            mc.thePlayer.sendQueue.addToSendQueue(new C14PacketTabComplete(p_146405_1_, blockpos));
            waitingOnAutocomplete = true;
        }
    }

    public void getSentHistory(int msgPos) {
        int i = sentHistoryCursor + msgPos;
        int j = mc.ingameGUI.getChatGUI().getSentMessages().size();
        i = MathHelper.clamp_int(i, 0, j);

        if (i != sentHistoryCursor) {
            if (i == j) {
                sentHistoryCursor = j;
                inputField.setText(historyBuffer);
            } else {
                if (sentHistoryCursor == j) {
                    historyBuffer = inputField.getText();
                }

                inputField.setText(mc.ingameGUI.getChatGUI().getSentMessages().get(i));
                sentHistoryCursor = i;
            }
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawRect(2, height - 14, width - 2, height - 2, Integer.MIN_VALUE);
        inputField.drawTextBox();
        IChatComponent ichatcomponent = mc.ingameGUI.getChatGUI().getChatComponent(Mouse.getX(), Mouse.getY());

        if (ichatcomponent != null && ichatcomponent.getChatStyle().getChatHoverEvent() != null) {
            handleComponentHover(ichatcomponent, mouseX, mouseY);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    public void onAutocompleteResponse(String[] p_146406_1_) {
        if (waitingOnAutocomplete) {
            playerNamesFound = false;
            foundPlayerNames.clear();

            for (String s : p_146406_1_) {
                if (!s.isEmpty()) {
                    foundPlayerNames.add(s);
                }
            }

            String s1 = inputField.getText().substring(inputField.func_146197_a(-1, inputField.getCursorPosition(), false));
            String s2 = StringUtils.getCommonPrefix(p_146406_1_);

            if (!s2.isEmpty() && !s1.equalsIgnoreCase(s2)) {
                inputField.deleteFromCursor(inputField.func_146197_a(-1, inputField.getCursorPosition(), false) - inputField.getCursorPosition());
                inputField.writeText(s2);
            } else if (!foundPlayerNames.isEmpty()) {
                playerNamesFound = true;
                autocompletePlayerNames();
            }
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }
}
