package com.daxiangce123.android.ui.adapter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.view.PhotoView;
import com.daxiangce123.android.util.Utils;

public class AlbumGridViewAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<FileEntity> dataList = new ArrayList<FileEntity>();
	private HashSet<FileEntity> clickedMap = new HashSet<FileEntity>();
	private AbsListView parentView;
	private ImageSize imageSize;

	public AlbumGridViewAdapter(Context context) {
		mContext = context;
	}

	public void setData(List<FileEntity> dataList) {
		this.dataList = dataList;
	}

	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}

	public void onFileSelect(FileEntity file) {
		if (clickedMap.contains(file)) {
			clickedMap.remove(file);
		} else {
			clickedMap.add(file);
		}
		onStateChanged();
	}

	private void onStateChanged() {
		TextView tvDelete = (TextView) (((Activity) mContext).findViewById(R.id.tv_delete));
		RelativeLayout rlDeleteLayout = (RelativeLayout) (((Activity) mContext).findViewById(R.id.rl_delete_1));

		if (clickedMap.isEmpty()) {
			tvDelete.setText(R.string.delete);
			tvDelete.setTextColor(0xffd7d7d7);
			rlDeleteLayout.setBackgroundColor(Color.WHITE);
		} else {
			tvDelete.setText(((Activity) mContext).getResources().getString(R.string.delete) + "(" + clickedMap.size() + ")");
			rlDeleteLayout.setBackgroundColor(0xff0794e1);
			tvDelete.setTextColor(Color.WHITE);
		}
	}

	public void setSeletctAll(boolean selected) {
		if (selected) {
			for (FileEntity entity : dataList) {
				clickedMap.add(entity);
			}
		} else {
			clickedMap.clear();
		}
		onStateChanged();
	}

	public HashSet<FileEntity> getSelectFile() {
		return clickedMap;
	}

	public void cancelSelect() {
		clickedMap.clear();
	}

	@Override
	public int getCount() {
		if (Utils.isEmpty(dataList)) {
			return 0;
		}
		return dataList.size();
	}

	@Override
	public FileEntity getItem(int position) {
		if (position >= getCount()) {
			return null;
		}
		return dataList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (parentView == null && parent instanceof AbsListView) {
			parentView = (AbsListView) parent;
		}
		PhotoView photoView = null;
		if (convertView == null || !(convertView instanceof PhotoView)) {
			convertView = photoView = new PhotoView(mContext);
			ViewGroup.LayoutParams param = new AbsListView.LayoutParams(imageSize.getWidth(), imageSize.getHeight());
			convertView.setPadding(15, 15, 15, 15);
			convertView.setLayoutParams(param);
		} else {
			photoView = (PhotoView) convertView;
		}

		FileEntity file = getItem(position);
		BulletManager.instance().addFile(file);
		boolean selected = clickedMap.contains(file);
		photoView.setData(imageSize, file);
		photoView.checked(selected);
		photoView.resetLoadingSize(imageSize.getWidth(), imageSize.getWidth());
		photoView.showPhoto();
		return convertView;
	}

}
