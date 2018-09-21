package com.daxiangce123.android.ui.adapter;

import java.util.List;
import java.util.Map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.DividerView;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time 2014-6-1
 * @author
 */
public class AlbumSearchAdapter extends BaseAdapter {

	private List<AlbumEntity> albums;

	private ImageSize size;

	private Map<String, String> coverMap;

	public AlbumSearchAdapter(Map<String, String> coverMap) {
		this.coverMap = coverMap;
		int width = Utils.getDip(50);
		size = new ImageSize(width, width);
		size.setHasThumbFile(true);
		size.setThumb(true);
	}

	public void setAlbums(List<AlbumEntity> albums) {
		this.albums = albums;
	}

	@Override
	public int getCount() {
		if (albums == null) {
			return 0;
		}
		return albums.size();
	}

	@Override
	public AlbumEntity getItem(int position) {
		if (albums == null) {
			return null;
		}
		return albums.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder = null;
		DividerView view = null;

		if (convertView == null) {
			view = new DividerView(parent.getContext());
			LayoutInflater.from(parent.getContext()).inflate(
					R.layout.adapter_nearby_album, view);

			holder = new Holder(view);
			view.setTag(holder);
			convertView = view;
		} else {
			holder = (Holder) convertView.getTag();
			view = (DividerView) convertView;
		}

		if (position == getCount() - 1 || position == 0) {
			view.setBottomMarginLeft(0);
		} else {
			int margin = Utils.getDip(10);
			view.setBottomMarginLeft(margin);
		}

		AlbumEntity nearyAlbum = getItem(position);
		holder.setAlbum(nearyAlbum);
		return view;
	}

	private class Holder {
		ImageViewEx ivCover;
		TextView tvName;
		TextView tvSize;
		TextView tvMember;
		TextView tvDistance;

		public Holder(View parent) {
			if (parent == null) {
				return;
			}
			ivCover = (ImageViewEx) parent
					.findViewById(R.id.iv_album_cover_nearby);
			tvName = (TextView) parent.findViewById(R.id.tv_album_name_nearby);
			tvSize = (TextView) parent.findViewById(R.id.tv_album_size_nearby);
			tvMember = (TextView) parent
					.findViewById(R.id.tv_album_member_nearby);
			tvDistance = (TextView) parent
					.findViewById(R.id.tv_album_distance_nearby);
			tvDistance.setVisibility(View.GONE);

			ViewGroup.LayoutParams flp = new FrameLayout.LayoutParams(
					size.getWidth(), size.getHeight());
			ivCover.setLayoutParams(flp);
		}

		public void setAlbum(AlbumEntity album) {
			if (album == null) {
				return;
			}
			tvName.setText(album.getName());
			tvSize.setText("  " + album.getSize());
			tvMember.setText("  " + album.getMembers());
			String coverId = null;
			if (coverMap != null) {
				coverId = coverMap.get(album.getId());
			}
			ivCover.setImageBitmap(null);
			ImageManager.instance().load(ivCover, coverId, size);
		}

	}

}
