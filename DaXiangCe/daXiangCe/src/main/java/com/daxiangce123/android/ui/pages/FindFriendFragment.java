package com.daxiangce123.android.ui.pages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.ui.activities.FriendActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.ViewUtil;

public class FindFriendFragment extends BaseFragment implements OnClickListener {
    private View mRootView = null;
    private ImageView mBack;
    private TextView title, center_content;
    private ImageView center_logo;
    private Button bottomButton;
    private UserInfo userInfo = App.getUserInfo();

    @Override
    public String getFragmentName() {
        return "FindFriendFragment";
    }

    public FindFriendFragment() {
        setBoottomBarVisibility(View.GONE);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.fragment_find_friend, container,
                    false);
            initUI();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initUI();
        return mRootView;
    }

    private void initUI() {
        if (mBack == null) {
            mBack = (ImageView) mRootView.findViewById(R.id.iv_about_us_back);
            mBack.setOnClickListener(this);
        }
        title = (TextView) mRootView.findViewById(R.id.title);
        center_content = (TextView) mRootView.findViewById(R.id.tv_center_content);
        bottomButton = (Button) mRootView.findViewById(R.id.tv_bottom_button);
        bottomButton.setOnClickListener(this);
        center_logo = (ImageView) mRootView.findViewById(R.id.iv_center_logo);
        if (userInfo.isBindMobile()) {
            center_content.setText(R.string.play_with_friends);
            center_logo.setImageDrawable(getResources().getDrawable(R.drawable.play_with_friend_icon));
            bottomButton.setText(R.string.view_friend_in_contacts);
        } else {
            center_content.setText(R.string.binding_phone_guide_content);
            center_logo.setImageDrawable(getResources().getDrawable(R.drawable.binding_phone_guide_icon));
            bottomButton.setText(R.string.binding_phone);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_about_us_back) {
            back();
        } else if (id == R.id.tv_bottom_button) {
            onBottmoButtonClick();
        }
    }

    public void onBottmoButtonClick() {
        if (userInfo.isBindMobile()) {
            ((FriendActivity) getActivity()).showFriends();
            AppData.sethasUserAgreeToReadContact(true);
            AppData.setShowContactGruid(false);
            UMutils.instance().diyEvent(UMutils.ID.EventCheckFriend);
        } else {
            UMutils.instance().diyEvent(UMutils.ID.EventTourBindingMobile);
            ((FriendActivity) getActivity()).showBindMobile();
        }
    }
}
