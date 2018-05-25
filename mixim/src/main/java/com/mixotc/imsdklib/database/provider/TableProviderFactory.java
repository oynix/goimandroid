package com.mixotc.imsdklib.database.provider;

import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

/**
 * Author   : xiaoyu
 * Date     : 2018/3/25 下午4:21
 * Version  : v1.0.0
 * Describe : IMTableProvider的生产工厂类。
 */

public class TableProviderFactory {

    private Map<Class<? extends BaseIMTableProvider>, Object> mProviderCache = new HashMap<>();
    private Context mContext;
    private String mUid;

    /**
     * 一个工厂只为一个用户的数据库生产data provider，所以uid是固定的。
     *
     * @param context context
     * @param uid     当前登录的用户UID
     */
    public TableProviderFactory(Context context, String uid) {
        mContext = context;
        mUid = uid;
    }

    /**
     * 生产TableProvider实例
     *
     * @param clazz 类型
     * @param <T>   生产类型
     * @return 返回实例，如果过程出现异常则返回null。
     */
    public <T extends BaseIMTableProvider> T createProvider(Class<T> clazz) {
        if (mProviderCache.containsKey(clazz)) {
            return (T) mProviderCache.get(clazz);
        } else {
            T result = null;
            try {
                Constructor<T> constructor = clazz.getDeclaredConstructor(Context.class, String.class);
                result = constructor.newInstance(mContext, mUid);
                mProviderCache.put(clazz, result);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
            return result;
        }
    }

}
