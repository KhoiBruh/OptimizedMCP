package net.minecraft.network;

import com.google.common.collect.Queues;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollIoHandler;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.local.LocalChannel;
import io.netty.channel.local.LocalIoHandler;
import io.netty.channel.local.LocalServerChannel;
import io.netty.channel.nio.NioIoHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.TimeoutException;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.util.*;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import javax.crypto.SecretKey;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class NetworkManager extends SimpleChannelInboundHandler<Packet> {
    public static final Marker logMarkerNetwork = MarkerManager.getMarker("NETWORK");
    public static final Marker logMarkerPackets = MarkerManager.getMarker("NETWORK_PACKETS").setParents(logMarkerNetwork);
    public static final AttributeKey<ConnectionState> attrKeyConnectionState = AttributeKey.valueOf("protocol");
    public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_NIO_EVENTLOOP = new LazyLoadBase<>() {
        protected MultiThreadIoEventLoopGroup load() {
            return new MultiThreadIoEventLoopGroup(0, Thread.ofVirtual().name("Netty Client IO #%d", 0).factory(), NioIoHandler.newFactory());
        }
    };
    public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_EPOLL_EVENTLOOP = new LazyLoadBase<>() {
        protected MultiThreadIoEventLoopGroup load() {
            return new MultiThreadIoEventLoopGroup(0, Thread.ofVirtual().name("Netty Client IO #%d", 0).factory(), EpollIoHandler.newFactory());
        }
    };
    public static final LazyLoadBase<MultiThreadIoEventLoopGroup> CLIENT_LOCAL_EVENTLOOP = new LazyLoadBase<>() {
        protected MultiThreadIoEventLoopGroup load() {
            return new MultiThreadIoEventLoopGroup(0, Thread.ofVirtual().name("Netty Client IO #%d", 0).factory(), LocalIoHandler.newFactory());
        }
    };
    private static final Logger logger = LogManager.getLogger();
    private final PacketDirection direction;
    private final Queue<NetworkManager.InboundHandlerTuplePacketListener> outboundPacketsQueue = new ConcurrentLinkedDeque<>();
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private Channel channel;
    private SocketAddress socketAddress;
    private INetHandler packetListener;
    private IChatComponent terminationReason;
    private boolean isEncrypted;
    private boolean disconnected;

    public NetworkManager(PacketDirection packetDirection) {
        direction = packetDirection;
    }

    public static NetworkManager createNetworkManagerAndConnect(InetAddress address, int serverPort, boolean useNativeTransport) {
        final NetworkManager networkmanager = new NetworkManager(PacketDirection.CLIENTBOUND);
        Class<? extends SocketChannel> oclass;
        LazyLoadBase<? extends EventLoopGroup> lazyloadbase;

        if (Epoll.isAvailable() && useNativeTransport) {
            oclass = EpollSocketChannel.class;
            lazyloadbase = CLIENT_EPOLL_EVENTLOOP;
        } else {
            oclass = NioSocketChannel.class;
            lazyloadbase = CLIENT_NIO_EVENTLOOP;
        }

        new Bootstrap().group(lazyloadbase.getValue()).handler(new ChannelInitializer<>() {
            protected void initChannel(Channel p_initChannel_1_) {
                try {
                    p_initChannel_1_.config().setOption(ChannelOption.TCP_NODELAY, Boolean.TRUE);
                } catch (ChannelException var3) {
                }

                p_initChannel_1_.pipeline().addLast("timeout", new ReadTimeoutHandler(30)).addLast("splitter", new MessageDeserializer2()).addLast("decoder", new MessageDeserializer(PacketDirection.CLIENTBOUND)).addLast("prepender", new MessageSerializer2()).addLast("encoder", new MessageSerializer(PacketDirection.SERVERBOUND)).addLast("packet_handler", networkmanager);
            }
        }).channel(oclass).connect(address, serverPort).syncUninterruptibly();
        return networkmanager;
    }

    public static NetworkManager provideLocalClient(SocketAddress address) {
        final NetworkManager networkmanager = new NetworkManager(PacketDirection.CLIENTBOUND);
        (new Bootstrap()).group(CLIENT_LOCAL_EVENTLOOP.getValue()).handler(new ChannelInitializer<>() {
            protected void initChannel(Channel p_initChannel_1_) {
                p_initChannel_1_.pipeline().addLast("packet_handler", networkmanager);
            }
        }).channel(LocalChannel.class).connect(address).syncUninterruptibly();
        return networkmanager;
    }

    public void channelActive(ChannelHandlerContext p_channelActive_1_) throws Exception {
        super.channelActive(p_channelActive_1_);
        channel = p_channelActive_1_.channel();
        socketAddress = channel.remoteAddress();

        try {
            setConnectionState(ConnectionState.HANDSHAKING);
        } catch (Throwable throwable) {
            logger.fatal(throwable);
        }
    }

    public void setConnectionState(ConnectionState newState) {
        channel.attr(attrKeyConnectionState).set(newState);
        channel.config().setAutoRead(true);
        logger.debug("Enabled auto read");
    }

    public void channelInactive(ChannelHandlerContext p_channelInactive_1_) {
        closeChannel(new ChatComponentTranslation("disconnect.endOfStream"));
    }

    public void exceptionCaught(ChannelHandlerContext p_exceptionCaught_1_, Throwable p_exceptionCaught_2_) {
        ChatComponentTranslation chatcomponenttranslation;

        if (p_exceptionCaught_2_ instanceof TimeoutException) {
            chatcomponenttranslation = new ChatComponentTranslation("disconnect.timeout");
        } else {
            chatcomponenttranslation = new ChatComponentTranslation("disconnect.genericReason", "Internal Exception: " + p_exceptionCaught_2_);
        }

        closeChannel(chatcomponenttranslation);
    }

    protected void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet p_channelRead0_2_) {
        if (channel.isOpen()) {
            try {
                p_channelRead0_2_.processPacket(packetListener);
            } catch (ThreadQuickExitException var4) {
            }
        }
    }

    public void sendPacket(Packet packetIn) {
        if (isChannelOpen()) {
            flushOutboundQueue();
            dispatchPacket(packetIn, null);
        } else {
            readWriteLock.writeLock().lock();

            try {
                outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, (GenericFutureListener<? extends Future<? super Void>>) null));
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    @SafeVarargs
    public final void sendPacket(Packet packetIn, GenericFutureListener<? extends Future<? super Void>> listener, GenericFutureListener<? extends Future<? super Void>>... listeners) {
        if (isChannelOpen()) {
            flushOutboundQueue();
            dispatchPacket(packetIn, ArrayUtils.add(listeners, listener));
        } else {
            readWriteLock.writeLock().lock();

            try {
                outboundPacketsQueue.add(new NetworkManager.InboundHandlerTuplePacketListener(packetIn, ArrayUtils.add(listeners, listener)));
            } finally {
                readWriteLock.writeLock().unlock();
            }
        }
    }

    private void dispatchPacket(final Packet inPacket, final GenericFutureListener<? extends Future<? super Void>>[] futureListeners) {
        final ConnectionState enumconnectionstate = ConnectionState.getFromPacket(inPacket);
        final ConnectionState enumconnectionstate1 = channel.attr(attrKeyConnectionState).get();

        if (enumconnectionstate1 != enumconnectionstate) {
            logger.debug("Disabled auto read");
            channel.config().setAutoRead(false);
        }

        if (channel.eventLoop().inEventLoop()) {
            if (enumconnectionstate != enumconnectionstate1) {
                setConnectionState(enumconnectionstate);
            }

            ChannelFuture channelfuture = channel.writeAndFlush(inPacket);

            if (futureListeners != null) {
                channelfuture.addListeners(futureListeners);
            }

            channelfuture.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        } else {
            channel.eventLoop().execute(() -> {
                if (enumconnectionstate != enumconnectionstate1) {
                    setConnectionState(enumconnectionstate);
                }

                ChannelFuture channelfuture1 = channel.writeAndFlush(inPacket);

                if (futureListeners != null) {
                    channelfuture1.addListeners(futureListeners);
                }

                channelfuture1.addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            });
        }
    }

    private void flushOutboundQueue() {
        if (channel != null && channel.isOpen()) {
            readWriteLock.readLock().lock();

            try {
                while (!outboundPacketsQueue.isEmpty()) {
                    NetworkManager.InboundHandlerTuplePacketListener networkmanager$inboundhandlertuplepacketlistener = outboundPacketsQueue.poll();
                    dispatchPacket(networkmanager$inboundhandlertuplepacketlistener.packet, networkmanager$inboundhandlertuplepacketlistener.futureListeners);
                }
            } finally {
                readWriteLock.readLock().unlock();
            }
        }
    }

    public void processReceivedPackets() {
        flushOutboundQueue();

        if (packetListener instanceof ITickable) {
            ((ITickable) packetListener).update();
        }

        channel.flush();
    }

    public SocketAddress getRemoteAddress() {
        return socketAddress;
    }

    public void closeChannel(IChatComponent message) {
        if (channel.isOpen()) {
            channel.close().awaitUninterruptibly();
            terminationReason = message;
        }
    }

    public boolean isLocalChannel() {
        return channel instanceof LocalChannel || channel instanceof LocalServerChannel;
    }

    public void enableEncryption(SecretKey key) {
        isEncrypted = true;
        channel.pipeline().addBefore("splitter", "decrypt", new NettyEncryptingDecoder(CryptManager.createNetCipherInstance(2, key)));
        channel.pipeline().addBefore("prepender", "encrypt", new NettyEncryptingEncoder(CryptManager.createNetCipherInstance(1, key)));
    }

    public boolean getIsencrypted() {
        return isEncrypted;
    }

    public boolean isChannelOpen() {
        return channel != null && channel.isOpen();
    }

    public boolean hasNoChannel() {
        return channel == null;
    }

    public INetHandler getNetHandler() {
        return packetListener;
    }

    public void setNetHandler(INetHandler handler) {
        Validate.notNull(handler, "packetListener");
        logger.debug("Set listener of {} to {}", new Object[]{this, handler});
        packetListener = handler;
    }

    public IChatComponent getExitMessage() {
        return terminationReason;
    }

    public void disableAutoRead() {
        channel.config().setAutoRead(false);
    }

    public void setCompressionTreshold(int treshold) {
        if (treshold >= 0) {
            if (channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                ((NettyCompressionDecoder) channel.pipeline().get("decompress")).setCompressionTreshold(treshold);
            } else {
                channel.pipeline().addBefore("decoder", "decompress", new NettyCompressionDecoder(treshold));
            }

            if (channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                ((NettyCompressionEncoder) channel.pipeline().get("decompress")).setCompressionTreshold(treshold);
            } else {
                channel.pipeline().addBefore("encoder", "compress", new NettyCompressionEncoder(treshold));
            }
        } else {
            if (channel.pipeline().get("decompress") instanceof NettyCompressionDecoder) {
                channel.pipeline().remove("decompress");
            }

            if (channel.pipeline().get("compress") instanceof NettyCompressionEncoder) {
                channel.pipeline().remove("compress");
            }
        }
    }

    public void checkDisconnected() {
        if (channel != null && !channel.isOpen()) {
            if (!disconnected) {
                disconnected = true;

                if (terminationReason != null) {
                    packetListener.onDisconnect(terminationReason);
                } else if (packetListener != null) {
                    packetListener.onDisconnect(new ChatComponentText("Disconnected"));
                }
            } else {
                logger.warn("handleDisconnection() called twice");
            }
        }
    }

    static class InboundHandlerTuplePacketListener {
        private final Packet packet;
        private final GenericFutureListener<? extends Future<? super Void>>[] futureListeners;

        @SafeVarargs
        public InboundHandlerTuplePacketListener(Packet inPacket, GenericFutureListener<? extends Future<? super Void>>... inFutureListeners) {
            packet = inPacket;
            futureListeners = inFutureListeners;
        }
    }
}
