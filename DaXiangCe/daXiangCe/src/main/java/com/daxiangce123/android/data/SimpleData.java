package com.daxiangce123.android.data;

public interface SimpleData {
	public void putString(String key, String value);

	public void putInt(String key, int value);

	public void putBoolean(String key, boolean value);

	public void putLong(String key, long value);

	public String getString(String key, String defValue);

	public boolean getBoolean(String key, boolean defValue);

	public int getInt(String key, int defValue);

	public long getLong(String key, long defValue);

	public void remove(String key);

	public void clear();

}
