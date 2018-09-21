package com.daxiangce123.android.business;

import android.util.Log;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.business.event.Signal;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.listener.OnFileOptionListener;
import com.daxiangce123.android.ui.pages.AlbumTimeLineFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import de.greenrobot.event.EventBus;

/**
 * a data structure that store album items
 */
public class AlbumItemController {
    private String TAG = "AlbumItemList";

    protected Hashtable<String, FileEntity> loadedFiles;
    protected Vector<FileEntity> files;
    protected String albumId;
    protected AlbumEntity albumEntity;
    protected boolean isJoined = true, isLoadingMore = false, hasMore = true, forceClearFlag = false, changeSortAndClearFlag = false, joinedChangeFlag = false;
    protected OnFileOptionListener fileOptionListener;
    protected DBHelper dbHelper;
    private Consts.FileSort fileSort = Consts.FileSort.TIMELINE_SORT;

    public boolean isJoinedChangeFlag() {
        return joinedChangeFlag;
    }

    public void setJoinedChangeFlag(boolean joinedChangeFlag) {
        this.joinedChangeFlag = joinedChangeFlag;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public boolean hasMore() {
        return this.hasMore;
    }

    public void setDbHelper(DBHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public void setOnFileOptionListener(OnFileOptionListener onFileOptionListener) {
        this.fileOptionListener = onFileOptionListener;
    }

    public boolean isLoadingMore() {
        return isLoadingMore;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public AlbumEntity getAlbumEntity() {
        return albumEntity;
    }

    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    public FileEntity get(String fileId) {
        return loadedFiles.get(fileId);
    }

    public FileEntity get(int position) {
        return files.get(position);
    }

    public List<FileEntity> getFileList() {
        return files;
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public Consts.FileSort getFileSort() {
        return fileSort;
    }

    public void setFileSortAndPreReload(Consts.FileSort fileSort) {
        this.fileSort = fileSort;
        changeSortAndClearFlag = true;
    }

    public boolean isChangeSortAndClearFlag() {
        return changeSortAndClearFlag;
    }

    public void setChangeSortAndClearFlag(boolean changeSortAndClearFlag) {
        this.changeSortAndClearFlag = changeSortAndClearFlag;
    }

    public boolean isForceClearFlag() {
        return forceClearFlag;
    }

    public void setForceClearFlag(boolean forceClearFlag) {
        this.forceClearFlag = forceClearFlag;
    }

    public AlbumItemController(String albumId) {
        loadedFiles = new Hashtable<>();
        files = new Vector<>();
        this.albumId = albumId;
    }

    public AlbumItemController(AlbumEntity albumEntity) {
        loadedFiles = new Hashtable<>();
        files = new Vector<>();
        this.albumId = albumEntity.getId();
    }

    public boolean isUploading() {
        for (FileEntity itemEntity : files) {
            if (itemEntity.isUploading()) {
                return true;
            }
        }
        return false;
    }

    public void showItemsForSample(List<FileEntity> serverList) {
        if (changeSortAndClearFlag) {
            clear();
            changeSortAndClearFlag = false;
        }
        if (serverList != null && !serverList.isEmpty()) {
            List<FileEntity> toRemove = new ArrayList<>();
            for (FileEntity file : serverList) {
                String fileId = file.getId();
                if (loadedFiles.containsKey(fileId)) {
                    loadedFiles.get(fileId).clone(file);
                    toRemove.add(file);
                } else {
                    loadedFiles.put(fileId, file);
                }
            }
            serverList.removeAll(toRemove);
        }
        files.addAll(serverList);
    }

    public void showItems(List<FileEntity> serverList) {
        if (changeSortAndClearFlag || forceClearFlag) {
            forceClearFlag = false;
            changeSortAndClearFlag = false;
            clear();
        }
        files.addAll(serverList);
        List<FileEntity> toRemove = new ArrayList<>();
        for (FileEntity file : serverList) {
            if (loadedFiles.containsKey(file.getId())) {
                loadedFiles.get(file.getId()).clone(file);
                toRemove.add(file);
            } else {
                String fakeId = Utils.createEntityHashId(file);
                if (loadedFiles.containsKey(fakeId)) {
                    loadedFiles.get(fakeId).clone(file);
                    FileEntity newItem = loadedFiles.remove(fakeId);
                    if (newItem != null) {
                        loadedFiles.put(newItem.getId(), newItem);
                    }
                    toRemove.add(file);
                } else {
                    loadedFiles.put(file.getId(), file);
                }
            }

        }
        files.removeAll(toRemove);
    }

    /**
     * add file into List
     *
     * @param list        the fileList need to be added
     * @param uniq        weather remove item that duplicated or not
     * @param checkUpload sendBroadcast to check unUploaded file
     */
    public void add(List<FileEntity> list, boolean uniq, boolean checkUpload, boolean addToFirst) {
        if (Utils.isEmpty(list)) {
            return;
        }
        boolean syncUpload = false;
        if (uniq) {
            List<FileEntity> toRemove = new ArrayList<>();
            for (FileEntity file : list) {
                if (loadedFiles.containsKey(file.getId())) {
                    toRemove.add(file);
                }
            }
            list.removeAll(toRemove);
            if (!Utils.isEmpty(list)) {
                for (FileEntity file : list) {
                    loadedFiles.put(file.getId(), file);
                }
            }
        } else {
            for (FileEntity file : list) {
                // loadedFiles.add(file.getId());
                loadedFiles.put(file.getId(), file);
                if (file.isUploading()) {
                    syncUpload = true;
                }
            }

        }
        if (addToFirst) {
            files.addAll(0, list);
        } else {
            files.addAll(list);
        }
        if (syncUpload && checkUpload) {
            Broadcaster.sendBroadcast(Consts.START_UPLOADING_CHECKING);
        }

    }

    public boolean isEmpty() {
        return Utils.isEmpty(files);
    }

    /**
     * load more item and send broadcast
     */
    public void loadMore(final String key) {
        if (isLoadingMore) {
            return;
        }
        isLoadingMore = true;
        if (isJoined) {
            TaskRuntime.instance().run(new Task() {
                @Override
                public void run() {
                    if (App.DEBUG) {
                        Log.v(TAG, "=======loadMore begin=====\n  files=" + size());
                    }
                    final List<FileEntity> listFromDb = readDbAlbumList();
                    if (Utils.isEmpty(listFromDb)) {
                        if (!isUploading() && !(Consts.PHOTO_VIEWER_FRAGMENT).equals(key)) {
                            runOnUI(new Runnable() {
                                @Override
                                public void run() {
                                    CToast.showToast(R.string.no_more);
                                }
                            });
                        }
                        EventBus.getDefault().post(new Signal(Signal.ALBUM_CONTROLLER_LOAD_MORE_EMPTY));
                    } else {
                        runOnUI(new Runnable() {
                            @Override
                            public void run() {
                                AlbumItemController.this.add(listFromDb, true, true, false);
                                EventBus.getDefault().post(new Signal(Signal.ALBUM_CONTROLLER_LOAD_MORE_OK));
                            }
                        });
                    }
                    isLoadingMore = false;
                }
            });
        } else {
            if (hasMore) {
                ConnectBuilder.getAlbumItems(albumId, size(), 45, fileSort.getServer_sort(), Consts.Order.DESC, Consts.GET_NON_MEMBER_ALBUM_ITEMS);
            } else {
                if (!(Consts.PHOTO_VIEWER_FRAGMENT).equals(key)) {
                    CToast.showToast(R.string.no_more);
                }
                EventBus.getDefault().post(new Signal(Signal.ALBUM_CONTROLLER_LOAD_MORE_EMPTY));
            }
            isLoadingMore = false;
        }

    }

    private DBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(App.getUid());
        }
        return dbHelper;
    }

    public List<FileEntity> readDbAlbumList() {
        DBHelper dbHelper = getDbHelper();
        int limit = fileOptionListener.getPageSize();
        // int limit = albumEntity.getSize();
        int offset = size();
        String WHERE;
        if (fileOptionListener instanceof AlbumTimeLineFragment) {
            WHERE = Consts.ALBUM + "=\"" + albumId + "\" AND " + Consts.BATCH_ID + " IN  (SELECT * FROM " + "(SELECT " + Consts.BATCH_ID + " FROM " + FileEntity.EMPTY.getTableName() + " WHERE "
                    + Consts.ALBUM + "=\"" + albumId + "\" ORDER BY create_date DESC LIMIT " + limit + " OFFSET " + offset + " ) " + "GROUP BY " + Consts.BATCH_ID + ") " + "ORDER BY "
                    + Consts.CREATE_DATE + " DESC";
        } else {
            WHERE = Consts.ALBUM + "=\"" + albumId + "\" ORDER BY " + fileSort.getDb_sort() + " DESC LIMIT " + limit + " OFFSET " + offset;
        }
        List<FileEntity> result = dbHelper.getList(FileEntity.EMPTY, WHERE);
        LogUtil.d(TAG, "===readDbAlbumList====\n" + WHERE);
        LogUtil.d(TAG, "+\n readDbAlbumList	RESULT:" + Utils.sizeOf(result));
        return result;
    }

    public void clear() {
        files.clear();
        loadedFiles.clear();
    }

    public void releaseAll() {
        files.clear();
        loadedFiles.clear();
        fileOptionListener = null;

    }

    public int positionOf(FileEntity file) {
        return files.indexOf(file);
    }

    /**
     * get the position of item
     *
     * @param fileId
     * @return
     */
    public int positionOf(String fileId) {
        FileEntity fileEntity = loadedFiles.get(fileId);
        return positionOf(fileEntity);
    }

    public int size() {
        return files.size();
    }

    public FileEntity remove(String fileId) {
        FileEntity toRemove = loadedFiles.remove(fileId);
        if (toRemove != null) {
            files.remove(toRemove);
        }
        return toRemove;
    }

    public boolean containsKey(String key) {
        return loadedFiles.containsKey(key);
    }

    /**
     * add file into list
     *
     * @return old fileEntity that old key
     */
    public FileEntity addFile(FileEntity file, boolean addToFirst, boolean repleaceOld) {
        FileEntity old = null;
        boolean existed = loadedFiles.containsKey(file.getId());
        if (existed && repleaceOld) {
            return loadedFiles.get(file.getId());
        } else {
            old = loadedFiles.put(file.getId(), file);
        }
        if (addToFirst) {
            files.add(0, file);
        } else {
            files.add(file);
        }
        return old;
    }

    public void clearAndResetFileList(List<FileEntity> list) {
        files.clear();
        files.addAll(list);
    }

    /**
     * update key in loadedFiles
     *
     * @param oldHash
     * @param fileEntity
     */
    public void updateHash(String oldHash, FileEntity fileEntity) {
        FileEntity file = remove(oldHash);
        if (file != null && fileEntity != null) {
            loadedFiles.put(fileEntity.getId(), fileEntity);
            files.add(fileEntity);
        }
    }

    public void sortList() {
        if (fileOptionListener != null) {
            fileOptionListener.sortAlbumList();
        }
    }

}
