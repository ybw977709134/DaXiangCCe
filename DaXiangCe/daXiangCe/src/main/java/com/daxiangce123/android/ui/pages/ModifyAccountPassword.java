package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class ModifyAccountPassword extends BaseFragment implements
        OnClickListener {
    private static final String TAG = "ModifyAccountPassword";
    private View mRootView = null;
    private EditText edOldPassword, edNewPassword, edRepectPassword;
    private TextView submit;
    private ImageView mBack;
    private String oldPassword, newPassword, repectPassword;
    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            checkInput();
        }
    };

    public ModifyAccountPassword() {
        setBoottomBarVisibility(View.GONE);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                if (response != null) {
                    handlePasswordChange(info, response);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };


    private void handlePasswordChange(ConnectInfo info, Response response) {
        if (response != null) {
            LoadingDialog.dismiss();
            if (response.getStatusCode() == 200) {
                CToast.showToast(R.string.passcode_change_successed);
                onBackPressed();
            } else {
                CToast.showToast(R.string.passcode_wrong_passcode);
            }
        }
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(
                    R.layout.fragment_change_account_password, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        initBroadcast();
        return mRootView;
    }


    private void initComponent() {
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        edOldPassword = (EditText) mRootView.findViewById(R.id.et_input_old_password);
        edOldPassword.addTextChangedListener(textWatcher);
        edNewPassword = (EditText) mRootView.findViewById(R.id.et_input_new_password);
        edNewPassword.addTextChangedListener(textWatcher);
        edRepectPassword = (EditText) mRootView.findViewById(R.id.et_input_again_password);
        edRepectPassword.addTextChangedListener(textWatcher);
        submit = (TextView) mRootView.findViewById(R.id.tv_submite);
        submit.setOnClickListener(this);

        checkInput();
    }

    private void checkInput() {
        oldPassword = edOldPassword.getText().toString();
        newPassword = edNewPassword.getText().toString();
        repectPassword = edRepectPassword.getText().toString();
        if (Utils.isPasswordVilied(oldPassword) && Utils.isPasswordVilied(newPassword) && Utils.isPasswordVilied(repectPassword)) {
            submit.setEnabled(true);
        } else {
            submit.setEnabled(false);
        }

    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.tv_submite) {
            submit();
        }
    }


    public void submit() {

        if (Utils.isPasswordVilied(oldPassword) && Utils.isPasswordVilied(newPassword)) {
            if (repectPassword.equals(newPassword)) {
                if (!oldPassword.equals(newPassword)) {
                    LoadingDialog.show(R.string.loading);
                    ConnectBuilder.changePassword(oldPassword, newPassword, 0);
                } else {
                    CToast.showToast(R.string.password_same);
                }
            } else {
                CToast.showToast(R.string.twice_input_not_same_input_again);
            }
        } else {
            CToast.showToast(R.string.password_not_validate);
        }
    }

    @Override
    public String getFragmentName() {
        // TODO Auto-generated method stub
        return "ModifyBindingPhoneNumberFragment";
    }


    private void initBroadcast() {
        IntentFilter checkFilter = new IntentFilter();
        checkFilter.addAction(Consts.CHANGE_PASSWORD);
        Broadcaster.registerReceiver(receiver, checkFilter);
    }

    @Override
    public void onDestroy() {
        try {
            Broadcaster.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        Utils.hideIME(edRepectPassword);
        ((HomeActivity) getActivity()).showMe();
        try {
            Broadcaster.unregisterReceiver(receiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

}
