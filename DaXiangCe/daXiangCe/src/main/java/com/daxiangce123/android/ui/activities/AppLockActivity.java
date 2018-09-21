package com.daxiangce123.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.View.OnTouchListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.applock.core.AppLock;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.UnbindDevice;
import com.daxiangce123.android.util.Utils;

public class AppLockActivity extends BaseCliqActivity {
	private int type = -1;
	private String unverifiedPasscode = null;

	protected EditText pinCodeField1 = null;
	protected EditText pinCodeField2 = null;
	protected EditText pinCodeField3 = null;
	protected EditText pinCodeField4 = null;
	protected InputFilter[] filters = null;
	protected TextView topMessage = null;
	private TextView mErrorMsg;
	private TextView mTitle = null;
	private LinearLayout llTitle;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				// Response response =
				// intent.getParcelableExtra(Consts.RESPONSE);
				// ConnectInfo connectInfo = intent
				// .getParcelableExtra(Consts.REQUEST);
				if (Consts.APP_LOCK_UNBIND_DEVICE.equals(action)) {
					// unbindDevice();
					AppData.setAppLockTime(0);
					UnbindDevice.unbindDevice(true);
					App.finish();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		}

		@Override
		public void afterTextChanged(Editable s) {
			// String value = s.toString();
			// if (value.length() > 0) {
			requestNext();
			// }
		}

	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// super.onCreate(savedInstanceState);

		super.onCreate(savedInstanceState);
		// App.addActivity(this);
		setContentView(R.layout.activity_app_lock);

		topMessage = (TextView) findViewById(R.id.top_message);
		mTitle = (TextView) findViewById(R.id.tv_title);
		llTitle = (LinearLayout) findViewById(R.id.ll_title);
		mErrorMsg = (TextView) findViewById(R.id.tv_error_msg);

		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			String message = extras.getString("message");
			if (message != null) {
				topMessage.setText(message);
			}

