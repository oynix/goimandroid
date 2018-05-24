package com.mixotc.imsdklib.remotechat;

import android.database.Cursor;

import com.mixotc.imsdklib.message.GOIMMessage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.mixotc.imsdklib.database.table.ConversationTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_SINGLE;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_TOP;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TEXT;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TIME;
import static com.mixotc.imsdklib.database.table.ConversationTable.NAME;

public class RemoteConversation {
    private static final String TAG = RemoteConversation.class.getSimpleName();
    private List<GOIMMessage> mMessages;
    private long mGroupId;
    private String mName;
    private long mMsgCount = 0L;
    private boolean mIsOnTop = false;
    private boolean mIsSingle = false;
    private long mLastMsgTime;
    private String mLastMsgText;

    public RemoteConversation(long groupId, String name, List<GOIMMessage> messages, long msgCount, boolean isOnTop, boolean isSingle, long lastMsgTime, String lastMsgText) {
//        Logger.e(TAG, groupId + "创建RemoteConversation is single:" + name + isSingle);
        mGroupId = groupId;
        mName = name;
        if (mMessages == null) {
            mMessages = Collections.synchronizedList(new ArrayList<GOIMMessage>());
        } else {
            mMessages = Collections.synchronizedList(messages);
        }
        mMsgCount = msgCount;
        mIsOnTop = isOnTop;
        mIsSingle = isSingle;
        mLastMsgTime = lastMsgTime;
        mLastMsgText = lastMsgText;
    }

    public List<GOIMMessage> getAllMessages() {
        return mMessages;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public String getName() {
        return mName;
    }

    public boolean isOnTop() {
        return mIsOnTop;
    }

    public boolean isSingle() {
        return mIsSingle;
    }

    public long lastMsgTime() {
        return mLastMsgTime;
    }

    public String lastMsgText() {
        return mLastMsgText;
    }

    public long getMsgCount() {
        return mMsgCount;
    }

    public void addMessage(GOIMMessage msg) {
        mMessages.add(0, msg);
        mMsgCount = mMessages.size();
    }

    /** 从Cursor中创建一个实例 */
    public static RemoteConversation createFromCursor(Cursor cursor) {
        long groupId = cursor.getLong(cursor.getColumnIndex(GROUP_ID));
        String name = cursor.getString(cursor.getColumnIndex(NAME));
        boolean isOnTop = cursor.getInt(cursor.getColumnIndex(IS_TOP)) == 1;
        boolean isSingle = cursor.getInt(cursor.getColumnIndex(IS_SINGLE)) == 1;
        long lastMsgTime = cursor.getLong(cursor.getColumnIndex(LAST_MSG_TIME));
        String lastMsg = cursor.getString(cursor.getColumnIndex(LAST_MSG_TEXT));

        return new RemoteConversation(groupId, name, null, 0, isOnTop, isSingle, lastMsgTime, lastMsg);
    }
}
