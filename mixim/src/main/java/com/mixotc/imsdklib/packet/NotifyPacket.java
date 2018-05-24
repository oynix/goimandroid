package com.mixotc.imsdklib.packet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by junnikokuki on 2017/9/18.
 */

public class NotifyPacket extends BasePacket {
    public enum NotifyPacketType {
        UNKNOWN,
        ADDFRIEND,
        DELFRIEND,
        CREATEGROUP,
        DELGROUP,
        REQUESTFRIEND,
        ADDGROUPMEMBER,
        DELGROUPMEMBER,
        DELGROUPMEMBERSELF,
        UPDATEGROUP,
        QUITGROUP,
        ORDERUPDATE,
        ORDERREMIND,
        ORDERAPPEALCANCELLED,
        SECUREDUPDATE,
        SECUREDREMIND,
        SECUREDAPPEALCANCELLED,
        REDPACKETTAKEN,
        TRANSTAKEN,
        TRANSREJECTED
    }

    private NotifyPacketType mNotifyPacketType = NotifyPacketType.UNKNOWN;
    private Object mNotifyData;
    private long mMid;

    public NotifyPacket(BasePacket pkt) {
        super(pkt.getVersion(), pkt.getPacketType(), pkt.getPacketId(), pkt.getPacketBody());
        mType = PacketType.NOTIFY;

        try {
            JSONObject data = new JSONObject(new String(this.mPacketBody));
            mMid = data.optLong("id");
            String action = data.optString("type");
            mNotifyPacketType = convertToNotifyPacketType(action);
            mNotifyData = data.opt("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static NotifyPacketType convertToNotifyPacketType(String type) {
        NotifyPacketType msgType = NotifyPacketType.UNKNOWN;
        if (type.equals("add_fd")) {
            msgType = NotifyPacketType.ADDFRIEND;
        } else if (type.equals("del_fd")) {
            msgType = NotifyPacketType.DELFRIEND;
        } else if (type.equals("cre_g")) {
            msgType = NotifyPacketType.CREATEGROUP;
        } else if (type.equals("req_fd")) {
            msgType = NotifyPacketType.REQUESTFRIEND;
        } else if (type.equals("add_g")) {
            msgType = NotifyPacketType.ADDGROUPMEMBER;
        } else if (type.equals("kicked_g")) {
            msgType = NotifyPacketType.DELGROUPMEMBERSELF;
        } else if (type.equals("kick_g")) {
            msgType = NotifyPacketType.DELGROUPMEMBER;
        } else if (type.equals("del_g")) {
            msgType = NotifyPacketType.DELGROUP;
        } else if (type.equals("upd_g")) {
            msgType = NotifyPacketType.UPDATEGROUP;
        } else if (type.equals("upd_ord")) {
            msgType = NotifyPacketType.ORDERUPDATE;
        } else if (type.equals("rmd_ord")) {
            msgType = NotifyPacketType.ORDERREMIND;
        } else if (type.equals("upd_sec")) {
            msgType = NotifyPacketType.SECUREDUPDATE;
        } else if (type.equals("rmd_sec")) {
            msgType = NotifyPacketType.SECUREDREMIND;
        } else if (type.equals("tak_gift")) {
            msgType = NotifyPacketType.REDPACKETTAKEN;
        } else if (type.equals("tak_trans")) {
            msgType = NotifyPacketType.TRANSTAKEN;
        } else if (type.equals("rej_trans")) {
            msgType = NotifyPacketType.TRANSREJECTED;
        } else if (type.equals("quit_g")) {
            msgType = NotifyPacketType.QUITGROUP;
        } else if (type.equals("apl_ord")) {
            msgType = NotifyPacketType.ORDERAPPEALCANCELLED;
        } else if (type.equals("apl_sec")) {
            msgType = NotifyPacketType.SECUREDAPPEALCANCELLED;
        }

        return msgType;
    }

    public NotifyPacketType getNotifyPacketType() {
        return mNotifyPacketType;
    }

    public Object getNotifyData() {
        return mNotifyData;
    }

    public long getMid() {
        return mMid;
    }
}
