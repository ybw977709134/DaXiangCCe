package com.daxiangce123.android.util.outh;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.sina.weibo.sdk.utils.LogUtil;
import com.tencent.mm.sdk.modelbase.BaseReq;
import com.tencent.mm.sdk.modelbase.BaseResp;
import com.tencent.mm.sdk.modelmsg.SendAuth;
import com.tencent.mm.sdk.modelmsg.SendMessageToWX;
import com.tencent.mm.sdk.modelmsg.WXEmojiObject;
import com.tencent.mm.sdk.modelmsg.WXImageObject;
import com.tencent.mm.sdk.modelmsg.WXMediaMessage;
import com.tencent.mm.sdk.modelmsg.WXTextObject;
import com.tencent.mm.sdk.modelmsg.WXVideoObject;
import com.tencent.mm.sdk.modelmsg.WXWebpageObject;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

/**
 * @author ram
 * @project sns_sample
 * @time 2013-8-8
 */
public class WXHelper extends OauthHelper {

    protected final static String TAG = "WXHelper";
    public static final String APP_ID = "wxaeab599214bdaaac";// ram@yunio:wx7caf9062644a2bb3,chris@yun.io:wx0bd8b4448730892c
    public static final String APP_SECRET = "b48c78a79033a974f16dc01fedb82e32";
    public static final String URL_FROM_AT = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=" + APP_ID + "&secret=" + APP_SECRET + "&grant_type=authorization_code&code=";
    public static final String URL_UNION_ID = "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";
    public String accessToken = null;
    private IWXAPI api;

