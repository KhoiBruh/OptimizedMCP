package net.minecraft.network;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.local.LocalAddress;
import io.netty.channel.local.LocalEventLoopGroup;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.ServerSocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import net.minecraft.client.network.NetHandlerHandshakeMemory;
import net.minecraft.crash.CrashReport;
import net.minecraft.crash.CrashReportCategory;
import net.minecraft.network.play.server.S40PacketDisconnect;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.NetHandlerHandshakeTCP;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.LazyLoadBase;
import net.minecraft.util.MessageDeserializer;
import net.minecraft.util.MessageDeserializer2;
import net.minecraft.util.MessageSerializer;
import net.minecraft.util.MessageSerializer2;
import net.minecraft.util.ReportedException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class NetworkSystem
{
    private static final Logger logger = LogManager.getLogger();
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
    private final MinecraftServer mcServer;
    public volatile boolean isAlive;
    private final List<ChannelFuture> endpoints = Collections.<ChannelFuture>synchronizedList(Lists.<ChannelFuture>newArrayList());
    private final List<NetworkManager> networkManagers = Collections.<NetworkManager>synchronizedList(Lists.<NetworkManager>newArrayList());

    public NetworkSystem(MinecraftServer server)
    {
        this.mcServer = server;
        this.isAlive = true;
    }

    public void addLanEndpoint(InetAddress address, int port) throws IOException
    {
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

    public SocketAddress addLocalEndpoint()
    {
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

    public void terminateEndpoints()
    {
        this.isAlive = false;

        for (ChannelFuture channelfuture : this.endpoints)
        {
            try
            {
                channelfuture.channel().close().sync();
            }
            catch (InterruptedException var4)
            {
                logger.error("Interrupted whilst closing channel");
            }
        }
    }

    public void networkTick()
    {
        synchronized (this.networkManagers)
        {
            Iterator<NetworkManager> iterator = this.networkManagers.iterator();

            while (iterator.hasNext())
            {
                final NetworkManager networkmanager = (NetworkManager)iterator.next();

                if (!networkmanager.hasNoChannel())
                {
                    if (!networkmanager.isChannelOpen())
                    {
                        iterator.remove();
                        networkmanager.checkDisconnected();
                    }
                    else
                    {
                        try
                        {
                            networkmanager.processReceivedPackets();
                        }
                        catch (Exception exception)
                        {
                            if (networkmanager.isLocalChannel())
                            {
                                CrashReport crashreport = CrashReport.makeCrashReport(exception, "Ticking memory connection");
                                CrashReportCategory crashreportcategory = crashreport.makeCategory("Ticking connection");
                                crashreportcategory.addCrashSectionCallable("Connection", new Callable<String>()
                                {
                                    public String call() throws Exception
                                    {
                                        return networkmanager.toString();
                                    }
                                });
                                throw new ReportedException(crashreport);
                            }

                            logger.warn((String)("Failed to handle packet for " + networkmanager.getRemoteAddress()), (Throwable)exception);
                            final ChatComponentText chatcomponenttext = new ChatComponentText("Internal server error");
                            networkmanager.sendPacket(new S40PacketDisconnect(chatcomponenttext), new GenericFutureListener < Future <? super Void >> ()
                            {
                                public void operationComplete(Future <? super Void > p_operationComplete_1_) throws Exception
                                {
                                    networkmanager.closeChannel(chatcomponenttext);
                                }
                            }, new GenericFutureListener[0]);
                            networkmanager.disableAutoRead();
                        }
                    }
                }
            }
        }
    }

    public MinecraftServer getServer()
    {
        return this.mcServer;
    }
}
