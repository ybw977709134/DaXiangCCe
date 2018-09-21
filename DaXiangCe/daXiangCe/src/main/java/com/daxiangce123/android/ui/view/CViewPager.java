package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.PointF;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * @author ram
 * @project Pickup
 * @time Jan 7, 2014
 */
public class CViewPager extends ViewPager {

    public interface OnSingleTouchListener {
        public void onSingleTouch();
    }

    private PointF downPoint = new PointF();
    private PointF curPoint = new PointF();
    private OnSingleTouchListener onSingleTouchListener;

    public CViewPager(Context context) {
        this(context, null);
    }

    public CViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (Exception e) {
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        curPoint.x = event.getX();
        curPoint.y = event.getY();
        int action = event.getAction();
        if (action == MotionEvent.ACTION_DOWN) {
            downPoint.x = event.getX();
            downPoint.y = event.getY();
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        if (action == MotionEvent.ACTION_MOVE) {
            getParent().requestDisallowInterceptTouchEvent(true);
        }
        if (action == MotionEvent.ACTION_UP) {
            if (downPoint.x == curPoint.x && downPoint.y == curPoint.y) {
                onSingleTouch();
                return true;
            }
        }
        try {
            return super.onTouchEvent(event);
        } catch (Exception e) {
        }
        return false;
    }

    public void onSingleTouch() {
        if (onSingleTouchListener != null) {
            onSingleTouchListener.onSingleTouch();
        }
    }

    public void setOnSingleTouchListener(
            OnSingleTouchListener onSingleTouchListener) {
        this.onSingleTouchListener = onSingleTouchListener;
    }
}
