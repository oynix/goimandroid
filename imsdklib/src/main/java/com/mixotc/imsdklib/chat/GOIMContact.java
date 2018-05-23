package com.mixotc.imsdklib.chat;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.mixotc.imsdklib.RemoteConfig;
import com.mixotc.imsdklib.database.table.ContactTable;
import com.mixotc.imsdklib.database.table.TempContactTable;
import com.mixotc.imsdklib.utils.CharacterParserUtils;

import org.json.JSONObject;

public class GOIMContact implements Parcelable {

    public long mUid;
    public String mUsername;
    public String mNick;
    public long mGroupId;
    private String mHeader;
    private String mAvatar;
    private String mPhone;
    private String mEmail;
    private String mRegion;

    /** 0 for unknown, 1 for female, 2 for male */
    private int mGender;

    /** 判断是否设置过密码 */
    private boolean mIsNew;

    /** 谷歌验证:0不需要;1未验证;2已验证 */
    private int mGoogleAuth;

    /** 是否认证: 0 未认证; 1 已上传; 2 已审核 3 认证未通过 4 恶意认证 */
    private int mVerify;

    /** 绑定银行卡  0 未申请 1 申请中 2 申请通过 3 申请未通过 4 恶意绑定 */
    private int mBtVerify;

    /** 认证时间 当在状态为 4 时，利用这个时间来判断是否已过3天 */
    private int mVerifyTime;

    /** 绑定时间 当在状态为 4 时，利用这个时间来判断是否已过3天 */
    private int mBtVerifyTime;

    public static final Creator<GOIMContact> CREATOR = new Creator<GOIMContact>() {
        @Override
        public GOIMContact createFromParcel(Parcel source) {
            return new GOIMContact(source);
        }

        @Override
        public GOIMContact[] newArray(int size) {
            return new GOIMContact[size];
        }
    };

    public GOIMContact(long uid, String username) {
        this(uid, username, null);
    }

    public GOIMContact(long uid, String username, String nickname) {
        this(uid, username, nickname, -1);
    }

    protected GOIMContact(Parcel in) {
        readFromParcel(in);
    }

    public GOIMContact(long uid, String username, String nickname, long groupid) {
        mUid = uid;
        mUsername = username;
        mNick = nickname;
        mGroupId = groupid;
        mAvatar = "";
        mPhone = "";
        mGender = 0;
        mRegion = "";
        mIsNew = false;
        mGoogleAuth = 1;
        mVerify = 0;
        mBtVerify = 0;
    }

    public GOIMContact() {
        mUid = -1;
        mUsername = "";
        mNick = "";
        mGroupId = -1;
        mAvatar = "";
        mPhone = "";
        mEmail = "";
        mGender = 0;
        mRegion = "";
        mIsNew = false;
        mGoogleAuth = 1;
        mVerify = 0;
        mBtVerify = 0;
        mBtVerifyTime = 0;
        mVerifyTime = 0;
    }

    public void setUsername(String username) {
        mUsername = username;
    }

    public String getUsername() {
        return mUsername;
    }

    public void setNick(String nick) {
        mNick = nick;
    }

    public String getNick() {
        if (TextUtils.isEmpty(mNick)) {
            return mUsername;
        }
        return mNick;
    }

    // 判断该用户是否已经实名认证（审核中属于未认证）
    public boolean isCertified() {
        return mVerify == 2;
    }

    public void setAvatar(String avatar) {
        mAvatar = avatar;
    }

    public String getAvatar() {
        return mAvatar;
    }

