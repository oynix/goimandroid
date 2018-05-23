package com.mixotc.imsdklib.connection;

import android.content.Context;
import android.os.RemoteException;

import com.mixotc.imsdklib.error.ErrorType;
import com.mixotc.imsdklib.listener.ChannelConnectionListener;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.PacketDecoder;
import com.mixotc.imsdklib.packet.PacketEncoder;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.NetUtils;

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

    // 获取单例
    public static RemoteConnectionManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private Context mContext;
    private Channel mChannel;
    private Bootstrap mBootstrap;
    private EventLoopGroup mWorkerGroup;
    private boolean mConnected = false;
    private boolean mLogin = false;
    private ReconnectTask mReconnectThread;

    public void init(Context context) {
        mContext = context;
    }

    /**
     * 主动建立与服务器的socket连接
     */
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
        try {
            InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(ConnectConfig.SERVER_IP), ConnectConfig.SERVER_PORT);
            ChannelFuture future = mBootstrap.connect(serverAddress);
            future.addListener(mConnectFutureListener);
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    private ChannelFutureListener mConnectFutureListener = new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture pChannelFuture) {
            if (pChannelFuture.isSuccess()) {
                mChannel = pChannelFuture.channel();
                Logger.d(TAG, "operationComplete: 连接成功!");
                BasePacket.resetPacketId();
                mConnected = true;
            } else {
                Logger.d(TAG, "operationComplete: 连接失败!");
                mConnected = false;
            }
        }
    };

    /**
     * 主动断开和服务器的socket连接
     */
    private void disconnect() {
        Logger.d(TAG, "disconnect invoked!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (mChannel != null) {
            mChannel.disconnect();
            mChannel = null;
        }
//        if (mHeartBeatThread != null) {
//            mHeartBeatThread.mShouldStop = true;
//            mHeartBeatThread.interrupt();
//            mHeartBeatThread = null;
//        }
        if (mBootstrap != null) {
            mBootstrap = null;
        }
        if (mWorkerGroup != null) {
            mWorkerGroup.shutdownGracefully();
            mWorkerGroup = null;
        }

        mConnected = false;
        mLogin = false;
    }

    /**
     * 重新建立与服务器的socket连接
     */
    public void reconnect() {
        Logger.e("RemoteConnectionManager", "启动自动重连");

        disconnect();

        if (mReconnectThread != null) {
            mReconnectThread.mShouldStop = true;
            mReconnectThread.interrupt();
            mReconnectThread = null;
        }
        mReconnectThread = new ReconnectTask();
        mReconnectThread.start();
    }

    /**
     * 向服务器发送packet
     *
     * @param pkt      packet实例
     * @param callBack 发送回调
     * @throws RemoteException 可能抛出的异常
     */
    public void writeAndFlushPacket(BasePacket pkt, RemoteCallBack callBack) throws RemoteException {
        if (!mConnected) {
            if (callBack != null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_NO_CONNECTION_ERROR, null);
            }
            return;
        }
        if (!mLogin) {
            if (callBack != null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_NO_LOGIN_ERROR, null);
            }
            return;
        }
        if (!doChannelCheck()) {
            if (callBack != null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, "");
            }
            return;
        }
        mChannel.writeAndFlush(pkt);
    }

    private boolean doChannelCheck() {
        if (mChannel == null) {
            Logger.e(TAG, "send: channel is null");
            return false;
        }
        if (!mChannel.isWritable()) {
            Logger.e(TAG, "send: channel is not Writable");
            return false;
        }
        if (!mChannel.isActive()) {
            Logger.e(TAG, "send: channel is not active");
            return false;
        }
        return true;
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

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

    class ReconnectTask extends Thread {
        volatile boolean mShouldStop = false;

        @Override
        public void run() {
            while (!mShouldStop && !isInterrupted()) {
                try {
                    if (doChannelCheck()) {
                        return;
                    }
                    if (isInterrupted()) {
                        return;
                    }
                    // 有网才尝试登录
                    if (NetUtils.isNetConnected(mContext)) {
                        Logger.e(TAG, "网络可用，autoLogin");
                        autoLogin();
                    } else {
                        Logger.e(TAG, "网络不可用");
                    }
                    Thread.sleep(10000);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void autoLogin() {
//        if (RemoteServiceInitializer.getInstance().isLoginPermit()) {
//            String phone = PreferenceUtils.getInstance(mContext).getLastLoginPhoneNumber();
//            String email = PreferenceUtils.getInstance(mContext).getLastLoginEmail();
//            String code = PreferenceUtils.getInstance(mContext).getLastLoginCode();
//            Logger.e(TAG, "auto login");
//            RemoteAccountManager.getInstance().login(phone, email, code, RemoteAccountManager.LOGIN_MODE_RECONNECT,
//                    new RemoteCallBack.Stub() {
//
//                        @Override
//                        public void onSuccess(List result) {
//                            Logger.d("RemoteConnectionManager", "自动登录成功，自动重连结束。");
//                            if (mReconnectThread != null) {
//                                mReconnectThread.mShouldStop = true;
//                                mReconnectThread.interrupt();
//                                mReconnectThread = null;
//                            }
//                        }
//
//                        @Override
//                        public void onError(int errorCode, String reason) {
//                            if (errorCode == ERROR_EXCEPTION_CODEINVALID || errorCode == ERROR_EXCEPTION_CODEERROR
//                                    || errorCode == ERROR_EXCEPTION_OTHERLOGIN) {
//                                Logger.d("RemoteConnectionManager", "自动登录失败，终止。");
//                                if (mReconnectThread != null) {
//                                    mReconnectThread.mShouldStop = true;
//                                    mReconnectThread.interrupt();
//                                    mReconnectThread = null;
//                                }
//
//                                RemoteChatManager.getInstance().onLoggedOut(false);
//                                RemoteAccountManager.getInstance().onLoggedOut();
//
//                                Intent intent = new Intent(RemoteChatManager.getInstance().getLogoutBroadcastAction());
//                                mContext.sendOrderedBroadcast(intent, null);
//                            } else {
//                                Logger.d("RemoteConnectionManager", "自动登录失败，继续。");
//                            }
//                        }
//
//                        @Override
//                        public void onProgress(int progress, String message) {
//
//                        }
//                    });
//        }
    }

}
