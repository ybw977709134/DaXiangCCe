package com.daxiangce123.android.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.Utils;

public class TransferMemberAdapter extends BaseAdapter {
	private Context mContext = null;
	private List<MemberEntity> dataList;
	private ImageSize imageSize;
	private boolean isDelete;
	private OnClickListener clickListener;

	public TransferMemberAdapter(Context context) {
		mContext = context;
	}

	public void setData(List<MemberEntity> dataList) {
		this.dataList = dataList;
	}

	public void setIsDelete(boolean isDelete) {
		this.isDelete = isDelete;
		notifyDataSetChanged();
	}

	public void setClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	public void setImageSize(ImageSize size) {
		this.imageSize = size;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}

	public int getCount() {

		if (Utils.isEmpty(dataList)) {
			return 0;
		}
		return dataList.size();

	}

	public Object getItem(int position) {
		if (position >= getCount()) {
			return null;
		}
		return dataList.get(position);

	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder = null;
		if (convertView == null) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.transfer_member_item, parent, false);
			viewHolder.tvUserName = (TextViewParserEmoji) convertView.findViewById(R.id.tv_member_name);
			viewHolder.userIcon = (ImageViewEx) convertView.findViewById(R.id.member_icon);
			viewHolder.bottomRightIcon = (ImageView) convertView.findViewById(R.id.bottom_right_icon);
			convertView.setTag(viewHolder);
			ViewGroup.LayoutParams lp = new FrameLayout.LayoutParams(imageSize.getWidth(), imageSize.getWidth());
			viewHolder.userIcon.setLayoutParams(lp);

		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}
		MemberEntity member = (MemberEntity) getItem(position);
		viewHolder.tvUserName.setEmojiText(member.getName());
		// viewHolder.tvUserName.setText(member.getName());
		// viewHolder.tvUserName.measure(MeasureSpec.UNSPECIFIED,
		// MeasureSpec.UNSPECIFIED);
		// double viewWidth = (double) viewHolder.tvUserName.getMeasuredWidth();
		// TextPaint paint = viewHolder.tvUserName.getPaint();
		// double textWidth = (double) paint.measureText(member.getName());
		// viewHolder.tvUserName.setEmojiText(member.getName(), viewWidth,
		// textWidth);

		viewHolder.setAvatar(member);
		viewHolder.bottomRightIcon.setTag(member);
		convertView.setTag(convertView.getId(), member);
		String role = member.getRole();
		if (role.equals(Consts.OWNER)) {
			viewHolder.bottomRightIcon.setVisibility(View.VISIBLE);
			viewHolder.bottomRightIcon.setImageResource(R.drawable.owner_bg);
		} else {
			if (isDelete == true) {
				viewHolder.bottomRightIcon.setVisibility(View.VISIBLE);
				viewHolder.bottomRightIcon.setImageResource(R.drawable.circle_delete_member);
				if (clickListener != null) {
					viewHolder.bottomRightIcon.setOnClickListener(clickListener);
				}
			} else {
				viewHolder.bottomRightIcon.setVisibility(View.INVISIBLE);
			}

		}

		return convertView;
	}

	private class ViewHolder {
		ImageViewEx userIcon = null;
		TextViewParserEmoji tvUserName = null;
		ImageView bottomRightIcon = null;

		public void setAvatar(MemberEntity member) {
			if (member == null) {
				return;
			}
			if (userIcon == null) {
				return;
			}
			userIcon.setImageBitmap(null);
			ImageManager.instance().loadAvater(userIcon, member.getUserId());
		}

	}

}
