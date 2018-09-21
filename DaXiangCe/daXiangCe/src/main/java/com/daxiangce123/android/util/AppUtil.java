package com.daxiangce123.android.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import com.daxiangce123.android.App;

public class AppUtil {
	public static final String TAG = "AppUtil";

	public static long getMaxMemory() {
		Runtime runtime = Runtime.getRuntime();
		long maxMemory = runtime.maxMemory();
		if (App.DEBUG) {
			LogUtil.d(TAG, "application max memory " + maxMemory);
		}
		return maxMemory;
	}

	public static final void showInputMethod(Context context) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,
				InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static final void hideInputMethod(Context context, View v) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	public static final int getScreenWidth(Context context) {
		Resources resources = context.getResources();
		DisplayMetrics dm = resources.getDisplayMetrics();
		return dm.widthPixels;
	}

	public static final int getOrientation(Context context) {
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		Display display = manager.getDefaultDisplay();
		int rotation = display.getRotation();
		if (rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180) {
			return ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
		} else {
			return ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
		}
	}

	public static final boolean startActivity(Intent intent, boolean finish) {
		Context context = App.getActivity();

		if (context == null || intent == null) {
			return false;
		}

		try {
			context.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		if (!finish) {
			return true;
		}

		if (context instanceof Activity) {
			Activity activity = (Activity) context;
			activity.finish();
			return true;
		}
		return false;
	}

	public static final boolean sendSMS(String text) {
		if (Utils.isEmpty(text)) {
			return false;
		}
		Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
		intent.putExtra("sms_body", text);
		return startActivity(intent, false);
	}

	public static String getString(int resId) {
		Context context = App.getActivity();
		if (context == null) {
			return null;
		}
		return context.getString(resId);
	}

	public static final String getString(int resId, Object... formatArgs) {
		Context context = App.getActivity();
		if (context == null) {
			return null;
		}
		return context.getString(resId, formatArgs);
	}

	public static final boolean isMainThread() {
		return Looper.getMainLooper().getThread() == Thread.currentThread();
	}

	@SuppressWarnings("unchecked")
	public static <T> T getMetaData(Context context, String metaName, T defValue) {
		try {
			ApplicationInfo appInfo = context.getPackageManager()
					.getApplicationInfo(context.getPackageName(),
							PackageManager.GET_META_DATA);
			Bundle bundle = appInfo.metaData;
			if (bundle == null) {
				return null;
			}
			Object obj = null;
			if (defValue instanceof String) {
				obj = bundle.getString(metaName);
			} else if (defValue instanceof Long) {
				obj = Long.valueOf(bundle.getLong(metaName));
			} else if (defValue instanceof Integer) {
				obj = Integer.valueOf(bundle.getInt(metaName));
			} else if (defValue instanceof Float) {
				obj = Float.valueOf(bundle.getFloat(metaName));
			} else if (defValue instanceof Boolean) {
				obj = Boolean.valueOf(bundle.getBoolean(metaName));
			} else if (defValue instanceof Double) {
				obj = Double.valueOf(bundle.getDouble(metaName));
			}
			if (obj != null) {
				return (T) obj;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return defValue;
	}
}
