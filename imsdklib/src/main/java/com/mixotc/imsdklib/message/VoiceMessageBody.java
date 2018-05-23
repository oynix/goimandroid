package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.mixotc.imsdklib.utils.FileUtils;
import com.mixotc.imsdklib.utils.Logger;

import java.io.File;

public class VoiceMessageBody extends FileMessageBody implements Parcelable {
    private int mLength = 0;
    public static final Creator<VoiceMessageBody> CREATOR = new Creator<VoiceMessageBody>() {
        public VoiceMessageBody createFromParcel(Parcel parcel) {
            return new VoiceMessageBody(parcel);
        }

        public VoiceMessageBody[] newArray(int size) {
            return new VoiceMessageBody[size];
        }
    };

    public VoiceMessageBody(File file, int length) {
        mLocalUrl = file.getAbsolutePath();
        mMime = FileUtils.getMIMEType(file.getAbsolutePath());
        mLength = length;
        Logger.d("voicemsg", "create voice, message body for:" + file.getAbsolutePath());
    }

    public VoiceMessageBody(String remoteId, String mime, int length) {
        mRemoteId = remoteId;
        mMime = mime;
        mLength = length;
    }

    public int getLength() {
        return mLength;
    }

    public String toString() {
        return "voice: mime:" + mMime + ",localurl:" + mLocalUrl + ",remoteId:" + mRemoteId + ",length:" + mLength;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int unknown) {
        super.writeToParcel(parcel, unknown);
        parcel.writeInt(mLength);
    }

    private VoiceMessageBody(Parcel parcel) {
        super(parcel);
        mLength = parcel.readInt();
    }
}
