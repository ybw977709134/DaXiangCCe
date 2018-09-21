package com.daxiangce123.android.ui.adapter;

import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.TextPaint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

public class GroupAlbumGridViewAdapter extends BaseAdapter {

    protected final static String TAG = "GroupAlbumGridViewAdapter";
    private Context mContext = null;
    private List<AlbumEntity> list;
    private boolean isDelete;
    private AbsListView parentView;
    private OnClickListener clickListener;
    private ImageSize imageSize;
    private ImageSize targetSize;
    // private int spacing;
    private DisplayImageOptions options;
    private HashMap<String, ViewHolder> holderMap;
    private Bitmap defCover;
    private boolean isSearchMode;

    public GroupAlbumGridViewAdapter(Context context) {
        mContext = context;
        holderMap = new HashMap<String, ViewHolder>();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setCover(Bitmap bitmap) {
        defCover = bitmap;
        if (defCover == null) {
            Bitmap bm = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_image_large);
            bm = BitmapUtil.squareBitmap(bm);
            defCover = BitmapUtil.toRoundCorner(bm);
            defCover = BitmapUtil.rotateOverlay(defCover);
        }
        Drawable drawable = new BitmapDrawable(mContext.getResources(), defCover);
        options = UILUtils.getDiaplayOption().showImageOnFail(drawable).showImageForEmptyUri(drawable).showImageOnLoading(new BitmapDrawable(defCover)).build();
        // .preProcessor(new BitmapProcessor() {
        // @Override
        // public Bitmap process(Bitmap bitmap) {
        // if (bitmap == null) {
        // return null;
        // }
        // try {
        // Bitmap reBmp = BitmapUtil.getThumbnail(bitmap, targetSize.getWidth(),
        // targetSize.getHeight());
        // Bitmap rouBitmap = BitmapUtil.toRoundCorner(reBmp);
        // Bitmap bmp = BitmapUtil.rotateOverlay(rouBitmap);
        // BitmapUtil.recycle(bitmap, reBmp, rouBitmap);
        // return bmp;
        // } catch (Exception e) {
        // e.printStackTrace();
        // }
        // // catch (OutOfMemoryError e) {
        // // e.printStackTrace();
        // // }
        // return bitmap;
        // }
        // })
    }

    @Override
    public void notifyDataSetChanged() {
        holderMap.clear();
        super.notifyDataSetChanged();
    }

    // public void setSpacing(int spacing) {
    // this.spacing = spacing;
    // }

    public void setImageSize(ImageSize imageSize) {
        this.imageSize = imageSize;
    }

    public void setData(List<AlbumEntity> list) {
        this.list = list;
    }

    public void isSearchMode(boolean isSearchMode) {
        this.isSearchMode = isSearchMode;
    }

    public void setIsDelete(boolean isDelete) {
        this.isDelete = isDelete;
        notifyDataSetChanged();
    }

    public ImageSize getImageSize() {
        return imageSize;
    }

    @Override
    public int getCount() {

        // if (isSearchMode) {
        if (Utils.isEmpty(list)) {
            return 0;
        } else {
            return list.size();
        }
        // }

        // else {
        //
        // if (Utils.isEmpty(list)) {
        // return 1;
        // } else {
        // return list.size() + 1;
        // }
        // }
    }

