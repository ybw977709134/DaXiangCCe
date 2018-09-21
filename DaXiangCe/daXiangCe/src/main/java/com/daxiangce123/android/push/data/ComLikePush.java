package com.daxiangce123.android.push.data;

import android.os.Parcel;

import com.google.gson.annotations.SerializedName;

/**
 * @project DaXiangCe
 * @time 2014-7-15
 * @author
 */
public class ComLikePush extends Push {

	@SerializedName("fileid")
	private String fileId;

	public ComLikePush(Parcel parcel) {
		super(parcel);
		fileId = parcel.readString();
	}

	public ComLikePush() {
		super();
	}

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[fileId		" + fileId + "]\n");
		return builder.toString();
	}

	@Override
	public int describeContents() {
		return super.describeContents();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(fileId);
	}

}
