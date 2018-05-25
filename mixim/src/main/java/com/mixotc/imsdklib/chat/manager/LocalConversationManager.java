package com.mixotc.imsdklib.chat.manager;

import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.listener.GOIMConversationListener;
import com.mixotc.imsdklib.listener.RemoteConversationListener;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

public class LocalConversationManager {
    private static final String TAG = LocalConversationManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final LocalConversationManager INSTANCE = new LocalConversationManager();
    }

    /** conversation首次加载时默认加载的消息条数 */
    private static final int DEFAULT_LOAD_MESSAGE_COUNT = 20;

    private Hashtable<String, GOIMMessage> mAllMessages = new Hashtable<>(50);
    private Hashtable<Long, GOIMConversation> mConversations = new Hashtable<>(50);
    private GOIMConversation mSystemConversation = null;
    private List<GOIMConversationListener> mConversationListeners = new ArrayList<>();

    private LocalConversationManager() {
    }

    public static LocalConversationManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        if (mConversations != null) {
            mConversations.clear();
        }
        if (mAllMessages != null) {
            mAllMessages.clear();
        }
        mSystemConversation = null;
    }

    // TODO: 2018/4/19 conversation需要做成分页加载。
    public synchronized void initData() {
        mConversations.clear();
        mAllMessages.clear();
        mConversations.putAll(LocalChatDBProxy.getInstance().getConversationsWithoutMessage(DEFAULT_LOAD_MESSAGE_COUNT));
        for (GOIMConversation conversation : mConversations.values()) {
            loadMoreMsgFromDbToConversation(conversation);
        }
        Logger.d(TAG,  "GOIM Conversation Manager initialize data, size:" + mConversations.size());
    }

    public void addConversationListener(GOIMConversationListener l) {
        if (l == null) {
            return;
        }
        if (!mConversationListeners.contains(l)) {
            mConversationListeners.add(l);
        }
    }

    public void removeConversationListener(GOIMConversationListener l) {
        mConversationListeners.remove(l);
    }

    /** 从列表主动删除一个conversation */
    public void deleteConversation(long groupId) {
        Logger.d(TAG, "remove conversation for user: " + groupId);
        LocalChatDBProxy.getInstance().deleteConversionMsgs(groupId);
        LocalChatDBProxy.getInstance().deleteConversation(groupId);
        GOIMConversation conversation = mConversations.remove(groupId);
        if (conversation == null) {
            return;
        }
        conversation.clear();
        List<GOIMMessage> messages = conversation.getAllMessages();
        for (GOIMMessage message : messages) {
            mAllMessages.remove(message.getMsgId());
        }
    }

    /** 清空聊天记录 */
    public void clearConversation(long groupId) {
        Logger.d(TAG, "clear conversation for user: " + groupId);
        LocalChatDBProxy.getInstance().deleteConversionMsgs(groupId);
        GOIMConversation conversation = mConversations.get(groupId);
        if (conversation == null) {
            return;
        }
        List<GOIMMessage> messages = conversation.getAllMessages();
        for (GOIMMessage message : messages) {
            mAllMessages.remove(message.getMsgId());
        }
        conversation.clear();
    }

    /** 获取系统conversation */
    private GOIMConversation getSystemConversation() {
        if (mSystemConversation == null) {
            mSystemConversation = new GOIMConversation(0, "系统通知", null, 0, false, false, true);
            GOIMSystemMessage lastSystemMsg = LocalChatDBProxy.getInstance().getLastSystemMsg();
            if (lastSystemMsg != null) {
                mSystemConversation.setLastMsgTime(lastSystemMsg.getMsgTime());
                mSystemConversation.setLastMsgText(lastSystemMsg.getMsgText());
            }
        }
        return mSystemConversation;
    }

    /** 清空系统conversation */
    public void clearSystemConversation() {
        LocalChatDBProxy.getInstance().clearAllSystemMsgs();
        getSystemConversation().addSystemMessage(null);
    }

    /** 清除未读的系统通知消息 */
    public void clearUnreadSystemMsg() {
        LocalChatDBProxy.getInstance().clearUnreadSystemMsgs();
        getSystemConversation().resetUnreadMsgCount();
    }

    public GOIMMessage getMessage(String msgId) {
        return mAllMessages.get(msgId);
    }

    public void removeMessage(String msgId) {
        mAllMessages.remove(msgId);
    }

    /** 加载更多消息到conversation和memory */
    public void loadMoreMsgFromDbToConversation(GOIMConversation conversation) {
        long groupId = conversation.getGroupId();
        List<GOIMMessage> existMsgs = conversation.getAllMessages();
        String startMsgId = null;
        if (existMsgs.size() > 0) {
            startMsgId = existMsgs.get(0).getMsgId();
        }
        List<GOIMMessage> messages = LocalChatDBProxy.getInstance().loadMessageById(groupId, startMsgId, DEFAULT_LOAD_MESSAGE_COUNT);
        existMsgs.addAll(0, messages);
        for (GOIMMessage message : messages) {
            addMessageToM(message, false);
        }
        conversation.updateLastMsgInfo();
    }

    /** 获取所有conversation，包括系统消息conversation */
    public Hashtable<Long, GOIMConversation> getAllConversations() {
        GOIMConversation systemConversation = getSystemConversation();
        Hashtable<Long, GOIMConversation> conversationList = getConversationList();
        conversationList.put(systemConversation.getGroupId(), systemConversation);
        return conversationList;
    }

    public Hashtable<Long, GOIMConversation> getConversationList() {
        Logger.e(TAG, "get conversation list :" + mConversations.size());
        return mConversations;
    }

    /** 从数据库加载的消息，添加到LocalManager */
    public void addMessageToM(GOIMMessage message, boolean unread) {
//        Logger.d(TAG, "add message to memory :" + message.toString());
        String msgId = message.getMsgId();
        if (mAllMessages.containsKey(msgId)) {
            return;
        }
        mAllMessages.put(msgId, message);
        GOIMConversation conversation = getConversation(message.getGroupId());
        conversation.addMessage(message, unread, false);
        if (!mConversations.containsKey(message.getGroupId())) {
            mConversations.put(message.getGroupId(), conversation);
        }
    }

    /** 1 发送消息时会调用该方法；2 打开红包后调用此方法 */
    public void saveMessage(GOIMMessage message, boolean unread) {
        Logger.d(TAG, "save message:" + message.getMsgId());
        try {
            addMessageToM(message, unread);
            LocalChatDBProxy.getInstance().saveMsgToDB(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 获取未读消息数量 */
    public int getUnreadMessageCount() {
        int result = 0;
        Collection<GOIMConversation> localCollection = mConversations.values();
        for (GOIMConversation aLocalCollection : localCollection) {
            if (aLocalCollection.getGroupId() != 0) {
                result += aLocalCollection.getUnreadMsgCount();
            }
        }
        Logger.d(TAG, "get unread msg count return:" + result);
        return result;
    }

    /** 未读消息数量更新时，比如删除一个对话等，调用此方法 */
    public void updateUnreadCount() {
        for (GOIMConversationListener listener : mConversationListeners) {
            listener.onUnreadCountUpdate();
        }
    }

    /** 获取conversation，当不存在时初始化一个新的conversation */
    public GOIMConversation getConversation(long groupId) {
//        Logger.d(TAG, "get conversation for group:" + groupId);
        GOIMConversation conversation = mConversations.get(groupId);
        if (conversation != null) {
            return conversation;
        }
        conversation = createConversation(groupId);
        mConversations.put(groupId, conversation);
        return conversation;
    }

    public boolean isConversationExist(long groupId) {
        return mConversations != null && mConversations.containsKey(groupId);
    }

    // 当conversation不存在时，新建一个conversation
    private GOIMConversation createConversation(long groupId) {
        Logger.d(TAG, "load create conversation :" + groupId);
        List<GOIMMessage> messages = LocalChatDBProxy.getInstance().loadMessageById(groupId, null, DEFAULT_LOAD_MESSAGE_COUNT);
        long count = LocalChatDBProxy.getInstance().getMsgCount(groupId);
        GOIMGroup group = LocalGroupManager.getInstance().getGroupById(groupId);
        GOIMConversation conversation = new GOIMConversation(groupId, "", messages, count, false, true, true);
        if (group != null) {
            conversation.setIsSingle(group.isSingle());
            if (group.getMembers().size() > 0) {
                // TODO: 2018/4/18 没有成员时如何初始化group name
                conversation.setName(group.isSingle() ? group.getMembers().get(0).getNick() : group.getGroupName());
            }
        }
        return conversation;
    }

    /** 更改conversation的置顶状态 */
    public void setConversationOnTop(long groupId, boolean isOnTop) {
        Logger.d(TAG, "set on top for conversation:" + groupId);
        LocalChatDBProxy.getInstance().setConversationOnTop(groupId, isOnTop);
        GOIMConversation conversation = mConversations.get(groupId);
        if (conversation != null) {
            conversation.setIsOnTop(isOnTop);
        }
    }

    // 通过该Listener回调执行的操作，都是更新内存中的数据，不用更新database数据
    public RemoteConversationListener mConversationListener = new RemoteConversationListener.Stub() {
        @Override
        public void onConversationDelete(long groupId) {
            Logger.e(TAG, "remote listener : on conversation delete," + mConversations.size());
            GOIMConversation conversation = mConversations.remove(groupId);
            Logger.e(TAG, groupId + " -- remote listener : on conversation delete : " + (conversation == null) + "," + mConversations.size());
            if (conversation == null) {
                return;
            }
            for (GOIMMessage message : conversation.getAllMessages()) {
                mAllMessages.remove(message.getMsgId());
            }

            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onConversationDelete(groupId);
            }
        }

        /** 更新 group id, 同时更新local group manager */
        @Override
        public void onConversationGroupIdUpdate(long oldGroupId, long newGroupId) {
            Logger.e(TAG, "remote listener : on conversation id update");
            GOIMConversation conversation = mConversations.remove(oldGroupId);
            if (conversation == null) {
                return;
            }
            conversation.setNewGroupId(newGroupId);
            for (GOIMMessage message : conversation.getAllMessages()) {
                message.setGroupId(newGroupId);
                mAllMessages.put(message.getMsgId(), message);
            }
            mConversations.put(newGroupId, conversation);

            // 2018/4/16 更新之后服务器会发送新的通知，随后RemoteGroupManager会进行相应更新
            // update group id
//            ConcurrentHashMap<Long, GOIMGroup> groups = LocalGroupManager.getInstance().getGroupList();
//            GOIMGroup group = groups.remove(oldGroupId);
//            if (newGroupId > 0) {
//                group.setGroupId(newGroupId);
//                groups.put(newGroupId, group);
//            }

            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onConversationGroupIdUpdate(oldGroupId, newGroupId);
            }
        }

        /** 更新conversation name */
        @Override
        public void onConversationNameUpdate(long groupId, String newName) {
            GOIMConversation conversation = mConversations.get(groupId);
            if (conversation == null) {
                return;
            }
            conversation.setName(newName);
            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onConversationNameUpdate(groupId, newName);
            }
        }

        /** 收到新消息时 */
        @Override
        public void onMessageReceived(GOIMMessage message, boolean unread) {
            // 已经在database了，所以不用存储到DB，增加至内存
            Logger.d(TAG, "on message receive:" + message.toString());
            addMessageToM(message, unread);
            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onReceiveNewMessage(message);
            }
        }

        /** 收到新系统消息时 */
        @Override
        public void onSysMsgReceived(GOIMSystemMessage message) {
            getSystemConversation().addSystemMessage(message);
            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onReceiveSysMessage(message);
            }
        }

        /** 消息更新时：状态／消息体等 */
        @Override
        public void onMessageUpdate(GOIMMessage message) {
            Logger.e(TAG, "update msg:" + message.getStatus().name() + ",content:" + message.getBody().toString());
            mAllMessages.put(message.getMsgId(), message);
            GOIMConversation conversation = mConversations.get(message.getGroupId());
            if (conversation == null) {
                return;
            }
            GOIMMessage msg = conversation.getMessage(message.getMsgId(), false);
            if (msg != null) {
                msg.setStatus(message.getStatus());
            }
            for (GOIMConversationListener listener : mConversationListeners) {
                listener.onMessageUpdate(message);
            }
        }
    };
}
