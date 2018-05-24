package com.mixotc.imsdklib.packet;

/**
 * Created by junnikokuki on 2017/8/29.
 */

public class LogoutPacket extends BasePacket {
    public LogoutPacket() {
        super();
        mType = PacketType.LOGOUT;
    }
}
