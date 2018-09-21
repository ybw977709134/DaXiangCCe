package me.yourbay.barcoder;

import me.yourbay.barcoder.barcode.ViewfinderView;
import android.graphics.Bitmap;
import android.view.SurfaceView;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.google.zxing.Result;

public class Scanner extends CapActivity {

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
		}
	}
}
