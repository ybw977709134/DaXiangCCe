package com.daxiangce123.android.push;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.NotifyManager;
import com.daxiangce123.android.parser.GsonParser;
import com.daxiangce123.android.push.PushCallBack.Provider;
import com.daxiangce123.android.push.data.ActiveAlbumPush;
import com.daxiangce123.android.push.data.CommentPush;
import com.daxiangce123.android.push.data.LikePush;
import com.daxiangce123.android.push.data.Push;
import com.daxiangce123.android.push.data.UpdatePush;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @author ram
 * @project DaXiangCe
 * @time Jul 11, 2014
 */
public class PushCenter {

    private PushCallBack pushCallBack;
    protected final static String TAG = "PushCenter";
    private static PushCenter instance;
    private static boolean DEBUG = true;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(Consts.REGI_PUSH)) {
                try {
                    Broadcaster.unregisterReceiver(this);
                    ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);

                    if (DEBUG) {
                        LogUtil.d(TAG, "onReceive()	UID=" + App.getUid());
                        LogUtil.d(TAG, "onReceive()\n-------------->request:\n" + info + "\n-------------->response:\n" + response);
                    }

                    if (response.getStatusCode() != 200) {
                        return;
                    }
                    AppData.setRegisterId(info.getTag());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private PushCenter() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    public static PushCenter instance() {
        if (instance == null) {
            instance = new PushCenter();
        }
        return instance;
    }

    public void setCallBack(PushCallBack pushCallBack) {
        this.pushCallBack = pushCallBack;
    }

//	public void bind(String regId, Provider provider) {
//		bind(regId, null, provider);
//	}

    public static boolean hasBinded() {
        String regId = AppData.getRegisterId();
        boolean binded = !Utils.isEmpty(regId);
        if (DEBUG) {
            LogUtil.d(TAG, "hasBinded()	" + binded + "	" + App.getUid() + "	" + regId);
        }
        return binded;
    }

    public void bind(String regId, String channelId, Provider provider) {
        boolean reusult = ConnectBuilder.registerNotificationId(regId, channelId, provider);
        if (DEBUG) {
            LogUtil.e(TAG, "bind()	" + reusult + "	" + regId + "	" + channelId + "	" + provider.getName() + "	API_KEY:" + Consts.BAIDU_AK);
        }
        if (reusult) {
            Broadcaster.registerReceiver(receiver, new IntentFilter(Consts.REGI_PUSH));
        }
    }

    /**
     * <pre>
     * add:
     * {
     *  "loc-key" : "NEW_PHOTO(s)"/"NEW_VIDEO(s)",
     *   "userid" : "...",
     *   "username" : "...",
     *   "albumid" : "...",
     *   "albumname" : "...",
     *   "badge" : file_num
     * }
     *
     * Comment:
     * {
     *   "loc-key" : "COMMENT_PHOTO"/"COMMENT_VIDEO"/"REPLY_PHOTO"/"REPLY_VIDEO",
     *   "userid" : "...",
     *   "username" : "...",
     *   "fileid" : "...",
     *   "body" : msg_content
     * }
     *
     * Like:
     * {
     *   "loc-key" : "LIKE_PHOTO"/"LIKE_VIDEO",
     *   "userid" : "...",
     *   "username" : "...",
     *   "fileid" : "...",
     * }
     *
     * Active Album
     * {
     * "title": title,
     * "description":description,
     * "custom_content":
     * {
     *  "message":message,
     *  "invitecode":invitecode,
     *  "albumid":albumid,
     *  "loc-key":ACTIVE_ALBUM
     * }
     * }
     *
     * </pre>
     *
     * @time Jul 12, 2014
     * @param message
     * @param provider
     */
    @SuppressLint("DefaultLocale")
    int count;

    public void OnMessage(String message, Provider provider) {
        if (DEBUG) {
            LogUtil.d(TAG, "------->OnMessage	" + message + "	count=" + (++count));
        }
        if (pushCallBack != null) {
            pushCallBack.onNewMessage(message, provider);
        }

        if (App.getActivity() != null && App.getActivity().isForeGround()) {
            if (DEBUG) {
                LogUtil.d(TAG, "NOT IN BACKGROUND");
            }
            return;
        }

        JSONObject jo = JSONObject.parseObject(message);

        String lk = jo.getString("loc-key");
        PushType type = PushType.valueOf(lk.trim().toUpperCase());
        Push push = null;
        String content = null;
        String fileType = "";
        if (type == null) {
            return;
        }
        if (type.name().contains("_PHOTO")) {
            fileType = Utils.getString(R.string.image);
        } else if (type.name().contains("_VIDEO")) {
            fileType = Utils.getString(R.string.video);
        }
        int notificationId = -1;
        boolean needSound = false;
        if (type == PushType.NEW_PHOTO || type == PushType.NEW_PHOTOS || type == PushType.NEW_VIDEO || type == PushType.NEW_VIDEOS) {
            notificationId = PushType.NEW_PHOTO.hashCode();
            push = GsonParser.parser(message, UpdatePush.class);
            UpdatePush updatePush = (UpdatePush) push;
            content = Utils.getString(R.string.x_uploaded_x_x_to_album_x, updatePush.getName(), updatePush.getCount(), fileType, updatePush.getAlbumName());
        } else if (type == PushType.COMMENT_PHOTO || type == PushType.COMMENT_VIDEO) {
            notificationId = PushType.COMMENT_PHOTO.hashCode();
            push = GsonParser.parser(message, CommentPush.class);
            content = Utils.getString(R.string.x_comment_x, push.getName(), ((CommentPush) push).getComment());
            needSound = true;
        } else if (type == PushType.LIKE_PHOTO || type == PushType.LIKE_VIDEO) {
            notificationId = PushType.LIKE_PHOTO.hashCode();
            push = GsonParser.parser(message, LikePush.class);
            content = Utils.getString(R.string.x_liked_your_x, push.getName(), fileType);
        } else if (type == PushType.REPLY_PHOTO || type == PushType.REPLY_PHOTO) {
            needSound = true;
            notificationId = PushType.REPLY_PHOTO.hashCode();
            push = GsonParser.parser(message, CommentPush.class);
            content = Utils.getString(R.string.x_replay_x, push.getName(), ((CommentPush) push).getComment());
        } else if (type == PushType.ACTIVE_ALBUM) {
            needSound = true;
            notificationId = PushType.ACTIVE_ALBUM.hashCode();
            push = GsonParser.parser(message, ActiveAlbumPush.class);
            content = ((ActiveAlbumPush) push).getProductionMsg();
        }
        if (push == null) {
            if (DEBUG) {
                LogUtil.d(TAG, "push obj is null");
            }
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, push.getType() + " " + content);
        }
        Intent intent = new Intent();
        intent.putExtra(Consts.PUSH, push);
        intent.putExtra(Consts.TYPE, type.toString());
        Context context = null;
        if (App.getActivity() == null) {
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context = App.getAppContext();
        } else {
            context = App.getActivity();
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, "	" + context.getClass().getSimpleName());
        }

        intent.setClass(context, HomeActivity.class);
        String title = Utils.getString(R.string.app_name);
        NotifyManager.instance().showNotification(title, content, notificationId, intent, needSound);
    }
}
