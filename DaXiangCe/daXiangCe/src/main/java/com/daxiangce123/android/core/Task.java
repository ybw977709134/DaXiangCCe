package com.daxiangce123.android.core;

import android.os.Handler;
import android.os.Looper;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time Jun 12, 2014
 * @author ram
 */
public abstract class Task implements Runnable {

	protected final static String TAG = "Task";
	private static final Handler handler = new Handler(Looper.getMainLooper());
	private static boolean DEBUG = true;

	public Task() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public void runOnUI(Runnable runnable) {
		if (Utils.isMainThread()) {
			runnable.run();
		} else {
			handler.post(runnable);
		}

		if (DEBUG) {
			LogUtil.d(TAG, "runOnUI()	" + handler);
		}
	}

}
