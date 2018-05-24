package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.TempContactTable;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;

import static com.mixotc.imsdklib.database.table.TempContactTable.AVATAR;
import static com.mixotc.imsdklib.database.table.TempContactTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.TempContactTable.NICK_NAME;
import static com.mixotc.imsdklib.database.table.TempContactTable.TABLE_NAME;
import static com.mixotc.imsdklib.database.table.TempContactTable.TEMP_ID;
import static com.mixotc.imsdklib.database.table.TempContactTable.USER_ID;
import static com.mixotc.imsdklib.database.table.TempContactTable.USER_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:20
 * Version  : v1.0.0
 * Describe :
 */

public class IMTempContactTableProvider extends BaseIMTableProvider {

    private static final String TAG = IMTempContactTableProvider.class.getSimpleName();

    public IMTempContactTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 替换一个临时联系人 */
    public void replaceTempContact(GOIMContact contact) {
        try {
            ContentValues contentValues = TempContactTable.createContentValues(contact);
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
            Logger.d(TAG, "add contact to temp db:" + contact.toString());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 替换一组联系人 */
    public void replaceTempContacts(Collection<GOIMContact> contacts) {
        try {
            List<ReplaceParams> params = new ArrayList<>();
            for (GOIMContact contact : contacts) {
                ContentValues contentValues = TempContactTable.createContentValues(contact);
                ReplaceParams param = new ReplaceParams(TABLE_NAME, contentValues);
                params.add(param);
            }
            mHelper.replace(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 更新一个临时联系人 */
//    public void updateTempContact(GOIMContact contact) {
//        try {
//            ContentValues contentValues = TempContactTable.createContentValues(contact);
//            UpdateParams params = new UpdateParams(TABLE_NAME, TEMP_ID + "=?", new String[]{contact.getUid() + "|" + contact.getGroupId()}, contentValues);
//            mHelper.update(params);
//            Logger.d(TAG, "update contact to temp db:" + contact.toString());
//        } catch (DatabaseException e) {
//            e.printStackTrace();
//        }
//    }

    /** 删除一个临时联系人 */
//    public void deleteTempContact(long uid) {
//        try {
//            DeleteParams params = new DeleteParams(TABLE_NAME, USER_ID + "=?", new String[]{String.valueOf(-uid)});
//            mHelper.delete(params);
//            Logger.d(TAG, "delete temp contact uid:" + uid);
//        } catch (DatabaseException e) {
//            e.printStackTrace();
//        }
//    }

    /** 根据groupId加载一组临时联系人 */
    public Hashtable<Long, GOIMContact> loadTempContactsByGroupId(long groupId) {
        return loadMembersByGroupId(groupId, null);
    }

    /**
     * 加载group id为传入的group id并且uid存在于memberStr中的contact，
     * memberStr为空时，加载全部group id为传入group id的contact。
     */
    public Hashtable<Long, GOIMContact> loadMembersByGroupId(long groupId, String memberStr) {
        Hashtable<Long, GOIMContact> result = new Hashtable<>();
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, GROUP_ID + "=?", new String[]{String.valueOf(groupId)}, null, null, null);
            if (cursor == null) {
                return result;
            }
            boolean loadAll = false;
            if (TextUtils.isEmpty(memberStr)) {
                loadAll = true;
            }
            while (cursor.moveToNext()) {
                GOIMContact contact = GOIMContact.createFromTempCursor(cursor);
                if (loadAll || memberStr.contains(String.valueOf(contact.getUid()))) {
                    result.put(contact.getUid(), contact);
                }
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

    /** 删除group id为传入值的所有contact */
    public void deleteContactByGroupId(long groupId) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, GROUP_ID + "=?", new String[]{String.valueOf(groupId)});
            mHelper.delete(params);
            Logger.d(TAG, "delete member of group id:" + groupId);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 更新联系人的基本信息 */
    public void updateContactBaseInfo(GOIMContact contact) {
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_NAME, contact.getUsername());
            contentValues.put(NICK_NAME, contact.getNick());
            contentValues.put(AVATAR, contact.getAvatar());
            UpdateParams params = new UpdateParams(TABLE_NAME, USER_ID + "=?", new String[]{String.valueOf(contact.getUid())}, contentValues);
            mHelper.update(params);
            Logger.d(TAG, "update temp contact base info to db:" + contact.toString());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 获取一个陌生联系人 groupId  uid **/
    public GOIMContact getTempContactById(long uid, long groupId) {
        GOIMContact result = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, USER_ID + "=? AND " + GROUP_ID + "=?", new String[]{String.valueOf(uid), String.valueOf(groupId)}, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                result = GOIMContact.createFromTempCursor(cursor);
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

    /** 单聊、被删，temp表中还保留着记录 */
    public GOIMContact getExContactById(long uid) {
        GOIMContact result = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, USER_ID + "=?", new String[]{String.valueOf(uid)}, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                result = GOIMContact.createFromTempCursor(cursor);
                result.setGroupId(-uid);
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

    /** clear table */
    public void clear() {
        try {
            mHelper.clearTable(TABLE_NAME);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 删除某个group里的用户 */
    public void deleteGroupMember(long groupId, List<Long> users) {
        try {
            List<DeleteParams> params = new ArrayList<>();
            for (Long user : users) {
                DeleteParams param = new DeleteParams(TABLE_NAME, TEMP_ID + "=?", new String[]{user + "|" + groupId});
                params.add(param);
            }
            mHelper.delete(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }
}
