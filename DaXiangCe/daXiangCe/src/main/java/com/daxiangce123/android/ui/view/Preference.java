package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class Preference extends DividerView implements OnCheckedChangeListener {

    public static interface CheckedChangedListener {
        public void onCheckedChanged(View v, boolean isChecked);
    }

    public final static String TAG = "Perference";
    private boolean hasCheck = false;
    private boolean hasMore = false;
    private int drawableId;
    private int iconId = -1;
    private int leftIconId;
    private String title;
    private String content;
    private View ivMore;
    private View rlRight;
    private TextView tvTitle;
    private TextView littleTitle;
    private TextViewParserEmoji tvContent;
    private ImageView ivIcon;
    private ImageView leftIcon;
    private CheckBox checkBox;
    private ColorStateList titleColor;
    private ColorStateList contentColor;
    private CheckedChangedListener changedListener;

    public Preference(Context context) {
        this(context, null);
    }

    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
        Resources res = getResources();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.preference);
        titleColor = res.getColorStateList(R.color.black);
        contentColor = res.getColorStateList(R.color.grey);
        try {
            if (a.hasValue(R.styleable.preference_title)) {
                title = a.getString(R.styleable.preference_title);
            }
            if (a.hasValue(R.styleable.preference_content)) {
                content = a.getString(R.styleable.preference_content);
            }
            if (a.hasValue(R.styleable.preference_drawable)) {
                drawableId = a.getResourceId(R.styleable.preference_drawable, 0);
            }
            if (a.hasValue(R.styleable.preference_hasMore)) {
                hasMore = a.getBoolean(R.styleable.preference_hasMore, false);
            }
            if (a.hasValue(R.styleable.preference_hasCheck)) {
                hasCheck = a.getBoolean(R.styleable.preference_hasCheck, false);
            }
            if (a.hasValue(R.styleable.preference_icon)) {
                iconId = a.getResourceId(R.styleable.preference_icon, 0);
            }
            if (a.hasValue(R.styleable.preference_leftIcon)) {
                leftIconId = a.getResourceId(R.styleable.preference_leftIcon, 0);
            }
            if (a.hasValue(R.styleable.preference_titleColor)) {
                titleColor = a.getColorStateList(R.styleable.preference_titleColor);
            }
            if (a.hasValue(R.styleable.preference_contentColor)) {
                contentColor = a.getColorStateList(R.styleable.preference_contentColor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        a.recycle();
        initView();
    }

    private void initView() {
        try {
            LayoutInflater.from(getContext()).inflate(R.layout.view_preference, this);
            rlRight = findViewById(R.id.rl_right_preference);
            tvTitle = (TextView) findViewById(R.id.tv_title_perference);
            tvTitle.setTextColor(titleColor);
            tvContent = (TextViewParserEmoji) findViewById(R.id.tv_content_perference);
            tvContent.setTextColor(contentColor);
            littleTitle = (TextView) findViewById(R.id.tv_little_title_perference);
            littleTitle.setTextColor(R.color.grey);
            ivMore = findViewById(R.id.iv_more_preference);
            ivIcon = (ImageView) findViewById(R.id.iv_icon_preference);
            leftIcon = (ImageView) findViewById(R.id.iv_left_icon_perference);
            checkBox = (CheckBox) findViewById(R.id.cb_preference);

            if (!Utils.isEmpty(title)) {
                setTitle(title);
            }
            if (!Utils.isEmpty(content)) {
                setContent(content);
            }
            if (drawableId > 0) {
                setDrawable(drawableId);
            }

            if (iconId > 0) {
                ivIcon.setImageResource(iconId);
                // ivIcon.setImageBitmap(bitmap);
                ivIcon.setVisibility(View.VISIBLE);
            }
            if (leftIconId > 0) {
                leftIcon.setImageResource(leftIconId);
                leftIcon.setVisibility(View.VISIBLE);
            }
            if (hasMore) {
                rlRight.setVisibility(View.VISIBLE);
                ivMore.setVisibility(View.VISIBLE);
            }
            if (hasCheck) {
                rlRight.setVisibility(View.VISIBLE);
                checkBox.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void setTitle(String title) {
        // TODO Auto-generated method stub
        this.title = title;
        tvTitle.setText(title);

    }

    public void setTitleColor(ColorStateList color) {
        this.titleColor = color;
        tvTitle.setTextColor(color);
    }


    public void setContent(String content) {
        // TODO Auto-generated method stub
        this.content = content;
        tvContent.setEmojiText(content);
        // tvContent.setText(content);
        // tvContent.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        // double viewWidth = (double) tvContent.getMeasuredWidth();
        // TextPaint paint = tvContent.getPaint();
        // double textWidth = (double) paint.measureText(content);
        // tvContent.setEmojiText(content, viewWidth, textWidth);

    }

    public void setDrawable(int resId) {
        if (resId <= 0) {
            return;
        }
        drawableId = resId;
        tvContent.setCompoundDrawablesWithIntrinsicBounds(0, 0, resId, 0);
    }

    public void setChecked(boolean isChecked) {
        checkBox.setChecked(isChecked);
    }

    public boolean getChecked() {
        if (hasCheck) {
            return checkBox.isChecked();
        }
        return false;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public void setRightIconSize(int width, int height) {
        ViewGroup.LayoutParams rlp = new LinearLayout.LayoutParams(width, height);
        ivIcon.setLayoutParams(rlp);
    }

    public ImageView getRightImage() {
        return ivIcon;
    }

    public void setRightIcon(int drawable) {
        ivIcon.setImageResource(drawable);
    }

    public void setRightIcon(Bitmap bitmap) {
        ivIcon.setImageBitmap(bitmap);
    }

    public void setLeftIcon(Bitmap bitmap) {
        leftIcon.setImageBitmap(bitmap);
    }

    @Override
    public boolean performClick() {
        if (hasCheck) {
            boolean isChecked = checkBox.isChecked();
            checkBox.setChecked(!isChecked);
        }
        return super.performClick();
    }

    public void setOnCheckedChangeListener(CheckedChangedListener listener) {
        changedListener = listener;
        checkBox.setOnCheckedChangeListener(this);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (changedListener != null) {
            LogUtil.d(TAG, "onCheckedChanged() isChecked=" + isChecked + "	tag=" + getTag());
            changedListener.onCheckedChanged(this, isChecked);
        }
    }

    public void setLittleText(String text) {
        littleTitle.setText(text);
    }

    public void setContentColor(ColorStateList color) {
        this.contentColor = color;
        this.tvContent.setTextColor(color);
    }
}
