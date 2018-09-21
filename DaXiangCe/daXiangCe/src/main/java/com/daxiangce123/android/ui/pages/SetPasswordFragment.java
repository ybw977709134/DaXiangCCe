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
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.SensitiveWordGrepManager;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class SetPasswordFragment extends BaseFragment implements
        OnClickListener {

    private String TAG = "SetPasswordFragment";
    private View mRootView = null;
    private ImageView mBack;
    private TextView mTitle;
    private TextView mSubmit;
    private EditText mInputPWD;
    private EditText mInputPWDAgain;
    private EditText mInputNmae;

    private IRegisterController.RegisterBundle bundle;

    public SetPasswordFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    public void setRegisterBundle(IRegisterController.RegisterBundle bundle) {
        this.bundle = bundle;
    }


    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                if (response != null) {
                    LogUtil.d(TAG, "action:" + action + "	response:" + response);
                    if (response.getStatusCode() == 200) {
                        ((IRegisterController) getActivity()).mobileLogin(bundle);
                    } else {
                        CToast.showToast(R.string.request_failed);
                        if (App.DEBUG) {
                            LogUtil.v(TAG, response.getError().toString());
                        }
                        mSubmit.setEnabled(true);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_set_password, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        initBroadcast();
        return mRootView;
    }

    private void initComponent() {
        mTitle = (TextView) mRootView.findViewById(R.id.register_title);
        mTitle.setText(bundle.title);
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        if (bundle.isNew) {
            mBack.setVisibility(View.GONE);
        }
        mSubmit = (TextView) mRootView.findViewById(R.id.submit);
        mInputPWD = (EditText) mRootView.findViewById(R.id.input_password);
        mInputPWDAgain = (EditText) mRootView.findViewById(R.id.input_password_again);
        mInputNmae = (EditText) mRootView.findViewById(R.id.input_name);
        if (bundle.registerType == IRegisterController.RegisterType.passwordRecovery || bundle.registerType == IRegisterController.RegisterType.bind) {
            mInputNmae.setVisibility(View.GONE);
        } else {
            mInputNmae.setVisibility(View.VISIBLE);
        }
        mBack.setOnClickListener(this);
        mSubmit.setOnClickListener(this);
        mSubmit.setEnabled(true);
    }

    private boolean verfierPassword() {
        String password = mInputPWD.getText().toString();
        String passwordAgain = mInputPWDAgain.getText().toString();
        if (Utils.isPasswordVilied(password)) {
            return true;
        } else {
            return false;
        }
    }

    private void submitRegisterMobile() {
        String password = mInputPWD.getText().toString();
        String passwordAgain = mInputPWDAgain.getText().toString();
        if (!password.equals(passwordAgain)) {
            CToast.showToast(R.string.repassword_not_same);
        } else if (verfierPassword()) {
            submitToServer();
        } else {
            CToast.showToast(R.string.password_not_validate);
        }
    }

    private void submitToServer() {
        String password = mInputPWD.getText().toString();
        bundle.password = password;
        mSubmit.setEnabled(false);
        String inputName = mInputNmae.getText().toString();
        String nick;
        if (Utils.isEmpty(inputName)) {
            nick = Utils.repleaseMobileNumberWithContact(bundle.mobile);
        } else {
            nick = inputName;
        }
        if (!SensitiveWordGrepManager.getInstance().doSensitiveGrep(getActivity(), new SensitiveWordGrepManager.WordsWrapper(nick, SensitiveWordGrepManager.Type.user_name))) {
            return;
        }

        if (bundle.registerType == IRegisterController.RegisterType.passwordRecovery) {
            ConnectBuilder.resetPassword(bundle.mobile, null, bundle.confirmation, password);
        } else if (bundle.registerType == IRegisterController.RegisterType.register) {
            UMutils.instance().diyEvent(UMutils.ID.EventSignInViaMobile);
            ConnectBuilder.createNewUser(bundle.mobile, password, bundle.confirmation, nick);
        } else if (bundle.registerType == IRegisterController.RegisterType.bind) {
            UMutils.instance().diyEvent(UMutils.ID.EventBindingMobile);
            ConnectBuilder.createNewUser(bundle.mobile, password, bundle.confirmation, null);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            Utils.hideIME(mInputPWDAgain);
            onBackPressed();
        } else if (id == R.id.submit) {
            submitRegisterMobile();
        }
    }

    @Override
    public String getFragmentName() {
        return "SetPasswordFragment";
    }


    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.CREATE_NEW_USER);
        ift.addAction(Consts.RESET_PASSWORD);
        Broadcaster.registerReceiver(receiver, ift);
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (mInputPWDAgain != null)
            Utils.hideIME(mInputPWDAgain);
        if (bundle.registerType == IRegisterController.RegisterType.bind) {
            if (bundle.isNew) {
                ((HomeActivity) getActivity()).showConfirmationReceiver(bundle);
            } else {

                ((HomeActivity) getActivity()).showMe();
            }
        } else {
            back();

        }
        return true;
    }
}
