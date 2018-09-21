package com.daxiangce123.android.manager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.HandlerThread;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpStatus;

import java.io.File;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Vector;

/**
 * @author ram
 * @project Groubum
 * @time Mar 3, 2014
 */
public class FileUploadManager {

    public class CreateTask {
        boolean compress = true;
        String localPath;
        String batchId;
        String albumId;
        int index;
        /**
         * if set indicate that task is created from eventservice
         */
        UploadImage uploadImage = null;

        public CreateTask(String localPath, String batchId, String albumId, int index, boolean compress, UploadImage uploadImage) {
            this.uploadImage = uploadImage;
            this.localPath = localPath;
            this.batchId = batchId;
            this.albumId = albumId;
            this.index = index;
            this.compress = compress;
            this.uploadImage = uploadImage;
        }

    }

    protected final static String TAG = "FileCreateManager";
    private static FileUploadManager manager;
    private boolean running;
    private HandlerThread handlerThread;
    private Handler mHandler;

    /**
     * filelist to upload
     */
    private Vector<CreateTask> uploadList;

    private String avaterId;
    private String avaterPath;
    /**
     * batchId -> files
     */
    private Hashtable<String, Integer> uploadBatchs;

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            while (running) {
                try {
                    uploadAvater();
                    if (!hasTask()) {
                        stopLocal();
                        // LogUtil.d(TAG, "does not has task");
                        continue;
                    }
                    prepareFile();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Utils.isEmpty(action)) {
                    return;
                }
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (action.equals(Consts.CREATE_FILE)) {
                    handleFileCreate(info, response);
                } else if (action.equals(Consts.UPLOAD_FILE)) {
                    if (response.getStatusCode() == HttpStatus.SC_OK) {
                        FileEntity file = Parser.parseFile(response.getContent());
                        if (file == null) {
                            return;
                        }
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "UPLOAD_FILE SUCCESS");
                        }
                        renameUploadedFile(file);
                        removeBatchId(file);
                    } else {
                        //upload file again
                        if (!UploadCancelManager.sharedInstance().checkFileEntity(info.getTag3()) && info.needRepect()) {
                            info.decreaseRepectTime();
                            HttpUploadManager.instance().addConnect(info);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void renameUploadedFile(final FileEntity fileEntity) {
        final String srcPath = ImageManager.instance().getImageCachePath(Utils.createHashId(fileEntity.getAlbum(), fileEntity.getBatchId(), String.valueOf(fileEntity.getSeqNum())));
        // String destPath = ImageManager.instance().getImageCachePath();
        TaskRuntime.instance().run(new Runnable() {
            @Override
            public void run() {
                ImageManager.instance().copyToCache(srcPath, fileEntity.getId());
            }
        });

    }

    private boolean removeBatchId(FileEntity fileEntity) {
        String batchId = fileEntity.getBatchId();
        if (!uploadBatchs.containsKey(batchId)) {
            if (App.DEBUG) {
                LogUtil.d(TAG, "NO contain " + batchId);
            }
            return true;
        }
        int count = uploadBatchs.get(batchId);
        count--;
        if (count > 0) {
            uploadBatchs.put(batchId, Integer.valueOf(count));
            return true;
        }
        uploadBatchs.remove(batchId);
        ConnectBuilder.finishBatch(fileEntity.getAlbum(), batchId);
        if (App.DEBUG) {
            LogUtil.d(TAG, "finishBatch" + batchId);
        }
        return false;
    }

    private void handleFileCreate(ConnectInfo info, Response response) {
        if (response.getStatusCode() != 200) {
            String batchId = JSONObject.parseObject(info.getTag()).getString(Consts.BATCH_ID);
            int count = uploadBatchs.get(batchId);
            count--;
            uploadBatchs.put(batchId, Integer.valueOf(count));
            if (App.DEBUG) {
                LogUtil.d(TAG, "-------error!!!---CREATE_FILE---- " + response.getStatusCode());
            }
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "------------CREATE_FILE----->RESPONSE---> " + response);
        }
        String content = response.getContent();
        FileEntity fileEntity = Parser.parseFile(content);
        if (fileEntity == null) {
            return;
        }
        if (fileEntity.isActive()) {
            renameUploadedFile(fileEntity);
            removeBatchId(fileEntity);
            if (App.DEBUG) {
                LogUtil.d(TAG, "NO NEED TO UPLOAD AGAIN ");
            }
        } else {

            // uploading state need to upload file
            String path = info.getTag2();
            if (!Utils.existsEmpty(path, fileEntity.getDigest())) {
                // if (FileUtil.isImage(path)) {
                // path = FileUtil.rename(path, fileEntity.getId());
                // }
                ConnectBuilder.uploadFile(path, fileEntity.getId(), fileEntity.getBatchId(), fileEntity.getSize(), content);
                if (App.DEBUG) {
                    LogUtil.d(TAG, "-----UPLOADFILE----new ");
                }
            }
        }

    }

    /**
     * check task source
     *
     * @param uploadImage
     * @return F if create file again T handle by this function
     */
    private boolean handleUpload(UploadImage uploadImage) {
        FileEntity fileEntity = Parser.parseFile(uploadImage.getFileEntityContent());
        if (fileEntity == null) {
            return false;
        }
        if (fileEntity.getStatus().equals(Consts.FILEENTITY_STATUS_UNUPLOAD)) {
            // need do it again
            return false;
        } else if (fileEntity.getStatus().equals(Consts.FILEENTITY_STATUS_UPLOADING)) {
            // just need to upload file
            if (UploadCancelManager.sharedInstance().checkFileEntity(fileEntity)) {
                return true;
            }
            String path = uploadImage.getFilePath();
            boolean compress = uploadImage.isCompress();
            String name = FileUtil.getFileName(path);
            String mime = FileUtil.getMimeTypeFromPath(path);
            String suffix = FileUtil.getSuffix(name);
            String destPath = ImageManager.instance().getImageCachePath(Utils.createHashId(fileEntity.getAlbum(), fileEntity.getBatchId(), String.valueOf(fileEntity.getSeqNum())));
            if (!(new File(destPath)).exists()) {
                if (compress && (MimeTypeUtil.getMime(mime) == Mime.IMG)) {
                    BitmapUtil.compress(path, destPath, 640, 160 * 1024, true);
                }
                if (!FileUtil.exists(destPath) || FileUtil.size(destPath) == 0) {
                    FileUtil.copy(path, destPath);
                }
            }
            // if (FileUtil.isImage(destPath)) {
            // destPath = FileUtil.rename(destPath, fileEntity.getId());
            // }
            ConnectBuilder.uploadFile(destPath, fileEntity.getId(), fileEntity.getBatchId(), fileEntity.getSize(), uploadImage.getFileEntityContent());
            if (App.DEBUG) {
                LogUtil.d(TAG, "FILE_UPLOADED");
            }
        }

        return true;
    }

    private boolean uploadAvater() {
        if (Utils.existsEmpty(avaterPath, avaterId)) {
            return false;
        }
        if (!FileUtil.exists(avaterPath)) {
            return false;
        }
        ImageManager.instance().deleteLocal(avaterId);
        String destPath = ImageManager.instance().getImageCachePath(avaterId);
        BitmapUtil.compress(avaterPath, destPath, 640, 100 * 1024, true, true);
        if (!FileUtil.exists(destPath) || FileUtil.size(destPath) == 0) {
            destPath = avaterPath;
        }
        ConnectBuilder.setAvatar(destPath);
        avaterPath = null;
        return true;
    }

    private boolean prepareFile() {
        final CreateTask task = getFileTask();
        if (task == null) {
            return false;
        }
        if (task.uploadImage != null && handleUpload(task.uploadImage)) {
            return true;
        }
        if (!FileUtil.exists(task.localPath)) {
            return false;
        }
        String path = task.localPath;
        boolean compress = task.compress;
        String name = FileUtil.getFileName(path);
        String mime = FileUtil.getMimeTypeFromPath(path);
        String suffix = FileUtil.getSuffix(name);
        String destPath = ImageManager.instance().getImageCachePath(Utils.createHashId(task.albumId, task.batchId, String.valueOf(task.index)));
        if (compress && (MimeTypeUtil.getMime(mime) == Mime.IMG)) {
            BitmapUtil.compress(path, destPath, 640, 160 * 1024, true);
        }
        if (!FileUtil.exists(destPath) || FileUtil.size(destPath) == 0) {
            FileUtil.copy(path, destPath);
        }
        // compare file that gen in files

        long size = FileUtil.size(destPath);
        String hash = FileUtil.getFileMD5(destPath);
        name = FileUtil.removeSuffix(name);
        name = Utils.toMaxLength(name, 20);
        String title = name + "." + suffix;
        String jsonParam = createJSon(task.albumId, title, title, size, hash, mime, task.batchId, task.index);

        if (App.DEBUG) {
            // LogUtil.d(TAG, "---------------------------------->REQUEST:\n" +
            // jsonParam);
        }
        if (!Utils.existsEmpty(jsonParam, task.batchId)) {
            ConnectBuilder.createFile(jsonParam, destPath);
            return true;
        }
        return false;
    }

    private CreateTask getFileTask() {
        if (Utils.isEmpty(uploadList)) {
            return null;
        }
        return uploadList.remove(0);
    }

    private String createJSon(String albumId, String title, String name, long size, String digest, String mime, String batchId, int seqNum) {
        if (size <= 0) {
            return null;
        }
        if (Utils.existsEmpty(albumId, title, name, digest, mime, batchId)) {
            LogUtil.w(TAG, "bad params:[albumId:" + albumId + "] - " //
                    + "[title:" + title + "] - " //
                    + "[name:" + name + "] - " //
                    + "[digest:" + digest + "] - " //
                    + "[mime:" + mime + "] - " //
                    + "[batchId:" + batchId + "]");
            return null;
        }
        JSONObject jo = new JSONObject();
        jo.put(Consts.ALBUM, albumId);
        jo.put(Consts.TITLE, title);
        jo.put(Consts.NAME, name);
        jo.put(Consts.SIZE, size);
        jo.put(Consts.DIGEST, digest);
        jo.put(Consts.MIME_TYPE, mime);
        jo.put(Consts.BATCH_ID, batchId);
        jo.put(Consts.SEQ_NUM, seqNum);
        return jo.toString();
    }

    public boolean hasTask() {
        if (Utils.isEmpty(uploadList)) {
            return false;
        }
        return true;
    }

    public boolean hasTaskRunning() {
        return uploadBatchs.size() != 0;
    }

    private void startIt() {
        if (running) {
            return;
        }
        running = true;
        if (handlerThread == null) {
            handlerThread = new HandlerThread(TAG);
            handlerThread.start();
        }
        if (mHandler == null) {
            mHandler = new Handler(handlerThread.getLooper());
        }
        mHandler.post(runnable);
    }

    private void stopLocal() {
        running = false;
        if (mHandler == null) {
            return;
        }
        mHandler.removeCallbacks(runnable);
    }

    private FileUploadManager() {
        running = false;
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.CREATE_FILE);
        ift.addAction(Consts.UPLOAD_FILE);
        ift.addAction(Consts.METHOD_FINISH_BATCH);
        Broadcaster.registerReceiver(receiver, ift);
        uploadBatchs = new Hashtable<String, Integer>();
    }

