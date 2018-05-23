package com.mixotc.imsdklib.database.table;

import android.provider.BaseColumns;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午8:07
 * Version  : v1.0.0
 * Describe :
 */

public class ConversationTable implements BaseColumns {

    public static final String TABLE_NAME = "conversation";

    public static final String GROUP_ID = "group_id";

    public static final String NAME = "name";

    public static final String IS_SINGLE = "is_single";

    public static final String IS_TOP = "is_top";

    public static final String LAST_MSG_TIME = "last_msg_time";

    public static final String LAST_MSG_TEXT = "last_msg_text";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                GROUP_ID +
                " INTEGER PRIMARY KEY," +
                NAME +
                " TEXT," +
                IS_SINGLE +
                " INTEGER," +
                IS_TOP +
                " INTEGER," +
                LAST_MSG_TIME +
                " INTEGER," +
                LAST_MSG_TEXT +
                " TEXT);";
    }
}
