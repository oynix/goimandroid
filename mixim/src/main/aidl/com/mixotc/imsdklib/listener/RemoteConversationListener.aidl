// GOIMRemotePacketListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.message.GOIMMessage;

interface RemoteConversationListener {
    void onConversationGroupIdUpdate(long oldGroupId, long newGroupId);
    void onConversationNameUpdate(long groupId, String newName);
    void onMessageReceived(in GOIMMessage message, boolean unread);
    void onSysMsgReceived(in GOIMSystemMessage message);
    void onConversationDelete(long groupId);
    void onMessageUpdate(in GOIMMessage message);
}
