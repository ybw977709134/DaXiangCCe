package com.daxiangce123.android.util;

import android.os.Handler;
import android.os.Message;

import com.daxiangce123.android.App;

/**
 * @project DaXiangCe
 * @time Jun 14, 2014
 * @author ram
 */
public class BaseHandler extends Handler {

	public final static String TAG = "BaseHanlder";
	private static boolean DEBUG = true;

	public BaseHandler() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public static interface HandleListener {
		/**
		 * @see Handler#handleMessage(Message)
		 */
		public void handleMessage(Message msg);
	}

	private HandleListener handleListener;

	public void setHandleListener(HandleListener handleListener) {
		this.handleListener = handleListener;
	}

	@Override
	public void handleMessage(Message msg) {
		if (handleListener != null) {
			handleListener.handleMessage(msg);
		}
	}

	public void clear() {
		removeCallbacksAndMessages(null);
	}

}
