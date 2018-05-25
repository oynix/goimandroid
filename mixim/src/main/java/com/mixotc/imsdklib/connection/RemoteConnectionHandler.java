package com.mixotc.imsdklib.connection;

import com.mixotc.imsdklib.listener.ChannelConnectionListener;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.utils.Logger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:16
 * Version  : v1.0.0
 * Describe : channel通道管理
 */
public class RemoteConnectionHandler extends SimpleChannelInboundHandler<BasePacket> {

    private static final String TAG = RemoteConnectionHandler.class.getSimpleName();

    private PacketReceivedListener mPacketReceivedListener;
    private ChannelConnectionListener mChannelConnectionListener;

    RemoteConnectionHandler(PacketReceivedListener pl, ChannelConnectionListener cl) {
        mPacketReceivedListener = pl;
        mChannelConnectionListener = cl;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasePacket packet) throws Exception {
        Logger.d(TAG, "接收packet：" + "id:" + packet.getPacketId() + ",type:" + packet.getPacketType().name() + "," + packet.toString());
        Logger.i(TAG, "-----------------------------------------------------------------------------------");
        if (mPacketReceivedListener != null) {
            mPacketReceivedListener.onReceivedPacket(packet);
        }
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        Logger.d(TAG, "----------已连接，channel处于活动状态中----------");
        if (mChannelConnectionListener != null) {
            mChannelConnectionListener.onChannelConnected();
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Logger.d(TAG, "----------连接断开！！channel处于非活动状态中----------");
        if (mChannelConnectionListener != null) {
            mChannelConnectionListener.onChannelDisconnected();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        Logger.d(TAG, "----------捕获到异常！！----------");
        cause.printStackTrace();
        if (mChannelConnectionListener != null) {
            mChannelConnectionListener.onChannelError(cause.getLocalizedMessage());
        }
    }
}
