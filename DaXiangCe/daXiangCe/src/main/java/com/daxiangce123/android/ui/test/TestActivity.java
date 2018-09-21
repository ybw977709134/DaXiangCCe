package com.daxiangce123.android.ui.test;

import java.io.File;

import android.content.Intent;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.wxapi.WXEntryActivity;

public class TestActivity extends BaseCliqActivity implements OnClickListener {
	private LinearLayout contentView;
	private TextView tvResult;
	public final static String TAG = "TestActivity";

	private enum TYPE {
		LOGIN, // album
		ALBUM, // album
		FILE, // file
		ZXING, // file
		LOADIMAGE, // file
		COMPRESS, // file
		DIALOG, // dialog
		BITMAP, //
		NOTIFY, // NOTIFY
		TEST,
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
		// bindView(TYPE.LOGIN);
		// bindView(TYPE.ALBUM);
		// bindView(TYPE.FILE);
		// bindView(TYPE.ZXING);
		// bindView(TYPE.COMPRESS);
		// bindView(TYPE.LOADIMAGE);
		// bindView(TYPE.DIALOG);
		// bindView(TYPE.BITMAP);
		// bindView(TYPE.TEST);
		// bindView(TYPE.NOTIFY);
		TYPE[] tp = TYPE.values();
		for (TYPE type : tp) {
			bindView(type);
		}
		testTimeUtil();

		LogUtil.d("test", "" + (0 ^ 1) + "	" + (1 ^ 2) + "	" + (3 ^ 4));
	}

	private void testTimeUtil() {
		// 2014-08-12T09:36:16.000Z
		// String date = TimeUtil.formatTime("2014-08-12T20:35:15.000Z",
		// Consts.SERVER_UTC_FORMAT, "yyyy/MM/dd HH/mm/ss SSSS");
		// tvResult.setText(date);
		// date = TimeUtil.humanizeDate("2014-08-12T09:36:16.000Z",
		// Consts.SERVER_UTC_FORMAT);
		// tvResult.append("\n");
		// tvResult.append(date);
		//
		// long now = System.currentTimeMillis();
		// long dest = TimeUtil.toLong("2014-08-12T20:37:35.000Z",
		// Consts.SERVER_UTC_FORMAT);
		// date = TimeUtil.formatTime(now, "yyyy/MM/dd HH/mm/ss SSSS");
		// tvResult.append("\n");
		// tvResult.append(date);
		// tvResult.append("\n");
		// tvResult.append("" + now);
		// tvResult.append("\n");
		// tvResult.append("" + dest);
		// tvResult.append("\n");
		// tvResult.append("" + (dest - now) / Consts.HOU_IN_MILLS);
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
			Class<?> cls = null;
			if (type == TYPE.ALBUM) {
				cls = TestAlbumActivity.class;
			} else if (type == TYPE.LOGIN) {
				cls = TestLoginActivity.class;
			} else if (type == TYPE.FILE) {
				cls = TestFilesActivity.class;
			} else if (type == TYPE.ZXING) {
				cls = TestZxingActivity.class;
			} else if (type == TYPE.COMPRESS) {
				compress();
			} else if (type == TYPE.LOADIMAGE) {
				compress();
			} else if (type == TYPE.DIALOG) {
				cls = TestDialogAcitivity.class;
			} else if (type == TYPE.BITMAP) {
				cls = TestBitmapUtilsActivity.class;
			} else if (type == TYPE.NOTIFY) {
				cls = TestNotificationActivity.class;
			} else if (type == TYPE.TEST) {
				cls = WXEntryActivity.class;
			}
			if (cls != null) {
				Intent intent = new Intent();
				intent.setClass(this, cls);
				startActivity(intent);
			}
		}
	}

	private void compress() {
		new AsyncTask<Void, Integer, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				String orgPath = null;
				// orgPath =
				// "/sdcard/test_compress_20140523_112650_samuel_redmi_1.jpg";//
				// orgPath = "/sdcard/test_compress_rain_iphone4s.jpg";//
				orgPath = "/sdcard/test_compress_20140523_112802_jau_sam_s3.jpg";
				// "/sdcard/Pictures/Screenshots/Screenshot_2014-04-16-14-38-13.png";//
				// orgPath = "/sdcard/DCIM/Camera/IMG_20140523_111600.jpg";
				String destPath = "/sdcard/testCompress/" + new File(orgPath).getName();
				BitmapUtil.compress(orgPath, destPath, 640, 160 * 1024, true);
				LogUtil.d(TAG, orgPath + " " + destPath);
				return null;
			}

			protected void onPostExecute(Void result) {

			};

		}.execute();
	}

}
