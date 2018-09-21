package com.daxiangce123.android.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import android.os.Environment;
import android.util.Log;

/**
 * <pre>
 * Default path is hidden and it's:
 * 	{@link Environment#getExternalStorageDirectory() SDCARD}{@link File#separator /}.config-hacker{@link File#separator /}hacker.config
 * </pre>
 * 
 * @project HackConfig
 * @time Sep 15, 2014
 * @author ram
 */
public class ConfigHacker {

	public final static String TAG = "ConfigHacker";
	private final static int IO_BUFFER = 4 * 1024;
	private static ConfigHacker INSTANCE;
	private boolean isReadingConfig = false;
	private boolean DEBUG = true;

	private String configPath;
	private String commentStarter = "#";
	private String configKvDivider = "=";
	private String configItemDivider = "\n";
	private HackerListener hackerListener;
	private Map<String, String> configs;

	private ConfigHacker() {
		configs = new HashMap<String, String>();
	}

	public final static ConfigHacker instance() {
		if (INSTANCE == null) {
			INSTANCE = new ConfigHacker();
		}
		return INSTANCE;
	}

	public final ConfigHacker setDebuggable(boolean debug) {
		DEBUG = debug;
		return this;
	}

	/**
	 * The absolute path where the config file exists
	 */
	public final ConfigHacker setConfigPath(String absPath) {
		configPath = absPath;
		return this;
	}

	/**
	 * U Must set the call back
	 */
	public ConfigHacker setHackerListener(HackerListener hackerListener) {
		this.hackerListener = hackerListener;
		return this;
	}

	/**
	 * Throw {@link IllegalArgumentException} if {@link #hackerListener} is null
	 */
	public void read() {
		if (isReadingConfig) {
			if (DEBUG) {
				Log.d(TAG, "is reading config");
			}
			return;
		}
		if (hackerListener == null) {
			throw new IllegalArgumentException("U must set a callback by calling setHackerListener");
		}
		isReadingConfig = true;
		new Thread() {
			@Override
			public void run() {
				if (configPath == null) {
					configPath = generateDefaultPath();
				}
				HackerListener.Result result = process();
				hackerListener.OnReadConfig(result, configs);
				isReadingConfig = false;
				if (DEBUG) {
					Log.d(TAG, "read()	" + result + "	" + configs);
				}
			}
		}.start();
	}

	private HackerListener.Result process() {
		if (DEBUG) {
			Log.d(TAG, "process()	" + configPath);
		}
		if (!validPath(configPath)) {
			return HackerListener.Result.INVALIDPATH;
		}
		try {
			String configStr = readContentFromFile(configPath);
			if (DEBUG) {
				Log.d(TAG, "process()	Content is " + configStr);
			}
			readToConfig(configs, configStr);
		} catch (Exception e) {
			return HackerListener.Result.UNKOWN;
		}
		return HackerListener.Result.SUCCESS;
	}

	/**
	 * Parsing configs from file content, for example
	 * 
	 * <pre>
	 * KEY1{@link #configKvDivider}VALUD1{@link #configItemDivider}KEY2{@link #configKvDivider}VALUD2
	 * </pre>
	 */
	private void readToConfig(Map<String, String> map, String content) {
		String[] itemArray = content.split(configItemDivider);
		if (itemArray == null || itemArray.length < 1) {
			return;
		}
		for (String item : itemArray) {
			if (item == null || item.trim().startsWith(commentStarter)) {
				continue;
			}
			String[] itemKV = item.split(configKvDivider);
			if (itemKV == null || itemKV.length < 2) {
				continue;
			}
			int length = itemKV.length;
			String key = itemKV[0].trim();
			StringBuilder sbd = new StringBuilder();
			for (int i = 1; i < length; i++) {
				if (i > 1) {
					sbd.append(configKvDivider);
				}
				sbd.append(itemKV[i].trim());
			}
			configs.put(key, sbd.toString());
			if (DEBUG) {
				Log.d(TAG, "readToConfig[KV]:	" + key + "	" + sbd);
			}
		}
	}

	private boolean validPath(String absPath) {
		if (absPath == null) {
			return false;
		}
		File absFile = new File(absPath);
		if (!absFile.exists()) {
			if (DEBUG) {
				Log.d(TAG, "validPath()	does NOT exists");
			}
			return false;
		}
		if (absFile.isDirectory()) {
			if (DEBUG) {
				Log.d(TAG, "validPath()	isDirectory");
			}
			return false;
		}
		if (!absFile.canRead()) {
			if (DEBUG) {
				Log.d(TAG, "validPath()	can't Read");
			}
			return false;
		}
		return true;
	}

	private String readContentFromFile(String absPath) {
		String result = null;
		FileInputStream inputStream = null;
		ByteArrayOutputStream outputStream = null;
		String state = Environment.getExternalStorageState();
		if (state.equals(Environment.MEDIA_MOUNTED)) {
			File file = new File(absPath);
			int len = 0;
			byte[] data = new byte[IO_BUFFER];
			if (file.exists()) {
				try {
					outputStream = new ByteArrayOutputStream();
					inputStream = new FileInputStream(file);
					while ((len = inputStream.read(data)) != -1) {
						outputStream.write(data, 0, len);
					}
					result = new String(outputStream.toByteArray());
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (inputStream != null) {
					try {
						inputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				if (outputStream != null) {
					try {
						outputStream.close();
					} catch (Exception e2) {
						e2.printStackTrace();
					}
				}
			}
		}
		return result;
	}

	/**
	 * <pre>
	 * Default path is hidden and it's:
	 * {@link Environment#getExternalStorageDirectory() SDCARD}{@link File#separator /}.configHacker{@link File#separator /}hacker.config
	 * </pre>
	 */
	private String generateDefaultPath() {
		File SDCARD = Environment.getExternalStorageDirectory();
		if (SDCARD == null || !SDCARD.exists()) {
			return null;
		}
		File destDir = new File(SDCARD, ".config-hacker");
		if (!destDir.exists()) {
			destDir.mkdir();
		}
		return destDir.getAbsolutePath() + File.separator + "hacker.config";
	}

	public static interface HackerListener {
		public enum Result {
			SUCCESS, INVALIDPATH, UNKOWN;
		}

		public void OnReadConfig(Result result, Map<String, String> map);
	}

}
