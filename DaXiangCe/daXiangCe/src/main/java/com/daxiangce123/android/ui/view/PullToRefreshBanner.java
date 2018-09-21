package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Adapter;

import com.daxiangce123.R;
import com.handmark.pulltorefresh.library.PullToRefreshBase;

public class PullToRefreshBanner extends PullToRefreshBase<View> {

	// private RelativeLayout rlBanner;
	// private GridView gvItems;
	private View refreshableView;
	private ListGrid gridViewContent;

	public PullToRefreshBanner(Context context) {
		super(context);
	}

	public PullToRefreshBanner(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PullToRefreshBanner(Context context, Mode mode) {
		super(context, mode);
	}

	public PullToRefreshBanner(Context context, Mode mode, AnimationStyle style) {
		super(context, mode, style);
	}

	@Override
	public com.handmark.pulltorefresh.library.PullToRefreshBase.Orientation getPullToRefreshScrollDirection() {
		return Orientation.VERTICAL;
	}

	@Override
	protected View createRefreshableView(Context context, AttributeSet attrs) {
		if (refreshableView == null) {
			refreshableView = LayoutInflater.from(context).inflate(
					R.layout.grid_banner, null, false);
			// rlBanner = (RelativeLayout) refreshableView
			// .findViewById(R.id.rl_banner);
			gridViewContent = (ListGrid) refreshableView
					.findViewById(R.id.gv_content);
		}
		return refreshableView;
	}

	@Override
	protected boolean isReadyForPullEnd() {
		return isLastItemVisible();
	}

	@Override
	protected boolean isReadyForPullStart() {
		return isFirstItemVisible();
	}

	private boolean isFirstItemVisible() {
		// int y = svContent.getScrollY();
		// return (y == 0);
		final Adapter adapter = gridViewContent.getAdapter();
		if (null == adapter || adapter.isEmpty()) {
			return true;
		} else {
			if (gridViewContent.getFirstVisiblePosition() <= 1) {
				final View firstVisibleChild = gridViewContent.getChildAt(0);
				if (firstVisibleChild != null) {
					return firstVisibleChild.getTop() >= 0;
				}
			}
		}

		return false;
	}

	private boolean isLastItemVisible() {
		final Adapter adapter = gridViewContent.getAdapter();

		if (null == adapter || adapter.isEmpty()) {
			return true;
		} else {
			final int lastItemPosition = gridViewContent.getCount() - 1;
			final int lastVisiblePosition = gridViewContent
					.getLastVisiblePosition();

			if (lastVisiblePosition >= lastItemPosition - 1) {
				final int childIndex = lastVisiblePosition
						- gridViewContent.getFirstVisiblePosition();
				final View lastVisibleChild = gridViewContent
						.getChildAt(childIndex);
				if (lastVisibleChild != null) {
					return lastVisibleChild.getBottom() <= gridViewContent
							.getBottom();
				}
			}
		}

		return false;
	}

	public ListGrid getGridView() {
		return gridViewContent;
	}

	// public RelativeLayout getBanner() {
	// return rlBanner;
	// }

}
