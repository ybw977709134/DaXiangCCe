package com.daxiangce123.android.data;

import java.util.LinkedList;

public class Batches {
	private int limit;
	private boolean hasMore;
	private LinkedList<Batch> batches;

	public Batches() {

	}

	public Batches(int limit, boolean hasMore, LinkedList<Batch> batches) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.batches = batches;
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public boolean isHasMore() {
		return hasMore;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public LinkedList<Batch> getBatches() {
		return batches;
	}

	public void setBatches(LinkedList<Batch> batches) {
		this.batches = batches;
	}

}
