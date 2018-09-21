package com.daxiangce123.android.ui.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.outh.Oauth;
import com.daxiangce123.android.util.outh.OauthHelper;
import com.daxiangce123.android.util.outh.OauthHelper.onOauthListener;
import com.daxiangce123.android.util.outh.QQHelper;
import com.daxiangce123.android.util.outh.WBHelper;

/**
 * @project Groubum
 * @time Feb 25, 2014
 * @author ram
 */
public class TestLoginActivity extends BaseCliqActivity implements OnClickListener {

	private String TAG = "TestLoginActivity";
	private LinearLayout contentView;
	private TextView tvResult;
	private OauthHelper oauthHelper;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (Consts.SSO_BIND.equals(action)) {
					LogUtil.d(TAG, " action = " + action + "\n");
					LogUtil.d(TAG, "RESPONSE:" + intent.getParcelableExtra(Consts.RESPONSE));
					// token: c71dbbde-d177-4153-aa39-d668cd40e320
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	private onOauthListener oauthListener = new onOauthListener() {

		@Override
		public void onOauthSucceed(Oauth oauth) {
			JSONObject jo = new JSONObject();
			jo.put(Consts.SSO_PROVIDER, oauth.getType());
			jo.put(Consts.TOKEN, oauth.getToken());
			// jo.put(Consts.TOKEN, "2.00aWhf3BvM7V9C08fdbc0d1691ce8C");
			jo.put("device", "sa");
			jo.put(Consts.OS, "android");
			LogUtil.d(TAG, "onOauthSucceed: " + jo.toJSONString());
			ConnectBuilder.sso_bind(jo.toString());
		}

		@Override
		public void onOauthFailed(String provider, Object object) {
			LogUtil.d(TAG, "onOauthFailed: " + provider + " object=" + object);
		}
	};

	private enum TYPE {
		WEIBO, QQ, GOOGLE, FACEBOOK, TWITTER
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		initBroad();

		int padding = 20;
		tvResult = new TextView(this);
		contentView = new LinearLayout(this);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setGravity(Gravity.CENTER);
		contentView.setPadding(padding, padding, padding, padding);
		contentView.addView(tvResult);
		setContentView(contentView);
		bindView(TYPE.WEIBO);
		bindView(TYPE.QQ);
		bindView(TYPE.GOOGLE);
		bindView(TYPE.FACEBOOK);
		bindView(TYPE.TWITTER);
	}

	private void initBroad() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.SSO_BIND);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void bindView(TYPE type) {
		if (type == null) {
			return;
		}
		TextView tv = null;
		if (type == TYPE.FACEBOOK) {
			// tv = new LoginButton(this);
			tv = new Button(this);
		} else {
			tv = new Button(this);
		}
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(this);
		tv.setText(type.toString());
		tv.setTag(type);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		contentView.addView(tv, lp);
	}

	public void onClick(android.view.View v) {
		Object obj = v.getTag();
		if (obj instanceof TYPE) {
			oauthHelper = null;
			if (obj == TYPE.WEIBO) {
				oauthHelper = new WBHelper();
			} else if (obj == TYPE.QQ) {
				oauthHelper = new QQHelper();
			} else if (obj == TYPE.GOOGLE) {
			} else if (obj == TYPE.FACEBOOK) {
				// oauthHelper = new FBHelper();
			} else if (obj == TYPE.TWITTER) {

			}
			if (oauthHelper != null) {
				oauthHelper.setOauthListener(oauthListener);
				oauthHelper.oauth();
			}
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (oauthHelper != null) {
			oauthHelper.onActivityResult(requestCode, resultCode, data);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	protected void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		super.onDestroy();
	}

}
