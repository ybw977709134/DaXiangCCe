package com.daxiangce123.android.ui.test;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.daxiangce123.R;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.LogUtil;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @project DaXiangCe
 * @time Sep 26, 2014
 * @author ram
 */
public class TestFragment extends BaseFragment {

	private final String TAG = "TestFragment";
	private View mRootView = null;
	ListView lvNotification;

	@Override
	public String getFragmentName() {
		return "NotificationCenterFragment";
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		LogUtil.d(TAG, "onCreateView");
		if (mRootView == null) {
			initCompontent();
		} else {
			// ViewUtil.removeFromParent(mRootView);
		}
		return mRootView;
	}

	private void initCompontent() {
		LogUtil.d(TAG, "initCompontent	");
		// sucked
		// lvNotification = new ListView(getActivity());
		// mRootView = lvNotification;

		// Smooth
		mRootView = LayoutInflater.from(getActivity()).inflate(R.layout.test_listview, null, false);
		PullToRefreshListView mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.gv_members);
		lvNotification = mPullRefreshListView.getRefreshableView();
		lvNotification.setAdapter(new TestAdapter());
	}

	public void onResume() {
		super.onResume();
		LogUtil.d(TAG, "onResume");
	}

	public void onHiddenChanged(boolean hidden) {
		super.onHiddenChanged(hidden);
		LogUtil.d(TAG, "onHiddenChanged	" + hidden);
	}
}
