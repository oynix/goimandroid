package com.mixotc.imsdklib.database.table;

import android.provider.BaseColumns;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午7:34
 * Version  : v1.0.0
 * Describe :
 */

public class UnreadCountTable implements BaseColumns {

    /** 表名字 */
    public static final String TABLE_NAME = "unread_count";

    /** group id */
    public static final String GROUP_ID = "group_id";

    /** 数量 */
    public static final String COUNT = "count";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                GROUP_ID +
                " INTEGER PRIMARY KEY," +
                COUNT +
                " INTEGER);";
    }
}
