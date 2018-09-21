package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.AlbumListener;
import com.daxiangce123.android.listener.GetNearyAlbumCoverListener;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.adapter.FindAlbumPagerAdapter;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * @author Ram
 * @project DaXiangCe
 * @time 2014-6-1
 */
public class FindAlbumTabFragment extends BaseFragment implements OnClickListener, OnItemClickListener, OnScrollListener {

    private final static String TAG = "NearbyAlbumFragment";
    private final int DIVIDER_TO_REFRESH_ALBUM_SAMPLES = 2 * 60 * 1000;
    private final int MAX_INPUT_COUNT = 25;
    private View contentView;
    /**
     * search view
     */
    private View searchBar;
    private ImageView mSearch;
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
    private PullToRefreshListView refreshListView;

    private View emptyView;

    private ListAllAlbums searchResults;

    // private NearbyAlumList nearbyAlumList;
    private NearbyAlbumAdapter nearbyAlbumAdapter;
    /**
     * albumId, sample file list
     */
    private HashMap<String, List<FileEntity>> sampleMap;
    /**
     * all albumId, needPassword
     */
    private HashMap<String, TempToken> tokenMap;
    /**
     * all nearby album id
     */
    private HashSet<String> nearbyAlbumSet;
    /**
     * search result album id
     */
    private HashSet<String> searchAlbumSet;
    private AlbumViewerDialog albumViewerDialog;
    private boolean DEBUG = true;
    private AlbumListener albumListener;

    private long lastSampleInMills;
    private final static int PAGE_SIZE = 30;
    private final static int MSG_REFRESH_COMPLETE = 1;

    private HashSet<String> taskMaps;

    private TextView tvHotAlbums;
    private TextView tvActivityAlbums;
    private TextView tvLocalAlbums;

    private HotAlbumFragment hotAlbumFragment;
    private PromotedAlbumFragment promotedAlbumFragment;
    private NearbyAlbumFragment nearbyAlbumFragment;

    private ViewPager vpContainer;
    private ArrayList<Fragment> fragmentsList;
    // private PullToRefreshBase<?> pullRefreshView;
    private View vListView;
    private Map<String, AlbumEntity> albumMap;
    private LinearLayout mTabBar;
    private boolean scrolling = true;
    private HashSet<String> albumCover;


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

    private OnPageChangeListener changeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            updateUI(position);
            UMutils.instance().diyEvent(ID.EventSwipeAlbum);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

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

