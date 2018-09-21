package com.daxiangce123.android.ui.pages.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.View;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.BaseHandler;
import com.daxiangce123.android.util.BaseHandler.HandleListener;
import com.daxiangce123.android.util.LogUtil;
import com.umeng.analytics.MobclickAgent;

/**
 * @author ram
 * @project Cliq
 * @time Mar 20, 2014
 */
public abstract class BaseFragment extends Fragment implements HandleListener {

    // private View contentView;
    // protected abstract View getContentView(LayoutInflater inflater);
    private int bottomVisibility = View.VISIBLE;
    private int titleVisibility = View.VISIBLE;
    public final static String TAG = "BaseFragment";
    private boolean isShowen = false;

    private static BaseHandler mHandler = new BaseHandler();

    public abstract String getFragmentName();

    private void updateBaseicFrame() {
        updateBaseicFrame(getActivity());
    }

    private void updateBaseicFrame(Activity activity) {
        if (activity == null) {
            return;
        }
        View titleBar = activity.findViewById(R.id.title_bar);
        if (titleBar != null) {
            titleBar.setVisibility(titleVisibility);
        }
        if (!isVisible()) {
            return;
        }
        View bottomBar = activity.findViewById(R.id.bottom_bar);
        if (bottomBar != null) {
            bottomBar.setVisibility(bottomVisibility);
        }
    }

    public void setBoottomBarVisibility(int visibility) {
        this.bottomVisibility = visibility;
    }

    public void setTitleBarVisibility(int visibility) {
        this.titleVisibility = visibility;
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        // TODO Auto-generated method stub
        if (getActivity() != null) {
            getActivity().startActivityForResult(intent, requestCode);
        } else {
            super.startActivityForResult(intent, requestCode);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        // LogUtil.d(TAG, getClass().getSimpleName() + "	onAttach() "
        // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
        // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
        // + "  isRemoving()=" + isRemoving());
        // updateBaseicFrame(activity);
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void onNewIntent(Intent intent) {
    }

    // @Override
    // public void onViewCreated(View view, Bundle savedInstanceState) {
    // LogUtil.d(TAG, getClass().getSimpleName() + "	onViewCreated() "
    // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
    // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
    // + "  isRemoving()=" + isRemoving());
    // super.onViewCreated(view, savedInstanceState);
    // }

    public void onResume() {
        // LogUtil.d(TAG, getClass().getSimpleName() + "	onResume() "
        // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
        // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
        // + "  isRemoving()=" + isRemoving());
        onShown();
        super.onResume();
        MobclickAgent.onPageStart(getFragmentName());
    }

    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getFragmentName());
    }

    @Override
    public void onStop() {
        // LogUtil.d(TAG, getClass().getSimpleName() + "	onStop() "
        // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
        // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
        // + "  isRemoving()=" + isRemoving());
        onHidden();
        super.onStop();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        // LogUtil.d(TAG, getClass().getSimpleName()
        // + "	onHiddenChanged() hidden=" + hidden + " " + "isVisible() "
        // + isVisible() + " isHidden()=" + isHidden() + " isDetached()="
        // + isDetached() + " isAdded()=" + isAdded() + "  isRemoving()="
        // + isRemoving());
        if (hidden) {
            onHidden();
        } else {
            onShown();
        }
        super.onHiddenChanged(hidden);
    }

    // @Override
    // public void onSaveInstanceState(Bundle outState) {
    // LogUtil.d(TAG, getClass().getSimpleName()
    // + "	onSaveInstanceState() outState" + outState);
    // super.onSaveInstanceState(outState);
    // }

    // @Override
    // public void onStart() {
    // LogUtil.d(TAG, getClass().getSimpleName() + "	onStart() "
    // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
    // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
    // + "  isRemoving()=" + isRemoving());
    // super.onStart();
    // }

    // public void onPause() {
    // LogUtil.d(TAG, getClass().getSimpleName() + "	onPause() "
    // + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
    // + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
    // + "  isRemoving()=" + isRemoving());
    // super.onPause();
    // }

    @Override
    public void onDestroy() {
        if (App.DEBUG) {
            LogUtil.d(TAG, getClass().getSimpleName() + "	onDestroy() "
                    + "isVisible() " + isVisible() + " isHidden()=" + isHidden()
                    + " isDetached()=" + isDetached() + " isAdded()=" + isAdded()
                    + "  isRemoving()=" + isRemoving());
        }
        super.onDestroy();
    }

    /**
     * same logic with keyEvent
     *
     * @return Return true to prevent this event from being propagated further,
     * or false to indicate that you have not handled this event and it
     * should continue to be propagated.
     * @see android.view.KeyEvent
     * @see android.app.Activity#onKeyUp(int, android.view.KeyEvent)
     */
    public boolean onBackPressed() {
        return false;
    }

    public void setData(JSONObject jo) {
    }

    public void show(BaseCliqActivity activity) {
        if (activity == null) {
            return;
        }
        activity.showFragment(this);
    }

    public BaseCliqActivity getBaseActivity() {
        Activity activity = getActivity();
        if (activity instanceof BaseCliqActivity) {
            return (BaseCliqActivity) activity;
        }
        return null;
    }

    /**
     * finish the activity which this fragment had been attached
     */
    public void finish() {
        if (getActivity() == null) {
            return;
        }
        getActivity().finish();
    }

    /**
     * @see BaseFragment#back(JSONObject)
     */
    public void back() {
        back(null);
    }

    /**
     * Activity will directly back to previous fragment(or {@link #finish()})
     * without care about {@link BaseFragment#onBackPressed()}
     *
     * @param jo
     */
    public void back(JSONObject jo) {
        if (getActivity() == null) {
            return;
        }
        getBaseActivity().back(jo);
    }

    /**
     * !isAdded() || !isResumed()
     *
     * @return
     * @time Apr 1, 2014
     */
    public boolean isShown() {
        return isShowen;
    }

    /**
     * onResult / onHiddenChanged
     *
     * @time Apr 15, 2014
     */
    public void onShown() {
        mHandler.setHandleListener(this);
        updateBaseicFrame();
        isShowen = true;
    }

    /**
     * onStop / onHiddenChanged
     *
     * @time Apr 15, 2014
     */
    public void onHidden() {
        isShowen = false;
    }

    @Override
    public void handleMessage(Message msg) {

    }

    /**
     * @see Handler#sendEmptyMessage(int)
     */
    protected void sendMessage(int what) {
        sendMessage(what, 0);
    }

    /**
     * @see Handler#sendEmptyMessage(int)
     */
    protected void sendMessage(int what, int delay) {
        if (delay <= 0) {
            mHandler.sendEmptyMessage(what);
        } else {
            mHandler.sendEmptyMessageDelayed(what, delay);
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

}
