package com.mixotc.imsdklib.message;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Pair;

import com.mixotc.imsdklib.chat.GOIMContact;
import com.mixotc.imsdklib.database.table.ChatTable;
import com.mixotc.imsdklib.remotechat.RemoteDBManager;
import com.mixotc.imsdklib.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class GOIMMessage implements Parcelable, Cloneable {
    public static final String IS_NOTIFY_KEY = "is_notify";
    public static final String NOTIFY_TYPE_KEY = "notify_type";
    public static final String NOTIFY_ID_KEY = "notify_id";
    private static final String TAG = "msg";

    private Type mType;
    private Direct mDirect;
    private Status mStatus = Status.CREATE;
    private GOIMContact mContact;
    private long mGroupId;
    private MessageBody mBody;
    private String mMsgId;
    private boolean mUnread = true;
    private boolean mIsAcked = false;
    private boolean mIsDelivered = false;
    private boolean mIsListened = false;
    private long mMsgTime;
    private Hashtable<String, Object> mAttributes = new Hashtable<>();
    private int mProgress = 0;
    private int mError = 0;

    public static final Creator<GOIMMessage> CREATOR = new Creator<GOIMMessage>() {
        public GOIMMessage createFromParcel(Parcel parcel) {
            return new GOIMMessage(parcel);
        }

        public GOIMMessage[] newArray(int size) {
            return new GOIMMessage[size];
        }
    };

    GOIMMessage(Type type) {
        mType = type;
        mMsgTime = System.currentTimeMillis();
    }

    public Type getType() {
        return mType;
    }

    public void setType(Type type) {
        mType = type;
    }

    public Direct getDirect() {
        return mDirect;
    }

    public void setDirect(Direct direct) {
        mDirect = direct;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public GOIMContact getContact() {
        return mContact;
    }

    public void setContact(GOIMContact contact) {
        mContact = contact;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setGroupId(long groupId) {
        mGroupId = groupId;
    }

    public MessageBody getBody() {
        return mBody;
    }

    public void addBody(MessageBody messageBody) {
        mBody = messageBody;
    }

    public String getMsgId() {
        return mMsgId;
    }

    public void setMsgId(String msgId) {
        mMsgId = msgId;
    }

    public boolean isUnread() {
        return mUnread;
    }

    public void setUnread(boolean unread) {
        mUnread = unread;
    }

    public boolean isAcked() {
        return mIsAcked;
    }

    public void setAcked(boolean acked) {
        mIsAcked = acked;
    }

    public boolean isDelivered() {
        return mIsDelivered;
    }

    public void setDelivered(boolean delivered) {
        mIsDelivered = delivered;
    }

    public boolean isListened() {
        return mIsListened;
    }

    public void setListened(boolean listened) {
        mIsListened = listened;
    }

    public long getMsgTime() {
        return mMsgTime;
    }

    public void setMsgTime(long msgTime) {
        mMsgTime = msgTime;
    }

    public Hashtable getAttributes() {
        return mAttributes;
    }

    public void setAttribute(String key, Object value) {
        if (mAttributes == null) {
            mAttributes = new Hashtable();
        }
        mAttributes.put(key, value);
    }

    public Object getAttribute(String key) {
        if ((mAttributes != null) && (mAttributes.containsKey(key))) {
            return mAttributes.get(key);
        }
        return null;
    }

    public void setProgress(int progress) {
        mProgress = progress;
    }

    public int getProgress() {
        return mProgress;
    }

    void setError(int error) {
        mError = error;
    }

    public int getError() {
        return mError;
    }

    public String toString() {
        return "msg{contact:" +
                (mContact == null ? "" : mContact.mUsername) +
                " body:" +
                mBody.toString();
    }

    public static GOIMMessage createSendMessage(Type type) {
        GOIMMessage message = new GOIMMessage(type);
        message.mDirect = Direct.SEND;
        message.mContact = new GOIMContact();
        message.setMsgId(GOIMMessageUtils.getUniqueMessageId());
        return message;
    }

    public static GOIMMessage createReceiveMessage(Type type) {
        GOIMMessage message = new GOIMMessage(type);
        message.mDirect = Direct.RECEIVE;
        message.mContact = new GOIMContact();
        message.setMsgId(GOIMMessageUtils.getUniqueMessageId());
        return message;
    }

    public static GOIMMessage createTxtSendMessage(String text, long groupId) {
        if (text.length() > 0) {
            GOIMMessage message = createSendMessage(Type.TXT);
            TextMessageBody textMessageBody = new TextMessageBody(text);
            message.addBody(textMessageBody);
            message.setGroupId(groupId);
            return message;
        }
        Logger.e(TAG, "text content size must be greater than 10");
        return null;
    }

    public static GOIMMessage createVoiceSendMessage(String filePath, int length, long groupId) {
        if (!new File(filePath).exists()) {
            Logger.e(TAG, "voice file does not exsit");
            return null;
        }
        GOIMMessage message = createSendMessage(Type.VOICE);
        VoiceMessageBody voiceMessageBody = new VoiceMessageBody(new File(filePath), length);
        message.addBody(voiceMessageBody);
        message.setGroupId(groupId);
        return message;
    }

    public static GOIMMessage createImageSendMessage(String filePath, boolean sendOriginalImage, long groupId) {
        if (!new File(filePath).exists()) {
            Logger.e(TAG, "image file does not exsit");
            return null;
        }
        GOIMMessage message = createSendMessage(Type.IMAGE);
        message.setGroupId(groupId);
        ImageMessageBody imageMessageBody = new ImageMessageBody(new File(filePath));
        imageMessageBody.setSendOriginalImage(sendOriginalImage);
        message.addBody(imageMessageBody);
        return message;
    }

    public static GOIMMessage createVideoSendMessage(String filePath, String localThumb, int length, long groupId) {
        File file = new File(filePath);
        if (!file.exists()) {
            Logger.e(TAG, "video file does not exist");
            return null;
        }
        GOIMMessage message = createSendMessage(Type.VIDEO);
        message.setGroupId(groupId);
        VideoMessageBody videoMessageBody = new VideoMessageBody(file, length);
        message.addBody(videoMessageBody);
        return message;
    }

    public static GOIMMessage createLocationSendMessage(double latitude, double longitude, String address, long groupId) {
        GOIMMessage message = createSendMessage(Type.LOCATION);
        LocationMessageBody locationMessageBody = new LocationMessageBody(address, latitude, longitude);
        message.addBody(locationMessageBody);
        message.setGroupId(groupId);
        return message;
    }

    public static GOIMMessage createFileSendMessage(String filePath, long groupId) {
        File file = new File(filePath);
        if (!file.exists()) {
            Logger.e(TAG, "file does not exist");
            return null;
        }
        GOIMMessage message = createSendMessage(Type.FILE);
        message.setGroupId(groupId);
        NormalFileMessageBody normalFileMessageBody = new NormalFileMessageBody(new File(filePath));
        message.addBody(normalFileMessageBody);
        return message;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel parcel, int flag) {
        parcel.writeString(mType.name());
        parcel.writeString(mDirect.name());
        parcel.writeString(mStatus.name());
        parcel.writeParcelable(mContact, flag);
        parcel.writeLong(mGroupId);
        parcel.writeParcelable(mBody, flag);
        parcel.writeString(mMsgId);
        parcel.writeInt(mUnread ? 1 : 0);
        parcel.writeInt(mIsAcked ? 1 : 0);
        parcel.writeInt(mIsDelivered ? 1 : 0);
        parcel.writeInt(mIsListened ? 1 : 0);
        parcel.writeLong(mMsgTime);
        List<Pair<String, JSONObject>> jsonObjectList = new ArrayList<>();
        List<Pair<String, JSONArray>> jsonArrayList = new ArrayList<>();
        Hashtable<String, Object> hashtable = new Hashtable<>();
        Iterator iterator = mAttributes.entrySet().iterator();
        Object value;
        while (iterator.hasNext()) {
            value = iterator.next();
            if (((Entry) value).getValue() != null) {
                if ((((Entry) value).getValue() instanceof JSONObject)) {
                    jsonObjectList.add(Pair.create((String) ((Entry) value).getKey(), (JSONObject) ((Entry) value).getValue()));
                } else if ((((Entry) value).getValue() instanceof JSONArray)) {
                    jsonArrayList.add(Pair.create((String) ((Entry) value).getKey(), (JSONArray) ((Entry) value).getValue()));
                } else {
                    hashtable.put((String) ((Entry) value).getKey(), ((Entry) value).getValue());
                }
            }
        }
        parcel.writeInt(jsonObjectList.size());
        for (Pair<String, JSONObject> aJsonObjectList : jsonObjectList) {
            parcel.writeString(aJsonObjectList.first);
            parcel.writeString(aJsonObjectList.second.toString());
        }
        parcel.writeInt(jsonArrayList.size());
        for (Pair<String, JSONArray> aJsonArrayList : jsonArrayList) {
            parcel.writeString(aJsonArrayList.first);
            parcel.writeString(aJsonArrayList.second.toString());
        }
        parcel.writeMap(hashtable);
    }

    private GOIMMessage(Parcel parcel) {
        mType = Type.valueOf(parcel.readString());
        mDirect = Direct.valueOf(parcel.readString());
        mStatus = Status.valueOf(parcel.readString());
        mContact = ((GOIMContact) parcel.readParcelable(GOIMContact.class.getClassLoader()));
        mGroupId = parcel.readLong();
        mBody = ((MessageBody) parcel.readParcelable(MessageBody.class.getClassLoader()));
        mMsgId = parcel.readString();
        mUnread = (parcel.readInt() == 1);
        mIsAcked = (parcel.readInt() == 1);
        mIsDelivered = (parcel.readInt() == 1);
        mIsListened = (parcel.readInt() == 1);
        mMsgTime = parcel.readLong();
        mAttributes = new Hashtable<>();
        int count = parcel.readInt();
        for (int j = 0; j < count; j++) {
            String str = parcel.readString();
            JSONObject jsonObject = null;
            try {
                jsonObject = new JSONObject(str);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            mAttributes.put(str, jsonObject);
        }
        count = parcel.readInt();
        for (int k = 0; k < count; k++) {
            String str = parcel.readString();
            JSONArray jsonArray = null;
            try {
                jsonArray = new JSONArray(str);
            } catch (JSONException jsonException) {
                jsonException.printStackTrace();
            }
            mAttributes.put(str, jsonArray);
        }
        Hashtable hashtable = new Hashtable();
        parcel.readMap(hashtable, null);
        mAttributes.putAll(hashtable);
    }

    public Object clone()
            throws CloneNotSupportedException {
        return super.clone();
    }

    public enum Direct {
        SEND, RECEIVE
    }

    public enum Status {
        SUCCESS, FAIL, INPROGRESS, CREATE;

        public static Status getStatusFromOrdinal(int ordinal) {
            Status result = SUCCESS;
            if (ordinal == SUCCESS.ordinal()) {
                result = SUCCESS;
            } else if (ordinal == FAIL.ordinal()) {
                result = FAIL;
            } else if (ordinal == INPROGRESS.ordinal()) {
                result = INPROGRESS;
            } else if (ordinal == CREATE.ordinal()) {
                result = CREATE;
            }
            return result;
        }
    }

    public enum Type {
        UNKNOWN, TXT, IMAGE, VIDEO, LOCATION, VOICE, FILE, PACKET, TRANSFER, SECURETRANS
    }

    public static GOIMMessage createFromCursor(Cursor cursor) {
        String msgBody = cursor.getString(cursor.getColumnIndex(ChatTable.MSG_BODY));
        GOIMMessage message = MessageEncoder.getMsgFromJson(msgBody);
        if (message == null) {
            return null;
        }
        message.setMsgId(cursor.getString(cursor.getColumnIndex(ChatTable.MSG_ID)));
        message.setMsgTime(cursor.getLong(cursor.getColumnIndex(ChatTable.MSG_TIME)));
        int direct = cursor.getInt(cursor.getColumnIndex(ChatTable.MSG_DIR));
        if (direct == GOIMMessage.Direct.SEND.ordinal()) {
            message.setDirect(GOIMMessage.Direct.SEND);
        } else {
            message.setDirect(GOIMMessage.Direct.RECEIVE);
        }
        int status = cursor.getInt(cursor.getColumnIndex(ChatTable.STATUS));
        message.setStatus(Status.getStatusFromOrdinal(status));
        int acked = cursor.getInt(cursor.getColumnIndex(ChatTable.IS_ACK));
        message.setAcked(acked == 1);
        int delivered = cursor.getInt(cursor.getColumnIndex(ChatTable.IS_DELIVERED));
        message.setDelivered(delivered == 1);
        int listened = cursor.getInt(cursor.getColumnIndex(ChatTable.IS_LISTENED));
        message.setListened(listened == 1);
        message.setUnread(false);
        long groupId = cursor.getLong(cursor.getColumnIndex(ChatTable.GROUP_ID));
        message.setGroupId(groupId);
        long userId = cursor.getLong(cursor.getColumnIndex(ChatTable.PARTICIPANT));
        GOIMContact contact = RemoteDBManager.getInstance().getTempContactById(userId, groupId);
        if (contact != null) {
            message.setContact(contact);
        } else {
            message.setContact(new GOIMContact(userId, "", "", groupId));
        }
        return message;
    }
}
