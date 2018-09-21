package com.daxiangce123.android.ui.view;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

public class ActPageDialog extends CDialog implements android.view.View.OnClickListener {

	private final static String TAG = "AlbumView";
	private int numColumns = 4;
	private int spacing;
	private View contentView;
	private ImageView ivClose;
	private ProgressBar pb;
	// private PullToRefreshWebView refreshWebView;
	private WebView mWebView;
	private String homeUrl;
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
			// tvTitle.setText(title);
		}

		@Override
		public void onProgressChanged(WebView view, int newProgress) {
			super.onProgressChanged(view, newProgress);
			if (newProgress >= 70) {
				pb.setVisibility(View.INVISIBLE);
				// refreshWebView.onRefreshComplete();
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

	public ActPageDialog() {
		super();
		initDialog();
		initUI();
	}

	private void initDialog() {
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		Window window = getWindow();
		window.setWindowAnimations(R.style.AnimBottom);
		window.setGravity(Gravity.CENTER_VERTICAL);

	}

	private void initUI() {
		// spacing = Utils.getDip(10);

		final int dialogWidth = App.SCREEN_WIDTH;

		// int margin = (int) (App.SCREEN_WIDTH / 15);
		LayoutParams dlp = new LayoutParams(App.SCREEN_WIDTH, LayoutParams.WRAP_CONTENT);

		int margin = dialogWidth / 15;

		contentView = LayoutInflater.from(getContext()).inflate(R.layout.act_page_view, null);

		// contentView.setPadding(margin, margin, margin, margin * 3 / 2);
		setContentView(contentView, dlp);

		// int padding = Utils.getDip(20);
		// int width = (int) (App.SCREEN_WIDTH - 2 * margin) - padding * 2;

		// init views
		ivClose = (ImageView) contentView.findViewById(R.id.iv_close);
		ivClose.setOnClickListener(this);

		pb = (ProgressBar) contentView.findViewById(R.id.pb);
		pb.setMax(100);

		try {
			// refreshWebView = (PullToRefreshWebView)
			// contentView.findViewById(R.id.wv_browser);
			// refreshWebView.setMode(Mode.DISABLED);

			// mWebView = refreshWebView.getRefreshableView();
			mWebView = (WebView) contentView.findViewById(R.id.wv_browser);
			mWebView.requestFocus();
			mWebView.setWebViewClient(webViewClient);
			mWebView.setWebChromeClient(chromeClient);
			mWebView.getSettings().setSupportZoom(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setJavaScriptEnabled(true);
			mWebView.getSettings().setDomStorageEnabled(true);
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.getSettings().setLoadWithOverviewMode(true);
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
		updateUI();
	}

	// public void onBackPressed() {
	// try {
	// String url = getPureUrl(mWebView.getUrl());
	// String home = getPureUrl(homeUrl);
	// if (url.equals(home)) {
	// return;
	// }
	// if (mWebView.canGoBack()) {
	// mWebView.stopLoading();
	// mWebView.goBack();
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private String getPureUrl(String url) {
	// if (TextUtils.isEmpty(url)) {
	// return null;
	// }
	// if (url.startsWith("http://")) {
	// url = url.replace("http://", "");
	// } else if (url.startsWith("https://")) {
	// url = url.replace("https://", "");
	// }
	// return url;
	// }

	@Override
	public void dismiss() {
		mWebView.stopLoading();
		mWebView.loadUrl("");
		homeUrl = null;
		AppData.setFirstShowAct(false);
		super.dismiss();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.iv_close) {
			dismiss();
		}
	}
}
