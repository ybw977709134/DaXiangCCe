package com.daxiangce123.android.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.daxiangce123.R;
import com.daxiangce123.android.applock.core.AppLock;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.view.Preference;

public class PwdSetupActivity extends BaseCliqActivity implements
		OnClickListener {
	public static final String TAG = "HomePage";

	private Preference pfOnOff;
	private Preference pfChange;
	private ImageView mBack;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pwd_setup);
		// App.addActivity(this);
		initUI();
		updateUI();
	}

	@Override
	public void onClick(View view) {
		if (view.equals(pfOnOff)) {
			int type = LockManager.getInstance().getAppLock().isPasscodeSet() ? AppLock.DISABLE_PASSLOCK
					: AppLock.ENABLE_PASSLOCK;
			Intent intent = new Intent(this, AppLockActivity.class);
			intent.putExtra("type", type);
			startActivityForResult(intent, type);
		} else if (view.equals(pfChange)) {
			Intent intent = new Intent(this, AppLockActivity.class);
			intent.putExtra("type", AppLock.CHANGE_PASSWORD);
			intent.putExtra("message",
					getString(R.string.passcode_enter_old_passcode));
			startActivityForResult(intent, AppLock.CHANGE_PASSWORD);
		} else if (view.equals(mBack)) {
			finish();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode) {
		case AppLock.DISABLE_PASSLOCK:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this,
						getString(R.string.passcode_close_successed),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case AppLock.ENABLE_PASSLOCK:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this,
						getString(R.string.passcode_set_successed),
						Toast.LENGTH_SHORT).show();
			}
			break;
		case AppLock.CHANGE_PASSWORD:
			if (resultCode == RESULT_OK) {
				Toast.makeText(this,
						getString(R.string.passcode_change_successed),
						Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
		updateUI();
	}

	private void initUI() {
		pfOnOff = (Preference) findViewById(R.id.pf_on_off);
		pfOnOff.setOnClickListener(this);

		pfChange = (Preference) findViewById(R.id.pf_change);
		pfChange.setOnClickListener(this);

		mBack = (ImageView) findViewById(R.id.iv_back);
		mBack.setOnClickListener(this);
	}

	private void updateUI() {
		if (LockManager.getInstance().getAppLock().isPasscodeSet()) {
			pfOnOff.setTitle(getResources().getString(
					R.string.passcode_turn_off));
			pfOnOff.setTitleColor(getResources().getColorStateList(
					R.color.preference_title_blue_color));
			pfChange.setEnabled(true);
			pfChange.setTitleColor(getResources().getColorStateList(
					R.color.preference_title_blue_color));
		} else {
			pfOnOff.setTitle(getResources()
					.getString(R.string.passcode_turn_on));
			pfOnOff.setTitleColor(getResources().getColorStateList(
					R.color.preference_title_blue_color));
			pfChange.setEnabled(false);
			pfChange.setTitleColor(getResources().getColorStateList(
					R.color.black));
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

}
