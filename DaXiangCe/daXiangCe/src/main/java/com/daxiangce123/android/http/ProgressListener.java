package com.daxiangce123.android.http;

public interface ProgressListener {
	void onProgress(String localPath, int progress, long speed, long offset, long totalSize);
}
