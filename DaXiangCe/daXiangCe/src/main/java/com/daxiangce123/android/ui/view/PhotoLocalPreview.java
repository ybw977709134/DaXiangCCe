package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.FileUtil;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.IOException;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @project DaXiangCe
 * @time Apr 16, 2014
 * @time Jul 16, 2014
 * @author ram
 * @author hansontian
 * 
 *         local version of {@link PhotoView} support UploadImage
 *
 */
public class PhotoLocalPreview extends FrameLayout {

	public final static String TAG = "PhotoPreview";
	private ImageView ivVideoOverlay;
	protected ZoomImageView zImageView;
	private GifImageView gifView;
	private Bitmap mBitmap;
	private float lastX, lastY;

	private OnClickListener clickListener;
	protected boolean isDeattached;
	protected String filePathString;
	private boolean DEBUG = true;
	protected ImageSize pictureSize;
	protected GifDrawable gifDrawable;

	protected ImageLoadingProgressListener progressListener = new ImageLoadingProgressListener() {

		@Override
		public void onProgressUpdate(String imageUri, View view, int current, int total) {

		}
	};

	private void initView() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_photo_local_preview, this, true);
		zImageView = (ZoomImageView) findViewById(R.id.ziv_photo_preview);
		ivVideoOverlay = (ImageView) findViewById(R.id.iv_video_overlay);
		gifView = (GifImageView) findViewById(R.id.iv_photo_preview);

		float zoomRatio = 0.8f;
		if (App.SCREEN_WIDTH <= 800) {
			zoomRatio = 1.0f;
		}
		pictureSize = new ImageSize((int) (App.SCREEN_WIDTH * zoomRatio), (int) (App.SCREEN_HEIGHT * zoomRatio));
		zImageView.getLayoutParams().height = App.SCREEN_HEIGHT;
		zImageView.getLayoutParams().width = App.SCREEN_WIDTH;
		zImageView.setVisibility(View.VISIBLE);

	}

	public PhotoLocalPreview(Context context) {
		this(context, null);
	}

	public PhotoLocalPreview(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		initView();
	}

	public void setImageBitmap(Bitmap bitmap) {
		zImageView.setImageBitmap(bitmap);
		mBitmap = bitmap;

	}

	public void setImageSize(ImageSize imageSize) {
		pictureSize = imageSize;
	}

	public void setFile(String filePath) {
		this.filePathString = filePath;
	}

	public boolean isVideo() {
		return FileUtil.isVideo(filePathString);
	}

	private void showGifView(boolean show) {
		if (show) {
			if (!visiable(gifView)) {
				gifView.setVisibility(View.VISIBLE);
			}
		} else {
			if (visiable(gifView)) {
				release();
			}
		}
	}

	public void releaseGif() {

	}

	public void release() {
		releaseGif();
		gifView.setImageBitmap(null);
		mBitmap = null;
		zImageView.setImageBitmap(null);
		// ivVideoOverlay.setImageBitmap(null);
	}

	public Bitmap showFile(boolean playGif) {
		if (filePathString == null) {
			return null;
		}
		releaseGif();

		if (FileUtil.isVideo(filePathString)) {
			return showVideoThumb();
		} else if (FileUtil.isGif(filePathString)) {
			return playGif(playGif);
		} else {
			return showImage();
		}
	}

	private Bitmap playGif(boolean playGif) {
		playGif = true;
		showVideoOverlay(false);
		setImageBitmap(null);
		showGifView(true);

		if (!FileUtil.isGif(filePathString)) {
			return null;
		}

		String localPath = filePathString;
		if (FileUtil.exists(localPath)) {
			try {
				if (gifDrawable != null) {
					gifDrawable.recycle();
					gifDrawable = null;
				}
				gifDrawable = new GifDrawable(new File(localPath));
				gifView.setImageDrawable(gifDrawable);

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private void showVideoOverlay(boolean visiable) {
		if (visiable) {
			ivVideoOverlay.setTag(filePathString);
			if (!visiable(ivVideoOverlay)) {
				ivVideoOverlay.setVisibility(VISIBLE);
			}
		} else {
			ivVideoOverlay.setTag(null);
			if (visiable(ivVideoOverlay)) {
				ivVideoOverlay.setVisibility(GONE);
			}
		}
	}

	private Bitmap showImage() {
		if (FileUtil.isImage(filePathString) || FileUtil.isVideo(filePathString)) {
			showVideoOverlay(false);
			showGifView(false);
			setImageBitmap(null);
			pictureSize.setThumb(false);
			loadImage();
		}
		return zImageView.getBitmap();
	}

	private Bitmap showVideoThumb() {

		if (!FileUtil.isVideo(filePathString)) {
			return null;
		}
		showVideoOverlay(true);
		showGifView(false);
		setImageBitmap(null);

		pictureSize.setThumb(true);
		loadImage();
		return zImageView.getBitmap();
	}

	protected void loadImage() {
		ImageManager.instance().loadLocal(zImageView, filePathString, null, null);
	}

	private boolean visiable(View v) {
		if (v == null) {
			return false;
		}
		int visiablity = v.getVisibility();
		if (visiablity == VISIBLE) {
			return true;
		}
		return false;
	}

	@Override
	public void setOnClickListener(OnClickListener l) {
		super.setOnClickListener(l);
		this.clickListener = l;
	}

	/**
	 * view's tag will be {@link FileEntity}
	 */
	public void setOnVideoClickListener(OnClickListener l) {
		ivVideoOverlay.setOnClickListener(l);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getPointerCount() == 1) {
			if (ev.getAction() == MotionEvent.ACTION_MOVE) {
				// TODO
			} else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
				lastX = ev.getX();
				lastY = ev.getY();
				// if (App.DEBUG) {
				// LogUtil.d(TAG, "	ACTION_DOWN	" + lastX + "/" + lastY);
				// }
			} else if (ev.getAction() == MotionEvent.ACTION_UP) {
				// if (App.DEBUG) {
				// LogUtil.d(TAG, "	ACTION_UP	" + lastX + "/" + lastY);
				// }
				if (lastX != -1 && lastY != -1) {
					float deltaX = Math.abs(ev.getX() - lastX);
					float deltaY = Math.abs(ev.getY() - lastY);
					// if (App.DEBUG) {
					// LogUtil.d(TAG, "	ACTION_UP	" + deltaX + "/" + deltaY);
					// }
					if (deltaX <= 10 && deltaY <= 10) {
						lastX = lastY = -1;

						if (clickListener != null) {
							clickListener.onClick(this);
							return true;
						}
					}
				}
			}
		}
		return super.dispatchTouchEvent(ev);
	}

	// private boolean resetEvent(MotionEvent ev) {
	// float currentX = ev.getX();
	// float currentY = ev.getY();
	//
	// float deltaX = Math.abs(currentX - lastX);
	// float deltaY = Math.abs(currentY - lastY);
	// if (deltaX <= 10 && deltaY <= 10) {
	// lastX = lastY = -1;
	// return true;
	// }
	// lastX = currentX;
	// lastY = currentY;
	// return false;
	// }

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		isDeattached = false;
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		isDeattached = true;
		release();
	}

}
