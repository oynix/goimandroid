package com.mixotc.imsdklib.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.mixotc.imsdklib.database.table.ChatTable;
import com.mixotc.imsdklib.database.table.ContactTable;
import com.mixotc.imsdklib.database.table.ConversationTable;
import com.mixotc.imsdklib.database.table.FriendRequestTable;
import com.mixotc.imsdklib.database.table.GroupTable;
import com.mixotc.imsdklib.database.table.SysMsgTable;
import com.mixotc.imsdklib.database.table.TempContactTable;
import com.mixotc.imsdklib.database.table.UnreadCountTable;
import com.mixotc.imsdklib.utils.Logger;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午5:54
 * Version  : v1.0.0
 * Describe : 用户IM操作数据库.
 */

public class DatabaseHelper extends BaseDatabaseHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    /**
     * 数据库名字的后缀。名字组成为$uid + 后缀名称。每个用户使用单独的数据库避免数据交错。
     */
    private static final String DB_NAME_SUFFIX = "_mixotcim.db";

    /**
     * 当前数据库的版本号，从1开始，每次升级数据库版本号顺次加1，并说明对应的app版本号和升级内容
     *
     * @version 1 v1.0.0 创建数据库和表
     */
    private static final int DB_VERSION_CODE = 1;

    public DatabaseHelper(Context context, String uid) {
        super(context, uid + DB_NAME_SUFFIX, DB_VERSION_CODE);
        SQLiteDatabase db;
        try {
            db = getWritableDatabase();
            // 如果更新失败则删除数据库重新创建，程序不能在错误的数据库上运行
            if (!mUpgradeSuccess) {
                if (db != null) {
                    db.close();
                }
                Logger.e(TAG, "数据库更新失败，删除后重现创建，数据库名：" + uid + DB_NAME_SUFFIX);
                context.deleteDatabase(uid + DB_NAME_SUFFIX);
                getWritableDatabase();
            }
        } catch (Exception e) {
            e.printStackTrace();
            context.deleteDatabase(uid + DB_NAME_SUFFIX);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();
            // 1. create chat table
            db.execSQL(ChatTable.CREATE_TABLE);
            // 2. create contact table
            db.execSQL(ContactTable.CREATE_TABLE);
            // 3. create conversation table
            db.execSQL(ConversationTable.CREATE_TABLE);
            // 4. create friend request table
            db.execSQL(FriendRequestTable.CREATE_TABLE);
            // 5. create group table
            db.execSQL(GroupTable.CREATE_TABLE);
            // 6. create system message table
            db.execSQL(SysMsgTable.CREATE_TABLE);
            // 7. create temporary contact table
            db.execSQL(TempContactTable.CREATE_TABLE);
            // 8. create unread count table
            db.execSQL(UnreadCountTable.CREATE_TABLE);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static String getDBNameWithId(long uid) {
        return uid + DB_NAME_SUFFIX;
    }
}
