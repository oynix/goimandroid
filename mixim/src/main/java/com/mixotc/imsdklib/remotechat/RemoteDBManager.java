package com.mixotc.imsdklib.remotechat;

import android.content.Context;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.database.DatabaseUtils;
import com.mixotc.imsdklib.database.DatabaseHelper;
import com.mixotc.imsdklib.database.provider.ChatTableProvider;
import com.mixotc.imsdklib.database.provider.ContactTableProvider;
import com.mixotc.imsdklib.database.provider.ConversationTableProvider;
import com.mixotc.imsdklib.database.provider.FriendReqTableProvider;
import com.mixotc.imsdklib.database.provider.GroupTableProvider;
import com.mixotc.imsdklib.database.provider.SysMsgTableProvider;
import com.mixotc.imsdklib.database.provider.TableProviderFactory;
import com.mixotc.imsdklib.database.provider.TempContactTableProvider;
import com.mixotc.imsdklib.database.provider.UnreadCountTableProvider;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class RemoteDBManager {
    private static final String TAG = RemoteDBManager.class.getSimpleName();

    private static RemoteDBManager sDBManager = null;
    private String mParamString = null;
    private TableProviderFactory mProviderFactory;

    private RemoteDBManager(Context context, String uid) {
        mProviderFactory = new TableProviderFactory(context, uid);
    }

    // after user log in, this method will be invoked to initialize database.
    public static boolean initDB(Context context, long userId) {
        Logger.d(TAG, "initDB : " + userId);
        if (sDBManager == null || !String.valueOf(userId).equals(sDBManager.mParamString)) {
            synchronized (RemoteDBManager.class) {
                if (sDBManager == null) {
                    sDBManager = new RemoteDBManager(context, String.valueOf(userId));
                }
            }
        }
        sDBManager.mParamString = String.valueOf(userId);
        boolean dbExist = DatabaseUtils.isDatabaseExist(context, DatabaseHelper.getDBNameWithId(userId));
        Logger.d(TAG, "initialize database complete , if database exist :" + dbExist);
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
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.insertMessage(message);
    }

    public boolean updateMessageBody(GOIMMessage message) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.updateMsgBody(message);
    }

    public void deleteMsg(String msgId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.deleteMessage(msgId);
    }

    public GOIMMessage loadMsg(String msgId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.loadMessage(msgId);
    }

    public void updateMsgListen(String msgId, boolean listened) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.updateMsgListen(msgId, listened);
    }

    public void updateMsgDelivered(String msgId, boolean delivered) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.updateMsgDelivered(msgId, delivered);
    }

    public void updateMsgStatus(String msgId, int newStatus) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.updateMsgStatus(msgId, newStatus);
    }

    public void deleteConversionMsgs(long groupId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.deleteConversationMsg(groupId);
    }

    public void updateMessagesGroupId(long oldGroupId, long newGroupId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.updateMsgGroupId(oldGroupId, newGroupId);
    }

    public long getMsgCount(long groupId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.getMsgCountById(groupId);
    }

    public List<GOIMMessage> findMsgs(long groupId, String startMsgId, int count) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.loadMsgByLastId(groupId, startMsgId, count);
    }

    public void deleteGroupMsgs(long groupId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        provider.deleteMsgByGroupId(groupId);
    }

    public GOIMMessage findTransferMsg(long groupId, long transId) {
        ChatTableProvider provider = mProviderFactory.createProvider(ChatTableProvider.class);
        return provider.loadTransferMsg(groupId, transId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public Hashtable<Long, RemoteConversation> getConversationsWithoutMessage(int count) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        return provider.loadAllConversation();
    }

    public GOIMConversation getConversationById(long groupId) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        return provider.loadConversationById(groupId);
    }

    public void deleteConversation(long groupId) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        provider.deleteConversationById(groupId);
    }

    public void saveConversation(GOIMConversation conversation) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        provider.insertConversation(conversation);
    }

    public void updateConversationName(long groupId, String newName) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        provider.updateConversationName(groupId, newName);
    }

    public void setConversationOnTop(long groupId, boolean isOnTop) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        provider.updateConversationTop(groupId, isOnTop);
    }

    public void updateConversationId(long oldGroupId, long newGroupId) {
        ConversationTableProvider provider = mProviderFactory.createProvider(ConversationTableProvider.class);
        provider.updateConversationId(oldGroupId, newGroupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public void clearUnread(long groupId) {
        UnreadCountTableProvider provider = mProviderFactory.createProvider(UnreadCountTableProvider.class);
        provider.deleteById(groupId);
    }

    public void updateUnreadGroupId(long oldId, long newId) {
        UnreadCountTableProvider provider = mProviderFactory.createProvider(UnreadCountTableProvider.class);
        provider.updateUnreadGroupId(oldId, newId);
    }

    public void saveUnreadCount(long groupId, int count) {
        UnreadCountTableProvider provider = mProviderFactory.createProvider(UnreadCountTableProvider.class);
        provider.replaceUnreadCount(groupId, count);
    }

    public void increaseUnreadCount(long groupId) {
        UnreadCountTableProvider provider = mProviderFactory.createProvider(UnreadCountTableProvider.class);
        provider.increaseUnreadCount(groupId);
    }

    public int getUnreadCount(long groupId) {
        UnreadCountTableProvider provider = mProviderFactory.createProvider(UnreadCountTableProvider.class);
        return provider.getCountById(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized boolean containContact(long uid) {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        return provider.containContact(uid);
    }

    public synchronized GOIMContact getContactById(long uid) {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        return provider.getContactById(uid);
    }

    public synchronized void saveContacts(Collection<GOIMContact> contacts) {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        provider.restoreContacts(contacts);
        TempContactTableProvider tempContactProvider = mProviderFactory.createProvider(TempContactTableProvider.class);
        tempContactProvider.replaceTempContacts(contacts);
    }

    public synchronized void addContact(GOIMContact contact) {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        provider.replaceContact(contact);
        replaceTempContact(contact);
    }

    public synchronized int updateContactBaseInfo(GOIMContact contact) {
        TempContactTableProvider tempProvider = mProviderFactory.createProvider(TempContactTableProvider.class);
        tempProvider.updateContactBaseInfo(contact);
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        return provider.updateContactBaseInfo(contact);
    }

    public synchronized void deleteContact(long uid) {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        provider.deleteContact(uid);
    }

    public synchronized List<GOIMContact> loadContacts() {
        ContactTableProvider provider = mProviderFactory.createProvider(ContactTableProvider.class);
        return provider.loadAllContacts();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized void replaceTempContact(GOIMContact contact) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        provider.replaceTempContact(contact);
    }

    public synchronized GOIMContact getTempContactById(long userId, long groupId) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        return provider.getTempContactById(userId, groupId);
    }

    public synchronized GOIMContact getTempContactById(long uid) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        return provider.getTempContactById(uid, -uid);
    }

    public synchronized GOIMContact getExTempContact(long uid) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        return provider.getExContactById(uid);
    }

    public synchronized Hashtable<Long, GOIMContact> loadTempContacts(long groupId) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        return provider.loadTempContactsByGroupId(groupId);
    }

    public synchronized void deleteGroupMember(long groupId, List<Long> users) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        provider.deleteGroupMember(groupId, users);
    }

    public synchronized void deleteGroupContacts(long groupId) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        provider.deleteContactByGroupId(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    public synchronized boolean containGroup(long gid) {
        GroupTableProvider provider = mProviderFactory.createProvider(GroupTableProvider.class);
        return provider.containGroup(gid);
    }

    public synchronized GOIMGroup getGroupById(long groupId) {
        long start = System.currentTimeMillis();
        GroupTableProvider provider = mProviderFactory.createProvider(GroupTableProvider.class);
        GOIMGroup group = provider.getGroupById(groupId);
        if (group != null) {
            TempContactTableProvider tempContactTableProvider = mProviderFactory.createProvider(TempContactTableProvider.class);
            Hashtable<Long, GOIMContact> members = tempContactTableProvider.loadTempContactsByGroupId(groupId);
            group.setMembers(members);
        }
        long end = System.currentTimeMillis();
        Logger.e(TAG, "get group by id 耗时：" + (end - start) + ", group == null ?" + (group == null));
        return group;
    }

    public synchronized void updateGroupName(long groupId, String newName) {
        GroupTableProvider provider = mProviderFactory.createProvider(GroupTableProvider.class);
        provider.updateGroupName(groupId, newName);
    }

    public synchronized void saveGroup(GOIMGroup group) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        provider.replaceTempContacts(group.getMembers());
        GroupTableProvider tableProvider = mProviderFactory.createProvider(GroupTableProvider.class);
        tableProvider.replaceGroup(group);
    }

    public synchronized void restoreGroups(Collection<GOIMGroup> groups) {
        TempContactTableProvider provider = mProviderFactory.createProvider(TempContactTableProvider.class);
        provider.clear();
        for (GOIMGroup group : groups) {
            saveGroup(group);
        }
    }

    public List<GOIMGroup> loadAllGroupsWithoutMember() {
        GroupTableProvider provider = mProviderFactory.createProvider(GroupTableProvider.class);
        return provider.loadAllGroups();
    }

    public void deleteGroup(long groupId) {
        GroupTableProvider provider = mProviderFactory.createProvider(GroupTableProvider.class);
        provider.deleteGroup(groupId);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * @return if already exist, just update and return true; if not exist, insert and return false.
     */
    public synchronized boolean addFriendRequest(GOIMFriendRequest request) {
        FriendReqTableProvider provider = mProviderFactory.createProvider(FriendReqTableProvider.class);
        return provider.addOrReplaceRequest(request);
    }

    public synchronized void deleteFriendRequest(GOIMFriendRequest request) {
        FriendReqTableProvider provider = mProviderFactory.createProvider(FriendReqTableProvider.class);
        provider.deleteRequest(request.getUid());
    }

    public synchronized void updateFriendRequestResponse(GOIMFriendRequest request) {
        FriendReqTableProvider provider = mProviderFactory.createProvider(FriendReqTableProvider.class);
        provider.updateRequestResponse(request);
    }

    public void clearAllFriendRequests() {
        FriendReqTableProvider provider = mProviderFactory.createProvider(FriendReqTableProvider.class);
        provider.clearRequest();
    }

    public List<GOIMFriendRequest> getFriendRequests() {
        FriendReqTableProvider provider = mProviderFactory.createProvider(FriendReqTableProvider.class);
        return provider.loadAllRequest();
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////
    public synchronized void addSystemMsg(GOIMSystemMessage systemMessage) {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        provider.replaceSysMsg(systemMessage);
    }

    public List<GOIMSystemMessage> getSystemMsgs() {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        return provider.loadAllSysMsg();
    }

    public GOIMSystemMessage getLastSystemMsg() {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        return provider.loadLastSysMsg();
    }

    public int getUnreadSystemMsgs() {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        return provider.getUnreadSysMsg();
    }

    // 将所有系统消息置为已读状态
    public void updateSysMsgToRead() {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        provider.updateSysMsgToRead();
    }

    public void clearAllSystemMsgs() {
        SysMsgTableProvider provider = mProviderFactory.createProvider(SysMsgTableProvider.class);
        provider.clearSysMsg();
    }
}