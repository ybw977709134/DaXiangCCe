package com.daxiangce123.android.ui.view;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView.ScaleType;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time 2014-6-2
 * @author
 */
public class AlbumViewTest extends FrameLayout implements OnClickListener, OnItemClickListener {

	private final static String TAG = "AlbumView";
	private View ivPrivate;
	private TextView tvName;
	private TextView tvSize;
	private TextView tvMember;
	private TextView tvDistance;
	private TextView tvJoin;
	private TextView tvInviteCode;
	private GridView gvImages;
	private ImageViewEx ivCover;
	private Object data;
	private AlbumEntity album;
	private ImageSize mSize;
	private ImageSize thumbSize;
	private int numColumns = 4;
	private int spacing;
	private ImageAdapter adapter;
	private OnClickListener clickListener;
	private List<FileEntity> sampleFiles;
	private static boolean DEBUG = true;
	private Context mContext;
	private TempToken tempToken;
	private SamplePictureViewerDialog samplePictureViewerDialog;
	private boolean needPassword;

	public AlbumViewTest(Context context) {
		this(context, null);
	}

	public AlbumViewTest(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.mContext = context;
		if (App.DEBUG) {
			LogUtil.d(TAG, "mContext" + mContext);
		}
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
		initView();
	}

	private void initView() {
		spacing = Utils.getDip(10);
		adapter = new ImageAdapter();
		LayoutInflater.from(getContext()).inflate(R.layout.view_album_view, this);
		findViewById(R.id.tv_report).setOnClickListener(this);

		// init views
		ivPrivate = findViewById(R.id.iv_private);
		tvName = (TextView) findViewById(R.id.tv_title);
		ivCover = (ImageViewEx) findViewById(R.id.iv_album_cover_nearby);
		ivCover.setOnClickListener(this);
		tvSize = (TextView) findViewById(R.id.tv_album_size_nearby);
		tvMember = (TextView) findViewById(R.id.tv_album_member_nearby);
		tvDistance = (TextView) findViewById(R.id.tv_album_distance_nearby);
		gvImages = (GridView) findViewById(R.id.gv_images);
		tvJoin = (TextView) findViewById(R.id.tv_join_in);
		tvInviteCode = (TextView) findViewById(R.id.tv_invite_code);
		// init gridview
		gvImages.setOnItemClickListener(this);
		gvImages.setAdapter(adapter);
		gvImages.setNumColumns(numColumns);
		gvImages.setVerticalSpacing(spacing);
		gvImages.setHorizontalSpacing(spacing);
	}

	private FileEntity getSampleFile(int position) {
		int count = Utils.sizeOf(sampleFiles);
		if (position >= count) {
			return null;
		}
		return sampleFiles.get(position);
	}

	private void showImages() {
		if (adapter != null) {
			adapter.notifyDataSetChanged();
		}
		ivCover.setImageBitmap(null);
		FileEntity file = getSampleFile(0);
		if (file == null) {
			return;
		}
		// ImageManager.instance().loadSampleThumb(ivCover, file.getId(),
		// mSize);
		ImageManager.instance().load(ivCover, file.getId(), mSize);
		if (DEBUG) {
			LogUtil.d(TAG, "showImages()	file=" + (file != null));
		}
	}

	public void showReport(boolean show) {
		View v = findViewById(R.id.tv_report);
		if (v == null) {
			return;
		}
		int visibility = v.getVisibility();
		if (show) {
			if (visibility != VISIBLE) {
				v.setVisibility(VISIBLE);
			}
		} else {
			if (visibility == VISIBLE) {
				v.setVisibility(INVISIBLE);
			}
		}
	}

	public void setSample(List<FileEntity> sampleFiles) {
		if (this.sampleFiles == sampleFiles) {
			return;
		}
		this.sampleFiles = sampleFiles;
		showImages();
	}

	public void setImageSize(ImageSize size) {
		mSize = size;
		if (size == null) {
			return;
		}
		int width = (mSize.getWidth() - (numColumns * spacing)) / numColumns;
		thumbSize = new ImageSize(width, width);
		thumbSize.setHasThumbFile(true);
		thumbSize.setThumb(true);
		ViewGroup.LayoutParams vlp = new LayoutParams(mSize.getWidth(), mSize.getHeight());
		if (ivCover != null) {
			ivCover.setLayoutParams(vlp);
		}
	}

