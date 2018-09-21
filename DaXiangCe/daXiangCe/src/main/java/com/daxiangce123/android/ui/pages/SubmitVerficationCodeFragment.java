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
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.IRegisterController;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * To enter the confirmationCode and setPassword
 */
public class SubmitVerficationCodeFragment extends BaseFragment implements OnClickListener {
    private static final int REFRESH_TIMER = 2;
    private static final String TAG = "SubmitVerficationCodeFragment";
    private View mRootView = null;
    private ImageView mBack;
    private TextView mTitle;
    private TextView mSubmitVerificationCode;
    private TextView mTvSendVerificationTo;
    private TextView mTvTime;
    private int lastTime = 60;
    private EditText mEtVerification;
    private Timer timer;
//    private long last

    private IRegisterController.RegisterBundle bundle;

    public void setRegisterBundle(IRegisterController.RegisterBundle bundle) {
        this.bundle = bundle;
        if (mTvSendVerificationTo != null) {
            try {
                mTvSendVerificationTo.setText(getString(R.string.send_verification_code_to_x, bundle.mobile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SubmitVerficationCodeFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    private BroadcastReceiver smsReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
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
        }
    };

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d(TAG, "!!!action:");
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                if (response != null) {
                    LogUtil.d(TAG, "action:" + action + "	response:" + response);
                    if (response.getStatusCode() == 200) {
                        showSetPassword();
                    } else {
                        if (isVisible())
                            ViewUtil.aleartMessage(R.string.verfication_error, null, getActivity());
                        confirmationVerificationCode();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };

    private boolean handleMSM(String messageBody) {
        if (App.DEBUG) {
            LogUtil.e(TAG, "on message received:" + messageBody);
        }
        if (!Utils.checkMessage(messageBody)) {
            return false;
        }
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 6 + "})(?![0-9])");
        Matcher m = continuousNumberPattern.matcher(messageBody);
        String confirmation = "";
        if (m.find()) {
            confirmation = m.group();
            onVerficationCodereceived(confirmation);
            return true;
        }
        return false;
    }

    private void onVerficationCodereceived(String verficationCode) {
        mEtVerification.setText(verficationCode);
        //no need to check confirmation
        showSetPassword();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_submit_verfication_code, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        initBroadcast();
        restartTimer();
        return mRootView;
    }

    public void restartTimer() {
        timer = new Timer();
        lastTime = 60;
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
            if (lastTime > 0) {
                mTvTime.setTextColor(R.color.gray);
                mTvTime.setEnabled(false);
                mTvTime.setText(getString(R.string.get_agin, "(" + lastTime + "s" + ")"));
            } else {
                if (timer != null) {
                    timer.cancel();
                }
                mTvTime.setEnabled(true);
                mTvTime.setTextColor(this.getResources().getColor(R.color.blue));
                mTvTime.setText(getString(R.string.get_agin, ""));
            }
        }
    }

    private void initComponent() {
        mTitle = (TextView) mRootView.findViewById(R.id.register_title);
        mTitle.setText(bundle.title);
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        if(bundle.isNew){
            mBack.setVisibility(View.GONE);
        }
        mSubmitVerificationCode = (TextView) mRootView.findViewById(R.id.submit_verification_code);
        mTvSendVerificationTo = (TextView) mRootView.findViewById(R.id.tv_send_verification_code_to);
        mEtVerification = (EditText) mRootView.findViewById(R.id.et_input_sms);
        mEtVerification.requestFocus();
        mTvSendVerificationTo.setText(getString(R.string.send_verification_code_to_x, bundle.mobile));
        mTvTime = (TextView) mRootView.findViewById(R.id.tv_time);
        mTvTime.setOnClickListener(this);
        mTvTime.setText(getString(R.string.get_agin, "(" + String.valueOf(lastTime) + "s" + ")"));
        confirmationVerificationCode();
        mEtVerification.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                confirmationVerificationCode();
            }
        });
        mBack.setOnClickListener(this);
        mSubmitVerificationCode.setOnClickListener(this);
    }

    private void confirmationVerificationCode() {
        Editable verificationCode = mEtVerification.getText();
        if (verificationCode.length() == 6) {
            mSubmitVerificationCode.setEnabled(true);
        } else {
            mSubmitVerificationCode.setEnabled(false);
        }
    }

    @Override
    public String getFragmentName() {

        return "SubmitVerficationCodeFragment";
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            onBackPressed();
        } else if (id == R.id.submit_verification_code) {
            mSubmitVerificationCode.setEnabled(false);
            ConnectBuilder.checkConfirmation(bundle.mobile, mEtVerification.getText().toString(), bundle.purpoze);
        } else if (id == R.id.tv_time) {
            restartTimer();
            ConnectBuilder.requestConfirmationCode(bundle.purpoze, bundle.mobile, System.currentTimeMillis());
        }
    }

    private void showSetPassword() {
        String confirmationCode = mEtVerification.getText().toString();
        Pattern continuousNumberPattern = Pattern.compile("(?<![0-9])([0-9]{"
                + 6 + "})(?![0-9])");
        if (continuousNumberPattern.matcher(confirmationCode).matches()) {
            bundle.confirmation = confirmationCode;
            ((IRegisterController) getActivity()).showSubmiter(bundle);
            mSubmitVerificationCode.setEnabled(true);
        } else {
            CToast.showToast(R.string.verfication_error);
        }
    }


    private void initBroadcast() {
        IntentFilter filter = new IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION);
        getActivity().registerReceiver(smsReceiver, filter);
        IntentFilter checkFilter = new IntentFilter();
        checkFilter.addAction(Consts.CHECK_CONFIRMATION);
        Broadcaster.registerReceiver(receiver, checkFilter);
    }

    @Override
    public void onDestroy() {
        try {
            getActivity().unregisterReceiver(smsReceiver);
            Broadcaster.unregisterReceiver(receiver);
            timer.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onDestroy();
    }

    @Override
    public boolean onBackPressed() {
        if (bundle.registerType == IRegisterController.RegisterType.bind) {
            ((HomeActivity) getActivity()).showRegister(bundle);
        } else {
            back();
        }
        return true;
    }

}
