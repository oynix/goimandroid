package com.mixotc.imsdklib.database.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.database.DatabaseException;
import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.InsertParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;
import com.mixotc.imsdklib.database.table.ContactTable;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.mixotc.imsdklib.database.table.ContactTable.AVATAR;
import static com.mixotc.imsdklib.database.table.ContactTable.NICK_NAME;
import static com.mixotc.imsdklib.database.table.ContactTable.TABLE_NAME;
import static com.mixotc.imsdklib.database.table.ContactTable.USER_ID;
import static com.mixotc.imsdklib.database.table.ContactTable.USER_NAME;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:18
 * Version  : v1.0.0
 * Describe : 联系人表数据操作类
 */

public class ContactTableProvider extends BaseIMTableProvider {

    private static final String TAG = ContactTableProvider.class.getSimpleName();

    public ContactTableProvider(Context context, String uid) {
        super(context, uid);
    }

    /** 重新储存联系人，即将原有的全部删除，再重新添加。 */
    public void restoreContacts(Collection<GOIMContact> contacts) {
        try {
            mHelper.clearTable(TABLE_NAME);
            insertContacts(contacts);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 插入一个联系人 */
    public void replaceContact(GOIMContact contact) {
        try {
            ContentValues contentValues = ContactTable.createContentValues(contact);
            ReplaceParams params = new ReplaceParams(TABLE_NAME, contentValues);
            mHelper.replace(params);
            Logger.e(TAG, "add contact to db:" + contact.toString());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 插入一组联系人 */
    public void insertContacts(Collection<GOIMContact> contacts) {
        try {
            List<InsertParams> params = new ArrayList<>();
            for (GOIMContact contact : contacts) {
                ContentValues contentValues = ContactTable.createContentValues(contact);
                InsertParams param = new InsertParams(TABLE_NAME, contentValues);
                params.add(param);
            }
            mHelper.insert(params);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 删除一个联系人 */
    public void deleteContact(long uid) {
        try {
            DeleteParams params = new DeleteParams(TABLE_NAME, USER_ID + "=?",
                    new String[]{String.valueOf(uid)});
            mHelper.delete(params);
            Logger.e(TAG, "delete contact uid:" + uid);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /** 加载所有联系人 */
    public List<GOIMContact> loadAllContacts() {
        List<GOIMContact> result = new ArrayList<>();
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, null, null,
                    null, null, null);
            if (cursor == null) {
                return result;
            }
            while (cursor.moveToNext()) {
                GOIMContact contact = GOIMContact.createFromCursor(cursor);
                result.add(contact);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "loaded contacts from db:" + result.size());
        return result;
    }

    /** 更新联系人的基本信息 */
    public int updateContactBaseInfo(GOIMContact contact) {
        int rowsAffected = 0;
        try {
            ContentValues contentValues = new ContentValues();
            contentValues.put(USER_NAME, contact.getUsername());
            contentValues.put(NICK_NAME, contact.getNick());
            contentValues.put(AVATAR, contact.getAvatar());
            UpdateParams params = new UpdateParams(TABLE_NAME, USER_ID + "=?",
                    new String[]{String.valueOf(contact.getUid())}, contentValues);
            rowsAffected = mHelper.update(params);
            Logger.e(TAG, "update contact base info to db:" + contact.toString());
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
        return rowsAffected;
    }

    /** 获取一个联系人 */
    public GOIMContact getContactById(long uid) {
        GOIMContact contact = null;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null,USER_ID + "=?",
                    new String[]{String.valueOf(uid)}, null, null, null);
            if (cursor == null) {
                return null;
            }
            if (cursor.moveToFirst()) {
                contact = GOIMContact.createFromCursor(cursor);
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return contact;
    }

    /** 是否包含某个user */
    public boolean containContact(long uid) {
        boolean result = false;
        Cursor cursor = null;
        try {
            cursor = mHelper.query(TABLE_NAME, null, USER_ID + "=?",
                    new String[]{String.valueOf(uid)}, null, null, null);
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
