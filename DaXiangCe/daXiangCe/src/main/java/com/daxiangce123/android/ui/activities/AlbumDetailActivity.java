package com.daxiangce123.android.ui.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.FileSort;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.business.event.BulletClickEvent;
import com.daxiangce123.android.business.event.FileDeleteEvent;
import com.daxiangce123.android.business.event.Signal;
import com.daxiangce123.android.core.SingleTaskRuntime;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.Batch;
import com.daxiangce123.android.data.Batches;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.SimpleAlbumItems;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OnDetialListener;
import com.daxiangce123.android.listener.OnFileOptionListener;
import com.daxiangce123.android.listener.OnPullListener;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.HttpTimeOutManger;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.manager.UploadCancelManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.AlbumDetailFragment;
import com.daxiangce123.android.ui.pages.AlbumTimeLineFragment;
import com.daxiangce123.android.ui.pages.MemberFragment;
import com.daxiangce123.android.ui.pages.ModifyAlbumSettingsFragment;
import com.daxiangce123.android.ui.pages.NewMemberFragment;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.pages.base.BaseTabBarFragment;
import com.daxiangce123.android.ui.view.AlbumBottomBar;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.ui.view.PreviewDialog;
import com.daxiangce123.android.ui.view.ShareDialog;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.ui.view.TransferOwnerPopup;
import com.daxiangce123.android.ui.view.TransferOwnerPopup.OnTransfrerOwnerClicked;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.ShareImageCreateUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import de.greenrobot.event.EventBus;

public class AlbumDetailActivity extends BaseCliqActivity implements OnClickListener, OptionListener, OnTimeLineHeaderActionListener {

    public static final String TAG = "AlbumDetailActivity";
    protected final static int MSG_REFRESH_COMPLETE = 123;

    // private ImageView btnMoreOption = null;
    protected AlbumEntity albumEntity;
    protected String albumId;
    protected RelativeLayout rlSetting;
    protected TextView tvSetting;
    protected TextView tvAlbumReport;
    protected RelativeLayout rlBack;
    protected TextViewParserEmoji mTitle;
    protected Batches batches;
    private AlbumBottomBar bottomBar;

    protected TextView mShowTimeLine, mShowMembers;

    protected ArrayList<BaseTabBarFragment> baseTabBarList = new ArrayList<BaseTabBarFragment>();

    protected UserInfo ownerInfo;

    // private UploadFileDialog uploadFileDialog;
    private LinearLayout llBottomBtn;
    // protected LinearLayout ivBottomBtn;
    protected RelativeLayout rlJoinAlbum, rlUploadPhoto;
    protected LinearLayout tabBar, dayNightViews;
    protected FrameLayout avatorLayout;
    // private TextView tvCancel;
    private TextView tvSelectAll;
    private RelativeLayout rlCancel;
    private RelativeLayout rlDelete;
    private RelativeLayout rlSelectAll;
    private ImageView redDotImageView = null;
    private ShareDialog shareDialog;
    private OptionDialog sortDialog;
    protected OnFileOptionListener fileOptionListener = null;
    // private OptionDialog moreDialog;
    protected AlbumDetailFragment detailFragment;
    protected AlbumTimeLineFragment timeLineFragment;
    private ModifyAlbumSettingsFragment modifyAlbumSettingsFragment;
    protected RelativeLayout mAlbumDetail;
    protected RelativeLayout bulletContainer;
    private MemberFragment mMemberFragment;
    private NewMemberFragment mNewMemberFragment;
    private TransferOwnerPopup mTransferOwnerPopup;
    // private CDialog albumDetail;

    private DBHelper dbHelper;

    private int lastSort = 0;
    protected boolean DEBUG = true;
    private boolean isSelectMode;
    private MemberEntity curMember;
    private boolean hasPassword;
    protected boolean isRefreshing;
    private PullToRefreshBase<?> pullRefreshView;
    private PreviewDialog previewDialog;

    protected boolean canBeShared = true;
    protected int curSize = 0, updateCount = 0;
    private String status;
    private LinearLayout uploadGuide;
    private LinearLayout shareGuide;
    private LinearLayout uploadShareGuide;
    protected LinearLayout bulletGuide;

    private int deleteCounter = 0;
    private long unAuthLastShowTime = 0;
    private int waitForBatchLength = 0;
    private boolean showNewUserGuide;

    protected AlbumItemController albumList;

    protected OnPullListener onPullListener = new OnPullListener() {
        @Override
        public void onPullUpToRefresh(PullToRefreshBase<?> refreshView, int pageSize) {
            pullRefreshView = refreshView;
            if (albumList.isLoadingMore()) {
                sendMessage(MSG_REFRESH_COMPLETE, 50);
                return;
            }
            if (isDeleteMode()) {
                sendMessage(MSG_REFRESH_COMPLETE, 50);
                return;
            }
            if (isDeleting()) {
                sendMessage(MSG_REFRESH_COMPLETE, 50);
                return;
            }
            loadMore();
        }

        @Override
        public void onPullDownToRefresh(PullToRefreshBase<?> refreshView, int limit) {
            pullRefreshView = refreshView;
            if (isDeleteMode()) {
                sendMessage(MSG_REFRESH_COMPLETE, 50);
                return;
            }
            if (isDeleting()) {
                sendMessage(MSG_REFRESH_COMPLETE, 50);
                return;
            }
            reLoadAlbumItemsFromNet();
        }
    };

    protected OnDetialListener detailListener = new OnDetialListener() {

        @Override
        public boolean onFileClicked(int position) {
            if (albumList.isEmpty()) {
                return false;
            }
            return viewImageDetail(position);
        }


        @Override
        public boolean onFileClicked(FileEntity entity) {
            //this is never use
            if (entity == null || Utils.isEmpty(albumList.getFileList())) {
                return false;
            }

            int position = albumList.positionOf(entity);
            return viewImageDetail(position);
        }

        @Override
        public boolean onDelete(Collection<FileEntity> entities) {
            return deleteFiles(entities);
        }

        @Override
        public boolean onDisplayAllSelected(boolean select) {
            return showAllSelected(select);
        }

        public boolean onUpload() {
            return showUpload();
        }

        public boolean onFileLongClicked(FileEntity entity) {
            previewFile(entity);
            return false;
        }
    };

    /**
     * 点击打开评论所对应的照片
     */
    public void onBulletClick(String fileId) {
        //TODO 需要调整弹幕 打开方式
        fileOptionListener.sortAlbumList();
        int position = albumList.positionOf(fileId);
        viewImageDetail(position);
        if (getBottomBtnShow()) {
            llBottomBtn.setVisibility(View.GONE);
        }

    }