    /**
     * copy file to cache dir
     */
    // private void backupImage(String absPath, String fileId, boolean force) {
    // if (!FileUtil.exists(absPath)) {
    // LogUtil.w(TAG, "file not exists");
    // return;
    // }
    //
    // String imageDir = MediaUtil.getImageDir();
    // String tag = FileUtil.fileName(absPath);
    // if (FileUtil.childOf(absPath, imageDir)) {
    // LogUtil.w(TAG, "already in image manager");
    // return;
    // }
    //
    // String imagePath = ImageManager.instance().getImagePath(tag);
    // if (Utils.isEmpty(imagePath) || imagePath.equals(absPath)) {
    // LogUtil.w(TAG, "invalid image path");
    // return;
    // }
    //
    // if (FileUtil.exists(imagePath)) {
    // if (!force) {
    // LogUtil.w(TAG, "image exists, set force flag to overwrite!");
    // return;
    // } else {
    // FileUtil.delete(imagePath);
    // }
    // }
    // FileUtil.copy(absPath, imagePath);
    // }
    public final static FileUploadManager instance() {
        if (manager == null) {
            manager = new FileUploadManager();
        }
        return manager;
    }

    public final void stop() {
        stopLocal();
        manager = null;
        Broadcaster.unregisterReceiver(receiver);
        // if (hashMap != null) {
        // hashMap.clear();
        // hashMap = null;
        // }
        // if (uploadMap != null) {
        // uploadMap.clear();
        // uploadMap = null;
        // }
        if (uploadList != null) {
            uploadList.clear();
            uploadList = null;
        }
    }

