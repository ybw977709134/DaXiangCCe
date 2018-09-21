package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.daxiangce123.R;
import com.daxiangce123.android.data.AlbumEntity;

public class EmptyAlbumAdapter extends BaseAdapter {

    public static final String TAG = "EmptyAlbumAdapter";

    private Context mContext = null;

    public EmptyAlbumAdapter(Context context) {
        mContext = context;
    }


    // JUST show the first 5 likes
    @Override
    public int getCount() {
        return 1;
    }

    @Override
    public AlbumEntity getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.adapter_empty_album_view, parent, false);
        }

        return convertView;
    }

}