    protected boolean deleteFiles(Collection<FileEntity> entities) {
        if (entities == null) {
            return false;
        }
        final ArrayList<FileEntity> collection = new ArrayList<FileEntity>();
        collection.addAll(entities);
        Runnable runnable = new Runnable() {
            public void run() {
                try {
                    for (FileEntity entity : collection) {
                        if (entity.isActive()) {
                            if (deleteCounter < 0) {
                                deleteCounter = 0;
                            }
                            deleteCounter++;
                            ConnectBuilder.deleteFile(entity.getId());

                        } else if (entity.isUploading()) {
                            onFileCancel(entity);
                        }
                    }
                    if (DEBUG) {
                        LogUtil.v(TAG, "deleteCounter=" + deleteCounter);
                    }
                    collection.clear();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        onCancelDelete();
        TaskRuntime.instance().run(runnable);
        setBottomBarState(true, false);
        llBottomBtn.setVisibility(View.GONE);
        return true;
    }

    protected boolean showAllSelected(boolean select) {
        isSelectMode = select;
        if (!isSelectMode) {
            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
        } else {
            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle_click);
        }
        return true;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Consts.GET_EVENTS.equals(action)) {
                    List<Event> events = intent.getParcelableArrayListExtra(Consts.EVENT_LIST);
                    onEvent(events);
                } else {
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);
                    ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                    if (response == null) {
                        return;
                    }
                    if (DEBUG) {
                        LogUtil.d(TAG, "onReceive	" + info.getType() + "	" + response.getStatusCode());
                    }
                    String content = response.getContent();
                    if (Consts.GET_BATCHES.equals(action)) {
                        onGetBatches(info, response);
                    } else if (Consts.CREATE_FILE.equals(action)) {
                        handleFileCreate(response, info);
                    } else if (Consts.UPLOAD_FILE.equals(action)) {
                        int sCode = response.getStatusCode();
                        if (sCode == 200) {
                            FileEntity fileEntity = Parser.parseFile(info.getTag3());
                            onFileUploadDone(fileEntity);
                        } else if (sCode == 401 || sCode == 403) {
                            FileEntity fileEntity = Parser.parseFile(info.getTag3());
                            onUnAuthFileCreate(info, fileEntity);
                        }
                    } else if (Consts.GET_ALBUM_UPDATE_ITEMS.equals(action)) {
                        if (!albumId.equals(info.getTag())) {
                            return;
                        }
                        onGetUpdateItems(info, response);
                    } else if (Consts.GET_ALBUM_ITEMS.equals(action)) {
                        if (!albumId.equals(info.getTag())) {
                            return;
                        }
                        onGetItems(info, response);
                        // LoadingDialog.dismiss();
                        sendMessage(MSG_REFRESH_COMPLETE, 50);
                    } else if (Consts.GET_ALBUM_INFO.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            onGetAlbumInfo(response);
                        }
                    } else if (Consts.GET_USER_INFO.equals(action)) {
                        if (App.DEBUG) {
                            LogUtil.d(TAG, " GET_USER_INF--Oresponse : " + response);
                        }
                        if ((ownerInfo != null) && (ownerInfo.getId().equals(albumEntity.getOwner()))) {
                            return;
                        }
                        if (albumEntity == null) {
                            return;
                        }

                        if (response.getErrCode() == ErrorCode.TIME_OUT) {
                            ConnectBuilder.getUserInfo(albumEntity.getOwner());
                        } else if (response.getStatusCode() == 200) {
                            UserInfo user = Parser.parseUserInfo(content);
                            if (user == null) {
                                return;
                            }
                            if (albumEntity.getOwner().equals(user.getId())) {
                                ownerInfo = user;
                                notificateFragmentTitleChanged();
                            }
                        }
                    } else if (Consts.UPDATE_ALBUM.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            updateAlbum(info);
                            CToast.showToast(R.string.modify_album_settings_succeed);
                        }
                    } else if (Consts.SET_MEMBER_ROLE.equals(action)) {
                        onRoleChanged(response, info);
                    } else if (Consts.DELETE_FILE.equals(action)) {
                        int code = response.getStatusCode();
                        if (code == 200 || code == 404) {
                            String fileId = info.getTag();
                            onFileDelete(fileId, true);
                        }
                    } else if (Consts.GET_MEMBER_ROLE.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            onGetMemerEntity(response);
                        }
                    } else if (Consts.CHECK_ALBUM_ACCESS_CONTROL.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            hasPassword = Parser.parseHasPasswd(content);
                            if (hasPassword) {
                                canBeShared = false;
                            } else {
                                canBeShared = true;
                            }
                        }
                    } else if ((Consts.DELETE_ALBUM).equals(action)) {
                        if (response.getStatusCode() == 200) {
                            String albumId = info.getTag();
                            if (albumId.equals(albumEntity.getId())) {
                                finish();
                            }
                        }
                    } else if (Consts.LEAVE_ALBUM.equals(action)) {
                        if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
                            String albumId = info.getTag();
                            if (albumId.equals(albumEntity.getId())) {
                                finish();
                            }
                        }
                    } else if (Consts.NO_PUSH.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            onPushChanged(response, info);
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public void onGetBatches(ConnectInfo info, Response response) {
        try {
            if (info.getTag().equals(albumId)) {
                if (response.getStatusCode() == 200) {
                    batches = Parser.parseBatches(response.getContent());
                    int limit = 0;
                    if (batches != null && batches.getBatches() != null) {
                        for (Batch batch : batches.getBatches()) {
                            limit += batch.getSize();
                            if (limit > fileOptionListener.getPageSize()) {
                                break;
                            }
                        }
                    } else {
                        limit = 50;
                    }
                    waitForBatchLength = limit;
                    ConnectBuilder.getAlbumItems(albumId, 0, limit, FileSort.TIMELINE_SORT, Order.DESC);
                    if (DEBUG) {
                        LogUtil.d(TAG, "onGetBatches()	curSize < albumEntity.getSize() cS=" + curSize + "    limit=" + limit);
                    }
                    isRefreshing = true;
                } else {
                    handle404(info, response);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        super.onCreate(savedInstanceState);
        initBroadCast();
        setContentView(R.layout.album_activity);
        initCompontent();
        showTimeLine(false, true);
        status = AppData.getStatus();
        showUserGuide();
        showBulletGuide();
        HttpTimeOutManger.instance().register(Consts.GET_ALBUM_ITEMS);
        initData();
    }

    @Override
    public void onResume() {
        super.onResume();
        initBullet();
        sendMessage(MSG_REFRESH_COMPLETE, 50);
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        if (what == MSG_REFRESH_COMPLETE) {
            if (pullRefreshView != null) {
                pullRefreshView.onRefreshComplete();
            }
        }
    }

    private DBHelper getDbHelper() {
        if (dbHelper == null) {
            dbHelper = new DBHelper(App.getUid());
        }
        if (albumList != null) {
            albumList.setDbHelper(dbHelper);
        }
        return dbHelper;
    }

    private void initCompontent() {
        // btnMoreOption.setOnClickListener(this);
        rlBack = (RelativeLayout) this.findViewById(R.id.rl_back);
        rlBack.setOnClickListener(this);
        redDotImageView = (ImageView) this.findViewById(R.id.tv_red_dot);
        rlSetting = (RelativeLayout) this.findViewById(R.id.rl_settings);
        tvSetting = (TextView) this.findViewById(R.id.title_album_setting);
        tvAlbumReport = (TextView) this.findViewById(R.id.title_album_report);
        rlSetting.setOnClickListener(this);
        tvAlbumReport.setOnClickListener(this);
        mTitle = (TextViewParserEmoji) this.findViewById(R.id.title_album_detail_text);
        mAlbumDetail = (RelativeLayout) this.findViewById(R.id.rl_album_detail);
        mAlbumDetail.setOnClickListener(this);
        bulletContainer = (RelativeLayout) this.findViewById(R.id.rl_bullet);
        llBottomBtn = (LinearLayout) this.findViewById(R.id.ll_btn_panel);

        tvSelectAll = (TextView) this.findViewById(R.id.tv_all_circle);
        rlCancel = (RelativeLayout) this.findViewById(R.id.rl_cancel);
        rlCancel.setOnClickListener(this);
        rlDelete = (RelativeLayout) this.findViewById(R.id.rl_delete);
        rlDelete.setOnClickListener(this);
        rlSelectAll = (RelativeLayout) this.findViewById(R.id.rl_all);
        rlSelectAll.setOnClickListener(this);
        mShowTimeLine = (TextView) this.findViewById(R.id.tv_showtimeline);
        mShowTimeLine.setOnClickListener(this);
        mShowMembers = (TextView) this.findViewById(R.id.tv_showmembers);
        mShowMembers.setOnClickListener(this);
        tabBar = (LinearLayout) this.findViewById(R.id.ll_tab_bar);
        dayNightViews = (LinearLayout) this.findViewById(R.id.ll_date_indexer);
        avatorLayout = (FrameLayout) this.findViewById(R.id.fl_time_avater_outer);
        bottomBar = (AlbumBottomBar) this.findViewById(R.id.iv_btn_panel);
        bottomBar.setOnTimeLineHeaderActionListener(this);

        uploadGuide = (LinearLayout) this.findViewById(R.id.ll_user_guide_upload);
        uploadGuide.setOnClickListener(this);
        shareGuide = (LinearLayout) this.findViewById(R.id.ll_user_guide_share);
        shareGuide.setOnClickListener(this);
        uploadShareGuide = (LinearLayout) this.findViewById(R.id.ll_upload_share_guide);
        uploadShareGuide.setOnClickListener(this);

        bulletGuide = (LinearLayout) this.findViewById(R.id.ll_bullet_guide);
        bulletGuide.setOnClickListener(this);

        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Consts.ALBUM_ID)) {
            albumId = bundle.getString(Consts.ALBUM_ID);
            App.albumItemController = new AlbumItemController(albumId);
            albumList = App.albumItemController;
        }
        ConnectBuilder.checkAlbumAccessControl(albumId);
        ConnectBuilder.getMemberRole(albumId, App.getUid());
    }

    private void initBroadCast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_ALBUM_ITEMS);
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.UPDATE_ALBUM);
        ift.addAction(Consts.SET_MEMBER_ROLE);
        ift.addAction(Consts.DELETE_FILE);
        ift.addAction(Consts.UPLOAD_FILE);
        ift.addAction(Consts.GET_MEMBER_ROLE);
        ift.addAction(Consts.CHECK_ALBUM_ACCESS_CONTROL);
        ift.addAction(Consts.GET_EVENTS);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.DELETE_ALBUM);
        ift.addAction(Consts.SHARED_ALBUM);
        ift.addAction(Consts.NO_PUSH);
        ift.addAction(Consts.GET_ALBUM_UPDATE_ITEMS);
        ift.addAction(Consts.CREATE_FILE);
        ift.addAction(Consts.GET_ALBUM_INFO);
        ift.addAction(Consts.GET_BATCHES);
        Broadcaster.registerReceiver(receiver, ift);
        EventBus.getDefault().register(this);
    }

    private void showUserGuide() {
        // uploadGuide.setVisibility(View.VISIBLE);
        if (status == null) {
            return;
        }
//        if (status.equals(Consts.STATUS_CREATE)) {
//            uploadGuide.setVisibility(View.VISIBLE);
//        } else if (status.equals(Consts.STATUS_EXISTED)) {
//            uploadGuide.setVisibility(View.GONE);
//        }
        if (status.equals(Consts.STATUS_CREATE) || showNewUserGuide) {
            uploadGuide.setVisibility(View.VISIBLE);
        } else {
            uploadGuide.setVisibility(View.GONE);
        }
    }

    private void showBulletGuide() {
        if (Utils.isEmpty(AppData.getToken())) {
            if (!AppData.getFirstOpenAlbum()) {
                bulletGuide.setVisibility(View.VISIBLE);
                // AppData.hasOpenAlbum(true);
                AppData.setFirstOpenAlbum(true);
            }
        } else {
            if (!AppData.getFirstOpenAlbum() && status.equals(Consts.STATUS_EXISTED)) {
                bulletGuide.setVisibility(View.VISIBLE);
                AppData.setFirstOpenAlbum(true);
            }
        }
    }

    /**
     * this function must be called after showFragment,(that means
     * fileOptionListener should be null)
     */
    private void initData() {
        deleteCounter = 0;
        TaskRuntime.instance().run(new Task() {
            @Override
            public void run() {
                try {
                    readAlbumAndFetch();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                runOnUI(new Runnable() {
                    @Override
                    public void run() {
                        onGetItemsEnd();
                        if (albumEntity != null) {
                            updateTitleBar(false);
                            notificateFragmentTitleChanged();
                            updateRedDotView();
                        }
                    }
                });
            }
        });
    }

    private void initBullet() {
        BulletManager.instance().initDB(getDbHelper());
        BulletManager.instance().initContainerLayout(bulletContainer);
    }

    protected AlbumEntity getAlbum() {
        if (Utils.isEmpty(albumId)) {
            return null;
        }
        dbHelper = getDbHelper();
        if (dbHelper == null) {
            return null;
        }
        if (App.DEBUG) {
            LogUtil.v(TAG, "dbHelper.getAlbum(albumId)");
        }
        return dbHelper.getAlbum(albumId);
    }

    /**
     * run in thread
     * <p/>
     * get {@link AlbumEntity} from db
     * <p/>
     * If current file size is smaller than size in {@link AlbumEntity}, load
     * more files from server
     */
    protected void readAlbumAndFetch() {
        // LoadingDialog.show(R.string.loading);
        int repect = 0;
        while (repect < 4) {
            albumEntity = getAlbum();
            if (albumEntity == null) {
                repect++;
                try {
                    Thread.sleep(1500);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                repect = 5;
            }
        }

        if (albumEntity == null) {
            CToast.showToast(R.string.album_has_been_delete);
            finish();
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "readAlbumAndFetch()	query album	DONE");
        }
        ConnectBuilder.getUserInfo(albumEntity.getOwner());
        ConnectBuilder.getAlbumInfo(albumEntity.getId());
        if (DEBUG) {
            LogUtil.d(TAG, "readAlbumAndFetch()	ConnectBuilder.getUserInfo");
        }
        List<FileEntity> dbCacheList = albumList.readDbAlbumList();
        if (DEBUG) {
            LogUtil.d(TAG, "readAlbumAndFetch()	query file list	DONE	size:" + Utils.sizeOf(dbCacheList));
        }
        curSize = Utils.sizeOf(dbCacheList);

        if (curSize > 0) {
            albumList.add(dbCacheList, false, true, false);
            if (DEBUG) {
                LogUtil.d(TAG, "readAlbumAndFetch()	files.addAll(dbCacheList); size:" + Utils.sizeOf(albumList.getFileList()));
            }
        } else {
            // no file in db so request batch
            ConnectBuilder.getBatcheList(albumId, 0, 45);
            return;
        }

        updateCount = albumEntity.getUpdateCount();
        if (updateCount != 0) {
            // step1 . reset updateCount
            albumEntity.setUpdateCount(0);
            dbHelper.execute("UPDATE " + albumEntity.getTableName() + " SET " + Consts.UPDATE_COUNT + "=0 WHERE " + Consts.ALBUM_ID + "=\"" + albumEntity.getId() + "\"");
            // step2 . request update image
            ConnectBuilder.getAlbumUpdateItems(albumId, 0, updateCount, FileSort.TIMELINE_SORT, Order.DESC);
            if (DEBUG) {
                LogUtil.d(TAG, "readAlbumAndFetch()	updateCount != 0 updateCount=" + updateCount);
            }
            return;
        }
        if (curSize < albumEntity.getSize()) {
            ConnectBuilder.getAlbumItems(albumId, curSize, fileOptionListener.getPageSize(), FileSort.TIMELINE_SORT, Order.DESC);
            if (DEBUG) {
                LogUtil.d(TAG, "readAlbumAndFetch()	curSize < albumEntity.getSize() cS=" + curSize);
            }
            isRefreshing = true;
        }
    }

    protected void reLoadAlbumItemsFromNet() {
        sendMessage(MSG_REFRESH_COMPLETE, 500);
        if (isSelectMode) {
            return;
        }
        if (isRefreshing) {
            if (DEBUG) {
                LogUtil.d(TAG, "onPullDownToRefresh	loading abort reload!");
            }
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "onPullDownToRefresh	loadAlbumItems");
        }
        if (albumList.getFileSort() == FileSort.TIMELINE_SORT) {
            ConnectBuilder.getBatcheList(albumId, 0, 45);
        } else {
            ConnectBuilder.getAlbumItems(albumId, 0, fileOptionListener.getPageSize(), albumList.getFileSort(), Order.DESC);
        }
        isRefreshing = true;
    }

    /**
     * onEventMainThread is callback used in EventBus
     *
     * @param signal
     */
    public void onEventMainThread(Signal signal) {
        switch (signal.action) {
            case Signal.ALBUM_CONTROLLER_LOAD_MORE_EMPTY:
//                CToast.showToast(R.string.no_more);
                sendMessage(MSG_REFRESH_COMPLETE, 150);
                if (fileOptionListener.isShowTabBar()) {
                    fileOptionListener.setShowBottomBar(false);
                    setBottomBarState(false, true);
                }
                break;
            case Signal.ALBUM_CONTROLLER_LOAD_MORE_OK:
                sendMessage(MSG_REFRESH_COMPLETE, 150);
                onGetItemsEnd();
                break;
        }
    }

    public void onEventMainThread(BulletClickEvent signal) {
        onBulletClick(signal.fileId);
    }

    public void loadMore() {
        albumList.loadMore(null);
    }

    private void onGetUpdateItems(ConnectInfo info, Response response) {
        if (info == null || response == null) {
            return;
        }
        String content = response.getContent();
        SimpleAlbumItems items = Parser.parseSimpleAlbumItems(content);
        if (items == null) {
            return;
        }
        showUpdateItems(response);
        if (!items.hasMore()) {
            return;
        }
        if (curSize < albumEntity.getSize()) {
            ConnectBuilder.getAlbumItems(albumId, updateCount + curSize, fileOptionListener.getPageSize(), FileSort.TIMELINE_SORT, Order.DESC);
            isRefreshing = true;
        }
    }

    protected boolean onGetItems(ConnectInfo info, Response response) {
        handle404(info, response);
        isRefreshing = false;
        if (info == null || response == null) {
            return true;
        }
        String content = response.getContent();
        SimpleAlbumItems items = Parser.parseSimpleAlbumItems(content);
        if (items == null) {
            return true;
        }
        int startPos = 0;
        Object obj = info.getObj();
        if (obj instanceof Integer) {
            startPos = (Integer) obj;
        }
        if (albumList.isEmpty() || startPos == 0) {
            //indicate that is start form new
            //从未加入状态变成加入状态 请求来的不再显示。
            if (!albumList.isJoinedChangeFlag()) {
                if (!albumList.isUploading()) {
                    albumList.setForceClearFlag(true);
                }
                showItems(response);
            }
        } else if ((fileOptionListener != null) && (fileOptionListener.getCurrentLoadFileCount() < 45)) {
            // when the size of showing <45 show more items
            showItems(response);
            showItems(response);
        }
        if (App.DEBUG) {
            LogUtil.v(TAG, "onGetItems getAlbumItems +oldStartPos" + startPos + "   items=" + items.getSize() + "hasMore=" + items.hasMore());
        }
        if (items.hasMore() && (!albumList.isChangeSortAndClearFlag())) {

            startPos = startPos + items.getSize();
            isRefreshing = true;
            ConnectBuilder.getAlbumItems(albumId, startPos, 100, albumList.getFileSort(), Order.DESC);

        } else {
            isRefreshing = false;
            return true;
        }
        return false;
    }