    public final void createFile(List<UploadImage> localPaths, String albumId) {
        // TODO change here from ImageManager.java
        String batchId = localPaths.get(0).batchId;
        int count = Utils.sizeOf(localPaths);
        for (UploadImage image : localPaths) {
            if (image.getAlbumeId() != null) {
                createFile(image.filePath, image.getAlbumeId(), batchId, image.getSeqNum(), image.compress, image);
            } else {
                createFile(image.filePath, albumId, batchId, image.getSeqNum(), image.compress, null);
            }
        }
        Map<String, String> map = new HashMap<String, String>();
        map.put("Num", count + "");
        UMutils.instance().diyEvent(ID.EventUploadFileSuccess, map);
        uploadBatchs.put(batchId, Integer.valueOf(count));
    }

    public void createFile(String localPath, String albumId) {
        String batchId = UUID.randomUUID().toString();
        createFile(localPath, albumId, batchId, 0, true, null);
        uploadBatchs.put(batchId, Integer.valueOf(1));
    }

    private final synchronized void createFile(String path, String albumId, String batchId, int seqNum, boolean compress, UploadImage uploadImage) {
        if (!FileUtil.exists(path)) {
            return;
        }
        if (!FileUtil.exists(path)) {
            return;
        }

        CreateTask task = new CreateTask(path, batchId, albumId, seqNum, compress, uploadImage);
        if (uploadList == null) {
            uploadList = new Vector<CreateTask>();
        }
        uploadList.add(task);
        if (!running) {
            startIt();
        }
    }

    public final void setAvater(String path, String avaterId) {
        this.avaterPath = path;
        this.avaterId = avaterId;
        if (!running) {
            startIt();
        }
    }

    public Vector<CreateTask> getUploadList() {
        return uploadList;
    }

    public Hashtable<String, Integer> getUploadTask() {
        return uploadBatchs;
    }

}
