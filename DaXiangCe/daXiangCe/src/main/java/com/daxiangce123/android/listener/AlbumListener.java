package com.daxiangce123.android.listener;

import com.daxiangce123.android.data.AlbumEntity;

/**
 * called to open an Album
 * <p>
 * Notice the difference with {@link OnOpenAlbumListener}
 * 
 * @project DaXiangCe
 * @time 2014-4-12
 * @author
 */
public interface AlbumListener {

	public boolean openAlbum(AlbumEntity albumEntity);

	public boolean hasAlbum(String albumId);

	public boolean bannerContentIsShow(boolean isShow);

}
