package com.daxiangce123.android.util;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;

import com.daxiangce123.android.Consts;

/**
 * @project Yunio-Android
 * @time 2013-7-16
 * @author ram
 */
public class CapturePic {

	// public final static void capturePic(Fragment fragment, String picName) {
	// java.io.File file = new java.io.File(MediaUtil.getDestSaveDir());
	// if (!file.exists()) {
	// file.mkdirs();
	// }
	// Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	// String p = file.getPath() + "/" + picName;
	// capture.putExtra(MediaStore.EXTRA_OUTPUT,
	// Uri.fromFile(new java.io.File(p))); // 另存图片地址
	// fragment.startActivityForResult(capture,
	// Consts.REQUEST_CODE_CAMERA_IMAGE);
	// }
	//
	// public final static void capturePic(Activity fragment, String picName) {
	// java.io.File file = new java.io.File(MediaUtil.getDestSaveDir());
	// if (!file.exists()) {
	// file.mkdirs();
	// }
	// Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	// String p = file.getPath() + "/" + picName;
	// capture.putExtra(MediaStore.EXTRA_OUTPUT, p);
	// fragment.startActivityForResult(capture,
	// Consts.REQUEST_CODE_CAMERA_IMAGE);
	// }

	public final static void capturePic(Fragment fragment, String destPath) {
		if (Utils.isEmpty(destPath)) {
			return;
		}
		java.io.File file = new java.io.File(destPath);
		if (file == null || file.getParentFile() == null) {
			return;
		}
		Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		capture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); // 另存图片地址
		fragment.startActivityForResult(capture,
				Consts.REQUEST_CODE_CAMERA_IMAGE);
	}

	public final static void capturePic(Activity fragment, String destPath) {
		if (Utils.isEmpty(destPath)) {
			return;
		}
		java.io.File file = new java.io.File(destPath);
		if (file == null || file.getParentFile() == null) {
			return;
		}
		Intent capture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		// capture.putExtra(MediaStore.EXTRA_OUTPUT, destPath);
		capture.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file)); // 另存图片地址
		fragment.startActivityForResult(capture,
				Consts.REQUEST_CODE_CAMERA_IMAGE);
	}

}
