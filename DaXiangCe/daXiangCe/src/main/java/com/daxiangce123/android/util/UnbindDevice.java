package com.daxiangce123.android.util;

import android.content.Intent;
import android.os.Bundle;

import com.baidu.android.pushservice.PushManager;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.RequestManager;
import com.daxiangce123.android.ui.UIManager;
import com.daxiangce123.android.ui.activities.SplashActivity;

public class UnbindDevice {
    public static void unbindDevice(boolean isStart) {
        try {
            RequestManager.sharedInstance().stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        PushManager.stopWork(App.getActivity());
        ConnectBuilder.unregisterNotificationId(AppData.getRegisterId());
        AppData.clear();
        App.setUserInfo(null);
        ConnectBuilder.clear();
        LockManager.getInstance().getAppLock().setPasscode(null);
        Broadcaster.sendBroadcast(new Intent(Consts.STOP_EVENT_SERVICE));
        if (isStart) {
            startActivity();
        }
    }

    public static void startActivity() {
        Bundle bundle = new Bundle();
        bundle.putString(Consts.LOG_OUT, "");
        UIManager.instance().startActivity(SplashActivity.class, bundle);
    }
}
