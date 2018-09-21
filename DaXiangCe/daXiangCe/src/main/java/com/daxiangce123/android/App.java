package com.daxiangce123.android;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.util.LruCache;
import android.util.DisplayMetrics;

import com.baidu.frontia.FrontiaApplication;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.business.MobileInfo;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.monitor.MediaMonitor;
import com.daxiangce123.android.monitor.NetworkMonitor;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.ConfigHacker;
import com.daxiangce123.android.util.ConfigHacker.HackerListener;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.outh.WXHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

public class App extends Application {

    private final static String TAG = "App_";
    private static Context mAppContext;
    private static BaseCliqActivity mActivity;
    private static SharedPreferences mAppPrefs ;
    private static ArrayList<BaseCliqActivity> activityList;
    private static LruCache<String, AlbumEntity> albumEntityCache;

    public static MobileInfo mobileInfo;

    public static int SCREEN_WIDTH;
    public static int SCREEN_HEIGHT;
    public static float SCREEN_DENSITY;

    public static String NET_STATE = Consts.INTERNAL;

    public static boolean RELEASE = false;
    /**
     * Change its value in meta-data.<br>
     * If {@value #RELEASE} is TRUE or DEBUG = FALSE meta-data will not take
     * effect
     */
    public static boolean DEBUG = true;//
    public static DBHelper dbHelper;
    public static UserInfo info;
    public static Uri scheme;
    public static String shareType;
    public static String shareObject;
    public static boolean shareToFriends;
    public static boolean NO_HTTPS = true;

    public static WXHelper wxHelper;
    public static AlbumItemController albumItemController;


    public void onCreate() {
        initEnv();
        init();
        detectDeviceInfo();
        readConfig();
        Broadcaster.start();
        MediaMonitor.start();
        NetworkMonitor.start();
        ImageManager.instance().init(getApplicationContext());
        LockManager.getInstance().enableAppLock(this);
        renameOldSaveDir();
        if (DEBUG) {
            LogUtil.d("Context", "ApplicationContext is Application ? " + (mAppContext == this));
//            LogUtil.d("Context", "mAppContext	" + Utils.printInheritance(mAppContext));
//            LogUtil.d("Context", "mAppContext	" + Utils.printInheritance(getBaseContext()));
        }
    }

    /**
     * <pre>
     * "http://10.32.1.5:8081/1.0";// inner
     *  "http://203.195.194.27/1.0";// out
     *  "http://10.32.5.107:8081/1.0";//thomas
     *  "https://api.daxiangce123.com/1.0";//live
     * </pre>
     */
    protected void initEnv() {
        configEnv(Consts.INTERNAL);
        // BAIDU PUSH
        FrontiaApplication.initFrontiaApplication(getApplicationContext());
    }


    private void init() {
        if (RELEASE) {
            DEBUG = false;
        }
        activityList = new ArrayList<BaseCliqActivity>();
        albumEntityCache = new LruCache<>(Consts.CONFIG_ALBUM_CACHE_LIMIT);
        mAppContext = getApplicationContext();
        mAppPrefs = getSharedPreferences("application", Context.MODE_PRIVATE);
        if (DEBUG) {
            DEBUG = AppUtil.getMetaData(mAppContext, "DEBUG", true);
        }
        LogUtil.init();
        LogUtil.i(TAG, "***************************************");
        LogUtil.i(TAG, "********** Cliq Application *********");
        LogUtil.i(TAG, "**********	" + DEBUG + "	*********");
        LogUtil.i(TAG, "***************************************");
    }

