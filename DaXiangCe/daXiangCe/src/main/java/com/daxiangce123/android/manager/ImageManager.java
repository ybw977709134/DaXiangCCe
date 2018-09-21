package com.daxiangce123.android.manager;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageKey;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.ImageSize.ThumbSize;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.Utils;
import com.nostra13.universalimageloader.cache.disc.DiskCache;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DefaultConfigurationFactory;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.download.ImageDownloader.Scheme;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class ImageManager {
    public static final String TAG = "ImageManager";
    private static ImageManager mInstance;
    private ImageLoaderConfiguration loaderConfiguration;
    private DisplayImageOptions options;
    private ImageLoader imageLoader;
    private DiskCache diskCache;
    private int DISKCACHESIZE = 800 * 1024 * 1024;

    private ImageManager() {
        initOption();
        imageLoader = ImageLoader.getInstance();
    }

    private void initOption() {
        options = UILUtils.getDiaplayOption()
                // .showImageForEmptyUri(R.drawable.default_image_small)
                // .showImageOnFail(R.drawable.default_image_small)
                // .showImageOnLoading(R.drawable.default_image_small)
                .build();
    }

    /**
     * Before using {@link ImageManager} please ensure u had
     * {@link #init(Context)}
     *
     * @return
     * @time Jun 25, 2014
     */
    public static ImageManager instance() {
        if (mInstance == null) {
            mInstance = new ImageManager();
        }
        return mInstance;
    }

    public ImageLoader getLoader() {
        return imageLoader;
    }

    public void init(Context context) {
        FileNameGenerator nameGenerator = new FileNameGenerator() {
            @Override
            public String generate(String imageUri) {
                // parser udid from uri
                if (imageUri == null) {
                    return imageUri;
                }
                String name = imageUri;
                // if (!imageUri.startsWith(Consts.HOST_HTTPS)) {
                // name = Utils.MD5(imageUri);
                // } else {
                name = imageUri.trim().replace(Consts.HOST_HTTPS, "");
                name = name.trim().replace(Consts.HOST_HTTP, "");
                String method = null;
                if (name.startsWith(Consts.METHOD_DOWNLOAD_FILE)) {
                    // /file/content/{file_id}
                    method = Consts.METHOD_DOWNLOAD_FILE;
                } else if (name.startsWith(Consts.METHOD_DOWNLOAD_AVATAR)) {
                    // /user/avatar/{user_id}
                    method = Consts.METHOD_DOWNLOAD_AVATAR;
                } else if (name.startsWith(Consts.METHOD_DOWN_BANNER)) {
                    // /banner/content/{banner id}
                    method = Consts.METHOD_DOWN_BANNER;
                } else if (name.startsWith(Consts.METHOD_DOWNLOAD_THUMB)) {
                    // /thumb/{file_id}?dim={width_x_height}
                    method = Consts.METHOD_DOWNLOAD_THUMB;
                } else if (name.startsWith(Consts.METHOD_GET_FIEL_DWRUL)) {
                    method = Consts.METHOD_GET_FIEL_DWRUL;
                } else {
                    name = Utils.MD5(imageUri);
                }
                if (method != null) {
                    name = name.replace(method + "/", "");
                    if (name.contains("?dim=")) {
                        name = name.replace("?dim=", "_");
                    }
                    if (name.contains(Consts.ACCESS_TOKEN_TAG)) {
                        int index = name.indexOf(Consts.ACCESS_TOKEN_TAG);
                        name = name.substring(0, index);
                    }
                }
//                if (App.DEBUG) {
//                    LogUtil.d(TAG, "imageUri	" + imageUri + "| name=" + name);
//                }
                return name;
            }
        };
        diskCache = DefaultConfigurationFactory.createDiskCache(context, nameGenerator, DISKCACHESIZE, 0);
        loaderConfiguration = UILUtils.getImageLoaderConfiguration(context).diskCache(diskCache).build();
        imageLoader.init(loaderConfiguration);
    }

    public void load(ImageView imageView, FileEntity fileEntity, ImageSize size, DisplayImageOptions imageOptions) {
        // TODO
        if (fileEntity.isUploading() || fileEntity.isNewUpload()) {
            ImageManager.instance().loadLocal(imageView, fileEntity.getFilePath(), imageOptions, null);
        } else {
            ImageManager.instance().load(imageView, fileEntity.getId(), size, false, imageOptions);
        }
    }

    /**
     * if local file does not exists, it will download from server
     */
    public void load(ImageView view, String fileId, ImageSize size) {
        load(view, fileId, size, false, options);
    }

    public void load(ImageView view, String fileId, ImageSize size, boolean justLocal) {
        load(view, fileId, size, justLocal, options);
    }

    /**
     * @param justLocal now is Useless
     */
    public void load(ImageView view, String fileId, ImageSize size, boolean justLocal, DisplayImageOptions imageOptions) {
        if (view == null) {
            return;
        }
        String url = null;
        if (justLocal) {

        } else {
            url = getImageUrl(fileId, size);
            // if (App.DEBUG) {
            // LogUtil.d(TAG, "load()	url=" + url);
            // }
        }
        // if (imageOptions == null) {
        // imageOptions = options;
        // }
        // imageLoader.displayImage(url, view, imageOptions);
        load(view, fileId, size, imageOptions, null, null);
    }

    /**
     * load from fileEntity
     *
     * @param view
     * @param fileEntity
     * @param size
     * @param opt
     * @param loadingListener
     * @param progressListener
     */
    public void load(ImageView view, FileEntity fileEntity, ImageSize size, DisplayImageOptions opt, ImageLoadingListener loadingListener, ImageLoadingProgressListener progressListener) {
        if (fileEntity == null) {
            return;
        }
        if (fileEntity.isUploading()) {
            imageLoader.displayImage(Scheme.FILE.wrap(fileEntity.getFilePath()), view, opt == null ? options : opt, loadingListener, progressListener);
        } else {
            load(view, fileEntity.getId(), size, opt, loadingListener, progressListener);
        }
    }

    public void load(ImageView view, String fileId, ImageSize size, DisplayImageOptions opt, ImageLoadingListener loadingListener, ImageLoadingProgressListener progressListener) {
        if (fileId == null) {
            return;
        }
        String url = getImageUrl(fileId, size);
        if (opt == null) {
            opt = this.options;
        }
        imageLoader.displayImage(url, view, opt, loadingListener, progressListener);
        if (App.DEBUG) {
            LogUtil.d(TAG, "load()	url=" + url + " opt " + opt );
        }
    }

    public String getImageUrl(String fileId, ImageSize size) {
        if (Utils.isEmpty(fileId)) {
            return null;
        }
        if (size != null && size.isThumb()) {
            return ConnectBuilder.getThumbUrl(fileId, size);
        } else {
            return ConnectBuilder.getFileUrl(fileId, true);
        }
    }

    public void cancel(ImageKey key) {

    }

    public void loadLocal(ImageView imageEx, String localPath, DisplayImageOptions option, ImageLoadingListener loadingListener) {
        imageLoader.displayImage(Scheme.FILE.wrap(localPath), imageEx, option == null ? options : option, loadingListener, null);
    }

    /**
     * get avater from original image. avater does not has thumb image in local
     * sdcard.It'll get thumb from a big iamge;
     * <p/>
     * Use @see #getImage(String, ImageSize) instead. Before user make sure set
     * ImageSize#setHasThumbFile(false)
     *
     * @return
     */
    public void loadAvater(ImageView view, String userId) {
        loadAvater(view, userId, true);
    }

    public void loadAvater(ImageView view, String userId, boolean cache) {
        DisplayImageOptions loadOptions;
        if (cache) {
            loadOptions = options;
        } else {
            loadOptions = (new DisplayImageOptions.Builder()).cloneFrom(options).cacheInMemory(false).cacheOnDisk(false).build();
        }
        loadAvater(view, userId, loadOptions);
    }

    public void loadAvater(ImageView view, String userId, DisplayImageOptions option) {
        if (view == null) {
            return;
        }
        String url = ConnectBuilder.getAvaterUrl(userId);
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cloneFrom(options).showImageOnFail(R.drawable.default_image_small);
        option = builder.build();
        imageLoader.displayImage(url, view, option);
    }

    public void loadAvater(ImageView view, String userId, DisplayImageOptions opt, ImageLoadingListener loadingListener) {
        if (view == null) {
            return;
        }
        if (opt == null) {
            opt = options;
        }
        String url = ConnectBuilder.getAvaterUrl(userId);
        DisplayImageOptions.Builder builder = new DisplayImageOptions.Builder();
        builder.cloneFrom(opt).showImageOnFail(R.drawable.default_image_small);
        opt = builder.build();
        imageLoader.displayImage(url, view, opt, loadingListener);
    }


    public void loadBanner(ImageView view, String bannerId) {
        if (view == null) {
            return;
        }
        String url = ConnectBuilder.getBannerUrl(bannerId);
        imageLoader.displayImage(url, view, options);
        if (App.DEBUG) {
            LogUtil.d(TAG, "loadBanner()	" + url);
        }
    }

    public void setAvater(String localPath, String avaterId) {
        FileUploadManager.instance().setAvater(localPath, avaterId);
    }

    public void uploadImage(String localPath, String albumId) {
        FileUploadManager.instance().createFile(localPath, albumId);
    }

    public void uploadImage(List<UploadImage> localPaths, String albumId) {
        // TODO change here from AlbumDetailActivity.java
        if (Utils.isEmpty(localPaths) || Utils.isEmpty(albumId)) {
            return;
        }
        FileUploadManager.instance().createFile(localPaths, albumId);
    }

    /**
     * delete cache/ server
     */
    public void deleteImage(final String fileId) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ConnectBuilder.deleteFile(fileId);
                deleteLocal(fileId);
            }
        };
        TaskRuntime.instance().run(runnable);

        // TODO remove from memory
    }

    // TODO XXX remove image from memory
    public boolean remove(String fileId) {

        return false;
    }

    /**
     * both thumb/original image file in SDCARD will be deleted
     *
     * @param fileId
     * @time Apr 25, 2014
     */
    public void deleteLocal(String fileId) {
        String path = getImageCachePath(fileId);
        FileUtil.delete(path);
        int[][] thuSizes = ThumbSize.Sizes();
        if (thuSizes == null) {
            return;
        }
        for (int[] size : thuSizes) {
            if (size == null || size.length != 2) {
                continue;
            }
            FileUtil.delete(path + "_" + ThumbSize.getSizes(size));
        }
    }

    /**
     * just from memory
     */
    // public Bitmap getBitmap(String uri, ImageView view) {
    // return imageLoader.getBitmap(uri, view);
    // }
    public Bitmap getBitmap(String fileId, ImageSize size, ImageView view) {
        String url = getImageUrl(fileId, size);
        return imageLoader.getBitmap(url, view);
    }

    public Bitmap getBitmapFromCache(String userId, ImageView view) {
        String url = ConnectBuilder.getAvaterUrl(userId);
        return imageLoader.getBitmap(url, view);
    }

    public String save(FileEntity entity) {
        if (entity == null) {
            return null;
        }
        final String path = getImageCachePath(entity.getId());
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

    /**
     * 1. clear all bitmap by call {@link # cleanMemory()}<br>
     * 2. clear all images cached in SDCARD<br>
     */
    public void cleanCache() {
        clearMemory();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                clearDisk();
            }
        };
        TaskRuntime.instance().run(runnable);
    }

    public String getImageCachePath(String fileId, ImageSize size) {
        String name = getImageName(fileId, size);
        return getImageCachePath(name);
    }

    @SuppressWarnings("deprecation")
    public String getImageCachePath(String imageName) {
        if (Utils.isEmpty(imageName)) {
            return null;
        }
        File image = diskCache.getDirectory();
        if (image == null) {
            return null;
        }
        // XXX why *.0
        String imagePath = image + File.separator + imageName + ".0";
        return imagePath;
    }

    public String getImageName(String fileId, ImageSize size) {
        if (fileId == null) {
            return null;
        }
        if (size != null && size.isThumb()) {
            return fileId + "_" + size.toURI();
        }
        return fileId;
    }

    public final void clearMemory() {
        LogUtil.v(TAG, "clearMemoryCache");
        ImageLoader.getInstance().clearMemoryCache();
    }

    public final void clearDisk() {
        ImageLoader.getInstance().clearDiskCache();
    }

    @SuppressWarnings("deprecation")
    public void copyToCache(String srcPath, String fileId) {
        if (fileId == null || srcPath == null) {
            return;
        }
        String realUrl = Consts.HOST_HTTPS + Consts.METHOD_GET_FIEL_DWRUL + "/" + fileId;
        InputStream imageStream = null;
        File srcImageFile = new File(srcPath);
        try {
            imageStream = new FileInputStream(srcImageFile);
            diskCache.save(realUrl, imageStream, null);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            if (imageStream != null) {
                imageStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        srcImageFile.delete();
        // TODO create thumbnail bitmapp
    }

}
