package com.daxiangce123.android.ui.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.LoginFragment;
import com.daxiangce123.android.ui.pages.MobileLoginFragment;
import com.daxiangce123.android.ui.pages.RegisterFragment;
import com.daxiangce123.android.ui.pages.SetPasswordFragment;
import com.daxiangce123.android.ui.pages.SubmitVerficationCodeFragment;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;

import java.util.ArrayList;

public class LoginActivity extends BaseCliqActivity implements IRegisterController {
    private LoginFragment loginFragment;
    private String inviteCode;
    private AlbumEntity albumEntity;
    private boolean isOpening;
    private String status;
    private long uniqId = 0L;
    public static final String TAG = "LoginActivity";

    RegisterFragment registerFragment = null;
    MobileLoginFragment mobileLoginFragment = null;
    SubmitVerficationCodeFragment submitVerficationCodeFragment = null;
    SetPasswordFragment setPasswordFragment = null;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                if(App.DEBUG){
                    LogUtil.d(TAG, " connectInfo " + connectInfo  + " entity "  + connectInfo.getEntity() );
                }
                if (response != null) {
                    LogUtil.d(TAG, "action:" + action + "	response:" + response);
                    if (Consts.MOBILE_LOGIN.equals(action)) {
                        initToken(response);
                    } else if (Consts.GET_MIME_INFO.equals(action)) {
                        onGetMineInfo(response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    @Override
    public void showSignin(RegisterBundle bundle) {
        mobileLoginFragment = null;
        if (mobileLoginFragment == null) {
            mobileLoginFragment = new MobileLoginFragment();
        }
        mobileLoginFragment.setRegisterBundle(bundle);
        mobileLoginFragment.show(this);
        // UMutils.instance().diyEvent(ID.EventSignin);
    }

    @Override
    public void showRegister(RegisterBundle bundle) {
        registerFragment = null;
        if (registerFragment == null) {
            registerFragment = new RegisterFragment();
        }
        registerFragment.setRegisterBundle(bundle);
        registerFragment.show(this);
        // UMutils.instance().diyEvent(ID.EventRegister);
    }

    @Override
    public void showConfirmationReceiver(RegisterBundle bundle) {
        submitVerficationCodeFragment = null;
        if (submitVerficationCodeFragment == null) {
            submitVerficationCodeFragment = new SubmitVerficationCodeFragment();
        }
        submitVerficationCodeFragment.setRegisterBundle(bundle);
        submitVerficationCodeFragment.show(this);
    }

    @Override
    public void showSubmiter(RegisterBundle bundle) {

        if (setPasswordFragment == null) {
            setPasswordFragment = new SetPasswordFragment();
        }
        setPasswordFragment.setRegisterBundle(bundle);
        setPasswordFragment.show(this);
    }

    @Override
    public void mobileLogin(RegisterBundle bundle) {
        LoadingDialog.show(R.string.longining);
        if (bundle.registerType == RegisterType.register) {
            status = Consts.STATUS_CREATE;
        } else {
            status = Consts.STATUS_EXISTED;
        }
        uniqId = System.currentTimeMillis();
        ConnectBuilder.mobileUserLogine(bundle.mobile, bundle.password, uniqId);
        UMutils.instance().diyEvent(UMutils.ID.EventSignInViaMobile);
    }

    private void onGetMineInfo(Response response) {
        if (response.getStatusCode() != 200) {
            return;
        }
        LoadingDialog.dismiss();
        UserInfo info = Parser.parseUserInfo(response.getContent());
        if (info != null) {
            App.setUserInfo(info);
            AppData.setUid(info.getId());
            if (AppData.getStatus().equals(Consts.STATUS_EXISTED)) {
                UMutils.instance().diyEvent(UMutils.ID.EventSignInSuccess);
            } else if (AppData.getStatus().equals(Consts.STATUS_CREATE)) {
                UMutils.instance().diyEvent(UMutils.ID.EventSignUpSuccess);
            }
            onLogin();
        }
    }

    private void onLogin() {
        if (isOpening) {
            return;
        }
        ArrayList<BaseCliqActivity> list = App.getActivityList();
        if (list != null) {
            for (BaseCliqActivity a : list) {
                if (a != this) {
                    a.finish();
                }
            }
        }
        isOpening = true;
        Intent start = new Intent();
        start.putExtra(Consts.INVITE_CODE, inviteCode);
        start.putExtra(Consts.ALBUM, albumEntity);
        start.setClass(this, HomeActivity.class);
        AppUtil.startActivity(start, true);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        initData();
        initBroad();
        showViewer();
    }

    private void initData() {
        try {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null && bundle.containsKey(Consts.INVITE_CODE)) {
                inviteCode = bundle.getString(Consts.INVITE_CODE);
            }
            if (bundle != null && bundle.containsKey(Consts.ALBUM)) {
                albumEntity = bundle.getParcelable(Consts.ALBUM);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showViewer() {

        if (loginFragment == null) {
            loginFragment = new LoginFragment();
        }
        loginFragment.setInviteCode(inviteCode);
        loginFragment.setAlbum(albumEntity);
        loginFragment.show(this);
    }

    @Override
    protected void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        isOpening = false;
        super.onDestroy();
    }


    private boolean initToken(Response response) {
        if (response.getStatusCode() != 200) {
            return false;
        }
        String session = response.getContent();
        if (App.DEBUG) {
            LogUtil.d(TAG, "isJSONObject \n" + session);
        }
        if (!JSONUtil.isJSONObject(session)) {
            return false;
        }
        JSONObject jo = JSONObject.parseObject(session);
        String token = jo.getString(Consts.ACCESS_TOKEN);
        String status = jo.getString(Consts.STATUS);
        if (status == null) {
            status = this.status;
        }
        AppData.setToken(token);
        if (App.DEBUG) {
            LogUtil.d(TAG, "  token  " + token);
        }
        AppData.setStatus(status);
        ConnectBuilder.init();
        ConnectBuilder.getMineInfo();
        return true;
    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.MOBILE_LOGIN);
        ift.addAction(Consts.GET_MIME_INFO);
        Broadcaster.registerReceiver(receiver, ift);
    }


}
