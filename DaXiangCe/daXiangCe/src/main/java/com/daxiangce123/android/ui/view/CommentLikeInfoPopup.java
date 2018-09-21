package com.daxiangce123.android.ui.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileComments;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.FileLike;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.ProgressInfo;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.LikedUserActivity;
import com.daxiangce123.android.ui.activities.LoginActivity;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.adapter.CommentPopUpListViewAdapter;
import com.daxiangce123.android.ui.view.InputView.OnInputListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.DrawableUtils;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class CommentLikeInfoPopup extends PopupWindow implements OnClickListener, OnItemClickListener, OnScrollListener, OptionListener {

    private static final int WHAT_ONREFRESHCOMPLETE = 1;
    public Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case WHAT_ONREFRESHCOMPLETE:
                    if (mPullRefreshListView != null) {
                        mPullRefreshListView.onRefreshComplete();
                    }
                    break;
            }
        }
    };

    private static final String TAG = "CommentLikeInfoPopup";
    private BaseCliqActivity mContext;
    private View mRootView;
    private boolean DEBUG = true;
    private InputView mInputView;
    private ListView mListView;
    private RelativeLayout mBack;

    private OnCommentLikeDismissListener commentDismissListener;
    private CommentPopUpListViewAdapter mCommentAdapter = null;
    private LinkedList<Object> dataList = null;
    private LinkedList<LikeEntity> likeList;
    private boolean liked = false;
    private FileEntity curFile;
    private UserInfo userInfo;
    private String fileId;
    private String albumOwner;
    private AlbumEntity albumEntity;
    private CommentEntity replyComment;
    private List<FileEntity> fileList;
    private DeleteCommentPopup deleteCommentDialog;
    private DeleteCommentPopup deleteReplyCommentDialog;
    private int startPos;
    // private LikedUserFragment likedUserFragment;
    private HashMap<String, LikeEntity> likeMap;
    public PullToRefreshListView mPullRefreshListView;
    private FileComments comments;
    private boolean isLoading = false;
    private PreviewDialog previewDialog;
    private boolean isJoined;
    private OnInputListener inputListener = new OnInputListener() {

        @Override
        public void onSend(CharSequence content) {
            if (content == null) {
                return;
            }
            final String msg = EmojiParser.getInstance().convertToMsg(content);
            SensitiveWordGrepManager.WordsWrapper wordsWrapper = new SensitiveWordGrepManager.WordsWrapper(msg, SensitiveWordGrepManager.Type.comment);
            if (!SensitiveWordGrepManager.getInstance().doSensitiveGrep(CommentLikeInfoPopup.this.mContext, wordsWrapper)) {
                return;
            }
            if (Utils.isEmpty(msg) || msg.equals("") || msg.trim().equals("")) {
                return;
            }
            JSONObject jo = new JSONObject();
            jo.put(Consts.MSG, msg);
            if (replyComment != null) {
                jo.put(Consts.REPLY_TO_USER, replyComment.getUserId());
            }

            if (App.DEBUG) {
                LogUtil.d(TAG, "onSend()	content " + msg);
            }
            ConnectBuilder.postComment(fileId, jo.toJSONString());
            UMutils.instance().diyEvent(ID.EventComment);
            replyComment = null;
        }

        @Override
        public void checkIsJoined(CharSequence hint, boolean showIME, String showView) {
            checkComment(hint, showIME, showView);
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (Consts.TRANSFER_PROGRESS.equals(action)) {
                    onProgress(intent);
                } else {
                    ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                    Response response = intent.getParcelableExtra(Consts.RESPONSE);
                    String content = response.getContent();
//                    LogUtil.d(TAG, "action=" + action + "; content=" + content);
                    if (Consts.GET_COMMENTS.equals(action)) {
                        showCommentList(content);
                    } else if (Consts.GET_LIKE.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            String fileId = connectInfo.getTag();
                            if (curFile != null && curFile.getId().equals(fileId)) {
                                showLikeList(content);
                            }
                        }
                    } else if (Consts.GET_USER_INFO.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            UserInfo info = Parser.parseUserInfo(content);
                            if (DEBUG) {
                                LogUtil.d(TAG, " userInfo  +  Consts.GET_USER_INFO : " + userInfo);
                            }
                            if (info != null) {
                                if (info.getId().equals(curFile.getOwner())) {
                                    userInfo = info;
                                    dataList.set(0, info);
                                    notifyDataSetChange(false);
                                }
                            }
                        }
                    } else if (Consts.POST_COMMENT.equals(action)) {
                        postComment(content);
                    } else if (Consts.DELETE_COMMENT.equals(action)) {
                        String commnetId = connectInfo.getTag();
                        if (response.getStatusCode() == 200) {
                            if (removeFromList(commnetId)) {
                                notifyDataSetChange(false);
                            }
                        } else {
                            CToast.showToast(R.string.request_failed);
                        }
                    } else if (Consts.GET_FILE_INFO.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            if (onGetFileInfo(content)) {
                                return;
                            }
                        }
                        dismiss();
                        CToast.showToast(R.string.failed_to_fet_file_info);
                    } else if (Consts.DELETE_FILE.equals(action)) {
                        if (response.getStatusCode() == 200) {
                            String fileId = connectInfo.getTag();
                            if (curFile.getId().equals(fileId)) {
                                JSONObject jo = new JSONObject();
                                jo.put(Consts.DELETED_FILE_ID, fileId);
                                dismiss();
                                CToast.showToast("file deleted");
                            }
                        }
                    } else if (Consts.LIKE_FILE.equals(action) || Consts.DISLIKE_FILE.equals(action) || Consts.HAS_LIKED.equals(action)) {
                        onLike(connectInfo.getTag(), response, action);
                    } else if (Consts.JOIN_ALBUM.equals(action)) {
                        joinAlbum(response, connectInfo);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showLogin() {
        if (albumEntity == null) {
            return;
        }
        CToast.showToast(R.string.you_not_login);
        Intent intent = new Intent(mContext, LoginActivity.class);
        intent.putExtra(Consts.INVITE_CODE, albumEntity.getInviteCode());
        mContext.startActivity(intent);
    }

    private void nonVistorToComment(final CharSequence hint, boolean showIME, final String showView) {
        if (isJoined) {
            if (showView.equals(Consts.SHOW_EMOJI)) {
                mInputView.showEmojiView();
            }
            if (showView.equals(Consts.SHOW_NONE)) {
                mInputView.showNone();
            }
            if (showView.equals(Consts.SHOW_EDIT)) {
                mInputView.showEdit(hint, showIME);
            }
        } else {
            AlertDialog.Builder dialog = DialogUtils.create();
            dialog.setMessage(R.string.not_album_member_can_not_comment_file);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        JSONObject jo = new JSONObject();
                        jo.put(Consts.USER_ID, App.getUid());
                        jo.put(Consts.NOT_OPEN_ALBUM, false);
                        jo.put(Consts.REPORT_COMMENT, false);
                        if (showView.equals(Consts.SHOW_EMOJI)) {
                            jo.put(Consts.SHOW_EMOJI, false);
                        }
                        if (showView.equals(Consts.SHOW_EDIT)) {
                            jo.put(Consts.SHOW_EDIT, false);
                            jo.put(Consts.HINT, hint);
                        }
                        ConnectBuilder.joinAlbum(albumEntity.getId(), albumEntity.getInviteCode(), jo.toJSONString());
                    } else {
                        mInputView.hideIME();
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

    private void checkComment(final CharSequence hint, boolean showIME, final String showView) {

        if (Utils.isEmpty(AppData.getToken())) {
            showLogin();
        } else {
            nonVistorToComment(hint, showIME, showView);
        }
    }

    private boolean onLike(String fileId, Response response, String action) {
        if (Utils.isEmpty(fileId)) {
            return false;
        }
        if (curFile == null) {
            return false;
        }
        if (!fileId.equals(curFile.getId())) {
            return false;
        }
        if (response == null) {
            return false;
        }
        boolean succeed = response.getStatusCode() == 200;
        if (App.DEBUG) {
            LogUtil.d(TAG, "onLike succeed	" + succeed + "	action=" + action);
        }
        if (Consts.LIKE_FILE.equals(action)) {
            if (succeed) {
                liked = true;
                LikeEntity likeEntity = Parser.parseLike(response.getContent());
                if (!likeMap.containsKey(App.getUid())) {
                    likeMap.put(App.getUid(), likeEntity);
                    likeList.add(0, likeEntity);
                    curFile.setLikes(curFile.getLikes() + 1);
                }
            }
        } else if (Consts.DISLIKE_FILE.equals(action)) {
            if (succeed) {
                int likeNum = curFile.getLikes() - 1;
                curFile.setLikes(likeNum < 0 ? 0 : likeNum);
                liked = false;
                LikeEntity like = likeMap.remove(App.getUid());
                boolean removed = likeList.remove(like);
                if (DEBUG) {
                    LogUtil.d(TAG, "onLike()	removed=" + removed + "	like=" + like);
                }
                int likeSize = Utils.sizeOf(likeList);
                if (likeSize < 5) {
                    ConnectBuilder.getLikeList(fileId, likeSize, 5 - likeSize);
                }
            }
        } else if (Consts.HAS_LIKED.equals(action)) {
            if (succeed) {
                if (App.DEBUG) {
                    LogUtil.d(TAG, "HAS_LIKED liked" + liked);
                }
                liked = true;
            }
        }
        if (isShowing()) {
            mCommentAdapter.updateLikeImage(liked);
            mCommentAdapter.setData(dataList);
            mCommentAdapter.notifyDataSetChanged();
        }
        return false;
    }

    private void postComment(String content) {
        CommentEntity comment = Parser.parseComment(content);
        if (comment == null) {
            return;
        }
        curFile.setComments(curFile.getComments() + 1);
        dataList.add(comment);
        notifyDataSetChange(true);
        // mListView.setSelection(mCommentAdapter.getCount() - 1);
        mListView.setSelection(0);
        if (App.DEBUG) {
            LogUtil.d(TAG, "POST_COMMENT " + content);
        }
    }

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
                if (jo.containsKey(Consts.REPORT_COMMENT)) {
                    if (jo.containsKey(Consts.SHOW_EDIT)) {
                        String hint = jo.getString(Consts.HINT);
                        checkComment(hint, true, Consts.SHOW_EDIT);
                    }
                    if (jo.containsKey(Consts.SHOW_EMOJI)) {
                        checkComment(null, false, Consts.SHOW_EMOJI);
                    }
                }
            } else if (response.getStatusCode() == 401 && errors.toErrorCode() == ErrorCode.INVALID_PASSWORD) {
                if (entity instanceof StringEntity) {
                    if (jo.containsKey(Consts.PASSWORD)) {
                        CToast.showToast(R.string.error_password);
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
//        Context mContext = mRootView.getContext();
        final EditText etInputId = new EditText(mContext);
        AlertDialog.Builder passwordDialog = null;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            passwordDialog = new AlertDialog.Builder(mContext);
        } else {
            passwordDialog = new AlertDialog.Builder(mContext, AlertDialog.THEME_HOLO_LIGHT);
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
                        jo.put(Consts.REPORT_COMMENT, false);
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


    private void showLikeList(String content) {
        FileLike fileLike = Parser.parseFileLike(content);
        if (fileLike == null) {
            return;
        }
        List<LikeEntity> l = fileLike.getLike();
        if (Utils.isEmpty(l)) {
            return;
        }
        for (LikeEntity like : l) {
            String uid = like.getUserId();
            if (!likeMap.containsKey(uid)) {
                likeMap.put(uid, like);
                likeList.add(like);
            }
        }
        notifyDataSetChange(false);
        // if (likedUserFragment != null && likedUserFragment.isShown()) {
        // likedUserFragment.onLoadLikedUser();
        // }

    }

    public void completeRefresh() {
        mHandler.sendEmptyMessageDelayed(WHAT_ONREFRESHCOMPLETE, 50);
    }

    private void showCommentList(String content) {
        isLoading = false;
        comments = Parser.parseFileComments(content);
        if (comments == null) {
            return;
        }
        LinkedList<CommentEntity> commentList = comments.getcomments();
        if (Utils.isEmpty(commentList)) {
            LogUtil.d(TAG, startPos + " comments is empty");
            return;
        } else {
            LogUtil.d(TAG, startPos + " comments size is " + commentList.size());
        }
        startPos = startPos + comments.getcomments().size();
        dataList.addAll(commentList);
        // if (comments.hasMore()) {
        // ConnectBuilder.getComments(fileId, startPos, 100);
        // } else {
        // if (curFile != null) {
        // curFile.setComments(startPos);
        // }
        // startPos = 0;
        // }
        notifyDataSetChange(true);
    }

    private boolean removeFromList(String commnetId) {
        if (Utils.isEmpty(dataList) || Utils.isEmpty(commnetId)) {
            return false;
        }
        try {
            for (Object obj : dataList) {
                if (obj instanceof CommentEntity) {
                    CommentEntity commentEntity = (CommentEntity) obj;
                    if (commnetId.equals(commentEntity.getId())) {
                        dataList.remove(commentEntity);
                        int comNum = curFile.getComments() - 1;
                        curFile.setComments(comNum < 0 ? 0 : comNum);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void initCompontent() {
        mCommentAdapter = new CommentPopUpListViewAdapter(mContext);
        mCommentAdapter.setClickListener(this, this);
        mCommentAdapter.setData(dataList);

        // mListView = (ListView) mRootView.findViewById(R.id.lv_comment_like);
        mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.lv_comment_like);
        mPullRefreshListView.setOnRefreshListener(new OnRefreshListener2<ListView>() {

            @Override
            public void onPullDownToRefresh(PullToRefreshBase<ListView> refreshView) {
                completeRefresh();
            }

            @Override
            public void onPullUpToRefresh(PullToRefreshBase<ListView> refreshView) {
                if (isLoading) {
                    return;
                }
                if (comments != null) {
                    if (comments.hasMore()) {
                        isLoading = true;
                        ConnectBuilder.getComments(fileId, startPos, 30, null);
                    } else {
                        startPos = 0;
                        CToast.showToast(R.string.no_more);
                    }
                    completeRefresh();
                }
            }
        });
        mPullRefreshListView.setMode(Mode.PULL_FROM_END);
        mListView = mPullRefreshListView.getRefreshableView();
        mListView.setOnItemClickListener(this);
        mListView.setAdapter(mCommentAdapter);
        mListView.setOnScrollListener(this);

        mBack = (RelativeLayout) mRootView.findViewById(R.id.ll_comment_back);
        mBack.setOnClickListener(this);
        mInputView = (InputView) mRootView.findViewById(R.id.input_view);
        mInputView.setOnInputListner(inputListener);
        // mInputView.setAlbum(albumEntity);
        canComment(albumEntity);
        initData();
    }

    public void canComment(AlbumEntity entity) {
        albumEntity = entity;
        if (albumEntity != null) {
            if (albumEntity.getCommentOff() && !albumOwner.equals(App.getUid())) {
                mInputView.getSendButton().setClickable(false);
                mInputView.getEmojiView().setClickable(false);
                mInputView.getEmojiEditText().setClickable(false);
                mInputView.getEmojiEditText().setFocusable(false);
                mInputView.getEmojiEditText().setHint(R.string.coles_commtent);
            } else {
                mInputView.getSendButton().setClickable(true);
                mInputView.getEmojiView().setClickable(true);
                mInputView.getEmojiEditText().setClickable(true);
                mInputView.getEmojiEditText().setFocusable(true);
                mInputView.getEmojiEditText().setHint(R.string.add_comment);
            }
        }
    }

    private void initData() {
        dataList.clear();
        dataList.add(userInfo);
        dataList.add(likeList);

        ConnectBuilder.getComments(fileId);
        completeRefresh();
        ConnectBuilder.getLikeList(fileId);

        // mCommentAdapter.setAlbum(albumEntity);
        if (curFile == null || albumOwner == null) {
            ConnectBuilder.getFileInfo(fileId);
        } else {
            mCommentAdapter.setFile(curFile);
            LogUtil.d(TAG, "albumEntity->mCommentAdapter" + albumEntity);
            if (userInfo == null) {
                ConnectBuilder.getUserInfo(curFile.getOwner());
            }
        }
        showLike();
        if (App.DEBUG) {
            LogUtil.d(TAG, "initData liked" + liked);

        }
        mCommentAdapter.updateLikeImage(liked);
    }

    private void showLike() {
        if (curFile == null) {
            return;
        }
        String fileId = curFile.getId();
        ConnectBuilder.hasLiked(App.getUid(), fileId);
    }

    private void onProgress(Intent intent) {
        if (intent == null) {
            return;
        }
        ProgressInfo pinfo = intent.getParcelableExtra(Consts.PROGRESS_INFO);
        ConnectInfo cInfo = intent.getParcelableExtra(Consts.REQUEST);
        if (pinfo == null || cInfo == null) {
            return;
        }
        String fileId = cInfo.getTag2();
        if (fileId == null || !fileId.equals(this.fileId)) {
            return;
        }
        mCommentAdapter.setProgress(pinfo.getProgress());
    }

    private boolean onGetFileInfo(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return false;
        }
        if (curFile != null) {
            return true;
        }
        curFile = Parser.parseFile(jsonStr);
        if (curFile == null) {
            return false;
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, "curFile  = " + curFile + "userInfo" + userInfo);
        }

        if (userInfo == null) {
            ConnectBuilder.getUserInfo(curFile.getOwner());
        }

        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper != null) {
            AlbumEntity albumEntity = dbHelper.getAlbum(curFile.getAlbum());
            albumOwner = albumEntity.getOwner();
            if (App.DEBUG) {
                LogUtil.d(TAG, "albumOwner:" + albumOwner);
            }
        }

        if (dataList != null) {
            int commentSize = dataList.size() - 2;
            curFile.setComments(commentSize);
        }

        mCommentAdapter.setFile(curFile);
        showLike();
        return true;
    }

    public void setFile(FileEntity fileEntity) {
        likeList.clear();
        likeMap.clear();
        liked = false;
        this.curFile = fileEntity;
        if (fileEntity != null) {
            fileId = fileEntity.getId();
        }
    }

    public void setAlbumOwner(String owner) {
        this.albumOwner = owner;
    }

    public void setAlbum(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return;
        }
        this.albumEntity = albumEntity;
    }

    public void setUser(UserInfo info) {
        this.userInfo = info;
        if (DEBUG) {
            LogUtil.d(TAG, "userInfo: " + userInfo + "info : " + info);
        }
    }

    public void setFileList(List<FileEntity> list) {
        this.fileList = list;
    }

    public void setIsJoined(boolean isJoined) {
        this.isJoined = isJoined;
    }

    // private void showLikedUser() {
    // if (likedUserFragment == null) {
    // likedUserFragment = new LikedUserFragment();
    // likedUserFragment.setFileLikeList(likeList);
    // }
    // likedUserFragment.setFile(curFile);
    // likedUserFragment.setFileList(fileList);
    // likedUserFragment.show(mContext);
    // }

    private void showLikedUser() {

        try {
            Intent intent = new Intent();
            intent.putExtra(Consts.FILE, curFile);
            // intent.putExtra(Consts.LIKE_LIST, likeList);
            intent.putExtra(Consts.ALBUM, albumEntity);
            intent.putExtra(Consts.IS_JOINED, isJoined);
            intent.setClass(App.getActivity(), LikedUserActivity.class);
            App.getActivity().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openUserDetails(String userId) {
        if (App.DEBUG) {
            LogUtil.d(TAG, " openUserDetails->userId" + userId);
        }
        if (Utils.isEmpty(userId)) {
            return;
        }
        // LinkedList<FileEntity> results = Utils.seperate(fileList, userId);
        // if (App.DEBUG) {
        // LogUtil.d(TAG, "openUserDetails->results" + results);
        // }
        try {
            Intent intent = new Intent();
            // intent.putExtra(Consts.IMAGE_LIST, results);
            intent.putExtra(Consts.ALBUM_ID, curFile.getAlbum());
            intent.putExtra(Consts.USER_ID, userId);
            if (!isJoined) {
                intent.putExtra(Consts.ALBUM, albumEntity);
            }
            intent.putExtra(Consts.IS_JOINED, isJoined);
            intent.setClass(mContext, UserDetailActivity.class);
            intent.putExtra(Consts.TIME, System.currentTimeMillis());
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteCommentPop(CommentEntity entity) {
        if (deleteCommentDialog == null) {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            mDatas.add(R.string.delete);
            mDatas.add(R.string.cancel);
            deleteCommentDialog = new DeleteCommentPopup(mContext);
            deleteCommentDialog.setOptionListener(this);
            deleteCommentDialog.setData(mDatas);
        }
        deleteCommentDialog.setCommentData(entity);
        deleteCommentDialog.show();
    }

    private void deleteReplyCommentPop(CommentEntity entity) {
        if (deleteReplyCommentDialog == null) {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            mDatas.add(R.string.delete);
            mDatas.add(R.string.reply);
            mDatas.add(R.string.cancel);
            deleteReplyCommentDialog = new DeleteCommentPopup(mContext);
            deleteReplyCommentDialog.setOptionListener(this);
            deleteReplyCommentDialog.setData(mDatas);
        }
        deleteReplyCommentDialog.setCommentData(entity);
        deleteReplyCommentDialog.show();
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (!(object instanceof CommentEntity)) {
            return;
        }
        CommentEntity entity = (CommentEntity) object;
        if (optionId == R.string.delete) {
            String commentId = entity.getId();
            ConnectBuilder.deleteComment(commentId);
            UMutils.instance().diyEvent(ID.EventRemoveComment);
        } else if (optionId == R.string.reply) {
            Spanned hint = Html.fromHtml("回复 <b>" + entity.getUserName() + "</b> :");
            replyComment = entity;
            if (App.DEBUG) {
                LogUtil.d(TAG, "replyComment " + replyComment);
            }
//            mInputView.showEdit(hint, true);
            checkComment(hint, true, Consts.SHOW_EDIT);
            UMutils.instance().diyEvent(ID.EventReplyComment);
        }
    }

    // @Override
    // public boolean onBackPressed() {
    // if (mInputView.hasShowenOther()) {
    // mInputView.showNone();
    // return true;
    // }
    // return super.onBackPressed();
    // }

    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper != null) {
            dbHelper.update(curFile);
        }
    }

    private void notifyDataSetChange(boolean needSort) {
        if (needSort) {
            sortComments();
        }
        mCommentAdapter.notifyDataSetChanged();
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (!isShowing()) {
            return;
        }
        if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            mInputView.showNone();
            Utils.hideIME(view);
            mInputView.showEdit(mContext.getString(R.string.add_comment), false);
//            checkComment(mContext.getString(R.string.add_comment), false);
            replyComment = null;
        }

    }

    private void sortComments() {
        dataList.removeFirst();
        dataList.removeFirst();
        Collections.sort(dataList, new Comparator<Object>() {
            public int compare(Object object1, Object object2) {
                int result = ((CommentEntity) object1).getCreateDate().compareToIgnoreCase(((CommentEntity) object2).getCreateDate());
                result = -result;
                return result;
            }
        });
        dataList.add(0, userInfo);

        dataList.add(1, likeList);
    }

    private void previewFile(FileEntity entity) {
        if (entity == null) {
            return;
        }
        if (previewDialog == null) {
            previewDialog = new PreviewDialog();
        }
        previewDialog.show(entity);
    }

    // ///////////////////////////PopUpWin//////////////////////////////////////////////////////

    private OnDismissListener dismissListener = new OnDismissListener() {
        @Override
        public void onDismiss() {
            Broadcaster.unregisterReceiver(receiver);
            onDestroy();
            mInputView.clearFocus();
//            Utils.hideIME(mInputView);
            commentDismissListener.onCommentLikePopUpDissmiss();
        }
    };

    public CommentLikeInfoPopup() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        dataList = new LinkedList<Object>();
        likeMap = new HashMap<String, LikeEntity>();
        likeList = new LinkedList<LikeEntity>();
    }

    public CommentLikeInfoPopup(BaseCliqActivity context) {
        super(context);
        this.mContext = context;
        dataList = new LinkedList<Object>();
        likeMap = new HashMap<String, LikeEntity>();
        likeList = new LinkedList<LikeEntity>();
        setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        initBroadcast();
    }

    public void initPopupWindow() {
        mRootView = (View) LayoutInflater.from(mContext).inflate(R.layout.popup_comment_like_info, null, false);
        initCompontent();
        setBackgroundDrawable(DrawableUtils.getDrawbale(0x00000000));
        setOnDismissListener(dismissListener);
        setAnimationStyle(R.style.AnimBottomShort);
        setHeight(LayoutParams.MATCH_PARENT);
        setWidth(LayoutParams.MATCH_PARENT);
        setOutsideTouchable(false);
        setContentView(mRootView);
        setFocusable(true);
        comments = null;
        startPos = 0;
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_COMMENTS);
        ift.addAction(Consts.GET_LIKE);
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.POST_COMMENT);
        ift.addAction(Consts.DELETE_COMMENT);
        ift.addAction(Consts.GET_FILE_INFO);
        ift.addAction(Consts.DELETE_FILE);
        ift.addAction(Consts.LIKE_FILE);
        ift.addAction(Consts.DISLIKE_FILE);
        ift.addAction(Consts.HAS_LIKED);
        ift.addAction(Consts.TRANSFER_PROGRESS);
        ift.addAction(Consts.JOIN_ALBUM);
        Broadcaster.registerReceiver(receiver, ift);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.comment_user_icon) {
            if (v.getTag() instanceof CommentEntity) {
                CommentEntity entity = (CommentEntity) v.getTag();
                openUserDetails(entity.getUserId());
            } else if (v.getTag() instanceof UserInfo) {
                UserInfo info = (UserInfo) v.getTag();
                openUserDetails(info.getId());
            }
        } else if (id == R.id.iv_like) {
            LogUtil.d(TAG, "iv_like " + liked);
            if (Utils.isEmpty(AppData.getToken())) {
                showLogin();
            } else {
                likeFile();
            }
        } else if (id == R.id.iv_file) {
            previewFile(curFile);
        } else if (id == R.id.tv_someone_like) {
            showLikedUser();
        } else if (id == R.id.ll_comment_back) {
            Utils.hideIME(mInputView);
            dismiss();
        }
    }

    private void likeFile() {
        if (albumEntity == null) {
            return;
        }
        if (albumEntity.getLikeOff() && !albumOwner.equals(App.getUid())) {
            CToast.showToast(R.string.like_file_off);
        } else {
            if (liked) {
                ConnectBuilder.dislikeFile(fileId);
            } else {
                ConnectBuilder.likeFile(fileId);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        LogUtil.d(TAG, "mListView.getHeaderViewsCount() : " + mListView.getHeaderViewsCount());
        LogUtil.d(TAG, "position: " + position);

        int realPos = position - mListView.getHeaderViewsCount() + 1;
        if (parent instanceof GridView) {
            if (view.getTag(view.getId()) instanceof LikeEntity) {
                LikeEntity entity = (LikeEntity) view.getTag(view.getId());
                openUserDetails(entity.getUserId());
            }

        } else {
            if (realPos <= 1) {
                return;
            } else {
                CommentEntity entity = (CommentEntity) dataList.get(realPos);
                LogUtil.d(TAG, "position: " + position);
                String commentUserId = entity.getUserId();
                if (commentUserId.equals(App.getUid())) {
                    deleteCommentPop(entity);
                } else if (curFile.getOwner().equals(App.getUid()) || albumOwner.equals(App.getUid())) {
                    deleteReplyCommentPop(entity);
                } else {
                    Spanned hint = Html.fromHtml("回复 <b>" + entity.getUserName() + "</b> :");
                    replyComment = entity;
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "replyComment " + replyComment);
                    }
//                    mInputView.showEdit(hint, true);
                    checkComment(hint, true, Consts.SHOW_EDIT);

                }
            }
        }
    }

    @Override
    public void showAsDropDown(View anchor) {
        onShow();
        super.showAsDropDown(anchor);
    }

    @Override
    public void showAsDropDown(View anchor, int xoff, int yoff) {
        onShow();
        super.showAsDropDown(anchor, xoff, yoff);
    }

    @Override
    public void showAtLocation(View parent, int gravity, int x, int y) {
        super.showAtLocation(parent, gravity, x, y);
        onShow();
    }

    private void onShow() {
        initBroadcast();
        // showSoftKeyboard(mInputView);
    }

    public OnCommentLikeDismissListener getCommentDismissListener() {
        return commentDismissListener;
    }

    public void setCommentDismissListener(OnCommentLikeDismissListener commentDismissListener) {
        this.commentDismissListener = commentDismissListener;
    }

    public static interface OnCommentLikeDismissListener {
        public void onCommentLikePopUpDissmiss();
    }

    public void showSoftKeyboard(View view) {
        if (view == null) return;
        // view.requestFocus();
        Utils.showIME();
    }

}
