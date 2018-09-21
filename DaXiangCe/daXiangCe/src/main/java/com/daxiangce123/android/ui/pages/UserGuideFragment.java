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
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.Interpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.ErrorCode;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.HomeActivity;
import com.daxiangce123.android.ui.activities.SplashActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.outh.Oauth;
import com.daxiangce123.android.util.outh.OauthHelper;
import com.daxiangce123.android.util.outh.OauthHelper.onOauthListener;
import com.daxiangce123.android.util.outh.QQHelper;
import com.daxiangce123.android.util.outh.WBHelper;
import com.daxiangce123.android.util.outh.WXHelper;

/**
 * this is not use any more
 * @project DaXiangCe
 * @time May 14, 2014
 * @author ram
 */
@Deprecated
public class UserGuideFragment extends BaseFragment implements OnClickListener {

	private final static String TAG = "UserGuideFragment";
	private OauthHelper oauthHelper;
	private boolean isOpening = false;
	private View contView;
	private View splashView;
	private View detailView;
	private TextView tvContent;
	private View albumView;
	private View timelineView;
	private View shareView;
	private View loginView;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (!intent.hasExtra(Consts.RESPONSE)) {
					return;
				}
				Response response = intent.getParcelableExtra(Consts.RESPONSE);
				LogUtil.d(TAG, "action:" + action + "	response:" + response);
				if (response.getStatusCode() != 200) {
					onFailed(response);
					return;
				}
				if (Consts.SSO_BIND.equals(action)) {
					initToken(response.getContent());
					ConnectBuilder.getMineInfo();
				} else if (Consts.GET_MIME_INFO.equals(action)) {
					LoadingDialog.dismiss();
					UserInfo info = Parser.parseUserInfo(response.getContent());
					if (info != null) {
						App.setUserInfo(info);
						AppData.setUid(info.getId());
						UMutils.instance().diyEvent(ID.EventSignInSuccess);
						onLogin();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	};

	private onOauthListener oauthListener = new onOauthListener() {
		@Override
		public void onOauthSucceed(Oauth oauth) {
			JSONObject jo = new JSONObject();
			jo.put(Consts.SSO_PROVIDER, oauth.getType());
			jo.put(Consts.TOKEN, oauth.getToken());
			jo.put(Consts.DEVICE, App.mobileInfo.BRAND + "-" + App.mobileInfo.PRODUCT);
			jo.put(Consts.UNIQUE_ID, oauth.getUid());
			jo.put(Consts.OS, "android");
			ConnectBuilder.sso_bind(jo.toString());
			LoadingDialog.show(R.string.logining);
		}

		@Override
		public void onOauthFailed(String provider, Object object) {
			LogUtil.d(TAG, "onOauthFailed: " + provider + "	object " + object);
			LoadingDialog.dismiss();
			String msg = getString(R.string.fail_to_get_token_from_x, provider);
			CToast.showToast(msg);
		}
	};

	@Override
	public String getFragmentName() {
		return "UserGuideFragment";
	}

	public UserGuideFragment() {
		setTitleBarVisibility(View.GONE);
		setBoottomBarVisibility(View.GONE);
	}

	@Override
	public void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		if (oauthHelper instanceof WXHelper) {
			((WXHelper) oauthHelper).handleIntent(intent);
		}
	}

	@Override
	public void onShown() {
		super.onShown();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		initBroad();
		if (contView == null) {
			contView = inflater.inflate(R.layout.fragment_user_guide, container, false);
			initUI();
		} else {
			ViewUtil.removeFromParent(contView);
		}
		return contView;
	}

	private void initUI() {
		splashView = contView.findViewById(R.id.ll_splash_guide);
		contView.findViewById(R.id.btn_begin_to_use).setOnClickListener(this);

		detailView = contView.findViewById(R.id.ll_detail_guide);
		contView.findViewById(R.id.text_skip_guide).setOnClickListener(this);
		contView.findViewById(R.id.tv_experence_now).setOnClickListener(this);

		tvContent = (TextView) contView.findViewById(R.id.text_content_guide);
		albumView = contView.findViewById(R.id.ll_album_guide);
		albumView.findViewById(R.id.user_guide_blink_btn).setOnClickListener(this);

		timelineView = contView.findViewById(R.id.ll_timeline_guide);
		timelineView.findViewById(R.id.user_guide_blink_btn).setOnClickListener(this);

		shareView = contView.findViewById(R.id.ll_share_guide);
		loginView = contView.findViewById(R.id.ll_login_guide);
		contView.findViewById(R.id.weibo).setOnClickListener(this);
		contView.findViewById(R.id.qq).setOnClickListener(this);
		contView.findViewById(R.id.wx).setOnClickListener(this);
		// contView.findViewById(R.id.tv_rule).setOnClickListener(this);

		contView.findViewById(R.id.agreement).setOnClickListener(this);
		contView.findViewById(R.id.privacy).setOnClickListener(this);

		if (getActivity() instanceof SplashActivity) {
			contView.findViewById(R.id.text_skip_guide).setOnClickListener(this);
		} else {
			contView.findViewById(R.id.text_skip_guide).setVisibility(View.GONE);
		}

	}

