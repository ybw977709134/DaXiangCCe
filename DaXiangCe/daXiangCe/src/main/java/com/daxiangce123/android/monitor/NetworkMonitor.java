package com.daxiangce123.android.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;

public class NetworkMonitor {
	private static String TAG = "NetworkMonitor";

	private static final int NETWORK_NONE = -1;
	private static int mNetworkType = NETWORK_NONE;

	private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
				updateNetwork();
			}
		}
	};

	public static void start() {
		// filter for network state actions
		IntentFilter networkFilter = new IntentFilter();
		networkFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		App.getAppContext().registerReceiver(mReceiver, networkFilter);
		updateNetwork();
	}

	public static boolean networkConnected() {
		return (mNetworkType != NETWORK_NONE);
	}

	public static boolean mobileNetwork() {
		return (mNetworkType == ConnectivityManager.TYPE_MOBILE);
	}

	public static boolean wifiNetwork() {
		return (mNetworkType == ConnectivityManager.TYPE_WIFI);
	}

	public static void updateNetwork() {
		int oldType = mNetworkType;
		mNetworkType = NETWORK_NONE;

		do {
			Context context = App.getAppContext();
			ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			if (manager == null) {
				break;
			}

			NetworkInfo networkInfo = manager.getActiveNetworkInfo();
			if (networkInfo == null) {
				break;
			}

			int type = networkInfo.getType();
			boolean connected = networkInfo.isConnected();
			if (App.DEBUG) {
				LogUtil.i(TAG, "network [type] " + type + " [connected] " + connected);
			}
			if (connected) {
				mNetworkType = type;
			}
		} while (false);

		if (mNetworkType != oldType) {
			Intent intent = new Intent(Consts.NETWORK_STATE_CHANGED);
			Broadcaster.sendBroadcast(intent);
		}
	}
}
