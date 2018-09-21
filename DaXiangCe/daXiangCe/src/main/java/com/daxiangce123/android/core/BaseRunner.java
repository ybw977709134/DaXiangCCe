package com.daxiangce123.android.core;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @project DaXiangCe
 * @time 2014-6-8
 * @author
 */
public abstract class BaseRunner {
	private ThreadPoolExecutor mExecutor = null;
	private int corePoolSize = 5;
	private boolean isRunning;

	private boolean invalid() {
		if (mExecutor == null) {
			return true;
		}

		if (mExecutor.isTerminated()) {
			return true;
		}

		if (mExecutor.isShutdown()) {
			return true;
		}

		return false;
	}

	protected ThreadPoolExecutor createExecutor() {
		if (!invalid()) {
			shutdown();
		}
		mExecutor = null;
		int nThreads = getCorePoolSize();
		return new ThreadPoolExecutor(nThreads, nThreads, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
	}

	protected void init() {
		mExecutor = createExecutor();
	}

	public boolean cancel(Runnable task) {
		if (task == null) {
			return false;
		}
		if (invalid()) {
			return false;
		}
		return mExecutor.remove(task);
	}

	public boolean run(Runnable task) {
		if (task == null) {
			return false;
		}
		if (invalid()) {
			mExecutor = createExecutor();
		}
		mExecutor.submit(task);
		return true;
	}

	public void shutdown() {
		mExecutor.shutdown();
		mExecutor = null;
		isRunning = false;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public boolean isRunning() {
		return isRunning;
	}

	public long getTaskCount() {
		return mExecutor.getTaskCount();
	}

	public void start() {
		if (isRunning()) {
			return;
		}
		init();
	}
}
