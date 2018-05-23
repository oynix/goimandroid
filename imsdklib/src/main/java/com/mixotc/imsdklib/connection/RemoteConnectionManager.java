package com.mixotc.imsdklib.connection;

import com.mixotc.imsdklib.listener.ChannelConnectionListener;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.PacketDecoder;
import com.mixotc.imsdklib.packet.PacketEncoder;
import com.mixotc.imsdklib.utils.Logger;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:51
 * Version  : v1.0.0
 * Describe : 连接管理
 */
public final class RemoteConnectionManager implements ChannelConnectionListener, PacketReceivedListener {

    private static final String TAG = RemoteConnectionManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final RemoteConnectionManager INSTANCE = new RemoteConnectionManager();
    }

    private RemoteConnectionManager() {
    }

    public static RemoteConnectionManager getInstance() {
        return LazyHolder.INSTANCE;
    }


    private Channel mChannel;
    private Bootstrap mBootstrap;
    private EventLoopGroup mWorkerGroup;
    private boolean mConnected = false;

    public void connect() {
        if (mChannel != null && mChannel.isActive()) {
            return;
        }

        if (mBootstrap == null) {
            mWorkerGroup = new NioEventLoopGroup();
            mBootstrap = new Bootstrap();
            mBootstrap.group(mWorkerGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
//                            if (!Constant.IS_TEST_SERVER) {
//                                SSLEngine sslEngine = SslContextFactory.getClientContext().createSSLEngine();
//                                sslEngine.setUseClientMode(true);
//                                pipeline.addFirst("ssl", new SslHandler(sslEngine));
//                            }

                            pipeline.addLast("decoder", new PacketDecoder());
                            pipeline.addLast("encoder", new PacketEncoder());
                            pipeline.addLast("handler", new RemoteConnectionHandler(RemoteConnectionManager.this, RemoteConnectionManager.this));
                        }
                    })
                    .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        }
        InetSocketAddress serverAddress = null;
        try {
            serverAddress = new InetSocketAddress(InetAddress.getByName(ConnectConfig.SERVER_IP), ConnectConfig.SERVER_PORT);
            ChannelFuture future = mBootstrap.connect(serverAddress);
            future.addListener(mConnectFutureListener);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private ChannelFutureListener mConnectFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture pChannelFuture) throws Exception {
            if (pChannelFuture.isSuccess()) {
                mChannel = pChannelFuture.channel();
                Logger.i(TAG, "operationComplete: connected!");
                BasePacket.resetPacketId();
                mConnected = true;
            } else {
                Logger.i(TAG, "operationComplete: connect failed!");
                mConnected = false;
            }
        }
    };

    @Override
    public void onChannelConnected() {

    }

    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onChannelError(String reason) {

    }

    @Override
    public void onReceivedPacket(BasePacket packet) {

    }

}
