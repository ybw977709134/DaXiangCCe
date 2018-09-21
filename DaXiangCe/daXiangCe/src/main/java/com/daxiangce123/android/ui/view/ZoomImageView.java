package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.data.ImageKey;
import com.daxiangce123.android.listener.ImageEx;
import com.daxiangce123.android.util.LogUtil;

public class ZoomImageView extends ImageView implements ImageEx {

	private enum STATUS {
		INIT, //
		ZOOM_OUT, //
		ZOOM_IN, //
		MOVE
	}

	protected final String TAG = "ZoomImageView";
	private STATUS currentStatus;
	private int width;
	private int height;
	private float scaledWidth;
	private float scaledHeight;
	private float centerPointX;
	private float centerPointY;
	private float currentBitmapWidth;
	private float currentBitmapHeight;
	private float lastXMove = -1;
	private float lastYMove = -1;
	/**
	 * moved distance to the last point
	 */
	private float movedDistanceX;
	/**
	 * moved distance to the last point
	 */
	private float movedDistanceY;
	/**
	 * total moved distance to the x axis in negative
	 */
	private float totalTranslateX;
	/**
	 * total moved distance to the y axis in negative
	 */
	private float totalTranslateY;
	private float totalRatio;
	private float scaledRatio;
	private float initRatio;
	private double lastFingerDis;
	private Matrix matrix = new Matrix();
	private Bitmap sourceBitmap;
	private boolean zoomable = true;
	protected final int MIN_SIZE_TO_FULL = 500;
	protected final float DEF_ZOOM_RATIO = 2.5f;
	private boolean DEBUG = true;
	private ImageKey imageKey;
	private boolean isDeattached;

	public ZoomImageView(Context context) {
		this(context, null);
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		currentStatus = STATUS.INIT;
	}

	public void setZoomable(boolean zoomable) {
		this.zoomable = zoomable;
	}

	public void setImageBitmap(Bitmap bitmap) {
		// super.setImageBitmap(bitmap);
		if (DEBUG) {
			if (bitmap != null) {
				if (sourceBitmap == bitmap) {
					LogUtil.d(TAG, "setImageBitmap() BITMAP IS SAME");
				} else {
					LogUtil.d(TAG, "setImageBitmap() " + (bitmap.getWidth() + "x" + bitmap.getHeight()));
				}
			}
		}
		if (sourceBitmap == bitmap) {
			return;
		}
		sourceBitmap = bitmap;
		currentStatus = STATUS.INIT;
		invalidate();
	}

