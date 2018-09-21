package com.daxiangce123.android.data;

/**
 * @project DaXiangCe
 * @time 2014-6-1
 * @author
 */
public class NearbyAlbum {

	private float distance;
	private AlbumEntity album;

	public NearbyAlbum() {
		super();
	}

	public NearbyAlbum(float distance, AlbumEntity album) {
		super();
		this.distance = distance;
		this.album = album;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public AlbumEntity getAlbum() {
		return album;
	}

	public void setAlbum(AlbumEntity album) {
		this.album = album;
	}

}
