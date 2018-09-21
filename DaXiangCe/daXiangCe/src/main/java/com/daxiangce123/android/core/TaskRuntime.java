package com.daxiangce123.android.core;

public class TaskRuntime extends BaseRunner {

	private static TaskRuntime instance;

	public final static TaskRuntime instance() {
		if (instance == null) {
			instance = new TaskRuntime();
		}
		return instance;
	}

	private TaskRuntime() {
		init();
	}

	@Override
	public int getCorePoolSize() {
		return 8;
	}

}