    private final void detectDeviceInfo() {
        mobileInfo.MODEL = android.os.Build.MODEL;
        mobileInfo.DEVICE = android.os.Build.DEVICE;
        mobileInfo.BRAND = android.os.Build.BRAND;
        mobileInfo.PRODUCT = android.os.Build.PRODUCT;
        mobileInfo.DISPLAY = android.os.Build.DISPLAY;
        mobileInfo.MANUFACTURER = android.os.Build.MANUFACTURER;
        if (App.DEBUG) {
            LogUtil.v(TAG, "BRAND=" + mobileInfo.BRAND + " DISPLAY=" + Utils.getDeviceId());
        }
        try {
            PackageInfo pkgInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_CONFIGURATIONS);
            mobileInfo.BUILD_NUMBER = pkgInfo.versionCode;
            mobileInfo.VERSION = pkgInfo.versionName;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            mobileInfo.BUILD_NUMBER = 0;
            mobileInfo.VERSION = "0.0";
        }
        resizeScreenSize(true);
    }

    public final static void resizeScreenSize(boolean resetWidth) {
        Resources resources = getAppContext().getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        SCREEN_WIDTH = dm.widthPixels;
        SCREEN_HEIGHT = dm.heightPixels;
        SCREEN_DENSITY = dm.density;
        if (resetWidth) {
            if (SCREEN_HEIGHT < SCREEN_WIDTH) {
                int tmp = SCREEN_HEIGHT;
                SCREEN_HEIGHT = SCREEN_WIDTH;
                SCREEN_WIDTH = tmp;
            }
        }
    }

    public final static DBHelper getDBHelper() {
        String uid = getUid();
        if (Utils.isEmpty(uid)) {
            return null;
        }
        if (dbHelper == null) {
            dbHelper = new DBHelper(uid);
        } else if (!dbHelper.getDbName().equals(uid)) {
            dbHelper.close();
            dbHelper = new DBHelper(uid);
        }
        return dbHelper;
    }

    public final static Context getAppContext() {
        return mAppContext;
    }

    public final static void removeActivity(BaseCliqActivity activity) {
        activityList.remove(activity);
    }

    public final static void finish() {
        try {
            for (BaseCliqActivity activity : activityList) {
                if (activity != null) activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static void addActivity(BaseCliqActivity activity) {
        activityList.add(activity);
    }

    public final static void setActivity(BaseCliqActivity activity) {
        mActivity = activity;
    }

    public final static BaseCliqActivity getActivity() {
        if (mActivity == null) {
            return null;
        }
        return mActivity;
    }

    public final static ArrayList<BaseCliqActivity> getActivityList() {
        if (Utils.isEmpty(activityList)) {
            return null;
        }
        return activityList;
    }

    public final static SharedPreferences getAppPrefs() {
        return mAppPrefs;
    }

    public final static void setUserInfo(UserInfo uinfo) {
        info = uinfo;
    }

    public final static String getUid() {
        if (info != null) {
            return info.getId();
        }
        return AppData.getUid();
    }

    public final static UserInfo getUserInfo() {
        return info;
    }


    public static void closeClearAll() {
        if (activityList == null) {
            return;
        }
        try {
            while (!activityList.isEmpty()) {
                BaseCliqActivity activity = activityList.remove(0);
                activity.clearFragments();
                activity.finish();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        ImageManager.instance().clearMemory();
    }

    private void readConfig() {
        String configPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "daxiangce" + File.separator + "config";
        ConfigHacker.instance()//
                .setDebuggable(DEBUG)//
                .setConfigPath(configPath)//
                .setHackerListener(new HackerListener() {

                    @Override
                    public void OnReadConfig(Result result, Map<String, String> map) {
                        if (result != Result.SUCCESS) {
                            return;
                        }
                        if (map == null) {
                            return;
                        }
                        if (map.containsKey("debug")) {
                            DEBUG = Boolean.valueOf(map.get("debug"));
                        }
                        if (map.containsKey("host")) {
                            String host = map.get("host");
                            if (!Utils.isEmpty(host)) {
                                configEnv(host);
                            }
                        }
                    }
                })//
                .read();
    }

    private void configEnv(String host) {
        NET_STATE = host;
        if (Consts.INTERNAL.equals(host)) {
            Consts.HOST_HTTPS = "http://10.32.1.5:8081/1.0";//
            Consts.HOST_HTTP = "http://10.32.1.5:8081/1.0";//
            Consts.URL_ENTITY_VIEWER = "http://dev.daxiangce123.com/sharelink?link=";
            Consts.URL_ENTITY_RAW = "http://dev.daxiangce123.com/share/shareimg?link=";
            Consts.URL_AGREE = "http://dev.daxiangce123.com/agree";
            Consts.URL_PRIVACY = "http://dev.daxiangce123.com/privacy";
            Consts.URL_RECOMMEND_APPS = "http://dev.daxiangce123.com/app_recommand";
            Consts.URL_RECOMMEND_APPS_STAUTS = "http://dev.daxiangce123.com/get_v_info";
            Consts.URL_GET_ACTIVITY_PAGE = "http://dev.daxiangce123.com/get_event_info?type=1";
            Consts.URL_ACTIVITY_PAGE = "http://dev.daxiangce123.com/event";
            Consts.URL_GET_ALBUM_QR = "http://dev.daxiangce123.com/get_qrcode_info?type=1";
        } else {
            Consts.HOST_HTTPS = "https://api.daxiangce123.com/1.0";//
            Consts.HOST_HTTP = "http://api.daxiangce123.com/1.0";//
            Consts.URL_ENTITY_VIEWER = "https://www.daxiangce123.com/sharelink?link=";
            Consts.URL_ENTITY_RAW = "https://www.daxiangce123.com/share/shareimg?link=";
            Consts.URL_AGREE = "https://www.daxiangce123.com/agree";
            Consts.URL_PRIVACY = "https://www.daxiangce123.com/privacy";
            Consts.URL_RECOMMEND_APPS = "https://www.daxiangce123.com/app_recommand";
            Consts.URL_RECOMMEND_APPS_STAUTS = "https://www.daxiangce123.com/get_v_info";
            Consts.URL_GET_ACTIVITY_PAGE = "https://www.daxiangce123.com/get_event_info?type=1";
            Consts.URL_ACTIVITY_PAGE = "https://www.daxiangce123.com/event";
            Consts.URL_GET_ALBUM_QR = "https://www.daxiangce123.com/get_qrcode_info?type=1";
        }
    }

    /**
     * the old dir for saving is not suitable, renaming it to a new one
     */
    private void renameOldSaveDir() {
        final String path = MediaUtil.getDestCliqSaveDir();
        if (!FileUtil.exists(path)) {
            return;
        }
        final String newPath = MediaUtil.getDestSaveDir();
        TaskRuntime.instance().run(new Runnable() {

            @Override
            public void run() {
                long start = System.currentTimeMillis();
                FileUtil.renameTo(path, newPath);
                if (DEBUG) {
                    LogUtil.d(App.TAG, "initMediaMonitor()	DURATION	" + (System.currentTimeMillis() - start));
                }
            }
        });
    }

    public static AlbumEntity putAlbum(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return null;
        } else {
            return albumEntityCache.put(albumEntity.getId(), albumEntity);
        }
    }

    public static AlbumEntity getAlbum(String id) {
        return albumEntityCache.get(id);
    }
}
