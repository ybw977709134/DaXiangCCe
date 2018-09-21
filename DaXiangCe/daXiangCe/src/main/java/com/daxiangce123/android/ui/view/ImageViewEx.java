package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.data.ImageKey;
import com.daxiangce123.android.listener.ImageEx;
import com.daxiangce123.android.listener.OnLoadListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.LogUtil;

/**
 * @author ram
 * @project DaXiangCe
 * @time May 23, 2014
 */
public class ImageViewEx extends ImageView implements ImageEx {

    private ImageKey imageKey;
    private boolean isDeattached;
    /**
     * {@link Deprecated}
     */
    private OnLoadListener loadListener;
    private Bitmap defBitmap;

    public ImageViewEx(Context context) {
        this(context, null);
    }

    public ImageViewEx(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    /**
     * {@link Deprecated}
     */
    public void setLoadListener(OnLoadListener loadListener) {
        this.loadListener = loadListener;
    }

    private void initView() {

    }

    public Bitmap getDefBitmap() {
        return defBitmap;
    }

    public void setDefBitmap(Bitmap defBitmap) {
        this.defBitmap = defBitmap;
    }

    @Override
    public void setImageDrawable(Drawable drawable) {
//		if (drawable == null) {
//			
//			setImageBitmap(defBitmap);
//			return;
//		}
        super.setImageDrawable(drawable);
    }

    @Override
    public void setImageBitmap(Bitmap bm) {
//		if (bm == null) {
//			super.setImageBitmap(defBitmap);
//			return;
//		}
        super.setImageBitmap(bm);
    }

    @Override
    public void setImageKey(ImageKey imageTag) {
        this.imageKey = imageTag;
    }

    @Override
    public ImageKey getImageKey() {
        return imageKey;
    }

    @Override
    public void onLoad(Bitmap bitmap) {
        if (getParent() == null) {
            return;
        }
        setImageBitmap(bitmap);
        if (loadListener != null) {
            loadListener.onLoad(bitmap, this);
        }
    }

    @Override
    public void onLoad(Bitmap bitmap, ImageKey imagekey) {
        if (this.imageKey == null || imagekey == null) {
            return;
        }
        if (this.imageKey.equals(imagekey)) {
            onLoad(bitmap);
        }
    }

    @Override
    public boolean isShowen() {
        if (isDeattached) {
            return false;
        }
        if (getVisibility() != VISIBLE) {
            return false;
        }
        return true;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isDeattached = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDeattached = false;
        cancelLoad();
    }

    @Override
    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == INVISIBLE || visibility == GONE) {
            cancelLoad();
        }
    }

    private void cancelLoad() {
        ImageManager.instance().cancel(imageKey);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        try {
            super.onDraw(canvas);
        } catch (Exception e) {
            if (App.DEBUG) {
                LogUtil.d("ImageViewEx",
                        "ImageViewEx -> onDraw() Canvas: trying to use a recycled bitmap");
            }

        }
    }

}
