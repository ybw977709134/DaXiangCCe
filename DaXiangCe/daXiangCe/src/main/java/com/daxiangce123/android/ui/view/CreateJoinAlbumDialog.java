package com.daxiangce123.android.ui.view;

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.text.InputType;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.activities.ZXingActivity;
import com.daxiangce123.android.ui.pages.CreateAlbumFragment;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.DrawableUtils;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.UMutils.ID;

public class CreateJoinAlbumDialog extends CDialog implements
		OnItemClickListener, android.view.View.OnClickListener {

	private String TAG = "CreateJoinAlbumDialog";
	private GridView gridView;
	private Button mCancel;
	private CreateJoinAlbumAdapter createJoinAlbumAdapter;
	private List<AlbumEntity> mAlumList;
	private final int[] drawables = { R.drawable.album_id, R.drawable.qr,
			R.drawable.create_new_album };
	private final int[] strings = { R.string.input_invite_code,
			R.string.scan_code, R.string.new_ablum };
	private TextView tvTitle;
	private CreateAlbumFragment createAlbumFragment;

	public CreateJoinAlbumDialog() {
		super();
		initDialog();
		initUI();
	}

	private void initDialog() {
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
		window.setWindowAnimations(R.style.AnimBottom);
		window.setGravity(Gravity.BOTTOM);

	}

	private void initUI() {
		final int dialogWidth = App.SCREEN_WIDTH;
		final int numColums = 4;
		int widthRatio = 3;
		int padding = dialogWidth / (numColums * widthRatio + numColums + 1);
		int width = widthRatio * padding;

		LayoutParams lp = new LayoutParams(dialogWidth,
				LayoutParams.WRAP_CONTENT);
		View rootView = LayoutInflater.from(App.getActivity()).inflate(
				R.layout.dialog_create_join_album, null);
		rootView.setPadding(padding, padding, padding, padding * 3 / 2);
		setContentView(rootView, lp);

		createJoinAlbumAdapter = new CreateJoinAlbumAdapter();
		createJoinAlbumAdapter.setWidth(width);
		gridView = (GridView) rootView
				.findViewById(R.id.gv_dialog_create_join_album);
		mCancel = (Button) rootView.findViewById(R.id.bt_cancel);
		mCancel.setTextColor(rootView.getResources().getColorStateList(
				R.color.clickable_blue_white));
		mCancel.setBackgroundResource(R.drawable.dialog_cancel_white);
		tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		mCancel.setOnClickListener(this);
		tvTitle.setText(R.string.join_create_album);
		gridView.setNumColumns(numColums);
		gridView.setAdapter(createJoinAlbumAdapter);
		gridView.setPadding(0, padding, 0, 0);
		gridView.setOnItemClickListener(this);
		gridView.setHorizontalSpacing(padding);
		gridView.setVerticalSpacing(padding / 2);
		gridView.setSelector(DrawableUtils.getDrawbale(0x00000000));
	}

	public void show(List<AlbumEntity> mAlumList) {
		this.mAlumList = mAlumList;
		show();
	}

	@Override
	public void onClick(View v) {
		if (v.equals(mCancel)) {
			super.dismiss();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (position >= strings.length) {
			return;
		}
		Context context = parent.getContext();
		int stringId = (int) strings[position];
		if (stringId == R.string.input_invite_code) {

			UMutils.instance().diyEvent(ID.EventJoinAlbumByInviteCode);
			joninAlbum();
		} else if (stringId == R.string.scan_code) {
			UMutils.instance().diyEvent(ID.EventJoinAlbumByQrCode);
			Activity activity = null;
			if (context instanceof Activity) {
				activity = (Activity) context;
			}
			if (activity == null) {
				if (App.DEBUG) {
					LogUtil.d(TAG, "activity is null");
				}
				return;
			}
			Intent intent = new Intent();
			intent.setClass(activity, ZXingActivity.class);
			activity.startActivityForResult(intent, Consts.REQUEST_CODE_ZXING);
		} else if (stringId == R.string.new_ablum) {
			UMutils.instance().diyEvent(ID.EventCreateAlbum);
			BaseCliqActivity activity = null;
			if (context instanceof BaseCliqActivity) {
				activity = (BaseCliqActivity) context;
			}
			if (activity == null) {
				return;
			}
			createAlbumFragment = new CreateAlbumFragment();
			createAlbumFragment.show(activity);

		}
		super.dismiss();

	}

	private void joninAlbum() {
		final EditText etInputId = new EditText(this.getContext());
		etInputId.setInputType(InputType.TYPE_CLASS_NUMBER);

		AlertDialog.Builder joinDialog = DialogUtils.create();
		joinDialog.setTitle(R.string.join_ablum);
		joinDialog.setMessage(R.string.pls_input_album_invite_code);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					String inviteCode = etInputId.getText().toString();
					if (Utils.isEmpty(inviteCode)) {
						CToast.showToast(R.string.havet_input);
					} else {
						if (!isInviteCodeExist(inviteCode)) {
							JSONObject jo = new JSONObject();
							jo.put(Consts.USER_ID, App.getUid());
							ConnectBuilder.joinAlbum(inviteCode,
									jo.toJSONString());
						} else {
							CToast.showToast(R.string.album_exists);
						}
					}
				}
				dialog.cancel();
			}
		};
		joinDialog.setPositiveButton(R.string.confirm, listener);
		joinDialog.setNegativeButton(R.string.cancel, listener);
		joinDialog.setView(etInputId);
		joinDialog.show();

	}

	private boolean isInviteCodeExist(String inviteCode) {
		if (Utils.isEmpty(inviteCode)) {
			return false;
		}
		try {

			for (AlbumEntity info : mAlumList) {
				if (info == null) {
					continue;
				}
				if (inviteCode.equals(info.getInviteCode())) {
					return true;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private class CreateJoinAlbumAdapter extends BaseAdapter {
		private int width;

		@Override
		public int getCount() {
			if (strings == null) {
				return 0;
			}
			return strings.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		public void setWidth(int width) {
			this.width = width;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			AbsListView.LayoutParams absLp = new AbsListView.LayoutParams(
					width, android.widget.AbsListView.LayoutParams.WRAP_CONTENT);
			int textSize = (int) getContext().getResources().getDimension(
					R.dimen.text_size_tiny);
			int paddingTop = Utils.dp2px(getContext(), 5);
			ColorStateList colors = getContext().getResources()
					.getColorStateList(R.color.clickable_grey);
			int stringRes = strings[position];
			LinearLayout ll = new LinearLayout(getContext());
			ll.setOrientation(LinearLayout.VERTICAL);
			ll.setGravity(Gravity.CENTER_HORIZONTAL);
			ll.setLayoutParams(absLp);

			ImageView iv = new ImageView(getContext());
			iv.setImageResource(drawables[position]);

			TextView textView = new TextView(getContext());
			TextPaint textPaint = textView.getPaint();
			textView.setSingleLine();
			textView.setText(stringRes);
			textView.setTextColor(colors);
			textPaint.setTextSize(textSize);
			textView.setGravity(Gravity.CENTER);
			textView.setPadding(0, paddingTop, 0, 0);

			ll.addView(iv);
			ll.addView(textView);
			return ll;
		}
	}

}
