package com.mixotc.imsdklib.database.params;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午4:49
 * Version  : v1.0.0
 * Describe : 进行数据库的删除操作的参数
 */

public class DeleteParams extends BaseDbOpParams {

    private String mWhereClause;
    private String[] mValues;

    public DeleteParams(String tableName, String whereClause, String[] values) {
        super(tableName);
        this.mWhereClause = whereClause;
        this.mValues = values;
    }

    public String getWhereClause() {
        return mWhereClause;
    }

    public String[] getValues() {
        return mValues;
    }
}
