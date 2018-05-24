package com.mixotc.imsdklib.listener;

import com.mixotc.imsdklib.chat.GOIMGroup;

public interface GOIMGroupListener {
    void onGroupCreated(GOIMGroup group);
    // 0加人 1减人 2改名
    void onGroupUpdated(GOIMGroup group, int type);
    void onGroupDelete(GOIMGroup group);
}
