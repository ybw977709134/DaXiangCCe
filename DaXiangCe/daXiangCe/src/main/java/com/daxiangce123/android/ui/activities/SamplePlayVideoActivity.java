package com.daxiangce123.android.ui.activities;

import android.os.Bundle;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.VideoPlayerFragment;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;

public class SamplePlayVideoActivity extends BaseCliqActivity {

	protected final static String TAG = "SamplePlayVideoActivity";
	private String token;
	private FileEntity file;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_empty);
		// App.addActivity(this);
		try {
			Bundle bundle = this.getIntent().getExtras();
			if (bundle != null) {
				file = bundle.getParcelable(Consts.FILE);
				token = bundle.getString(Consts.ACCESS_TOKEN);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (!play()) {
			finish();
		}
	}

	private boolean play() {
		if (file == null) {
			return false;
		}
		if (Mime.VID != MimeTypeUtil.getMime(file.getMimeType())) {
			return false;
		}
		VideoPlayerFragment playerFragment = new VideoPlayerFragment();
		playerFragment.setFileEntity(file);
		playerFragment.setTempToken(token);
		playerFragment.show(this);
		if (App.DEBUG) {
			LogUtil.d(TAG, "--------------------------------playVideo()!!!!");
			LogUtil.d(TAG, "	" + file);
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
