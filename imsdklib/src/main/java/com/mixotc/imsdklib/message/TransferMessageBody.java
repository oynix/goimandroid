package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

public class TransferMessageBody extends MessageBody implements Parcelable {
    public enum Status {
        UNKNOWN,
        UNTAKEN,
        TAKEN,
        TIMEOUT,
        RETURNED
    }

    private long mTransferId;
    private long mFrom;
    private long mTo;
    private String mCurrency;
    private float mAmount;
    private String mInfo;
    private Status mStatus = Status.UNTAKEN;

    public static final Creator<TransferMessageBody> CREATOR = new Creator<TransferMessageBody>() {
        public TransferMessageBody createFromParcel(Parcel parcel) {
            return new TransferMessageBody(parcel);
        }

        public TransferMessageBody[] newArray(int size) {
            return new TransferMessageBody[size];
        }
    };

    public TransferMessageBody(long transferId, String currency, float amount, String info, long from, long to) {
        mTransferId = transferId;
        mCurrency = currency;
        mAmount = amount;
        mInfo = info;
        mFrom = from;
        mTo = to;
    }

    public long getTransferId() {
        return mTransferId;
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

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public long getFrom() {
        return mFrom;
    }

    public long getTo() {
        return mTo;
    }

    public String toString() {
        return "transferId:" + mTransferId + ",currency:" + mCurrency + ",amount:" + mAmount + ",info:" + mInfo + ",from:" + mFrom + ",to:" + mTo;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        parcel.writeLong(mTransferId);
        parcel.writeLong(mFrom);
        parcel.writeLong(mTo);
        parcel.writeString(mCurrency);
        parcel.writeFloat(mAmount);
        parcel.writeString(mInfo);
        parcel.writeInt(mStatus.ordinal());
    }

    private TransferMessageBody(Parcel parcel) {
        mTransferId = parcel.readLong();
        mFrom = parcel.readLong();
        mTo = parcel.readLong();
        mCurrency = parcel.readString();
        mAmount = parcel.readFloat();
        mInfo = parcel.readString();
        mStatus = statusFromOrdinal(parcel.readInt());
    }

    public Status statusFromOrdinal(int status) {
        if (status == Status.UNTAKEN.ordinal()) {
            return Status.UNTAKEN;
        } else if (status == Status.TAKEN.ordinal()) {
            return Status.TAKEN;
        } else if (status == Status.TIMEOUT.ordinal()) {
            return Status.TIMEOUT;
        } else if (status == Status.RETURNED.ordinal()) {
            return Status.RETURNED;
        }
        return Status.UNKNOWN;
    }
}
