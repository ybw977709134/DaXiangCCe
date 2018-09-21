package com.daxiangce123.android.push;

import java.util.Date;
import java.util.List;

import android.content.Context;

import com.alibaba.fastjson.JSONObject;
import com.baidu.frontia.api.FrontiaPushMessageReceiver;
import com.daxiangce123.android.App;
import com.daxiangce123.android.push.PushCallBack.Provider;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * <pre>
 * Push消息处理receiver。请编写您需要的回调函数， 一般来说： onBind是必须的，用来处理startWork返回值；
 * onMessage用来接收透传消息； onSetTags、onDelTags、onListTags是tag相关操作的回调；
 * onNotificationClicked在通知被点击时回调； onUnbind是stopWork接口的返回值回调
 * 
 * 返回值中的errorCode，解释如下： 
 *  0 - Success
 *  10001 - Network Problem
 *  30600 - Internal Server Error
 *  30601 - Method Not Allowed 
 *  30602 - Request Params Not Valid
 *  30603 - Authentication Failed 
 *  30604 - Quota Use Up Payment Required 
 *  30605 - Data Required Not Found 
 *  30606 - Request Time Expires Timeout 
 *  30607 - Channel Token Timeout 
 *  30608 - Bind Relation Not Found 
 *  30609 - Bind Number Too Many
 * 
 * 当您遇到以上返回错误时，如果解释不了您的问题，请用同一请求的返回值requestId和errorCode联系我们追查问题。
 * 
 * </pre>
 * 
 * @project DaXiangCe
 * @time Jul 11, 2014
 * @author ram
 */
public class BaiduReceiver extends FrontiaPushMessageReceiver {

	protected final static String TAG = "BaiduReceiver";
	protected static boolean DEBUG = true;
	static {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	@Override
	public void onBind(Context context, int errorCode, String appId, String userId, String channelId, String requestId) {
		if (DEBUG) {
			LogUtil.d(TAG, "onReceive()	UID=" + App.getUid());
			LogUtil.d(TAG, "onBind()	" + new Date() + "	userId:" + userId + "	channelId:" + channelId + "	" + errorCode);
		}
		if (errorCode == 0) {
			PushCenter.instance().bind(userId, channelId, Provider.BAIDU);
		}
	}

	@Override
	public void onDelTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
	}

	@Override
	public void onListTags(Context arg0, int arg1, List<String> arg2, String arg3) {
	}

	/**
	 * <pre>
	 * {
	 *     "title": "daxiangce", 
	 *     "description": "...", 
	 *     "custom_content": {
	 *         json String
	 *     }
	 * }
	 * </pre>
	 */
	@Override
	public void onMessage(Context context, String message, String customContentString) {
		if (App.DEBUG) {
			LogUtil.e(TAG, "message:	" + message + "	custom:" + customContentString);
		}

		if (Utils.isEmpty(message)) {
			return;
		}

		try {
			String jstr = JSONObject.parseObject(message).getString("custom_content");
			PushCenter.instance().OnMessage(jstr, Provider.BAIDU);
		} catch (Exception e) {
			e.printStackTrace();
			LogUtil.d(TAG, "onMessage()	cant parser json");
		}
	}

	@Override
	public void onNotificationClicked(Context arg0, String arg1, String arg2, String arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onSetTags(Context arg0, int arg1, List<String> arg2, List<String> arg3, String arg4) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onUnbind(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub

	}

}
