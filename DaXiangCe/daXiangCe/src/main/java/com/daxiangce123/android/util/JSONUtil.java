package com.daxiangce123.android.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

public class JSONUtil {
	public static final String TAG = "JSONUtil";

	public static boolean isJSON(String value) {
		// long startTime = System.currentTimeMillis();
		if (Utils.isEmpty(value)) {
			return false;
		}

		try {
			JSON.parse(value);
		} catch (Exception e) {
			// no need to print stack trace!
			// e.printStackTrace();
			return false;
		}
		// long endTime = System.currentTimeMillis();
		// YLog.d(TAG, "parse JSON object elapsed " + (endTime - startTime));
		return true;
	}

	public static JSON parseJSON(String value) {
		if (Utils.isEmpty(value)) {
			return null;
		}

		JSON jo = null;
		// long startTime = System.currentTimeMillis();
		try {
			jo = (JSON) JSON.parse(value);
		} catch (Exception e) {
		}
		// long endTime = System.currentTimeMillis();
		// YLog.i(TAG, "parse JSON object elapsed " + (endTime - startTime));
		return jo;
	}

	public static boolean isJSONObject(String value) {
		// long startTime = System.currentTimeMillis();
		if (Utils.isEmpty(value)) {
			return false;
		}

		try {
			JSON.parseObject(value);
		} catch (Exception e) {
			// no need to print stack trace!
			// e.printStackTrace();
			return false;
		}
		// long endTime = System.currentTimeMillis();
		// YLog.d(TAG, "parse JSON object elapsed " + (endTime - startTime));
		return true;
	}

	public static JSONObject parseObject(String value) {
		if (Utils.isEmpty(value)) {
			return null;
		}

		JSONObject jo = null;
		// long startTime = System.currentTimeMillis();
		try {
			jo = JSON.parseObject(value);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		// long endTime = System.currentTimeMillis();
		// YLog.i(TAG, "parse JSON object elapsed " + (endTime - startTime));
		return jo;
	}

	public static boolean isJSONArray(String value) {
		if (Utils.isEmpty(value)) {
			return false;
		}

		try {
			JSON.parseArray(value);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	public static JSONArray parseArray(String value) {
		if (Utils.isEmpty(value)) {
			return null;
		}

		// long startTime = System.currentTimeMillis();
		JSONArray ja = null;
		try {
			ja = JSON.parseArray(value);
		} catch (Exception e) {
			// e.printStackTrace();
		}
		// long endTime = System.currentTimeMillis();
		// YLog.i(TAG, "parse JSON array elapsed " + (endTime - startTime));
		return ja;
	}

	public static boolean isEmpty(JSONObject jo) {
		if (jo == null || jo.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isEmpty(JSONArray ja) {
		if (ja == null || ja.isEmpty()) {
			return true;
		} else {
			return false;
		}
	}
}
