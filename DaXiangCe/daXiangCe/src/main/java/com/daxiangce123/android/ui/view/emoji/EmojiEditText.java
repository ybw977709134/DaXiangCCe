package com.daxiangce123.android.ui.view.emoji;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

import com.daxiangce123.android.util.EmojiEditableFactory;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.Utils;

/**
 * @author ram
 * @project Emoji
 * @time 2013-11-29
 */
public class EmojiEditText extends EditText {

    public EmojiEditText(Context context) {
        this(context, null);
    }

    public EmojiEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        setEditableFactory(new EmojiEditableFactory(context));
    }

    public void insert(String text) {
        if (Utils.isEmpty(text)) {
            return;
        }
        text = Utils.grepTextBlank(text);
        CharSequence cs = EmojiParser.getInstance().convetToEmoji(text + "", getContext());
        getEditableText().replace(getSelectionStart(), getSelectionEnd(), cs);
    }

}
