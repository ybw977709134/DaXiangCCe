package com.daxiangce123.android.manager;

/**
 * @project Groubum
 * @time Mar 3, 2014
 * @author ram
 */
public class HttpDownloadManager extends ConnectManager {

	protected final static String TAG = "FileDownloadManager";
	private static HttpDownloadManager manager;

	private HttpDownloadManager() {
		setMaxTaskLimit(20);
	}

	public static HttpDownloadManager instance() {
		if (manager == null) {
			manager = new HttpDownloadManager();
		}
		return manager;
	}

	@Override
	protected int initPoolSize() {
		return 4;
	}

	@Override
	protected ConnectRunner createRunner() {
		return new DlConnector();
	}

	class DlConnector extends ConnectRunner {

		@Override
		protected boolean needProgress() {
			return true;
		}

	}

}
