package com.daxiangce123.android.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.Utils;

public class LikeUserAdapter extends BaseAdapter {

	public static final String TAG = "LikeUserAdapter";

	private Context mContext = null;
	private List<LikeEntity> dataList = null;
	private int imageSize;

	public LikeUserAdapter(Context context) {
		mContext = context;
	}

	public void setData(List<LikeEntity> dataList) {
		this.dataList = dataList;
	}

	public void setImageSize(int size) {
		this.imageSize = size;
	}

	// JUST show the first 5 likes
	@Override
	public int getCount() {
		if (Utils.isEmpty(dataList)) {
			return 0;
		}
		return dataList.size();
	}

	@Override
	public LikeEntity getItem(int position) {
		if (dataList == null) {
			return null;
		} else {
			return dataList.get(position);
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	// @Override
	// public View getView(int position, View convertView, ViewGroup parent) {
	// ImageView ivAvater = null;
	// if (convertView == null) {
	// convertView = ivAvater = new ImageView(mContext);
	// AbsListView.LayoutParams alp = new AbsListView.LayoutParams(
	// imageSize, imageSize);
	// ivAvater.setLayoutParams(alp);
	// } else {
	// ivAvater = (ImageView) convertView;
	// }
	// ivAvater.setImageBitmap(null);
	// LikeEntity like = getItem(position);
	// convertView.setTag(convertView.getId(), like);
	// if (like != null) {
	// ImageManager.instance().loadAvater(ivAvater, like.getUserId());
	// }
	// return convertView;
	// }

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.transfer_member_item, parent, false);
			viewHolder.tvUserName = (TextViewParserEmoji) convertView.findViewById(R.id.tv_member_name);
			viewHolder.userIcon = (ImageViewEx) convertView.findViewById(R.id.member_icon);
			convertView.setTag(viewHolder);
			ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(imageSize, imageSize);
			viewHolder.userIcon.setLayoutParams(lp);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		LikeEntity like = getItem(position);

		viewHolder.tvUserName.setEmojiText(like.getUserName());
		// viewHolder.tvUserName.setText(like.getUserName());
		// viewHolder.tvUserName.measure(MeasureSpec.UNSPECIFIED,
		// MeasureSpec.UNSPECIFIED);
		// double viewWidth = (double) viewHolder.tvUserName.getMeasuredWidth();
		// TextPaint paint = viewHolder.tvUserName.getPaint();
		// double textWidth = (double) paint.measureText(like.getUserName());
		// viewHolder.tvUserName.setEmojiText(like.getUserName(), viewWidth,
		// textWidth);

		viewHolder.setAvatar(like);
		convertView.setTag(convertView.getId(), like);
		return convertView;
	}

	private class ViewHolder {
		ImageViewEx userIcon = null;
		TextViewParserEmoji tvUserName = null;

		public void setAvatar(LikeEntity like) {
			if (like == null) {
				return;
			}
			if (userIcon == null) {
				return;
			}
			userIcon.setImageBitmap(null);
			if (like != null) {
				ImageManager.instance().loadAvater(userIcon, like.getUserId());
			}
		}

	}

}
