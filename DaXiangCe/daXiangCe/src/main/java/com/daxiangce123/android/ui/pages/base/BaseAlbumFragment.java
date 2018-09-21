package com.daxiangce123.android.ui.pages.base;

import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.data.FileEntity;

import java.util.List;

/**
 * Created by hansentian on 3/12/15.
 */
public abstract class BaseAlbumFragment extends BaseTabBarFragment {

    public List<FileEntity> fileList = null;
    public List<FileEntity> ownedList = null;
    public AlbumItemController albumItemController;

    public void setFileList(AlbumItemController albumItemController) {
        this.albumItemController = albumItemController;
        this.fileList = albumItemController.getFileList();
    }


}
