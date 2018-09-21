package com.daxiangce123.android.ui.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.VersionUtil;
import com.nineoldandroids.view.ViewHelper;
import com.nineoldandroids.view.ViewPropertyAnimator;

public class AlbumBottomBar extends LinearLayout implements View.OnClickListener {
    private static final String TAG = "AlbumBottomBar";

    private static final int TRANSLATE_DURATION_MILLIS = 75;
    private static boolean hasHoneycomb = true;
    private boolean mVisible;
    private boolean isJoined = true;
    private final Interpolator mInterpolator = new AccelerateDecelerateInterpolator();

    private ImageView sort, upload_join, share, delete;

    OnTimeLineHeaderActionListener onTimeLineHeaderActionListener;

    private AlphaAnimation mHideAnimation = null;
    private AlphaAnimation mShowAnimation = null;

    public AlbumBottomBar(Context context) {
        super(App.getAppContext());
    }

    public AlbumBottomBar(Context context, AttributeSet attrs) {
        super(App.getAppContext(), attrs);
        init(attrs);
    }

    public boolean isJoined() {
        return isJoined;
    }

    public void setJoined(boolean isJoined) {
        this.isJoined = isJoined;
        if (App.DEBUG) {
            LogUtil.d(TAG, "upload_join " + upload_join);
        }
        if (upload_join != null) {
            if (isJoined) {
                upload_join.setImageResource(R.drawable.abb_upload);
            } else {
                upload_join.setImageResource(R.drawable.abb_join);
            }
        }

        if (delete != null) {
            if (isJoined) {
                delete.setClickable(true);
                delete.setAlpha(255);
            } else {
                delete.setClickable(false);
                delete.setAlpha(80);
            }
        }

    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public AlbumBottomBar(Context context, AttributeSet attrs, int defStyle) {
        super(App.getAppContext(), attrs, defStyle);
        init(attrs);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.abb_sort:
                onTimeLineHeaderActionListener.onSortClicked();
                break;
            case R.id.abb_upload_join:
                if (isJoined) {
                    onTimeLineHeaderActionListener.onUploadPhotoClicked();
                } else {
                    onTimeLineHeaderActionListener.onJoinClicked();
                }
                break;
            case R.id.abb_share:
                onTimeLineHeaderActionListener.onShareClicked(false);
                break;
            case R.id.abb_delete:
                onTimeLineHeaderActionListener.onDeleteClicked();
                break;
        }
    }

    @Override
    protected void onAttachedToWindow() {
        // TODO Auto-generated method stub
        super.onAttachedToWindow();
        findViews();
        setJoined(isJoined);
    }

    private void findViews() {
        sort = (ImageView) findViewById(R.id.abb_sort);
        upload_join = (ImageView) findViewById(R.id.abb_upload_join);
        share = (ImageView) findViewById(R.id.abb_share);
        delete = (ImageView) findViewById(R.id.abb_delete);
        sort.setOnClickListener(this);
        upload_join.setOnClickListener(this);
        share.setOnClickListener(this);
        delete.setOnClickListener(this);
    }

    private void init(AttributeSet attrs) {
        hasHoneycomb = VersionUtil.hasHoneycomb();
        mVisible = true;
    }

    public void setOnTimeLineHeaderActionListener(OnTimeLineHeaderActionListener onTimeLineHeaderActionListener) {
        this.onTimeLineHeaderActionListener = onTimeLineHeaderActionListener;
    }

    // ///////copy form FloatingActionButton/////

    private int getMarginBottom() {
        int marginBottom = 0;
        final ViewGroup.LayoutParams layoutParams = getLayoutParams();
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginBottom = ((ViewGroup.MarginLayoutParams) layoutParams).bottomMargin;
        }
        return marginBottom;
    }

    public void show() {
        show(true);
    }

    public void hide() {
        hide(true);
    }

    public void show(boolean animate) {
        toggle(true, animate, false);
    }

    public void hide(boolean animate) {
        toggle(false, animate, false);
    }

    private void toggle(final boolean visible, final boolean animate, boolean force) {
        if ((mVisible != visible || force)) {
            Log.v(TAG, System.currentTimeMillis() + "toggle " + visible);
            mVisible = visible;
            int height = getHeight();
            if (height == 0 && !force) {
                ViewTreeObserver vto = getViewTreeObserver();
                if (vto.isAlive()) {
                    Log.v(TAG, "vto.isAlive");
                    vto.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            Log.v(TAG, "onPreDraw");
                            ViewTreeObserver currentVto = getViewTreeObserver();
                            if (currentVto.isAlive()) {
                                currentVto.removeOnPreDrawListener(this);
                            }
                            toggle(visible, animate, true);
                            return true;
                        }
                    });
                    return;
                }
            }
            int translationY = visible ? 0 : height;
            if (animate) {
                ViewPropertyAnimator.animate(this).setDuration(TRANSLATE_DURATION_MILLIS).translationY(translationY);
            } else {
                ViewHelper.setTranslationY(this, translationY);
            }
        }
    }

    /**
     * A {@link android.os.Parcelable} representing the
     */
    public static class SavedState extends BaseSavedState {

        private int mScrollY;

        public SavedState(Parcelable parcel) {
            super(parcel);
        }

        private SavedState(Parcel in) {
            super(in);
            mScrollY = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel out, int flags) {
            super.writeToParcel(out, flags);
            out.writeInt(mScrollY);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {

            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }

}
