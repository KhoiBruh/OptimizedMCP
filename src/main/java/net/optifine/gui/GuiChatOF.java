package net.optifine.gui;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiVideoSettings;
import net.minecraft.src.Config;
import net.optifine.shaders.Shaders;

public class GuiChatOF extends GuiChat {

    public GuiChatOF(GuiChat guiChat) {
        super(GuiVideoSettings.getGuiChatText(guiChat));
    }

    public void sendChatMessage(String msg) {
        if (checkCustomCommand(msg)) {
            mc.ingameGUI.getChatGUI().addToSentMessages(msg);
        } else {
            super.sendChatMessage(msg);
        }
    }

    private boolean checkCustomCommand(String msg) {
        if (msg == null) {
            return false;
        } else {
            msg = msg.trim();

            if (msg.equals("/reloadShaders")) {
                if (Config.isShaders()) {
                    Shaders.uninit();
                    Shaders.loadShaderPack();
                }

                return true;
            } else if (msg.equals("/reloadChunks")) {
                mc.renderGlobal.loadRenderers();
                return true;
            } else {
                return false;
            }
        }
    }
}
