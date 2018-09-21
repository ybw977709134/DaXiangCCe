package com.daxiangce123.android.ui.pages;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.R;

/**
 * @author Hiccup
 */
public class CliqTestFragment extends BaseFragment implements OnClickListener {

	private static final String TAG = "CliqTestFragment";

	private View mRootView = null;
	private Button loginBtn = null;
	private TextView text = null;
	@Override
	public String getFragmentName() {
		return "CliqTestFragment";
	}
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.cliq_test_fragment,
					container, false);
		} else {
			ViewUtil.removeFromParent(mRootView);
		}
		initCompontent();
		return mRootView;
	}

	private void initCompontent() {
		loginBtn = (Button) mRootView.findViewById(R.id.loginBtn);
		text = (TextView) mRootView.findViewById(R.id.text);

		loginBtn.setOnClickListener(this);
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.obj instanceof String) {
				String s = (String) msg.obj;
				text.setText(s);
			}
		}
	};

	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.loginBtn) {
			// UIManager.getInstance().sendMsg(AppRequest.REQ_NET_LOGIN,
			// MsgPriority.PRIORITY_HIGH, TAG, handler, null);
		}

	}

}