    public void setPhone(String phone) {
        mPhone = phone;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setGender(int gender) {
        mGender = gender;
    }

    public int getGender() {
        return mGender;
    }

    public void setRegion(String region) {
        mRegion = region;
    }

    public String getRegion() {
        return mRegion;
    }

    public boolean isNew() {
        return mIsNew;
    }

    public void setIsNew(boolean isNew) {
        mIsNew = isNew;
    }

    public int getmGoogleAuth() {
        return mGoogleAuth;
    }

    public void setmGoogleAuth(int mGoogleAuth) {
        this.mGoogleAuth = mGoogleAuth;
    }

    public int getmVerify() {
        return mVerify;
    }

    public void setmVerify(int mVerify) {
        this.mVerify = mVerify;
    }

    public int getBtVerify() {
        return mBtVerify;
    }

    public void setBtVerify(int mBtVerify) {
        this.mBtVerify = mBtVerify;
    }

    public int compare(GOIMContact contact) {
        return getNick().compareTo(contact.getNick());
    }

    public long getUid() {
        return mUid;
    }

    public void setUid(long uid) {
        mUid = uid;
    }

    public long getGroupId() {
        return mGroupId;
    }

    public void setGroupId(long gid) {
        mGroupId = gid;
    }

    public String getHeader() {
        if (mHeader == null) {
            mHeader = CharacterParserUtils.getInstance().getHeader(getNick());
        }
        return mHeader;
    }

    public String getAvatarUrl() {
        if (!TextUtils.isEmpty(mAvatar)) {
            return RemoteConfig.AVATAR_DOWNLOAD_URL + mAvatar + RemoteConfig.PARAM_OF_THUMB;
        }
        return null;
    }

    public int getVerifyTime() {
        return mVerifyTime;
    }

    public void setVerifyTime(int mVerifyTime) {
        this.mVerifyTime = mVerifyTime;
    }

    public int getBtVerifyTime() {
        return mBtVerifyTime;
    }

    public void setBtVerifyTime(int mBtVerifyTime) {
        this.mBtVerifyTime = mBtVerifyTime;
    }

    public void updateBaseInfo(GOIMContact contact) {
        setNick(contact.getNick());
        setUsername(contact.getUsername());
        setAvatar(contact.getAvatar());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public void readFromParcel(Parcel parcel) {
        mUid = parcel.readLong();
        mUsername = parcel.readString();
        mNick = parcel.readString();
        mGroupId = parcel.readLong();
        mHeader = parcel.readString();
        mAvatar = parcel.readString();
        mPhone = parcel.readString();
        mEmail = parcel.readString();
        mGender = parcel.readInt();
        mRegion = parcel.readString();
        mIsNew = (parcel.readInt() == 1);
        mGoogleAuth = parcel.readInt();
        mVerify = parcel.readInt();
        mBtVerify = parcel.readInt();
        mVerifyTime = parcel.readInt();
        mBtVerifyTime = parcel.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.mUid);
        dest.writeString(this.mUsername);
        dest.writeString(this.mNick);
        dest.writeLong(this.mGroupId);
        dest.writeString(this.mHeader);
        dest.writeString(this.mAvatar);
        dest.writeString(this.mPhone);
        dest.writeString(this.mEmail);
        dest.writeInt(this.mGender);
        dest.writeString(this.mRegion);
        dest.writeInt(this.mIsNew ? 1 : 0);
        dest.writeInt(this.mGoogleAuth);
        dest.writeInt(this.mVerify);
        dest.writeInt(this.mBtVerify);
        dest.writeInt(this.mVerifyTime);
        dest.writeInt(this.mBtVerifyTime);
    }

    @Override
    public String toString() {
        return "GOIMContact{" +
                "mUid=" + mUid +
                ", mUsername='" + mUsername + '\'' +
                ", mNick='" + mNick + '\'' +
                ", mGroupId=" + mGroupId +
                ", mHeader='" + mHeader + '\'' +
                ", mAvatar='" + mAvatar + '\'' +
                ", mPhone='" + mPhone + '\'' +
                ", mEmail='" + mEmail + '\'' +
                ", mGender=" + mGender +
                ", mRegion='" + mRegion + '\'' +
                ", mIsNew=" + mIsNew +
                ", mGoogleAuth=" + mGoogleAuth +
                ", mVerify=" + mVerify +
                ", mBtVerify=" + mBtVerify +
                ", mVerifyTime=" + mVerifyTime +
                ", mBtVerifyTime=" + mBtVerifyTime +
                '}';
    }

    // TODO: 2018/4/19 constructor需要重构

    /** 从临时联系人表李创建实例 */
    public static GOIMContact createFromTempCursor(Cursor cursor) {
        long uid = cursor.getLong(cursor.getColumnIndex(TempContactTable.USER_ID));
        String username = cursor.getString(cursor.getColumnIndex(TempContactTable.USER_NAME));
        String nick = cursor.getString(cursor.getColumnIndex(TempContactTable.NICK_NAME));
        long gid = cursor.getLong(cursor.getColumnIndex(TempContactTable.GROUP_ID));
        String avatar = cursor.getString(cursor.getColumnIndex(TempContactTable.AVATAR));
        String phone = cursor.getString(cursor.getColumnIndex(TempContactTable.PHONE));
        String email = cursor.getString(cursor.getColumnIndex(TempContactTable.EMAIL));
        int gender = cursor.getInt(cursor.getColumnIndex(TempContactTable.GENDER));
        String region = cursor.getString(cursor.getColumnIndex(TempContactTable.REGION));

        GOIMContact contact = new GOIMContact(uid, username, nick, gid);
        contact.setAvatar(avatar);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setGender(gender);
        contact.setRegion(region);
        return contact;
    }

    /** 从联系人表里创建实例 */
    public static GOIMContact createFromCursor(Cursor cursor) {
        long uid = cursor.getLong(cursor.getColumnIndex(ContactTable.USER_ID));
        String username = cursor.getString(cursor.getColumnIndex(ContactTable.USER_NAME));
        String nick = cursor.getString(cursor.getColumnIndex(ContactTable.NICK_NAME));
        long gid = cursor.getLong(cursor.getColumnIndex(ContactTable.GROUP_ID));
        String avatar = cursor.getString(cursor.getColumnIndex(ContactTable.AVATAR));
        String phone = cursor.getString(cursor.getColumnIndex(ContactTable.PHONE));
        String email = cursor.getString(cursor.getColumnIndex(ContactTable.EMAIL));
        int gender = cursor.getInt(cursor.getColumnIndex(ContactTable.GENDER));
        String region = cursor.getString(cursor.getColumnIndex(ContactTable.REGION));

        GOIMContact contact = new GOIMContact(uid, username, nick, gid);
        contact.setAvatar(avatar);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setGender(gender);
        contact.setRegion(region);
        return contact;
    }

    /** 从json中创建实例 */
    public static GOIMContact createFromJson(JSONObject jsonData) {
        long uid = jsonData.optLong("id", -1);
        long gid = jsonData.optLong("gid", -1);
        String username = jsonData.optString("name", "");
        String phone = jsonData.optString("phone", "");
        String email = jsonData.optString("email", "");
        String nick = jsonData.optString("nick");
        String avatar = jsonData.optString("icon", "");

        GOIMContact contact = new GOIMContact(uid, username, nick);
        contact.setAvatar(avatar);
        contact.setPhone(phone);
        contact.setEmail(email);
        contact.setGroupId(gid);
        return contact;
    }

    /** 创建登录用户实例 */
    public static GOIMContact createLoginUser(JSONObject data, String phone, String email) {
        long uid = data.optLong("uid", -1);
        String name = data.optString("name", "");
        String nick = data.optString("nick", "");
        String avatar = data.optString("icon", "");
        int isNewInt = data.optInt("is_new", 0);
        int googleAuth = data.optInt("auth", 1);
        boolean isNew = (isNewInt != 0);
        int verify = data.optInt("verify", 0);
        int btVerify = data.optInt("btverify", 0);
        int verifyTime = data.optInt("verifytime", 0);
        int btVerifyTime = data.optInt("bt_verifytime", 0);

        GOIMContact lastLoginUser = new GOIMContact(uid, name);
        lastLoginUser.setNick(nick);
        lastLoginUser.setAvatar(avatar);
        lastLoginUser.setPhone(phone);
        lastLoginUser.setEmail(email);
        lastLoginUser.setIsNew(isNew);
        lastLoginUser.setmGoogleAuth(googleAuth);
        lastLoginUser.setmVerify(verify);
        lastLoginUser.setBtVerify(btVerify);
        lastLoginUser.setVerifyTime(verifyTime);
        lastLoginUser.setBtVerifyTime(btVerifyTime);
        return lastLoginUser;
    }
}
