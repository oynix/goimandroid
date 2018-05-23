package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;

import com.mixotc.imsdklib.listener.RemoteCallBack;

public class FileMessageBody extends MessageBody implements Parcelable {
    public transient RemoteCallBack mDownloadCallback = null;
    public transient boolean mDownloaded = false;
    protected String mMime = null;
    protected String mLocalUrl = null;
    protected String mRemoteId = null;
//    String mSecret = null;

    public static final Creator<FileMessageBody> CREATOR = new Creator<FileMessageBody>() {
        public FileMessageBody createFromParcel(Parcel parcel) {
            return new FileMessageBody(parcel);
        }

        public FileMessageBody[] newArray(int size) {
            return new FileMessageBody[size];
        }
    };

    public FileMessageBody() {
    }

    public void setDownloadCallback(RemoteCallBack callBack) {
        if (mDownloaded) {
            try {
                callBack.onProgress(100, null);
                callBack.onSuccess(null);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            return;
        }
        mDownloadCallback = callBack;
    }

    public String getMime() {
        return mMime;
    }

    public void setMime(String mime) {
        mMime = mime;
    }

    public String getLocalUrl() {
        return mLocalUrl;
    }

    public void setLocalUrl(String localUrl) {
        mLocalUrl = localUrl;
    }

    public String getRemoteId() {
        return mRemoteId;
    }

    public void setRemoteId(String remoteId) {
        mRemoteId = remoteId;
    }

//    public void setSecret(String secret) {
//        mSecret = secret;
//    }
//
//    public String getSecret() {
//        return mSecret;
//    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int unknown) {
        parcel.writeString(mMime);
        parcel.writeString(mLocalUrl);
        parcel.writeString(mRemoteId);
//        parcel.writeString(mSecret);
    }

    protected FileMessageBody(Parcel parcel) {
        mMime = parcel.readString();
        mLocalUrl = parcel.readString();
        mRemoteId = parcel.readString();
//        mSecret = parcel.readString();
    }
}
