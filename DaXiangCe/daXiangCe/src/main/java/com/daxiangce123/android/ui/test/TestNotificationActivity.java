package com.daxiangce123.android.ui.test;

import java.util.Date;

import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.manager.NotifyManager;
import com.daxiangce123.android.ui.activities.base.BaseActivity;
import com.daxiangce123.android.util.NotificationBuilder;

/**
 * @project DaXiangCe
 * @time Sep 3, 2014
 * @author ram
 */
public class TestNotificationActivity extends BaseActivity implements OnClickListener {
	private LinearLayout contentView;
	private TextView tvResult;
	public final static String TAG = "TestActivity";

	private enum TYPE {
		DEFAULT, // album
		VIRBATE_ON, // album
		VIRBATE_OFF, // album
		SOUND_ON, // file
		SOUND_OFF, // file
		LIGHT_ON, // file
		LIGHT_OFF, // file
	}

	protected void onCreate(android.os.Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int padding = 20;
		ScrollView sv = new ScrollView(this);
		tvResult = new TextView(this);
		tvResult.setTextColor(0xff000000);
		contentView = new LinearLayout(this);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setGravity(Gravity.CENTER);
		contentView.setPadding(padding, padding, padding, padding);
		contentView.addView(tvResult);
		sv.addView(contentView);
		setContentView(sv);
		TYPE[] tp = TYPE.values();
		for (TYPE type : tp) {
			bindView(type);
		}
	}

	private void bindView(TYPE type) {
		if (type == null) {
			return;
		}
		TextView tv = new Button(this);
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(this);
		tv.setText(type.toString());
		tv.setTag(type);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		contentView.addView(tv, lp);
	}

	@Override
	public void onClick(View v) {

		if (v.getTag() instanceof TYPE) {
			TYPE type = (TYPE) v.getTag();
			boolean isVibrate = false;
			boolean isSound = false;
			boolean isLight = false;
			if (type == TYPE.VIRBATE_OFF) {
			} else if (type == TYPE.VIRBATE_ON) {
				isVibrate = true;
			} else if (type == TYPE.SOUND_OFF) {
			} else if (type == TYPE.SOUND_ON) {
				isSound = true;
			} else if (type == TYPE.LIGHT_OFF) {
			} else if (type == TYPE.LIGHT_ON) {
				isLight = true;
			}
			NotificationBuilder nb = new NotificationBuilder(App.getAppContext()).setContent("Content " + new Date()).setTitle("Notification").setIntent(getIntent()).setNotificationId(1234).setSound(
					isSound).setVibrate(isVibrate).setLight(isLight);
			NotifyManager.instance().showNotification(nb);
		}
	}

}
