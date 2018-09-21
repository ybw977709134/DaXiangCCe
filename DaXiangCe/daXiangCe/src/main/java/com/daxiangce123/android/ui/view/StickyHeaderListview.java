package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListAdapter;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author ram
 * @project DaXiangCe
 * @time Apr 8, 2014
 */
public class StickyHeaderListview extends PullToRefreshListView {

    public final static String TAG = "StickyHeaderListview";
    private StickHeaderListener stickHeaderListener;
    private StickyHeaderListViewWrapper frame;
    private boolean clippingToPadding = true;
    private int firstPosition = 0;
    private int totalVisiableCount = -1;
    private int firstHeaderBottomPos;
    // private int secondHeaderYScroll;
    private boolean isFirstMeasure = true;

    public interface StickHeaderListener {
        public View getFirstStickyView(int position);

        public View getSecondStickyView(int position);

        public View getHeader(int position, View convertView, ViewGroup parent);
    }

    private OnScrollListener onScrollListener = new OnScrollListener() {

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (isFirstMeasure) {
                isFirstMeasure = false;
                obtainHeaderView();
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            // if (App.DEBUG) {
            // LogUtil.d(TAG, " firstVisibleItem :" + firstVisibleItem
            // + " visibleItemCount:" + visibleItemCount
            // + " totalItemCount:" + totalItemCount);
            // }
            updateHeaderVisibilities();
            totalVisiableCount = visibleItemCount;
            if (firstPosition == firstVisibleItem) {
                return;
            }
            firstPosition = firstVisibleItem;
            if (stickHeaderListener == null) {
                return;
            }
            obtainHeaderView();
            // updateHeaderVisibilities();
        }
    };

    public StickyHeaderListview(Context context) {
        this(context, null);
    }

    public StickyHeaderListview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    private void initView() {
        // viewid = R.id.tv_time_top;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (frame == null) {
            ViewGroup parent = ((ViewGroup) getParent());
            int listIndex = parent.indexOfChild(this);
            parent.removeView(this);

            final int visibility = getVisibility();
            setVisibility(View.VISIBLE);

            frame = new StickyHeaderListViewWrapper(getContext());
            frame.setSelector(getRefreshableView().getSelector());
            frame.setDrawSelectorOnTop(true);
            frame.setVisibility(visibility);

            if (clippingToPadding) {
                frame.setPadding(0, getPaddingTop(), 0, getPaddingBottom());
                setPadding(getPaddingLeft(), 0, getPaddingRight(), 0);
            }

            ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
            setLayoutParams(params);

            frame.addView(this);
            frame.setBackgroundDrawable(getBackground());
            super.setBackgroundDrawable(null);

            ViewGroup.MarginLayoutParams p = (MarginLayoutParams) getLayoutParams();
            frame.setLayoutParams(p);

            parent.addView(frame, listIndex);

        }
    }

    @Override
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof StickHeaderListener) {
            if (stickHeaderListener == adapter) {
                return;
            }
            stickHeaderListener = (StickHeaderListener) adapter;
            if (App.DEBUG) {
                LogUtil.d(TAG, "StickHeaderListener is not null");
            }
            if (frame != null) {
                frame.removeHeader();
                obtainHeaderView();
            }
            updateHeaderVisibilities();
        }
        super.setAdapter(adapter);
    }


    private void obtainHeaderView() {
        View v = stickHeaderListener.getHeader(firstPosition, frame.getHeader(), frame);
        frame.setHeader(v);
    }

    private void updateHeaderVisibilities() {
        if (stickHeaderListener == null) {
            return;
        }
        if (frame == null || frame.getHeader() == null || totalVisiableCount < 1 || firstPosition < 0) {
            return;
        }
        firstHeaderBottomPos = Integer.MIN_VALUE;
        // secondHeaderYScroll = Integer.MIN_VALUE;

        View firstStickyView = frame.getHeader().findViewById(R.id.ll_date_indexer);
        // View secondStickyView = frame.getHeader().findViewById(
        // R.id.ll_time_avater);
        for (int i = 0; i < totalVisiableCount; i++) {
            View listChild = getChildAt(i);
            if (listChild == null) {
                continue;
            }
            View firstHeader = stickHeaderListener.getFirstStickyView(i + firstPosition);
            // View firstHeader = listChild.findViewById(R.id.ll_date_indexer);
            boolean result = updateFirstHeader(listChild, firstHeader, 0);
            if (!result && i == 0) {
                firstHeaderBottomPos = firstStickyView.getMeasuredHeight();
            }
            // View secondHeader = stickHeaderListener.getSecondStickyView(i
            // + theFirstVisibleItem);
            // updateSecondHeader(listChild, secondHeader,
            // firstHeader.getMeasuredHeight());
        }
        frame.setHeaderBottomPosition(firstStickyView, firstHeaderBottomPos);
        // frame.setHeaderBottomPosition(secondStickyView, secondHeaderYScroll
        // - firstHeaderYScroll);
    }

    private boolean updateFirstHeader(View listChild, View header, int toTop) {
        if (listChild == null) {
            // LogUtil.d(TAG, "listChild is null");
            return false;
        }
        if (header == null) {
            // LogUtil.d(TAG, "header is NULL!!!!!!!!!!!!!");
            return false;
        }
        // LogUtil.d(TAG, "visibility is " + listChild.getVisibility() + "|"
        // + header.getVisibility() + " " + header.getTag());
        if (header.getVisibility() == View.GONE) {
            // LogUtil.d(TAG, "header visibility is GONE!!!!!!!!!!!!!");
            return false;
        }
        if (listChild.getVisibility() == View.GONE) {
            // LogUtil.d(TAG,
            // "listChild visibility is NOT visiable!!!!!!!!!!!!!");
            return false;
        }
        int top = clippingToPadding ? getPaddingTop() : 0;
        int childTop = listChild.getTop() + toTop;

        final int oldYScroll = firstHeaderBottomPos;
        if (childTop > 0) {
            firstHeaderBottomPos = childTop - top;
        }

        if (childTop <= top) {
            header.setVisibility(View.INVISIBLE);
            firstHeaderBottomPos = header.getMeasuredHeight();
        } else {
            header.setVisibility(View.VISIBLE);
        }

        if (oldYScroll != Integer.MIN_VALUE) {
            firstHeaderBottomPos = Math.min(oldYScroll, firstHeaderBottomPos);
        }

        if (Math.abs(firstHeaderBottomPos) > header.getMeasuredHeight()) {
            firstHeaderBottomPos = header.getMeasuredHeight();
        }
        return true;
    }

}
