package com.daxiangce123.android.ui.view;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout.LayoutParams;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.ui.pages.VideoPlayerFragment;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;

/**
 * @project DaXiangCe
 * @time 2014-6-27
 * @author
 */
public class PreviewDialog extends CDialog implements
		android.view.View.OnClickListener {

	private FileEntity fileEntity;
	private View contentView;
	private PhotoPreview photoPreview;
	private VideoPlayerFragment playerFragment;

	public PreviewDialog() {
		super();
		initUI();
	}

	private void initUI() {
		setCanceledOnTouchOutside(true);
		contentView = LayoutInflater.from(getContext()).inflate(
				R.layout.big_file_dailog, null);
		LayoutParams lp = new LayoutParams(App.SCREEN_WIDTH,
				LayoutParams.WRAP_CONTENT);
		setContentView(contentView, lp);

		findViewById(R.id.view_empty_big_file).setOnClickListener(this);
		photoPreview = (PhotoPreview) findViewById(R.id.iv_big_file);
		findViewById(R.id.iv_big_file).setOnClickListener(this);
	}

	public void show(FileEntity fileEntity) {
		super.show();
		this.fileEntity = fileEntity;
		photoPreview.setTag(fileEntity);
		photoPreview.setFile(fileEntity);
		photoPreview.showFile(true);
	}

	private boolean playVideo() {
		if (fileEntity == null) {
			return false;
		}
		if (Mime.VID != MimeTypeUtil.getMime(fileEntity.getMimeType())) {
			return false;
		}
		if (playerFragment == null) {
			playerFragment = new VideoPlayerFragment();
		}
		playerFragment.setFileEntity(fileEntity);
		playerFragment.show(App.getActivity());
		return true;
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.iv_big_file) {
			playVideo();
		}
		dismiss();
	}
}
