package com.daxiangce123.android.manager;

import com.daxiangce123.android.App;
import com.daxiangce123.android.data.Location;
import com.daxiangce123.android.util.LogUtil;

/**
 * @project DaXiangCe
 * @time Jun 11, 2014
 * 
 */
public class LocateManager {

	private final static String TAG = "LocateManager";
	private static LocateManager locateManager = null;
	// private LocationClient mLocationClient = null;
	private int scanDividerTime = 60 * 1000;
	private Location curLocation;
	private static boolean DEBUG = true;
	private boolean isWorking = false;
	// private LocationClientOption option;
	private long lastTimeMillis;

	// private BDLocationListener bdLocationListener = new BDLocationListener() {
	//
	// @Override
	// public void onReceiveLocation(BDLocation location) {
	// if (location == null) {
	// return;
	// }
	// if (DEBUG) {
	// LogUtil.d(TAG, "duration		" + (System.currentTimeMillis() - lastTimeMillis));
	// lastTimeMillis = System.currentTimeMillis();
	// LogUtil.d(TAG, "-------------------curLocation-------------------\n" + curLocation);
	// }
	// curLocation = new Location(location.getLatitude(), location.getLongitude(), (int) location.getRadius(), location.getCity(), "");
	//
	// if (option != null && mLocationClient != null) {
	// option.setScanSpan(scanDividerTime);
	// mLocationClient.setLocOption(option);
	// // option = null;
	// }
	//
	// if (curLocation != null) {
	// Intent intent = new Intent(Consts.ACTION_LOCATED);
	// Broadcaster.sendBroadcast(intent);
	// stop();
	// }
	// }
	//
	// @Override
	// public void onReceivePoi(BDLocation location) {
	//
	// }
	// };

	public LocateManager() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public final static LocateManager instance() {
		if (locateManager == null) {
			locateManager = new LocateManager();
		}
		return locateManager;
	}

	// public final void start() {
	// if (DEBUG) {
	// LogUtil.d(TAG, "------------start working=" + isWorking + "	" + new Date() + "------------");
	// }
	// if (isWorking) {
	// return;
	// }
	// init();
	// if (option == null) {
	// option = new LocationClientOption();
	// // LocationClientOption option = new LocationClientOption();
	// // option.setOpenGps(true);
	// // option.setAddrType("all");
	// // option.setScanSpan(3000);
	// // option.disableCache(true);
	//
	// option.setOpenGps(false);
	// option.setAddrType("all");
	// option.setScanSpan(2 * 1000);
	// option.disableCache(true);
	// // option.setPoiExtraInfo(false);
	// // option.setPriority(LocationClientOption.NetWorkFirst);
	// // option.setPoiNumber(10);
	// lastTimeMillis = System.currentTimeMillis();
	// }
	// mLocationClient.setLocOption(option);
	// mLocationClient.start();
	// isWorking = true;
	//
	// if (DEBUG) {
	// LogUtil.d(TAG, "start()	" + isWorking);
	// }
	// }
	//
	// public final LocationClient getClient() {
	// return mLocationClient;
	// }
	//
	// private final void stopLocate() {
	// if (mLocationClient == null || !isWorking) {
	// return;
	// }
	// isWorking = false;
	// mLocationClient.unRegisterLocationListener(bdLocationListener);
	// mLocationClient.stop();
	// }

	public final void stop() {
		if (App.DEBUG) {
			LogUtil.d(TAG, "------------stop------------");
		}
		// if (locateManager != null) {
		// locateManager.stopLocate();
		// }
	}

	// public static boolean isGpsEnabled(LocationManager locationManager) {
	// boolean isOpenGPS = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
	// boolean isOpenNetwork = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
	// if (isOpenGPS || isOpenNetwork) {
	// return true;
	// }
	// return false;
	// }
	//
	// public Location getLocation() {
	// return curLocation;
	// }
	//
	// private void init() {
	// if (mLocationClient == null) {
	// mLocationClient = new LocationClient(App.getAppContext());
	// }
	// mLocationClient.registerLocationListener(bdLocationListener);
	// mLocationClient.setAK(Consts.BAIDU_API_KEY);
	// }
}
