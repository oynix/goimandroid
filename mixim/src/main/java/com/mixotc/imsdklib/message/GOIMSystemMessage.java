package com.mixotc.imsdklib.message;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONException;
import org.json.JSONObject;

import static com.mixotc.imsdklib.database.table.SysMsgTable.MSG_ATTR;
import static com.mixotc.imsdklib.database.table.SysMsgTable.MSG_TEXT;
import static com.mixotc.imsdklib.database.table.SysMsgTable.MSG_TIME;
import static com.mixotc.imsdklib.database.table.SysMsgTable.MSG_TYPE;
import static com.mixotc.imsdklib.database.table.SysMsgTable.UNREAD;

/**
 * Created by junnikokuki on 2017/9/29.
 */

public class GOIMSystemMessage implements Parcelable {
    public enum SystemMessageType {
        UNKNOWN,
        REQUESTFRIEND;

        public static SystemMessageType getType(int ordinal) {
            SystemMessageType type = REQUESTFRIEND;
            if (ordinal == UNKNOWN.ordinal()) {
                type = UNKNOWN;
            }
            return type;
        }

    }

    private long mMsgTime;
    private String mMsgText;
    private JSONObject mMsgAttributes;
    private SystemMessageType mMsgType;
    private boolean mUnRead;

    public static final Creator<GOIMSystemMessage> CREATOR = new Creator<GOIMSystemMessage>() {
        public GOIMSystemMessage createFromParcel(Parcel parcel) {
            return new GOIMSystemMessage(parcel);
        }

        public GOIMSystemMessage[] newArray(int size) {
            return new GOIMSystemMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(mMsgTime);
        parcel.writeString(mMsgText);
        parcel.writeString(mMsgAttributes.toString());
        parcel.writeInt(mMsgType.ordinal());
        parcel.writeInt(mUnRead ? 1 : 0);
    }

    public GOIMSystemMessage(Parcel parcel) {
        mMsgTime = parcel.readLong();
        mMsgText = parcel.readString();
        try {
            mMsgAttributes = new JSONObject(parcel.readString());
        } catch (JSONException e) {
            mMsgAttributes = new JSONObject();
        }
        int type = parcel.readInt();
        mMsgType = SystemMessageType.UNKNOWN;
        if (type == SystemMessageType.REQUESTFRIEND.ordinal()) {
            mMsgType = SystemMessageType.REQUESTFRIEND;
        }
        int unread = parcel.readInt();
        mUnRead = (unread == 1);
    }

    public GOIMSystemMessage(long msgTime, String msgText, SystemMessageType msgType, boolean unRead) {
        mMsgTime = msgTime;
        mMsgText = msgText;
        mMsgAttributes = new JSONObject();
        mMsgType = msgType;
        mUnRead = unRead;
    }

    public long getMsgTime() {
        return mMsgTime;
    }

    public void setMsgTime(long time) {
        mMsgTime = time;
    }

    public String getMsgText() {
        return mMsgText;
    }

    public SystemMessageType getMsgType() {
        return mMsgType;
    }

    public boolean unRead() {
        return mUnRead;
    }

    public void setUnRead(boolean read) {
        mUnRead = read;
    }

    public JSONObject getMsgAttributes() {
        return mMsgAttributes;
    }

    private void setMsgAttributes(String json) {
        try {
            mMsgAttributes = new JSONObject(json);
        } catch (JSONException e) {
            mMsgAttributes = new JSONObject();
        }
    }

    public static GOIMSystemMessage createFromCursor(Cursor cursor) {
        long msgTime = cursor.getLong(cursor.getColumnIndex(MSG_TIME));
        String msgText = cursor.getString(cursor.getColumnIndex(MSG_TEXT));
        String attributes = cursor.getString(cursor.getColumnIndex(MSG_ATTR));
        int msgTypeInt = cursor.getInt(cursor.getColumnIndex(MSG_TYPE));
        GOIMSystemMessage.SystemMessageType msgType = GOIMSystemMessage.SystemMessageType.getType(msgTypeInt);
        int msgUnread = cursor.getInt(cursor.getColumnIndex(UNREAD));

        GOIMSystemMessage systemMessage = new GOIMSystemMessage(msgTime, msgText, msgType, msgUnread == 1);
        systemMessage.setMsgAttributes(attributes);
        return systemMessage;
    }

}
