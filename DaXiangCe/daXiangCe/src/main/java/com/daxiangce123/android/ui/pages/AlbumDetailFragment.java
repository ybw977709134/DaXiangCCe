package com.daxiangce123.android.ui.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.FileSort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.listener.OnDetialListener;
import com.daxiangce123.android.listener.OnFileOptionListener;
import com.daxiangce123.android.listener.OnPullListener;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.ui.adapter.AlbumGridViewAdapter;
import com.daxiangce123.android.ui.adapter.EmptyFileAdapter;
import com.daxiangce123.android.ui.pages.base.BaseAlbumFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ListGrid;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.PullToRefreshBanner;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

public class AlbumDetailFragment extends BaseAlbumFragment implements OnItemClickListener, OnFileOptionListener, OnItemLongClickListener {

    private static final String TAG = "AlbumDetailFragment";

    private View mRootView = null;

    private ListGrid mImageGrid;
    private PullToRefreshBanner mPullRefreshGridView;
    //    private LinearLayout emptyView;
    private AlbumGridViewAdapter mImageAdapter = null;
    private EmptyFileAdapter emptyFileAdapter = null;

    private String owner = null;
    private OnDetialListener detailListener;
    private FileSort mSort;
    private int numColumns = 3;

    private boolean selected;

    private OnRefreshListener2<View> onRefreshListener2 = new OnRefreshListener2<View>() {

        @Override
        public void onPullDownToRefresh(PullToRefreshBase<View> refreshView) {
            if (onPullListener == null) {
                return;
            }
            onPullListener.onPullDownToRefresh(refreshView, getPageSize());
        }

        @Override
        public void onPullUpToRefresh(PullToRefreshBase<View> refreshView) {
            if (onPullListener == null) {
                return;
            }
            onPullListener.onPullUpToRefresh(refreshView, getPageSize());
        }

    };

    // private AlbumEntity albumEntity;
    @Override
    public String getFragmentName() {
        return "AlbumDetailFragment";
    }

