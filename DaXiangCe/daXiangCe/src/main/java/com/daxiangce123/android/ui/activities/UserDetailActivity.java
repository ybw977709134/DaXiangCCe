package com.daxiangce123.android.ui.activities;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumAct;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.MemberPermissionSetFragment;
import com.daxiangce123.android.ui.pages.UserDetailFragment;
import com.daxiangce123.android.ui.pages.UserDetailOtherAlbumFragment;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CDialog;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;

import java.util.ArrayList;
import java.util.HashSet;

public class UserDetailActivity extends BaseCliqActivity implements OnClickListener, OptionListener {

    private final static String TAG = "UserDetailActivity";
    private UserDetailFragment userDetailFragment;
    private MemberPermissionSetFragment memberPermissionSetFragment;
    private UserDetailOtherAlbumFragment userDetailOtherAlbumFragment;
    // private List<FileEntity> fileEntities;
    private String albumId;
    private String userId;
    private DBHelper dbHelper;
    private boolean isJoined;
    private boolean DEBUG = true;
    private TextViewParserEmoji tvName;
    private TextView tvComment;
    private TextView tvLikeNum;
    private TextView tvImageNum;
    private TextView tvOtherAlbum;
    private TextView filesInAlbum;
    // private TextView permissionSet;
    private ImageView ivOptions;
    private TextView tvReport;
    private ImageViewEx ivAvater;
    private ImageView mBack;
    private int pictureNum;
    private int videoNum;
    private int commentNum;
    private int likeNum;
    private CDialog avatarDialog;
    private ImageSize bigAvatarSize;
    private AlbumEntity albumEntity;
    private MemberEntity memberEntity;
    private UserInfo userInfo;
    private ArrayList<Integer> mOptionDatas = new ArrayList<Integer>();
    private OptionDialog optionsDialog;
    private OptionDialog reportReasonDialog;
//    private boolean nonMember;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                String content = response.getContent();
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.GET_USER_INFO.equals(action)) {
                    onGetUserInfo(response);
                } else if (Consts.GET_ALBUM_ACT.equals(action)) {
                    onGetDetail(response);
                } else if (Consts.DOWNLOAD_FILE.equals(action)) {
                    onDownloadAvater(info, response);
                } else if (Consts.GET_MEMBER_ROLE.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        MemberEntity member = Parser.parseMember(content);
                        if (member != null && member.getUserId().equals(userId)) {
                            memberEntity = member;
                        }
                    }
