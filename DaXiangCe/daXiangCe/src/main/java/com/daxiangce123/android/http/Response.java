package com.daxiangce123.android.http;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.Consts;
    //网络请求响应类？
public class Response implements Parcelable {
    public static final String TAG = "Response";

    private int mStatusCode;
    private ErrorCode mErrCode;
    private String mContent;
    private boolean mStatus;
    private Error error;

    public Response() {
        mErrCode = ErrorCode.NONE;
        mStatusCode = Consts.RESPONSE_STATUS_DEFAULT;
        mStatus = false;
    }

    private Response(Parcel parcel) {
        this();
    }

    public void setStatus(boolean status) {
        this.mStatus = status;
    }

    public boolean getStatus() {
        return this.mStatus;
    }

    public void setStatusCode(int statusCode) {
        this.mStatusCode = statusCode;
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public void setContent(String content) {
        this.mContent = content;
    }

    public String getContent() {
        return mContent;
    }

    public void setErrCode(ErrorCode errorCode) {
        this.mErrCode = errorCode;
    }

    public ErrorCode getErrCode() {
        return mErrCode;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String toString() {
        return "Response [status code] " + mStatusCode + " [error code] " + mErrCode + " [content] " + mContent;
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        //
    }

    public static final Parcelable.Creator<Response> CREATOR = new Parcelable.Creator<Response>() {
        public Response createFromParcel(Parcel parcel) {
            return new Response(parcel);
        }

        public Response[] newArray(int size) {
            return new Response[size];
        }
    };
}
