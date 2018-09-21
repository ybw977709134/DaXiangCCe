package com.daxiangce123.android.ui.pages.base;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.listener.OnAlbumDetailActivityActionListener;
import com.daxiangce123.android.listener.OnPullListener;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.activities.AlbumDetailActivity;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.TimeLineAdapter;
import com.daxiangce123.android.ui.view.ExpandableTextView;
import com.daxiangce123.android.ui.view.StickyHeaderListview;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;

import java.util.LinkedList;
import java.util.List;

/**
 * a wraper of BaseFragment
 *
 * @author hansontian
 */
public abstract class BaseTabBarFragment extends BaseFragment implements OnScrollListener, OnClickListener, OnAlbumDetailActivityActionListener {// OnItemClickListener,
    public static boolean showReadDot = false;
    public static int SCROLL_STATE = 0;
    private int mScrollY;
    protected ImageView redDotImageView = null;
    protected TimeLineAdapter timeLineAdapter;
    protected View outerTabView;
    protected AlbumEntity albumEntity;
    protected UserInfo ownerInfo;
    protected View mRootViews = null;
    protected PullToRefreshBase<? extends View> pullToRefreshBase = null;
    protected ListView refreshableView = null;
    protected boolean showDayNightView = false;
    protected boolean addSortDeleteButtonToListHeader = false;
    protected boolean isJoined = true;
    protected int firstVisibleItemPosition = 2;

    protected int preLast = 0;
    public boolean showTabBar = false;
    public boolean autoLoad = false;
    public boolean showBottomBar = true;
//    public boolean isShowBottomBar;
    protected boolean deleteMode = false;

    protected int TABBAR_STATE;
    protected OnTimeLineHeaderActionListener albumActivityActionListener;
    protected OnPullListener onPullListener;

    // /////Header View use//////
    private ImageView avatarView;
    protected TextView mShowTimeLine, mShowMembers;
    protected TextView mTitlePeoples, mTitleViews, mTitleFiles, mTitleShares;
    private View tabViews, titlebarViews;
    protected TextViewParserEmoji mOwnerName;
    protected ExpandableTextView mDescription;
    private LinearLayout downDescriptionTab;
    private View dayNightViews;
    private int previouslylastPostion = -1;
    protected boolean showUpload = true, showJoin = false;

    abstract protected void initTabBar();

    abstract public int getPageSize();

