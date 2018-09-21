package com.daxiangce123.android.ui.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.http.ProgressInfo;
import com.daxiangce123.android.ui.activities.SamplePlayVideoActivity;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;

public class SamplePictureViewerDialog extends CDialog implements
		OnClickListener {
	public final static String TAG = "SamplePictureViewerDialog";
	private int curPosition;
	private boolean DEBUG = true;
	private ViewPager vpContainer;
	private View contentView;
	private FileEntity curFile;
	private List<FileEntity> fileList;
	private ImagePagerAdapter pagerAdapter;
	private TempToken token;

	private ArrayList<SamplePreview> viewList = new ArrayList<SamplePreview>();
	/**
	 * fileId -> view
	 */
	private HashMap<String, SamplePreview> viewMap = new HashMap<String, SamplePreview>();
	private OnPageChangeListener changeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			curPosition = position;
			if (DEBUG) {
				LogUtil.d(TAG, "onPageSelected()	curPosition=" + curPosition);
			}
			updateUI();
			UMutils.instance().diyEvent(ID.EventSwipePreview);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	};

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				if (Consts.TRANSFER_PROGRESS.equals(action)) {
					onProgress(intent);
				} else if (Consts.DOWNLOAD_FILE.equals(action)) {
					ConnectInfo info = intent
							.getParcelableExtra(Consts.REQUEST);
					onDowload(info.getTag2());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	public SamplePictureViewerDialog() {
		super();
		initDialog();
		initUI();
		initBroadcast();
	}

	private void initDialog() {
		setCancelable(true);
		setCanceledOnTouchOutside(false);
		Window window = getWindow();
		window.setWindowAnimations(R.style.AnimBottom);
		window.setGravity(Gravity.BOTTOM);
	}

	private void initUI() {
		contentView = LayoutInflater.from(getContext()).inflate(
				R.layout.fragment_sample_picture_viewer, null);
		LayoutParams dlp = new LayoutParams(App.SCREEN_WIDTH,
				LayoutParams.WRAP_CONTENT);
		setContentView(contentView, dlp);

		for (int i = 0; i < 3; i++) {
			SamplePreview photoView = new SamplePreview(this.getContext());
			photoView.setOnVideoClickListener(this);
			viewList.add(photoView);
		}

		pagerAdapter = new ImagePagerAdapter();
		vpContainer = (ViewPager) contentView
				.findViewById(R.id.vp_container_picture);
		vpContainer.setAdapter(pagerAdapter);
		vpContainer.setOnPageChangeListener(changeListener);
	}

	private void initBroadcast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.TRANSFER_PROGRESS);
		ift.addAction(Consts.DOWNLOAD_FILE);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void updateUI() {
		if (curFile == null) {
			return;
		}
		PhotoPreview preview = viewMap.get(curFile.getId());
		if (preview == null) {
			return;
		}
		preview.setFile(curFile);
		preview.showFile(true);
	}

	private Bitmap showMedia(FileEntity file) {
		if (file == null) {
			return null;
		}
		SamplePreview preview = viewMap.get(file.getId());
		preview.setFile(file);
		preview.setTempToken(token);
		return preview.showFile(file == curFile);
	}

	public void setTempToken(TempToken token) {
		this.token = token;
	}

	public void notifyDataSetChange() {
		pagerAdapter.notifyDataSetChanged();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		Broadcaster.unregisterReceiver(receiver);
		vpContainer.removeAllViews();
	}

	@Override
	protected void onStart() {
		super.onStart();
		vpContainer.setCurrentItem(curPosition);
	}

	@Override
	public void onClick(View v) {
		Object obj = v.getTag();
		if (DEBUG) {
			LogUtil.d(TAG, "--------------------------------onClick()!!!!"
					+ obj);
		}
		if (obj instanceof FileEntity) {
			onPreviewClick((FileEntity) obj);
		}
		super.dismiss();
	}

	private boolean onPreviewClick(FileEntity file) {
		if (DEBUG) {
			LogUtil.d(TAG,
					"--------------------------------onPreviewClick()!!!!");
		}
		if (file == null) {
			return false;
		}
		if (MimeTypeUtil.getMime(file.getMimeType()) == Mime.VID) {
			return playVideo(file);
		}
		return false;
	}

	private boolean playVideo(FileEntity fileEntity) {
		if (fileEntity == null) {
			return false;
		}
		if (DEBUG) {
			LogUtil.d(TAG, "--------------------------------playVideo()!!!!");
		}
		if (Mime.VID != MimeTypeUtil.getMime(fileEntity.getMimeType())) {
			return false;
		}
		Intent intent = new Intent();
		intent.putExtra(Consts.ACCESS_TOKEN,
				token == null ? null : token.getToken());
		intent.putExtra(Consts.FILE, fileEntity);
		intent.setClass(getContext(), SamplePlayVideoActivity.class);
		getContext().startActivity(intent);
		return true;
	}

	private FileEntity getFile(int position) {
		if (position < 0 || position >= getImageCount()) {
			return null;
		}
		FileEntity entity = fileList.get(position);
		return entity;
	}

	private int getImageCount() {
		if (Utils.isEmpty(fileList)) {
			return 0;
		}
		return fileList.size();
	}

	public SamplePreview getView(int position) {
		if (viewList == null) {
			return null;
		}
		int size = viewList.size();
		int truePosition = position % size;
		SamplePreview view = null;
		if (truePosition < size) {
			view = viewList.get(truePosition);
		}
		return view;
	}

	private void onProgress(Intent intent) {
		ProgressInfo pinfo = intent.getParcelableExtra(Consts.PROGRESS_INFO);
		ConnectInfo cInfo = intent.getParcelableExtra(Consts.REQUEST);
		String fileId = cInfo.getTag2();
		PhotoPreview photoPreview = viewMap.get(fileId);
		if (photoPreview == null) {
			return;
		}
		FileEntity file = (FileEntity) photoPreview.getTag();
		if (file == null || file.getId().equals(fileId)) {
			return;
		}
		if (photoPreview.hasBitmap() && photoPreview.isVideo()) {
			if (pinfo.getProgress() < 100) {
				photoPreview.showProgress(true);
			}
		} else if (MimeTypeUtil.getMime(file.getMimeType()) == Mime.GIF) {
			if (pinfo.getProgress() < 100) {
				photoPreview.showProgress(true);
			}
		}
		photoPreview.setProgress(pinfo.getProgress());
	}

	private void onDowload(String fileId) {
		if (curFile == null || fileId != curFile.getId()) {
			return;
		}
		PhotoPreview photoPreview = viewMap.get(fileId);
		if (photoPreview == null) {
			return;
		}
		playGif();
	}

	private void playGif() {
		PhotoPreview preview = viewMap.get(curFile.getId());
		if (preview == null) {
			return;
		}
		preview.setFile(curFile);
		preview.showFile(true);
	}

	public void setCurPosition(int position) {
		this.curPosition = position;
		if (DEBUG) {
			LogUtil.d(TAG, "setCurPosition()	position=" + position);
		}
	}

	public void setFileList(List<FileEntity> list) {
		fileList = list;
		if (pagerAdapter != null) {
			pagerAdapter.notifyDataSetChanged();
		}
	}

	private class ImagePagerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return getImageCount();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			FileEntity fileEntity = getFile(position);
			String fileId = fileEntity == null ? null : fileEntity.getId();
			SamplePreview photoPreview = getView(position);
			if (photoPreview.getTag() instanceof FileEntity) {
				FileEntity preFile = (FileEntity) photoPreview.getTag();
				viewMap.remove(preFile.getId());
				photoPreview.setImageBitmap(null);
				container.removeView(photoPreview);
			}

			photoPreview.setTag(fileEntity);
			viewMap.put(fileId, photoPreview);
			showMedia(fileEntity);
			container.addView(photoPreview);
			return photoPreview;
		}

		@Override
		public int getItemPosition(Object object) {
			if (getCount() > 0) {
				return PagerAdapter.POSITION_NONE;
			}
			return super.getItemPosition(object);
		}

	}
}
