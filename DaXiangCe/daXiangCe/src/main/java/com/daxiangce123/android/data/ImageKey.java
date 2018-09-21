package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.util.Utils;

/**
 * @project Pickup
 * @time Feb 12, 2014
 * @author ram
 */
public class ImageKey implements Parcelable {

	private String path;
	private ImageSize size;
	private String Tag;

	public ImageKey() {
	}

	public ImageKey(String path, ImageSize size) {
		this.path = path;
		this.size = size;
	}

	public ImageKey(Parcel source) {
	}

	public String getPath() {
		return path;
	}

	public ImageSize getSize() {
		return size;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public void setSize(ImageSize size) {
		this.size = size;
	}

	public String getTag() {
		return Tag;
	}

	public void setTag(String tag) {
		Tag = tag;
	}

	public boolean isValid() {
		if (Utils.isEmpty(path) || size == null || !size.valid()) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[path]	" + path + "\n");
		builder.append("[size]	" + size);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		if (path == null) {
			return 0;
		}
		return path.hashCode();
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImageKey) {
			ImageKey key = (ImageKey) o;
			if (Utils.isEmpty(path)) {
				return super.equals(o);
			}
			boolean result = path.equals(key.getPath());
			if (!result) {
				return false;
			}
			if (size != null && key.getSize() != null) {
				return size.equals(key.getSize());
			} else {
				return true;
			}
		}
		return super.equals(o);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	public static final Parcelable.Creator<ImageKey> CREATOR = new Parcelable.Creator<ImageKey>() {
		public ImageKey createFromParcel(Parcel parcel) {
			return new ImageKey(parcel);
		}

		public ImageKey[] newArray(int size) {
			return new ImageKey[size];
		}
	};
}
