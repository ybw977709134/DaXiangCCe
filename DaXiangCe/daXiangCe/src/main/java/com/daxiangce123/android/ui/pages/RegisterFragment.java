package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UnbindDevice;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class RegisterFragment extends BaseFragment implements OnClickListener {
    private String TAG = "RegisterFragment";
    private View mRootView = null;
    private TextView mTitle;

    //    private WebView webView;
    private WebViewClient client;
    private ImageView mBack;
    private TextView mPrompt;
    private TextView mGetVerificationCode;
    private TextView mArea;
    private EditText mPhoneNumber;
    private SubmitVerficationCodeFragment submitVerficationCodeFragment;
    private long requestId = 0L;
    private IRegisterController.RegisterBundle bundle;
    private RegionChooserFragment regionChooserFragment;

    private void showPhoneArea() {
        if (regionChooserFragment == null) {
            regionChooserFragment = new RegionChooserFragment();
        }
        regionChooserFragment.setTarget(mArea);
        regionChooserFragment.show(getBaseActivity());
    }

    public RegisterFragment() {
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
                // ToDo if this page is visible then get this action
                if (!isVisible()) {
                    return;
                }
                if (response != null) {
                    LogUtil.d(TAG, "action:" + action + "	response:" + response);
                    if (response.getStatusCode() == 200) {
                        CToast.showToast(getString(R.string.send_verification_code_to_x, mArea.getText() + mPhoneNumber.getText().toString()));
                        showSubmitVerificationCode();
                        return;
                    } else if (response.getStatusCode() == 404) {
                        ViewUtil.aleartMessage(R.string.phone_not_existed_singin_first, R.string.register, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
                                bundle.setRegisterType(IRegisterController.RegisterType.register);
                                ((IRegisterController) getActivity()).showRegister(bundle);
                            }
                        }, getActivity());
                        loacInputUi(true);
                    } else if (response.getStatusCode() == 409) {

                        if (bundle.registerType != IRegisterController.RegisterType.bind) {
                            ViewUtil.aleartMessage(R.string.phone_existed, R.string.login, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    IRegisterController.RegisterBundle bundle = new IRegisterController.RegisterBundle();
                                    bundle.setRegisterType(IRegisterController.RegisterType.register);
                                    bundle.mobile = mPhoneNumber.getText().toString();
                                    ((IRegisterController) getActivity()).showSignin(bundle);
                                }
                            }, getActivity());
                        } else {
                            ViewUtil.aleartMessage(R.string.phone_existed_no_login, null, getActivity());
                        }
                    } else if (response.getStatusCode() == 400) {
                        CToast.showToast(R.string.invalid_phone);
                    }
                    loacInputUi(true);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_get_verifcation_code, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initBroadcast();
        initData();
        return mRootView;
    }

    private void initComponent() {
        mTitle = (TextView) mRootView.findViewById(R.id.register_title);

        mBack = (ImageView) mRootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mPrompt = (TextView) mRootView.findViewById(R.id.bind_phone_prompt);
        mGetVerificationCode = (TextView) mRootView.findViewById(R.id.get_verification_code);
        mArea = (TextView) mRootView.findViewById(R.id.tv_area_code);
        mArea.setOnClickListener(this);
        mPhoneNumber = (EditText) mRootView.findViewById(R.id.et_input_phone_number);

        isPhoneNumberValid();
        mPhoneNumber.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // TODO Auto-generated method stub
                LogUtil.d(TAG, "mPhoneNumber: +" + mPhoneNumber.getText());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub

                LogUtil.d(TAG, "mPhoneNumber: --" + mPhoneNumber.getText());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // TODO Auto-generated method stub
                isPhoneNumberValid();
                LogUtil.d(TAG, "mPhoneNumber: " + mPhoneNumber.getText());
            }
        });

        mGetVerificationCode.setOnClickListener(this);
