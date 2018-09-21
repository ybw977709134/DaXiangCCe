package com.daxiangce123.android.ui.view;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.FrameLayout;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.pages.AlbumFragment.AlbumFragmentListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.UMutils.ID;

public class BannerAlbumDialog extends CDialog implements
		android.view.View.OnClickListener {

	protected final static String TAG = "BannerAlbumDialog";
	private AlbumEntity albumEntity;
	private ImageSize imageSize;
	private List<FileEntity> sampleFiles;
	private static boolean DEBUG = true;
	private AlbumView albumView;
	private AlbumFragmentListener albumListener;
	private TempToken tempToken;
	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				Response response = intent.getParcelableExtra(Consts.RESPONSE);
				ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
				String action = intent.getAction();
				if (Consts.JOIN_ALBUM.equals(action)) {
					onJoinAlbum(info, response);
				} else if (Consts.GET_TEMP_TOKEN_BY_LINK.equals(action)) {
					onGetToken(info, response);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public BannerAlbumDialog() {
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
		final int dialogWidth = App.SCREEN_WIDTH;
		final int margin = dialogWidth / 20;
		final int padding = Utils.getDip(20);
		final int width = (int) (dialogWidth - 2 * margin) - padding * 2;

		imageSize = new ImageSize(width, width);
		imageSize.setHasThumbFile(true);
		imageSize.setThumb(true);

		LayoutParams dlp = new LayoutParams(dialogWidth, App.SCREEN_HEIGHT
				- margin);
		FrameLayout contentView = new FrameLayout(getContext());
		contentView.setPadding(margin, margin, margin, margin);
		albumView = new AlbumView(getContext());
		albumView.setJoinClickListener(this);
		albumView.setImageSize(imageSize);
		contentView.addView(albumView);
		albumView.showReport(false);
		setContentView(contentView, dlp);
	}

	private void updateUI() {
		albumView.setData(albumEntity);
		albumView.setSample(sampleFiles);
		albumView.showPrivate(false,
				albumListener.hasJoined(albumEntity.getId()));
		if (tempToken == null) {
			ConnectBuilder.getTempTokenByLink(albumEntity.getLink(),
					albumEntity.getId());
		}
	}

	private void onJoinAlbum(ConnectInfo info, Response response) {
		if (response == null || info == null || albumEntity == null) {
			return;
		}
		if (DEBUG) {
			LogUtil.d(TAG,
					"-------------------onJoinAlbum()-------------------");
		}
		String albumId = info.getTag2();
		if (!albumEntity.getId().equals(albumId)) {
			return;
		}
		int sCode = response.getStatusCode();
		if (sCode == 200) {
			albumView.postDelayed(new Runnable() {
				@Override
				public void run() {
					updateUI();
				}
			}, 500);
		}
	}

	private void initBroadCast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.JOIN_ALBUM);
		ift.addAction(Consts.GET_TEMP_TOKEN_BY_LINK);
		Broadcaster.registerReceiver(receiver, ift);
	}

	public void notifyDataSetChanged() {
		updateUI();
	}

	public void setAlbumEntity(AlbumEntity albumEntity) {
		this.albumEntity = albumEntity;
	}

	public void setSample(List<FileEntity> sampleFiles) {
		this.sampleFiles = sampleFiles;
	}

	public void setOnAlbumListener(AlbumFragmentListener albumListener) {
		this.albumListener = albumListener;
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		Broadcaster.unregisterReceiver(receiver);
	}

	@Override
	public void show() {
		super.show();
		initBroadCast();
		updateUI();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_join_in) {
			if (albumListener.hasJoined(albumEntity.getId())) {
				albumListener.openAlbum(albumEntity);
			} else {
				JSONObject jo = new JSONObject();
				jo.put(Consts.USER_ID, App.getUid());
				ConnectBuilder.joinAlbum(albumEntity.getId(),
						albumEntity.getInviteCode(), jo.toJSONString());
				UMutils.instance().diyEvent(ID.JoinAlbumViaBanner);
			}
		}
	}

	private void onGetToken(ConnectInfo info, Response response) {
		if (response == null || info == null || albumEntity == null) {
			return;
		}
		String albumId = info.getTag2();
		if (!albumEntity.getId().equals(albumId)) {
			return;
		}
		if (response.getStatusCode() == 401) {
			tempToken = TempToken.EMPTY;
		} else if (response.getStatusCode() == 200) {
			tempToken = Parser.parseTempToken(response.getContent());
		} else {
			return;
		}
		if (tempToken != null) {
			albumView.setToken(tempToken);
		}
	}
}
