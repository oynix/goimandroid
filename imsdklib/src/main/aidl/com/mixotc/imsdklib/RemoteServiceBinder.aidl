// RemoteServiceBinder.aidl
package com.mixotc.imsdklib;

// Declare any non-default types here with import statements
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.ListLongParcelable;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMChatOptions;
import com.mixotc.imsdklib.listener.RemoteLoggedStatusListener;
import com.mixotc.imsdklib.listener.RemoteContactListener;
import com.mixotc.imsdklib.listener.RemoteGroupListener;
import com.mixotc.imsdklib.listener.RemoteConversationListener;

interface RemoteServiceBinder {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */
     // bind
     void addLogStatusListener(RemoteLoggedStatusListener listener);
     void addContactListener(RemoteContactListener listener);
     void addGroupListener(RemoteGroupListener listener);
     void addConversationListener(RemoteConversationListener listener);
     void removeAllRemoteListeners();

    // account
    void sendCode(String phone, String email, RemoteCallBack callback);
    void login(String phone, String email, String code, RemoteCallBack callback);
    boolean isLogin();
    void logout(RemoteCallBack callback);
    GOIMContact getLoginUser();

    // chat
    GOIMChatOptions getChatOptions();
    void setChatOptions(in GOIMChatOptions option);
    void resetNotification();

    // contact
    List<GOIMContact> getContactsFromDB();
    void updateOrInsertIfNotExist(in GOIMContact contact);
    List<GOIMContact> loadTempContacts(long groupId);
    void updateFriendRequest(in GOIMFriendRequest request);
    void deleteFriendRequest(in GOIMFriendRequest request);
    void clearFriendRequests();
    List<GOIMFriendRequest> getFriendRequests();
    GOIMContact getClientService();
    boolean isClientService(long uid);
    void sendFriendRequest(long userId, String username, String avatar, String info, RemoteCallBack callback);
    void responseFriendRequest(long userId, boolean accept, RemoteCallBack callback);
    void delFriend(in GOIMContact contact, RemoteCallBack callback);
    void searchUser(String keyword, RemoteCallBack callback);
    void updateUserBaseInfo(long userId, RemoteCallBack callback);

    // group
    List<GOIMGroup> getGroupListWithoutMember();
    void createGroupActive(String groupname, String intro, in ListLongParcelable members, RemoteCallBack callback);
    void deleteGroupActive(long groupId, RemoteCallBack callback);
    void quitGroupActive(long groupId, RemoteCallBack callback);
    void updateGroupName(long groupId, String groupName, RemoteCallBack callback);
    void addGroupMember(long groupId, in ListLongParcelable members, RemoteCallBack callback);
    void removeGroupMember(long groupId, in ListLongParcelable members, RemoteCallBack callback);

    // conversation

    // database
    boolean saveMsgToDB(in GOIMMessage message);
    boolean updateMessageBody(in GOIMMessage message);
    void deleteMsg(String msgId);
    GOIMMessage loadMsg(String msgId);
    void deleteConversionMsgs(long groupId);
    void updateMsgListen(String msgId, boolean listened);
    void updateMsg(String msgId, int newStatus);
    String loadAllConversationsWithoutMessage(int count);
    void deleteConversation(long groupId);
    void saveConversation(in GOIMConversation conversation);
    void updateConversationName(long groupId, String newName);
    void setConversationOnTop(long groupId, boolean isOnTop);
    void updateMessagesGroupId(long oldGroupId, long newGroupId);
    void clearUnread(long groupId);
    void saveUnreadCount(long groupId, int count);
    int getUnreadCount(long groupId);
    long getMsgCount(long groupId);
    List<GOIMMessage> findMsgs(long groupId, String startMsgId, int count);
    GOIMMessage findTransferMsg(long groupId, long transId);
    void addSystemMsg(in GOIMSystemMessage systemMessage);
    List<GOIMSystemMessage> getSystemMsgs();
    GOIMSystemMessage getLastSystemMsg();
    int getUnreadSystemMsgs();
    void clearUnreadSystemMsgs();
    void clearAllSystemMsgs();

}
