package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.mixotc.imsdklib.utils.FileUtils;

import java.io.File;

public class NormalFileMessageBody extends FileMessageBody implements Parcelable {
    private long mFileSize;
    private String mFileName;
    public static final Creator<NormalFileMessageBody> CREATOR = new Creator<NormalFileMessageBody>() {
        public NormalFileMessageBody createFromParcel(Parcel parcel) {
            return new NormalFileMessageBody(parcel);
        }

        public NormalFileMessageBody[] newArray(int size) {
            return new NormalFileMessageBody[size];
        }
    };

    public NormalFileMessageBody(File file) {
        mLocalUrl = file.getAbsolutePath();
        mMime = FileUtils.getMIMEType(file.getAbsolutePath());
        mFileName = file.getName();
        mFileSize = file.length();
    }

    public NormalFileMessageBody(String remoteId, String filename, String mime, long fileSize) {
        mRemoteId = remoteId;
        mFileName = filename;
        mMime = mime;
        mFileSize = fileSize;
    }

    public String getFileName() {
        return mFileName;
    }

    public void setFileName(String fileName) {
        mFileName = fileName;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int unknown) {
        super.writeToParcel(parcel, unknown);
        parcel.writeLong(mFileSize);
        parcel.writeString(mFileName);
    }

    public NormalFileMessageBody() {
    }

    private NormalFileMessageBody(Parcel parcel) {
        super(parcel);
        mFileSize = parcel.readLong();
        mFileName = parcel.readString();
    }

    public String toString() {
        return "normal file:" + mFileName + ",mime:" + mMime + ",localUrl:" + mLocalUrl + ",mRemoteId:" + mRemoteId + ",file size:" + mFileSize;
    }

    public long getFileSize() {
        return mFileSize;
    }
}
