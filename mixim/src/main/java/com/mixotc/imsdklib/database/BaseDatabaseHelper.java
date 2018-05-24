package com.mixotc.imsdklib.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.InsertParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午3:56
 * Version  : v1.0.0
 * Describe : 所有数据库的Helper都要继承此类，此类封装了所有数据库的基本操作。
 *
 * v1.0.0 只有一个数据库，即一个子类：用户IM操作数据库
 */

public abstract class BaseDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = BaseDatabaseHelper.class.getSimpleName();

    protected boolean mUpgradeSuccess = true;

    public BaseDatabaseHelper(Context context, String name, int version) {
        super(context, name, null, version);
    }

    public void closeDb() {
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                db.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 插入数据 */
    public void insert(InsertParams params) throws DatabaseException {
        DatabaseUtils.insert(this, params);
    }

    /** 插入一组数据 */
    public void insert(List<InsertParams> params) throws DatabaseException {
        DatabaseUtils.insert(this, params);
    }

    /** 查询数据 */
    public Cursor query(String tableName, String[] projection, String selection, String[] selectionArgs,
                        String groupBy, String having, String sortOrder) throws DatabaseException {
        return DatabaseUtils.query(this, tableName, projection, selection, selectionArgs, groupBy, having, sortOrder);
    }

    /** 更新数据 */
    public int update(UpdateParams params) throws DatabaseException {
        return DatabaseUtils.update(this, params);
    }

    /** 删除数据 */
    public int delete(DeleteParams params) throws DatabaseException {
        return DatabaseUtils.delete(this, params);
    }

    /** 删除数据 */
    public void delete(List<DeleteParams> params) throws DatabaseException {
        DatabaseUtils.delete(this, params);
    }

    /** 原生SQL语句查询 */
    public Cursor rawQuery(String sql, String[] argues) throws DatabaseException {
        return DatabaseUtils.rawQuery(this, sql, argues);
    }

    /** 替换数据 */
    public void replace(ReplaceParams params) throws DatabaseException {
        DatabaseUtils.replace(this, params);
    }

    /** 替换一组数据 */
    public void replace(List<ReplaceParams> params) throws DatabaseException {
        DatabaseUtils.replace(this, params);
    }

    /** 清空一张表的数据 */
    public void clearTable(String tableName) throws DatabaseException {
        DatabaseUtils.clearTable(this, tableName);
    }
}
