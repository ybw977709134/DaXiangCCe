package com.daxiangce123.android.ui.activities.base;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.applock.core.PageListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

import java.util.List;

/**
 * @author ram
 * @project Cliq
 */
public class BaseCliqActivity extends BaseActivity {
    public final static String TAG = "BaseCliqActivity";
    private static boolean DEBUG = true;
    private static boolean DEBUG_LIFE_CIRCLE = true;
    private boolean isForeGround = true;
    private static PageListener pageCallback;

    public static void setListener(PageListener listener) {
        pageCallback = listener;
    }

    /**
     * use static handler to avoid handler leak
     */
    protected void onCreate(Bundle savedInstanceState) {
        App.setActivity(this);
        App.addActivity(this);
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }

        if (pageCallback != null) {
            pageCallback.onActivityCreated(this);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (needEventService()) {
            Broadcaster.sendBroadcast(Consts.START_EVENT_SERVICE);
        }

        if (pageCallback != null) {
            pageCallback.onActivityStarted(this);
        }

    }

    @Override
    protected void onDestroy() {
        App.removeActivity(this);
        isForeGround = false;
        super.onDestroy();

        if (pageCallback != null) {
            pageCallback.onActivityDestroyed(this);
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        isForeGround = true;
    }

    @Override
    public void onResume() {
        App.setActivity(this);
        super.onResume();
        MobclickAgent.onResume(this);
        isForeGround = true;

        if (pageCallback != null) {
            pageCallback.onActivityResumed(this);
        }

    }

    @Override
    public void onPause() {
        super.onPause();
        isForeGround = false;
        MobclickAgent.onPause(this);

        if (pageCallback != null) {
            pageCallback.onActivityPaused(this);
        }

    }

    @Override
    protected void onStop() {
        super.onStop();

        if (pageCallback != null) {
            pageCallback.onActivityStopped(this);
        }
        if (App.DEBUG && DEBUG_LIFE_CIRCLE) {
            LogUtil.v(TAG, "onStop:" + getClass().getName().toString());
        }
    }

    public boolean isForeGround() {
        return isForeGround;
    }

    // @Override
    // public void onLowMemory() {
    // if (DEBUG) {
    // LogUtil.d(TAG, "onLowMemory!!!!!!!!!!!");
    // }
    // if (App.getActivity() == this) {
    // if (DEBUG) {
    // LogUtil.d(TAG, "onLowMemory!!!!!!!!!!!	CURRENT ACTIVITY");
    // }
    // }
    // App.
    // super.onLowMemory();
    // }

    protected boolean needEventService() {
        return true;
    }

    @Override
    protected void onActivityResult(int arg0, int arg1, Intent arg2) {
        // TODO Auto-generated method stub
        super.onActivityResult(arg0, arg1, arg2);
    }

    public static boolean isAppShowen(Context context) {
        if (context == null) {
            return false;
        }
        String packageName = context.getPackageName();
        ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses != null) {
            for (ActivityManager.RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(packageName)) {
                    int status = runningAppProcessInfo.importance;
                    if (status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE || status == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                        return true;
                    }
                    return false;
                }
            }
        }
        return false;
    }
}
