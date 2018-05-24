package com.mixotc.imsdklib.database.table;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.MessageEncoder;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午6:55
 * Version  : v1.0.0
 * Describe : 消息表
 * 
 */

public class ChatTable implements BaseColumns {

    /** 表名字 */
    public static final String TABLE_NAME = "chat";

    /** 消息id */
    public static final String MSG_ID = "msg_id";

    /** 存储时间 */
    public static final String MSG_TIME = "msg_time";

    /** 消息方向：发送 or 接收 */
    public static final String MSG_DIR = "msg_dir";

    /** 参与者 */
    public static final String PARTICIPANT = "participant";

    /** 消息体 */
    public static final String MSG_BODY = "msg_body";

    /** group id */
    public static final String GROUP_ID = "group_id";

    /** 是否确认到服务器 */
    public static final String IS_ACK = "is_acked";

    /** 是否已发送 */
    public static final String IS_DELIVERED = "is_delivered";

    /** 消息状态 */
    public static final String STATUS = "status";

    /** 是否已听 */
    public static final String IS_LISTENED = "is_listened";
    
    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MSG_ID +
                " TEXT," +
                MSG_TIME +
                " INTEGER," +
                MSG_DIR +
                " INTEGER," +
                STATUS +
                " INTEGER," +
                PARTICIPANT +
                " INTEGER," +
                MSG_BODY +
                " TEXT NOT NULL," +
                GROUP_ID +
                " INTEGER," +
                IS_ACK +
                " INTEGER," +
                IS_DELIVERED +
                " INTEGER," +
                IS_LISTENED +
                " INTEGER);";
    }

    public static ContentValues createContentValues(GOIMMessage message) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(MSG_ID, message.getMsgId());
        contentValues.put(ChatTable.MSG_TIME, message.getMsgTime());
        contentValues.put(ChatTable.IS_ACK, message.isAcked());
        contentValues.put(ChatTable.IS_DELIVERED, message.isDelivered());
        contentValues.put(ChatTable.MSG_DIR, message.getDirect().ordinal());
        GOIMMessage.Status status = message.getStatus();
        if (status == GOIMMessage.Status.INPROGRESS) {
            status = GOIMMessage.Status.CREATE;
        }
        contentValues.put(ChatTable.STATUS, status.ordinal());
        contentValues.put(ChatTable.PARTICIPANT, message.getContact().getUid());
        contentValues.put(MSG_BODY, MessageEncoder.getJSONMsg(message));
        contentValues.put(ChatTable.GROUP_ID, message.getGroupId());
        contentValues.put(ChatTable.IS_LISTENED, message.isListened() ? 1 : 0);
        return contentValues;
    }

}
