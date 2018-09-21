package com.daxiangce123.android.ui.activities;


import android.os.Bundle;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.InviteFriendsFragment;

public class InviteFriendsActivity extends BaseCliqActivity {
	public final static String TAG = "InviteFriendsActivity";
	private InviteFriendsFragment inviteFriendsFragment;
	// private String inviteCode;
	private AlbumEntity albumEntity;


	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// App.addActivity(this);
		setContentView(R.layout.activity_invite_friends);
		initData();
		showViewer();
	}

	private void initData() {
		try {
			Bundle bundle = this.getIntent().getExtras();
			// inviteCode = bundle.getString(Consts.INVITE_CODE);
			if (bundle.containsKey(Consts.ALBUM)) {
				albumEntity = bundle.getParcelable(Consts.ALBUM);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void showViewer() {
		if (inviteFriendsFragment == null) {
			inviteFriendsFragment = new InviteFriendsFragment();
		}
		if (albumEntity != null) {
			inviteFriendsFragment.setAlbumEntity(albumEntity);
		} 
		inviteFriendsFragment.show(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}
}
