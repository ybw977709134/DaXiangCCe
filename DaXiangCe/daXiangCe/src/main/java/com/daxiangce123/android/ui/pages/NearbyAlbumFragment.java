package com.daxiangce123.android.ui.pages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.data.NearbyAlumList;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.AlbumListener;
import com.daxiangce123.android.listener.GetNearyAlbumCoverListener;
import com.daxiangce123.android.manager.HttpTimeOutManger;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.adapter.NearbyAlbumAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.AlbumViewerDialog;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

/**
 * @author Ram
 * @project DaXiangCe
 * @time 2014-6-1
 */
public class NearbyAlbumFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OnScrollListener {

    protected final static String TAG = "NearbyAlbumFragment";
    protected final int DIVIDER_TO_REFRESH_ALBUM_SAMPLES = 2 * 60 * 1000;
    protected final int MAX_INPUT_COUNT = 25;
    protected View contentView;
    protected PullToRefreshListView refreshListView;
    protected View emptyView;
    private NearbyAlumList nearbyAlumList;
    protected NearbyAlbumAdapter nearbyAlbumAdapter;
    protected Map<String, AlbumEntity> albumMap;
    /**
     * all albumId, needPassword
     */
    protected HashMap<String, TempToken> tokenMap;
    /**
     * all nearby album id
     */
    protected HashSet<String> nearbyAlbumSet;
    protected AlbumViewerDialog albumViewerDialog;
    protected boolean DEBUG = true;
    protected AlbumListener albumListener;
    protected long lastSampleInMills;
    private final static int PAGE_SIZE = 30;
    protected final static int MSG_REFRESH_COMPLETE = 1;
    protected HashSet<String> taskMaps;
    protected HashSet<String> albumCover;
    private ArrayList<NearbyAlbum> arrayListNearbyAlbum;
    private boolean scrolling = true;
    private GetNearyAlbumCoverListener getNearyAlbumCoverListener = new GetNearyAlbumCoverListener() {

        @Override
        public void getAlbumCover(AlbumEntity albumEntity) {
            if (scrolling) {

                if (!albumCover.contains(albumEntity.getId())) {
                    // ConnectBuilder.getAlbumCover(albumEntity.getId(),
                    // Consts.GET_ALBUM_SAMPLE_COVER);
                    ConnectBuilder.getAlbumCoverId(albumEntity.getId(), Consts.GET_ALBUM_SAMPLE_COVER);
                    albumCover.add(albumEntity.getId());
                }
            }
        }

        @Override
        public void getAlbumCover(String albumId) {
            // TODO Auto-generated method stub

        }
    };

