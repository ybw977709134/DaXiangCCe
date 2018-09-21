package com.daxiangce123.android.ui.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.ViewUtil;

public class FragmentAboutUs extends BaseFragment implements OnClickListener {
	private View mRootView = null;
	private ImageView mBack;
	private TextView tvVersion;

	@Override
	public String getFragmentName() {
		return "FragmentAboutUs";
	}

	public FragmentAboutUs() {
		setBoottomBarVisibility(View.GONE);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.fragment_about_us, container,
					false);
			initUI();
		} else {
			ViewUtil.removeFromParent(mRootView);
		}
		updateUI();
		return mRootView;
	}

	private void initUI() {
		if (mBack == null) {
			mBack = (ImageView) mRootView.findViewById(R.id.iv_about_us_back);
			mBack.setOnClickListener(this);
		}
		tvVersion = (TextView) mRootView.findViewById(R.id.tv_versions);
		mRootView.findViewById(R.id.agreement).setOnClickListener(this);
		mRootView.findViewById(R.id.privacy).setOnClickListener(this);
	}

	private void updateUI() {
		String version = getString(R.string.version_x, " " + App.mobileInfo.VERSION
				+ " / " + App.mobileInfo.BUILD_NUMBER);
		tvVersion.setText(version);
	}

	private void showAgreement() {
		BrowserFragment browserFragment = new BrowserFragment();
		browserFragment.setHomeUrl(Consts.URL_AGREE);
		browserFragment.show(getBaseActivity());
	}

	private void showPrivacy() {
		BrowserFragment browserFragment = new BrowserFragment();
		browserFragment.setHomeUrl(Consts.URL_PRIVACY);
		browserFragment.show(getBaseActivity());
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.iv_about_us_back) {
			back();
		} else if (id == R.id.agreement) {
			showAgreement();
		} else if (id == R.id.privacy) {
			showPrivacy();
		}
	}

}
