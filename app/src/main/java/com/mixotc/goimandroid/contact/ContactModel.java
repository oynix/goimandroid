package com.mixotc.goimandroid.contact;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.chat.GOIMContact;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/24 下午2:20
 * Version  : v1.0.0
 * Describe :
 */
public class ContactModel implements ContactContract.Model {
    @Override
    public List<GOIMContact> getContacts() {
        Collection<GOIMContact> values = AdminManager.getInstance().getContacts().values();
        return new ArrayList<>(values);
    }
}
