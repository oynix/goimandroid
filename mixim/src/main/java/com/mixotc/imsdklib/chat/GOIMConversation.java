package com.mixotc.imsdklib.chat;

import android.content.Context;
import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.R;
import com.mixotc.imsdklib.chat.manager.LocalChatDBProxy;
import com.mixotc.imsdklib.chat.manager.LocalChatManager;
import com.mixotc.imsdklib.chat.manager.LocalContactManager;
import com.mixotc.imsdklib.chat.manager.LocalConversationManager;
import com.mixotc.imsdklib.chat.manager.LocalGroupManager;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.message.TextMessageBody;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mixotc.imsdklib.database.table.ConversationTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_SINGLE;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_TOP;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TEXT;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TIME;
import static com.mixotc.imsdklib.database.table.ConversationTable.NAME;

/**
 * 实现Parcelable进行数据传输只操作{@link com.mixotc.imsdklib.database.table.ConversationTable}中存储的列的项。
 * 共6项：
 * 1. groupId
 * 2. name
 * 3. is_single
 * 4. is_top
 * 5. last_msg_time
 * 6. last_msg_text
 * 这6项从数据库存储和获取，其他数据基于上面这些数据直接或间接获取。
 */
public class GOIMConversation implements Parcelable {

    private static final String TAG = GOIMConversation.class.getSimpleName();

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#GROUP_ID} */
    private long mGroupId;

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#NAME} */
    private String mName;

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#IS_SINGLE} */
    private boolean mIsSingle = false;

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#IS_TOP} */
    private boolean mIsOnTop = false;

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#LAST_MSG_TIME} */
    private long mLastMsgTime;

    /** {@link com.mixotc.imsdklib.database.table.ConversationTable#LAST_MSG_TEXT} */
    private String mLastMsgText;

    private transient int mUnreadMsgCount = 0;
    private transient List<GOIMMessage> mMessages = new ArrayList<>();
    private transient GOIMGroup mGroup;

    public static final Creator<GOIMConversation> CREATOR = new Creator<GOIMConversation>() {
        @Override
        public GOIMConversation createFromParcel(Parcel source) {
            return new GOIMConversation(source);
        }

        @Override
        public GOIMConversation[] newArray(int size) {
            return new GOIMConversation[size];
        }
    };

    public GOIMConversation(Parcel parcel) {
        readFromParcel(parcel);
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(mGroupId);
        dest.writeString(mName);
        dest.writeInt(mIsSingle ? 1 : 0);
        dest.writeInt(mIsOnTop ? 1 : 0);
        dest.writeLong(mLastMsgTime);
        dest.writeString(mLastMsgText);
    }

    private void readFromParcel(Parcel parcel) {
        mGroupId = parcel.readLong();
        mName = parcel.readString();
        mIsSingle = parcel.readInt() == 1;
        mIsOnTop = parcel.readInt() == 1;
        mLastMsgTime = parcel.readLong();
        mLastMsgText = parcel.readString();
    }

    public GOIMConversation(long groupId, String name, List<GOIMMessage> messages, long msgCount, boolean isOnTop, boolean isSingle, boolean isTemp) {
//        Logger.e(TAG, groupId + "create GOIMConversation:" + name + isSingle);
        mGroupId = groupId;
        mIsOnTop = isOnTop;
        mIsSingle = isSingle;
        mGroup = LocalGroupManager.getInstance().getGroupById(groupId);
        mName = name;
        if (TextUtils.isEmpty(mName)) {
            if (mGroup != null) {
                if (mGroup.isSingle()) {
                    GOIMContact member = mGroup.getMemberByIndex(0);
                    if (member != null) {
                        mName = member.getNick();
                    }
                } else {
                    mName = mGroup.getGroupName();
                }
            } else {
                if (groupId < 0) {
                    GOIMContact anonymousContact = LocalContactManager.getInstance().getAnonymousContact(-groupId);
                    if (anonymousContact != null) {
                        mName = anonymousContact.getNick();
                    }
                }
            }
        }

        if (messages == null) {
            mMessages = Collections.synchronizedList(new ArrayList<GOIMMessage>());
        } else {
            mMessages = Collections.synchronizedList(messages);
        }
        if (mUnreadMsgCount <= 0) {
            mUnreadMsgCount = mGroupId == 0 ? LocalChatDBProxy.getInstance().getUnreadSystemMsgs() : LocalChatDBProxy.getInstance().getUnreadCount(groupId);
        }
        if (mMessages.size() > 0) {
            GOIMMessage lastMsg = mMessages.get(mMessages.size() - 1);
            mLastMsgTime = lastMsg.getMsgTime();
            mLastMsgText = getMessageDigest(lastMsg, AdminManager.getInstance().getApplicationContext());
        }
//        if (mGroup != null) {
//            mIsSingle = mGroup.isSingle();
//        } else {
//            if (groupId < 0) {
//                mIsSingle = true;
//            } else if (groupId > 0) {
//                GOIMContact contact = LocalContactManager.getInstance().getContactByGroupId(groupId);
//                Logger.e(TAG, name + "构造函数初始化isSingle，getContactByGroupId是否为null：" + (contact == null));
//                mIsSingle = contact != null;
//            }
//        }
    }

