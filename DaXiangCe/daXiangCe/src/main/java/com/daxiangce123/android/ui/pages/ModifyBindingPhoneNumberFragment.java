package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ModifyBindingPhoneNumberFragment extends BaseFragment implements
        OnClickListener {
    private static final int REFRESH_TIMER = 2;
    private static final String TAG = "ModifyBindingPhoneNumberFragment";
    private View mRootView = null;
    private ImageView mBack;
    private TextView mGetVerificationCode;
    private TextView mOldPhone;
    private TextView mPrompt;
    private RelativeLayout mInputNewPhone;
    private TextView mNewPhone;
    private View mTopLine;
    private View mBottomLine;
    private EditText edOldInputPhoneNumber, edNewInputPhoneNumber, edConfirmation;
    private TextView tvOldArea, tvNewArea;
    private TextView tvSubmitConfirmation;
    private LinearLayout llInputsms;
    private IRegisterController.RegisterBundle bundle;
    private String newMobileInput, oldMobileInput;
    private Timer timer;
    private long requestId = 0L;
    private long lastTime = 60;

    private RegionChooserFragment regionChooserFragment;

    private void showPhoneArea(TextView target) {
        if (regionChooserFragment == null) {
            regionChooserFragment = new RegionChooserFragment();
        }
        regionChooserFragment.setTarget(target);
        regionChooserFragment.show(getBaseActivity());
    }

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

    public ModifyBindingPhoneNumberFragment() {
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
                    if (Consts.REQUEST_CONFIRMATION.equals(action)) {
                        handleRequestMSM(info, response);
                    } else if (Consts.CREATE_NEW_USER.equals(action)) {
                        handleChangeBind(info, response);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            try {
                Object[] messages = (Object[]) bundle.get("pdus");
                SmsMessage[] sms = new SmsMessage[messages.length];
                // Create messages for each incoming PDU
                for (int n = 0; n < messages.length; n++) {
                    sms[n] = SmsMessage.createFromPdu((byte[]) messages[n]);
                }
                for (SmsMessage msg : sms) {
                    // Verify if the message came from our known sender
                    if (handleMSM(msg.getMessageBody())) {
                        break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void handleChangeBind(ConnectInfo info, Response response) {
        if (response != null) {
            if (response.getStatusCode() == 200) {
                bundle.mobile = newMobileInput;
                ((IRegisterController) getActivity()).mobileLogin(bundle);
                mGetVerificationCode.setText(R.string.get_verification_code);
                UMutils.instance().diyEvent(UMutils.ID.EventModifyMobile);
            } else {
                CToast.showToast(R.string.verfication_error);
                if (App.DEBUG) {
                    LogUtil.v(TAG, response.getError().toString());
                }
                lockUi(false);
                tvSubmitConfirmation.setEnabled(true);
                mGetVerificationCode.setEnabled(true);
                timer.cancel();
            }
        }
    }

    private void checkInput() {
        oldMobileInput = tvOldArea.getText().toString() + edOldInputPhoneNumber.getText().toString();
        newMobileInput = tvNewArea.getText().toString() + edNewInputPhoneNumber.getText().toString();
        if (Utils.isMobileVilied(tvNewArea.getText().toString(), edNewInputPhoneNumber.getText().toString()) &&
                Utils.isMobileVilied(tvOldArea.getText().toString(), edOldInputPhoneNumber.getText().toString())) {
            mGetVerificationCode.setEnabled(true);
        } else {
            mGetVerificationCode.setEnabled(false);
        }
        if (edConfirmation.getText().toString().length() == 6) {
            tvSubmitConfirmation.setEnabled(true);
        } else {
            tvSubmitConfirmation.setEnabled(false);
        }
    }

    private boolean handleMSM(String messageBody) {
        if (App.DEBUG) {
            LogUtil.e(TAG, "on message received:" + messageBody);
        }
        if (Utils.checkMessage(messageBody)) {
            return false;
        }
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(messageBody);
        String confirmation = "";
        if (m.find()) {
            confirmation = m.group();
            submitConfirmaiton(confirmation);
            return true;
        }
        return false;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(
                    R.layout.fragment_binding_phone_number, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        initBroadcast();
        checkInput();
        return mRootView;
    }

    public void setBundle(IRegisterController.RegisterBundle bundle) {
        this.bundle = bundle;
    }

    private void initComponent() {
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);

        mGetVerificationCode = (TextView) mRootView
                .findViewById(R.id.get_verification_code);
        mGetVerificationCode.setOnClickListener(this);

        mOldPhone = (TextView) mRootView
                .findViewById(R.id.pls_input_old_phone_number);
        mOldPhone.setVisibility(View.VISIBLE);

        mPrompt = (TextView) mRootView
                .findViewById(R.id.modify_phone_number_prompt);
        mPrompt.setText(R.string.modify_phone_prompt);

        mInputNewPhone = (RelativeLayout) mRootView
                .findViewById(R.id.rl_input_new_phone_number);
        mInputNewPhone.setVisibility(View.VISIBLE);

        mNewPhone = (TextView) mRootView
                .findViewById(R.id.pls_input_new_phone_number);
        mNewPhone.setVisibility(View.VISIBLE);

        mTopLine = mRootView
                .findViewById(R.id.rl_input_new_phone_number_top_line);
        mTopLine.setVisibility(View.VISIBLE);
        mBottomLine = mRootView
                .findViewById(R.id.rl_input_new_phone_number_bottom_line);
        mBottomLine.setVisibility(View.VISIBLE);

        edOldInputPhoneNumber = (EditText) mRootView
                .findViewById(R.id.et_input_old_phone_number);
        edOldInputPhoneNumber.setHint(R.string.pls_input_eleven_old_phone_number);

        edNewInputPhoneNumber = (EditText) mRootView.findViewById(R.id.et_input_new_phone_number);
        edOldInputPhoneNumber.addTextChangedListener(textWatcher);
        edNewInputPhoneNumber.addTextChangedListener(textWatcher);

        tvOldArea = (TextView) mRootView.findViewById(R.id.tv_old_area_code);
        tvOldArea.setOnClickListener(this);

        tvNewArea = (TextView) mRootView.findViewById(R.id.tv_new_area_code);
        tvNewArea.setOnClickListener(this);

        llInputsms = (LinearLayout) mRootView.findViewById(R.id.ll_input_sms);
        tvSubmitConfirmation = (TextView) mRootView.findViewById(R.id.submit_verification_code);
        tvSubmitConfirmation.setOnClickListener(this);
        edConfirmation = (EditText) mRootView.findViewById(R.id.et_input_sms);
        edConfirmation.addTextChangedListener(textWatcher);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.get_verification_code) {
            checkOldNumber(false);

        } else if (id == R.id.submit_verification_code) {
            submitConfirmaiton(null);
        } else if (id == R.id.tv_new_area_code) {
            showPhoneArea((TextView) v);
        } else if (id == R.id.tv_old_area_code) {
            showPhoneArea((TextView) v);
        }
    }

    private void submitConfirmaiton(String confirmation) {
        restartTimer(lastTime);
        checkOldNumber(true);
        if (confirmation != null) {
            edConfirmation.setText(confirmation);
        }
        if (edConfirmation.getText().toString().length() == 6) {
//            lockUi(true);
            tvSubmitConfirmation.setEnabled(false);
            mGetVerificationCode.setEnabled(false);
            ConnectBuilder.createNewUser(newMobileInput, null, edConfirmation.getText().toString(), null);
            edConfirmation.setText("");
        } else {
            CToast.showToast(R.string.verfication_error);
        }
    }

    private void handleRequestMSM(ConnectInfo info, Response response) {
        if (info.getUniqId() != requestId) {
            return;
        }
        if (response.getStatusCode() == 200) {
            CToast.showToast(getString(R.string.send_verification_code_to_x, newMobileInput));
            llInputsms.setVisibility(View.VISIBLE);
            lockUi(true);
            restartTimer(60);
        } else if (response.getStatusCode() == 409) {
            ViewUtil.aleartMessage(R.string.phone_existed_no_login, null, getActivity());
            lockUi(false);
        }
    }

    public void checkOldNumber(boolean onlycheck) {
        oldMobileInput = tvOldArea.getText().toString() + edOldInputPhoneNumber.getText().toString();
        newMobileInput = tvNewArea.getText().toString() + edNewInputPhoneNumber.getText().toString();
        if (!oldMobileInput.equals(bundle.oldMobile)) {
            ViewUtil.aleartMessage(R.string.old_mobile_not_match, null, getActivity());
        } else if (!Utils.isMobileVilied(tvNewArea.getText().toString(), edNewInputPhoneNumber.getText().toString())) {
            ViewUtil.aleartMessage(R.string.phone_check_failed, null, getActivity());
        } else if (!onlycheck) {
//            lockUi(true);
            requestId = System.currentTimeMillis();
            ConnectBuilder.requestConfirmationCode(Consts.PURPOZE.registration, newMobileInput, requestId);
        }
    }

    public void lockUi(boolean lock) {
        tvOldArea.setEnabled(!lock);
        tvNewArea.setEnabled(!lock);
        edOldInputPhoneNumber.setEnabled(!lock);
        edNewInputPhoneNumber.setEnabled(!lock);
    }

    @Override
    public String getFragmentName() {
        return "ModifyBindingPhoneNumberFragment";
    }

    public void restartTimer(long time) {
        timer = new Timer();
        lastTime = time;
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                lastTime--;
                sendMessage(REFRESH_TIMER);
            }
        }, 1000, 1000);
    }


    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        if (msg.what == REFRESH_TIMER) {
            if (lastTime > 59) {

            } else if (lastTime > 0) {
                mGetVerificationCode.setTextColor(R.color.gray);
                mGetVerificationCode.setEnabled(false);
                mGetVerificationCode.setText(getString(R.string.get_agin, "(" + String.valueOf(lastTime) + "s" + ")"));
            } else {
                if (timer != null) {
                    timer.cancel();
                }
                mGetVerificationCode.setEnabled(true);
                mGetVerificationCode.setText(getString(R.string.get_agin, ""));
                mGetVerificationCode.setTextColor(this.getResources().getColor(R.color.blue));
                lockUi(false);
            }
        }
    }

    private void initBroadcast() {
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        getActivity().registerReceiver(smsReceiver, filter);
        IntentFilter checkFilter = new IntentFilter();
        checkFilter.addAction(Consts.CREATE_NEW_USER);
        checkFilter.addAction(Consts.REQUEST_CONFIRMATION);
        Broadcaster.registerReceiver(receiver, checkFilter);
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(smsReceiver);
            Broadcaster.unregisterReceiver(receiver);
            if (timer != null) {
                timer.cancel();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        Utils.hideIME(edOldInputPhoneNumber);
        if (timer != null) {
            timer.cancel();
        }
        if (bundle.registerType == IRegisterController.RegisterType.bind) {
            ((HomeActivity) getActivity()).showMe();
            try {
                getActivity().unregisterReceiver(smsReceiver);
                Broadcaster.unregisterReceiver(receiver);
                timer.cancel();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return true;
        }
        return super.onBackPressed();
    }

}
