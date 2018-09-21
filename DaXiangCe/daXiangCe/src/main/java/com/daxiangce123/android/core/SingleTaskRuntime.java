package com.daxiangce123.android.core;

public class SingleTaskRuntime extends BaseRunner {

	private static SingleTaskRuntime instance;

	public final static SingleTaskRuntime instance() {
		if (instance == null) {
			instance = new SingleTaskRuntime();
		}
		return instance;
	}

	private SingleTaskRuntime() {
		init();
	}

	@Override
	public int getCorePoolSize() {
		return 1;
	}

}
