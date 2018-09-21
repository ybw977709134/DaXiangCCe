package com.daxiangce123.android.ui.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

import java.util.List;

public class TestAlbumActivity extends BaseCliqActivity implements OnClickListener {

    private final static String TAG = "TestAlbumActivity";
    private LinearLayout contentView;
    private TextView tvResult;

    private enum TYPE {
        LIST, // list
        CREATE, // create
        DELETE, // delete
        JOIN, // join
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                LogUtil.d(TAG, "action:" + action + "\n" + response);
                if (action.equals(Consts.LIST_ALBUM)) {
                    ListAllAlbums listAllAlbums = Parser
                            .parseAlbumList(response.getContent());
                    String strs = showAlbums(listAllAlbums.getAlbums());
                    tvResult.setText(strs);
                    LogUtil.d(TAG, "albums:\n" + strs);
                    App.getDBHelper().insert(listAllAlbums.getAlbums());
                } else if (action.equals(Consts.CREATE_ALBUM)) {
                    AlbumEntity albumEntity = Parser.parseAlbum(response
                            .getContent());
                    tvResult.setText("action:" + action // action
                            + "\nname:" + albumEntity.getName() // name
                            + "\nnote:" + albumEntity.getNote() // note
                    );
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBroad();
        int padding = 20;
        ScrollView sv = new ScrollView(this);
        tvResult = new TextView(this);
        tvResult.setTextColor(0xff000000);
        contentView = new LinearLayout(this);
        contentView.setOrientation(LinearLayout.VERTICAL);
        contentView.setGravity(Gravity.CENTER);
        contentView.setPadding(padding, padding, padding, padding);
        contentView.addView(tvResult);
        sv.addView(contentView);
        setContentView(sv);
        bindView(TYPE.CREATE);
        bindView(TYPE.JOIN);
        bindView(TYPE.DELETE);
        bindView(TYPE.LIST);
        readDB();
    }

    private void readDB() {
        new Thread() {
            public void run() {
                LogUtil.d(TAG, "reading DB");
                List<AlbumEntity> l = App.getDBHelper().getList(AlbumEntity.EMPTY);
                LogUtil.d(TAG,
                        "From DB:	size=" + (l == null ? "IS NULL" : l.size()));
            }
        }.start();
    }

    private void initBroad() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.LIST_ALBUM);
        ift.addAction(Consts.CREATE_ALBUM);
        Broadcaster.registerReceiver(receiver, ift);
    }

    private void bindView(TYPE type) {
        if (type == null) {
            return;
        }
        TextView tv = new Button(this);
        tv.setGravity(Gravity.CENTER);
        tv.setOnClickListener(this);
        tv.setText("ALBUM " + type.toString());
        tv.setTag(type);

        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        contentView.addView(tv, lp);
    }

    private String showAlbums(List<AlbumEntity> list) {
        if (Utils.isEmpty(list)) {
            return null;
        }
        StringBuffer result = new StringBuffer();
        for (AlbumEntity albumEntity : list) {
            if (albumEntity == null) {
                continue;
            }
            if (result.length() != 0) {
                result.append("\n");
            }
            result.append("------------------------------------------------");
            result.append("\nNAME:" + albumEntity.getName() + " LINK:"
                    + albumEntity.getLink() + "\nID:" + albumEntity.getId());
        }
        return result.toString();
    }

    @Override
    public void onClick(View v) {
        Object obj = v.getTag();
        if (obj instanceof TYPE) {
            if (obj == TYPE.LIST) {
                ConnectBuilder.listAlbum();
            } else if (obj == TYPE.CREATE) {
                JSONObject jo = new JSONObject();
                jo.put(Consts.NAME, "lillian-" + System.currentTimeMillis()
                        + "-" + App.mobileInfo.MANUFACTURER + "-" + App.mobileInfo.DEVICE);
                jo.put(Consts.NOTE,
                        "the very first album-" + System.currentTimeMillis()
                                + "-" + App.mobileInfo.MANUFACTURER + "-" + App.mobileInfo.DEVICE);
                ConnectBuilder.createAlbum(jo.toString());
            } else if (obj == TYPE.DELETE) {

            } else if (obj == TYPE.JOIN) {

            }
        }
    }

    @Override
    protected void onDestroy() {
        Broadcaster.unregisterReceiver(receiver);
        super.onDestroy();
    }
}
