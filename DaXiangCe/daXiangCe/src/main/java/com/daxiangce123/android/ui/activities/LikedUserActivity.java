package com.daxiangce123.android.ui.activities;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.FileLike;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.ui.adapter.LikeUserAdapter;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

public class LikedUserActivity extends BaseCliqActivity implements OnClickListener, OnItemClickListener {
	private static final String TAG = "LikedUserFragment";
	private ImageView mBack;
	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mMemberGridView;
	private LikeUserAdapter likedUserAdapter = null;
	private List<LikeEntity> likeList;
	// private List<FileEntity> fileList;
	private FileEntity curFile;
	// private String fileId;
	private boolean isLoading = false;
	private final int WHAT_ONREFRESHCOMPLETE = 1;
	private HashMap<String, LikeEntity> likeMap;
	private boolean isJoined;
	private AlbumEntity albumEntity;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {

			try {
				String action = intent.getAction();
				ConnectInfo connectInfo = intent.getParcelableExtra(Consts.REQUEST);
				Response response = intent.getParcelableExtra(Consts.RESPONSE);
				String content = response.getContent();

				if (Consts.GET_LIKE.equals(action)) {
					String fileId = connectInfo.getTag();
					if (curFile != null && curFile.getId().equals(fileId)) {
						showLikeList(content);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.fragmen_liked_user);
		initCompontent();
		initData();
		initBroadcast();
	}

	private void initBroadcast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.GET_LIKE);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void initCompontent() {

		likeList = new LinkedList<LikeEntity>();
		likeMap = new HashMap<String, LikeEntity>();
		mBack = (ImageView) this.findViewById(R.id.iv_back);
		mBack.setOnClickListener(this);

		int numColumns = 3;
		int padding = App.SCREEN_WIDTH / 15;

		mPullRefreshGridView = (PullToRefreshGridView) this.findViewById(R.id.gv_liked_user);
		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				if (App.DEBUG) {
					LogUtil.d(TAG, "onPullUpToRefresh	isLoading=" + isLoading);
				}
				if (isLoading) {
					return;
				}
				if (hasMore()) {
					if (App.DEBUG) {
						LogUtil.d(TAG, "onPullUpToRefresh	hasMore()");
					}
					isLoading = true;
					ConnectBuilder.getLikeList(curFile.getId(), Utils.sizeOf(likeList), 30);
				} else {
					CToast.showToast(R.string.no_more);
					sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
				}
			}
		});

		mPullRefreshGridView.setMode(Mode.PULL_FROM_END);
		mMemberGridView = mPullRefreshGridView.getRefreshableView();
		likedUserAdapter = new LikeUserAdapter(this);
		likedUserAdapter.setData(likeList);
		mMemberGridView.setAdapter(likedUserAdapter);
		mMemberGridView.setOnItemClickListener(this);
		mMemberGridView.setNumColumns(numColumns);
		mMemberGridView.setHorizontalSpacing(padding);
		mMemberGridView.setVerticalSpacing(padding);
		mMemberGridView.setPadding(padding, 15, padding, 15);
		int paddings = 2 * padding;
		int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings) / numColumns;
		likedUserAdapter.setImageSize(singleWidth);
	}

	private void initData() {
		try {
			Intent intent = getIntent();
			if (intent.hasExtra(Consts.FILE)) {
				curFile = intent.getParcelableExtra(Consts.FILE);
			}
			if (intent.hasExtra(Consts.IS_JOINED)) {
				isJoined = intent.getBooleanExtra(Consts.IS_JOINED, false);
			}

			if (intent.hasExtra(Consts.ALBUM)) {
				albumEntity = intent.getParcelableExtra(Consts.ALBUM);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		// if (hasMore()) {
		ConnectBuilder.getLikeList(curFile.getId(), Utils.sizeOf(likeList), 30);
		LoadingDialog.show(R.string.loading);
		// }

	}

	private void showLikeList(String content) {
		LoadingDialog.dismiss();
		FileLike fileLike = Parser.parseFileLike(content);
		if (fileLike == null) {
			return;
		}
		List<LikeEntity> l = fileLike.getLike();
		if (Utils.isEmpty(l)) {
			return;
		}
		for (LikeEntity like : l) {
			String uid = like.getUserId();
			if (!likeMap.containsKey(uid)) {
				likeMap.put(uid, like);
				likeList.add(like);
			}
		}
		onLoadLikedUser();
	}

	public void onLoadLikedUser() {
		isLoading = false;
		if (App.DEBUG) {
			LogUtil.d(TAG, "onLoadLikedUser");
		}
		sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
		likedUserAdapter.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.iv_back) {
			finish();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (likeList != null && position < likeList.size()) {
			onOpenUserDetails(likeList.get(position));
		}

	}

	private void onOpenUserDetails(LikeEntity likeEntity) {
		if (App.DEBUG) {
			LogUtil.d(TAG, " openUserDetails->userId" + likeEntity.getUserId());
		}
		if (likeEntity == null) {
			return;
		}
		try {
			Intent intent = new Intent();
			intent.putExtra(Consts.ALBUM_ID, curFile.getAlbum());
			intent.putExtra(Consts.USER_ID, likeEntity.getUserId());
			if (!isJoined) {
				intent.putExtra(Consts.ALBUM, albumEntity);
			}
			intent.putExtra(Consts.IS_JOINED, isJoined);
			intent.setClass(this, UserDetailActivity.class);
			intent.putExtra(Consts.TIME, System.currentTimeMillis());
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// public void setFile(FileEntity fileEntity) {
	// this.curFile = fileEntity;
	// if (fileEntity != null) {
	// fileId = fileEntity.getId();
	// }
	// }

	// public void setFileList(List<FileEntity> list) {
	// this.fileList = list;
	// }
	//
	// public void setFileLikeList(List<LikeEntity> list) {
	// this.likeList = list;
	// }

	@Override
	public void handleMessage(Message msg) {
		int what = msg.what;
		if (App.DEBUG) {
			LogUtil.d(TAG, "handleMessage	what=" + what);
		}
		if (what == WHAT_ONREFRESHCOMPLETE) {
			mPullRefreshGridView.onRefreshComplete();
		}
	}

	private boolean hasMore() {
		if (curFile != null) {
			int size = Utils.sizeOf(likeList);
			if (size < curFile.getLikes()) {
				return true;
			}
		}
		return false;
	}

}
