package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

public class TextMessageBody extends MessageBody implements Parcelable {
    public String mMessage;
    public static final Creator<TextMessageBody> CREATOR = new Creator<TextMessageBody>() {
        public TextMessageBody createFromParcel(Parcel parcel) {
            return new TextMessageBody(parcel);
        }

        public TextMessageBody[] newArray(int size) {
            return new TextMessageBody[size];
        }
    };

    public TextMessageBody(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public String toString() {
        return "txt:\"" + mMessage + "\"";
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int unknown) {
        parcel.writeString(mMessage);
    }

    private TextMessageBody(Parcel parcel) {
        mMessage = parcel.readString();
    }
}
