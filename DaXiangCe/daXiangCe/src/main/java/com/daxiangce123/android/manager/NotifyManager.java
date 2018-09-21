package com.daxiangce123.android.manager;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.NotificationBuilder;
import com.daxiangce123.android.util.Utils;

/**
 * default will just light on
 * 
 * @author ram
 * @project DaXiangCe
 * @time Jul 12, 2014
 * 
 */
public class NotifyManager {

	private static NotifyManager instance;
	private NotificationManager notificationManager;

	private NotifyManager() {
		notificationManager = (NotificationManager) App.getAppContext().getSystemService(Context.NOTIFICATION_SERVICE);
	}

	public static NotifyManager instance() {
		if (instance == null) {
			instance = new NotifyManager();
		}
		return instance;
	}

	public final void showNotification(CharSequence title, CharSequence content, int notifyID, Intent intent, boolean needSound) {
		if (Utils.isEmpty(title) && Utils.isEmpty(content)) {
			return;
		}
		NotificationBuilder nb = new NotificationBuilder(App.getAppContext()).setContent(content).setTitle(title).setIntent(intent).setNotificationId(notifyID).setSound(needSound);
		showNotification(nb);
	}

	public final void showNotification(NotificationBuilder nb) {
		if (nb == null) {
			return;
		}
		notificationManager.notify(nb.getNotificationId(), nb.build());
	}

	public final void hide() {
		notificationManager.cancelAll();
	}

	public final void hide(int notifyID) {
		notificationManager.cancel(notifyID);
	}

}
