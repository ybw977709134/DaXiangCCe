package com.daxiangce123.android.listener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * @project DaXiangCe
 * @time Jun 11, 2014
 * @author ram
 */
public interface OnPullListener {

	public void onPullDownToRefresh(PullToRefreshBase<?> refreshView, int pageSize);

	public void onPullUpToRefresh(PullToRefreshBase<?> refreshView, int pageSize);
}
