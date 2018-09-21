package com.daxiangce123.android.helper;

import java.lang.Thread.UncaughtExceptionHandler;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;

public class CrashHelper implements UncaughtExceptionHandler {
	public final String TAG = "CrashHelper";

	private Thread.UncaughtExceptionHandler mDefaultHandler;

	private static CrashHelper INSTANCE;

	public static CrashHelper sharedInstance() {
		if (INSTANCE == null) {
			INSTANCE = new CrashHelper();
		}
		return INSTANCE;
	}

	private CrashHelper() {
	}

	public void init() {
		mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(this);
		boolean crashed = isCrash();
		if (crashed) {
			setCrash(false);
		}
	}

	@Override
	public void uncaughtException(Thread thread, Throwable ex) {
		// first we handle the exception.
		handleException(ex);

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// then call the system exception handler.
		if (mDefaultHandler != null) {
			mDefaultHandler.uncaughtException(thread, ex);
		}
	}

	private boolean handleException(Throwable ex) {
		if (ex == null) {
			return false;
		}

		String exStr = ex.toString();
		StackTraceElement[] stack = ex.getStackTrace();

		LogUtil.e(TAG, exStr);
		for (int i = 0; i < stack.length; i++) {
			LogUtil.e(TAG, "		at " + stack[i].toString());
		}
		Throwable origin = ex;
		while (true) {
			Throwable cause = origin.getCause();
			if (cause != null) {
				LogUtil.e(TAG, "caused by:" + cause.toString());
				StackTraceElement[] causeStack = cause.getStackTrace();
				for (int i = 0; i < causeStack.length; i++) {
					LogUtil.e(TAG, "		at " + causeStack[i].toString());
				}
				origin = cause;
			} else {
				break;
			}
		}
		LogUtil.flushLog();
		setCrash(true);
		return true;
	}

	public void setCrash(boolean crash) {
		App.getAppPrefs().edit().putBoolean(TAG, crash).commit();
	}

	public boolean isCrash() {
		try {
			return App.getAppPrefs().getBoolean(TAG, false);
		} catch (Exception e) {
		}
		return false;
	}
//    public final static void displayDeviceInfo() {
//        LogUtil.i(TAG, "***** MODEL				" + MODEL);
//        LogUtil.i(TAG, "***** DEVICE			" + DEVICE);
//        LogUtil.i(TAG, "***** BRAND				" + BRAND);
//        LogUtil.i(TAG, "***** PRODUCT			" + PRODUCT);
//        LogUtil.i(TAG, "***** DISPLAY			" + DISPLAY);
//        LogUtil.i(TAG, "***** MANUFACTURER		" + MANUFACTURER);
//
//        LogUtil.i(TAG, "***** SCREEN_WIDTH		" + SCREEN_WIDTH);
//        LogUtil.i(TAG, "***** SCREEN_HEIGHT		" + SCREEN_HEIGHT);
//        LogUtil.i(TAG, "***** SCREEN_DENSITY	" + SCREEN_DENSITY);
//
//        LogUtil.i(TAG, "***** BUILD_NUMBER		" + BUILD_NUMBER);
//        LogUtil.i(TAG, "***** VERSION			" + VERSION);
//    }
//
//    public static void onCrash() {
//        displayDeviceInfo();
//        closeClearAll();
//    }
}
