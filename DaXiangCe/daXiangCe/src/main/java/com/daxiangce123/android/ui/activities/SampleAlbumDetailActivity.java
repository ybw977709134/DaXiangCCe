package com.daxiangce123.android.ui.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.EditText;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.business.event.Signal;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Error;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.HttpTimeOutManger;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.io.Serializable;
import java.util.List;

import de.greenrobot.event.EventBus;

public class SampleAlbumDetailActivity extends AlbumDetailActivity {

    // reciver
    // showItem
    private final String TAG = "SampleAlbumDetailActivity";

    private AlbumSamples items;
    //    private int albumList.size() = 0;
    private Serializable eventId;
    // private String ownerId;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (response == null) {
                    return;
                }
                if (Consts.GET_NON_MEMBER_ALBUM_ITEMS.equals(action)) {
                    if (!albumId.equals(info.getTag())) {
                        return;
                    }
                    if (response.getStatusCode() == 200) {
                        showItems(response);
                        LoadingDialog.dismiss();
                        sendMessage(MSG_REFRESH_COMPLETE, 1000);
                    } else {
                        handle404(info, response);
                    }
                } else if (Consts.GET_USER_INFO.equals(action)) {
                    if (ownerInfo != null) {
                        if (DEBUG) {
                            LogUtil.d(TAG, "Sample ---Consts.GET_USER_INFO  :  " + ownerInfo);
                        }
                        return;
                    }
                    if (albumEntity == null) {
                        return;
                    }
                    if (response.getStatusCode() == 200) {
                        String content = response.getContent();
                        UserInfo user = Parser.parseUserInfo(content);
                        if (user == null) {
                            return;
                        }
                        if (albumEntity == null) {
                            return;
                        }
                        if (albumEntity.getOwner().equals(user.getId())) {
                            ownerInfo = user;
                            notificateFragmentTitleChanged();
                        }
                    }
                } else if (Consts.JOIN_ALBUM.equals(action)) {
                    joinAlbum(response, info);
                } else if (Consts.GET_MEMBER_ROLE.equals(action)) {
                    if (DEBUG) {
                        LogUtil.d(TAG, "respone" + response);
                    }
                    if (response.getStatusCode() == 200) {
                        onGetMemerEntity(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void joinAlbum(Response response, ConnectInfo info) {
        try {
            HttpEntity entity = info.getEntity();
            JSONObject jo = null;
            if (entity instanceof StringEntity) {
                String entityStr = EntityUtils.toString(entity);
                jo = JSONObject.parseObject(entityStr);
            }
            String inviteCode = info.getTag();
            Error errors = response.getError();
            int status = response.getStatusCode();
            if (status == 200) {
                ConnectBuilder.getMemberRole(albumId, App.getUid());
                onUserJoined(response, info);
                CToast.showToast(R.string.join_album_successed);
            } else if (response.getStatusCode() == 401 && errors.toErrorCode() == ErrorCode.INVALID_PASSWORD) {
                if (entity instanceof StringEntity) {
                    if (jo.containsKey(Consts.PASSWORD)) {
                        CToast.showToast(R.string.error_password);
                    } else if (jo.containsKey(Consts.REPORT_COMMENT) || jo.containsKey(Consts.DOWNLOAD_FILE)) {
                        return;
                    } else {
                        inputPassword(info.getTag2(), inviteCode);
                    }
                } else {
                    CToast.showToast(R.string.can_not_allow_join);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("NewApi")
    private void inputPassword(final String albumId, final String inviteCode) {
        final EditText etInputId = new EditText(this);
        AlertDialog.Builder passwordDialog = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            passwordDialog = new AlertDialog.Builder(this);
        } else {
            passwordDialog = new AlertDialog.Builder(this, AlertDialog.THEME_HOLO_LIGHT);
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
                        jo.put(Consts.NOT_OPEN_ALBUM, false);
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


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // SampleAlbumDetailActivity need album to init
        Bundle bundle = this.getIntent().getExtras();
        if (bundle != null && bundle.containsKey(Consts.ALBUM)) {
            albumEntity = bundle.getParcelable(Consts.ALBUM);
            App.albumItemController = new AlbumItemController(albumEntity);
            albumList = App.albumItemController;
        }
        if (bundle != null && bundle.containsKey(Consts.EVENT_ID)) {
            eventId = bundle.getSerializable(Consts.EVENT_ID);
        }
        super.onCreate(savedInstanceState);
        if (App.DEBUG) {
            LogUtil.d(TAG, albumId + "     " + albumEntity);
            LogUtil.d(TAG, "" + TAG + albumId + "     " + albumEntity);
        }
    }

    @Override
    protected void readAlbumAndFetch() {
        if (!Utils.isEmpty(AppData.getToken())) {
            AlbumEntity album = getAlbum();
            if (DEBUG) {
                LogUtil.d(TAG, "albumId " + albumId + " uid " + App.getUid() + "token" + AppData.getToken());
            }
            if (album == null) {
                updateUI();
            } else {
                albumList.setJoined(true);
                setBottomBarJoinedState();
                tvSetting.setVisibility(View.VISIBLE);
                tvAlbumReport.setVisibility(View.GONE);
                timeLineFragment.setIsJoined(albumList.isJoined());
                timeLineFragment.setAlbum(albumEntity);
                // timeLineFragment.setAlbumInfoObj(objStr);
                super.readAlbumAndFetch();
                // ConnectBuilder.getAlbumItems(albumId, 0, 45,
                // Sort.BY_MOD_DATE, Order.DESC,
                // Consts.GET_NON_MEMBER_ALBUM_ITEMS);
                // ConnectBuilder.getAlbumSample(albumId);
                // ConnectBuilder.getAlbumItems(albumId);
                if (DEBUG) {
                    LogUtil.d(TAG, "albumid -------getNonMemberAlbumItems  " + albumId);
                }
            }
        } else {
            updateUI();
        }
    }

    /**
     * refresh ui only use in SampleAlbumDetail
     */
    private void updateUI() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                albumList.setJoined(false);
                initBroadCast();
                if (App.DEBUG) {
                    LogUtil.d(TAG, "updateUI is Running" + albumList.isJoined());
                }
                setBottomBarJoinedState();
                tvSetting.setVisibility(View.GONE);
                if (!Utils.isEmpty(AppData.getToken())) {
                    tvAlbumReport.setVisibility(View.VISIBLE);
                } else {
                    tvAlbumReport.setVisibility(View.GONE);
                }

                timeLineFragment.setIsJoined(albumList.isJoined());
                if (albumEntity != null) {
                    mTitle.setText(albumEntity.getName());
                    timeLineFragment.setAlbum(albumEntity);
                    ConnectBuilder.getUserInfo(albumEntity.getOwner());
                }
                ConnectBuilder.getBatcheList(albumId, albumList.size(), 100);
                HttpTimeOutManger.instance().register(Consts.GET_NON_MEMBER_ALBUM_ITEMS);
                LoadingDialog.show(R.string.loading);
            }

        });
        if (DEBUG) {
            LogUtil.e(TAG, "Cant find album with id : " + albumId);
        }
        if (DEBUG) {
            LogUtil.d(TAG, "albumid -------getNonMemberAlbumItems  " + albumId);
        }
    }

    private void onUserJoined(Response response, ConnectInfo info) {
        canBeShared = true;
        AlbumEntity album = Parser.parseAlbum(response.getContent());
        if (album != null) {
            // TODO need refactor here
            if (album.getId().equals(albumId)) {
                albumList.setJoined(true);
                albumList.setJoinedChangeFlag(true);
                setBottomBarJoinedState();
                tvSetting.setVisibility(View.VISIBLE);
                tvAlbumReport.setVisibility(View.GONE);
                albumEntity.setMembers(albumEntity.getMembers() + 1);
                timeLineFragment.setIsJoined(albumList.isJoined());
                if (detailFragment != null) detailFragment.setIsJoined(albumList.isJoined());
            }
        }
        ConnectBuilder.getAlbumItems(albumId, 0, 100, albumList.getFileSort(), Order.DESC);
    }

    private void initBroadCast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_NON_MEMBER_ALBUM_ITEMS);
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.JOIN_ALBUM);
        Broadcaster.registerReceiver(receiver, ift);
    }

    @Override
    protected void reLoadAlbumItemsFromNet() {
        if (albumList.isJoined()) {
            super.reLoadAlbumItemsFromNet();
        } else {
            sendMessage(MSG_REFRESH_COMPLETE, 1000);
            if (isRefreshing) {
                if (DEBUG) {
                    LogUtil.d(TAG, "onPullDownToRefresh	isRefreshing abort!");
                }
                return;
            }
            isRefreshing = true;
            ConnectBuilder.getAlbumItems(albumId, 0, 45, albumList.getFileSort().getServer_sort(), Order.DESC, Consts.GET_NON_MEMBER_ALBUM_ITEMS);
        }

    }

    @Override
    protected void onMemberLeft(Object member) {
        albumList.setJoined(false);
        // hide bottom bar upload buttun
        setBottomBarJoinedState();
        tvSetting.setVisibility(View.GONE);
        tvAlbumReport.setVisibility(View.VISIBLE);
    }

    protected boolean onGetItems(ConnectInfo info, Response response) {
        if (albumList.isJoined()) {
            return super.onGetItems(info, response);
        }
        if (info == null || response == null) {
            return true;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "onGetItems --- isRefreshing=" + isRefreshing);
        }
        showItems(response);
        return true;
    }


    protected void showItems(Response response) {
        isRefreshing = false;
        if (response.getStatusCode() == 200) {
            items = Parser.parseAlbumSamples(response.getContent(), false);
            if (items == null) {
                return;
            }
            albumList.setHasMore(items.hasMore());
            if (items.hasMore()) {
                if (DEBUG) {
                    LogUtil.d(TAG, "showItems --- albumList.size()" + albumList.size() + "items.size:" + items.getLimit());
                }
            }
            if (items.getFiles() != null) {
                List<FileEntity> fileList = items.getFiles();
                albumList.showItemsForSample(fileList);
            }
            onGetItemsEnd();
            EventBus.getDefault().post(new Signal(Signal.ALBUM_DETAIL_ACTIVITY_LOAD_MORE_OK));
        }
    }


    /**
     * onEventMainThread is callback used in EventBus
     *
     * @param signal
     */
    public void onEventMainThread(Signal signal) {
        switch (signal.action) {
            case Signal.ALBUM_CONTROLLER_LOAD_MORE_EMPTY:
                sendMessage(MSG_REFRESH_COMPLETE, 150);
                if (fileOptionListener.isShowTabBar()) {
                    fileOptionListener.setShowBottomBar(false);
                    setBottomBarState(false, true);
                }
                break;
            case Signal.ALBUM_CONTROLLER_LOAD_MORE_OK:
                if (albumList.isJoined()) {
                    super.onEventMainThread(signal);
                } else {
                    sendMessage(MSG_REFRESH_COMPLETE, 150);
                }
                break;
        }
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
    }

    @Override
    protected void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        HttpTimeOutManger.instance().unRegister(Consts.GET_NON_MEMBER_ALBUM_ITEMS);
        super.onDestroy();
    }

    @Override
    public void onJoinClicked() {
        if (!Utils.isEmpty(AppData.getToken())) {
            JSONObject jo = new JSONObject();
            jo.put(Consts.USER_ID, App.getUid());
            jo.put(Consts.NOT_OPEN_ALBUM, false);
            ConnectBuilder.joinAlbum(albumEntity.getId(), albumEntity.getInviteCode(), jo.toJSONString());
        } else {
            String inviteCode = null;
            if (albumEntity != null) {
                inviteCode = albumEntity.getInviteCode();
            }
            showLogin(inviteCode);
        }
        UMutils.instance().diyEvent(ID.EventJoinAlbumFromPreview);
        if (eventId.equals(UMutils.ID.JoinAlbumViaBanner)) {
            UMutils.instance().diyEvent(ID.JoinAlbumViaBanner);
        } else if (eventId.equals(UMutils.ID.EventJoinNearbyAlbumSuccess)) {
            UMutils.instance().diyEvent(ID.EventJoinNearbyAlbumSuccess);
        } else if (eventId.equals(UMutils.ID.EventJoinHotAlbumSuccess)) {
            UMutils.instance().diyEvent(ID.EventJoinHotAlbumSuccess);
        } else if (eventId.equals(UMutils.ID.EventJoinPromotedAlbumSuccess)) {
            UMutils.instance().diyEvent(ID.EventJoinPromotedAlbumSuccess);
        } else if (eventId.equals(UMutils.ID.EventJoinUserOtherAlbumSuccess)) {
            UMutils.instance().diyEvent(ID.EventJoinUserOtherAlbumSuccess);
        } else if (eventId.equals(UMutils.ID.EventJoinAutoEnterSourceAlbumSuccess)) {
            UMutils.instance().diyEvent(ID.EventJoinAutoEnterSourceAlbumSuccess);
        }
    }

    private void showLogin(String inviteCode) {
        CToast.showToast(R.string.you_not_login);
        Intent intent = new Intent(SampleAlbumDetailActivity.this, LoginActivity.class);
        intent.putExtra(Consts.INVITE_CODE, inviteCode);
        this.startActivity(intent);
    }

    public void showMembers() {
        super.showMembers();
    }

    @Override
    public void onDeleteClicked() {
        if (albumList.isJoined()) {
            super.onDeleteClicked();
        }
    }

    @Override
    protected void showSortDetailFragment(Consts.FileSort fileSort, boolean refreshData) {
        super.showSortDetailFragment(fileSort, refreshData);
        detailFragment.setIsJoined(albumList.isJoined());
        // detailFragment.setAlbumInfoObj(objStr);
        if (albumList.isJoined()) {
            setBottomBarJoinedState();
        } else {
            setBottomBarJoinedState();
        }
    }

}
