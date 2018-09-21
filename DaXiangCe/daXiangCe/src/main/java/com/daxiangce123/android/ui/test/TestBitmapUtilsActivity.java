package com.daxiangce123.android.ui.test;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project DaXiangCe
 * @time May 9, 2014
 * @author ram
 */
public class TestBitmapUtilsActivity extends BaseCliqActivity implements
		OnClickListener {

	private boolean isLoading;
	private final String TAG = "TestBitmapUtilsActivity";

	private enum TYPE {
		ROUND, OVERLAY
	}

	private LinearLayout contentView;
	private TextView tvResult;
	private ImageView ivResult;
	private Bitmap mBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		loadImage();
		initView();
		bindView(TYPE.OVERLAY);
		bindView(TYPE.ROUND);
	}

	private void initView() {
		int width = (int) (App.SCREEN_WIDTH * 0.8f);
		int padding = 20;

		ScrollView sv = new ScrollView(this);
		setContentView(sv);

		contentView = new LinearLayout(this);
		sv.addView(contentView);

		contentView.setBackgroundColor(Color.GRAY);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setGravity(Gravity.CENTER);
		contentView.setPadding(padding, padding, padding, padding);

		tvResult = new TextView(this);
		tvResult.setTextColor(0xff000000);
		contentView.addView(tvResult);
		ivResult = new ImageView(this);
		ivResult.setLayoutParams(new LayoutParams(width, width));
		contentView.addView(ivResult);

	}

	private void bindView(TYPE type) {
		if (type == null) {
			return;
		}
		TextView tv = new Button(this);
		tv.setGravity(Gravity.CENTER);
		tv.setOnClickListener(this);
		tv.setText("FILES " + type.toString());
		tv.setTag(type);

		LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT);
		contentView.addView(tv, lp);
	}

	@Override
	public void onClick(View v) {
		if (mBitmap == null || mBitmap.isRecycled()) {
			CToast.showToast("NO BITMAP");
			loadImage();
			return;
		}
		Object obj = v.getTag();
		long start = System.currentTimeMillis();
		Bitmap bitmap = null;
		if (obj == TYPE.OVERLAY) {
			bitmap = BitmapUtil.rotateOverlay(mBitmap);
			CToast.showToast("duration is "
					+ (System.currentTimeMillis() - start));
		} else if (obj == TYPE.ROUND) {
			bitmap = BitmapUtil.toRoundBitmap(mBitmap, mBitmap.getWidth());
		}
		CToast.showToast("duration is " + (System.currentTimeMillis() - start));
		LogUtil.d(TAG, "duration is " + (System.currentTimeMillis() - start));
		start = System.currentTimeMillis();
		if (bitmap != null) {
			ivResult.setScaleType(ScaleType.CENTER_CROP);
			ivResult.setImageBitmap(bitmap);
		}
		LogUtil.d(TAG, "duration2 is " + (System.currentTimeMillis() - start));
	}

	private void loadImage() {
		if (isLoading) {
			return;
		}
		new AsyncTask<Void, Integer, Bitmap>() {

			@Override
			protected Bitmap doInBackground(Void... params) {
				isLoading = true;
				String path = "/sdcard/test_compress_20140523_112802_jau_sam_s3.jpg";
				long start = System.currentTimeMillis();
				int width = (int) (App.SCREEN_WIDTH * 0.8f);
				mBitmap = BitmapUtil.getImageThumbnail(path, width, width);
				LogUtil.d(TAG, "duration is "
						+ (System.currentTimeMillis() - start) + " fileSize = "
						+ FileUtil.size(path));
				return mBitmap;
			}

			protected void onPostExecute(Bitmap result) {
				CToast.showToast("BITMAP OK");
				isLoading = false;
				ivResult.setImageBitmap(mBitmap);
			};
		}.execute();
	}

}
