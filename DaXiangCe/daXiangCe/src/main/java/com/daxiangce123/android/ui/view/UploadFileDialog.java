package com.daxiangce123.android.ui.view;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
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
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.activities.LocalImageActivity;
import com.daxiangce123.android.util.CapturePic;
import com.daxiangce123.android.util.DrawableUtils;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;

public class UploadFileDialog extends CDialog implements OnItemClickListener,
		android.view.View.OnClickListener {

	private String TAG = "UploadFileDialog";
	private GridView gridView;
	private Button mCancel;
	private UploadFileAdapter uploadFileAdapter;
	private final int[] drawables = { R.drawable.camera,
			R.drawable.local_photos };
	private final int[] strings = { R.string.shoot, R.string.local_photo };
	private Activity activity;
	private ArrayList<UploadImage> destPaths;
	private String albumId;
	private TextView tvTitle;

	public UploadFileDialog() {
		super();
		initDialog();
		initUI();
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
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

		uploadFileAdapter = new UploadFileAdapter();
		uploadFileAdapter.setWidth(width);
		gridView = (GridView) rootView
				.findViewById(R.id.gv_dialog_create_join_album);
		mCancel = (Button) rootView.findViewById(R.id.bt_cancel);
		mCancel.setTextColor(rootView.getResources().getColorStateList(
				R.color.clickable_blue_white));
		mCancel.setBackgroundResource(R.drawable.dialog_cancel_white);
		tvTitle = (TextView) rootView.findViewById(R.id.tv_title);
		mCancel.setOnClickListener(this);
		tvTitle.setText(R.string.upload_file);
		gridView.setNumColumns(numColums);
		gridView.setAdapter(uploadFileAdapter);
		gridView.setPadding(0, padding, 0, 0);
		gridView.setOnItemClickListener(this);
		gridView.setHorizontalSpacing(padding);
		gridView.setVerticalSpacing(padding / 2);
		gridView.setSelector(DrawableUtils.getDrawbale(0x00000000));

	}

	private void capturePicture() {
		String path = MediaUtil.getDestSaveDir()
				+ "/camera-"
				+ TimeUtil.formatTime(System.currentTimeMillis(),
						Consts.CAPTURE_IMAGE_TIME_FORMAT) + ".jpg";
		if (destPaths == null) {
			destPaths = new ArrayList<UploadImage>();
		}
		destPaths.add(new UploadImage(path,true));
		CapturePic.capturePic(activity, path);
	}

	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK) {
			return;
		}
		if (requestCode != Consts.REQUEST_CODE_CAMERA_IMAGE) {
			return;
		}
		if (Utils.isEmpty(destPaths) || Utils.isEmpty(albumId)) {
			return;
		}
		try {
			ImageManager.instance().uploadImage(destPaths, albumId);
			destPaths.clear();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void show(String albumId) {
		this.albumId = albumId;
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
		if (stringId == R.string.shoot) {
			capturePicture();
		} else if (stringId == R.string.local_photo) {
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
			intent.setClass(activity, LocalImageActivity.class);
			activity.startActivityForResult(intent,
					Consts.REQUEST_CODE_CHOOSE_IMAGE);
		}
		super.dismiss();
	}

	private class UploadFileAdapter extends BaseAdapter {
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
