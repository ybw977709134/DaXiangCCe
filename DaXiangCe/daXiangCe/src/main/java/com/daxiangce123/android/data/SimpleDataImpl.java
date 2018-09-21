package com.daxiangce123.android.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.daxiangce123.android.App;

public class SimpleDataImpl implements SimpleData {

	public static final String TAG = "SimpleDataImpl";

	private SharedPreferences mData;

	public SimpleDataImpl(String name) {
		mData = App.getAppContext().getSharedPreferences(name, Context.MODE_PRIVATE);
	}

	public void putString(String key, String value) {
		Editor editor = mData.edit();
		editor.putString(key, value);
		editor.commit();
	}

	public void putInt(String key, int value) {
		Editor editor = mData.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	public void putBoolean(String key, boolean value) {
		Editor editor = mData.edit();
		editor.putBoolean(key, value);
		editor.commit();
	}

	public void putLong(String key, long value) {
		Editor editor = mData.edit();
		editor.putLong(key, value);
		editor.commit();
	}

	public String getString(String key, String defValue) {
		return mData.getString(key, defValue);
	}

	public boolean getBoolean(String key, boolean defValue) {
		return mData.getBoolean(key, defValue);
	}

	public int getInt(String key, int defValue) {
		return mData.getInt(key, defValue);
	}

	public long getLong(String key, long defValue) {
		return mData.getLong(key, defValue);
	}

	public void remove(String key) {
		Editor editor = mData.edit();
		editor.remove(key);
		editor.commit();

	}

	public void clear() {
		Editor editor = mData.edit();
		editor.clear();
		editor.commit();
	}
}
