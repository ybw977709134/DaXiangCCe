package com.daxiangce123.android.parser;

import java.lang.reflect.Type;

import com.daxiangce123.android.util.Utils;
import com.google.gson.Gson;

/**
 * @project yunioGE
 * @time 2014-6-30
 * @author
 */
public class GsonParser {

	public final static String TAG = "Parser";
	private static Gson mGson;

	private final static boolean valid(String jsonStr) {
		return !Utils.isEmpty(jsonStr);
	}

	public final static <T> T parser(String jsonStr, Class<T> classOfT) {
		if (!valid(jsonStr)) {
			return null;
		}
		Gson mGson = getGson();
		try {
			return mGson.fromJson(jsonStr, classOfT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final static <T> T parser(String jsonStr, Type typeOfT) {
		if (!valid(jsonStr)) {
			return null;
		}
		Gson mGson = getGson();
		try {
			return mGson.fromJson(jsonStr, typeOfT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public final static Gson getGson() {
		if (mGson == null) {
			mGson = new Gson();
		}
		return mGson;
	}

}
