package com.daxiangce123.wxapi;

import android.content.Intent;
import android.os.Bundle;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.outh.WXHelper;

/**
 * @author ram
 * @project DaXiangCe
 * @time Aug 11, 2014
 */
public class WXEntryActivity extends BaseCliqActivity {

    public static final String TAG = "WXEntryActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onCreate");
        }

        // No matter is canceled or not, wechat will call this activity!!!!!!
        if (!hasLogined()) {
            Intent intent = getIntent();
            intent.setAction(Consts.LOGIN_WX_SUCCEED);
            Broadcaster.sendBroadcast(intent);
        } else {
            if (App.wxHelper != null) {
                Intent intent = getIntent();
                intent.setAction(Consts.LOGIN_WX_SUCCEED);
                Broadcaster.sendBroadcast(intent);
                App.wxHelper = null;
            } else {
                WXHelper helper = new WXHelper();
                helper.handleIntent(getIntent());
            }

        }
        finish();
        overridePendingTransition(0, 0);
    }

    private boolean hasLogined() {
        String session = AppData.getToken();
        if (Utils.isEmpty(session)) {
            return false;
        }
        return true;
    }

}
