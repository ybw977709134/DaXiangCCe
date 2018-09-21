package com.daxiangce123.android.ui.view;

import android.app.Activity;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.ui.view.CDialog.PDialogBackListener;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.LogUtil;

public class LoadingDialog {

    private final static String TAG = "LoadingDialog";
    private static CDialog mDialog;
    private static PDialogBackListener dialogBackListener = new PDialogBackListener() {
        @Override
        public void onPBackPressed(DialogInterface dialog) {
            LogUtil.i(TAG, "onYBackPressed");
            dismiss();
        }
    };

    private static void init(String msg) {
        if (mDialog == null) {
            View contentView = LayoutInflater.from(App.getActivity()).inflate(
                    R.layout.view_loading_dialog, null);
            mDialog = new CDialog();
            mDialog.setBackListener(dialogBackListener);
            mDialog.setContentView(contentView);
        }
        TextView tvMsg = (TextView) mDialog
                .findViewById(R.id.tv_msg_yprocess_dialog);
        tvMsg.setText(msg);
    }

    // public static LoadingDialog setCancelable(boolean cancelAble) {
    // if (mDialog == null) {
    // return null;
    // }
    // mDialog.setCancelable(cancelAble);
    // return mDialog;
    // }

    public static boolean Showing() {
        if (mDialog == null) {
            return false;
        }
        return mDialog.isShowing();
    }

    public static CDialog show(int resId) {
        if (resId <= 0) {
            mDialog.setCancelable(true);
            return mDialog;
        }
        String message = AppUtil.getString(resId);
        return show(message);
    }

    public static CDialog show(String msg) {
        if (!isValid()) {
            if (mDialog != null) {
                mDialog.setCancelable(true);
            }
            return mDialog;
        }
        init(msg);
        if (Showing()) {
            return mDialog;
        }
        try {
            mDialog.show();
            if (App.DEBUG) {
                LogUtil.d(TAG, "show()");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mDialog;
    }

    public static void dismiss() {
        if (!Showing()) {
            return;
        }
        try {
            mDialog.setCancelable(true);
            mDialog.dismiss();
            mDialog = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, "dismiss()");
        }
    }

    private final static boolean isValid() {
        if (!(App.getActivity() instanceof Activity)) {
            return false;
        }
        Activity activity = App.getActivity();
        if (activity.isFinishing()) {
            return false;
        }
        return true;
    }

}
