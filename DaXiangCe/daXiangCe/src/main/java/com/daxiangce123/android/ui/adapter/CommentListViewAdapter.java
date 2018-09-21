package com.daxiangce123.android.ui.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.listener.OnLoadListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.DividerView;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewFixTouchConsume;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;

public class CommentListViewAdapter extends BaseAdapter {

	public final static String TAG = "CommentListViewAdapter";
	private Context mContext = null;
	private LinkedList<Object> dataList = null;
	private ImageSize pictureSize;
	private LikeAdapter likeAdapter;
	private FileEntity curFile;
	private OnClickListener clickListener;
	private OnItemClickListener itemClickListener;
	private AbsListView absListView;
	private boolean liked;
	private int avaterSize;
	private int likeAvaterSize;

	public CommentListViewAdapter(Context context) {
		mContext = context;
		avaterSize = Utils.dp2px(context, 60);
		likeAvaterSize = Utils.dp2px(context, 40);
		pictureSize = new ImageSize(App.SCREEN_WIDTH / 2, App.SCREEN_HEIGHT / 3);
		pictureSize.setRound(true);
	}

	public boolean containSize(ImageSize imageSize) {
		if (imageSize == null) {
			return false;
		}
		if (pictureSize == imageSize) {
			return true;
		}
		return false;
	}

	public void setClickListener(OnItemClickListener itemClickListener, OnClickListener clickListener) {
		this.clickListener = clickListener;
		this.itemClickListener = itemClickListener;
	}

	public void setData(LinkedList<Object> list) {
		this.dataList = list;
	}

	public void updateLikeImage(boolean liked) {
		this.liked = liked;
	}

	public void setFile(FileEntity entity) {
		curFile = entity;
	}

	public int getCount() {
		return dataList.size();
	}

	public Object getItem(int position) {
		return dataList.get(position);
	}

	public long getItemId(int position) {
		return 0;
	}

	public void setProgress(int progress) {
		if (absListView == null) {
			return;
		}
		if (absListView.getFirstVisiblePosition() != 0) {
			return;
		}
		View v = absListView.getChildAt(0);
		if (v == null) {
			return;
		}
		Object obj = v.getTag();
		if (!(obj instanceof ViewHolder)) {
			return;
		}
		// ViewHolder vh = (ViewHolder) obj;
		// vh.commentFile.setText("["
		// + absListView.getContext().getString(R.string.downloaded_x,
		// progress) + "]");
	}

