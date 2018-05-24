// GOIMRemotePacketListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.ListLongParcelable;

// 1. 创建群：主动建群/被动建群
// 2. 群更新：包括群名称、群成员增加或者减少
// 3. 删除群：主动删除/被动删除
interface RemoteGroupListener {
    // 直接传输group list会造成OOM，所以只通知。
    void onGroupInit();
    void onGroupCreate(in GOIMGroup group);
    // 0:加人 1：减人 2：更新名称
    void onGroupUpdate(in GOIMGroup group, int type);
    void onGroupDelete(in GOIMGroup group);
}
