package com.daxiangce123.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.business.event.BulletClickEvent;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileComments;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.pages.PhotoViewerFragment;
import com.daxiangce123.android.ui.view.BulletView;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import de.greenrobot.event.EventBus;

public class BulletManager implements View.OnClickListener {
    private static final String TAG = "BulletManager";
    private static final int MAX_SHOW_IN_SCREEN = 1;
    private static final int MAX_SHOW_IN_VIEW_CACHE = MAX_SHOW_IN_SCREEN + 3;
    // private static final boolean QUERY_FROM_NETWORK = true;

    private Integer bulletInScreen = 0;
    private String requestUUID = "";
    public static boolean showBullet = true;

    private static BulletManager bulletManager;
    private static SharedPreferences settings;
    /**
     * CommentEntity-javabean
     */
    private LinkedHashMap<String, List<CommentEntity>> showQueue;

    /**
     * BulletView-extends LinearLayout
     */
    private ArrayList<BulletView> bulletViews = new ArrayList<BulletView>();
    private DBHelper dbHelper;

    private RelativeLayout container;
//    private BaseCliqActivity context;

    public static int translateY;
    private PhotoViewerFragment photoViewerFragment;
    private boolean showNextBullet = true;

    public boolean isShowNextBullet() {
        return showNextBullet;
    }

    public void setShowNextBullet(boolean showNextBullet) {
        this.showNextBullet = showNextBullet;
        if (showNextBullet) {
            popOneToScreen();
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            //请求响应
            Response response = intent.getParcelableExtra(Consts.RESPONSE);
            ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
            String content;
            if (response == null || info == null || (content = response.getContent()) == null) {
                return;
            }
            if (response.getStatusCode() == 200 && requestUUID.equals(info.getTag2())) {
                onGetComments(content);
            }
        }
    };

    //获取评论信息
    private void onGetComments(String content) {
        List<CommentEntity> serverList;
        FileComments files = Parser.parseFileComments(content);
        if (files == null || Utils.isEmpty(serverList = files.getcomments())) {
            return;
        }
        String fileIdString = serverList.get(0).getObjId();
        synchronized (showQueue) {
            List<CommentEntity> cached = showQueue.get(fileIdString);
            if (cached != null) {
                cached.addAll(serverList);
            } else {
                cached = new LinkedList<CommentEntity>();
                cached.addAll(serverList);
                showQueue.put(fileIdString, cached);
            }
            popAnimation();
        }
    }

    /* ============================================================= */
    /* =======================init component======================== */
    /* ============================================================= */
    private BulletManager() {
        if (settings == null) {
            settings = PreferenceManager.getDefaultSharedPreferences(App.getAppContext());
            showQueue = new LinkedHashMap<String, List<CommentEntity>>();
            readSP();
            initBroadCast();
            translateY = -App.SCREEN_HEIGHT / 10;
        }
    }

    public final static BulletManager instance() {
        if (bulletManager == null) {
            bulletManager = new BulletManager();
        }
        return bulletManager;
    }

    public void initDB(DBHelper dbHelper) {
        if (App.getUid() == null || App.getUid().equals("")) {
            this.dbHelper = new DBHelper("temp");
        } else if (dbHelper != null) {
            this.dbHelper = dbHelper;
        } else {
            this.dbHelper = new DBHelper(App.getUid());
        }
    }

    public void initContainerLayout(RelativeLayout container) {
        this.container = container;
//        this.context = activity;
        resumeShow();
        showQueue.clear();
        bulletInScreen = 0;
        bulletViews.clear();
    }

