package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;

public class SimpleAlbumItems implements Parcelable {
	protected final static String TAG = "AlbumItems";
	private int size;
	private int limit;
	private boolean hasMore;
	private boolean DEBUG = true;

	public SimpleAlbumItems() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public SimpleAlbumItems(Parcel parcel) {
		if (parcel == null) {
			return;
		}
		limit = parcel.readInt();
		size = parcel.readInt();
		boolean[] bools = new boolean[1];
		parcel.readBooleanArray(bools);
		if (bools != null && bools.length > 1) {
			hasMore = bools[0];
		}
	}

	public SimpleAlbumItems(int limit, boolean hasMore, int size) {
		this.size = size;
		this.limit = limit;
		this.hasMore = hasMore;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	// ////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(limit);
		dest.writeInt(size);
		boolean[] bools = { hasMore };
		dest.writeBooleanArray(bools);
	}

	public static final Parcelable.Creator<SimpleAlbumItems> CREATOR = new Parcelable.Creator<SimpleAlbumItems>() {
		public SimpleAlbumItems createFromParcel(Parcel parcel) {
			return new SimpleAlbumItems(parcel);
		}

		public SimpleAlbumItems[] newArray(int size) {
			return new SimpleAlbumItems[size];
		}
	};
}
