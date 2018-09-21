package com.daxiangce123.android.ui.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.baidu.android.pushservice.PushConstants;
import com.baidu.android.pushservice.PushManager;
import com.baidu.android.pushservice.PushSettings;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.EventService;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.AlbumListener;
import com.daxiangce123.android.listener.ClearNotifyCountListener;
import com.daxiangce123.android.listener.OnDeleteAlbumListener;
import com.daxiangce123.android.listener.OnOpenAlbumListener;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ContactManager;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.push.data.ActiveAlbumPush;
import com.daxiangce123.android.push.data.ComLikePush;
import com.daxiangce123.android.push.data.Push;
import com.daxiangce123.android.push.data.UpdatePush;
import com.daxiangce123.android.ui.UIManager;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.AlbumFragment;
import com.daxiangce123.android.ui.pages.CreateAlbumFragment;
import com.daxiangce123.android.ui.pages.FindAlbumTabFragment;
import com.daxiangce123.android.ui.pages.JoinAlbumFragment;
import com.daxiangce123.android.ui.pages.MeFragment;
import com.daxiangce123.android.ui.pages.MobileLoginFragment;
import com.daxiangce123.android.ui.pages.ModifyAccountPassword;
import com.daxiangce123.android.ui.pages.ModifyBindingPhoneNumberFragment;
import com.daxiangce123.android.ui.pages.NotificationCenterFragment;
import com.daxiangce123.android.ui.pages.RegisterFragment;
import com.daxiangce123.android.ui.pages.SetPasswordFragment;
import com.daxiangce123.android.ui.pages.SubmitVerficationCodeFragment;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.ActPageDialog;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.InitAlarm;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.tencent.stat.MtaSDkException;
import com.tencent.stat.StatService;
import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

//import com.daxiangce123.android.util.TestUtil;

public class HomeActivity extends BaseCliqActivity implements OnClickListener, OptionListener, IRegisterController {

    private final static String TAG = "HomeActivity";
    // private TextView tvAlbumNotify;
    private TextView tvNotification;
    private RelativeLayout meBtn;
    private RelativeLayout albumBtn;
    private RelativeLayout notifyBtn;
    private RelativeLayout addAlbumBtn;
    private View tabLocal;
    private View curTab;
    private ArrayList<Event> notificationEvents;
    private int unreadNotificationNum;
    // private int unreadFileNum;
    private List<AlbumEntity> albumList;
    private Bitmap defBitmap;
    private Push tempPush;
    // private CreateJoinAlbumDialog createJoinAlbumDialog;
    // private LinearLayout mFoundMask;
    // private ImageView ivFoundMask;

    private OptionDialog createJoinAlbumDialog;
    private int lastSort = 0;


    private LinearLayout mGiveUp;
    private ImageView mTryShare;
    private LinearLayout mUserGuide1;
    private LinearLayout mUserGuide2;
    private LinearLayout mBindPhoneGuide;
    private TextView tvBindPhone;
    private TextView tvSkip;
    private String session;
    private String status;
    private boolean showNewUserGuide;
    private String shareAlbumInfo;
    private View blankBanner1;
    private View blankBanner2;
    private boolean bannerIsShow;
    private long lastClickTime;
    private String albumFromPush;
    private ActPageDialog actPageDialog;
    private String firstShowDate;
    private String cacheSize = " 0KB";
    private HashSet<String> eventIdSet = new HashSet<String>();

    private AlbumFragment albumFragment;
    private NotificationCenterFragment notificationFrament;
    private CreateAlbumFragment createAlbumFragment;
    private JoinAlbumFragment joinAlbumFragment;
    private MeFragment meFragment;

    private FindAlbumTabFragment findAlbumTabFragment;

    private RegisterFragment registerFragment = null;
    private MobileLoginFragment mobileLoginFragment = null;
    private SubmitVerficationCodeFragment submitVerficationCodeFragment = null;
    private SetPasswordFragment setPasswordFragment = null;
    private AlbumEntity oldAlbumEntity;
    private UserInfo info;

    private AlbumListener albumListener = new AlbumListener() {

        @Override
        public boolean openAlbum(AlbumEntity albumEntity) {
            HomeActivity.this.openAlbum(albumEntity);
            return false;
        }

        @Override
        public boolean hasAlbum(String albumId) {
            if (albumFragment == null) {
                return false;
            }
            return albumFragment.hasAlbum(albumId);
        }

        @Override
        public boolean bannerContentIsShow(boolean isShow) {
            // HomeActivity.this.showBlankBanner(isShow);
            bannerIsShow = isShow;
            return false;
        }
    };

    private ClearNotifyCountListener clearNotifyCountListener = new ClearNotifyCountListener() {

        @Override
        public void clearNotifyCount() {
            // TODO Auto-generated method stub
            updateNotify(tvNotification, 0);
        }
    };

    private OnOpenAlbumListener onOpenAlbumListener = new OnOpenAlbumListener() {

        @Override
        public void onOpenAlbum(AlbumEntity albumEntity) {
            if (albumEntity == null) {
                return;
            }
            if (App.DEBUG) {
                LogUtil.d(TAG, "onOpenAlbum	albumEntity" + albumEntity.getName());
            }
            // unreadFileNum = unreadFileNum - albumEntity.getUpdateCount();
            // updateAlbumNotify();
            albumEntity.setUpdateCount(0);
            DBHelper dbHelper = App.getDBHelper();
            if (dbHelper == null) {
                return;
            }
            // dbHelper.execute("UPDATE " + albumEntity.getTableName() + " SET "
            // + Consts.UPDATE_COUNT + "=0 WHERE " + Consts.ALBUM_ID + "=\"" +
            // albumEntity.getId() + "\"");
        }
    };

