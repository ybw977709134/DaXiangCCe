package com.daxiangce123.android.push.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * @project DaXiangCe
 * @time Jul 12, 2014
 * @author ram
 */
public class UpdatePush extends Push {

	@SerializedName("albumid")
	private String albumId;

	@SerializedName("albumname")
	private String albumName;

	@SerializedName("badge")
	private int count;

	public UpdatePush(Parcel in) {
		super(in);
		albumId = in.readString();
		albumName = in.readString();
		count = in.readInt();
	}

	public UpdatePush() {

	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[albumId		" + albumId + "]");
		builder.append("\n");
		builder.append("[albumName		" + albumName + "]");
		builder.append("\n");
		builder.append("[count			" + count + "]");
		builder.append("\n");
		return builder.toString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(albumId);
		out.writeString(albumName);
		out.writeInt(count);
	}

	public static final Parcelable.Creator<UpdatePush> CREATOR = new Parcelable.Creator<UpdatePush>() {

		@Override
		public UpdatePush createFromParcel(Parcel source) {
			return new UpdatePush(source);
		}

		@Override
		public UpdatePush[] newArray(int size) {
			return new UpdatePush[size];
		}

	};

}
