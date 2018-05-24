package com.mixotc.imsdklib.database.params;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午4:51
 * Version  : v1.0.0
 * Describe : 操作数据库的参数类型的基类
 */

public class BaseDbOpParams {

    private String mTableName;

    BaseDbOpParams(String tableName) {
        this.mTableName = tableName;
    }

    public String getTableName() {
        return mTableName;
    }
}
