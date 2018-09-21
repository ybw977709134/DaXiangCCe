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
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.Preference;
import com.daxiangce123.android.ui.view.Preference.CheckedChangedListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.UMutils.ID;

public class MemberPermissionSetFragment extends BaseFragment implements
        CheckedChangedListener, OnClickListener {

    private static final String TAG = "MemberPermissionSetFragment";
    private View contentView;
    private Preference pfPermissionSet;
    private MemberEntity memberEntity;
    private boolean uploadable;
    private TextView mComplete;
    private TextView mBack;
    private boolean DEBUG = true;
//    private boolean nonMember = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                String content = response.getContent();
                if (action.equals(Consts.GET_MEMBER_ROLE)) {
                    memberEntity = Parser.parseMember(content);
                    updatePermission();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public MemberPermissionSetFragment() {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    @Override
    public String getFragmentName() {
        return "MemberPermissionSetFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        initBroad();
        if (contentView == null) {
            contentView = inflater.inflate(
                    R.layout.fragment_member_permission_set, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        return contentView;
    }

    private void initComponent() {
        pfPermissionSet = (Preference) contentView
                .findViewById(R.id.pf_allow_member_upload_file);
        pfPermissionSet.setOnCheckedChangeListener(this);
        mComplete = (TextView) contentView.findViewById(R.id.tv_complete);
        mComplete.setOnClickListener(this);
        mBack = (TextView) contentView.findViewById(R.id.tv_back);
        mBack.setOnClickListener(this);
        updatePermission();
    }

    private void updatePermission() {
        if (memberEntity != null
                && memberEntity.getPermissions() == Consts.IO_PERMISSION_R) {
            uploadable = false;
            pfPermissionSet.setChecked(false);
        } else {
            uploadable = true;
            pfPermissionSet.setChecked(true);
        }
    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.GET_MEMBER_ROLE);
        Broadcaster.registerReceiver(receiver, ift);
    }

    public void setMember(MemberEntity memberEntity) {
        if (DEBUG) {
            LogUtil.d(TAG, "");
        }
        this.memberEntity = memberEntity;
    }

//    public void isAlbumMember(boolean nonMember) {
//        this.nonMember = nonMember;
//    }

    @Override
    public void onCheckedChanged(View v, boolean isChecked) {

    }

    @Override
    public void onClick(View v) {
        if (v.equals(mComplete)) {
            UMutils.instance().diyEvent(ID.EventMemberPermissionSet);

            if (memberEntity == null) {
//                if (nonMember) {
//                    CToast.showToast("已经不是相册成员，不能设置权限！");

//                }
                back();
                return;
            }
            if (pfPermissionSet.getChecked() == uploadable) {
                back();
                return;
            }
            String[] permissions = new String[]{"read"};
            if (pfPermissionSet.getChecked()) {
                permissions = new String[]{"read", "write"};
            }
            JSONObject jo = new JSONObject();
            jo.put(Consts.USER_ID, memberEntity.getUserId());
            jo.put(Consts.ALBUM_ID, memberEntity.getAlbumId());
            jo.put(Consts.ROLE, Consts.MEMBER);
            jo.put(Consts.PERMISSIONS, permissions);
            ConnectBuilder.setMemberRole(jo.toString());
            back();
        } else if (v.equals(mBack)) {
            back();
        }
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

}
