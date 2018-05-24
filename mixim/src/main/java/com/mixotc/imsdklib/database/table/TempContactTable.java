package com.mixotc.imsdklib.database.table;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.mixotc.imsdklib.chat.GOIMContact;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午7:43
 * Version  : v1.0.0
 * Describe : 临时联系人表
 * <p>
 */

public class TempContactTable implements BaseColumns {

    public static final String TABLE_NAME = "temp_contacts";

    public static final String TEMP_ID = "temp_id";

    public static final String USER_ID = "user_id";

    public static final String USER_NAME = "user_name";

    public static final String NICK_NAME = "nick_name";

    public static final String GROUP_ID = "group_id";

    public static final String AVATAR = "avatar";

    public static final String PHONE = "phone";

    public static final String EMAIL = "email";

    public static final String GENDER = "gender";

    public static final String REGION = "region";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                TEMP_ID +
                " TEXT PRIMARY KEY," +
                USER_ID +
                " INTEGER," +
                USER_NAME +
                " TEXT," +
                NICK_NAME +
                " TEXT," +
                GROUP_ID +
                " INTEGER," +
                AVATAR +
                " TEXT," +
                PHONE +
                " TEXT," +
                EMAIL +
                " TEXT," +
                GENDER +
                " INTEGER," +
                REGION +
                " TEXT);";
    }

    public static ContentValues createContentValues(GOIMContact contact) {
        ContentValues contentValues = new ContentValues();
        if (contact != null) {
            contentValues.put(TEMP_ID, contact.getUid() + "|" + contact.getGroupId());
            contentValues.put(USER_ID, contact.getUid());
            contentValues.put(USER_NAME, contact.getUsername());
            contentValues.put(NICK_NAME, contact.getNick());
            contentValues.put(GROUP_ID, contact.getGroupId() == -1 ? -contact.getUid() : contact.getGroupId());
            contentValues.put(AVATAR, contact.getAvatar());
            contentValues.put(PHONE, contact.getPhone());
            contentValues.put(EMAIL, contact.getEmail());
            contentValues.put(GENDER, contact.getGender());
            contentValues.put(REGION, contact.getRegion());
        }
        return contentValues;
    }
}
