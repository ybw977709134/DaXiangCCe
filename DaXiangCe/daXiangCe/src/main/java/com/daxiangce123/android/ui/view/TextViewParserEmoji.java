package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.util.AttributeSet;
import android.widget.TextView;

import com.daxiangce123.android.util.EmojiParser;

public class TextViewParserEmoji extends TextView {
    private Context mContext;
    private static final String TAG = "TextViewParserEmoji";

    public TextViewParserEmoji(Context context) {
        super(context);
        this.mContext = context;
    }

    public TextViewParserEmoji(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public TextViewParserEmoji(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mContext = context;
    }

    public void setEmojiText(String text) {
        if (text != null) {
            text = text.replace(" ", "");
            SpannableStringBuilder ssb = EmojiParser.getInstance().convetToEmoji(text, mContext);
            setText(ssb);
        }

    }

}
