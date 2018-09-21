package com.daxiangce123.android.util;

import android.os.Environment;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.monitor.MediaMonitor;

import java.io.File;

public class MediaUtil {
	public static final String TAG = "MediaUtil";

	public final static void init() {
		String path = getUserDir();
		FileUtil.mkdir(path);

		path = getImageDir();
		FileUtil.mkdir(path);

		path = getVideoDir();
		FileUtil.mkdir(path);

		path = getAudioDir();
		FileUtil.mkdir(path);

		path = getTempDir();
		FileUtil.mkdir(path);

		path = getLogDir();
		FileUtil.mkdir(path);

		path = getDestSaveDir();
		FileUtil.mkdir(path);

        path = getThirdPartyApkPath();
        FileUtil.mkdir(path);

	}

	public static String getAppDir() {
		if (!MediaMonitor.mediaMounted()) {
			LogUtil.e(TAG, "no media mounted!");
			return null;
		}
		File fileDir = Environment.getExternalStorageDirectory();
		// File fileDir = App.getAppContext().getExternalFilesDir("");
		if (fileDir == null) {
			LogUtil.e(TAG, "External Storage ERROR!!!!!!");
			return null;
		}
		if (!fileDir.exists()) {
			LogUtil.e(TAG, "media not exists!");
			return null;
		}
		// String appDir = FileUtil.parent(fileDir);
		String appDir = fileDir.getAbsolutePath() + File.separator + ".daXiangce123";
		return appDir;
	}

	public static String getUserDir() {
		String appDir = getAppDir();
		String userDir = null;
		if (Utils.isEmpty(appDir)) {
			return userDir;
		}

		String uid = App.getUid();
		if (Utils.isEmpty(uid)) {
			LogUtil.e(TAG, "user id can't be empty");
			// return userDir;
			userDir = appDir + File.separator + "visitor";
		} else {

			userDir = appDir + File.separator + uid;
		}
		return userDir;
	}

	public static String getImageDir() {
		String appDir = getUserDir();
		String imageDir = null;
		if (appDir != null) {
			imageDir = appDir + File.separator + "image";
		}
		return imageDir;
	}

	public static String getVideoDir() {
		String appDir = getUserDir();
		String videoDirDir = null;
		if (appDir != null) {
			videoDirDir = appDir + File.separator + "video";
		}
		return videoDirDir;
	}

	public static String getAudioDir() {
		String appDir = getUserDir();
		String audioDir = null;
		if (appDir != null) {
			audioDir = appDir + File.separator + "audio";
		}
		return audioDir;
	}

	public static String getTempDir() {
		String appDir = getAppDir();
		String tempDir = null;
		if (appDir != null) {
			tempDir = appDir + File.separator + "temp";
		}
		return tempDir;
	}

    public static String getThirdPartyApkPath(){
        String ThirdParty = null;
        if(getAppDir() != null){
            ThirdParty = getAppDir() + File.separator + "thirdly";
        }
        return ThirdParty;
    }

	public static String getLogDir() {
		String appDir = getAppDir();
		String logDir = null;
		if (appDir != null) {
			logDir = appDir + File.separator + "log";
		}
		return logDir;
	}

	public static String getDestSaveDir() {
		String appName = App.getAppContext().getString(R.string.app_name);
		String path = getDCIMDir() + File.separator + appName;
		return path;
	}

	/**
	 * for the old version , the is the dest dir for saving
	 * 
	 * @deprecated
	 */
	public static String getDestCliqSaveDir() {
		String path = getDCIMDir() + File.separator + "cliq";
		return path;
	}

	public static String getDCIMDir() {
		try {
			return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
}
