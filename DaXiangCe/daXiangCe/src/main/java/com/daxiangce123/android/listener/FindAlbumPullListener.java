package com.daxiangce123.android.listener;

import com.handmark.pulltorefresh.library.PullToRefreshBase;

/**
 * @project DaXiangCe
 * @time Jun 11, 2014
 * @author ram
 */
public interface FindAlbumPullListener {

	public void onPullDownToRefresh(PullToRefreshBase<?> refreshView,
			String mode);

	public void onPullUpToRefresh(PullToRefreshBase<?> refreshView, String mode);
}
