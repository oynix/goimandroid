package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

import com.mixotc.imsdklib.utils.FileUtils;
import com.mixotc.imsdklib.utils.Logger;

import java.io.File;

public class ImageMessageBody extends FileMessageBody implements Parcelable {
    private boolean mSendOriginalImage;
    public static final Creator<ImageMessageBody> CREATOR = new Creator<ImageMessageBody>() {
        public ImageMessageBody createFromParcel(Parcel parcel) {
            return new ImageMessageBody(parcel);
        }

        public ImageMessageBody[] newArray(int size) {
            return new ImageMessageBody[size];
        }
    };

    public ImageMessageBody() {
    }

    public ImageMessageBody(File file) {
        mLocalUrl = file.getAbsolutePath();
        mMime = FileUtils.getMIMEType(file.getAbsolutePath());
        Logger.d("imagemsg", "create image message body for:" + file.getAbsolutePath());
    }

    public ImageMessageBody(String remoteId, String mime) {
        mRemoteId = remoteId;
        mMime = mime;
    }

    public String toString() {
        return "image: mime:" + mMime + ",localurl:" + mLocalUrl + ",remoteId:" + mRemoteId;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        super.writeToParcel(parcel, paramInt);
    }

    private ImageMessageBody(Parcel parcel) {
        super(parcel);
    }

    public void setSendOriginalImage(boolean sendOriginalImage) {
        mSendOriginalImage = sendOriginalImage;
    }

    public boolean isSendOriginalImage() {
        return mSendOriginalImage;
    }
}