    private OnRefreshListener2<ListView> onRefreshListener2 = new OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(final PullToRefreshBase<ListView> refreshView) {
            if (DEBUG) {
                LogUtil.d(TAG, "OnRefreshListener2	PULL	DOWN	" + (refreshListView == refreshView));
            }
            boolean isSearchMode = isSearchMode();
            if (isSearchMode) {
                if (Utils.isEmpty(etInput.getText().toString().trim())) {
                    hideSearchBar();
                } else {
                    if (searchResults != null) {
                        searchResults.clear();
                        searchResults = null;
                        notifyDataSetChange();
                    }
                    searchIt(true);
                    return;
                }
            }
            releaseRefreshing();
            // ConnectBuilder.listNearByAlbum(PAGE_SIZE);
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (DEBUG) {
                LogUtil.d(TAG, "OnRefreshListener2	PULL	UP	" + (refreshListView == refreshView));
            }
            boolean isSearchMode = isSearchMode();
            if (isSearchMode) {
                searchIt(false);
            } else {
                // loadMoreNearby();
            }
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.SEARCH_ALBUM.equals(action)) {
                    UMutils.instance().diyEvent(ID.EventSearchedNearbyAlbumSuccess);
                    onSearchAlbum(info, response);
                    releaseRefreshing();
                } else if (Consts.GET_ALBUM_SAMPLE_COVER.equals(action)) {
                    String albumId = info.getTag();
                    Log.d(TAG, "updateAlbumCover " + albumId);
                    updateAlbumCover(response, info);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public FindAlbumTabFragment() {
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
        // updateUI();
        return contentView;
    }

    private View getContentView(LayoutInflater inflater, ViewGroup container) {
        if (inflater == null || container == null) {
            return null;
        }
        return inflater.inflate(R.layout.fragment_find_album_tab, container, false);
    }

    private void initBroadCast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_ALBUM_SAMPLE_COVER);
        // ift.addAction(Consts.GET_ALBUM_COVER);
        ift.addAction(Consts.SEARCH_ALBUM);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initUI() {
        searchAlbumSet = new HashSet<String>();
        nearbyAlbumSet = new HashSet<String>();
        taskMaps = new HashSet<String>();
        tokenMap = new HashMap<String, TempToken>();
        sampleMap = new HashMap<String, List<FileEntity>>();
        albumMap = new HashMap<String, AlbumEntity>();
        albumCover = new HashSet<String>();

        contentView.findViewById(R.id.tv_search_it).setOnClickListener(this);
        mSearch = (ImageView) contentView.findViewById(R.id.iv_show_search);
        mSearch.setOnClickListener(this);

        vClear = contentView.findViewById(R.id.iv_search_clear);
        vClear.setOnClickListener(this);

        searchBar = contentView.findViewById(R.id.search_bar);
        emptyView = contentView.findViewById(R.id.v_empty);
        tvSearchResultCount = (TextView) contentView.findViewById(R.id.iv_search_result_count);

        etInput = (EditText) contentView.findViewById(R.id.et_search_input);
        etInput.addTextChangedListener(textWatcher);

        mTabBar = (LinearLayout) contentView.findViewById(R.id.ll_tab_bar);

        tvHotAlbums = (TextView) contentView.findViewById(R.id.hot_album);
        tvActivityAlbums = (TextView) contentView.findViewById(R.id.activity_album);
        tvLocalAlbums = (TextView) contentView.findViewById(R.id.local_album);

        vListView = (View) contentView.findViewById(R.id.fl_search_album);

        tvHotAlbums.setOnClickListener(new MyOnClickListener(0));
        tvActivityAlbums.setOnClickListener(new MyOnClickListener(1));
        tvLocalAlbums.setOnClickListener(new MyOnClickListener(2));

        initViewPager();
        nearbyAlbumAdapter = new NearbyAlbumAdapter(getActivity());
        // nearbyAlbumAdapter.setOnAlbumListener(nearbyAlbumListener);
        refreshListView = (PullToRefreshListView) contentView.findViewById(R.id.lv_album_results);
        refreshListView.setOnRefreshListener(onRefreshListener2);
        refreshListView.setAdapter(nearbyAlbumAdapter);
        refreshListView.setOnItemClickListener(this);
        // refreshListView.setMode(Mode.BOTH);
//        refreshListView.setMode(Mode.PULL_FROM_END);
        refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);

        nearbyAlbumAdapter.setGetNearyAlbumCoverListener(getNearyAlbumCoverListener);

