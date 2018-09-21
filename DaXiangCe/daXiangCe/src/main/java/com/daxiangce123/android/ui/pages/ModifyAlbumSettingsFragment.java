package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.AlbumDetailActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.Preference;
import com.daxiangce123.android.ui.view.Preference.CheckedChangedListener;
import com.daxiangce123.android.ui.view.TransferOwnerPopup.OnTransfrerOwnerClicked;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class ModifyAlbumSettingsFragment extends BaseFragment implements OnClickListener, CheckedChangedListener {

    private static final String TAG = "ModifyAlbumSettings";
    private View contentView;
    private TextView mTitle;
    private TextView mBack;
    private TextView mComplete;
    private LinearLayout llAlbumName;
    private TextView quitAlbum, reportAlbum, transferOwner, deleteAlbum;
    private EditText mInputAlbumName;
    private EditText mInputAlbumDescription;
    private EditText mInputPassword;
    private Preference pfPrivateAlbum;
    private Preference pfSeniorManagement;
    private Preference pfAlbumPassword;
    private Preference pfCommentFile;
    private Preference pfLikeFile;
    private Preference pfMemberUploadFile;
    private Preference pfAllowJoinAlbum;
    private Preference pfPushNotification;
    private LinearLayout llPermissionSettings;

    private boolean uploadable;
    private boolean isPrivate;
    private boolean isLocked;
    private boolean commentOff;
    private boolean likeOff;
    private boolean hasPassword;
    private boolean pushNotificationOn;
    // private boolean pwdChecked;
    private AlbumEntity albumEntity;
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                String content = response.getContent();
                if (Consts.CHECK_ALBUM_ACCESS_CONTROL.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        LogUtil.d(TAG, "CHECK_ALBUM_ACCESS_CONTROL");
                        hasPassword = Parser.parseHasPasswd(content);
                        LogUtil.d(TAG, "hasPassword-BroadcastReceiver" + hasPassword);
                        pfAlbumPassword.setChecked(hasPassword);
                    }
                    LogUtil.d(TAG, "CHECK_ALBUM_ACCESS_CONTROL END " + hasPassword);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };
    OnTransfrerOwnerClicked transfrerOwnerClickedListener = new OnTransfrerOwnerClicked() {

        @Override
        public void onTransferOwnerClicked() {
            back();

        }
    };

    @Override
    public String getFragmentName() {
        return "ModifyAlbumSettingsFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        initBroadcast();
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_create_album, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        return contentView;
    }

    private void initComponent() {
        mTitle = (TextView) contentView.findViewById(R.id.tv_title);
        mTitle.setText(R.string.modify_album_settings);
        mBack = (TextView) contentView.findViewById(R.id.tv_back);
        mComplete = (TextView) contentView.findViewById(R.id.tv_complete);
        mComplete.setText(R.string.save);
        llAlbumName = (LinearLayout) contentView.findViewById(R.id.ll_album_name);
        llAlbumName.setVisibility(View.GONE);
        mInputAlbumName = (EditText) contentView.findViewById(R.id.et_album_name);
        mInputAlbumDescription = (EditText) contentView.findViewById(R.id.et_album_description);
        mInputPassword = (EditText) contentView.findViewById(R.id.et_album_password);

        pfPrivateAlbum = (Preference) contentView.findViewById(R.id.pf_private_album);
        pfSeniorManagement = (Preference) contentView.findViewById(R.id.pf_senior_management);
        llPermissionSettings = (LinearLayout) contentView.findViewById(R.id.ll_permission_settings);
        pfAlbumPassword = (Preference) contentView.findViewById(R.id.pf_album_password);
        pfCommentFile = (Preference) contentView.findViewById(R.id.pf_comment_file);
        pfLikeFile = (Preference) contentView.findViewById(R.id.pf_like_file);
        pfMemberUploadFile = (Preference) contentView.findViewById(R.id.pf_member_upload_file);
        pfAllowJoinAlbum = (Preference) contentView.findViewById(R.id.pf_allow_join_album);
        pfPushNotification = (Preference) contentView.findViewById(R.id.pf_push_notification);
        quitAlbum = (TextView) contentView.findViewById(R.id.pf_quit_album);
        reportAlbum = (TextView) contentView.findViewById(R.id.pf_report_album);
        deleteAlbum = (TextView) contentView.findViewById(R.id.pf_delete_album);
        transferOwner = (TextView) contentView.findViewById(R.id.pf_transfer_owner);

        quitAlbum.setOnClickListener(this);
        reportAlbum.setOnClickListener(this);
        deleteAlbum.setOnClickListener(this);
        transferOwner.setOnClickListener(this);
        mBack.setOnClickListener(this);
        mComplete.setOnClickListener(this);
        mInputAlbumName.setOnClickListener(this);
        mInputAlbumDescription.setOnClickListener(this);

        pfPrivateAlbum.setOnCheckedChangeListener(this);

        pfSeniorManagement.setOnCheckedChangeListener(this);
        pfSeniorManagement.setChecked(true);

        pfAlbumPassword.setOnCheckedChangeListener(this);

        pfCommentFile.setOnCheckedChangeListener(this);

        pfLikeFile.setOnCheckedChangeListener(this);

        pfMemberUploadFile.setOnCheckedChangeListener(this);

        pfAllowJoinAlbum.setOnCheckedChangeListener(this);
        pfPushNotification.setOnCheckedChangeListener(this);
        initData();
    }

    public void initData() {
        mInputAlbumName.setText(albumEntity.getName());
        mInputAlbumDescription.setText(albumEntity.getNote());
        pfPrivateAlbum.setChecked(isPrivate);
        LogUtil.d(TAG, "isPrivate-initData" + isPrivate);
        // pfAlbumPassword.setChecked(pwdChecked);
        pfAlbumPassword.setChecked(hasPassword);
        LogUtil.d(TAG, "hasPassword-initData" + hasPassword);
        pfCommentFile.setChecked(!commentOff);
        pfLikeFile.setChecked(!likeOff);
        pfAllowJoinAlbum.setChecked(!isLocked);
        int value = albumEntity.getPermissions();
        if (value == Consts.IO_PERMISSION_R) {
            uploadable = false;
        } else {
            uploadable = true;
        }
        pfMemberUploadFile.setChecked(uploadable);
        if (pushNotificationOn) {
            pfPushNotification.setChecked(true);
        } else {
            pfPushNotification.setChecked(false);
        }
        if (albumEntity.getOwner().equals(App.getUid())) {
            quitAlbum.setVisibility(View.GONE);
            reportAlbum.setVisibility(View.GONE);

        } else {
            LinearLayout action_setting = (LinearLayout) contentView.findViewById(R.id.ll_album_basis_setting);
            action_setting.setVisibility(View.GONE);
            deleteAlbum.setVisibility(View.GONE);
            transferOwner.setVisibility(View.GONE);
        }
        ConnectBuilder.checkAlbumAccessControl(albumEntity.getId());
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.CHECK_ALBUM_ACCESS_CONTROL);
        Broadcaster.registerReceiver(receiver, ift);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_back) {
            Utils.hideIME(mInputAlbumName);
            back();
        } else if (id == R.id.tv_complete) {
            onResult();
        } else if (id == R.id.et_album_password) {

        } else if (id == R.id.pf_quit_album) {
            ((AlbumDetailActivity) getActivity()).OnOptionClick(0, R.string.quit_album, null);
        } else if (id == R.id.pf_report_album) {
            ((AlbumDetailActivity) getActivity()).OnOptionClick(0, R.string.report_album, null);
        } else if (id == R.id.pf_delete_album) {
            if (Utils.isClick()) {
                return;
            }
            ((AlbumDetailActivity) getActivity()).OnOptionClick(0, R.string.delete_album, null);
        } else if (id == R.id.pf_transfer_owner) {
            ((AlbumDetailActivity) getActivity()).showTransferOwnerPopup(transfrerOwnerClickedListener);
        }
    }

    public void setAlbum(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return;
        }
        this.albumEntity = albumEntity;
        this.commentOff = albumEntity.getCommentOff();
        this.likeOff = albumEntity.getLikeOff();
        this.isPrivate = albumEntity.getIsPrivate();
        LogUtil.d(TAG, "isPrivate-setAlbum" + isPrivate);
        this.isLocked = albumEntity.getIsLocked();
        this.isPrivate = albumEntity.getIsPrivate();
        this.pushNotificationOn = !albumEntity.isNoPush();
    }

    private void onResult() {
        // is valid??
        String albumName = mInputAlbumName.getText().toString();
        String albumDescription = mInputAlbumDescription.getText().toString();
        albumDescription = albumDescription.replaceAll("\n", "");
        albumDescription = albumDescription.replaceAll("\r", "");
        // String description = mInputAlbumDescription.getText().toString();
        // // albumDescription =
        // Pattern p = Pattern.compile("\\t*|\r|\n");
        // Matcher m = p.matcher(description);
        // String albumDescription = m.replaceAll("");

        String password = mInputPassword.getText().toString();
        if (albumName == null || albumName.equals("") || albumName.trim().equals("")) {
            CToast.showToast(R.string.album_name_empty);
            return;
        }
        SensitiveWordGrepManager.WordsWrapper albumNameWrapper = new SensitiveWordGrepManager.WordsWrapper(albumName, SensitiveWordGrepManager.Type.album_name);
        SensitiveWordGrepManager.WordsWrapper albumNoteWrapper = new SensitiveWordGrepManager.WordsWrapper(albumDescription, SensitiveWordGrepManager.Type.album_note);
        if (!SensitiveWordGrepManager.getInstance().doSensitiveGrep(getActivity(), albumNameWrapper, albumNoteWrapper)) {
            return;
        }
        if (pfAlbumPassword.getChecked()) {
            LogUtil.d(TAG, "hasPassword-onResult-pfAlbumPassword" + pfAlbumPassword.getChecked());
            if (hasPassword) {
                LogUtil.d(TAG, "hasPassword-onResult-hasPassword" + hasPassword);
                if (!Utils.isEmpty(password) || !password.trim().equals("")) {// has
                    // input
                    if (password.length() < 6) {// length < 6
                        CToast.showToast(R.string.album_password_limit);
                        return;
                    }
                }
            } else {
                if ((Utils.isEmpty(password) || password.length() < 6 || password.trim().equals(""))) {
                    CToast.showToast(R.string.album_password_limit);
                    return;
                }
            }
        }
        if (password == null) {
            password = "";
        }
        JSONObject jo = new JSONObject();
        jo.put(Consts.NAME, albumName);
        jo.put(Consts.NOTE, albumDescription);
        if (!(hasPassword && pfAlbumPassword.getChecked() && Utils.isEmpty(password))) {
            LogUtil.d(TAG, "hasPassword-pfAlbumPassword.getChecked()" + hasPassword + pfAlbumPassword.getChecked());
            jo.put(Consts.ACCESS_PASSWORD, password);
            LogUtil.d(TAG, "password" + password);
        }
        jo.put(Consts.IS_PRIVATE, isPrivate);
        LogUtil.d(TAG, "isPrivate-onResult" + isPrivate);
        jo.put(Consts.IS_LOCKED, isLocked);
        jo.put(Consts.COMMENT_OFF, commentOff);
        jo.put(Consts.LIKE_OFF, likeOff);

        if (uploadable != pfMemberUploadFile.getChecked()) {
            if (pfMemberUploadFile.getChecked()) {
                jo.put(Consts.PERMISSIONS, new String[]{"read", "write"});
            } else {
                jo.put(Consts.PERMISSIONS, new String[]{"read"});
            }
        }

        ConnectBuilder.updateAlbum(jo.toString(), albumEntity.getId());
        ConnectBuilder.noPush(albumEntity.getId(), !pushNotificationOn);
        albumEntity.setNoPush(!pushNotificationOn);
        back();
        Utils.hideIME(mInputAlbumName);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onResult CREATE_ALBUM_RESULT:" + jo);
        }
        onAnalytic();
    }

    @Override
    public void onCheckedChanged(View v, boolean isChecked) {
        int id = v.getId();
        if (id == R.id.pf_private_album) {
            isPrivate = isChecked;
            LogUtil.d(TAG, "isPrivate-onCheckedChanged" + isPrivate);
        } else if (id == R.id.pf_senior_management) {
            if (isChecked) {
                llPermissionSettings.setVisibility(View.VISIBLE);
            } else {
                llPermissionSettings.setVisibility(View.GONE);
            }
        } else if (id == R.id.pf_album_password) {
            // pwdChecked = isChecked;
            LogUtil.d(TAG, "hasPassword-onCheckedChanged" + pfAlbumPassword.getChecked());
            if (isChecked) {
                mInputPassword.setVisibility(View.VISIBLE);
                float marginLeft = getResources().getDimension(R.dimen.preference_margin_rl);
                pfAlbumPassword.setBottomMarginLeft(marginLeft);
            } else {
                mInputPassword.setVisibility(View.GONE);
                float marginLeft = getResources().getDimension(R.dimen.preference_margin);
                pfAlbumPassword.setBottomMarginLeft(marginLeft);
            }
        } else if (id == R.id.pf_comment_file) {
            commentOff = !isChecked;
        } else if (id == R.id.pf_like_file) {
            likeOff = !isChecked;
        } else if (id == R.id.pf_allow_join_album) {
            isLocked = !isChecked;
        } else if (id == R.id.pf_push_notification) {
            pushNotificationOn = isChecked;
        }
    }

    private void onAnalytic() {
        if (albumEntity == null) {
            return;
        }
        if (!albumEntity.getName().equals(mInputAlbumName.getText().toString())) {
            UMutils.instance().diyEvent(ID.EventRenameAlbum);
        }

        if (!isPrivate && pfPrivateAlbum.getChecked()) {
            UMutils.instance().diyEvent(ID.EventTurnOnAlbumPrivacy);
        }

        if (hasPassword != pfAlbumPassword.getChecked()) {
            if (hasPassword) {
                UMutils.instance().diyEvent(ID.EventTurnOffAlbumPassword);
            } else {
                UMutils.instance().diyEvent(ID.EventTurnOnAlbumPassword);
            }
        } else if (Utils.isEmpty(mInputPassword.getText().toString())) {
            UMutils.instance().diyEvent(ID.EventResetAlbumPassword);
        }

        if (commentOff != pfCommentFile.getChecked()) {
            if (!commentOff) {
                UMutils.instance().diyEvent(ID.EventTurnOffAlbumComment);
            }
        }

        if (likeOff != pfLikeFile.getChecked()) {
            if (!likeOff) {
                UMutils.instance().diyEvent(ID.EventTurnOffAlbumLike);
            }
        }

        if (uploadable != pfMemberUploadFile.getChecked()) {
            if (uploadable) {
                UMutils.instance().diyEvent(ID.EventTurnOffAlbumMemberUpload);
            }
        }

        if (isLocked != pfAllowJoinAlbum.getChecked()) {
            if (!isLocked) {
                UMutils.instance().diyEvent(ID.EventTurnOffAlbumJoinMember);
            }
        }

    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

}
