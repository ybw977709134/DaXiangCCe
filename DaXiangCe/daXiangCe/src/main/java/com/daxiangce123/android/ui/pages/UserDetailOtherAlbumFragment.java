package com.daxiangce123.android.ui.pages;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.AlbumSort;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.SimpleData;
import com.daxiangce123.android.data.SimpleDataImpl;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.activities.SampleAlbumDetailActivity;
import com.daxiangce123.android.ui.adapter.UserOtherAlbumAdapter;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

/**
 * @project Cliq
 * @time Mar 24, 2014
 * @author ram
 */
public class UserDetailOtherAlbumFragment extends BaseFragment implements OnClickListener, OnItemClickListener {

	private final static String TAG = "UserDetailFragment";
	private View contentView;
	private View emptyView;
	private PullToRefreshGridView mPullToRefreshGridView;
	private GridView gvImagies;
	private UserOtherAlbumAdapter imageAdapter;
	private Bitmap albumCover;
	private String curUserId;
	private boolean DEBUG = true;
	private List<AlbumEntity> mAlumList = null;
	private Map<String, AlbumEntity> albumMap;
	private int startPos;
	private boolean isJoined;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				Response response = intent.getParcelableExtra(Consts.RESPONSE);
				ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
				if (Consts.LIST_OTHER_USER_ALBUM.equals(action)) {
					if (response.getStatusCode() == 200) {
						onListAlbums(response, info);
					}
				} else if (Consts.GET_ALBUM_COVER.equals(action)) {
					if (response.getStatusCode() == 200) {
						updateAlbumCover(response, info);
					}
				} else if (Consts.DELETE_ALBUM.equals(action)) {
					onDeleteAlbum(response, info);
				} else if (Consts.LEAVE_ALBUM.equals(action)) {
					onLeaveAlbum(response, info);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};

	@Override
	public String getFragmentName() {
		return "UserDetailOtherAlbumFragment";
	}

	public UserDetailOtherAlbumFragment() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	@Override
	public View onCreateView(android.view.LayoutInflater inflater, ViewGroup container, android.os.Bundle savedInstanceState) {
		if (contentView == null) {
			contentView = inflater.inflate(R.layout.fragment_user_detail_other_album, container, false);
			// initSize();
			ConnectBuilder.listAlbum(curUserId, 0, 100, Sort.BY_MOD_DATE, Order.DESC);
			initUI();
			// updateAvater();
		} else {
			ViewUtil.removeFromParent(contentView);
		}
		LogUtil.d(TAG, "----------------------------------onCreateView-----------------------------------------------");
		initBroad();
		setDefaultCover();
		// initData();
		// updateUI();

		return contentView;
	};

