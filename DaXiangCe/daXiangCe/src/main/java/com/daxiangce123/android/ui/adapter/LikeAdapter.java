package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.util.Utils;

import java.util.List;

public class LikeAdapter extends BaseAdapter {

    public static final String TAG = "LikeAdapter";

    private Context mContext = null;
    private List<LikeEntity> dataList = null;
    private int imageSize;

    public LikeAdapter(Context context) {
        mContext = context;
    }

    public void setData(List<LikeEntity> dataList) {
        this.dataList = dataList;
    }

    public void setImageSize(int size) {
        this.imageSize = size;
    }

    // JUST show the first 5 likes
    @Override
    public int getCount() {
        int size = Utils.sizeOf(dataList);
        if (size > 5) {
            return 5;
        }
        return size;
    }

    @Override
    public LikeEntity getItem(int position) {
        if (dataList == null) {
            return null;
        } else {
            return dataList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
//        ImageViewEx ivAvater = null;
//		if (convertView == null) {
//			convertView = ivAvater = new ImageViewEx(mContext);
//			AbsListView.LayoutParams alp = new AbsListView.LayoutParams(
//					imageSize, imageSize);
//			ivAvater.setLayoutParams(alp);
//		} else {
//			ivAvater = (ImageViewEx) convertView;
//		}
//		ivAvater.setImageBitmap(null);
//		// ivAvater.setScaleType(ScaleType.CENTER_CROP);
//		LikeEntity like = getItem(position);
//		convertView.setTag(convertView.getId(), like);
//		if (like != null) {
//
//			ImageManager.instance().loadAvater(ivAvater, like.getUserId());
//		}
        ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.like_avater_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        LikeEntity like = getItem(position);
        convertView.setTag(convertView.getId(), like);
        holder.setAvatar(like);

        return convertView;
    }

    private class ViewHolder {
        private ImageViewEx ivAvater;

        public ViewHolder(View parent) {
            ivAvater = (ImageViewEx) parent.findViewById(R.id.iv_avater);
            LinearLayout.LayoutParams flp = new LinearLayout.LayoutParams(imageSize, imageSize);
            flp.setMargins(3, 0, 3, 0);
            flp.weight = 1;
            ivAvater.setLayoutParams(flp);
        }

        public void setAvatar(LikeEntity like) {
            if (like == null) {
                return;
            }
            if (ivAvater == null) {
                return;
            }
            LikeEntity oldEntity = (LikeEntity) ivAvater.getTag();
            if ((oldEntity == null) || !(oldEntity.getUserId().equals(like.getUserId()))) {
                ivAvater.setTag(like);
                showAvatar(like.getUserId());
            }
        }

        public void showAvatar(String userId) {
            ivAvater.setImageBitmap(null);
            ImageManager.instance().loadAvater(ivAvater, userId);
        }

    }

}
