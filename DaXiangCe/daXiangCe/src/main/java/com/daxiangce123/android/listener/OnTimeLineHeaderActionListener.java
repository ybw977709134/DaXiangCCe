package com.daxiangce123.android.listener;

import java.util.List;

import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.MemberEntity;

public interface OnTimeLineHeaderActionListener {

	public static final int STATE_SHOW_MEMBER = 1;
	public static final int STATE_SHOW_ALBUM = 2;
	public static final int STATE_HIDE = 3;

	public void setTabBarState(int state, boolean showDayNight);

	public void setBottomBarState(boolean invisible, boolean animate);

	public void onJoinClicked();

	public void onUploadPhotoClicked();

	public void onSortClicked();

	public void onDeleteClicked();

	public void onShareClicked(boolean isGuide);

	public void onQRCodeClicked();

	public void showMembers();

	public void showAlbumPhotos();

	public void showNewMember(List<MemberEntity> memberEntities, int count);

	public void setBottomDeleteBtnsState();

	public void onFileCancel(FileEntity file);

}
