package com.daxiangce123.android.ui.activities.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Window;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.BaseHandler;
import com.daxiangce123.android.util.BaseHandler.HandleListener;
import com.daxiangce123.android.util.LogUtil;

/**
 * @author ram
 * @project Cliq
 */
public class BaseActivity extends BaseFragmentActivity implements HandleListener {
    public final static String TAG = "BaseActivity";
    /**
     * use static handler to avoid handler leak
     */
    private final BaseHandler mHandler = new BaseHandler();
    private static boolean DEBUG = true;

    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        mHandler.setHandleListener(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onStart();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clearAllMessage();
    }

    /**
     * @see Handler#sendEmptyMessage(int)
     */
    protected void sendMessage(int what) {
        sendMessage(what, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    /**
     * @see Handler#sendEmptyMessage(int)
     */
    public void sendMessage(int what, int delay) {
        if (delay <= 0) {
            mHandler.sendEmptyMessage(what);
        } else {
            mHandler.sendEmptyMessageDelayed(what, delay);
        }
        if (DEBUG) {
            LogUtil.d(TAG, "sendMessage()	what=" + what + "	delay=" + delay);
        }
    }

    /**
     * @see Handler#sendMessage(Message)
     */
    protected void sendMessage(Message msg) {
        if (msg == null) {
            return;
        }
        sendMessage(msg, 0);
    }

    /**
     * @see Handler#sendMessage(Message)
     */
    protected void sendMessage(Message msg, int delay) {
        if (msg == null) {
            return;
        }
        if (delay <= 0) {
            mHandler.sendMessage(msg);
        } else {
            mHandler.sendMessageDelayed(msg, delay);
        }
        mHandler.setHandleListener(null);
    }

    /**
     * @param what {@link Message#what}
     * @param obj  {@link Message#obj}
     */
    protected void sendMessage(int what, Object obj) {
        sendMessage(what, obj, 0);
    }

    /**
     * @param what {@link Message#what}
     * @param obj  {@link Message#obj}
     */
    protected void sendMessage(int what, Object obj, int delay) {
        if (obj == null) {
            sendMessage(what, delay);
            return;
        }
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = obj;
        sendMessage(msg, delay);
    }

    /**
     * all callbacks and messages will be removed.
     * <p/>
     * if {@link #onDestroy()} this will be called
     * <p/>
     *
     * @see Handler#removeCallbacksAndMessages(Object)
     */
    protected void clearAllMessage() {
        if (mHandler == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.v(TAG, "clearAllMessage");
        }
        mHandler.removeCallbacksAndMessages(null);
    }

    /**
     * handle all messages
     */
    @Override
    public void handleMessage(Message msg) {

    }

}
