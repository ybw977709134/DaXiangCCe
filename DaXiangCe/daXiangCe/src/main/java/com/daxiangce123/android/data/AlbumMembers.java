package com.daxiangce123.android.data;

import java.util.LinkedList;

public class AlbumMembers {
	private int limit;
	private boolean hasMore;
	private LinkedList<MemberEntity> members;

	public AlbumMembers() {

	}

	public AlbumMembers(int limit, boolean hasMore,
			LinkedList<MemberEntity> members) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.members = members;
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

	public void setAlbums(LinkedList<MemberEntity> members) {
		this.members = members;
	}

	public LinkedList<MemberEntity> getMembers() {
		return members;
	}
}
