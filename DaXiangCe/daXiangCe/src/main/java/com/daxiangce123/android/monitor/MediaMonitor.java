package com.daxiangce123.android.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.LogUtil;

public class MediaMonitor {
	public static final String TAG = "MediaMonitor";

	public static final String MEDIA_MOUNTED = Environment.MEDIA_MOUNTED;
	public static final String MEDIA_UNMOUNTED = Environment.MEDIA_UNMOUNTED;

	private static String mExternalStorageState = MEDIA_UNMOUNTED;

	private static BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			LogUtil.d(TAG, "receive broadcast " + action);
			if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
				updateMediaState();
			} else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
				updateMediaState();
			}
		}
	};

	public static void start() {
		IntentFilter mediaFilter = new IntentFilter();
		// filter for SD card actions
		mediaFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
		mediaFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
		mediaFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
		mediaFilter.addAction(Intent.ACTION_MEDIA_EJECT);
		mediaFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
		mediaFilter.addDataScheme(Consts.SCHEME_FILE);
		App.getAppContext().registerReceiver(mReceiver, mediaFilter);

		updateMediaState();
	}

	public static boolean mediaMounted() {
		return (mExternalStorageState.equals(MEDIA_MOUNTED));
	}

	public static void updateMediaState() {
		String state = Environment.getExternalStorageState();
		LogUtil.d(TAG, "external storage state " + state);
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			mExternalStorageState = MEDIA_MOUNTED;
		} else {
			mExternalStorageState = MEDIA_UNMOUNTED;
		}
	}
}
