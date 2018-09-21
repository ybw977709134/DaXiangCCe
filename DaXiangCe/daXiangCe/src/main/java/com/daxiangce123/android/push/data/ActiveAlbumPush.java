package com.daxiangce123.android.push.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * @project DaXiangCe
 * @time Jul 12, 2014
 * @author ram
 */
public class ActiveAlbumPush extends Push {

	@SerializedName("albumid")
	private String albumId;

	@SerializedName("invitecode")
	private String invitecode;

	@SerializedName("message")
	private String message;

	public ActiveAlbumPush(Parcel in) {
		super(in);
		albumId = in.readString();
		invitecode = in.readString();
		message = in.readString();
	}

	public ActiveAlbumPush() {

	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumInviteCode() {
		return invitecode;
	}

	public void setAlbumInviteCode(String invitecode) {
		this.invitecode = invitecode;
	}

	public String getProductionMsg() {
		return message;
	}

	public void setProductionMsg(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[albumId		" + albumId + "]");
		builder.append("\n");
		builder.append("[invitecode		" + invitecode + "]");
		builder.append("\n");
		builder.append("[message			" + message + "]");
		builder.append("\n");
		return builder.toString();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(albumId);
		out.writeString(invitecode);
		out.writeString(message);
	}

	public static final Parcelable.Creator<ActiveAlbumPush> CREATOR = new Parcelable.Creator<ActiveAlbumPush>() {

		@Override
		public ActiveAlbumPush createFromParcel(Parcel source) {
			return new ActiveAlbumPush(source);
		}

		@Override
		public ActiveAlbumPush[] newArray(int size) {
			return new ActiveAlbumPush[size];
		}

	};

}
