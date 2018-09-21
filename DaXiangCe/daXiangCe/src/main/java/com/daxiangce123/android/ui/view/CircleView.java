package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;

public class CircleView extends ImageView {
	public CircleView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CircleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CircleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void setImageResource(int resId) {
		// super.setImageResource(resId);
		int numColumns = 3;
		int padding = App.SCREEN_WIDTH / 15;
		int paddings = 2 * padding;
		int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings)
				/ numColumns;
		
		Bitmap bitmap = getCircleImage(singleWidth, resId);
		setImageBitmap(bitmap);
	}

	private Bitmap getCircleImage(int imageSize, int imageRes) {
		Bitmap image = Bitmap.createBitmap(imageSize, imageSize,
				Bitmap.Config.ARGB_8888);

		int center = imageSize / 2;
		int innerCircle = center - center / 16;

		Canvas canvas = new Canvas(image);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		paint.setColor(0xff9d9da2);
		canvas.drawCircle(center, center, center, paint);

		paint.setColor(getResources().getColor(R.color.white));
		canvas.drawCircle(center, center, innerCircle, paint);

		Bitmap res = BitmapFactory.decodeResource(getResources(), imageRes);
		int resWidth = res.getWidth();
		int resHeight = res.getHeight();

		Rect rect = new Rect();
		int iconWidth = (int) (imageSize / 3);
		int iconHeight = (int) (iconWidth * resHeight / resWidth);
		int left = (int) (center - iconWidth / 2);
		int top = (int) (center - iconHeight / 2);
		int right = (int) (center + iconWidth / 2);
		int bottom = (int) (center + iconHeight / 2);
		rect.set(left, top, right, bottom);
		canvas.drawBitmap(res, null, rect, null);

		canvas.save();
		return image;
	}

}
