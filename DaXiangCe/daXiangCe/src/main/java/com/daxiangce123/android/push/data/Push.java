package com.daxiangce123.android.push.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.push.PushType;
import com.google.gson.annotations.SerializedName;

/**
 * @project DaXiangCe
 * @time Jul 12, 2014
 * @author ram
 */
public class Push implements Parcelable {

	@SerializedName("loc-key")
	private PushType type = PushType.DEFAULT;
	@SerializedName("userid")
	private String uid;
	@SerializedName("username")
	private String name;

	public Push(Parcel in) {
		uid = in.readString();
		name = in.readString();
		type = PushType.valueOf(in.readString());
	}

	public Push() {
	}

	public PushType getType() {
		return type;
	}

	public void setType(PushType type) {
		this.type = type;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[Type		" + type + "]");
		builder.append("\n");
		builder.append("[uid		" + uid + "]");
		builder.append("\n");
		builder.append("[name		" + name + "]");
		builder.append("\n");
		return builder.toString();
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(uid);
		out.writeString(name);
		out.writeString(type.toString());
	}

	@Override
	public int describeContents() {
		return 0;
	}
}
