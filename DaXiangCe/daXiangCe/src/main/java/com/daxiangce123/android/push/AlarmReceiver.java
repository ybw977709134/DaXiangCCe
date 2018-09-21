package com.daxiangce123.android.push;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class AlarmReceiver extends BroadcastReceiver {

	private Context context;
	private NotificationManager mManager;
	private Notification mNotification;
	private static final String ACTION_BOOT = "android.intent.action.BOOT_COMPLETED";

	public static final String TAG = "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		this.context = context;
		if (App.DEBUG) {
			LogUtil.d(TAG, "xxxxxxxxxxxxxxxxxxxx");
		}
		initNotifiManager();
		String content = null;
		String title = context.getString(R.string.app_name);
		long currentTime = System.currentTimeMillis();
		long quitTiem = AppData.getQuitTime();
		long intervalTime = currentTime - quitTiem;
		if (App.DEBUG) {
			LogUtil.d(TAG, "--onReceive -- intent " + intent);
			LogUtil.d(TAG, "--onReceive -- action " + intent.getAction());
		}
		if (intent.getAction().equals(Consts.NOT_REGISTER)) {
			content = context.getString(R.string.one_day_not_register_push);
			if (Utils.isEmpty(AppData.getToken())) {
				showNotification(title, content, 0);
			}
		} else if (intent.getAction().equals(Consts.ONE_DAY_NOT_LAUNCH)) {
			content = context.getString(R.string.one_day_not_launch_push);
			if (24 * 60 * 60 * 1000 <= intervalTime && intervalTime < 48 * 60 * 60 * 1000) {
				showNotification(title, content, 1);
			}
		} else if (intent.getAction().equals(Consts.TWO_DAYS_NOT_LAUNCH)) {
			content = context.getString(R.string.two_days_not_launch_push);
			if (48 * 60 * 60 * 1000 <= intervalTime && intervalTime < 72 * 60 * 60 * 1000) {
				showNotification(title, content, 1);
			}
		} else if (intent.getAction().equals(Consts.THREE_DAYS_NOT_LAUNCH)) {
			content = context.getString(R.string.three_days_not_launch_push);
			if (72 * 60 * 60 * 1000 <= intervalTime) {
				showNotification(title, content, 1);
			}
		}
		// else if (intent.getAction().equals(ACTION_BOOT)) {
		// SharedPreferences sp = context.getSharedPreferences("application",
		// Context.MODE_PRIVATE);
		// quitTiem = sp.getLong(Consts.QUIT_APP_TIME, 0);
		// intervalTime = currentTime - quitTiem;
		// if (quitTiem == 0) {
		// InitAlarm.initAlarm();
		// } else {
		//
		// if (24 * 60 * 60 * 1000 <= intervalTime
		// && intervalTime < 48 * 60 * 60 * 1000) {
		// content = context
		// .getString(R.string.two_days_not_launch_push);
		// } else if (48 * 60 * 60 * 1000 <= intervalTime
		// && intervalTime < 72 * 60 * 60 * 1000) {
		// content = context
		// .getString(R.string.two_days_not_launch_push);
		// } else if (72 * 60 * 60 * 1000 <= intervalTime) {
		// content = context
		// .getString(R.string.two_days_not_launch_push);
		// }
		// showNotification(title, content, 1);
		//
		// }
		// }
	}

	private void initNotifiManager() {
		mManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		int icon = R.drawable.ic_launcher;
		mNotification = new Notification();
		mNotification.icon = icon;
		mNotification.tickerText = context.getString(R.string.app_name);
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		mNotification.flags = Notification.FLAG_AUTO_CANCEL;
	}

	@SuppressWarnings("deprecation")
	private void showNotification(String title, String content, int notificationId) {
		mNotification.when = System.currentTimeMillis();
		Intent start = new Intent(context, HomeActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, notificationId, start, Intent.FLAG_ACTIVITY_NEW_TASK);
		mNotification.setLatestEventInfo(context, title, content, pendingIntent);
		mManager.notify(notificationId, mNotification);
		// String title = Utils.getString(R.string.app_name);
		// String content =
		// Utils.getString(R.string.twenty_four_hours_not_register_push);
		// int notificationId = -1;
		// NotifyManager.instance().showNotification(title, content,
		// notificationId, start, true);
	}
}
