package com.mixotc.imsdklib.chat.manager;

import android.os.RemoteException;

import com.mixotc.imsdklib.AdminManager;
import com.mixotc.imsdklib.RemoteServiceBinder;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.ListLongParcelable;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.GOIMGroupListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteGroupListener;
import com.mixotc.imsdklib.utils.Logger;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class LocalGroupManager {
    private static String TAG = LocalGroupManager.class.getSimpleName();

    private static final class LazyHolder {
        private static final LocalGroupManager INSTANCE = new LocalGroupManager();
    }

    private final List<GOIMGroupListener> mGroupListeners = new ArrayList<>();
    private Hashtable<Long, GOIMGroup> mGroups = new Hashtable<>(100);

    private LocalGroupManager() {
    }

    public static LocalGroupManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void clear() {
        mGroups.clear();
    }

    public synchronized void initData() {
        mGroups.clear();
        RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
        if (binder != null) {
            try {
                List<GOIMGroup> groupList = binder.getGroupsWithoutMember();
                for (GOIMGroup group : groupList) {
                    if (group == null) {
                        continue;
                    }
                    Hashtable<Long, GOIMContact> members = LocalContactManager.getInstance().getTempContacts(group.getGroupId());
                    if (members.size() <=0) {
                        continue;
                    }
                    group.setMembers(members);
                    mGroups.put(group.getGroupId(), group);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
        Logger.d(TAG, "GOIM Group Manager initialize data, size :" + mGroups.values().size());
    }

    public void addGroupListener(GOIMGroupListener groupListener) {
        if (groupListener == null) {
            return;
        }
        if (!mGroupListeners.contains(groupListener)) {
            mGroupListeners.add(groupListener);
        }
    }

    public void removeGroupListener(GOIMGroupListener groupListener) {
        mGroupListeners.remove(groupListener);
    }

    public GOIMGroup getGroupById(long groupId) {
        return mGroups.get(groupId);
    }

    public Hashtable<Long, GOIMGroup> getGroupList() {
        return mGroups;
    }

    /** 主动创建group */
    public void createGroupActive(final String groupName, final String intro, final ListLongParcelable members, final RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.createGroupActive(groupName, intro, members, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** (是群主时）主动删除group */
    public void deleteGroupActive(long groupId, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.deleteGroupActive(groupId, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** (不是群主时）主动退出group */
    public void quitGroupActive(long groupId, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.quitGroupActive(groupId, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 更新group的name */
    public void updateGroupName(long groupId, String newName, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.updateGroupName(groupId, newName, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 向group中添加成员 */
    public void addGroupMember(long groupId, ListLongParcelable members, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.addGroupMember(groupId, members, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    /** 移除group中的成员 */
    public void removeGroupMember(long groupId, ListLongParcelable members, RemoteCallBack callBack) {
        try {
            RemoteServiceBinder binder = AdminManager.getInstance().getBinder();
            if (binder == null) {
                callBack.onError(ErrorType.ERROR_EXCEPTION_SERVICE_NOT_RUNNING, "");
            } else {
                binder.removeGroupMember(groupId, members, callBack);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    // 来自RemoteGroupManager的通知
    public RemoteGroupListener mGroupListener = new RemoteGroupListener.Stub() {

        @Override
        public void onGroupInit() {
            initData();
        }

        @Override
        public void onGroupCreate(GOIMGroup group) {
            Logger.e(TAG, "on group create callback:" + group.getGroupName());
            mGroups.put(group.getGroupId(), group);
            for (GOIMGroupListener listener : mGroupListeners) {
                listener.onGroupCreated(group);
            }
        }

        @Override
        public void onGroupUpdate(GOIMGroup group, int type) {
            Logger.e(TAG, "on group update:0:加人 1：减人 2：更新名称:" + type + ", member:" + group.getGroupName() + "," + group.getMemberCount());
            mGroups.put(group.getGroupId(), group);
            for (GOIMGroupListener listener : mGroupListeners) {
                listener.onGroupUpdated(group, type);
            }
        }

        @Override
        public void onGroupDelete(GOIMGroup group) {
            mGroups.remove(group.getGroupId());
            for (GOIMGroupListener listener : mGroupListeners) {
                listener.onGroupDelete(group);
            }
        }
    };

    /** 某个联系人的基本信息更新了，检查Local的数据是否需要更新 */
    public void onUserBaseInfoUpdate(GOIMContact contact) {
        for (GOIMGroup group : mGroups.values()) {
            group.updateMemberBaseInfo(contact);
        }
    }
}
