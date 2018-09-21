package com.daxiangce123.android.ui.view;

import java.util.List;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TypefaceSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.Utils;

public class ExpandableTextView extends LinearLayout implements View.OnClickListener {

	private static final String TAG = ExpandableTextView.class.getSimpleName();

	// The default number of lines;
	private static final int MAX_COLLAPSED_LINES = 8;

	// The default animation duration
	private static final int DEFAULT_ANIM_DURATION = 300;

	// The default alpha value when the animation starts
	private static final float DEFAULT_ANIM_ALPHA_START = 0.7f;

	protected TextView mTv;

	protected ImageButton mButton; // Button to expand/collapse

	private boolean mRelayout;

	private boolean mCollapsed = true; // Show short version as default.

	private int mCollapsedHeight;

	private int mMeasuredTextHeight;

	private int mMaxCollapsedLines;

	private int mMarginBetweenTxtAndBottom;

	private Drawable mExpandDrawable;

	// private Drawable mCollapseDrawable;

	private int mAnimationDuration;

	private float mAnimAlphaStart;

	public ExpandableTextView(Context context) {
		super(context);
	}

	public ExpandableTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public ExpandableTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(attrs);
	}

	@Override
	public void onClick(View view) {
		if (mButton.getVisibility() != View.VISIBLE) {
			return;
		}
		if (!mCollapsed) return;
		mCollapsed = !mCollapsed;
		// mButton.setImageDrawable(mCollapsed ? mExpandDrawable :
		// mCollapseDrawable);

		if (mCollapsed) {
			mButton.setImageDrawable(mExpandDrawable);
		} else {
			mButton.setImageDrawable(null);
		}

		Animation animation;
		if (mCollapsed) {
			animation = new ExpandCollapseAnimation(this, getHeight(), mCollapsedHeight);
		} else {
			animation = new ExpandCollapseAnimation(this, getHeight(), getHeight() + mMeasuredTextHeight - mTv.getHeight());
		}

		animation.setFillAfter(true);
		animation.setAnimationListener(new Animation.AnimationListener() {
			@Override
			public void onAnimationStart(Animation animation) {
				applyAlphaAnimation(mTv, mAnimAlphaStart);
			}

			@Override
			public void onAnimationEnd(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}
		});

		clearAnimation();
		startAnimation(animation);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		// If no change, measure and return
		if (!mRelayout || getVisibility() == View.GONE) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
			return;
		}
		mRelayout = false;

		// Setup with optimistic case
		// i.e. Everything fits. No button needed
		mButton.setVisibility(View.GONE);
		mTv.setMaxLines(Integer.MAX_VALUE);

		// Measure
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		// If the text fits in collapsed mode, we are done.
		if (mTv.getLineCount() <= mMaxCollapsedLines) {
			return;
		}

		// Saves the text height w/ max lines
		mMeasuredTextHeight = mTv.getMeasuredHeight();

		// Doesn't fit in collapsed mode. Collapse text view as needed. Show
		// button.
		if (mCollapsed) {
			mTv.setMaxLines(mMaxCollapsedLines);
		}
		mButton.setVisibility(View.VISIBLE);

		// Re-measure with new setup
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		if (mCollapsed) {
			// Gets the margin between the TextView's bottom and the ViewGroup's
			// bottom
			mTv.post(new Runnable() {
				@Override
				public void run() {
					mMarginBetweenTxtAndBottom = getHeight() - mTv.getHeight();
				}
			});
			// Saves the collapsed height of this ViewGroup
			mCollapsedHeight = getMeasuredHeight();
		}
	}

	private void init(AttributeSet attrs) {
		TypedArray typedArray = getContext().obtainStyledAttributes(attrs, R.styleable.ExpandableTextView);
		mMaxCollapsedLines = typedArray.getInt(R.styleable.ExpandableTextView_maxCollapsedLines, MAX_COLLAPSED_LINES);
		mAnimationDuration = typedArray.getInt(R.styleable.ExpandableTextView_animDuration, DEFAULT_ANIM_DURATION);
		mAnimAlphaStart = typedArray.getFloat(R.styleable.ExpandableTextView_animAlphaStart, DEFAULT_ANIM_ALPHA_START);
		mExpandDrawable = typedArray.getDrawable(R.styleable.ExpandableTextView_expandDrawable);
		// mCollapseDrawable =
		// typedArray.getDrawable(R.styleable.ExpandableTextView_collapseDrawable);

		if (mExpandDrawable == null) {
			mExpandDrawable = getResources().getDrawable(R.drawable.ic_expand_small_holo_light);
		}
		// if (mCollapseDrawable == null) {
		// mCollapseDrawable =
		// getResources().getDrawable(R.drawable.ic_collapse_small_holo_light);
		// }

		typedArray.recycle();
	}

	private static boolean isPostHoneycomb() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
	}

	private void findViews() {
		mTv = (TextView) findViewById(R.id.expandable_text);
		mTv.setOnClickListener(this);
		mButton = (ImageButton) findViewById(R.id.expand_collapse);
		// mButton.setImageDrawable(mCollapsed ? mExpandDrawable :
		// mCollapseDrawable);
		if (mCollapsed) {
			mButton.setImageDrawable(mExpandDrawable);
		} else {
			mButton.setImageDrawable(null);
		}

		mButton.setOnClickListener(this);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private static void applyAlphaAnimation(View view, float alpha) {
		if (isPostHoneycomb()) {
			view.setAlpha(alpha);
		} else {
			AlphaAnimation alphaAnimation = new AlphaAnimation(alpha, alpha);
			// make it instant
			alphaAnimation.setDuration(0);
			alphaAnimation.setFillAfter(true);
			view.startAnimation(alphaAnimation);
		}
	}

	public void setText(String text) {
		mRelayout = true;
		if (mTv == null) {
			findViews();
		}
		setDescription(text);

		// String trimmedText = text.trim();
		// mTv.setText(trimmedText);
		// setVisibility(trimmedText.length() == 0 ? View.GONE : View.VISIBLE);
	}

	private void setDescription(String text) {
		if (Utils.isEmpty(text) || text.equals("") || text.trim().equals("")) {
			mTv.setVisibility(View.GONE);
		} else {
			mTv.setVisibility(View.VISIBLE);

			SpannableStringBuilder msg = EmojiParser.getInstance().convetToEmoji(text, this.getContext());

			List<String> urls = Utils.extractUrls(msg.toString());
			SpannableString content = new SpannableString(msg);
			for (String url : urls) {
				int start = text.indexOf(url);
				int length = url.length();
				int end = start + length;
				content.setSpan(new UnderlineSpan(), start, end, 0);
				content.setSpan(new ForegroundColorSpan(0xFFFF0000), start, end, 0);
				content.setSpan(new TypefaceSpan("courier"), start, end, 0);
				content.setSpan(new ClickableString(url), start, end, 0);
			}
			mTv.setMovementMethod(LinkMovementMethod.getInstance());
			mTv.setText(content);
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

	public CharSequence getText() {
		if (mTv == null) {
			return "";
		}
		return mTv.getText();
	}

	protected class ExpandCollapseAnimation extends Animation {
		private final View mTargetView;
		private final int mStartHeight;
		private final int mEndHeight;

		public ExpandCollapseAnimation(View view, int startHeight, int endHeight) {
			mTargetView = view;
			mStartHeight = startHeight;
			mEndHeight = endHeight;
			setDuration(mAnimationDuration);
		}

		@Override
		protected void applyTransformation(float interpolatedTime, Transformation t) {
			final int newHeight = (int) ((mEndHeight - mStartHeight) * interpolatedTime + mStartHeight);
			mTv.setMaxHeight(newHeight - mMarginBetweenTxtAndBottom);
			applyAlphaAnimation(mTv, mAnimAlphaStart + interpolatedTime * (1.0f - mAnimAlphaStart));
			mTargetView.getLayoutParams().height = newHeight;
			mTargetView.requestLayout();
		}

		@Override
		public void initialize(int width, int height, int parentWidth, int parentHeight) {
			super.initialize(width, height, parentWidth, parentHeight);
		}

		@Override
		public boolean willChangeBounds() {
			return true;
		}
	};
}