	@Override
	public int getViewTypeCount() {
		return 3;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		if (absListView == null && parent instanceof AbsListView) {
			absListView = (AbsListView) parent;
		}
		final ViewHolder holder;
		if (position == 0) {
			holder = new ViewHolder();
			DividerView dView = new DividerView(mContext);
			LayoutInflater.from(mContext).inflate(R.layout.comment_item, dView);
			holder.init(dView);
			convertView = dView;
			holder.tvName.setVisibility(View.GONE);
			holder.tvDate.setVisibility(View.GONE);
			holder.llFile.setVisibility(View.VISIBLE);
			holder.ivFile.setOnClickListener(clickListener);
			holder.ivFile.setLoadListener(new OnLoadListener() {

				@Override
				public void onLoad(Bitmap bitmap, View v) {
					if (App.DEBUG) {
						LogUtil.d(TAG, "Comment Adapter onLoad Bitmap");
						// CToast.showToast("CommentAdapter onLoad:"
						// + (bitmap != null) + " show:"
						// + (v == holder.ivFile && bitmap != null));
					}
					if (v == holder.ivFile && bitmap != null) {
						holder.llFile.setBackgroundColor(Color.TRANSPARENT);
					}
				}
			});

			ViewGroup.LayoutParams llp = new LinearLayout.LayoutParams(pictureSize.getWidth(), pictureSize.getHeight());
			holder.llFile.setLayoutParams(llp);

			String name = "";
			UserInfo info = (UserInfo) dataList.get(0);
			if (info != null) {
				name = info.getName();
				holder.ivAvater.setTag(info);
			}
			holder.showAvatar(curFile.getOwner());

			if (App.DEBUG) {
				LogUtil.d(TAG, "getView()	curFile=" + (curFile != null ? curFile.getId() : "NULL"));
			}

			holder.ivFile.setImageBitmap(null);
			if (curFile != null) {
				if (MimeTypeUtil.getMime(curFile.getMimeType()) != Mime.IMG) {
					pictureSize.setThumb(true);
				} else {
					pictureSize.setThumb(false);
				}
				ImageManager.instance().load(holder.ivFile, curFile.getId(), pictureSize);
			}

			if (curFile != null) {
				long mills = TimeUtil.toLong(curFile.getCreateDate(), Consts.SERVER_UTC_FORMAT);
				String time = TimeUtil.formatTime(mills, "HH:mm");
				Spanned text = Html.fromHtml(mContext.getString(R.string.x_added_at_time_x, name, TimeUtil.humanizeDate(mills) + " " + time));
				holder.tvComment.setText(text);
			}
			convertView.setTag(holder);
		} else if (position == 1) {
			DividerView dView = new DividerView(mContext);
			convertView = LayoutInflater.from(mContext).inflate(R.layout.like_item, dView);
			float margin = mContext.getResources().getDimension(R.dimen.preference_margin_rl);
			dView.setBottomMarginLeft(margin);
			dView.setPadding(0, 0, 0, 0);
			GridView gvLike = (GridView) convertView.findViewById(R.id.gv_like_list);
			ImageView ivLike = (ImageView) convertView.findViewById(R.id.iv_like);
			TextView tvSomeoneLike = (TextView) convertView.findViewById(R.id.tv_someone_like);

			if (clickListener != null) {
				ivLike.setOnClickListener(clickListener);
			}
			LinkedList<LikeEntity> likeList = (LinkedList<LikeEntity>) dataList.get(1);

			// if (albumEntity.getLikeOff()) {
			// ivLike.setClickable(false);
			// } else {
			// ivLike.setClickable(true);
			// }

			// LogUtil.d(TAG, "curFile.getLikes : " + curFile.getLikes());
			if (curFile != null) {
				if (curFile.getLikes() <= 5) {
					tvSomeoneLike.setVisibility(View.GONE);
				} else {
					tvSomeoneLike.setVisibility(View.VISIBLE);
					int likePeople = 0;
					if (likeList != null) {
						// 从新计算点赞数
						if (likeList.size() < 5) {
							likePeople = likeList.size();
						} else {
							likePeople = Math.max(likeList.size(), curFile.getLikes());
						}
					} else {
						likePeople = curFile.getLikes();
					}
					String someoneLiked = Utils.getString(R.string.more_x_someone_liked, likePeople);
					tvSomeoneLike.setText(Html.fromHtml(someoneLiked));
					tvSomeoneLike.setOnClickListener(clickListener);
				}
			}
			if (liked) {
				ivLike.setImageResource(R.drawable.like_bg);
			} else {
				ivLike.setImageResource(R.drawable.unlike_bg);
			}

			likeAdapter = new LikeAdapter(mContext);
			likeAdapter.setData(likeList);
			likeAdapter.setImageSize(likeAvaterSize);
			likeAdapter.notifyDataSetChanged();
			gvLike.setAdapter(likeAdapter);
			gvLike.setOnItemClickListener(itemClickListener);
			likeAdapter.notifyDataSetChanged();
			convertView.setTag(null);
		} else {
			if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
				holder = new ViewHolder();
				DividerView dView = new DividerView(mContext);
				LayoutInflater.from(mContext).inflate(R.layout.comment_item, dView);

				float margin = mContext.getResources().getDimension(R.dimen.preference_margin_rl);
				dView.setBottomMarginLeft(margin);
				holder.init(dView);
				convertView = dView;
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
				holder.tvName.setVisibility(View.VISIBLE);
				holder.tvDate.setVisibility(View.VISIBLE);
				holder.llFile.setVisibility(View.GONE);
			}
			CommentEntity comment = (CommentEntity) getItem(position);
			holder.setComment(comment);
		}
		return convertView;
	}

	private class ViewHolder {
		ImageViewEx ivAvater;
		TextView tvDate;
		TextViewParserEmoji tvName;
		TextViewFixTouchConsume tvComment;
		ImageViewEx ivFile;
		View llFile;

		// boolean ret = false;

		public void init(View view) {
			ivAvater = (ImageViewEx) view.findViewById(R.id.comment_user_icon);
			tvDate = (TextView) view.findViewById(R.id.comment_time);
			tvName = (TextViewParserEmoji) view.findViewById(R.id.comment_user_name);
			tvComment = (TextViewFixTouchConsume) view.findViewById(R.id.tv_comment);
			ivFile = (ImageViewEx) view.findViewById(R.id.iv_file);
			llFile = view.findViewById(R.id.fl_file);

			ViewGroup.LayoutParams flp = new FrameLayout.LayoutParams(avaterSize, avaterSize);
			ivAvater.setLayoutParams(flp);
			ivAvater.setOnClickListener(clickListener);
		}

		public void setAvatar(CommentEntity comment) {
			if (comment == null) {
				return;
			}
			if (ivAvater == null) {
				return;
			}
			ivAvater.setTag(comment);
			showAvatar(comment.getUserId());
		}

		public void showAvatar(String userId) {
			ivAvater.setImageBitmap(null);
			ImageManager.instance().loadAvater(ivAvater, userId);
		}

		public void setComment(CommentEntity comment) {
			if (comment == null) {
				return;
			}
			String createDate = comment.getCreateDate();
			String time = TimeUtil.humanizeDateTime(createDate, Consts.SERVER_UTC_FORMAT);
			SpannableStringBuilder msg = EmojiParser.getInstance().convetToEmoji(comment.getMsg(), mContext);
			List<String> urls = Utils.extractUrls(msg.toString());

			SpannableStringBuilder content = new SpannableStringBuilder(msg);
			for (String url : urls) {
				int start = msg.toString().indexOf(url);
				int length = url.length();
				int end = start + length;
				content.setSpan(new UnderlineSpan(), start, end, 0);
				content.setSpan(new ForegroundColorSpan(0xFFFF0000), start, end, 0);
				content.setSpan(new TypefaceSpan("courier"), start, end, 0);
				content.setSpan(new ClickableString(url), start, end, 0);
			}
			tvComment.setMovementMethod(TextViewFixTouchConsume.LocalLinkMovementMethod.getInstance());

			if (!Utils.isEmpty(comment.getReplyToUser())) {
				tvComment.setText(Html.fromHtml(mContext.getString(R.string.reply_to_x, comment.getReplayToUserName())));
				tvComment.append(content);

			} else {
				tvComment.setText(content);
			}
			tvDate.setText(time);
			// trim name
			// tvName.setText(comment.getUserName());
			// tvName.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
			// double viewWidth = (double) tvName.getMeasuredWidth();
			// TextPaint paint = tvName.getPaint();
			// double textWidth = (double) paint
			// .measureText(comment.getUserName());
			// tvName.setEmojiText(comment.getUserName(), viewWidth, textWidth);
			tvName.setEmojiText(comment.getUserName());
			setAvatar(comment);
		}
	}

	private static class ClickableString extends ClickableSpan {
		private String text;

		public ClickableString(String text) {
			this.text = text;
		}

		@Override
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			if (!text.startsWith("http://") && !text.startsWith("https://")) {
				text = "http://" + text;
			}
			Uri content_url = Uri.parse(text);
			intent.setData(content_url);
			v.getContext().startActivity(intent);
		}
	}
}
