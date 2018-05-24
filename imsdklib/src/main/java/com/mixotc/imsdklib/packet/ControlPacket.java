package com.mixotc.imsdklib.packet;

import android.text.TextUtils;

import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by junnikokuki on 2017/8/29.
 */

public class ControlPacket extends BasePacket {
    public enum ControlPacketType {
        UNKNOWN,
        ADDFRIEND,// 添加朋友
        DELFRIEND,// 删除朋友
        GETFRIENDLIST,// 请求好友列表
        CREATEGROUP,// 创建群聊
        GETGROUPLIST,// 请求群列表
        ADDGROUPMEMBER,// 添加群成员
        DELGROUPMEMBER,// 删除群成员
        GETGROUPMEMBERLIST,// 请求群成员列表
        SEARCHUSER,// 查找用户
        UPDATEUSERINFO,// 更新用户信息
        REQUESTFRIEND,// 添加好友请求
        DELGROUP,// 删除组
        QUITGROUP,// 离开组
        UPDATEGROUP,// 更新组
        USERINFO,// 用户信息
        ClIENTSERVICE,// 客服
        RESET_PASS,// 重置密码
        GET_SECRET,// 谷歌验证
        VERIFY_SECRET,
        SEND_CODE,// 发送验证码
        USER_IDENTITY,
        GET_IDENTITY,
        GET_BTIDENTITY,
        NEWGIFT,// 新红包
        GETGIFT,
        TAKEGIFT,
        NEWTRANS,
        GETTRANS,
        TAKETRANS,
        REJECTTRANS,
        NEWSECURED,
        GETSECURED,
        UPDATESECURED,
        CANCELSECURED,
        SENDSECURED,
        OFFLINEMSGS,
        CANCELAPPEAL,
        GET_FEE
    }

    public ControlPacket(ControlPacketType type, Object data) {
        super();
        mType = PacketType.CONTROL;

        JSONObject body = new JSONObject();
        try {
            body.put("action", convertFromControlPacketType(type));
            if (data != null) {
                body.put("data", data);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPacketBody = body.toString().getBytes();

        Logger.d("ControlPacket", "请求值：" + body.toString());
    }

    private static String convertFromControlPacketType(ControlPacketType type) {
        String rawType = "";
        switch (type) {
            case ADDFRIEND:
                rawType = "add_friend";
                break;
            case DELFRIEND:
                rawType = "del_friend";
                break;
            case GETFRIENDLIST:
                rawType = "friend_list";
                break;
            case CREATEGROUP:
                rawType = "create_group";
                break;
            case GETGROUPLIST:
                rawType = "group_list";
                break;
            case ADDGROUPMEMBER:
                rawType = "add_g_member";
                break;
            case DELGROUPMEMBER:
                rawType = "del_g_member";
                break;
            case GETGROUPMEMBERLIST:
                rawType = "g_member_list";
                break;
            case SEARCHUSER:
                rawType = "search_user";
                break;
            case UPDATEUSERINFO:
                rawType = "user_update";
                break;
            case REQUESTFRIEND:
                rawType = "request_friend";
                break;
            case DELGROUP:
                rawType = "delete_group";
                break;
            case QUITGROUP:
                rawType = "quit_group";
                break;
            case USERINFO:
                rawType = "user_info";
                break;
            case ClIENTSERVICE:
                rawType = "client_service";
                break;
            case RESET_PASS:
                rawType = "reset_pass";
                break;
            case UPDATEGROUP:
                rawType = "update_group";
                break;
            case GET_SECRET:
                rawType = "get_secret";
                break;
            case VERIFY_SECRET:
                rawType = "verify_secret";
                break;
            case SEND_CODE:
                rawType = "send_code";
                break;
            case USER_IDENTITY:
                rawType = "user_identity";
                break;
            case GET_IDENTITY:
                rawType = "get_identity";
                break;
            case GET_BTIDENTITY:
                rawType = "get_btidentity";
                break;
            case NEWGIFT:
                rawType = "new_gift";
                break;
            case GETGIFT:
                rawType = "get_gift";
                break;
            case TAKEGIFT:
                rawType = "take_gift";
                break;
            case NEWTRANS:
                rawType = "new_trans";
                break;
            case GETTRANS:
                rawType = "get_trans";
                break;
            case TAKETRANS:
                rawType = "take_trans";
                break;
            case REJECTTRANS:
                rawType = "reject_trans";
                break;
            case NEWSECURED:
                rawType = "new_secured";
                break;
            case GETSECURED:
                rawType = "get_secured";
                break;
            case UPDATESECURED:
                rawType = "update_secured";
                break;
            case CANCELSECURED:
                rawType = "cancel_secured";
                break;
            case SENDSECURED:
                rawType = "send_secured";
                break;
            case OFFLINEMSGS:
                rawType = "offline_msg";
                break;
            case CANCELAPPEAL:
                rawType = "cancel_appeal";
                break;
            case GET_FEE:
                rawType = "get_fee";
                break;
            default:
        }
        return rawType;
    }

    public static ControlPacket createAddFriendPacket(long userId, boolean accept) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", userId);
            data.put("ack", accept ? 0 : 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacketType.ADDFRIEND, data);
    }

    public static ControlPacket createUpdateUserInfoPacket(long userId) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", userId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacketType.USERINFO, data);
    }

