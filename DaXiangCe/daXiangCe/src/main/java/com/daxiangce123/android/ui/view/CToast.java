package com.daxiangce123.android.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.Toast;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

// avoid to many messages showed on screen

public class CToast {
    public static final String TAG = "PToast";

    private static Toast mToast;

    @SuppressLint("ShowToast")
    private static synchronized final void initToast() {
        if (mToast != null) {
            return;
        }
        Context context = App.getAppContext();
        mToast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
    }

    public static synchronized final void showToast(int resId) {
        if (resId < 0) {
            LogUtil.w(TAG, "invalid resource id");
            return;
        }
        initToast();
        mToast.setText(resId);
        mToast.show();
    }

    public static synchronized final void showToast(String message) {
        if (Utils.isEmpty(message)) {
            LogUtil.w(TAG, "invalid message string");
            return;
        }
        initToast();
        mToast.setText(message);
        mToast.show();
    }

    public static synchronized final void cancelToast() {
        if (mToast == null) {
            return;
        }
        mToast.cancel();
    }

    public static synchronized final void showToast(int resId, int length) {
        if (resId < 0) {
            LogUtil.w(TAG, "invalid resource id");
            return;
        }
        if (mToast != null) {
            return;
        }
        Context context = App.getAppContext();
        mToast = Toast.makeText(context, "", length);
        mToast.setText(resId);
        mToast.show();
    }
}
