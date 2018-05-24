package com.mixotc.imsdklib.packet;

import android.text.TextUtils;

import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by junnikokuki on 2017/8/29.
 */

public class LoginPacket extends BasePacket {
    public LoginPacket(String phone, String email, String code, String country, String device, int version, int os, int mode, String imei) {
        super();
        mType = PacketType.LOGIN;

//        String imei = DeviceUtil.getIMEI(MyApplication.getAppContext());
        JSONObject body = new JSONObject();
        try {
            if (phone != null && !phone.equals("")) {
                body.put("phone", phone);
            } else if (email != null && !email.equals("")) {
                body.put("email", email);
            }
            body.put("code", code);
            body.put("country", country);
            if (!TextUtils.isEmpty(device)) {
                body.put("device", device);
            }
            if (version != 0) {
                body.put("version", version);
            }
            if (os != 0) {
                body.put("os", os);
            }
            body.put("mode", mode);
            body.put("imei", imei);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        mPacketBody = body.toString().getBytes();

        Logger.d("LoginPacket", "请求值：" + body.toString());
    }
}
