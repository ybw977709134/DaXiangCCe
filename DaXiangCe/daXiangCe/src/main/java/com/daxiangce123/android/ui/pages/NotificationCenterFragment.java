package com.daxiangce123.android.ui.pages;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.ClearNotifyCountListener;
import com.daxiangce123.android.listener.GetNearyAlbumCoverListener;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.UIManager;
import com.daxiangce123.android.ui.activities.AlbumDetailActivity;
import com.daxiangce123.android.ui.activities.PhotoViewerActivity;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.NotificationAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class NotificationCenterFragment extends BaseFragment implements OnItemClickListener, OnItemLongClickListener, OptionListener, OnClickListener {
    private final static String TAG = "NotificationCenterFragment";
    private View mRootView = null;
    private NotificationAdapter mNotificationAdapter;
    private ListView lvNotification;
    private ImageSize avaterSize;
    private ImageSize fileSize;
    private List<Event> events;
    private Event event;
    private View emptyView;
    private OptionDialog deleteNotficaionDialog;
    private TextView tvClear;
    // private UserInfo userInfo;
    private boolean DEBUG = true;
    private PhotoViewerFragment pictureViewerFragment;
    private AlbumEntity albumEntity;
    private FileEntity fileEntity;
    private String tmpAlbumId;
    private String tmpFileId;
    private HashSet<String> albumCover;
    private ClearNotifyCountListener clearNotifyCountListener;
    private GetNearyAlbumCoverListener getNearyAlbumCoverListener = new GetNearyAlbumCoverListener() {

        @Override
        public void getAlbumCover(AlbumEntity albumEntity) {

        }

        @Override
        public void getAlbumCover(String albumId) {
            if (!albumCover.contains(albumId)) {
                // ConnectBuilder.getAlbumCover(albumId,
                // Consts.GET_NONE_ALBUM_COVER);
                ConnectBuilder.getAlbumCoverId(albumId, Consts.GET_NONE_ALBUM_COVER);
                albumCover.add(albumId);
            }

        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                String content = response.getContent();
                if (Consts.GET_FILE_INFO.equals(action)) {
                    String fileId = (String) connectInfo.getTag();
                    if (fileId != null && fileId.equals(tmpFileId)) {
                        if (response.getStatusCode() == 200) {
                            FileEntity fileEntity = Parser.parseFile(content);
                            if (fileEntity == null) {
                                return;
                            }
                            DBHelper db = App.getDBHelper();
                            if (db != null) {
                                albumEntity = db.getAlbum(fileEntity.getAlbum());
                            }
                            viewImageDetail(fileEntity, albumEntity);
                        } else if (response.getStatusCode() == 404) {
                            CToast.showToast(R.string.failed_to_fet_file_info);
                            return;
                        }
                        tmpFileId = null;
                    }

                } else if (Consts.GET_ALBUM_INFO.equals(action)) {
                    String albumId = connectInfo.getTag();
                    if (!albumId.equals(tmpAlbumId)) {
                        return;
                    }
                    tmpAlbumId = null;
                    if (response.getStatusCode() == 200) {
                        AlbumEntity albumEntity = Parser.parseAlbum(content);
                        viewImageDetail(fileEntity, albumEntity);
                    }
                } else if (Consts.GET_NONE_ALBUM_COVER.equals(action)) {
                    String albumId = connectInfo.getTag();
                    if (response.getStatusCode() == 200) {
                        updateAlbumCover(response, connectInfo);
                    } else {
                        albumCover.remove(albumId);
                    }
                }
                // else if (Consts.GET_USER_INFO.equals(action)) {
                // if (response.getStatusCode() == 200) {
                // userInfo = Parser.parseUserInfo(content);
                // }
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    public NotificationCenterFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public String getFragmentName() {
        return "NotificationCenterFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.notification_list_view, container, false);
            initCompontent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initBroadcast();
        return mRootView;
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_FILE_INFO);
        // ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.GET_ALBUM_INFO);
        ift.addAction(Consts.GET_NONE_ALBUM_COVER);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initCompontent() {
        mNotificationAdapter = new NotificationAdapter();
        if (lvNotification == null) {

            PullToRefreshListView mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.lv_notification_list);
            lvNotification = mPullRefreshListView.getRefreshableView();
            mPullRefreshListView.setMode(Mode.DISABLED);
            // lvNotification = (ListView)
            // mRootView.findViewById(R.id.lv_notification_list);
            lvNotification.setOnItemClickListener(this);
            lvNotification.setOnItemLongClickListener(this);
            ViewUtil.ajustMaximumVelocity(lvNotification, 3);
        }
        lvNotification.setAdapter(mNotificationAdapter);
        emptyView = mRootView.findViewById(R.id.v_empty_notification);
        tvClear = (TextView) mRootView.findViewById(R.id.tv_clear);
        tvClear.setOnClickListener(this);

        // imagesize
        int height = Utils.dp2px(getActivity(), 55);
        avaterSize = new ImageSize(height, height);
        avaterSize.setCircle(true);
        avaterSize.setThumb(true);
        fileSize = new ImageSize(height, height);
        fileSize.setRound(true);
        fileSize.setThumb(true);
        mNotificationAdapter.setImageSize(avaterSize, fileSize);
        mNotificationAdapter.setClickListener(this);
        mNotificationAdapter.setGetNearyAlbumCoverListener(getNearyAlbumCoverListener);
        showData();
    }

    void delete(Event event) {
        if (event == null) {
            return;
        }
        if (events == null) {
            return;
        }
        events.remove(event);
    }

    void delete(int position) {
        if (events == null) {
            return;
        }
        if (position < 0) {
            return;
        }
        int length = events.size();
        if (position >= length) {
            events.remove(position);
        }
    }

    protected void updateAlbumCover(Response response, ConnectInfo connectInfo) {
        if (response == null || connectInfo == null) {
            return;
        }
        String albumId = connectInfo.getTag();
        if (Utils.isEmpty(albumId)) {
            return;
        }

        // AlbumSamples albumItems =
        // Parser.parseAlbumSamples(response.getContent(), false);
        // if (albumItems == null || Utils.isEmpty(albumItems.getFiles())) {
        // return;
        // }
        // FileEntity file = albumItems.getFiles().getFirst();
        // if (file == null) {
        // return;
        // }
        String fileId = Parser.parseAlbumThumId(response.getContent());
        mNotificationAdapter.setFileAlbum(albumId, fileId);
        // mNotificationAdapter.setFileEntity(file);
        mNotificationAdapter.notifyDataSetChanged();
    }

    public void setClearNotifyCount(ClearNotifyCountListener clearNotifyCountListener) {
        this.clearNotifyCountListener = clearNotifyCountListener;
    }

    private void showData() {
        if (events == null || events.isEmpty()) {
            emptyView.setVisibility(View.VISIBLE);
            tvClear.setVisibility(View.GONE);

        } else {
            emptyView.setVisibility(View.GONE);
            tvClear.setVisibility(View.VISIBLE);
        }
        albumCover = new HashSet<String>();
        Utils.sortByCreatedAt(events, true);
        mNotificationAdapter.setData(events);
        mNotificationAdapter.notifyDataSetChanged();
        // lvNotification.setSelection(0);

    }

    // private void showComment(FileEntity file) {
    // if (DEBUG) {
    // LogUtil.d(TAG,
    // "----------------------------showComment----------------------------");
    // }
    // if (file == null) {
    // return;
    // }
    // DBHelper dbHelper = App.getDBHelper();
    // AlbumEntity album = null;
    // if (dbHelper != null) {
    // album = dbHelper.getAlbum(file.getAlbum());
    // }
    // FragmentCommentLikeInfo commentLikeInfoagment = new
    // FragmentCommentLikeInfo();
    // commentLikeInfoagment.setFile(file);
    // commentLikeInfoagment.setAlbumOwner(album.getOwner());
    // commentLikeInfoagment.setAlbum(album);
    // // commentLikeInfoagment.setUser(userInfo);
    // commentLikeInfoagment.show(this.getBaseActivity());
    //
    // if (DEBUG) {
    // LogUtil.d(TAG, "----------------------------ALBUM:\n" + album
    // + "\n----------------------------FILE:\n" + file);
    // }
    // }

    public boolean openAlbum(String albumId) {
        Bundle bundle = new Bundle();
        bundle.putString(Consts.ALBUM_ID, albumId);
        UIManager.instance().startActivity(AlbumDetailActivity.class, bundle);
        return true;
    }

    private boolean viewImageDetail(FileEntity file, AlbumEntity albumEntity) {
        if (App.DEBUG) {
            LogUtil.d(TAG, " --viewImageDetail--- " + file + " --- " + albumEntity);
        }
        if (file == null || albumEntity == null) {
            return false;
        }
        App.albumItemController = new AlbumItemController(albumEntity);
        App.albumItemController.addFile(file, true, false);
        App.putAlbum(albumEntity);
        Intent intent = new Intent(getActivity(), PhotoViewerActivity.class);
        intent.putExtra(Consts.IS_JOINED, true);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.POSITION, 0);
        startActivity(intent);
        return true;
    }

    private void handlerEvent(Event event, boolean isOpenUser) {
        if (event == null) {
            return;
        }
        Object object = event.getObject();
        String userId = event.getUserId();
        try {
            String fileId = null;
            String albumId = null;
            if (object instanceof CommentEntity) {
                fileId = ((CommentEntity) object).getObjId();
            } else if (object instanceof LikeEntity) {
                fileId = ((LikeEntity) object).getObjId();
            } else if (object instanceof FileEntity) {
                FileEntity fileEntity = (FileEntity) object;
                fileId = fileEntity.getId();
            } else if (object instanceof AlbumEntity) {
                albumId = ((AlbumEntity) object).getId();
            }
            if (fileId != null) {
                DBHelper dbHelper = App.getDBHelper();
                if (dbHelper != null) {
                    fileEntity = dbHelper.getData(FileEntity.EMPTY, fileId);
                }
                if (fileEntity == null) {
                    tmpFileId = fileId;
                    ConnectBuilder.getFileInfo(fileId);
                }

                if (dbHelper != null && fileEntity != null) {
                    albumEntity = dbHelper.getAlbum(fileEntity.getAlbum());
                }
                if (albumEntity == null) {
                    tmpAlbumId = fileEntity.getAlbum();
                    ConnectBuilder.getAlbumInfo(fileEntity.getAlbum());
                }

                if (isOpenUser) {
                    openUserDetails(userId);
                } else {
                    viewImageDetail(fileEntity, albumEntity);
                }

            }

            if (albumId != null) {
                AlbumEntity albumEntity = null;
                if (App.getDBHelper() != null) {
                    albumEntity = App.getDBHelper().getData(AlbumEntity.EMPTY, albumId);
                }
                if (albumEntity != null) {
                    if (isOpenUser) {
                        openUserDetails(userId);
                    } else {
                        openAlbum(albumEntity.getId());
                    }
                } else {
                    CToast.showToast(R.string.album_has_been_delete);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onEvent(List<Event> events) {
        this.events = events;
        if (isHidden() || !isVisible()) {
            return;
        }
        showData();
    }

    private void clearEvents() {
        final ArrayList<Event> list = new ArrayList<Event>(events);
        events.clear();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                DBHelper dbHelper = App.getDBHelper();
                if (dbHelper != null) {
                    try {
                        dbHelper.delete(list);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                tvClear.post(new Runnable() {
                    @Override
                    public void run() {
                        showData();
                    }
                });
                list.clear();
            }
        };
        TaskRuntime.instance().run(runnable);
    }

    private void showClearDialog() {
        AlertDialog.Builder clearNotification = new AlertDialog.Builder(NotificationCenterFragment.this.getActivity());
        clearNotification.setTitle(R.string.confirm_clear);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (events == null || events.isEmpty()) {
                        return;
                    }
                    clearEvents();
                    clearNotifyCountListener.clearNotifyCount();
                } else {
                    dialog.cancel();
                }
                dialog.cancel();
            }
        };
        clearNotification.setPositiveButton(R.string.confirm, listener);
        clearNotification.setNegativeButton(R.string.cancel, listener);
        clearNotification.show();
        UMutils.instance().diyEvent(ID.EventRemoveAllNotifications);
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (optionId == R.string.delete) {
            if (event == null) {
                return;
            }
            DBHelper dbHelper = App.getDBHelper();
            if (dbHelper != null) {
                dbHelper.delete(event);
                delete(event);
                showData();
                UMutils.instance().diyEvent(ID.EventRemoveNotification);
            }
        }
        deleteNotficaionDialog.dismiss();
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        int realPos = position - lvNotification.getHeaderViewsCount();
        if (deleteNotficaionDialog == null) {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            mDatas.add(R.string.delete);
            mDatas.add(R.string.cancel);
            deleteNotficaionDialog = new OptionDialog(getActivity());
            deleteNotficaionDialog.setOptionListener(this);
            deleteNotficaionDialog.setData(mDatas);
        }
        deleteNotficaionDialog.show();
        event = events.get(realPos);
        if (DEBUG) {
            LogUtil.d(TAG, "events.get(position)" + event);
        }
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Object object = view.getTag(view.getId());
        if (!(object instanceof Event)) {
            return;
        }
        Event event = (Event) object;
        handlerEvent(event, false);
    }

    @Override
    public void onHidden() {
        super.onHidden();
//        Broadcaster.unregisterReceiver(receiver);
        if (albumCover != null) {
            albumCover.clear();
            albumCover = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Broadcaster.unregisterReceiver(receiver);
    }

    @Override
    public void onShown() {
        super.onShown();
//        initBroadcast();
        showData();
    }

    @Override
    public void onClick(View v) {
        if (v.equals(tvClear)) {
            showClearDialog();
        } else if (v.getId() == R.id.iv_notification_avater) {
            if (v.getTag() instanceof Event) {
                Event event = (Event) v.getTag();
                // openUserDetails(event.getUserId());
                if (Consts.SYSTEM_FILE_DELETED.equals(event.getOpType()) || Consts.SYSTEM_ALBUM_DELETED.equals(event.getOpType())) {
                    return;
                }
                handlerEvent(event, true);
            }
        }
    }

    private void openUserDetails(String userId) {
        if (App.DEBUG) {
            LogUtil.d(TAG, " openUserDetails->userId" + userId);
        }
        if (Utils.isEmpty(userId)) {
            return;
        }
        try {
            Intent intent = new Intent();
            intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
            intent.putExtra(Consts.USER_ID, userId);
            // if (!isJoined) {
            intent.putExtra(Consts.ALBUM, albumEntity);
            // }
            // intent.putExtra(Consts.IS_JOINED, isJoined);
            intent.setClass(this.getActivity(), UserDetailActivity.class);
            intent.putExtra(Consts.TIME, System.currentTimeMillis());
            this.getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