    /**
     * 发送新消息时，先调用此方法将消息存储到conversation中，然后在后台发送，
     * 发送的实际动作在{MessageAdapter}中调用
     */
    public void addMessage(GOIMMessage message) {
        addMessage(message, true, true);
    }

    public void addMessage(GOIMMessage message, boolean unread, boolean toDB) {
        // TODO ：2018.4.16 重复消息检查了两次，是否可以删除一种
        if (mMessages.size() > 0) {
            GOIMMessage lastMessage = mMessages.get(mMessages.size() - 1);
            if ((message.getMsgId() != null) && (lastMessage.getMsgId() != null)
                    && (message.getMsgId().equals(lastMessage.getMsgId()))) {
                return;
            }
        }
        boolean messageExist = false;
        for (GOIMMessage theMessage : mMessages) {
            if (theMessage.getMsgId().equals(message.getMsgId())) {
                messageExist = true;
                break;
            }
        }
        if (!messageExist) {
            mMessages.add(message);
            if ((message.getDirect() == GOIMMessage.Direct.RECEIVE) && (message.isUnread()) && (unread)) {
                mUnreadMsgCount += 1;
                saveUnreadMsgCount(mUnreadMsgCount);
            }

            mLastMsgTime = message.getMsgTime();
            mLastMsgText = getMessageDigest(message, AdminManager.getInstance().getApplicationContext());
            if (toDB) {
                LocalChatDBProxy.getInstance().saveConversation(this);
            }
        }
    }

    private void saveUnreadMsgCount(final int unreadMsgCount) {
        LocalChatManager.getInstance().mMsgCountThreadPool.submit(new Runnable() {
            public void run() {
                LocalChatDBProxy.getInstance().saveUnreadCount(mGroupId, unreadMsgCount);
            }
        });
    }

    public int getUnreadMsgCount() {
        if (mUnreadMsgCount < 0) {
            mUnreadMsgCount = 0;
        }
        return mUnreadMsgCount;
    }

    public void addSystemMessage(GOIMSystemMessage message) {
        if (message != null) {
            setLastMsgTime(message.getMsgTime());
            setLastMsgText(message.getMsgText());
            if (message.unRead()) {
                mUnreadMsgCount++;
            }
        } else {
            setLastMsgTime(0);
            setLastMsgText("");
            mUnreadMsgCount = 0;
        }
    }

    public void resetUnreadMsgCount() {
        mUnreadMsgCount = 0;
        saveUnreadMsgCount(0);
    }

    public int getAllMsgCount() {
        return mMessages.size();
    }

    public GOIMMessage getMessage(int index) {
        return getMessage(index, true);
    }

    public GOIMMessage getMessage(int index, boolean markRead) {
        if (index >= mMessages.size()) {
            Logger.e(TAG, "out of bound, messages.size:" + mMessages.size());
            return null;
        }
        GOIMMessage message = mMessages.get(index);
        if ((markRead) && (message != null) && (message.isUnread())) {
            message.setUnread(false);
            if (mUnreadMsgCount > 0) {
                mUnreadMsgCount -= 1;
                saveUnreadMsgCount(mUnreadMsgCount);
            }
        }
        return message;
    }

//    public List<GOIMMessage> loadMoreMsgFromDB(String startMsgId, int count) {
//        List<GOIMMessage> messages = LocalChatDBProxy.getInstance().loadMessageById(mGroupId, startMsgId, count);
//        mMessages.addAll(0, messages);
//        for (GOIMMessage message : messages) {
//            LocalConversationManager.getInstance().addMessageToM(message, false);
//        }
//        if (mMessages.size() > 0) {
//            GOIMMessage lastMsg = mMessages.get(mMessages.size() - 1);
//            mLastMsgTime = lastMsg.getMsgTime();
//            mLastMsgText = getMessageDigest(lastMsg, AdminManager.getInstance().getApplicationContext());
//        }
//        return messages;
//    }

    public void updateLastMsgInfo() {
        if (mMessages.size() > 0) {
            GOIMMessage lastMsg = mMessages.get(mMessages.size() - 1);
            mLastMsgTime = lastMsg.getMsgTime();
            mLastMsgText = getMessageDigest(lastMsg, AdminManager.getInstance().getApplicationContext());
        }
    }

    public GOIMMessage getMessage(String msgId) {
        return getMessage(msgId, true);
    }

