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

public class TestCreatAlbum extends BaseCliqActivity implements OnClickListener {
	private static final String TAG = "TestCreatAlbum";

	private LinearLayout contentView;
	private TextView tvResult;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			try {
				String action = intent.getAction();
				if (Consts.CREATE_ALBUM.equals(action)) {
					LogUtil.d(TAG, " action = " + action + "\n");
					LogUtil.d(
							TAG,
							"RESPONSE:"
									+ intent.getParcelableExtra(Consts.RESPONSE));
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

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
		bindView("xinjian");

	}

	private void bindView(String str) {
		TextView tv = new Button(this);
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(this);
		tv.setText(str);
		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		contentView.addView(tv, lp);
	}

	private void initBroad() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.CREATE_ALBUM);
		Broadcaster.registerReceiver(receiver, ift);
	}

	public void onClick(android.view.View v) {
		JSONObject jo = new JSONObject();
		jo.put(Consts.NAME, "lillian");
		jo.put(Consts.NOTE, "the very first album");
		ConnectBuilder.createAlbum(jo.toString());
	};

	@Override
	protected void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		super.onDestroy();
	}

}
