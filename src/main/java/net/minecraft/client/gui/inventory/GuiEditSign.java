package net.minecraft.client.gui.inventory;

import net.minecraft.block.Block;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.ChatComponentText;
import org.lwjgl.input.Keyboard;

import java.io.IOException;

public class GuiEditSign extends GuiScreen {
    private final TileEntitySign tileSign;
    private int updateCounter;
    private int editLine;
    private GuiButton doneBtn;

    public GuiEditSign(TileEntitySign teSign) {
        tileSign = teSign;
    }

    public void initGui() {
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        buttonList.add(doneBtn = new GuiButton(0, width / 2 - 100, height / 4 + 120, I18n.format("gui.done")));
        tileSign.setEditable(false);
    }

    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
        NetHandlerPlayClient nethandlerplayclient = mc.getNetHandler();

        if (nethandlerplayclient != null) {
            nethandlerplayclient.addToSendQueue(new C12PacketUpdateSign(tileSign.getPos(), tileSign.signText));
        }

        tileSign.setEditable(true);
    }

    public void updateScreen() {
        ++updateCounter;
    }

    protected void actionPerformed(GuiButton button) throws IOException {
        if (button.enabled) {
            if (button.id == 0) {
                tileSign.markDirty();
                mc.displayGuiScreen(null);
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == 200) {
            editLine = editLine - 1 & 3;
        }

        if (keyCode == 208 || keyCode == 28 || keyCode == 156) {
            editLine = editLine + 1 & 3;
        }

        String s = tileSign.signText[editLine].getUnformattedText();

        if (keyCode == 14 && !s.isEmpty()) {
            s = s.substring(0, s.length() - 1);
        }

        if (ChatAllowedCharacters.isAllowedCharacter(typedChar) && fontRendererObj.getStringWidth(s + typedChar) <= 90) {
            s = s + typedChar;
        }

        tileSign.signText[editLine] = new ChatComponentText(s);

        if (keyCode == 1) {
            actionPerformed(doneBtn);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, I18n.format("sign.edit"), width / 2, 40, 16777215);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.pushMatrix();
        GlStateManager.translate((float) (width / 2), 0.0F, 50.0F);
        float f = 93.75F;
        GlStateManager.scale(-f, -f, -f);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        Block block = tileSign.getBlockType();

        if (block == Blocks.standing_sign) {
            float f1 = (float) (tileSign.getBlockMetadata() * 360) / 16.0F;
            GlStateManager.rotate(f1, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        } else {
            int i = tileSign.getBlockMetadata();
            float f2 = 0.0F;

            if (i == 2) {
                f2 = 180.0F;
            }

            if (i == 4) {
                f2 = 90.0F;
            }

            if (i == 5) {
                f2 = -90.0F;
            }

            GlStateManager.rotate(f2, 0.0F, 1.0F, 0.0F);
            GlStateManager.translate(0.0F, -1.0625F, 0.0F);
        }

        if (updateCounter / 6 % 2 == 0) {
            tileSign.lineBeingEdited = editLine;
        }

        TileEntityRendererDispatcher.instance.renderTileEntityAt(tileSign, -0.5D, -0.75D, -0.5D, 0.0F);
        tileSign.lineBeingEdited = -1;
        GlStateManager.popMatrix();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
