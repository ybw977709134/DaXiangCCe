package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;

import java.util.ArrayList;
import java.util.List;

public class FriendAdapter extends BaseAdapter {

	public boolean showNewMemberOnly = false;
	private static final String TAG = "MemberAdapter";
	private Context mContext = null;
	private List<MemberEntity> memberList;
	private List<MemberEntity> newMemberList = new ArrayList<MemberEntity>();
	private AlbumEntity albumEntity;

	private ImageSize imageSize;
	private int showNewPosition = -1;
	private int showCountPosition = -1;
	private int newMemberCount = 0;
	private int memberCount = 0;

	private boolean isDisplayDeleteMember;
	private OnClickListener clickListener;
	private AbsListView parentView;
	private boolean DEBUG = true;

	public int getShowNewPosition() {
		return showNewPosition;
	}

	public void setShowNewPosition(int showNewPosition) {
		this.showNewPosition = showNewPosition;
	}

	public int getShowCountPosition() {
		return showCountPosition;
	}

	public void setShowCountPosition(int showCountPosition) {
		this.showCountPosition = showCountPosition;
	}

	public FriendAdapter(Context context) {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		mContext = context;
	}

	public AlbumEntity getAlbumEntity() {
		return albumEntity;
	}

	public void setAlbumEntity(AlbumEntity albumEntity) {
		this.albumEntity = albumEntity;
	}

	public List<MemberEntity> getMemberList() {
		return memberList;
	}

	public void setMemberList(List<MemberEntity> memberList) {
		this.memberList = memberList;
	}

	public ArrayList<MemberEntity> getNewMemberList() {
		return (ArrayList<MemberEntity>) newMemberList;
	}

	public int getNewMemberCount() {
		return newMemberCount;
	}

	public void setNewMemberCount(int newMemberCount) {
		this.newMemberCount = newMemberCount;
	}

	public void setNewMemberList(List<MemberEntity> newMemberList) {
		this.newMemberList = newMemberList;
	}

	public boolean isDisplayDeleteMember() {
		return isDisplayDeleteMember;
	}

	public void setDisplayDeleteMember(boolean isDisplayDeleteMember) {
		this.isDisplayDeleteMember = isDisplayDeleteMember;
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
		int count = 0;
		if (showNewMemberOnly) {
			// newMemberCount = 2;
			newMemberCount = albumEntity.getMembers() - albumEntity.getOldMembers();
			count = newMemberCount;
			showNewPosition = -99;
			showCountPosition = -1;
			if (count <= 0) {
				count = 0;
			}
		} else {
			if (albumEntity != null) {
				newMemberCount = albumEntity.getOldMembers() == 0 ? 0 : albumEntity.getMembers() - albumEntity.getOldMembers();
				if (newMemberCount < 0) {
					newMemberCount = 0;
				}
				memberCount = albumEntity.getMembers();
			}

			showNewPosition = 0;
			// if (newMemberCount > 0) {
			// showNewPosition = 0;
			// } else {
			// showNewPosition = -1;
			// }

			showCountPosition = (showNewPosition == 0) ? 1 : 0;
			count = memberList.size() + showCountPosition + 1;

		}
		return count;
	}

	public MemberEntity getItem(int position) {
		if (showNewMemberOnly) {
			return newMemberList.get(position);
		} else {
			position = position - 1 - (showCountPosition);
		}
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
		if (position == showNewPosition) {
			// show new
			convertView = LayoutInflater.from(mContext).inflate(R.layout.new_member_item, parent, false);
			TextView countTextView = (TextView) convertView.findViewById(R.id.tv_new_number);
			if (newMemberCount > 99) {
				countTextView.setBackgroundDrawable(App.getAppContext().getResources().getDrawable(R.drawable.image_update));
				countTextView.setText("");
			} else if (newMemberCount == 0) {
				countTextView.setVisibility(View.INVISIBLE);
			} else {
				countTextView.setText(String.valueOf(newMemberCount));
			}
			return convertView;

		} else if (position == showCountPosition) {

			// show member
			convertView = LayoutInflater.from(mContext).inflate(R.layout.member_count_item, parent, false);
			TextView countTextView = (TextView) convertView.findViewById(R.id.tv_member_count);
			countTextView.setText(Html.fromHtml(mContext.getString(R.string.members_count, memberCount)));
			return convertView;

		} else {
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
			// double viewWidth = (double) viewHolder.tvUserName
			// .getMeasuredWidth();
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
			// else {
			// if (isDisplayDeleteMember && deletePosition == position) {
			// viewHolder.deleteButton.setVisibility(View.VISIBLE);
			// viewHolder.deleteButton.setTag(member);
			// } else {
			// viewHolder.deleteButton.setVisibility(View.INVISIBLE);
			// }
			// }

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
