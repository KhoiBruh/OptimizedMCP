package net.minecraft.client.gui;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.base64.Base64;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.util.ChatFormat;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.image.BufferedImage;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

public class ServerListEntryNormal implements GuiListExtended.IGuiListEntry {
    private static final Logger logger = LogManager.getLogger();
    private static final ThreadPoolExecutor field_148302_b = new ScheduledThreadPoolExecutor(5, (new ThreadFactoryBuilder()).setNameFormat("Server Pinger #%d").setDaemon(true).build());
    private static final ResourceLocation UNKNOWN_SERVER = new ResourceLocation("textures/misc/unknown_server.png");
    private static final ResourceLocation SERVER_SELECTION_BUTTONS = new ResourceLocation("textures/gui/server_selection.png");
    private final GuiMultiplayer owner;
    private final Minecraft mc;
    private final ServerData server;
    private final ResourceLocation serverIcon;
    private String field_148299_g;
    private DynamicTexture field_148305_h;
    private long field_148298_f;

    protected ServerListEntryNormal(GuiMultiplayer p_i45048_1_, ServerData serverIn) {
        owner = p_i45048_1_;
        server = serverIn;
        mc = Minecraft.getMinecraft();
        serverIcon = new ResourceLocation("servers/" + serverIn.serverIP + "/icon");
        field_148305_h = (DynamicTexture) mc.getTextureManager().getTexture(serverIcon);
    }

