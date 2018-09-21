package com.daxiangce123.android.listener;

import com.daxiangce123.android.data.AlbumEntity;

/**
 * called after open an album
 * <p/>
 * Notice the difference with {@link AlbumListener}
 *
 * @author
 * @project DaXiangCe
 * @time 2014-4-12
 */
public interface OnOpenAlbumListener {

    public void onOpenAlbum(AlbumEntity albumEntity);

}
