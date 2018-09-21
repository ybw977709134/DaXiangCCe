package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.adapter.UserOtherAlbumAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ListGrid;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ram
 * @project Cliq
 * @time Mar 24, 2014
 */
public class UserDetailOtherAlbumFragmentForContact extends BaseFragment implements OnClickListener, OnItemClickListener {

    private final static String TAG = "UserDetailFragment";
    private View contentView;
    private View emptyView;
    private TextViewParserEmoji ownerName;
    private TextView ownerAlbumSize;
    private ListGrid gvImagies;
    private UserOtherAlbumAdapter imageAdapter;
    private Bitmap albumCover;
    private String curUserId;
    private boolean DEBUG = true;
    private List<AlbumEntity> mAlumList = null;
    private Map<String, AlbumEntity> albumMap;
    private int startPos;
    private boolean isJoined;
    private ImageView mBack, mAvatar;
    private UserInfo userInfo;


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.GET_USER_INFO.equals(action)) {
                    onGetUserInfo(response);
                } else if (Consts.LIST_OTHER_USER_ALBUM.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        onListAlbums(response, info);
                    }
                } else if (Consts.GET_ALBUM_COVER.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        updateAlbumCover(response, info);
                    }
                } else if (Consts.DELETE_ALBUM.equals(action)) {
                    onDeleteAlbum(response, info);
                } else if (Consts.LEAVE_ALBUM.equals(action)) {
                    onLeaveAlbum(response, info);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public String getFragmentName() {
        return "UserDetailOtherAlbumFragment";
    }

    public UserDetailOtherAlbumFragmentForContact() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_user_detail_other_album_contact, container, false);
            initUI(inflater, container);
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        initBroad();
        setDefaultCover();
        initData();
        return contentView;
    }

    private void initData() {
        ConnectBuilder.listAlbum(curUserId, 0, 100, Sort.BY_MOD_DATE, Order.DESC);
        ConnectBuilder.getUserInfo(curUserId);
        LoadingDialog.show(R.string.loading);
        ImageManager.instance().loadAvater(mAvatar, curUserId);


    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.LIST_OTHER_USER_ALBUM);
        ift.addAction(Consts.GET_ALBUM_COVER);
        ift.addAction(Consts.DELETE_ALBUM);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.GET_USER_INFO);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void onListAlbums(Response response, ConnectInfo info) {
        if (startPos == 0) {
            albumMap.clear();
            if (mAlumList != null) {
                mAlumList.clear();
            }
        }

        ListAllAlbums listAllAlbums = Parser.parseAlbumList(response.getContent());
        String uid = info.getTag();

        if (Utils.existsEmpty(curUserId, uid) || !uid.equals(curUserId)) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.d("onListAlbums", "onListAlbums	getStatusCode=" + response.getStatusCode());

        }
        if (listAllAlbums == null) {
            return;
        }

        List<AlbumEntity> list = listAllAlbums.getAlbums();
        if (list == null) {
            onUpdateAdapter(true);
            return;
        }
        if (listAllAlbums.hasMore()) {
            startPos += list.size();
            ConnectBuilder.listAlbum(curUserId, startPos, 100, Sort.BY_MOD_DATE, Order.DESC);
        } else {
            startPos += list.size();
            ownerAlbumSize.setText(getString(R.string.x_album, String.valueOf(startPos)));
            startPos = 0;
        }
        try {
            for (AlbumEntity album : list) {
                albumMap.put(album.getId(), album);
                if (DEBUG) {
                    LogUtil.d(TAG, " first album  : " + list.get(0).getId());
                }
                // ConnectBuilder.getAlbumCover(album.getId());
                ConnectBuilder.getAlbumCoverId(album.getId());
            }
            if (mAlumList == null) {
                mAlumList = list;
            } else {
                mAlumList.addAll(list);
                list.clear();
                list = null;
            }
            if (DEBUG) {
                LogUtil.d(TAG, " mAlumList  : " + mAlumList.size());
            }
            onUpdateAdapter(true);
        } catch (Exception e) {
            e.printStackTrace();
            onUpdateAdapter(true);
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
        if (statusCode == 200) {
            String fileId = Parser.parseAlbumThumId(response.getContent());
            album.setLocalCover(fileId);
            if (imageAdapter != null) {
                imageAdapter.updateSingle(albumId);
            }
            if (DEBUG) {
                LogUtil.d("updateAlbumCoverFrag", "Fragment	updateAlbumCover ---	cover =  " + fileId + "  ----- " + album.getTrueCover() + "  time :   " + System.currentTimeMillis()
                        + "  -- albumID =   " + album.getId());
            }
        }
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
            String userId = info.getTag2();

            if (!userId.equals(curUserId)) {
                return;
            }
            if (DEBUG) {
                LogUtil.d(TAG, "LEAVE_ALBUM userId = " + albumId);
            }
            if (onAlbumDeleted(albumId)) {
                CToast.showToast(R.string.quite_album_succeed);
                return;
            }
        }
        CToast.showToast(R.string.request_failed);
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

        onUpdateAdapter(false);
        return true;
    }

    private void onUpdateAdapter(boolean sort) {
        LoadingDialog.dismiss();
        if (Utils.isEmpty(mAlumList)) {
            emptyView.setVisibility(View.VISIBLE);
            if (gvImagies != null && gvImagies.getVisibility() != View.GONE) {
                gvImagies.setVisibility(View.GONE);
            }
        } else {
            emptyView.setVisibility(View.GONE);
            if (gvImagies != null && gvImagies.getVisibility() != View.VISIBLE) {
                gvImagies.setVisibility(View.VISIBLE);
            }
        }
        imageAdapter.setData(mAlumList);
        imageAdapter.notifyDataSetChanged();

    }

    public void setDefaultCover() {
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image_large);
        bitmap = BitmapUtil.squareBitmap(bitmap);
        albumCover = BitmapUtil.toRoundCorner(bitmap);
        albumCover = BitmapUtil.rotateOverlay(albumCover);
        if (imageAdapter != null) {
            imageAdapter.setCover(albumCover);
        }
    }

    private void initUI(android.view.LayoutInflater inflater, ViewGroup container) {
        if (contentView == null) {
            return;
        }
        mBack = (ImageView) contentView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        albumMap = new HashMap<>();
        emptyView = contentView.findViewById(R.id.empty_view);
        gvImagies = (ListGrid) contentView.findViewById(R.id.lv_images);
        final int numColums = 2;
        final int padding = Utils.getDip(20);
        // final int hSpaceing = padding;
        imageAdapter = new UserOtherAlbumAdapter(getActivity());
        imageAdapter.setClickListener(this);
        imageAdapter.setCover(albumCover);
        int gvWidth = App.SCREEN_WIDTH - padding * 2;
        int width = (gvWidth - (numColums - 1) * padding) / numColums;
        ImageSize imageSize = new ImageSize(width, width);
        imageSize.setThumb(true);
        imageAdapter.setImageSize(imageSize);
        LinearLayout header = (LinearLayout) inflater.inflate(R.layout.detial_title_item, null, false);
        ownerName = (TextViewParserEmoji) header.findViewById(R.id.tv_user_name);
        ownerAlbumSize = (TextView) header.findViewById(R.id.tv_album_count);
        mAvatar = (ImageView) header.findViewById(R.id.iv_avatar);
        gvImagies.setRowCount(2);
        gvImagies.addHeaderView(header);
        gvImagies.setAdapter(imageAdapter);
        gvImagies.setOnItemClickListener(this);
        gvImagies.setPadding(0, 0, 0, 0);
        imageAdapter.setClickListener(this);


    }

    public void setUserId(String userId) {
        this.curUserId = userId;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            back();
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        openAlbum(mAlumList.get(position));
        UMutils.instance().diyEvent(ID.EventUserOtherAlbumPreview);

    }

    private void openAlbum(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "onOpenAlbum	albumEntity" + albumEntity.getName());
        }
        // unreadFileNum = unreadFileNum - albumEntity.getUpdateCount();
        // updateAlbumNotify();
        // albumEntity.setUpdateCount(0);
        if (isJoined) {
            DBHelper dbHelper = App.getDBHelper();
            if (dbHelper == null) {
                return;
            }
            dbHelper.execute("UPDATE " + albumEntity.getTableName() + " SET " + Consts.UPDATE_COUNT + "=0 WHERE " + Consts.ALBUM_ID + "=\"" + albumEntity.getId() + "\"");
        }

        onOpenAlbum(albumEntity);
    }

    private void onGetUserInfo(Response response) {
        String jsonstr = response.getContent();
        UserInfo user = Parser.parseUserInfo(jsonstr);
        if (user.getId().equals(curUserId)) {
            userInfo = user;
        }
        ownerName.setText(userInfo.getName());
    }

    public boolean onOpenAlbum(AlbumEntity albumEntity) {
        // Bundle bundle = new Bundle();
        // bundle.putString(Consts.ALBUM_ID, albumEntity.getId());
        // bundle.putParcelable(Consts.ALBUM, albumEntity);
        Intent intent = new Intent();
        intent.putExtra(Consts.ALBUM, albumEntity);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.EVENT_ID, ID.EventJoinUserOtherAlbumSuccess);
        intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
        // UIManager.instance().startActivity(SampleAlbumDetailActivity.class,
        // bundle);
        startActivity(intent);
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

}
