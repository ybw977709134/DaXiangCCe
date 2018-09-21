package com.daxiangce123.android.ui.view;

import android.content.Context;

import com.daxiangce123.android.App;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project DaXiangCe
 * @time Jun 30, 2014
 * @author ram
 */
public class SamplePreview extends PhotoPreview {

	protected static String TAg = "SamplePreview";
	private TempToken token;

	public SamplePreview(Context context) {
		super(context);
	}

	public void setTempToken(TempToken tempToken) {
		this.token = tempToken;
	}

	@Override
	protected void downloadGif(String path) {
		if (token == null || fileEntity == null) {
			return;
		}
		ConnectBuilder.downloadFile(fileEntity.getId(), token.getToken(), path, fileEntity.getSize(), null);
		// ConnectBuilder.downloadFileCount(fileEntity.getId(), path,
		// token.getToken(),
		// fileEntity.getSize());
		// ConnectBuilder.downloadSample(file.getId(), path, token.getToken(),
		// file.getSize());
	}

	@Override
	protected void loadImage() {
		if (App.DEBUG) {
			LogUtil.d(TAg, "-------------------------------------------------loadImage()\n" + fileEntity + "\n----------token" + token);
		}
		if (token == null || fileEntity == null) {
			return;
		}
		if (App.DEBUG) {
			LogUtil.d(TAg, "loadImage()	file=" + fileEntity.getId() + "	" + token.getToken());
		}
		// ImageManager.instance().loadSampleImage(zImageView,
		// fileEntity.getId(),
		// token.getToken(), pictureSize, null, loadingListener,
		// progressListener);
	}

}
