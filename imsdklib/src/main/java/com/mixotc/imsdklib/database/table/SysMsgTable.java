package com.mixotc.imsdklib.database.table;

import android.provider.BaseColumns;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午8:15
 * Version  : v1.0.0
 * Describe :
 */

public class SysMsgTable implements BaseColumns {

    public static final String TABLE_NAME = "system_msg";

    public static final String MSG_TIME = "msg_time";

    public static final String MSG_TEXT = "msg_text";

    public static final String MSG_ATTR = "msg_attribute";

    public static final String MSG_TYPE = "msg_type";

    public static final String UNREAD = "unread";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                _ID +
                " INTEGER PRIMARY KEY AUTOINCREMENT," +
                MSG_TIME +
                " INTEGER," +
                MSG_TEXT +
                " TEXT NOT NULL," +
                MSG_ATTR +
                " TEXT," +
                MSG_TYPE +
                " INTEGER," +
                UNREAD +
                " INTEGER);";
    }

//    public static ContentValues createContentValues(GOIMSystemMessage systemMessage) {
//        ContentValues contentValues = new ContentValues();
//        contentValues.put(MSG_TIME, systemMessage.getMsgTime());
//        contentValues.put(MSG_TEXT, systemMessage.getMsgText());
//        contentValues.put(MSG_ATTR, systemMessage.getMsgAttributes().toString());
//        contentValues.put(MSG_TYPE, systemMessage.getMsgType().ordinal());
//        contentValues.put(UNREAD, systemMessage.unRead() ? 1 : 0);
//        return contentValues;
//    }

}
