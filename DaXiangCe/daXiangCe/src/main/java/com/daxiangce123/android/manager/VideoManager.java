package com.daxiangce123.android.manager;

import android.annotation.SuppressLint;

import com.daxiangce123.android.App;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.Utils;

import java.io.File;
import java.util.HashSet;

/**
 * @author ram
 * @project DaXiangCe
 * @time May 4, 2014
 */
public class VideoManager {
    private static VideoManager manager;
    private HashSet<String> downloadVideoFileId = new HashSet<String>();

    private VideoManager() {

    }

    public final static VideoManager instance() {
        if (manager == null) {
            manager = new VideoManager();
        }
        return manager;
    }

    public final void download(FileEntity fileEntity) {
        download(fileEntity, null);
    }

    public final void download(FileEntity fileEntity, String token) {
        if (!downloadVideoFileId.contains(fileEntity.getId())) {
            String path = getVideoPath(fileEntity);

            ConnectBuilder.downloadFile(fileEntity.getId(), token, path, fileEntity.getSize(), null);
            downloadVideoFileId.add(fileEntity.getId());
//		ConnectBuilder.downloadFileCount(fileEntity.getId(), path, token, fileEntity.getSize());
        }
        if (App.DEBUG) {
            LogUtil.d("ViedoManger", "downlaod : " + downloadVideoFileId);
        }

    }

    public final boolean playable(FileEntity fileEntity) {
        if (fileEntity == null) {
            return false;
        }
        String path = getVideoPath(fileEntity);
        if (App.DEBUG) {
            LogUtil.d("ViedoManger", "downlaod -- path " + path);
        }

        if (!FileUtil.exists(path)) {
            return false;
        }
        long size = FileUtil.size(path);
        if (App.DEBUG) {
            LogUtil.d("ViedoManger", "downlaod -- size " + size + "--- fileEntity.getSize()---" + fileEntity.getSize());
        }
        if (size != fileEntity.getSize()) {
            return false;
        }
        return true;
    }

//    public final HashSet<String> getDownloadVideoFileId() {
//        return downloadVideoFileId;
//    }

    public final String getVideoPath(FileEntity fileEntity) {
        if (fileEntity == null) {
            return null;
        }
        return getVideoPath(fileEntity.getId());
    }

    public final String getVideoPath(String fileID) {
        if (Utils.isEmpty(fileID)) {
            return null;
        }

        String videoDir = MediaUtil.getVideoDir();
        if (videoDir == null) {
            return null;
        }
        String path = videoDir + File.separator + fileID;
        LogUtil.d("xxxxx", " download -- getVideoPath-- paht " + path);
        return path;
    }

    @SuppressLint("DefaultLocale")
    public boolean isVideo(FileEntity entity) {
        if (entity == null || entity.getMimeType() == null) {
            return false;
        }
        if (entity.getMimeType().toLowerCase().startsWith("video/")) {
            return true;
        }
        return false;
    }

    public boolean hasLocal(FileEntity entity) {
        String path = getVideoPath(entity);
        return FileUtil.exists(path);
    }

    public String save(final FileEntity entity) {
        if (entity == null) {
            return null;
        }
        if (!isVideo(entity)) {
            return null;
        }
        final String path = getVideoPath(entity.getId());
        if (FileUtil.exists(path)) {
            String suffix = FileUtil.mimeToSuffix(entity.getMimeType());
            String title = FileUtil.removeSuffix(entity.getTitle());
            String tmpDest = MediaUtil.getDestSaveDir() + File.separator + title + "." + suffix;
            if (FileUtil.exists(tmpDest)) {
                tmpDest = MediaUtil.getDestSaveDir() + File.separator + title + "_" + System.currentTimeMillis() + "." + suffix;
            }
            final String dest = tmpDest;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    FileUtil.copy(path, dest);
                    Utils.scanNewMedia(dest);
                }
            };
            TaskRuntime.instance().run(runnable);
            return dest;
        }
        return null;
    }


    public void clear() {
        if (downloadVideoFileId != null) {
            downloadVideoFileId.clear();
//            downloadVideoFileId=null;
        }
    }
}
