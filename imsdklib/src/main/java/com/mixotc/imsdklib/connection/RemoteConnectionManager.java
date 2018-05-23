package com.mixotc.imsdklib.connection;

import android.content.Context;
import android.os.RemoteException;

import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.error.ErrorType;
import com.mixotc.imsdklib.exception.GOIMException;
import com.mixotc.imsdklib.listener.ChannelConnectionListener;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteConnectionListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.LogoutPacket;
import com.mixotc.imsdklib.packet.PacketDecoder;
import com.mixotc.imsdklib.packet.PacketEncoder;
import com.mixotc.imsdklib.packet.ReplyPacket;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.NetUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private HeartbeatTask mHeartBeatThread;
    private List<PacketReceivedListener> mPacketReceivedListeners = new ArrayList<>();
    private List<RemoteConnectionListener> mRemoteConnectionListeners = new ArrayList<>();
    private ExecutorService mListenerExecutor = Executors.newSingleThreadExecutor();

    public void init(Context context) {
        mContext = context;
    }

    /**
     * 主动建立与服务器的socket连接
     */
    public boolean connect() {
        if (mChannel != null && mChannel.isActive()) {
            return true;
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
            InetSocketAddress serverAddress = new InetSocketAddress(InetAddress.getByName(RemoteConfig.SERVER_IP), RemoteConfig.SERVER_PORT);
            ChannelFuture future = mBootstrap.connect(serverAddress);
            future.addListener(mConnectFutureListener);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
        return true;
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
    public void disconnect() {
        Logger.d(TAG, "disconnect invoked!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        if (mChannel != null) {
            mChannel.disconnect();
            mChannel = null;
        }
        if (mHeartBeatThread != null) {
            mHeartBeatThread.mShouldStop = true;
            mHeartBeatThread.interrupt();
            mHeartBeatThread = null;
        }
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
     * 向服务器发送packet,要求已登陆状态
     *
     * @param pkt      packet实例
     * @param callBack 发送回调
     */
    public void writeAndFlushPacket(BasePacket pkt, RemoteCallBack callBack) {
        writeAndFlushPacket(pkt, callBack, true);
    }

    /**
     * 向服务器发送packet
     *
     * @param pkt      packet实例
     * @param callBack 发送回调
     */
    public void writeAndFlushPacket(BasePacket pkt, RemoteCallBack callBack, boolean isLoginNeed) {
        if (!mConnected) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_NO_CONNECTION_ERROR, null);
            return;
        }
        if (isLoginNeed && !mLogin) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_NO_LOGIN_ERROR, null);
            return;
        }
        if (!doChannelCheck()) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
            return;
        }
        mChannel.writeAndFlush(pkt);
    }

    public long logout() {
        mLogin = false;
        if (mHeartBeatThread != null) {
            mHeartBeatThread.mShouldStop = true;
            mHeartBeatThread.interrupt();
            mHeartBeatThread = null;
        }

        LogoutPacket pkt = new LogoutPacket();
        mChannel.writeAndFlush(pkt);
        return pkt.getPacketId();
    }

    private void callbackOnError(RemoteCallBack callBack, int code, String reason) {
        if (callBack != null) {
            try {
                callBack.onError(code, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
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

    /**
     * 添加packet接收监听器
     */
    public void addPacketListener(PacketReceivedListener l) {
        if (l != null && !mPacketReceivedListeners.contains(l)) {
            mPacketReceivedListeners.add(l);
        }
    }

    /**
     * 移除packet接收监听器
     */
    public void removePacketListener(PacketReceivedListener l) {
        mPacketReceivedListeners.remove(l);
    }

    /**
     * 添加连接状态监听器
     */
    public void addConnectionListener(RemoteConnectionListener l) {
        if (l != null && !mRemoteConnectionListeners.contains(l)) {
            mRemoteConnectionListeners.add(l);
        }
    }

    /**
     * 移除连接状态监听器
     */
    public void removeConnectionListener(RemoteConnectionListener l) {
        mRemoteConnectionListeners.remove(l);
    }
    ///////////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public void onChannelConnected() {
        for (RemoteConnectionListener listener : mRemoteConnectionListeners) {
            try {
                listener.onConnected();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChannelDisconnected() {
        for (RemoteConnectionListener listener : mRemoteConnectionListeners) {
            try {
                listener.onDisconnected();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onChannelError(String reason) {
        for (RemoteConnectionListener listener : mRemoteConnectionListeners) {
            try {
                listener.onError(reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 每接收到一个packet，就要遍历所有listener分别执行处理packet
    @Override
    public void onReceivedPacket(BasePacket packet) {
        mListenerExecutor.submit(new PacketListenerNotification(packet));
        if (packet.getPacketType() == BasePacket.PacketType.LOGIN_REPLY) {
            ReplyPacket replyPacket = new ReplyPacket(packet);
            int ret = replyPacket.getResult();
            if (ret == 0) {
                mLogin = true;
                if (mHeartBeatThread != null) {
                    mHeartBeatThread.mShouldStop = true;
                    mHeartBeatThread.interrupt();
                    mHeartBeatThread = null;
                }
                mHeartBeatThread = new HeartbeatTask();
                mHeartBeatThread.start();
                Logger.i(TAG, "start heartbeat");
            }
        } else if (packet.getPacketType() == BasePacket.PacketType.LOGOUT_REPLY) {
            Logger.e(TAG, "logout reply disconnect!!!!!!!!");
            disconnect();
        }
    }

    class HeartbeatTask extends Thread {
        private int defaultHeartBeatTimeOut = 15000;
        volatile boolean mShouldStop = false;

        @Override
        public void run() {
            while (!mShouldStop && !isInterrupted()) {
                try {
                    Logger.e(TAG, "enter while, before sleep, 当前线程：" + Thread.currentThread());
                    Thread.sleep(defaultHeartBeatTimeOut);
                    Logger.e(TAG, "after sleep");
                    if (!isInterrupted()) {
                        Logger.e(TAG, "enter heart beat write");
                        if (!doChannelCheck()) {
                            throw new GOIMException(ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, "");
                        }
                        BasePacket packet = new BasePacket();
                        packet.setPacketType(BasePacket.PacketType.HEARTBEAT);
                        mChannel.writeAndFlush(packet);
                        Logger.e(TAG, "enter heart beat write and write finish");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Logger.e(TAG, "interrupt:" + e.toString());
                } catch (GOIMException e) {
                    e.printStackTrace();
                    Logger.e(TAG, "GOIMException:" + e.toString());
                    if (mShouldStop) {
                        Logger.e(TAG, " should stop , disconnect!!!!!!!!");
                        disconnect();
                    } else {
                        Logger.e(TAG, "should not stop, reconnect");
                        reconnect();
                    }
                    break;
                }
            }
        }
    }

    private class PacketListenerNotification implements Runnable {

        private BasePacket packet;

        PacketListenerNotification(BasePacket packet) {
            this.packet = packet;
        }

        @Override
        public void run() {
            for (PacketReceivedListener listener : mPacketReceivedListeners) {
                listener.onReceivedPacket(packet);
            }
        }
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