	public Bitmap getBitmap() {
		return sourceBitmap;
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (changed) {
			width = getWidth();
			height = getHeight();
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (!zoomable) {
			return super.onTouchEvent(event);
		}
		if (initRatio == totalRatio || scaledWidth <= getWidth()) {
			getParent().requestDisallowInterceptTouchEvent(false);
		} else {
			getParent().requestDisallowInterceptTouchEvent(true);
		}
		switch (event.getActionMasked()) {
			case MotionEvent.ACTION_POINTER_DOWN:
				if (event.getPointerCount() == 2) {
					lastFingerDis = distanceBetweenFingers(event);
				}
				break;
			case MotionEvent.ACTION_CANCEL:
				return super.onTouchEvent(event);
			case MotionEvent.ACTION_MOVE:
				if (event.getPointerCount() == 1) {
					currentStatus = STATUS.MOVE;
					final float xMove = event.getX();
					final float yMove = event.getY();

					if (lastXMove == -1 && lastYMove == -1) {
						lastXMove = xMove;
						lastYMove = yMove;
					}

					movedDistanceX = xMove - lastXMove;
					movedDistanceY = yMove - lastYMove;

					lastXMove = xMove;
					lastYMove = yMove;

					final float movedX = movedDistanceX;
					boolean hasMoveX = true;
					if (totalTranslateX + movedDistanceX > 0) {
						movedDistanceX = 0;
						hasMoveX = false;
					} else if (width - (totalTranslateX + movedDistanceX) > currentBitmapWidth) {
						movedDistanceX = 0;
						hasMoveX = false;
					}
					if (totalTranslateY + movedDistanceY > 0) {
						movedDistanceY = 0;
					} else if (height - (totalTranslateY + movedDistanceY) > currentBitmapHeight) {
						movedDistanceY = 0;
					}

					boolean isX = Math.abs(movedDistanceX) >= Math.abs(movedDistanceY);
					if (meetBolderX(movedX) && isX) {// hasMoveX
						LogUtil.d(TAG, "hasMoveX	" + hasMoveX);
						getParent().requestDisallowInterceptTouchEvent(false);
						lastXMove = -1;
						lastYMove = -1;
						return super.onTouchEvent(event);
					}
					invalidate();
				} else if (event.getPointerCount() == 2) {
					centerPointBetweenFingers(event);
					double fingerDis = distanceBetweenFingers(event);
					if (fingerDis > lastFingerDis) {
						currentStatus = STATUS.ZOOM_OUT;
					} else {
						currentStatus = STATUS.ZOOM_IN;
					}
					// 进行缩放倍数检查，最大只允许将图片放大4倍，最小可以缩小到初始化比例
					if ((currentStatus == STATUS.ZOOM_OUT && totalRatio < 4 * initRatio) || (currentStatus == STATUS.ZOOM_IN && totalRatio > initRatio)) {
						scaledRatio = (float) (fingerDis / lastFingerDis);
						totalRatio = totalRatio * scaledRatio;
						if (totalRatio > 4 * initRatio) {
							totalRatio = 4 * initRatio;
						} else if (totalRatio < initRatio) {
							totalRatio = initRatio;
						}
						invalidate();
						lastFingerDis = fingerDis;
					}
				}
			case MotionEvent.ACTION_POINTER_UP:
				if (event.getPointerCount() == 2) {
					lastXMove = -1;
					lastYMove = -1;
				}
				break;
			case MotionEvent.ACTION_UP:
				lastXMove = -1;
				lastYMove = -1;
				break;
			default:
				break;
		}
		// if (hasMove) {
		// hasMove = false;
		// return true;
		// }
		return true;
		// return super.onTouchEvent(event);
	}

	private boolean meetBolderX(float movedX) {// float movedX
		final float deltaWidth = scaledWidth - getWidth();
		if (DEBUG) {
			StringBuilder builder = new StringBuilder();
			builder.append("---------------------------ACTION_MOVE");
			builder.append("	total:" + (int) totalTranslateX);
			builder.append("	moved:" + (int) movedX);
			builder.append("	sWidth:" + (int) currentBitmapWidth + "/" + (int) scaledWidth + "/" + getWidth() + " ->" + (int) (deltaWidth + totalTranslateX));
			LogUtil.d(TAG, builder.toString());
		}

		int offset = 15;
		// from left to right
		if (movedX > 0 && Math.abs(totalTranslateX) <= offset) {
			return true;
		}
		// from right to left
		if (movedX < 0 && Math.abs(totalTranslateX + deltaWidth) <= offset) {
			return true;
		}
		return false;
	}

	@Override
	protected void onDraw(Canvas canvas) {
		// super.onDraw(canvas);

		if (currentStatus == STATUS.INIT) {
			initBitmap(canvas);
		} else if (currentStatus == STATUS.MOVE) {
			move(canvas);
		} else if (currentStatus == STATUS.ZOOM_OUT || currentStatus == STATUS.ZOOM_IN) {
			zoom(canvas);
		} else {
			if (sourceBitmap != null) {
				canvas.drawBitmap(sourceBitmap, matrix, null);
			} else {
				clear(canvas);
			}
		}
	}

	private void zoom(Canvas canvas) {
		if (sourceBitmap == null) {
			return;
		}
		matrix.reset();
		// 将图片按总缩放比例进行缩放
		matrix.postScale(totalRatio, totalRatio);
		scaledWidth = sourceBitmap.getWidth() * totalRatio;
		scaledHeight = sourceBitmap.getHeight() * totalRatio;
		float translateX = 0f;
		float translateY = 0f;
		// 如果当前图片宽度小于屏幕宽度，则按屏幕中心的横坐标进行水平缩放。否则按两指的中心点的横坐标进行水平缩放
		if (currentBitmapWidth < width) {
			translateX = (width - scaledWidth) / 2f;
		} else {
			translateX = totalTranslateX * scaledRatio + centerPointX * (1 - scaledRatio);
			// 进行边界检查，保证图片缩放后在水平方向上不会偏移出屏幕
			if (translateX > 0) {
				translateX = 0;
			} else if (width - translateX > scaledWidth) {
				translateX = width - scaledWidth;
			}
		}
		// 如果当前图片高度小于屏幕高度，则按屏幕中心的纵坐标进行垂直缩放。否则按两指的中心点的纵坐标进行垂直缩放
		if (currentBitmapHeight < height) {
			translateY = (height - scaledHeight) / 2f;
		} else {
			translateY = totalTranslateY * scaledRatio + centerPointY * (1 - scaledRatio);
			// 进行边界检查，保证图片缩放后在垂直方向上不会偏移出屏幕
			if (translateY > 0) {
				translateY = 0;
			} else if (height - translateY > scaledHeight) {
				translateY = height - scaledHeight;
			}
		}
		// 缩放后对图片进行偏移，以保证缩放后中心点位置不变
		matrix.postTranslate(translateX, translateY);
		totalTranslateX = translateX;
		totalTranslateY = translateY;
		currentBitmapWidth = scaledWidth;
		currentBitmapHeight = scaledHeight;
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	private void move(Canvas canvas) {
		if (sourceBitmap == null) {
			return;
		}
		matrix.reset();
		// 根据手指移动的距离计算出总偏移值
		float translateX = totalTranslateX + movedDistanceX;
		float translateY = totalTranslateY + movedDistanceY;
		// 先按照已有的缩放比例对图片进行缩放
		matrix.postScale(totalRatio, totalRatio);
		// 再根据移动距离进行偏移
		matrix.postTranslate(translateX, translateY);
		totalTranslateX = translateX;
		totalTranslateY = translateY;
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	private void initBitmap(Canvas canvas) {
		scaledWidth = 0;
		scaledHeight = 0;
		centerPointX = 0;
		centerPointY = 0;
		currentBitmapWidth = 0;
		currentBitmapHeight = 0;
		lastXMove = -1;
		lastYMove = -1;
		movedDistanceX = 0;
		movedDistanceY = 0;
		totalTranslateX = 0;
		totalTranslateY = 0;
		totalRatio = 1;
		scaledRatio = 1;
		initRatio = 1;
		lastFingerDis = 0;
		if (sourceBitmap == null) {
			clear(canvas);
			return;
		}
		matrix.reset();
		int bitmapWidth = sourceBitmap.getWidth();
		int bitmapHeight = sourceBitmap.getHeight();
		if (bitmapWidth > width || bitmapHeight > height) {
			if (bitmapWidth - width > bitmapHeight - height) {
				// 当图片宽度大于屏幕宽度时，将图片等比例压缩，使它可以完全显示出来
				float ratio = width / (bitmapWidth * 1.0f);
				matrix.postScale(ratio, ratio);
				float translateY = (height - (bitmapHeight * ratio)) / 2f;
				// 在纵坐标方向上进行偏移，以保证图片居中显示
				matrix.postTranslate(0, translateY);
				totalTranslateY = translateY;
				totalRatio = initRatio = ratio;
			} else {
				// 当图片高度大于屏幕高度时，将图片等比例压缩，使它可以完全显示出来
				float ratio = height / (bitmapHeight * 1.0f);
				matrix.postScale(ratio, ratio);
				float translateX = (width - (bitmapWidth * ratio)) / 2f;
				// 在横坐标方向上进行偏移，以保证图片居中显示
				matrix.postTranslate(translateX, 0);
				totalTranslateX = translateX;
				totalRatio = initRatio = ratio;
			}
			currentBitmapWidth = bitmapWidth * initRatio;
			currentBitmapHeight = bitmapHeight * initRatio;
		} else {
			float ratio = 1.0f;
			float ratioW = width / (bitmapWidth * 1.0f);
			float ratioH = height / (bitmapHeight * 1.0f);
			ratio = Math.min(ratioW, ratioH);
			// if (bitmapWidth >= MIN_SIZE_TO_FULL
			// || bitmapHeight >= MIN_SIZE_TO_FULL) {
			// } else {
			// // zoom to double size
			// float ratioW = width / (bitmapWidth * 1.0f);
			// float ratioH = height / (bitmapHeight * 1.0f);
			// ratioW = ratioW > DEF_ZOOM_RATIO ? DEF_ZOOM_RATIO : ratioW;
			// ratioH = ratioH > DEF_ZOOM_RATIO ? DEF_ZOOM_RATIO : ratioH;
			// ratio = Math.min(ratioW, ratioH);
			// }
			matrix.postScale(ratio, ratio);
			float translateX = (width - (bitmapWidth * ratio)) / 2f;
			float translateY = (height - (bitmapHeight * ratio)) / 2f;
			matrix.postTranslate(translateX, translateY);
			totalTranslateX = translateX;
			totalTranslateY = translateY;
			totalRatio = initRatio = ratio;
			currentBitmapWidth = bitmapWidth;
			currentBitmapHeight = bitmapHeight;
		}
		canvas.drawBitmap(sourceBitmap, matrix, null);
	}

	/**
	 * @param event
	 * @return distance between fingers
	 */
	private double distanceBetweenFingers(MotionEvent event) {
		float disX = Math.abs(event.getX(0) - event.getX(1));
		float disY = Math.abs(event.getY(0) - event.getY(1));
		return Math.sqrt(disX * disX + disY * disY);
	}

	/**
	 * calculate center point between fingers
	 * 
	 * @param event
	 */
	private void centerPointBetweenFingers(MotionEvent event) {
		float xPoint0 = event.getX(0);
		float yPoint0 = event.getY(0);
		float xPoint1 = event.getX(1);
		float yPoint1 = event.getY(1);
		centerPointX = (xPoint0 + xPoint1) / 2;
		centerPointY = (yPoint0 + yPoint1) / 2;
	}

	private void clear(Canvas canvas) {
		canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
		if (DEBUG) {
			LogUtil.d(TAG, "clear() bitmap=" + (sourceBitmap != null) + " status=" + currentStatus);
		}
	}

	@Override
	public void setImageKey(ImageKey key) {
		this.imageKey = key;
	}

	@Override
	public ImageKey getImageKey() {
		return imageKey;
	}

	@Override
	public void onLoad(Bitmap bitmap) {
		setImageBitmap(sourceBitmap);
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
	}

	public void resetImage() {
		currentStatus = STATUS.INIT;
		invalidate();
	}
}
