package com.daxiangce123.android.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.monitor.NetworkMonitor;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;

/**
 * default handle {@link Consts#DELETE_FILE}/ {@link Consts#CREATE_FILE} /
 * {@link Consts#UPLOAD_FILE}.<br>
 * Request again when time out appeared.
 * 
 * @project DaXiangCe
 * @time Aug 21, 2014
 * @author ram
 */
public class HttpTimeOutManger {

	private final static String TAG = "HttpTimeOutManger";
	/**
	 * the max repeat count for {@link ErrorCode#TIME_OUT} of a single
	 * {@link ConnectInfo}.<br>
	 * -1 means unlimited
	 */
	private int MAX_REPEAT_COUNT = 10;
	private static boolean DEBUG = true;
	private static HttpTimeOutManger instance;
	/**
	 * {@link ConnectInfo#getUnique()} -> repeated count
	 */
	private Map<String, Integer> repeatMap;
	private TreeSet<String> actionTree;
	private Vector<ConnectCache> cacheList;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Consts.NETWORK_STATE_CHANGED)) {
				handleNetworkChanged(intent);
			} else {
				handleIntent(intent);
			}
		}
	};

	private HttpTimeOutManger() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		repeatMap = new HashMap<String, Integer>();
		actionTree = new TreeSet<String>();
		cacheList = new Vector<ConnectCache>();
	}

	private void unRegistBoadCast() {
		try {
			Broadcaster.unregisterReceiver(receiver);
		} catch (Exception e) {
			e.printStackTrace();
			if (DEBUG) {
				// LogUtil.d(TAG, "unRegistBroadCast	Exception:" +
				// e.getMessage());
			}
		}
	}

	private void reRegistBroadCast() {
		try {
			synchronized (actionTree) {
				IntentFilter ift = new IntentFilter();
				for (String s : actionTree) {
					ift.addAction(s);
					if (DEBUG) {
						// LogUtil.d(TAG, "reRegistBroadCast	" + s);
					}
				}
				Broadcaster.registerReceiver(receiver, ift);
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (DEBUG) {
				// LogUtil.d(TAG, "reRegistBroadCast	Exception:" +
				// e.getMessage());
			}
		}
	}

	/**
	 * the max repeat count for {@link ErrorCode#TIME_OUT} of a single
	 * {@link ConnectInfo}.<br>
	 * <b>-1</b> or <b>0</b> means <b>unlimited</b>
	 */
	public void setMaxRepeatCount(int count) {
		MAX_REPEAT_COUNT = count;
	}

	public static HttpTimeOutManger instance() {
		if (instance == null) {
			instance = new HttpTimeOutManger();
		}
		return instance;
	}

	public void start() {
		actionTree.add(Consts.DELETE_FILE);
		actionTree.add(Consts.CREATE_FILE);
		actionTree.add(Consts.UPLOAD_FILE);
		actionTree.add(Consts.NETWORK_STATE_CHANGED);
		reRegistBroadCast();
	}

	public void stop() {
		unRegistBoadCast();
		repeatMap.clear();
		actionTree.clear();
		if (DEBUG) {
			// LogUtil.d(TAG, "------------------>stop()");
		}
	}

	/**
	 * action will restart when {@link ErrorCode#TIME_OUT}
	 * 
	 * @param action
	 *            see more {@link ConnectInfo#getType()}
	 */
	public void register(String action) {
		try {
			boolean isSuccess = false;
			synchronized (actionTree) {
				isSuccess = actionTree.add(action);
			}
			if (isSuccess) {
				reRegistBroadCast();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * action will restart when {@link ErrorCode#TIME_OUT}
	 * 
	 * @param action
	 *            see more {@link ConnectInfo#getType()}
	 */
	public void register(String action[]) {
		try {
			boolean isSuccess = false;
			synchronized (actionTree) {
				if (action != null) {
					for (String item : action) {
						isSuccess = isSuccess | actionTree.add(item);
					}
				}

			}
			if (isSuccess) {
				reRegistBroadCast();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * unRegister action
	 * 
	 * @param action
	 *            see more {@link ConnectInfo#getType()}
	 */
	public boolean unRegister(String action) {
		boolean isSuccess = false;
		try {
			synchronized (actionTree) {
				isSuccess = actionTree.remove(action);
			}
			if (isSuccess) {
				unRegistBoadCast();
				reRegistBroadCast();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	/**
	 * unRegister action
	 * 
	 * @param action
	 *            see more {@link ConnectInfo#getType()}
	 */
	public boolean unRegister(String action[]) {
		if (action == null || action.length <= 0) {
			return false;
		}
		boolean isSuccess = false;
		try {
			synchronized (actionTree) {
				for (String ac : action) {
					if (ac != null) {
						isSuccess = isSuccess || actionTree.remove(ac);
					}
				}
			}
			if (isSuccess) {
				unRegistBoadCast();
				reRegistBroadCast();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return isSuccess;
	}

	private void handleIntent(Intent intent) {
		try {
			Response response = intent.getParcelableExtra(Consts.RESPONSE);
			ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
			String action = intent.getAction();
			String uuid = info.getUnique();
			if (DEBUG) {
				// LogUtil.d(TAG, "----------------->handleIntent	" +
				// action.toUpperCase());
			}
			if (!needReconnect(response, info)) {
				repeatMap.remove(uuid);
				if (DEBUG) {
					// LogUtil.d(TAG, "NO need to Reconnect");
				}
				return;
			}
			if (!needRepeat(action, response, info)) {
				return;
			}
			reRepuest(action, info, response);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private boolean needReconnect(Response response, ConnectInfo info) {
		if (response == null) {
			return true;
		}
		if (response.getStatusCode() == 200) {
			return false;
		}
		if (UploadCancelManager.sharedInstance().chedkId(info.getFakeId())) {
			return false;
		}
		if (response.getErrCode() == ErrorCode.TIME_OUT || response.getErrCode() == ErrorCode.UNKNOWN || response.getErrCode() == ErrorCode.NETWORK_ERROR) {
			return true;
		}
		return false;
	}

	private void reRepuest(final String action, final ConnectInfo info, final Response response) {

		ConnectManager manager = null;
		if (Consts.DELETE_FILE.equals(action)) {
			manager = RequestManager.sharedInstance();
		} else if (Consts.CREATE_FILE.equals(action)) {
			manager = RequestManager.sharedInstance();
		} else if (Consts.UPLOAD_FILE.equals(action)) {
			manager = HttpUploadManager.instance();
		} else {
			manager = RequestManager.sharedInstance();
		}
		if (manager == null) {
			return;
		}
		if (DEBUG) {
			LogUtil.d(TAG, "---------------------restart------------------");
			LogUtil.d(TAG, "MANAGER:\n" + manager);
			LogUtil.d(TAG, "REQUEST:\n" + info);
			if (response != null) {
				LogUtil.d(TAG, "RESPONSE:\n" + response);
			}
			LogUtil.d(TAG, "---------------------------------------------");
		}
		manager.addConnect(info);

		// TaskRuntime.instance().run(new Runnable() {
		// @Override
		// public void run() {
		// try {
		// Thread.sleep(20);
		// } catch (InterruptedException e) {
		// // TODO Auto-generated catch block
		// e.printStackTrace();
		// }
		//
		// }
		// });

	}

	private boolean needRepeat(String action, Response response, ConnectInfo info) {
		if (action == null || info == null) {
			return false;
		}
		if (response.getErrCode() == ErrorCode.NETWORK_ERROR) {
			NetworkMonitor.updateNetwork();
		}
		if ((!NetworkMonitor.networkConnected())) {
			ConnectCache cache = new ConnectCache(action, info);
			cacheList.add(cache);
			if (DEBUG) {
				LogUtil.d(TAG, "----------------!networkConnected------------------");
				LogUtil.d(TAG, action + "add: cacheList.size()" + cacheList.size());
			}
			return false;
		}
		// if (MAX_REPEAT_COUNT < 0) {
		// return false;
		// }
		// if (response.getErrCode() == ErrorCode.NETWORK_ERROR) {
		// return true;
		// }
		// String uuid = info.getUnique();
		// int repeatCount = 0;
		// Integer rc = repeatMap.get(uuid);
		// if (rc != null) {
		// repeatCount = rc;
		// }
		// if (repeatCount >= MAX_REPEAT_COUNT) {
		// return false;
		// }
		//
		// repeatMap.put(uuid, Integer.valueOf(++repeatCount));
		return true;
	}

	private void handleNetworkChanged(Intent intent) {
		if (NetworkMonitor.networkConnected()) {
			for (ConnectCache item : cacheList) {
				reRepuest(item.action, item.info, null);
			}
			cacheList.clear();
		}
	}

	public class ConnectCache {
		public String action;
		public ConnectInfo info;

		public ConnectCache(String action, ConnectInfo info) {
			this.action = action;
			this.info = info;
		}
	}

}