    public static ControlPacket createSearchUserPacket(String keyword) {
        JSONObject data = new JSONObject();
        try {
            data.put("keyword", keyword);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.SEARCHUSER, data);
    }

    public static ControlPacket createDeleteFriendPacket(long uid, long gid) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", uid);
            data.put("gid", gid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.DELFRIEND, data);
    }

    public static ControlPacket createFriendRequestPacket(long userId, String username, String avatar, String info) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", userId);
            data.put("name", username);
            data.put("icon", avatar);
            data.put("info", info);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.REQUESTFRIEND, data);
    }

    public static ControlPacket createCreateGroupPacket(String groupName, String intro, List<Long> members) {
        JSONObject data = new JSONObject();
        try {
            data.put("name", groupName);
            data.put("intro", intro);

            JSONArray ids = new JSONArray();
            for (Long userId : members) {
                ids.put(userId);
            }
            data.put("ids", ids);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.CREATEGROUP, data);
    }

    public static ControlPacket createDeleteGroupPacket(long groupId) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.DELGROUP, data);
    }

    public static ControlPacket createQuitGroupPacket(long groupId) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", groupId);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.QUITGROUP, data);
    }

    public static ControlPacket createUpdateGroupPacket(long groupId, String groupName) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", groupId);
            if (!TextUtils.isEmpty(groupName)) {
                data.put("name", groupName);
            }
//            if (!TextUtils.isEmpty(intro)) {
//                data.put("intro", intro);
//            }
//            if (!TextUtils.isEmpty(notice)) {
//                data.put("notice", notice);
//            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.UPDATEGROUP, data);
    }

    public static ControlPacket createAddGroupMemberPacket(long groupId, List<Long> members) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", groupId);
            JSONArray ids = new JSONArray();
            for (Long userId : members) {
                ids.put(userId);
            }
            data.put("ids", ids);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.ADDGROUPMEMBER, data);
    }

    public static ControlPacket createDeleteGroupMemberPacket(long groupId, List<Long> members) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", groupId);
            JSONArray ids = new JSONArray();
            for (Long userId : members) {
                ids.put(userId);
            }
            data.put("ids", ids);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.DELGROUPMEMBER, data);
    }

    public static ControlPacket createGroupListPacket() {
        return new ControlPacket(ControlPacket.ControlPacketType.GETGROUPLIST, null);
    }

    public static ControlPacket createClientServicePacket() {
        JSONObject data = new JSONObject();
        return new ControlPacket(ControlPacket.ControlPacketType.ClIENTSERVICE, data);
    }

    public static ControlPacket createOfflineMsgPacket(long lastMid) {
        JSONObject data = new JSONObject();
        try {
            data.put("id", lastMid);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ControlPacket(ControlPacket.ControlPacketType.OFFLINEMSGS, data);
    }
}
