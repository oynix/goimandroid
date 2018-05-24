package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.mixotc.imsdklib.utils.FileUtils;
import com.mixotc.imsdklib.utils.Logger;

import java.io.File;

public class VideoMessageBody extends FileMessageBody implements Parcelable {
    private int mLength = 0;
    private long mFileSize = 0L;
    public static final Creator<VideoMessageBody> CREATOR = new Creator<VideoMessageBody>() {
        public VideoMessageBody[] newArray(int size) {
            return new VideoMessageBody[size];
        }

        public VideoMessageBody createFromParcel(Parcel parcel) {
            return new VideoMessageBody(parcel);
        }
    };

    public VideoMessageBody() {
    }

    public VideoMessageBody(File file, int length) {
        mLocalUrl = file.getAbsolutePath();
        mMime = FileUtils.getMIMEType(file.getAbsolutePath());
        mLength = length;
        mFileSize = file.length();
        Logger.d("videomsg", "create video,message body for:" + file.getAbsolutePath());
    }

    public VideoMessageBody(String remoteId, String mime, int length, long fileSize) {
        mRemoteId = remoteId;
        mMime = mime;
        mLength = length;
        mFileSize = fileSize;
    }

    public long getFileSize() {
        return mFileSize;
    }

    public int getLength() {
        return mLength;
    }

    public String toString() {
        return "video: mime:" + mMime + ",localUrl:" + mLocalUrl + ",mRemoteId:" + mRemoteId + ",length:" + mLength;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        super.writeToParcel(parcel, paramInt);
        parcel.writeInt(mLength);
        parcel.writeLong(mFileSize);
    }

    private VideoMessageBody(Parcel parcel) {
        super(parcel);
        mLength = parcel.readInt();
        mFileSize = parcel.readLong();
    }
}
