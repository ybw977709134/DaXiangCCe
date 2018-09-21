package com.daxiangce123.android.data;

import java.util.Collection;
import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.util.Utils;

/**
 * TODO havent been {@link Parcelable}
 * 
 * @project Groubum
 * @time Feb 28, 2014
 * @author ram
 */
public class ListAllAlbums implements Parcelable {

	private int limit;
	private boolean hasMore;
	private LinkedList<AlbumEntity> albums;

	public ListAllAlbums(int limit, boolean hasMore,
			LinkedList<AlbumEntity> albums) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.albums = albums;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public void setAlbums(LinkedList<AlbumEntity> albums) {
		this.albums = albums;
	}

	public int getLimit() {
		return limit;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public LinkedList<AlbumEntity> getAlbums() {
		return albums;
	}

	public boolean remove(String albumId) {
		if (Utils.isEmpty(albumId)) {
			return false;
		}
		if (albums == null) {
			return false;
		}
		for (AlbumEntity albumEntity : albums) {
			if (albumId.equals(albumEntity.getId())) {
				return albums.remove(albumEntity);
			}
		}
		return false;
	}

	public boolean remove(AlbumEntity album) {
		if (album == null || albums == null) {
			return false;
		}
		return albums.remove(album);
	}

	public boolean add(Collection<AlbumEntity> collect) {
		if (albums == null) {
			albums = new LinkedList<AlbumEntity>();
		}
		return albums.addAll(collect);
	}

	public void clear() {
		if (albums == null) {
			return;
		}
		albums.clear();
	}

	@Override
	public String toString() {
		return "hasMore:" + hasMore + " limit:" + limit + " albums:" + albums;
	}

	// ////////////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
	}

	public final static Creator<ListAllAlbums> CREATOR = new Creator<ListAllAlbums>() {

		@Override
		public ListAllAlbums createFromParcel(Parcel source) {
			return null;
		}

		@Override
		public ListAllAlbums[] newArray(int size) {
			return null;
		}

	};
}
