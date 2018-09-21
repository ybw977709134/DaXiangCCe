package com.daxiangce123.android.push.data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @project DaXiangCe
 * @time Jul 12, 2014
 * @author ram
 */
public class LikePush extends ComLikePush {

	public LikePush(Parcel source) {
		super(source);
	}

	public LikePush() {

	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		super.writeToParcel(out, flags);
	}

	public static Parcelable.Creator<LikePush> CREATOR = new Parcelable.Creator<LikePush>() {

		@Override
		public LikePush createFromParcel(Parcel source) {
			return new LikePush(source);
		}

		@Override
		public LikePush[] newArray(int size) {
			return new LikePush[size];
		}

	};
}
