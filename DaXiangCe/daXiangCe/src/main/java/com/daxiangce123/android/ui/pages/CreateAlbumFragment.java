package com.daxiangce123.android.ui.pages;

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
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.Preference;
import com.daxiangce123.android.ui.view.Preference.CheckedChangedListener;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class CreateAlbumFragment extends BaseFragment implements OnClickListener, CheckedChangedListener {

    private static final String TAG = "CreateAlbumActivity";
    private View contentView;
    private TextView mTitle;
    private TextView mBack;
    private TextView mComplete;
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
    private LinearLayout llPermissionSettings;
    private boolean uploadable = true;
    private boolean isPrivate = false;
    private boolean isLocked = false;
    private boolean commentOff = false;
    private boolean likeOff = false;

    @Override
    public String getFragmentName() {
        return "CreateAlbumFragment";
    }

    public CreateAlbumFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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
        mTitle.setText(R.string.new_ablum);
        mBack = (TextView) contentView.findViewById(R.id.tv_back);
        mComplete = (TextView) contentView.findViewById(R.id.tv_complete);
        mComplete.setText(R.string.complete);
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

        mBack.setOnClickListener(this);
        mComplete.setOnClickListener(this);
        mInputAlbumName.setOnClickListener(this);
        mInputAlbumDescription.setOnClickListener(this);

        pfPrivateAlbum.setOnCheckedChangeListener(this);

        pfSeniorManagement.setOnCheckedChangeListener(this);

        pfAlbumPassword.setOnClickListener(this);
        pfAlbumPassword.setOnCheckedChangeListener(this);

        pfCommentFile.setOnCheckedChangeListener(this);
        pfCommentFile.setChecked(!commentOff);

        pfLikeFile.setOnCheckedChangeListener(this);
        pfLikeFile.setChecked(!likeOff);

        pfMemberUploadFile.setOnCheckedChangeListener(this);
        pfMemberUploadFile.setChecked(uploadable);

        pfAllowJoinAlbum.setOnCheckedChangeListener(this);
        pfAllowJoinAlbum.setChecked(!isLocked);
        LinearLayout action_setting = (LinearLayout) contentView.findViewById(R.id.ll_album_action_setting);
        action_setting.setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_back) {
            Utils.hideIME(mInputAlbumName);
//            back();
            onBackPressed();
        } else if (id == R.id.tv_complete) {
            onResult();
        } else if (id == R.id.et_album_password) {

        }
    }

    private boolean onResult() {
        // is valid??
        String albumName = mInputAlbumName.getText().toString();
        String albumDescription = mInputAlbumDescription.getText().toString();
        albumDescription = albumDescription.replaceAll("\n", "");
        albumDescription = albumDescription.replaceAll("\r", "");
        // albumDescription =
        // Pattern p = Pattern.compile("\\t*|\r|\n");
        // Matcher m = p.matcher(description);
        // String albumDescription = m.replaceAll("");
        // String password ;
        String password = mInputPassword.getText().toString();
        SensitiveWordGrepManager.WordsWrapper albumNameWrapper = new SensitiveWordGrepManager.WordsWrapper(albumName, SensitiveWordGrepManager.Type.album_name);
        SensitiveWordGrepManager.WordsWrapper albumNoteWrapper = new SensitiveWordGrepManager.WordsWrapper(albumDescription, SensitiveWordGrepManager.Type.album_note);
        if (!SensitiveWordGrepManager.getInstance().doSensitiveGrep(getActivity(), albumNameWrapper, albumNoteWrapper)) {
            return false;
        }


        if (albumName == null || albumName.equals("") || albumName.trim().equals("")) {
            CToast.showToast(R.string.album_name_empty);
        } else if (pfAlbumPassword.getChecked() && (Utils.isEmpty(password) || password.length() < 6 || password.trim().equals(""))) {
            CToast.showToast(R.string.album_password_limit);
        } else {
            String[] permissions = new String[]{"read"};
            if (uploadable) {
                permissions = new String[]{"read", "write"};
            }
            JSONObject jo = new JSONObject();
            jo.put(Consts.NAME, albumName);
            jo.put(Consts.NOTE, albumDescription);
            jo.put(Consts.ACCESS_PASSWORD, password);
            jo.put(Consts.IS_PRIVATE, isPrivate);
            jo.put(Consts.IS_LOCKED, isLocked);
            jo.put(Consts.COMMENT_OFF, commentOff);
            jo.put(Consts.LIKE_OFF, likeOff);
            jo.put(Consts.PERMISSIONS, permissions);
            ConnectBuilder.createAlbum(jo.toString());
            onAnalytic();
//            back();
            onBackPressed();
            Utils.hideIME(mInputAlbumName);
            if (App.DEBUG) {
                LogUtil.d(TAG, "onResult CREATE_ALBUM_RESULT:" + jo);
            }
        }

        return true;
    }

    private void onAnalytic() {
        if (isPrivate) {
            UMutils.instance().diyEvent(ID.EventTurnOnNewAlbumPrivacy);
        }

        if (mInputPassword.getText() != null && mInputPassword.getText().toString().length() >= 6) {
            UMutils.instance().diyEvent(ID.EventTurnOnNewAlbumPassword);
        }

        if (llPermissionSettings.getVisibility() == View.VISIBLE) {
            UMutils.instance().diyEvent(ID.EventTurnOnNewAlbumAdvance);

            if (commentOff) UMutils.instance().diyEvent(ID.EventTurnOffNewAlbumComment);
            if (likeOff) UMutils.instance().diyEvent(ID.EventTurnOffNewAlbumLike);
            if (!uploadable) UMutils.instance().diyEvent(ID.EventTurnOffNewAlbumMemberUpload);
            if (isLocked) UMutils.instance().diyEvent(ID.EventTurnOffAlbumMemberJoin);
        }
    }

    @Override
    public void onCheckedChanged(View v, boolean isChecked) {
        int id = v.getId();
        if (id == R.id.pf_private_album) {
            isPrivate = isChecked;
        } else if (id == R.id.pf_senior_management) {
            if (isChecked) {
                llPermissionSettings.setVisibility(View.VISIBLE);
            } else {
                llPermissionSettings.setVisibility(View.GONE);
            }
        } else if (id == R.id.pf_album_password) {
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
        } else if (id == R.id.pf_member_upload_file) {
            uploadable = isChecked;
        } else if (id == R.id.pf_allow_join_album) {
            isLocked = !isChecked;
        }
    }

    @Override
    public boolean onBackPressed() {
        ((HomeActivity) getActivity()).showAlbum();
        return true;
    }

}
