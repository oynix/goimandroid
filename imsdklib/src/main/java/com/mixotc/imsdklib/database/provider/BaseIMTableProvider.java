package com.mixotc.imsdklib.database.provider;

import android.content.Context;

import com.mixotc.imsdklib.database.IMDatabaseHelper;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:10
 * Version  : v1.0.0
 * Describe : 所有IM数据提供类都要继承此类。每个table对应一个独立的provider，每个provider操作与之对应的表数据。
 */

public class BaseIMTableProvider {

    IMDatabaseHelper mHelper;

    public BaseIMTableProvider(Context context, String uid) {
        mHelper = new IMDatabaseHelper(context, uid);
    }

    public void closeDb() {
        mHelper.closeDb();
    }
}
