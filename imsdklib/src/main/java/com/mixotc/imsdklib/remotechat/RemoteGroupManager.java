package com.mixotc.imsdklib.remotechat;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mixotc.imsdklib.R;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.chat.ListLongParcelable;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteGroupListener;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.TextMessageBody;
import com.mixotc.imsdklib.packet.BasePacket;
import com.mixotc.imsdklib.packet.ControlPacket;
import com.mixotc.imsdklib.packet.ControlReplyPacket;
import com.mixotc.imsdklib.packet.NotifyPacket;
import com.mixotc.imsdklib.utils.Logger;
import com.mixotc.imsdklib.utils.SharedPreferencesUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_MSG_ID;

public class RemoteGroupManager {
    private static String TAG = RemoteGroupManager.class.getSimpleName();
    private static RemoteGroupManager sInstance;
    private final List<RemoteGroupListener> mGroupListeners = new ArrayList<>();
    private Context mContext;

    private RemoteGroupManager() {
    }

    public static RemoteGroupManager getInstance() {
        if (sInstance == null) {
            synchronized (RemoteGroupManager.class) {
                if (sInstance == null) {
                    sInstance = new RemoteGroupManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    synchronized void clear() {
        Logger.d(TAG, "group manager clear");
        mGroupListeners.clear();
    }

    /**
     * 添加监听, 目前只有binder调用
     */
    public void addGroupListener(RemoteGroupListener groupListener) {
        if (groupListener == null) {
            return;
        }
        if (!mGroupListeners.contains(groupListener)) {
            mGroupListeners.add(groupListener);
        }
    }

    /**
     * 移除监听
     */
    public void removeGroupListener() {
        mGroupListeners.clear();
    }

    public PacketReceivedListener getGroupNotifyListener() {
        return mGroupNotifyListener;
    }

    /**
     * 主动创建群聊
     */
    public void createGroupActive(final String groupName, final String intro, final ListLongParcelable members, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.CREATEGROUP) {
                            // 创建group成功，需要做的事情：添加到database、 通知Local，最后callback调用更新界面
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            Object data = replyPacket.getReplyData();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            if (data == null) {
                                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                                return;
                            }
                            JSONObject jsonData = (JSONObject) data;
                            long gid = jsonData.optLong("id", -1);
                            if (gid <= 0) {
                                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                                return;
                            }
                            GOIMGroup group = new GOIMGroup(gid, groupName, groupName);
                            group.setDescription(intro);
                            group.setOwner(RemoteAccountManager.getInstance().getLoginUser().getUid());
                            group.setIsSingle(false);

                            for (Long contactId : members.getData()) {
                                GOIMContact contact = RemoteDBManager.getInstance().getContactById(contactId);
                                if (contact != null) {
                                    group.addMember(contact);
                                }
                            }
                            // 此处更新两个表：group表和临时联系人表，与之对应的本地数据也都要更新
                            RemoteDBManager.getInstance().saveGroup(group);
                            RemoteContactManager.getInstance().updateGroupMember(group);
                            onGroupCreate(group);
                            List<GOIMGroup> result = new ArrayList<>();
                            result.add(group);
                            callbackOnSuccess(callBack, result);
                        }
                    }
                }
            });
            ControlPacket createGroupPacket = ControlPacket.createCreateGroupPacket(groupName, intro, members.getData());
            RemoteConnectionManager.getInstance().writeAndFlushPacket(createGroupPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /**
     * (是群主时)主动删除群聊,服务器会发del_g通知,Local回调只进行信息显示
     */
    public void deleteGroupActive(final long groupId, final RemoteCallBack callBack) {
        final GOIMGroup group = RemoteDBManager.getInstance().getGroupById(groupId);
        if (group == null) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_FOUND, null);
            return;
        }
        if (group.getOwner() != RemoteAccountManager.getInstance().getLoginUser().getUid()) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_OWN, null);
            return;
        }
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.DELGROUP) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            // 主动删除一个group时需要删除3个地方的数据：1。group 2。conversation和messages 3。group members
                            // 删除group
