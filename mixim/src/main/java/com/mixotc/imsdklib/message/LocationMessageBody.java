package com.mixotc.imsdklib.message;

import android.os.Parcel;
import android.os.Parcelable;

public class LocationMessageBody extends MessageBody implements Parcelable {
    private String mAddress;
    private double mLatitude;
    private double mLongitude;
    public static final Creator<LocationMessageBody> CREATOR = new Creator<LocationMessageBody>() {
        public LocationMessageBody createFromParcel(Parcel parcel) {
            return new LocationMessageBody(parcel);
        }

        public LocationMessageBody[] newArray(int size) {
            return new LocationMessageBody[size];
        }
    };

    public LocationMessageBody(String address, double latitude, double longitude) {
        mAddress = address;
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public String getAddress() {
        return mAddress;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public String toString() {
        return "location:" + mAddress + ",lat:" + mLatitude + ",lng:" + mLongitude;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        parcel.writeString(mAddress);
        parcel.writeDouble(mLatitude);
        parcel.writeDouble(mLongitude);
    }

    private LocationMessageBody(Parcel parcel) {
        mAddress = parcel.readString();
        mLatitude = parcel.readDouble();
        mLongitude = parcel.readDouble();
    }
}
