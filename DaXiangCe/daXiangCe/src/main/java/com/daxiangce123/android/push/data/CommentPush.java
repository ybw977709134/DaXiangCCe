package com.daxiangce123.android.push.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

/**
 * @project DaXiangCe
 * @time Jul 12, 2014
 * @author ram
 */
public class CommentPush extends ComLikePush {

	@SerializedName("body")
	private String comment;

	public CommentPush(Parcel parcel) {
		super(parcel);
		comment = parcel.readString();
	}

	public CommentPush() {
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder(super.toString());
		builder.append("[comment		" + comment + "]");
		return builder.toString();
	}

	@Override
	public int describeContents() {
		return super.describeContents();
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
		out.writeString(comment);
	}

	public static final Parcelable.Creator<CommentPush> CREATOR = new Parcelable.Creator<CommentPush>() {

		@Override
		public CommentPush createFromParcel(Parcel source) {
			return new CommentPush(source);
		}

		@Override
		public CommentPush[] newArray(int size) {
			return new CommentPush[size];
		}

	};

}
