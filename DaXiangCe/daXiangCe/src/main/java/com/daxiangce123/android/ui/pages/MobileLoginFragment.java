package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.UserSuspendedInfo;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.activities.LoginActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class MobileLoginFragment extends BaseFragment implements OnClickListener {

    private String TAG = "MobileLoginFragment";
    private View mRootView = null;
    private ImageView mBack;
    private TextView mSignin;
    private TextView mForgetPassword;
    private TextView mTvPhoneArea;
    private EditText mEdMobile, mEdPassword;
    private IRegisterController.RegisterBundle bundle;
    private RegionChooserFragment regionChooserFragment;

    private void showPhoneArea() {
        if (regionChooserFragment == null) {
            regionChooserFragment = new RegionChooserFragment();
        }
        regionChooserFragment.setTarget(mTvPhoneArea);
        regionChooserFragment.show(getBaseActivity());
    }

    public void setRegisterBundle(IRegisterController.RegisterBundle bundle) {
        this.bundle = bundle;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_signin, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initBroadcast();
        return mRootView;
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (!isVisible()) {
                    return;
                }

                if (response != null) {
                    if (Consts.GET_USER_SUSPENDED_INFO.equals(action)) {
                        onGetUserSuspend(response, info);
                    } else {
                        handleLoginRespsonse(response);
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

    private void handleLoginRespsonse(Response response) {
        lockUi(false);
        if (response.getStatusCode() == 200) {
            return;
        }
        LoadingDialog.dismiss();
        if (response.getStatusCode() == 404) {
            ViewUtil.aleartMessage(R.string.phone_not_existed_singin_first, R.string.register, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
                    bundle.setRegisterType(IRegisterController.RegisterType.register);
                    ((IRegisterController) getActivity()).showRegister(bundle);
                }
            }, getActivity());
            lockUi(false);
            return;
        } else if (response.getStatusCode() == 401) {
//                && response.getError().toErrorCode() == ErrorCode.ACCOUNT_SUSPENDED) {
//            DialogUtils.dialog(R.string.account_suspended);
            return;
        }
        Toast.makeText(getActivity(), R.string.login_failed_user_invalid, Toast.LENGTH_LONG).show();
        lockUi(false);
    }

    private void initComponent() {
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        mSignin = (TextView) mRootView.findViewById(R.id.login);
        mForgetPassword = (TextView) mRootView.findViewById(R.id.forget_password);
        mTvPhoneArea = (TextView) mRootView.findViewById(R.id.tv_phone_area);
        mEdMobile = (EditText) mRootView.findViewById(R.id.input_phone_number);
        mEdPassword = (EditText) mRootView.findViewById(R.id.input_password);
        mBack.setOnClickListener(this);
        mSignin.setOnClickListener(this);
        mForgetPassword.setOnClickListener(this);
        mTvPhoneArea.setOnClickListener(this);
        if (bundle != null && bundle.mobile != null && !bundle.mobile.equals("")) {
            mEdMobile.setText(bundle.mobile);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            back();
            Utils.hideIME(mEdMobile);
        } else if (id == R.id.login) {
            userLogin();
        } else if (id == R.id.forget_password) {
            showFindPassword();
        } else if (id == R.id.tv_phone_area) {
            showPhoneArea();
        }
    }


    private void showFindPassword() {
        IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
        bundle.setRegisterType(IRegisterController.RegisterType.passwordRecovery);
        bundle.mobile = mEdMobile.getText().toString();
        ((IRegisterController) getActivity()).showRegister(bundle);
    }

    @Override
    public String getFragmentName() {

        return "MobileLoginFragment";
    }

    public void userLogin() {
        String area = mTvPhoneArea.getText().toString();
        String mobile = mEdMobile.getText().toString();
        String password = mEdPassword.getText().toString();
        if (!Utils.isMobileVilied(area, mobile)) {
            CToast.showToast(R.string.pls_input_right_phone_number);
            return;
        }
        if (!Utils.isPassword(password)) {
            CToast.showToast(R.string.password_not_validate);
            return;
        }
        IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
        bundle.mobile = area + mobile;
        bundle.password = password;
        bundle.registerType = IRegisterController.RegisterType.login;
        ((LoginActivity) getActivity()).mobileLogin(bundle);
        lockUi(true);
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

    private void lockUi(boolean lock) {
        mEdPassword.setEnabled(!lock);
        mEdMobile.setEnabled(!lock);
        mSignin.setEnabled(!lock);
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.MOBILE_LOGIN);
        ift.addAction(Consts.GET_USER_SUSPENDED_INFO);
        Broadcaster.registerReceiver(receiver, ift);
    }

}