    private OnDeleteAlbumListener deleteAlbumListener = new OnDeleteAlbumListener() {
        @Override
        public void onDeleteAlbum(AlbumEntity albumEntity) {
            if (albumEntity == null) {
                return;
            }
            // unreadFileNum = unreadFileNum - albumEntity.getUpdateCount();
            // updateAlbumNotify();
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Consts.GET_EVENTS.equals(action)) {
                    List<Event> events = intent.getParcelableArrayListExtra(Consts.EVENT_LIST);
                    onEvent(events);
                } else {
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);
                    ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                    String content = response.getContent();
                    if (Consts.GET_MIME_INFO.equals(action)) {
                        onGetMimeInfo(response);
                    } else if (Consts.GET_FILE_INFO.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            onGetFileInfo(content);
                        } else if (response.getStatusCode() == 404) {
                            CToast.showToast(R.string.failed_to_fet_file_info);
                            return;
                        }
                    } else if (Consts.GET_TEMP_TOKEN_BY_LINK.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            onGetToken(connectInfo, response);
                        }
                    } else if (Consts.GET_ALBUM_INFO.equals(action)) {
                        String albumId = connectInfo.getTag();
                        if (!albumId.equals(albumFromPush)) {
                            return;
                        }
                        albumFromPush = null;
                        if (response.getStatusCode() == 200) {
                            AlbumEntity albumEntity = Parser.parseAlbum(content);
                            Intent start = new Intent();
                            start.putExtra(Consts.ALBUM_ID, albumId);
                            start.putExtra(Consts.EVENT_ID, "");
                            start.putExtra(Consts.ALBUM, albumEntity);
                            start.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
                            startActivity(start);
                            // reset not for using
                        }
                    } else if (Consts.GET_ACTIVITY_PAGE.equals(action)) {
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "action --- GET_ACTIVITY_PAGE" + action + "response" + response);
                        }
                        if (response.getStatusCode() != 200) {
                            return;
                        }
                        if (response.getContent().equals("1")) {
                            showActPage();
                        }
                    }
                    // else if (Consts.GET_PIN_TO_WEB.equals(action)) {
                    // if (App.DEBUG) {
                    // LogUtil.d(TAG, "action ---GET_PIN_TO_WEB" + action +
                    // "response" + response);
                    // }
                    // }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void onNewIntent(Intent intent) {
        LogUtil.d("PushCenter", "-----------------onNewIntent");
        initIntent(intent);
        initUri();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // App.addActivity(this);

        session = AppData.getToken();
        status = AppData.getStatus();

        if (!Utils.isEmpty(session)) {
            initPush();
        }
        veryInit();

        UmengUpdateAgent.update(this);
        MobclickAgent.updateOnlineConfig(this);
        setContentView(R.layout.home_activity);
        if (!AppData.getNotFirstLaunch()) {
            findShareAlbumInfo();
            AppData.setNotFirstLaunch(true);
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, " firstLaunch" + AppData.getNotFirstLaunch());
        }
        initCompontent();
        initBroadcast();
        initData();
        initUri();
        initMTA();
        SensitiveWordGrepManager.getInstance().refreshSensitiveWord();
        Intent intent = getIntent();
        if (intent.hasExtra(Consts.INVITE_CODE)) {
            String inviteCode = intent.getStringExtra(Consts.INVITE_CODE);
            if (inviteCode != null) {
                joinAlbum(inviteCode);
            }
        }
        if (intent.hasExtra(Consts.ALBUM)) {
            if (status.equals(Consts.STATUS_EXISTED)) {
                AlbumEntity albumEntity = intent.getParcelableExtra(Consts.ALBUM);
                if (albumEntity != null) {
                    openNotJoinAlbum(albumEntity.getId(), albumEntity);
                }
            }
        }
        initIntent(intent);
        // setMask();
        // initAlarm();
        if (App.DEBUG) {
            LogUtil.d(TAG, "uid : " + App.getUid());
        }

        // initCacheSize();

        ConnectBuilder.requestSplashData(0,10);
        isDownloadSplash();
    }

    private void isDownloadSplash(){
        File file = new File(MediaUtil.getDestSaveDir() + AppData.getSplashId() + ".png");
        if(!Utils.isEmpty(AppData.getSplashId()) && !file.exists()){
            if(!Utils.isEmpty(AppData.getSplashId())){
                ConnectBuilder.splashDownload(AppData.getSplashId());
            }
        }
    }

    private void onGetMimeInfo(Response response) {
        info = Parser.parseUserInfo(response.getContent());
        if (info == null) {
            return;
        }
        AppData.setUid(info.getId());
        App.setUserInfo(info);
        if (Utils.isEmpty(info.getMobile()) && status.equals(Consts.STATUS_EXISTED)) {
            int time = AppData.getBindPhoneGuideTime();
            time++;
            if (time >= 0 && time < 4) {
                mBindPhoneGuide.setVisibility(View.VISIBLE);
                AppData.setBindPhoneGuideTime(time);
            }
        }

        long createTime = TimeUtil.toLong(info.getCreateDate(), Consts.SERVER_UTC_FORMAT);
        long onLineTime = TimeUtil.toLong("2015-04-18T00:00:00.000Z", Consts.SERVER_UTC_FORMAT);
        if (App.DEBUG) {
            LogUtil.d(TAG, "  --- createTime ---  " + createTime + " --- onLineTime --- " + onLineTime + "createTime - onLineTime" + (createTime - onLineTime));
        }
        if (Utils.isEmpty(info.getMobile()) && createTime - onLineTime >= 0) {
            IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
            bundle.setRegisterType(IRegisterController.RegisterType.bind);
            bundle.newUserBindPhone(true);
            showRegister(bundle);
            AppData.setFirstBindPhone(true);

        } else if (!Utils.isEmpty(info.getMobile()) && createTime - onLineTime >= 0) {
            showUserGuide();
        }

    }

    private void showActPage() {
        firstShowDate = TimeUtil.formatTime(AppData.getFirstShowActTime(), "yyyy-MM-dd");
        String currentTime = TimeUtil.formatTime(System.currentTimeMillis(), "yyyy-MM-dd");
        if (firstShowDate.equals(currentTime)) {
            AppData.setFirstShowAct(false);
        } else {
            AppData.setFirstShowAct(true);
            AppData.setFirstShowActTime(System.currentTimeMillis());
        }
        if (AppData.getFirstShowAct()) {
            if (actPageDialog == null) {
                actPageDialog = new ActPageDialog();
            }
            actPageDialog.setHomeUrl(Consts.URL_ACTIVITY_PAGE);
            actPageDialog.show();
        }
    }

    private void initMTA() {
        try {
            // 第三个参数必须为：com.tencent.stat.common.StatConstants.VERSION
            StatService.startStatService(this, null,
                    com.tencent.stat.common.StatConstants.VERSION);
        } catch (MtaSDkException e) {
            // MTA初始化失败
            Log.e(TAG, "MTA start failed.");
        }
    }

    private void initUri() {
        Uri uri = App.scheme;
        if (App.DEBUG) {
            LogUtil.d(TAG, " initUri-uri : " + uri);
        }
        if (uri != null) {
            String inviteCode = uri.getQueryParameter("album");
            if (App.DEBUG) {
                LogUtil.d(TAG, "inviteCode : " + inviteCode);
            }
            if (!Utils.isEmpty(inviteCode)) {
                joinAlbum(inviteCode);
            }

            BaseFragment baseFragment = getCurrentFragment();
            if (!(baseFragment instanceof AlbumFragment)) {
                showAlbum();
                curTab.setSelected(false);
                curTab = albumBtn;
            }
            String pin = uri.getQueryParameter("pin");
            if (App.DEBUG) {
                LogUtil.d(TAG, "pin: " + pin);
            }
            if (!Utils.isEmpty(pin)) {
                ConnectBuilder.getPinToWEB(pin);
            }
            UMutils.instance().diyEvent(ID.EventAutoEnterSourceAlbum);

        }

        App.scheme = null;
    }

    private void joinAlbum(String inviteCode) {
        JSONObject jo = new JSONObject();
        jo.put(Consts.USER_ID, App.getUid());
        if (status.equals(Consts.STATUS_CREATE)) {
            jo.put(Consts.NOT_OPEN_ALBUM, false);
        }
        ConnectBuilder.joinAlbum(inviteCode, jo.toJSONString());
    }

    private void veryInit() {
        // LocateManager.instance().start();
        ConnectBuilder.init();
        // AppData.setLogined(true);
        MediaUtil.init();
        startService();
    }

    private void initPush() {
        try {
            // if (!PushCenter.hasBinded()) {
            regPushService();
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void regPushService() {
        PushManager.startWork(App.getAppContext(), PushConstants.LOGIN_TYPE_API_KEY, Consts.BAIDU_AK);
        // PushManager.enableLbs(getApplicationContext());
        PushSettings.enableDebugMode(App.getAppContext(), App.DEBUG);

        ContactManager.getInstance().syncToServer(true);
    }

    private void startService() {
        if (EventService.running()) {
            Broadcaster.sendBroadcast(Consts.START_EVENT_SERVICE);
            // Broadcaster.sendBroadcast(Consts.START_UPLOADING_CHECKING);
            return;
        }
        Intent intent = new Intent();
        intent.setClass(this, EventService.class);
        startService(intent);
        if (App.DEBUG) {
            LogUtil.d(TAG, "TEMPP startService	" + System.currentTimeMillis());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Broadcaster.sendBroadcast(Consts.START_UPLOADING_CHECKING);
        if (oldAlbumEntity != null) {
            onAlbumUpdate(App.getAlbum(oldAlbumEntity.getId()));
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, " -- albumEntity --  onResume " + oldAlbumEntity);
        }
    }

    private void initCompontent() {
        albumBtn = (RelativeLayout) this.findViewById(R.id.rl_album);
        albumBtn.setOnClickListener(this);
        notifyBtn = (RelativeLayout) this.findViewById(R.id.rl_notification);
        notifyBtn.setOnClickListener(this);
        // tvAlbumNotify = (TextView) findViewById(R.id.album_update);
        tvNotification = (TextView) findViewById(R.id.notification_num);
        meBtn = (RelativeLayout) this.findViewById(R.id.rl_me);
        meBtn.setOnClickListener(this);

        tabLocal = findViewById(R.id.rl_local);
        tabLocal.setOnClickListener(this);

        addAlbumBtn = (RelativeLayout) this.findViewById(R.id.rl_add);
        addAlbumBtn.setOnClickListener(this);

        // mFoundMask = (LinearLayout) this.findViewById(R.id.ll_mask);
        // mFoundMask.setOnClickListener(this);
        //
        // ivFoundMask = (ImageView) this.findViewById(R.id.iv_mask);

        mGiveUp = (LinearLayout) this.findViewById(R.id.ll_share_give_up);
        mGiveUp.setOnClickListener(this);
        mTryShare = (ImageView) this.findViewById(R.id.iv_share_try);
        mTryShare.setOnClickListener(this);
        mUserGuide1 = (LinearLayout) this.findViewById(R.id.ll_user_guide1);
        mUserGuide2 = (LinearLayout) this.findViewById(R.id.ll_user_guide2);
        mUserGuide2.setOnClickListener(this);

        blankBanner1 = this.findViewById(R.id.tv_guide_blank1);
        blankBanner2 = this.findViewById(R.id.tv_guide_blank2);

        mBindPhoneGuide = (LinearLayout) this.findViewById(R.id.ll_binding_phone_guide);
        mBindPhoneGuide.setOnClickListener(this);
        tvBindPhone = (TextView) this.findViewById(R.id.tv_binding_phone);
        tvBindPhone.setOnClickListener(this);
        tvSkip = (TextView) this.findViewById(R.id.tv_skip);
        tvSkip.setOnClickListener(this);


        ConnectBuilder.getActivtyPage(TimeUtil.formatTime(System.currentTimeMillis(), "yyyyMMdd"), App.mobileInfo.VERSION);
        initUI();
    }


    private void initUI() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "session : " + session);
        }
        if (!Utils.isEmpty(session)) {
            ConnectBuilder.getMineInfo();
            showAlbum();
            curTab = albumBtn;
//            showUserGuide();
        } else {
            // ConnectBuilder.getTempTokenByLink("J80b99uTe", null);
            if (Utils.isEmpty(shareAlbumInfo)) {
                showNearby();
                curTab = tabLocal;
                if (App.DEBUG) {
                    LogUtil.d(TAG, "curTab-- " + curTab);
                }
            } else {
                int index = shareAlbumInfo.indexOf("_");
                if (index == -1) {
                    return;
                }
                int length = 9;
                index += 1;
                int last = index + length;
                if (last >= shareAlbumInfo.length()) {
                    return;
                }
                String link = shareAlbumInfo.substring(index, last);
                ConnectBuilder.getTempTokenByLink(link, null);
                if (App.DEBUG) {
                    LogUtil.d(TAG, "initUI  --link--" + link);
                }
                showNearby();
                curTab = tabLocal;
            }
        }
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_MIME_INFO);
        ift.addAction(Consts.GET_FILE_INFO);
        ift.addAction(Consts.GET_EVENTS);
        ift.addAction(Consts.GET_TEMP_TOKEN_BY_LINK);
        ift.addAction(Consts.GET_ALBUM_INFO);
        ift.addAction(Consts.GET_ACTIVITY_PAGE);
        // ift.addAction(Consts.GET_PIN_TO_WEB);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initData() {
        TaskRuntime.instance().run(new Task() {
            @Override
            public void run() {
                final ArrayList<Event> events = readDB();
                HomeActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onEvent(events);
                        if (albumFragment == null) {
                            return;
                        }
                        albumFragment.setDefaultCover(defBitmap);
                        albumFragment.onReadAlbumsFromDB(albumList);
                    }
                });
            }
        });

    }

    private void initIntent(Intent intent) {
        // NotifyManager.instance().hide();
        if (App.DEBUG) {
            LogUtil.d("PushCenter", "------------------HomeActivity()");
        }
        tempPush = null;
        if (intent == null) {
            return;
        }
        tempPush = intent.getParcelableExtra(Consts.PUSH);
        if (tempPush == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.d("PushCenter", "------------------tempPush " + tempPush.getType() + "\n---------> type = " + intent.getStringExtra(Consts.TYPE));
        }
        handlePush();
    }


    private void findShareAlbumInfo() {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File RDirectory = Environment.getExternalStorageDirectory();
        String absPath = RDirectory.getAbsolutePath();
        String[] paths = {absPath + "/360Browser/download", absPath + "/UCDownloads", absPath + "/QQBrowser/安装包", absPath + "/Download", absPath + "/baidu/flyflow/downloads", file.getPath()};

        for (String path : paths) {
            // if (Utils.isEmpty(shareAlbumInfo)) {
            if (App.DEBUG) {
                LogUtil.d(TAG, "findShareAlbumInfo -- shareAlbumInfo : " + shareAlbumInfo + "  --- path : " + path);
                // }
            }
            findPath(path);
        }
    }

    private void findPath(String path) {
        File file = new File(path);
        if (!file.exists()) {
            return;
        }
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            String fn = f.getName();

            if ((fn.startsWith("daxiangce") || fn.startsWith("axiangce")) && !Utils.isEmpty(shareAlbumInfo)) {
                f.delete();
            } else {
                if (fn.endsWith(".apk") && (fn.startsWith("daxiangce") || fn.startsWith("axiangce"))) {
                    shareAlbumInfo = fn;
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "findPath --- shareAlbumInfo : " + shareAlbumInfo + "   --- path : " + path);
                    }
                    f.delete();
                }
            }

        }
    }

    private void showUserGuide() {
        if (status == null) {
            return;
        }
        showNewUserGuide = AppData.getShowNewUserGuide();
        if (status.equals(Consts.STATUS_CREATE) || showNewUserGuide) {
            mUserGuide1.setVisibility(View.VISIBLE);
        } else {
            mUserGuide1.setVisibility(View.GONE);
        }

        if ((status.equals(Consts.STATUS_CREATE) || showNewUserGuide) && bannerIsShow) {
            blankBanner1.setVisibility(View.VISIBLE);
            blankBanner2.setVisibility(View.GONE);
        }

//        if (status.equals(Consts.STATUS_CREATE)) {
//            mUserGuide1.setVisibility(View.VISIBLE);
//        } else if (status.equals(Consts.STATUS_EXISTED)) {
//            mUserGuide1.setVisibility(View.GONE);
//        }
//
//        if (status.equals(Consts.STATUS_CREATE) && bannerIsShow) {
//            blankBanner1.setVisibility(View.VISIBLE);
//            blankBanner2.setVisibility(View.GONE);
//        }
    }

    private void handlePush() {
        try {
            boolean result = false;
            if (tempPush instanceof ComLikePush) {
                result = showComment(((ComLikePush) tempPush).getFileId());
            } else if (tempPush instanceof UpdatePush) {
                result = openAlbum(((UpdatePush) tempPush).getAlbumId());
            } else if (tempPush instanceof ActiveAlbumPush) {
                result = openActiveAlbum(((ActiveAlbumPush) tempPush).getAlbumId());
            }
            if (result) {
                tempPush = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean viewImageDetail(FileEntity file) {
        if (file == null) {
            return false;
        }
        AlbumEntity albumEntity = null;
        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper != null) {
            albumEntity = dbHelper.getAlbum(file.getAlbum());
        }

        App.albumItemController = new AlbumItemController(albumEntity);
        App.albumItemController.addFile(file, true, false);
        App.putAlbum(albumEntity);
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.putExtra(Consts.IS_JOINED, true);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.POSITION, 0);
        startActivity(intent);
        return true;
    }

    private boolean showComment(String fileId) {
        try {
            if (fileId != null) {
                FileEntity fileEntity = null;
                if (App.getDBHelper() != null) {
                    fileEntity = App.getDBHelper().getData(FileEntity.EMPTY, fileId);
                }
                if (fileEntity != null) {
                    // showComment(fileEntity);
                    viewImageDetail(fileEntity);
                    return true;
                } else {
                    ConnectBuilder.getFileInfo(fileId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private void onGetFileInfo(String content) {
        if (isForeGround()) {
            FileEntity fileEntity = Parser.parseFile(content);
            if (fileEntity == null) {
                return;
            }
            if (tempPush instanceof ComLikePush) {
                String fileId = ((ComLikePush) tempPush).getFileId();
                if (!fileEntity.getId().equals(fileId)) {
                    return;
                }
                // showComment(fileEntity);
                viewImageDetail(fileEntity);
            }
        }
    }

    private void onGetToken(ConnectInfo info, Response response) {
        if (response == null || info == null) {
            return;
        }
        TempToken tempToken;
        if (response.getStatusCode() == 401) {
            tempToken = TempToken.EMPTY;
        } else if (response.getStatusCode() == 200) {
            tempToken = Parser.parseTempToken(response.getContent());
        } else {
            return;
        }
        if (tempToken == null) {
            return;
        }
        String type = tempToken.getObjType();
        if (type.equals(Consts.ALBUM)) {
            AlbumEntity albumEntity = (AlbumEntity) tempToken.getObject();
            JSONObject jo = JSONObject.parseObject(tempToken.getObjStr());
            if (!Utils.isEmpty(session)) {
                String inviteCode = jo.getString(Consts.INVITE_CODE);
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Consts.USER_ID, App.getUid());
                ConnectBuilder.joinAlbum(inviteCode, jsonObject.toJSONString());
            } else {
                String albumId = jo.getString(Consts.ID);
                openNotJoinAlbum(albumId, albumEntity);

//                String code;
//                if (shareAlbumInfo.startsWith("axiangce")) {
//                    code = shareAlbumInfo.substring(19, 20);
//                } else {
//                    code = shareAlbumInfo.substring(20, 21);
//                }
//                Map<String, String> map = new HashMap<String, String>();
//                if (code.equals("1")) {
//                    map.put(Consts.SHARES, String.valueOf(1));
//                } else if (code.equals("2")) {
//                    map.put(Consts.INVITE, String.valueOf(1));
//                }
//                UMutils.instance().diyEvent(ID.EventAutoEnterSourceAlbum, map);
//                shareAlbumInfo = null;
            }
            String code;
            if (shareAlbumInfo.startsWith("axiangce")) {
                code = shareAlbumInfo.substring(19, 20);
            } else {
                code = shareAlbumInfo.substring(20, 21);
            }

            Map<String, String> map = new HashMap<String, String>();
            if (code.equals("1")) {
                map.put(Consts.SHARES, String.valueOf(1));
            } else if (code.equals("2")) {
                map.put(Consts.INVITE, String.valueOf(1));
            }
            UMutils.instance().diyEvent(ID.EventAutoEnterSourceAlbum, map);
            shareAlbumInfo = null;
        }
    }

    private void openNotJoinAlbum(String albumId, AlbumEntity albumEntity) {
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM_ID, albumId);
        intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinAutoEnterSourceAlbumSuccess);
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        startActivity(intent);
        this.oldAlbumEntity = albumEntity;
        App.putAlbum(albumEntity);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onGetToken -- " + intent);
        }
    }

    public boolean onOpenNearbyAlbum(AlbumEntity albumEntity) {
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinNearbyAlbumSuccess);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        startActivity(intent);

        this.oldAlbumEntity = albumEntity;
        App.putAlbum(albumEntity);
        if (App.DEBUG) {
            LogUtil.d(TAG, " -- albumEntity --  onOpenNearbyAlbum " + albumEntity + " oldAlbumEntity " + oldAlbumEntity);
        }
        return true;
    }

    private ArrayList<Event> readDB() {

        long start = System.currentTimeMillis();
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image_large);
        bitmap = BitmapUtil.squareBitmap(bitmap);
        defBitmap = BitmapUtil.toRoundCorner(bitmap);
        defBitmap = BitmapUtil.rotateOverlay(defBitmap);
        if (App.DEBUG) {
            LogUtil.d(TAG, "handle bitmap duration is " + (System.currentTimeMillis() - start));
        }
        DBHelper dbHelper = App.getDBHelper();
        if (App.DEBUG) {
            LogUtil.d(TAG, " --readDB-- " + dbHelper);
        }
        if (dbHelper == null) {
            return null;
        }
        albumList = dbHelper.getList(AlbumEntity.EMPTY);
        if (App.DEBUG) {
            LogUtil.d(TAG, " --readDB-- " + dbHelper + " ---albumList--- " + albumList.size());
        }

        if (!Utils.isEmpty(albumList)) {
            for (int i = 0; i < albumList.size(); i++) {
                // AlbumEntity albumEntity = albumList.get(i);
                // unreadFileNum = unreadFileNum + albumEntity.getUpdateCount();
            }
        }
        ArrayList<Event> events = dbHelper.getAllEvent();
        if (App.DEBUG) {
            LogUtil.d(TAG, "getAllEvent size is " + (events == null ? 0 : events.size()));
        }
        return events;

    }

    public void showAlbum() {
        albumBtn.setSelected(true);
        if (albumFragment == null) {
            albumFragment = new AlbumFragment();
            // albumFragment.setOpenAlbumListener(albumListener);
            albumFragment.setAlbumListener(albumListener);
            albumFragment.setDeleteAlbumListener(deleteAlbumListener);
            albumFragment.onReadAlbumsFromDB(albumList);
            // albumFragment.setDefaultCover(defBitmap);
        }
        albumFragment.setDefaultCover(defBitmap);
        showOnly(albumFragment);
    }

    public void openAlbum(AlbumEntity albumEntity) {
        if (onOpenAlbumListener != null) {
            onOpenAlbumListener.onOpenAlbum(albumEntity);
        }
        this.oldAlbumEntity = albumEntity;
        App.putAlbum(albumEntity);
        openAlbum(albumEntity.getId());
    }

    public boolean openAlbum(String albumId) {
        Bundle bundle = new Bundle();
        bundle.putString(Consts.ALBUM_ID, albumId);
        UIManager.instance().startActivity(AlbumDetailActivity.class, bundle);
        return true;
    }

    public boolean openActiveAlbum(String albumId) {
        albumFromPush = albumId;
        ConnectBuilder.getAlbumInfo(albumId);
        return true;
    }

    private boolean onFileCreate(Object file) {
        if (albumFragment == null) {
            return false;
        }
        if (!(file instanceof FileEntity)) {
            return false;
        }
        return albumFragment.onFileCreate((FileEntity) file);
    }

    private boolean onAlbumCreate(Object album) {
        if (albumFragment == null) {
            return false;
        }
        if (!(album instanceof AlbumEntity)) {
            return false;
        }
        return albumFragment.onAlbumCreate((AlbumEntity) album);
    }

    private boolean onMemberChange(Object member, boolean increase) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "HomeActivity->onMemberLeft() member=" + " " + member);
        }
        if (albumFragment == null) {
            return false;
        }
        if (!(member instanceof MemberEntity)) {
            return false;
        }
        return albumFragment.onMemberChange((MemberEntity) member, increase);
    }

    private boolean onAlbumUpdate(Object album) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "HomeActivity->onAlbumUpdate() album=" + " " + album);
        }
        if (albumFragment == null) {
            return false;
        }
        if (!(album instanceof AlbumEntity)) {
            return false;
        }
        return albumFragment.onAlbumUpdate((AlbumEntity) album);
    }

    private boolean onFileDelete(Object file) {
        if (albumFragment == null) {
            return false;
        }
        if (!(file instanceof FileEntity)) {
            return false;
        }
        FileEntity en = (FileEntity) file;
        return albumFragment.onFileDelete(en);
    }

    private boolean onAlbumDeleted(Object album) {
        if (albumFragment == null) {
            return false;
        }
        if (!(album instanceof AlbumEntity)) {
            return false;
        }
        return albumFragment.onAlbumDeleted(((AlbumEntity) album).getId());
    }

    private void onAvaterUpdate(Event event) {
        if (event == null) {
            return;
        }
        String uid = event.getUserId();
        ImageManager.instance().deleteLocal(uid);
        ImageManager.instance().remove(uid);
        if (meFragment != null) {
            meFragment.showIconAvatar(false);
        }
    }

    public void showMe() {
        if (meFragment == null) {
            meFragment = new MeFragment();
        }
        // meFragment.setCacheSize(cacheSize);
        showOnly(meFragment);
    }

    public void bindPhoneBack() {
        onBackPressed();
    }

    private void showNotification() {
        if (unreadNotificationNum > 0) {
            readAllNotification();
        }
        if (notificationFrament == null) {
            notificationFrament = new NotificationCenterFragment();
        }
        notificationFrament.setClearNotifyCount(clearNotifyCountListener);
        showOnly(notificationFrament);
        notificationFrament.onEvent(notificationEvents);
        // showOnly(new TestFragment());

    }

    private void showLogin() {
        CToast.showToast(R.string.you_not_login);
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        this.startActivity(intent);
    }

    private void readAllNotification() {
        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper == null) {
            return;
        }
        if (!dbHelper.readAllEvent(Consts.NOTIFICATION)) {
            return;
        }
        unreadNotificationNum = 0;
        updateNotification();
    }

    // private void showAddAlbum() {
    // UMutils.instance().diyEvent(ID.EventCreateOrJoinAlbum);
    // if (createJoinAlbumDialog == null) {
    // createJoinAlbumDialog = new CreateJoinAlbumDialog();
    // }
    // createJoinAlbumDialog.show(albumList);
    // }

    private void showAddAlbum() {
        if (createJoinAlbumDialog != null && createJoinAlbumDialog.isShowing()) {
            return;
        } else {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            mDatas.add(R.string.new_ablum);
            mDatas.add(R.string.join_ablum);

            createJoinAlbumDialog = new OptionDialog(this);
            createJoinAlbumDialog.setOptionListener(this);
            createJoinAlbumDialog.setData(mDatas);
            createJoinAlbumDialog.showCancleView(true);
            createJoinAlbumDialog.setSelected(lastSort);
            createJoinAlbumDialog.show();
        }
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        lastSort = position;

        if (optionId == R.string.new_ablum) {
            UMutils.instance().diyEvent(ID.EventCreateAlbum);
            createAlbumFragment = new CreateAlbumFragment();
            showOnly(createAlbumFragment);
        } else if (optionId == R.string.join_ablum) {
            joinAlbumFragment = new JoinAlbumFragment();
            joinAlbumFragment.setAlbumList(albumList);
            showOnly(joinAlbumFragment);
        } else if (optionId == R.drawable.add_album_cancle) {
            createJoinAlbumDialog.dismiss();
        }
    }

    private void showNearby() {
        tabLocal.setSelected(true);
        if (findAlbumTabFragment == null) {
            findAlbumTabFragment = new FindAlbumTabFragment();
            findAlbumTabFragment.setAlbumListener(albumListener);
        }
        showOnly(findAlbumTabFragment);
    }

    private synchronized void onEvent(List<Event> lists) {
        if (Utils.isEmpty(lists)) {
            return;
        }
        if (notificationEvents == null) {
            notificationEvents = new ArrayList<Event>();
        }
        for (int i = 0; i < lists.size(); i++) {
            Event event = lists.get(i);
            if (event == null) {
                continue;
            }
            handleEvent(event);
        }
        // lists.clear();
        updateNotification();
        // updateAlbumNotify();
        if (notificationFrament != null && notificationFrament.isShown()) {
            notificationFrament.onEvent(notificationEvents);
        }
    }

    private void handleEvent(Event event) {
        if (event == null) {
            return;
        }
        String type = event.getOpType();
        if (Consts.ALBUM_CREATED.equals(type)) {
            onAlbumCreate(event.getObject());
        } else if (Consts.FILE_CREATED.equals(type)) {
            onFileCreate(event.getObject());
        } else if (Consts.MEMBER_JOINED.equals(type)) {
            onMemberChange(event.getObject(), true);
        } else if (Consts.AVATAR_UPDATED.equals(type)) {
            onAvaterUpdate(event);
        } else if (Consts.ALBUM_UPDATED.equals(type)) {
            onAlbumUpdate(event.getObject());
        } else if (Consts.ALBUM_DELETED.equals(type) || Consts.SYSTEM_ALBUM_DELETED.equals(type)) {
            onAlbumDeleted(event.getObject());
        } else if (Consts.MEMBER_LEFT.equals(type)) {
            onMemberChange((MemberEntity) event.getObject(), false);
        } else if (Consts.FILE_DELETED.equals(type) || Consts.SYSTEM_FILE_DELETED.equals(type)) {
            onFileDelete(event.getObject());
        }
        if (event.isNeedShown()) {
            if (notificationEvents == null) {
                return;
            }
            if (Consts.LIKE_CREATED.equals(type)) {
                // REMOVE SAME LIKE EVENT
                for (Event event2 : notificationEvents) {
                    if (event2.getObject() instanceof LikeEntity) {
                        if (Utils.isSame(event.getObjectWidthId(), event2.getObjectWidthId())) {
                            notificationEvents.remove(event2);
                            unreadNotificationNum--;
                            break;
                        }
                    }
                }
            }
            if (!event.hasRead()) {
                unreadNotificationNum++;
            }
            if (!eventIdSet.contains(event.getEventId())) {
                notificationEvents.add(event);
                eventIdSet.add(event.getEventId());
            }
        }
    }

    // private void updateAlbumNotify() {
    // updateNotify(tvAlbumNotify, unreadFileNum);
    // }

    private void updateNotification() {
        updateNotify(tvNotification, unreadNotificationNum);
    }

    private void updateNotify(TextView tv, int count) {
        if (tv == null) {
            return;
        }
        if (count <= 0) {
            tv.setVisibility(View.GONE);
            return;
        }
        tv.setVisibility(View.VISIBLE);
        if (count > 99) {
            tv.setText("");
            tv.setBackgroundResource(R.drawable.album_update);
        } else {
            tv.setText("" + count);
            tv.setBackgroundResource(R.drawable.bottom_new_bg);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        switch (requestCode) {
            case Consts.REQUEST_CODE_ZXING:
                if (albumFragment != null) {
                    albumFragment.onActivityResult(requestCode, resultCode, data);
                    if (joinAlbumFragment != null) {
                        joinAlbumFragment.onBackPressed();
                    }
                    return;
                }
                break;
            case Consts.REQUEST_CODE_FIND_FRIEND:
                IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
                bundle.setRegisterType(IRegisterController.RegisterType.bind);
                bundle.newUserBindPhone(false);
                showRegister(bundle);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (!Utils.isEmpty(session)) {
            if (id == R.id.rl_album || //
                    id == R.id.rl_notification || //
                    id == R.id.rl_local || //
                    id == R.id.rl_me) {
                if (curTab != null) {
                    curTab.setSelected(false);
                }
                v.setSelected(true);
                curTab = v;
                if (id == R.id.rl_album) {
                    showAlbum();
                } else if (id == R.id.rl_notification) {
                    showNotification();
                } else if (id == R.id.rl_me) {
                    showMe();
                } else if (id == R.id.rl_local) {
                    showNearby();
                }
            } else if (id == R.id.rl_add) {
                curTab.setSelected(false);
                curTab = albumBtn;
                curTab.setSelected(true);
                showAlbum();
                showAddAlbum();
            } else if (id == R.id.ll_share_give_up) {
                mUserGuide1.setVisibility(View.GONE);
                blankBanner1.setVisibility(View.GONE);
                blankBanner2.setVisibility(View.GONE);
                UMutils.instance().diyEvent(ID.EventTourPressSkipTour);
                if (showNewUserGuide) {
                    AppData.showNewUserGuide(false);
                }
                AppData.setStatus(Consts.STATUS_EXISTED);
            } else if (id == R.id.iv_share_try) {
                mUserGuide1.setVisibility(View.GONE);
                if (bannerIsShow) {
                    blankBanner1.setVisibility(View.GONE);
                    blankBanner2.setVisibility(View.VISIBLE);
                }
                mUserGuide2.setVisibility(View.VISIBLE);
                UMutils.instance().diyEvent(ID.EventTourPressTryTour);
            } else if (id == R.id.ll_user_guide2) {
                autoCreateAlbum();
                mUserGuide2.setVisibility(View.GONE);
                UMutils.instance().diyEvent(ID.EventTourEnterMyPhotos);
            } else if (id == R.id.tv_binding_phone) {
                mBindPhoneGuide.setVisibility(View.GONE);
                if (curTab != null) {
                    curTab.setSelected(false);
                    curTab = meBtn;
                    curTab.setSelected(true);
                }
                UMutils.instance().diyEvent(UMutils.ID.EventTourBindingMobile);
                IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
                bundle.setRegisterType(IRegisterController.RegisterType.bind);
                bundle.newUserBindPhone(false);
                showRegister(bundle);
            } else if (id == R.id.tv_skip) {
                mBindPhoneGuide.setVisibility(View.GONE);
            }
        } else {

            if (id == R.id.rl_album) {
                showLogin();
            } else if (id == R.id.rl_notification) {
                showLogin();
            } else if (id == R.id.rl_me) {
                if (curTab != null) {
                    curTab.setSelected(false);
                    curTab = meBtn;
                    curTab.setSelected(true);
                    showMe();
                }

            } else if (id == R.id.rl_local) {
                if (curTab != null) {
                    curTab.setSelected(false);
                    curTab = tabLocal;
                    showNearby();
                }
            } else if (id == R.id.rl_add) {
                showLogin();
            }
        }

        // else if (id == R.id.ll_mask) {
        // curTab.setSelected(false);
        // curTab = tabLocal;
        // curTab.setSelected(true);
        // mFoundMask.setVisibility(View.GONE);
        // showNearby();
        // }
    }

    private void autoCreateAlbum() {
        JSONObject jo = new JSONObject();
        jo.put(Consts.NAME, getResources().getString(R.string.my_photo_album));
        jo.put(Consts.NOTE, "");
        jo.put(Consts.ACCESS_PASSWORD, "");
        jo.put(Consts.IS_PRIVATE, true);
        jo.put(Consts.IS_LOCKED, false);
        jo.put(Consts.COMMENT_OFF, false);
        jo.put(Consts.LIKE_OFF, false);
        String[] permissions = new String[]{"read", "write"};
        jo.put(Consts.PERMISSIONS, permissions);
        ConnectBuilder.createAlbum(jo.toString());
    }

    @Override
    public void onBackPressed() {
        if (onBack(null)) {
            if (App.DEBUG) {
                LogUtil.v(TAG, "onBack(null):true");
            }
            return;
        } else {
            long time = System.currentTimeMillis();
            long timeD = time - lastClickTime;
            if (timeD >= 1500) {
                lastClickTime = time;
                CToast.showToast(R.string.press_again_exit_app);
                return;
            }
            if (AppData.getStatus().equals(Consts.STATUS_CREATE)) {
                AppData.setStatus(Consts.STATUS_EXISTED);
            }
            if (showNewUserGuide) {
                AppData.showNewUserGuide(false);
            }


            AppData.setQuitTime(System.currentTimeMillis());
            InitAlarm.initAlarm();
            if (albumFragment != null) {
                albumFragment.setLastRefreshAlbumListTime(0);
            }
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        Broadcaster.sendBroadcast(Consts.STOP_FETCH_EVENT_SERVICE);
        Broadcaster.unregisterReceiver(receiver);
        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (eventIdSet != null) {
            eventIdSet.clear();
            eventIdSet = null;
        }
        super.onDestroy();
    }

    @Override
    public void showSignin(RegisterBundle bundle) {
        if (mobileLoginFragment == null) {
            mobileLoginFragment = new MobileLoginFragment();
        }
        mobileLoginFragment.setRegisterBundle(bundle);
        showOnly(mobileLoginFragment);
    }

    @Override
    public void showRegister(IRegisterController.RegisterBundle bundle) {
        if (registerFragment == null) {
            registerFragment = new RegisterFragment();
        }
        registerFragment.setRegisterBundle(bundle);
        showOnly(registerFragment);
    }

    @Override
    public void showConfirmationReceiver(IRegisterController.RegisterBundle bundle) {
        if (submitVerficationCodeFragment == null) {
            submitVerficationCodeFragment = new SubmitVerficationCodeFragment();
        }
        submitVerficationCodeFragment.setRegisterBundle(bundle);
        showOnly(submitVerficationCodeFragment);
    }

    @Override
    public void showSubmiter(IRegisterController.RegisterBundle bundle) {

        if (setPasswordFragment == null) {
            setPasswordFragment = new SetPasswordFragment();
        }
        setPasswordFragment.setRegisterBundle(bundle);
        showOnly(setPasswordFragment);
    }

    @Override
    public void mobileLogin(IRegisterController.RegisterBundle bundle) {
        if (bundle.isNew) {
            if (albumFragment != null) {
                showOnly(albumFragment);
                CToast.showToast(R.string.bind_success);
                info.setMobile(bundle.mobile);
                AppData.setFirstBindPhone(false);
                if (!status.equals(Consts.STATUS_CREATE)) {
                    AppData.showNewUserGuide(true);
                }
                showUserGuide();
            }
        } else {
            if (meFragment != null) {
                showOnly(meFragment);
                meFragment.onMobileBundSuccess(bundle);
            }
        }
    }

    public void showModifyPhoneFragment(RegisterBundle bundle) {
        ModifyBindingPhoneNumberFragment modifyBindingPhoneNumberFragment = new ModifyBindingPhoneNumberFragment();
        modifyBindingPhoneNumberFragment.setBundle(bundle);
        showOnly(modifyBindingPhoneNumberFragment);
    }


    public void showChangePassword() {
        ModifyAccountPassword modifyAccountPassword = new ModifyAccountPassword();
        showOnly(modifyAccountPassword);
    }

    public void showAddFriendActivity() {
        Intent intent = new Intent(this, FriendActivity.class);
        startActivityForResult(intent, Consts.REQUEST_CODE_FIND_FRIEND);

    }
}
