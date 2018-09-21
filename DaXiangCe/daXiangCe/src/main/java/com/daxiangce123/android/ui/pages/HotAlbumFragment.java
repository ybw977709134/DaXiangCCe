package com.daxiangce123.android.ui.pages;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView.OnItemClickListener;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.HotType;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.HotAlumList;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

/**
 * @author Ram
 * @project DaXiangCe
 * @time 2014-6-1
 */
public class HotAlbumFragment extends NearbyAlbumFragment implements OnClickListener, OnItemClickListener {

    // private int curPosition;
    private HotAlumList hotAlumList;

    protected BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.LIST_HOT_ALBUM_HOT.equals(action)) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, " LIST_HOT_ALBUM_HOT " + response);
                    }
                    if (hotAlumList != null) {
                        hotAlumList.clear();
                    }
                    if (nearbyAlbumSet != null) {
                        nearbyAlbumSet.clear();
                    }
                    if (albumCover != null) {
                        albumCover.clear();
                    }
                    onGetNearbyAlbums(info, response);
                    releaseRefreshing();
                } else if (Consts.GET_ALBUM_SAMPLE_COVER.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        updateAlbumCover(response, info);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    protected void initData() {

        ConnectBuilder.listHotAlbum(HotType.HOT, Consts.LIST_HOT_ALBUM_HOT);
        refreshListView.setMode(Mode.PULL_FROM_START);
        LoadingDialog.show(R.string.loading);

    }

    @Override
    protected void initBroadCast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.LIST_HOT_ALBUM_HOT);
        ift.addAction(Consts.GET_ALBUM_SAMPLE_COVER);
        // ift.addAction(Consts.GET_ALBUM_COVER);
        Broadcaster.registerReceiver(receiver, ift);
    }

    @Override
    protected void pullDownRefresh() {
        ConnectBuilder.listHotAlbum(HotType.HOT, Consts.LIST_HOT_ALBUM_HOT);
    }

    // @Override
    // protected void loadMoreNearby() {
    // releaseRefreshing();
    // CToast.showToast(R.string.no_more);
    //
    // }

    @Override
    protected void onGetNearbyAlbums(ConnectInfo info, Response response) {
        if (response == null || info == null) {
            LoadingDialog.dismiss();
            return;
        }
        String content = response.getContent();

        HotAlumList alumList = Parser.parseHotAlbumList(content);
        if (alumList == null || Utils.isEmpty(alumList.getList())) {
            LoadingDialog.dismiss();
            // CToast.showToast(R.string.no_more);
            return;
        }
        boolean needRefresh = false;
        if (hotAlumList == null) {
            hotAlumList = alumList;
        } else {
            hotAlumList.add(alumList.getList());
        }
        for (int i = 0; i < alumList.getList().size(); i++) {
            AlbumEntity album = alumList.getList().get(i);
            String albumId = album.getId();

            if (albumMap == null) {
                albumMap = new HashMap<String, AlbumEntity>();
            }
            albumMap.put(albumId, album);
            // ConnectBuilder.getAlbumCover(albumId);

            if (Utils.isEmpty(nearbyAlbumSet)) {
                nearbyAlbumSet = new HashSet<String>();
            }
            boolean added = nearbyAlbumSet.add(albumId);
            if (!added) {
                hotAlumList.remove(album);
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
        if (hotAlumList == null) {
            return;
        }
        if (albumMap == null) {
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
        //
        // album.setLocalCover(fileId);
        // nearbyAlbumAdapter.notifyDataSetChanged();
        // }
    }

    @Override
    protected void notifyDataSetChange() {
        List<?> list = null;
        // if (isSearchMode()) {
        // if (searchResults != null) {
        // list = searchResults.getAlbums();
        // }
        // } else {
        if (hotAlumList != null) {
            list = hotAlumList.getList();
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

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int realPos = position - 1;

        UMutils.instance().diyEvent(ID.EventClickHotAlbum);
        onOpenAlbum(hotAlumList.getList().get(realPos));

    }

    @Override
    protected boolean onOpenAlbum(AlbumEntity albumEntity) {
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinHotAlbumSuccess);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        startActivity(intent);
        return true;
    }


    @Override
    public void onResume() {
        super.onResume();
        ConnectBuilder.listHotAlbum(HotType.HOT, Consts.LIST_HOT_ALBUM_HOT);
    }

    @Override
    public String getFragmentName() {
        return "HotAlbumFragment";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (nearbyAlbumSet != null) {
            nearbyAlbumSet.clear();
            nearbyAlbumSet = null;
        }

        if (albumMap != null) {
            albumMap.clear();
            albumMap = null;
        }

        if (hotAlumList != null) {
            hotAlumList.clear();
            hotAlumList = null;
        }

    }

}
