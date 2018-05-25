package com.mixotc.imsdklib.remotechat;

import android.content.Context;
import android.os.RemoteException;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.listener.RemoteConversationListener;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class RemoteConversationManager {
    private static final String TAG = RemoteConversationManager.class.getSimpleName();
    private static RemoteConversationManager sInstance = new RemoteConversationManager();
    private Context mContext;
    private RemoteConversationListener mConversationListener = null;

    private RemoteConversationManager() {
    }

    public void init(Context context) {
        mContext = context;
    }

    public static RemoteConversationManager getInstance() {
        return sInstance;
    }

    public void setConversationListener(RemoteConversationListener conversationListener) {
        mConversationListener = conversationListener;
    }

    public void removeConversationListener() {
        mConversationListener = null;
    }

    /** 更新一条消息体 */
    public void updateMessageBody(GOIMMessage message) {
        RemoteDBManager.getInstance().updateMessageBody(message);
        onMessageUpdate(message);
    }

    /** 更新一条消息状态 */
    public void updateMessageStatus(GOIMMessage message) {
        RemoteDBManager.getInstance().updateMsgStatus(message.getMsgId(), message.getStatus().ordinal());
        onMessageUpdate(message);
    }

    /** 主动删除group时要删除conversation，以及messages */
    public void deleteConversationAndRelation(long groupId) {
        RemoteDBManager.getInstance().deleteConversation(groupId);
        RemoteDBManager.getInstance().deleteGroupMsgs(groupId);
        onConversationDelete(groupId);
    }

    /** 同意别人请求成功／被添加好友成功／被删除好友成功后，更新相关conversation的groupId */
    public void updateConversationGroupId(long oldGroupId, long newGroupId) {
        RemoteDBManager.getInstance().updateConversationId(oldGroupId, newGroupId);
        RemoteDBManager.getInstance().updateMessagesGroupId(oldGroupId, newGroupId);
        RemoteDBManager.getInstance().updateUnreadGroupId(oldGroupId, newGroupId);
        onConversationGroupIdUpdate(oldGroupId, newGroupId);
    }

    /** 更新Conversation Name.对话的name和群组的name相关联，当被删除后对话中不存在群组，但是存在name。 */
    public void updateConversationName(long groupId, String newName) {
        RemoteDBManager.getInstance().updateConversationName(groupId, newName);
        onConversationNameUpdate(groupId, newName);
    }

    /** 接收到新的系统消息, 目前只有添加好友请求 */
    public void receiveSysMsg(GOIMSystemMessage message) {
        RemoteDBManager.getInstance().addSystemMsg(message);
        onSysMsgReceived(message);
    }

    /** 接收到新消息 */
    public void receiveNewMessage(GOIMMessage message, boolean unread) {
        Logger.d(TAG, "receive and save message:" + message.getMsgId());
        RemoteDBManager.getInstance().saveMsg(message);

        if (unread && message.getDirect() == GOIMMessage.Direct.RECEIVE) {
            RemoteDBManager.getInstance().increaseUnreadCount(message.getGroupId());
        }

        GOIMConversation conversation = RemoteDBManager.getInstance().getConversationById(message.getGroupId());
        if (conversation == null) {
            conversation = createConversation(message);
        }
        conversation.setLastMsgText(GOIMConversation.getMessageDigest(message, mContext));
        conversation.setLastMsgTime(message.getMsgTime());
        RemoteDBManager.getInstance().saveConversation(conversation);
        onMessageReceived(message, unread);
    }

    /** 当conversation不存在时，创建一个 */
    private GOIMConversation createConversation(GOIMMessage message) {
        boolean isSingle = true;
        String name = "";
        if (message.getGroupId() >= 0) {
            GOIMGroup group = RemoteDBManager.getInstance().getGroupById(message.getGroupId());
            if (group != null) {
                isSingle = group.isSingle();
                name = isSingle ? group.getMemberByIndex(0).getNick() : group.getGroupName();
            }
        } else {
            GOIMContact contact = RemoteDBManager.getInstance().getTempContactById(-message.getGroupId());
            if (contact != null) {
                name = contact.getNick();
            }
        }
        List<GOIMMessage> messages = new ArrayList<>();
        messages.add(message);
        return new GOIMConversation(message.getGroupId(), name, messages, 1, false, isSingle, false);
    }

    // 通知Local更新conversation name
    private void onConversationNameUpdate(long groupId, String newName) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onConversationNameUpdate(groupId, newName);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 通知Local更新conversation group id
    private void onConversationGroupIdUpdate(long oldId, long newId) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onConversationGroupIdUpdate(oldId, newId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 收到新消息时通知Local
    private void onMessageReceived(GOIMMessage msg, boolean unread) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onMessageReceived(msg, unread);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 收到系统消息时
    private void onSysMsgReceived(GOIMSystemMessage msg) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onSysMsgReceived(msg);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 删除对话时，如：主动删除group时要删除conversation
    private void onConversationDelete(long groupId) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onConversationDelete(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 消息更新时，通知Local
    private void onMessageUpdate(GOIMMessage message) {
        if (mConversationListener != null) {
            try {
                mConversationListener.onMessageUpdate(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 当某个联系人的基本信息更新后，检查是否有需要同步的conversation */
    public void onUpdateUserBaseInfo(GOIMContact newContact) {
        GOIMConversation conversation = RemoteDBManager.getInstance().getConversationById(newContact.getGroupId());
        if (conversation == null) {
            return;
        }
        RemoteDBManager.getInstance().updateConversationName(newContact.getGroupId(), newContact.getUsername());
        onConversationNameUpdate(newContact.getGroupId(), newContact.getUsername());
    }
}
