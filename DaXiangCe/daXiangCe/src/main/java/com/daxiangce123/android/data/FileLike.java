package com.daxiangce123.android.data;

import java.util.LinkedList;

public class FileLike {
	private int limit;
	private boolean hasMore;
	private LinkedList<LikeEntity> like;

	public FileLike() {

	}

	public FileLike(int limit, boolean hasMore, LinkedList<LikeEntity> like) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.like = like;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public void setLike(LinkedList<LikeEntity> like) {
		this.like = like;
	}

	public LinkedList<LikeEntity> getLike() {
		return like;
	}

}
