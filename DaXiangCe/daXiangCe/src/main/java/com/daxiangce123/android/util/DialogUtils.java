package com.daxiangce123.android.util;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.os.Build;

import com.daxiangce123.android.App;

/**
 * @project DaXiangCe
 * @time Jun 5, 2014
 * @author ram
 */
public class DialogUtils {

	@SuppressLint("NewApi")
	public final static Builder create() {
		AlertDialog.Builder builder = null;

		// try {
		// builder = new Builder(App.getActivity(),
		// AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		// } catch (NoSuchMethodError e) {
		// builder = new AlertDialog.Builder(App.getActivity());
		// }

		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			builder = new Builder(App.getActivity(),
					AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
		} else {
			builder = new Builder(App.getActivity());
		}
		return builder;
	}

	@SuppressLint("NewApi")
	public final static Builder create(int targetTheme) {
		AlertDialog.Builder builder = null;
		if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB) {
			builder = new Builder(App.getActivity(), targetTheme);
		} else {
			builder = new Builder(App.getActivity());
		}
		return builder;
	}

	public final static void dialog(CharSequence message) {
		create().setMessage(message)
				.setPositiveButton(android.R.string.yes, null).show();
	}

	public final static void dialog(int msgId) {
		create().setMessage(msgId)
				.setPositiveButton(android.R.string.yes, null).show();
	}

}
