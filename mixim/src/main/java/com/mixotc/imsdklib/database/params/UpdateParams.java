package com.mixotc.imsdklib.database.params;

import android.content.ContentValues;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午5:02
 * Version  : v1.0.0
 * Describe : 进行数据库更新操作时的参数
 */

public class UpdateParams extends DeleteParams {

    private ContentValues mContentValues;

    public UpdateParams(String tableName, String whereClause, String[] values, ContentValues contentValues) {
        super(tableName, whereClause, values);
        mContentValues = contentValues;
    }

    public ContentValues getContentValues() {
        return mContentValues;
    }
}