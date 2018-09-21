package com.daxiangce123.android.ui.activities;

import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Window;

import com.baidu.android.pushservice.PushManager;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.SplashFragment;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class SplashActivity extends BaseCliqActivity {
	public static final String TAG = "SplashActivity";
	public static final int SPLASH_TIME = 3000; // 2s
	private Handler mHandler;
	private Runnable mRunnable;
	private BaseFragment currentFragment;

	// @Override
	// protected void onNewIntent(Intent intent) {
	// super.onNewIntent(intent);
	// if (currentFragment != null) {
	// currentFragment.onNewIntent(intent);
	// }
	// }

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		LockManager.getInstance().getAppLock().addIgnoredActivity(this.getClass());
		// getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
		// WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		// App.addActivity(this);
		initPara();
		setContentView(R.layout.splash_activity);
		final BaseFragment splashFragment = new SplashFragment();
		splashFragment.show(this);

		Intent intent = getIntent();
		Uri uri = intent.getData();
		App.scheme = uri;

		mHandler = new Handler();
		mRunnable = new Runnable() {
			public void run() {
				String session = AppData.getToken();
				if (App.DEBUG) {
					LogUtil.d(TAG, "session : " + session);
				}
				// if (!Utils.isEmpty(session)) {
				if (App.scheme != null) {
					if (Utils.isEmpty(session)) {
						Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
						AppUtil.startActivity(intent, true);
						overridePendingTransition(0, 0);
						// }
					} else if (!Utils.isEmpty(session)) {
						Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
						AppUtil.startActivity(intent, true);
						overridePendingTransition(0, 0);
					}
				} else {

					Bundle bundle = SplashActivity.this.getIntent().getExtras();
					if (bundle != null && bundle.containsKey(Consts.LOG_OUT)) {
						Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
						AppUtil.startActivity(intent, true);
						overridePendingTransition(0, 0);

						// bug that still got pushed when u clear data of app
						PushManager.stopWork(SplashActivity.this);
					} else {
						Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
						AppUtil.startActivity(intent, true);
						overridePendingTransition(0, 0);
					}
				}

				// }

				// else {
				// if (AppData.hasLogined()) {
				// currentFragment = new LoginFragment();
				// } else {
				// currentFragment = new UserGuideFragment();
				// }
				// showOnly(currentFragment);
				// UmengUpdateAgent.update(SplashActivity.this);
				// MobclickAgent.updateOnlineConfig(SplashActivity.this);
				// // bug that still got pushed when u clear data of app
				// PushManager.stopWork(SplashActivity.this);
				// }
			}
		};
		mHandler.postDelayed(mRunnable, SPLASH_TIME);
	}

	public void onStop() {
		super.onStop();
		mHandler.removeCallbacks(mRunnable);
	}

	protected void onActivityResult(int arg0, int arg1, Intent arg2) {
		if (currentFragment != null) {
			currentFragment.onActivityResult(arg0, arg1, arg2);
			return;
		}
		super.onActivityResult(arg0, arg1, arg2);
	}

	private final void initPara() {
		Resources resources = getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		// App.SCREEN_WIDTH = dm.widthPixels;
		// App.SCREEN_HEIGHT = dm.heightPixels;
	}

	@Override
	protected boolean needEventService() {
		return false;
	}

	@Override
	public void finish() {
		if (App.DEBUG) {
			LogUtil.d(TAG, "finish()	!!!!!!!!!!!!!!!!!");
		}
		super.finish();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
