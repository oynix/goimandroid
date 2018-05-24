package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

public class SecuredTransferMessageBody extends MessageBody implements Parcelable {
    public enum Status {
        UNKNOWN,
        AWAITPAYMENT, //1
        PAID,         //2
        COMPLAIN,     //3
        CANCELLED,    //4
        TIMEOUT,      //5
        COINTRANSFERED,//6
        BUYERRATED,    //7
        SELLERRATED,   //8
        RATED          //9
    }

    private long mTransferId;
    private long mFrom;
    private long mTo;
    private String mCurrency;
    private float mAmount;
    private int mDays;
    private String mInfo;
    private Status mStatus = Status.UNKNOWN;

    public static final Creator<SecuredTransferMessageBody> CREATOR = new Creator<SecuredTransferMessageBody>() {
        public SecuredTransferMessageBody createFromParcel(Parcel parcel) {
            return new SecuredTransferMessageBody(parcel);
        }

        public SecuredTransferMessageBody[] newArray(int size) {
            return new SecuredTransferMessageBody[size];
        }
    };

    public SecuredTransferMessageBody(long transferId, String currency, float amount, int days, String info, long from, long to) {
        mTransferId = transferId;
        mCurrency = currency;
        mAmount = amount;
        mDays = days;
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

    public int getDays() {
        return mDays;
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
        return "transferId:" + mTransferId + ",currency:" + mCurrency + ",amount:" + mAmount + ",days:" + mDays + ",info:" + mInfo + ",from:" + mFrom + ",to:" + mTo;
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
        parcel.writeInt(mDays);
        parcel.writeString(mInfo);
        parcel.writeInt(mStatus.ordinal());
    }

    private SecuredTransferMessageBody(Parcel parcel) {
        mTransferId = parcel.readLong();
        mFrom = parcel.readLong();
        mTo = parcel.readLong();
        mCurrency = parcel.readString();
        mAmount = parcel.readFloat();
        mDays = parcel.readInt();
        mInfo = parcel.readString();
        mStatus = statusFromOrdinal(parcel.readInt());
    }

    public static Status toSecuredStatus(int serverStatus) {
        Status toStatus = Status.UNKNOWN;
        if (serverStatus == 1) {
            toStatus = Status.AWAITPAYMENT;
        } else if (serverStatus == 2) {
            toStatus = Status.PAID;
        } else if (serverStatus == 3) {
            toStatus = Status.COMPLAIN;
        } else if (serverStatus == 4) {
            toStatus = Status.CANCELLED;
        } else if (serverStatus == 5) {
            toStatus = Status.TIMEOUT;
        } else if (serverStatus == 6) {
            toStatus = Status.COINTRANSFERED;
        } else if (serverStatus == 7) {
            toStatus = Status.BUYERRATED;
        } else if (serverStatus == 8) {
            toStatus = Status.SELLERRATED;
        } else if (serverStatus == 9) {
            toStatus = Status.RATED;
        }
        return toStatus;
    }

    public static int toServerStatus(Status status) {
        int toStatus = -1;
        switch (status) {
            case AWAITPAYMENT:
                toStatus = 1;
                break;
            case PAID:
                toStatus = 2;
                break;
            case COMPLAIN:
                toStatus = 3;
                break;
            case CANCELLED:
                toStatus = 4;
                break;
            case TIMEOUT:
                toStatus = 5;
                break;
            case COINTRANSFERED:
                toStatus = 6;
                break;
            case BUYERRATED:
                toStatus = 7;
                break;
            case SELLERRATED:
                toStatus = 8;
                break;
            case RATED:
                toStatus = 9;
                break;
        }
        return toStatus;
    }

    public Status statusFromOrdinal(int status) {
        if (status == Status.AWAITPAYMENT.ordinal()) {
            return Status.AWAITPAYMENT;
        } else if (status == Status.PAID.ordinal()) {
            return Status.PAID;
        } else if (status == Status.COMPLAIN.ordinal()) {
            return Status.COMPLAIN;
        } else if (status == Status.CANCELLED.ordinal()) {
            return Status.CANCELLED;
        } else if (status == Status.TIMEOUT.ordinal()) {
            return Status.TIMEOUT;
        } else if (status == Status.COINTRANSFERED.ordinal()) {
            return Status.COINTRANSFERED;
        } else if (status == Status.BUYERRATED.ordinal()) {
            return Status.BUYERRATED;
        } else if (status == Status.SELLERRATED.ordinal()) {
            return Status.SELLERRATED;
        } else if (status == Status.RATED.ordinal()) {
            return Status.RATED;
        }
        return Status.UNKNOWN;
    }
}
