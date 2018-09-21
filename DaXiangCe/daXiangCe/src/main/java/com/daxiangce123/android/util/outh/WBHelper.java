package com.daxiangce123.android.util.outh;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.sina.weibo.sdk.api.BaseMediaObject;
import com.sina.weibo.sdk.api.ImageObject;
import com.sina.weibo.sdk.api.TextObject;
import com.sina.weibo.sdk.api.WeiboMultiMessage;
import com.sina.weibo.sdk.api.share.IWeiboShareAPI;
import com.sina.weibo.sdk.api.share.SendMultiMessageToWeiboRequest;
import com.sina.weibo.sdk.api.share.WeiboShareSDK;
import com.sina.weibo.sdk.auth.Oauth2AccessToken;
import com.sina.weibo.sdk.auth.WeiboAuth;
import com.sina.weibo.sdk.auth.WeiboAuthListener;
import com.sina.weibo.sdk.auth.sso.SsoHandler;
import com.sina.weibo.sdk.exception.WeiboException;


/**
 * @project sns_sample
 * @time 2013-8-8
 * @author ram
 */
public class WBHelper extends OauthHelper {
	public final static String APP_KEY = "3512297689";
	public final static String REDICT_URL = "http://www.daxiangce123.com";
	public static final String SCOPE = null;
	private WeiboAuth mWeibo = null;
	private IWeiboShareAPI weiboAPI;
	private Activity mActivity;
	private SsoHandler mSsoHandler;

	private WeiboAuthListener weiboAuthListener = new WeiboAuthListener() {

		@Override
		public void onComplete(Bundle values) {
			try {
				String token = values.getString("access_token");
				String expires_in = values.getString("expires_in");
				// String uid = values.getString("uid");
				Oauth2AccessToken accessToken = new Oauth2AccessToken(token,
						expires_in);
				if (accessToken.isSessionValid()) {
					Oauth oauth = new Oauth(Oauth.TYPE_WEIBO, token);
					if (oauth.isValid()) {
						oauthSucceed(oauth);
						return;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				LogUtil.e(TAG, "Exception:" + e);
			}
			oauthFailed(Oauth.TYPE_WEIBO, values);
		}


		@Override
		public void onCancel() {
			oauthFailed(Oauth.TYPE_WEIBO, null);
		}

		@Override
		public void onWeiboException(WeiboException e) {
			LogUtil.e(TAG, "Auth exception : " + e.getMessage());
			// oauthFailed(Oauth.TYPE_WEIBO);
		}
	};

	public WBHelper() {
		mActivity = App.getActivity();
		mWeibo = new WeiboAuth(mActivity,APP_KEY, REDICT_URL, SCOPE);
		weiboAPI = WeiboShareSDK.createWeiboAPI(mActivity, APP_KEY);
		weiboAPI.registerApp();
	}

	@Override
	public void oauth() {
		mSsoHandler = new SsoHandler(mActivity, mWeibo);
		mSsoHandler.authorize(weiboAuthListener);
	}

	public void sendText(String text) {
		TextObject textObject = new TextObject();
		textObject.text = (String) text;
		sendIt(textObject);
	}

	/**
	 * @param object
	 *            should be a drawable or bitmap or localpath
	 */
	public void sendImg(Object object) {
		sendImg(object, null);
	}

	/**
	 * @param object
	 *            should be a drawable or bitmap or localpath
	 */
	public void sendImg(Object object, String title) {
		if (object == null)
			return;
		try {
			Bitmap bitmap = null;
			if (object instanceof Drawable) {
				bitmap = ((BitmapDrawable) object).getBitmap();
			} else if (object instanceof Bitmap) {
				bitmap = (Bitmap) object;
			} else if (object instanceof String) {
				bitmap = BitmapFactory.decodeFile(object.toString());
			} else {
				return;
			}
			ImageObject imageObject = new ImageObject();
			imageObject.setImageObject(bitmap);

			WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
			weiboMessage.imageObject = imageObject;
			if (title != null) {
				TextObject textObject = new TextObject();
				textObject.text = title;
				weiboMessage.textObject = textObject;
			}

			SendMultiMessageToWeiboRequest req = new SendMultiMessageToWeiboRequest();
			/* transaction is a unique string */
			req.transaction = APP_KEY + System.currentTimeMillis();
			req.multiMessage = weiboMessage;
			weiboAPI.sendRequest( req);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (mSsoHandler != null) {
			mSsoHandler.authorizeCallBack(requestCode, resultCode, data);
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	private void sendIt(BaseMediaObject object) {
		WeiboMultiMessage weiboMessage = new WeiboMultiMessage();
		weiboMessage.mediaObject = object;
		SendMultiMessageToWeiboRequest req = new SendMultiMessageToWeiboRequest();
		/* transaction is a unique string */
		req.transaction = APP_KEY + System.currentTimeMillis();
		req.multiMessage = weiboMessage;
		weiboAPI.sendRequest(req);
	}

}
