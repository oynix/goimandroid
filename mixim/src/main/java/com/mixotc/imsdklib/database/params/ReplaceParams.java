package com.mixotc.imsdklib.database.params;

import android.content.ContentValues;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午4:56
 * Version  : v1.0.0
 * Describe :
 */

public class ReplaceParams extends InsertParams {

    public ReplaceParams(String tableName, ContentValues contentValues) {
        super(tableName, contentValues);
    }
}
