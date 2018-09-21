package com.daxiangce123.android.ui.view;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

import com.daxiangce123.R;

public class RotateImage extends ImageView {

	private static Bitmap defaultBitmap;
	private Bitmap bitmap;
	private Bitmap finalBitmap;

	private int boarderDips = 12;
	private int radiusDips = 6;
	private int rotateAngle = -12;
	private int boarderColor = Color.GRAY;

	public RotateImage(Context context) {
		this(context, null);
	}

	public RotateImage(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public RotateImage(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		if (defaultBitmap == null) {
			defaultBitmap = BitmapFactory.decodeResource(
					context.getResources(), R.drawable.ic_launcher);
		}
	}

	public void setImage(String absPath) {

	}

	public void setImage(int resId) {

	}

	public void setImage(Bitmap bitmap) {
		if (bitmap == null) {
			return;
		}
		this.bitmap = bitmap;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		// Paint paint = new Paint();
		// paint.setAntiAlias(true);
		// paint.setStyle(Paint.Style.STROKE);
		// paint.setStrokeWidth(16);
		// paint.setColor(Color.RED);
		// int width = this.getWidth();
		// int height = this.getHeight();
		// int radius = ((width > width ? height : width) - 32) / 4;
		// canvas.drawCircle(radius + 16, radius + 16, radius, paint);

		if (finalBitmap == null) {
			genFinalBitmap();
			setImageBitmap(finalBitmap);
		}
	}

	private void genFinalBitmap() {
		if (bitmap == null) {
			bitmap = defaultBitmap;
		}
		Bitmap rounded = getRoundedCornerBitmap(bitmap, boarderColor,
				boarderDips, radiusDips, this.getContext());
		Bitmap rotated = getRotatedBitmap(rounded, rotateAngle);

		finalBitmap = getCompositedBitmap(rounded, rotated);
	}

	public static Bitmap getCompositedBitmap(Bitmap front, Bitmap back) {
		Canvas canvas = new Canvas(back);
		Paint paint = new Paint();

		int left = (back.getWidth() - front.getWidth()) / 2;
		int top = (back.getHeight() - front.getHeight()) / 2;

		canvas.drawBitmap(front, left, top, paint);
		canvas.save(Canvas.ALL_SAVE_FLAG);
		canvas.restore();
		return back;
	}

	public static Bitmap getRotatedBitmap(Bitmap bitmap, float angle) {
		Matrix matrix = new Matrix();
		matrix.postRotate(angle);
		return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
				bitmap.getHeight(), matrix, true);

	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int color,
			int cornerDips, int borderDips, Context context) {
		Bitmap output = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final int borderSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, (float) borderDips, context
						.getResources().getDisplayMetrics());
		final int cornerSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, (float) cornerDips, context
						.getResources().getDisplayMetrics());
		final Paint paint = new Paint();
		final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
		final RectF rectF = new RectF(rect);

		// prepare canvas for transfer
		paint.setAntiAlias(true);
		paint.setColor(0xFFFFFFFF);
		paint.setStyle(Paint.Style.FILL);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

		// draw bitmap
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, rect, rect, paint);

		// draw border
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth((float) borderSizePx);
		canvas.drawRoundRect(rectF, cornerSizePx, cornerSizePx, paint);

		return output;
	}
}
