package com.mixotc.imsdklib.chat;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import com.mixotc.imsdklib.RemoteConfig;

import static com.mixotc.imsdklib.database.table.FriendRequestTable.AVATAR;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.INFO;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.REQ_TIME;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.RESPONSE;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.USER_ID;
import static com.mixotc.imsdklib.database.table.FriendRequestTable.USER_NAME;

/**
 * Created by junnikokuki on 2017/9/26.
 */

public class GOIMFriendRequest implements Parcelable {
    public enum GOIMFriendResponse {
        PENDING,
        AGREED,
        REFUSED;

        public static GOIMFriendResponse getResponse(int ordinal) {
            GOIMFriendResponse response = PENDING;
            if (ordinal == AGREED.ordinal()) {
                response = AGREED;
            } else if (ordinal == REFUSED.ordinal()) {
                response = REFUSED;
            }
            return response;
        }
    }

    private long mUid;
    private String mUsername;
    private String mAvatar;
    private String mRequestInfo;
    private long mRequestTime;
    private GOIMFriendResponse mResponse;

    public static final Creator<GOIMFriendRequest> CREATOR = new Creator<GOIMFriendRequest>() {
        public GOIMFriendRequest createFromParcel(Parcel parcel) {
            return new GOIMFriendRequest(parcel);
        }

        public GOIMFriendRequest[] newArray(int size) {
            return new GOIMFriendRequest[size];
        }
    };

    protected GOIMFriendRequest() {
        mUid = -1;
        mUsername = "";
        mAvatar = "";
        mRequestInfo = "";
        mRequestTime = 0;
        mResponse = GOIMFriendResponse.PENDING;
    }

    public GOIMFriendRequest(long uid, String username, String avatar, String info, long reqTime) {
        mUid = uid;
        mUsername = username;
        mAvatar = avatar;
        mRequestInfo = info;
        mRequestTime = reqTime;
        mResponse = GOIMFriendResponse.PENDING;
    }

    public GOIMFriendResponse getResponse() {
        return mResponse;
    }

    public void setResponse(GOIMFriendResponse response) {
        mResponse = response;
    }

    public String getUsername() {
        return mUsername;
    }

    public long getUid() {
        return mUid;
    }

    public String getRequestInfo() {
        return mRequestInfo;
    }

    public long getRequestTime() {
        return mRequestTime;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public String getAvatarUrl() {
        if (mAvatar != null && !mAvatar.equals("")) {
            return RemoteConfig.AVATAR_DOWNLOAD_URL + mAvatar + RemoteConfig.PARAM_OF_THUMB;
        }
        return null;
    }

    public void writeToParcel(Parcel parcel, int paramInt) {
        parcel.writeLong(mUid);
        parcel.writeString(mUsername);
        parcel.writeString(mAvatar);
        parcel.writeString(mRequestInfo);
        parcel.writeLong(mRequestTime);
        parcel.writeInt(mResponse.ordinal());
    }

    private GOIMFriendRequest(Parcel parcel) {
        mUid = parcel.readLong();
        mUsername = parcel.readString();
        mAvatar = parcel.readString();
        mRequestInfo = parcel.readString();
        mRequestTime = parcel.readLong();
        int response = parcel.readInt();
        mResponse = GOIMFriendResponse.PENDING;
        if (response == GOIMFriendResponse.AGREED.ordinal()) {
            mResponse = GOIMFriendResponse.AGREED;
        } else if (response == GOIMFriendResponse.REFUSED.ordinal()) {
            mResponse = GOIMFriendResponse.REFUSED;
        }
    }

    public int describeContents() {
        return 0;
    }

    public static GOIMFriendRequest createFromCursor(Cursor cursor) {
        long userId = cursor.getLong(cursor.getColumnIndex(USER_ID));
        String username = cursor.getString(cursor.getColumnIndex(USER_NAME));
        String avatar = cursor.getString(cursor.getColumnIndex(AVATAR));
        String info = cursor.getString(cursor.getColumnIndex(INFO));
        long reqTime = cursor.getLong(cursor.getColumnIndex(REQ_TIME));
        int response = cursor.getInt(cursor.getColumnIndex(RESPONSE));
        GOIMFriendRequest.GOIMFriendResponse rep = GOIMFriendRequest.GOIMFriendResponse.getResponse(response);
        GOIMFriendRequest request = new GOIMFriendRequest(userId, username, avatar, info, reqTime);
        request.setResponse(rep);
        return request;
    }
}
