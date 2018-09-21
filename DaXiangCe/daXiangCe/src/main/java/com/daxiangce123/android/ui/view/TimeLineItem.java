package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.util.Utils;

public class TimeLineItem extends LinearLayout {
	private int mMeasuredHeight = 0;
	private Paint mPaint = new Paint();
	private Rect mRect = new Rect();
	private int mLineWidth = 0;

	public TimeLineItem(Context context) {
		this(context, null);
	}

	public TimeLineItem(Context context, AttributeSet attrs) {
		super(context, attrs);
		// setOrientation(VERTICAL);
		setWillNotDraw(false);
		mPaint.setColor(0xffdcdcdc);
		mPaint.setStyle(Style.FILL);
		mLineWidth = Utils.dp2px(context, 6);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		View timeLine = findViewById(R.id.fl_time_avater_outer);
		int left = (timeLine.getRight() + timeLine.getLeft() - mLineWidth) / 2;
		int top = 0;
		mRect.set(left, top, left + mLineWidth, top + mMeasuredHeight);
		canvas.drawRect(mRect, mPaint);
		super.onDraw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		mMeasuredHeight = this.getMeasuredHeight();
	}
}
