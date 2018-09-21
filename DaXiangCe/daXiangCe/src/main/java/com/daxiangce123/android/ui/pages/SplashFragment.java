package com.daxiangce123.android.ui.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.ViewUtil;

import java.io.File;

public class SplashFragment extends BaseFragment {
    static final String TAG = "SplashFragment";
	private View contentView;
    private LinearLayout bg;
    private ImageView adView;
    private File file = null;
    private String SID = null;

    @Override
	public String getFragmentName() {
		return "SplashFragment";
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
        SID = AppData.getSplashId();
		if (contentView == null) {
            file = new File(MediaUtil.getDestSaveDir() + SID + ".png");
            if(!file.exists()){
                contentView = inflater.inflate(R.layout.splash_fragment, container, false);
            }else{
                contentView = inflater.inflate(R.layout.splash_ad_fragment, container, false);
                bg = (LinearLayout) contentView.findViewById(R.id.ad_bg);
                adView = (ImageView) contentView.findViewById(R.id.advertisement);
                showSplash("",SID);
            }
		} else {
			ViewUtil.removeFromParent(contentView);
		}
		return contentView;
	}


    private void showSplash(String bgColor, String sId) {
            //bg.setBackgroundColor(Color.parseColor("#" + bgColor));
            adView.getLayoutParams().height = App.SCREEN_HEIGHT;
            adView.getLayoutParams().width = App.SCREEN_WIDTH;
            ImageManager.instance().loadLocal(adView, MediaUtil.getDestSaveDir() + sId + ".png", null, null);
            adView.setOnClickListener(adClick);
    }

    /**
     * Click start download third-party App
     */
    View.OnClickListener adClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ConnectBuilder.splashClick(AppData.getSplashId(),AppData.getSplashUrl());
            adView.setClickable(false);
        }
    };
}