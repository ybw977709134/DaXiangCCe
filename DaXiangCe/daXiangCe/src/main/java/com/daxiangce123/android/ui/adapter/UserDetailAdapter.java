package com.daxiangce123.android.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.ui.view.PhotoView;
import com.daxiangce123.android.util.SparseArray;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time Mar 26, 2014
 * @author ram
 */
public class UserDetailAdapter extends BaseAdapter {

	protected final String TAG = "UserDetailListAdapter";
	private SparseArray<SparseArray<FileEntity>> datas;
	private ArrayList<SparseArray<FileEntity>> timeLineList;
	private int numColums;
	private ImageSize imageSize;
	private OnClickListener clickListener;
	private Context mContext;
	private int gridSpacing;
	private HashMap<String, View> photoMap = new HashMap<String, View>();

	public UserDetailAdapter(Context context, int numColum) {
		this.mContext = context;
		this.numColums = numColum;
	}

	public void setImageSize(ImageSize imageSize) {
		this.imageSize = imageSize;
	}

	public void setSpacing(int space) {
		gridSpacing = space;
	}

	public void setData(SparseArray<SparseArray<FileEntity>> datas) {
		this.datas = datas;
		updateTimeList();
	}

	public void setClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

	/**
	 * whenever data is changed. This must be called before
	 * {@link #notifyDataSetChanged()}.
	 * <p>
	 * If {@link #setData(HashMap, HashSet, SparseArray)} is called. No NEED TO
	 * CALL THIS
	 * 
	 * @time Apr 17, 2014
	 * 
	 */
	public void updateTimeList() {
		if (timeLineList == null) {
			timeLineList = new ArrayList<SparseArray<FileEntity>>();
		} else {
			timeLineList.clear();
		}
		if (datas == null) {
			return;
		}
		int batchSize = datas.size();
		for (int i = 0; i < batchSize; i++) {
			// DESC
			SparseArray<FileEntity> sa = datas.valueAntiAt(i);
			if (sa == null) {
				continue;
			}
			final int fileSize = sa.size();
			for (int j = 0; j < fileSize; j++) {
				SparseArray<FileEntity> l = new SparseArray<FileEntity>();
				while (j < fileSize && l.size() < numColums) {
					FileEntity file = sa.valueAntiAt(j);
					long timeInMills = TimeUtil.toLong(file.getCreateDate(), Consts.SERVER_UTC_FORMAT);
					l.put(timeInMills, file, false);
					if (l.size() >= numColums) {
						break;
					}
					j++;
				}
				if (l.size() == 0) {
					l = null;
					continue;
				}
				timeLineList.add(l);
			}
		}
	}

	public boolean containSize(ImageSize size) {
		if (size == null) {
			return false;
		}
		if (size == imageSize) {
			return true;
		}
		return false;
	}

	private long getIndexTime(int position) {
		int count = getCount();
		if (position < 0 || position >= count) {
			return -1;
		}
		SparseArray<FileEntity> sa = getItem(position);
		if (sa == null) {
			return -1;
		}
		return sa.keyAntiAt(0);
	}

	@Override
	public int getCount() {
		if (timeLineList == null) {
			return 0;
		}
		return timeLineList.size();
	}

	@Override
	public SparseArray<FileEntity> getItem(int position) {
		if (position >= getCount() || position < 0) {
			return null;
		}
		return timeLineList.get(position);
	}

	public void showPhoto(String id) {
		if (Utils.isEmpty(id)) {
			return;
		}
		if (photoMap == null || photoMap.isEmpty()) {
			return;
		}
		View v = photoMap.get(id);
		if (v == null) {
			return;
		}
		if (v instanceof PhotoView) {
			PhotoView photoView = (PhotoView) v;
			if (id.equals(photoView.getFileId())) {
				if (photoView.showPhoto()) {
					photoMap.remove(id);
				}
			}
		}
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// initSizes(parent);
		ViewHolder viewHolder = null;
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.adapter_user_detail_item, null);
			viewHolder = new ViewHolder(convertView);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		long dayIndex = getIndexTime(position);
		SparseArray<FileEntity> array = getItem(position);
		viewHolder.setData(dayIndex, array);
		boolean show = viewHolder.showDateIndexer(position);
		if (show) {
			viewHolder.llDate.setTag("" + position + " time:"
					+ viewHolder.tvTitle.getText());
		}
		return convertView;
	}

	// private class ViewHoler
	private class ViewHolder {
		/**
		 * parent of {@link HeaderHolder#ivDay} and {@link HeaderHolder#tvDate}
		 */
		private View llDate;
		/**
		 * parent which contains all the photo
		 */
		private LinearLayout llPhotos;
		private TextView tvTitle;

		public ViewHolder(View parent) {
			if (parent == null) {
				return;
			}
			parent.setPadding(0, 0, 0, gridSpacing);
			llDate = parent.findViewById(R.id.ll_date);
			llPhotos = (LinearLayout) parent.findViewById(R.id.ll_user_photos);
			tvTitle = (TextView) parent.findViewById(R.id.title);
		}

		private void setData(long keyIndex, SparseArray<FileEntity> sparseArray) {
			if (sparseArray == null) {
				return;
			}
			int size = sparseArray.size();
			if (size <= 0) {
				return;
			}
			int viewCount = llPhotos.getChildCount();
			if (viewCount > size) {
				for (int i = 0; i < viewCount - size; i++) {
					View view = llPhotos.getChildAt(viewCount - i - 1);
					view.setVisibility(View.GONE);
				}
			} else if (viewCount < size) {
				for (int i = 0; i < size - viewCount; i++) {
					PhotoView photoView = new PhotoView(mContext);
					llPhotos.addView(photoView);
				}
			}

			for (int i = 0; i < size; i++) {
				// llPhotos has a right padding which has a size gridSpacing
				final int paddingRight = i == size - 1 ? 0 : gridSpacing;
				LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(
						imageSize.getWidth() + paddingRight,
						imageSize.getHeight());

				FileEntity file = sparseArray.valueAntiAt(i);
				PhotoView photoView = (PhotoView) llPhotos.getChildAt(i);
				photoView.setPadding(0, 0, paddingRight, 0);
				photoView.setOnClickListener(clickListener);
				photoView.setVisibility(View.VISIBLE);
				photoView.setData(imageSize, file);
				photoView.setLayoutParams(llp);
				photoView.setTag(file);

				if (photoView.showPhoto()) {
					photoMap.remove(file.getId());
				} else {
					photoMap.put(file.getId(), photoView);
				}
			}

			if (sparseArray == null || sparseArray.size() == 0) {
				return;
			}
			String date = sparseArray.valueAt(0).getCreateDate();
			long timeInMills = TimeUtil.toLong(date, Consts.SERVER_UTC_FORMAT);
			String time = TimeUtil.humanizeDate(timeInMills);
			tvTitle.setText(time);
		}

		private boolean showDateIndexer(int position) {
			if (position < 0) {
				return false;
			}
			if (position == 0) {
				llDate.setVisibility(View.VISIBLE);
			} else {
				long millis = getIndexTime(position);
				long timeInDays = TimeUtil.toDay(millis);

				long lastMillis = getIndexTime(position - 1);
				long lastTimeInDays = TimeUtil.toDay(lastMillis);

				if (lastTimeInDays != timeInDays) {
					llDate.setVisibility(View.VISIBLE);
				} else {
					llDate.setVisibility(View.GONE);
					return false;
				}
			}
			return true;
		}

	}

}
