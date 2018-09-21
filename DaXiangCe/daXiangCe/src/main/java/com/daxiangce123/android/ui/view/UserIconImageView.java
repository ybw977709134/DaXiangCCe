package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;

public class UserIconImageView extends ImageView {
	private int radiusDips = 6;
	private int boarderColor = Color.WHITE;

	public UserIconImageView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public UserIconImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public UserIconImageView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setImageResource(int resId) {
		// super.setImageResource(resId);
//		Bitmap bitmap = BitmapUtil.toRoundBitmap(BitmapFactory.decodeResource(getResources(), resId));
		Bitmap bitmap = toRoundBitmap(
				BitmapFactory.decodeResource(getResources(), resId),
				boarderColor, radiusDips, this.getContext());
		setImageBitmap(bitmap);
	}

	public Bitmap toRoundBitmap(Bitmap bitmap, int color, int borderDips,
			Context context) {

		final int borderSizePx = (int) TypedValue.applyDimension(
				TypedValue.COMPLEX_UNIT_DIP, (float) borderDips, context
						.getResources().getDisplayMetrics());

		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int size = width > height ? height : width;
		int center = (int) (size / 2);
		int left = (int) ((width - size) / 2);
		int top = (int) ((height - size) / 2);
		int right = left + size;
		int bottom = top + size;

		Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
		Canvas canvas = new Canvas(output);

		final Paint paint = new Paint();
		final Rect src = new Rect(left, top, right, bottom);
		final Rect dst = new Rect(0, 0, size, size);

		canvas.drawARGB(0, 0, 0, 0);

		// draw border
		paint.setColor(Color.WHITE);
		paint.setAntiAlias(true);
		int radius = center - borderSizePx;
		canvas.drawCircle(center, center, radius, paint);

		// draw bitmap
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
		canvas.drawBitmap(bitmap, src, dst, paint);

		paint.reset();
		paint.setColor(color);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeWidth((float) borderSizePx);
		radius = center - borderSizePx / 2;
		canvas.drawCircle(center, center, radius, paint);

		canvas.save();

		return output;
	}

}
