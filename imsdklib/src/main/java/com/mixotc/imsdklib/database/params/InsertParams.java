package com.mixotc.imsdklib.database.params;

import android.content.ContentValues;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午4:44
 * Version  : v1.0.0
 * Describe : 进行数据库插入操作的参数
 */

public class InsertParams extends BaseDbOpParams {

    private ContentValues mContentValues;

    public InsertParams(String tableName, ContentValues contentValues) {
        super(tableName);
        mContentValues = contentValues;
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }
}
