package com.daxiangce123.android.ui.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.listener.OnDetialListener;
import com.daxiangce123.android.listener.OnFileOptionListener;
import com.daxiangce123.android.listener.OnPullListener;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.EmptyFileInTimeLineAdapter;
import com.daxiangce123.android.ui.adapter.TimeLineAdapter;
import com.daxiangce123.android.ui.pages.base.BaseAlbumFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.PhotoView;
import com.daxiangce123.android.ui.view.StickyHeaderListview;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.SparseArray;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AlbumTimeLineFragment extends BaseAlbumFragment implements OnFileOptionListener, OnLongClickListener {// OnItemCckListener,

    private static final String TAG = "AlbumTimeLineFragment";
    private View mRootView = null;

    private OnDetialListener detailListener;
    private StickyHeaderListview lvTimelineList;
    private TimeLineAdapter mTimelineListAdapter = null;
    private EmptyFileInTimeLineAdapter emptyFileAdapter;


    /**
     * batchId -> files
     */
    private HashMap<String, SparseArray<FileEntity>> batchData;
    /**
     * batchId -> files
     */
    private HashSet<FileEntity> selectedData = new HashSet<FileEntity>();
    /**
     * batchId(s) put all batch in this and sort files
     */
    private SparseArray<String> batchIndex;


    private String owner;
//    private LinearLayout emptyView;

    private OnRefreshListener2<ListView> onRefreshListener2 = new OnRefreshListener2<ListView>() {
        @Override
        public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (onPullListener == null) {
                return;
            }
            onPullListener.onPullDownToRefresh(refreshView, getPageSize());
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
            if (onPullListener == null) {
                return;
            }
            onPullListener.onPullUpToRefresh(refreshView, getPageSize());
        }
    };

    // private boolean selected;

    @Override
    public String getFragmentName() {
        return "AlbumTimeLineFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.image_timeline_list_view, container, false);
            initCompontent();
            // LoadingDialog.show(R.string.loading);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        LoadingDialog.show(R.string.loading);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onCreateView	show(R.string.loading)");
        }
        initData(fileList);
        return mRootView;
    }

    private void initCompontent() {
        mTimelineListAdapter = new TimeLineAdapter(getActivity());
        emptyFileAdapter = new EmptyFileInTimeLineAdapter(getActivity());
        mTimelineListAdapter.setClickListener(this);
        mTimelineListAdapter.setLongClickListener(this);
        mTimelineListAdapter.setData(batchData, selectedData, batchIndex);
        mTimelineListAdapter.setIsJoined(isJoined);
        mTimelineListAdapter.setOnActionListenerra(albumActivityActionListener);
        lvTimelineList = (StickyHeaderListview) mRootView.findViewById(R.id.lv_image_timeline_sort);
        initListHeader(null);
        checkEmpty();
//        lvTimelineList.setAdapter(mTimelineListAdapter);
        lvTimelineList.setOnRefreshListener(onRefreshListener2);
        lvTimelineList.setMode(Mode.BOTH);
//        emptyView = (LinearLayout) mRootView.findViewById(R.id.ll_empty_album);

        ViewUtil.ajustMaximumVelocity(lvTimelineList.getRefreshableView(), Consts.SLOW_ABS_SCROLL_RATION);
    }

    private void initData(List<FileEntity> list) {
        if (batchData == null) {
            batchData = new HashMap<String, SparseArray<FileEntity>>();
        } else {
            batchData.clear();
        }
        if (batchIndex == null) {
            batchIndex = new SparseArray<String>();
        } else {
            batchIndex.clear();
        }
        if (Utils.isEmpty(list)) {
            return;
        }
        long start = System.currentTimeMillis();
        synchronized (list) {
            for (FileEntity entity : list) {
                addToBatchData(entity);
            }
        }
        if (App.DEBUG) {
            long duration = System.currentTimeMillis() - start;
            LogUtil.d(TAG, "without duration:" + duration + "	index:" + batchIndex.size() + "<>" + batchData.size());
        }
    }

    private void checkEmpty() {
        if (App.DEBUG) {
            LogUtil.d(TAG,
                    "checkEmpty() fileList size = " + Utils.sizeOf(fileList));
        }
        if (Utils.isEmpty(fileList)) {
//            emptyView.setVisibility(View.VISIBLE);
            lvTimelineList.setAdapter(emptyFileAdapter);
        } else {
//            emptyView.setVisibility(View.GONE);
            lvTimelineList.setAdapter(mTimelineListAdapter);
        }
    }

    private void onOpenUserDetails(String uid) {
        if (Utils.isEmpty(uid)) {
            LogUtil.w(TAG, "INVALID USER ID!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            return;
        }

        Set<Entry<String, SparseArray<FileEntity>>> set = batchData.entrySet();
        if (set == null || set.isEmpty()) {
            if (App.DEBUG) {
                LogUtil.w(TAG, "EMPTY DATA");
            }
            return;
        }

        long startInMill = System.currentTimeMillis();
        // LinkedList<FileEntity> fileEntities = new LinkedList<FileEntity>();

        Iterator<Entry<String, SparseArray<FileEntity>>> it = set.iterator();
        FileEntity file = new FileEntity();
        try {
            for (; it.hasNext(); ) {
                Entry<String, SparseArray<FileEntity>> entry = it.next();
                SparseArray<FileEntity> sparseArray = entry.getValue();
                if (sparseArray == null || sparseArray.size() == 0) {
                    continue;
                }
                FileEntity fileEntity = sparseArray.getByIndex(0);
                // file = sparseArray.getByIndex(0);
                if (!uid.equals(fileEntity.getOwner())) {
                    continue;
                }
                for (int i = 0; i < sparseArray.size(); i++) {
                    // FileEntity entity = sparseArray.valueAt(i);
                    file = sparseArray.valueAt(i);
                    // fileEntities.add(file);
                    if (App.DEBUG) {
                        LogUtil.d(TAG, i + " name:" + file.getName());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            Intent intent = new Intent();
            // intent.putExtra(Consts.IMAGE_LIST, fileEntities);
            intent.putExtra(Consts.ALBUM_ID, file.getAlbum());
            intent.putExtra(Consts.USER_ID, file.getOwner());
            intent.putExtra(Consts.IS_JOINED, isJoined);
            if (!isJoined) {
                intent.putExtra(Consts.ALBUM, albumEntity);
            }
            intent.setClass(getActivity(), UserDetailActivity.class);
            intent.putExtra(Consts.TIME, System.currentTimeMillis());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setOnPullListener(OnPullListener listener2) {
        this.onPullListener = listener2;
    }


    public void setDetailListener(OnDetialListener detailListener) {
        this.detailListener = detailListener;
    }

    @Override
    public void selectAll(boolean selected) {
        // this.selected = selected;
        if (owner == null) {
            return;
        }
        if (owner.equals(App.getUid())) {
            ownedList = fileList;
            if (App.DEBUG) {
                LogUtil.d(TAG, "current user is OWNER owner size is " + ownedList.size());
            }
        }
        if (ownedList == null || ownedList.isEmpty()) {
            return;
        }
        onAllSelected(selected);
        mTimelineListAdapter.notifyDataSetChanged();
    }

    public void setCancelSelect() {
        deleteMode = false;
        if (selectedData != null) {
            selectedData.clear();
        }
        onStateChanged();
        mTimelineListAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean showDeleteMode() {
        if (owner != null && owner.equals(App.getUid())) {
            if (App.DEBUG) {
                LogUtil.d(TAG, "owner is ME");
            }
            ownedList = fileList;
        } else {
            // if (ownedList == null) {
            ownedList = new LinkedList<FileEntity>();
            // } else {
            // ownedList.clear();
            // }
            for (int index = 0; index < fileList.size(); index++) {
                FileEntity fileEntity = fileList.get(index);
                if (fileEntity.getOwner().equals(App.getUid()) && (!fileEntity.isUploading())) {
                    ownedList.add(fileEntity);
                }
            }
            if (App.DEBUG) {
                LogUtil.d(TAG, "ownedList size is " + ownedList.size());
            }
        }
        if (Utils.isEmpty(ownedList)) {
            return false;
        }
        deleteMode = true;
        // fileClickedListener = null;
        initData(ownedList);
        mTimelineListAdapter.updateTimeList();
        mTimelineListAdapter.notifyDataSetChanged();
        return true;
    }

    public void setAlbumData() {
        initData(fileList);
        mTimelineListAdapter.setData(batchData, selectedData, batchIndex);
        mTimelineListAdapter.notifyDataSetChanged();
    }

    @Override
    public void deleteFile() {
        if (!isShown()) {
            return;
        }
        if (selectedData == null || selectedData.isEmpty()) {
            CToast.showToast(R.string.havet_select_file);
            return;
        }
        AlertDialog.Builder deleteAFile = new AlertDialog.Builder(getActivity());
        deleteAFile.setTitle(R.string.confime_delete);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {

                    if (selectedData == null || selectedData.isEmpty()) {
                        dialog.cancel();
                    }
                    if (detailListener != null) {
                        detailListener.onDelete(selectedData);
                    }
                }
                dialog.cancel();
            }
        };
        deleteAFile.setPositiveButton(R.string.confirm, listener);
        deleteAFile.setNegativeButton(R.string.cancel, listener);
        deleteAFile.show();
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);
        int id = v.getId();
        if (!v.isShown()) {
            return;
        }
        Object obj = v.getTag();
        if (obj instanceof UserInfo) {
            onOpenUserDetails(((UserInfo) obj).getId());
            albumActivityActionListener.setBottomDeleteBtnsState();
        } else if (obj instanceof FileEntity) {
            FileEntity fileEntity = (FileEntity) obj;
            if (deleteMode) {
                boolean result = onSelectFile(fileEntity);
                if (v instanceof PhotoView) {
                    ((PhotoView) v).checked(result);
                }
            } else {
                openFile(fileEntity);
            }
        }
    }

    @Override
    public boolean onLongClick(View v) {
        if (!deleteMode) {
            return false;
        }
        Object obj = v.getTag();
        if (obj instanceof FileEntity) {
            detailListener.onFileLongClicked((FileEntity) obj);
        }
        return false;
    }

    private void openFile(FileEntity fileEntity) {
        if (detailListener == null || fileEntity == null) {
            return;
        }
        if (mTimelineListAdapter == null) {
            return;
        }
        sortAlbumList();
        int position = albumItemController.positionOf(fileEntity);
        if (App.DEBUG) {
            LogUtil.d(TAG, " --openFile-- " + fileEntity + " --position-- " + position);
        }
        detailListener.onFileClicked(position);
    }

    public void sortAlbumList() {
        ArrayList<SparseArray<FileEntity>> array = mTimelineListAdapter.getTimeLineList();
        if (array == null) {
            return;
        }
        LinkedList<FileEntity> list = new LinkedList<FileEntity>();
        int size = array.size();
        for (int i = 0; i < size; i++) {
            SparseArray<FileEntity> sa = array.get(i);
            int saSize = sa.size();
            for (int j = 0; j < saSize; j++) {
                FileEntity entity = sa.valueAt(j);
                list.add(entity);
            }
        }
        albumItemController.clearAndResetFileList(list);
        //TODO set into albumItemList
    }

    public boolean isShowTabBar() {
        return showTabBar;
    }

    public void setShowBottomBar(boolean isShow) {
        this.showBottomBar = isShow;
    }

    private boolean onSelectFile(FileEntity entity) {
        if (entity == null || selectedData == null) {
            return false;
        }
        boolean select = false;
        if (selectedData.contains(entity)) {
            selectedData.remove(entity);
        } else {
            selectedData.add(entity);
            select = true;
        }
        onStateChanged();
        boolean selected;
        if (selectedData.size() == ownedList.size()) {
            selected = true;
            detailListener.onDisplayAllSelected(selected);
        } else {
            selected = false;
            detailListener.onDisplayAllSelected(selected);
        }

        return select;
    }

    private void onAllSelected(boolean selected) {
        if (selectedData == null) {
            return;
        }
        if (selected) {
            for (FileEntity entity : ownedList) {
                selectedData.add(entity);
            }
        } else {
            selectedData.clear();
        }
        onStateChanged();
    }

    private void onStateChanged() {
        TextView tvDelete = (TextView) (getActivity().findViewById(R.id.tv_delete));
        RelativeLayout rlDeleteLayout = (RelativeLayout) (getActivity().findViewById(R.id.rl_delete_1));

        if (selectedData.isEmpty()) {
            tvDelete.setText(R.string.delete);
            tvDelete.setTextColor(0xffd7d7d7);
            rlDeleteLayout.setBackgroundColor(Color.WHITE);
        } else {
            tvDelete.setText(getResources().getString(R.string.delete) + "(" + selectedData.size() + ")");
            rlDeleteLayout.setBackgroundColor(0xff0794e1);
            tvDelete.setTextColor(Color.WHITE);
        }
    }

    @Override
    public int getPageSize() {
        return 45;
    }

    public void setAlbum(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
        if (albumEntity == null) {
            owner = null;
            return;
        }
        this.owner = albumEntity.getOwner();
    }

    @Override
    public void onFileDeleted(FileEntity file) {
        if (file == null) {
            return;
        }
        if (ownedList != fileList && ownedList != null) {
            ownedList.remove(file);
        }
        if (selectedData != null) {
            selectedData.remove(file);
        }
        String batchId = file.getBatchId();
        if (batchData != null && batchIndex != null) {
            SparseArray<FileEntity> array = batchData.get(batchId);
            if (array == null) {
                return;
            }

            int arraySize = array.size();
            // remove file
            for (int i = 0; i < arraySize; i++) {
                FileEntity entity = array.valueAt(i);
                if (entity == file) {
                    array.removeAt(i);
                    break;
                }
            }
            // remove this batch
            if (array.size() <= 0) {
                int index = batchIndex.indexOfValue(batchId);
                int batchSize = batchIndex.size();
                if (index >= 0 && index < batchSize) {
                    batchIndex.removeAt(index);
                }
            }
        }
        onStateChanged();
        mTimelineListAdapter.updateTimeList();
        mTimelineListAdapter.notifyDataSetChanged();
        checkEmpty();
        if (App.DEBUG) {
            LogUtil.d(TAG, "onFileDeleted() = " + file.getName());
        }
    }

    @Override
    public void onFileUpload(FileEntity fileEntity) {
        if (fileEntity == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "onFileUpload " + fileEntity.getName());
        }
        addToBatchData(fileEntity);
        checkEmpty();
        mTimelineListAdapter.setData(batchData, selectedData, batchIndex);
        mTimelineListAdapter.notifyDataSetChanged();
    }

    @Override
    public void onFileUploadDone(FileEntity fileEntity) {
        if (fileEntity == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.e(TAG, "onFileUploadDone " + fileEntity.getName());
        }
        final String batchId = fileEntity.getBatchId();
        final int seqNum = fileEntity.getSeqNum();
        updateTimelineData(seqNum, batchId, fileEntity, true);
        // updateTimelineIndex(timeInMills, batchId, fileEntity);
        mTimelineListAdapter.refresh(fileEntity);
        mTimelineListAdapter.setData(batchData, selectedData, batchIndex);
        // mTimelineListAdapter.notifyDataSetChanged();
    }

    private void addToBatchData(FileEntity entity) {
        if (entity == null || !entity.isValid()) {
            return;
        }
        final String batchId = entity.getBatchId();
        final String utcTime = entity.getCreateDate();
        final int seqNum = entity.getSeqNum();
        long timeInMills = TimeUtil.toLong(utcTime, Consts.SERVER_UTC_FORMAT);
        updateTimelineData(seqNum, batchId, entity, false);
        updateTimelineIndex(timeInMills, batchId, entity);
    }

    private void updateTimelineData(int seqNum, final String batchId, FileEntity entity, boolean override) {
        // TODO need change this 根据batchID查找
        if (batchData.containsKey(batchId)) {
            SparseArray<FileEntity> datas = batchData.get(batchId);
            datas.put(seqNum, entity, override);
            // LogUtil.d(TAG, "containsKey:" + batchId + "	timeInMills:"
            // + timeInMills);
        } else {
            SparseArray<FileEntity> datas = new SparseArray<FileEntity>();
            datas.put(seqNum, entity);
            batchData.put(batchId, datas);
        }
    }

    /* update index */
    private void updateTimelineIndex(final long timeInMills, final String batchId, FileEntity entity) {
        final int index = batchIndex.indexOfValue(batchId);
        if (index >= 0) {
            long oldMills = batchIndex.keyAt(index);
            // remove old and put newer
            if (oldMills >= timeInMills) {
                return;
            }
            batchIndex.delete(oldMills);
        }
        batchIndex.put(timeInMills, batchId, false);
    }

    @Override
    public void onItemEnd(AlbumEntity albumEntity) {
        setAlbum(albumEntity);
        //TODO 加入为空的判断
//        if (lists == null && albumEntity != null && albumEntity.getSize() > 0) {
//            return;
//        }
//        fileList = lists;
        checkEmpty();
        initData(fileList);
        if (mTimelineListAdapter != null) {
            mTimelineListAdapter.setData(batchData, selectedData, batchIndex);
            mTimelineListAdapter.notifyDataSetChanged();
        }
        LoadingDialog.dismiss();
        if (App.DEBUG) {
            LogUtil.d(TAG, "onItemEnd	dismiss	EMPTY=");
        }
    }

    @Override
    public void onShown() {
        super.onShown();
    }

    @Override
    protected void initTabBar() {
        mRootViews = mRootView;
        showDayNightView = true;
        refreshableView = lvTimelineList.getRefreshableView();
        pullToRefreshBase = lvTimelineList;
        addSortDeleteButtonToListHeader = false;
        autoLoad = true;
        TABBAR_STATE = OnTimeLineHeaderActionListener.STATE_SHOW_ALBUM;
        setTimeLineAdapter(mTimelineListAdapter);
    }

    @Override
    public int getCurrentLoadFileCount() {
        if (fileList != null) {
            return fileList.size();
        }
        return 0;
    }

    @Override
    protected void getFileInPosition(List<FileEntity> currentList, int position) {
        SparseArray<FileEntity> cached = mTimelineListAdapter.getItem(position);
        if (cached != null) {
            for (int i = 0; i < cached.size(); i++) {
                currentList.add(cached.valueAntiAt(i));
            }
        }
    }

    @Override
    public void onDestroyView() {
        // TODO Auto-generated method stub
        super.onDestroyView();
        mTimelineListAdapter.clear();
        if (App.DEBUG) {
            LogUtil.v(TAG, "onDestroyView(");
        }
    }

}
