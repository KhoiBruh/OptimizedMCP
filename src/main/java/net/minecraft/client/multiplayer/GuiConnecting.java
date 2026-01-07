package net.minecraft.client.multiplayer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiDisconnected;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.network.NetHandlerLoginClient;
import net.minecraft.client.resources.I18n;
import net.minecraft.network.EnumConnectionState;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.handshake.client.C00Handshake;
import net.minecraft.network.login.client.C00PacketLoginStart;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.atomic.AtomicInteger;

public class GuiConnecting extends GuiScreen {
    private static final AtomicInteger CONNECTION_ID = new AtomicInteger(0);
    private static final Logger logger = LogManager.getLogger();
    private final GuiScreen previousGuiScreen;
    private NetworkManager networkManager;
    private boolean cancel;

    public GuiConnecting(GuiScreen p_i1181_1_, Minecraft mcIn, ServerData p_i1181_3_) {
        mc = mcIn;
        previousGuiScreen = p_i1181_1_;
        ServerAddress serveraddress = ServerAddress.fromString(p_i1181_3_.serverIP);
        mcIn.loadWorld(null);
        mcIn.setServerData(p_i1181_3_);
        connect(serveraddress.getIP(), serveraddress.getPort());
    }

    public GuiConnecting(GuiScreen p_i1182_1_, Minecraft mcIn, String hostName, int port) {
        mc = mcIn;
        previousGuiScreen = p_i1182_1_;
        mcIn.loadWorld(null);
        connect(hostName, port);
    }

    private void connect(final String ip, final int port) {
        logger.info("Connecting to {}, {}", ip, port);
        (new Thread("Server Connector #" + CONNECTION_ID.incrementAndGet()) {
            public void run() {
                InetAddress inetaddress = null;

                try {
                    if (cancel) {
                        return;
                    }

                    inetaddress = InetAddress.getByName(ip);
                    networkManager = NetworkManager.createNetworkManagerAndConnect(inetaddress, port, mc.gameSettings.isUsingNativeTransport());
                    networkManager.setNetHandler(new NetHandlerLoginClient(networkManager, mc, previousGuiScreen));
                    networkManager.sendPacket(new C00Handshake(47, ip, port, EnumConnectionState.LOGIN));
                    networkManager.sendPacket(new C00PacketLoginStart(mc.getSession().getProfile()));
                } catch (UnknownHostException unknownhostexception) {
                    if (cancel) {
                        return;
                    }

                    GuiConnecting.logger.error("Couldn't connect to server", unknownhostexception);
                    mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", "Unknown host")));
                } catch (Exception exception) {
                    if (cancel) {
                        return;
                    }

                    GuiConnecting.logger.error("Couldn't connect to server", exception);
                    String s = exception.toString();

                    if (inetaddress != null) {
                        String s1 = inetaddress + ":" + port;
                        s = s.replaceAll(s1, "");
                    }

                    mc.displayGuiScreen(new GuiDisconnected(previousGuiScreen, "connect.failed", new ChatComponentTranslation("disconnect.genericReason", s)));
                }
            }
        }).start();
    }

    public void updateScreen() {
        if (networkManager != null) {
            if (networkManager.isChannelOpen()) {
                networkManager.processReceivedPackets();
            } else {
                networkManager.checkDisconnected();
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
    }

    public void initGui() {
        buttonList.clear();
        buttonList.add(new GuiButton(0, width / 2 - 100, height / 4 + 120 + 12, I18n.format("gui.cancel")));
    }

    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            cancel = true;

            if (networkManager != null) {
                networkManager.closeChannel(new ChatComponentText("Aborted"));
            }

            mc.displayGuiScreen(previousGuiScreen);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();

        if (networkManager == null) {
            drawCenteredString(fontRendererObj, I18n.format("connect.connecting"), width / 2, height / 2 - 50, 16777215);
        } else {
            drawCenteredString(fontRendererObj, I18n.format("connect.authorizing"), width / 2, height / 2 - 50, 16777215);
        }

        super.drawScreen(mouseX, mouseY, partialTicks);
    }
}
