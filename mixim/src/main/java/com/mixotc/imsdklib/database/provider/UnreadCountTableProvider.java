package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;

import static com.mixotc.imsdklib.database.table.UnreadCountTable.COUNT;
import static com.mixotc.imsdklib.database.table.UnreadCountTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.UnreadCountTable.TABLE_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:21
 * Version  : v1.0.0
 * Describe :
 */

public class UnreadCountTableProvider extends BaseIMTableProvider {
    public UnreadCountTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 根据group_id清除对应的row */
    public void deleteById(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            mHelper.delete(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 替换一条数据 */
    public void replaceUnreadCount(long groupId, int count) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GROUP_ID, groupId);
            contentValues.put(COUNT, count);
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 根据group_id查询未读数量 */
    public int getCountById(long groupId) {
        int result = 0;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, new String[]{COUNT}, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, null, null, null);
            if (cursor == null) {
                return 0;
            }
            if (cursor.moveToFirst()) {
                result = cursor.getInt(cursor.getColumnIndex(COUNT));
                if (result < 0)
                    result = 0;
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

    /** 将未读数量增加1 */
    public void increaseUnreadCount(long groupId) {
        replaceUnreadCount(groupId, getCountById(groupId) + 1);
    }

    /** 替换groupId, 因为是主键，所以先删除再插入 */
    public void updateUnreadGroupId(long oldId, long newId) {
        int count = getCountById(oldId);
        if (count > 0) {
            replaceUnreadCount(newId, count);
        }
    }
}
