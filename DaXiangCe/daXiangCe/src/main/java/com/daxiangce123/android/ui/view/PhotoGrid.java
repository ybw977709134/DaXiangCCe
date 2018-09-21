package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;

/**
 * @project DaXiangCe
 * @time 2014-4-16
 * @author
 */
public class PhotoGrid extends ViewGroup {

	private int verticalSpacing;
	private int horizontalSpacing;

	public PhotoGrid(Context context) {
		this(context, null);
	}

	public PhotoGrid(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public int getCount() {
		return getChildCount();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		final int width = getMeasuredWidth();
		final int childCount = getCount();
		final int paddingLeft = getPaddingLeft();
		final int paddingRight = getPaddingRight();
		final int paddingTop = getPaddingTop();
		final int paddingBottom = getPaddingBottom();
	}
}
