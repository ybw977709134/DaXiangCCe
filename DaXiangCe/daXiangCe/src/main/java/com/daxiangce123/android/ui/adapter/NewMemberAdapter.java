package com.daxiangce123.android.ui.adapter;

import java.util.List;

import android.content.Context;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.MeasureSpec;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;

public class NewMemberAdapter extends BaseAdapter {

	private static final String TAG = "MemberAdapter";
	private Context mContext = null;
	private List<MemberEntity> memberList;
	private ImageSize imageSize;
	private boolean isDisplayDeleteMember;
	private AbsListView parentView;
	private boolean DEBUG = true;

	public NewMemberAdapter(Context context) {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		mContext = context;
	}

	public List<MemberEntity> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<MemberEntity> memberList) {
		this.memberList = memberList;
	}

	public boolean isDisplayDeleteMember() {
		return isDisplayDeleteMember;
	}

	public void setDisplayDeleteMember(boolean isDisplayDeleteMember) {
		this.isDisplayDeleteMember = isDisplayDeleteMember;
	}

	public void setImageSize(ImageSize size) {
		this.imageSize = size;
	}

	public ImageSize getImageSize() {
		return imageSize;
	}

	public int getCount() {
		if (memberList != null) {
			return memberList.size();
		}
		return 0;
	}

	public MemberEntity getItem(int position) {
		return memberList.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (parentView == null && parent instanceof AbsListView) {
			parentView = (AbsListView) parent;
		}
		mContext = parent.getContext();
		ViewHolder viewHolder = null;
		if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
			viewHolder = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(R.layout.member_item, parent, false);
			viewHolder.tvUserName = (TextViewParserEmoji) convertView.findViewById(R.id.tv_member_name);
			viewHolder.userIcon = (ImageViewEx) convertView.findViewById(R.id.member_icon);
			viewHolder.bottomRightIcon = (ImageView) convertView.findViewById(R.id.bottom_right_icon);
			convertView.setTag(viewHolder);
			ViewGroup.LayoutParams lp = new RelativeLayout.LayoutParams((int) (imageSize.getWidth() / 1.5), (int) (imageSize.getWidth() / 1.5));
			viewHolder.userIcon.setLayoutParams(lp);
			// viewHolder.deleteButton = (Button) convertView
			// .findViewById(R.id.btn_member_delete);
			// viewHolder.deleteButton.setOnClickListener(clickListener);
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
			viewHolder.bottomRightIcon.setVisibility(View.INVISIBLE);
		}

		return convertView;
	}

	private class ViewHolder {
		ImageViewEx userIcon = null;
		TextViewParserEmoji tvUserName = null;
		ImageView bottomRightIcon = null;

		// Button deleteButton = null;

		public void setAvatar(MemberEntity member) {
			if (userIcon == null) {
				return;
			}
			userIcon.setImageBitmap(null);
			if (member == null) {
				return;
			}
			if (imageSize == null) {
				return;
			}
			ImageManager.instance().loadAvater(userIcon, member.getUserId());
		}
	}

}
