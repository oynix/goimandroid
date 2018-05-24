package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.InsertParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.ChatTable;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.MessageEncoder;
import com.mixotc.imsdklib.message.TransferMessageBody;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.database.table.ChatTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.ChatTable.IS_DELIVERED;
import static com.mixotc.imsdklib.database.table.ChatTable.IS_LISTENED;
import static com.mixotc.imsdklib.database.table.ChatTable.MSG_BODY;
import static com.mixotc.imsdklib.database.table.ChatTable.MSG_ID;
import static com.mixotc.imsdklib.database.table.ChatTable.MSG_TIME;
import static com.mixotc.imsdklib.database.table.ChatTable.STATUS;
import static com.mixotc.imsdklib.database.table.ChatTable.TABLE_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:18
 * Version  : v1.0.0
 * Describe :
 */

public class IMChatTableProvider extends BaseIMTableProvider {

    private static final String TAG = IMChatTableProvider.class.getSimpleName();

    public IMChatTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 插入一条消息 */
    public boolean insertMessage(GOIMMessage message) {
        boolean result = false;
        if (isMsgExist(message)) {
            return false;
        }
        ContentValues contentValues = ChatTable.createContentValues(message);
        InsertParams params = new InsertParams(TABLE_NAME, contentValues);
        try {
            mHelper.insert(params);
            result = true;
            Logger.d(TAG, "save msg to db");
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /** 查看某条消息是否存在，根据消息id判断 */
    public boolean isMsgExist(GOIMMessage msg) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, MSG_ID + "=?", new String[]{msg.getMsgId()}, null, null, null);
            if (cursor.moveToFirst()) {
                result = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return result;
    }

    /** 更新消息内容 */
    public boolean updateMsgBody(GOIMMessage message) {
        boolean result = false;
        try {
            ContentValues contentValues = new ContentValues();
            String msgId = message.getMsgId();
            String msgBody = MessageEncoder.getJSONMsg(message);
            contentValues.put(MSG_BODY, msgBody);
            UpdateParams params = new UpdateParams(TABLE_NAME, MSG_ID + "=?", new String[]{msgId}, contentValues);
            mHelper.update(params);
            Logger.d(TAG, "update msg:" + msgId + " message body:" + msgBody);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /** 删除消息 */
    public void deleteMessage(String msgId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, MSG_ID + "=?", new String[]{msgId});
            int affected = mHelper.delete(params);
            Logger.d(TAG, "delete msg:" + msgId + " return:" + affected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 加载一条消息 */
    public GOIMMessage loadMessage(String msgId) {
        GOIMMessage message = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, MSG_ID + "=?", new String[]{msgId}, null, null, null);
            if (cursor.moveToFirst()) {
                message = GOIMMessage.createFromCursor(cursor);
                Logger.d(TAG, "load msg msgId:" + msgId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return message;
    }

    /** 删除一个对话的所有消息 */
    public void deleteConversationMsg(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            int affected = mHelper.delete(params);
            Logger.d(TAG, "delete chat msgs with:" + groupId + " return:" + affected);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新一条消息的收听状态 */
    public void updateMsgListen(String msgId, boolean listened) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IS_LISTENED, listened ? 1 : 0);
            UpdateParams params = new UpdateParams(TABLE_NAME, MSG_ID + "=?", new String[]{msgId}, contentValues);
            mHelper.update(params);
            Logger.d(TAG, "update msg:" + msgId + " isListened:" + listened);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新一条消息的发送状态 */
    public void updateMsgDelivered(String msgId, boolean delivered) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(IS_DELIVERED, delivered ? 1 : 0);
            UpdateParams params = new UpdateParams(TABLE_NAME, MSG_ID + "=?", new String[]{msgId}, contentValues);
            mHelper.update(params);
            Logger.d(TAG, "update msg:" + msgId + " delivered:" + delivered);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新一条消息的状态 */
    public void updateMsgStatus(String msgId, int newStatus) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(STATUS, newStatus);
            UpdateParams params = new UpdateParams(TABLE_NAME, MSG_ID + "=?", new String[]{msgId}, contentValues);
            mHelper.update(params);
            Logger.d(TAG, "update msg:" + msgId + " status:" + newStatus);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 将所有group_id值为oldGroupId的行，更新为newGroupId */
    public void updateMsgGroupId(long oldGroupId, long newGroupId) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GROUP_ID, newGroupId);
            UpdateParams params = new UpdateParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(oldGroupId)}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 根据groupId获取消息数量 */
    public int getMsgCountById(long groupId) {
        int result = 0;
        Cursor cursor;
        try {
            String sql = "SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + GROUP_ID + "=?;";
            cursor = mHelper.rawQuery(sql, new String[]{String.valueOf(groupId)});
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 从属于groupId的消息列中，从lastMsgId的位置，加载数量为count的消息，以消息时间descending排列。
     *
     * @param groupId   查询的组
     * @param lastMsgId 最后一条消息id，即本次查询的基准，如果为空则由最新消息开始查询
     * @param count     加载的数量
     * @return 查询结果
     */
    public List<GOIMMessage> loadMsgByLastId(long groupId, String lastMsgId, int count) {
        Logger.e(TAG, "load message by last id invoked");
        List<GOIMMessage> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            String sql;
            String[] whereArgs;
            if (!TextUtils.isEmpty(lastMsgId)) {
                GOIMMessage message = loadMessage(lastMsgId);
                if (message == null) {
                    return result;
                }
                sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID + "=?" + " AND " + MSG_TIME + "<?" + " ORDER BY " + MSG_TIME + " DESC LIMIT " + count + ";";
                whereArgs = new String[]{String.valueOf(groupId), String.valueOf(message.getMsgTime())};
            } else {
                sql = "SELECT * FROM " + TABLE_NAME + " WHERE " + GROUP_ID + "=?" + " ORDER BY " + MSG_TIME + " DESC LIMIT " + count + ";";
                whereArgs = new String[]{String.valueOf(groupId)};
            }
            cursor = mHelper.rawQuery(sql, whereArgs);
            if (cursor == null) {
                return result;
            }
            while (cursor.moveToNext()) {
                GOIMMessage message = GOIMMessage.createFromCursor(cursor);
                if (message == null) {
                    continue;
                }
                Logger.e(TAG, "add message to list:" + message.toString());
                result.add(0, message);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "load msgs size:" + result.size() + " for groupid:" + groupId);
        return result;
    }

    /** 加载转账消息 */
    public GOIMMessage loadTransferMsg(long groupId, long transferId) {
        String typeWords = "\"type\":\"transfer\"";
        String idWords = "\"id\":" + transferId;
        Cursor cursor = null;
        GOIMMessage message = null;

        try {
            cursor = mHelper.rawQuery("SELECT * FROM " + TABLE_NAME +
                            " WHERE " + GROUP_ID + " = ? AND " + MSG_BODY +
                            " LIKE '%" + typeWords + "%' AND " + MSG_BODY +
                            " LIKE '%" + idWords + "%' ORDER BY " + MSG_TIME +
                            " DESC",
                    new String[]{String.valueOf(groupId)});
            if (cursor == null) {
                return null;
            }
            while (cursor.moveToNext()) {
                GOIMMessage msg = GOIMMessage.createFromCursor(cursor);
                TransferMessageBody body = (TransferMessageBody) msg.getBody();
                if (body.getTransferId() == transferId) {
                    message = msg;
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return message;
    }

    /** 删除group id为传入group id的所有message */
    public void deleteMsgByGroupId(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            mHelper.delete(params);
            Logger.d(TAG, "delete chat of group id:" + groupId);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
}