    public final static String TAG = "BaseTabBarFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (albumActivityActionListener == null)
            albumActivityActionListener = (OnTimeLineHeaderActionListener) getActivity();
    }

    /**
     * 初始化 header
     */
    protected void initListHeader(ViewGroup container) {
        initTabBar();
        tabViews = getLayoutInflater(null).inflate(R.layout.layout_album_image_mumber_tab, container);
        titlebarViews = getLayoutInflater(null).inflate(R.layout.layout_album_below_details, container);
        refreshableView.addHeaderView(titlebarViews);
        refreshableView.addHeaderView(tabViews);
        refreshableView.setOnScrollListener(this);
        findHeaderViews();
        updateTitleViewsContent();
        initTabBarUI();
        if (showDayNightView) {
            timeLineAdapter.initTabHeader(outerTabView, refreshableView);
            dayNightViews = outerTabView.findViewById(R.id.ll_date_indexer);
        }
        updateRedDotView();
        // sendMessage(SCROLL_TO_TOP, 1000);

    }

    private void findHeaderViews() {
        avatarView = (ImageView) titlebarViews.findViewById(R.id.cv_member_avatar);
        avatarView.setOnClickListener(this);
        mOwnerName = (TextViewParserEmoji) titlebarViews.findViewById(R.id.tv_owner_name);
        mOwnerName.setOnClickListener(this);
        mDescription = (ExpandableTextView) titlebarViews.findViewById(R.id.ept_album_description);
        mShowTimeLine = (TextView) tabViews.findViewById(R.id.tv_showtimeline);
        mShowTimeLine.setOnClickListener(this);
        mShowMembers = (TextView) tabViews.findViewById(R.id.tv_showmembers);
        mShowMembers.setOnClickListener(this);
        redDotImageView = (ImageView) tabViews.findViewById(R.id.tv_red_dot);
        downDescriptionTab = (LinearLayout) titlebarViews.findViewById(R.id.ll_album_details_description_tab);
        mTitlePeoples = (TextView) titlebarViews.findViewById(R.id.album_title_people);
        mTitleFiles = (TextView) titlebarViews.findViewById(R.id.album_title_file);
        mTitleViews = (TextView) titlebarViews.findViewById(R.id.album_title_views);
        mTitleShares = (TextView) titlebarViews.findViewById(R.id.album_title_shares);
    }

    protected void updateTitleViewsContent() {
        if (albumEntity != null) {
            updateAlbumDetail(albumEntity.getNote(), albumEntity.getSize(), albumEntity.getMembers(), albumEntity.getViews(), albumEntity.getOwner(), albumEntity.getShares());
        }
        if (ownerInfo != null) {
            if (App.DEBUG) {
                LogUtil.d(TAG, " -- ownerName -- " + ownerInfo.getName());
            }
            mOwnerName.setEmojiText(ownerInfo.getName());
            // mOwnerName.setText(ownerInfo.getName());
            // mOwnerName
            // .measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            // double viewWidth = (double) mOwnerName.getMehhasuredWidth();
            // TextPaint paint = mOwnerName.getPaint();
            // double textWidth = (double)
            // paint.measureText(ownerInfo.getName());
            // mOwnerName.setEmojiText(ownerInfo.getName(), viewWidth,
            // textWidth);

        }
        updateRedDotView();
    }

    private void updateAlbumDetail(String note, int albumSize, int albumMember, int albumView, String ownerId, int albumShares) {
        mDescription.setText(note);
        mTitleFiles.setText(String.valueOf(albumSize >= 0 ? albumSize : 0));
        mTitlePeoples.setText(String.valueOf(albumMember >= 0 ? albumMember : 0));
        mTitleViews.setText(String.valueOf(albumView >= 0 ? albumView : 0));
        mTitleShares.setText(String.valueOf(albumShares >= 0 ? albumShares : 0));

        if ((Utils.isEmpty(note) || note.equals("") || note.trim().equals("")) && downDescriptionTab != null) {
            downDescriptionTab.setVisibility(View.GONE);
            tabViews.invalidate();
        } else if (downDescriptionTab != null) {
            downDescriptionTab.setVisibility(View.VISIBLE);
        }

        ImageManager.instance().loadAvater(avatarView, ownerId);
    }

    public boolean updateRedDotView() {
        if (albumEntity == null) {
            return false;
        }
        if (albumEntity.getOldMembers() != 0 && albumEntity.getMembers() > albumEntity.getOldMembers() && albumEntity.getMembers() != 1) {
            showReadDot = true;
            if (redDotImageView != null) {
                redDotImageView.setVisibility(View.VISIBLE);
            }
        } else {
            showReadDot = false;
            if (redDotImageView != null) {
                redDotImageView.setVisibility(View.INVISIBLE);
            }
        }
        return showReadDot;
    }

    @Override
    public void onShown() {
        super.onShown();
        if (this.isVisible()) {
            updateTabBar();
            updateRedDotView();
            updateBottomBar();
        }
    }

    protected int getVisibleCount(ListView listView) {
        int height = 0;
        for (int i = 0; i < listView.getHeaderViewsCount() - 1; i++) {
            height += listView.getChildAt(i).getHeight();
        }

        return height;
    }

    protected void updateTabBar() {
        if (showTabBar) {
            albumActivityActionListener.setTabBarState(TABBAR_STATE, showDayNightView);
        } else {
            albumActivityActionListener.setTabBarState(OnTimeLineHeaderActionListener.STATE_HIDE, showDayNightView);
        }
    }

    protected void updateBottomBar() {
        if (showBottomBar) {
            albumActivityActionListener.setBottomBarState(true, false);
        } else {
            albumActivityActionListener.setBottomBarState(false, false);
        }

    }

    private void showAlbumPhotos() {
        albumActivityActionListener.showAlbumPhotos();
    }

    private void showMembers() {
        albumActivityActionListener.showMembers();
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (dayNightViews != null) {
            ((StickyHeaderListview) pullToRefreshBase).setVisibleHeightWihtDayIndicator(dayNightViews.getTop());
        }
        SCROLL_STATE = scrollState;
        if (scrollState == SCROLL_STATE_IDLE) {
            onListIdle();
        } else {
            onListMove(scrollState);
        }
        LogUtil.v(TAG, "onScrollStateChanged " + scrollState);
    }

    protected int getListViewScrollY() {
        View topChild = refreshableView.getChildAt(0);
        // refreshableView.getFirstVisiblePosition() * topChild.getHeight()
        return topChild == null ? 0 : -topChild.getTop();
    }

    public boolean isShowTabBar() {
        return showTabBar;
    }

    public void setShowTabBar(boolean showTabBar) {
        this.showTabBar = showTabBar;
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (App.DEBUG) {
            LogUtil.v(TAG, "first=" + firstVisibleItem + " visibleItemCount" + visibleItemCount);
        }
        if (firstVisibleItem >= firstVisibleItemPosition && firstVisibleItem < firstVisibleItemPosition + 4 && !showTabBar) {
            showTabBar = true;
            ((AlbumDetailActivity) getActivity()).setTabBarVisibilty(true);
        } else if (firstVisibleItem <= firstVisibleItemPosition - 1) {
            showTabBar = false;
            ((AlbumDetailActivity) getActivity()).setTabBarVisibilty(false);
        }
        if (showDayNightView && firstVisibleItem >= firstVisibleItemPosition) {
            firstVisibleItem = firstVisibleItem - firstVisibleItemPosition;
            timeLineAdapter.setHeader(firstVisibleItem);
        }
        if (deleteMode) {
            return;
        }
        if (autoLoad) {
            int lastItem = firstVisibleItem + visibleItemCount + refreshableView.getHeaderViewsCount();
            if ((lastItem == totalItemCount) && (preLast != lastItem)) {
                if (timeLineAdapter != null && timeLineAdapter.getCount() >= 10) {
                    pullToRefreshBase.setCurrentMode(Mode.PULL_FROM_END);
                    pullToRefreshBase.setRefreshing(false);
                } else if (timeLineAdapter == null) {
                    pullToRefreshBase.setCurrentMode(Mode.PULL_FROM_END);
                    pullToRefreshBase.setRefreshing(false);
                }
                preLast = lastItem;
            }
        }
        if (!showBottomBar ) {
            showBottomBar = true;
            return;
        }
        int newScrollY = getListViewScrollY();
        if (newScrollY == mScrollY) {
            return;
        }
        if (newScrollY - mScrollY > 3) {
            albumActivityActionListener.setBottomBarState(false, true);
        } else if (mScrollY - newScrollY > 3) {
            albumActivityActionListener.setBottomBarState(true, true);
        }
        mScrollY = newScrollY;

    }

    @Override
    public String getFragmentName() {
        return null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_showtimeline:
                showAlbumPhotos();
                break;
            case R.id.tv_showmembers:
                showMembers();
                break;
            case R.id.cv_member_avatar:
                // if (isJoined) {
                onOpenUserDetails();
                albumActivityActionListener.setBottomDeleteBtnsState();
                // } else {
                // CToast.showToast(R.string.join_album_can_look);
                // }
                break;
        }
    }

    private void onOpenUserDetails() {
        if (albumEntity != null) {
            openUserDetails(albumEntity.getId(), albumEntity.getOwner());
        }
    }

    private void openUserDetails(String albumId, String albumOwner) {
        try {
            Intent intent = new Intent();
            intent.putExtra(Consts.ALBUM_ID, albumId);
            intent.putExtra(Consts.USER_ID, albumOwner);
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

    public AlbumEntity getAlbumEntity() {
        return albumEntity;
    }

    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    public UserInfo getOwnerInfo() {
        return ownerInfo;
    }

    public void setOwnerInfo(UserInfo ownerInfo) {
        this.ownerInfo = ownerInfo;

    }

    public OnTimeLineHeaderActionListener getAlbumActivityActionListener() {
        return albumActivityActionListener;
    }

    public void setAlbumActivityActionListener(OnTimeLineHeaderActionListener albumActivityActionListener) {
        this.albumActivityActionListener = albumActivityActionListener;
    }

    @Override
    public void onAlbumEntityReady() {
        updateTitleViewsContent();
    }

    @Override
    public void onUserInfoReady() {
        updateTitleViewsContent();
    }

    @Override
    public void refreshTitleContent() {
        updateTitleViewsContent();
    }

    public void setIsJoined(boolean joined) {
        this.isJoined = joined;
    }

    public void initTabBarUI() {
        switch (TABBAR_STATE) {
            case OnTimeLineHeaderActionListener.STATE_SHOW_MEMBER:
                mShowMembers.setTextColor(0xff0794e1);
                mShowMembers.setBackgroundResource(R.drawable.files_in_album_corners_bg);
                mShowTimeLine.setTextColor(0xFFFFFFFF);
                mShowTimeLine.setBackgroundResource(R.drawable.transparent);
                break;
            case OnTimeLineHeaderActionListener.STATE_SHOW_ALBUM:
                mShowTimeLine.setTextColor(0xff0794e1);
                mShowTimeLine.setBackgroundResource(R.drawable.files_in_album_corners_bg);
                mShowMembers.setTextColor(0xFFFFFFFF);
                mShowMembers.setBackgroundResource(R.drawable.transparent);
                break;
        }
    }

    public TimeLineAdapter getTimeLineAdapter() {
        return timeLineAdapter;
    }

    public void setTimeLineAdapter(TimeLineAdapter timeLineAdapter) {
        this.timeLineAdapter = timeLineAdapter;
        this.showDayNightView = true;
    }

    public View getOuterTabView() {
        return outerTabView;
    }

    public void setOuterTabView(View outerTabView) {
        this.outerTabView = outerTabView;
    }

    public void onListIdle() {
        int firstPosition = refreshableView.getFirstVisiblePosition() - firstVisibleItemPosition - 1;
        int lastPosition = refreshableView.getLastVisiblePosition() - firstVisibleItemPosition - 1;
        // refreshableView.getF
        if (App.DEBUG) {
            LogUtil.v(TAG, "firstPos:" + firstPosition + " lastPos:" + lastPosition);
        }
        if (firstPosition < 0) {
            firstPosition = 0;
        }
        if (firstPosition >= previouslylastPostion) {
            LinkedList<FileEntity> currentList = new LinkedList<FileEntity>();
            for (int i = firstPosition; i <= lastPosition; i++) {
                getFileInPosition(currentList, i);
            }
            BulletManager.instance().onListIdle(currentList);
        }
        previouslylastPostion = lastPosition;
    }

    public void onListMove(int state) {

    }

    protected abstract void getFileInPosition(List<FileEntity> currentList, int position);
    // protected get

    public Bitmap getAvatarBitmap() {
        Bitmap bitmap = null;
        Drawable drawable = avatarView.getDrawable();
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else if (drawable instanceof NinePatchDrawable) {
            bitmap = BitmapUtil.getNinePatchDrawable((NinePatchDrawable) drawable, 64, 64);
        }
        return bitmap;
    }

}
