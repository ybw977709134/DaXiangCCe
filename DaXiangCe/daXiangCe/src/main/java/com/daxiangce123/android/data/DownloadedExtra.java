package com.daxiangce123.android.data;

public class DownloadedExtra {
	public String token;
	public long fileSize;
	public String localPath;
	public String params;

	public DownloadedExtra(String token, long fileSize, String localPath, String params) {
		super();
		this.token = token;
		this.fileSize = fileSize;
		this.localPath = localPath;
		this.params = params;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

}
