package com.mixotc.imsdklib.packet;


import com.mixotc.imsdklib.utils.PacketUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by junnikokuki on 2017/8/16.
 */

public class PacketEncoder extends MessageToByteEncoder<BasePacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, BasePacket pkt, ByteBuf out) throws Exception {
        if(null == pkt){
            throw new Exception("msg is null");
        }

        int packLength = (pkt.getPacketBody() == null ? 0 : pkt.getPacketBody().length) + 16;
        byte[] packet = new byte[16];

        // package length
        int offset = PacketUtils.encodeIntBigEndian(packet, packLength, 0, 4 * PacketUtils.BSIZE);
        // header length
        offset = PacketUtils.encodeIntBigEndian(packet, 16, offset, 2 * PacketUtils.BSIZE);
        // ver
        offset = PacketUtils.encodeIntBigEndian(packet, pkt.getVersion(), offset, 2 * PacketUtils.BSIZE);
        // type
        offset = PacketUtils.encodeIntBigEndian(packet, BasePacket.convertFromPacketType(pkt.getPacketType()), offset, 4 * PacketUtils.BSIZE);
        // sequenceId
        offset = PacketUtils.encodeIntBigEndian(packet, pkt.getPacketId(), offset, 4 * PacketUtils.BSIZE);

        if (pkt.getPacketBody() != null) {

            byte[] w = PacketUtils.add(packet, pkt.getPacketBody());

            out.writeBytes(w);
        } else {
            out.writeBytes(packet);
        }
    }
}