    protected OnRefreshListener2<ListView> onRefreshListener2 = new OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
            if (DEBUG) {
                LogUtil.d(TAG, "OnRefreshListener2	PULL	DOWN	" + (refreshListView == refreshView));
            }
            pullDownRefresh();
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (DEBUG) {
                LogUtil.d(TAG, "OnRefreshListener2	PULL	UP	" + (refreshListView == refreshView));
            }
            loadMoreNearby();
        }
    };

    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.LIST_NEARBY_ALBUM.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        onGetNearbyAlbums(info, response);
                    }
                    releaseRefreshing();
                } else if (Consts.GET_ALBUM_SAMPLE_COVER.equals(action)) {
                    String albumId = info.getTag();
                    if (response.getStatusCode() == 200) {
                        updateAlbumCover(response, info);
                    } else {
                        albumCover.remove(albumId);
                    }
                }

                // else if (Consts.GET_ALBUM_SAMPLES.equals(action)) {
                // onGetSamples(info, response);
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public NearbyAlbumFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public String getFragmentName() {
        return Utils.getString(R.string.local_albums);
    }

    public void setAlbumListener(AlbumListener openAlbumListener) {
        this.albumListener = openAlbumListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBroadCast();
        if (contentView == null) {
            contentView = getContentView(inflater, container);
            initUI();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        updateUI();
        HttpTimeOutManger.instance().register(Consts.LIST_NEARBY_ALBUM);
        return contentView;
    }

    private View getContentView(LayoutInflater inflater, ViewGroup container) {
        if (inflater == null || container == null) {
            return null;
        }
        return inflater.inflate(R.layout.fragment_local_album, container, false);
    }

    protected void initBroadCast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.LIST_NEARBY_ALBUM);
        ift.addAction(Consts.GET_ALBUM_SAMPLE_COVER);
        // ift.addAction(Consts.GET_ALBUM_COVER);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initUI() {
        nearbyAlbumSet = new HashSet<String>();
        taskMaps = new HashSet<String>();
        tokenMap = new HashMap<String, TempToken>();
        albumMap = new HashMap<String, AlbumEntity>();
        albumCover = new HashSet<String>();
        arrayListNearbyAlbum = new ArrayList<NearbyAlbum>();

        emptyView = contentView.findViewById(R.id.v_empty);

        nearbyAlbumAdapter = new NearbyAlbumAdapter(getActivity());
        refreshListView = (PullToRefreshListView) contentView.findViewById(R.id.lv_album_results);
        refreshListView.setOnRefreshListener(onRefreshListener2);

        // refreshListView.setOnScrollListener(onScrollListener);
        refreshListView.setOnScrollListener(this);
        refreshListView.setAdapter(nearbyAlbumAdapter);
        refreshListView.setOnItemClickListener(this);

        nearbyAlbumAdapter.setGetNearyAlbumCoverListener(getNearyAlbumCoverListener);

        initData();
    }

    protected void initData() {
        ConnectBuilder.listNearByAlbum(PAGE_SIZE);
        // refreshListView.setMode(Mode.PULL_FROM_START);
        refreshListView.setMode(Mode.BOTH);
        LoadingDialog.show(R.string.loading);
    }

    private void updateUI() {

    }

    protected void notifyDataSetChange() {
        List<?> list = null;
        // if (isSearchMode()) {
        // if (searchResults != null) {
        // list = searchResults.getAlbums();
        // }
        // } else {
        if (nearbyAlumList != null) {
            list = nearbyAlumList.getList();
        }
        // }
        nearbyAlbumAdapter.setData(list);
        nearbyAlbumAdapter.notifyDataSetChanged();
        if (Utils.isEmpty(list)) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        updateDialog(null);
    }

    protected void pullDownRefresh() {
        ConnectBuilder.listNearByAlbum(PAGE_SIZE);
    }

    protected void loadMoreNearby() {
        int curSize = 0;
        if (nearbyAlumList != null) {
            // if (!nearbyAlumList.isHasMore()) {
            // CToast.showToast(R.string.no_more);
            // sendMessage(MSG_REFRESH_COMPLETE, 50);
            // return;
            // }
            curSize = Utils.sizeOf(nearbyAlumList.getList());
        }
        ConnectBuilder.listNearByAlbum(curSize, PAGE_SIZE, Sort.BY_MOD_DATE, Order.DESC);
    }

    protected void updateDialog(String albumId) {
        if (albumViewerDialog == null) {
            return;
        }
        if (!albumViewerDialog.isShowing()) {
            return;
        }
        // TODO update single albumView
        albumViewerDialog.notifyDataSetChanged();
    }

    protected void onGetNearbyAlbums(ConnectInfo info, Response response) {
        if (response == null || info == null) {
            LoadingDialog.dismiss();
            return;
        }
        String content = response.getContent();
        if (DEBUG) {
            LogUtil.d(TAG, "------------------onGetNearbyAlbums()	content:" + content);
        }
        NearbyAlumList alumList = Parser.parseNearbyAlbumList(content);
        if (alumList != null) {
            arrayListNearbyAlbum = alumList.getList();
        }
        String tag = info.getTag2();
        if (!Consts.FROM_STARTER.equals(tag)) {
            if (alumList == null || Utils.isEmpty(arrayListNearbyAlbum)) {
                LoadingDialog.dismiss();
                CToast.showToast(R.string.no_more);
                return;
            }
        }
        boolean needRefresh = false;
//		Utils.sortByDistance(arrayListNearbyAlbum, false);


        if (Consts.FROM_STARTER.equals(tag)) {
            long now = System.currentTimeMillis();
            needRefresh = (now - lastSampleInMills) >= DIVIDER_TO_REFRESH_ALBUM_SAMPLES;
            if (needRefresh) {
                lastSampleInMills = now;
            }
            nearbyAlumList = alumList;
            nearbyAlbumSet.clear();
            tokenMap.clear();
            albumCover.clear();
        } else {
            if (nearbyAlumList == null) {
                nearbyAlumList = alumList;
            } else {
                nearbyAlumList.add(arrayListNearbyAlbum);
            }
        }

        for (NearbyAlbum nearbyAlbum : arrayListNearbyAlbum) {
            AlbumEntity album = nearbyAlbum.getAlbum();
            String albumId = album.getId();

            if (DEBUG) {
                LogUtil.d(TAG, "onGetNearbyAlbums albumId: " + albumId);
            }

            albumMap.put(albumId, album);
            // ConnectBuilder.getAlbumCover(albumId);
            boolean added = nearbyAlbumSet.add(albumId);
            if (!added) {
                nearbyAlumList.remove(nearbyAlbum);
            }
        }
        notifyDataSetChange();
        LoadingDialog.dismiss();
    }

    protected void updateAlbumCover(Response response, ConnectInfo connectInfo) {
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
        if (nearbyAlumList == null) {
            return;
        }
        AlbumEntity album = albumMap.get(albumId);

        if (album == null) {
            return;
        }
        if (statusCode == 200) {
            String fileId = Parser.parseAlbumThumId(response.getContent());
            album.setLocalCover(fileId);
            nearbyAlbumAdapter.notifyDataSetChanged();
        }

        // if (statusCode == 200) {
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
        // if (DEBUG) {
        // LogUtil.d(TAG, "updateAlbumCover	cover=" + fileId);
        // }
        // album.setLocalCover(fileId);
        // nearbyAlbumAdapter.notifyDataSetChanged();
        // }
    }

    @Override
    public boolean onBackPressed() {
        // if (hideSearchBar()) {
        // return true;
        // }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        UMutils.instance().diyEvent(ID.EventPreviewNearbyAlbum);
        int realPos = position - 1;
        onOpenAlbum(nearbyAlumList.getList().get(realPos).getAlbum());
        if (DEBUG) {
            LogUtil.d(TAG, "position :" + position);
        }

    }

    protected boolean onOpenAlbum(AlbumEntity albumEntity) {
//        Intent intent = new Intent();
//        intent.putExtra(Consts.ALBUM, albumEntity);
//        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
//        intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinNearbyAlbumSuccess);
//        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
//        startActivity(intent);
//        return true;
        if(App.DEBUG){
            LogUtil.d(TAG," -- albumEntity --  onOpenAlbum "+albumEntity);
        }
        return  ((HomeActivity) getActivity()).onOpenNearbyAlbum(albumEntity);
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        HttpTimeOutManger.instance().unRegister(Consts.LIST_NEARBY_ALBUM);
        super.onDestroy();
        if (nearbyAlbumSet != null) {
            nearbyAlbumSet.clear();
            nearbyAlbumSet = null;
        }

        if (tokenMap != null) {
            tokenMap.clear();
            tokenMap = null;
        }

        if (nearbyAlumList != null) {
            nearbyAlumList.clear();
            nearbyAlumList = null;
        }

        if (albumMap != null) {
            albumMap.clear();
            albumMap = null;
        }

        if (albumCover != null) {
            albumCover.clear();
            albumCover = null;
        }

    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        if (what == MSG_REFRESH_COMPLETE) {
            if (refreshListView != null) {
                refreshListView.onRefreshComplete();
            }
            if (DEBUG) {
                LogUtil.d(TAG, "handleMessage()	onRefreshComplete	" + (refreshListView != null));
            }
        }
    }

    protected void releaseRefreshing() {
        sendMessage(MSG_REFRESH_COMPLETE, 1000);
        if (refreshListView != null) {
            refreshListView.onRefreshComplete();
        }
    }

    // private boolean modifyTokenTask(String albumId, boolean add) {
    // return modifyTask(albumId, Consts.TOKEN, add);
    // }
    //
    // private boolean modifySampleTask(String albumId, boolean add) {
    // return modifyTask(albumId, Consts.FILE_LIST, add);
    // }
    //
    // private boolean modifyTask(String albumId, String action, boolean add) {
    // if (Utils.existsEmpty(albumId, action)) {
    // return false;
    // }
    // String key = albumId + action;
    // if (add) {
    // return taskMaps.add(key);
    // }
    // return taskMaps.remove(key);
    // }

    // protected void getAlbumCover(ArrayList<NearbyAlbum> list) {
    // if (Utils.isEmpty(list)) {
    // return;
    // }
    //
    // for (NearbyAlbum nearbyAlbum : list) {
    // AlbumEntity albumEntity = nearbyAlbum.getAlbum();
    // String thumFileId = albumEntity.getThumbFileId();
    // String albumId = albumEntity.getId();
    //
    // if (Utils.isEmpty(thumFileId)) {
    // if (!albumCover.contains(albumId)) {
    // ConnectBuilder.getAlbumCover(albumId);
    // albumCover.add(albumId);
    //
    // }
    // }
    // }
    //
    // }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        // TODO Auto-generated method stub
        switch (scrollState) {
            case SCROLL_STATE_FLING:
                scrolling = false;
                break;

            case SCROLL_STATE_TOUCH_SCROLL:
                scrolling = true;
                break;

            case SCROLL_STATE_IDLE:
                scrolling = true;
                nearbyAlbumAdapter.notifyDataSetChanged();
                break;
        }

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // TODO Auto-generated method stub

    }

}
