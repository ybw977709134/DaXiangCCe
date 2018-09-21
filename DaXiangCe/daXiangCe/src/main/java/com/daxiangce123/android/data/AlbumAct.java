package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

public class AlbumAct implements Parcelable {

	private String albumId;
	private String userId;
	private int pictureNum;
	private int videoNum;
	private int commentNum;
	private int likeNum;

	public AlbumAct() {
	}

	public AlbumAct(Parcel parcel) {
		albumId = parcel.readString();
		userId = parcel.readString();
		pictureNum = parcel.readInt();
		videoNum = parcel.readInt();
		commentNum = parcel.readInt();
		likeNum = parcel.readInt();
	}

	public AlbumAct(String albumId, String userId, int pictures,
			int video, int comments, int likes) {

		this.albumId = albumId;
		this.userId = userId;
		this.pictureNum = pictures;
		this.videoNum = video;
		this.commentNum = comments;
		this.likeNum = likes;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setPictureNum(int pictures) {
		this.pictureNum = pictures;
	}

	public int getPictureNum() {
		return pictureNum;
	}

	public void setVideoNum(int video) {
		this.videoNum = video;
	}

	public int getVideoNum() {
		return videoNum;
	}

	public void setCommentNum(int comments) {
		this.commentNum = comments;
	}

	public int getCommentNum() {
		return commentNum;
	}

	public void setLikeNum(int likes) {
		this.likeNum = likes;
	}

	public int getLikeNum() {
		return likeNum;
	}

	@Override
	public String toString() {
		StringBuilder dest = new StringBuilder();
		dest.append("[albumId]		" + albumId + "\n");
		dest.append("[userId]	" + userId + "\n");
		dest.append("[pictureNum]	" + pictureNum + "\n");
		dest.append("[videoNum]	" + videoNum + "\n");
		dest.append("[commentNum]	" + commentNum + "\n");
		dest.append("[likeNum]	" + likeNum + "\n");
		return dest.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(albumId);
		dest.writeString(userId);
		dest.writeInt(pictureNum);
		dest.writeInt(videoNum);
		dest.writeInt(commentNum);
		dest.writeInt(likeNum);
	}

	public final static Parcelable.Creator<AlbumAct> CREATOR = new Creator<AlbumAct>() {

		@Override
		public AlbumAct[] newArray(int size) {
			return new AlbumAct[size];
		}

		@Override
		public AlbumAct createFromParcel(Parcel source) {
			return new AlbumAct(source);
		}
	};

}
