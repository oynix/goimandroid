package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.chat.GOIMConversation;
import com.mixotc.imsdklib.remotechat.RemoteConversation;
import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.utils.Logger;

import java.util.Hashtable;

import static com.mixotc.imsdklib.database.table.ConversationTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_SINGLE;
import static com.mixotc.imsdklib.database.table.ConversationTable.IS_TOP;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TEXT;
import static com.mixotc.imsdklib.database.table.ConversationTable.LAST_MSG_TIME;
import static com.mixotc.imsdklib.database.table.ConversationTable.NAME;
import static com.mixotc.imsdklib.database.table.ConversationTable.TABLE_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:19
 * Version  : v1.0.0
 * Describe :
 */

public class ConversationTableProvider extends BaseIMTableProvider {

    private static final String TAG = ConversationTableProvider.class.getSimpleName();

    /** 加载conversation时，没个conversation加载消息数量的默认值 */
    private static final int COUNT_OF_MESSAGE_LOADED_INIT = 20;

    public ConversationTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /**
     * 加载所有的conversation。加载数据时conversation表中already包含conversation所需要的所有数据，
     * 所以直接从conversation表中加载即可,首次加载每个conversation最多load20条聊天消息。
     * 其中的消息集合按照消息的时间值降序排列，即最新的消息排在最前面，这样符合常规逻辑。
     */
    public synchronized Hashtable<Long, RemoteConversation> loadAllConversation() {
        long start = System.currentTimeMillis();
        Hashtable<Long,RemoteConversation> result = new Hashtable<>();
        Cursor cursor = null;
        try {
            // load base information from conversation table
            cursor = mHelper.query(TABLE_NAME, null, null,null,null,null,
                    LAST_MSG_TIME + " DESC");
            if (cursor == null) {
                Logger.e(TAG, "查询数据库加载所有会话失败!!");
                return result;
            }
            while (cursor.moveToNext()) {
                RemoteConversation conversation = RemoteConversation.createFromCursor(cursor);
                result.put(conversation.getGroupId(), conversation);
            }
//            // close current query cursor and begin next query.
//            cursor.close();
//            // load message count and all message from chat table.
//            // query messages descending so that the newest message will be at first.
//            cursor = mHelper.query(ChatTable.TABLE_NAME, null, null, null, null, null,
//                    ChatTable.GROUP_ID + "," + ChatTable.MSG_ID + " DESC");
//            if (cursor == null) {
//                Logger.e(TAG, "查询会话消息失败！！");
//                return result;
//            }
//            int count = 0;
//            while (cursor.moveToNext() && count <= COUNT_OF_MESSAGE_LOADED_INIT) {
//                GOIMMessage msg = GOIMMessage.createFromCursor(cursor);
//                if (msg == null) {
//                    continue;
//                }
//                RemoteConversation conversation = result.get(msg.getGroupId());
//                conversation.addMessage(msg);
//                Logger.e(TAG, "add message to conversation :" + msg.getBody().toString());
//                count ++;
//            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "加载所有conversation耗时：" + (System.currentTimeMillis() - start) + ",size:" + result.size());
        return result;
    }

    /** 根据groupId加载一条conversation，若查询不到则返回null。 */
    public GOIMConversation loadConversationById(long groupId) {
        GOIMConversation result = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, null, null, null);
            if (cursor.moveToFirst()) {
                result = GOIMConversation.createFromCursor(cursor);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /** 根据groupId删除一个conversation */
    public void deleteConversationById(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            mHelper.delete(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 插入一条新的数据conversation */
    public void insertConversation(GOIMConversation conversation) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GROUP_ID, conversation.getGroupId());
            contentValues.put(NAME, conversation.getName());
            contentValues.put(IS_SINGLE, conversation.isSingle() ? 1 : 0);
            contentValues.put(IS_TOP, conversation.isOnTop() ? 1 : 0);
            contentValues.put(LAST_MSG_TIME, conversation.lastMsgTime());
            contentValues.put(LAST_MSG_TEXT, conversation.lastMsgText());
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
            Logger.d(TAG, "add conversation to db:" + contentValues.toString());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 更新conversation的name */
    public void updateConversationName(long groupId, String name) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(NAME, name);
            UpdateParams params = new UpdateParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 更新conversation的置顶状态 */
    public void updateConversationTop(long groupId, boolean isTop) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IS_TOP, isTop ? 1 : 0);
            UpdateParams params = new UpdateParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 更新conversation的group_id */
    public void updateConversationId(long oldGroupId, long newGroupId) {
        GOIMConversation conversation = loadConversationById(oldGroupId);
        if (conversation == null) {
            return;
        }
        conversation.setNewGroupId(newGroupId);
        deleteConversationById(oldGroupId);
        insertConversation(conversation);
    }
}
