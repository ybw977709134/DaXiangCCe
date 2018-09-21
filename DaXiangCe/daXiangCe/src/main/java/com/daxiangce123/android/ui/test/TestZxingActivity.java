package com.daxiangce123.android.ui.test;

import java.util.Date;

import me.yourbay.barcoder.Generator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.activities.ZXingActivity;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project Cliq
 * @time Mar 5, 2014
 * @author ram
 */
public class TestZxingActivity extends BaseCliqActivity implements OnClickListener {
	private final static String TAG = "TestZxingActivity";
	private LinearLayout contentView;
	private TextView tvResult;
	private ImageView ivResult;

	private enum TYPE {
		SCAN, GENERATE
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		int padding = 20;
		ScrollView sv = new ScrollView(this);
		contentView = new LinearLayout(this);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setGravity(Gravity.CENTER);
		contentView.setPadding(padding, padding, padding, padding);

		tvResult = new TextView(this);
		tvResult.setTextColor(0xff000000);
		ivResult = new ImageView(this);
		contentView.addView(ivResult);
		contentView.addView(tvResult);
		sv.addView(contentView);
		setContentView(sv);

		bindView(TYPE.SCAN);
		bindView(TYPE.GENERATE);
	}

	private void bindView(TYPE type) {
		if (type == null) {
			return;
		}
		TextView tv = new Button(this);
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(this);
		tv.setText("ZXING " + type.toString());
		tv.setTag(type);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		contentView.addView(tv, lp);
	}

	@Override
	public void onClick(View v) {
//		Object obj = v.getTag();
//		if (obj == TYPE.SCAN) {
//			Intent intent = new Intent();
//			intent.setClass(this, ZXingActivity.class);
//			startActivityForResult(intent, Consts.REQUEST_CODE_ZXING);
//		} else if (obj == TYPE.GENERATE) {
//			Bitmap bitmap = Generator.encode("" + new Date() + "\nBRAND:"
//					+ App.BRAND + "\nMANUFACTURER:" + App.MANUFACTURER
//					+ "\nMODEL:" + App.MODEL + "\nPRODUCT:" + App.PRODUCT,
//					(int) (App.SCREEN_WIDTH * 0.8f));
//			if (bitmap != null) {
//				ivResult.setImageBitmap(bitmap);
//			}
//		}
    }

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK) {
			if (requestCode == Consts.REQUEST_CODE_ZXING) {
				try {
					if (!data.hasExtra(Consts.ZXING_RESULT)) {
						return;
					}
					String result = data.getStringExtra(Consts.ZXING_RESULT);
					tvResult.setText("SCAN RESULT:\n" + result);
					LogUtil.d(TAG, "ZXING_RESULT:" + result);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
