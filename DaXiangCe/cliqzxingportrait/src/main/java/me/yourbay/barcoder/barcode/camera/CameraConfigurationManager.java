/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.yourbay.barcoder.barcode.camera;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.yourbay.barcoder.barcode.Hyb;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * A class which deals with reading, parsing, and setting the camera parameters
 * which are used to configure the camera hardware.
 */
final class CameraConfigurationManager {

	// This is bigger than the size of a small screen, which is still supported.
	// The routine
	// below will still select the default (presumably 320x240) size for these.
	// This prevents
	// accidental selection of very low resolution on some devices.
	// private static final int MIN_PREVIEW_PIXELS = 470 * 320; // normal
	// screen/
	private static final int MIN_PREVIEW_PIXELS = 310 * 240; // normal screen
	private static final int MAX_PREVIEW_PIXELS = 1280 * 720;

	private final Context context;
	private Point screenResolution;
	private Point cameraResolution;

	CameraConfigurationManager(Context context) {
		this.context = context;
	}

	/**
	 * Reads, one time, values from the camera that are needed by the app.
	 */
	void initFromCameraParameters(Camera camera) {
		Camera.Parameters parameters = camera.getParameters();
		WindowManager manager = (WindowManager) context
				.getSystemService(Context.WINDOW_SERVICE);
		DisplayMetrics metrics = new DisplayMetrics();
		manager.getDefaultDisplay().getMetrics(metrics);
		int width = metrics.widthPixels;
		int height = metrics.heightPixels;
		screenResolution = new Point(width, height);
		log("\n\n\n\n");
		log("Screen resolution: " + screenResolution);
		cameraResolution = findBestPreviewSizeValue(parameters,
				screenResolution);
		log("Camera resolution: " + cameraResolution);
	}

	void setDesiredCameraParameters(Camera camera, boolean safeMode) {
		/**
		 * TO PORTRAIT START
		 */
		camera.setDisplayOrientation(90);
		/**
		 * TO PORTRAIT END
		 */
		Camera.Parameters parameters = camera.getParameters();
		if (parameters == null) {
			log("Device error: no camera parameters are available. Proceeding without configuration.");
			return;
		}
		// log("Initial camera parameters: " + parameters.flatten());
		if (safeMode) {
			log("In camera config safe mode -- most settings will not be honored");
		}
		initializeTorch(parameters, safeMode);
		String focusMode = null;
		if (safeMode) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					Camera.Parameters.FOCUS_MODE_AUTO);
		} else {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					"continuous-picture", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE
											// in 4.0+
					"continuous-video", // Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO
										// in 4.0+
					Camera.Parameters.FOCUS_MODE_AUTO);
		}
		// Maybe selected auto-focus but not available, so fall through here:
		if (!safeMode && focusMode == null) {
			focusMode = findSettableValue(parameters.getSupportedFocusModes(),
					Camera.Parameters.FOCUS_MODE_MACRO, "edof"); // Camera.Parameters.FOCUS_MODE_EDOF
																	// in 2.2+
		}
		if (focusMode != null) {
			parameters.setFocusMode(focusMode);
		}

		parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);
		camera.setParameters(parameters);
	}

	Point getCameraResolution() {
		return cameraResolution;
	}

	Point getScreenResolution() {
		return screenResolution;
	}

	void setTorch(Camera camera, boolean newSetting) {
		Camera.Parameters parameters = camera.getParameters();
		doSetTorch(parameters, newSetting, false);
		camera.setParameters(parameters);
	}

	private void initializeTorch(Camera.Parameters parameters, boolean safeMode) {
		doSetTorch(parameters, false, safeMode);
	}

	private void doSetTorch(Camera.Parameters parameters, boolean newSetting,
			boolean safeMode) {
		String flashMode;
		if (newSetting) {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_TORCH,
					Camera.Parameters.FLASH_MODE_ON);
		} else {
			flashMode = findSettableValue(parameters.getSupportedFlashModes(),
					Camera.Parameters.FLASH_MODE_OFF);
		}
		if (flashMode != null) {
			parameters.setFlashMode(flashMode);
		}

	}

	private Point findBestPreviewSizeValue(Camera.Parameters parameters,
			Point screenResolution) {

		List<Camera.Size> rawSupportedSizes = parameters
				.getSupportedPreviewSizes();
		if (rawSupportedSizes == null) {
			log("Device returned no supported preview sizes; using default");
			Camera.Size defaultSize = parameters.getPreviewSize();
			return new Point(defaultSize.width, defaultSize.height);
		}

		// Sort by size, descending
		List<Camera.Size> supportedPreviewSizes = new ArrayList<Camera.Size>(
				rawSupportedSizes);
		Collections.sort(supportedPreviewSizes, new Comparator<Camera.Size>() {
			@Override
			public int compare(Camera.Size a, Camera.Size b) {
				int aPixels = a.height * a.width;
				int bPixels = b.height * b.width;
				if (bPixels < aPixels) {
					return -1;
				}
				if (bPixels > aPixels) {
					return 1;
				}
				return 0;
			}
		});

		Point bestSize = null;
		float screenAspectRatio = (float) screenResolution.x
				/ (float) screenResolution.y;
		/**
		 * TO PORTRAIT START
		 */
		boolean isCandidatePortrait = screenAspectRatio < 1;
		/**
		 * TO PORTRAIT END
		 */
		float diff = Float.POSITIVE_INFINITY;
		for (Camera.Size supportedPreviewSize : supportedPreviewSizes) {
			int realWidth = supportedPreviewSize.width;
			int realHeight = supportedPreviewSize.height;
			int pixels = realWidth * realHeight;
			if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
				continue;
			}
			int maybeFlippedWidth = isCandidatePortrait ? realHeight
					: realWidth;
			int maybeFlippedHeight = isCandidatePortrait ? realWidth
					: realHeight;
			if (maybeFlippedWidth == screenResolution.x
					&& maybeFlippedHeight == screenResolution.y) {
				Point exactPoint = new Point(realWidth, realHeight);
				log("Found preview size exactly matching screen size: "
						+ exactPoint);
				return exactPoint;
			}
			float aspectRatio = (float) maybeFlippedWidth
					/ (float) maybeFlippedHeight;
			float newDiff = Math.abs(aspectRatio - screenAspectRatio);
			if (newDiff < diff) {
				bestSize = new Point(realWidth, realHeight);
				diff = newDiff;
			}
		}

		if (bestSize == null) {
			Camera.Size defaultSize = parameters.getPreviewSize();
			bestSize = new Point(defaultSize.width, defaultSize.height);
			log("No suitable preview sizes, using default: " + bestSize);
		}

		log("Found best approximate preview size: " + bestSize);
		return bestSize;
	}

	private static String findSettableValue(Collection<String> supportedValues,
			String... desiredValues) {
		// Hyb.log("CameraConfigurationManager-findSettableValue-Supported values: "
		// + supportedValues);
		String result = null;
		if (supportedValues != null) {
			for (String desiredValue : desiredValues) {
				if (supportedValues.contains(desiredValue)) {
					result = desiredValue;
					break;
				}
			}
		}
		// Hyb.log("CameraConfigurationManager-findSettableValue-Settable value: "
		// + result);
		return result;
	}

	private void log(String msg) {
		Hyb.log(this.getClass(), msg);
	}

}
