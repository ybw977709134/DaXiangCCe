package com.daxiangce123.android.ui.test;

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

public class TestTimeActivity extends BaseCliqActivity implements OnClickListener {
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
		BITMAP,
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
		bindView(TYPE.LOGIN);
		bindView(TYPE.ALBUM);
		bindView(TYPE.FILE);
		bindView(TYPE.ZXING);
		bindView(TYPE.COMPRESS);
		bindView(TYPE.LOADIMAGE);
		bindView(TYPE.DIALOG);
		bindView(TYPE.BITMAP);
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

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
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
				String orgPath = "/sdcard/DCIM/Camera/IMG_20140402_181746.jpg";// "/sdcard/Pictures/Screenshots/Screenshot_2014-04-16-14-38-13.png";//
				String destPath = "/sdcard/test_compress.jpg";
				BitmapUtil.compress(orgPath, destPath, 640, 160 * 1024, true);
				LogUtil.d(TAG, orgPath + " " + destPath);
				return null;
			}

			protected void onPostExecute(Void result) {

			};

		}.execute();
	}

}
