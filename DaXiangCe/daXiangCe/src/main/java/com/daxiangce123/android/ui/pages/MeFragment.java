package com.daxiangce123.android.ui.pages;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputFilter.LengthFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.applock.core.LockManager;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.Binding;
import com.daxiangce123.android.data.Bindings;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.manager.VideoManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.activities.LocalImageActivity;
import com.daxiangce123.android.ui.activities.LoginActivity;
import com.daxiangce123.android.ui.activities.PwdSetupActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CDialog;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.view.OptionDialog;
import com.daxiangce123.android.ui.view.Preference;
import com.daxiangce123.android.ui.view.Preference.CheckedChangedListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.UnbindDevice;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.outh.Oauth;
import com.daxiangce123.android.util.outh.OauthHelper;
import com.daxiangce123.android.util.outh.QQHelper;
import com.daxiangce123.android.util.outh.WBHelper;
import com.daxiangce123.android.util.outh.WXHelper;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MeFragment extends BaseFragment implements OnClickListener, OptionListener, CheckedChangedListener {
    private static final String TAG = "MeFragment";
    private View mRootView = null;
    private Preference pfName;
    private Preference pfAvatar;
    private Preference pfFeedback;
    private Preference pfAboutUs;
    private Preference pfPwdLock;
    private Preference pfRecommendedApps;
    private Preference pfBullet;
    // private Preference pfClearCache;
    private LinearLayout rlClearCache, llAccountInfo;
    private Preference pfMobile, pfACCountPassword, pfWechat, pfWeibo, pfQQ;
    private TextView tvCacheSize;
    private Button logout;
    private UserInfo info;
    private CDialog avatarDialog;
    // private CDialog feedbackDialog;
    private FragmentAboutUs fragmentAboutUs;

    // private PWDSetUpFragment pwdSetupFragment;
    private OptionDialog optionDialog;
    private LinearLayout mUserInfo;
    private LinearLayout mNotLogin;
    private ImageView addFriend;
    private ArrayList<Binding> bindings;

    private String cacheSize = "0KB";
    private OauthHelper oauthHelper;
//    private List<String> list;

    private OauthHelper.onOauthListener oauthListener = new OauthHelper.onOauthListener() {
        @Override
        public void onOauthSucceed(Oauth oauth) {
            if (App.DEBUG) {
                LogUtil.e(TAG, "sso onOauthSucceed!!");
            }
            LoadingDialog.show(R.string.loading);
            if (!isVisible()) {
                return;
            }
            JSONObject jo = new JSONObject();
            jo.put(Consts.SSO_PROVIDER, oauth.getType());
            jo.put(Consts.TOKEN, oauth.getToken());
            jo.put(Consts.DEVICE, App.mobileInfo.BRAND + "-" + App.mobileInfo.PRODUCT);
            jo.put(Consts.UNIQUE_ID, oauth.getUid());
            if (oauth.getUnion_id() != null) {
                jo.put(Consts.UNION_ID, oauth.getUnion_id());
            }
            jo.put(Consts.OS, "android");
            ConnectBuilder.sso_bind(jo.toString(), oauth.getType());
            oauthHelper = null;
        }

        @Override
        public void onOauthFailed(String provider, Object object) {
            if (!isVisible()) {
                return;
            }
            if (App.DEBUG) {
                LogUtil.d(TAG, "onOauthFailed: " + provider + "	object " + object);
            }
            String msg = getString(R.string.fail_to_get_access_token_from_x, provider);
            CToast.showToast(msg);
            oauthHelper = null;
        }
    };
    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (App.DEBUG) {
                    LogUtil.e(TAG, "onReceive+" + action);
                }
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
                if (Consts.LOGIN_WX_SUCCEED.equals(action)) {
                    onNewIntent(intent);
                } else if (Consts.UNBIND_SSO.equals(action)) {
                    handleSSOUnbind(response, connectInfo);
                } else if (Consts.SSO_BIND.equals(action)) {
                    handleSSOBind(response, connectInfo);
                } else if (Consts.LIST_BINDINGS.equals(action)) {
                    if (response != null && connectInfo.getTag().equals(App.getUid())) {
                        onListBindings(response);
                    }
                } else if (Consts.UNBIND_DEVICE.equals(action)) {
                    UnbindDevice.unbindDevice(true);
                    getActivity().finish();
                } else if (Consts.UPDATE_MIME_INFO.equals(action)) {
                    if (response.getStatusCode() == 200) {
                        UMutils.instance().diyEvent(ID.EventRenamedSuccess);
                        JSONObject jo = JSONObject.parseObject(connectInfo.getTag());
                        String name = jo.getString(Consts.NAME);
                        info.setName(name);
                        updateName();
                    } else {
                        CToast.showToast(R.string.request_failed);
                    }
                } else if (Consts.SET_AVATAR.equals(action)) {
                    if (response.getStatusCode() != 200) {
                        return;
                    }
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "connectInfo=" + connectInfo + "\n response=" + response);
                    }
                    UMutils.instance().diyEvent(ID.EventChangedAvatarSuccess);
                    ImageManager.instance().remove(App.getUid());
                    showIconAvatar(false);
                } else if (Consts.GET_RECOMMEND_APP_STATUS.equals(action)) {
                    if (response.getStatusCode() != 200) {
                        return;
                    }
                    if (response.getContent().equals("1")) {
                        pfRecommendedApps.setVisibility(View.VISIBLE);
                    } else {
                        pfRecommendedApps.setVisibility(View.GONE);
                    }
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "action ---GET_RECOMMEND_APP_STATUS : " + action + "response : " + response);
                    }
                } else if (Consts.GET_MIME_INFO.equals(action)) {
                    info = Parser.parseUserInfo(response.getContent());
                    if (info == null) {
                        return;
                    }
                    updateName();
                    showIconAvatar(false);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void handleSSOBind(Response response, ConnectInfo info) {
        if (!isVisible() || App.getUid() == null | App.getUid().equals("")) {
            return;
        }
        if (response == null || info == null) {
            LoadingDialog.dismiss();
            aleartMessage(R.string.bind_failed, null);
            return;
        }
        if (response.getStatusCode() == 200) {
            CToast.showToast(R.string.bind_success);
            LoadingDialog.dismiss();
            ConnectBuilder.getBindList(App.getUid());
        } else if (response.getStatusCode() == 400) {
            if (Oauth.TYPE_QQ.equals(info.getTag2())) {
                aleartMessage(R.string.bind_conflict_qq, null);
            } else if (Oauth.TYPE_WECHAT.equals(info.getTag2())) {
                aleartMessage(R.string.bind_conflict_wecat, null);
            } else if (Oauth.TYPE_WEIBO.equals(info.getTag2())) {
                aleartMessage(R.string.bind_conflict_weibo, null);
            } else {
                aleartMessage(R.string.bind_conflict, null);
            }
            LoadingDialog.dismiss();
        } else {
            aleartMessage(R.string.bind_failed, null);
            LoadingDialog.dismiss();
        }
    }

    private void handleSSOUnbind(Response response, ConnectInfo info) {
        if (response == null) {
            CToast.showToast(R.string.bind_failed);
            return;
        }
        if (response.getStatusCode() == 200 || response.getStatusCode() == 404) {
            for (Binding binding : bindings) {
                if (binding.getProvider().equals(info.getTag())) {
                    bindings.remove(binding);
                    break;
                }
            }
            CToast.showToast(R.string.unbind_success);
            updateBindUi();
        } else {
            CToast.showToast(R.string.unbind_failed);
        }

    }

    public MeFragment() {
        // UMutils.instance().diyEvent(ID.EventAboutUs);
    }

    @Override
    public String getFragmentName() {
        return "MeFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.profile_fragment, container, false);
            initComponent();
            initBroadcast();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        LogUtil.d(TAG, " info --- " + info);
        return mRootView;
    }

    @Override
    public void onShown() {
        super.onShown();
        initCacheSize();
        info = App.getUserInfo();
        if (info == null) {
            ConnectBuilder.getMineInfo();
        }
        updateUI();
        if (App.DEBUG && App.getUid() != null) {
            LogUtil.d(TAG, "UserID " + App.getUid());
        }

    }

    public void initCacheSize() {
        TaskRuntime.instance().run(new Task() {
            @Override
            public void run() {
                File externalCacheDir = MeFragment.this.getActivity().getExternalCacheDir();
                File videoFile = new File(MediaUtil.getVideoDir());

                long imageSize = Utils.getDirSize(externalCacheDir);
                long videoSize = Utils.getDirSize(videoFile);
                long allSize = imageSize + videoSize;

                if (allSize > 0) {
                    cacheSize = Utils.formatFileSize(allSize);
                    // pfClearCache.setContent(cacheSize);
                    tvCacheSize.setText(cacheSize);
                }
                if (App.DEBUG) {
                    LogUtil.d(TAG, "-- externalCacheDir --" + externalCacheDir + "-- fileSize -- " + allSize + "-- cacheSize --" + cacheSize);
                }
            }
        });
    }

    /**
     * invoked by listBindings received
     */
    private void onListBindings(Response response) {
        Bindings bingingz = Parser.parseBingdings(response.getContent());
        bindings = bingingz.getBindings();
        AppData.setBindings(bindings);
        updateBindUi();
//        LoadingDialog.dismiss();
    }

    private void updateMobileUi() {
        if (!isVisible()) return;
        if (info == null) {
            return;
        }
        if (info.isBindMobile()) {
            pfMobile.setLittleText(Utils.repleaseMobileNumber(info.getMobile()));
            pfMobile.setContent(getString(R.string.modify));
            pfACCountPassword.setVisibility(View.VISIBLE);
        } else {
            pfMobile.setContent(getString(R.string.binding));
            pfMobile.setLittleText("");
            pfACCountPassword.setVisibility(View.GONE);
        }

    }

    private void updateBindUi() {
        if (!isVisible()) return;
        String session = AppData.getToken();
        if (!Utils.isEmpty(session)) {
            mUserInfo.setVisibility(View.VISIBLE);
        } else {
            llAccountInfo.setVisibility(View.GONE);
            return;
        }
        if (bindings == null) {
            bindings = AppData.getBindings();
        }
        Preference lastRemove = null;
        ArrayList<Preference> preferences = new ArrayList<Preference>();
        preferences.add(pfQQ);
        preferences.add(pfWechat);
        preferences.add(pfWeibo);
        for (Binding binding : bindings) {
            Preference preference = null;
            if (binding.getProvider().equals(Consts.PROVIDERS.qq.toString())) {
                preference = pfQQ;
            } else if (binding.getProvider().equals(Consts.PROVIDERS.wechat.toString())) {
                preference = pfWechat;
            } else if (binding.getProvider().equals(Consts.PROVIDERS.weibo.toString())) {
                preference = pfWeibo;
            }
            preferences.remove(preference);
            lastRemove = preference;
            setAccountPreference(preference, binding);
        }
        for (Preference preference : preferences) {
            setAccountPreference(preference, null);
        }
        if (bindings.size() == 1 && lastRemove != null) {
            lastRemove.setContent("");
        }
        AppData.setBindings(bindings);
    }


    private void setAccountPreference(Preference preference, Binding binding) {
        if (preference == null) return;
        if (binding == null) {
            preference.setLittleText("");
            preference.setContent(getString(R.string.binding));
        } else {
            preference.setLittleText(binding.getUser_name());
            preference.setContent(getString(R.string.cancel_binding));
        }

    }

    private void initComponent() {
        pfName = (Preference) mRootView.findViewById(R.id.pf_user_name);
        pfAvatar = (Preference) mRootView.findViewById(R.id.pf_user_icon);
        pfFeedback = (Preference) mRootView.findViewById(R.id.pf_feedback);
        pfAboutUs = (Preference) mRootView.findViewById(R.id.pf_about_us);
        pfPwdLock = (Preference) mRootView.findViewById(R.id.pf_pwd);
        pfBullet = (Preference) mRootView.findViewById(R.id.pf_bullet);

        pfMobile = (Preference) mRootView.findViewById(R.id.pf_mobile);
        pfACCountPassword = (Preference) mRootView.findViewById(R.id.pf_account_password);
        pfWechat = (Preference) mRootView.findViewById(R.id.pf_wechat);
        pfWeibo = (Preference) mRootView.findViewById(R.id.pf_weibo);
        pfQQ = (Preference) mRootView.findViewById(R.id.pf_qq);

        pfRecommendedApps = (Preference) mRootView.findViewById(R.id.pf_recommended_apps);

        rlClearCache = (LinearLayout) mRootView.findViewById(R.id.rl_clear_cache);
        tvCacheSize = (TextView) mRootView.findViewById(R.id.tv_cache_size);

        logout = (Button) mRootView.findViewById(R.id.bt_logout);
        mUserInfo = (LinearLayout) mRootView.findViewById(R.id.ll_user_info);
        mNotLogin = (LinearLayout) mRootView.findViewById(R.id.ll_not_login);
        llAccountInfo = (LinearLayout) mRootView.findViewById(R.id.ll_account_info);

        addFriend = (ImageView) mRootView.findViewById(R.id.iv_add_firend);
        addFriend.setOnClickListener(this);

        pfMobile.setOnClickListener(this);
        pfACCountPassword.setOnClickListener(this);
        pfWechat.setOnClickListener(this);
        pfWeibo.setOnClickListener(this);
        pfQQ.setOnClickListener(this);

        pfName.setOnClickListener(this);
        pfAvatar.setOnClickListener(this);
        pfFeedback.setOnClickListener(this);
        pfAboutUs.setOnClickListener(this);
        pfPwdLock.setOnClickListener(this);
        pfRecommendedApps.setOnClickListener(this);
        pfBullet.setOnCheckedChangeListener(this);
        // pfClearCache.setOnClickListener(this);
        rlClearCache.setOnClickListener(this);
        logout.setOnClickListener(this);
        mNotLogin.setOnClickListener(this);
        // mRootView.findViewById(R.id.pf_userguide).setVisibility(View.GONE);
        // mRootView.findViewById(R.id.pf_userguide).setOnClickListener(this);
        int width = Utils.dp2px(getActivity(), 40);
        pfAvatar.setRightIconSize(width, width);
    }

    /**
     * update ui login or not login
     */
    private void updateUI() {
        String session = AppData.getToken();
        if (!Utils.isEmpty(session)) {
            mUserInfo.setVisibility(View.VISIBLE);
            mNotLogin.setVisibility(View.GONE);
            llAccountInfo.setVisibility(View.VISIBLE);
            logout.setVisibility(View.VISIBLE);
            pfPwdLock.setVisibility(View.VISIBLE);
            addFriend.setVisibility(View.VISIBLE);
            updateName();
            showIconAvatar(false);
        } else {
            llAccountInfo.setVisibility(View.GONE);
            mUserInfo.setVisibility(View.GONE);
            mNotLogin.setVisibility(View.VISIBLE);
            logout.setVisibility(View.GONE);
            pfPwdLock.setVisibility(View.GONE);
            addFriend.setVisibility(View.GONE);
        }
        // pfClearCache.setContent(cacheSize);
        tvCacheSize.setText(cacheSize);
        updatePwdset();
        updateBullet();
        updateBindUi();
        updateMobileUi();
        // String version = App.VERSION;
        // ConnectBuilder.getRecommendedApps("1", "1.2.0");
        ConnectBuilder.getRecommendedApps("1", App.mobileInfo.VERSION);
        ConnectBuilder.getBindList(App.getUid());
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.UNBIND_DEVICE);
        ift.addAction(Consts.UPDATE_MIME_INFO);
        ift.addAction(Consts.SET_AVATAR);
        ift.addAction(Consts.GET_RECOMMEND_APP_STATUS);
        ift.addAction(Consts.LIST_BINDINGS);
        ift.addAction(Consts.SSO_BIND);
        ift.addAction(Consts.UNBIND_SSO);
        ift.addAction(Consts.LOGIN_WX_SUCCEED);
        ift.addAction(Consts.GET_MIME_INFO);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void updateName() {
        if (info == null) {
            return;
        }
        pfName.setContent(info.getName());
    }

    private void updatePwdset() {
        if (LockManager.getInstance().getAppLock().isPasscodeSet()) {
            pfPwdLock.setContent(getResources().getString(R.string.open));
        } else {
            pfPwdLock.setContent(getResources().getString(R.string.close));
        }
    }

    private void updateBullet() {
        pfBullet.setChecked(BulletManager.showBullet);
    }

    public void showIconAvatar(boolean reload) {
        if (!isShown()) return;
        if (info == null) {
            return;
        }
        //ToDO get new avatar has delay;
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        pfAvatar.setRightIcon(R.drawable.default_image_small);
        ImageManager.instance().loadAvater(pfAvatar.getRightImage(), info.getId(), reload);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bt_logout) {
            showLogout();
        } else if (id == R.id.pf_user_name) {
            modifyName();
        } else if (id == R.id.pf_user_icon) {
            showOptionDialog();
        } else if (id == R.id.pf_feedback) {
            showFeedbackInfo();
        } else if (id == R.id.pf_about_us) {
            showAboutUS();
        } else if (id == R.id.pf_pwd) {
            showPwdSet();
        } else if (id == R.id.ll_not_login) {
            showLogin();
        } else if (id == R.id.pf_recommended_apps) {
            showRecommentApps();
        } else if (id == R.id.rl_clear_cache) {
            showClearCache();
        } else if (id == R.id.pf_qq) {
            if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mobileqq")) {
                CToast.showToast(R.string.not_install_qq);
            } else {
                handleThirdAccountClick(Consts.PROVIDERS.qq);
            }
        } else if (id == R.id.pf_weibo) {
            handleThirdAccountClick(Consts.PROVIDERS.weibo);
        } else if (id == R.id.pf_wechat) {
            if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mm")) {
                CToast.showToast(R.string.not_install_wx);
            } else {
                handleThirdAccountClick(Consts.PROVIDERS.wechat);
            }
        } else if (id == R.id.pf_mobile) {
            handleMobileClick();
        } else if (id == R.id.pf_account_password) {
            ((HomeActivity) getActivity()).showChangePassword();
        } else if (id == R.id.iv_add_firend) {
            if (App.getUserInfo() != null) {
                ((HomeActivity) getActivity()).showAddFriendActivity();
                UMutils.instance().diyEvent(ID.EventFindFriends);
            }

//            LogUtil.d(TAG, "sort b " + list);
//            Utils.sordContastName(list);
//            LogUtil.d(TAG, "sort a " + list);
        }
    }

    private void handleMobileClick() {
        if (info == null) {
            return;
        }
        if (info.getMobile() == null || info.getMobile().equals("")) {
            IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
            bundle.setRegisterType(IRegisterController.RegisterType.bind);
            bundle.newUserBindPhone(false);
            ((IRegisterController) getActivity()).showRegister(bundle);
        } else {
            IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
            bundle.setRegisterType(IRegisterController.RegisterType.bind);
            bundle.oldMobile = info.getMobile();
            bundle.newUserBindPhone(false);
            ((HomeActivity) getActivity()).showModifyPhoneFragment(bundle);
        }
    }

    private void handleThirdAccountClick(Consts.PROVIDERS provider) {
        if (Utils.checkBinded(bindings, provider)) {
            if (bindings.size() > 1) {
                unBindAccount(provider);
            }
        } else {
            switch (provider) {
                case qq:
                    oauthHelper = new QQHelper();
                    break;
                case wechat:
                    oauthHelper = new WXHelper();
                    App.wxHelper = (WXHelper) oauthHelper;
                    break;
                case weibo:
                    oauthHelper = new WBHelper();
                    break;
            }
            if (oauthHelper != null) {
                oauthHelper.setOauthListener(oauthListener);
                oauthHelper.oauth();
            }
        }

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

    private void unBindAccount(final Consts.PROVIDERS provider) {
        if (Utils.checkBinded(bindings, provider)) {
            AlertDialog.Builder clearCache = new AlertDialog.Builder(getActivity());
            clearCache.setMessage(R.string.unbind_third_warning);
            DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (which == DialogInterface.BUTTON_POSITIVE) {
                        ConnectBuilder.sso_unbind(provider);
                    } else {
                        dialog.cancel();
                    }
                    dialog.cancel();
                }
            };
            clearCache.setPositiveButton(R.string.confirm, listener);
            clearCache.setNegativeButton(R.string.cancel, listener);
            AlertDialog dialog = clearCache.show();
            TextView msg = (TextView) dialog.findViewById(android.R.id.message);
            msg.setGravity(Gravity.CENTER);
        }
    }

    private void aleartMessage(int content, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder clearCache = new AlertDialog.Builder(getActivity());
        clearCache.setMessage(content);
        clearCache.setPositiveButton(R.string.confirm, listener);
        AlertDialog dialog = clearCache.show();
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
    }

    private void showClearCache() {
        AlertDialog.Builder clearCache = new AlertDialog.Builder(getActivity());
        clearCache.setMessage(R.string.clear_cache_warnning);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    clearCache();
                    // pfClearCache.setContent("0B");
                    tvCacheSize.setText("0KB");
                } else {
                    dialog.cancel();
                }
                dialog.cancel();
            }
        };

        clearCache.setPositiveButton(R.string.confirm, listener);
        clearCache.setNegativeButton(R.string.cancel, listener);
        AlertDialog dialog = clearCache.show();
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
    }

    private void clearCache() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ImageLoader.getInstance().clearDiskCache();
                VideoManager.instance().clear();
                String path = MediaUtil.getVideoDir();
                if (!Utils.isEmpty(path)) {
                    File file = new File(path);
                    delete(file);
                }

                LogUtil.d(TAG, " path " + MediaUtil.getVideoDir());
            }
        };
        TaskRuntime.instance().run(runnable);
    }

    public void delete(File file) {
        if (!FileUtil.exists(file)) {
            return;
        }
        if (file.isFile()) {
        }
        File files[] = file.listFiles();
        if (files == null || files.length < 1) {
        }
        for (int index = 0; index < files.length; index++) {
            // delete(files[index]);
            files[index].delete();
        }
        // file.delete();

    }

    private void showRecommentApps() {
        BrowserFragment browserFragment = new BrowserFragment();
        browserFragment.setHomeUrl(Consts.URL_RECOMMEND_APPS);
        browserFragment.show(getBaseActivity());
    }

    private void showLogout() {
        AlertDialog.Builder dialog = DialogUtils.create();
        dialog.setMessage(R.string.confirm_logout);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    ConnectBuilder.unbindDevice();
                } else {
                    dialog.cancel();
                }
                dialog.cancel();
            }
        };
        dialog.setPositiveButton(R.string.confirm, listener);
        dialog.setNegativeButton(R.string.cancel, listener);
        AlertDialog d = dialog.show();
        TextView msg = (TextView) d.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
        UMutils.instance().diyEvent(ID.EventSignOut);
    }

    private void modifyName() {
        if (info == null) {
            return;
        }
        final AlertDialog.Builder editName = new AlertDialog.Builder(MeFragment.this.getActivity());
        editName.setTitle(R.string.input_name);
        editName.setMessage(R.string.input_name);

        final EditText inputNewName = new EditText(MeFragment.this.getActivity());
        if (info.getName() == null) {
            return;
        } else {
            inputNewName.setText(info.getName());
        }
//        int index = inputNewName.getSelectionStart();
        inputNewName.setSelection(info.getName().length());
        if (App.DEBUG) {
            LogUtil.d(TAG, " modifyName -- index : " + "" + " -- info.getName().length() -- " + info.getName().length());
        }

        InputFilter[] filters = {new LengthFilter(25)};
        inputNewName.setFilters(filters);
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                if (which == DialogInterface.BUTTON_POSITIVE) {

                    String newName = inputNewName.getText().toString();
                    if (Utils.isEmpty(newName) || newName.trim().equals("")) {
                        CToast.showToast(R.string.name_empty);
                    } else if (SensitiveWordGrepManager.getInstance().doSensitiveGrep(getActivity(), new SensitiveWordGrepManager.WordsWrapper(newName, SensitiveWordGrepManager.Type.user_name))) {
                        if (newName.trim().equals(info.getName())) {
                            CToast.showToast(R.string.name_not_change);
                        } else {
                            JSONObject jo = new JSONObject();
                            jo.put(Consts.NAME, newName.trim());
                            ConnectBuilder.updateMineInfo(jo.toString());
                        }
                    }

                }
                dialog.cancel();
                MeFragment.this.getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
            }

        };

        editName.setPositiveButton(R.string.confirm, listener);
        editName.setNegativeButton(R.string.cancel, listener);
        editName.setView(inputNewName);
        editName.show();
    }

    private void showFeedbackInfo() {
        // if (feedbackDialog == null) {
        // LinearLayout contentView = (LinearLayout) LayoutInflater.from(
        // this.getActivity()).inflate(R.layout.feedback_dialog, null);
        // LayoutParams lp = new LayoutParams((int) (App.SCREEN_WIDTH * 0.9),
        // LayoutParams.WRAP_CONTENT);
        // final OnClickListener onClickListener = new OnClickListener() {
        // @Override
        // public void onClick(View v) {
        // feedbackDialog.dismiss();
        // }
        // };
        // feedbackDialog = new CDialog();
        // feedbackDialog.setCanceledOnTouchOutside(true);
        // feedbackDialog.setContentView(contentView, lp);
        // feedbackDialog.findViewById(R.id.view_empty_feedback)
        // .setOnClickListener(onClickListener);
        // feedbackDialog.findViewById(R.id.tv_close).setOnClickListener(
        // onClickListener);
        // }
        // feedbackDialog.show();
        DialogUtils.dialog(R.string.feedback_info);
        UMutils.instance().diyEvent(ID.EventFeedback);
    }

    private void showPwdSet() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), PwdSetupActivity.class);
        startActivity(intent);
        UMutils.instance().diyEvent(ID.EventPWDSETUP);
    }

    private void showAboutUS() {
        if (fragmentAboutUs == null) {
            fragmentAboutUs = new FragmentAboutUs();
        }
        fragmentAboutUs.show(this.getBaseActivity());
        UMutils.instance().diyEvent(ID.EventAboutUs);
    }

    private void showLogin() {
        Intent intent = new Intent(getBaseActivity(), LoginActivity.class);
        this.startActivity(intent);
    }

    private void showOptionDialog() {
        if (optionDialog == null) {
            ArrayList<Integer> mDatas = new ArrayList<Integer>();
            mDatas.add(R.string.check_avatar);
            mDatas.add(R.string.modify);
            mDatas.add(R.string.cancel);

            optionDialog = new OptionDialog(getActivity());
            optionDialog.setOptionListener(this);
            optionDialog.setData(mDatas);
        }
        optionDialog.show();
    }

    @Override
    public void OnOptionClick(int position, int optionId, Object object) {
        if (optionId == R.string.check_avatar) {
            showAvatarDialog();
        } else if (optionId == R.string.modify) {
            Intent intent = new Intent();
            intent.setClass(MeFragment.this.getActivity(), LocalImageActivity.class);
            intent.putExtra(Consts.MAX_CHOOSEN, 1);
            intent.putExtra(Consts.NEED_VIDEO, false);
            intent.putExtra(Consts.DISABLE_PHOTO_PREVIEW, true);
            startActivityForResult(intent, Consts.REQUEST_CODE_CHOOSE_IMAGE);
        }
    }

    private void showAvatarDialog() {
        if (avatarDialog == null) {
            avatarDialog = new CDialog(R.style.custom_dialog);
            avatarDialog.setCanceledOnTouchOutside(true);
            LinearLayout contentView = (LinearLayout) LayoutInflater.from(this.getActivity()).inflate(R.layout.big_avatar_dialog, null);
            avatarDialog.setContentView(contentView);
            avatarDialog.findViewById(R.id.iv_big_avatar).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    avatarDialog.dismiss();
                }
            });
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
        if (info == null) {
            return;
        }
        if (!avatarDialog.isShowing()) {
            return;
        }
        ImageViewEx ivAvatar = (ImageViewEx) avatarDialog.findViewById(R.id.iv_big_avatar);
        ImageManager.instance().loadAvater(ivAvatar, info.getId(), false);
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        AppData.setBindings(bindings);
        oauthHelper = null;
        super.onDestroy();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (oauthHelper != null) {
            oauthHelper.onActivityResult(requestCode, resultCode, data);
        }
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == Consts.REQUEST_CODE_CHOOSE_IMAGE) {
            if (!data.hasExtra(Consts.PATH_LIST)) {
                return;
            }
            try {
                List<UploadImage> paths = (List<UploadImage>) data.getExtras().get(Consts.PATH_LIST);
                if (!Utils.isEmpty(paths)) {
                    String path = paths.get(0).filePath;
                    if (info != null) {
                        ImageManager.instance().setAvater(path, info.getId());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onCheckedChanged(View v, boolean isChecked) {
        if (v.getId() == R.id.pf_bullet) {
            BulletManager.instance().setShowBullet(isChecked);
        }
    }

    public void onMobileBundSuccess(IRegisterController.RegisterBundle bundle) {
        if (bundle.registerType.equals(IRegisterController.RegisterType.bind)) {
            CToast.showToast(R.string.bind_success);
        }
        info.setMobile(bundle.mobile);
        updateMobileUi();
        ConnectBuilder.getBindList(App.getUid());
    }

}
