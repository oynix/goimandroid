package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.GroupTable;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mixotc.imsdklib.database.table.GroupTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.GroupTable.GROUP_NAME;
import static com.mixotc.imsdklib.database.table.GroupTable.TABLE_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:19
 * Version  : v1.0.0
 * Describe :
 */

public class GroupTableProvider extends BaseIMTableProvider {

    private static final String TAG = GroupTableProvider.class.getSimpleName();

    public GroupTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 替换一个group */
    public void replaceGroup(GOIMGroup group) {
        try {
            ContentValues contentValues = GroupTable.createContentValues(group);
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
            Logger.d(TAG, "save group to db group name:" + group.getGroupName());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 替换一组group */
    public void replaceGroups(Collection<GOIMGroup> groups) {
        try {
            List<ReplaceParams> params = new ArrayList<>();
            for (GOIMGroup group : groups) {
                ContentValues contentValues = GroupTable.createContentValues(group);
                ReplaceParams param = new ReplaceParams(TABLE_NAME, contentValues);
                params.add(param);
            }
            mHelper.replace(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 重新存储groups，即将原表数据清除再存储 */
    public void restoreGroups(Collection<GOIMGroup> groups) {
        try {
            mHelper.clearTable(TABLE_NAME);
            replaceGroups(groups);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 加载所有的group, 不加载member */
    public List<GOIMGroup> loadAllGroups() {
        List<GOIMGroup> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                return result;
            }
            while (cursor.moveToNext()) {
                result.add(GOIMGroup.createGroupFromCursor(cursor));
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "load groups without members from db, count:" + result.size());
        return result;
    }

    /** 根据id删除group */
    public void deleteGroup(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            mHelper.delete(params);
            Logger.d(TAG, "delete group uid:" + groupId);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 根据id获取一个group */
    public GOIMGroup getGroupById(long groupId) {
        GOIMGroup result = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, null,null,null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                result = GOIMGroup.createGroupFromCursor(cursor);
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

    /** 更新指定group的名字 */
    public void updateGroupName(long groupId, String newName) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(GROUP_NAME, newName);
            UpdateParams params = new UpdateParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    public boolean containGroup(long gid) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, GROUP_ID + "=?", new String[]{String.valueOf(gid)}, null, null, null);
            if (cursor == null) {
                return false;
            }
            if (cursor.moveToFirst()) {
                result = true;
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
}
