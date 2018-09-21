package com.daxiangce123.android.ui.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.daxiangce123.R;
import com.daxiangce123.android.ui.adapter.AddAlbumPopupAdapter;

public class SortImagePopup extends PopupWindow {
	private static final String TAG = "SortImagePopup";
	private Context mContext;
	private View mView;
	private ListView mSortImageList;
	private AddAlbumPopupAdapter mAddAlbumPopupAdapter;
	private ArrayList<Integer> mDatas;

	public SortImagePopup(Context context) {
		super(context);
		this.mContext = context;

		initData();

		initPopupWindow();
	}

	private void initPopupWindow() {
		mView = (View) LayoutInflater.from(mContext).inflate(
				R.layout.sort_image_popup, null, false);
		mSortImageList = (ListView) mView
				.findViewById(R.id.lv_sort_image_popup);
		mAddAlbumPopupAdapter = new AddAlbumPopupAdapter(mContext);

		mAddAlbumPopupAdapter.setData(mDatas);
		mSortImageList.setAdapter(mAddAlbumPopupAdapter);

		setWidth(LayoutParams.MATCH_PARENT);
		setHeight(LayoutParams.WRAP_CONTENT);
		setOutsideTouchable(false);
		// setBackgroundDrawable(Utils.getEmptyDrawable());
		setContentView(mView);
		setAnimationStyle(R.style.AnimBottom);
		setFocusable(true);
	}

	private void initData() {
		// TODO Auto-generated method stub
		mDatas = new ArrayList<Integer>();
		mDatas.add(R.string.comments_sort);
		mDatas.add(R.string.timeline_sort);
		mDatas.add(R.string.cancel);
	}

	public void setListener(OnItemClickListener listener) {
		// TODO Auto-generated method stub
		if (mSortImageList == null) {
			return;
		} else {
			mSortImageList.setOnItemClickListener(listener);
		}
	}

}
