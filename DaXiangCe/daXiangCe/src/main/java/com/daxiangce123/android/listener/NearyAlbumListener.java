package com.daxiangce123.android.listener;

import java.util.List;

import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.TempToken;

public interface NearyAlbumListener {
	public boolean onAlbumSelected(int position);

	public boolean hasJoined(String albumId);

	public List<FileEntity> getSamples(String albumId);

	public boolean needPasswd(AlbumEntity album);

	public TempToken getToken(AlbumEntity album);

	public boolean openAlbum(AlbumEntity album);
}