    private IWXAPIEventHandler eventHandler = new IWXAPIEventHandler() {

        @Override
        public void onResp(BaseResp arg0) {
            if (App.DEBUG) {
                LogUtil.d(TAG, "onResp	" + arg0);
            }
            if (arg0 instanceof SendAuth.Resp) {
                SendAuth.Resp resp = (SendAuth.Resp) arg0;
                if (App.DEBUG) {
                    LogUtil.d(TAG, "errCode	" + arg0.errCode);
                    LogUtil.d(TAG, "token	" + resp.code);
                }
                ConnectBuilder.wxAccessToken(URL_FROM_AT + resp.code);
                IntentFilter intentFilter = new IntentFilter();
                intentFilter.addAction(Consts.WX_ACCESS_TOEKN);
                intentFilter.addAction(Consts.GET_WX_UNION_ID);
                Broadcaster.registerReceiver(receiver, intentFilter);
            } else if (arg0 instanceof SendMessageToWX.Resp) {
                SendMessageToWX.Resp resp = (SendMessageToWX.Resp) arg0;
                if (App.DEBUG) {
                    LogUtil.d(TAG, "type " + resp.getType());
                    LogUtil.d(TAG, "errCode	" + resp.errCode);
                    LogUtil.d(TAG, "errStr	" + resp.errStr);
                }
                if (resp.errCode == 0 && Consts.WEI_XIN.equals(App.shareType)) {
                    if (Consts.ALBUM.equals(App.shareObject)) {
                        UMutils.instance().diyEvent(ID.EventSharedAlbumToSocialNetworkSuccess);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "EventSharedAlbumToSocialNetworkSuccess" + App.shareObject + " shareType " + App.shareType + " shareToFriends  " + App.shareToFriends);
                        }
                    } else if (Consts.FILE.equals(App.shareObject)) {
                        UMutils.instance().diyEvent(ID.EventSharedFileToSocialNetworkSuccess);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "EventSharedFileToSocialNetworkSuccess" + App.shareObject + " shareType " + App.shareType + " shareToFriends  " + App.shareToFriends);
                        }

                    }
                    if (App.shareToFriends) {
                        UMutils.instance().diyEvent(ID.EventShareAlbumToMomentsSuccess);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "EventShareAlbumToMomentsSuccess " + App.shareObject + " shareType " + App.shareType + " shareToFriends  " + App.shareToFriends);
                        }

                    } else {
                        UMutils.instance().diyEvent(ID.EventShareAlbumToWechatSuccess);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "EventShareAlbumToWechatSuccess" + App.shareObject + " shareType " + App.shareType + " shareToFriends  " + App.shareToFriends);
                        }

                    }
                    App.shareType = null;
                    App.shareObject = null;
                }
            }
        }

        @Override
        public void onReq(BaseReq arg0) {

            if (App.DEBUG) {
                LogUtil.d(TAG, "onReq	" + arg0.transaction + "	" + arg0.toString() + "");
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (Consts.WX_ACCESS_TOEKN.equals(action) || Consts.GET_WX_UNION_ID.equals(action)) {
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);
                    if (!handleToken(response, action)) {
                        oauthFailed(Oauth.TYPE_WECHAT, null);
                        Broadcaster.unregisterReceiver(receiver);
                    }
                    if (Consts.GET_WX_UNION_ID.equals(action)) {
                        Broadcaster.unregisterReceiver(receiver);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public WXHelper() {
        api = WXAPIFactory.createWXAPI(App.getActivity(), APP_ID, true);
        api.registerApp(APP_ID);
    }

    public void sendText(String content, boolean toMoments) {
        com.tencent.mm.sdk.modelmsg.WXTextObject textObj = new WXTextObject();
        textObj.text = content;
        com.tencent.mm.sdk.modelmsg.WXMediaMessage msg = new WXMediaMessage();
        msg.mediaObject = textObj;
        // msg.title = "Will be ignored";
        msg.description = content;
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("text");
        req.message = msg;
        req.scene = toMoments ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public void sendWebPage(String url, Bitmap thumb, String title, String description, boolean toMoments) {
        WXWebpageObject webpage = new WXWebpageObject();
        webpage.webpageUrl = url;
        WXMediaMessage msg = new WXMediaMessage(webpage);
        msg.title = title;
        msg.description = description;
        msg.thumbData = bmpToByteArray(thumb, false);
        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("webpage");
        req.message = msg;
        req.scene = toMoments ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);

    }

    public void sendVideo(String url, String title, String description, boolean toMoments) {
        WXVideoObject video = new WXVideoObject();
        video.videoLowBandUrl = url;

        WXMediaMessage msg = new WXMediaMessage(video);
        msg.title = title;
        msg.description = description;

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("video");
        req.message = msg;
        req.scene = toMoments ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public void sendImage(String path, Bitmap thumb, boolean toMoments) {
        if (thumb == null) {
            return;
        }
        WXImageObject imgObj = new WXImageObject();
        imgObj.imagePath = path;

        WXMediaMessage msg = new WXMediaMessage(imgObj);
        msg.thumbData = bmpToByteArray(thumb, true);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("img");
        req.message = msg;
        req.scene = toMoments ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    public void sendGif(String path, Bitmap thumb, boolean toMoments) {
        WXEmojiObject emoji = new WXEmojiObject();
        emoji.emojiPath = path;

        WXMediaMessage msg = new WXMediaMessage(emoji);
        msg.thumbData = bmpToByteArray(thumb, false);

        SendMessageToWX.Req req = new SendMessageToWX.Req();
        req.transaction = buildTransaction("emoji");
        req.message = msg;
        req.scene = toMoments ? SendMessageToWX.Req.WXSceneTimeline : SendMessageToWX.Req.WXSceneSession;
        api.sendReq(req);
    }

    /**
     * NO OAUTH <br>
     * NO NEED TO OAUTH
     */
    public void oauth() {
        final SendAuth.Req req = new SendAuth.Req();
        req.scope = "snsapi_userinfo";
        req.state = "wechat_sdk_demo_test_" + UUID.randomUUID().toString();
        api.sendReq(req);
    }

    private static byte[] bmpToByteArray(final Bitmap bmp, final boolean needRecycle) {
        if (bmp == null) {
            return null;
        }
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        bmp.compress(CompressFormat.PNG, 100, output);
        if (needRecycle) {
            bmp.recycle();
        }
        byte[] result = output.toByteArray();
        try {
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    private String buildTransaction(final String type) {
        return (type == null) ? String.valueOf(System.currentTimeMillis()) : type + System.currentTimeMillis();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onActivityResult	" + (data != null));
        }
        handleIntent(data);
    }

    public void handleIntent(Intent intent) {
        if (api == null || intent == null) {
            return;
        }
        api.handleIntent(intent, eventHandler);
    }

    private boolean handleToken(Response response, String action) {
        if (response == null || response.getStatusCode() != 200) {
            return false;
        }
        try {
            String content = response.getContent();
            JSONObject jo = JSONObject.parseObject(content);
            if (action.equals(Consts.WX_ACCESS_TOEKN)) {
                String token = jo.getString("access_token");
                String uid = jo.getString("openid");
                if (Utils.existsEmpty(token, uid)) {
                    return false;
                }
                this.accessToken = token;
                String getUnionUrl = String.format(URL_UNION_ID, token, uid);
                ConnectBuilder.wxUnionId(getUnionUrl);
            } else if (action.equals(Consts.GET_WX_UNION_ID)) {
                String unionId = jo.getString("unionid");
                String uid = jo.getString("openid");
                if (Utils.existsEmpty(accessToken, uid, unionId)) {
                    return false;
                }
                Oauth oauth = new Oauth(Oauth.TYPE_WECHAT, accessToken);
                oauth.setUid(uid);
                oauth.setUnion_id(unionId);
                oauthSucceed(oauth);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }
}
