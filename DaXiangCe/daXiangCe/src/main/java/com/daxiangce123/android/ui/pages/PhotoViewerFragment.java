package com.daxiangce123.android.ui.pages;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.business.AlbumItemController;
import com.daxiangce123.android.business.event.FileDeleteEvent;
import com.daxiangce123.android.business.event.Signal;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.ProgressInfo;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.manager.VideoManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.activities.LoginActivity;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.CommentLikeInfoPopup;
import com.daxiangce123.android.ui.view.CommentLikeInfoPopup.OnCommentLikeDismissListener;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.ui.view.PhotoPreview;
import com.daxiangce123.android.ui.view.ShareDialog;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;

import de.greenrobot.event.EventBus;


/**
 * @author ram
 * @project Cliq
 * @time Mar 19, 2014
 */

public class PhotoViewerFragment extends BaseFragment implements OnClickListener, OptionListener {
    private final static String TAG = "PhotoViewerFragment";
    private int curPosition;
    private boolean DEBUG = true;
    private String albumOwner;
    private boolean isJoined;
    private AlbumEntity albumEntity;
    private ViewPager vpContainer;
    private View contentView;
    private LinearLayout topBar;
    private LinearLayout bottomBar;
    private LinearLayout foreground;
    private View rlLike;
    private ImageView ivLike;
    private ImageView ivShare;
    private TextView tvLikeNum;
    private View rlShare;
    private TextView tvSharesNum;
    private TextView tvDownloadsNum;
    private TextView tvComments;
    private TextViewParserEmoji tvTitle;
    private TextView tvSubtitle;
    private ImageView ivOption;
    private ImageView ivAvater;
    private FileEntity curFile;
    private UserInfo userInfo;
    private ShareDialog shareDialog;
    private AlbumItemController fileList;
    private VideoPlayerFragment playerFragment;
    private ImagePagerAdapter pagerAdapter;
    private ArrayList<PhotoPreview> viewList;
    float zoomRatio;
    private boolean landSpace = false;
    /**
     * fileId -> view
     */
    private Hashtable<String, PhotoPreview> viewMap = new Hashtable<String, PhotoPreview>();
    /**
     * fileId -> UserInfo
     */
    private HashMap<String, UserInfo> infoMap = new HashMap<String, UserInfo>();
    /**
     * fileId -> liked
     */
    private HashMap<String, Boolean> fileLiked = new HashMap<String, Boolean>();
    private CommentLikeInfoPopup commentLikePopUp;
    private boolean hasPassword;
    private OptionDialog optionsDialog;
    private ArrayList<Integer> mOptionDatas = new ArrayList<Integer>();
    private HashSet<String> downloadGifFileId;
    //    private HashSet<String> downloadVideoFileId;
    private boolean firstPage = true;
    private boolean isWifi = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @SuppressWarnings("deprecation")
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Consts.TRANSFER_PROGRESS.equals(action)) {
                    onProgress(intent);
                } else {
                    ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);
                    int statusCode = response.getStatusCode();
                    String content = response.getContent();
                    if (Consts.GET_USER_INFO.equals(action)) {
                        UserInfo userInfo = Parser.parseUserInfo(content);
                        if (userInfo == null) {
                            return;
                        }
                        infoMap.put(userInfo.getId(), userInfo);
                        showUserInfo();
                    } else if (Consts.HAS_LIKED.equals(action)) {
                        boolean liked = false;
                        if (statusCode == 200 && response.getErrCode() != ErrorCode.NETWORK_ERROR) {
                            liked = true;
                        } else if (statusCode == 404) {
                            onNotFound(info, response);
                            return;
                        }
                        onLike(info.getTag(), liked, action);
                    } else if (Consts.LIKE_FILE.equals(action)) {
                        if (statusCode == 200) {
                            onLike(info.getTag(), true, action);
                        } else if (statusCode == 404) {
                            onNotFound(info, response);
                        }
                    } else if (Consts.DISLIKE_FILE.equals(action)) {
                        if (statusCode == 200) {
                            onLike(info.getTag(), false, action);
                        } else if (statusCode == 404) {
                            onNotFound(info, response);
                            return;
                        }
                    } else if (Consts.CHECK_ALBUM_ACCESS_CONTROL.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            hasPassword = Parser.parseHasPasswd(content);
                        }
                    } else if (Consts.DOWNLOAD_FILE.equals(action)) {
                        onDownload(info.getTag2());
                    } else if (Consts.SHARED_FILE.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            int count = curFile.getShares();
                            curFile.setShares(++count);
                            showShareNum();
                        }
                    } else if (Consts.DOWNLOADED_FILE.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            int count = curFile.getDownloads();
                            curFile.setDownloads(++count);
                            showDownloadNum();
                        }
                    } else if (Consts.GET_FILE_INFO.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            if (onGetFileInfo(content)) {
                                return;
                            }
                        }

                    } else if (Consts.JOIN_ALBUM.equals(action)) {
//                        if (response.getStatusCode() == 200) {
//                            isJoined = true;
//                            save();
//                        }
                        joinAlbum(response, info);
                    }
                    // else if (Consts.POST_REPORT.equals(action)) {
                    // onReport(response, info);
                    // }
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
            com.daxiangce123.android.http.Error errors = response.getError();
            int status = response.getStatusCode();
            if (status == 200) {
                isJoined = true;
                if (jo.containsKey(Consts.DOWNLOAD_FILE)) {
                    save();
                }

            } else if (response.getStatusCode() == 401 && errors.toErrorCode() == ErrorCode.INVALID_PASSWORD) {
                if (entity instanceof StringEntity) {
                    if (jo.containsKey(Consts.PASSWORD)) {
                        CToast.showToast(R.string.error_password);
                    } else if (jo.containsKey(Consts.REPORT_COMMENT)) {
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
        final EditText etInputId = new EditText(this.getActivity());
        AlertDialog.Builder passwordDialog = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            passwordDialog = new AlertDialog.Builder(this.getActivity());
        } else {
            passwordDialog = new AlertDialog.Builder(this.getActivity(), AlertDialog.THEME_HOLO_LIGHT);
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
                        jo.put(Consts.DOWNLOAD_FILE, false);
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

    private OnPageChangeListener changeListener = new OnPageChangeListener() {

        @Override
        public void onPageSelected(int position) {
            if (DEBUG) {
                LogUtil.d(TAG, pagerAdapter.getCount() + "onPageSelected()	position=" + position);
            }
            curPosition = position;
            updateUI(true);
            UMutils.instance().diyEvent(ID.EventSwipePreview);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageScrollStateChanged(int state) {
        }
    };

    @Override
    public String getFragmentName() {
        return "PictureViewerFragment";
    }

    public PhotoViewerFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        setBoottomBarVisibility(View.GONE);
    }

    @Override
    public void onShown() {
        super.onShown();
        if (DEBUG) {
            LogUtil.d(TAG, "onShown()		curPosition=" + curPosition);
        }
        if (curFile != null && MimeTypeUtil.getMime(curFile.getMimeType()) == Mime.GIF) {
            updateUI(true);
        } else {
            updateUI(false);
        }
        firstPage = true;
        View bottomBar = this.getActivity().findViewById(R.id.bottom_bar);
        if (bottomBar != null) {
            bottomBar.setVisibility(View.GONE);
        }
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
    }

    private Runnable mNavHider = new Runnable() {
        @Override
        public void run() {
            setNavVisibility(false);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBroadcast();
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_picture_viewer, container, false);
            initUI();
        } else {
            ViewUtil.removeFromParent(contentView);
            resetViewPager();
        }

        isWifi = isWifiConnected(this.getActivity());
        initData();
        initConfiguration();

        setNavVisibility(true);
        return contentView;
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.DELETE_FILE);
        ift.addAction(Consts.LIKE_FILE);
        ift.addAction(Consts.DISLIKE_FILE);
        ift.addAction(Consts.HAS_LIKED);
        ift.addAction(Consts.CHECK_ALBUM_ACCESS_CONTROL);
        ift.addAction(Consts.DOWNLOAD_FILE);
        ift.addAction(Consts.TRANSFER_PROGRESS);
        ift.addAction(Consts.SHARED_FILE);
        ift.addAction(Consts.DOWNLOADED_FILE);
        ift.addAction(Consts.POST_REPORT);
        ift.addAction(Consts.GET_FILE_INFO);
        ift.addAction(Consts.JOIN_ALBUM);
        Broadcaster.registerReceiver(receiver, ift);
        EventBus.getDefault().register(this);
    }

    private void initUI() {
        viewMap = new Hashtable<String, PhotoPreview>();
        contentView.findViewById(R.id.back).setOnClickListener(this);
        tvLikeNum = (TextView) contentView.findViewById(R.id.tv_likes_num_picture);
        rlLike = contentView.findViewById(R.id.rl_likes_picture);
        ivLike = (ImageView) contentView.findViewById(R.id.iv_likes);
        rlShare = contentView.findViewById(R.id.rl_share_picture);
        rlShare.setOnClickListener(this);
        ivShare = (ImageView) contentView.findViewById(R.id.iv_share_picture);
        tvSharesNum = (TextView) contentView.findViewById(R.id.tv_shares_num_picture);
        tvComments = (TextView) contentView.findViewById(R.id.tv_comments_num_picture);
        tvDownloadsNum = (TextView) contentView.findViewById(R.id.tv_downloads_num_picture);
        ivAvater = (ImageView) contentView.findViewById(R.id.iv_avater_picture_viewers);
        ivAvater.setOnClickListener(this);
        tvTitle = (TextViewParserEmoji) contentView.findViewById(R.id.tv_title_picture_viewers);
        tvSubtitle = (TextView) contentView.findViewById(R.id.tv_subtitle_picture_viewers);
        ivOption = (ImageView) contentView.findViewById(R.id.iv_option_picture);
        ivOption.setOnClickListener(this);
        /* bottom panel */
        topBar = (LinearLayout) contentView.findViewById(R.id.ll_topbar_picture_viewer);
        bottomBar = (LinearLayout) contentView.findViewById(R.id.ll_bottom_panel_picture);
        foreground = (LinearLayout) contentView.findViewById(R.id.ll_picture_viewer_foreground);
        for (int i = 0; i < bottomBar.getChildCount(); i++) {
            View view = bottomBar.getChildAt(i);
            view.setOnClickListener(this);
        }
        viewList = new ArrayList<PhotoPreview>();
        for (int i = 0; i < 3; i++) {
            zoomRatio = 0.8f;
            if (App.SCREEN_WIDTH <= 720) {
                zoomRatio = 1.0f;
            }
            ImageSize pictureSize = new ImageSize((int) (App.SCREEN_WIDTH * zoomRatio), (int) (App.SCREEN_HEIGHT * zoomRatio));
            if (App.DEBUG) {
                LogUtil.v(TAG, "pictureSize=" + pictureSize.getWidth() + "=W h=" + pictureSize.getHeight());
            }
            PhotoPreview view = new PhotoPreview(getActivity());
            ViewPager.LayoutParams vlp = new ViewPager.LayoutParams();
            vlp.width = pictureSize.getWidth();
            vlp.height = pictureSize.getHeight();
            view.setLayoutParams(vlp);

            viewList.add(view);
            view.setOnClickListener(this);
            view.setImageSize(pictureSize);
        }

        pagerAdapter = new ImagePagerAdapter();
        vpContainer = (ViewPager) contentView.findViewById(R.id.vp_container_picture);
        vpContainer.setAdapter(pagerAdapter);
        vpContainer.setPageMargin(50);

        vpContainer.setOnPageChangeListener(changeListener);
    }

    private void initConfiguration() {
        Configuration conf = getResources().getConfiguration();
        onConfigurationChanged(conf);
    }

    private void initData() {
        if (albumEntity != null) {
            canLike(albumEntity);
            ConnectBuilder.checkAlbumAccessControl(albumEntity.getId());
        }
    }


    @SuppressWarnings("deprecation")
    public void canLike(AlbumEntity entity) {
        //TODO 加入 like 相关
        albumEntity = entity;
        if (albumEntity.getLikeOff() && !albumOwner.equals(App.getUid())) {
            rlLike.setClickable(false);
            ivLike.setAlpha(80);
        } else {
            rlLike.setClickable(true);
            ivLike.setAlpha(255);
        }
        if (commentLikePopUp != null) {
            commentLikePopUp.canComment(entity);
        }
    }

    private void updateUI(boolean showGif) {
        updateDetail();
        if (curFile == null) {
            return;
        }
        if (curFile.isActive()) {
            ConnectBuilder.getFileInfo(curFile.getId());
            BulletManager.instance().resetShow();
            BulletManager.instance().addFile(curFile);
        }
        if (showGif) {
            playGif();
        }
        if (!showUserInfo()) {
            ConnectBuilder.getUserInfo(curFile.getOwner());
        }
    }

    public boolean isWifiConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiNetworkInfo = connectivityManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return wifiNetworkInfo != null && wifiNetworkInfo.isConnected();
    }

    private void playGif() {
        PhotoPreview preview = getShowView(curFile.getId());
        if (preview == null) {
            return;
        }
        for (PhotoPreview v : viewList) {
            if (v == preview) {
                continue;
            }
        }
        preview.setFile(curFile);
        preview.showFile(true);
    }

    private boolean showUserInfo() {
        if (curFile == null) {
            return false;
        }
        userInfo = infoMap.get(curFile.getOwner());
        if (userInfo == null) {
            tvTitle.setText("");
            return false;
        }
        tvTitle.setEmojiText(userInfo.getName());
        return true;
    }

    private void updateDetail() {
        if (curPosition >= getImageCount()) {
            if (DEBUG) {
                LogUtil.d(TAG, "curPosition out of range [curPosition" + curPosition + "] getImageCount[" + getImageCount());
            }
            return;
        }
        curFile = fileList.get(curPosition);
        if (curFile == null) {
            if (DEBUG) {
                LogUtil.d(TAG, "Cant get current file");
            }
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "updateDetail()		" + curFile.getId() + "	" + curPosition);
        }
        vpContainer.setCurrentItem(curPosition);
        /* create time */
        showDate();

		/* comments */
        showCommentNum();

		/* shares */
        showShareNum();

		/* downloads */
        showDownloadNum();

        if (!Utils.isEmpty(AppData.getToken())) {
            ivOption.setVisibility(View.VISIBLE);
            updateOptionData();
        } else {
            ivOption.setVisibility(View.GONE);
        }

		/* likes count */

        showLikeNum();
        showAvater();
        showLike();
    }

    private void showDate() {
        if (!isVisible()) {
            return;
        }
        long mills = TimeUtil.toLong(curFile.getCreateDate(), Consts.SERVER_UTC_FORMAT);
        String time = TimeUtil.formatTime(mills, "HH:mm");
        String date = getString(R.string.x_upload, TimeUtil.humanizeDate(mills) + " " + time);
        tvSubtitle.setText(date);
    }

    private void showCommentNum() {
        int comments = curFile.getComments();
        if (comments > 0) {
            tvComments.setVisibility(View.VISIBLE);
            tvComments.setText(" " + comments);
        } else {
            tvComments.setVisibility(View.INVISIBLE);
        }

    }

    private void showShareNum() {
        int shares = curFile.getShares();
        if (shares > 0) {
            tvSharesNum.setVisibility(View.VISIBLE);
            tvSharesNum.setText(" " + shares);
        } else {
            tvSharesNum.setVisibility(View.INVISIBLE);
        }
    }

    private void showDownloadNum() {
        int downloads = curFile.getDownloads();
        if (downloads > 0) {
            tvDownloadsNum.setVisibility(View.VISIBLE);
            tvDownloadsNum.setText(" " + downloads);
        } else {
            tvDownloadsNum.setVisibility(View.INVISIBLE);
        }
    }

    private void showLikeNum() {
        int likes = curFile.getLikes();
        if (likes > 0) {
            tvLikeNum.setVisibility(View.VISIBLE);
            tvLikeNum.setText("" + likes);
        } else {
            tvLikeNum.setVisibility(View.INVISIBLE);
        }
    }

    private boolean showAvater() {
        if (curFile == null) {
            return false;
        }
        ivAvater.setImageBitmap(null);
        ImageManager.instance().loadAvater(ivAvater, curFile.getOwner());
        if (ivAvater.getDrawable() != null) {
            return true;
        }
        return false;
    }

    private int getImageCount() {
        return fileList.size();
    }

    private void onProgress(Intent intent) {
        ProgressInfo pinfo = intent.getParcelableExtra(Consts.PROGRESS_INFO);
        ConnectInfo cInfo = intent.getParcelableExtra(Consts.REQUEST);
        String fileId = cInfo.getTag2();
        // PhotoPreview photoPreview = viewMap.get(fileId);
        PhotoPreview photoPreview = getShowView(fileId);
        if (photoPreview == null || pinfo == null) {
            return;
        }
        FileEntity file = (FileEntity) photoPreview.getTag();
        if (file == null || !file.getId().equals(fileId) || MimeTypeUtil.getMime(file.getMimeType()) == Mime.VID || !curFile.getId().equals(file.getId())) {
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "onProgress()	" + fileId + "	" + pinfo.getProgress());
        }
        if (photoPreview.hasBitmap() && photoPreview.isVideo()) {
            if (pinfo.getProgress() < 100) {
                photoPreview.showProgress(true);
            }
        } else if (MimeTypeUtil.getMime(file.getMimeType()) == Mime.GIF) {
            if (pinfo.getProgress() < 100) {
                photoPreview.showProgress(true);
            }
        }
        photoPreview.setProgress(pinfo.getProgress());
    }

    // private void onReport(Response response, ConnectInfo info) {
    // if (response.getStatusCode() == 200) {
    // String jsonStr = info.getTag();
    // JSONObject jo = JSONObject.parseObject(jsonStr);
    // String fileId = jo.getString(Consts.OBJ_ID);
    // if (!fileId.equals(curFile.getId())) {
    // return;
    // }
    // CToast.showToast(R.string.we_ll_handle_report_later);
    // }
    // }

    private void onDownload(String fileId) {
        // if (DEBUG) {
        // LogUtil.d(TAG, curFile.getId() + "=====onDownload()	" + fileId +
        // " MIME=" + curFile.getMimeType());
        // }
        if (curFile == null || !fileId.equals(curFile.getId()) || MimeTypeUtil.getMime(curFile.getMimeType()) == Mime.VID) {
            return;
        }
        // PhotoPreview photoPreview = viewMap.get(fileId);
        PhotoPreview photoPreview = getShowView(fileId);
        if (photoPreview == null) {
            return;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "=====onDownload() playGif();=======" + fileId);
        }
        playGif();
    }

    private boolean onFileDeleted(String fileId, int deletedPosition, boolean needToast) {
        if (fileList == null) {
            return false;
        }
        if (App.albumItemController != null && App.albumItemController != fileList) {
            return false;
        }
        if (deletedPosition < 0) {
            deletedPosition = fileList.positionOf(fileId);
        }
        if (deletedPosition >= 0) {
            if (needToast) {
                CToast.showToast(R.string.delete_succeed);
            }
            fileList.remove(fileId);
            PhotoPreview preview = viewMap.remove(fileId);
            if (preview != null) {
                preview.release();
            }
            pagerAdapter.notifyDataSetChanged();
            if (fileList.isEmpty()) {
                back();
            } else {
                vpContainer.setCurrentItem(deletedPosition);
            }
            updateUI(true);
            return true;
        }
        return false;
    }

    private boolean onNotFound(ConnectInfo info, Response response) {
        // 需要判断下是否
        if (info == null || response == null || response.getContent().contains("not found like of file")) {
            return false;
        }
        if (!onFileDeleted(info.getTag(), -1, false)) {
            return false;
        }
        Intent intent = new Intent(Consts.DELETE_FILE);
        intent.putExtra(Consts.RESPONSE, response);
        intent.putExtra(Consts.REQUEST, info);
        Broadcaster.sendBroadcast(intent);
        CToast.showToast(R.string.file_does_not_exists);
        if (DEBUG) {
            LogUtil.d(TAG, "onNotFound() fileID=" + info.getTag());
        }
        return true;
    }

    private boolean onLike(String fileId, boolean liked, String action) {
        if (Utils.isEmpty(fileId)) {
            return false;
        }
        fileLiked.put(fileId, liked);
        FileEntity fileEntity = null;
        if (fileId.equals(curFile.getId())) {
            fileEntity = curFile;
            if (isShown()) {
                updateLikeImage(liked);
            }
        } else {
            fileEntity = getFileById(fileId);
        }
        if (fileEntity != null) {
            int count = curFile.getLikes();
            if (Consts.DISLIKE_FILE.equals(action) && !liked) {
                if (!isShownComment()) {
                    // curFile will change like size in FramentCommentLikeInfo.
                    // To avoid repeat, if comment is shown, does NOT handle
                    // this
                    curFile.setLikes(--count);
                }
            } else if (Consts.LIKE_FILE.equals(action) && liked) {
                if (!isShownComment()) {
                    // curFile will change like size in FramentCommentLikeInfo.
                    // To avoid repeat, if comment is shown, does NOT handle
                    // this
                    curFile.setLikes(++count);
                }
            }
        }
        showLikeNum();
        return false;
    }

    private boolean isShownComment() {
        return commentLikePopUp != null && commentLikePopUp.isShowing();
    }

    private void showLike() {
        if (curFile == null) {
            return;
        }
        String fileId = curFile.getId();
        boolean liked = false;
        if (fileLiked.containsKey(fileId)) {
            liked = fileLiked.get(fileId);
        } else {
            if (!curFile.isUploading()) {
                ConnectBuilder.hasLiked(App.getUid(), fileId);
            }
        }
        updateLikeImage(liked);
    }

    private boolean onGetFileInfo(String jsonStr) {
        // Log.e(TAG, "onGetFileInfo" + curFile.getId());
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return false;
        }

        FileEntity file = Parser.parseFile(jsonStr);
        if (curFile == null || file == null) {
            return false;
        }

        if (file.getId().equals(curFile.getId())) {
            // Log.e(TAG, "file.getId().equals(curFile.getId()) " +
            // curFile.getId());
            curFile.clone(file);
            updateDetail();
        }

        return true;
    }

    private void updateLikeImage(boolean liked) {
        if (liked) {
            ivLike.setImageResource(R.drawable.heart);
        } else {
            ivLike.setImageResource(R.drawable.preview_likes);
        }
    }

    private FileEntity getFileById(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return null;
        }
        for (int i = 0; i < getImageCount(); i++) {
            FileEntity entity = fileList.get(i);
            if (entity == null) {
                continue;
            }
            if (fileId.equals(entity.getId())) {
                return entity;
            }
        }
        return null;
    }

    private FileEntity getFile(int position) {
        if (position < 0 || position >= getImageCount()) {
            return null;
        }
        FileEntity entity = fileList.get(position);
        return entity;
    }

    private Bitmap showMedia(FileEntity file) {
        Log.e(TAG, "showMedia");
        if (file == null) {
            return null;
        }
        // PhotoPreview preview = viewMap.get(file.getId());
        PhotoPreview preview = getShowView(file.getId());
        preview.setFile(file);
        return preview.showFile(true);
    }

    private void updateOptionData() {
        mOptionDatas.clear();
        if (albumOwner.equals(App.getUid())) {
            mOptionDatas.add(R.string.set_to_album_cover);
            mOptionDatas.add(R.string.delete);
            if (!curFile.getOwner().equals(App.getUid())) {
                mOptionDatas.add(R.string.report);
            }
        } else {
            if (curFile.getOwner().equals(App.getUid())) {
                mOptionDatas.add(R.string.delete);
            } else {
                mOptionDatas.add(R.string.report);
            }
        }
        mOptionDatas.add(R.string.cancel);
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (optionId == R.string.set_to_album_cover) {
            UMutils.instance().diyEvent(ID.EventSetAlbumCover);
            setAlbumCover();
        } else if (optionId == R.string.delete) {
            deleteFile();
        } else if (optionId == R.string.report) {
            JSONObject jo = new JSONObject();
            jo.put(Consts.OBJ_TYPE, Consts.FILE);
            jo.put(Consts.OBJ_ID, curFile.getId());
            ConnectBuilder.postReport(jo.toString());
            CToast.showToast(R.string.we_ll_handle_report_later);
        }

    }

    private void setAlbumCover() {
        ConnectBuilder.setAlbumThum(albumEntity.getId(), curFile.getId());
    }

    private void onOptionClicked() {
        optionsDialog = new OptionDialog(this.getBaseActivity());
        optionsDialog.setOptionListener(this);
        optionsDialog.setData(mOptionDatas);
        optionsDialog.show();
    }

    private void deleteFile() {
        AlertDialog.Builder deleteFile = new AlertDialog.Builder(this.getActivity());
        deleteFile.setTitle(R.string.confime_delete);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (curFile == null) {
                        return;
                    }
                    ImageManager.instance().deleteImage(curFile.getId());
                }
                dialog.cancel();
            }
        };
        deleteFile.setPositiveButton(R.string.confirm, listener);
        deleteFile.setNegativeButton(R.string.cancel, listener);
        deleteFile.show();
        UMutils.instance().diyEvent(ID.EventRemoveFileFormPreview);
    }

    private boolean showShare() {
        if (curFile == null) {
            CToast.showToast(R.string.resource_error);
            return false;
        }
        if (checkUploading(true)) {
            return false;
        }
        String userName;
        if (userInfo == null) {
            userName = null;
        } else {
            userName = userInfo.getName();
        }
        // PhotoPreview photoPreview = viewMap.get(curFile.getId());
        PhotoPreview photoPreview = getShowView(curFile.getId());
        if (photoPreview == null) {
            return false;
        }
        Bitmap bitmap = photoPreview.getBitmap();
        if (bitmap == null) {
            String message = getResources().getString(R.string.image);
            message = getResources().getString(R.string.x_havent_been_downloaded, message);
            CToast.showToast(message);
            return false;
        }
        if (shareDialog == null) {
            shareDialog = new ShareDialog();
            shareDialog.setOnDismissListener(new OnDismissListener() {

                @Override
                public void onDismiss(DialogInterface dialog) {
                    getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);

                }
            });
        }

        shareDialog.show(curFile, bitmap, getString(R.string.share_to), userName);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        if (App.DEBUG) {
            LogUtil.d(TAG, "curFile : " + curFile + "bimap" + bitmap + "getString(R.string.share_to)" + getString(R.string.share_to) + "userInfo" + userInfo);
        }
        UMutils.instance().diyEvent(ID.EventSharedFileToSocialNetwork);
        return true;
    }

    private void showComments() {
        if (checkUploading(true)) {
            return;
        }
        if (curFile == null || curFile.getOwner() == null || infoMap.get(curFile.getOwner()) == null) {
            return;
        }
        if (commentLikePopUp == null) {
            commentLikePopUp = new CommentLikeInfoPopup((BaseCliqActivity) getActivity());
        }
        commentLikePopUp.setFile(curFile);
        commentLikePopUp.setAlbumOwner(albumOwner);
        commentLikePopUp.setAlbum(albumEntity);
        commentLikePopUp.setUser(infoMap.get(curFile.getOwner()));
        commentLikePopUp.setFileList(fileList.getFileList());
        commentLikePopUp.setIsJoined(isJoined);
        commentLikePopUp.initPopupWindow();
        commentLikePopUp.setCommentDismissListener(new OnCommentLikeDismissListener() {
            @Override
            public void onCommentLikePopUpDissmiss() {
                if (curFile != null && MimeTypeUtil.getMime(curFile.getMimeType()) == Mime.GIF) {
                    updateUI(true);
                } else {
                    updateUI(false);
                }
                foreground.setVisibility(View.GONE);
                getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_USER);
            }
        });
        commentLikePopUp.showAtLocation(contentView, Gravity.BOTTOM, 0, 0);
        foreground.setVisibility(View.VISIBLE);
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }


    private boolean checkUploading(boolean showTip) {
        if (curFile != null && curFile.isUploading()) {
            if (showTip) CToast.showToast(R.string.uploading);
            return true;
        }
        return false;
    }

    private void onOpenUserDetails() {
        if (curFile == null || fileList.isEmpty()) {
            return;
        }
        // LinkedList<FileEntity> results = Utils.seperate(fileList,
        // curFile.getOwner());
        // if (Utils.isEmpty(results)) {
        // return;
        // }
        try {
            Intent intent = new Intent();
            // intent.putExtra(Consts.IMAGE_LIST, results);
            intent.putExtra(Consts.ALBUM_ID, curFile.getAlbum());
            intent.putExtra(Consts.USER_ID, curFile.getOwner());
            if (!isJoined) {
                intent.putExtra(Consts.ALBUM, albumEntity);
            }
            intent.putExtra(Consts.IS_JOINED, isJoined);
            intent.setClass(getActivity(), UserDetailActivity.class);
            intent.putExtra(Consts.TIME, System.currentTimeMillis());
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onLikeClicked() {

        if (Utils.isEmpty(AppData.getToken())) {
            showLogin();
        } else {
            if (curFile == null) {
                LogUtil.d(TAG, "onLikeClicked curFile is NULL");
                return;
            }
            if (checkUploading(true)) {
                return;
            }
            String fileId = curFile.getId();
            boolean liked = false;
            if (fileLiked.containsKey(fileId)) {
                liked = fileLiked.get(fileId);
            }
            if (liked) {
                UMutils.instance().diyEvent(ID.EventUnlike);
                ConnectBuilder.dislikeFile(fileId);
            } else {
                UMutils.instance().diyEvent(ID.EventLike);
                ConnectBuilder.likeFile(fileId);
            }
        }
    }

    private void showLogin() {
        if (albumEntity == null) {
            return;
        }
        CToast.showToast(R.string.you_not_login);
        Intent intent = new Intent(this.getActivity(), LoginActivity.class);
        intent.putExtra(Consts.INVITE_CODE, albumEntity.getInviteCode());
        this.startActivity(intent);
    }

    private boolean playVideo(FileEntity file) {
        if (file == null) {
            return false;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "-----------------onFileClick\n" + file);
        }
        if (MimeTypeUtil.getMime(file.getMimeType()) != Mime.VID) {
            return false;
        }
        if (playerFragment == null) {
            playerFragment = new VideoPlayerFragment();
        }
        playerFragment.setFileEntity(file);
        playerFragment.show(getBaseActivity());
//        downloadVideoFileId = playerFragment.getDownloadVideoFileId();
        return true;
    }

    private PhotoPreview getShowView(String fileId) {
        PhotoPreview preview = viewMap.get(fileId);
        FileEntity preFile = null;
        if (preview == null) {
            return null;
        }

        if (preview.getTag() instanceof FileEntity) {
            preFile = (FileEntity) preview.getTag();
        }
        if (preFile == null) {
            return null;
        }
        if (fileId.equals(preFile.getId())) {
            return preview;
        } else {
            return null;
        }

    }

    private void showDownloadMessage(String msg) {
        AlertDialog.Builder dialog = DialogUtils.create();
        dialog.setMessage(msg);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    Intent intent = new Intent(PhotoViewerFragment.this.getActivity(), LoginActivity.class);
                    intent.putExtra(Consts.ALBUM, albumEntity);
                    PhotoViewerFragment.this.startActivity(intent);
                } else {
                    dialog.cancel();
                }
                dialog.cancel();
            }
        };
        dialog.setPositiveButton(R.string.login, listener);
        dialog.setNegativeButton(R.string.not_need, listener);
        AlertDialog d = dialog.show();
        TextView tvmsg = (TextView) d.findViewById(android.R.id.message);
        tvmsg.setGravity(Gravity.CENTER);
    }

    private void save() {
        if (checkUploading(true)) {
            return;
        }
        UMutils.instance().diyEvent(ID.EventDownloadFile);
        if (curFile == null) {
            return;
        }

        // PhotoPreview photoPreview = viewMap.get(curFile.getId());

        PhotoPreview photoPreview = getShowView(curFile.getId());
        if (DEBUG) {
            LogUtil.d(TAG, "-----------------save----------------");
            // LogUtil.d(TAG, " fileId : " + curFile.getId() + "\ncurFile : " +
            // curFile + "\nviewMap : " + viewMap + "\nviewMap=" +
            // viewMap.size() + "	" + viewMap + "\n photoPreview : " +
            // photoPreview);
        }

        if (photoPreview == null) {
            return;
        }
        if (!photoPreview.hasBitmap()) {
            CToast.showToast(R.string.pls_wait_for_preview);
            return;
        }

//		if (!photoPreview.isVideo()) {
//			destPath = ImageManager.instance().save(curFile);
//		} else {
//			if (!VideoManager.instance().playable(curFile)) {
//				CToast.showToast(R.string.downloading);
//				VideoManager.instance().download(curFile);
//				return;
//			} else {
//				destPath = VideoManager.instance().save(curFile);
//			}
//		}

        onSave(photoPreview);
    }

    private void onSave(PhotoPreview photoPreview) {

        if (Utils.isEmpty(AppData.getToken())) {
            int time = AppData.getDownloadFileTime();
            time++;
            if (time == 1) {
                AppData.setDownloadFileTime(time);
                saveFile(photoPreview);
                showDownloadMessage(getActivity().getString(R.string.download_file_first_time));
            } else if (time == 2) {

                AppData.setDownloadFileTime(time);
                saveFile(photoPreview);
                showDownloadMessage(getActivity().getString(R.string.download_file_second_time));
            } else if (time >= 3) {
                AppData.setDownloadFileTime(time);
                showDownloadMessage(getActivity().getString(R.string.download_file_third_time));
            }
        } else {
            if (isJoined) {
                saveFile(photoPreview);
            } else {
                AlertDialog.Builder dialog = DialogUtils.create();
                dialog.setMessage(R.string.not_album_member_can_not_download_file);
                DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == DialogInterface.BUTTON_POSITIVE) {
                            JSONObject jo = new JSONObject();
                            jo.put(Consts.USER_ID, App.getUid());
                            jo.put(Consts.NOT_OPEN_ALBUM, false);
                            jo.put(Consts.DOWNLOAD_FILE, false);
                            ConnectBuilder.joinAlbum(albumEntity.getId(), albumEntity.getInviteCode(), jo.toJSONString());
                        } else {
                            dialog.cancel();
                        }
                        dialog.cancel();
                    }
                };
                dialog.setPositiveButton(R.string.join_ablum, listener);
                dialog.setNegativeButton(R.string.not_need, listener);
                AlertDialog d = dialog.show();
                TextView tvmsg = (TextView) d.findViewById(android.R.id.message);
                tvmsg.setGravity(Gravity.CENTER);
            }
        }
    }

    private void saveFile(PhotoPreview photoPreview) {
        String destPath = null;
        if (!photoPreview.isVideo()) {
            destPath = ImageManager.instance().save(curFile);
        } else {
            if (!VideoManager.instance().playable(curFile)) {
                CToast.showToast(R.string.downloading);
                VideoManager.instance().download(curFile);
                return;
            } else {
                destPath = VideoManager.instance().save(curFile);
            }
        }
        if (destPath != null) {
            CToast.showToast(R.string.done_download_to_phone);
            JSONObject jo = new JSONObject();
            jo.put(Consts.DOWNLOADS, 1);
            ConnectBuilder.downloadFileCount(jo.toString(), curFile.getId());
        } else {
            CToast.showToast(R.string.save_failed);
        }
    }

    private void toggleBarHideShow() {
        if (topBar.getVisibility() == View.GONE || bottomBar.getVisibility() == View.GONE) {
            setStatusUi(true);
            trigger();
        } else {
            setStatusUi(false);
            trigger();

        }
    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void trigger() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }
        View view = getActivity().getWindow().getDecorView();
        int visibility = view.getSystemUiVisibility();
        boolean visible = (visibility & view.SYSTEM_UI_FLAG_FULLSCREEN) != 0;
        setNavVisibility(visible);

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    void setNavVisibility(boolean visible) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            return;
        }
        View view = getActivity().getWindow().getDecorView();
        int visibility = view.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | view.SYSTEM_UI_FLAG_LAYOUT_STABLE | view.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
        if (!visible) {
            visibility |= view.SYSTEM_UI_FLAG_LOW_PROFILE | view.SYSTEM_UI_FLAG_FULLSCREEN | view.SYSTEM_UI_FLAG_IMMERSIVE;
        }
        final boolean changed = visibility == contentView.getSystemUiVisibility();
        if (changed || visible) {
            view.removeCallbacks(mNavHider);
        }
        view.setSystemUiVisibility(visibility);
    }


    private boolean onFileClick(FileEntity file) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "-----------------onFileClick\n" + file);
        }
        if (file == null) {
            return false;
        }
        if (MimeTypeUtil.getMime(file.getMimeType()) == Mime.VID) {
            return playVideo(file);
        } else {
            toggleBarHideShow();
        }
        return true;
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        EventBus.getDefault().unregister(this);
        vpContainer.removeAllViews();
        releaseGif();
        super.onDestroy();
    }

    @Override
    public void setData(JSONObject jo) {
        if (jo == null) {
            return;
        }
        if (jo.containsKey(Consts.DELETED_FILE_ID)) {
            String id = jo.getString(Consts.DELETED_FILE_ID);
            onFileDeleted(id, -1, true);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (App.DEBUG) {
            LogUtil.d(TAG, "onClick()	v=" + v + "\ntag=" + v.getTag());
        }
        if (id == R.id.rl_likes_picture) {
            onLikeClicked();
        } else if (id == R.id.iv_avater_picture_viewers) {
            onOpenUserDetails();
        } else if (id == R.id.rl_comments_picture) {
            showComments();
        } else if (id == R.id.rl_share_picture) {
            showShare();
        } else if (id == R.id.rl_download_picture) {
            save();
        } else if (id == R.id.iv_option_picture) {
            onOptionClicked();
        } else if (id == R.id.back) {
            back();
        } else if (v.getTag() instanceof FileEntity) {
            onFileClick((FileEntity) v.getTag());
        }
    }

    public void setCurPosition(int position) {
        this.curPosition = position;
        if (DEBUG) {
            LogUtil.d(TAG, "setCurPosition()	position=" + position);
        }
    }

    public void setAlbum(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return;
        }
        this.albumEntity = albumEntity;
        this.albumOwner = albumEntity.getOwner();
    }


    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    public void setFileList(AlbumItemController controller) {
        if (pagerAdapter != null) {
            LogUtil.d(TAG, "setFileList()		notifyDataSetChanged");
        }
        fileList = controller;
        if (pagerAdapter != null) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    public PhotoPreview getView(int position) {
        if (viewList == null) {
            return null;
        }
        int size = viewList.size();
        int truePosition = position % size;
        PhotoPreview view = null;
        if (truePosition < size) {
            view = viewList.get(truePosition);
        }
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (shareDialog != null) {
            shareDialog.onActivityResult(requestCode, resultCode, data);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class ImagePagerAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return getImageCount();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FileEntity file = getFile(position);
            String fileId = file == null ? null : file.getId();
            PhotoPreview photoPreview = getView(position);

            if (DEBUG) {
                LogUtil.d(TAG, "-----------------instantiateItem----------------");
            }
            if (photoPreview.getTag() instanceof FileEntity) {
                FileEntity preFile = (FileEntity) photoPreview.getTag();
                if (DEBUG) {
                    LogUtil.d(TAG, " preFile : " + preFile.getId());
                }
                // viewMap.remove(preFile.getId());
                photoPreview.setImageBitmap(null);
                container.removeView(photoPreview);
            }

            photoPreview.setTag(file);
            viewMap.put(fileId, photoPreview);
            // if (DEBUG) {
            // LogUtil.d(TAG, " fileId : " + fileId + "\nviewMap :" + viewMap);
            // }
            downloadGifFileId = photoPreview.getDownloadGifFileId();
            if (isWifi || firstPage) {
                firstPage = false;
                showMedia(file);
            }
            container.addView(photoPreview);
            photoPreview.setOnVideoClickListener(PhotoViewerFragment.this);
            if (DEBUG) {
                LogUtil.d(TAG, "position=" + position + "pagerAdapter.getCount()=" + pagerAdapter.getCount());
            }
            if (position + 1 == pagerAdapter.getCount() && App.albumItemController.hasMore() && pagerAdapter.getCount() > 1) {
                if (DEBUG) {
                    LogUtil.d(TAG, "=============loadMore============" + pagerAdapter.getCount());
                }
                if (App.albumItemController != null) {
                    App.albumItemController.loadMore(Consts.PHOTO_VIEWER_FRAGMENT);
                }

            }
            return photoPreview;
        }

        @Override
        public int getItemPosition(Object object) {
            if (getCount() > 0) {
                return PagerAdapter.POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

    }

    private void releaseGif() {
        if (viewList == null) {
            return;
        }
        for (PhotoPreview preview : viewList) {
            preview.release();
        }
    }

    public void clear() {
        releaseGif();
        if (viewList != null) {
            viewList.clear();
            viewList = null;
        }
        if (viewMap != null) {
            viewMap.clear();
            viewMap = null;
        }

        if (infoMap != null) {
            infoMap.clear();
            infoMap = null;
        }

        if (fileLiked != null) {
            fileLiked.clear();
            fileLiked = null;
        }
        if (downloadGifFileId != null) {
            downloadGifFileId.clear();
            downloadGifFileId = null;
        }

//        if (downloadVideoFileId != null) {
//            downloadVideoFileId.clear();
//            downloadVideoFileId = null;
//        }
//        VideoManager.instance().clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        App.resizeScreenSize(false);
        resetViewPager();
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setStatusUi(false);
            landSpace = true;
        } else {
            landSpace = false;
            setStatusUi(true);
        }
    }

    private void setStatusUi(boolean show) {
        if (landSpace) {
            return;
        }
        if (show) {
            topBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            // Utils.setStatusBarVisibility(getActivity(), false);
        } else {
            topBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            // Utils.setStatusBarVisibility(getActivity(), true);
        }

    }

    private void resetViewPager() {
        ImageSize pictureSize = new ImageSize((int) (App.SCREEN_WIDTH * zoomRatio), (int) (App.SCREEN_HEIGHT * zoomRatio));
        for (PhotoPreview view : viewList) {
            ViewPager.LayoutParams vlp = new ViewPager.LayoutParams();
            vlp.width = pictureSize.getWidth();
            vlp.height = pictureSize.getHeight();
            view.setImageSize(pictureSize);
            view.setLayoutParams(vlp);
            view.resetUI();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        // getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        App.resizeScreenSize(true);

    }

    @Override
    public boolean onBackPressed() {
        getActivity().setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        back(null);
        return true;
    }

    public void onEventMainThread(Signal signal) {
        if (!isVisible()) {
            return;
        }
        switch (signal.action) {
            case Signal.ALBUM_DETAIL_ACTIVITY_LOAD_MORE_OK:
                if (pagerAdapter != null && App.albumItemController != null) {
                    App.albumItemController.sortList();
                    pagerAdapter.notifyDataSetChanged();
                }
            case Signal.ALBUM_FINISH:
                if (albumEntity.getId().equals(signal.album)) {
                    finish();
                }
                break;
        }

    }

    public void onEventMainThread(FileDeleteEvent signal) {
        if (signal.shouldDeleteInPhotoView()) {
            onFileDeleted(signal.fileId, signal.deletedPosition, true);
        }
    }

}
