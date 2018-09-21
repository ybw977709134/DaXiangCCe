package com.daxiangce123.android.ui.test;

import me.yourbay.barcoder.CapActivity;
import com.daxiangce123.R;
import me.yourbay.barcoder.barcode.ViewfinderView;
import android.graphics.Bitmap;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.daxiangce123.android.util.LogUtil;
import com.google.zxing.Result;

/**
 * @project Cliq
 * @time Mar 5, 2014
 * @author ram
 */
public class TestScannerActivity extends CapActivity {
	private final static String TAG = "TestScannerActivity";

	@Override
	public void init() {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setContentView(R.layout.scanner);
	}

	@Override
	public ViewfinderView bindViewfinderView() {
		return (ViewfinderView) findViewById(R.id.viewfinder_view);
	}

	@Override
	public SurfaceView bindSurfaceView() {
		return (SurfaceView) findViewById(R.id.preview_view);
	}

	@Override
	public void handleResult(Result rawResult, Bitmap barcode) {
		if (barcode != null) {
			((ImageView) findViewById(R.id.barcode_image_view))
					.setImageBitmap(barcode);
			LogUtil.d(TAG, "result:" + rawResult.getText());
		}
	}
}
