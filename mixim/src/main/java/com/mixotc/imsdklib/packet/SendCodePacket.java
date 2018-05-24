package com.mixotc.imsdklib.packet;

import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by junnikokuki on 2017/8/29.
 */

public class SendCodePacket extends BasePacket {
    public SendCodePacket(String phone, String email) {
        super();
        mType = PacketType.SENDCODE;

        JSONObject body = new JSONObject();
        try {
            if (phone != null && !phone.equals("")) {
                body.put("phone", phone);
            } else if (email != null && !email.equals("")) {
                body.put("email", email);
            }
            // android 为 1
            body.put("os", 1);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPacketBody = body.toString().getBytes();

        Logger.d("SendCodePacket", "请求值：" + body.toString());
    }
}
