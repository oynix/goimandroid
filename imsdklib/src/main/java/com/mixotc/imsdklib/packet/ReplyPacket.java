package com.mixotc.imsdklib.packet;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by junnikokuki on 2017/8/29.
 */

public class ReplyPacket extends BasePacket {
    private int mResult;
    private String mMessage;
    protected JSONObject mData;

    public ReplyPacket(BasePacket msg) {
        super(msg.getVersion(), msg.getPacketType(), msg.getPacketId(), msg.getPacketBody());

        mResult = -1;

        try {
            mData = new JSONObject(new String(this.mPacketBody));
            mResult = mData.optInt("ret", 0);
            mMessage = mData.optString("msg");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public int getResult() {
        return mResult;
    }

    public String getMessage() {
        return mMessage;
    }

    public JSONObject getData() {
        return mData;
    }

    public static boolean isReplyMsg(BasePacket msg) {
        if (msg.mType == PacketType.HEARTBEAT_REPLY || msg.mType == PacketType.CHAT_SEND_REPLY
                || msg.mType == PacketType.SENDCODE_REPLY || msg.mType == PacketType.LOGIN_REPLY
                || msg.mType == PacketType.LOGOUT_REPLY || msg.mType == PacketType.CONTROL_REPLY) {
            return true;
        }
        return false;
    }
}
