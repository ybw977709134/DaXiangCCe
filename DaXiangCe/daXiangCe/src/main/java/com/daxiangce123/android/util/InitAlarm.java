package com.daxiangce123.android.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.push.AlarmReceiver;

public class InitAlarm {
	public static void initAlarm() {
		AlarmManager manager = (AlarmManager) App.getAppContext()
				.getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(App.getAppContext(), AlarmReceiver.class);
		long triggerAtMillis = System.currentTimeMillis() + 24 * 60 * 60 * 1000;
		intent.setAction(Consts.NOT_REGISTER);
		Intent intent1 = new Intent(App.getAppContext(), AlarmReceiver.class);
		long triggerAtMillis1 = System.currentTimeMillis() + 24 * 60 * 60
				* 1000;
		intent1.setAction(Consts.ONE_DAY_NOT_LAUNCH);
		Intent intent2 = new Intent(App.getAppContext(), AlarmReceiver.class);
		long triggerAtMillis2 = System.currentTimeMillis() + 48 * 60 * 60
				* 1000;
		intent2.setAction(Consts.TWO_DAYS_NOT_LAUNCH);
		Intent intent3 = new Intent(App.getAppContext(), AlarmReceiver.class);
		long triggerAtMillis3 = System.currentTimeMillis() + 72 * 60 * 60
				* 1000;
		intent3.setAction(Consts.THREE_DAYS_NOT_LAUNCH);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				App.getAppContext(), 0, intent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingIntent1 = PendingIntent.getBroadcast(
				App.getAppContext(), 1, intent1,
				PendingIntent.FLAG_UPDATE_CURRENT);

		PendingIntent pendingIntent2 = PendingIntent.getBroadcast(
				App.getAppContext(), 2, intent2,
				PendingIntent.FLAG_UPDATE_CURRENT);
		PendingIntent pendingIntent3 = PendingIntent.getBroadcast(
				App.getAppContext(), 3, intent3,
				PendingIntent.FLAG_UPDATE_CURRENT);

		manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis, pendingIntent);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis1, pendingIntent1);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis2, pendingIntent2);
		manager.set(AlarmManager.RTC_WAKEUP, triggerAtMillis3, pendingIntent3);

	}

}
