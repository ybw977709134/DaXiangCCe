package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.ZXingActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.List;

public class JoinAlbumFragment extends BaseFragment implements OnClickListener {
    private static final String TAG = "JoinAlbumFragment";
    private View contentView;
    private EditText etInputCode;
    private Button btJoinAlbum;
    private ImageView scanAlbumCode;
    private List<AlbumEntity> mAlumList;
    private TextView tvBack;
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                if (Consts.JOIN_ALBUM.equals(action)) {
                    if (response.getStatusCode() == 200 || response.getStatusCode() == 304) {
                        onBackPressed();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_join_album, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        setBoottomBarVisibility(View.GONE);
        View bottomBar = getActivity().findViewById(R.id.bottom_bar);
        if (bottomBar != null) {
            bottomBar.setVisibility(View.GONE);
        }
        initBroadcast();
        return contentView;
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.JOIN_ALBUM);
        // ift.addAction(Consts.SET_ALBUM_THUM);

        // ift.addAction(Consts.GET_ALBUM_SAMPLES);
        Broadcaster.registerReceiver(receiver, ift);
    }

    public JoinAlbumFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    private void initComponent() {
        etInputCode = (EditText) contentView.findViewById(R.id.input_invite_code);
        btJoinAlbum = (Button) contentView.findViewById(R.id.bt_join_album);
        scanAlbumCode = (ImageView) contentView.findViewById(R.id.iv_scan_album_code);
        tvBack = (TextView) contentView.findViewById(R.id.tv_back);
        isInviteCodeValid();
        etInputCode.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                LogUtil.d(TAG, "etInputCode: +" + etInputCode.getText());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

                LogUtil.d(TAG, "etInputCode: --" + etInputCode.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                isInviteCodeValid();
                LogUtil.d(TAG, "etInputCode: " + etInputCode.getText());
            }
        });
        btJoinAlbum.setOnClickListener(this);
        scanAlbumCode.setOnClickListener(this);
        tvBack.setOnClickListener(this);
    }

    private void isInviteCodeValid() {
        Editable inviteCode = etInputCode.getText();
        if (!Utils.isEmpty(inviteCode)) {
            btJoinAlbum.setEnabled(true);
        } else {
            btJoinAlbum.setEnabled(false);
        }
    }

    public void setAlbumList(List<AlbumEntity> mAlumList) {
        this.mAlumList = mAlumList;
    }

    @Override
    public String getFragmentName() {
        return "JoinAlbumFragment";
    }

    @Override
    public void onShown() {
        if (isVisible()) {
            etInputCode.setInputType(InputType.TYPE_CLASS_NUMBER);
            Utils.showIME();
            etInputCode.requestFocus();
            initBroadcast();
        }
    }

    ;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_join_album) {
            joinAlbum();
        } else if (id == R.id.iv_scan_album_code) {
            Utils.hideIME(etInputCode);
            UMutils.instance().diyEvent(ID.EventJoinAlbumByQrCode);
            Intent intent = new Intent();
            intent.setClass(JoinAlbumFragment.this.getActivity(), ZXingActivity.class);
            startActivityForResult(intent, Consts.REQUEST_CODE_ZXING);
        } else if (id == R.id.tv_back) {
            onBackPressed();
        }
    }

    @Override
    public void back() {
        Utils.hideIME(etInputCode);
        super.back();
    }

    ;

    private void joinAlbum() {
        String inviteCode = etInputCode.getText().toString();
        if (Utils.isEmpty(inviteCode)) {
            CToast.showToast(R.string.havet_input);
        } else {
            if (!isInviteCodeExist(inviteCode)) {
                JSONObject jo = new JSONObject();
                jo.put(Consts.USER_ID, App.getUid());
                ConnectBuilder.joinAlbum(inviteCode, jo.toJSONString());
            } else {
                CToast.showToast(R.string.album_exists);
            }
//            onBackPressed();
            UMutils.instance().diyEvent(ID.EventJoinAlbumByInviteCode);
        }
    }

    private boolean isInviteCodeExist(String inviteCode) {
        if (Utils.isEmpty(inviteCode)) {
            return false;
        }
        if (mAlumList == null) {
            return false;
        }
        try {

            for (AlbumEntity info : mAlumList) {
                if (info == null) {
                    continue;
                }
                if (inviteCode.equals(info.getInviteCode())) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean onBackPressed() {
        Utils.hideIME(etInputCode);
        ((HomeActivity) getActivity()).showAlbum();
        return true;
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
