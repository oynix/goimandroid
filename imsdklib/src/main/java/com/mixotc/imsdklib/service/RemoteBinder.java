package com.mixotc.imsdklib.service;

import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.account.RemoteAccountManager;
import com.mixotc.imsdklib.chat.GOIMChatOptions;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.ListLongParcelable;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.remotechat.RemoteChatManager;
import com.mixotc.imsdklib.remotechat.RemoteContactManager;
import com.mixotc.imsdklib.remotechat.RemoteConversation;
import com.mixotc.imsdklib.remotechat.RemoteDBManager;
import com.mixotc.imsdklib.remotechat.RemoteGroupManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午5:32
 * Version  : v1.0.0
 * Describe : 这个类就像是一个中间人，一个中介，接收主进程的调用后，直接转发至服务进程的方法实现。
 */
public class RemoteBinder extends RemoteServiceBinder.Stub {

    // Account
    @Override
    public void sendCode(String phone, String email, RemoteCallBack callBack) {
        RemoteAccountManager.getInstance().sendLoginCode(phone, email, callBack);
    }

    @Override
    public void login(String phone, String email, String code, RemoteCallBack callBack) {
        RemoteAccountManager.getInstance().login(phone, email, code, 0, callBack);
    }

    // chat
    @Override
    public GOIMChatOptions getChatOptions() {
        return RemoteChatManager.getInstance().getChatOptions();
    }

    @Override
    public void setChatOptions(GOIMChatOptions option) {
        RemoteChatManager.getInstance().setChatOptions(option);
    }

    @Override
    public void resetNotification() {
        RemoteChatManager.getInstance().resetNotification();
    }

    // Contact
    @Override
    public List<GOIMContact> getContactsFromDB() {
        return RemoteDBManager.getInstance().loadContacts();
    }

    @Override
    public void updateOrInsertIfNotExist(GOIMContact contact) {
        RemoteContactManager.getInstance().updateOrInsertIfNotExist(contact);
    }

    @Override
    public List<GOIMContact> loadTempContacts(long groupId) {
        if (groupId < 0) {
            List<GOIMContact> result = new ArrayList<>();
            GOIMContact tempContact = RemoteDBManager.getInstance().getExTempContact(-groupId);
            if (tempContact != null) {
                result.add(tempContact);
            }
            return result;
        }
        return new ArrayList<>(RemoteDBManager.getInstance().loadTempContacts(groupId).values());
    }

    @Override
    public void updateFriendRequest(GOIMFriendRequest request) {
        RemoteDBManager.getInstance().updateFriendRequestResponse(request);
    }

    @Override
    public void deleteFriendRequest(GOIMFriendRequest request) {
        RemoteDBManager.getInstance().deleteFriendRequest(request);
    }

    @Override
    public void clearFriendRequests() {
        RemoteDBManager.getInstance().clearAllFriendRequests();
    }

    @Override
    public List<GOIMFriendRequest> getFriendRequests() {
        return RemoteDBManager.getInstance().getFriendRequests();
    }

    @Override
    public GOIMContact getClientService() {
        return RemoteContactManager.getInstance().getClientService();
    }

    @Override
    public boolean isClientService(long uid) {
        return RemoteContactManager.getInstance().isClientService(uid);
    }

    @Override
    public void sendFriendRequest(long userId, String username, String avatar, String info, RemoteCallBack callback) {
        RemoteContactManager.getInstance().sendFriendRequest(userId, username, avatar, info, callback);
    }

    @Override
    public void responseFriendRequest(long userId, boolean accept, RemoteCallBack callback) {
        RemoteContactManager.getInstance().responseFriendRequest(userId, accept, callback);
    }

    @Override
    public void delFriend(GOIMContact contact, RemoteCallBack callback) {
        RemoteContactManager.getInstance().deleteFriend(contact, callback);
    }

    @Override
    public void searchUser(String keyword, RemoteCallBack callback) {
        RemoteContactManager.getInstance().searchUser(keyword, callback);
    }

    @Override
    public void updateUserBaseInfo(long userId, RemoteCallBack callback) {
        RemoteContactManager.getInstance().updateUserBaseInfo(userId, callback);
    }

    // Group
    @Override
    public List<GOIMGroup> getGroupListWithoutMember() {
        return RemoteDBManager.getInstance().loadAllGroupsWithoutMember();
    }

    @Override
    public void createGroupActive(String groupname, String intro, ListLongParcelable members, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().createGroupActive(groupname, intro, members, callback);
    }

    @Override
    public void deleteGroupActive(long groupId, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().deleteGroupActive(groupId, callback);
    }

    @Override
    public void quitGroupActive(long groupId, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().quitGroupActive(groupId, callback);
    }

    @Override
    public void updateGroupName(long groupId, String groupName, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().updateGroupName(groupId, groupName, callback);
    }

    @Override
    public void addGroupMember(long groupId, ListLongParcelable members, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().addGroupMember(groupId, members, callback);
    }

