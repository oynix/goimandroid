package com.mixotc.imsdklib.chat;

import android.database.Cursor;
import android.os.Parcel;

import com.mixotc.imsdklib.remotechat.RemoteAccountManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static com.mixotc.imsdklib.database.table.GroupTable.DESC;
import static com.mixotc.imsdklib.database.table.GroupTable.GROUP_ID;
import static com.mixotc.imsdklib.database.table.GroupTable.GROUP_NAME;
import static com.mixotc.imsdklib.database.table.GroupTable.IS_BLOCK;
import static com.mixotc.imsdklib.database.table.GroupTable.IS_SINGLE;
import static com.mixotc.imsdklib.database.table.GroupTable.MAX_USERS;
import static com.mixotc.imsdklib.database.table.GroupTable.MEMBERS;
import static com.mixotc.imsdklib.database.table.GroupTable.MODIFY_TIME;
import static com.mixotc.imsdklib.database.table.GroupTable.OWNER;

public class GOIMGroup extends GOIMContact {
    private String mDescription;
    private long mOwner;
    private Hashtable<Long, GOIMContact> mMembers = new Hashtable<>();
    private long mLastModifiedTime;
    private int mMaxUsers = 0;
    private boolean mIsMsgBlocked = false;
    private boolean mIsSingle = true;
    private String mMemberStr = "";

    /**
     * 修改群名称的用户 ID
     */
    private String mUpdaterId;

    public GOIMGroup() {
        mLastModifiedTime = 0L;
        mDescription = "";
        mOwner = -1;
    }