//                    else if (response.getStatusCode() == 404){
//                        nonMember = true;
//                    }
                } else if (Consts.SET_MEMBER_ROLE.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        ConnectBuilder.getMemberRole(albumId, userId);
                    }
                } else if (Consts.LEAVE_ALBUM.equals(action)) {
                    LogUtil.d(TAG, "LEAVE_ALBUM response = " + response);
                    if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
                        if (!info.getTag().equals("")) {
                            return;
                        }
                        if (info.getTag3() != null) {
                            if (albumEntity.getId().equals(info.getTag3())) {
                                String id = info.getTag2();
                                LogUtil.d(TAG, "LEAVE_ALBUM userId = " + id);
                                if (deleteMember(id)) {
                                    CToast.showToast(R.string.delete_succeed);
                                    // after delete
                                    return;
                                }
                            }
                        }
                    } else {
                        CToast.showToast(R.string.request_failed);
                    }
                } else if (Consts.DELETE_MEMBER.equals(action)) {
                    LogUtil.d(TAG, "DELETE_MEMBER response = " + response);
                    if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
                        String id = info.getTag2();
                        LogUtil.d(TAG, "DELETE_MEMBER userId = " + id);
                        if (deleteMember(id)) {
                            CToast.showToast(R.string.delete_succeed);
                            // after delete
                            return;
                        }
                    } else {
                        CToast.showToast(R.string.request_failed);
                    }
                }

                // else if (Consts.POST_REPORT.equals(action)) {
                // onReport(response, info);
                // }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private boolean deleteMember(String uid) {
        if (Utils.isEmpty(uid)) {
            LogUtil.d(TAG, "bad paraments " + uid);
            return false;
        }
        try {
            if (!uid.equals(memberEntity.getUserId())) {
                return false;
            }
            int members = albumEntity.getMembers() - 1;
            albumEntity.setMembers(members);
            writeMemberSize();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private void writeMemberSize() {
        if (isJoined) {
            if (dbHelper == null) {
                dbHelper = new DBHelper(App.getUid());
            }
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            HashSet<String> keys = new HashSet<String>();
            keys.add(Consts.MEMBERS);
            albumEntity.setUpdateKey(keys);
            albumEntity.update(db,false);
            db.close();
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        super.onCreate(savedInstanceState);
        // App.addActivity(this);
        setContentView(R.layout.activity_user_detail);
        initCompontent();

        initBroad();
        initData();
        updateAvater();
        showDetail();

        ConnectBuilder.getAlbumAct(albumId, userId);
        ConnectBuilder.getUserInfo(userId);
        ConnectBuilder.getMemberRole(albumId, userId);

    }

    private void initCompontent() {
        mBack = (ImageView) this.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        ivAvater = (ImageViewEx) this.findViewById(R.id.iv_avater);
        ivAvater.setOnClickListener(this);
        tvComment = (TextView) this.findViewById(R.id.tv_comments_user_detail);
        tvImageNum = (TextView) this.findViewById(R.id.tv_image_num_user_detail);
        tvLikeNum = (TextView) this.findViewById(R.id.tv_likes_user_detail);
        tvName = (TextViewParserEmoji) this.findViewById(R.id.tv_user_name);
        tvOtherAlbum = (TextView) this.findViewById(R.id.other_album);
        tvOtherAlbum.setOnClickListener(this);
        filesInAlbum = (TextView) this.findViewById(R.id.files_in_album);
        filesInAlbum.setOnClickListener(this);
        // permissionSet = (TextView) this.findViewById(R.id.tv_permission_set);
        // permissionSet.setOnClickListener(this);
        ivOptions = (ImageView) this.findViewById(R.id.iv_options);
        ivOptions.setOnClickListener(this);

        tvReport = (TextView) this.findViewById(R.id.title_user_report);
        tvReport.setOnClickListener(this);

        int padding = Utils.dp2px(this, 10);
        int width = App.SCREEN_WIDTH - 2 * padding;
        int height = (width - ((3 - 1) * padding)) / 3;

        LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(height, height);
        ivAvater.setLayoutParams(llp);
    }

    private void initData() {
        try {
            Intent intent = getIntent();

            if (intent.hasExtra(Consts.TIME)) {
                if (DEBUG) {
                    try {
                        long duration = System.currentTimeMillis() - intent.getLongExtra(Consts.TIME, 0);
                        LogUtil.d(TAG, "duration = " + duration);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            // if (intent.hasExtra(Consts.IMAGE_LIST)) {
            // fileEntities = intent.getExtras().getParcelableArrayList(
            // Consts.IMAGE_LIST);
            // }

            if (intent.hasExtra(Consts.ALBUM_ID)) {
                albumId = intent.getStringExtra(Consts.ALBUM_ID);
            }
            if (intent.hasExtra(Consts.USER_ID)) {
                userId = intent.getStringExtra(Consts.USER_ID);
            }

            if (intent.hasExtra(Consts.IS_JOINED)) {
                isJoined = intent.getBooleanExtra(Consts.IS_JOINED, false);
            }

            if (intent.hasExtra(Consts.ALBUM)) {
                albumEntity = intent.getParcelableExtra(Consts.ALBUM);
            }

            // if (intent.hasExtra(Consts.USER_INFO)) {
            // userInfo = intent.getExtras().getParcelable(Consts.USER_INFO);
            // }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // if (DEBUG) {
        // if (fileEntities != null) {
        // LogUtil.d(TAG, " file size is "
        // + ((fileEntities != null) ? fileEntities.size()
        // : "null"));
        // }
        // }

        updateUI();
    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_USER_INFO);
        ift.addAction(Consts.GET_ALBUM_ACT);
        ift.addAction(Consts.DOWNLOAD_FILE);
        ift.addAction(Consts.GET_MEMBER_ROLE);
        ift.addAction(Consts.SET_MEMBER_ROLE);
        ift.addAction(Consts.POST_REPORT);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.DELETE_MEMBER);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void updateUI() {
        if (isJoined) {
            DBHelper dbHelper = App.getDBHelper();
            if (dbHelper != null) {
                albumEntity = dbHelper.getAlbum(albumId);
            }
        }

        tvLikeNum.setText(" " + String.valueOf(likeNum));
        tvComment.setText(" " + String.valueOf(commentNum));
        int total = pictureNum + videoNum;
        tvImageNum.setText(" " + String.valueOf(total));

        if (albumEntity != null && !App.getUid().equals(userId) && !Utils.isEmpty(AppData.getToken())) {
            if (App.getUid().equals(albumEntity.getOwner())) {
                ivOptions.setVisibility(View.VISIBLE);
                tvReport.setVisibility(View.GONE);
                updateOptionData();
            } else {
                ivOptions.setVisibility(View.GONE);
                tvReport.setVisibility(View.VISIBLE);
            }

        } else {
            ivOptions.setVisibility(View.GONE);
        }

        showAvater();
        showName();
    }

    private void updateOptionData() {
        mOptionDatas.clear();
//		if (App.getUid().equals(albumEntity.getOwner())) {
        mOptionDatas.add(R.string.permission_set);
        mOptionDatas.add(R.string.delete_member);
        mOptionDatas.add(R.string.delete_member_and_block);
        mOptionDatas.add(R.string.report);
//		} else {
//			mOptionDatas.add(R.string.report);
//		}

        mOptionDatas.add(R.string.cancel);
    }

    private void onGetUserInfo(Response response) {
        if (response == null) {
            return;
        }
        String jsonstr = response.getContent();
        UserInfo user = Parser.parseUserInfo(jsonstr);
        if (user.getId().equals(userId)) {
            userInfo = user;
        }
        showName();
    }

    private void showAvater() {
        ivAvater.setImageBitmap(null);
        ImageManager.instance().loadAvater(ivAvater, userId);
    }

    private void showName() {
        if (userInfo == null) {
            return;
        }
        // tvName.setText(userInfo.getName());
        // tvName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // double viewWidth = (double) tvName.getMeasuredWidth();
        // TextPaint paint = tvName.getPaint();
        // double textWidth = (double) paint.measureText(userInfo.getName());
        // tvName.setEmojiText(userInfo.getName(), viewWidth, textWidth);
        tvName.setEmojiText(userInfo.getName());

    }

    private void onGetDetail(Response response) {
        if (response == null) {
            return;
        }
        String jsonStr = response.getContent();
        AlbumAct albumActivity = Parser.parseAlbumAct(jsonStr);
        pictureNum = albumActivity.getPictureNum();
        videoNum = albumActivity.getVideoNum();
        commentNum = albumActivity.getCommentNum();
        likeNum = albumActivity.getLikeNum();
        updateUI();
    }

    // private void onReport(Response response, ConnectInfo info) {
    // if (response.getStatusCode() == 200) {
    // String jsonStr = info.getTag();
    // JSONObject jo = JSONObject.parseObject(jsonStr);
    // String user = jo.getString(Consts.OBJ_ID);
    // if (!user.equals(userId)) {
    // return;
    // }
    // CToast.showToast(R.string.we_ll_handle_report_later);
    // }
    // }

    private void onDownloadAvater(ConnectInfo info, Response response) {
        if (info == null || response == null || info.getTag() == null) {
            return;
        }
        if (response.getStatusCode() != 200) {
            return;
        }
        String path = ImageManager.instance().getImageCachePath(userId);
        if (!info.getTag().equals(path)) {
            return;
        }
        ImageManager.instance().remove(userId);
        showAvater();
        showDialogAvater();
    }

    private void showAvatarDialog() {
        if (avatarDialog == null) {
            avatarDialog = new CDialog(R.style.custom_dialog);
            avatarDialog.setCanceledOnTouchOutside(true);
            LinearLayout contentView = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.big_avatar_dialog, null);
            avatarDialog.setContentView(contentView);
            avatarDialog.findViewById(R.id.view_empty_big_avatar).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    avatarDialog.dismiss();
                }
            });
//			int width = App.SCREEN_WIDTH / 2;
//			int height = App.SCREEN_HEIGHT / 2;
//			bigAvatarSize = new ImageSize(width, height);
            ImageViewEx ivAvatar = (ImageViewEx) avatarDialog.findViewById(R.id.iv_big_avatar);
            int width = (int) (App.SCREEN_WIDTH * 0.8);
            int height = (int) (App.SCREEN_HEIGHT * 0.8);
            ViewGroup.LayoutParams llp = new LinearLayout.LayoutParams(width, height);
            ivAvatar.setLayoutParams(llp);
        }
        avatarDialog.show();
        showDialogAvater();
    }

    private void showDialogAvater() {
        if (avatarDialog == null) {
            return;
        }
        if (!avatarDialog.isShowing()) {
            return;
        }
        ImageViewEx ivAvatar = (ImageViewEx) avatarDialog.findViewById(R.id.iv_big_avatar);
        ivAvatar.setImageBitmap(null);
//		ImageManager.instance().load(ivAvatar, userId, bigAvatarSize);
        ImageManager.instance().loadAvater(ivAvatar, userId);
    }

    private void showUserOtherAlbum() {
        tvOtherAlbum.setTextColor(0xff0794e1);
        tvOtherAlbum.setBackgroundResource(R.drawable.files_in_album_corners_bg);

        filesInAlbum.setTextColor(0xFFFFFFFF);
        filesInAlbum.setBackgroundResource(R.drawable.transparent);

        if (userDetailOtherAlbumFragment == null) {
            userDetailOtherAlbumFragment = new UserDetailOtherAlbumFragment();
        }
        userDetailOtherAlbumFragment.setUserId(userId);
        showOnly(userDetailOtherAlbumFragment);
    }

    private void showDetail() {
        filesInAlbum.setTextColor(0xff0794e1);
        filesInAlbum.setBackgroundResource(R.drawable.files_in_album_corners_bg);

        tvOtherAlbum.setTextColor(0xFFFFFFFF);
        tvOtherAlbum.setBackgroundResource(R.drawable.transparent);

        if (userDetailFragment == null) {
            userDetailFragment = new UserDetailFragment();
        }
        // userDetailFragment.setFiles(fileEntities);
        userDetailFragment.setAlbumId(albumId);
        if (albumEntity != null) {
            userDetailFragment.setAlbumEntity(albumEntity);
        }
        userDetailFragment.setUserId(userId);
        userDetailFragment.setIsJoined(isJoined);
        showOnly(userDetailFragment);
    }

    private void updateAvater() {
        if (Utils.isEmpty(userId)) {
            return;
        }
        if (userId.equals(App.getUid())) {
            return;
        }
        String path = ImageManager.instance().getImageCachePath(userId);
        if (!FileUtil.exists(path)) {
            return;
        }
        ConnectBuilder.getAvatar(path, userId);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            finish();
        } else if (id == R.id.iv_avater) {

            showAvatarDialog();
        } else if (id == R.id.other_album) {
            UMutils.instance().diyEvent(ID.EventUserOtherAlbum);
            showUserOtherAlbum();
        } else if (id == R.id.files_in_album) {
            showDetail();
        } else if (id == R.id.iv_options) {
            onOptionClicked();
        } else if (id == R.id.title_user_report) {
            onReportReason();
        }
        // else if (id == R.id.tv_permission_set) {
        // memberPermissionSetFragment = new MemberPermissionSetFragment();
        // memberPermissionSetFragment.setMember(memberEntity);
        // memberPermissionSetFragment.show(this);
        //
        // }
    }

    private void onOptionClicked() {
        optionsDialog = new OptionDialog(this);
        optionsDialog.setOptionListener(this);
        optionsDialog.setData(mOptionDatas);
        optionsDialog.show();
    }

    private void onReportReason() {
        ArrayList<Integer> mDatas = new ArrayList<Integer>();
        mDatas.add(R.string.release_of_sensitive_info);
        mDatas.add(R.string.advertising_content);
        mDatas.add(R.string.invasion_of_privacy);
        mDatas.add(R.string.other_reason);
        mDatas.add(R.string.cancel);

        reportReasonDialog = new OptionDialog(this);
        reportReasonDialog.setOptionListener(this);
        reportReasonDialog.setData(mDatas);
        reportReasonDialog.setTitle(getResources().getString(R.string.why_report));
        reportReasonDialog.show();
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        JSONObject jo = new JSONObject();
        if (optionId == R.string.permission_set) {
            memberPermissionSetFragment = new MemberPermissionSetFragment();
            memberPermissionSetFragment.setMember(memberEntity);
            memberPermissionSetFragment.show(this);
        } else if (optionId == R.string.delete_member) {
            showDeleteConfirmDialog(false);
        } else if (optionId == R.string.delete_member_and_block) {
            showDeleteConfirmDialog(true);
        } else if (optionId == R.string.report) {
            onReportReason();
        } else if (optionId == R.string.release_of_sensitive_info) {
            jo.put(Consts.REPORT_REASON, getResources().getString(R.string.release_of_sensitive_info));
            reportUser(jo);
        } else if (optionId == R.string.advertising_content) {
            jo.put(Consts.REPORT_REASON, getResources().getString(R.string.advertising_content));
            reportUser(jo);
        } else if (optionId == R.string.invasion_of_privacy) {
            jo.put(Consts.REPORT_REASON, getResources().getString(R.string.invasion_of_privacy));
            reportUser(jo);
        } else if (optionId == R.string.other_reason) {
            jo.put(Consts.REPORT_REASON, getResources().getString(R.string.other_reason));
            reportUser(jo);
        }
    }

    private void reportUser(JSONObject jo) {
        jo.put(Consts.OBJ_TYPE, Consts.USER);
        jo.put(Consts.OBJ_ID, userId);
        ConnectBuilder.postReport(jo.toString());
        CToast.showToast(R.string.we_ll_handle_report_later);
    }

    private void showDeleteConfirmDialog(final boolean block) {
        if (memberEntity == null) {
            return;
        }

        int tip = block ? R.string.confirm_let_x_delete_from_album_and_block : R.string.confirm_let_x_delete_from_album;
        AlertDialog.Builder deleteMember = new AlertDialog.Builder(this);
        deleteMember.setMessage(this.getString(tip, memberEntity.getName()));
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "deleteMember");
                    }
                    if (block) {
                        ConnectBuilder.leaveAlbum(memberEntity.getAlbumId(), memberEntity.getUserId(), true);
                    } else {
                        ConnectBuilder.deleteMember(memberEntity.getAlbumId(), memberEntity.getUserId());
                    }
                    UMutils.instance().diyEvent(ID.EventRemoveMember);
                }
                dialog.cancel();
            }
        };

        deleteMember.setPositiveButton(R.string.confirm, listener);
        deleteMember.setNegativeButton(R.string.cancel, listener);
        AlertDialog deleteDialog = deleteMember.show();
        TextView textView = (TextView) deleteDialog.findViewById(android.R.id.message);
        if (block) {
            textView.setLines(2);
        }
        textView.setSingleLine(false);
        textView.setGravity(Gravity.CENTER_HORIZONTAL);

    }

    @Override
    public boolean showFragment(BaseFragment fragment) {
        if (fragment == null) {
            return false;
        }

        fragment.setTitleBarVisibility(View.GONE);
        fragment.setBoottomBarVisibility(View.GONE);

        return super.showFragment(fragment);
    }

    @Override
    protected void onDestroy() {
        Broadcaster.sendBroadcast(Consts.STOP_FETCH_EVENT_SERVICE);
        Broadcaster.unregisterReceiver(receiver);
        DBHelper dbHelper = App.getDBHelper();
        if (dbHelper != null) {
            dbHelper.close();
        }
        super.onDestroy();
    }
}
