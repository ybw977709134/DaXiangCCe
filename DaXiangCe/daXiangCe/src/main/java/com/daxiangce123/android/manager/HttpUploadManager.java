package com.daxiangce123.android.manager;

import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.manager.ConnectManager.ConnectRunner;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project Groubum
 * @time Mar 3, 2014
 * @author ram
 */
public class HttpUploadManager extends ConnectManager {

	protected final static String TAG = "HttpUploadManager";
	private static HttpUploadManager manager;

	private HttpUploadManager() {
	}

	public static HttpUploadManager instance() {
		if (manager == null) {
			manager = new HttpUploadManager();
		}
		return manager;
	}

	@Override
	public synchronized void addConnect(ConnectInfo info) {
		if (info == null) {
			return;
		}
		super.addConnect(info);
	}

	@Override
	protected int initPoolSize() {
		return 4;
	}

	@Override
	protected ConnectRunner createRunner() {
		return new UlConnector();
	}

	class UlConnector extends ConnectRunner {

		@Override
		public void onProgress(String localPath, int progress, long speed, long offset, long totalSize) {
			super.onProgress(localPath, progress, speed, offset, totalSize);
			LogUtil.d(TAG, "progress" + progress + "	offset=" + offset + "	speed=" + speed + "	totalSize=" + totalSize + "	localPath" + localPath);
		}

		@Override
		protected boolean needProgress() {
			return true;
		}

	}

	public synchronized void cancelUploadConnect(String fakeId) {
		int size = mConnectList.size();
		for (int index = size - 1; index >= 0; --index) {
			ConnectInfo info = mConnectList.get(index);
			if (!fakeId.equals(info.getFakeId())) {
				continue;
			}
			// remove transfer info
			mConnectList.remove(index);
			Runnable runner = info.getRunner();
			if (runner == null || !(runner instanceof ConnectRunner)) {
				continue;
			}
			// cancel transfer connection
			ConnectRunner tr = (ConnectRunner) runner;
			tr.cancel();
		}

		size = currentList.size();
		for (int index = size - 1; index >= 0; --index) {
			ConnectInfo info = currentList.get(index);
			if (!fakeId.equals(info.getFakeId())) {
				continue;
			}
			// remove transfer info
			currentList.remove(index);
			Runnable runner = info.getRunner();
			if (runner == null || !(runner instanceof ConnectRunner)) {
				continue;
			}
			// cancel transfer connection
			ConnectRunner tr = (ConnectRunner) runner;
			tr.cancel();
		}
	}
}
