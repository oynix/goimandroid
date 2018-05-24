package com.mixotc.imsdklib.message;

import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;

public class MessageEncoder {
    private static final String TAG = "encoder";

    public MessageEncoder() {
    }

    public static String getJSONMsg(GOIMMessage message) {
        try {
            JSONObject msg = new JSONObject();
            JSONArray bodies = new JSONArray();
            if (message.getType() == GOIMMessage.Type.TXT) {
                addTxtBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.IMAGE) {
                addImageBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.VOICE) {
                addVoiceBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.LOCATION) {
                addLocationBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.VIDEO) {
                addVideoBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.FILE) {
                addFileBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.PACKET) {
                addPacketBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.TRANSFER) {
                addTransferBody(bodies, message);
            } else if (message.getType() == GOIMMessage.Type.SECURETRANS) {
                addSecureTransferBody(bodies, message);
            }
            msg.put("bodies", bodies);
            if (message.getAttributes() != null) {
                addExtAttr(msg, message);
            }
            return msg.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return "";
    }

    private static void addExtAttr(JSONObject jsonObject, GOIMMessage message) throws JSONException {
        JSONObject ext = new JSONObject();
        synchronized (message.getAttributes()) {
            Iterator iterator = message.getAttributes().keySet().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                Object value = message.getAttributes().get(key);
                ext.put(key, value);
            }
        }
        jsonObject.put("ext", ext);
    }

