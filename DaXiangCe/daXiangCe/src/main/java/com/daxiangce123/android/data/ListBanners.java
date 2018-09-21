package com.daxiangce123.android.data;

import java.util.LinkedList;

public class ListBanners {
	private LinkedList<Banner> banners;

	public ListBanners(LinkedList<Banner> banners) {
		this.banners = banners;
	}

	public void setBanner(LinkedList<Banner> banners) {
		this.banners = banners;
	}

	public LinkedList<Banner> getBanner() {
		return banners;
	}
}
