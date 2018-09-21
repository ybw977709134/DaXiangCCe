package com.daxiangce123.android.data;

import java.util.ArrayList;


/**
 * @project DaXiangCe
 * @time 2014-6-1
 * @author
 */
public class HotAlumList {
	// private int limit;
	// private boolean hasMore;
	private ArrayList<AlbumEntity> list;

	public HotAlumList( ArrayList<AlbumEntity> list) {
		super();
//		this.limit = limit;
//		this.hasMore = hasMore;
		this.list = list;
	}

//	public int getLimit() {
//		return limit;
//	}

	// public void setLimit(int limit) {
	// this.limit = limit;
	// }
	//
	// public boolean isHasMore() {
	// return hasMore;
	// }
	//
	// public void setHasMore(boolean hasMore) {
	// this.hasMore = hasMore;
	// }

	public ArrayList<AlbumEntity> getList() {
		return list;
	}

	public void setList(ArrayList<AlbumEntity> list) {
		this.list = list;
	}

	public void add(AlbumEntity album) {
		if (this.list == null) {
			list = new ArrayList<AlbumEntity>();
		}
		list.add(album);
	}

	public void add(ArrayList<AlbumEntity> list) {
		if (this.list == null) {
			this.list = list;
		} else {
			this.list.addAll(list);
		}
	}

	public boolean clear() {
		if (list != null) {
			list.clear();
		}
		return true;
	}

	public boolean remove(AlbumEntity album) {
		if (list == null) {
			return false;
		}
		return list.remove(album);
	}

	// public boolean remove(String albumId) {
	// if (Utils.isEmpty(albumId)) {
	// return false;
	// }
	// if (list == null) {
	// return false;
	// }
	// for (HotAlumList albumEntity : list) {
	// if (albumEntity == null) {
	// continue;
	// }
	// AlbumEntity albumEntity = list.;
	// if (albumEntity.getId().equals(albumId)) {
	// return list.remove(nearbyAlbum);
	// }
	// }
	// return false;
	// }

}
