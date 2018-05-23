package com.mixotc.imsdklib.database;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/24 下午5:34
 * Version  : v1.0.0
 * Describe :
 */

public class DatabaseException extends Exception {

    public DatabaseException(Exception e) {
        super(e);
    }

    public DatabaseException(String msg) {
        super(msg);
    }

}
