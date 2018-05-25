package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.SysMsgTable;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.database.table.SysMsgTable.MSG_TIME;
import static com.mixotc.imsdklib.database.table.SysMsgTable.TABLE_NAME;
import static com.mixotc.imsdklib.database.table.SysMsgTable.UNREAD;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:20
 * Version  : v1.0.0
 * Describe :
 */

public class SysMsgTableProvider extends BaseIMTableProvider {

    private static final String TAG = SysMsgTableProvider.class.getSimpleName();

    public SysMsgTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 替换一条系统消息 */
    public void replaceSysMsg(GOIMSystemMessage msg) {
        try {
            ContentValues contentValues = SysMsgTable.createContentValues(msg);
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
            Logger.d(TAG, "add system msg to db:" + msg.getMsgText());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 加载所有系统消息 */
    public List<GOIMSystemMessage> loadAllSysMsg() {
        List<GOIMSystemMessage> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, null, null, null, null, null);
            if (cursor == null) {
                return result;
            }
            while (cursor.moveToNext()) {
                GOIMSystemMessage msg = GOIMSystemMessage.createFromCursor(cursor);
                result.add(msg);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "load all system msg from db, count:" + result.size());
        return result;
    }

    /** 加载最后一条系统消息 */
    public GOIMSystemMessage loadLastSysMsg() {
        GOIMSystemMessage result = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, null, null, null, null, MSG_TIME + " DESC");
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                result = GOIMSystemMessage.createFromCursor(cursor);
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

    /** 获取未读系统消息数量 */
    public int getUnreadSysMsg() {
        int result = 0;
        Cursor cursor = null;
        try {
            cursor = mHelper.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME + " WHERE " + UNREAD + "=?", new String[]{String.valueOf(1)});
            if (cursor == null) {
                return 0;
            }
            if (cursor.moveToFirst()) {
                result = cursor.getInt(0);
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

    /** 将所有系统消息置为已读状态 */
    public void updateSysMsgToRead() {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(UNREAD, 0);
            UpdateParams params = new UpdateParams(TABLE_NAME, UNREAD + "=?", new String[]{String.valueOf(1)}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 清空系统消息 */
    public void clearSysMsg() {
        try {
            mHelper.clearTable(TABLE_NAME);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

}
