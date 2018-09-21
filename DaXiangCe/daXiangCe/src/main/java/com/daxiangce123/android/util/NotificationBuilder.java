package com.daxiangce123.android.util;

import android.app.Application;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.daxiangce123.R;

/**
 * @project DaXiangCe
 * @time Sep 4, 2014
 * @author ram
 */
public class NotificationBuilder {

	private final static int DEFUALT_LEDARGB = 0xffffffff;
	private Notification notification;
	private boolean cancelable = true;
	private boolean isVibrate = false;
	private boolean isSound = false;
	private boolean isLight = false;
	private int notificationId;
	private CharSequence content;
	private CharSequence title;
	private Intent intent;
	private Context context;

	private int ledARGB = -1;

	/**
	 * using {@link Application#getApplicationContext()} for better perfermance
	 */
	public NotificationBuilder(Context context) {
		this.context = context;
		notification = new Notification();
	}

	public NotificationBuilder setSound(boolean isSound) {
		this.isSound = isSound;
		return this;
	}

	public NotificationBuilder setLight(boolean isLight) {
		this.isLight = isLight;
		return this;
	}

	public NotificationBuilder setVibrate(boolean isVibrate) {
		this.isVibrate = isVibrate;
		return this;
	}

	public NotificationBuilder setIntent(Intent intent) {
		this.intent = intent;
		return this;
	}

	public NotificationBuilder setContent(CharSequence content) {
		this.content = content;
		return this;
	}

	public NotificationBuilder setTitle(CharSequence title) {
		this.title = title;
		return this;
	}

	public NotificationBuilder setNotificationId(int notificationId) {
		this.notificationId = notificationId;
		return this;
	}

	public NotificationBuilder setLedARGB(int argb) {
		this.ledARGB = argb;
		return this;
	}

	public int getNotificationId() {
		return notificationId;
	}

	@SuppressWarnings("deprecation")
	public Notification build() {
		if (isSound) {
			/* add sound */
			notification.defaults |= Notification.DEFAULT_SOUND;
		}
		if (isVibrate) {
			/* add vibrate */
			notification.defaults |= Notification.DEFAULT_VIBRATE;
		}

		if (isLight) {
			/* add lights */
			notification.defaults |= Notification.DEFAULT_LIGHTS;
			if (ledARGB <= 0) {
				ledARGB = DEFUALT_LEDARGB;
			}
			notification.ledARGB = ledARGB;
			notification.ledOnMS = 1000;
			notification.ledOffMS = 1000;
		}

		if (cancelable) {
			notification.flags |= Notification.FLAG_AUTO_CANCEL;
		} else {
			notification.flags |= Notification.FLAG_NO_CLEAR;
		}

		notification.icon |= R.drawable.ic_launcher;

		PendingIntent contentIntent = PendingIntent.getActivity(context, notificationId, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		notification.setLatestEventInfo(context, title, content, contentIntent);
		return notification;
	}
}
