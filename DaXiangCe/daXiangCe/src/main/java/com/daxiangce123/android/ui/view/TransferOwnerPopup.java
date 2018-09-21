package com.daxiangce123.android.ui.view;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumMembers;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.adapter.TransferMemberAdapter;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DrawableUtils;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.UMutils.ID;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener2;
import com.handmark.pulltorefresh.library.PullToRefreshGridView;

public class TransferOwnerPopup extends PopupWindow implements OnClickListener, OnItemClickListener {

	private static final String TAG = "TransferOwnerPopup";
	private Context mContext;
	private View mRootView;
	private TextView mTitle;
	private ImageView mClose;
	private GridView mMemberGridView;
	private PullToRefreshGridView mPullRefreshGridView;
	private TransferMemberAdapter memberAdapter = null;
	private OnTransfrerOwnerClicked transfrerOwnerClickedListener;
	private List<MemberEntity> memberList;
	private MemberEntity curOwner;
	private String albumId = null;
	private boolean DEBUG = true;
	private AlbumMembers albumMembers;
	private boolean isLoading = false;
	private int startPos;
	private HashSet<String> userSet;

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			try {
				String action = intent.getAction();
				Response response = intent.getParcelableExtra(Consts.RESPONSE);
				String content = response.getContent();
				if (Consts.GET_ALBUM_MEMBERS.equals(action)) {
					if (response.getStatusCode() == 200) {

						showMemberList(content);
						// albumMembers = Parser.parseAlbumMembers(content);
						// LinkedList<MemberEntity> members = albumMembers
						// .getMembers();
						// if (DEBUG) {
						// LogUtil.d(TAG, "response :" + response);
						// }
						// if (memberList == null) {
						// memberList = new ArrayList<MemberEntity>();
						// }
						// if (!Utils.isEmpty(members)) {
						// memberList.clear();
						// for (MemberEntity entity : members) {
						// String role = entity.getRole();
						// if (role.equals(Consts.OWNER)) {
						// memberList.add(0, entity);
						// curOwner = entity;
						// } else {
						// memberList.add(entity);
						// }
						// }
						// if (DEBUG) {
						// LogUtil.d(TAG,
						// "albumMembers.size:" + members.size());
						// }
						// }
						// contactAdapter.setData(memberList);
						// contactAdapter.notifyDataSetChanged();
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	};
	private OnDismissListener dismissListener = new OnDismissListener() {
		@Override
		public void onDismiss() {
			Broadcaster.unregisterReceiver(receiver);
			if (transfrerOwnerClickedListener != null) {
				transfrerOwnerClickedListener.onTransferOwnerClicked();
			}
		}
	};

	public TransferOwnerPopup() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		memberList = new ArrayList<MemberEntity>();
	}

	public TransferOwnerPopup(Context context) {
		super(context);
		this.mContext = context;
		initPopupWindow();
		initBroadcast();
	}

	private void initPopupWindow() {
		userSet = new HashSet<String>();
		memberList = new ArrayList<MemberEntity>();

		mRootView = (View) LayoutInflater.from(mContext).inflate(R.layout.members_gridview, null, false);
		mClose = (ImageView) mRootView.findViewById(R.id.close_members);
		mClose.setOnClickListener(this);
		mTitle = (TextView) mRootView.findViewById(R.id.title_members);
		mTitle.setText(R.string.appoint_new_owner);

		int numColumns = 3;
		int padding = App.SCREEN_WIDTH / 15;
		mPullRefreshGridView = (PullToRefreshGridView) mRootView.findViewById(R.id.gv_members);
		mPullRefreshGridView.setOnRefreshListener(new OnRefreshListener2<GridView>() {

			@Override
			public void onPullDownToRefresh(PullToRefreshBase<GridView> refreshView) {
				refreshView.onRefreshComplete();
			}

			@Override
			public void onPullUpToRefresh(PullToRefreshBase<GridView> refreshView) {
				if (isLoading) {
					return;
				}
				if (albumMembers != null) {
					if (albumMembers.hasMore()) {
						isLoading = true;
						ConnectBuilder.getAlbumMembers(albumId, startPos, 30);
						LogUtil.d(TAG, "startPos:" + startPos);
					} else {
						startPos = 0;
						CToast.showToast(R.string.no_more);
						refreshView.onRefreshComplete();
					}
				}
			}

		});

		// mMemberGridView = (GridView) mRootView.findViewById(R.id.gv_members);
		mPullRefreshGridView.setMode(Mode.BOTH);

		mMemberGridView = mPullRefreshGridView.getRefreshableView();
		memberAdapter = new TransferMemberAdapter(this.mContext);
		memberAdapter.setData(memberList);
		mMemberGridView.setAdapter(memberAdapter);
		mMemberGridView.setOnItemClickListener(this);
		mMemberGridView.setNumColumns(numColumns);
		mMemberGridView.setHorizontalSpacing(padding);
		mMemberGridView.setVerticalSpacing(padding);

		mRootView.setPadding(padding, 0, padding, 0);
		int paddings = 2 * padding;
		int singleWidth = (App.SCREEN_WIDTH - (numColumns - 1) * padding - paddings) / numColumns;
		ImageSize imageSize = new ImageSize(singleWidth, singleWidth);
		imageSize.setCircle(true);
		memberAdapter.setImageSize(imageSize);

		// init
		setBackgroundDrawable(DrawableUtils.getDrawbale(0x00000000));
		setOnDismissListener(dismissListener);
		setAnimationStyle(R.style.AnimBottom);
		setHeight(LayoutParams.MATCH_PARENT);
		setWidth(LayoutParams.MATCH_PARENT);
		setOutsideTouchable(false);
		setContentView(mRootView);
		setFocusable(true);
	}

	private void initBroadcast() {
		IntentFilter ift = new IntentFilter();
		ift.addAction(Consts.GET_ALBUM_MEMBERS);
		Broadcaster.registerReceiver(receiver, ift);
	}

	private void showMemberList(String content) {
		LoadingDialog.dismiss();
		if (Utils.isEmpty(content)) {
			return;
		}
		isLoading = false;
		mPullRefreshGridView.onRefreshComplete();
		albumMembers = Parser.parseAlbumMembers(content);
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
		if (startPos == 0) {
			if (memberList != null) {
				memberList.clear();
			}
		}
		startPos = startPos + members.size();
		for (MemberEntity entity : members) {
			String role = entity.getRole();
			String uid = entity.getUserId();
			if (!userSet.add(uid)) {
				continue;
			}
			if (role.equals(Consts.OWNER)) {
				memberList.add(0, entity);
			} else {
				memberList.add(entity);
			}
		}

		// startPos = startPos + members.size();
		// for (MemberEntity entity : members) {
		// String role = entity.getRole();
		// String uid = entity.getUserId();
		// if (!userSet.add(uid)) {
		// continue;
		// }
		// if (!role.equals(Consts.OWNER)) {
		// memberList.add(entity);
		// }
		// }
		// if (App.DEBUG) {
		// LogUtil.d(TAG, "albumMembers.size:" + members.size());
		// }
		memberAdapter.setData(memberList);
		memberAdapter.notifyDataSetChanged();
	}

	public void updateOwner(String owner) {
		if (Utils.isEmpty(memberList)) {
			return;
		}
		if (curOwner != null) {
			if (curOwner.getUserId().equals(owner)) {
				return;
			}
			curOwner.setRole(Consts.MEMBER);
		}
		for (int i = 0; i < memberList.size(); i++) {
			MemberEntity memberEntity = memberList.get(i);
			if (memberEntity.getUserId().equals(owner)) {
				memberEntity.setRole(Consts.OWNER);
				curOwner = memberEntity;
				memberList.remove(i);
				memberList.add(0, memberEntity);
				memberAdapter.notifyDataSetChanged();
				return;
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.close_members) {
			dismiss();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		if (position == 0) {
			CToast.showToast(R.string.you_are_album_owner);
		} else {
			try {
				showTransferOwnerDialog(memberList.get(position));
			} catch (NoSuchFieldException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void showTransferOwnerDialog(final MemberEntity memberEntity) throws NoSuchFieldException, IllegalAccessException, IllegalArgumentException {
		AlertDialog.Builder transferOwner = new AlertDialog.Builder(mContext);
		transferOwner.setMessage(mContext.getString(R.string.confirm_to_transfer_album_to_x, memberEntity.getName()));
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					// destOwner = memberEntity;
					JSONObject jo = new JSONObject();
					jo.put(Consts.USER_ID, memberEntity.getUserId());
					jo.put(Consts.ALBUM_ID, memberEntity.getAlbumId());
					jo.put(Consts.ROLE, Consts.OWNER);
					ConnectBuilder.setMemberRole(jo.toString());
					dismiss();
				} else {
					// destOwner = null;
				}
				dialog.cancel();
			}
		};
		transferOwner.setPositiveButton(R.string.confirm, listener);
		transferOwner.setNegativeButton(R.string.cancel, listener);
		// transferOwner.show();
		AlertDialog transferOwnerDialog = transferOwner.show();
		TextView msg = (TextView) transferOwnerDialog.findViewById(android.R.id.message);
		msg.setGravity(Gravity.CENTER);

	}

	public void setData(String albumId) {
		this.albumId = albumId;
		ConnectBuilder.getAlbumMembers(albumId);
		LoadingDialog.show(R.string.loading);

		if (DEBUG) {
			LogUtil.d(TAG, "albumId:" + albumId);
		}
	}

	@Override
	public void showAsDropDown(View anchor) {
		onShow();
		super.showAsDropDown(anchor);
	}

	@Override
	public void showAsDropDown(View anchor, int xoff, int yoff) {
		onShow();
		super.showAsDropDown(anchor, xoff, yoff);
	}

	@Override
	public void showAtLocation(View parent, int gravity, int x, int y) {
		onShow();
		super.showAtLocation(parent, gravity, x, y);
	}

	private void onShow() {
		initBroadcast();
	}

	public OnTransfrerOwnerClicked getTransfrerOwnerClickedListener() {
		return transfrerOwnerClickedListener;
	}

	public void setTransfrerOwnerClickedListener(OnTransfrerOwnerClicked transfrerOwnerClickedListener) {
		this.transfrerOwnerClickedListener = transfrerOwnerClickedListener;
	}

	public interface OnTransfrerOwnerClicked {
		public void onTransferOwnerClicked();
	}

}
