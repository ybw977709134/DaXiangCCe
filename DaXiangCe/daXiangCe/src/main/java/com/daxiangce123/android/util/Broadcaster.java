package com.daxiangce123.android.util;

import java.util.LinkedList;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;

import com.daxiangce123.android.App;

public class Broadcaster {
	public static final String TAG = "Broadcaster";

	private static LocalBroadcastManager mManager;
	private static LinkedList<BroadcastReceiver> mReceivers;
	private static boolean DEBUG = true;

	public static void start() {
		mReceivers = new LinkedList<BroadcastReceiver>();
		mManager = LocalBroadcastManager.getInstance(App.getAppContext());
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public static void registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
		if (receiver == null || filter == null || mManager == null || mReceivers == null) {
			if (DEBUG) {
				LogUtil.e(TAG, "invalid parameters");
			}
			return;
		}

		synchronized (mManager) {
			if (mReceivers.contains(receiver)) {
				if (DEBUG) {
					LogUtil.e(TAG, "unregister old receiver!");
				}
				mManager.unregisterReceiver(receiver);
			}

			mReceivers.addLast(receiver);
			mManager.registerReceiver(receiver, filter);
		}

		if (DEBUG) {
			LogUtil.d(TAG, "registerReceiver " + receiver + "	" + System.currentTimeMillis());
		}
	}

	public static void unregisterReceiver(BroadcastReceiver receiver) {
		if (receiver == null || mManager == null || mReceivers == null) {
			if (DEBUG) {
				LogUtil.e(TAG, "invalid parameters!");
			}
			return;
		}

		if (!mReceivers.contains(receiver)) {
			return;
		}

		synchronized (mManager) {
			mReceivers.remove(receiver);
			mManager.unregisterReceiver(receiver);
		}
	}

	public static void sendBroadcast(String action) {
		if (action == null) {
			return;
		}
		mManager.sendBroadcast(new Intent(action));
	}

	public static void sendBroadcast(Intent intent) {
		if (intent == null) {
			return;
		}
		String action = intent.getAction();
		if (DEBUG) {
			LogUtil.d(TAG, "sendBroadcast " + action + "   " + System.currentTimeMillis());
		}
		mManager.sendBroadcast(intent);
	}

	public static void debug() {
		if (DEBUG) {
			LogUtil.d(TAG, "********** Broadcaster Debug **********");
			LogUtil.d(TAG, "mReceivers size " + mReceivers.size());
		}
	}
}
