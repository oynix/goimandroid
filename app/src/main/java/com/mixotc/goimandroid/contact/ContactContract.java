package com.mixotc.goimandroid.contact;

import com.mixotc.imsdklib.chat.GOIMContact;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午2:16
 * Version  : v1.0.0
 * Describe :
 */
public interface ContactContract {
    interface View {
        void showListData(List<GOIMContact> data);
    }

    interface Presenter {
        void onCreate();
    }

    interface Model {
        List<GOIMContact> getContacts();
    }
}
