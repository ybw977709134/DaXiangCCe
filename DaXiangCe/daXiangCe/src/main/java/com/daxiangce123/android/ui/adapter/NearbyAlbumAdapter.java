package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.listener.GetNearyAlbumCoverListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.DividerView;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.Utils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.HashMap;
import java.util.List;

/**
 * @author
 * @project DaXiangCe
 * @time 2014-6-1
 */
public class NearbyAlbumAdapter extends BaseAdapter {

    final static String TAG = "NearbyAlbumAdapter";
    private List<?> albums;

    private ImageSize size;
    private HashMap<String, Holder> viewMaps = new HashMap<String, Holder>();
    private GetNearyAlbumCoverListener getNearyAlbumCoverListener;
    private DisplayImageOptions options;
    private Bitmap defCover;
    private Context mContext = null;

    public NearbyAlbumAdapter(Context context) {
        mContext = context;
        int width = Utils.getDip(80);
        size = new ImageSize(width, width);
        size.setHasThumbFile(true);
        size.setThumb(true);
        if (defCover == null) {
            defCover = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.default_image_normal);
        }
        Drawable drawable = new BitmapDrawable(mContext.getResources(), defCover);
        options = UILUtils.getDiaplayOption().showImageOnFail(drawable).showImageForEmptyUri(drawable).showImageOnLoading(new BitmapDrawable(defCover)).build();
    }

    @Override
    public void notifyDataSetChanged() {
        viewMaps.clear();
        super.notifyDataSetChanged();
    }

    public void setData(List<?> albums) {
        this.albums = albums;
    }

    public void setGetNearyAlbumCoverListener(GetNearyAlbumCoverListener getNearyAlbumCoverListener) {
        this.getNearyAlbumCoverListener = getNearyAlbumCoverListener;
    }

    public List<?> getData() {
        return albums;
    }

    @Override
    public int getCount() {
        if (albums == null) {
            return 0;
        }
        return albums.size();
    }

    @Override
    public Object getItem(int position) {
        if (albums == null) {
            return null;
        }
        return albums.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    public boolean isShown(String albumId) {
        Holder holder = viewMaps.get(albumId);
        if (holder == null) {
            return false;
        }
        if (albumId.equals(getAlbumId(holder.object))) {
            return true;
        }
        return false;
    }

    public void updateSingle(String albumId) {
        Holder holder = viewMaps.get(albumId);
        if (holder == null) {
            return;
        }
        if (!albumId.equals(getAlbumId(holder.object))) {
            return;
        }
        holder.setAlbum(holder.object);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder = null;
        DividerView view = null;

        if (convertView == null) {
            view = new DividerView(parent.getContext());
            LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_nearby_album, view);

            holder = new Holder(view);
            view.setTag(holder);
            convertView = view;
        } else {
            holder = (Holder) convertView.getTag();
            view = (DividerView) convertView;
        }

        if (position == getCount() - 1) {
            view.setBottomMarginLeft(0);
        } else {
            int margin = Utils.getDip(10);
            view.setBottomMarginLeft(margin);
        }

        Object nearyAlbum = getItem(position);
        holder.setAlbum(nearyAlbum);
        viewMaps.put(getAlbumId(nearyAlbum), holder);
        return view;
    }

    private boolean hasPasswd(AlbumEntity album) {
        return false;
        // if (album == null) {
        // return false;
        // }
        // if (albumListener == null) {
        // return false;
        // }
        // return albumListener.needPasswd(album);
    }

    public class Holder {
        ImageViewEx ivCover;
        TextViewParserEmoji tvName;
        TextView tvSize;
        TextView tvMember;
        TextView tvViews;
        TextView tvShares;
        TextView tvDistance;
        TextViewParserEmoji tvDescription;
        View ivPrivate;
        Object object;

        public Holder(View parent) {
            if (parent == null) {
                return;
            }
            ivPrivate = parent.findViewById(R.id.iv_private);
            ivCover = (ImageViewEx) parent.findViewById(R.id.iv_album_cover_nearby);
            tvName = (TextViewParserEmoji) parent.findViewById(R.id.tv_album_name_nearby);
            tvSize = (TextView) parent.findViewById(R.id.tv_album_size_nearby);
            tvMember = (TextView) parent.findViewById(R.id.tv_album_member_nearby);
            tvViews = (TextView) parent.findViewById(R.id.tv_album_views_nearby);
            tvShares = (TextView) parent.findViewById(R.id.tv_album_shares_nearby);

            tvDistance = (TextView) parent.findViewById(R.id.tv_album_distance_nearby);
            tvDistance.setVisibility(View.GONE);

            tvDescription = (TextViewParserEmoji) parent.findViewById(R.id.tv_album_discription);

            ViewGroup.LayoutParams flp = new FrameLayout.LayoutParams(size.getWidth(), size.getHeight());
            ivCover.setLayoutParams(flp);
        }

        public void setAlbum(Object object) {
            if (object == null) {
                return;
            }
            this.object = object;
            AlbumEntity album = null;
            if (object instanceof NearbyAlbum) {
                NearbyAlbum nearbyalbum = (NearbyAlbum) object;
                album = nearbyalbum.getAlbum();
                // tvDistance.setVisibility(View.VISIBLE);
                String dis = "";
                if (nearbyalbum.getDistance() < 0.1f) {
                    dis = "100m";
                } else {
                    if (nearbyalbum.getDistance() < 1) {
                        dis = (int) (nearbyalbum.getDistance() * 1000) + "m";
                    } else {
                        dis = Utils.formatNum(nearbyalbum.getDistance(), "#.#") + "km";
                    }
                }
                // tvDistance.setText("  " + dis);
            } else if (object instanceof AlbumEntity) {
                tvDistance.setVisibility(View.GONE);
                album = (AlbumEntity) object;
            } else {
                return;
            }
            tvName.setEmojiText(album.getName());
            // tvName.setText(album.getName());
            // tvName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            // double viewWidth = (double) tvName.getMeasuredWidth();
            // TextPaint paint = tvName.getPaint();
            // double textWidth = (double) paint.measureText(album.getName());
            // tvName.setEmojiText(album.getName(), viewWidth, textWidth);
            // if (App.DEBUG) {
            // LogUtil.d(TAG, "paint.measureText :" + textWidth
            // + " getMeasuredWidth : " + viewWidth + " name.size : "
            // + album.getName().length());
            // }

            com.daxiangce123.android.util.TextUtils.adjustSizeText(tvSize, " ", album.getSize());
            com.daxiangce123.android.util.TextUtils.adjustSizeText(tvMember, " ", album.getMembers());
            com.daxiangce123.android.util.TextUtils.adjustSizeText(tvViews, " ", album.getViews());
            com.daxiangce123.android.util.TextUtils.adjustSizeText(tvShares, " ", album.getShares());
//            tvSize.setText("  " + album.getSize());
//            tvMember.setText("  " + album.getMembers());
//            tvViews.setText(" " + album.getViews());
//            tvShares.setText(" " + album.getShares());

            // hasPasswd
            boolean hasPasswd = hasPasswd(album);
            if (hasPasswd) {
                ivPrivate.setVisibility(View.VISIBLE);
            } else {
                ivPrivate.setVisibility(View.GONE);
            }

            String note = album.getNote();
            if (note == null || note.equals("") || note.trim().equals("")) {
                tvDescription.setVisibility(View.GONE);
            } else {
                tvDescription.setVisibility(View.VISIBLE);
                tvDescription.setEmojiText(note);

                // tvDescription.setText(note);
                // tvDescription.measure(MeasureSpec.UNSPECIFIED,
                // MeasureSpec.UNSPECIFIED);
                // double vw = (double) tvDescription.getMeasuredWidth();
                // TextPaint p = tvDescription.getPaint();
                // double tw = (double) paint.measureText(album.getName());
                // tvName.setEmojiText(album.getName(), vw, tw);

            }

            // show albumCover

            // ivCover.setImageBitmap(null);
            // FileEntity cover = getAlbumCover(album.getId());
            // if (cover == null) {
            // return;
            // }
            // ImageManager.instance().loadSampleThumb(ivCover,
            // cover != null ? cover.getId() : null, size);
            setCover(album);
        }

        public void setCover(AlbumEntity album) {
            // NEED Listener.getAlbumCover
            if (getNearyAlbumCoverListener != null) {
                if (Utils.isEmpty(album.getTrueCover())) {
                    getNearyAlbumCoverListener.getAlbumCover(album);
                }
            }
            // ivCover.setImageBitmap(null);
            ivCover.setImageBitmap(defCover);
            // ImageManager.instance().loadThumb(ivCover, album.getTrueCover(),
            // size);
            ImageManager.instance().load(ivCover, album.getTrueCover(), size, false, options);
        }
    }

    private String getAlbumId(Object obj) {
        if (obj instanceof NearbyAlbum) {
            return ((NearbyAlbum) obj).getAlbum().getId();
        } else if (obj instanceof AlbumEntity) {
            return ((AlbumEntity) obj).getId();
        }
        return null;
    }

}
