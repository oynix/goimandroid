package com.mixotc.imsdklib.packet;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.chat.GOIMGroup;
import com.mixotc.imsdklib.message.GOIMMessage;
import com.mixotc.imsdklib.message.ImageMessageBody;
import com.mixotc.imsdklib.message.LocationMessageBody;
import com.mixotc.imsdklib.message.PacketMessageBody;
import com.mixotc.imsdklib.message.SecuredTransferMessageBody;
import com.mixotc.imsdklib.message.TextMessageBody;
import com.mixotc.imsdklib.message.TransferMessageBody;
import com.mixotc.imsdklib.message.VideoMessageBody;
import com.mixotc.imsdklib.message.VoiceMessageBody;
import com.mixotc.imsdklib.remotechat.RemoteDBManager;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by junnikokuki on 2017/9/14.
 */

public class ReceivedMsgPacket extends BasePacket {
    private String mChatPacketType;
    private long mGid;
    private long mUid;
    private long mMid;
    private JSONObject mData = null;

    public ReceivedMsgPacket(BasePacket pkt) {
        super(pkt.getVersion(), pkt.getPacketType(), pkt.getPacketId(), pkt.getPacketBody());
        mChatPacketType = "";
        mMid = -1;
        mGid = -1;
        mUid = -1;
        try {
            JSONObject body = new JSONObject(new String(pkt.getPacketBody()));
            mMid = body.optLong("id", -1);
            mGid = body.optLong("gid", -1);
            mUid = body.optLong("uid", -1);
            mChatPacketType = body.optString("type", "");
            mData = body.optJSONObject("data");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (mType == PacketType.ANONYMOUS_CHAT_RECEIVE) {
            mGid = -mUid;
        }
    }

    public long getMid() {
        return mMid;
    }

    public GOIMMessage.Type getMessageType() {
        return getMessageType(mChatPacketType);
    }

    public static GOIMMessage.Type getMessageType(String type) {
        GOIMMessage.Type t = GOIMMessage.Type.UNKNOWN;
        if (type.equals("text")) {
            t = GOIMMessage.Type.TXT;
        } else if (type.equals("image")) {
            t = GOIMMessage.Type.IMAGE;
        } else if (type.equals("audio")) {
            t = GOIMMessage.Type.VOICE;
        } else if (type.equals("video")) {
            t = GOIMMessage.Type.VIDEO;
        } else if (type.equals("locat")) {
            t = GOIMMessage.Type.LOCATION;
        } else if (type.equals("gift")) {
            t = GOIMMessage.Type.PACKET;
        } else if (type.equals("trans")) {
            t = GOIMMessage.Type.TRANSFER;
        } else if (type.equals("secured")) {
            t = GOIMMessage.Type.SECURETRANS;
        }
        return t;
    }

    public GOIMMessage getMessage() {
        GOIMMessage.Type type = getMessageType();
        if (type == GOIMMessage.Type.UNKNOWN) {
            return null;
        }
        GOIMMessage message = GOIMMessage.createReceiveMessage(type);
        switch (type) {
            case TXT: {
                String txt = "";
                if (mData != null) {
                    txt = mData.optString("msg", "");
                }
                TextMessageBody textMessageBody = new TextMessageBody(txt);
                message.addBody(textMessageBody);
            }
            break;
            case IMAGE: {
                String remoteId = "";
                String mime = "";
                if (mData != null) {
                    remoteId = mData.optString("id", "");
                    mime = mData.optString("ext", "");
                }
                ImageMessageBody imgMessageBody = new ImageMessageBody(remoteId, mime);
                message.addBody(imgMessageBody);
            }
            break;
            case VOICE: {
                String remoteId = "";
                String mime = "";
                int length = 0;
                if (mData != null) {
                    remoteId = mData.optString("id", "");
                    mime = mData.optString("ext", "");
                    length = mData.optInt("length", 0);
                }
                VoiceMessageBody voiceMessageBody = new VoiceMessageBody(remoteId, mime, length);
                message.addBody(voiceMessageBody);
            }
            break;
            case VIDEO: {
                String remoteId = "";
                String mime = "";
                int length = 0;
                long fileLength = 0;
                if (mData != null) {
                    remoteId = mData.optString("id", "");
                    mime = mData.optString("ext", "");
                    length = mData.optInt("length", 0);
                    fileLength = mData.optLong("size", 0);
                }
                VideoMessageBody videoMessageBody = new VideoMessageBody(remoteId, mime, length, fileLength);
                message.addBody(videoMessageBody);
            }
            break;
            case LOCATION: {
                String address = "";
                double lat = 0;
                double lon = 0;
                if (mData != null) {
                    address = mData.optString("locat", "");
                    lat = Double.parseDouble(mData.optString("lati", "0"));
                    lon = Double.parseDouble(mData.optString("logi", "0"));
                }
                LocationMessageBody locMessageBody = new LocationMessageBody(address, lat, lon);
                message.addBody(locMessageBody);
            }
            break;
            case PACKET: {
                long packetId = -1;
                String currency = "";
                float amount = 0;
                String info = "";
                int count = 0;
                if (mData != null) {
                    packetId = mData.optLong("id", -1);
                    currency = mData.optString("currency", "");
                    amount = Float.parseFloat(mData.optString("amount", "0"));
                    info = mData.optString("info", "");
                    count = mData.optInt("count", 0);
                }
                PacketMessageBody packetMessageBody = new PacketMessageBody(packetId, currency, amount, info, count);
                message.addBody(packetMessageBody);
            }
            break;
            case TRANSFER: {
                long transferId = -1;
                long from = -1;
                long to = -1;
                String currency = "";
                float amount = 0;
                String info = "";
                if (mData != null) {
                    transferId = mData.optLong("id", -1);
                    from = mData.optLong("from", -1);
                    to = mData.optLong("to", -1);
                    currency = mData.optString("currency", "");
                    amount = Float.parseFloat(mData.optString("amount", "0"));
                    info = mData.optString("info", "");
                }
                TransferMessageBody transferMessageBody = new TransferMessageBody(transferId, currency, amount, info, from, to);
                message.addBody(transferMessageBody);
            }
            break;
            case SECURETRANS: {
                long transferId = -1;
                long from = -1;
                long to = -1;
                String currency = "";
                float amount = 0;
                String info = "";
                int days = 0;
                if (mData != null) {
                    transferId = mData.optLong("id", -1);
                    from = mData.optLong("from", -1);
                    to = mData.optLong("to", -1);
                    currency = mData.optString("currency", "");
                    amount = Float.parseFloat(mData.optString("amount", "0"));
                    info = mData.optString("info", "");
                    days = mData.optInt("days", 0);
                }
                SecuredTransferMessageBody transferMessageBody = new SecuredTransferMessageBody(transferId, currency, amount, days, info, from, to);
                message.addBody(transferMessageBody);
            }
            break;
        }

        message.setGroupId(mGid);
        GOIMContact contact = null;
        if (mType == PacketType.ANONYMOUS_CHAT_RECEIVE) {
            String name = "";
            String icon = "";
            try {
                JSONObject body = new JSONObject(new String(mPacketBody));
                name = body.optString("name");
                icon = body.optString("icon");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            contact = new GOIMContact(mUid, name);
            contact.setAvatar(icon);
            contact.setGroupId(-mUid);
        } else if (mType == PacketType.CHAT_RECEIVE) {
            GOIMGroup group = RemoteDBManager.getInstance().getGroupById(mGid);
            if (group != null) {
                contact = group.getMember(mUid);
            }
        }

        if (contact != null) {
            message.setContact(contact);
        } else {
            message.getContact().setUid(mUid);
            message.getContact().setGroupId(mGid);
        }

        return message;
    }
}
