package com.daxiangce123.android.util.outh;

import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;

/**
 * @project Pickup
 * @time 2013-11-13
 * @author ram
 */
public class QQHelper extends OauthHelper {

	private final static String TAG = "QQHelper";
	private Tencent mTencent;
	private int shareType = QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT;
	private final String APP_KEY = "101027006";
	private IUiListener listener = new IUiListener() {
		@Override
		public void onCancel() {
			oauthFailed(Oauth.TYPE_QQ, null);
		}

		@Override
		public void onError(UiError uiError) {
			oauthFailed(Oauth.TYPE_QQ, uiError);
			if (App.DEBUG) {
				LogUtil.d(TAG, "onComplete()  UiError " + uiError.errorCode
						+ " errorMessage=" + uiError.errorMessage
						+ " errorDetail=" + uiError.errorDetail);
			}
		}

		@Override
		public void onComplete(Object arg0) {
			if (App.DEBUG) {
				LogUtil.d(TAG, "onComplete()  arg0 is " + arg0);
			}
			try {
				parserToken((JSONObject) arg0);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public QQHelper() {
		mTencent = Tencent.createInstance(APP_KEY, App.getActivity());
	}

	/**
	 * {@link #onActivityResult(int, int, Intent)} is not called
	 */
	@Override
	public void oauth() {
		if (mTencent == null) {
			oauthFailed(Oauth.TYPE_QQ, null);
			return;
		}
		if (App.DEBUG) {
			LogUtil.d(TAG, "oauth!!!!!!!!!!!!");
		}
		try {
			if (App.getActivity() instanceof Activity) {
				Activity activity = App.getActivity();
				mTencent.logout(activity);
				mTencent.login(activity, "all", listener);
				// mTencent.loginWithOEM(activity, "all", listener, "10000144",
				// "10000144", "xxxx");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent
	 data) {
	 if (mTencent != null) {
	 mTencent.onActivityResult(requestCode, resultCode, data);
	 }
	 super.onActivityResult(requestCode, resultCode, data);
	 }

	@SuppressLint("SdCardPath")
	public void shareToQQSpace(String title, String summary, String targetURL,
			String imageUrl) {// String id
		try {
			Bundle bundle = new Bundle();
			bundle.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE, shareType);
			bundle.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL,
			// " http://dev.cliq123.com/sharelink?link=" + link);
					targetURL);
			bundle.putString(QzoneShare.SHARE_TO_QQ_TITLE, title);
			bundle.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, summary);
			bundle.putString(QzoneShare.SHARE_TO_QQ_IMAGE_URL, imageUrl);
			// "/sdcard/eclipse_logo_colour.png");
			// "http://img3.cache.netease.com/photo/0005/2013-03-07/8PBKS8G400BV0005.jpg");
			// " http://dev.cliq123.com/share/shareimg?link=" + link);
			mTencent.shareToQQ(App.getActivity(), bundle, listener);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void parserToken(JSONObject arg0) {
		try {
			String token = arg0.getString("access_token");
			Oauth oauth = new Oauth(Oauth.TYPE_QQ, token);
			if (App.DEBUG) {
				LogUtil.d(TAG, "parserToken()	" + arg0);
			}
			if (oauth.isValid()) {
				oauthSucceed(oauth);
				return;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		oauthFailed(Oauth.TYPE_QQ, null);
	}

}