			type = extras.getInt("type", -1);
		}

		filters = new InputFilter[2];
		filters[0] = new InputFilter.LengthFilter(1);
		filters[1] = onlyNumber;

		// Setup the pin fields row
		pinCodeField1 = (EditText) findViewById(R.id.pincode_1);
		setupPinItem(pinCodeField1);

		pinCodeField2 = (EditText) findViewById(R.id.pincode_2);
		setupPinItem(pinCodeField2);

		pinCodeField3 = (EditText) findViewById(R.id.pincode_3);
		setupPinItem(pinCodeField3);

		pinCodeField4 = (EditText) findViewById(R.id.pincode_4);
		setupPinItem(pinCodeField4);

		overridePendingTransition(R.anim.slide_up, R.anim.do_nothing);

		switch (type) {

			case AppLock.DISABLE_PASSLOCK:
				llTitle.setVisibility(View.VISIBLE);
				mTitle.setText(R.string.passcode_turn_off);
				break;
			case AppLock.ENABLE_PASSLOCK:
				llTitle.setVisibility(View.VISIBLE);
				mTitle.setText(R.string.passcode_turn_on);
				break;
			case AppLock.CHANGE_PASSWORD:
				llTitle.setVisibility(View.VISIBLE);
				mTitle.setText(R.string.passcode_change_passcode);
				break;
			case AppLock.UNLOCK_PASSWORD:
				llTitle.setVisibility(View.GONE);
				mTitle.setText("");
				break;
		}

		initBroadcast();
		requestNext();
	}

	public int getType() {
		return type;
	}

	protected void onPinLockInserted() {
		String passLock = pinCodeField1.getText().toString() + pinCodeField2.getText().toString() + pinCodeField3.getText().toString() + pinCodeField4.getText();

		pinCodeField1.setText("");
		pinCodeField2.setText("");
		pinCodeField3.setText("");
		pinCodeField4.setText("");
		pinCodeField1.requestFocus();

		switch (type) {

			case AppLock.DISABLE_PASSLOCK:
				if (LockManager.getInstance().getAppLock().checkPasscode(passLock)) {
					setResult(RESULT_OK);
					LockManager.getInstance().getAppLock().setPasscode(null);
					mErrorMsg.setVisibility(View.GONE);
					finish();
				} else {
					// showPasscodeError();
					mErrorMsg.setVisibility(View.VISIBLE);
					mErrorMsg.setText(R.string.passcode_wrong_passcode);
					runShake();
				}
				break;

			case AppLock.ENABLE_PASSLOCK:
				if (unverifiedPasscode == null) {
					((TextView) findViewById(R.id.top_message)).setText(R.string.passcode_re_enter_passcode);
					unverifiedPasscode = passLock;
					mErrorMsg.setVisibility(View.GONE);
				} else {
					if (passLock.equals(unverifiedPasscode)) {
						setResult(RESULT_OK);
						LockManager.getInstance().getAppLock().setPasscode(passLock);
						mErrorMsg.setVisibility(View.GONE);
						finish();
					} else {
						unverifiedPasscode = null;
						topMessage.setText(R.string.passcode_enter_passcode);
						mErrorMsg.setVisibility(View.VISIBLE);
						mErrorMsg.setText(R.string.twice_input_not_same_input_again);
						// showPasscodeError();
						runShake();
					}
				}
				break;

			case AppLock.CHANGE_PASSWORD:
				// verify old passcode
				if (LockManager.getInstance().getAppLock().checkPasscode(passLock)) {
					topMessage.setText(R.string.passcode_enter_passcode);
					type = AppLock.ENABLE_PASSLOCK;
					mErrorMsg.setVisibility(View.GONE);
				} else {
					// showPasscodeError();
					mErrorMsg.setVisibility(View.VISIBLE);
					mErrorMsg.setText(R.string.passcode_wrong_passcode);
					runShake();
				}
				break;

			case AppLock.UNLOCK_PASSWORD:

				if (LockManager.getInstance().getAppLock().checkPasscode(passLock)) {
					setResult(RESULT_OK);
					AppData.setAppLockTime(0);
					finish();
				} else {
					getErrorTime();
					runShake();
				}
				break;

			default:
				break;
		}
	}

	private void initBroadcast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.APP_LOCK_UNBIND_DEVICE);
		Broadcaster.registerReceiver(receiver, ift);
	}

	// private void unbindDevice() {
	// AppData.setAppLockTime(0);
	// PushManager.stopWork(this);
	// ConnectBuilder.unregisterNotificationId(AppData.getRegisterId());
	// AppData.clear();
	// App.setUserInfo(null);
	// ConnectBuilder.clear();
	// LockManager.getInstance().getAppLock().setPasscode(null);
	// Broadcaster.sendBroadcast(new Intent(Consts.STOP_EVENT_SERVICE));
	//
	// // finish();
	// App.finish();
	// UIManager.instance().startActivity(SplashActivity.class, null);
	// // restarApp(SplashActivity.class);
	// }

	// public static void restarApp(Class<?> clazz) {
	// Context context = App.getAppContext();
	// Intent intent = new Intent(context, clazz);
	// int pendingIntentId = 198964;
	// PendingIntent pendingIntent = PendingIntent.getActivity(context,
	// pendingIntentId, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	// AlarmManager am = (AlarmManager) context
	// .getSystemService(Context.ALARM_SERVICE);
	// am.set(AlarmManager.RTC, System.currentTimeMillis() + 500,
	// pendingIntent);
	// System.exit(0);
	// }

	private void getErrorTime() {
		mErrorMsg.setVisibility(View.VISIBLE);
		int time = AppData.getAppLockTime();
		time++;
		mErrorMsg.setText(getString(R.string.error_pwd_input_again, time));

		if (time >= 10) {
			ConnectBuilder.unbindDevice(Consts.APP_LOCK_UNBIND_DEVICE);
		} else {
			AppData.setAppLockTime(time);
		}
	}

	@Override
	public void onBackPressed() {
		if (type == AppLock.UNLOCK_PASSWORD) {
			Intent intent = new Intent();
			intent.setAction(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			this.startActivity(intent);
			finish();
		} else {
			finish();
		}
	}

	protected void setupPinItem(EditText item) {
		// item.setInputType(InputType.NULL);
		item.setFilters(filters);
		item.setOnTouchListener(otl);
		item.setTransformationMethod(PasswordTransformationMethod.getInstance());
		item.addTextChangedListener(watcher);
		item.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
					onDelKey();
					return true;
				}
				return false;
			}
		});
	}

	private void requestNext() {
		String passcode = "" + pinCodeField1.getText().toString() + pinCodeField2.getText().toString() + pinCodeField3.getText().toString() + pinCodeField4.getText();

		if (passcode.length() == 0) {
			pinCodeField1.setEnabled(true);
			pinCodeField1.requestFocus();
			pinCodeField2.setEnabled(false);
			pinCodeField3.setEnabled(false);
			pinCodeField4.setEnabled(false);
		} else if (passcode.length() == 1) {
			pinCodeField2.setEnabled(true);
			pinCodeField2.requestFocus();
			pinCodeField1.setEnabled(false);
			pinCodeField3.setEnabled(false);
			pinCodeField4.setEnabled(false);
		} else if (passcode.length() == 2) {
			pinCodeField3.setEnabled(true);
			pinCodeField3.requestFocus();
			pinCodeField1.setEnabled(false);
			pinCodeField2.setEnabled(false);
			pinCodeField4.setEnabled(false);
		} else if (passcode.length() == 3) {
			pinCodeField4.setEnabled(true);
			pinCodeField4.requestFocus();
			pinCodeField1.setEnabled(false);
			pinCodeField2.setEnabled(false);
			pinCodeField3.setEnabled(false);
		} else if (passcode.length() == 4) {
			onPinLockInserted();
		}
	}

	private InputFilter onlyNumber = new InputFilter() {
		@Override
		public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {

			if (source.length() > 1) return "";

			if (source.length() == 0) // erase
			return null;

			try {
				int number = Integer.parseInt(source.toString());
				if ((number >= 0) && (number <= 9)) return String.valueOf(number);
				else return "";
			} catch (NumberFormatException e) {
				return "";
			}
		}
	};

	@Override
	public void finish() {
		super.finish();
		Utils.hideIME(pinCodeField1);
	}

	private OnTouchListener otl = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			v.performClick();

			if (v instanceof EditText) {
				((EditText) v).setText("");
			}
			return false;
		}
	};

	private void onDelKey() {
		if (pinCodeField1.isFocused()) {
			pinCodeField1.setText("");
		} else if (pinCodeField2.isFocused()) {
			pinCodeField1.requestFocus();
			pinCodeField1.setText("");
		} else if (pinCodeField3.isFocused()) {
			pinCodeField2.requestFocus();
			pinCodeField2.setText("");
		} else if (pinCodeField4.isFocused()) {
			pinCodeField3.requestFocus();
			pinCodeField3.setText("");
		}
	}

	protected void runShake() {
		Thread shake = new Thread() {
			public void run() {
				Animation shake = AnimationUtils.loadAnimation(AppLockActivity.this, R.anim.shake);
				findViewById(R.id.AppUnlockLinearLayout1).startAnimation(shake);
				// showPasscodeError();
				pinCodeField1.setText("");
				pinCodeField2.setText("");
				pinCodeField3.setText("");
				pinCodeField4.setText("");
				pinCodeField1.requestFocus();
			}
		};
		runOnUiThread(shake);
	}

	@Override
	public void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		super.onDestroy();
	}

}
