package com.daxiangce123.android.listener;

import com.daxiangce123.android.data.ImageKey;

import android.graphics.Bitmap;

/**
 * @project DaXiangCe
 * @time May 23, 2014
 * @author ram
 */
public interface ImageEx {
	public void setImageKey(ImageKey imagekey);

	public ImageKey getImageKey();

	public void onLoad(Bitmap bitmap, ImageKey imagekey);

	public void onLoad(Bitmap bitmap);

	/**
	 * If the view is in Screen that you can see it
	 */
	public boolean isShowen();

}
