package com.daxiangce123.android.ui.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.daxiangce123.R;

public class AddAlbumPopupAdapter extends BaseAdapter {
	private List<Integer> datas = null;
	private LayoutInflater mInflater = null;

	public AddAlbumPopupAdapter(Context context) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setData(List<Integer> datas) {
		this.datas = datas;
	}

	@Override
	public int getCount() {
		return datas.size();
	}

	@Override
	public Object getItem(int position) {
		return datas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView btnPopupbutton;
		if (convertView == null) {
			convertView = mInflater
					.inflate(R.layout.add_album_popup_item, null);
			btnPopupbutton = (TextView) convertView
					.findViewById(R.id.bt_add_album_item);
			btnPopupbutton.setText(datas.get(position));
		}
		return convertView;
	}

}
