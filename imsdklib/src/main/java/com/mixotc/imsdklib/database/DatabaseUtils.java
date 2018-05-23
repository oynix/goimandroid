package com.mixotc.imsdklib.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.mixotc.imsdklib.database.params.DeleteParams;
import com.mixotc.imsdklib.database.params.InsertParams;
import com.mixotc.imsdklib.database.params.ReplaceParams;
import com.mixotc.imsdklib.database.params.UpdateParams;

import java.io.File;
import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午5:14
 * Version  : v1.0.0
 * Describe : 对数据库进行增删改查操作的工具类。
 */

public final class DatabaseUtils {

    private DatabaseUtils() {
        throw new IllegalStateException("Don't instantiate me, too young to simple");
    }

    /**
     * 向数据库的表里插入一条数据
     *
     * @param helper SQLiteOpenHelper instance.
     * @param params data to inserted.
     * @throws DatabaseException when exception occurs.
     */
    public static void insert(SQLiteOpenHelper helper, InsertParams params) throws DatabaseException {
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.beginTransaction();
            database.insert(params.getTableName(), null, params.getContentValues());
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
    }

    /**
     * 向数据库的表里插入一组数据
     *
     * @param helper SQLiteOpenHelper instance.
     * @param params data to inserted.
     * @throws DatabaseException when exception occurs.
     */
    public static void insert(SQLiteOpenHelper helper, List<InsertParams> params) throws DatabaseException {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            for (InsertParams param : params) {
                db.insert(param.getTableName(), null, param.getContentValues());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 从数据库的表删除数据
     *
     * @param helper SQLiteOpenHelper instance.
     * @param params for delete operation.
     * @throws DatabaseException when exception occurs.
     */
    public static int delete(SQLiteOpenHelper helper, DeleteParams params) throws DatabaseException {
        int result = 0;
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.beginTransaction();
            result = database.delete(params.getTableName(), params.getWhereClause(), params.getValues());
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return result;
    }

    /**
     * 从数据库的表删除数据
     *
     * @param helper SQLiteOpenHelper instance.
     * @param params for delete operation.
     * @throws DatabaseException when exception occurs.
     */
    public static void delete(SQLiteOpenHelper helper, List<DeleteParams> params) throws DatabaseException {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            for (DeleteParams param : params) {
                db.delete(param.getTableName(), param.getWhereClause(), param.getValues());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 更新数据库表中的数据
     *
     * @param helper SQLiteDatabase instance.
     * @param params for update operation.
     * @throws DatabaseException when exception occurs.
     */
    public static int update(SQLiteOpenHelper helper, UpdateParams params) throws DatabaseException {
        int result = 0;
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.beginTransaction();
            result = database.update(params.getTableName(), params.getContentValues(), params.getWhereClause(), params.getValues());
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
        return result;
    }

    /**
     * 替换数据库表中的一条数据
     *
     * @param helper SQLiteDatabase instance.
     * @param params for replace operation.
     * @throws DatabaseException when exception occurs.
     */
    public static void replace(SQLiteOpenHelper helper, ReplaceParams params) throws DatabaseException {
        SQLiteDatabase database = null;
        try {
            database = helper.getWritableDatabase();
            database.beginTransaction();
            database.replace(params.getTableName(), null, params.getContentValues());
            database.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (database != null) {
                database.endTransaction();
            }
        }
    }

    public static void replace(SQLiteOpenHelper helper, List<ReplaceParams> params) throws DatabaseException {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            for (ReplaceParams param : params) {
                db.replace(param.getTableName(), null, param.getContentValues());
            }
            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 从数据库中查询数据
     *
     * @param helper        SQLiteDatabase instance.
     * @param tableName     表名字
     * @param projection    查询的列
     * @param selection     查询条件
     * @param selectionArgs 查询条件的值
     * @param groupBy       分组条件
     * @param having        筛选条件
     * @param sortOrder     排序条件
     * @return cursor
     */
    public static Cursor query(SQLiteOpenHelper helper, String tableName, String[] projection,
                               String selection, String[] selectionArgs,
                               String groupBy, String having, String sortOrder) throws DatabaseException {
        Cursor result = null;
        try {
            SQLiteDatabase db = helper.getReadableDatabase();
            result = db.query(tableName, projection, selection, selectionArgs, groupBy, having, sortOrder);
        } catch (SQLException | IllegalStateException e) {
            throw new DatabaseException(e);
        }
        return result;
    }

    public static Cursor rawQuery(SQLiteOpenHelper helper, String sql, String[] argues) throws DatabaseException {
        try {
            SQLiteDatabase db = helper.getWritableDatabase();
            return db.rawQuery(sql, argues);
        } catch (Exception e) {
            throw new DatabaseException(e);
        }
    }

    /**
     * 清空一张表的数据
     *
     * @param helper    SQLiteDatabase instance.
     * @param tableName 表名字
     * @throws DatabaseException when exception occurs.
     */
    public static void clearTable(SQLiteOpenHelper helper, String tableName) throws DatabaseException {
        SQLiteDatabase db = null;
        try {
            db = helper.getWritableDatabase();
            db.beginTransaction();
            db.execSQL("DELETE FROM " + tableName);
            db.setTransactionSuccessful();
        } catch (Exception e) {
            throw new DatabaseException(e);
        } finally {
            if (db != null) {
                db.endTransaction();
            }
        }
    }

    /**
     * 判断某个数据库是否存在
     *
     * @param context context
     * @param path    path
     */
    public static boolean isDatabaseExist(Context context, String path) {
        boolean result = false;
        try {
            File databasePath = context.getDatabasePath(path);
            result =  databasePath.exists();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
