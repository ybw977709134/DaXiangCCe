package com.daxiangce123.android.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.daxiangce123.android.App;

public class UIManager {

	private static UIManager uiManager;

	private UIManager() {
	}

	public final static UIManager instance() {
		if (uiManager == null) {
			uiManager = new UIManager();
		}
		return uiManager;
	}

	public final void startActivity(Class<?> to) {
		startActivity(to, null, null);
	}

	public final void startActivity(Class<?> to, Bundle bundle) {
		startActivity(to, bundle, null);
	}

	/**
	 * 
	 * @time Mar 7, 2014
	 * 
	 * @param destClass
	 *            class of which activity to start
	 * @param bundle
	 *            Extra data to pass to the dest {@link Activity}
	 * @param anims
	 *            length must be 2. anims[0] is enterAnimationId, anims[1] is
	 *            exitAnimationId. default is null
	 */
	public final void startActivity(Class<?> destClass, Bundle bundle,
			int[] anims) {
		Intent intent = new Intent();
		Context context = App.getAppContext();
		if (App.getActivity() != null) {
			context = App.getActivity();
			if (anims != null && anims.length != 2) {
				App.getActivity().overridePendingTransition(anims[0], anims[1]);
			}
		} else {
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		}
		if (context == null) {
			return;
		}
		if (bundle != null) {
			intent.putExtras(bundle);
		}
		intent.setClass(context, destClass);
		context.startActivity(intent);
	}

}
