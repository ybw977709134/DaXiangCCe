package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.PhotoViewerActivity;
import com.daxiangce123.android.ui.adapter.UserDetailAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.SparseArray;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ram
 * @project Cliq
 * @time Mar 24, 2014
 */
public class UserDetailFragment extends BaseFragment implements OnClickListener {

    private final static String TAG = "UserDetailFragment";
    private View contentView;
    private View emptyView;
    private PullToRefreshListView mPullRefreshListView;
    private ListView lvImagies;
    private List<FileEntity> fileEntities;
    private SparseArray<SparseArray<FileEntity>> datas;
    private UserDetailAdapter imageAdapter;
    private final int numColums = 3;
    private ImageSize imageSize;
    private String curUserId;
    private String albumId;
    private int padding;
    private boolean DEBUG = true;
    private PhotoViewerFragment pictureViewerFragment;
    private AlbumEntity albumEntity = null;
    private boolean isJoined;
    private final int WHAT_ONREFRESHCOMPLETE = 1;
    private boolean isLoadingMore;
    private HashSet<String> loadFiles;
    private int startPos = 0;
    // private AlbumSamples items;
    private LinkedList<FileEntity> list = new LinkedList<FileEntity>();

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);

                if (Consts.GET_EVENTS.equals(action)) {
                    List<Event> events = intent.getParcelableArrayListExtra(Consts.EVENT_LIST);
                    onEvent(events);
                } else if (Consts.DELETE_FILE.equals(action)) {
                    int code = response.getStatusCode();
                    if (code == 200 || code == 404) {
                        String fileId = info.getTag();
                        onFileDeleted(fileId);
                    }
                } else if (Consts.GET_USER_OF_ALBUM_ITEMS.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        getUserOfAlbumItems(response, info);
                    }
                    sendMessage(WHAT_ONREFRESHCOMPLETE);
                    isLoadingMore = false;
                    LoadingDialog.dismiss();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public String getFragmentName() {
        return "UserDetailFragment";
    }

    public UserDetailFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_user_detail, container, false);
            initSize();
            initUI();
            // updateAvater();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        LogUtil.d(TAG, "----------------------------------onCreateView-----------------------------------------------");

        // ConnectBuilder.getUserOfAlbumItems(albumId, curUserId, 0, 100,
        // Sort.BY_MOD_DATE, Order.DESC);
        // LoadingDialog.show(R.string.loading);

        initBroad();
        initData();
        return contentView;
    }

    ;

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.DELETE_FILE);
        ift.addAction(Consts.GET_EVENTS);
        ift.addAction(Consts.GET_USER_OF_ALBUM_ITEMS);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initUI() {

        if (contentView == null) {
            return;
        }
        loadFiles = new HashSet<String>();
        emptyView = contentView.findViewById(R.id.empty_view);

        mPullRefreshListView = (PullToRefreshListView) contentView.findViewById(R.id.lv_images);
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (isLoadingMore) {
                    return;
                }
                // updateData();
                loadMore();
                // sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
            }
        });
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);

        // lvImagies = (ListView) contentView.findViewById(R.id.lv_images);
        lvImagies = mPullRefreshListView.getRefreshableView();
        imageAdapter = new UserDetailAdapter(getActivity(), numColums);
        imageAdapter.setImageSize(imageSize);
        imageAdapter.setSpacing(padding);
        lvImagies.setAdapter(imageAdapter);
        // imageAdapter.setOnItemClickListener(this);
        imageAdapter.setClickListener(this);
        lvImagies.setPadding(padding, 0, padding, 0);
    }

    private void initSize() {
        padding = Utils.dp2px(getActivity(), 10);
        int gridWidth = App.SCREEN_WIDTH - 2 * padding;
        int itemWidth = (gridWidth - ((numColums - 1) * padding)) / numColums;

        imageSize = new ImageSize(itemWidth, itemWidth);
        imageSize.setThumb(true);
        imageSize.setRound(true);

    }

    private void loadMore() {
        if (isLoadingMore) {
            return;
        }
        isLoadingMore = true;

        TaskRuntime.instance().run(new Task() {
            @Override
            public void run() {
                if (isJoined) {
                    DBHelper dbHelper = App.getDBHelper();
                    if (dbHelper == null) {
                        return;
                    }

                    int limit = 30;
                    // int offset = Utils.sizeOf(fileEntities);
                    String WHERE = null;
                    String lastCreate = "" + Integer.MAX_VALUE;
                    int size = Utils.sizeOf(fileEntities);
                    if (size > 0) {
                        lastCreate = fileEntities.get(size - 1).getCreateDate();
                    }
                    WHERE = Consts.ALBUM + "=\"" + albumId + "\" AND " + Consts.OWNER + "=\"" + curUserId + "\" AND " + Consts.CREATE_DATE + "<=\"" + lastCreate + "\"" + " ORDER BY "
                            + Consts.CREATE_DATE + " DESC LIMIT " + limit + "";

                    // WHERE = Consts.ALBUM + "=\"" + albumId + "\" AND "
                    // + Consts.OWNER + "=\"" + curUserId + "\""
                    // + " ORDER BY " + Consts.CREATE_DATE + " DESC LIMIT "
                    // + limit + " OFFSET " + offset;
                    if (DEBUG) {
                        LogUtil.d(TAG, "UserDetailFragment->WHERE " + WHERE);
                    }
                    final LinkedList<FileEntity> list = dbHelper.getList(FileEntity.EMPTY, WHERE);

                    if (list != null) {
                        List<FileEntity> toRemove = new ArrayList<FileEntity>();
                        for (FileEntity file : list) {
                            if (loadFiles.contains(file.getId())) {
                                toRemove.add(file);
                            } else {
                                loadFiles.add(file.getId());
                            }
                        }
                        list.removeAll(toRemove);
                    }

                    runOnUI(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage(WHAT_ONREFRESHCOMPLETE);
                            if (Utils.isEmpty(list)) {
                                CToast.showToast(R.string.no_more);
                            } else {
                                if (fileEntities != null) fileEntities.addAll(list);
                                updateData();
                            }
                            isLoadingMore = false;
                            // progressDialog.dismiss();
                        }
                    });
                } else {
                    ConnectBuilder.getUserOfAlbumItems(albumId, curUserId, startPos, 45, Sort.BY_MOD_DATE, Order.DESC);
                }
            }
        });
    }

    private void initData() {
        if (isJoined) {
            DBHelper dbHelper = App.getDBHelper();
            if (dbHelper == null) {
                if (DEBUG) {
                    LogUtil.d(TAG, "ERROR: dbHelper is null");
                }
                return;
            }

            if (DEBUG) {
                LogUtil.d(TAG, "albumId = " + albumId + "  curUserId = " + curUserId);
            }

            int limit = 30;
            int offset = Utils.sizeOf(fileEntities);
            loadFiles.clear();

            if (DEBUG) {
                LogUtil.d(TAG, "UserDetailFragment->offset " + offset);
            }
            String WHERE = null;
            WHERE = Consts.ALBUM + "=\"" + albumId + "\" AND " + Consts.OWNER + "=\"" + curUserId + "\"" + " ORDER BY " + Consts.CREATE_DATE + " DESC LIMIT " + limit + " OFFSET " + offset;
            if (DEBUG) {
                LogUtil.d(TAG, "UserDetailFragment->WHERE " + WHERE);
            }

            list = dbHelper.getList(FileEntity.EMPTY, WHERE);
            if (DEBUG) {
                LogUtil.d(TAG, "UserDetailFragment->list " + list);
            }
            loadFile();
        } else {
            ConnectBuilder.getUserOfAlbumItems(albumId, curUserId, 0, 45, Sort.BY_MOD_DATE, Order.DESC);
            LoadingDialog.show(R.string.loading);
        }
    }

    private void loadFile() {
        if (list != null) {

            for (FileEntity file : list) {
                loadFiles.add(file.getId());
            }
            fileEntities = list;
        }
        updateData();

    }

    private void getUserOfAlbumItems(Response response, ConnectInfo info) {
        if (!albumId.equals(info.getTag())) {
            return;
        }
        if (!curUserId.equals(info.getTag2())) {
            return;
        }
        AlbumSamples items = Parser.parseAlbumSamples(response.getContent(), false);
        if (items.hasMore()) {
            if (DEBUG) {
                LogUtil.d(TAG, "getUserOfAlbumItems --- startPos" + startPos);
            }
            if (DEBUG) {
                LogUtil.d(TAG, "getUserOfAlbumItems --- startPos" + startPos);
            }

            // ConnectBuilder.getUserOfAlbumItems(albumId, curUserId, startPos,
            // 100, Sort.BY_MOD_DATE, Order.DESC);
        }
        if (items != null) {
            if (App.DEBUG) {
                LogUtil.d(TAG, " -- getUserOfAlbumItems -- " + items + " list " + items.getFiles());
            }
            LinkedList<FileEntity> list = items.getFiles();
            if (Utils.isEmpty(list)) {
                if (startPos != 0) {
                    CToast.showToast(R.string.no_more);
                }

            } else {
                List<FileEntity> toRemove = new ArrayList<FileEntity>();
                for (FileEntity file : list) {
                    if (loadFiles.contains(file.getId())) {
                        toRemove.add(file);
                    } else {
                        loadFiles.add(file.getId());
                    }
                }
                list.removeAll(toRemove);

                if (fileEntities == null) {
                    fileEntities = list;
                } else {
                    fileEntities.addAll(list);
                }
                updateData();
            }
        }
        startPos = startPos + items.getSize();
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
        if (Consts.FILE_DELETED.equals(type) || Consts.SYSTEM_FILE_DELETED.equals(type)) {
            FileEntity fileEntity = (FileEntity) event.getObject();
            onFileDeleted(fileEntity.getId());
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, "handleEvent() type=" + event.getOpType() + " srcDevice=" + event.getSrcDevice() + " uid=" + App.getUid());
        }
    }

    private void onFileDeleted(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return;
        }

        if (fileEntities == null) {
            return;
        }
        for (FileEntity fileEntity : fileEntities) {
            if (!fileId.equals(fileEntity.getId())) {
                continue;
            }
            loadFiles.remove(fileEntity.getId());
            fileEntities.remove(fileEntity);

            imageAdapter.notifyDataSetChanged();

            updateData();
        }
    }

    private void updateData() {
        if (Utils.isEmpty(fileEntities)) {
            datas = null;
            return;
        } else {
            if (DEBUG) {
                LogUtil.d(TAG, "updateData size=" + fileEntities.size());
            }
        }
        if (datas == null) {
            datas = new SparseArray<SparseArray<FileEntity>>();
        } else {
            datas.clear();
        }
        for (int i = 0; i < fileEntities.size(); i++) {
            FileEntity entity = fileEntities.get(i);
            if (entity == null) {
                continue;
            }
            String createData = entity.getCreateDate();
            final long dateInMills = TimeUtil.toLong(createData, Consts.SERVER_UTC_FORMAT);
            final long dateInDays = TimeUtil.toDay(dateInMills);
            int index = datas.indexOfKey(dateInDays);
            SparseArray<FileEntity> entities = null;
            if (index < 0) {
                entities = new SparseArray<FileEntity>();
                datas.put(dateInDays, entities);
            } else {
                entities = datas.getByIndex(index);
            }
            if (DEBUG) {
                LogUtil.d(TAG, i + " date" + dateInMills + " name:" + entity.getName());
            }
            entities.put(dateInMills, entity, false);
        }
        imageAdapter.setData(datas);
        if (Utils.isEmpty(fileEntities)) {
            emptyView.setVisibility(View.VISIBLE);
        } else {
            emptyView.setVisibility(View.GONE);
            imageAdapter.notifyDataSetChanged();
        }
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
        if (App.DEBUG) {
            LogUtil.d(TAG, "setAlbumId() albumId=" + albumId);
        }
    }

    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public void setUserId(String userId) {
        this.curUserId = userId;
    }

    @Override
    public void onClick(View v) {
        if (v.getTag() instanceof FileEntity) {
            FileEntity entity = (FileEntity) v.getTag();
            if (albumEntity != null) {
                viewImageDetail(entity, albumEntity.getOwner());
            }
        }
    }

    private void viewImageDetail(FileEntity entity, String owner) {
        if (owner == null || Utils.isEmpty(fileEntities)) {
            return;
        }
        if (datas == null) {
            return;
        }

        int index = 0;
        int position = 0;
        List<FileEntity> results = new LinkedList<FileEntity>();
        int size = datas.size();
        for (int i = 0; i < size; i++) {
            SparseArray<FileEntity> sa = datas.valueAntiAt(i);
            if (sa == null) {
                continue;
            }
            int saSize = sa.size();
            for (int j = 0; j < saSize; j++) {
                FileEntity fileEntity = sa.valueAntiAt(j);
                if (fileEntity == null) {
                    continue;
                }
                if (fileEntity == entity) {
                    position = index;
                    if (DEBUG) {
                        LogUtil.d(TAG, "position +index " + position + "      --     " + index);
                    }
                }
                results.add(fileEntity);
                index++;
            }
        }

        if (Utils.isEmpty(results)) {
            return;
        }

        App.albumItemController = new AlbumItemController(albumEntity);
        App.albumItemController.add(results,true, false, false);
        App.putAlbum(albumEntity);
        Intent intent = new Intent(this.getActivity(), PhotoViewerActivity.class);
        intent.putExtra(Consts.IS_JOINED,  true);
        intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
        intent.putExtra(Consts.POSITION, position);
        startActivity(intent);


//        if (pictureViewerFragment == null) {
//            pictureViewerFragment = new PhotoViewerFragment();
//        }
//        try {
//            pictureViewerFragment.setCurPosition(position);
//            pictureViewerFragment.setFileList(results);
//            pictureViewerFragment.setAlbumOwner(owner);
//            pictureViewerFragment.setIsJoined(isJoined);
//            if (albumEntity != null) {
//                pictureViewerFragment.setAlbum(albumEntity);
//            }
//            pictureViewerFragment.show(getBaseActivity());
//            UMutils.instance().diyEvent(ID.EventPreview);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        if (hidden) {
            // Broadcaster.unregisterReceiver(receiver);
        } else {
            // initBroad();
        }
        super.onHiddenChanged(hidden);
    }

    @Override
    public void onResume() {
        // initBroad();
        super.onResume();
    }

    @Override
    public void onStop() {
        Broadcaster.unregisterReceiver(receiver);
        super.onStop();
    }

    @Override
    public void handleMessage(Message msg) {
        int what = msg.what;
        if (App.DEBUG) {
            LogUtil.d(TAG, "handleMessage	what=" + what);
        }
        if (what == WHAT_ONREFRESHCOMPLETE) {
            mPullRefreshListView.onRefreshComplete();
        }
    }

}