        // ConnectBuilder.listNearByAlbum(PAGE_SIZE);
        // ConnectBuilder.listHotAlbum(HotType.HOT);
        // LoadingDialog.show(R.string.loading);
    }

    public class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        @Override
        public void onClick(View v) {
            vpContainer.setCurrentItem(index);
        }
    }

    ;

    private void initViewPager() {
        fragmentsList = new ArrayList<Fragment>();

        if (hotAlbumFragment == null) {
            hotAlbumFragment = new HotAlbumFragment();
        }
        if (promotedAlbumFragment == null) {
            promotedAlbumFragment = new PromotedAlbumFragment();
        }

        if (nearbyAlbumFragment == null) {
            nearbyAlbumFragment = new NearbyAlbumFragment();
        }

        fragmentsList.add(hotAlbumFragment);
        fragmentsList.add(promotedAlbumFragment);
        fragmentsList.add(nearbyAlbumFragment);

        vpContainer = (ViewPager) contentView.findViewById(R.id.vp_container_album);
        vpContainer.setAdapter(new FindAlbumPagerAdapter(getChildFragmentManager(), fragmentsList));
        vpContainer.setOnPageChangeListener(changeListener);
        vpContainer.setCurrentItem(0);

    }

    private void updateUI(int position) {
        if (position == 2) {
            tvLocalAlbums.setTextColor(0xff0794e1);
            tvLocalAlbums.setBackgroundResource(R.drawable.files_in_album_corners_bg);

            tvHotAlbums.setTextColor(0xFFFFFFFF);
            tvHotAlbums.setBackgroundResource(R.drawable.transparent);

            tvActivityAlbums.setTextColor(0xFFFFFFFF);
            tvActivityAlbums.setBackgroundResource(R.drawable.transparent);

        } else if (position == 1) {
            tvActivityAlbums.setTextColor(0xff0794e1);
            tvActivityAlbums.setBackgroundResource(R.drawable.files_in_album_corners_bg);
            tvLocalAlbums.setTextColor(0xFFFFFFFF);
            tvLocalAlbums.setBackgroundResource(R.drawable.transparent);
            tvHotAlbums.setTextColor(0xFFFFFFFF);
            tvHotAlbums.setBackgroundResource(R.drawable.transparent);
        } else if (position == 0) {
            tvHotAlbums.setTextColor(0xff0794e1);
            tvHotAlbums.setBackgroundResource(R.drawable.files_in_album_corners_bg);

            tvLocalAlbums.setTextColor(0xFFFFFFFF);
            tvLocalAlbums.setBackgroundResource(R.drawable.transparent);
            tvActivityAlbums.setTextColor(0xFFFFFFFF);
            tvActivityAlbums.setBackgroundResource(R.drawable.transparent);
        }
    }

    private void notifyDataSetChange() {
        List<?> list = null;
        if (isSearchMode()) {
            if (searchResults != null) {
                list = searchResults.getAlbums();
            }
        }
        // else {
        // if (nearbyAlumList != null) {
        // list = nearbyAlumList.getList();
        // }
        // }
        if (Utils.isEmpty(list)) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
        }
        nearbyAlbumAdapter.setData(list);
        nearbyAlbumAdapter.notifyDataSetChanged();
        updateDialog(null);
    }

    private void updateDialog(String albumId) {
        if (albumViewerDialog == null) {
            return;
        }
        if (!albumViewerDialog.isShowing()) {
            return;
        }
        // TODO update single albumView
        albumViewerDialog.notifyDataSetChanged();
    }

    private void showSearchBar() {
        if (searchBar.isShown()) {
            return;
        }
        mSearch.setVisibility(View.GONE);
        vListView.setVisibility(View.VISIBLE);
        emptyView.setVisibility(View.GONE);
        vpContainer.setVisibility(View.GONE);
        mTabBar.setVisibility(View.GONE);
        Utils.showIME();
        etInput.requestFocus();
        searchBar.setVisibility(View.VISIBLE);
    }

    private boolean hideSearchBar() {
        if (isSearchMode()) {
            mSearch.setVisibility(View.VISIBLE);
            searchBar.setVisibility(View.GONE);
            tvSearchResultCount.setText("");
            etInput.setText(null);
            searchAlbumSet.clear();
            if (searchResults != null) {
                searchResults.clear();
                searchResults = null;
            }

            vListView.setVisibility(View.GONE);
            vpContainer.setVisibility(View.VISIBLE);
            mTabBar.setVisibility(View.VISIBLE);
            notifyDataSetChange();
            Utils.hideIME(etInput);
            return true;
        }
        return false;
    }

    private boolean isSearchMode() {
        if (searchBar.getVisibility() == ViewGroup.VISIBLE) {
            return true;
        }
        return false;
    }

    private boolean searchIt(boolean fromStart) {
        Editable text = etInput.getText();
        if (text == null || Utils.isEmpty(text.toString().trim())) {
            CToast.showToast(R.string.havet_input);
            LoadingDialog.dismiss();
            refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            return false;
        }
        int length = text.toString().trim().length();
        if (length >= MAX_INPUT_COUNT) {
            CToast.showToast(getString(R.string.max_string_to_input_is_x, MAX_INPUT_COUNT));
            return false;
        }
        Utils.hideIME(etInput);
        // LoadingDialog.show(getString(R.string.searching_x, text.toString()));
        int startPos = fromStart ? 0 : Utils.sizeOf(searchAlbumSet);
        ConnectBuilder.searchAlbum(text.toString().trim(), startPos, PAGE_SIZE);
        UMutils.instance().diyEvent(ID.EventSearchNearbyAlbum);
        refreshListView.setMode(Mode.PULL_FROM_END);
        return true;
    }

    private void clearInput() {
        etInput.setText("");
        if (searchResults != null) {
            searchResults.clear();
            searchResults = null;
            refreshListView.setMode(PullToRefreshBase.Mode.DISABLED);
            notifyDataSetChange();
        }
    }

    private void onSearchAlbum(ConnectInfo info, Response response) {
        if (!isSearchMode()) {
            return;
        }
        if (response == null || info == null) {
            return;
        }
        if (response.getStatusCode() != 200) {
            LoadingDialog.dismiss();
            return;
        }
        String query = info.getTag();
        String fromStart = info.getTag2();
        if (DEBUG) {
            String content = response.getContent();
            LogUtil.d(TAG, "-------------------------onSearchAlbum()	query=" + query + " \n content=" + content);
        }
        // not current search result
        if (query == null || !query.equals(etInput.getText().toString().trim())) {
            LoadingDialog.dismiss();
            return;
        }
        String content = response.getContent();
        ListAllAlbums items = Parser.parseAlbumList(content);

        if (items == null || Utils.isEmpty(items.getAlbums())) {
            if (fromStart == null) {
                LoadingDialog.dismiss();
                CToast.showToast(R.string.no_more);
                return;
            }
            if (fromStart.equals(Consts.FROM_STARTER)) {
                LoadingDialog.dismiss();
                CToast.showToast(getString(R.string.album_x_has_not_found, query));
                return;
            }
        }

        boolean needRefresh = false;
        String tag = info.getTag2();
        if (Consts.FROM_STARTER.equals(tag)) {
            long now = System.currentTimeMillis();
            needRefresh = (now - lastSampleInMills) >= DIVIDER_TO_REFRESH_ALBUM_SAMPLES;
            if (needRefresh) {
                lastSampleInMills = now;
            }
            searchResults = items;
            searchAlbumSet.clear();
            albumMap.clear();
            albumCover.clear();
        } else {
            if (searchResults == null) {
                searchResults = items;
            } else {
                searchResults.add(items.getAlbums());
            }
        }

        // albumMap.clear();
        ArrayList<AlbumEntity> newList = new ArrayList<AlbumEntity>();
        for (AlbumEntity album : items.getAlbums()) {
            String albumId = album.getId();
            boolean result = sampleMap.containsKey(albumId);
            if (!result || needRefresh) {
                newList.add(album);
                albumMap.put(albumId, album);
                // ConnectBuilder.getAlbumCover(albumId);
            }
            boolean existed = searchAlbumSet.add(albumId);
            if (!existed) {
                searchResults.remove(album);
            }
        }

        // showSearchCount();
        notifyDataSetChange();
        LoadingDialog.dismiss();

        // getAlbumSample(newList, items.getAlbums());
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
        if (searchResults == null) {
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

    private void notifySingle(String albumId) {
        if (nearbyAlbumAdapter == null) {
            return;
        }
        nearbyAlbumAdapter.updateSingle(albumId);
        updateDialog(albumId);
    }

    // private void showSearchCount() {
    // int count = 0;
    // if (searchResults != null) {
    // count = Utils.sizeOf(searchResults.getAlbums());
    // }
    // tvSearchResultCount.setText(getString(R.string.x_related_albums, count));
    // }

    @Override
    public boolean onBackPressed() {
        if (hideSearchBar()) {
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_show_search) {
            showSearchBar();
        } else if (id == R.id.tv_search_it) {
            if (searchResults != null) {
                searchResults.clear();
                searchResults = null;
            }
            notifyDataSetChange();
            LoadingDialog.show(getString(R.string.searching_x, etInput.getText().toString()));
            searchIt(true);


        } else if (id == R.id.iv_search_clear) {
            clearInput();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // if (albumViewerDialog == null) {
        // albumViewerDialog = new AlbumViewerDialog();
        // albumViewerDialog.setOnAlbumListener(nearbyAlbumListener);
        // }
        // albumViewerDialog.setCurrentPosition(position);
        // albumViewerDialog.setData(nearbyAlbumAdapter.getData());

        int realPos = position - 1;
        onOpenAlbum(searchResults.getAlbums().get(realPos));
        UMutils.instance().diyEvent(ID.EventPreviewNearbyAlbum);
    }

    public boolean onOpenAlbum(AlbumEntity albumEntity) {
        // Bundle bundle = new Bundle();
        // bundle.putString(Consts.ALBUM_ID, albumEntity.getId());
        // bundle.putParcelable(Consts.ALBUM, albumEntity);
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinNearbyAlbumSuccess);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        // UIManager.instance().startActivity(SampleAlbumDetailActivity.class,
        // bundle);
        startActivity(intent);
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nearbyAlbumSet != null) {
            nearbyAlbumSet.clear();
            nearbyAlbumSet = null;
        }

        if (searchAlbumSet != null) {
            searchAlbumSet.clear();
            searchAlbumSet = null;
        }

        if (sampleMap != null) {
            sampleMap.clear();
            sampleMap = null;
        }

        if (albumMap != null) {
            albumMap.clear();
            albumMap = null;
        }

        if (tokenMap != null) {
            tokenMap.clear();
            tokenMap = null;
        }

        if (searchResults != null) {
            searchResults.clear();
            searchResults = null;
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

    private void releaseRefreshing() {
        sendMessage(MSG_REFRESH_COMPLETE, 1000);
        if (refreshListView != null) {
            refreshListView.onRefreshComplete();
        }
    }

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
