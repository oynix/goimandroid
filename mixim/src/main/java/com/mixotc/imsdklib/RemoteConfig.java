package com.mixotc.imsdklib;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午3:21
 * Version  : v1.0.0
 * Describe : 参数配置
 */
public class RemoteConfig {
    public static final String HOST_URL = "http://192.168.113.26";
    public static final String AVATAR_DOWNLOAD_URL = HOST_URL + "/image/";
    public static final String AVATAR_UPLOAD_URL = HOST_URL + "/image/";
    public static final String FILE_DOWNLOAD_URL = HOST_URL + "/file/";
    public static final String FILE_UPLOAD_URL = HOST_URL + "/file/";
    public static final String HTTP_SALES_REQUEST_URL = HOST_URL + "/otc/sales/";
    public static final String HTTP_PAYMENT_REQUEST_URL = HOST_URL + "/otc/payment/";
    public static final String HTTP_COINS_REQUEST_URL = HOST_URL + "/otc/coins/";
    public static final String HTTP_PRICES_REQUEST_URL = HOST_URL + "/otc/prices/";
    public static final String HTTP_COIN_LIMIT_COUNT_URL = HOST_URL + "/otc/search/coin/?keyword=";

    public static final String SERVER_IP = "192.168.113.26";
    public static final int SERVER_PORT = 8000;

    public static final String PARAM_OF_THUMB = "?size=thumb";
    public static final String COUNTRY = "CN";

}
