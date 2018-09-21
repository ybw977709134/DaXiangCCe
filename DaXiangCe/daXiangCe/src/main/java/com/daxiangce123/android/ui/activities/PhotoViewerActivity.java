package com.daxiangce123.android.ui.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import android.widget.RelativeLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.PhotoViewerFragment;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class PhotoViewerActivity extends BaseCliqActivity {

    protected final static String TAG = "PhotoViewerActivity";
    protected PhotoViewerFragment pictureViewerFragment;
    protected AlbumItemController itemList;
    protected AlbumEntity albumEntity;
    private DBHelper dbHelper;
    protected RelativeLayout bulletContainer;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (Build.VERSION.SDK_INT >Build.VERSION_CODES.JELLY_BEAN) {
//            Utils.setStatusBarVisibility(this, true);
//        }
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
        setContentView(R.layout.activity_photo_viewer);
        bulletContainer = (RelativeLayout) this.findViewById(R.id.rl_bullet);
        initBullet();
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        if ((albumEntity = App.getAlbum(intent.getStringExtra(Consts.ALBUM_ID))) == null) {
            return;
        }
        if ((itemList = App.albumItemController) == null) {
            return;
        }
        pictureViewerFragment = new PhotoViewerFragment();
        pictureViewerFragment.setCurPosition(intent.getIntExtra(Consts.POSITION, 0));
        pictureViewerFragment.setFileList(itemList);
        pictureViewerFragment.setIsJoined(intent.getBooleanExtra(Consts.IS_JOINED, true));
        pictureViewerFragment.setAlbum(albumEntity);
        pictureViewerFragment.show(this);
        BulletManager.instance().resetShow();
        BulletManager.instance().setFragment(pictureViewerFragment);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        clear();
        BulletManager.instance().clearAll();
        LogUtil.v(TAG, "onDestory ~");
    }

    private void initBullet() {
        BulletManager.instance().initDB(getDbHelper());
        BulletManager.instance().initContainerLayout(bulletContainer);
    }

    private DBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(App.getUid());
        }
//        if (itemList != null) {
//            itemList.setDbHelper(dbHelper);
//        }
        return dbHelper;
    }


    private void clear() {
        if (pictureViewerFragment != null) {
            pictureViewerFragment.clear();
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        LogUtil.v(TAG, "onStop ~");
        App.resizeScreenSize(true);
    }
}