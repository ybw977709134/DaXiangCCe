package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.listener.GetNearyAlbumCoverListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.DividerView;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class NotificationAdapter extends BaseAdapter {
    private final static String TAG = "NotificationAdapter";
    private float marginLeft;
    private List<Event> events;
    private ImageSize avaterSize;
    private ImageSize fileSize;
    private Context mContext;
    private String userId;
    private GetNearyAlbumCoverListener getNearyAlbumCoverListener;
    private HashMap<String, String> albumFileIdMap = new HashMap<String, String>();
    private HashSet<String> phoneUserId = new HashSet<String>();

    private OnClickListener clickListener;
    private ImageLoadingListener loadingListener = new ImageLoadingListener() {
        @Override
        public void onLoadingStarted(String imageUri, View view) {

        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
            if (imageUri == null) {
                return;
            }
            if (userId == null || !imageUri.contains(userId)) {
                return;
            }
            phoneUserId.add(userId);
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {

        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {

        }
    };


    private float getMarginLeft(Context context) {
        if (marginLeft == 0) {
            marginLeft = context.getResources().getDimension(R.dimen.preference_margin_rl);
        }
        return marginLeft;
    }

    public void setImageSize(ImageSize avaterSize, ImageSize fileSize) {
        this.avaterSize = avaterSize;
        this.fileSize = fileSize;
    }

    public void setData(List<Event> events) {
        this.events = events;
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    // public void notifyDataSetChanged() {
    // albumFileIdMap.clear();
    // super.notifyDataSetChanged();
    // }

    public void setGetNearyAlbumCoverListener(GetNearyAlbumCoverListener getNearyAlbumCoverListener) {
        this.getNearyAlbumCoverListener = getNearyAlbumCoverListener;
    }

    // public void setFileEntity(FileEntity fileEntity) {
    // albumFileIdMap.put(fileEntity.getAlbum(), fileEntity.getId());
    // }

    public void setFileAlbum(String albumId, String fileId) {
        albumFileIdMap.put(albumId, fileId);
    }

    @Override
    public int getCount() {
        if (events == null) {
            return 0;
        }
        return events.size();
    }

    @Override
    public Event getItem(int position) {
        if (events == null) {
            return null;
        }
        if (position < 0 || position >= events.size()) {
            return null;
        }
        return events.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        mContext = parent.getContext();
        ViewHolder holder;
        if (convertView == null) {
            final int layoutId = R.layout.notification_list_view_item;
            DividerView dView = new DividerView(mContext);
            LayoutInflater.from(mContext).inflate(layoutId, dView);
            dView.setId(R.id.tag_divider);

            holder = new ViewHolder(dView);
            convertView = dView;
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        float mLeft = 0;
        if (position != getCount() - 1) {
            mLeft = getMarginLeft(mContext);
        }
        Event event = getItem(position);
        // if (event.getOpType().equals(Consts.ALBUM_DELETED) ||
        // event.getOpType().equals(Consts.MEMBER_LEFT)) {
        // if (fileEntity != null) {
        // holder.fileId = fileEntity.getId();
        // }
        // }
        holder.setMarginLeft(mLeft);
        holder.setData(event);
        return convertView;
    }

    private class ViewHolder {
        private DividerView convertView;

        private ImageViewEx ivAvater;
        private ImageViewEx ivPhoto;

        private TextViewParserEmoji tvName;
        private TextView tvDetail;
        private TextView tvDate;
        private String detail;
        private String fileId;
        private String opType;

        public ViewHolder(DividerView v) {
            ivAvater = (ImageViewEx) v.findViewById(R.id.iv_notification_avater);
            tvName = (TextViewParserEmoji) v.findViewById(R.id.tv_notification_user_name);
            tvDetail = (TextView) v.findViewById(R.id.tv_message_detail);
            ivPhoto = (ImageViewEx) v.findViewById(R.id.iv_notification_photo);
            tvDate = (TextView) v.findViewById(R.id.tv_notification_time);

            ivAvater.setOnClickListener(clickListener);

            convertView = v;
            v.setTag(this);
            if (avaterSize == null) {
                return;
            }
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(avaterSize.getWidth(), avaterSize.getHeight());
            ivAvater.setLayoutParams(llp);
            // ivAvater.setOnClickListener(clickListener);
            if (fileSize == null) {
                return;
            }
            ViewGroup.LayoutParams llpPhoto = new FrameLayout.LayoutParams(fileSize.getWidth(), fileSize.getHeight());
            ivPhoto.setLayoutParams(llpPhoto);
            ivPhoto.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }

        public void setMarginLeft(float left) {
            if (convertView == null) {
                return;
            }
            convertView.setBottomMarginLeft(left);
        }

        public void setData(Event event) {
            if (event == null) {
                return;
            }
            String date = TimeUtil.humanizeDateTime(event.getCreatedDate(), Consts.SERVER_UTC_FORMAT);
            detail = null;
            fileId = null;
            userId = event.getUserId();
            ivAvater.setTag(event);

            getDetail(event);

            tvDate.setText(date);
            // if (Consts.SYSTEM_FILE_DELETED.equals(opType)) {
            // tvName.setText(R.string.system_file_deleted_admin_name);
            // } else {

            // tvName.setText(event.getUserName());
            // tvName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            // double viewWidth = (double) tvName.getMeasuredWidth();
            // TextPaint paint = tvName.getPaint();
            // double textWidth = (double)
            // paint.measureText(event.getUserName());
            // tvName.setEmojiText(event.getUserName(), viewWidth, textWidth);
            if (userId.equals(Consts.VISITOR_UID)) {
                tvName.setEmojiText(mContext.getString(R.string.visitor_name));
            } else {
                tvName.setEmojiText(event.getUserName());
            }
            // }
            convertView.setTag(convertView.getId(), event);
            if (Consts.LIKE_CREATED.equals(event.getOpType())) {
                tvDetail.setBackgroundResource(R.drawable.heart);
                tvDetail.setText(null);
            } else {
                tvDetail.setBackgroundColor(0x00000000);
                tvDetail.setText(EmojiParser.getInstance().convetToEmoji(detail, mContext));
            }
            showAvater();
            showImage();
        }

        // 照片被赞通知（Photo been Liked）
        // 视频被赞通知（Video been Liked）
        // 新评论通知（New Comment）
        // 新回复通知（New Reply）
        // 照片已被删除通知（Photo been Deleted）
        // 视频已被删除通知（Video been Deleted）
        // 被管理员从相册中删除通知（Been Deleted from an Album）
        // 相册已被删除通知（Album has been Deleted）

        // 文件被分享 (file been shared)
        // 文件被下载 （file been downloaded）
        // 相册被分享 (album been shared)

        // 点赞/评论 删除图片相册
        private void getDetail(Event event) {
            if (event == null) {
                return;
            }
            opType = event.getOpType();
            Object object = event.getObject();
            try {
                if (Consts.ALBUM_DELETED.equals(opType) || Consts.SYSTEM_ALBUM_DELETED.equals(opType)) {
                    AlbumEntity entity = (AlbumEntity) object;
                    if (Consts.SYSTEM_ALBUM_DELETED.equals(opType)) {
                        detail = mContext.getString(R.string.system_album_deleted, entity.getName());
                    } else {
                        detail = mContext.getString(R.string.album_x_been_deleted_by_x, entity.getName());
                    }
                    fileId = null;
                    // if (getNearyAlbumCoverListener != null) {
                    // getNearyAlbumCoverListener.getAlbumCover(entity.getId());
                    // }
                } else if (Consts.ALBUM_SHARED.equals(opType)) {
                    AlbumEntity entity = (AlbumEntity) object;
                    detail = mContext.getString(R.string.album_x_been_shared, entity.getName());
                    if (entity.getId() != null) {
                        if (App.getDBHelper() != null) {
                            AlbumEntity albumEntity = App.getDBHelper().getData(AlbumEntity.EMPTY, entity.getId());
                            if (albumEntity != null) {
                                fileId = albumEntity.getTrueCover();
                            } else {
                                fileId = null;
                            }
                        }
                    }
                } else if (Consts.COMMENT_CREATED.equals(opType)) {
                    CommentEntity entity = (CommentEntity) object;
                    if (!Utils.isEmpty(entity.getReplyToUser())) {
                        detail = mContext.getString(R.string.replay_x, entity.getMsg());
                    } else {
                        detail = mContext.getString(R.string.comment_x, entity.getMsg());
                    }
                    fileId = entity.getObjId();

                } else if (Consts.LIKE_CREATED.equals(opType)) {
                    LikeEntity likeEntity = (LikeEntity) object;
                    // detail = mContext.getString(R.string.x_liked,
                    // likeEntity.getUserName());
                    fileId = likeEntity.getObjId();

                } else if (Consts.FILE_DELETED.equals(opType) || Consts.SYSTEM_FILE_DELETED.equals(opType)) {
                    FileEntity fileEntity = (FileEntity) object;
                    Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());

                    if (Consts.SYSTEM_FILE_DELETED.equals(opType)) {
                        detail = mContext.getString(R.string.system_file_deleted);
                    } else {
                        if (mime == Mime.VID) {
                            detail = mContext.getString(R.string.video_been_deleted);
                        } else {
                            detail = mContext.getString(R.string.image_been_deleted);
                        }
                    }
                    fileId = fileEntity.getId();

                } else if (Consts.FILE_SHARED.equals(opType)) {
                    FileEntity fileEntity = (FileEntity) object;
                    Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());

                    if (mime == Mime.VID) {
                        detail = mContext.getString(R.string.video_been_shared);
                    } else {
                        detail = mContext.getString(R.string.photo_been_shared);
                    }
                    fileId = fileEntity.getId();
                } else if (Consts.FILE_DOWNLOADED.equals(opType)) {
                    FileEntity fileEntity = (FileEntity) object;

                    Mime mime = MimeTypeUtil.getMime(fileEntity.getMimeType());

                    if (mime == Mime.VID) {
                        detail = mContext.getString(R.string.video_been_downloaded);
                    } else {
                        detail = mContext.getString(R.string.photo_been_downloaded);
                    }
                    fileId = fileEntity.getId();
                } else if (Consts.MEMBER_LEFT.equals(opType)) {
                    MemberEntity memberEntity = (MemberEntity) object;
                    detail = mContext.getString(R.string.kicked_from_album_x_by_x, event.getUserName(), memberEntity.getAlbumName());
                    // fileId = null;
                    fileId = albumFileIdMap.get(memberEntity.getAlbumId());
                    if (fileId == null) {
                        if (getNearyAlbumCoverListener != null) {
                            getNearyAlbumCoverListener.getAlbumCover(memberEntity.getAlbumId());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void showAvater() {
            // if (Consts.SYSTEM_FILE_DELETED.equals(opType)) {
            // ivAvater.setImageResource(R.drawable.ic_launcher);
            // } else {
            if (Utils.isEmpty(userId)) {
                ivAvater.setImageResource(R.drawable.default_image_small);
                return;
            } else if (userId.equals(Consts.VISITOR_UID)) {
                ivAvater.setImageResource(R.drawable.default_avater_icon);
                return;
            }

//            ImageManager.instance().loadAvater(ivAvater, userId);
            if (phoneUserId.contains(userId)) {
                ivAvater.setImageResource(R.drawable.default_image_small);
                return;
            }
            ivAvater.setImageBitmap(null);
            ImageManager.instance().loadAvater(ivAvater, userId, null, loadingListener);

            // LogUtil.d(TAG, " -- Width -- " + avaterSize.getWidth() +
            // " -- Height-- " + avaterSize.getHeight());
            // }
        }

        public void showImage() {
            if (Utils.isEmpty(fileId)) {
                ivPhoto.setImageResource(R.drawable.default_image_small);
                return;
            }

            if (fileSize == null) {
                return;
            }

            if (ivPhoto == null) {
                return;
            }

            ivPhoto.setImageBitmap(null);
            if (Consts.FILE_DELETED.equals(opType) || Consts.SYSTEM_FILE_DELETED.equals(opType) || Consts.ALBUM_DELETED.equals(opType) || Consts.SYSTEM_ALBUM_DELETED.equals(opType) || Consts.MEMBER_LEFT.equals(opType)) {
                ImageManager.instance().load(ivPhoto, fileId, fileSize, true);
            } else {
                ImageManager.instance().load(ivPhoto, fileId, fileSize);
            }
        }
    }
}
