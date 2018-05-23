package com.mixotc.imsdklib.packet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Author   : xiaoyu
 * Date     : 2018/5/23 下午2:27
 * Version  : v1.0.0
 * Describe :
 */
public class BasePacket implements Parcelable {

    public enum PacketType {
        UNKNOWN,//0
        HEARTBEAT,//2
        HEARTBEAT_REPLY,//3
        CHAT_SEND,//4
        CHAT_SEND_REPLY,//5
        CHAT_RECEIVE,//6
        ANONYMOUS_CHAT_RECEIVE,//7
        SENDCODE,//15
        SENDCODE_REPLY,//16
        LOGIN,//17
        LOGIN_REPLY,//18
        LOGOUT,//19
        LOGOUT_REPLY,//20
        CONTROL,//21
        CONTROL_REPLY,//22
        NOTIFY,//24
    }

    private long mVersion;
    private PacketType mType;
    private long mPacketId;
    private byte[] mPacketBody;

    private static long sNextPacketId = 0L;

    public BasePacket() {
        this.mVersion = 1L;
        this.mType = PacketType.UNKNOWN;
        this.mPacketId = sNextPacketId++;
        this.mPacketBody = null;
    }

    public BasePacket(long version, PacketType type, long sequenceId, byte[] body) {
        this.mVersion = version;
        this.mType = type;
        this.mPacketId = sequenceId;
        this.mPacketBody = body;
    }

    protected BasePacket(Parcel parcel) {
        mVersion = parcel.readLong();
        mType = convertToPacketType(parcel.readLong());
        mPacketId = parcel.readLong();
        mPacketBody = new byte[parcel.readInt()];
        parcel.readByteArray(mPacketBody);
    }

    public static final Creator<BasePacket> CREATOR = new Creator<BasePacket>() {
        @Override
        public BasePacket createFromParcel(Parcel in) {
            return new BasePacket(in);
        }

        @Override
        public BasePacket[] newArray(int size) {
            return new BasePacket[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(mVersion);
        parcel.writeLong(convertFromPacketType(mType));
        parcel.writeLong(mPacketId);
        parcel.writeInt(mPacketBody.length);
        parcel.writeByteArray(mPacketBody);
    }

    public static PacketType convertToPacketType(long type) {
        PacketType msgType;
        if (type == 2) {
            msgType = PacketType.HEARTBEAT;
        } else if (type == 3) {
            msgType = PacketType.HEARTBEAT_REPLY;
        } else if (type == 4) {
            msgType = PacketType.CHAT_SEND;
        } else if (type == 5) {
            msgType = PacketType.CHAT_SEND_REPLY;
        } else if (type == 6) {
            msgType = PacketType.CHAT_RECEIVE;
        } else if (type == 7) {
            msgType = PacketType.ANONYMOUS_CHAT_RECEIVE;
        } else if (type == 15) {
            msgType = PacketType.SENDCODE;
        } else if (type == 16) {
            msgType = PacketType.SENDCODE_REPLY;
        } else if (type == 17) {
            msgType = PacketType.LOGIN;
        } else if (type == 18) {
            msgType = PacketType.LOGIN_REPLY;
        } else if (type == 19) {
            msgType = PacketType.LOGOUT;
        } else if (type == 20) {
            msgType = PacketType.LOGOUT_REPLY;
        } else if (type == 21) {
            msgType = PacketType.CONTROL;
        } else if (type == 22) {
            msgType = PacketType.CONTROL_REPLY;
        } else if (type == 24) {
            msgType = PacketType.NOTIFY;
        } else {
            msgType = PacketType.UNKNOWN;
        }
        return msgType;
    }

    public static long convertFromPacketType(PacketType type) {
        long rawType = 0;
        switch (type) {
            case HEARTBEAT:
                rawType = 2;
                break;
            case HEARTBEAT_REPLY:
                rawType = 3;
                break;
            case CHAT_SEND:
                rawType = 4;
                break;
            case CHAT_SEND_REPLY:
                rawType = 5;
                break;
            case CHAT_RECEIVE:
                rawType = 6;
                break;
            case ANONYMOUS_CHAT_RECEIVE:
                rawType = 7;
                break;
            case SENDCODE:
                rawType = 15;
                break;
            case SENDCODE_REPLY:
                rawType = 16;
                break;
            case LOGIN:
                rawType = 17;
                break;
            case LOGIN_REPLY:
                rawType = 18;
                break;
            case LOGOUT:
                rawType = 19;
                break;
            case LOGOUT_REPLY:
                rawType = 20;
                break;
            case CONTROL:
                rawType = 21;
                break;
            case CONTROL_REPLY:
                rawType = 22;
                break;
            case NOTIFY:
                rawType = 24;
                break;
        }
        return rawType;
    }

    public static void resetPacketId() {
        sNextPacketId = 0L;
    }

    @Override
    public String toString() {
        if (mPacketBody != null) {
            return "BasePacket{" +
                    "mVersion=" + mVersion +
                    ", mType=" + mType +
                    ", mPacketId=" + mPacketId +
                    ", mPacketBody=" + new String(mPacketBody) +
                    '}';
        }
        return "";

    }

    public long getVersion() {
        return mVersion;
    }

    public void setVersion(long version) {
        mVersion = version;
    }

    public PacketType getPacketType() {
        return mType;
    }

    public void setPacketType(PacketType type) {
        mType = type;
    }

    public long getPacketId() {
        return mPacketId;
    }

    public void setPacketId(long packetId) {
        mPacketId = packetId;
    }

    public byte[] getPacketBody() {
        return mPacketBody;
    }

    public void setPacketBody(byte[] packetBody) {
        mPacketBody = packetBody;
    }
}
