package com.daxiangce123.android.ui.pages;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumMembers;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.NewMemberAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshListView;

public class NewMemberFragment extends BaseFragment implements OnItemClickListener, OnClickListener {

	private static final String TAG = "MemberFragment";
	private static final int WHAT_ONREFRESHCOMPLETE = 1;
	private Context mContext;
	private View mRootView;
	private int newMemberCount = 0;
	private AlbumEntity albumEntity;
	private PullToRefreshListView mPullRefreshListView;
	private ListView mMemberListView;
	private NewMemberAdapter memberAdapter = null;
	private List<MemberEntity> memberList;
	protected OnTimeLineHeaderActionListener albumActivityActionListener;

	private boolean isLoading = false;
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
		isLoading = false;
		sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
		AlbumMembers albumMembers = Parser.parseAlbumMembers(content);
		if (albumMembers == null) {
			return;
		}
		LinkedList<MemberEntity> members = albumMembers.getMembers();
		if (Utils.isEmpty(members)) {
			if (App.DEBUG) {
				LogUtil.d(TAG, " members is empty");
			}
			return;
		} else {
			if (App.DEBUG) {
				LogUtil.d(TAG, " members size is " + members.size());
			}
		}
		ArrayList<MemberEntity> memberList = new ArrayList<MemberEntity>();
		for (MemberEntity entity : members) {
			if (!entity.getRole().equals(Consts.OWNER)) {
				memberList.add(entity);
			}
		}
		memberAdapter.setMemberList(memberList);
		memberAdapter.notifyDataSetChanged();
	}

	public NewMemberFragment() {
		memberList = new ArrayList<MemberEntity>();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.mContext = getActivity();
		initBroadcast();
	}

	@Override
	public void handleMessage(Message msg) {
		int what = msg.what;
		if (what == WHAT_ONREFRESHCOMPLETE) {
			mPullRefreshListView.onRefreshComplete();
		}
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.members_listview, container, false);
			initComponent();
		} else {
			ViewUtil.removeFromParent(mRootView);
		}
		initComponent();
		if (albumActivityActionListener == null) albumActivityActionListener = (OnTimeLineHeaderActionListener) getActivity();
		return mRootView;
	}

	public void initComponent() {
		if (memberList == null) memberList = new ArrayList<MemberEntity>();

		mPullRefreshListView = (PullToRefreshListView) mRootView.findViewById(R.id.gv_members);
		mRootView.findViewById(R.id.iv_invite_number).setVisibility(View.GONE);
		mPullRefreshListView.setMode(Mode.PULL_FROM_END);
		mMemberListView = mPullRefreshListView.getRefreshableView();
		memberAdapter = new NewMemberAdapter(this.mContext);
		memberAdapter.setMemberList(memberList);
		mMemberListView.setAdapter(memberAdapter);
		mMemberListView.setOnItemClickListener(this);
		ViewUtil.ajustMaximumVelocity(mMemberListView, Consts.DEFAUTL_ABS_SCROLL_RATION);
        int numColumns = 3;
        int padding = App.SCREEN_WIDTH / 15;
		int paddings = 2 * padding;
		int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings) / numColumns;
		ImageSize imageSize = new ImageSize(singleWidth, singleWidth);
		imageSize.setCircle(true);
		memberAdapter.setImageSize(imageSize);
		memberAdapter.notifyDataSetChanged();
		if (newMemberCount <= 0) {
			CToast.showToast(R.string.no_new_member);
		} else {
			ConnectBuilder.getAlbumMembersDESC(albumEntity.getId(), 1, newMemberCount > 100 ? 100 : newMemberCount);
			showLoadingDialog();
		}
	}

	private void initBroadcast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.GET_ALBUM_MEMBERS_DESC);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void onOpenUserDetails(MemberEntity memberEntity) {
		if (memberEntity.getUserId() == null) {
			return;
		}
		if (albumEntity == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
			intent.putExtra(Consts.USER_ID, memberEntity.getUserId());
			intent.setClass(App.getActivity(), UserDetailActivity.class);
			intent.putExtra(Consts.TIME, System.currentTimeMillis());
			mContext.startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onBackPressed() {
		albumActivityActionListener.showMembers();
		return true;
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		try {
			onOpenUserDetails(memberAdapter.getItem(position - 1));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setData(AlbumEntity albumEntity, List<MemberEntity> memberEntities, int count) {
		if (albumEntity == null) {
			return;
		}
		this.albumEntity = albumEntity;
		this.newMemberCount = count;
		this.memberList = memberEntities;
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
		// TODO Auto-generated method stub

	}
}