    public void drawEntry(int slotIndex, int x, int y, int listWidth, int slotHeight, int mouseX, int mouseY, boolean isSelected) {
        if (!server.field_78841_f) {
            server.field_78841_f = true;
            server.pingToServer = -2L;
            server.serverMOTD = "";
            server.populationInfo = "";
            field_148302_b.submit(() -> {
                try {
                    owner.getOldServerPinger().ping(server);
                } catch (UnknownHostException var2) {
                    server.pingToServer = -1L;
                    server.serverMOTD = ChatFormat.DARK_RED + "Can't resolve hostname";
                } catch (Exception var3) {
                    server.pingToServer = -1L;
                    server.serverMOTD = ChatFormat.DARK_RED + "Can't connect to server.";
                }
            });
        }

        boolean flag = server.version > 47;
        boolean flag1 = server.version < 47;
        boolean flag2 = flag || flag1;
        mc.fontRendererObj.drawString(server.serverName, x + 32 + 3, y + 1, 16777215);
        List<String> list = mc.fontRendererObj.listFormattedStringToWidth(server.serverMOTD, listWidth - 32 - 2);

        for (int i = 0; i < Math.min(list.size(), 2); ++i) {
            mc.fontRendererObj.drawString(list.get(i), x + 32 + 3, y + 12 + mc.fontRendererObj.FONT_HEIGHT * i, 8421504);
        }

        String s2 = flag2 ? ChatFormat.DARK_RED + server.gameVersion : server.populationInfo;
        int j = mc.fontRendererObj.getStringWidth(s2);
        mc.fontRendererObj.drawString(s2, x + listWidth - j - 15 - 2, y + 1, 8421504);
        int k = 0;
        String s = null;
        int l;
        String s1;

        if (flag2) {
            l = 5;
            s1 = flag ? "Client out of date!" : "Server out of date!";
            s = server.playerList;
        } else if (server.field_78841_f && server.pingToServer != -2L) {
            if (server.pingToServer < 0L) {
                l = 5;
            } else if (server.pingToServer < 150L) {
                l = 0;
            } else if (server.pingToServer < 300L) {
                l = 1;
            } else if (server.pingToServer < 600L) {
                l = 2;
            } else if (server.pingToServer < 1000L) {
                l = 3;
            } else {
                l = 4;
            }

            if (server.pingToServer < 0L) {
                s1 = "(no connection)";
            } else {
                s1 = server.pingToServer + "ms";
                s = server.playerList;
            }
        } else {
            k = 1;
            l = (int) (Minecraft.getSystemTime() / 100L + (slotIndex * 2L) & 7L);

            if (l > 4) {
                l = 8 - l;
            }

            s1 = "Pinging...";
        }

        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(Gui.icons);
        Gui.drawModalRectWithCustomSizedTexture(x + listWidth - 15, y, (float) (k * 10), (float) (176 + l * 8), 10, 8, 256.0F, 256.0F);

        if (server.getBase64EncodedIconData() != null && !server.getBase64EncodedIconData().equals(field_148299_g)) {
            field_148299_g = server.getBase64EncodedIconData();
            prepareServerIcon();
            owner.getServerList().saveServerList();
        }

        if (field_148305_h != null) {
            drawTextureAt(x, y, serverIcon);
        } else {
            drawTextureAt(x, y, UNKNOWN_SERVER);
        }

        int i1 = mouseX - x;
        int j1 = mouseY - y;

        if (i1 >= listWidth - 15 && i1 <= listWidth - 5 && j1 >= 0 && j1 <= 8) {
            owner.setHoveringText(s1);
        } else if (i1 >= listWidth - j - 15 - 2 && i1 <= listWidth - 15 - 2 && j1 >= 0 && j1 <= 8) {
            owner.setHoveringText(s);
        }

        if (mc.gameSettings.touchscreen || isSelected) {
            mc.getTextureManager().bindTexture(SERVER_SELECTION_BUTTONS);
            Gui.drawRect(x, y, x + 32, y + 32, -1601138544);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            int k1 = mouseX - x;
            int l1 = mouseY - y;

            if (func_178013_b()) {
                if (k1 < 32 && k1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 0.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (owner.func_175392_a(this, slotIndex)) {
                if (k1 < 16 && l1 < 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 96.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }

            if (owner.func_175394_b(this, slotIndex)) {
                if (k1 < 16 && l1 > 16) {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 32.0F, 32, 32, 256.0F, 256.0F);
                } else {
                    Gui.drawModalRectWithCustomSizedTexture(x, y, 64.0F, 0.0F, 32, 32, 256.0F, 256.0F);
                }
            }
        }
    }

    protected void drawTextureAt(int p_178012_1_, int p_178012_2_, ResourceLocation p_178012_3_) {
        mc.getTextureManager().bindTexture(p_178012_3_);
        GlStateManager.enableBlend();
        Gui.drawModalRectWithCustomSizedTexture(p_178012_1_, p_178012_2_, 0.0F, 0.0F, 32, 32, 32.0F, 32.0F);
        GlStateManager.disableBlend();
    }

    private boolean func_178013_b() {
        return true;
    }

    private void prepareServerIcon() {
        if (server.getBase64EncodedIconData() == null) {
            mc.getTextureManager().deleteTexture(serverIcon);
            field_148305_h = null;
        } else {
            ByteBuf bytebuf = Unpooled.copiedBuffer(server.getBase64EncodedIconData(), StandardCharsets.UTF_8);
            ByteBuf bytebuf1 = Base64.decode(bytebuf);
            BufferedImage bufferedimage;
            label101:
            {
                try {
                    bufferedimage = TextureUtil.readBufferedImage(new ByteBufInputStream(bytebuf1));
                    Validate.validState(bufferedimage.getWidth() == 64, "Must be 64 pixels wide");
                    Validate.validState(bufferedimage.getHeight() == 64, "Must be 64 pixels high");
                    break label101;
                } catch (Throwable throwable) {
                    logger.error("Invalid icon for server {} ({})", server.serverName, server.serverIP, throwable);
                    server.setBase64EncodedIconData(null);
                } finally {
                    bytebuf.release();
                    bytebuf1.release();
                }

                return;
            }

            if (field_148305_h == null) {
                field_148305_h = new DynamicTexture(bufferedimage.getWidth(), bufferedimage.getHeight());
                mc.getTextureManager().loadTexture(serverIcon, field_148305_h);
            }

            bufferedimage.getRGB(0, 0, bufferedimage.getWidth(), bufferedimage.getHeight(), field_148305_h.getTextureData(), 0, bufferedimage.getWidth());
            field_148305_h.updateDynamicTexture();
        }
    }

    public boolean mousePressed(int slotIndex, int p_148278_2_, int p_148278_3_, int p_148278_4_, int p_148278_5_, int p_148278_6_) {
        if (p_148278_5_ <= 32) {
            if (p_148278_5_ < 32 && p_148278_5_ > 16 && func_178013_b()) {
                owner.selectServer(slotIndex);
                owner.connectToSelected();
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ < 16 && owner.func_175392_a(this, slotIndex)) {
                owner.func_175391_a(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }

            if (p_148278_5_ < 16 && p_148278_6_ > 16 && owner.func_175394_b(this, slotIndex)) {
                owner.func_175393_b(this, slotIndex, GuiScreen.isShiftKeyDown());
                return true;
            }
        }

        owner.selectServer(slotIndex);

        if (Minecraft.getSystemTime() - field_148298_f < 250L) {
            owner.connectToSelected();
        }

        field_148298_f = Minecraft.getSystemTime();
        return false;
    }

    public void setSelected(int p_178011_1_, int p_178011_2_, int p_178011_3_) {
    }

    public void mouseReleased(int slotIndex, int x, int y, int mouseEvent, int relativeX, int relativeY) {
    }

    public ServerData getServerData() {
        return server;
    }
}
