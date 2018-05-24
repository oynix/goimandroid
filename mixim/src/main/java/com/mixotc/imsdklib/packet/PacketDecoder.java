package com.mixotc.imsdklib.packet;

import com.mixotc.imsdklib.utils.PacketUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * Created by junnikokuki on 2017/8/16.
 */

public class PacketDecoder extends LengthFieldBasedFrameDecoder {
    private static final int HEADER_SIZE = 16;
    private static final int MAX_SIZE = 1024 * 1024;

    public PacketDecoder() {
        super(MAX_SIZE, 0, 4, 0, 0, false);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        if (in == null) {
            return null;
        }

        int msgSize = in.readableBytes();
        if (msgSize < HEADER_SIZE) {
            return null;
        }

        byte[] headerBuffer = new byte[HEADER_SIZE];
        in.markReaderIndex();
        in.readBytes(headerBuffer);

        long packageLength = PacketUtils.decodeIntBigEndian(headerBuffer, 0, 4);

        int bodySize = in.readableBytes();
        if (bodySize < packageLength - HEADER_SIZE) {
            in.resetReaderIndex();
            return null;
        }

        long headLength = PacketUtils.decodeIntBigEndian(headerBuffer, 4, 2);
        long version = PacketUtils.decodeIntBigEndian(headerBuffer, 6, 2);
        long type = PacketUtils.decodeIntBigEndian(headerBuffer, 8, 4);
        long sequenceId = PacketUtils.decodeIntBigEndian(headerBuffer, 12, 4);
        byte[] body = null;
        if (packageLength - headLength > 0) {
            body = new byte[(int)(packageLength - headLength)];
            in.readBytes(body);
        }

        return new BasePacket(version, BasePacket.convertToPacketType(type), sequenceId, body);
    }
}
