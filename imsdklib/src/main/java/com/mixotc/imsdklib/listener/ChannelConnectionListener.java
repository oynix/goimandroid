package com.mixotc.imsdklib.listener;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:32
 * Version  : v1.0.0
 * Describe : channel链接状态改变后回调此类型接口的方法
 */
public interface ChannelConnectionListener {
    /**
     * 建立链接
     */
    void onChannelConnected();

    /**
     * 链接断掉
     */
    void onChannelDisconnected();

    /**
     * 发生错误
     *
     * @param reason 原因
     */
    void onChannelError(String reason);
}
