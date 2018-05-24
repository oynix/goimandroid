package com.mixotc.imsdklib.remotechat;

import android.content.Context;
import android.os.RemoteException;
import android.text.TextUtils;

import com.mixotc.imsdklib.R;
import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMFriendRequest;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.connection.RemoteConnectionManager;
import com.mixotc.imsdklib.exception.ErrorType;
import com.mixotc.imsdklib.listener.PacketReceivedListener;
import com.mixotc.imsdklib.listener.RemoteCallBack;
import com.mixotc.imsdklib.listener.RemoteContactListener;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.GOIMSystemMessage;
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
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.mixotc.imsdklib.message.GOIMMessage.Type.TXT;
import static com.mixotc.imsdklib.utils.SharedPreferencesIds.KEY_LAST_MSG_ID;

public class RemoteContactManager {
    private static final String TAG = RemoteContactManager.class.getSimpleName();

    private static RemoteContactManager sInstance = null;
    private Hashtable<Long, GOIMContact> mClientServices = new Hashtable<>();
    private final Collection<RemoteContactListener> mContactListeners = new CopyOnWriteArrayList<>();
    private Context mContext;

    private RemoteContactManager() {

    }

    public static RemoteContactManager getInstance() {
        if (sInstance == null) {
            synchronized (RemoteContactManager.class) {
                if (sInstance == null) {
                    sInstance = new RemoteContactManager();
                }
            }
        }
        return sInstance;
    }

    public void init(Context context) {
        mContext = context;
    }

    /** 添加联系人监听 */
    public void addContactListener(RemoteContactListener contactListener) {
        if (contactListener == null) {
            return;
        }
        if (!mContactListeners.contains(contactListener)) {
            mContactListeners.add(contactListener);
        }
    }

    /** 移除联系人监听 */
    public void removeContactListener(RemoteContactListener contactListener) {
        mContactListeners.remove(contactListener);
    }

    /** 获取packet监听 */
    public PacketReceivedListener getContactNotifyListener() {
        return mContactNotifyListener;
    }