    public AlbumDetailFragment() {
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.album_fragment, container, false);
            initCompontent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        // showData();
        // initBroadcast();
        return mRootView;

    }

    public void setSort(FileSort fileSort) {
        this.mSort = fileSort;
        sortFile();
    }

    protected void sortFile() {
        if (mSort == null) {
            return;
        }
        if (mSort == FileSort.COMMENTS_SORT) {
            Utils.sortByComments(fileList, true);
        } else if (mSort == FileSort.LIKE_SORT) {
            Utils.sortByLikes(fileList, true);
        }

        if (mImageAdapter != null) {
            mImageAdapter.setData(fileList);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    private void showData() {
        LoadingDialog.dismiss();
        if (Utils.isEmpty(fileList)) {
//            emptyView.setVisibility(View.VISIBLE);
            mImageGrid.setRowCount(1);
            mImageGrid.setGridSquare(false);
            mImageGrid.setAdapter(emptyFileAdapter);

        } else {
            mImageGrid.setRowCount(3);
            mImageGrid.setGridSquare(true);
            mImageGrid.setAdapter(mImageAdapter);
        }
        mImageAdapter.setData(fileList);
        mImageAdapter.notifyDataSetChanged();
    }

    private void initCompontent() {
        mImageAdapter = new AlbumGridViewAdapter(getActivity());
        emptyFileAdapter = new EmptyFileAdapter(getActivity());
        if (mPullRefreshGridView == null) {
            mPullRefreshGridView = (PullToRefreshBanner) mRootView.findViewById(R.id.gv_album_detail_list);

            mImageGrid = mPullRefreshGridView.getGridView();
            mPullRefreshGridView.setOnRefreshListener(onRefreshListener2);
            mPullRefreshGridView.setMode(Mode.BOTH);
            mImageGrid.setOnItemClickListener(this);
            mImageGrid.setOnItemLongClickListener(this);
//			mImageGrid.setRowCount(3);
//            mImageGrid.setGridSquare(true);
            initListHeader(null);
            int padding = Utils.dp2px(getActivity(), 10);
            int gridWidth = App.SCREEN_WIDTH - 2 * padding;
            int itemWidth = (gridWidth - ((numColumns - 1) * padding)) / numColumns;
            ImageSize imageSize = new ImageSize(itemWidth, itemWidth);
            imageSize.setThumb(true);

            mImageAdapter.setImageSize(imageSize);
        }
        // mImageAdapter.setData(fileList);
        showData();
//		mImageGrid.setAdapter(mImageAdapter);
        ViewUtil.ajustMaximumVelocity(mImageGrid, Consts.SLOW_ABS_SCROLL_RATION);
        // mImageAdapter.notifyDataSetChanged();
//        if (emptyView == null) {
//            emptyView = (LinearLayout)
//                    mRootView.findViewById(R.id.ll_empty_album);
//        }
        LoadingDialog.show(R.string.loading);
    }

    public void setOnPullListener(OnPullListener listener2) {
        this.onPullListener = listener2;
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (!deleteMode) {
            return false;
        } else {
            if (owner == null) {
                return false;
            }
            FileEntity entity = null;
            if (owner.equals(App.getUid())) {
                if (fileList == null) {
                    return false;
                }
                entity = fileList.get(position);
            } else {
                if (ownedList == null) {
                    return false;
                }
                entity = ownedList.get(position);
            }
            detailListener.onFileLongClicked(entity);
        }

        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!deleteMode) {
            detailListener.onFileClicked(position);
        } else {
            if (owner == null) {
                return;
            }
            // TextView tvSelectAll = (TextView) (getActivity()
            // .findViewById(R.id.tv_all_circle));
            if (owner.equals(App.getUid())) {
                if (fileList == null) {
                    return;
                }
                FileEntity entity = fileList.get(position);
                if (App.DEBUG) {
                    LogUtil.d(TAG, "fileList :" + fileList);
                }
                mImageAdapter.onFileSelect(entity);
                mImageAdapter.notifyDataSetChanged();
                if (mImageAdapter.getSelectFile().size() == fileList.size()) {
                    selected = true;
                    detailListener.onDisplayAllSelected(selected);
                } else {
                    // tvSelectAll.setBackgroundResource(R.drawable.select_all_circle);
                    selected = false;
                    detailListener.onDisplayAllSelected(selected);
                }

            } else {
                if (ownedList == null) {
                    return;
                }
                FileEntity entity = ownedList.get(position);
                if (App.DEBUG) {
                    LogUtil.d(TAG, "ownedList :" + ownedList);
                }
                mImageAdapter.onFileSelect(entity);
                mImageAdapter.notifyDataSetChanged();
                boolean selected;
                if (mImageAdapter.getSelectFile().size() == ownedList.size()) {
                    selected = true;
                    detailListener.onDisplayAllSelected(selected);
                } else {
                    selected = false;
                    detailListener.onDisplayAllSelected(selected);
                }
            }
        }
    }

    public void setAlbum(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
        if (albumEntity == null) {
            return;
        }
        this.owner = albumEntity.getOwner();
    }

    public void setDetailListener(OnDetialListener detailListener) {
        this.detailListener = detailListener;
    }

    public void selectAll(boolean selected) {
        this.selected = selected;
        if (owner == null) {
            return;
        }
        if (owner.equals(App.getUid())) {
            if (fileList == null || fileList.isEmpty()) {
                return;
            }
            // for (int i = 0; i < fileList.size(); i++) {
            // FileEntity entity = fileList.get(i);
            // mImageAdapter.onFileSelect(entity);
            // }
            mImageAdapter.setSeletctAll(selected);
            mImageAdapter.notifyDataSetChanged();
        } else {
            if (ownedList == null || ownedList.isEmpty()) {
                return;
            }
            for (int i = 0; i < ownedList.size(); i++) {
                FileEntity entity = ownedList.get(i);
                mImageAdapter.onFileSelect(entity);

            }
            mImageAdapter.setSeletctAll(selected);
            mImageAdapter.notifyDataSetChanged();
        }
    }

    public void setCancelSelect() {
        deleteMode = false;
        mImageAdapter.cancelSelect();
        mImageAdapter.setSeletctAll(false);
        mImageAdapter.notifyDataSetChanged();
    }

    public boolean showDeleteMode() {
        if (owner != null && owner.equals(App.getUid())) {
            ownedList = fileList;
        } else {
            // if (ownedList == null) {
            ownedList = new LinkedList<FileEntity>();
            // } else {
            ownedList.clear();
            // }

            for (int index = 0; index < fileList.size(); index++) {
                FileEntity fileEntity = fileList.get(index);
                if (fileEntity.getOwner().equals(App.getUid())) {
                    ownedList.add(fileEntity);
                }
            }
        }
        if (Utils.isEmpty(ownedList)) {
            return false;
        }
        deleteMode = true;
        // fileClickedListener = null;
        mImageAdapter.setData(ownedList);
        mImageAdapter.notifyDataSetChanged();
        return true;
    }

    public void setAlbumData() {
        mImageAdapter.setData(fileList);
        mImageAdapter.notifyDataSetChanged();
    }

    public void deleteFile() {
        if (!isShown()) {
            return;
        }
        if (mImageAdapter.getSelectFile() == null || mImageAdapter.getSelectFile().isEmpty()) {
            CToast.showToast(R.string.havet_select_file);
            return;
        }
        AlertDialog.Builder deleteAFile = new AlertDialog.Builder(this.getActivity());
        deleteAFile.setTitle(R.string.confime_delete);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (detailListener != null && mImageAdapter != null) {
                        detailListener.onDelete(mImageAdapter.getSelectFile());
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
    public int getPageSize() {
        int remainder = Utils.sizeOf(fileList) % numColumns;
        if (remainder != 0) {
            remainder = numColumns - remainder;
        }
        return 15 * numColumns + remainder;
    }

    @Override
    public void onFileDeleted(FileEntity file) {
        if (file == null) {
            return;
        }
        if (owner == null) {
            return;
        }
        if (ownedList != fileList && ownedList != null) {
            ownedList.remove(file);
        }

        HashSet<FileEntity> selectList = mImageAdapter.getSelectFile();
        if (selectList != null) {
            selectList.remove(file);
        }
        mImageAdapter.setSeletctAll(false);
//        mImageAdapter.notifyDataSetChanged();
        showData();
    }

    @Override
    public void onFileUpload(FileEntity fileEntity) {
        if (fileEntity == null) {
            return;
        }
        showData();
    }

    @Override
    public void onShown() {
        super.onShown();
        showData();
    }

    @Override
    public void onItemEnd(AlbumEntity albumEntity) {
        setAlbum(albumEntity);
        //TODO 加入空判断
//        if (Utils.isEmpty(lists) && albumEntity != null && albumEntity.getSize() > 0) {
//            return;
//        }
        sortFile();
        showData();
        LoadingDialog.dismiss();
    }

    @Override
    protected void initTabBar() {
        mRootViews = mRootView;
        refreshableView = mImageGrid;
        autoLoad = true;
        addSortDeleteButtonToListHeader = true;
        pullToRefreshBase = mPullRefreshGridView;
        TABBAR_STATE = OnTimeLineHeaderActionListener.STATE_SHOW_ALBUM;
        firstVisibleItemPosition = 1;
    }

    @Override
    public int getCurrentLoadFileCount() {
        if (fileList != null) {
            return fileList.size();
        }
        return 0;
    }

    @Override
    public void onFileUploadDone(FileEntity fileEntity) {
        if (fileEntity == null) {
            return;
        }
        showData();
    }

    @Override
    public void sortAlbumList() {

    }

    public boolean isShowTabBar() {
        return showTabBar;
    }

    public void setShowBottomBar(boolean isShow) {
        this.showBottomBar = isShow;
    }

    @Override
    protected void getFileInPosition(List<FileEntity> currentList, int position) {
        for (int i = 0; i < mImageGrid.getRowCount(); i++) {
            FileEntity tempFileEntity;
            if ((tempFileEntity = mImageAdapter.getItem(mImageGrid.getRowCount() * position + i)) != null) {
                currentList.add(tempFileEntity);
            }
        }
    }
}
