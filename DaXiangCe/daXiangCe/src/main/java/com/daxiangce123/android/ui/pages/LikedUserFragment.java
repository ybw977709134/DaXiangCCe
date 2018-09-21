package com.daxiangce123.android.ui.pages;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;
import android.os.Bundle;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.ui.activities.UserDetailActivity;
import com.daxiangce123.android.ui.adapter.LikeUserAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

public class LikedUserFragment extends BaseFragment implements OnClickListener,
		OnItemClickListener {
	private static final String TAG = "LikedUserFragment";
	private View mRootView = null;
	private ImageView mBack;
	private PullToRefreshGridView mPullRefreshGridView;
	private GridView mMemberGridView;
	private LikeUserAdapter likedUserAdapter = null;
	private List<LikeEntity> likeList;
	private List<FileEntity> fileList;
	private FileEntity curFile;
	private String fileId;
	private boolean isLoading = false;
	private final int WHAT_ONREFRESHCOMPLETE = 1;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		if (mRootView == null) {
			mRootView = inflater.inflate(R.layout.fragmen_liked_user,
					container, false);
			initCompontent();
		} else {
			ViewUtil.removeFromParent(mRootView);
		}
		initData();
		return mRootView;
	}

	private void initCompontent() {
		mBack = (ImageView) mRootView.findViewById(R.id.iv_back);
		mBack.setOnClickListener(this);

		int numColumns = 3;
		int padding = App.SCREEN_WIDTH / 15;

		mPullRefreshGridView = (PullToRefreshGridView) mRootView
				.findViewById(R.id.gv_liked_user);
		mPullRefreshGridView
				.setOnRefreshListener(new OnRefreshListener2<GridView>() {

					@Override
					public void onPullDownToRefresh(
							PullToRefreshBase<GridView> refreshView) {
					}

					@Override
					public void onPullUpToRefresh(
							PullToRefreshBase<GridView> refreshView) {
						if (App.DEBUG) {
							LogUtil.d(TAG, "onPullUpToRefresh	isLoading="
									+ isLoading);
						}
						if (isLoading) {
							return;
						}
						if (hasMore()) {
							if (App.DEBUG) {
								LogUtil.d(TAG, "onPullUpToRefresh	hasMore()");
							}
							isLoading = true;
							ConnectBuilder.getLikeList(curFile.getId(),
									Utils.sizeOf(likeList), 30);
						} else {
							CToast.showToast(R.string.no_more);
							sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
						}
					}
				});

		mPullRefreshGridView.setMode(Mode.PULL_FROM_END);
		mMemberGridView = mPullRefreshGridView.getRefreshableView();
		likedUserAdapter = new LikeUserAdapter(this.getActivity());
		likedUserAdapter.setData(likeList);
		mMemberGridView.setAdapter(likedUserAdapter);
		mMemberGridView.setOnItemClickListener(this);
		mMemberGridView.setNumColumns(numColumns);
		mMemberGridView.setHorizontalSpacing(padding);
		mMemberGridView.setVerticalSpacing(padding);
		mMemberGridView.setPadding(padding, 15, padding, 15);
		int paddings = 2 * padding;
		int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings)
				/ numColumns;
		likedUserAdapter.setImageSize(singleWidth);
	}

	private void initData() {
		if (hasMore()) {
			ConnectBuilder.getLikeList(fileId, Utils.sizeOf(likeList), 30);
		}
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
			back();
		}
	}
 
	@Override
	public String getFragmentName() {
		return "LikedUserFragment";
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
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
		LinkedList<FileEntity> results = Utils.seperate(fileList,
				likeEntity.getUserId());
		if (App.DEBUG) {
			LogUtil.d(TAG, "openUserDetails->results" + results);
		}
		try {
			Intent intent = new Intent();
			intent.putExtra(Consts.IMAGE_LIST, results);
			intent.putExtra(Consts.ALBUM_ID, curFile.getAlbum());
			intent.putExtra(Consts.USER_ID, likeEntity.getUserId());
			intent.setClass(getActivity(), UserDetailActivity.class);
			intent.putExtra(Consts.TIME, System.currentTimeMillis());
			startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setFile(FileEntity fileEntity) {
		this.curFile = fileEntity;
		if (fileEntity != null) {
			fileId = fileEntity.getId();
		}
	}

	public void setFileList(List<FileEntity> list) {
		this.fileList = list;
	}

	public void setFileLikeList(List<LikeEntity> list) {
		this.likeList = list;
	}

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