    private void initBroadCast() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(Consts.GET_COMMENTS);
        Broadcaster.registerReceiver(receiver, filter);
    }

    private void readSP() {
        showBullet = settings.getBoolean(Consts.SHOW_BULLET, true);
    }

	/* ============================================================= */
    /* =======================END component========================= */
    /* ============================================================= */

    public void setShowBullet(boolean showBullet) {
        BulletManager.showBullet = showBullet;
        settings.edit().putBoolean(Consts.SHOW_BULLET, showBullet).commit();
        if (!showBullet) {
            bulletInScreen = 0;
            if (showQueue != null) showQueue.clear();
            if (bulletViews != null) bulletViews.clear();
            if (container != null) container.removeAllViews();
        }
    }

    public void resumeShow() {
        if (showBullet) {
            requestUUID = UUID.randomUUID().toString();
            if (container != null && container.getVisibility() != View.VISIBLE) {
                container.removeAllViews();
                container.setVisibility(View.VISIBLE);
                // DiskCacheUtils.
            }
        } else {
            clear();
        }
    }

    /**
     * invoked when user change fragment
     */
    public void clear() {
        // TODO 1.clear queue
        showQueue.clear();
        // 2.stop all Animation
        bulletInScreen = 0;
        // 3.hide layout

        for (BulletView bulletView : bulletViews) {
            ViewPropertyAnimator.animate(bulletView).cancel();
            bulletView = null;
        }
        if (container != null) {
            container.removeAllViews();
            container.setVisibility(View.GONE);
        }
        bulletViews.clear();
        requestUUID = "";
    }

    public void setFragment(PhotoViewerFragment photoViewerFragment) {
        this.photoViewerFragment = photoViewerFragment;
    }

    public void resetShow() {
        Log.v(BulletManager.TAG, "resetShow()");
        resumeShow();
        showQueue.clear();
        bulletInScreen = 0;
        for (BulletView bulletView : bulletViews) {
            ViewPropertyAnimator.animate(bulletView).cancel();
        }
        bulletViews.clear();
        if (container != null) {
            container.removeAllViews();
        }
    }

    /**
     * add file to show queue
     *
     * @param file
     */
    public void addFile(final FileEntity file) {
        // only add when user want to show bullet

        // if (BaseTabBarFragment.SCROLL_STATE !=
        // OnScrollListener.SCROLL_STATE_IDLE || !showBullet || file == null ||
        // file.getComments() == 0) {
        if (!showBullet || file == null || file.getComments() == 0) {
            return;
        }
        // return;
        final String fileId = file.getId();
        // not contain in showQueue
        if (showQueue.containsKey(fileId)) {
            return;
        } else {
            TaskRuntime.instance().run(new Runnable() {
                @Override
                public void run() {
                    // query db here
                    StringBuffer selection = new StringBuffer(Consts.OBJ_ID);
                    selection.append("='");
                    selection.append(fileId);
                    selection.append("' ORDER BY create_date ASC");
                    List<CommentEntity> cacheList;
                    // if (dbHelper != null) {
                    if (dbHelper == null) {
                        return;
                    }
                    synchronized (dbHelper) {
                        cacheList = dbHelper.getList(CommentEntity.EMPTY, selection.toString());
                        if (cacheList == null) {
                            cacheList = new LinkedList<CommentEntity>();
                            Log.v(BulletManager.TAG, "addFile====query" + fileId + " new List<CommentEntity>");
                        }
                        if (cacheList.size() < file.getComments()) {
                            Log.v(BulletManager.TAG, "addFile----query" + fileId + " queryNetwordk" + cacheList.size());
                            ConnectBuilder.getComments(fileId, 0, file.getComments() - cacheList.size(), requestUUID);
                        }
                    }
                    // } else {
                    // Log.v(BulletManager.TAG, "addFile----query" + fileId +
                    // " queryNetwordk" + cacheList.size());
                    // ConnectBuilder.getComments(fileId, 0, 99, requestUUID);
                    // }

                    // add to queue
                    synchronized (showQueue) {
                        showQueue.put(fileId, cacheList);
                        Log.v(BulletManager.TAG, "addFile+++showQueue.put " + fileId + " size" + cacheList.size());
                    }
                    popAnimation();
                }
            });
        }

    }

    public void popAnimation() {

        TaskRuntime.instance().run(new Task() {
            @Override
            public void run() {
                runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        if (getBulletInScreen() >= MAX_SHOW_IN_SCREEN) {
                            return;
                        } else {
                            popOneToScreen();
                        }
                    }
                });
            }
        });


