package me.yourbay.barcoder;

import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;

import me.yourbay.barcoder.barcode.CaptureActivityHandler;
import me.yourbay.barcoder.barcode.Hyb;
import me.yourbay.barcoder.barcode.InactivityTimer;
import me.yourbay.barcoder.barcode.IntentSource;
import me.yourbay.barcoder.barcode.ViewfinderView;
import me.yourbay.barcoder.barcode.camera.CameraManager;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.google.zxing.ResultPoint;

public abstract class CapActivity extends Activity implements
		SurfaceHolder.Callback {

	private CameraManager cameraManager;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private IntentSource source;
	private InactivityTimer inactivityTimer;
	private CaptureActivityHandler handler;
	private Collection<BarcodeFormat> decodeFormats = EnumSet
			.of(BarcodeFormat.QR_CODE);
	private String characterSet;
	private Result lastResult;
	/**
	 * IADD
	 */
	private SurfaceView surfaceView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onCreate(savedInstanceState);
		hasSurface = false;
		inactivityTimer = new InactivityTimer(this);
		source = IntentSource.NONE;
	}

	/**
	 * 准备在onCreate里面完成的东西全部在这里面完成
	 * 
	 * @time 2012-12-11下午1:21:59
	 * @author hyongbai|韩西 hyongbai@gmail.com
	 * 
	 */
	public abstract void init();

	private void initUI() {
		init();
		viewfinderView = bindViewfinderView();
		cameraManager = new CameraManager(this);
		viewfinderView.setCameraManager(cameraManager);
		surfaceView = bindSurfaceView();
	}

	public abstract ViewfinderView bindViewfinderView();

	public abstract SurfaceView bindSurfaceView();

	@Override
	protected void onResume() {
		super.onResume();
		initUI();
		handler = null;
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		inactivityTimer.onResume();
		lastResult = null;
	}

	@Override
	protected void onPause() {
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		inactivityTimer.onPause();
		cameraManager.closeDriver();
		if (!hasSurface) {
			SurfaceHolder surfaceHolder = surfaceView.getHolder();
			surfaceHolder.removeCallback(this);
		}
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if ((source == IntentSource.NONE || source == IntentSource.ZXING_LINK)
					&& lastResult != null) {
				restartPreviewAfterDelay(0L);
				return true;
			}
			break;
		case KeyEvent.KEYCODE_FOCUS:
		case KeyEvent.KEYCODE_CAMERA:
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			cameraManager.setTorch(false);
			return true;
		case KeyEvent.KEYCODE_VOLUME_UP:
			cameraManager.setTorch(true);
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public SurfaceView getSurfaceView() {
		return surfaceView;
	}

	// public void shutDown() {
	// inactivityTimer.shutdown();
	// surfaceView.setVisibility(View.GONE);
	// cameraManager.closeDriver();
	// }

	// --------------------------------------------------------------//

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public CameraManager getCameraManager() {
		return cameraManager;
	}

	/**
	 * A valid barcode has been found, so give an indication of success and show
	 * the results.
	 * 
	 * @param rawResult
	 *            The contents of the barcode.
	 * @param barcode
	 *            A greyscale bitmap of the camera data which was decoded.
	 */
	public void handleDecode(Result rawResult, Bitmap barcode) {
		inactivityTimer.onActivity();
		lastResult = rawResult;
		boolean fromLiveScan = barcode != null;
		if (fromLiveScan) {
			drawResultPoints(barcode, rawResult);
		}
		handleDecodeInternally(rawResult, barcode);
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();
	}

	// --------------------------------------------------------------//

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		if (holder == null) {
			Hyb.log(this.getClass(),
					"*** WARNING *** surfaceCreated() gave us a null surface!");
		}
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		hasSurface = false;
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {

	}

	// --------------------------------------------------------------//

	private void initCamera(SurfaceHolder surfaceHolder) {
		if (surfaceHolder == null) {
			throw new IllegalStateException("No SurfaceHolder provided");
		}
		if (cameraManager.isOpen()) {
			return;
		}
		try {
			cameraManager.openDriver(surfaceHolder);
			if (handler == null) {
				handler = new CaptureActivityHandler(this, decodeFormats,
						characterSet, cameraManager);
			}
		} catch (IOException ioe) {
		} catch (RuntimeException e) {
		}
	}

	private void drawResultPoints(Bitmap barcode, Result rawResult) {
		ResultPoint[] points = rawResult.getResultPoints();
		if (points != null && points.length > 0) {
			Canvas canvas = new Canvas(barcode);
			Paint paint = new Paint();
			paint.setColor(0xc099cc00);
			if (points.length == 2) {
				paint.setStrokeWidth(4.0f);
				drawLine(canvas, paint, points[0], points[1]);
			} else if (points.length == 4
					&& (rawResult.getBarcodeFormat() == BarcodeFormat.UPC_A || rawResult
							.getBarcodeFormat() == BarcodeFormat.EAN_13)) {
				// Hacky special case -- draw two lines, for the barcode and
				// metadata
				drawLine(canvas, paint, points[0], points[1]);
				drawLine(canvas, paint, points[2], points[3]);
			} else {
				paint.setStrokeWidth(10.0f);
				for (ResultPoint point : points) {
					canvas.drawPoint(point.getX(), point.getY(), paint);
				}
			}
		}
	}

	private static void drawLine(Canvas canvas, Paint paint, ResultPoint a,
			ResultPoint b) {
		canvas.drawLine(a.getX(), a.getY(), b.getX(), b.getY(), paint);
	}

	// Put up our own UI for how to handle the decoded contents.
	// private void handleDecodeInternally(Result rawResult,
	// ResultHandler resultHandler, Bitmap barcode) {
	private void handleDecodeInternally(Result rawResult, Bitmap barcode) {
		viewfinderView.setVisibility(View.GONE);
		handleResult(rawResult, barcode);
	}

	/**
	 * 解码之后返回的结果
	 * 
	 * @time 2012-12-11下午1:14:14
	 * @author hyongbai|韩西 hyongbai@gmail.com
	 * 
	 * @param rawResult
	 * @param barcode
	 */
	public abstract void handleResult(Result rawResult, Bitmap barcode);

	private void restartPreviewAfterDelay(long delayMS) {
		if (handler != null) {
			handler.sendEmptyMessageDelayed(Hyb.MESSAGE_ID_RESTART_PREVIEW,
					delayMS);
		}
		resetStatusView();
	}

	private void resetStatusView() {
		viewfinderView.setVisibility(View.VISIBLE);
		lastResult = null;
	}
}
