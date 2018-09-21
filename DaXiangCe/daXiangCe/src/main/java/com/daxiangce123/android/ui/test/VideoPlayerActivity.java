package com.daxiangce123.android.ui.test;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.VideoPlayerFragment;

/**
 * @project DaXiangCe
 * @time May 4, 2014
 * @author ram
 */
public class VideoPlayerActivity extends BaseCliqActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setId(R.id.fragment_content);
		setContentView(ll);

		VideoPlayerFragment playerFragment = new VideoPlayerFragment();
		playerFragment.setVideoPath("http://video-js.zencoder.com/oceans-clip.mp4");//http://bstart.github.io/video.mp4
		playerFragment.show(this);
	}
}
