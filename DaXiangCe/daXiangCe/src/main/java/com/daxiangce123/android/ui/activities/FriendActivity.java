package com.daxiangce123.android.ui.activities;

import android.os.Bundle;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.pages.ContactFragment;
import com.daxiangce123.android.ui.pages.FindFriendFragment;
import com.daxiangce123.android.ui.pages.UserDetailOtherAlbumFragmentForContact;

/**
 * for add new user
 */
public class FriendActivity extends BaseCliqActivity {

    protected final static String TAG = "FriendActivity";

    protected UserInfo info = App.getUserInfo();
    protected FindFriendFragment findFriendFragment;
    protected ContactFragment contactFragment;
    protected UserDetailOtherAlbumFragmentForContact udoaf;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend);
        try {
            Bundle bundle = this.getIntent().getExtras();
            if (bundle != null) {
                info = (UserInfo) bundle.getSerializable(Consts.USER);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        veryinit();
    }

    private void veryinit() {
        if (AppData.hasShowUserContactGruid()) {
            if (findFriendFragment == null) {
                findFriendFragment = new FindFriendFragment();
            }
            findFriendFragment.show(this);
        } else {
            showFriends();
        }
    }

    public void showUserOtherAlbum(String userid) {
//        if (udoaf == null) {
        udoaf = new UserDetailOtherAlbumFragmentForContact();
//        }
        udoaf.setUserId(userid);
        udoaf.show(this);
    }


    @Override
    protected void onDestroy() {
        contactFragment = null;
        super.onDestroy();
    }

    public void showBindMobile() {
        setResult(RESULT_OK);
        finish();
    }

    public void showFriends() {
        if (contactFragment == null) {
            contactFragment = new ContactFragment();
        }
        contactFragment.show(this);
    }
}
