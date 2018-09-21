package com.daxiangce123.android.util.outh;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.tencent.mm.sdk.modelmsg.SendAuth;

/**
 * @project DaXiangCe
 * @time Aug 12, 2014
 * @author ram
 * @deprecated
 */
public class WxTmpToken implements Parcelable {

	public final static String TAG = "WxTmpToken";
	public int errorCode;
	public String code;
	/**
	 * @see {@link SendAuth.Req#state}
	 */
	public String state;

	public WxTmpToken() {
	}

	public WxTmpToken(Parcel source) {
		code = source.readString();
		state = source.readString();
		errorCode = source.readInt();
	}

	public WxTmpToken(com.tencent.mm.sdk.modelmsg.SendAuth.Resp resp) {
		errorCode = resp.errCode;
		code = resp.code;
		state = resp.state;

		if (App.DEBUG) {
			LogUtil.d(TAG, "WxTmpToken	" + toString());
		}
	}

	public WxTmpToken(int errorCode, String code, String state) {
		super();
		this.errorCode = errorCode;
		this.code = code;
		this.state = state;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[errorCode	" + errorCode + "]");
		builder.append("\n");
		builder.append("[code		" + code + "]");
		builder.append("\n");
		builder.append("[state	" + state + "]");
		return builder.toString();
	}

	// ///////////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(code);
		dest.writeString(state);
		dest.writeInt(errorCode);
	}

	public static final Parcelable.Creator<WxTmpToken> CREATOR = new Parcelable.Creator<WxTmpToken>() {

		@Override
		public WxTmpToken[] newArray(int size) {
			return new WxTmpToken[size];
		}

		@Override
		public WxTmpToken createFromParcel(Parcel source) {
			return new WxTmpToken(source);
		}
	};

}
