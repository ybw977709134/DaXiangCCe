package com.daxiangce123.android.ui.pages;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.webkit.DownloadListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

/**
 * @project yunio-android-client
 * @time 2013-9-26
 * @author ram
 */
public class BrowserFragment extends BaseFragment implements OnClickListener, DownloadListener {

	private View rootView;
	private WebView mWebView;
	private String homeUrl;
	private ProgressBar pb;
	private TextView tvTitle;
	private PullToRefreshWebView refreshWebView;
	private WebViewClient webViewClient = new WebViewClient() {

		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}

		@Override
		public void onPageFinished(WebView view, String url) {
			super.onPageFinished(view, url);
		}
	};

	private WebChromeClient chromeClient = new WebChromeClient() {
		@Override
		public void onReceivedTitle(WebView view, String title) {
			super.onReceivedTitle(view, title);
			tvTitle.setText(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			if (newProgress >= 70) {
				pb.setVisibility(View.INVISIBLE);
				refreshWebView.onRefreshComplete();
			} else {
				pb.setVisibility(View.VISIBLE);
			}
			// if (refreshWebView.isRefreshing()) {
			// if (pb.isShown()) {
			// pb.setVisibility(View.GONE);
			// }
			// }
		}
	};

	private OnRefreshListener2<WebView> onRefreshListener2 = new OnRefreshListener2<WebView>() {

		@Override
		public void onPullDownToRefresh(PullToRefreshBase<WebView> refreshView) {
			if (mWebView == null) {
				return;
			}
			mWebView.reload();
		}

		@Override
		public void onPullUpToRefresh(PullToRefreshBase<WebView> refreshView) {

		}

	};

	public BrowserFragment() {
		setBoottomBarVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (rootView == null) {
			rootView = inflater.inflate(R.layout.fragment_browser, container, false);
			initUI();
		} else {
			ViewParent vp = rootView.getParent();
			if (vp instanceof ViewGroup) {
				((ViewGroup) vp).removeView(rootView);
			}
		}
		updateUI();
		return rootView;
	}

	@SuppressLint("SetJavaScriptEnabled")
	private void initUI() {
		pb = (ProgressBar) rootView.findViewById(R.id.pb);
		pb.setMax(100);

		rootView.findViewById(R.id.iv_back).setOnClickListener(this);
		tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		try {
			refreshWebView = (PullToRefreshWebView) rootView.findViewById(R.id.wv_browser);
			refreshWebView.setOnRefreshListener(onRefreshListener2);

			mWebView = refreshWebView.getRefreshableView();
			mWebView.requestFocus();
			mWebView.setWebViewClient(webViewClient);
			mWebView.setWebChromeClient(chromeClient);
			mWebView.getSettings().setSupportZoom(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setDomStorageEnabled(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.setDownloadListener(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateUI() {
		try {
			mWebView.clearHistory();
			mWebView.loadUrl(homeUrl);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setHomeUrl(String url) {
		this.homeUrl = url;
	}

	@Override
	public boolean onBackPressed() {
		try {
			String url = getPureUrl(mWebView.getUrl());
			String home = getPureUrl(homeUrl);
			if (url.equals(home)) {
				return false;
			}
			if (mWebView.canGoBack()) {
				mWebView.stopLoading();
				mWebView.goBack();
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private String getPureUrl(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		if (url.startsWith("http://")) {
			url = url.replace("http://", "");
		} else if (url.startsWith("https://")) {
			url = url.replace("https://", "");
		}
		return url;
	}

	@Override
	public void onDestroyView() {
		mWebView.stopLoading();
		mWebView.loadUrl("");
		homeUrl = null;
		super.onDestroyView();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.iv_back) {
			back();
		}
	}

	@Override
	public String getFragmentName() {
		return "BrowserFragment";
	}

	@Override
	public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimetype, long contentLength) {
		Uri uri = Uri.parse(url);
		// LogUtil.d(TAG, " onDownloadStart--url " + url + "-- uri --" +
		// uri);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
}
