package com.daxiangce123.android.util.outh;

import android.content.Intent;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;

/**
 * @author ram
 * @project Yunio-Android
 * @time 2013-8-13
 */
public abstract class OauthHelper {
    protected String TAG = "oauth";

    public interface onOauthListener {
        public void onOauthSucceed(Oauth oauth);

        public void onOauthFailed(String provider, Object object);
    }

    private onOauthListener mOauthListener = null;

    public void setOauthListener(onOauthListener listener) {
        if (App.DEBUG) {
//            LogUtil.v(TAG, getClass().getName() + "setOauthListener:" + listener.toString() + " Oauth:" + toString());
        }
        mOauthListener = listener;
    }

    public abstract void oauth();

    public void oauthSucceed(Oauth oauth) {
        if (mOauthListener != null) {
            mOauthListener.onOauthSucceed(oauth);
        } else {
            if (App.DEBUG) {
                LogUtil.v(TAG, getClass().getName() + "~oauthSucceed but mOauthListener ==null" + " Oauth:" + toString());
            }
        }
    }

    public void oauthFailed(String provider, Object object) {
        if (mOauthListener != null) {
            mOauthListener.onOauthFailed(provider, object);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    // public final static void storeOauth(Oauth oauth) {
    // // mSimpleData.putString(Const.SSO_TYPE, oauth.getType());
    // // mSimpleData.putString(Const.SSO_TOKEN, oauth.getToken());
    // }
    //
    // public final static Oauth getOauth() {
    // // String provider = mSimpleData.getString(Consts.TYPE, null);
    // // if (provider == null) {
    // // return null;
    // // }
    // // String token = mSimpleData.getString(Consts.TOKEN, null);
    // // if (token == null) {
    // // return null;
    // // }
    // // return new Oauth(provider, token);
    // return null;
    // }
    //
    // public final static void clearOAuth() {
    // // mSimpleData.remove(Const.SSO_TYPE);
    // // mSimpleData.remove(Const.SSO_TOKEN);
    // }

}
