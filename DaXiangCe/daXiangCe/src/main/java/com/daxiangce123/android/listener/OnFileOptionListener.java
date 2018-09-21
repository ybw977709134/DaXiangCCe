package com.daxiangce123.android.listener;

import android.graphics.Bitmap;

import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;

import java.util.List;

/**
 * @author ram
 * @project DaXiangCe
 * @time Apr 18, 2014
 */
public interface OnFileOptionListener {


    public void onFileDeleted(FileEntity fileEntity);

    public void onFileUpload(FileEntity fileEntity);

    public boolean showDeleteMode();

    public void deleteFile();

    public void selectAll(boolean select);

    public void onItemEnd(AlbumEntity album);

    public int getCurrentLoadFileCount();

    public int getPageSize();

    public void onFileUploadDone(FileEntity fileEntity);

    /**
     * get sorted album list
     *
     * @return
     */
    public void sortAlbumList();

    public Bitmap getAvatarBitmap();

    public void setShowBottomBar(boolean isShow);

    public boolean isShowTabBar();

}
