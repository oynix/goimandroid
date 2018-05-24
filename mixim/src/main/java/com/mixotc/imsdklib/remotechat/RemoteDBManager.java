package com.mixotc.imsdklib.remotechat;

import android.content.Context;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.database.DatabaseUtils;
import com.mixotc.imsdklib.database.IMDatabaseHelper;
import com.mixotc.imsdklib.database.provider.IMChatTableProvider;
import com.mixotc.imsdklib.database.provider.IMContactTableProvider;
import com.mixotc.imsdklib.database.provider.IMConversationTableProvider;
import com.mixotc.imsdklib.database.provider.IMFriendReqTableProvider;
import com.mixotc.imsdklib.database.provider.IMGroupTableProvider;
import com.mixotc.imsdklib.database.provider.IMSysMsgTableProvider;
import com.mixotc.imsdklib.database.provider.IMTableProviderFactory;
import com.mixotc.imsdklib.database.provider.IMTempContactTableProvider;
import com.mixotc.imsdklib.database.provider.IMUnreadCountTableProvider;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class RemoteDBManager {
    private static String TAG = RemoteDBManager.class.getSimpleName();

    private static RemoteDBManager sDBManager = null;
    private String mParamString = null;
    private IMTableProviderFactory mProviderFactory;

    private RemoteDBManager(Context context, String uid) {
        mProviderFactory = new IMTableProviderFactory(context, uid);
    }

    // after user log in, this method will be invoked to initialize database.
    public static boolean initDB(Context context, long userId) {
        Logger.e(TAG, "initDB : " + userId);
        if (sDBManager == null) {
            synchronized (RemoteDBManager.class) {
                if (sDBManager == null) {
                    sDBManager = new RemoteDBManager(context, String.valueOf(userId));
                }
            }
        } else {
            if (!String.valueOf(userId).equals(sDBManager.mParamString)) {
                sDBManager = new RemoteDBManager(context, String.valueOf(userId));
            }
        }
        sDBManager.mParamString = String.valueOf(userId);
        boolean dbExist = DatabaseUtils.isDatabaseExist(context, IMDatabaseHelper.getDBNameWithId(userId));
        Logger.e(TAG, "initialize database , is database exist :" + dbExist);
        return dbExist;
    }

    public static synchronized RemoteDBManager getInstance() {
        if (sDBManager == null) {
            Logger.e(TAG, "Please login first!");
            throw new IllegalStateException("Please login first!");
        }
        return sDBManager;
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public boolean saveMsg(GOIMMessage message) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.insertMessage(message);
    }

    public boolean updateMessageBody(GOIMMessage message) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.updateMsgBody(message);
    }

    public void deleteMsg(String msgId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.deleteMessage(msgId);
    }

    public GOIMMessage loadMsg(String msgId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.loadMessage(msgId);
    }

    public void updateMsgListen(String msgId, boolean listened) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.updateMsgListen(msgId, listened);
    }

    public void updateMsgDelivered(String msgId, boolean delivered) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.updateMsgDelivered(msgId, delivered);
    }

    public void updateMsgStatus(String msgId, int newStatus) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.updateMsgStatus(msgId, newStatus);
    }

    public void deleteConversionMsgs(long groupId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.deleteConversationMsg(groupId);
    }

    public void updateMessagesGroupId(long oldGroupId, long newGroupId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.updateMsgGroupId(oldGroupId, newGroupId);
    }

    public long getMsgCount(long groupId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.getMsgCountById(groupId);
    }

    public List<GOIMMessage> findMsgs(long groupId, String startMsgId, int count) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.loadMsgByLastId(groupId, startMsgId, count);
    }

    public void deleteGroupMsgs(long groupId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        provider.deleteMsgByGroupId(groupId);
    }

    public GOIMMessage findTransferMsg(long groupId, long transId) {
        IMChatTableProvider provider = mProviderFactory.createProvider(IMChatTableProvider.class);
        return provider.loadTransferMsg(groupId, transId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public Hashtable<Long, RemoteConversation> loadAllConversationsWithoutMessage(int count) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        return provider.loadAllConversation();
    }

    public GOIMConversation getConversationById(long groupId) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        return provider.loadConversationById(groupId);
    }

    public void deleteConversation(long groupId) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        provider.deleteConversationById(groupId);
    }

    public void saveConversation(GOIMConversation conversation) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        provider.insertConversation(conversation);
    }

    public void updateConversationName(long groupId, String newName) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        provider.updateConversationName(groupId, newName);
    }

    public void setConversationOnTop(long groupId, boolean isOnTop) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        provider.updateConversationTop(groupId, isOnTop);
    }

    public void updateConversationId(long oldGroupId, long newGroupId) {
        IMConversationTableProvider provider = mProviderFactory.createProvider(IMConversationTableProvider.class);
        provider.updateConversationId(oldGroupId, newGroupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearUnread(long groupId) {
        IMUnreadCountTableProvider provider = mProviderFactory.createProvider(IMUnreadCountTableProvider.class);
        provider.deleteById(groupId);
    }

    public void updateUnreadGroupId(long oldId, long newId) {
        IMUnreadCountTableProvider provider = mProviderFactory.createProvider(IMUnreadCountTableProvider.class);
        provider.updateUnreadGroupId(oldId, newId);
    }

    public void saveUnreadCount(long groupId, int count) {
        IMUnreadCountTableProvider provider = mProviderFactory.createProvider(IMUnreadCountTableProvider.class);
        provider.replaceUnreadCount(groupId, count);
    }

    public void increaseUnreadCount(long groupId) {
        IMUnreadCountTableProvider provider = mProviderFactory.createProvider(IMUnreadCountTableProvider.class);
        provider.increaseUnreadCount(groupId);
    }

    public int getUnreadCount(long groupId) {
        IMUnreadCountTableProvider provider = mProviderFactory.createProvider(IMUnreadCountTableProvider.class);
        return provider.getCountById(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized boolean containContact(long uid) {
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        return provider.containContact(uid);
    }

    public synchronized GOIMContact getContactById(long uid) {
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        return provider.getContactById(uid);
    }

    public synchronized void saveContacts(Collection<GOIMContact> contacts) {
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        provider.restoreContacts(contacts);
        IMTempContactTableProvider tempContactProvider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        tempContactProvider.replaceTempContacts(contacts);
    }

    public synchronized void addContact(GOIMContact contact) {
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        provider.replaceContact(contact);
        replaceTempContact(contact);
    }

    public synchronized int updateContactBaseInfo(GOIMContact contact) {
        IMTempContactTableProvider tempProvider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        tempProvider.updateContactBaseInfo(contact);
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        return provider.updateContactBaseInfo(contact);
    }

    public synchronized void deleteContact(long uid) {
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        provider.deleteContact(uid);
    }

    public synchronized List<GOIMContact> loadContacts() {
        Logger.e(TAG, "load all contacts");
        IMContactTableProvider provider = mProviderFactory.createProvider(IMContactTableProvider.class);
        return provider.loadAllContacts();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void replaceTempContact(GOIMContact contact) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        provider.replaceTempContact(contact);
    }

    public synchronized GOIMContact getTempContactById(long userId, long groupId) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        return provider.getTempContactById(userId, groupId);
    }

    public synchronized GOIMContact getTempContactById(long uid) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        return provider.getTempContactById(uid, -uid);
    }

    public synchronized GOIMContact getExTempContact(long uid) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        return provider.getExContactById(uid);
    }

    public synchronized Hashtable<Long, GOIMContact> loadTempContacts(long groupId) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        return provider.loadTempContactsByGroupId(groupId);
    }

    public synchronized void deleteGroupMember(long groupId, List<Long> users) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        provider.deleteGroupMember(groupId, users);
    }

    public synchronized void deleteGroupContacts(long groupId) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        provider.deleteContactByGroupId(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized boolean containGroup(long gid) {
        IMGroupTableProvider provider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        return provider.containGroup(gid);
    }

    public synchronized GOIMGroup getGroupById(long groupId) {
        long start = System.currentTimeMillis();
        IMGroupTableProvider provider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        GOIMGroup group = provider.getGroupById(groupId);
        if (group != null) {
            IMTempContactTableProvider tempContactTableProvider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
            Hashtable<Long, GOIMContact> members = tempContactTableProvider.loadTempContactsByGroupId(groupId);
            group.setMembers(members);
        }
        long end = System.currentTimeMillis();
        Logger.e(TAG, "get group by id 耗时：" + (end - start) + ", group == null ?" + (group == null));
        return group;
    }

    public synchronized void updateGroupName(long groupId, String newName) {
        IMGroupTableProvider provider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        provider.updateGroupName(groupId, newName);
    }

    public synchronized void saveGroup(GOIMGroup group) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        provider.replaceTempContacts(group.getMembers());
        IMGroupTableProvider tableProvider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        tableProvider.replaceGroup(group);
    }

    public synchronized void restoreGroups(Collection<GOIMGroup> groups) {
        IMTempContactTableProvider provider = mProviderFactory.createProvider(IMTempContactTableProvider.class);
        provider.clear();
        for (GOIMGroup group : groups) {
            saveGroup(group);
        }
    }

    public List<GOIMGroup> loadAllGroupsWithoutMember() {
        IMGroupTableProvider provider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        return provider.loadAllGroups();
    }

    public void deleteGroup(long groupId) {
        IMGroupTableProvider provider = mProviderFactory.createProvider(IMGroupTableProvider.class);
        provider.deleteGroup(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return if already exist, just update and return true; if not exist, insert and return false.
     */
    public synchronized boolean addFriendRequest(GOIMFriendRequest request) {
        IMFriendReqTableProvider provider = mProviderFactory.createProvider(IMFriendReqTableProvider.class);
        return provider.addOrReplaceRequest(request);
    }

    public synchronized void deleteFriendRequest(GOIMFriendRequest request) {
        IMFriendReqTableProvider provider = mProviderFactory.createProvider(IMFriendReqTableProvider.class);
        provider.deleteRequest(request.getUid());
    }

    public synchronized void updateFriendRequestResponse(GOIMFriendRequest request) {
        IMFriendReqTableProvider provider = mProviderFactory.createProvider(IMFriendReqTableProvider.class);
        provider.updateRequestResponse(request);
    }

    public void clearAllFriendRequests() {
        IMFriendReqTableProvider provider = mProviderFactory.createProvider(IMFriendReqTableProvider.class);
        provider.clearRequest();
    }

    public List<GOIMFriendRequest> getFriendRequests() {
        IMFriendReqTableProvider provider = mProviderFactory.createProvider(IMFriendReqTableProvider.class);
        return provider.loadAllRequest();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized void addSystemMsg(GOIMSystemMessage systemMessage) {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        provider.replaceSysMsg(systemMessage);
    }

    public List<GOIMSystemMessage> getSystemMsgs() {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        return provider.loadAllSysMsg();
    }

    public GOIMSystemMessage getLastSystemMsg() {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        return provider.loadLastSysMsg();
    }

    public int getUnreadSystemMsgs() {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        return provider.getUnreadSysMsg();
    }

    // 将所有系统消息置为已读状态
    public void updateSysMsgToRead() {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        provider.updateSysMsgToRead();
    }

    public void clearAllSystemMsgs() {
        IMSysMsgTableProvider provider = mProviderFactory.createProvider(IMSysMsgTableProvider.class);
        provider.clearSysMsg();
    }
}