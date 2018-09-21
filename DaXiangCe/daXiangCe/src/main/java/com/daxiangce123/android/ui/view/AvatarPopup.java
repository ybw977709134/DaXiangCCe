package com.daxiangce123.android.ui.view;

import java.util.ArrayList;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.daxiangce123.R;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.ui.adapter.AddAlbumPopupAdapter;

public class AvatarPopup extends PopupWindow implements OnItemClickListener{
	private static final String TAG = "AvatarPopup";
	private Context mContext;
	private View mView;
	private ListView mList;
	private AddAlbumPopupAdapter mAddAlbumPopupAdapter;
	private ArrayList<Integer> mDatas;
	private OptionListener optionListener;

	public AvatarPopup(Context context) {
		super(context);
		this.mContext = context;

		initData();

		initPopupWindow();
	}

	private void initPopupWindow() {
		// TODO Auto-generated method stub
		mView = (View) LayoutInflater.from(mContext).inflate(
				R.layout.avatar_popup, null, false);
		mList = (ListView) mView.findViewById(R.id.lv_avatar_popup);
		mAddAlbumPopupAdapter = new AddAlbumPopupAdapter(mContext);

		mAddAlbumPopupAdapter.setData(mDatas);
		mList.setAdapter(mAddAlbumPopupAdapter);
		mList.setOnItemClickListener(this);
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
		mDatas.add(R.string.check_avatar);
		mDatas.add(R.string.modify);
		mDatas.add(R.string.cancel);
	}

	public void setListener(OptionListener listener) {
		optionListener = listener;
	}

	public void show(View rootView) {
		showAtLocation(rootView, Gravity.BOTTOM, 0, 0);
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		if (optionListener == null) {
			return;
		}
		optionListener.OnOptionClick(position, mDatas.get(position),
				null);
	}

}

