package net.minecraft.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.minecraft.client.network.NetHandlerHandshakeMemory;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class NetworkSystem {
    public static final LazyLoadBase<MultiThreadIoEventLoopGroup> EVENT_LOOPS = new LazyLoadBase<>() {
        protected MultiThreadIoEventLoopGroup load() {
            return new MultiThreadIoEventLoopGroup(0, Thread.ofVirtual().name("Netty Server IO #%d", 0).factory(), NioIoHandler.newFactory());
        }
    };
    public static final LazyLoadBase<MultiThreadIoEventLoopGroup> SERVER_LOCAL_EVENTLOOP = new LazyLoadBase<>() {
        protected MultiThreadIoEventLoopGroup load() {
            return new MultiThreadIoEventLoopGroup(0, Thread.ofVirtual().name("Netty Local Server IO #%d", 0).factory(), LocalIoHandler.newFactory());
        }
    };
    private static final Logger logger = LogManager.getLogger();
    private final MinecraftServer mcServer;
    private final List<ChannelFuture> endpoints = Collections.synchronizedList(new ArrayList<>());
    private final List<NetworkManager> networkManagers = Collections.synchronizedList(new ArrayList<>());
    public volatile boolean isAlive;

    public NetworkSystem(MinecraftServer server) {
        mcServer = server;
        isAlive = true;
    }

    public void addLanEndpoint(InetAddress address, int port) {
        synchronized (endpoints) {
            Class<? extends ServerSocketChannel> oclass = NioServerSocketChannel.class;

            endpoints.add((new ServerBootstrap()).channel(oclass).childHandler(new ChannelInitializer<>() {
                protected void initChannel(Channel channel) {
                    channel.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                    channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("legacy_query", new PingResponseHandler(NetworkSystem.this)).addLast("splitter", new MessageDeserializer2()).addLast("decoder", new MessageDeserializer(EnumPacketDirection.SERVERBOUND)).addLast("prepender", new MessageSerializer2()).addLast("encoder", new MessageSerializer(EnumPacketDirection.CLIENTBOUND));
                    NetworkManager manager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    networkManagers.add(manager);
                    channel.pipeline().addLast("packet_handler", manager);
                    manager.setNetHandler(new NetHandlerHandshakeTCP(mcServer, manager));
                }
            }).group((EVENT_LOOPS).getValue()).localAddress(address, port).bind().syncUninterruptibly());
        }
    }

    public SocketAddress addLocalEndpoint() {
        ChannelFuture channelfuture;

        synchronized (endpoints) {
            channelfuture = (new ServerBootstrap()).channel(LocalServerChannel.class).childHandler(new ChannelInitializer<>() {
                protected void initChannel(Channel p_initChannel_1_) {
                    NetworkManager networkmanager = new NetworkManager(EnumPacketDirection.SERVERBOUND);
                    networkmanager.setNetHandler(new NetHandlerHandshakeMemory(mcServer, networkmanager));
                    networkManagers.add(networkmanager);
                    p_initChannel_1_.pipeline().addLast("packet_handler", networkmanager);
                }
            }).group(SERVER_LOCAL_EVENTLOOP.getValue()).localAddress(LocalAddress.ANY).bind().syncUninterruptibly();
            endpoints.add(channelfuture);
        }

        return channelfuture.channel().localAddress();
    }

    public void terminateEndpoints() {
        isAlive = false;

        for (ChannelFuture channelfuture : endpoints) {
            try {
                channelfuture.channel().close().sync();
            } catch (InterruptedException var4) {
                logger.error("Interrupted whilst closing channel");
            }
        }
    }

    public void networkTick() {
        synchronized (networkManagers) {
            Iterator<NetworkManager> iterator = networkManagers.iterator();

            while (iterator.hasNext()) {
                final NetworkManager networkmanager = iterator.next();

                if (!networkmanager.hasNoChannel()) {
                    if (!networkmanager.isChannelOpen()) {
                        iterator.remove();
                        networkmanager.checkDisconnected();
                    } else {
                        try {
                            networkmanager.processReceivedPackets();
                        } catch (Exception exception) {
                            if (networkmanager.isLocalChannel()) {
                                CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Ticking connection");
                                crashreportcategory.addCrashSectionCallable("Connection", networkmanager::toString);
                                throw new ReportedException(crashreport);
                            }

                            logger.warn("Failed to handle packet for {}", networkmanager.getRemoteAddress(), exception);
                            final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");
                            networkmanager.sendPacket(new S40PacketDisconnect(chatcomponenttext), p_operationComplete_1_ -> networkmanager.closeChannel(chatcomponenttext));
                            networkmanager.disableAutoRead();
                        }
                    }
                }
            }
        }
    }

    public MinecraftServer getServer() {
        return mcServer;
    }
}
