package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.InsertParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.FriendRequestTable;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.database.table.FriendRequestTable.REQ_TIME;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.RESPONSE;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.TABLE_NAME;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.USER_ID;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:19
 * Version  : v1.0.0
 * Describe :
 */

public class IMFriendReqTableProvider extends BaseIMTableProvider {

    private static final String TAG = IMFriendReqTableProvider.class.getSimpleName();

    public IMFriendReqTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 已存在该用户请求返回false，不存在返回true，返回值用来标记是否成功插入。 */
    public boolean addOrReplaceRequest(GOIMFriendRequest request) {
        boolean result = false;
        try {
            ContentValues contentValues = FriendRequestTable.createContentValues(request);
            UpdateParams updateParams = new UpdateParams(TABLE_NAME, USER_ID + "=?", new String[]{String.valueOf(request.getUid())}, contentValues);
            int update = mHelper.update(updateParams);
            result = update == 1;
            if (!result) {
                InsertParams insertParams = new InsertParams(TABLE_NAME, contentValues);
                mHelper.insert(insertParams);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        Logger.d(TAG, "add friend request to db:" + request.getUsername());
        return result;
    }

    /** 删除uid用户的添加好友请求 */
    public void deleteRequest(long uid) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, USER_ID + "=?", new String[]{String.valueOf(uid)});
            mHelper.delete(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 加载所有请求 */
    public List<GOIMFriendRequest> loadAllRequest() {
        List<GOIMFriendRequest> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, null, null, null, null, REQ_TIME + " DESC");
            if (cursor == null) {
                return result;
            }
            while (cursor.moveToNext()) {
                GOIMFriendRequest request = GOIMFriendRequest.createFromCursor(cursor);
                result.add(request);
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

    /** 更新好友请求回复状态 */
    public void updateRequestResponse(GOIMFriendRequest request) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(RESPONSE, request.getResponse().ordinal());
            UpdateParams params = new UpdateParams(TABLE_NAME, USER_ID + "=?", new String[]{String.valueOf(request.getUid())}, contentValues);
            mHelper.update(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 清空所有数据 */
    public void clearRequest() {
        try {
            mHelper.clearTable(TABLE_NAME);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
}
