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

    protected BasePacket(Parcel in) {
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
    public void writeToParcel(Parcel dest, int flags) {
    }
}
