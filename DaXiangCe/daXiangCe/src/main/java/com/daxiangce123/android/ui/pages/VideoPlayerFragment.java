package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.http.ProgressInfo;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.VideoManager;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.HashSet;

/**
 * @author ram
 * @project Pickup
 * @time Feb 20, 2014
 */
public class VideoPlayerFragment extends BaseFragment implements OnPreparedListener, OnCompletionListener, OnClickListener {

    private enum STATUS {
        INIT, PREPARED, STOPPED, PAUSED, PLAYING,
    }

    private final String TAG = "VideoPlayerFragment";
    private View ivPlay;
    private View llLoading;
    private View contentView;
    private VideoView videoView;
    private TextView tvLoadingMsg;
    private String videoKey;
    private FileEntity fileEntity;
    private String filePath;
    private MediaController mController;
    private STATUS mStatus = STATUS.INIT;
    private String token;
    private HashSet<String> downloadVideoFileId;


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (Consts.TRANSFER_PROGRESS.equals(action)) {
                    onProgress(intent);
                } else if (Consts.DOWNLOAD_FILE.equals(action)) {
                    onDownload(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    @Override
    public String getFragmentName() {
        return "VideoPlayerFragment";
    }

    public VideoPlayerFragment() {
        setBoottomBarVisibility(View.GONE);
        setTitleBarVisibility(View.GONE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBroad();
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_video_playder, container, false);
            initUI();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        updateUI();
        return contentView;
    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.DOWNLOAD_FILE);
        ift.addAction(Consts.TRANSFER_PROGRESS);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void initUI() {
        tvLoadingMsg = (TextView) contentView.findViewById(R.id.tv_msg_loading);
        videoView = (VideoView) contentView.findViewById(R.id.vv_player);
        contentView.findViewById(R.id.iv_back).setOnClickListener(this);
        llLoading = contentView.findViewById(R.id.ll_loading);
        ivPlay = contentView.findViewById(R.id.iv_play);
        ivPlay.setOnClickListener(this);
        mController = new MediaController(getActivity());
        videoView.setMediaController(mController);
        videoView.setOnCompletionListener(this);
        videoView.setOnPreparedListener(this);
        downloadVideoFileId = new HashSet<String>();
    }

    private void updateUI() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "updateUI()");
        }
        play();
        /*------------update title------------*/
    }

    private boolean play() {
        mStatus = STATUS.INIT;
        videoView.stopPlayback();
        videoView.setVideoURI(null);
        if (fileEntity != null && fileEntity.isUploading()) {
            filePath = fileEntity.getFilePath();
        }

        if (filePath != null) {
            videoKey = filePath;
        } else {
            videoKey = VideoManager.instance().getVideoPath(fileEntity);
            if (!VideoManager.instance().playable(fileEntity)) {
                if (!downloadVideoFileId.contains(fileEntity.getId())) {
                    VideoManager.instance().download(fileEntity, token);
                    ivPlay.setVisibility(View.GONE);
                    showLoading(getString(R.string.downloading));
                    downloadVideoFileId.add(fileEntity.getId());
                }
                return false;
            }
        }
        showLoading(getString(R.string.preparing_to_play));
        try {
            initUri();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

//    public HashSet<String> getDownloadVideoFileId() {
//        return downloadVideoFileId;
//    }

    private void initUri() {
        try {
            videoView.setVideoURI(Uri.parse(videoKey));
            // Method method = VideoView.class.getDeclaredMethod("setVideoURI",
            // new Class[] { Uri.class, Map.class });
            // method.invoke(videoView, new Object[] { Uri.parse(videoPath),
            // ConnectBuilder.getAuthentication() });
        } catch (Exception e) {
            videoView.setVideoURI(Uri.parse(videoKey));
            LogUtil.d(TAG, "Exception initUri()  " + videoKey + " exception is " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void onProgress(Intent intent) {
        if (intent == null) {
            return;
        }
        ProgressInfo pinfo = intent.getParcelableExtra(Consts.PROGRESS_INFO);
        if (pinfo == null) {
            return;
        }
        if (!pinfo.getTag().equals(videoKey)) {
            return;
        }
        String msg = getString(R.string.downloading) + " " + pinfo.getProgress() + "%";
        showLoading(msg);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onReceive()" + pinfo.getProgress());
        }
    }

    private boolean onDownload(Intent intent) {
        if (Utils.isEmpty(videoKey)) {
            return false;
        }
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        if (info == null) {
            return false;
        }
        if (!videoKey.equals(info.getTag())) {
            return false;
        }
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        if (response == null || response.getStatusCode() != 200) {
            CToast.showToast("Download Error");
            return false;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "onDownload() videoPath=" + videoKey);
        }
        return play();
    }

    // private boolean isWebAddr(String path) {
    // if (Utils.isEmpty(path)) {
    // return false;
    // }
    // path = path.toLowerCase(Locale.ENGLISH);
    // if (path.startsWith("http://") || path.startsWith("https://")) {
    // return true;
    // }
    // return false;
    // }

    private void showLoading(CharSequence msg) {
        if (msg == null) {
            llLoading.setVisibility(View.GONE);
            return;
        }
        llLoading.setVisibility(View.VISIBLE);
        tvLoadingMsg.setText(msg);
    }

    public void setTempToken(String token) {
        this.token = token;
    }

    public void setFileEntity(FileEntity fileEntity) {
        this.fileEntity = fileEntity;
    }

    public void setVideoPath(String path) {
        this.videoKey = path;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mStatus = STATUS.PREPARED;
        videoView.start();
        mStatus = STATUS.PLAYING;
        showLoading(null);
        LogUtil.d(TAG, "onPrepared()  mp duration=" + mp.getDuration() + " size:" + mp.getVideoWidth() + "x" + mp.getVideoHeight());
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mStatus = STATUS.STOPPED;
//        ivPlay.setVisibility(View.VISIBLE);
        back();
        CToast.showToast(R.string.video_play_completed);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_play) {
            videoView.resume();
            v.setVisibility(View.GONE);
        } else if (id == R.id.iv_back) {
            back();
            ivPlay.setVisibility(View.GONE);
            LogUtil.d(TAG, "onClick BACK");
        }
    }

    @Override
    public void onShown() {
        super.onShown();
        if (STATUS.INIT == mStatus) {
            return;
        }
        if (mStatus == STATUS.PLAYING) {
            return;
        }
        if (STATUS.PAUSED == mStatus) {
            videoView.start();
            mStatus = STATUS.PLAYING;
        }
    }

    @Override
    public void onHidden() {
        super.onHidden();
        if (mStatus == STATUS.PLAYING) {
            mStatus = STATUS.PAUSED;
            videoView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        videoView.stopPlayback();
        mStatus = STATUS.STOPPED;
        Broadcaster.unregisterReceiver(receiver);
    }

}