	private void initBroad() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.LIST_OTHER_USER_ALBUM);
		ift.addAction(Consts.GET_ALBUM_COVER);
		ift.addAction(Consts.DELETE_ALBUM);
		ift.addAction(Consts.LEAVE_ALBUM);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void onListAlbums(Response response, ConnectInfo info) {
		if (startPos == 0) {
			albumMap.clear();
			if (mAlumList != null) {
				mAlumList.clear();
			}
		}

		ListAllAlbums listAllAlbums = Parser.parseAlbumList(response.getContent());
		String uid = info.getTag();

		if (Utils.existsEmpty(curUserId, uid) || !uid.equals(curUserId)) {
			return;
		}
		if (App.DEBUG) {
			LogUtil.d("onListAlbums", "onListAlbums	getStatusCode=" + response.getStatusCode());

		}
		if (listAllAlbums == null) {
			return;
		}
		LoadingDialog.dismiss();
		List<AlbumEntity> list = listAllAlbums.getAlbums();
		if (list == null) {
			return;
		}
		if (listAllAlbums.hasMore()) {
			startPos += list.size();
			ConnectBuilder.listAlbum(curUserId, startPos, 100, Sort.BY_MOD_DATE, Order.DESC);
		} else {
			startPos = 0;
		}
		try {
			for (AlbumEntity album : list) {
				albumMap.put(album.getId(), album);
				if (DEBUG) {
					LogUtil.d(TAG, " first album  : " + list.get(0).getId());
				}
				// ConnectBuilder.getAlbumCover(album.getId());
				ConnectBuilder.getAlbumCoverId(album.getId());
			}
			if (mAlumList == null) {
				mAlumList = list;
			} else {
				mAlumList.addAll(list);
				list.clear();
				list = null;
			}
			if (DEBUG) {
				LogUtil.d(TAG, " mAlumList  : " + mAlumList.size());
			}
			onUpdateAdapter(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void updateAlbumCover(Response response, ConnectInfo connectInfo) {
		if (response == null || connectInfo == null) {
			return;
		}
		int statusCode = response.getStatusCode();
		if (statusCode != 404 && statusCode != 200) {
			return;
		}
		String albumId = connectInfo.getTag();
		if (Utils.isEmpty(albumId)) {
			return;
		}
		if (mAlumList == null) {
			return;
		}
		AlbumEntity album = albumMap.get(albumId);
		if (album == null) {
			return;
		}
		if (statusCode == 200) {
			// AlbumSamples albumItems =
			// Parser.parseAlbumSamples(response.getContent(), false);
			// if (albumItems == null || Utils.isEmpty(albumItems.getFiles())) {
			// return;
			// }
			// FileEntity file = albumItems.getFiles().getFirst();
			// if (file == null) {
			// return;
			// }
			// String fileId = file.getId();
			String fileId = Parser.parseAlbumThumId(response.getContent());
			album.setLocalCover(fileId);
			if (imageAdapter != null) {
				imageAdapter.updateSingle(albumId);
			}
			if (DEBUG) {
				LogUtil.d("updateAlbumCoverFrag", "Fragment	updateAlbumCover ---	cover =  " + fileId + "  ----- " + album.getTrueCover() + "  time :   " + System.currentTimeMillis()
						+ "  -- albumID =   " + album.getId());
			}
		}
	}

	private void onDeleteAlbum(Response response, ConnectInfo info) {
		if (info == null || response == null) {
			return;
		}
		int statusCode = response.getStatusCode();
		if (statusCode == 200 || statusCode == 404) {
			String albumId = info.getTag();
			if (DEBUG) {
				LogUtil.d(TAG, "LEAVE_ALBUM userId = " + albumId);
			}
			if (onAlbumDeleted(albumId)) {
				CToast.showToast(R.string.delete_album_succeed);
				return;
			}
		}
		CToast.showToast(R.string.request_failed);
	}

	private void onLeaveAlbum(Response response, ConnectInfo info) {
		if (info == null || response == null) {
			return;
		}
		int statusCode = response.getStatusCode();
		if (statusCode == 200 || statusCode == 404) {
			String albumId = info.getTag();
			String userId = info.getTag2();

			if (!userId.equals(curUserId)) {
				return;
			}
			if (DEBUG) {
				LogUtil.d(TAG, "LEAVE_ALBUM userId = " + albumId);
			}
			if (onAlbumDeleted(albumId)) {
				CToast.showToast(R.string.quite_album_succeed);
				return;
			}
		}
		CToast.showToast(R.string.request_failed);
	}

	public boolean onAlbumDeleted(String albumId) {
		if (Utils.isEmpty(mAlumList) || Utils.isEmpty(albumId)) {
			return false;
		}
		AlbumEntity album = albumMap.remove(albumId);
		if (album == null) {
			return false;
		}
		mAlumList.remove(album);

		onUpdateAdapter(false);
		return true;
	}

	private void onUpdateAdapter(boolean sort) {

		LoadingDialog.dismiss();
		if (Utils.isEmpty(mAlumList)) {
			emptyView.setVisibility(View.VISIBLE);
		} else {
			emptyView.setVisibility(View.GONE);
		}
		imageAdapter.setData(mAlumList);
		imageAdapter.notifyDataSetChanged();

	}

	public void setDefaultCover() {
		Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.default_image_large);
		bitmap = BitmapUtil.squareBitmap(bitmap);
		albumCover = BitmapUtil.toRoundCorner(bitmap);
		albumCover = BitmapUtil.rotateOverlay(albumCover);
		if (imageAdapter != null) {
			imageAdapter.setCover(albumCover);
		}
	}

	private void initUI() {
		if (contentView == null) {
			return;
		}
		albumMap = new HashMap<String, AlbumEntity>();

		emptyView = contentView.findViewById(R.id.empty_view);

		mPullToRefreshGridView = (PullToRefreshGridView) contentView.findViewById(R.id.lv_images);
		mPullToRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				ConnectBuilder.listAlbum(curUserId, 0, 100, Sort.BY_MOD_DATE, Order.DESC);
				refreshView.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				// if (isLoadingMore) {
				// return;
				// }
				// updateData();
				// loadMore();
				// sendMessage(WHAT_ONREFRESHCOMPLETE, 50);
			}
		});
		mPullToRefreshGridView.setMode(Mode.PULL_FROM_START);

		gvImagies = mPullToRefreshGridView.getRefreshableView();

		final int numColums = 2;
		final int padding = Utils.getDip(20);
		// final int hSpaceing = padding;

		imageAdapter = new UserOtherAlbumAdapter(getActivity());
		imageAdapter.setClickListener(this);
		imageAdapter.setCover(albumCover);

		int gvWidth = App.SCREEN_WIDTH - padding * 2;
		int width = (gvWidth - (numColums - 1) * padding) / numColums;
		ImageSize imageSize = new ImageSize(width, width);
		imageSize.setThumb(true);
		imageAdapter.setImageSize(imageSize);

		gvImagies.setNumColumns(numColums);
		gvImagies.setAdapter(imageAdapter);
		gvImagies.setOnItemClickListener(this);
		gvImagies.setPadding(0, 0, 0, 0);
		// gvImagies.setHorizontalSpacing(hSpaceing);
		imageAdapter.setClickListener(this);

		// gvAlbums.setHorizontalSpacing(hSpaceing);
		// gvAlbums.setOnItemLongClickListener(this);
		// gvAlbums.setPadding(0, 0, padding, 0);

		SimpleData data = new SimpleDataImpl(Consts.BASIC_CONFIG);
		// int i = data
		// .getInt(Consts.ALBUM_SORT, AlbumSort.CREATED_DATE.ordinal());
		int i = data.getInt(Consts.ALBUM_SORT, AlbumSort.UPDATED_DATE.ordinal());
		// try {
		// mSort = AlbumSort.values()[i];
		// } catch (Exception e) {
		// e.printStackTrace();
		// // mSort = AlbumSort.CREATED_DATE;
		// mSort = AlbumSort.UPDATED_DATE;
		// }

		LoadingDialog.show(R.string.loading);

	}

	public void setUserId(String userId) {
		this.curUserId = userId;
	}

	public void setIsJoined(boolean isJoined) {
		this.isJoined = isJoined;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		openAlbum(mAlumList.get(position));
		UMutils.instance().diyEvent(ID.EventUserOtherAlbumPreview);

	}

	private void openAlbum(AlbumEntity albumEntity) {
		if (albumEntity == null) {
			return;
		}
		if (App.DEBUG) {
			LogUtil.d(TAG, "onOpenAlbum	albumEntity" + albumEntity.getName());
		}
		// unreadFileNum = unreadFileNum - albumEntity.getUpdateCount();
		// updateAlbumNotify();
		// albumEntity.setUpdateCount(0);
		if (isJoined) {
			DBHelper dbHelper = App.getDBHelper();
			if (dbHelper == null) {
				return;
			}
			dbHelper.execute("UPDATE " + albumEntity.getTableName() + " SET " + Consts.UPDATE_COUNT + "=0 WHERE " + Consts.ALBUM_ID + "=\"" + albumEntity.getId() + "\"");
		}

		onOpenAlbum(albumEntity);
	}

	public boolean onOpenAlbum(AlbumEntity albumEntity) {
		// Bundle bundle = new Bundle();
		// bundle.putString(Consts.ALBUM_ID, albumEntity.getId());
		// bundle.putParcelable(Consts.ALBUM, albumEntity);
		Intent intent = new Intent();
		intent.putExtra(Consts.ALBUM, albumEntity);
		intent.putExtra(Consts.ALBUM_ID, albumEntity.getId());
		intent.putExtra(Consts.EVENT_ID, UMutils.ID.EventJoinUserOtherAlbumSuccess);
		intent.setClass(App.getAppContext(), SampleAlbumDetailActivity.class);
		// UIManager.instance().startActivity(SampleAlbumDetailActivity.class,
		// bundle);
		startActivity(intent);
		return true;
	}

	@Override
	public void onResume() {
		// ConnectBuilder.listAlbum();
		// if (openAlbumListener != null) {
		// openAlbumListener.onOpenAlbum(curAlbumEntity);
		// }
		super.onResume();
	}

	@Override
	public void onDestroy() {
		Broadcaster.unregisterReceiver(receiver);
		super.onDestroy();
	}

}
