package com.daxiangce123.android.http;

import android.os.Parcel;
import android.os.Parcelable;

public class ProgressInfo implements Parcelable {
	public static final String TAG = "ProgressInfo";

	private String mTag;
	private String mType;
	private int mProgress;
	private long received;
	private long speed;

	private ProgressInfo(Parcel parcel) {
		mTag = "";
		mType = "";
		mProgress = 0;
	}

	public ProgressInfo(String tag, String type, int progress) {
		this.mTag = tag;
		this.mType = type;
		this.mProgress = progress;
	}

	public ProgressInfo setTag(String tag) {
		this.mTag = tag;
		return this;
	}

	public String getTag() {
		return this.mTag;
	}

	public ProgressInfo setType(String type) {
		this.mType = type;
		return this;
	}

	public String getType() {
		return this.mType;
	}

	public ProgressInfo setProgress(int progress) {
		this.mProgress = progress;
		return this;
	}

	public int getProgress() {
		return this.mProgress;
	}

	public long getReceived() {
		return received;
	}

	public ProgressInfo setReceived(long received) {
		this.received = received;
		return this;
	}

	/**
	 * KB/s
	 */
	public long getSpeed() {
		return speed;
	}

	/**
	 * KB/s
	 */
	public ProgressInfo setSpeed(long speed) {
		this.speed = speed;
		return this;
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {

	}

	public static final Parcelable.Creator<ProgressInfo> CREATOR = new Parcelable.Creator<ProgressInfo>() {
		public ProgressInfo createFromParcel(Parcel parcel) {
			return new ProgressInfo(parcel);
		}

		public ProgressInfo[] newArray(int size) {
			return new ProgressInfo[size];
		}
	};
}
