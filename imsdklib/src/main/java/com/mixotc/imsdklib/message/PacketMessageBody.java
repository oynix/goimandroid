package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

public class PacketMessageBody extends MessageBody implements Parcelable {
    public static enum Status {
        UNKNOWN,
        LIVE,
        TAKEN,
        TIMEOUT,
        TAKEUP
    }

    private long mPacketId;
    private String mCurrency;
    private float mAmount;
    private String mInfo;
    private int mCount;
    private Status mStatus = Status.LIVE;
    private float mMyAmount = 0;

    public static final Creator<PacketMessageBody> CREATOR = new Creator<PacketMessageBody>() {
        public PacketMessageBody createFromParcel(Parcel parcel) {
            return new PacketMessageBody(parcel);
        }

        public PacketMessageBody[] newArray(int size) {
            return new PacketMessageBody[size];
        }
    };

    public PacketMessageBody(long packetId, String currency, float amount, String info, int count) {
        mPacketId = packetId;
        mCurrency = currency;
        mAmount = amount;
        mInfo = info;
        mCount = count;
    }

    public long getPacketId() {
        return mPacketId;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public float getAmount() {
        return mAmount;
    }

    public String getInfo() {
        return mInfo;
    }

    public int getCount() {
        return mCount;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public float getMyAmount() {
        return mMyAmount;
    }

    public void setMyAmount(float myAmount) {
        mMyAmount = myAmount;
    }

    public String toString() {
        return "packetId:" + mPacketId + ",currency:" + mCurrency + ",amount:" + mAmount + ",info:" + mInfo + ",count:" + mCount + ",myamount:" + mMyAmount;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        parcel.writeLong(mPacketId);
        parcel.writeString(mCurrency);
        parcel.writeFloat(mAmount);
        parcel.writeString(mInfo);
        parcel.writeInt(mCount);
        parcel.writeInt(mStatus.ordinal());
        parcel.writeFloat(mMyAmount);
    }

    private PacketMessageBody(Parcel parcel) {
        mPacketId = parcel.readLong();
        mCurrency = parcel.readString();
        mAmount = parcel.readFloat();
        mInfo = parcel.readString();
        mCount = parcel.readInt();
        mStatus = statusFromOrdinal(parcel.readInt());
        mMyAmount = parcel.readFloat();
    }

    public Status statusFromOrdinal(int status) {
        if (status == Status.LIVE.ordinal()) {
            return Status.LIVE;
        } else if (status == Status.TAKEN.ordinal()) {
            return Status.TAKEN;
        } else if (status == Status.TIMEOUT.ordinal()) {
            return Status.TIMEOUT;
        } else if (status == Status.TAKEUP.ordinal()) {
            return Status.TAKEUP;
        }
        return Status.UNKNOWN;
    }
}