	public void setJoinClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
		if (tvJoin != null) {
			tvJoin.setOnClickListener(clickListener);
		}
	}

	public void setToken(TempToken token) {
		if (DEBUG) {
			LogUtil.d(TAG, "-----------------setToken()	" + token);
		}
		this.tempToken = token;
		if (samplePictureViewerDialog == null) {
			return;
		}
		samplePictureViewerDialog.setTempToken(token);
		if (samplePictureViewerDialog.isShowing()) {
			samplePictureViewerDialog.notifyDataSetChange();
		}
	}

	/**
	 * @time 2014-6-2
	 * @param obj
	 */
	public void setData(Object obj) {
		if (data == obj) {
			return;
		}
		data = obj;
		if (obj instanceof AlbumEntity) {
			album = (AlbumEntity) obj;
			tvDistance.setVisibility(View.GONE);
		} else if (obj instanceof NearbyAlbum) {
			NearbyAlbum nearbyAlbum = (NearbyAlbum) obj;
			album = nearbyAlbum.getAlbum();
			tvDistance.setVisibility(View.VISIBLE);
			String dis = "";
			if (nearbyAlbum.getDistance() < 0.1f) {
				dis = "100m";
			} else {
				if (nearbyAlbum.getDistance() < 1) {
					dis = (int) (nearbyAlbum.getDistance() * 1000) + "m";
				} else {
					dis = Utils.formatNum(nearbyAlbum.getDistance(), "#.#") + "km";
				}
			}
			tvDistance.setText("  " + dis);
		}
		if (album == null) {
			return;
		}
		CharSequence invCode = Html.fromHtml(Utils.getString(R.string.invite_code_is, album.getInviteCode()));
		tvInviteCode.setText(invCode);
		tvName.setText(album.getName());
		tvSize.setText("  " + album.getSize());
		tvMember.setText("  " + album.getMembers());
		tvJoin.setOnClickListener(clickListener);
		tvJoin.setTag(album);
		ivPrivate.setVisibility(View.GONE);
	}

	public void showPrivate(boolean isPrivate, boolean hasJoined) {
		needPassword = isPrivate;
		if (hasJoined) {
			tvJoin.setText(R.string.open_album);
		}
		if (isPrivate) {
			if (ivPrivate.getVisibility() != View.VISIBLE) {
				ivPrivate.setVisibility(View.VISIBLE);
			}
			if (!hasJoined) {
				tvJoin.setText(R.string.this_album_need_psword_to_access);
			}
		} else {
			if (ivPrivate.getVisibility() == View.VISIBLE) {
				ivPrivate.setVisibility(View.GONE);
			}
			if (!hasJoined) {
				tvJoin.setText(R.string.join_ablum);
			}
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

		// CToast.showToast("click gv item");

		if (needPassword) {
			CToast.showToast(R.string.this_album_need_psword_to_access);
			return;
		} else if (view.getTag() instanceof FileEntity) {

			viewImageDetail((FileEntity) view.getTag(), sampleFiles);
			if (DEBUG) {
				LogUtil.d(TAG, "position : " + position + "sampleFiles.get(position + 1)" + view.getTag());
			}
		}
	}

	@Override
	public void onClick(View v) {
		int id = v.getId();
		if (id == R.id.tv_report) {
			DialogUtils.dialog(R.string.we_ll_handle_report_later);
		} else if (id == R.id.iv_album_cover_nearby) {
			// CToast.showToast("click cover");
			if (needPassword) {
				CToast.showToast(R.string.this_album_need_psword_to_access);
				return;
			} else {
				viewImageDetail(getSampleFile(0), sampleFiles);
			}
		}
	}

	private void viewImageDetail(FileEntity entity, List<FileEntity> files) {
		if (entity == null || Utils.isEmpty(files)) {
			return;
		}

		int size = files.size();
		int i = 0;
		for (; i < size; i++) {
			FileEntity fileEntity = files.get(i);
			if (fileEntity.equals(entity)) {
				break;
			}
		}
		final int position = i;
		if (DEBUG) {
			LogUtil.d(TAG, "position " + position + "  i: " + i);
		}

		if (samplePictureViewerDialog == null) {
			samplePictureViewerDialog = new SamplePictureViewerDialog();
		}
		try {
			samplePictureViewerDialog.setTempToken(tempToken);
			samplePictureViewerDialog.setCurPosition(position);
			samplePictureViewerDialog.setFileList(files);
			samplePictureViewerDialog.show();
			UMutils.instance().diyEvent(ID.EventPreview);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private class ImageAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 4;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Holder holder = null;
			if (convertView == null) {
				holder = new Holder(parent.getContext());
				convertView = holder;
			} else {
				holder = (Holder) convertView;
			}
			holder.show(position);
			return convertView;
		}

		private class Holder extends FrameLayout {
			private ImageViewEx viewEx = null;

			public Holder(Context context) {
				super(context);
				int padding = Utils.getDip(3);
				ViewGroup.LayoutParams lp = new AbsListView.LayoutParams(thumbSize.getWidth(), thumbSize.getHeight());
				setLayoutParams(lp);
				setBackgroundColor(Color.WHITE);
				setPadding(padding, padding, padding, padding);
				viewEx = new ImageViewEx(context);
				viewEx.setBackgroundResource(R.drawable.default_image_small);
				viewEx.setScaleType(ScaleType.CENTER_CROP);
				ViewGroup.LayoutParams vlp = new FrameLayout.LayoutParams(thumbSize.getWidth(), thumbSize.getHeight());
				addView(viewEx, vlp);
			}

			private void show(int position) {
				position = position + 1;
				FileEntity file = getSampleFile(position);
				if (file == null) {
					return;
				}
				viewEx.setImageBitmap(null);
				// ImageManager.instance().loadSampleThumb(viewEx, file.getId(),
				// thumbSize);
				ImageManager.instance().load(viewEx, file.getId(), thumbSize);
				setTag(file);
			}
		}

	}

}
