package com.daxiangce123.android.ui.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.FileUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

/**
 * @project DaXiangCe
 * @time Apr 16, 2014
 * @author ram
 */
public class PhotoView extends FrameLayout {

	protected final static String TAG = "PhotoView";
	private Context mContext;
	public ImageView imageView;
	private TextView tvComment;
	private TextView tvLikes;
	private View ivSelect;
	private ProgressBar pbUploading;
	private RelativeLayout rlUploadingBar;
	private FileEntity fileEntity;
	private ImageSize imageSize;
	private View ivIndicator;
	private static DisplayImageOptions options;
	private static Bitmap defCover;
	private static Drawable drawable;

	public PhotoView(Context context) {
		this(context, null);
	}

	public PhotoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
		initView();
	}

	private void initView() {
		LayoutInflater.from(mContext).inflate(R.layout.album_detail_grid_view_item, this, true);
		imageView = (ImageView) findViewById(R.id.iv_image_item);
		imageView.setScaleType(ScaleType.CENTER_CROP);
		tvLikes = (TextView) findViewById(R.id.tv_like_num);
		tvComment = (TextView) findViewById(R.id.tv_message_num);
		pbUploading = (ProgressBar) findViewById(R.id.pb_uploading);
		rlUploadingBar = (RelativeLayout) findViewById(R.id.rl_uploading);
		ivSelect = findViewById(R.id.tv_select);
		ivIndicator = findViewById(R.id.iv_indicator);
		if (defCover == null || defCover.isRecycled() || drawable == null || options == null) {
			defCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_image_normal);
			drawable = new BitmapDrawable(mContext.getResources(), defCover);
			options = UILUtils.getDiaplayOption().showImageOnFail(drawable).showImageForEmptyUri(drawable).showImageOnLoading(new BitmapDrawable(defCover)).build();
		}

		// bm = BitmapUtil.squareBitmap(bm);
		// defCover = BitmapUtil.toRoundCorner(bm);
		// defCover = BitmapUtil.rotateOverlay(defCover);

	}

	public void setData(ImageSize imageSize, FileEntity file) {
		this.imageSize = imageSize;
		this.fileEntity = file;
		if (App.DEBUG) {
			Log.d(TAG, "setData	" + imageSize);
		}
		if (file == null) {
			return;
		}
		if (tvComment == null || tvLikes == null || imageView == null) {
			return;
		}
		int comments = file.getComments();
		if (comments > 0) {
			tvComment.setText(" " + comments);
			tvComment.setVisibility(View.VISIBLE);
		} else {
			tvComment.setVisibility(View.GONE);
		}
		int likes = file.getLikes();
		if (likes > 0) {
			tvLikes.setText(" " + likes);
			tvLikes.setVisibility(View.VISIBLE);
		} else {
			tvLikes.setVisibility(View.GONE);
		}

		if (FileUtil.isVideoByMime(file.getMimeType())) {
			ivIndicator.setVisibility(View.VISIBLE);
		} else {
			ivIndicator.setVisibility(View.GONE);
		}
		checkUploading();
	}

	public void checkUploading() {
		if (fileEntity == null) return;
		if (fileEntity.isUploading()) {
			showLoading();
		} else {
			dismissLoading();
		}
	}

	public void dismissLoading() {
		pbUploading.setVisibility(View.GONE);
		rlUploadingBar.setVisibility(View.GONE);
	}

	public void showLoading() {
		pbUploading.setVisibility(View.VISIBLE);
		rlUploadingBar.setVisibility(View.VISIBLE);
	}

	@SuppressLint("NewApi")
	public boolean checked(boolean selected) {
		if (selected) {
			ivSelect.setVisibility(View.VISIBLE);
			ivSelect.setBackground((new ColorDrawable(0xb20794e1)));
			dismissLoading();
		} else {
			ivSelect.setVisibility(View.GONE);
			checkUploading();
		}
		return false;
	}

	public String getFileId() {
		if (fileEntity == null) {
			return null;
		}
		return fileEntity.getId();
	}

	public boolean showSamplePhoto() {
		if (fileEntity == null) {
			return false;
		}
		// Bitmap bitmap = ImageManager.instance().getImage(imageView,
		// fileEntity.getId(), imageSize);
		// if (bitmap == null) {
		// imageView.setImageBitmap(null);
		// return false;
		// }

		String fileId = (String) imageView.getTag();
		if ((fileId == null) || !(fileId.equals(fileEntity.getId()))) {
			imageView.setTag(fileEntity.getId());
			imageView.setImageBitmap(defCover);
			ImageManager.instance().load(imageView, getFileId(), imageSize, false, options);
		}

		return true;
	}

	public void resetLoadingSize(int width, int height) {
		pbUploading.getLayoutParams().width = width / 2;
		pbUploading.getLayoutParams().height = height / 2;
		ivIndicator.getLayoutParams().width = width / 2;
		ivIndicator.getLayoutParams().height = height / 2;
	}

	public boolean showPhoto() {
		if (fileEntity == null) {
			return false;
		}

		String fileId = (String) imageView.getTag();
		if ((fileId == null) || !(fileId.equals(fileEntity.getId()))) {
			imageView.setTag(fileEntity.getId());
			imageView.setImageBitmap(defCover);
			ImageManager.instance().load(imageView, fileEntity, imageSize, options);
		}

		return true;
	}
}
