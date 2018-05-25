package com.mixotc.imsdklib.chat.manager;

import android.os.RemoteException;
import android.util.Log;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.GOIMContactListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteContactListener;
import com.mixotc.imsdklib.remotechat.RemoteAccountManager;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

public class LocalContactManager {
    private static final String TAG = LocalContactManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final LocalContactManager INSTANCE = new LocalContactManager();
    }

    private Map<Long, GOIMContact> mContactMap = new Hashtable<>(100);
    // group与其的group member对应表
    private Hashtable<Long, Hashtable<Long, GOIMContact>> mTempGroupContactTable = new Hashtable<>(100);
    private List<GOIMContactListener> mContactListeners = new ArrayList<>();

    private LocalContactManager() {
    }

    public static LocalContactManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mContactMap.clear();
    }

    // 登录成功后初始化数据
    public synchronized void initData() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            mContactMap.clear();
            try {
                List<GOIMContact> contacts = binder.getContactsFromDB();
                for (GOIMContact contact : contacts) {
                    mContactMap.put(contact.getUid(), contact);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "GOIM Contact Manager initialize data from remote database, size :" + mContactMap.size());
    }

    /** 添加listener */
    public void addContactListener(GOIMContactListener contactListener) {
        if (contactListener == null) {
            return;
        }
        if (!mContactListeners.contains(contactListener)) {
            mContactListeners.add(contactListener);
        }
    }

    /** 移除listener */
    public void removeContactListener(GOIMContactListener contactListener) {
        mContactListeners.remove(contactListener);
    }

    /** 获取所有联系人 */
    public Map<Long, GOIMContact> getContactList() {
        Log.i(TAG, "getContactList: -----------" + mContactMap.size());
        return mContactMap;
    }

    /** 根据groupId获取contact */
    public GOIMContact getContactByGroupId(long groupId) {
        for (GOIMContact contact : getContactList().values()) {
            if (contact.getGroupId() == groupId) {
                return contact;
            }
        }
        return null;
    }

    /** 增加陌生联系人 */
    public void addAnonymousContact(GOIMContact contact) {
        Hashtable<Long, GOIMContact> tempContacts = getTempContacts(contact.getGroupId());
        tempContacts.put(contact.getUid(), contact);
        mTempGroupContactTable.put(contact.getGroupId(), tempContacts);
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateOrInsertIfNotExist(contact);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 查询某个联系人是否存在 */
    public boolean hasAnonymousContact(long uid) {
        Hashtable<Long, GOIMContact> tempContacts = getTempContacts(-uid);
        return tempContacts != null && tempContacts.size() != 0;
    }

    /** 根据uid获取陌生联系人 */
    public GOIMContact getAnonymousContact(long uid) {
        Hashtable<Long, GOIMContact> tempContacts = getTempContacts(-uid);
        if (tempContacts == null || tempContacts.size() == 0) {
            return null;
        }
        return tempContacts.get(uid);
    }

    /** 根据groupId获取group members，如果不存在则返回新的空集合 */
    public Hashtable<Long, GOIMContact> getTempContacts(final long groupId) {
        // 先从LocalManager获取，如果为null再从RemoteDatabase获取，如果依然为null则new一个返回。
        Hashtable<Long, GOIMContact> tempContacts = mTempGroupContactTable.get(groupId);
        if (tempContacts == null) {
            try {
                RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
                if (binder != null) {
                    List<GOIMContact> contacts = binder.loadTempContacts(groupId);
                    if (contacts != null && contacts.size() > 0) {
                        tempContacts = new Hashtable<>();
                        for (GOIMContact contact : contacts) {
                            tempContacts.put(contact.getUid(), contact);
                        }
                    }
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            if (tempContacts != null) {
                mTempGroupContactTable.put(groupId, tempContacts);
            } else {
                tempContacts = new Hashtable<>();
            }
        }
        return tempContacts;
    }

    /** 更新一条添加好友请求 */
    public void updateFriendRequestResponse(GOIMFriendRequest request) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.updateFriendRequest(request);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 删除一条添加好友请求 */
    public void deleteFriendRequest(GOIMFriendRequest request) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.deleteFriendRequest(request);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 清空添加好友请求 */
    public void clearFriendRequests() {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                binder.clearFriendRequests();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /** 获取所有添加好友请求 */
    public List<GOIMFriendRequest> getFriendRequests() {
        List<GOIMFriendRequest> friendRequests = new ArrayList<>();
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                friendRequests.addAll(binder.getFriendRequests());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return friendRequests;
    }

    public GOIMContact getClientService() {
        GOIMContact result = null;
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                result = binder.getClientService();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    public boolean isClientService(long uid) {
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                return binder.isClientService(uid);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /** 发送加好友请求 */
    public void sendFriendRequest(GOIMContact user, String info, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                GOIMContact loginUser = RemoteAccountManager.getInstance().getLoginUser();
                binder.sendFriendRequest(user.getUid(), loginUser.getNick(), loginUser.getAvatar(), info, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 回复其他用户的添加请求：同意／拒绝 */
    public void responseFriendRequest(long userId, boolean accept, RemoteCallBack.Stub callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.responseFriendRequest(userId, accept, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 主动删除好友 */
    public void removeFriend(GOIMContact contact, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.delFriend(contact, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 搜索用户 */
    public void searchUser(String keyword, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.searchUser(keyword, callBack);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** 更新用户基本信息 ： 用户名 头像 */
    public void updateUserBaseInfo(long userId, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.updateUserBaseInfo(userId, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过Id获取好友或者陌生人信息
     *
     * @param uid 用户Id
     * @return contact
     */
    public GOIMContact getContactByUid(long uid) {
        GOIMContact user;
        Map<Long, GOIMContact> contacts = LocalContactManager.getInstance().getContactList();
        if (contacts != null && contacts.containsKey(uid)) {
            user = contacts.get(uid);
        } else {
            user = LocalContactManager.getInstance().getAnonymousContact(uid);
        }
        return user;
    }

    public RemoteContactListener mContactListener = new RemoteContactListener.Stub() {

        @Override
        public void onContactDelete(GOIMContact contact) {
            // 该回调来自RemoteContactManager，被调用时，更新Local数据，然后通知Local所有相关的地方
            mContactMap.remove(contact.getUid());
            for (GOIMContactListener listener : mContactListeners) {
                listener.onContactDeleted(contact);
            }
        }

        @Override
        public void onContactInit() {
            initData();
        }

        @Override
        public void onTempContactInit(List<GOIMContact> data) {

        }

        @Override
        public void onContactAdd(GOIMContact contact) {
            // 该回调来自RemoteContactManager，被调用时，更新Local数据，然后通知Local所有相关的地方
            mContactMap.put(contact.getUid(), contact);
            for (GOIMContactListener listener : mContactListeners) {
                listener.onContactAdded(contact);
            }
        }

        @Override
        public void onContactUpdate(List<GOIMContact> data) {
            // 该回调来自RemoteContactManager，被调用时，更新Local数据，然后通知Local所有相关的地方
            // Group：database储存的是member的id，所以修改基本信息不用修改database，只修改Local即可
            // Conversation：可能会影响conversation name，除此无他。
            if (data.size() <= 0) {
                return;
            }
            for (GOIMContact contact : data) {
                // update contact manager
                GOIMContact newUser = mContactMap.get(contact.getUid());
                if (newUser != null) {
                    newUser.updateBaseInfo(contact);
                }
                for (Hashtable<Long, GOIMContact> tempGroup : mTempGroupContactTable.values()) {
                    for (GOIMContact c : tempGroup.values()) {
                        if (c.getUid() == contact.getUid()) {
                            c.updateBaseInfo(contact);
                        }
                    }
                }
                // update group manager,单聊群聊都需要更新
                LocalGroupManager.getInstance().onUserBaseInfoUpdate(contact);
            }
            for (GOIMContactListener listener : mContactListeners) {
                listener.onContactsUpdated();
            }
        }

        @Override
        public void onTempContactDelete(long groupId) {
            mTempGroupContactTable.remove(groupId);
        }

        // 临时联系人更新或插入
        @Override
        public void onTempContactUpdate(GOIMContact contact) {
            Hashtable<Long, GOIMContact> tempContacts = getTempContacts(contact.getGroupId());
            tempContacts.put(contact.getUid(), contact);
            mTempGroupContactTable.put(contact.getGroupId(), tempContacts);

            for (GOIMContactListener listener : mContactListeners) {
                listener.onTempContactUpdate(contact);
            }
        }

        @Override
        public void onGroupMemberUpdate(GOIMGroup group) {
            Hashtable<Long, GOIMContact> groupMembers = mTempGroupContactTable.get(group.getGroupId());
            if (groupMembers == null) {
                groupMembers = new Hashtable<>();
            }
            groupMembers.clear();
            for (GOIMContact contact : group.getMembers()) {
                groupMembers.put(contact.getUid(), contact);
            }
            mTempGroupContactTable.put(group.getGroupId(), groupMembers);
        }
    };
}