    @Override
    public void removeGroupMember(long groupId, ListLongParcelable members, RemoteCallBack callback) {
        RemoteGroupManager.getInstance().removeUsersFromGroup(groupId, members, callback);
    }

    // database
    @Override
    public boolean saveMsgToDB(GOIMMessage message) {
        return RemoteDBManager.getInstance().saveMsg(message);
    }

    @Override
    public boolean updateMessageBody(GOIMMessage message) {
        return RemoteDBManager.getInstance().updateMessageBody(message);
    }

    @Override
    public void deleteMsg(String msgId) {
        RemoteDBManager.getInstance().deleteMsg(msgId);
    }

    @Override
    public GOIMMessage loadMsg(String msgId) {
        return RemoteDBManager.getInstance().loadMsg(msgId);
    }

    @Override
    public void deleteConversionMsgs(long groupId) {
        RemoteDBManager.getInstance().deleteConversionMsgs(groupId);
    }

    @Override
    public void updateMsgListen(String msgId, boolean listened) {
        RemoteDBManager.getInstance().updateMsgListen(msgId, listened);
    }

    @Override
    public void updateMsg(String msgId, int newStatus) {
        RemoteDBManager.getInstance().updateMsgStatus(msgId, newStatus);
    }

    @Override
    public String loadAllConversationsWithoutMessage(int count) {
        Hashtable<Long, RemoteConversation> conversations = RemoteDBManager.getInstance().loadAllConversationsWithoutMessage(count);
        JSONArray jConversations = new JSONArray();
        for (RemoteConversation conversation : conversations.values()) {
            long groupId = conversation.getGroupId();
            // 当groupId不存在即为0时，舍弃；当groupId<0即为陌生人，但是-groupId已经是好友，舍弃。
            if (groupId == 0 || (groupId < 0 && RemoteDBManager.getInstance().containContact(-groupId))) {
                continue;
            }
            JSONObject jConversation = new JSONObject();
            try {
                jConversation.put("groupId", groupId);
                jConversation.put("name", conversation.getName());
                jConversation.put("msgCount", conversation.getMsgCount());
                jConversation.put("isOnTop", conversation.isOnTop());
                jConversation.put("isSingle", conversation.isSingle());
                jConversation.put("lastMsgTime", conversation.lastMsgTime());
                jConversation.put("lastMsgText", conversation.lastMsgText());
                jConversations.put(jConversation);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return jConversations.toString();
    }

    @Override
    public void deleteConversation(long groupId) {
        RemoteDBManager.getInstance().deleteConversation(groupId);
    }

    @Override
    public void saveConversation(GOIMConversation conversation) {
        RemoteDBManager.getInstance().saveConversation(conversation);
    }

    @Override
    public void updateConversationName(long groupId, String newName) {
        RemoteDBManager.getInstance().updateConversationName(groupId, newName);
    }

    @Override
    public void setConversationOnTop(long groupId, boolean isOnTop) {
        RemoteDBManager.getInstance().setConversationOnTop(groupId, isOnTop);
    }

    @Override
    public void updateMessagesGroupId(long oldGroupId, long newGroupId) {
        RemoteDBManager.getInstance().updateMessagesGroupId(oldGroupId, newGroupId);
    }

    @Override
    public void clearUnread(long groupId) {
        RemoteDBManager.getInstance().clearUnread(groupId);
    }

    @Override
    public void saveUnreadCount(long groupId, int count) {
        RemoteDBManager.getInstance().saveUnreadCount(groupId, count);
    }

    @Override
    public int getUnreadCount(long groupId) {
        return RemoteDBManager.getInstance().getUnreadCount(groupId);
    }

    @Override
    public long getMsgCount(long groupId) {
        return RemoteDBManager.getInstance().getMsgCount(groupId);
    }

    @Override
    public List<GOIMMessage> findMsgs(long groupId, String startMsgId, int count) {
        return RemoteDBManager.getInstance().findMsgs(groupId, startMsgId, count);
    }

    @Override
    public GOIMMessage findTransferMsg(long groupId, long transId) {
        return RemoteDBManager.getInstance().findTransferMsg(groupId, transId);
    }

    @Override
    public void addSystemMsg(GOIMSystemMessage systemMessage) {
        RemoteDBManager.getInstance().addSystemMsg(systemMessage);
    }

    @Override
    public List<GOIMSystemMessage> getSystemMsgs() {
        return RemoteDBManager.getInstance().getSystemMsgs();
    }

    @Override
    public GOIMSystemMessage getLastSystemMsg() {
        return RemoteDBManager.getInstance().getLastSystemMsg();
    }

    @Override
    public int getUnreadSystemMsgs() {
        return RemoteDBManager.getInstance().getUnreadSystemMsgs();
    }

    @Override
    public void clearUnreadSystemMsgs() {
        RemoteDBManager.getInstance().updateSysMsgToRead();
    }

    @Override
    public void clearAllSystemMsgs() {
        RemoteDBManager.getInstance().clearAllSystemMsgs();
    }

}
