package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.RegionInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.ArrayList;

public class RegionChooserFragment extends BaseFragment implements OnItemClickListener, OnClickListener {

    private static final String TAG = "MemberFragment";
    private Context mContext;
    private View mRootView, mBack;
    private static ArrayList<RegionInfo> regions = new ArrayList<RegionInfo>();
    private ListView listView;
    private RegionsAdapter regionsAdapter;
    private TextView target;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                String content = response.getContent();
                LogUtil.d(TAG, "response = " + response);
                showRegions(content);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    public TextView getTarget() {
        return target;
    }

    public void setTarget(TextView target) {
        this.target = target;
    }

    private void showRegions(String content) {
        ArrayList<RegionInfo> regionInfos = Parser.parseRegions(content);
        regions = regionInfos;
        if (regionInfos != null) {
            this.regionsAdapter.setRegionInfos(regionInfos);
        }
    }

    public RegionChooserFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        initBroadcast();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.regions_fragment, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        initDate();
        return mRootView;
    }

    public void initComponent() {
        this.regionsAdapter = new RegionsAdapter();
        mBack = mRootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        listView = (ListView) mRootView.findViewById(R.id.lv_regions);
        listView.setAdapter(regionsAdapter);
        listView.setOnItemClickListener(this);
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.LIST_REGION);
        Broadcaster.registerReceiver(receiver, ift);
    }


    @Override
    public String getFragmentName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            back();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
            if (target != null) {
                target.setText(regions.get(position).getCode());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        back();
    }


    private void initDate() {
        if (regions == null || regions.size() == 0) {
            ConnectBuilder.getRegionInfo(0, 100);
        } else {
            regionsAdapter.setRegionInfos(regions);
        }
    }

    private class RegionsAdapter extends BaseAdapter {
        ArrayList<RegionInfo> regionInfos = new ArrayList<RegionInfo>();

        public void setRegionInfos(ArrayList<RegionInfo> regionInfos) {
            if (regionInfos != null) {
                this.regionInfos = regionInfos;
                notifyDataSetChanged();
            }
        }

        @Override
        public int getCount() {
            return regionInfos.size();
        }

        @Override
        public Object getItem(int position) {
            return regionInfos.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            mContext = parent.getContext();
            ViewHolder viewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.region_item, parent, false);
                viewHolder.codeNumber = (TextView) convertView.findViewById(R.id.tv_region_code);
                viewHolder.displayName = (TextView) convertView.findViewById(R.id.tv_region_country_name);
                convertView.setTag(viewHolder);

            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.setData((RegionInfo) getItem(position));
            return convertView;
        }

        class ViewHolder {
            public TextView displayName;
            public TextView codeNumber;

            public void setData(RegionInfo regionInfo) {
                codeNumber.setText(regionInfo.getCode());
                displayName.setText(Utils.getCountryInCode(regionInfo.getRegion()));
            }
        }
    }
}