	private void showContent(final View parent) {
		int duration = 500;
		final Animation animIn = new AlphaAnimation(0f, 1.0f);
		animIn.setDuration(duration);

		final Animation animOut = new AlphaAnimation(1.0f, 0f);
		animOut.setDuration(duration);
		animOut.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {

			}

			@Override
			public void onAnimationEnd(Animation animation) {
				int contentId = R.string.guide_tip_timeline;
				if (parent == shareView) {
					contentId = R.string.guide_tip_share;
				}
				tvContent.setText(contentId);
				tvContent.clearAnimation();
				tvContent.startAnimation(animIn);
			}
		});
		tvContent.clearAnimation();
		tvContent.startAnimation(animOut);
	}

	private void blink(View v) {
		if (v == null) {
			return;
		}
		View blinkBg = v.findViewById(R.id.user_guide_blink_btn_bg);
		View blinkBtn = v.findViewById(R.id.user_guide_blink_btn);
		if (blinkBg == null || blinkBtn == null) {
			return;
		}
		int duration = 800;// heart frequence
		Interpolator interpolator = new AccelerateDecelerateInterpolator();
		Animation scale = new ScaleAnimation(0.5f, 1.0f, 0.5f, 1.0f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		scale.setRepeatCount(Animation.INFINITE);
		Animation alpha = new AlphaAnimation(1.0f, 0.0f);
		alpha.setRepeatCount(Animation.INFINITE);
		final AnimationSet set = new AnimationSet(true);
		set.setDuration(duration);
		set.setInterpolator(interpolator);
		set.addAnimation(scale);
		set.addAnimation(alpha);
		blinkBg.startAnimation(set);

		// btn
		Animation alphaBtn = new AlphaAnimation(1.0f, 0.5f);
		alphaBtn.setRepeatCount(Animation.INFINITE);
		alphaBtn.setDuration(duration);
		alphaBtn.setInterpolator(interpolator);
		blinkBtn.startAnimation(alphaBtn);
	}

	private void animLeftRight(final View in, final View out) {
		if (in == null || out == null) {
			return;
		}
		in.setVisibility(View.VISIBLE);
		int duration = 600;
		int style = Animation.RELATIVE_TO_SELF;
		Interpolator interpolator = new AccelerateDecelerateInterpolator();

		// final Animation animOut = new TranslateAnimation(0,
		// -out.getMeasuredWidth(), 0, 0);
		final Animation animOut = new TranslateAnimation(style, 0, style, -1.0f, style, 0f, style, 0f);
		animOut.setDuration(duration);
		animOut.setInterpolator(interpolator);

		// final Animation animIn = new
		// TranslateAnimation(in.getMeasuredWidth(),
		// 0, 0, 0);
		final Animation animIn = new TranslateAnimation(style, 1.0f, style, 0f, style, 0f, style, 0f);
		animIn.setDuration(duration);
		animIn.setInterpolator(interpolator);
		animIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				out.setVisibility(View.GONE);
			}
		});
		showContent(in);
		if (in == timelineView) {
			blink(in);
		}
		View imgIn = in.findViewById(R.id.user_guide_detail_image);
		View imgOut = out.findViewById(R.id.user_guide_detail_image);
		if (imgIn == null || imgOut == null) {
			return;
		}
		imgIn.clearAnimation();
		imgIn.startAnimation(animIn);

		imgOut.clearAnimation();
		imgOut.startAnimation(animOut);
	}

	private void animBottomTop(final View in, final View out) {
		if (in == null || out == null) {
			return;
		}
		in.setVisibility(View.VISIBLE);
		int height = contView.getMeasuredHeight();
		int duration = 600;

		Interpolator interpolator = new AccelerateDecelerateInterpolator();

		final Animation animOut = new TranslateAnimation(0, 0, 0, -height);
		animOut.setDuration(duration);
		animOut.setInterpolator(interpolator);

		final Animation animIn = new TranslateAnimation(0, 0, height, 0);
		animIn.setDuration(duration);
		animIn.setInterpolator(interpolator);
		animIn.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				out.setVisibility(View.GONE);
			}
		});
		in.clearAnimation();
		in.startAnimation(animIn);

		out.clearAnimation();
		out.startAnimation(animOut);
		if (in == detailView) {
			blink(in);
		}
	}

	private void showLogin() {
		if (getActivity() instanceof SplashActivity) {
			animBottomTop(loginView, detailView);
			// UMutils.instance().diyEvent(ID.EventCompletedGuide);
		} else {
			back();
		}
	}

	private void showAgreement() {
		BrowserFragment browserFragment = new BrowserFragment();
		browserFragment.setHomeUrl(Consts.URL_AGREE);
		browserFragment.show(getBaseActivity());
	}

	private void showPrivacy() {
		BrowserFragment browserFragment = new BrowserFragment();
		browserFragment.setHomeUrl(Consts.URL_PRIVACY);
		browserFragment.show(getBaseActivity());
	}

	@Override
	public void onClick(View v) {
		LogUtil.d(TAG, "onClick ");
		int id = v.getId();
		if (id == R.id.btn_begin_to_use) {
			animBottomTop(detailView, splashView);
		} else if (id == R.id.text_skip_guide) {
			// UMutils.instance().diyEvent(ID.EventSkipGuide);
			showLogin();
		} else if (id == R.id.user_guide_blink_btn) {
			Object obj = v.getTag();
			LogUtil.d(TAG, "onClick user_guide_blink_btn tag=" + obj);
			if ("timeline".equals(obj)) {
				animLeftRight(shareView, timelineView);
				return;
			}
			animLeftRight(timelineView, albumView);
			// } else if (id == R.id.tv_rule) {
			// LogUtil.d(TAG, "onClick FragmentAboutUs");
			// new FragmentAboutUs().show(getBaseActivity());
		} else if (id == R.id.tv_experence_now) {
			showLogin();
		} else if (id == R.id.weibo || id == R.id.qq || id == R.id.wx) {
			if (id == R.id.qq) {
				oauthHelper = new QQHelper();
			} else if (id == R.id.wx) {
				oauthHelper = new WXHelper();
			} else {
				oauthHelper = new WBHelper();
			}
			if (oauthHelper != null) {
				oauthHelper.setOauthListener(oauthListener);
				oauthHelper.oauth();
			}
		} else if (id == R.id.agreement) {
			showAgreement();
		} else if (id == R.id.privacy) {
			showPrivacy();
		}
	}

	private void initBroad() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.SSO_BIND);
		ift.addAction(Consts.GET_MIME_INFO);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void onFailed(Response response) {
		LoadingDialog.dismiss();
		AppData.clear();
		if (response == null) {
			CToast.showToast(R.string.login_failed);
			return;
		}
		if (response.getErrCode() == ErrorCode.NOT_FOUND) {
			CToast.showToast(R.string.sns_account_invalid);
		} else if (response.getErrCode() == ErrorCode.NETWORK_ERROR) {
			CToast.showToast(R.string.network_error);
		} else {
			CToast.showToast(R.string.unknown_error);
		}
	}

	private boolean initToken(String session) {
		if (App.DEBUG) {
			LogUtil.d(TAG, "isJSONObject \n" + session);
		}
		if (!JSONUtil.isJSONObject(session)) {
			return false;
		}
		JSONObject jo = JSONObject.parseObject(session);
		String token = jo.getString(Consts.ACCESS_TOKEN);
		AppData.setToken(token);
		ConnectBuilder.init();
		return true;
	}

	private void onLogin() {
		if (isOpening) {
			return;
		}
		isOpening = true;
		Intent start = new Intent(getActivity(), HomeActivity.class);
		AppUtil.startActivity(start, true);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		LogUtil.d(TAG, " onActivityResult data = " + data + " oauthHelper=" + oauthHelper);
		if (oauthHelper != null) {
			oauthHelper.onActivityResult(requestCode, resultCode, data);
		} else {
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	@Override
	public void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		isOpening = false;
		super.onDestroy();
	}
}
