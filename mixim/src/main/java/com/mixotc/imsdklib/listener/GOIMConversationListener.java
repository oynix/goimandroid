package com.mixotc.imsdklib.listener;

import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;

/**
 * Author   : xiaoyu
 * Date     : 2018/4/16 下午7:19
 * Version  : v1.0.0
 * Describe :
 */
public interface GOIMConversationListener {

    void onConversationDelete(long groupId);
    void onConversationGroupIdUpdate(long oldId, long newId);
    void onConversationNameUpdate(long groupId, String newName);
    void onUnreadCountUpdate();
    void onReceiveNewMessage(GOIMMessage message);
    void onReceiveSysMessage(GOIMSystemMessage message);
    void onMessageUpdate(GOIMMessage message);

}
