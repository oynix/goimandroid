package com.mixotc.imsdklib.packet;

/**
 * Created by junnikokuki on 2017/9/8.
 */

public class ControlReplyPacket extends ReplyPacket {
    public enum ControlReplyPacketType {
        UNKNOWN,
        ADDFRIEND,
        DELFRIEND,
        GETFRIENDLIST,
        CREATEGROUP,
        GETGROUPLIST,
        ADDGROUPMEMBER,
        DELGROUPMEMBER,
        GETGROUPMEMBERLIST,
        SEARCHUSER,
        UPDATEUSERINFO,
        REQUESTFRIEND,
        DELGROUP,
        QUITGROUP,
        UPDATEGROUP,
        USERINFO,
        RESET_PASS,
        CLIENTSERVICE,
        GET_SECRET,
        VERIFY_SECRET,
        SEND_CODE,
        USER_IDENTITY,
        GET_IDENTITY,
        GET_BTIDENTITY,
        NEWGIFT,
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

    private ControlReplyPacket.ControlReplyPacketType mControlReplyPacketType;
    private Object mReplyData;

    public ControlReplyPacket(BasePacket msg) {
        super(msg);

        if (mData != null) {
            String type = mData.optString("action");
            mControlReplyPacketType = convertToControlReplyPacketType(type);
            if (!mData.isNull("data")) {
                mReplyData = mData.opt("data");
            }
        }
    }

    private static ControlReplyPacket.ControlReplyPacketType convertToControlReplyPacketType(String type) {
        ControlReplyPacketType msgType = ControlReplyPacketType.UNKNOWN;
        if (type.equals("add_friend_r")) {
            msgType = ControlReplyPacketType.ADDFRIEND;
        } else if (type.equals("del_friend_r")) {
            msgType = ControlReplyPacketType.DELFRIEND;
        } else if (type.equals("friend_list_r")) {
            msgType = ControlReplyPacketType.GETFRIENDLIST;
        } else if (type.equals("create_group_r")) {
            msgType = ControlReplyPacketType.CREATEGROUP;
        } else if (type.equals("group_list_r")) {
            msgType = ControlReplyPacketType.GETGROUPLIST;
        } else if (type.equals("add_g_member_r")) {
            msgType = ControlReplyPacketType.ADDGROUPMEMBER;
        } else if (type.equals("del_g_member_r")) {
            msgType = ControlReplyPacketType.DELGROUPMEMBER;
        } else if (type.equals("g_member_list_r")) {
            msgType = ControlReplyPacketType.GETGROUPMEMBERLIST;
        } else if (type.equals("search_user_r")) {
            msgType = ControlReplyPacketType.SEARCHUSER;
        } else if (type.equals("user_update_r")) {
            msgType = ControlReplyPacketType.UPDATEUSERINFO;
        } else if (type.equals("request_friend_r")) {
            msgType = ControlReplyPacketType.REQUESTFRIEND;
        } else if (type.equals("delete_group_r")) {
            msgType = ControlReplyPacketType.DELGROUP;
        } else if (type.equals("quit_group_r")) {
            msgType = ControlReplyPacketType.QUITGROUP;
        } else if (type.equals("user_info_r")) {
            msgType = ControlReplyPacketType.USERINFO;
        } else if (type.equals("client_service_r")) {
            msgType = ControlReplyPacketType.CLIENTSERVICE;
        } else if (type.equals("reset_pass_r")) {
            msgType = ControlReplyPacketType.RESET_PASS;
        } else if (type.equals("update_group_r")) {
            msgType = ControlReplyPacketType.UPDATEGROUP;
        } else if (type.equals("get_secret_r")) {
            msgType = ControlReplyPacketType.GET_SECRET;
        } else if (type.equals("verify_secret_r")) {
            msgType = ControlReplyPacketType.VERIFY_SECRET;
        } else if (type.equals("send_code_r")) {
            msgType = ControlReplyPacketType.SEND_CODE;
        } else if (type.equals("user_identity_r")) {
            msgType = ControlReplyPacketType.USER_IDENTITY;
        } else if (type.equals("get_identity_r")) {
            msgType = ControlReplyPacketType.GET_IDENTITY;
        } else if (type.equals("get_btidentity_r")) {
            msgType = ControlReplyPacketType.GET_BTIDENTITY;
        } else if (type.equals("new_gift_r")) {
            msgType = ControlReplyPacketType.NEWGIFT;
        } else if (type.equals("get_gift_r")) {
            msgType = ControlReplyPacketType.GETGIFT;
        } else if (type.equals("take_gift_r")) {
            msgType = ControlReplyPacketType.TAKEGIFT;
        } else if (type.equals("new_trans_r")) {
            msgType = ControlReplyPacketType.NEWTRANS;
        } else if (type.equals("get_trans_r")) {
            msgType = ControlReplyPacketType.GETTRANS;
        } else if (type.equals("take_trans_r")) {
            msgType = ControlReplyPacketType.TAKETRANS;
        } else if (type.equals("reject_trans_r")) {
            msgType = ControlReplyPacketType.REJECTTRANS;
        } else if (type.equals("new_secured_r")) {
            msgType = ControlReplyPacketType.NEWSECURED;
        } else if (type.equals("get_secured_r")) {
            msgType = ControlReplyPacketType.GETSECURED;
        } else if (type.equals("update_secured_r")) {
            msgType = ControlReplyPacketType.UPDATESECURED;
        } else if (type.equals("cancel_secured_r")) {
            msgType = ControlReplyPacketType.CANCELSECURED;
        } else if (type.equals("send_secured_r")) {
            msgType = ControlReplyPacketType.SENDSECURED;
        } else if (type.equals("offline_msg_r")) {
            msgType = ControlReplyPacketType.OFFLINEMSGS;
        } else if (type.equals("cancel_appeal_r")) {
            msgType = ControlReplyPacketType.CANCELAPPEAL;
        } else if (type.equals("get_fee_r")) {
            msgType = ControlReplyPacketType.GET_FEE;
        }

        return msgType;
    }

    public ControlReplyPacket.ControlReplyPacketType getReplyPacketType() {
        return mControlReplyPacketType;
    }

    public Object getReplyData() {
        return mReplyData;
    }
}
