package com.mixotc.imsdklib.chat;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class GOIMChatOptions implements Parcelable {
    private boolean noticedBySound = true;
    private boolean noticedByVibrate = true;
    private boolean notificationEnable = true;
    private boolean useSpeaker = true;
    private int numberOfMessagesLoaded = 20;
    private boolean requireReadAck = false;
    private boolean requireDeliveryAck = false;
    private boolean showNotification = true;
    private Uri ringUri;
    private List<Long> groupsOfNotificationDisabled = new ArrayList<Long>();
    private List<Long> usersOfNotificationDisabled = new ArrayList<Long>();

    public GOIMChatOptions() {
    }

    public boolean getRequireAck() {
        return requireReadAck;
    }

    public void setRequireAck(boolean paramBoolean) {
        requireReadAck = paramBoolean;
    }

    public boolean getRequireDeliveryAck() {
        return requireDeliveryAck;
    }

    public void setRequireDeliveryAck(boolean paramBoolean) {
        requireDeliveryAck = paramBoolean;
    }

    public boolean getNoticedBySound() {
        return noticedBySound;
    }

    public void setNoticeBySound(boolean paramBoolean) {
        noticedBySound = paramBoolean;
    }

    public boolean getNoticedByVibrate() {
        return noticedByVibrate;
    }

    public void setNoticedByVibrate(boolean paramBoolean) {
        noticedByVibrate = paramBoolean;
    }

    public boolean getNotificationEnable() {
        return notificationEnable;
    }

    public void setNotificationEnable(boolean paramBoolean) {
        notificationEnable = paramBoolean;
    }

    public boolean getNotifyBySoundAndVibrate() {
        return notificationEnable;
    }

    public void setNotifyBySoundAndVibrate(boolean paramBoolean) {
        notificationEnable = paramBoolean;
    }

    public boolean getUseSpeaker() {
        return useSpeaker;
    }

    public void setUseSpeaker(boolean paramBoolean) {
        useSpeaker = paramBoolean;
    }

    public void setShowNotificationInBackgroud(boolean paramBoolean) {
        showNotification = paramBoolean;
    }

    public boolean isShowNotificationInBackgroud() {
        return showNotification;
    }

    public void setNotifyRingUri(Uri paramUri) {
        ringUri = paramUri;
    }

    public Uri getNotifyRingUri() {
        return ringUri;
    }

    public int getNumberOfMessagesLoaded() {
        return numberOfMessagesLoaded;
    }

    public void setNumberOfMessagesLoaded(int paramInt) {
        if (paramInt > 0) {
            numberOfMessagesLoaded = paramInt;
        }
    }

    public void setGroupsOfNotificationDisabled(List<Long> paramList) {
        groupsOfNotificationDisabled = paramList;
    }

    public List<Long> getGroupsOfNotificationDisabled() {
        return groupsOfNotificationDisabled;
    }

    public void setUsersOfNotificationDisabled(List<Long> paramList) {
        usersOfNotificationDisabled = paramList;
    }

    public List<Long> getUsersOfNotificationDisabled() {
        return usersOfNotificationDisabled;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(noticedBySound ? 1 : 0);
        parcel.writeInt(noticedByVibrate ? 1 : 0);
        parcel.writeInt(notificationEnable ? 1 : 0);
        parcel.writeInt(useSpeaker ? 1 : 0);
        parcel.writeInt(numberOfMessagesLoaded);
        parcel.writeInt(requireReadAck ? 1 : 0);
        parcel.writeInt(requireDeliveryAck ? 1 : 0);
        parcel.writeInt(showNotification ? 1 : 0);
        parcel.writeParcelable(ringUri, 0);
        parcel.writeParcelable(new ListLongParcelable(groupsOfNotificationDisabled), 0);
        parcel.writeParcelable(new ListLongParcelable(usersOfNotificationDisabled), 0);
    }

    public void readFromParcel(Parcel parcel) {
        noticedBySound = (parcel.readInt() == 1);
        noticedByVibrate = (parcel.readInt() == 1);
        notificationEnable = (parcel.readInt() == 1);
        useSpeaker = (parcel.readInt() == 1);
        numberOfMessagesLoaded = parcel.readInt();
        requireReadAck = (parcel.readInt() == 1);
        requireDeliveryAck = (parcel.readInt() == 1);
        showNotification = (parcel.readInt() == 1);
        ringUri = parcel.readParcelable(Uri.class.getClassLoader());
        ListLongParcelable groups = parcel.readParcelable(ListLongParcelable.class.getClassLoader());
        groupsOfNotificationDisabled = groups.getData();
        ListLongParcelable users = parcel.readParcelable(ListLongParcelable.class.getClassLoader());
        usersOfNotificationDisabled = users.getData();
    }

    private GOIMChatOptions(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static final Creator<GOIMChatOptions> CREATOR = new Creator<GOIMChatOptions>() {
        public GOIMChatOptions createFromParcel(Parcel parcel) {
            return new GOIMChatOptions(parcel);
        }

        public GOIMChatOptions[] newArray(int size) {
            return new GOIMChatOptions[size];
        }
    };
}
