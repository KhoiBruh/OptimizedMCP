package net.minecraft.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.resources.I18n;
import net.minecraft.command.server.CommandBlockLogic;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiCommandBlock extends GuiScreen {
    private static final Logger field_146488_a = LogManager.getLogger();
    private final CommandBlockLogic localCommandBlock;
    private GuiTextField commandTextField;
    private GuiTextField previousOutputTextField;
    private GuiButton doneBtn;
    private GuiButton cancelBtn;
    private GuiButton field_175390_s;
    private boolean field_175389_t;

    public GuiCommandBlock(CommandBlockLogic p_i45032_1_) {
        localCommandBlock = p_i45032_1_;
    }

    public void updateScreen() {
        commandTextField.updateCursorCounter();
    }

    public void initGui() {
        Keyboard.enableRepeatEvents(true);
        buttonList.clear();
        buttonList.add(doneBtn = new GuiButton(0, width / 2 - 4 - 150, height / 4 + 120 + 12, 150, 20, I18n.format("gui.done")));
        buttonList.add(cancelBtn = new GuiButton(1, width / 2 + 4, height / 4 + 120 + 12, 150, 20, I18n.format("gui.cancel")));
        buttonList.add(field_175390_s = new GuiButton(4, width / 2 + 150 - 20, 150, 20, 20, "O"));
        commandTextField = new GuiTextField(2, fontRendererObj, width / 2 - 150, 50, 300, 20);
        commandTextField.setMaxStringLength(32767);
        commandTextField.setFocused(true);
        commandTextField.setText(localCommandBlock.getCommand());
        previousOutputTextField = new GuiTextField(3, fontRendererObj, width / 2 - 150, 150, 276, 20);
        previousOutputTextField.setMaxStringLength(32767);
        previousOutputTextField.setEnabled(false);
        previousOutputTextField.setText("-");
        field_175389_t = localCommandBlock.shouldTrackOutput();
        func_175388_a();
        doneBtn.enabled = !commandTextField.getText().trim().isEmpty();
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    protected void actionPerformed(GuiButton button) {
        if (button.enabled) {
            if (button.id == 1) {
                localCommandBlock.setTrackOutput(field_175389_t);
                mc.displayGuiScreen(null);
            } else if (button.id == 0) {
                PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
                packetbuffer.writeByte(localCommandBlock.func_145751_f());
                localCommandBlock.func_145757_a(packetbuffer);
                packetbuffer.writeString(commandTextField.getText());
                packetbuffer.writeBoolean(localCommandBlock.shouldTrackOutput());
                mc.getNetHandler().addToSendQueue(new C17PacketCustomPayload("MC|AdvCdm", packetbuffer));

                if (!localCommandBlock.shouldTrackOutput()) {
                    localCommandBlock.setLastOutput(null);
                }

                mc.displayGuiScreen(null);
            } else if (button.id == 4) {
                localCommandBlock.setTrackOutput(!localCommandBlock.shouldTrackOutput());
                func_175388_a();
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        commandTextField.textboxKeyTyped(typedChar, keyCode);
        previousOutputTextField.textboxKeyTyped(typedChar, keyCode);
        doneBtn.enabled = !commandTextField.getText().trim().isEmpty();

        if (keyCode != 28 && keyCode != 156) {
            if (keyCode == 1) {
                actionPerformed(cancelBtn);
            }
        } else {
            actionPerformed(doneBtn);
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        commandTextField.mouseClicked(mouseX, mouseY, mouseButton);
        previousOutputTextField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("advMode.setCommand"), width / 2, 20, 16777215);
        drawString(fontRendererObj, I18n.format("advMode.command"), width / 2 - 150, 37, 10526880);
        commandTextField.drawTextBox();
        int i = 75;
        int j = 0;
        drawString(fontRendererObj, I18n.format("advMode.nearestPlayer"), width / 2 - 150, i + j++ * fontRendererObj.FONT_HEIGHT, 10526880);
        drawString(fontRendererObj, I18n.format("advMode.randomPlayer"), width / 2 - 150, i + j++ * fontRendererObj.FONT_HEIGHT, 10526880);
        drawString(fontRendererObj, I18n.format("advMode.allPlayers"), width / 2 - 150, i + j++ * fontRendererObj.FONT_HEIGHT, 10526880);
        drawString(fontRendererObj, I18n.format("advMode.allEntities"), width / 2 - 150, i + j++ * fontRendererObj.FONT_HEIGHT, 10526880);
        drawString(fontRendererObj, "", width / 2 - 150, i + j++ * fontRendererObj.FONT_HEIGHT, 10526880);

        if (!previousOutputTextField.getText().isEmpty()) {
            i = i + j * fontRendererObj.FONT_HEIGHT + 16;
            drawString(fontRendererObj, I18n.format("advMode.previousOutput"), width / 2 - 150, i, 10526880);
            previousOutputTextField.drawTextBox();
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void func_175388_a() {
        if (localCommandBlock.shouldTrackOutput()) {
            field_175390_s.displayString = "O";

            if (localCommandBlock.getLastOutput() != null) {
                previousOutputTextField.setText(localCommandBlock.getLastOutput().getUnformattedText());
            }
        } else {
            field_175390_s.displayString = "X";
            previousOutputTextField.setText("-");
        }
    }
}
