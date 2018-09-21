package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.nostra13.universalimageloader.core.listener.ImageLoadingProgressListener;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

/**
 * @author
 * @project DaXiangCe
 * @time 2014-5-4
 */
public class PhotoPreview extends FrameLayout {

    public final static String TAG = "PhotoPreview";
    private ImageView ivVideoOverlay;
    protected ZoomImageView zImageView;
    private GifImageView gifView;
    private View llProgress;
    private TextView tvProgress;
    private Bitmap mBitmap;
    private float lastX, lastY;

    private OnClickListener clickListener;
    protected boolean isDeattached;
    protected FileEntity fileEntity;
    private boolean DEBUG = true;
    protected ImageSize pictureSize;
    protected GifDrawable gifDrawable;
    private HashSet<String> downloadGifFileId = new HashSet<String>();

    protected ImageLoadingListener loadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (imageUri == null) {
                return;
            }
            String fileId = fileEntity != null ? fileEntity.getId() : null;
            if ((fileId == null || !imageUri.contains(fileId)) && (fileEntity.isUploading() ? (!imageUri.contains(fileEntity.getFilePath())) : (true))) {
                return;
            }
            if (App.DEBUG) {
                LogUtil.v(TAG, loadedImage.getHeight() + "=h" +
                        loadedImage.getWidth());
            }
            setImageBitmap(loadedImage);
            if (isVideo()) {
                showVideoOverlay(true);
            }
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
        }
    };

    protected ImageLoadingProgressListener progressListener = new ImageLoadingProgressListener() {

        @Override
        public void onProgressUpdate(String imageUri, View view, int current, int total) {
            if (imageUri == null) {
                return;
            }
            String fileId = fileEntity != null ? fileEntity.getId() : null;
            if (fileId == null || !imageUri.contains(fileId)) {
                return;
            }
            int fileSize = fileEntity.getSize();
            if (fileSize <= 0) {
                return;
            }
            int progress = current * 100 / fileSize;
            showProgress(true);
            setProgress(progress);
        }
    };

    private void initView() {
        LayoutInflater.from(getContext()).inflate(R.layout.view_photo_preview, this, true);
        zImageView = (ZoomImageView) findViewById(R.id.ziv_photo_preview);
        ivVideoOverlay = (ImageView) findViewById(R.id.iv_video_overlay);
        gifView = (GifImageView) findViewById(R.id.iv_photo_preview);
        llProgress = findViewById(R.id.ll_loading);
        tvProgress = (TextView) findViewById(R.id.tv_msg_loading);
        zImageView.getLayoutParams().height = App.SCREEN_HEIGHT;
        zImageView.getLayoutParams().width = App.SCREEN_WIDTH;
        float zoomRatio = 0.8f;
        if (App.SCREEN_WIDTH <= 800) {
            zoomRatio = 1.0f;
        }
        pictureSize = new ImageSize((int) (App.SCREEN_WIDTH * zoomRatio), (int) (App.SCREEN_HEIGHT * zoomRatio));
    }

    public PhotoPreview(Context context) {
        this(context, null);
    }

    public PhotoPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView();
    }

    public void setImageBitmap(Bitmap bitmap) {
        zImageView.setImageBitmap(bitmap);
        mBitmap = bitmap;
        showProgress(bitmap == null);
        if (bitmap == null) {
            setProgress(0);
        }
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public boolean hasBitmap() {
        return mBitmap != null;
    }

    public void setImageSize(ImageSize imageSize) {
        pictureSize = imageSize;
    }

    public void setFile(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public boolean isVideo() {
        return MimeTypeUtil.getMime(fileEntity.getMimeType()) == Mime.VID;
    }

    private void showGifView(boolean show) {
        if (show) {
            if (!visiable(gifView)) {
                gifView.setVisibility(View.VISIBLE);
            }
        } else {
            if (visiable(gifView)) {
                release();
            }
        }
    }

    public void releaseGif() {

    }

    public void release() {
        releaseGif();
        gifView.setImageBitmap(null);
        mBitmap = null;
        zImageView.setImageBitmap(null);
    }

    public void showProgress(boolean show) {
        if (show) {
            if (!visiable(llProgress)) {
                llProgress.setVisibility(View.VISIBLE);
            }
        } else {
            if (visiable(llProgress)) {
                llProgress.setVisibility(View.GONE);
            }
        }

        if (mBitmap == null) {
            if (visiable(zImageView)) {
                zImageView.setVisibility(View.GONE);
            }
        } else {
            if (!visiable(zImageView)) {
                zImageView.setVisibility(View.VISIBLE);
            }
        }
    }

    public void setProgress(int progress) {
        if (progress == 100) {
            showProgress(false);
        }
        if (progress > 100) {
            progress = 99;
        }
        if (progress > 0) {
            String msg = getContext().getString(R.string.downloaded_x, progress);
            tvProgress.setText(msg);
        } else {
            tvProgress.setText(R.string.loading);
        }
    }

    public Bitmap showFile(boolean playGif) {
        if (fileEntity == null) {
            return null;
        }
        releaseGif();
        Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());
        if (DEBUG) {
            LogUtil.d(TAG, "------------------------------showMedia------------------------------");
            LogUtil.d(TAG, "showMedia()		mime=" + fileEntity.getMimeType() + "	playGif=" + playGif + "	fileId=" + fileEntity.getId());
        }
        if (mime == Mime.VID) {
            return showVideoThumb();
        } else if (mime == Mime.GIF) {
            return playGif(playGif);
        } else {
            return showImage();
        }
    }

    private Bitmap playGif(boolean playGif) {
        if (DEBUG) {
            LogUtil.v(TAG, "playGif()");
        }
        showVideoOverlay(false);
        setImageBitmap(null);
        showGifView(true);

        if (MimeTypeUtil.getMime(fileEntity.getMimeType()) != Mime.GIF) {
            return null;
        }
        String localPath = null;
        if (fileEntity.isUploading()) {
            localPath = fileEntity.getFilePath();
        } else {
            localPath = ImageManager.instance().getImageCachePath(fileEntity.getId());
        }

        if (FileUtil.exists(localPath)) {
            try {
                if (gifDrawable != null) {
                    gifDrawable.recycle();
                    gifDrawable = null;
                }
                gifDrawable = new GifDrawable(new File(localPath));
                gifView.setImageDrawable(gifDrawable);
                mBitmap = gifDrawable.getThumb();
                if (DEBUG) {
                    LogUtil.v(TAG, "mBitmap = gifDrawable.getThumb(); playGif");
                }
                showProgress(false);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (!fileEntity.isUploading()) {
            showProgress(true);
            downloadGif(localPath);
        }
        return null;
    }

    protected void downloadGif(String path) {
        if (DEBUG) {
            LogUtil.v(TAG, "downloadGif() " + fileEntity.getId());
        }
        if (!downloadGifFileId.contains(fileEntity.getId())) {
            ConnectBuilder.downloadFile(fileEntity.getId(), null, path, fileEntity.getSize(), null);
            downloadGifFileId.add(fileEntity.getId());
        }

    }

    public HashSet<String> getDownloadGifFileId() {
        return downloadGifFileId;
    }

    private void showVideoOverlay(boolean visiable) {
        if (visiable) {
            ivVideoOverlay.setTag(fileEntity);
            if (!visiable(ivVideoOverlay)) {
                ivVideoOverlay.setVisibility(VISIBLE);
            }
        } else {
            ivVideoOverlay.setTag(null);
            if (visiable(ivVideoOverlay)) {
                ivVideoOverlay.setVisibility(GONE);
            }
        }
    }

    private Bitmap showImage() {
        Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());
        if (mime == Mime.IMG || (mime != Mime.VID)) {
            showVideoOverlay(false);
            showGifView(false);
            setImageBitmap(null);
            pictureSize.setThumb(false);
            loadImage();
        }
        return zImageView.getBitmap();
    }

    private Bitmap showVideoThumb() {
        Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());
        if (mime != Mime.VID) {
            return null;
        }
        showVideoOverlay(false);
        showGifView(false);
        setImageBitmap(null);

        pictureSize.setThumb(true);
        loadImage();
        return zImageView.getBitmap();
    }

    protected void loadImage() {
        ImageManager.instance().load(zImageView, fileEntity, pictureSize, null, loadingListener, progressListener);
    }

    private boolean visiable(View v) {
        if (v == null) {
            return false;
        }
        int visiablity = v.getVisibility();
        if (visiablity == VISIBLE) {
            return true;
        }
        return false;
    }

    @Override
    public void setOnClickListener(OnClickListener l) {
        super.setOnClickListener(l);
        this.clickListener = l;
    }

    /**
     * view's tag will be {@link FileEntity}
     */
    public void setOnVideoClickListener(OnClickListener l) {
        ivVideoOverlay.setOnClickListener(l);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getPointerCount() == 1) {
            if (ev.getAction() == MotionEvent.ACTION_MOVE) {
                // TODO
            } else if (ev.getAction() == MotionEvent.ACTION_DOWN) {
                lastX = ev.getX();
                lastY = ev.getY();
                // if (DEBUG) {
                // LogUtil.d(TAG, "	ACTION_DOWN	" + lastX + "/" + lastY);
                // }
            } else if (ev.getAction() == MotionEvent.ACTION_UP) {
                // if (DEBUG) {
                // LogUtil.d(TAG, "	ACTION_UP	" + lastX + "/" + lastY);
                // }
                if (lastX != -1 && lastY != -1) {
                    float deltaX = Math.abs(ev.getX() - lastX);
                    float deltaY = Math.abs(ev.getY() - lastY);
                    // if (DEBUG) {
                    // LogUtil.d(TAG, "	ACTION_UP	" + deltaX + "/" + deltaY);
                    // }
                    if (deltaX <= 10 && deltaY <= 10) {
                        lastX = lastY = -1;

                        if (clickListener != null) {
                            clickListener.onClick(this);
                            return true;
                        }
                    }
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // private boolean resetEvent(MotionEvent ev) {
    // float currentX = ev.getX();
    // float currentY = ev.getY();
    //
    // float deltaX = Math.abs(currentX - lastX);
    // float deltaY = Math.abs(currentY - lastY);
    // if (deltaX <= 10 && deltaY <= 10) {
    // lastX = lastY = -1;
    // return true;
    // }
    // lastX = currentX;
    // lastY = currentY;
    // return false;
    // }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        isDeattached = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        isDeattached = true;
        release();
    }

    public void resetUI() {
        zImageView.getLayoutParams().height = App.SCREEN_HEIGHT;
        zImageView.getLayoutParams().width = App.SCREEN_WIDTH;
        zImageView.resetImage();
    }
}
