// GOIMRemotePacketListener.aidl
package com.mixotc.imsdklib.listener;

// Declare any non-default types here with import statements

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMGroup;

interface RemoteContactListener {

    // add at 2018.3.31 16:37 for abstract im sdk

       /**
        * contact, when the local database initialized this method will be called
        *
        * @param data all contact stared in the database
        */
       void onContactInit();

       /**
        * temporary contact, when the local database initialized this method will be called
        *
        * @param data all temporary contact stored in the database
        */
       void onTempContactInit(in List<GOIMContact> data);

       /**
        * when receive new friend from server this method will be called
        *
        * @param data all contact that agree to add current user
        */
       void onContactAdd(in GOIMContact data);

       /**
        * when receive notification that deleted by someone this method will be called
        *
        * @param data all contact that deleted current user
        */
       void onContactDelete(in GOIMContact data);

       /**
        * when receive response of request for update contact this method will be called
        *
        * @param data all contact that update successfully
        */
       void onContactUpdate(in List<GOIMContact> data);

       /**
        * when delete group actively and then delete the related group members
        *
        * @param data all temporary contact thar update successfully
        */
       void onTempContactDelete(long groupId);

       /**
        * when receive response of request for update temporary contact this method will be called
        *
        * @param data all temporary contact thar update successfully
        */
       void onTempContactUpdate(in GOIMContact data);

       /**
        * when receive response of request for update temporary contact this method will be called
        *
        * @param data all temporary contact thar update successfully
        */
       void onGroupMemberUpdate(in GOIMGroup group);
}
