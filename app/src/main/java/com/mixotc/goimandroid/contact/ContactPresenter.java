package com.mixotc.goimandroid.contact;

import com.mixotc.imsdklib.chat.GOIMContact;

import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午2:20
 * Version  : v1.0.0
 * Describe :
 */
public class ContactPresenter implements ContactContract.Presenter {

    private ContactContract.View mView;
    private ContactContract.Model mModel;

    public ContactPresenter(ContactContract.View view, ContactContract.Model model) {
        mView = view;
        mModel = model;
    }

    @Override
    public void onCreate() {
        List<GOIMContact> contacts = mModel.getContacts();
        mView.showListData(contacts);
    }
}