    private static void addImageBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "img");
        ImageMessageBody imageMessageBody = (ImageMessageBody) message.getBody();
        body.put("remoteid", imageMessageBody.getRemoteId());
        body.put("localurl", imageMessageBody.getLocalUrl());
        body.put("mime", imageMessageBody.getMime());
        bodies.put(body);
    }

    private static void addVideoBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "video");
        VideoMessageBody videoMessageBody = (VideoMessageBody) message.getBody();
        body.put("remoteid", videoMessageBody.getRemoteId());
        body.put("localurl", videoMessageBody.getLocalUrl());
        body.put("mime", videoMessageBody.getMime());
        body.put("length", videoMessageBody.getLength());
        body.put("filesize", videoMessageBody.getFileSize());
        bodies.put(body);
    }

    private static void addTxtBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "txt");
        TextMessageBody textMessageBody = (TextMessageBody) message.getBody();
        body.put("msg", textMessageBody.mMessage);
        bodies.put(body);
    }

    private static void addVoiceBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "audio");
        VoiceMessageBody voiceMessageBody = (VoiceMessageBody) message.getBody();
        body.put("remoteid", voiceMessageBody.getRemoteId());
        body.put("localurl", voiceMessageBody.getLocalUrl());
        body.put("mime", voiceMessageBody.getMime());
        body.put("length", voiceMessageBody.getLength());
        bodies.put(body);
    }

    private static void addFileBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "file");
        NormalFileMessageBody fileMessageBody = (NormalFileMessageBody) message.getBody();
        body.put("remoteid", fileMessageBody.getRemoteId());
        body.put("localurl", fileMessageBody.getLocalUrl());
        body.put("mime", fileMessageBody.getMime());
        body.put("filesize", fileMessageBody.getFileSize());
        body.put("filename", fileMessageBody.getFileName());
        bodies.put(body);
    }

    private static void addLocationBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "loc");
        LocationMessageBody locationMessageBody = (LocationMessageBody) message.getBody();
        body.put("addr", locationMessageBody.getAddress());
        body.put("lat", locationMessageBody.getLatitude());
        body.put("lng", locationMessageBody.getLongitude());
        bodies.put(body);
    }

    private static void addPacketBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "packet");
        PacketMessageBody packetMessageBody = (PacketMessageBody) message.getBody();
        body.put("id", packetMessageBody.getPacketId());
        body.put("cur", packetMessageBody.getCurrency());
        body.put("amount", packetMessageBody.getAmount());
        body.put("info", packetMessageBody.getInfo());
        body.put("count", packetMessageBody.getCount());
        body.put("status", packetMessageBody.getStatus().ordinal());
        body.put("myamount", packetMessageBody.getMyAmount());
        bodies.put(body);
    }

    private static void addTransferBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "transfer");
        TransferMessageBody transferMessageBody = (TransferMessageBody) message.getBody();
        body.put("id", transferMessageBody.getTransferId());
        body.put("cur", transferMessageBody.getCurrency());
        body.put("amount", transferMessageBody.getAmount());
        body.put("info", transferMessageBody.getInfo());
        body.put("from", transferMessageBody.getFrom());
        body.put("to", transferMessageBody.getTo());
        body.put("status", transferMessageBody.getStatus().ordinal());
        bodies.put(body);
    }

    private static void addSecureTransferBody(JSONArray bodies, GOIMMessage message) throws JSONException {
        JSONObject body = new JSONObject();
        body.put("type", "securetrans");
        SecuredTransferMessageBody transferMessageBody = (SecuredTransferMessageBody) message.getBody();
        body.put("id", transferMessageBody.getTransferId());
        body.put("cur", transferMessageBody.getCurrency());
        body.put("amount", transferMessageBody.getAmount());
        body.put("days", transferMessageBody.getDays());
        body.put("info", transferMessageBody.getInfo());
        body.put("from", transferMessageBody.getFrom());
        body.put("to", transferMessageBody.getTo());
        body.put("status", transferMessageBody.getStatus().ordinal());
        bodies.put(body);
    }

    public static GOIMMessage getMsgFromJson(String json) {
        try {
            JSONObject jsonObject = new JSONObject(json);
            JSONArray bodies = jsonObject.optJSONArray("bodies");
            if (bodies == null || bodies.length() < 1) {
                Logger.d(TAG, "wrong msg without body");
                return null;
            }
            JSONObject body0 = bodies.optJSONObject(0);
            if (body0 == null) {
                Logger.d(TAG, "wrong msg without body");
                return null;
            }
            String type = body0.optString("type", "");
            GOIMMessage newMessage = null;
            if (type.equals("txt")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.TXT);
                String msg = body0.optString("msg", "");
                msg = msg.replaceAll("%22", "\"");
                TextMessageBody textMessageBody = new TextMessageBody(msg);
                newMessage.addBody(textMessageBody);
            } else if (type.equals("img")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.IMAGE);
                String remoteid = body0.optString("remoteid", "");
                String localurl = body0.optString("localurl", "");
                String mime = body0.optString("mime", "");
                ImageMessageBody imageMessageBody = new ImageMessageBody(remoteid, mime);
                imageMessageBody.setLocalUrl(localurl);
                newMessage.addBody(imageMessageBody);
            } else if (type.equals("file")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.FILE);
                String remoteid = body0.optString("remoteid", "");
                String localurl = body0.optString("localurl", "");
                String mime = body0.optString("mime", "");
                long filesize = body0.optLong("filesize", 0);
                String filename = body0.optString("filename");
                NormalFileMessageBody normalFileMessageBody = new NormalFileMessageBody(remoteid, filename, mime, filesize);
                normalFileMessageBody.setLocalUrl(localurl);
                newMessage.addBody(normalFileMessageBody);
            } else if (type.equals("video")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.VIDEO);
                String remoteid = body0.optString("remoteid", "");
                String localurl = body0.optString("localurl", "");
                String mime = body0.optString("mime", "");
                long filesize = body0.optLong("filesize", 0);
                int length = body0.optInt("length", 0);
                VideoMessageBody videoMessageBody = new VideoMessageBody(remoteid, mime, length, filesize);
                videoMessageBody.setLocalUrl(localurl);
                newMessage.addBody(videoMessageBody);
            } else if (type.equals("audio")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.VOICE);
                String remoteid = body0.optString("remoteid", "");
                String localurl = body0.optString("localurl", "");
                String mime = body0.optString("mime", "");
                int length = body0.optInt("length", 0);
                VoiceMessageBody voiceMessageBody = new VoiceMessageBody(remoteid, mime, length);
                voiceMessageBody.setLocalUrl(localurl);
                newMessage.addBody(voiceMessageBody);
            } else if (type.equals("loc")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.LOCATION);
                String addr = body0.optString("addr", "");
                double lat = Double.parseDouble(body0.optString("lat", "0"));
                double lng = Double.parseDouble(body0.optString("lng", "0"));
                LocationMessageBody locationMessageBody = new LocationMessageBody(addr, lat, lng);
                newMessage.addBody(locationMessageBody);
            } else if (type.equals("packet")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.PACKET);
                long packetId = body0.optLong("id", -1);
                String currency = body0.optString("cur", "");
                float amount = Float.parseFloat(body0.optString("amount", "0"));
                String info = body0.optString("info", "");
                int count = body0.optInt("count", 0);
                int status = body0.optInt("status", -1);
                float myamount = Float.parseFloat(body0.optString("myamount", "0"));
                PacketMessageBody packetMessageBody = new PacketMessageBody(packetId, currency, amount, info, count);
                packetMessageBody.setStatus(packetMessageBody.statusFromOrdinal(status));
                packetMessageBody.setMyAmount(myamount);
                newMessage.addBody(packetMessageBody);
            } else if (type.equals("transfer")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.TRANSFER);
                long transferId = body0.optLong("id", -1);
                long from = body0.optLong("from", -1);
                long to = body0.optLong("to", -1);
                String currency = body0.optString("cur", "");
                float amount = Float.parseFloat(body0.optString("amount", "0"));
                String info = body0.optString("info", "");
                int status = body0.optInt("status", -1);
                TransferMessageBody transferMessageBody = new TransferMessageBody(transferId, currency, amount, info, from, to);
                transferMessageBody.setStatus(transferMessageBody.statusFromOrdinal(status));
                newMessage.addBody(transferMessageBody);
            } else if (type.equals("securetrans")) {
                newMessage = new GOIMMessage(GOIMMessage.Type.SECURETRANS);
                long transferId = body0.optLong("id", -1);
                long from = body0.optLong("from", -1);
                long to = body0.optLong("to", -1);
                String currency = body0.optString("cur", "");
                float amount = Float.parseFloat(body0.optString("amount", "0"));
                int days = body0.optInt("days", 0);
                String info = body0.optString("info", "");
                int status = body0.optInt("status", -1);
                SecuredTransferMessageBody transferMessageBody = new SecuredTransferMessageBody(transferId, currency, amount, days, info, from, to);
                transferMessageBody.setStatus(transferMessageBody.statusFromOrdinal(status));
                newMessage.addBody(transferMessageBody);
            } else {
                Logger.d(TAG, "wrong msg without body");
                return null;
            }
            if (jsonObject.has("ext")) {
                JSONObject exts = jsonObject.optJSONObject("ext");
                if (exts != null) {
                    Iterator iterator = exts.keys();
                    while (iterator.hasNext()) {
                        String key = (String) iterator.next();
                        Object val = exts.opt(key);
                        if (val != null) {
                            newMessage.setAttribute(key, val);
                        }
                    }
                }
            }
            return newMessage;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
