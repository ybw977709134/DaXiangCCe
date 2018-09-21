package com.daxiangce123.android.ui.test;

import java.util.List;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project Groubum
 * @time Mar 3, 2014
 * @author ram
 */
public class TestFilesActivity extends BaseCliqActivity implements
		OnClickListener {

	private final static String TAG = "TestFilesActivity";
	private LinearLayout contentView;
	private TextView tvResult;
	private ImageViewEx ivResult;
	private String fileId = "0012fb23-2622-45a4-ac24-c83a2af300a8";
	private String albumId;
	private ImageSize size;
	private long startInMills;

	private enum TYPE {
		UPLOAD, DELETE, DOWNLOAD, THUMB, AVATER, VIDEO, LOAD
	}

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				LogUtil.d(TAG, "action:" + action);
				if (action.equals(Consts.DOWNLOAD_FILE)) {
					long duration = System.currentTimeMillis() - startInMills;
					LogUtil.d(TAG, "DOWNLOAD duration:" + duration);
				} else {
					Response response = intent
							.getParcelableExtra(Consts.RESPONSE);
					if (action.equals(Consts.UPLOAD_FILE)) {
						JSONObject jo = JSONObject.parseObject(response
								.getContent());
						fileId = jo.getString(Consts.ID);
					} else if (action.equals(Consts.CREATE_FILE)) {
						JSONObject jo = JSONObject.parseObject(response
								.getContent());
						fileId = jo.getString(Consts.ID);
					} else if (action.equals(Consts.DOWNLOAD_FILE)) {

					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	protected void onCreate(android.os.Bundle savedInstanceState) {
		int width = (int) (App.SCREEN_WIDTH * 0.8f);
		size = new ImageSize(0, 0);
		super.onCreate(savedInstanceState);
		LogUtil.d(TAG, "" + App.SCREEN_WIDTH + " x " + App.SCREEN_HEIGHT);
		initBroad();
		int padding = 20;
		ScrollView sv = new ScrollView(this);
		contentView = new LinearLayout(this);
		contentView.setOrientation(LinearLayout.VERTICAL);
		contentView.setGravity(Gravity.CENTER);
		contentView.setPadding(padding, padding, padding, padding);

		tvResult = new TextView(this);
		tvResult.setTextColor(0xff000000);
		ivResult = new ImageViewEx(this);
		ivResult.setLayoutParams(new LayoutParams(width, width));

		contentView.addView(ivResult);
		contentView.addView(tvResult);
		sv.addView(contentView);
		setContentView(sv);

		bindView(TYPE.LOAD);
		bindView(TYPE.UPLOAD);
		bindView(TYPE.DOWNLOAD);
		bindView(TYPE.DELETE);
		bindView(TYPE.THUMB);
		bindView(TYPE.AVATER);
		bindView(TYPE.VIDEO);
		readDB();
	}

	private void initBroad() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.UPLOAD_FILE);
		ift.addAction(Consts.CREATE_FILE);
		ift.addAction(Consts.DOWNLOAD_FILE);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void readDB() {
		new Thread() {
			public void run() {
				DBHelper dbHelper = App.getDBHelper();
				if (dbHelper != null) {
					List<AlbumEntity> l = dbHelper.getList(AlbumEntity.EMPTY);
					LogUtil.d(TAG, "From DB:	size="
							+ (l == null ? "IS NULL" : l.size()));
					if (Utils.isEmpty(l)) {
						return;
					}
					albumId = l.get(0).getId();
					LogUtil.d(TAG, "From DB:	albumId=" + albumId);
				}
			}
		}.start();
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

	@SuppressLint("SdCardPath")
	@Override
	public void onClick(View v) {
		Object obj = v.getTag();
		if (obj instanceof TYPE) {
			if (obj == TYPE.UPLOAD) {
				if (albumId == null) {
					albumId = "375ec243-66a1-4413-91f7-277e1944c53a";
				}
				ImageManager.instance()
						.uploadImage("/sdcard/test.png", albumId);
				// Thomas:2ffa6600-b780-4683-b1e8-26b7f397af7d
				// //de4327c0-b4aa-4750-ab7d-cc496e372692
			} else if (obj == TYPE.DOWNLOAD) {
				// String parent = "/sdcard/test";
				// File f = new File(parent);
				// if (!f.exists()) {
				// f.mkdir();
				// }
				// ConnectBuilder.downloadFileCount(fileId,
				// parent + "/" + System.currentTimeMillis(), 0);
				startInMills = System.currentTimeMillis();
				// Bitmap bitmap = ImageManager.instance().getImage(ivResult,
				// fileId, size);
				// if (bitmap != null) {
				// ivResult.setImageBitmap(bitmap);
				// }
			} else if (obj == TYPE.DELETE) {

			} else if (obj == TYPE.THUMB) {
				ImageSize imageSize = new ImageSize(180, 180);
				imageSize.setThumb(true);
				// Bitmap bitmap = ImageManager.instance().getImage(ivResult,
				// fileId, imageSize);
				// if (bitmap != null) {
				// ivResult.setImageBitmap(bitmap);
				// }
			} else if (obj == TYPE.AVATER) {
				String userId = "48710ab1-b4c5-4c95-b853-d0ee9009d041";
				ImageSize imageSize = new ImageSize(180, 180);
				imageSize.setThumb(true);
				ivResult.setImageBitmap(null);
				ImageManager.instance().loadAvater(ivResult, userId);
			} else if (obj == TYPE.VIDEO) {
				Intent intent = new Intent();
				intent.setClass(this, VideoPlayerActivity.class);
				startActivity(intent);
			} else if (obj == TYPE.LOAD) {
				loadLocal();
				// fileId = "0403-9da6aba7-57ce-4410-beb8-8e8ba03e5dd2";
				// ImageSize imageSize = new ImageSize(864, 1420);
				// imageSize.setThumb(false);
				// Bitmap bitmap = ImageManager.instance().getImage(fileId,
				// imageSize);
				// ivResult.setImageBitmap(bitmap);
			}
		}
	}

	private void loadLocal() {
		new AsyncTask<Void, Integer, Bitmap>() {

			protected Bitmap doInBackground(Void[] params) {
				String path;
				// path =
				// "/sdcard/Android/data/com.daxiangce123/0081-d9613ea8-48a7-4a79-a07b-4441f77032e9/image/0081-7e9e6594-0ab8-466c-90d7-75ef36d4476f_460x460";
				path = "/sdcard/0081-7e9e6594-0ab8-466c-90d7-75ef36d4476f_460x460.png";
				// Bitmap bitmap = BitmapUtil.getBitmap(path, 460, 460);
				Bitmap bitmap = BitmapUtil.getImageThumbnail(path, 460, 460);
				return bitmap;
			};

			protected void onPostExecute(Bitmap bitmap) {
				ivResult.setImageBitmap(bitmap);
				CToast.showToast("bitmap:"
						+ (bitmap != null ? bitmap.getWidth() + "x"
								+ bitmap.getHeight() : " is null"));
				LogUtil.d(TAG,
						"bitmap:"
								+ (bitmap != null ? bitmap.getWidth() + "x"
										+ bitmap.getHeight() : " is null"));
			};
		}.execute();
	}

	@Override
	protected void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		super.onDestroy();
	}

}
