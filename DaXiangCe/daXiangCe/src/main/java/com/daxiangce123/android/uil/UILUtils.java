package com.daxiangce123.android.uil;

import android.content.Context;
import android.graphics.Bitmap;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.LogUtil;
import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.FadeInBitmapDisplayer;

/**
 * @project auil
 * @time 2014-6-21
 * @author
 */
public class UILUtils {

	private final static String TAG = "UILUtils";
	private static boolean DEBUG = false;
	static {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	// private final static int CACHE_SIZE = (int) (AppUtil.getMaxMemory() *
	// 0.2);

	// public static void initImageLoader(Context context) {
	// // This configuration tuning is custom. You can tune every option, you
	// // may tune some of them,
	// // This configuration tuning is custom. You can tune every option, you
	// // may tune some of them,
	// // or you can create default configuration by
	// // ImageLoaderConfiguration.createDefault(this);
	// // method.
	// // Initialize ImageLoader with configuration.
	// ImageLoader.getInstance().init(getImageLoaderConfiguration(context));
	// }

	public static final ImageLoaderConfiguration.Builder getImageLoaderConfiguration(Context context) {
		if (context == null) {
			return null;
		}
		int size = (int) (AppUtil.getMaxMemory() / 8);
		int screenSize = App.SCREEN_HEIGHT * App.SCREEN_WIDTH;
		int memoryCacheSize = Math.min(screenSize * 4, size);
		if (memoryCacheSize <= 0) {
			memoryCacheSize = size;
		}
		if (App.DEBUG) {
			LogUtil.d(TAG, "getImageLoaderConfiguration	memoryCacheSize=" + memoryCacheSize + " screenSize=" + screenSize + " size=" + size);
		}
		ImageLoaderConfiguration.Builder builder = new ImageLoaderConfiguration.Builder(context)//
				.threadPriority(Thread.NORM_PRIORITY - 2)//
				.denyCacheImageMultipleSizesInMemory()//
				.memoryCacheSize(memoryCacheSize)//
				// .memoryCache(new WeakMemoryCache())
				.diskCacheFileNameGenerator(new Md5FileNameGenerator())//
				.tasksProcessingOrder(QueueProcessingType.FIFO)//
				// threadPoolSize
				.threadPoolSize(Consts.UIL_THREADPOOL_SIZE)//
				.memoryCacheExtraOptions(400, 400)//
				// max size to save in sdcard
				.diskCacheExtraOptions(App.SCREEN_WIDTH, App.SCREEN_HEIGHT, null)//
				.imageDownloader(new ImageDownloader(context));
		if (DEBUG) {
			// writeDebugLogs
			builder.writeDebugLogs();
		}
		return builder;
	}

	public static final DisplayImageOptions.Builder getDiaplayOption() {
		return new DisplayImageOptions.Builder().imageScaleType(ImageScaleType.EXACTLY)//
				// .displayer(new FadeInBitmapDisplayer(100))//
				.cacheInMemory(true)//
				.cacheOnDisk(true)//
				.considerExifParams(true)//
				// .displayer(new FadeInBitmapDisplayer(320))
				.bitmapConfig(Bitmap.Config.RGB_565);
	}

}
