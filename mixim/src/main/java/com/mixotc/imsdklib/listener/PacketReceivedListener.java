package com.mixotc.imsdklib.listener;

import com.mixotc.imsdklib.packet.BasePacket;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:30
 * Version  : v1.0.0
 * Describe : 与服务器的链接channel接收到packet后回调
 */
public interface PacketReceivedListener {
    void onReceivedPacket(BasePacket packet);
}
