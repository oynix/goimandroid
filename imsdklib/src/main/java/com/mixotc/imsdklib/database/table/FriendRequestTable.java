package com.mixotc.imsdklib.database.table;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.mixotc.imsdklib.chat.GOIMFriendRequest;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午8:01
 * Version  : v1.0.0
 * Describe : 好友请求表
 */

public class FriendRequestTable implements BaseColumns {

    public static final String TABLE_NAME = "friend_request";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String AVATAR = "avatar";

    public static final String INFO = "info";

    public static final String REQ_TIME = "req_time";

    public static final String RESPONSE = "response";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                USER_ID +
                " INTEGER PRIMARY KEY," +
                USER_NAME +
                " TEXT," +
                AVATAR +
                " TEXT," +
                INFO +
                " TEXT," +
                REQ_TIME +
                " INTEGER," +
                RESPONSE +
                " INTEGER);";
    }

    public static ContentValues createContentValues(GOIMFriendRequest request) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_ID, request.getUid());
        contentValues.put(USER_NAME, request.getUsername());
        contentValues.put(AVATAR, request.getAvatar());
        contentValues.put(INFO, request.getRequestInfo());
        contentValues.put(REQ_TIME, request.getRequestTime());
        contentValues.put(RESPONSE, request.getResponse().ordinal());
        return contentValues;
    }
}
