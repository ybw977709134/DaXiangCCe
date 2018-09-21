package com.daxiangce123.android.ui.pages;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.AlbumSort;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.Banner;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.ListBanners;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.SimpleData;
import com.daxiangce123.android.data.SimpleDataImpl;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Error;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.AlbumListener;
import com.daxiangce123.android.listener.OnDeleteAlbumListener;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.adapter.EmptyAlbumAdapter;
import com.daxiangce123.android.ui.adapter.GroupAlbumGridViewAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ListGrid;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.ui.view.PullToRefreshBanner;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class AlbumFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OptionListener {
    private static final String TAG = "AlbumFragment";
    private View mRootView = null;
    private PullToRefreshBanner mPullRefreshBanner;
    private ListGrid gvAlbums = null;
    private GroupAlbumGridViewAdapter mAlbumAdapter = null;
    private EmptyAlbumAdapter emptyAlbumAdapter = null;
    private List<AlbumEntity> mAlumList = null;
    // private CreateJoinAlbumDialog createJoinAlbumDialog;
    private OptionDialog sortAlbumDialog;
    private boolean mDelete = false;
    private AlbumSort mSort;
    // private OnOpenAlbumListener openAlbumListener;
    private OnDeleteAlbumListener deleteAlbumListener;
    //    private int lastSort = 0;
    private int startPos;
    private Bitmap albumCover;
    // private AlbumEntity curAlbumEntity;
    private Map<String, AlbumEntity> albumMap;
    private static boolean DEBUG = true;
    private ImageView ivBanner;
    private View headerView;
    private View bannerContent;
    private ImageView mClose;
    // private RelativeLayout rlBanner;
    private Banner banner;
    // private BannerAlbumDialog bannerAlbumDialog;
    // private List<FileEntity> sampleFiles;
    private AlbumListener albumListener;

    private boolean firstStart = true;

    private View searchBar;
    /**
     * click to clear all inputs
     */
    private View vClear;
    /**
     * except the search key word
     */
    private EditText etInput;
    /**
     * show count of search result
     */
    private TextView tvSearchResultCount;
    /**
     * local albums or search result
     */
    private View emptyView;
    //    private View noAlbumView;
    private final int MAX_INPUT_COUNT = 25;
    private List<AlbumEntity> searchResults;
    private HashSet<String> searchAlbumSet;
    private ImageView mSearch;
    private LinkedList<Banner> bannerList;

    private long lastRefreshAlbumListTime = 0;// System.currentTimeMillis();

    private TextWatcher textWatcher = new TextWatcher() {

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s == null || s.length() == 0) {
                vClear.setVisibility(View.GONE);
            } else {
                if (!vClear.isShown()) {
                    vClear.setVisibility(View.VISIBLE);
                }
            }
        }
    };

    // private ListGrid mListGridAlbum;

    public static interface AlbumFragmentListener {

        public boolean hasJoined(String albumId);

        public boolean openAlbum(AlbumEntity album);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                if (DEBUG) {
                    LogUtil.d(TAG, "onReceive() " + action + "\nresponse=" + response);
                }
                if ((Consts.ON_HANLDE_ALBUM_LIST).equals(action)) {
                    ListAllAlbums listAllAlbums = intent.getParcelableExtra(Consts.LIST_ALL_ALBUMS);
                    onHandleAlbums(listAllAlbums);
                } else if ((Consts.DELETE_ALBUM).equals(action)) {
                    onDeleteAlbum(response, connectInfo);
                } else if (Consts.LEAVE_ALBUM.equals(action)) {
                    onLeaveAlbum(response, connectInfo);
                } else if (Consts.CREATE_ALBUM.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        UMutils.instance().diyEvent(ID.EventCreateAlbumSuccess);
                        AlbumEntity albumEntity = Parser.parseAlbum(response.getContent());
                        onAlbumCreate(albumEntity);
                        albumListener.openAlbum(albumEntity);
                    }
                } else if (Consts.JOIN_ALBUM.equals(action)) {
                    // LogUtil.d(TAG, " --- Consts.JOIN_ALBUM.equals(action) --"
                    // + action + "--- response --" + response);
                    String inviteCode = connectInfo.getTag();
                    Error errors = response.getError();
                    if (response.getStatusCode() == 304) {
                        CToast.showToast(R.string.album_exists);
                    } else {
                        HttpEntity entity = connectInfo.getEntity();
                        JSONObject jo = null;
                        if (entity instanceof StringEntity) {
                            String entityStr = EntityUtils.toString(entity);
                            jo = JSONObject.parseObject(entityStr);
                        }
                        if (response.getStatusCode() == 200) {
                            AlbumEntity albumEntity = Parser.parseAlbum(response.getContent());
                            if (albumEntity != null) {
                                // ConnectBuilder.getAlbumCover(albumEntity.getId());
                                ConnectBuilder.getAlbumCoverId(albumEntity.getId());
                                onAlbumCreate(albumEntity);
                                if (!jo.containsKey(Consts.NOT_OPEN_ALBUM)) {
                                    albumListener.openAlbum(albumEntity);
                                }
                                CToast.showToast(R.string.join_album_successed);
                            }
                            // if ((banner.getAlbum().getId().equals(connectInfo
                            // .getTag2()))) {
                            // bannerAlbumDialog.setAlbumEntity(banner
                            // .getAlbum());
                            // }
                        } else if (response.getStatusCode() == 404) {
                            CToast.showToast(R.string.album_not_found);
                        } else if (response.getStatusCode() == 401 && errors.toErrorCode() == ErrorCode.INVALID_PASSWORD) {
                            if (entity instanceof StringEntity) {
                                if (jo.containsKey(Consts.PASSWORD)) {
                                    CToast.showToast(R.string.error_password);
                                } else if (jo.containsKey(Consts.NOT_OPEN_ALBUM)) {
                                    return;
                                } else {
                                    inputPassword(connectInfo.getTag2(), inviteCode);
                                }
                            } else {
                                CToast.showToast(R.string.can_not_allow_join);
                            }
                        } else if (response.getStatusCode() == 403) {
                            handleJoinAlbumError(errors);
                        } else {
                            CToast.showToast(R.string.join_album_failed);
                        }
                    }

                } else if (Consts.GET_ALBUM_COVER.equals(action)) {
                    updateAlbumCover(response, connectInfo);

                } else if (Consts.LIST_BANNER.equals(action)) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "--Consts.LIST_BANNER-- response: " + response);
                    }
                    if (response.getStatusCode() == 200) {
                        ListBanners banners = Parser.parseBannerList(response.getContent());
                        bannerList = banners.getBanner();
                        showBanner();
                    }

                } else if (Consts.SET_ALBUM_THUMB.equals(action)) {
                    setAlbumCover(response, connectInfo);
                }

                // else if (Consts.GET_ALBUM_SAMPLES.equals(action)) {
                // onGetSamples(connectInfo, response);
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public long getLastRefreshAlbumListTime() {
        return lastRefreshAlbumListTime;
    }

    public void setLastRefreshAlbumListTime(long lastRefreshAlbumListTime) {
        this.lastRefreshAlbumListTime = lastRefreshAlbumListTime;
    }

    @SuppressLint("NewApi")
    private void inputPassword(final String albumId, final String inviteCode) {
        Context mContext = mRootView.getContext();
        final EditText etInputId = new EditText(mContext);
        AlertDialog.Builder passwordDialog = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            passwordDialog = new Builder(mContext);
        } else {
            passwordDialog = new Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
        }
        passwordDialog.setTitle(R.string.input_password);
        passwordDialog.setMessage(R.string.please_input_password);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    String password = etInputId.getText().toString();
                    if (Utils.isEmpty(password)) {
                        CToast.showToast(R.string.havet_input);
                    } else {
                        JSONObject jo = new JSONObject();
                        jo.put(Consts.USER_ID, App.getUid());
                        jo.put(Consts.PASSWORD, password);
                        ConnectBuilder.joinAlbum(albumId, inviteCode, jo.toJSONString());
                    }
                }
                dialog.cancel();
            }
        };
        passwordDialog.setPositiveButton(R.string.confirm, listener);
        passwordDialog.setNegativeButton(R.string.cancel, listener);
        passwordDialog.setView(etInputId);
        passwordDialog.show();
    }

    //
    // private AlbumFragmentListener albumFragmentListener = new
    // AlbumFragmentListener() {
    //
    // @Override
    // public boolean openAlbum(AlbumEntity album) {
    // if (albumListener != null) {
    // return albumListener.openAlbum(album);
    // }
    // return false;
    // }
    //
    // @Override
    // public boolean hasJoined(String albumId) {
    // if (albumListener != null) {
    // return albumListener.hasAlbum(albumId);
    // }
    // return false;
    // }
    //
    // };

    private boolean isInviteCodeExist(String inviteCode) {
        if (Utils.isEmpty(inviteCode)) {
            return false;
        }
        try {

            for (AlbumEntity info : mAlumList) {
                if (info == null) {
                    continue;
                }
                if (inviteCode.equals(info.getInviteCode())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // private void onGetSamples(ConnectInfo info, Response response) {
    // if (DEBUG) {
    // LogUtil.d(TAG,
    // "-------------------------onGetSamples()-------------------------");
    // }
    // if (response == null || info == null || banner == null
    // || banner.getAlbum() == null) {
    // return;
    // }
    // if (response.getStatusCode() != 200) {
    // return;
    // }
    // String albumId = info.getTag();
    // if (!(banner.getAlbum().getId().equals(albumId))) {
    // return;
    // }
    // AlbumSamples items = Parser.parseAlbumSamples(response.getContent());
    // if (items == null || Utils.isEmpty(items.getFiles())) {
    // return;
    // }
    // sampleFiles = items.getFiles();
    // updateDialog();
    // }

    // private void updateDialog() {
    // if (bannerAlbumDialog == null) {
    // return;
    // }
    // if (!bannerAlbumDialog.isShowing()) {
    // return;
    // }
    // bannerAlbumDialog.notifyDataSetChanged();
    // }

    private void showBanner() {
        if (Utils.isEmpty(bannerList)) {
            return;
        }
        for (int i = 0; i < bannerList.size(); i++) {
            Banner b = bannerList.get(i);
            long bStartDate = TimeUtil.formatTime(b.getStartDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            long bEndDate = TimeUtil.formatTime(b.getEndDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            long bCurDate = System.currentTimeMillis();

            if (bStartDate <= bCurDate && bCurDate <= bEndDate) {
                banner = b;
                // ConnectBuilder.getAlbumSample(banner.getAlbum().getId());
            }
        }
        //
        // banner = bannerList.get(0);

        String oldId = AppData.getBannerId();
        long oldDate = AppData.getCloseBannerDate();
        if (DEBUG) {
            LogUtil.d(TAG, "oldID" + oldId);
        }
        if (DEBUG) {
//            LogUtil.d(TAG, "bannerID" + banner.getBannerId());
        }
        if (banner == null) {
            ivBanner.setVisibility(View.GONE);
            return;
        }

        if (Utils.isEmpty(oldId) || oldDate == 0) {
            bannerContent.setVisibility(View.VISIBLE);
            String id = banner.getBannerId();
            ImageManager.instance().loadBanner(ivBanner, id);
        } else {
            long startDate = TimeUtil.formatTime(banner.getStartDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            long endDate = TimeUtil.formatTime(banner.getEndDate(), "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");

            long curDate = System.currentTimeMillis();
            if (DEBUG) {
                LogUtil.d(TAG, "  startDate :" + startDate + "  endDate :" + endDate + " oldDate: " + oldDate + " curDate :" + curDate);
            }

            if (curDate < endDate && curDate >= startDate) {
                long curday = (TimeUtil.toDay(curDate));
                if (oldId.equals(banner.getBannerId()) && oldDate >= curday) {
                    bannerContent.setVisibility(View.GONE);
                } else {
                    bannerContent.setVisibility(View.VISIBLE);
                    ImageManager.instance().loadBanner(ivBanner, banner.getBannerId());
                }
            } else {
                bannerContent.setVisibility(View.VISIBLE);
            }
        }

        getBannerContentIsShow();
    }

    public void getBannerContentIsShow() {
        if (bannerContent.getVisibility() == ViewGroup.VISIBLE) {
            albumListener.bannerContentIsShow(true);
        } else {
            albumListener.bannerContentIsShow(false);
        }

    }

    public AlbumFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public String getFragmentName() {
        return "AlbumFragment";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBroadcast();
        ConnectBuilder.listBanner();
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.group_album_grid_view,
                    container, false);
            initCompontent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        LoadingDialog.show(R.string.loading);
        return mRootView;
    }

    @Override
    public void onShown() {
        super.onShown();
        if (!isSearchMode()) {
            initBroadcast();
            if (!firstStart) {
                onUpdateAdapter(true);

            }
            firstStart = false;
        }
    }

    public void onResume() {
        super.onResume();
        if (App.DEBUG) {
            LogUtil.v(TAG, "===========onResume()================");
        }
        if (isVisible()) {
            mAlbumAdapter.notifyDataSetChanged();
        }
        if (System.currentTimeMillis() - lastRefreshAlbumListTime > 5 * Consts.MIN_IN_MILLS) {
            ConnectBuilder.listAlbum();
            lastRefreshAlbumListTime = System.currentTimeMillis();
            if (App.DEBUG) {
                LogUtil.v(TAG, "onResume() listAlbum");
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        lastRefreshAlbumListTime = 0;
    }

    private void initCompontent() {
        albumMap = new HashMap<String, AlbumEntity>();
        searchAlbumSet = new HashSet<String>();
        mRootView.findViewById(R.id.iv_title_sort).setOnClickListener(this);
        // mRootView.findViewById(R.id.iv_add).setOnClickListener(this);

        mRootView.findViewById(R.id.tv_search_it).setOnClickListener(this);
        mSearch = (ImageView) mRootView.findViewById(R.id.iv_show_search);
        mSearch.setOnClickListener(this);

        vClear = mRootView.findViewById(R.id.iv_search_clear);
        vClear.setOnClickListener(this);

        searchBar = mRootView.findViewById(R.id.search_bar);
        emptyView = mRootView.findViewById(R.id.v_empty);
        emptyView.setOnClickListener(this);
//        noAlbumView = mRootView.findViewById(R.id.album_empty);
        tvSearchResultCount = (TextView) mRootView.findViewById(R.id.iv_search_result_count);

        etInput = (EditText) mRootView.findViewById(R.id.et_search_input);
        etInput.addTextChangedListener(textWatcher);

        mPullRefreshBanner = (PullToRefreshBanner) mRootView.findViewById(R.id.gv_group_album_list);


        mPullRefreshBanner.setOnRefreshListener(new OnRefreshListener2<View>() {
            @Override
            public void onPullDownToRefresh(PullToRefreshBase<View> refreshView) {
                // ConnectBuilder.listBanner();
                if (isSearchMode()) {
//                    if (Utils.isEmpty(etInput.getText().toString().trim())) {
//                        hideSearchBar();
//                    } else {
                    searchIt(true);
//                    }
                } else {
                    ConnectBuilder.listAlbum();
                }
                refreshView.onRefreshComplete();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<View> refreshView) {
            }
        });

        final int numColums = 2;
        final int padding = Utils.getDip(20);
        // final int hSpaceing = padding;

        mAlbumAdapter = new GroupAlbumGridViewAdapter(getActivity());
        emptyAlbumAdapter = new EmptyAlbumAdapter(getActivity());
        mAlbumAdapter.setClickListener(this);
        // mAlbumAdapter.setSpacing(hSpaceing);
        mAlbumAdapter.setCover(albumCover);

        gvAlbums = mPullRefreshBanner.getGridView();

        if (headerView == null) {
            headerView = LayoutInflater.from(this.getActivity()).inflate(R.layout.banner_view, gvAlbums, false);
        }

        ivBanner = (ImageView) headerView.findViewById(R.id.iv_banner);
        mClose = (ImageView) headerView.findViewById(R.id.iv_close);
        bannerContent = headerView.findViewById(R.id.rl_banner);
        ivBanner.setOnClickListener(this);
        mClose.setOnClickListener(this);

        gvAlbums.addHeaderView(headerView);
        checkIsEmpty();
//        gvAlbums.setRowCount(2);
        // gvAlbums.setNumColumns(numColums);
//        gvAlbums.setAdapter(mAlbumAdapter);
        gvAlbums.setOnItemClickListener(this);
        ViewUtil.ajustMaximumVelocity(gvAlbums, Consts.SLOW_ABS_SCROLL_RATION);
        // gvAlbums.setHorizontalSpacing(hSpaceing);
        // gvAlbums.setOnItemLongClickListener(this);
        // gvAlbums.setPadding(0, 0, padding, 0);

        int gvWidth = App.SCREEN_WIDTH - padding * 2;
        int width = (gvWidth - (numColums - 1) * padding) / numColums;
        ImageSize imageSize = new ImageSize(width, width);
        imageSize.setThumb(true);
        mAlbumAdapter.setImageSize(imageSize);

        SimpleData data = new SimpleDataImpl(Consts.BASIC_CONFIG);
        // int i = data
        // .getInt(Consts.ALBUM_SORT, AlbumSort.CREATED_DATE.ordinal());
//        int i = data.getInt(Consts.ALBUM_SORT, AlbumSort.UPDATED_DATE.ordinal());
        int i = AppData.getAlbumSort();
        try {
            mSort = AlbumSort.values()[i];
        } catch (Exception e) {
            e.printStackTrace();
            // mSort = AlbumSort.CREATED_DATE;
            mSort = AlbumSort.UPDATED_DATE;
        }
        /**
         * EventService will register the receiver and handle the album list
         */
        // ConnectBuilder.listAlbum();
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.CREATE_ALBUM);
        ift.addAction(Consts.ON_HANLDE_ALBUM_LIST);
        ift.addAction(Consts.JOIN_ALBUM);
        ift.addAction(Consts.GET_ALBUM_COVER);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.DELETE_ALBUM);
        ift.addAction(Consts.LIST_BANNER);
        ift.addAction(Consts.SET_ALBUM_THUMB);
        // ift.addAction(Consts.SET_ALBUM_THUM);

        // ift.addAction(Consts.GET_ALBUM_SAMPLES);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void checkIsEmpty() {
        if (Utils.isEmpty(mAlumList)) {
            gvAlbums.setRowCount(1);
            gvAlbums.setAdapter(emptyAlbumAdapter);
        } else {
            gvAlbums.setRowCount(2);
            gvAlbums.setAdapter(mAlbumAdapter);
        }
    }

    private void onUpdateAdapter(boolean sort) {
        if (!isShown()) {
            return;
        }
        if (sort) {
            sortAlbum();
        }

        List<AlbumEntity> list = null;
        if (isSearchMode()) {
//            noAlbumView.setVisibility(View.GONE);
            if (searchResults != null) {
                list = searchResults;
            }
            mAlbumAdapter.isSearchMode(true);
            if (Utils.isEmpty(list)) {
                gvAlbums.setRowCount(2);
                gvAlbums.setAdapter(mAlbumAdapter);
                emptyView.setVisibility(View.VISIBLE);
            } else {
                emptyView.setVisibility(View.GONE);
            }
        } else {
            if (mAlumList != null) {
                list = mAlumList;
            }
//            if (Utils.isEmpty(list)) {
//                noAlbumView.setVisibility(View.VISIBLE);
//            } else {
//                noAlbumView.setVisibility(View.GONE);
//            }
            checkIsEmpty();
            mAlbumAdapter.isSearchMode(false);

        }
        mAlbumAdapter.setData(list);
        mAlbumAdapter.notifyDataSetChanged();

        if (Utils.isEmpty(list)) {
            mDelete = false;
            mAlbumAdapter.setIsDelete(mDelete);
        }
        // else {
        // mAlbumAdapter.notifyDataSetChanged();
        // }
    }

    private void onDeleteAlbum(Response response, ConnectInfo info) {
        if (info == null || response == null) {
            return;
        }
        int statusCode = response.getStatusCode();
        if (statusCode == 200 || statusCode == 404) {
            String albumId = info.getTag();
            if (DEBUG) {
                LogUtil.d(TAG, "LEAVE_ALBUM userId = " + albumId);
            }
            if (onAlbumDeleted(albumId)) {
                CToast.showToast(R.string.delete_album_succeed);
                mDelete = false;
                mAlbumAdapter.setIsDelete(mDelete);
                return;
            }
        }
        CToast.showToast(R.string.request_failed);
    }

    private void onLeaveAlbum(Response response, ConnectInfo info) {
        if (info == null || response == null) {
            return;
        }
        int statusCode = response.getStatusCode();
        if (statusCode == 200 || statusCode == 404) {
            String albumId = info.getTag();
            if (DEBUG) {
                LogUtil.d(TAG, "LEAVE_ALBUM userId = " + albumId);
            }
            if (onAlbumDeleted(albumId)) {
                CToast.showToast(R.string.quite_album_succeed);
                mDelete = false;
                mAlbumAdapter.setIsDelete(mDelete);
                return;
            }
        }
        CToast.showToast(R.string.request_failed);
        if (App.DEBUG) {
            LogUtil.e(TAG, "request failed " + statusCode);
            LogUtil.e(TAG, "request failed " + response);
        }
    }

    public void setDefaultCover(Bitmap bitmap) {
        albumCover = bitmap;
        if (mAlbumAdapter != null) {
            mAlbumAdapter.setCover(albumCover);
        }
    }

    public void onReadAlbumsFromDB(List<AlbumEntity> l) {
        // if (l == null) {
        // return;
        // }
        this.mAlumList = l;
        onUpdateAdapter(true);

        if (albumMap != null) {
            albumMap.clear();
        }
        if (!Utils.isEmpty(l)) {
            for (AlbumEntity album : l) {
                albumMap.put(album.getId(), album);
            }
        }
        LoadingDialog.dismiss();
    }

    public boolean hasAlbum(String albumId) {
        if (Utils.isEmpty(albumId)) {
            return false;
        }
        if (albumMap == null || albumMap.isEmpty()) {
            return false;
        }
        return albumMap.containsKey(albumId);
    }


    public boolean onFileCreate(FileEntity file) {
        if (file == null) {
            return false;
        }
        if (Utils.isEmpty(mAlumList)) {
            return false;
        }
        AlbumEntity album = albumMap.get(file.getAlbum());
        if (album == null) {
            return false;
        }
        album.setLocalCover(file.getId());
        album.setSize(album.getSize() + 1);
        if (mSort == AlbumSort.ITEM_COUNT) {
            onUpdateAdapter(true);
        } else {
            mAlbumAdapter.updateSingle(album.getId());
        }
        return true;
    }

    public boolean onAlbumCreate(AlbumEntity album) {
        if (album == null) {
            return false;
        }
        if (mAlumList == null) {
            mAlumList = new LinkedList<AlbumEntity>();
        } else {
            if (albumMap.containsKey(album.getId())) {
                return false;
            }
        }
        albumMap.put(album.getId(), album);
        mAlumList.add(album);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image_large);
        bitmap = BitmapUtil.squareBitmap(bitmap);
        Bitmap defBitmap = BitmapUtil.toRoundCorner(bitmap);
        defBitmap = BitmapUtil.rotateOverlay(defBitmap);
        mAlbumAdapter.setCover(defBitmap);
        onUpdateAdapter(true);
        return true;
    }

    public boolean onAlbumUpdate(AlbumEntity album) {
        if (DEBUG) {
            LogUtil.d(TAG, "onAlbumUpdate	" + album);
        }
        if (album == null) {
            return false;
        }
        AlbumEntity oldAlbum = albumMap.get(album.getId());
        if (oldAlbum == null) {
            return false;
        }
        oldAlbum.updateFromRemote(album);
        mAlbumAdapter.updateSingle(album.getId());
        if (DEBUG) {
            LogUtil.d(TAG, "onAlbumUpdate	updateSingle" + album.getId());
        }
        return true;
    }

    /**
     * DB operation operated in EventService
     */
    public boolean onMemberChange(MemberEntity member, boolean increase) {
        if (member == null) {
            return false;
        }
        if (Utils.isEmpty(mAlumList)) {
            return false;
        }
        try {
            if (member.getUserId().equals(App.getUid()) && increase) {
                // has joint a new album
                // TODO why cant give us an album
                ConnectBuilder.listAlbum();
            } else {
                String albumId = member.getAlbumId();
                AlbumEntity album = albumMap.get(albumId);
                if (album == null) {
                    return false;
                }
                if (increase) {
                    album.setMembers(album.getMembers() + 1);
                } else {
                    if (member.getUserId().equals(App.getUid())) {
                        return onAlbumDeleted(albumId);
                    } else {
                        album.setMembers(album.getMembers() - 1);
                    }
                }
                return mAlbumAdapter.updateSingle(albumId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean onAlbumDeleted(String albumId) {
        if (Utils.isEmpty(mAlumList) || Utils.isEmpty(albumId)) {
            return false;
        }
        AlbumEntity album = albumMap.remove(albumId);
        if (album == null) {
            return false;
        }
        mAlumList.remove(album);
        if (isSearchMode()) {
            searchAlbumSet.remove(albumId);
            searchResults.remove(album);
        }
        onUpdateAdapter(false);
        if (deleteAlbumListener != null) {
            deleteAlbumListener.onDeleteAlbum(album);
        }
        return true;
    }

    public boolean onFileDelete(FileEntity file) {
        if (file == null) {
            return false;
        }
        if (mAlumList == null) {
            return false;
        }
        String albumId = file.getAlbum();
        String fileId = file.getId();
        AlbumEntity album = albumMap.get(albumId);
        if (album == null) {
            return false;
        }
        int size = album.getSize() - 1;
        size = size >= 0 ? size : 0;
        int unread = album.getUpdateCount();
        unread = (unread >= size) ? size : unread;

        album.setSize(size);
        // album.setUpdateCount(unread);

        if (size <= 0) {
            album.setLocalCover(null);
        } else {
            if (Utils.isSame(fileId, album.getTrueCover())) {
                // ConnectBuilder.getAlbumCover(file.getAlbum());
                ConnectBuilder.getAlbumCoverId(file.getAlbum());
            }
        }
        if (mSort == AlbumSort.ITEM_COUNT) {
            onUpdateAdapter(true);
        } else {
            mAlbumAdapter.updateSingle(albumId);
        }
        return true;
    }

    private void onHandleAlbums(ListAllAlbums listAllAlbums) {
        if (listAllAlbums == null) {
            return;
        }
        LoadingDialog.dismiss();
        List<AlbumEntity> list = listAllAlbums.getAlbums();
        if (list == null) {
            return;
        }
        if (listAllAlbums.hasMore()) {
            startPos += list.size();
            ConnectBuilder.listAlbum(startPos, 100, Sort.BY_MOD_DATE, Order.DESC);
        } else {
            startPos = 0;
        }
        try {
            albumMap.clear();
            for (AlbumEntity album : list) {
                albumMap.put(album.getId(), album);

                if (DEBUG) {
                    LogUtil.d(TAG, "onHandleAlbums		" + album.getTrueCover());
                    LogUtil.d(TAG, "onHandleAlbums		" + album);

                }
            }
            if (mAlumList == null) {
                mAlumList = list;
            } else {
                mAlumList.clear();
                mAlumList.addAll(list);
                list.clear();
                list = null;
            }
            onUpdateAdapter(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setAlbumCover(Response response, ConnectInfo connectInfo) {
        if (response == null || connectInfo == null) {
            return;
        }
        if (response.getStatusCode() == 200) {
            CToast.showToast(R.string.set_album_cover_succeed);
            String albumId = connectInfo.getTag();
            String fileId = connectInfo.getTag2();
//            AlbumEntity album = albumMap.get(albumId);
            AlbumEntity album = App.getAlbum(albumId);
            if (album == null) {
                return;
            }
            album.setThumbFileId(fileId);
            if (App.DEBUG) {
                LogUtil.d(TAG, " ---- cover + AsetAlbumCover ---- " + fileId);
            }
            mAlbumAdapter.updateSingle(albumId);
        } else {
            CToast.showToast(R.string.set_album_cover_failed);
        }
    }

    private void updateAlbumCover(Response response, ConnectInfo connectInfo) {
        if (response == null || connectInfo == null) {
            return;
        }
        int statusCode = response.getStatusCode();
        if (statusCode != 404 && statusCode != 200) {
            return;
        }
        String albumId = connectInfo.getTag();
        if (Utils.isEmpty(albumId)) {
            return;
        }
        if (mAlumList == null) {
            return;
        }
        AlbumEntity album = albumMap.get(albumId);
        if (album == null) {
            return;
        }
        if (statusCode == 404) {
            onAlbumDeleted(albumId);
        } else if (statusCode == 200) {
            // AlbumSamples albumItems =
            // Parser.parseAlbumSamples(response.getContent(), false);
            // if (albumItems == null || Utils.isEmpty(albumItems.getFiles())) {
            // return;
            // }
            // FileEntity file = albumItems.getFiles().getFirst();
            // if (file == null) {
            // return;
            // }
            // String fileId = file.getId();
            String fileId = Parser.parseAlbumThumId(response.getContent());
            if (DEBUG) {
                LogUtil.d(TAG, "updateAlbumCover	cover=" + fileId);
            }
            album.setLocalCover(fileId);
            mAlbumAdapter.updateSingle(albumId);
        }
    }

    private void showAlbumSort() {
        if (sortAlbumDialog != null && sortAlbumDialog.isShowing()) {
            return;
        } else {
            // if (sortAlbumDialog == null) {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            // mDatas.add(R.string.sort_by_file_updeted_count);
            mDatas.add(R.string.sort_by_updated_date);
            mDatas.add(R.string.sort_by_file_count);
            mDatas.add(R.string.sort_by_owner);
            mDatas.add(R.string.cancel);

            sortAlbumDialog = new OptionDialog(getActivity());
            sortAlbumDialog.setOptionListener(this);
            sortAlbumDialog.setTitle(getResources().getString(R.string.selecte_album_sort));
            sortAlbumDialog.setData(mDatas);
            // }
            sortAlbumDialog.setSelected(AppData.getAlbumSort());
            sortAlbumDialog.show();
        }
    }


    public void setAlbumListener(AlbumListener albumListener) {
        this.albumListener = albumListener;
    }

    public void setDeleteAlbumListener(OnDeleteAlbumListener deleteAlbumListener) {
        this.deleteAlbumListener = deleteAlbumListener;
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_title_sort) {
            try {
                showAlbumSort();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.iv_close) {
            if (banner != null) {
                bannerContent.setVisibility(View.GONE);
                AppData.setBannerId(banner.getBannerId());
                AppData.setCloseBannerDate(TimeUtil.toDay(System.currentTimeMillis()));
            }

        } else if (id == R.id.iv_banner) {
            UMutils.instance().diyEvent(ID.ClickBanner);
            if (banner != null) {
                AlbumEntity albumEntity = banner.getAlbum();

                onOpenAlbum(albumEntity);
            }
        } else if (id == R.id.iv_show_search) {
            showSearchBar();
        } else if (id == R.id.tv_search_it) {
            clearSearcheResults();
            searchIt(true);
        } else if (id == R.id.iv_search_clear) {
            clearInput();
        } else if (id == R.id.v_empty) {
            if (isSearchMode()) {
                hideSearchBar();
            }
        }

        // else if (id == R.id.tv_bottom_right_corner_icon) {
        // if (v.getTag() instanceof AlbumEntity) {
        // AlbumEntity ai = (AlbumEntity) v.getTag();
        // if (ai.getOwner().equals(App.getUid())) {
        // deleteAlbum(ai);
        // } else {
        // leaveAlbum(ai);
        // }
        // }
        // }

        // else if (id == R.id.iv_add) {
        // showAddAlbum();
        // }
    }

    private void showSearchBar() {
        if (searchBar.isShown()) {
            return;
        }
        mSearch.setVisibility(View.GONE);
        // gvAlbums.removeHeaderView(headerView);
        bannerContent.setVisibility(View.GONE);
        Utils.showIME();
        etInput.requestFocus();
        searchBar.setVisibility(View.VISIBLE);
        UMutils.instance().diyEvent(ID.EventSearchLocalAlbum);
    }

    private boolean searchIt(boolean fromStart) {
        Editable text = etInput.getText();
        if (text == null || Utils.isEmpty(text.toString().trim())) {
            CToast.showToast(R.string.havet_input);
            return false;
        }
        int length = text.toString().trim().length();
        if (length >= MAX_INPUT_COUNT) {
            CToast.showToast(getString(R.string.max_string_to_input_is_x, MAX_INPUT_COUNT));
            return false;
        }
        Utils.hideIME(etInput);
        LoadingDialog.show(getString(R.string.searching_x, text.toString()));
        UMutils.instance().diyEvent(ID.EventSearchMyAlbum);
        onSearchAlbum(text.toString().trim());
        return true;
    }

    private void onSearchAlbum(String searchText) {
        if (!isSearchMode()) {
            return;
        }
        mPullRefreshBanner.setMode(PullToRefreshBase.Mode.DISABLED);

        if (!Utils.isEmpty(mAlumList)) {
            for (AlbumEntity albumEntity : mAlumList) {
                String albumName = albumEntity.getName();
                if ((albumName.toLowerCase()).contains((searchText.toLowerCase()))) {
                    if (searchResults == null) {
                        searchResults = new LinkedList<AlbumEntity>();
                    }
                    if (!searchAlbumSet.contains(albumEntity.getId())) {
                        searchAlbumSet.add(albumEntity.getId());
                        searchResults.add(albumEntity);
                    }
                }

            }
        }

        if (searchResults == null) {
            LoadingDialog.dismiss();
            CToast.showToast(getString(R.string.album_x_has_not_found, searchText));
        }

        // showSearchCount();
        // notifyDataSetChange();
        onUpdateAdapter(false);
        LoadingDialog.dismiss();

        // getAlbumSample(newList, items.getAlbums());
    }

    // private void showSearchCount() {
    // int count = 0;
    // if (searchResults != null) {
    // count = Utils.sizeOf(searchResults);
    // }
    // tvSearchResultCount.setText(getString(R.string.x_related_albums, count));
    // }

    private void clearInput() {
        etInput.setText("");
        clearSearcheResults();
    }

    private void clearSearcheResults() {
        if (searchResults != null) {
            searchResults.clear();
            searchResults = null;
        }

        if (searchAlbumSet != null) {
            searchAlbumSet.clear();
        }

        // notifyDataSetChange();
        onUpdateAdapter(false);

    }

    private boolean hideSearchBar() {
        if (isSearchMode()) {
            mPullRefreshBanner.setMode(PullToRefreshBase.Mode.PULL_FROM_START);
            mSearch.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
            // gvAlbums.addHeaderView(headerView);
            // bannerContent.setVisibility(View.VISIBLE);
            showBanner();
            tvSearchResultCount.setText("");
            etInput.setText(null);
            searchAlbumSet.clear();
            if (searchResults != null) {
                searchResults.clear();
                searchResults = null;
            }

            if (isEmptyViewShow()) {
                emptyView.setVisibility(View.GONE);
            }
            // notifyDataSetChange();
            onUpdateAdapter(false);
            Utils.hideIME(etInput);
            return true;
        }
        return false;
    }

    private boolean isEmptyViewShow() {
        if (emptyView.getVisibility() == ViewGroup.VISIBLE) {
            return true;
        }
        return false;
    }

    // private void notifyDataSetChange() {
    // List<AlbumEntity> list = null;
    // if (isSearchMode()) {
    // if (searchResults != null) {
    // list = searchResults;
    // }
    // mAlbumAdapter.setData(list);
    // mAlbumAdapter.isSearchMode(true);
    // mAlbumAdapter.notifyDataSetChanged();
    // if (Utils.isEmpty(list)) {
    // emptyView.setVisibility(View.VISIBLE);
    // } else {
    // emptyView.setVisibility(View.GONE);
    // }
    // } else {
    // if (mAlumList != null) {
    // list = mAlumList;
    // }
    // onUpdateAdapter(false);
    // }
    //
    // }
    //

    private boolean isSearchMode() {
        if (searchBar.getVisibility() == ViewGroup.VISIBLE) {
            return true;
        }
        return false;
    }

    public boolean onOpenAlbum(AlbumEntity albumEntity) {
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.EVENT_ID, UMutils.ID.JoinAlbumViaBanner);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        startActivity(intent);
        albumEntity.setUpdateCount(0);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (mDelete) {
            return;
        }
        if (Utils.isClick()) {
            return;
        }
        List<AlbumEntity> list = null;
        if (isSearchMode()) {
            list = searchResults;
        } else {
            list = mAlumList;
        }

        if (list == null && isSearchMode()) {
            hideSearchBar();
        }

        if (list != null && position < list.size()) {
            if (DEBUG) {
                LogUtil.d(TAG, "position + onItemClick : " + position);
            }
            if (albumListener != null) {
                albumListener.openAlbum(list.get(position));
            }
        }
        // else {
        // if (createJoinAlbumDialog == null) {
        // createJoinAlbumDialog = new CreateJoinAlbumDialog();
        // }
        // createJoinAlbumDialog.show(list);
        // }
    }

    private void handleJoinAlbumError(Error errors) {
        if (errors != null) {
            if (errors.toErrorCode() == ErrorCode.NOT_ALLOWED) {
                CToast.showToast(R.string.can_not_allow_join);
            }
            if (errors.toErrorCode() == ErrorCode.USER_BLOCKED) {
                CToast.showToast(R.string.user_blocked);
            }
            if (errors.toErrorCode() == ErrorCode.ALBUM_LOCKED) {
                CToast.showToast(R.string.album_locked);
            }

        }

    }

    // private void deleteAlbum(final AlbumEntity albumEntity) {
    // AlertDialog.Builder deleteAlbum = new AlertDialog.Builder(
    // AlbumFragment.this.getActivity());
    // deleteAlbum.setTitle(getString(R.string.confirm_to_delete_album_x,
    // albumEntity.getName()));
    // DialogInterface.OnClickListener listener = new
    // DialogInterface.OnClickListener() {
    // public void onClick(DialogInterface dialog, int which) {
    // if (which == DialogInterface.BUTTON_POSITIVE) {
    // ConnectBuilder.deleteAlbum(albumEntity.getId());
    // UMutils.instance().diyEvent(ID.EventRemoveAlbum);
    // } else {
    // mDelete = false;
    // mAlbumAdapter.setIsDelete(mDelete);
    // }
    // dialog.cancel();
    // }
    // };
    // deleteAlbum.setPositiveButton(R.string.confirm, listener);
    // deleteAlbum.setNegativeButton(R.string.cancel, listener);
    // deleteAlbum.show();
    // }
    //
    // private void leaveAlbum(final AlbumEntity albumEntity) {
    // AlertDialog.Builder deleteAlbum = new AlertDialog.Builder(
    // AlbumFragment.this.getActivity());
    // deleteAlbum.setTitle(getString(R.string.confirm_to_quit_album_x,
    // albumEntity.getName()));
    // DialogInterface.OnClickListener listener = new
    // DialogInterface.OnClickListener() {
    // public void onClick(DialogInterface dialog, int which) {
    // if (which == DialogInterface.BUTTON_POSITIVE) {
    // ConnectBuilder
    // .leaveAlbum(albumEntity.getId(), App.getUid());
    // } else {
    // mDelete = false;
    // mAlbumAdapter.setIsDelete(mDelete);
    // }
    // dialog.cancel();
    // }
    // };
    // deleteAlbum.setPositiveButton(R.string.confirm, listener);
    // deleteAlbum.setNegativeButton(R.string.cancel, listener);
    // deleteAlbum.show();
    // }

    private void sortAlbum() {
        if (mSort == null) {
            return;
        }
        // if (mSort == AlbumSort.UPDATED_COUNT) {
        // Utils.sortByUpdateCount(mAlumList, true);
        // } else if (mSort == AlbumSort.CREATED_DATE) {
        // Utils.sortByCreateTime(mAlumList, true);
        // }
        if (mSort == AlbumSort.UPDATED_DATE) {
            Utils.sortByUpdateTime(mAlumList, true);
        } else if (mSort == AlbumSort.ITEM_COUNT) {
            Utils.sortByItemCount(mAlumList, true);
        } else if (mSort == AlbumSort.OWNER) {
//            Utils.sortByAlbumOwner(mAlumList, false);
            mAlumList = Utils.sortByAlbumOwnerList(mAlumList, true);
        }
    }

    @Override
    public boolean onBackPressed() {

        if (hideSearchBar()) {
            if (mDelete) {
                mDelete = false;
                mAlbumAdapter.setIsDelete(mDelete);
                return true;
            } else {
                return true;
            }
        } else if (mDelete) {
            mDelete = false;
            mAlbumAdapter.setIsDelete(mDelete);
            return true;
        }

        return super.onBackPressed();
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        AlbumSort albumSort = mSort;
//        if (optionId != R.string.cancel) {
//            lastSort = position;
//        }
        // if (optionId == R.string.sort_by_file_updeted_count) {
        // albumSort = AlbumSort.UPDATED_COUNT;
        // } else
        if (optionId == R.string.sort_by_updated_date) {
            // albumSort = AlbumSort.CREATED_DATE;
            albumSort = AlbumSort.UPDATED_DATE;
            UMutils.instance().diyEvent(ID.EventSortAlbumByMod);
        } else if (optionId == R.string.sort_by_file_count) {
            albumSort = AlbumSort.ITEM_COUNT;
            UMutils.instance().diyEvent(ID.EventSortAlbumBySize);
        } else if (optionId == R.string.sort_by_owner) {
            albumSort = AlbumSort.OWNER;
            UMutils.instance().diyEvent(ID.EventSortAlbumByOwner);
        }
        if (albumSort == mSort) {
            return;
        }
        mSort = albumSort;
        SimpleData data = new SimpleDataImpl(Consts.BASIC_CONFIG);
        data.putInt(Consts.ALBUM_SORT, mSort.ordinal());
        AppData.setAlbumSort(mSort.ordinal());
        onUpdateAdapter(true);
    }

    // @Override
    // public boolean onItemLongClick(AdapterView<?> parent, View view,
    // int position, long id) {
    // if (mAlumList == null) {
    // return false;
    // }
    // if (mAlumList.size() == 0) {
    // return false;
    // }
    // if (position < mAlumList.size()) {
    // mDelete = true;
    // mAlbumAdapter.setIsDelete(mDelete);
    // return true;
    // }
    // return false;
    // }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == Consts.REQUEST_CODE_ZXING) {
                try {
                    if (!data.hasExtra(Consts.ZXING_RESULT)) {
                        return;
                    }
                    String result = data.getStringExtra(Consts.ZXING_RESULT);
                    if (DEBUG) {
                        LogUtil.d(TAG, "onActivityResult ZXING_RESULT:" + result);
                    }
                    if (result.contains(Consts.URL_ENTITY_VIEWER)) {
                        int index = result.indexOf("=");
                        if (index == -1) {
                            return;
                        }
                        int length = 9;
                        index += 1;
                        int last = index + length;
                        if (last >= result.length()) {
                            return;
                        }
                        String link = result.substring(index, last);
                        ConnectBuilder.getTempTokenByLink(link, null);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "initUI  --link--" + link);
                        }
                    } else {
                        JSONObject jo = new JSONObject();
                        jo.put(Consts.USER_ID, App.getUid());
                        ConnectBuilder.joinAlbum(result, jo.toJSONString());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

}
