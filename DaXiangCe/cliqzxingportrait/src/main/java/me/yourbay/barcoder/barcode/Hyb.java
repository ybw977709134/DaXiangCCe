package me.yourbay.barcoder.barcode;

import android.graphics.Rect;
import android.util.Log;

public class Hyb {

	public final static int MESSAGE_ID_RESTART_PREVIEW = 10;
	public final static int MESSAGE_ID_DECODE_SUCCEEDED = 11;
	public final static int MESSAGE_ID_DECODE_FAILED = 12;
	public final static int MESSAGE_ID_RETURN_SCAN_RESULT = 13;
	public final static int MESSAGE_ID_LAUNCH_PRODUCT_QUERY = 14;
	public final static int MESSAGE_ID_QUIT = 15;
	public final static int MESSAGE_ID_DECODE = 16;

	public final static void log(Class<?> cls, String msg) {
		log(cls.getName() + "--->  " + msg);
	}

	public final static void log(Rect rect, String... s) {
		Hyb.log(s[0] + "--->  " + "rect:(" + rect.left + "," + rect.top + ","
				+ rect.right + "," + rect.bottom + ");width="
				+ (rect.right - rect.left) + ";height="
				+ (rect.bottom - rect.top));
	}

	public final static void log(String msg) {
		Log.d("zxing", "-------------------> " + msg);
	}
}