    @Override
    public AlbumEntity getItem(int position) {
        // if (isSearchMode) {
        if (position >= getCount()) {
            return null;
        }
        return list.get(position);
        // }

        // else {
        //
        // if (position >= getCount() - 1) {
        // return null;
        // }
        // return list.get(position);
        // }
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean updateSingle(String albumId) {
        ViewHolder holder = holderMap.get(albumId);
        if (holder == null) {
            return false;
        }
        if (!albumId.equals(holder.albumEntity.getId())) {
            return false;
        }
        holder.setData(holder.albumEntity);
        return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (parentView == null && parent instanceof AbsListView) {
            parentView = (AbsListView) parent;
        }
        mContext = parent.getContext();

        // if (position == getCount() - 1 && !isSearchMode) {// the last one is
        // // just add
        // convertView = LayoutInflater.from(mContext).inflate(
        // R.layout.add_album_item, parent, false);
        // // LayoutParams llp = new LayoutParams(imageSize.getWidth(),
        // // imageSize.getHeight());
        // // convertView.setLayoutParams(llp);
        // } else {
        ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.group_album_grid_view_item, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        AlbumEntity album = getItem(position);
        convertView.setTag(convertView.getId(), album);
        holder.updateDeleteMode();
        holder.setData(album);
        holderMap.put(album.getId(), holder);
        // }

        return convertView;
    }

    private class ViewHolder {
        View rlInfo;
        View rlSubParent;
        ImageViewEx ivCover;
        TextView tvMembers;
        TextView tvNotify;
        TextViewParserEmoji tvName;
        TextView tvSize;
        View vDelete;
        AlbumEntity albumEntity;

        public ViewHolder(View parent) {
            ivCover = (ImageViewEx) parent.findViewById(R.id.iv_album_item);
            ivCover.setDefBitmap(defCover);

            tvName = (TextViewParserEmoji) parent.findViewById(R.id.tv_album_item_name);
            rlInfo = parent.findViewById(R.id.ll_album_item_info);
            tvNotify = (TextView) parent.findViewById(R.id.tv_top_right_corner_icon);
            vDelete = parent.findViewById(R.id.tv_bottom_right_corner_icon);
            vDelete.setOnClickListener(clickListener);

            tvSize = (TextView) parent.findViewById(R.id.tv_photo_num);
            tvMembers = (TextView) parent.findViewById(R.id.tv_member_num);
            rlSubParent = parent.findViewById(R.id.rl_album_bg);

            int margin = Utils.getDip(5);
            int w = imageSize.getWidth() - margin * 2;
            int h = imageSize.getHeight() - margin * 2;

            if (targetSize == null) {
                targetSize = new ImageSize(imageSize);
                targetSize.setWidth(w);
                targetSize.setHeight(h);
            }

            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(imageSize.getWidth(), imageSize.getHeight());
            rlp.leftMargin = margin;
            rlp.topMargin = margin;
            rlp.rightMargin = margin;
            rlp.bottomMargin = margin;

            // album cover
            ivCover.setLayoutParams(rlp);

            // album name
            final int padding = Utils.getDip(20);
            int height = Utils.getDip(35);
            LinearLayout.LayoutParams llpName = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, height);
            llpName.topMargin = padding / 2;
            llpName.bottomMargin = padding;
            tvName.setLayoutParams(llpName);

            // subParent
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(imageSize.getWidth(), imageSize.getHeight());
            rlSubParent.setLayoutParams(llp);

            if (Build.VERSION.SDK_INT >= 11) {
                rlInfo.setBackgroundResource(R.drawable.album_photo_member_corners_bg_4);
            } else {
                rlInfo.setBackgroundResource(R.drawable.album_photo_member_corners_bg);
            }
        }

        public void updateDeleteMode() {
            int visibility = vDelete.getVisibility();
            if (isDelete) {
                if (visibility != View.VISIBLE) {
                    vDelete.setVisibility(View.VISIBLE);
                }
            } else {
                if (visibility != View.GONE) {
                    vDelete.setVisibility(View.GONE);
                }
            }
        }

        public void setData(AlbumEntity album) {
            albumEntity = album;
            setCover(album);
            vDelete.setTag(album);
            // tvName.setText(album.getName());
            // tvName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            // double viewWidth = (double) tvName.getMeasuredWidth();
            // TextPaint paint = tvName.getPaint();
            // double textWidth = (double) paint.measureText(album.getName());
            // tvName.setEmojiText(album.getName(), viewWidth, textWidth);
            tvName.setEmojiText(album.getName());

            tvSize.setText(" " + album.getSize());

            int visibility = tvNotify.getVisibility();

            int updates = album.getUpdateCount();
            if (updates <= 0) {
                if (visibility != View.GONE) {
                    tvNotify.setVisibility(View.GONE);
                }
            } else {
                if (visibility != View.VISIBLE) {
                    tvNotify.setVisibility(View.VISIBLE);
                }
                if (updates <= 99) {
                    tvNotify.setText(Integer.toString(updates));
                    tvNotify.setBackgroundResource(R.drawable.new_photo_bg);
                } else {
                    tvNotify.setText("");
                    tvNotify.setBackgroundResource(R.drawable.image_update);
                }
            }

            visibility = tvMembers.getVisibility();
            if (album.getMembers() == 1) {
                if (visibility != View.GONE) {
                    tvMembers.setVisibility(View.GONE);
                }
            } else {
                if (visibility != View.VISIBLE) {
                    tvMembers.setVisibility(View.VISIBLE);
                }
                tvMembers.setText(" " + album.getMembers());
            }
        }

        public void setCover(AlbumEntity album) {

            if (album == null || Utils.isEmpty(album.getTrueCover())) {
                ivCover.setImageBitmap(defCover);
            } else {
                ImageManager.instance().load(ivCover, album.getTrueCover(), targetSize, false, options);
            }
        }
    }

}
