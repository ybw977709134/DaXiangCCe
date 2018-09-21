package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

public class UserInfo implements Parcelable {

    private String id;
    private String name;
    private String email;
    private boolean emailVerified;
    private String mobile;
    private boolean mobileVerified;
    private String create_date;
    private String mod_date;
    private String level;
    private String status;
    private String gender;
    private String lang;
    private String quota;

    public UserInfo() {
    }

    public UserInfo(Parcel parcel) {
        id = parcel.readString();
        name = parcel.readString();
        email = parcel.readString();
        mobile = parcel.readString();
        create_date = parcel.readString();
        mod_date = parcel.readString();
        level = parcel.readString();
        status = parcel.readString();
        gender = parcel.readString();
        quota = parcel.readString();
        boolean[] bools = {emailVerified, mobileVerified};
        parcel.readBooleanArray(bools);
        if (bools == null || bools.length != 2) {
            return;
        }
        emailVerified = bools[0];
        mobileVerified = bools[1];
    }

    public UserInfo(String id, String name, String email,
                    boolean emailVerified, String mobile, boolean mobileVerified,
                    String createDate, String modDate, String level, String status,
                    String gender, String lang, String quota) {

        this.id = id;
        this.name = name;
        this.email = email;
        this.emailVerified = emailVerified;
        this.mobile = mobile;
        this.mobileVerified = mobileVerified;
        this.create_date = createDate;
        this.mod_date = modDate;
        this.level = level;
        this.status = status;
        this.gender = gender;
        this.lang = lang;
        this.quota = quota;

    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmailVerified(boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean getEmailVerified() {
        return emailVerified;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobileVerified(boolean mobileVerified) {
        this.mobileVerified = mobileVerified;
    }

    public boolean getMobileVerified() {
        return mobileVerified;
    }

    public void setCreateDate(String createDate) {
        this.create_date = createDate;
    }

    public String getCreateDate() {
        return create_date;
    }

    public void setModDate(String modDate) {
        this.mod_date = modDate;
    }

    public String getModDate() {
        return mod_date;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getLevel() {
        return level;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getGender() {
        return gender;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getLang() {
        return lang;
    }

    public void setQuota(String quota) {
        this.quota = quota;
    }

    public String getQuota() {
        return quota;
    }

    @Override
    public String toString() {
        StringBuilder dest = new StringBuilder();
        dest.append("[id]		" + id + "\n");
        dest.append("[name]	" + name + "\n");
        dest.append("[email]	" + email + "\n");
        dest.append("[emailVerify]	" + emailVerified + "\n");
        dest.append("[mobile]	" + mobile + "\n");
        dest.append("[mobileVerify]	" + mobileVerified + "\n");
        dest.append("[create]	" + create_date + "\n");
        dest.append("[modify]	" + mod_date + "\n");
        dest.append("[level]	" + level + "\n");
        dest.append("[status]	" + status + "\n");
        dest.append("[gender]	" + gender + "\n");
        dest.append("[quota]	" + quota);
        return dest.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(email);
        dest.writeString(mobile);
        dest.writeString(create_date);
        dest.writeString(mod_date);
        dest.writeString(level);
        dest.writeString(status);
        dest.writeString(gender);
        dest.writeString(quota);
        boolean[] bools = {emailVerified, mobileVerified};
        dest.writeBooleanArray(bools);
    }

    public final static Parcelable.Creator<UserInfo> CREATOR = new Creator<UserInfo>() {

        @Override
        public UserInfo[] newArray(int size) {
            return new UserInfo[size];
        }

        @Override
        public UserInfo createFromParcel(Parcel source) {
            return new UserInfo(source);
        }
    };

    public boolean isBindMobile() {
        return (getMobile() != null && (!getMobile().equals("")));
    }
}
