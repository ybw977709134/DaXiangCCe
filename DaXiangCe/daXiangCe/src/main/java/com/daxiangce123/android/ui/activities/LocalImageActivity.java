package com.daxiangce123.android.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.listener.CapturePictureListener;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.LocalImageFolderFragment;
import com.daxiangce123.android.util.CapturePic;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;

import java.io.File;
import java.util.ArrayList;

/**
 * @author ram
 * @project Cliq
 * @time Mar 8, 2014
 */
public class LocalImageActivity extends BaseCliqActivity {

    public static final int CHOOSE_TYPE_IMAGE = 0;
    public static final int CHOOSE_TYPE_AVATAR = 1;
    public static final int CHOOSE_TYPE_QRCODE = 2;

    private final static String TAG = "LocalImageActivity";
    private LocalImageFolderFragment folderFragment;
    private int maxChoosen = 100;
    private boolean needVideo = true;
    private boolean disablePreview = false;
    private ArrayList<UploadImage> destPaths;
    private boolean DEBUG = true;
    private String imageTakePathString;
    private CapturePictureListener capturePictureListener = new CapturePictureListener() {

        @Override
        public void capturePicture() {
            imageTakePathString = MediaUtil.getDestSaveDir() + "/camera-" + TimeUtil.formatTime(System.currentTimeMillis(), Consts.CAPTURE_IMAGE_TIME_FORMAT) + ".jpg";
            CapturePic.capturePic(LocalImageActivity.this, imageTakePathString);

        }
    };
    private int type;

    // private String albumId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        // App.addActivity(this);
        setContentView(R.layout.activity_empty);
        Intent previousIntent = getIntent();
        // TODO this judgement is not need //by Hansen
        if (previousIntent.hasExtra(Consts.MAX_CHOOSEN)) {
            maxChoosen = previousIntent.getIntExtra(Consts.MAX_CHOOSEN, maxChoosen);
        }
        if (previousIntent.hasExtra(Consts.NEED_VIDEO)) {
            needVideo = previousIntent.getBooleanExtra(Consts.NEED_VIDEO, true);
        }
        disablePreview = previousIntent.getBooleanExtra(Consts.DISABLE_PHOTO_PREVIEW, false);
        type = previousIntent.getIntExtra(Consts.TYPE, CHOOSE_TYPE_IMAGE);


        // if (intent.hasExtra(Consts.ALBUM_ID)) {
        // albumId = intent.getStringExtra(Consts.ALBUM_ID);
        // }
        showLocalFolders();
    }

    private void showLocalFolders() {
        if (folderFragment == null) {
            folderFragment = new LocalImageFolderFragment();
        }
        folderFragment.setMaxChoosen(maxChoosen, needVideo, disablePreview, type);
        folderFragment.setCapturePictureListener(capturePictureListener);
        folderFragment.show(this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (DEBUG) {
            LogUtil.d(TAG, "onActivityResult	" + requestCode + " " + resultCode);
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode != Consts.REQUEST_CODE_CAMERA_IMAGE) {
            return;
        }
        try {
            if (imageTakePathString != null && (new File(imageTakePathString)).exists()) {
                destPaths = new ArrayList<>();
                destPaths.add(new UploadImage(imageTakePathString, true));
                Utils.scanNewMedia(imageTakePathString);
                // ImageManager.instance().uploadImage(destPaths, albumId);
                // destPaths.clear();
                onResult();
                finish();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean onResult() {
        Intent intent = getIntent();
        intent.putExtra(Consts.PATH_LIST, destPaths);
        setResult(Activity.RESULT_OK, intent);
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    // public void onActivityResult(int requestCode, int resultCode, Intent
    // data) {
    // LogUtil.d(TAG, "onActivityResult	requestCode=" + requestCode
    // + "	resultCode=" + resultCode);
    // try {
    // LogUtil.d(
    // TAG,
    // "onActivityResult	uri	"
    // + data.getParcelableExtra(MediaStore.EXTRA_OUTPUT));
    // } catch (Exception e) {
    // LogUtil.d(TAG, "Exception	" + e.getMessage());
    // e.printStackTrace();
    // }
    // if (resultCode != Activity.RESULT_OK) {
    // return;
    // }
    // if (requestCode != Consts.REQUEST_CODE_CHOOSE_IMAGE) {
    // return;
    // }
    // // if (Utils.isEmpty(destPaths)) {
    // // return;
    // // }
    // try {
    // // ImageManager.instance().uploadImage(destPaths, albumId);
    // // destPaths.clear();
    // // onResult();
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // super.onActivityResult(requestCode, resultCode, data);
    // }

}
