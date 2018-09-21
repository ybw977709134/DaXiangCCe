package com.daxiangce123.android.listener;

import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.TempToken;

/**
 * @project DaXiangCe
 * @time Jun 30, 2014
 * @author ram
 */
public interface TempTokenListener {

	public TempToken getToken(AlbumEntity album);
}