//        if (context == null) return;
//        context.runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (getBulletInScreen() >= MAX_SHOW_IN_SCREEN) {
//                    return;
//                } else {
//                    popOneToScreen();
//                }
//            }
//        });
    }

    public void popOneToScreen() {
        if (showBullet) {
            CommentEntity commentEntity = deQueueCommentEntity();
            if (commentEntity != null) {
                putToScreen(commentEntity);
            } else {
                Log.v(BulletManager.TAG, "popOneToScreen no more comment");
            }
        }
    }

    public void putToScreen(CommentEntity commentEntity) {
        BulletView viewsBulletView = null;
        if (bulletViews == null || container == null) {
            return;
        }
        int cachedSzie = bulletViews.size();
        if (cachedSzie >= MAX_SHOW_IN_VIEW_CACHE) {
            // find one that not animation
            for (int i = 0; i < bulletViews.size(); i++) {
                if (!(viewsBulletView = bulletViews.get(i)).isShowInscreen) {
                    break;
                }
            }
            Log.v(BulletManager.TAG, "bulletViews.size() > MAX_SHOW_IN_SCREEN use cached view" + commentEntity.getMsg());
        } else {
            RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            layoutParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
            layoutParams.rightMargin = Utils.getDip(5);
            viewsBulletView = new BulletView(App.getAppContext());
            viewsBulletView.setOnClickListener(this);
            if (photoViewerFragment != null && photoViewerFragment.isVisible()) {
                viewsBulletView.setClickable(false);
            } else {
                viewsBulletView.setClickable(true);
            }
            container.addView(viewsBulletView, layoutParams);
            bulletViews.add(viewsBulletView);
        }
        Log.v(BulletManager.TAG, "popOneToScreen +" + commentEntity.getMsg());
        addAnmatingInScreen();
        viewsBulletView.setCommentEntity(commentEntity);
    }

    public CommentEntity deQueueCommentEntity() {
        List<CommentEntity> firstInShowQueue;
        String keyString = null;

        try {
            if (showQueue.keySet().iterator().hasNext()) {
                keyString = showQueue.keySet().iterator().next();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if ((firstInShowQueue = showQueue.get(keyString)) != null) {
            CommentEntity commentEntity = null;
            if (firstInShowQueue.size() == 0) {
                showQueue.remove(keyString);
                return deQueueCommentEntity();
            } else {
                commentEntity = firstInShowQueue.remove(0);
            }
            return commentEntity;
        }
        return null;
    }

    public synchronized int getBulletInScreen() {
        Log.v(BulletManager.TAG, "=====getBulletInScreen: " + bulletInScreen);
        synchronized (bulletInScreen) {
            return bulletInScreen;
        }
    }

    public synchronized void addAnmatingInScreen() {
        Log.v(BulletManager.TAG, "++++++++addAnmatingInScreen: " + bulletInScreen);
        this.bulletInScreen++;
    }

    public synchronized void removeAnmatingInScreen() {
        Log.v(BulletManager.TAG, "-------removeAnmatingInScreen: " + bulletInScreen);
        if (bulletInScreen > 0) {
            this.bulletInScreen--;
        }
    }

    @Override
    public void onClick(View v) {
        Object tagObject;
        if (App.DEBUG) {
            LogUtil.d(TAG, " ---onClick---" + v.getTag());
        }
        if ((tagObject = v.getTag()) instanceof CommentEntity) {
            if (App.DEBUG) {
                LogUtil.d(TAG, " ---onClick---" + tagObject);
            }
            if (ViewHelper.getAlpha(v) > 0.1f) {
                if (App.DEBUG) {
                    LogUtil.d(TAG, " ---onClick---" + ((CommentEntity) tagObject).getMsg());
                }
//                 context.onBulletClick(((CommentEntity) tagObject).getObjId());
                BulletClickEvent event = new BulletClickEvent(((CommentEntity) tagObject).getObjId());
                EventBus.getDefault().post(event);
            }
        }

    }

    public void onListIdle(List<FileEntity> list) {
        resumeShow();
        String firstKey = "";
        // List<CommentEntity> firstList;
        // if (showQueue.keySet().iterator().hasNext()) {
        // firstKey = showQueue.keySet().iterator().next();
        // }
        // firstList = showQueue.get(firstKey);
        // if(firstKey)
        showQueue.clear();
        for (FileEntity fileEntity : list) {
            // if (firstKey.equals(firstKey)) {
            //
            // }
            addFile(fileEntity);
        }
        // bulletViews.clear();

    }

    public void clearAll() {
        clear();
        try {
            photoViewerFragment = null;
//            context = null;
            container = null;
            if (dbHelper != null) {
                dbHelper.close();
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            dbHelper = null;
        }
    }
}