//        webView = (WebView) mRootView.findViewById(R.id.wv_content);
    }

    private void initData() {
        mTitle.setText(bundle.title);
        if (bundle != null && bundle.mobile != null && !bundle.mobile.equals("")) {
            mPhoneNumber.setText(bundle.mobile);
        }
        if (bundle.isNew) {
            mBack.setVisibility(View.GONE);
            mPrompt.setVisibility(View.VISIBLE);
        }

//        if (bundle != null && bundle.registerType == IRegisterController.RegisterType.register) {
//            webView.setVisibility(View.VISIBLE);
//            client = new WebViewClient() {
//                @Override
//                public boolean shouldOverrideUrlLoading(WebView view, String url) {
//                    if (url.equals("https://www.daxiangce123.com/agree")) {
//                        showUserAgreement();
//                    } else if (url.equals("https://www.daxiangce123.com/privacy")) {
//                        showPrivacyPolicy();
//                    }
//                    return true;
//                }
//            };
//
//            webView.setWebViewClient(client);
//            webView.setVerticalScrollBarEnabled(false);
//            webView.setBackgroundColor(0x00000000);
//            String url = "file:///android_asset/rich_text.html";
//            webView.loadUrl(url);
//        } else {
//            webView.setVisibility(View.GONE);
//        }
    }

    private void showUserAgreement() {
        Uri url = Uri.parse(Consts.URL_AGREE);
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        this.getActivity().startActivity(intent);
    }

    private void showPrivacyPolicy() {
        Uri url = Uri.parse(Consts.URL_PRIVACY);
        Intent intent = new Intent(Intent.ACTION_VIEW, url);
        this.getActivity().startActivity(intent);
    }

//    private void isPhoneNumberValid() {
//        Editable phoneNumber = mPhoneNumber.getText();
////        Pattern pattern = Pattern.compile("^((13[0-9])|(15[^4,//D])|(18[0,5-9]))\\d{8}$");
////        Matcher matcher = pattern.matcher(phoneNumber);
//        if (phoneNumber.length() > 9) {
//            mGetVerificationCode.setEnabled(true);
//        } else {
//            mGetVerificationCode.setEnabled(false);
//        }
//    }

    private void isPhoneNumberValid() {
        String phoneNumber = mPhoneNumber.getText().toString();
        String area = mArea.getText().toString();
        if (Utils.isMobileVilied(area, phoneNumber)) {
            mGetVerificationCode.setEnabled(true);
        } else {
            mGetVerificationCode.setEnabled(false);
        }
//        if (mArea.getText().equals("+86")) {
//            Pattern pattern = Pattern
//                    .compile("^((13[0-9])|(15[^4,//D])|(18[0,5-9]))\\d{8}$");
//            Matcher matcher = pattern.matcher(phoneNumber);
//            if (phoneNumber.length() == 11 && matcher.matches()) {
//                mGetVerificationCode.setEnabled(true);
//            } else {
//                mGetVerificationCode.setEnabled(false);
//            }
//        } else {
//            if (phoneNumber.length() > 9) {
//                mGetVerificationCode.setEnabled(true);
//            } else {
//                mGetVerificationCode.setEnabled(false);
//            }
//        }
    }

    @Override
    public String getFragmentName() {
        return "RegisterFragment";
    }

    public void initBroadcast() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Consts.REQUEST_CONFIRMATION);
        Broadcaster.registerReceiver(receiver, intentFilter);
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.get_verification_code) {
            requestConfirmationCode();
        } else if (id == R.id.tv_area_code) {
            showPhoneArea();
        }

    }

    /**
     * called when request send success
     */
    private void showSubmitVerificationCode() {
        String mobile = mArea.getText() + mPhoneNumber.getText().toString();
        bundle.mobile = mobile;
        ((IRegisterController) getActivity()).showConfirmationReceiver(bundle);
    }

    private void requestConfirmationCode() {
        loacInputUi(true);
        requestId = System.currentTimeMillis();
        String mobile = mArea.getText() + mPhoneNumber.getText().toString();
        ConnectBuilder.requestConfirmationCode(bundle.purpoze, mobile, requestId);
    }

    private void loacInputUi(boolean lock) {
        mArea.setEnabled(lock);
        mPhoneNumber.setEnabled(lock);
        mGetVerificationCode.setEnabled(lock);
    }

    @Override
    public void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }


    @Override
    public boolean onBackPressed() {
        Utils.hideIME(mPhoneNumber);
        if (bundle.registerType == IRegisterController.RegisterType.bind) {
            if (bundle.isNew) {
//                ((HomeActivity) getActivity()).bindPhoneBack();
                UnbindDevice.unbindDevice(true);
                if (AppData.getFirstBindPhone()) {
                    AppData.setFirstBindPhone(false);
                }
                App.getActivity().finish();
            } else {
                ((HomeActivity) getActivity()).showMe();
            }
            return true;
        } else {
            back();
            return true;
        }
    }
}