    public GOIMMessage getMessage(String msgId, boolean markRead) {
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            GOIMMessage message = mMessages.get(i);
            if (message.getMsgId().equals(msgId)) {
                if ((markRead) && (message.isUnread())) {
                    message.setUnread(false);
                    if (mUnreadMsgCount > 0) {
                        mUnreadMsgCount -= 1;
                        saveUnreadMsgCount(mUnreadMsgCount);
                    }
                }
                return message;
            }
        }
        return null;
    }

    public List<GOIMMessage> getAllMessages() {
        return mMessages;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setNewGroupId(long newGroupId) {
        mGroupId = newGroupId;
        mGroup = null;
    }

    public String getName() {
        if (mGroup != null) {
            if (mGroup.isSingle()) {
                GOIMContact user = mGroup.getMemberByIndex(0);
                if (user != null) {
                    mName = user.getNick();
                }
            }
        }
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public boolean isOnTop() {
        return mIsOnTop;
    }

    public void setIsOnTop(boolean isOnTop) {
        mIsOnTop = isOnTop;
    }

    public boolean isSingle() {
        return mIsSingle;
    }

    public void setIsSingle(boolean isSingle) {
        mIsSingle = isSingle;
    }

    public boolean isTemp() {
        return mLastMsgTime <= 0;
    }

    public long lastMsgTime() {
        return mLastMsgTime;
    }

    public void setLastMsgTime(long lastMsgTime) {
        mLastMsgTime = lastMsgTime;
    }

    public String lastMsgText() {
        return mLastMsgText;
    }

    public void setLastMsgText(String lastMsgText) {
        mLastMsgText = lastMsgText;
    }

    public GOIMGroup getGroup() {
        if (mGroup == null) {
            mGroup = LocalGroupManager.getInstance().getGroupById(mGroupId);
        }
        return mGroup;
    }

    public void removeMessage(String msgId) {
        Logger.d(TAG, "remove msg from conversation:" + msgId);
        for (int i = mMessages.size() - 1; i >= 0; i--) {
            GOIMMessage message = mMessages.get(i);
            if (message.getMsgId().equals(msgId)) {
                if (message.isUnread()) {
                    message.setUnread(false);
                    if (mUnreadMsgCount > 0) {
                        mUnreadMsgCount -= 1;
                        saveUnreadMsgCount(mUnreadMsgCount);
                    }
                }
                mMessages.remove(i);

                LocalChatDBProxy.getInstance().deleteMsg(msgId);
                LocalConversationManager.getInstance().removeMessage(msgId);
                break;
            }
        }
        if (mMessages.size() > 0) {
            // get the last message
            GOIMMessage message = mMessages.get(mMessages.size() - 1);
            mLastMsgTime = message.getMsgTime();
            mLastMsgText = getMessageDigest(message, AdminManager.getInstance().getApplicationContext());
        } else {
            mLastMsgText = "";
        }
    }

    public GOIMMessage getLastMessage() {
        if (mMessages.size() == 0) {
            return null;
        }
        return mMessages.get(mMessages.size() - 1);
    }

    public void clear() {
        mMessages.clear();
        mUnreadMsgCount = 0;
        mLastMsgText = "";
        LocalChatDBProxy.getInstance().clearUnread(mGroupId);
    }

    /**
     * 根据消息内容和消息类型获取消息内容提示
     *
     * @param message
     * @param context
     * @return
     */
    public static String getMessageDigest(GOIMMessage message, Context context) {
        String digest = "";
        switch (message.getType()) {
            case LOCATION: // 位置消息
                if (message.getDirect() == GOIMMessage.Direct.RECEIVE) {
                    digest = context.getResources().getString(R.string.location_recv);
                    digest = String.format(digest, message.getContact().getNick());
                    return digest;
                } else {
                    digest = context.getResources().getString(R.string.location_prefix);
                }
                break;
            case IMAGE: // 图片消息
                digest = context.getResources().getString(R.string.picture);
                break;
            case VOICE:// 语音消息
                digest = context.getResources().getString(R.string.voice);
                break;
            case VIDEO: // 视频消息
                digest = context.getResources().getString(R.string.video);
                break;
            case TXT: // 文本消息
                TextMessageBody txtBody = (TextMessageBody) message.getBody();
                digest = txtBody.getMessage();
                break;
            case FILE: // 普通文件消息
                digest = context.getResources().getString(R.string.file);
                break;
            case PACKET:
                digest = context.getResources().getString(R.string.packet);
                break;
            case TRANSFER:
                digest = context.getResources().getString(R.string.transfer);
                break;
            case SECURETRANS:
                digest = context.getResources().getString(R.string.securedtransfer);
                break;
            default:
                System.err.println("error, unknow type");
                return "";
        }

        return digest;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /** 从Cursor中创建一个实例 */
    public static GOIMConversation createFromCursor(Cursor cursor) {
        long groupId = cursor.getLong(cursor.getColumnIndex(GROUP_ID));
        String name = cursor.getString(cursor.getColumnIndex(NAME));
        boolean isOnTop = cursor.getInt(cursor.getColumnIndex(IS_TOP)) == 1;
        boolean isSingle = cursor.getInt(cursor.getColumnIndex(IS_SINGLE)) == 1;
        long lastMsgTime = cursor.getLong(cursor.getColumnIndex(LAST_MSG_TIME));
        String lastMsg = cursor.getString(cursor.getColumnIndex(LAST_MSG_TEXT));
        GOIMConversation conversation = new GOIMConversation(groupId, name, null, 0, isOnTop, isSingle, false);
        conversation.setLastMsgTime(lastMsgTime);
        conversation.setLastMsgText(lastMsg);
        return conversation;
    }

}
