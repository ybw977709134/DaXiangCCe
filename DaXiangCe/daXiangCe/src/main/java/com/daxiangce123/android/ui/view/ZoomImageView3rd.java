package com.daxiangce123.android.ui.view;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;

/**
 * @deprecated
 */
public class ZoomImageView3rd extends ImageViewTouch {

	private Bitmap bitmap;
	private boolean zoomable;

	public ZoomImageView3rd(Context context) {
		this(context, null);
	}

	public ZoomImageView3rd(Context context, AttributeSet attrs) {
		super(context, attrs);
		setDisplayType(DisplayType.FIT_TO_SCREEN);
	}

	@Override
	public void setImageBitmap(Bitmap bitmap) {
		super.setImageBitmap(bitmap);
		this.bitmap = bitmap;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}

	public boolean isZoomable() {
		return zoomable;
	}

	public void setZoomable(boolean zoomable) {
		this.zoomable = zoomable;
	}

}
