package com.daxiangce123.android.ui.test;

import java.util.ArrayList;
import java.util.List;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time Sep 26, 2014
 * @author ram
 */
public class TestAdapter extends BaseAdapter {
	private final static String TAG = "TestAdapter";

	public ImageSize fileSize;
	private List<FileEntity> lists;

	@Override
	public int getCount() {
		if (lists == null) {
			return 0;
		}
		return lists.size();
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	public void setImageSize(ImageSize avaterSize, ImageSize fileSize) {
		// this.fileSize = fileSize;
		if (App.DEBUG) {
			LogUtil.d(TAG, "setImageSize	" + avaterSize + "	" + fileSize);
		}
	}

	public void setData(Object object) {
	}

	public TestAdapter() {
		readTestFiles();
	}

	private void readTestFiles() {
		if (!App.DEBUG) {
			return;
		}
		lists = new ArrayList<FileEntity>();
		DBHelper dbHelper = App.getDBHelper();
		String WHERE = Consts.ALBUM + "!=\"" + 1 + "\"  ORDER BY " + Consts.CREATE_DATE + " DESC LIMIT " + 1000;
		List<FileEntity> l = dbHelper.getList(FileEntity.EMPTY, WHERE);
		LogUtil.d(TAG, "readTestFiles size	" + Utils.sizeOf(l));
		if (l == null) {
			return;
		}
		lists.addAll(l);
	}

	public FileEntity getFileId(int position) {
		if (Utils.isEmpty(lists)) {
			return null;
		}
		int size = lists.size();
		if (position >= size) {
			position = position % size;
		}
		return lists.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LogUtil.d(TAG, "getView--------------------------------------------------------------------	" + position + "	" + this);
		ImageView iv = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.member_item, parent, false);
			iv = (ImageView) convertView.findViewById(R.id.member_icon);
			iv.setLayoutParams(new RelativeLayout.LayoutParams(150, 150));
		} else {
			iv = (ImageView) convertView.findViewById(R.id.member_icon);
		}
		showImage(position, iv);
		return convertView;
	}

	public void showImage(int position, ImageView ivPhoto) {
		FileEntity file = getFileId(position);
		ivPhoto.setImageResource(R.drawable.ic_launcher);
		// ImageManager.instance().load(ivPhoto, file, fileSize);
	}

}
