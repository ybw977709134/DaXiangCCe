package com.daxiangce123.android.ui.pages;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.ListView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.ContactManager;
import com.daxiangce123.android.ui.activities.FriendActivity;
import com.daxiangce123.android.ui.adapter.ContactAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

public class ContactFragment extends BaseFragment implements OnItemClickListener, OnClickListener {

    private static final String TAG = "ContactFragment";
    private View mRootView;
    private ListView mMemberListView;
    public ContactAdapter contactAdapter = null;
    private ImageView mBack;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                String content = response.getContent();
                LogUtil.d(TAG, "response = " + response);
                if (Consts.GET_ALBUM_MEMBERS_DESC.equals(action)) {
                    showMemberListDESC(content);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void showMemberListDESC(String content) {
        LoadingDialog.dismiss();
    }

    public ContactFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initBroadcast();
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.contact_listview, container, false);
            initComponent();
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
//        checkEmpty();
        loadData();
        return mRootView;
    }

    /**
     * load data from
     */
    public void loadData() {
        ContactManager.getInstance().syncToServer2(true, this);
    }

    public void showEmptyView(boolean show) {
        if (show) {
            mRootView.findViewById(R.id.empty_view).setVisibility(View.VISIBLE);
            mMemberListView.setVisibility(View.GONE);
        } else {
            if (mRootView.findViewById(R.id.empty_view).getVisibility() != View.GONE)
                mRootView.findViewById(R.id.empty_view).setVisibility(View.GONE);
            if (mMemberListView.getVisibility() != View.VISIBLE)
                mMemberListView.setVisibility(View.VISIBLE);
        }
    }

    public void initComponent() {
        mBack = (ImageView) mRootView.findViewById(R.id.back);
        mBack.setOnClickListener(this);
        mMemberListView = (ListView) mRootView.findViewById(R.id.ll_contact);
        contactAdapter = new ContactAdapter(getActivity());
        contactAdapter.setData(ContactManager.getInstance().getNoMatchContact(), ContactManager.getInstance().getMatchContact());
        mMemberListView.setAdapter(contactAdapter);
        ViewUtil.ajustMaximumVelocity(mMemberListView, Consts.DEFAUTL_ABS_SCROLL_RATION);
        int numColumns = 3;
        int padding = App.SCREEN_WIDTH / 15;
        int paddings = 2 * padding;
        int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings) / numColumns;
        ImageSize imageSize = new ImageSize(singleWidth, singleWidth);
        imageSize.setCircle(true);
        contactAdapter.setImageSize(imageSize);
        contactAdapter.setClickListener(this);
        contactAdapter.notifyDataSetChanged();
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        Broadcaster.registerReceiver(receiver, ift);
    }


    @Override
    public boolean onBackPressed() {
        getActivity().finish();
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        try {
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onShown() {
        super.onShown();
    }

    private void showLoadingDialog() {
        LoadingDialog.show(R.string.loading);
    }

    @Override
    public String getFragmentName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.back) {
            getActivity().finish();
        }
        Object tag = v.getTag();
        if (tag instanceof Contact) {
            onViewInviteClicked((Contact) tag);
        }

    }

    private void onViewInviteClicked(Contact contact) {
        if (contact.isRegister()) {
            ((FriendActivity) getActivity()).showUserOtherAlbum(contact.getReg_user_id());
        } else {
            Utils.sendViaSMS(getActivity(), getString(R.string.invite_by_sms), contact.getContact());
        }
    }

}
