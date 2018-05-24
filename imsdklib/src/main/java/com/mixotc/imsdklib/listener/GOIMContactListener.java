package com.mixotc.imsdklib.listener;

import com.mixotc.imsdklib.chat.GOIMContact;

public interface GOIMContactListener {
    void onContactAdded(GOIMContact contact);

    void onContactDeleted(GOIMContact contact);

    void onContactsUpdated();

    void onTempContactUpdate(GOIMContact contact);
}
