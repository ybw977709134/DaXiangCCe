package com.daxiangce123.android.business.event;

import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.util.Utils;

/**
 * Created by hansentian on 3/16/15.
 */
public class FileDeleteEvent {
    public String fileId;
    public FileEntity fileEntity;
    public int deletedPosition = -1;

    public FileDeleteEvent(String fileId, int deletedPosition) {
        this.fileId = fileId;
        this.deletedPosition = deletedPosition;
    }

    public boolean shouldDeleteInPhotoView() {
        if (Utils.isEmpty(fileId) || deletedPosition == -1) {
            return false;
        } else {
            return true;
        }
    }
}