    /** 更新用户基本信息：name，avatar */
    public void updateUserBaseInfo(long uid, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.USERINFO) {
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
                            JSONObject jsonContact = (JSONObject) data;
                            GOIMContact newContact = GOIMContact.createFromJson(jsonContact);
                            // update database first, then notify local
                            int rowsAffected = RemoteDBManager.getInstance().updateContactBaseInfo(newContact);
                            if (rowsAffected > 0) {
                                List<GOIMContact> notifyData = new ArrayList<>();
                                notifyData.add(newContact);
                                RemoteConversationManager.getInstance().onUpdateUserBaseInfo(newContact);
                                onContactBaseInfoUpdate(notifyData);
                                if (callBack == null) {
                                    return;
                                }
                                try {
                                    callBack.onSuccess(notifyData);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            ControlPacket updateUserInfoPacket = ControlPacket.createUpdateUserInfoPacket(uid);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(updateUserInfoPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /** 根据关键词搜索用户 */
    public void searchUser(String keyWord, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.SEARCHUSER) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            Object data = replyPacket.getReplyData();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            if (data == null) {
                                if (callBack != null) {
                                    try {
                                        callBack.onSuccess(null);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return;
                            }
                            JSONArray contacts = null;
                            try {
                                contacts = new JSONArray(data.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            if (contacts == null) {
                                if (callBack != null) {
                                    try {
                                        callBack.onSuccess(null);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                                return;
                            }
                            List<GOIMContact> result = new ArrayList<>();
                            for (int i = 0; i < contacts.length(); i++) {
                                JSONObject jsonContact = (JSONObject) contacts.opt(i);
                                if (jsonContact != null) {
                                    GOIMContact contact = GOIMContact.createFromJson(jsonContact);
                                    result.add(contact);
                                }
                            }
                            if (callBack != null) {
                                try {
                                    callBack.onSuccess(result);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            ControlPacket searchUserPacket = ControlPacket.createSearchUserPacket(keyWord);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(searchUserPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /** 主动删除好友,其实就是删除好友和group以及conversation messages */
    public void deleteFriend(final GOIMContact contact, final RemoteCallBack callBack) {
        try {
            final long uid = contact.getUid();
            final long gid = contact.getGroupId();
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.DELFRIEND) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            RemoteDBManager.getInstance().deleteContact(uid);
                            // 删除成功后服务器会发送一条del_g通知，接收该通知的RemoteGroupManager会进行Group操作
                            // 服务器不保存conversation，不会发送相关通知 所以需要在此同时处理conversation
                            // 不能在接收del_g处处理，因为不是每条del_g都需要删除conversation
                            RemoteConversationManager.getInstance().deleteConversationAndRelation(contact.getGroupId());
                            onContactDeleted(contact);
                            if (callBack != null) {
                                try {
                                    callBack.onSuccess(null);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            ControlPacket deleteFriendPacket = ControlPacket.createDeleteFriendPacket(uid, gid);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(deleteFriendPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /** 发送添加好友请求 */
    public void sendFriendRequest(long userId, String username, String avatar, String info, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket pkt) {
                    if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.REQUESTFRIEND) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            if (callBack != null) {
                                try {
                                    callBack.onSuccess(null);
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
            });
            ControlPacket friendRequestPacket = ControlPacket.createFriendRequestPacket(userId, username, avatar, info);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(friendRequestPacket, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
        }
    }

    /** 更新或者新增匿名联系人数据 */
    public void updateOrInsertIfNotExist(GOIMContact contact) {
        RemoteDBManager.getInstance().replaceTempContact(contact);
        // TODO: 2018/4/2  notify when update really, should not every time.
        onTempUpdate(contact);
    }

    /**
     * 新增加group时需要更新database和内存中的group member数据
     * database中的数据RemoteGroupManager已经更新，在此只需更新Local
     */
    public void updateGroupMember(GOIMGroup group) {
        onGroupMemberUpdate(group);
    }

    /** 回应好友请求：同意 ／ 拒绝 */
    public void responseFriendRequest(final long uid, final boolean accept, final RemoteCallBack callBack) {
        try {
            RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
                @Override
                public void onReceivedPacket(BasePacket packet) {
                    if (packet.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                        ControlReplyPacket replyPacket = new ControlReplyPacket(packet);
                        if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.ADDFRIEND) {
                            RemoteConnectionManager.getInstance().removePacketListener(this);
                            final int ret = replyPacket.getResult();
                            String reason = replyPacket.getMessage();
                            Object data = replyPacket.getReplyData();
                            if (ret != 0) {
                                callbackOnError(callBack, ret, reason);
                                return;
                            }
                            if (!accept) {
                                try {
                                    if (callBack != null) {
                                        callBack.onSuccess(null);
                                    }
                                } catch (RemoteException e) {
                                    e.printStackTrace();
                                }
                                return;
                            }
                            if (data == null) {
                                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                                return;
                            }
                            GOIMContact contact = GOIMContact.createFromJson((JSONObject) data);
                            if (contact.mUid < 0) {
                                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_ALREADY_FRIEND, reason);
                                return;
                            }
                            if (contact.mUid > 0 || !TextUtils.isEmpty(contact.mUsername)) {
                                RemoteDBManager.getInstance().addContact(contact);
                                RemoteConversationManager.getInstance().updateConversationGroupId(-contact.getUid(), contact.getGroupId());
                                onContactAdd(contact);
                                List<GOIMContact> dataAdd = new ArrayList<>();
                                dataAdd.add(contact);
                                // notify the local manager
                                if (callBack != null) {
                                    try {
                                        callBack.onSuccess(dataAdd);
                                    } catch (RemoteException e) {
                                        e.printStackTrace();
                                    }
                                }
                            } else {
                                callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                            }
                        }
                    }
                }
            });
            // after add listener, start to request
            ControlPacket pkt = ControlPacket.createAddFriendPacket(uid, accept);
            RemoteConnectionManager.getInstance().writeAndFlushPacket(pkt, callBack);
        } catch (Exception e) {
            e.printStackTrace();
            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_SERVER_CONNECTION, null);
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

    /** 删除群聊时会将temp_contact表内相关的user一并删除，并通知Local */
    public void deleteTempContactsByGroupId(long groupId) {
        RemoteDBManager.getInstance().deleteGroupContacts(groupId);
        onTempContactDelete(groupId);
    }

    // 获取客服
    public GOIMContact getClientService() {
        GOIMContact result = null;
        if (mClientServices.size() > 0) {
            result = mClientServices.elements().nextElement();
        }
        return result;
    }

    // 判断是否是客服
    public boolean isClientService(long uid) {
        if (mClientServices.size() <= 0) {
            return false;
        }
        return mClientServices.containsKey(uid);
    }

    public void clientService(final RemoteCallBack callBack) {
        RemoteConnectionManager.getInstance().addPacketListener(new PacketReceivedListener() {
            @Override
            public void onReceivedPacket(BasePacket pkt) {
                if (pkt.getPacketType() == BasePacket.PacketType.CONTROL_REPLY) {
                    ControlReplyPacket replyPacket = new ControlReplyPacket(pkt);
                    if (replyPacket.getReplyPacketType() == ControlReplyPacket.ControlReplyPacketType.CLIENTSERVICE) {
                        RemoteConnectionManager.getInstance().removePacketListener(this);
                        final int ret = replyPacket.getResult();
                        String reason = replyPacket.getMessage();
                        Object data = replyPacket.getReplyData();
                        if (ret != 0) {
                            callbackOnError(callBack, ret, reason);
                            return;
                        }
                        JSONArray services = null;
                        try {
                            services = new JSONArray(data.toString());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        if (services == null) {
                            callbackOnError(callBack, ErrorType.ERROR_EXCEPTION_UNEXPECTED_RESPONSE_ERROR, reason);
                            return;
                        }
                        mClientServices.clear();
                        List<GOIMContact> result = new ArrayList<>();
                        for (int i = 0; i < services.length(); i++) {
                            JSONObject jsonContact = (JSONObject) services.opt(i);
                            if (jsonContact != null) {
                                long uid = jsonContact.optLong("id");
                                String username = jsonContact.optString("name");
                                String nick = jsonContact.optString("nick");
                                String phone = jsonContact.optString("phone", "");
                                String email = jsonContact.optString("email", "");
                                String avatar = jsonContact.optString("icon", "");
                                GOIMContact newContact = new GOIMContact(uid, username, nick);
                                newContact.setAvatar(avatar);
                                newContact.setPhone(phone);
                                newContact.setEmail(email);
                                result.add(newContact);
                                mClientServices.put(uid, newContact);
                            }
                        }
                        if (callBack != null) {
                            try {
                                callBack.onSuccess(result);
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        });
        ControlPacket clientServicePacket = ControlPacket.createClientServicePacket();
        RemoteConnectionManager.getInstance().writeAndFlushPacket(clientServicePacket, callBack);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    public void onContactInitUpdate() {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onContactInit();
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onContactAdd(GOIMContact data) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onContactAdd(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onContactBaseInfoUpdate(List<GOIMContact> data) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onContactUpdate(data);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onContactDeleted(GOIMContact contact) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onContactDelete(contact);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onTempContactDelete(long groupId) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onTempContactDelete(groupId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onTempUpdate(GOIMContact contact) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onTempContactUpdate(contact);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    private void onGroupMemberUpdate(GOIMGroup group) {
        for (RemoteContactListener listener : mContactListeners) {
            try {
                listener.onGroupMemberUpdate(group);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////

    private PacketReceivedListener mContactNotifyListener = new PacketReceivedListener() {
        @Override
        public void onReceivedPacket(BasePacket pkt) {
            if (pkt.getPacketType() == BasePacket.PacketType.NOTIFY) {
                NotifyPacket notifyPacket = new NotifyPacket(pkt);
                if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.ADDFRIEND
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELFRIEND
                        || notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.REQUESTFRIEND) {

                    SharedPreferencesUtils.getInstance(mContext).putLong(KEY_LAST_MSG_ID, notifyPacket.getMid());
                    if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.ADDFRIEND) {
                        handleAddFriendNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.DELFRIEND) {
                        handleDeleteFriendNotify(notifyPacket);
                    } else if (notifyPacket.getNotifyPacketType() == NotifyPacket.NotifyPacketType.REQUESTFRIEND) {
                        handleFriendRequestNotify(notifyPacket);
                    }
                }
            }
        }
    };

    /** 处理添加好友通知 */
    private void handleAddFriendNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "处理添加好友通知");
        Object data = notifyPacket.getNotifyData();
        if (data != null) {
            JSONObject jsonData = (JSONObject) data;
            int response = jsonData.optInt("ack", -1);
            if (response == 0) {//同意
                GOIMContact contact = GOIMContact.createFromJson(jsonData);
                if (contact.getUid() > 0 || contact.mUsername.length() > 0) {
                    RemoteDBManager.getInstance().addContact(contact);
                    onContactAdd(contact);

                    //delAnonymousContact(uid);
                    RemoteConversationManager.getInstance().updateConversationGroupId(-contact.mUid, contact.mGroupId);
                }
            }
        }
    }

    /** 处理被删除通知 */
    private void handleDeleteFriendNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "处理被删除通知");
        Object data = notifyPacket.getNotifyData();
        if (data == null) {
            return;
        }
        JSONObject jsonData = (JSONObject) data;
        long uid = jsonData.optLong("id", -1);
        if (uid <= 0) {
            return;
        }
        GOIMContact contact = RemoteDBManager.getInstance().getContactById(uid);
        if (contact != null) {
            RemoteDBManager.getInstance().deleteContact(contact.getUid());

            long oldGroupId = contact.getGroupId();
            contact.setGroupId(-uid);
            updateOrInsertIfNotExist(contact);
            RemoteConversationManager.getInstance().updateConversationGroupId(oldGroupId, -uid);
            onContactDeleted(contact);
        }

        GOIMMessage message = GOIMMessage.createReceiveMessage(TXT);
        TextMessageBody textMessageBody = new TextMessageBody(contact.getNick() + mContext.getString(R.string.contact_manager_removed_you_from_friends));
        message.addBody(textMessageBody);
        message.setAttribute(GOIMMessage.IS_NOTIFY_KEY, true);
        message.setGroupId(-uid);
        message.getContact().setUid(uid);
        message.getContact().setGroupId(-uid);
        RemoteConversationManager.getInstance().receiveNewMessage(message, false);
        RemoteChatManager.getInstance().broadcastMessage(message);
    }

    /** 处理好友请求通知 */
    private void handleFriendRequestNotify(NotifyPacket notifyPacket) {
        Logger.e(TAG, "处理添加好友请求通知");
        Object data = notifyPacket.getNotifyData();
        if (data != null) {
            JSONObject jsonData = (JSONObject) data;
            long uid = jsonData.optLong("id");
            String username = jsonData.optString("name");
            String avatar = jsonData.optString("icon");
            String info = jsonData.optString("info");

            GOIMFriendRequest request = new GOIMFriendRequest(uid, username, avatar, info, System.currentTimeMillis());
            boolean exist = RemoteDBManager.getInstance().addFriendRequest(request);

            GOIMSystemMessage msg = new GOIMSystemMessage(System.currentTimeMillis(), username + mContext.getString(R.string.contact_manager_request_add_friend),
                    GOIMSystemMessage.SystemMessageType.REQUESTFRIEND, true);

            // 如果数据库中已经存在该条消息，并且时未读状态，此时通知的消息不同设置成未读。
            if (exist) {
                List<GOIMSystemMessage> systemMsgs = RemoteDBManager.getInstance().getSystemMsgs();
                for (GOIMSystemMessage systemMsg : systemMsgs) {
                    if (systemMsg.getMsgType() == GOIMSystemMessage.SystemMessageType.REQUESTFRIEND) {
                        long uidExist = systemMsg.getMsgAttributes().optLong("id");
                        if (uid == uidExist && systemMsg.unRead()) {
                            msg.setUnRead(false);
                            break;
                        }
                    }
                }
            }
            try {
                msg.getMsgAttributes().put("id", uid);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // 现在同一个人发的好友请求都会当作一条新消息加到数据库中，暂时这么处理。
            RemoteConversationManager.getInstance().receiveSysMsg(msg);
//            RemoteChatManager.getInstance().notifySystemMessage();
        }
    }

}
