package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import com.daxiangce123.R;

public class DividerView extends FrameLayout {

	protected float bottomLineHeight = 1;
	protected float bottomMarginLeft = 0;
	protected int bottomLineColor = 0;

	protected float topLineHeight = 0;
	protected int topLineColor = -1;
	protected Paint paint;

	public DividerView(Context context) {
		this(context, null);
	}

	public DividerView(Context context, AttributeSet attrs) {
		super(context, attrs);

		TypedArray ta = context.obtainStyledAttributes(attrs,
				R.styleable.preference);

		int defaultLineColor = getResources().getColor(R.color.divider);
		bottomLineColor = defaultLineColor;
		topLineColor = defaultLineColor;

		try {
			if (ta.hasValue(R.styleable.preference_topLineColor)) {
				topLineColor = ta.getColor(R.styleable.preference_topLineColor,
						defaultLineColor);
			}
			if (ta.hasValue(R.styleable.preference_bottomLineColor)) {
				bottomLineColor = ta.getColor(
						R.styleable.preference_bottomLineColor,
						defaultLineColor);
			}
			if (ta.hasValue(R.styleable.preference_bottomLineHeight)) {
				bottomLineHeight = ta.getDimension(
						R.styleable.preference_bottomLineHeight, 1);
			}

			if (ta.hasValue(R.styleable.preference_topLineHeight)) {
				topLineHeight = ta.getDimension(
						R.styleable.preference_topLineHeight, 2);
			}
			if (ta.hasValue(R.styleable.preference_bottomMarginLeft)) {
				bottomMarginLeft = ta.getDimension(
						R.styleable.preference_bottomMarginLeft, 1);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		ta.recycle();
		initView();

	}

	private void initView() {
		// TODO Auto-generated method stub
		int defaultColor = getResources().getColor(R.color.divider);
		// int defaultColor = 0xff000000;
		bottomLineColor = defaultColor;
		topLineColor = defaultColor;
		/* ============paint============ */
		paint = new Paint();
		paint.setAntiAlias(true);
		paint.setStyle(Paint.Style.FILL);
		int color = getResources().getColor(R.color.divider);
		paint.setColor(color);
	}

	private void drawBottomLine(Canvas canvas) {
		if (bottomLineHeight <= 0 || bottomMarginLeft < 0) {
			return;
		}
		paint.setColor(bottomLineColor);
		paint.setStrokeWidth(bottomLineHeight);
		float startX = bottomMarginLeft;
		float startY = getHeight() - bottomLineHeight;
		float stopX = getWidth();
		float stopY = startY;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
	}

	private void drawTopLine(Canvas canvas) {
		if (topLineHeight <= 0) {
			return;
		}
		paint.setColor(topLineColor);
		paint.setStrokeWidth(topLineHeight);
		int startX = 0;
		int startY = 0;
		int stopX = getWidth();
		int stopY = startY;
		canvas.drawLine(startX, startY, stopX, stopY, paint);
	}

	/**
	 * if height <= 0 it will <b>NOT</b> draw top line.<br>
	 * <i><b>DEFAULT</b></i> is 0<br>
	 * 
	 * @see #setbottomLineHeight(int)
	 */
	public void setTopLineHeight(float height) {
		topLineHeight = height;
	}

	/**
	 * if height <= 0 it will <b>NOT</b> draw bottom line.<br>
	 * <i><b>DEFAULT</b></i> is 1<br>
	 * 
	 * @see #setTopLineHeight(int)
	 */

	public void setbottomLineHeight(float height) {
		bottomLineHeight = height;
	}

	/**
	 * <i><b>default</b></i> is 0
	 */
	public void setBottomMarginLeft(float margin) {
		bottomMarginLeft = margin;
	}

	@Override
	protected void dispatchDraw(Canvas canvas) {
		super.dispatchDraw(canvas);
		drawTopLine(canvas);
		drawBottomLine(canvas);
	}

	@Override
	public boolean hasFocusable() {
		return false;
	}

}
