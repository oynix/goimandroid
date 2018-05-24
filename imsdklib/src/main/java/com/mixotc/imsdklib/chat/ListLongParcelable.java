package com.mixotc.imsdklib.chat;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by junnikokuki on 2017/12/29.
 */

public class ListLongParcelable implements Parcelable {
    private List<Long> mData = new ArrayList<>();

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int p) {
        parcel.writeInt(mData.size());
        if (mData.size() > 0) {
            long[] values = new long[mData.size()];
            for (int i = 0; i < mData.size(); i++) {
                values[i] = mData.get(i);
            }

            parcel.writeLongArray(values);
        }
    }

    private ListLongParcelable(Parcel parcel) {
        int size = parcel.readInt();
        if (size > 0) {
            long[] values = new long[size];
            parcel.readLongArray(values);
            for (int i = 0; i < size; i++) {
                mData.add(values[i]);
            }
        }
    }

    public ListLongParcelable(List<Long> data) {
        mData.clear();
        mData.addAll(data);
    }

    public ListLongParcelable(long data) {
        mData.clear();
        mData.add(data);
    }

    public List<Long> getData() {
        return mData;
    }

    public static final Creator<ListLongParcelable> CREATOR = new Creator<ListLongParcelable>() {
        public ListLongParcelable createFromParcel(Parcel parcel) {
            return new ListLongParcelable(parcel);
        }

        public ListLongParcelable[] newArray(int size) {
            return new ListLongParcelable[size];
        }
    };
}
