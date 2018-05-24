package com.mixotc.imsdklib.database.table;

import android.content.ContentValues;
import android.provider.BaseColumns;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMGroup;

import java.util.Collection;
import java.util.Iterator;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午7:17
 * Version  : v1.0.0
 * Describe : 群组表。两个人也算作是一个gourp
 */

public class GroupTable implements BaseColumns {

    /** 表名字 */
    public static final String TABLE_NAME = "groups";

    /** group id */
    public static final String GROUP_ID = "group_id";

    /** 群组名称 */
    public static final String GROUP_NAME = "group_name";

    /** 群主 */
    public static final String OWNER = "owner";

    /** 群描述 */
    public static final String DESC = "desc";

    /** 成员 */
    public static final String MEMBERS = "members";

    /** 修改时间 */
    public static final String MODIFY_TIME = "modify_time";

    /** 是否锁定 */
    public static final String IS_BLOCK = "is_block";

    /** 最大成员 */
    public static final String MAX_USERS = "max_users";

    /** 是否时单聊 */
    public static final String IS_SINGLE = "is_single";

    public static final String CREATE_TABLE;

    static {
        CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " +
                TABLE_NAME +
                " (" +
                GROUP_ID +
                " INTEGER PRIMARY KEY," +
                GROUP_NAME +
                " TEXT NOT NULL," +
                OWNER +
                " INTEGER," +
                DESC +
                " TEXT," +
                MODIFY_TIME +
                " INTEGER," +
                MEMBERS +
                " TEXT," +
                IS_BLOCK +
                " INTEGER," +
                MAX_USERS +
                " INTEGER," +
                IS_SINGLE +
                " INTEGER);";
    }

    public static ContentValues createContentValues(GOIMGroup group) {
        StringBuilder memberStr = new StringBuilder();
        Collection<GOIMContact> members = group.getMembers();
        Iterator iterator = members.iterator();
        while (iterator.hasNext()) {
            GOIMContact contact = (GOIMContact) iterator.next();
            memberStr.append(contact.getUid());
            if (iterator.hasNext()) {
                memberStr.append("|");
            }
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(GROUP_ID, group.getGroupId());
        contentValues.put(GROUP_NAME, group.getGroupName());
        contentValues.put(DESC, group.getDescription());
        contentValues.put(OWNER, group.getOwner());
        contentValues.put(MEMBERS, memberStr.toString());
        contentValues.put(MODIFY_TIME, group.getLastModifiedTime());
        contentValues.put(IS_BLOCK, group.isMsgBlocked());
        contentValues.put(MAX_USERS, group.getMaxUsers());
        contentValues.put(IS_SINGLE, group.isSingle());
        return contentValues;
    }
}