//                            RemoteDBManager.getInstance().deleteGroup(groupId);
                            // 删除database中temp_contact保存的group中的user
                            RemoteContactManager.getInstance().deleteTempContactsByGroupId(groupId);
                            // 删除conversation以及聊天记录
                            RemoteConversationManager.getInstance().deleteConversationAndRelation(groupId);
                            onGroupDelete(group);
                            callbackOnSuccess(callBack, null);
                        }
                    }
                }
            });
            ControlPacket deleteGroupPacket = ControlPacket.createDeleteGroupPacket(groupId);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(deleteGroupPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /**
     * (不是群主时)主动退出群聊，服务器会不会发送通知
     */
    public void quitGroupActive(final long groupId, final RemoteCallBack callBack) {
        final GOIMGroup group = RemoteDBManager.getInstance().getGroupById(groupId);
        if (group == null) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_FOUND, null);
            return;
        }
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.QUITGROUP) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            // 删除group
                            RemoteDBManager.getInstance().deleteGroup(groupId);
                            // 删除group members
                            RemoteContactManager.getInstance().deleteTempContactsByGroupId(groupId);
                            // 删除conversation 和 messages
                            RemoteConversationManager.getInstance().deleteConversationAndRelation(groupId);
                            onGroupDelete(group);
                            callbackOnSuccess(callBack, null);
                        }
                    }
                }
            });
            ControlPacket quitGroupPacket = ControlPacket.createQuitGroupPacket(groupId);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(quitGroupPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /**
     * 更新群信息
     */
    public void updateGroupName(final long groupId, final String groupName, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.UPDATEGROUP) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            GOIMGroup group = RemoteDBManager.getInstance().getGroupById(groupId);
                            if (group != null && !TextUtils.isEmpty(groupName)) {
                                group.setGroupName(groupName);
                                RemoteDBManager.getInstance().updateGroupName(groupId, groupName);
                                RemoteConversationManager.getInstance().updateConversationName(groupId, groupName);
                                onGroupUpdate(group, 2);
                            }
                            callbackOnSuccess(callBack, null);
                        }
                    }
                }
            });
            ControlPacket updateGroupPacket = ControlPacket.createUpdateGroupPacket(groupId, groupName);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(updateGroupPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /**
     * 向group里添加人
     */
    public void addGroupMember(final long groupId, final ListLongParcelable members, final RemoteCallBack callBack) {
        final GOIMGroup group = RemoteDBManager.getInstance().getGroupById(groupId);
        if (group == null) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_FOUND, null);
            return;
        }
        Logger.e(TAG, "add group member before:" + group.getMemberCount());
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.ADDGROUPMEMBER) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            for (Long userId : members.getData()) {
                                GOIMContact user = RemoteDBManager.getInstance().getContactById(userId);
                                if (user != null) {
                                    group.addMember(user);
                                }
                            }
                            RemoteDBManager.getInstance().saveGroup(group);
                            RemoteContactManager.getInstance().updateGroupMember(group);
                            Logger.e(TAG, "add group member:" + group.getMemberCount());
                            onGroupUpdate(group, 0);
                            callbackOnSuccess(callBack, null);
                        }
                    }
                }
            });
            ControlPacket addGroupMemberPacket = ControlPacket.createAddGroupMemberPacket(groupId, members.getData());
            RemoteConnectionManager.getInstance().writeAndFlushPacket(addGroupMemberPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /**
     * (是群主时)移除群成员
     */
    public void removeUsersFromGroup(final long groupId, final ListLongParcelable members, final RemoteCallBack callBack) {
        final GOIMGroup group = RemoteDBManager.getInstance().getGroupById(groupId);
        Logger.e(TAG, "------> removeUsersFromGroup groupId = " + groupId);
        if (group == null) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_FOUND, null);
            return;
        }
        if (group.getOwner() != RemoteAccountManager.getInstance().getLoginUser().getUid()) {
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_GROUP_NOT_OWN, null);
            return;
        }
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        Logger.e(TAG, "------> removeUsersFromGroup 成功 00000000000000 ");
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.DELGROUPMEMBER) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            for (Long userId : members.getData()) {
                                group.removeMemberById(userId);
                            }
                            RemoteDBManager.getInstance().deleteGroupMember(groupId, members.getData());
                            RemoteDBManager.getInstance().saveGroup(group);
                            RemoteContactManager.getInstance().updateGroupMember(group);
                            onGroupUpdate(group, 1);
                            callbackOnSuccess(callBack, null);
                        }
                    }
                }
            });
            ControlPacket deleteGroupMemberPacket = ControlPacket.createDeleteGroupMemberPacket(groupId, members.getData());
            RemoteConnectionManager.getInstance().writeAndFlushPacket(deleteGroupMemberPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    private void callbackOnSuccess(RemoteCallBack callBack, List result) {
        if (callBack != null) {
            try {
                callBack.onSuccess(result);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void callbackOnError(RemoteCallBack callBack, int errorCode, String reason) {
        if (callBack != null) {
            try {
                callBack.onError(errorCode, reason);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    public void getGroupsFromServer(final RemoteCallBack callBack) throws RemoteException {
        RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket pkt) {
                if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                    ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                    if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.GETGROUPLIST) {
                        // 登录成功后请求返回调用
                        RemoteConnectionManager.getInstance().removePacketListener(this);
                        int ret = replyPacket.getResult();
                        String reason = replyPacket.getMessage();
                        Object data = replyPacket.getReplyData();
                        if (ret != 0) {
                            callbackOnError(callBack, ret, reason);
                            return;
                        }
                        if (data == null) {
                            callbackOnSuccess(callBack, null);
                            return;
                        }
                        JSONArray groups = null;
                        try {
                            groups = new JSONArray(data.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        if (groups == null) {
                            callbackOnSuccess(callBack, null);
                            return;
                        }
                        List<GOIMGroup> resultGroups = new ArrayList<>();
                        List<GOIMContact> resultContacts = new ArrayList<>();
                        // 解析所有的group
                        for (int i = 0; i < groups.length(); i++) {
                            JSONObject jsonGroup = (JSONObject) groups.opt(i);
                            GOIMGroup group = GOIMGroup.createGroupFromJson(jsonGroup);
                            if (group != null) {
                                resultGroups.add(group);
                                if (group.isSingle()) {
                                    //单聊，更新contact
                                    resultContacts.add(group.getMemberByIndex(0));
                                }
                            }
                        }
                        // 解析所有group完成
                        // 更新database的groups
                        RemoteDBManager.getInstance().restoreGroups(resultGroups);
                        // 通知local
                        onGroupInitUpdate();
                        // 更新database的contacts
                        RemoteDBManager.getInstance().saveContacts(resultContacts);
                        // 通知local
                        RemoteContactManager.getInstance().onContactInitUpdate();
                        callbackOnSuccess(callBack, null);
                    }
                }
            }
        });
        ControlPacket groupListPacket = ControlPacket.createGroupListPacket();
        RemoteConnectionManager.getInstance().writeAndFlushPacket(groupListPacket, callBack);
    }

    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 通知Local更新内存数据
     */
    private void onGroupInitUpdate() {
        for (RemoteGroupListener listener : mGroupListeners) {
            try {
                listener.onGroupInit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onGroupCreate(GOIMGroup group) {
        for (RemoteGroupListener listener : mGroupListeners) {
            try {
                listener.onGroupCreate(group);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    // 0 加人 1 减人 2 改名
    private void onGroupUpdate(GOIMGroup group, int type) {
        for (RemoteGroupListener listener : mGroupListeners) {
            try {
                listener.onGroupUpdate(group, type);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onGroupDelete(GOIMGroup group) {
        for (RemoteGroupListener listener : mGroupListeners) {
            try {
                listener.onGroupDelete(group);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private PacketReceivedListener mGroupNotifyListener = new PacketReceivedListener() {
        @Override
        public void onReceivedPacket(BasePacket pkt) {
            if (pkt.getPacketType() == BasePacket.PacketType.NOTIFY) {
                NotifyPacket notifyPacket = new NotifyPacket(pkt);
                Logger.e(TAG, "----->  mGroupNotifyListener --> 000 PacketType --> " + String.valueOf(notifyPacket.getNotifyPacketType()));
                if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.CREATEGROUP
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.ADDGROUPMEMBER
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUPMEMBER
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUPMEMBERSELF
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUP
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.UPDATEGROUP
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.QUITGROUP) {
                    SharedPreferencesUtils.getInstance(mContext).putLong(KEY_LAST_MSG_ID, notifyPacket.getMid());
                    if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.CREATEGROUP) {
                        onReceiveCreateGroupNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.ADDGROUPMEMBER) {
                        onReceiveAddGroupMemberNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUPMEMBER) {
                        onReceiveKickedOtherFromGroupNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUPMEMBERSELF) {
                        Logger.e(TAG, "----->  mGroupNotifyListener --> 2222 PacketType --->  DELGROUPMEMBERSELF");
                        onReceiveKickedSelfFromGroupNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELGROUP) {
                        onReceiveBreakGroupNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.UPDATEGROUP) {
                        onReceiveUpdateGroupNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.QUITGROUP) {
                        onReceiveGroupMemberQuitNotify(notifyPacket);
                    }
                }
            }
        }
    };

    /**
     * 收到服务器CREATE GROUP通知时，创建的group可能是群聊也可能是单聊，其中群聊可能是自己创建的群
     * 或者被别人拉进群，执行此方法
     */
    private void onReceiveCreateGroupNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "收到服务器CREATE GROUP通知时，创建的group可能是群聊也可能是单聊，其中群聊可能是自己创建的群或者被别人拉进群，执行此方法");
        Object data = notifyPacket.getNotifyData();
        if (data == null) {
            return;
        }
        GOIMGroup group = GOIMGroup.createGroupFromJson((JSONObject) data);
        if (group == null) {
            return;
        }
        // 创建了新的group，需要更新的数据有：group table, temporary contact table
        RemoteDBManager.getInstance().saveGroup(group);
        RemoteContactManager.getInstance().updateGroupMember(group);
        onGroupCreate(group);

        GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
        String msg = "";
        if (!group.isSingle()) {
            StringBuilder memberStr = new StringBuilder();
            for (GOIMContact c : group.getMembers()) {
                if (c.getUid() != group.getOwner()) {
                    if (memberStr.length() != 0) {
                        memberStr.append(mContext.getString(R.string.group_manager_group_member_separator));
                    }
                    memberStr.append(mContext.getString(R.string.group_manager_group_member_format_string, c.getUsername()));
                }
            }
            if (group.getOwner() == RemoteAccountManager.getInstance().getLoginUser().getUid()) {//我是群主
                msg = mContext.getString(R.string.group_manager_my_group_created_format_string, memberStr);
            } else {
                GOIMContact admin = group.getMember(group.getOwner());
                if (admin != null) {
                    msg = mContext.getString(R.string.group_manager_group_created_format_string, admin.getUsername(), memberStr);
                } else {
                    msg = mContext.getString(R.string.group_manager_unknown_group_created_format_string, memberStr);
                }
            }
            message.setContact(RemoteAccountManager.getInstance().getLoginUser());
        } else {
            GOIMContact friend = group.getMemberByIndex(0);
            if (group.getOwner() == RemoteAccountManager.getInstance().getLoginUser().getUid()) {//我请求的好友
                msg = mContext.getString(R.string.contact_manager_added_friend_format_string, friend.getUsername());
            } else {
                msg = friend.getUsername() + mContext.getString(R.string.contact_manager_accept_friend_request);
            }
            message.setContact(friend);
        }

        TextMessageBody textMessageBody = new TextMessageBody(msg);
        message.addBody(textMessageBody);
        message.setGroupId(group.getGroupId());
        message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
        RemoteConversationManager.getInstance().receiveNewMessage(message, group.isSingle());
        RemoteChatManager.getInstance().broadcastMessage(message);
    }

    /**
     * 收到服务器add_g通知时，执行此方法
     */
    private void onReceiveAddGroupMemberNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "收到服务器add_g通知时，执行此方法");
        Object data = notifyPacket.getNotifyData();
        GOIMGroup group = GOIMGroup.createGroupFromJson((JSONObject) data);
        if (group == null) {
            return;
        }
        Logger.e(TAG, "从Json创建Group成功， " + group.getGroupName());
        GOIMGroup originalGroup = RemoteDBManager.getInstance().getGroupById(group.getGroupId());
        Logger.e(TAG, "获取原始group成功，null:" + (originalGroup == null));
        RemoteDBManager.getInstance().saveGroup(group);
        RemoteContactManager.getInstance().updateGroupMember(group);
        onGroupUpdate(group, 0);

        String msg = "";
        String introducer = ((JSONObject) data).optString("updater");
        // 已在群内
        if (originalGroup != null) {
            StringBuilder memberStr = new StringBuilder();
            for (GOIMContact c : group.getMembers()) {
                if (originalGroup.getMember(c.getUid()) == null) {
                    if (memberStr.length() != 0) {
                        memberStr.append(mContext.getString(R.string.group_manager_group_member_separator));
                    }
                    memberStr.append(mContext.getString(R.string.group_manager_group_member_format_string, c.getUsername()));
                }
            }
            String selfName = RemoteAccountManager.getInstance().getLoginUser().getUsername();
            // 判断是否为自己
            if (selfName.equals(introducer)) {
                msg = mContext.getString(R.string.group_manager_my_group_created_format_string, memberStr);
            } else {
                msg = mContext.getString(R.string.group_manager_group_new_member_format_string, introducer, memberStr);
            }
        } else {
            //被加的人
            msg = mContext.getString(R.string.group_manager_group_added_you_format_string, introducer);
        }

        if (msg.length() > 0) {
            GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
            TextMessageBody textMessageBody = new TextMessageBody(msg);
            message.addBody(textMessageBody);
            message.setGroupId(group.getGroupId());
            message.setContact(RemoteAccountManager.getInstance().getLoginUser());
            message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
            RemoteConversationManager.getInstance().receiveNewMessage(message, originalGroup == null);
            RemoteChatManager.getInstance().broadcastMessage(message);
        }
    }

    /**
     * 接收到服务器删除group member的通知，kick_g 即被踢出群组时，执行此方法
     * 被踢出的是自己
     */
    private void onReceiveKickedSelfFromGroupNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "接收到服务器删除group member的通知，kick_g即被踢出群组时，执行此方法");
        Logger.e(TAG, "移除了自己");
        Object data = notifyPacket.getNotifyData();
        if (data == null) {
            return;
        }
        JSONObject jsonData = (JSONObject) data;
        long gid = jsonData.optLong("gid");
        Logger.e(TAG, "-------> NotifyPacket gid --> " + gid);
        JSONArray jsonArray = jsonData.optJSONArray("members");
        JSONObject object = null;
        try {
            object = jsonArray.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object == null) {
            return;
        }
        long uid = 0;
        try {
            uid = object.getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        GOIMGroup group = RemoteDBManager.getInstance().getGroupById(gid);
        if (group != null) {
            List<Long> list = new ArrayList<>();
            list.add(uid);
            RemoteDBManager.getInstance().deleteGroupMember(gid, list);
            RemoteDBManager.getInstance().deleteGroup(gid);
            RemoteContactManager.getInstance().updateGroupMember(group);
            onGroupDelete(group);
            GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
            TextMessageBody textMessageBody = new TextMessageBody(mContext.getString(R.string.group_manager_kicked_from_group));
            message.addBody(textMessageBody);
            message.setGroupId(gid);
            message.setContact(RemoteAccountManager.getInstance().getLoginUser());
            message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
            RemoteConversationManager.getInstance().receiveNewMessage(message, false);
            RemoteChatManager.getInstance().broadcastMessage(message);
        }
    }

    /**
     * 接收到服务器删除group member的通知，kicked_g 即被踢出群组时，执行此方法
     * 被踢出的是别人
     */
    private void onReceiveKickedOtherFromGroupNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "接收到服务器删除group member的通知，kicked_g 即被踢出群组时，执行此方法");
        Logger.e(TAG, "移除了别人");
        Object data = notifyPacket.getNotifyData();
        if (data == null) {
            return;
        }
        JSONObject jsonData = (JSONObject) data;
        JSONArray jsonArray = jsonData.optJSONArray("members");
        if (jsonArray == null) {
            return;
        }
        long gid = jsonData.optLong("gid");
        // TODO ： 暂时一次移除一个成员
        JSONObject object = null;
        try {
            object = jsonArray.getJSONObject(0);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (object == null) {
            return;
        }
        long uid = 0;
        try {
            uid = object.getLong("id");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String name = "";
        try {
            name = object.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Logger.e(TAG, "-------> NotifyPacket --> " + uid + "---> name " + name);
        GOIMGroup group = RemoteDBManager.getInstance().getGroupById(gid);
        if (group != null) {
            GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
            GOIMContact member = group.getMember(uid);
            group.removeMember(member);
            List<Long> list = new ArrayList<>();
            list.add(uid);
            RemoteDBManager.getInstance().deleteGroupMember(gid, list);
            RemoteDBManager.getInstance().saveGroup(group);
            RemoteContactManager.getInstance().updateGroupMember(group);
            onGroupUpdate(group, 1);
            String msg = mContext.getString(R.string.group_manager_kicked_other_from_group, name);
            TextMessageBody textMessageBody = new TextMessageBody(msg);
            message.addBody(textMessageBody);
            message.setGroupId(gid);
            message.setContact(RemoteAccountManager.getInstance().getLoginUser());
            message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
            RemoteConversationManager.getInstance().receiveNewMessage(message, false);
            RemoteChatManager.getInstance().broadcastMessage(message);
        }
    }

    /**
     * 接收到服务器群解散的通知，执行此方法;del_g主动删除好友时服务器也发送该通知
     */
    private void onReceiveBreakGroupNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "接收到服务器群解散的通知，执行此方法;del_g主动删除好友时服务器也发送该通知");
        JSONObject data = (JSONObject) notifyPacket.getNotifyData();
//        try {
//            data = new JSONObject(new String(notifyPacket.getPacketBody()));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        if (data == null) {
            return;
        }
        // FIXME: 2018/5/18 等待服务器修改。{"id":217415921147318272,"type":"del_g","data":null}} -- data为null无group id
        long gid = data.optLong("Id");
        GOIMGroup group = RemoteDBManager.getInstance().getGroupById(gid);
        RemoteDBManager.getInstance().deleteGroup(gid);
        onGroupDelete(group);

        if (group != null && !group.isSingle() && group.getOwner() != RemoteAccountManager.getInstance().getLoginUser().getUid()) {
            GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
            TextMessageBody textMessageBody = new TextMessageBody(mContext.getString(R.string.group_manager_group_dissolved));
            message.addBody(textMessageBody);
            message.setGroupId(gid);
            message.setContact(RemoteAccountManager.getInstance().getLoginUser());
            message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
            RemoteConversationManager.getInstance().receiveNewMessage(message, false);
            RemoteChatManager.getInstance().broadcastMessage(message);
        }
    }

    /**
     * 接收到服务器更新群信息的通知，执行此方法
     */
    private void onReceiveUpdateGroupNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "接收到服务器更新群信息的通知，执行此方法");
        //群信息更新
        Object data = notifyPacket.getNotifyData();
        GOIMGroup group = GOIMGroup.createGroupFromJson((JSONObject) data);
        if (group == null) {
            return;
        }
        RemoteDBManager.getInstance().saveGroup(group);
        RemoteConversationManager.getInstance().updateConversationName(group.getGroupId(), group.getGroupName());
        onGroupUpdate(group, 2);
        // 哪个成员更改群名称
        String updaterId = group.getUpdaterId();
        if (TextUtils.isEmpty(updaterId)) {
            return;
        }
        Long uid = Long.parseLong(updaterId);
        GOIMContact member = group.getMember(uid);
        String userName = member.getUsername();
        GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
        String groupName = group.getGroupName();
        String msg = mContext.getString(R.string.group_manager_group_change_group_name, userName, groupName);
        TextMessageBody textMessageBody = new TextMessageBody(msg);
        message.addBody(textMessageBody);
        message.setGroupId(group.getGroupId());
        message.setContact(RemoteAccountManager.getInstance().getLoginUser());
        message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
        RemoteConversationManager.getInstance().receiveNewMessage(message, false);
        RemoteChatManager.getInstance().broadcastMessage(message);
    }

    /**
     * 接收到服务器发送的群成员退出时，执行此方法
     */
    private void onReceiveGroupMemberQuitNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "接收到服务器发送的群成员退出时，执行此方法");
        //成员退出
        Object data = notifyPacket.getNotifyData();
        if (data == null) {
            return;
        }
        JSONObject jsonData = (JSONObject) data;
        long gid = jsonData.optLong("gid");
        GOIMGroup group = RemoteDBManager.getInstance().getGroupById(gid);
        List<String> names = new ArrayList<>();
        List<Long> quitIdList = new ArrayList<>();
        boolean changed = false;
        if (group != null) {
            JSONArray ids = jsonData.optJSONArray("members");
            if (ids != null && ids.length() > 0) {
                for (int j = 0; j < ids.length(); j++) {
                    JSONObject obj = null;
                    try {
                        obj = ids.getJSONObject(j);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    if (obj == null) {
                        return;
                    }
                    long uid = obj.optLong("id");
                    GOIMContact member = group.getMember(uid);
                    if (member != null) {
                        group.removeMember(member);
                        names.add(member.getUsername());
                        quitIdList.add(member.getUid());
                        changed = true;
                    }
                }
            }
        }

        if (changed) {
            RemoteDBManager.getInstance().deleteGroupMember(gid, quitIdList);
            RemoteDBManager.getInstance().saveGroup(group);
            RemoteContactManager.getInstance().updateGroupMember(group);
            onGroupUpdate(group, 1);

            for (String name : names) {
                GOIMMessage message = GOIMMessage.createReceiveMessage(GOIMMessage.Type.TXT);
                TextMessageBody textMessageBody = new TextMessageBody(mContext.getString(R.string.group_manager_group_member_quit, name));
                message.addBody(textMessageBody);
                message.setGroupId(gid);
                message.setContact(RemoteAccountManager.getInstance().getLoginUser());
                message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
                RemoteConversationManager.getInstance().receiveNewMessage(message, false);
                RemoteChatManager.getInstance().broadcastMessage(message);
            }
        }
    }
}
