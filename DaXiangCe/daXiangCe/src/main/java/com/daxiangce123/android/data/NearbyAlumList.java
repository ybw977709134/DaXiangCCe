package com.daxiangce123.android.data;

import java.util.ArrayList;

import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time 2014-6-1
 * @author
 */
public class NearbyAlumList {
	private int limit;
	private boolean hasMore;
	private ArrayList<NearbyAlbum> list;

	public NearbyAlumList(int limit, boolean hasMore,
			ArrayList<NearbyAlbum> list) {
		super();
		this.limit = limit;
		this.hasMore = hasMore;
		this.list = list;
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

	public ArrayList<NearbyAlbum> getList() {
		return list;
	}

	public void setList(ArrayList<NearbyAlbum> list) {
		this.list = list;
	}

	public void add(NearbyAlbum album) {
		if (this.list == null) {
			list = new ArrayList<NearbyAlbum>();
		}
		list.add(album);
	}

	public void add(ArrayList<NearbyAlbum> list) {
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

	public boolean remove(NearbyAlbum album) {
		if (list == null) {
			return false;
		}
		return list.remove(album);
	}

	public boolean remove(String albumId) {
		if (Utils.isEmpty(albumId)) {
			return false;
		}
		if (list == null) {
			return false;
		}
		for (NearbyAlbum nearbyAlbum : list) {
			if (nearbyAlbum.getAlbum() == null) {
				continue;
			}
			AlbumEntity albumEntity = nearbyAlbum.getAlbum();
			if (albumEntity.getId().equals(albumId)) {
				return list.remove(nearbyAlbum);
			}
		}
		return false;
	}

}