//    private void checkResponseOnRefresh(Response response) {
//        if (waitForBatchLength != 0) {
//            showItems(response);
//        } else {
//            showItems(response);
//        }
//    }

    /**
     * show new items that is new from server not in db cache
     *
     * @param response
     */
    protected void showUpdateItems(Response response) {
        AlbumSamples item = Parser.parseAlbumSamples(response.getContent(), false);
        if (item == null || item.getFiles() == null) {
            return;
        }
        List<FileEntity> responseList = item.getFiles();
        albumList.add(responseList, true, false, true);
        onGetItemsEnd();
    }

    protected void showItems(final Response response) {
        SingleTaskRuntime.instance().run(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                AlbumSamples item = Parser.parseAlbumSamples(response.getContent(), false);
                if (item == null || item.getFiles() == null) {
                    return;
                }
                LinkedList<FileEntity> serverList = item.getFiles();
                albumList.showItems(serverList);
                if (DEBUG) {
                    LogUtil.v(TAG, "========showItems use" + (System.currentTimeMillis() - start));
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onGetItemsEnd();
                    }
                });
            }
        });
    }

    private synchronized void onEvent(List<Event> lists) {
        if (Utils.isEmpty(lists)) {
            return;
        }
        for (Event event : lists) {
            if (event == null) {
                continue;
            }
            handleEvent(event);
        }
    }

    private void handleEvent(Event event) {
        if (event == null) {
            return;
        }
        String type = event.getOpType();
        if (Consts.FILE_CREATED.equals(type)) {
            FileEntity fileEntity = (FileEntity) event.getObject();
            onFileUpload(fileEntity);
            // } else if (Consts.MEMBER_JOINED.equals(type)) {
            // } else if (Consts.AVATAR_UPDATED.equals(type)) {
        } else if (Consts.ALBUM_DELETED.equals(type) || Consts.SYSTEM_ALBUM_DELETED.equals(type)) {
            onAlbumDeleted(event.getObject());
        } else if (Consts.MEMBER_LEFT.equals(type)) {
            MemberEntity memberEntity = (MemberEntity) event.getObject();
            if ((memberEntity.getUserId()).equals(App.getUid()) && memberEntity.getAlbumId().equals(albumEntity.getId())) {
                onMemberLeft(memberEntity);
            }
        } else if (Consts.FILE_DELETED.equals(type) || Consts.SYSTEM_FILE_DELETED.equals(type)) {
            FileEntity fileEntity = (FileEntity) event.getObject();
            String fileId = fileEntity.getId();
            onFileDelete(fileId, false);
        } else if (Consts.MEMBER_JOINED.equals(type)) {
            MemberEntity memberEntity = (MemberEntity) event.getObject();
            onMemberJoined(memberEntity);
        } else if (Consts.MEMBER_UPDATED.equals(type)) {
            MemberEntity memberEntity = (MemberEntity) event.getObject();
            onMemberUpdated(memberEntity);
        } else if (Consts.ALBUM_UPDATED.equals(type)) {
            AlbumEntity albumEntity = (AlbumEntity) event.getObject();
            updateAlbum(albumEntity);
        } else if (Consts.LIKE_CREATED.equals(type)) {
            LikeEntity likeEntity = (LikeEntity) event.getObject();
            FileEntity fileEntity = albumList.get(likeEntity.getObjId());
            if (fileEntity != null) {
                fileEntity.setLikes(fileEntity.getLikes() + 1);
            }
        } else if (Consts.LIKE_DELETED.equals(type)) {
            LikeEntity likeEntity = (LikeEntity) event.getObject();
            FileEntity fileEntity = albumList.get(likeEntity.getObjId());
            if (fileEntity != null) {
                fileEntity.setLikes(fileEntity.getLikes() - 1);
            }
        } else if (Consts.COMMENT_CREATED.equals(type)) {
            CommentEntity commentEntity = (CommentEntity) event.getObject();

            FileEntity fileEntity = albumList.get(commentEntity.getObjId());
            if (fileEntity != null) {
                fileEntity.setComments(fileEntity.getComments() + 1);
            }
        } else if (Consts.COMMENT_DELETED.equals(type)) {
            CommentEntity commentEntity = (CommentEntity) event.getObject();

            FileEntity fileEntity = albumList.get(commentEntity.getObjId());
            if (fileEntity != null) {
                fileEntity.setComments(fileEntity.getComments() - 1);
            }
        } else if (Consts.FILE_SHARED.equals(type)) {
            FileEntity file = (FileEntity) event.getObject();

            FileEntity fileEntity = albumList.get(file.getId());
            if (fileEntity != null) {
                fileEntity.setShares(fileEntity.getShares() + 1);
            }

        } else if (Consts.FILE_DOWNLOADED.equals(type)) {
            FileEntity file = (FileEntity) event.getObject();

            FileEntity fileEntity = albumList.get(file.getId());
            if (fileEntity != null) {
                fileEntity.setDownloads(fileEntity.getDownloads() + 1);
            }
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "handleEvent() type=" + event.getOpType() + " srcDevice=" + event.getSrcDevice() + " uid=" + App.getUid());
        }
    }

    protected void onMemberLeft(Object member) {
        if (!(member instanceof MemberEntity)) {
            return;
        }
        CToast.showToast(R.string.u_r_not_member_of_this_album);
        finish();
    }

    public void onMemberJoined(MemberEntity memberEntity) {
        if (memberEntity != null && memberEntity.getAlbumId().equals(albumId)) {
            albumEntity.setMembers(albumEntity.getMembers() + 1);
            if (mMemberFragment != null) {
                mMemberFragment.refreshData();
            }
        }
        updateRedDotView();
        updateTitleBarContent();
    }

    public void onMemberUpdated(MemberEntity memberEntity) {
        if (memberEntity == null) {
            return;
        }
        if ((memberEntity.getUserId()).equals(App.getUid())) {
            curMember = memberEntity;
            if (Consts.OWNER.equals(memberEntity.getRole())) {
                albumEntity.setOwner(curMember.getUserId());
            }
            if (mMemberFragment != null) {
                mMemberFragment.refreshData();
            }
            updateRedDotView();
            updateTitleBarContent();
        }
    }

    private void onAlbumDeleted(Object album) {
        if (!(album instanceof AlbumEntity)) {
            return;
        }
        if (((AlbumEntity) album).getId().equals(albumId)) {
            CToast.showToast(R.string.album_has_been_delete);
            finish();
        }
    }

    public void updateAlbum(AlbumEntity entity) {
        if (entity == null) {
            return;
        }
        if (entity.getId().equals(albumId)) {
            albumEntity = entity;
            updateTitleBar(false);
            int permissions = albumEntity.getPermissions();
            curMember.setPermissions(permissions);
            //TODO 加入postEvent
//            pictureViewerFragment.canLike(albumEntity);
        }
        updateTitleBarContent();
    }

    public void updateAlbum(ConnectInfo connectInfo) {
        HttpEntity entity = connectInfo.getEntity();
        String id = connectInfo.getTag();
        if (!id.equals(albumId)) {
            return;
        }
        try {
            if (entity instanceof StringEntity) {
                String entityStr = EntityUtils.toString(entity);
                JSONObject jo = JSONObject.parseObject(entityStr);

                String name = jo.getString(Consts.NAME);
                String note = jo.getString(Consts.NOTE);
                boolean isPrivate = jo.getBoolean(Consts.IS_PRIVATE);
                boolean commentOff = jo.getBooleanValue(Consts.COMMENT_OFF);
                boolean likeOff = jo.getBoolean(Consts.LIKE_OFF);
                boolean isLocked = jo.getBoolean(Consts.IS_LOCKED);
                if (jo.containsKey(Consts.PERMISSIONS)) {
                    JSONArray ja = jo.getJSONArray(Consts.PERMISSIONS);
                    int permissions = 0;
                    for (int i = 0; i < ja.size(); i++) {
                        String per = ja.getString(i);
                        if (Utils.isEmpty(per)) {
                            continue;
                        }
                        per = per.trim().toLowerCase(Locale.ENGLISH);
                        if ("read".equals(per)) {
                            permissions = permissions | Consts.IO_PERMISSION_R;
                        } else if ("write".equals(per)) {
                            permissions = permissions | Consts.IO_PERMISSION_W;
                        }
                        albumEntity.setPermissions(permissions);
                    }
                }
                albumEntity.setName(name);
                albumEntity.setNote(note);
                albumEntity.setIsPrivate(isPrivate);
                albumEntity.setCommentOff(commentOff);
                albumEntity.setLikeOff(likeOff);
                albumEntity.setIsLocked(isLocked);
                updateTitleBar(false);
                updateTitleBarContent();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onGetMemerEntity(Response response) {
        if (response == null) {
            return;
        }
        MemberEntity member = Parser.parseMember(response.getContent());
        if (member != null && member.getUserId().equals(App.getUid())) {
            curMember = member;
        }
    }

    protected void onGetAlbumInfo(Response response) {
        if (response == null || albumEntity == null) {
            return;
        }
        AlbumEntity albumInfo = Parser.parseAlbum(response.getContent());
        if (albumInfo.getId().equals(albumEntity.getId())) {
            if (albumInfo.getSize() > albumEntity.getSize()) {
                ConnectBuilder.getAlbumUpdateItems(albumId, 0, albumInfo.getSize() - albumEntity.getSize(), FileSort.TIMELINE_SORT, Order.DESC);
            }
            albumEntity.setSize(albumInfo.getSize());
            updateTitleBarContent();
        }
    }

    private void onUnAuthFileCreate(ConnectInfo info, FileEntity fileEntity) {
        String album;
        String fileId;
        if (fileEntity != null) {
            album = fileEntity.getAlbum();
            fileId = Utils.createEntityHashId(fileEntity);
        } else {
            album = Parser.parseAlbumId(info.getTag());
            fileId = Parser.parseFileFakeId(info.getTag());
        }
        if (!albumId.equals(album)) {
            return;
        }
        if (Utils.isEmpty(fileId)) {
            return;
        }
        if (albumList.isEmpty()) {
            return;
        }
        albumEntity.setSize(albumEntity.getSize() - 1);
        FileEntity file = albumList.remove(fileId);
        if (fileOptionListener != null && file != null) {
            fileOptionListener.onFileDeleted(file);
        }
        if (System.currentTimeMillis() - unAuthLastShowTime > (5 * Consts.MIN_IN_MILLS)) {
            CToast.showToast(R.string.close_member_upload_file);
            unAuthLastShowTime = System.currentTimeMillis();
        }
        curMember.setPermissions(Consts.IO_PERMISSION_R);
    }

    private void onFileDelete(String fileId, boolean changeConter) {
        if (Utils.isEmpty(fileId)) {
            return;
        }
        if (changeConter) {
            deleteCounter--;
        }
        int position = albumList.positionOf(fileId);
        FileEntity file = albumList.remove(fileId);
        if (position >= 0 && file != null) {
            FileDeleteEvent event = new FileDeleteEvent(fileId, position);
            EventBus.getDefault().post(event);
            albumEntity.setSize(albumEntity.getSize() - 1);
        }
        if (fileOptionListener != null && file != null) {
            fileOptionListener.onFileDeleted(file);
        }
        updateTitleBarContent();
        if (albumList.isEmpty()) {
            albumEntity.setSize(0);
            albumEntity.setLocalCover(null);
            albumEntity.setThumbFileId(null);
        } else {
            albumEntity.setSize(albumList.size());
            albumEntity.setLocalCover(albumList.get(0).getId());
        }
        HashSet<String> keys = new HashSet<String>();
        keys.add(Consts.COVER);
        albumEntity.setUpdateKey(keys);
        albumEntity.setLocalCover(albumList.get(0).getId());
        dbHelper.update(albumEntity);

    }

    /**
     * transfer album call back to this
     *
     * @param response
     * @param info
     */
    private void onRoleChanged(Response response, ConnectInfo info) {
        if (response == null || info == null) {
            return;
        }
        if (response.getStatusCode() == 200) {
            String jsonStr = info.getTag();
            JSONObject jo = JSONObject.parseObject(jsonStr);
            String album = jo.getString(Consts.ALBUM_ID);
            if (!album.equals(albumId)) {
                return;
            }
            if (jo.containsKey(Consts.PERMISSIONS)) {
                return;
            }
            String owner = jo.getString(Consts.USER_ID);
            albumEntity.setOwner(owner);
            if (mTransferOwnerPopup != null) {
                mTransferOwnerPopup.updateOwner(owner);
            }
            if (timeLineFragment != null) {
                timeLineFragment.setAlbum(albumEntity);
            }
            if (detailFragment != null) {
                detailFragment.setAlbum(albumEntity);
            }
            if (mMemberFragment != null) {
                mMemberFragment.setAlbumEntity(albumEntity);
                mMemberFragment.refreshData();
            }
            ConnectBuilder.getUserInfo(albumEntity.getOwner());
            updateTitleBarContent();

        }
    }

    protected void onGetItemsEnd() {
        if (fileOptionListener == null || albumEntity == null) {
            return;
        }
        if (!(albumEntity.getSize() != 0 && albumList.isEmpty())) {
            fileOptionListener.onItemEnd(albumEntity);
        }
        EventBus.getDefault().post(new Signal(Signal.ALBUM_DETAIL_ACTIVITY_LOAD_MORE_OK));
    }

    private void onPushChanged(Response response, ConnectInfo info) {
        JSONObject jo = JSONObject.parseObject(info.getTag2());
        boolean noPush = jo.getBooleanValue(Consts.NO_PUSH);
        albumEntity.setNoPush(noPush);
        // updateMoreOptionData();
        // if (moreDialog != null && moreDialog.isShowing()) {
        // moreDialog.notificationChanged();
        // }

    }

    @Override
    public void onBackPressed() {
        if (llBottomBtn.isShown()) {
            isSelectMode = false;
//            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
            onCancelDelete();
            return;
        }
        if (mNewMemberFragment != null && mNewMemberFragment.isVisible()) {
            showMembers();
            updateTitleBar(false);
            return;
        }
        if (bulletGuide.isShown()) {
            bulletGuide.setVisibility(View.GONE);
            return;
        }

        if (AppData.getStatus().equals(Consts.STATUS_CREATE)) {
            AppData.setStatus(Consts.STATUS_EXISTED);
        }

        if (showNewUserGuide) {
            AppData.showNewUserGuide(false);
        }

        super.onBackPressed();
    }

    private boolean isDeleteMode() {
        if (llBottomBtn.getVisibility() == ViewGroup.VISIBLE) {
            return true;
        }
        return false;
    }

    private void onBackButton() {
        if (mNewMemberFragment != null && mNewMemberFragment.isVisible()) {
            showMembers();
            updateTitleBar(false);
        } else {
            finish();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.rl_back) {
            onBackButton();
        } else if (id == R.id.rl_settings) {
            if (tvSetting.isShown()) {
                showModifySetting();
            } else {
                return;
            }

        } else if (id == R.id.rl_delete) {
            UMutils.instance().diyEvent(ID.EventRemoveFile);
            isSelectMode = false;
//            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
            if (fileOptionListener != null) {
                fileOptionListener.deleteFile();
            }
        } else if (id == R.id.rl_album_detail) {
            // showAlbumInfo();
        } else if (id == R.id.rl_cancel) {
            isSelectMode = false;
//            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
            onCancelDelete();
        } else if (id == R.id.rl_delete) {

        } else if (id == R.id.ll_user_guide_upload) {
            showUpload();
            uploadGuide.setVisibility(View.GONE);
            // shareGuide.setVisibility(View.VISIBLE);
            UMutils.instance().diyEvent(ID.EventTourPressUploadPhotos);
        } else if (id == R.id.ll_user_guide_share) {
            onShareClicked(true);
            shareGuide.setVisibility(View.GONE);
            UMutils.instance().diyEvent(ID.EventTourPressShareAlbum);
            AppData.setStatus(Consts.STATUS_EXISTED);
        } else if (id == R.id.ll_upload_share_guide) {
            onShareClicked(false);
            uploadShareGuide.setVisibility(View.GONE);
        } else if (id == R.id.ll_bullet_guide) {
            dismissBullet();
        } else if (id == R.id.rl_all) {
            isSelectMode = !isSelectMode;
            if (fileOptionListener != null) {
                fileOptionListener.selectAll(isSelectMode);
                if (!isSelectMode) {
                    tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
                } else {
                    tvSelectAll.setBackgroundResource(R.drawable.select_all_circle_click);
                }
            }
        } else if (id == R.id.tv_showmembers) {
            showMembers();
        } else if (id == R.id.tv_showtimeline) {
            showAlbumPhotos();
        } else if (id == R.id.iv_qr_album_detail) {
            generatorShareImage();
        } else if (id == R.id.title_album_report) {
            reportAlbumDialog();
        }
    }

    public void dismissBullet() {
        bulletGuide.setVisibility(View.GONE);
    }

    @Override
    public void onDeleteClicked() {
        if (fileOptionListener != null) {
            boolean select = fileOptionListener.showDeleteMode();
            if (select) {
                if (isDeleteMode()) {
                    return;
                } else {
                    llBottomBtn.setVisibility(View.VISIBLE);
                    setBottomBarState(false, false);
                    bottomBar.setVisibility(View.GONE);
                }
            } else {
                CToast.showToast(R.string.no_file_to_delete);
            }
        }
    }

    private void onCancelDelete() {
        tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
        llBottomBtn.setVisibility(View.GONE);
        bottomBar.setVisibility(View.VISIBLE);
        setBottomBarState(true, false);
        if (detailFragment != null && fileOptionListener == detailFragment) {
            detailFragment.setDetailListener(detailListener);
            detailFragment.setCancelSelect();
            detailFragment.setAlbumData();
            detailFragment.setOnPullListener(onPullListener);

        } else if (timeLineFragment != null && fileOptionListener == timeLineFragment) {
            timeLineFragment.setDetailListener(detailListener);
            timeLineFragment.setCancelSelect();
            timeLineFragment.setAlbumData();
            timeLineFragment.setOnPullListener(onPullListener);
        }
    }

    @Override
    public void onSortClicked() {
        // if (sortDialog == null) {
        ArrayList<Integer> mDatas = new ArrayList<>();
        mDatas.add(R.string.timeline_sort);
        mDatas.add(R.string.comments_sort);
        mDatas.add(R.string.like_sort);
        mDatas.add(R.string.cancel);

        sortDialog = new OptionDialog(this);
        sortDialog.setOptionListener(this);
        sortDialog.setData(mDatas);
        sortDialog.setTitle(getResources().getString(R.string.selecte_file_sort));
        sortDialog.setSelected(lastSort);
        sortDialog.show();
    }


    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (optionId != R.string.cancel) {
            lastSort = position;
        }
        if (optionId == R.string.timeline_sort) {
            UMutils.instance().diyEvent(ID.EventSortFileByDate);
            showTimeLine(true, false);
        } else if (optionId == R.string.comments_sort) {
            UMutils.instance().diyEvent(ID.EventSortFileByComment);
            showSortDetailFragment(FileSort.COMMENTS_SORT, true);
        } else if (optionId == R.string.like_sort) {
            UMutils.instance().diyEvent(ID.EventSortFileByLike);
            showSortDetailFragment(FileSort.LIKE_SORT, true);
        } else if (optionId == R.string.delete_album) {
            deleteAlbum();
        } else if (optionId == R.string.quit_album) {
            leaveAlbum();
        } else if (optionId == R.string.report_album) {
            reportAlbum();
        }
    }

    public void deleteAlbum() {
        AlertDialog.Builder deleteAlbum = new AlertDialog.Builder(this);
        deleteAlbum.setTitle(getString(R.string.confirm_to_delete_album_x, albumEntity.getName()));
        deleteAlbum.setMessage(R.string.delete_album_prompt);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ConnectBuilder.deleteAlbum(albumEntity.getId());
                    UMutils.instance().diyEvent(ID.EventRemoveAlbum);
                } else {

                }
                dialog.cancel();
            }
        };
        deleteAlbum.setPositiveButton(R.string.confirm, listener);
        deleteAlbum.setNegativeButton(R.string.cancel, listener);
        deleteAlbum.show();
    }

    public void leaveAlbum() {
        AlertDialog.Builder leaveAlbum = new AlertDialog.Builder(this);
        leaveAlbum.setTitle(getString(R.string.confirm_to_quit_album_x, albumEntity.getName()));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ConnectBuilder.leaveAlbum(albumEntity.getId(), App.getUid());
                } else {

                }
                dialog.cancel();
            }
        };
        leaveAlbum.setPositiveButton(R.string.confirm, listener);
        leaveAlbum.setNegativeButton(R.string.cancel, listener);
        leaveAlbum.show();
    }

    public void reportAlbumDialog() {
        final AlertDialog.Builder reportAlbum = new AlertDialog.Builder(this);
        reportAlbum.setTitle(R.string.confirm_to_report_album);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    reportAlbum();
                } else {

                }
                dialog.cancel();
            }
        };
        reportAlbum.setPositiveButton(R.string.report_album, listener);
        reportAlbum.setNegativeButton(R.string.cancel, listener);
        reportAlbum.show();
    }

    public void reportAlbum() {
        JSONObject jo = new JSONObject();
        jo.put(Consts.OBJ_TYPE, Consts.ALBUM);
        jo.put(Consts.OBJ_ID, albumEntity.getId());
        ConnectBuilder.postReport(jo.toString());
        CToast.showToast(R.string.we_ll_handle_report_later);
    }

    protected void showSortDetailFragment(FileSort fileSort, boolean refreshData) {
        if (detailFragment == null) {
            detailFragment = new AlbumDetailFragment();
            detailFragment.setDetailListener(detailListener);
            detailFragment.setAlbum(albumEntity);
            detailFragment.setOnPullListener(onPullListener);
            detailFragment.setOwnerInfo(ownerInfo);
            detailFragment.setAlbumActivityActionListener(this);
            baseTabBarList.add(detailFragment);
            detailFragment.setFileList(albumList);
        }
        fileOptionListener = detailFragment;
        albumList.setOnFileOptionListener(detailFragment);
        albumList.setFileSortAndPreReload(fileSort);
        detailFragment.setSort(fileSort);
        showOnly(detailFragment);
        BulletManager.instance().resumeShow();
        updateRedDotView();
        if (!detailFragment.isShowTabBar()) setTabBarVisibilty(false);
        if (refreshData) {
            reLoadAlbumItemsFromNet();
        }
    }


    public void showTransferOwnerPopup(OnTransfrerOwnerClicked transfrerOwnerClickedListener) {
        if (mTransferOwnerPopup == null) {
            mTransferOwnerPopup = new TransferOwnerPopup(this);
        }
        mTransferOwnerPopup.setData(albumId);
        mTransferOwnerPopup.setTransfrerOwnerClickedListener(transfrerOwnerClickedListener);
        mTransferOwnerPopup.showAtLocation(this.findViewById(R.id.album_detail), Gravity.BOTTOM, 0, 0);
    }

    /**
     * open modify Setting fragment
     */
    private void showModifySetting() {
        if (isDeleteMode()) {
            // llBottomBtn.setVisibility(View.GONE);
//            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
            onCancelDelete();
        }
        if (albumEntity == null) {
            return;
        }
        modifyAlbumSettingsFragment = new ModifyAlbumSettingsFragment();
        modifyAlbumSettingsFragment.setAlbum(albumEntity);
        modifyAlbumSettingsFragment.show(this);
        BulletManager.instance().clear();
        setTabBarVisibilty(false);
        setBottomBarState(false, false);

    }

    protected boolean showUpload() {
        if (!App.getUid().equals(albumEntity.getOwner())) {
            if (curMember == null) {
                return false;
            }
            if (curMember.getPermissions() == Consts.IO_PERMISSION_R) {
                CToast.showToast(R.string.close_member_upload_file);
                return false;
            }
        }
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM_ID, albumId);
        intent.setClass(this, LocalImageActivity.class);
        this.startActivityForResult(intent, Consts.REQUEST_CODE_CHOOSE_IMAGE);

        if (albumList.isEmpty()) {
            UMutils.instance().diyEvent(ID.EventUploadNow);
        } else {
            UMutils.instance().diyEvent(ID.EventUpload);
        }
        return true;
    }

    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("onActivityResult", "req=" + requestCode + " res=" + resultCode);

        if (resultCode != RESULT_OK) {
//            status = AppData.getStatus();
//            if (status.equals(Consts.STATUS_CREATE)) {
//                uploadGuide.setVisibility(View.VISIBLE);
//            } else if (status.equals(Consts.STATUS_EXISTED)) {
//                uploadGuide.setVisibility(View.GONE);
//            }
            showNewUserGuide = AppData.getShowNewUserGuide();
            if (status.equals(Consts.STATUS_CREATE) || showNewUserGuide) {
                uploadGuide.setVisibility(View.VISIBLE);
            } else {
                uploadGuide.setVisibility(View.GONE);
            }

            AppData.setFirstUpload(false);
            return;
        }

        if (requestCode == Consts.REQUEST_CODE_CHOOSE_IMAGE)

        {
            if (!data.hasExtra(Consts.PATH_LIST)) {
                return;
            }
            try {
                List<UploadImage> paths = (List<UploadImage>) data.getExtras().get(Consts.PATH_LIST);
                onUploadListReceived(paths);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (status == null) {
                return;
            }
            if (showNewUserGuide) {
                showShareGuide();
            } else {
                shareGuide.setVisibility(View.GONE);
                showUploadShareGuide();
            }
        }

    }

    private void showShareGuide() {
        if (!AppData.getHasShowShareGuide()) {
            shareGuide.setVisibility(View.VISIBLE);
            AppData.hasShowShareGuide(true);
        } else {
            shareGuide.setVisibility(View.GONE);
        }

    }

    private void showUploadShareGuide() {
        if (AppData.getFirstUpload()) {
            uploadShareGuide.setVisibility(View.VISIBLE);
            AppData.hasShowUploadShareGuide(true);
        }
    }

    /**
     * create fake file Entities and save to db
     *
     * @param images
     */
    public void onUploadListReceived(List<UploadImage> images) {
        if (Utils.isEmpty(images)) {
            return;
        }
        try {
            // S1. create batchID & seqNum then write into upload images
            UploadImage.initUploadImage(images);
            // S2. create fake fileEntities upload into ADA so that these image
            // can be shown in that
            ArrayList<FileEntity> fileEntities = Utils.createFakeFileEntities(albumEntity, images);
            onFileUploading(fileEntities);
            // S3. create fake intent send to event service to create db
            Intent intent = new Intent(Consts.UPLOADING_FILE);
            intent.putExtra(Consts.REQUEST, new ConnectInfo(""));
            intent.putExtra(Consts.RESPONSE, new Response());
            intent.putExtra(Consts.UPLOADING_FILE, fileEntities);
            Broadcaster.sendBroadcast(intent);
            // S4. upload file to server
            ImageManager.instance().uploadImage(images, albumId);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * //     is the same object in {@link AlbumDetailFragment} or
     * {@link AlbumTimeLineFragment}
     * <p/>
     * No need to Add fileEntity int files in in {@link AlbumDetailFragment} or
     * {@link AlbumTimeLineFragment}
     */
    private void onFileUpload(FileEntity fileEntity) {
        if (fileEntity == null || albumEntity == null) {
            return;
        }
        if (!albumId.equals(fileEntity.getAlbum())) {
            return;
        }
        if (albumList.remove(fileEntity.getId()) == null) {
            albumList.addFile(fileEntity, true, true);
            albumEntity.setSize(albumEntity.getSize() + 1);
            if (fileOptionListener != null) {
                fileOptionListener.onFileUpload(fileEntity);
            }
        }
        updateTitleBar(false);
        updateTitleBarContent();

    }

    private void onFileUploading(List<FileEntity> entities) {
        for (FileEntity fileEntity : entities) {
            onFileUpload(fileEntity);
        }
    }

    /**
     * on real fileEntity recived
     *
     * @param fileEntity
     */
    private void onFileUploadDone(FileEntity fileEntity) {
        if (UploadCancelManager.sharedInstance().checkFileEntity(fileEntity)) {
            return;
        }
        if (fileEntity.getAlbum().equals(albumId)) {
            fileEntity.setStatus(Consts.FILEENTITY_STATUS_ACTIVE);
            String fakeFileId = Utils.createEntityHashId(fileEntity);
            // remove old FE
            FileEntity fakeFile = albumList.get(fakeFileId);
            if (fakeFile != null) {
                fakeFile.clone(fileEntity);
                albumList.updateHash(fakeFileId, fakeFile);
                // notify fileOptionListener
                if (fileOptionListener != null) {
                    fileOptionListener.onFileUploadDone(fakeFile);
                }
                albumEntity.setLocalCover(fileEntity.getId());
                if (App.DEBUG) {
                    LogUtil.d(TAG, " ---- cover + onFileUploadDone ---- " + fileEntity.getId());
                }
                albumEntity.setSize(albumList.size());
                albumEntity.setModDate(TimeUtil.getCurrentUTCTime());
                HashSet<String> keys = new HashSet<String>();
                keys.add(Consts.COVER);
                albumEntity.setUpdateKey(keys);
                albumEntity.setLocalCover(fileEntity.getId());
                try {
                    albumEntity.update(dbHelper.getWritableDatabase(), false);
                    dbHelper.getWritableDatabase().close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void handleFileCreate(Response response, ConnectInfo info) {
        String content = response.getContent();
        int sCode = response.getStatusCode();
        if (sCode == 200) {
            FileEntity fileEntity = Parser.parseFile(content);
            if (fileEntity.isActive()) {
                onFileUploadDone(fileEntity);
            } else {
                // change file id
                if (UploadCancelManager.sharedInstance().checkFileEntity(fileEntity)) {
                    return;
                }
                if (fileEntity.getAlbum().equals(albumId)) {
                    String fakeFileId = Utils.createEntityHashId(fileEntity);
                    FileEntity fakeFile = albumList.get(fakeFileId);
                    if (fakeFile != null) {
                        fakeFile.setId(fileEntity.getId());
                    }
                }
            }
        } else if (sCode == 401 || sCode == 403) {
            onUnAuthFileCreate(info, null);
        }
    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        if (fragment == null) {
            return false;
        }
        if (fragment instanceof OnFileOptionListener) {
            fragment.setTitleBarVisibility(View.VISIBLE);
            fragment.setBoottomBarVisibility(View.VISIBLE);
        } else {
            fragment.setTitleBarVisibility(View.GONE);
            fragment.setBoottomBarVisibility(View.GONE);
        }
        return super.showFragment(fragment);
    }

    @Override
    protected void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        HttpTimeOutManger.instance().unRegister(Consts.GET_ALBUM_ITEMS);
        if (dbHelper != null) {
            dbHelper.close();
        }
        if (albumList != null) {
            albumList.releaseAll();
        }
        BulletManager.instance().clearAll();
        super.onDestroy();
    }


    protected boolean viewImageDetail(int position) {
        if (position < 0) {
            position = 0;
        } else {
            int size = albumList.size();
            position = position >= size ? size - 1 : position;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, " --viewImageDetail-- + position " + position);
        }
        App.putAlbum(albumEntity);
        App.albumItemController = albumList;
        Intent intent = new Intent(this, PhotoViewerActivity.class);
        intent.putExtra(Consts.IS_JOINED, albumList.isJoined());
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.POSITION, position);
        startActivity(intent);
        return true;
    }

    protected void previewFile(FileEntity entity) {
        if (entity == null) {
            return;
        }
        if (previewDialog == null) {
            previewDialog = new PreviewDialog();
        }
        previewDialog.show(entity);
    }

    public void showMembers() {
        if (isDeleteMode()) {
            // llBottomBtn.setVisibility(View.GONE);
//            tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
            onCancelDelete();
        }
        mShowMembers.setTextColor(0xff0794e1);
        mShowMembers.setBackgroundResource(R.drawable.files_in_album_corners_bg);
        mShowTimeLine.setTextColor(0xFFFFFFFF);
        mShowTimeLine.setBackgroundResource(R.drawable.transparent);

        if (mMemberFragment == null) {
            mMemberFragment = new MemberFragment();
            mMemberFragment.setAlbumEntity(albumEntity);
            mMemberFragment.setIsJoined(albumList.isJoined());
            mMemberFragment.setData(ownerInfo);
            baseTabBarList.add(mMemberFragment);
            // if (DEBUG) {
            // LogUtil.d(TAG, " token :" + AppData.getToken() + " albumId " +
            // albumEntity.getId());
            // }
            // if (App.DEBUG) {
            // LogUtil.d(TAG, "showMembers -> ownerInfo" + ownerInfo);
            // }
        }
        setBottomBarState(false, true);
        showOnly(mMemberFragment);
        BulletManager.instance().clear();
        updateRedDotView();
        if (!mMemberFragment.isShowTabBar()) setTabBarVisibilty(false);
    }

    public void showAlbumPhotos() {
        mShowTimeLine.setTextColor(0xff0794e1);
        mShowTimeLine.setBackgroundResource(R.drawable.files_in_album_corners_bg);
        mShowMembers.setTextColor(0xFFFFFFFF);
        mShowMembers.setBackgroundResource(R.drawable.transparent);
        if (lastSort == 0) {
            showTimeLine(false, false);
        } else if (lastSort == 1) {
            showSortDetailFragment(FileSort.COMMENTS_SORT, false);
        } else if (lastSort == 2) {
            showSortDetailFragment(FileSort.LIKE_SORT, false);
        }
    }

    public void showTimeLine(boolean refreshData, boolean firstStart) {
        if (timeLineFragment == null) {
            timeLineFragment = new AlbumTimeLineFragment();
            timeLineFragment.setDetailListener(detailListener);
            timeLineFragment.setAlbum(albumEntity);
            timeLineFragment.setOwnerInfo(ownerInfo);
            timeLineFragment.setOuterTabView(tabBar);
            timeLineFragment.setOnPullListener(onPullListener);
            timeLineFragment.setFileList(albumList);
            baseTabBarList.add(timeLineFragment);
        }
        fileOptionListener = timeLineFragment;
        albumList.setOnFileOptionListener(timeLineFragment);
        if (!firstStart) {
            albumList.setFileSortAndPreReload(FileSort.TIMELINE_SORT);
        }
        showOnly(timeLineFragment);
        BulletManager.instance().resumeShow();
        updateRedDotView();
        if (!timeLineFragment.isShowTabBar()) {
            setTabBarVisibilty(false);
        }
        if (refreshData && albumEntity != null && fileOptionListener != null) {
            fileOptionListener.onItemEnd(albumEntity);
            reLoadAlbumItemsFromNet();
        }
    }

    public void updateRedDotView() {
        if (albumEntity == null) {
            return;
        }
        if (albumEntity.getOldMembers() != 0 && albumEntity.getMembers() > albumEntity.getOldMembers() && albumEntity.getMembers() != 1) {
            redDotImageView.setVisibility(View.VISIBLE);
        } else {
            redDotImageView.setVisibility(View.INVISIBLE);
        }
        for (BaseTabBarFragment item : baseTabBarList) {
            item.updateRedDotView();
        }
    }

    public void updateTitleBar(boolean showNew) {
        if (albumEntity != null && !showNew) {
            rlSetting.setVisibility(View.VISIBLE);
            mTitle.setEmojiText(albumEntity.getName());
        }
        if (showNew) {
            rlSetting.setVisibility(View.INVISIBLE);
            mTitle.setText(getString(R.string.new_member));
        }
    }

    public void setTabBarVisibilty(boolean visible) {
        if (visible && (tabBar.getVisibility() != View.VISIBLE)) {
            tabBar.setVisibility(View.VISIBLE);
        } else if ((!visible) && (tabBar.getVisibility() != View.INVISIBLE)) {
            tabBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onShareClicked(boolean isGuide) {
        // if (!canBeShared) return;
        if (shareDialog == null) {
            shareDialog = new ShareDialog();
        }
        shareDialog.setQrClickListener(this);
        shareDialog.show(albumEntity, null, getString(R.string.share_album_to), (ownerInfo != null ? ownerInfo.getName() : ""));
        shareDialog.setIsGuide(isGuide);

        UMutils.instance().diyEvent(ID.EventAlbumDetailsPressShareAlbum);
    }

    @Override
    public void onQRCodeClicked() {
        // showAlbumInfo();
    }

    protected void notificateFragmentTitleChanged() {
        updateTitleBarContent();
    }

    public boolean showOnly(BaseFragment fragment) {
        if (!(fragment instanceof AlbumTimeLineFragment || fragment instanceof MemberFragment || fragment instanceof AlbumDetailFragment)) {
            setTabBarVisibilty(false);
            setBottomBarState(false, false);
        }
        return super.showOnly(fragment);
    }

    /**
     * set bottom bat state
     */
    public void setBottomBarJoinedState() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                bottomBar.setJoined(albumList.isJoined());
            }
        });
    }

    public void setBottomBarState(boolean invisible, boolean animate) {
        if (invisible) {
            BaseFragment currentFragment = getCurrentFragment();
            if ((currentFragment instanceof AlbumTimeLineFragment) || (currentFragment instanceof AlbumDetailFragment)) {
                if (!getBottomBtnShow()) {
                    bottomBar.show(animate);
                }
            }
        } else {
            bottomBar.hide(animate);
        }
    }

    public boolean getBottomBtnShow() {
        if (llBottomBtn.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onJoinClicked() {

    }

    @Override
    public void onUploadPhotoClicked() {
        showUpload();
        if (!AppData.getHasShowUploadShareGuide()) {
            AppData.setFirstUpload(true);
        } else {
            AppData.setFirstUpload(false);
        }
    }

    private void updateTitleBarContent() {
        for (BaseTabBarFragment item : baseTabBarList) {
            item.setOwnerInfo(ownerInfo);
            item.setAlbumEntity(albumEntity);
            item.refreshTitleContent();
        }
    }

    @Override
    public void setTabBarState(int state, boolean showDayNight) {
        switch (state) {
            case OnTimeLineHeaderActionListener.STATE_HIDE:
                tabBar.setVisibility(View.INVISIBLE);
                break;
            case OnTimeLineHeaderActionListener.STATE_SHOW_MEMBER:
                if (tabBar.getVisibility() != View.VISIBLE) tabBar.setVisibility(View.VISIBLE);
                mShowMembers.setTextColor(0xff0794e1);
                mShowMembers.setBackgroundResource(R.drawable.files_in_album_corners_bg);
                mShowTimeLine.setTextColor(0xFFFFFFFF);
                mShowTimeLine.setBackgroundResource(R.drawable.transparent);
                break;
            case OnTimeLineHeaderActionListener.STATE_SHOW_ALBUM:
                if (tabBar.getVisibility() != View.VISIBLE) tabBar.setVisibility(View.VISIBLE);
                mShowTimeLine.setTextColor(0xff0794e1);
                mShowTimeLine.setBackgroundResource(R.drawable.files_in_album_corners_bg);
                mShowMembers.setTextColor(0xFFFFFFFF);
                mShowMembers.setBackgroundResource(R.drawable.transparent);
                break;
        }
        if (showDayNight) {
            dayNightViews.setVisibility(View.VISIBLE);
            avatorLayout.setVisibility(View.VISIBLE);
        } else {
            dayNightViews.setVisibility(View.INVISIBLE);
            avatorLayout.setVisibility(View.INVISIBLE);
        }

    }

    @Override
    public void showNewMember(List<MemberEntity> memberEntities, int count) {
        if (mNewMemberFragment == null) {
            mNewMemberFragment = new NewMemberFragment();
        }
        mNewMemberFragment.setData(albumEntity, memberEntities, count);
        showOnly(mNewMemberFragment);
        updateTitleBar(true);
    }

    @Override
    public void setBottomDeleteBtnsState() {
        if (isDeleteMode()) {
            onCancelDelete();
        }
    }


    private boolean isDeleting() {
        return deleteCounter > 0;
    }


    @Override
    public void onFileCancel(final FileEntity file) {
        UploadCancelManager.sharedInstance().cancelUpload(file);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                onFileDelete(file.getFakeId(), false);
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    /**
     * generater QR code share iamge
     */
    public void generatorShareImage() {
        UMutils.instance().diyEvent(ID.EventSaveQrcode);
        Bitmap avatar = fileOptionListener.getAvatarBitmap();
        if (avatar != null && ownerInfo != null) {
            ShareImageCreateUtil scr = new ShareImageCreateUtil();
            scr.setAlbumEntity(albumEntity);
            scr.setAvatar(avatar);
            scr.setCreatorName(ownerInfo.getName());
            scr.setContext(this);
            scr.createShareImage();
        }
    }

    protected void handle404(ConnectInfo info, Response response) {
        if (response.getStatusCode() == 404) {
            CToast.showToast(R.string.album_has_been_delete);
            try {
                albumEntity.delete(App.getDBHelper().getWritableDatabase());
            } catch (Exception e) {
                e.printStackTrace();
            }
            finish();
        }
    }

    @Override
    public void finish() {
        if (albumList != null) {
            EventBus.getDefault().post(new Signal(Signal.ALBUM_FINISH, albumList.getAlbumId()));
        }
        if (albumEntity != null) {
            App.putAlbum(albumEntity);
        }
        super.finish();
    }

}