    public GOIMGroup(long gid, String username, String nickname) {
        mUid = gid;
        mUsername = username;
        mNick = nickname;
        mGroupId = gid;
        mLastModifiedTime = 0L;
        mDescription = "";
        mOwner = -1;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public long getOwner() {
        return mOwner;
    }

    public void setOwner(long owner) {
        mOwner = owner;
    }

    public synchronized void addMember(GOIMContact member) {
        mMembers.put(member.mUid, member);
    }

    public synchronized void removeMember(GOIMContact member) {
        mMembers.remove(member.mUid);
    }

    public synchronized GOIMContact getMember(long uid) {
        return mMembers.get(uid);
    }

    public synchronized List<GOIMContact> getMembers() {
        return new ArrayList<>(mMembers.values());
    }

    public int getMemberCount() {
        return mMembers.values().size();
    }

    public GOIMContact getMemberById(long uid) {
        return mMembers.get(uid);
    }

    public GOIMContact getMemberByIndex(int index) {
        List<GOIMContact> members = new ArrayList<>(mMembers.values());
        if (members.size() > index) {
            return members.get(index);
        }
        return null;
    }

    public synchronized void setMembers(Hashtable<Long, GOIMContact> members) {
        mMembers.clear();
        mMembers.putAll(members);
    }

//    public String getMembersStr() {
//        StringBuilder memberStr = new StringBuilder();
//        Collection<GOIMContact> members = getMembers();
//        Iterator iterator = members.iterator();
//        while (iterator.hasNext()) {
//            GOIMContact contact = (GOIMContact) iterator.next();
//            memberStr.append(contact.getUid());
//            if (iterator.hasNext()) {
//                memberStr.append("|");
//            }
//        }
//        return memberStr.toString();
//    }

    public void setMemberStr(String memberStr) {
        mMemberStr = memberStr;
    }

    public String getGroupName() {
        return mUsername;
    }

    public void setGroupName(String groupName) {
        mUsername = groupName;
    }

    public int getMaxUsers() {
        return mMaxUsers;
    }

    public void setMaxUsers(int paramInt) {
        mMaxUsers = paramInt;
    }

    public int getAffiliationsCount() {
        return mMembers.size();
    }

    public boolean isMsgBlocked() {
        return mIsMsgBlocked;
    }

    public void setMsgBlocked(boolean isMsgBlocked) {
        mIsMsgBlocked = isMsgBlocked;
    }

    public boolean isSingle() {
        return mIsSingle;
    }

    public void setIsSingle(boolean isSingle) {
        mIsSingle = isSingle;
    }

    public String getUpdaterId() {
        return mUpdaterId;
    }

    public void setUpdaterId(String updaterId) {
        this.mUpdaterId = updaterId;
    }

    protected void copyModel(GOIMGroup group) {
        mUid = group.mUid;
        mDescription = group.mDescription;
        mLastModifiedTime = System.currentTimeMillis();
        mMembers.clear();
        mMembers.putAll(group.mMembers);
        mNick = group.mNick;
        mOwner = group.mOwner;
        mUsername = group.mUsername;
        mMaxUsers = group.mMaxUsers;
        mIsMsgBlocked = group.mIsMsgBlocked;
        mGroupId = group.mGroupId;
        mIsSingle = group.mIsSingle;
    }

    public long getLastModifiedTime() {
        return mLastModifiedTime;
    }

    public void setLastModifiedTime(long time) {
        mLastModifiedTime = time;
    }

    public static final Creator<GOIMGroup> CREATOR = new Creator<GOIMGroup>() {
        @Override
        public GOIMGroup createFromParcel(Parcel parcel) {
            return new GOIMGroup(parcel);
        }

        @Override
        public GOIMGroup[] newArray(int size) {
            return new GOIMGroup[size];
        }
    };

    @Override
    public void writeToParcel(Parcel parcel, int paramInt) {
        super.writeToParcel(parcel, paramInt);
        parcel.writeString(mDescription);
        parcel.writeLong(mOwner);
//        parcel.writeTypedList(new ArrayList<>(mMembers.values()));
//        parcel.writeList(new ArrayList<>(mMembers.values()));
        parcel.writeLong(mLastModifiedTime);
        parcel.writeInt(mMaxUsers);
        parcel.writeInt(mIsMsgBlocked ? 1 : 0);
        parcel.writeInt(mIsSingle ? 1 : 0);
        parcel.writeInt(mMembers.size());
        for (GOIMContact contact : mMembers.values()) {
            parcel.writeParcelable(contact, paramInt);
        }
        parcel.writeString(mUpdaterId);
    }

    @Override
    public void readFromParcel(Parcel parcel) {
        super.readFromParcel(parcel);
        mDescription = parcel.readString();
        mOwner = parcel.readLong();
//        List<GOIMContact> contacts = new ArrayList<>();
//        parcel.readTypedList(contacts, GOIMContact.CREATOR);
//        parcel.readList(contacts, List.class.getClassLoader());
//        mMembers = new Hashtable<>(contacts.size());
//        for (GOIMContact contact : contacts) {
//            mMembers.put(contact.getUid(), contact);
//        }
        mLastModifiedTime = parcel.readLong();
        mMaxUsers = parcel.readInt();
        mIsMsgBlocked = parcel.readInt() == 1;
        mIsSingle = parcel.readInt() == 1;
        int size = parcel.readInt();
        for (int i = 0; i < size; i++) {
            GOIMContact contact = parcel.readParcelable(GOIMContact.class.getClassLoader());
            mMembers.put(contact.getUid(), contact);
        }
        mUpdaterId = parcel.readString();
    }

    public void removeMemberById(long userId) {
        mMembers.remove(userId);
    }

    private GOIMGroup(Parcel parcel) {
        readFromParcel(parcel);
    }

    public static GOIMGroup createGroupFromCursor(Cursor cursor) {
        String groupName = cursor.getString(cursor.getColumnIndex(GROUP_NAME));
        long gid = cursor.getLong(cursor.getColumnIndex(GROUP_ID));
        GOIMGroup group = new GOIMGroup(gid, groupName, null);
        group.setOwner(cursor.getLong(cursor.getColumnIndex(OWNER)));
        group.setLastModifiedTime(cursor.getLong(cursor.getColumnIndex(MODIFY_TIME)));
        group.setDescription(cursor.getString(cursor.getColumnIndex(DESC)));
        group.setMsgBlocked(cursor.getInt(cursor.getColumnIndex(IS_BLOCK)) == 1);
        group.setMaxUsers(cursor.getInt(cursor.getColumnIndex(MAX_USERS)));
        group.setIsSingle(cursor.getInt(cursor.getColumnIndex(IS_SINGLE)) == 1);
        group.setMemberStr(cursor.getString(cursor.getColumnIndex(MEMBERS)));
        return group;
    }

    /**
     * 某个联系人的基本信息更新，检查该group是否包含该联系人，是则更新
     */
    public void updateMemberBaseInfo(GOIMContact contact) {
        GOIMContact old = mMembers.get(contact.getUid());
        if (old == null) {
            return;
        }
        old.updateBaseInfo(contact);
    }

    public static GOIMGroup createGroupFromJson(JSONObject jsonGroup) {
        if (jsonGroup == null) {
            return null;
        }
        long gid = jsonGroup.optLong("id");
        String groupName = jsonGroup.optString("name");
        String intro = jsonGroup.optString("intro");
        long aid = jsonGroup.optLong("aid");
        JSONArray members = jsonGroup.optJSONArray("members");
        int type = jsonGroup.optInt("type");
        String updaterId = jsonGroup.optString("updater_id");
        GOIMGroup group = new GOIMGroup(gid, groupName, groupName);
        group.setOwner(aid);
        group.setDescription(intro);
        group.setIsSingle(type == 0);
        group.setUpdaterId(updaterId);

        if (members != null && members.length() > 1) {
            Hashtable<Long, GOIMContact> groupMembers = new Hashtable<>();
            for (int j = 0; j < members.length(); j++) {
                JSONObject jsonMember = (JSONObject) members.opt(j);
                if (jsonMember != null) {
                    long uid = jsonMember.optLong("id");
                    if (uid == RemoteAccountManager.getInstance().getLoginUser().getUid()) {
                        continue;//self, pass
                    }
                    String username = jsonMember.optString("name");
                    String nick = jsonMember.optString("nick");
                    String phone = jsonMember.optString("phone", "");
                    String email = jsonMember.optString("email", "");
                    String avatar = jsonMember.optString("icon", "");
                    GOIMContact newContact = new GOIMContact(uid, username, nick);
                    newContact.setAvatar(avatar);
                    newContact.setPhone(phone);
                    newContact.setEmail(email);
                    newContact.setGroupId(gid);
                    groupMembers.put(uid, newContact);
                }
            }

            group.setMembers(groupMembers);
            return group;
        }
        return null;
    }

    public int getGroupMemberSize() {
        return mMembers.size();
    }

}
