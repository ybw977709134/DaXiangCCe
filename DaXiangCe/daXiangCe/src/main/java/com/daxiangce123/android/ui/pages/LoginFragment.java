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
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.UserSuspendedInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.activities.LoginActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.outh.Oauth;
import com.daxiangce123.android.util.outh.OauthHelper;
import com.daxiangce123.android.util.outh.OauthHelper.onOauthListener;
import com.daxiangce123.android.util.outh.QQHelper;
import com.daxiangce123.android.util.outh.WBHelper;
import com.daxiangce123.android.util.outh.WXHelper;

/**
 * mobile login and password recovery
 */
public class LoginFragment extends BaseFragment implements OnClickListener {
    private String TAG = "LoginFragment";

    private OauthHelper oauthHelper;

    private View mRootView = null;
    private boolean isOpening = false;
    // private String status;
    private String inviteCode;
    private AlbumEntity albumEntity;
    private TextView mRegister;
    private TextView mLogin;


    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                // if (!intent.hasExtra(Consts.RESPONSE)) {
                // return;
                // }

                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (!isVisible()) {
                    return;
                }

                if (response != null) {

                    LogUtil.d(TAG, "action:" + action + "	response:" + response);
//                    if (response.getStatusCode() != 200 && response.getStatusCode() != 401) {
//                        onFailed(response);
//                        return;
//                    }
                    if (Consts.SSO_BIND.equals(action)) {
                        if (response.getStatusCode() != 200) {
                            onFailed(response);
                            return;
                        }
//                        if (response.getStatusCode() != 200) {
//                            return;
//                        }
                        initToken(response.getContent());
                        ConnectBuilder.getMineInfo();
                    } else if (Consts.GET_USER_SUSPENDED_INFO.equals(action)) {
                        if (response.getStatusCode() != 200) {
                            return;
                        }
                        onGetUserSuspend(response, info);
                    }
                } else {
                    if (Consts.LOGIN_WX_SUCCEED.equals(action)) {
                        onNewIntent(intent);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private void onGetUserSuspend(Response response, ConnectInfo info) {
        LoadingDialog.dismiss();
        String content = response.getContent();
        UserSuspendedInfo userSuspendedInfo = Parser.parseUserSupended(content);
        if (userSuspendedInfo == null) {
            return;
        }
        if (!userSuspendedInfo.getUserId().equals(info.getTag())) {
            return;
        }

        if (userSuspendedInfo.getStatus().equals(Consts.DISABLED_PERMANENTLY)) {
            DialogUtils.dialog(R.string.disabled_permanently);
        }

        if (userSuspendedInfo.getStatus().equals(Consts.DISABLED_TEMPORARILY)) {
            DialogUtils.dialog(R.string.disabled_temporarily);
        }

    }

    private onOauthListener oauthListener = new onOauthListener() {
        @Override
        public void onOauthSucceed(Oauth oauth) {
            if (App.DEBUG) {
                LogUtil.v(TAG, "onOauthSucceed");
            }
            if (!isVisible()) {
                return;
            }
            JSONObject jo = new JSONObject();
            jo.put(Consts.SSO_PROVIDER, oauth.getType());
            jo.put(Consts.TOKEN, oauth.getToken());
            jo.put(Consts.DEVICE, App.mobileInfo.BRAND + "-" + App.mobileInfo.PRODUCT);
            jo.put(Consts.UNIQUE_ID, oauth.getUid());
            jo.put(Consts.OS, "android");
            if (oauth.getUnion_id() != null) {
                jo.put(Consts.UNION_ID, oauth.getUnion_id());
            }
            ConnectBuilder.sso_bind(jo.toString());
            LoadingDialog.show(R.string.logining);
        }

        @Override
        public void onOauthFailed(String provider, Object object) {
            if (!isVisible()) {
                return;
            }
            if (App.DEBUG) {
                LogUtil.d(TAG, "onOauthFailed: " + provider + "	object " + object);
            }
            LoadingDialog.dismiss();
            String msg = getString(R.string.fail_to_get_token_from_x, provider);
            CToast.showToast(msg);
        }
    };

    @Override
    public String getFragmentName() {
        return "LoginFragment";
    }

    public LoginFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    @Override
    public void onNewIntent(Intent intent) {
        if (App.DEBUG) {
            LogUtil.v(TAG, "onNewIntent" + oauthHelper.getClass());
        }
        super.onNewIntent(intent);
        if (oauthHelper instanceof WXHelper) {
            ((WXHelper) oauthHelper).handleIntent(intent);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            // mRootView = inflater.inflate(R.layout.login_fragment, container,
            // false);
            mRootView = inflater.inflate(R.layout.fragment_login, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initCompontent();
        initBroad();
        return mRootView;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public void setAlbum(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    private void initCompontent() {

        mRootView.findViewById(R.id.weibo).setOnClickListener(this);
        mRootView.findViewById(R.id.qq).setOnClickListener(this);
        mRootView.findViewById(R.id.wx).setOnClickListener(this);
        mRootView.findViewById(R.id.close_login).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_agreement).setOnClickListener(this);
        mRootView.findViewById(R.id.tv_privacy).setOnClickListener(this);
        // mFaceBook = (Button) mRootView.findViewById(R.id.facebook);
        // mTwitter = (Button) mRootView.findViewById(R.id.twitter);

        // mFaceBook.setOnClickListener(this);
        // mTwitter.setOnClickListener(this);
        mRegister = (TextView) mRootView.findViewById(R.id.tv_register);
        mLogin = (TextView) mRootView.findViewById(R.id.tv_login);

        mRegister.setOnClickListener(this);
        mLogin.setOnClickListener(this);

    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.SSO_BIND);
        ift.addAction(Consts.GET_MIME_INFO);
        ift.addAction(Consts.LOGIN_WX_SUCCEED);
        ift.addAction(Consts.GET_USER_SUSPENDED_INFO);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void onFailed(Response response) {
        LoadingDialog.dismiss();
        AppData.clear();
        if (response == null) {
            CToast.showToast(R.string.login_failed);
            return;
        }
//        if (response.getStatusCode() == 401 && response.getError().toErrorCode() == ErrorCode.ACCOUNT_SUSPENDED) {
////            DialogUtils.dialog(R.string.account_suspended);
//        }
        if (response.getStatusCode() == 401) {
            return;
        } else if (response.getErrCode() == ErrorCode.NOT_FOUND) {
            CToast.showToast(R.string.sns_account_invalid);
        } else if (response.getErrCode() == ErrorCode.NETWORK_ERROR) {
            CToast.showToast(R.string.network_error);
        } else {
            CToast.showToast(R.string.unknown_error);
        }
    }

    private boolean initToken(String session) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "isJSONObject \n" + session);
        }
        if (!JSONUtil.isJSONObject(session)) {
            return false;
        }
        JSONObject jo = JSONObject.parseObject(session);
        String token = jo.getString(Consts.ACCESS_TOKEN);
        String status = jo.getString(Consts.STATUS);
        AppData.setToken(token);
        if (App.DEBUG) {
            LogUtil.d(TAG, "  token  " + token);
        }
        AppData.setStatus(status);
        ConnectBuilder.init();
        return true;
    }


    @Override
    public void onClick(View v) {
        oauthHelper = null;
        int id = v.getId();
        if (id == R.id.weibo) {
            oauthHelper = new WBHelper();
            UMutils.instance().diyEvent(UMutils.ID.EventSignUpViaWeibo);
        } else if (id == R.id.qq) {
            if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mobileqq")) {
                CToast.showToast(R.string.not_install_qq);
            } else {
                oauthHelper = new QQHelper();
            }
            UMutils.instance().diyEvent(UMutils.ID.EventSignUpViaQQ);
        } else if (id == R.id.wx) {
            if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mm")) {
                CToast.showToast(R.string.not_install_wx);
            } else {
                oauthHelper = new WXHelper();
            }
            UMutils.instance().diyEvent(UMutils.ID.EventSignUpViaWechat);
        } else if (id == R.id.close_login) {
            // back();
            oauthHelper = null;
            this.getActivity().finish();
        } else if (id == R.id.tv_register) {
            IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
            bundle.setRegisterType(IRegisterController.RegisterType.register);
            ((IRegisterController) getActivity()).showRegister(bundle);
            UMutils.instance().diyEvent(UMutils.ID.EventSignUpViaMobile);
        } else if (id == R.id.tv_login) {
            ((LoginActivity) getActivity()).showSignin(null);
            UMutils.instance().diyEvent(UMutils.ID.EventSignInViaMobile);
        } else if (id == R.id.tv_agreement) {
            showAgreement();
        } else if (id == R.id.tv_privacy) {
            showPrivacy();
        }
        if (oauthHelper != null) {
            oauthHelper.setOauthListener(oauthListener);
            oauthHelper.oauth();
        }
    }

    private void showAgreement() {
        BrowserFragment browserFragment = new BrowserFragment();
        browserFragment.setHomeUrl(Consts.URL_AGREE);
        browserFragment.show(getBaseActivity());
    }

    private void showPrivacy() {
        BrowserFragment browserFragment = new BrowserFragment();
        browserFragment.setHomeUrl(Consts.URL_PRIVACY);
        browserFragment.show(getBaseActivity());
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        LogUtil.d(TAG, " onActivityResult data = " + data + " oauthHelper=" + oauthHelper);
        if (oauthHelper != null) {
            oauthHelper.onActivityResult(requestCode, resultCode, data);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onBackPressed() {
        oauthHelper = null;
        this.getActivity().finish();
        return super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        if (oauthHelper != null) {
            oauthHelper.setOauthListener(null);
        }
        isOpening = false;
        super.onDestroy();
    }

}
