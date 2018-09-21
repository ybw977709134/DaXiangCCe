package com.daxiangce123.android.manager;

public class RequestManager extends ConnectManager {
	public static final String TAG = "RequestManager";

	private static RequestManager mInstance = null;

	public static RequestManager sharedInstance() {
		if (mInstance == null) {
			mInstance = new RequestManager();
		}
		return mInstance;
	}

	private RequestManager() {

	}

	protected ConnectRunner createRunner() {
		return new RequestRunner();
	}

	public void destroy() {
		stop();
		mInstance = null;
	}

	@Override
	protected int initPoolSize() {
		return 4;
	}

	class RequestRunner extends ConnectRunner {

		public RequestRunner() {

		}

		public void doConnect() {

		}
	}
}
