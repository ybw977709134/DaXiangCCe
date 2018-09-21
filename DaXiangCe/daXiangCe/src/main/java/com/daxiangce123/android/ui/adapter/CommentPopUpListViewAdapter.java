package com.daxiangce123.android.ui.adapter;

import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.view.DividerView;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewFixTouchConsume;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;

public class CommentPopUpListViewAdapter extends BaseAdapter {

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

	public CommentPopUpListViewAdapter(Context context) {
		mContext = context;
		avaterSize = Utils.dp2px(context, 60);
		likeAvaterSize = Utils.dp2px(context, 35);
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
		return dataList.size() - 1;
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

	public View getView(int position, View convertView, ViewGroup parent) {
		if (absListView == null && parent instanceof AbsListView) {
			absListView = (AbsListView) parent;
		}
		final ViewHolder holder;
		if (position == 0) {
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
			if (curFile != null) {
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
				if (likeList.size() == 0) {
					tvSomeoneLike.setVisibility(View.VISIBLE);
					String someoneLiked = Utils.getString(R.string.be_the_first);
					tvSomeoneLike.setText(Html.fromHtml(someoneLiked));
				} else if (curFile.getLikes() <= 5) {
					tvSomeoneLike.setVisibility(View.GONE);
				} else {
					tvSomeoneLike.setVisibility(View.VISIBLE);
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
			CommentEntity comment = (CommentEntity) getItem(position + 1);
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
			CommentEntity oldEntity = (CommentEntity) ivAvater.getTag();
			if ((oldEntity == null) || !(oldEntity.getUserId().equals(comment.getUserId()))) {
				ivAvater.setTag(comment);
				showAvatar(comment.getUserId());
			}
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
			// // tvName.setEmojiText(comment.getUserName(), viewWidth,
			// textWidth);
			// tvName.setEmojiText((comment.getUserName() + "").trim(),
			// viewWidth,
			// textWidth);
			tvName.setEmojiText((comment.getUserName() + "").trim());
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
