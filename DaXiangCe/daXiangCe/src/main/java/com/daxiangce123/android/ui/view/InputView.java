package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.FrameLayout;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.ui.view.emoji.EmojiEditText;
import com.daxiangce123.android.ui.view.emoji.EmojiViewer;
import com.daxiangce123.android.ui.view.emoji.EmojiViewer.EmojiIndex;
import com.daxiangce123.android.ui.view.emoji.EmojiViewer.EmojiListner;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.Utils;

import java.util.LinkedList;

/**
 * @author ram
 * @project Pickup
 * @time Jan 21, 2014
 */
public class InputView extends FrameLayout implements OnClickListener, EmojiListner, OnKeyListener, OnFocusChangeListener {


    public enum MultiType {
        /**
         * image *
         */
        IMAGE,
        /**
         * video *
         */
        VIDEO
    }

    public interface OnInputListener {
        public void onSend(CharSequence content);

        public void checkIsJoined(CharSequence hint, boolean showIME, String showView);
    }

    private Context context;
    private FrameLayout flContainer;
    private View indexEmoji;
    private Button btnSend;
    private EmojiEditText emojiEdit;
    private EmojiViewer emojiViewer;
    private OnInputListener inputListener;

    // private AlbumEntity albumEntity;

    public InputView(Context context) {
        this(context, null);
    }

    public InputView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {
        LayoutInflater.from(context).inflate(R.layout.view_input, this);

        indexEmoji = findViewById(R.id.btn_emoji_index_input);
        indexEmoji.setOnClickListener(this);

        btnSend = (Button) findViewById(R.id.btn_send_indexer_input);
        btnSend.setOnClickListener(this);

        emojiEdit = (EmojiEditText) findViewById(R.id.et_edit_indexer_input);
        emojiEdit.setImeOptions(EditorInfo.IME_ACTION_SEND);
        emojiEdit.setOnClickListener(this);
        emojiEdit.setOnKeyListener(this);
        emojiEdit.setOnFocusChangeListener(this);

        // if (albumEntity.getCommentOff()) {
        // indexEmoji.setClickable(false);
        // btnSend.setClickable(false);
        // emojiEdit.setClickable(false);
        // emojiEdit.setHint("评论已关闭");
        // } else {
        // indexEmoji.setClickable(true);
        // btnSend.setClickable(true);
        // emojiEdit.setClickable(true);
        // emojiEdit.setHint(R.string.add_comment);
        // }

        // try {
        // Class<EditText> cls = EditText.class;
        // Method setSoftInputShownOnFocus = cls.getMethod(
        // "setSoftInputShownOnFocus", boolean.class);
        // setSoftInputShownOnFocus.setAccessible(true);
        // setSoftInputShownOnFocus.invoke(emojiEdit, false);
        // emojiEdit.setInputType(InputType.TYPE_NULL);
        // } catch (Exception e) {
        // e.printStackTrace();
        // }

        flContainer = (FrameLayout) findViewById(R.id.fl_container_input);
    }

    public View getEmojiView() {
        return indexEmoji;
    }

    public Button getSendButton() {
        return btnSend;
    }

    public EmojiEditText getEmojiEditText() {
        return emojiEdit;
    }

    public boolean hasShowenOther() {
        if (flContainer.getChildCount() > 0) {
            return true;
        }
        return false;
    }

    private boolean isShowen(View v) {
        if (flContainer.indexOfChild(v) == -1) {
            return false;
        }
        return true;
    }

    public void showEmojiView() {
        if (isShowen(emojiViewer)) {
            return;
        }
        showNone();
        Utils.hideIME(this);
        if (emojiViewer == null) {
            emojiViewer = new EmojiViewer(context);
            emojiViewer.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, App.SCREEN_WIDTH / 7 * 4));
            emojiViewer.setEmojiListner(this);
            emojiViewer.bindEditText(emojiEdit);
            LinkedList<EmojiIndex> emojis = new LinkedList<EmojiIndex>();
            emojis.add(new EmojiIndex(R.drawable.input_indexer_smile, EmojiParser.emoji_string_list_0));
            emojis.add(new EmojiIndex(R.drawable.input_indexer_flower, EmojiParser.emoji_string_list_1));
            emojis.add(new EmojiIndex(R.drawable.input_indexer_alarm, EmojiParser.emoji_string_list_2));
            emojis.add(new EmojiIndex(R.drawable.input_indexer_car, EmojiParser.emoji_string_list_3));
            emojis.add(new EmojiIndex(R.drawable.input_indexer_clock, EmojiParser.emoji_string_list_4));
            emojiViewer.setEmojis(emojis);
        }
        flContainer.addView(emojiViewer);
        emojiEdit.requestFocus();
    }

    public void showEdit(CharSequence hint, boolean showIME) {
        showNone();
        if (showIME) {
            Utils.showIME();
        }
        emojiEdit.requestFocus();
        emojiEdit.setVisibility(VISIBLE);
        if (hint == null) {
            return;
        }
        emojiEdit.setHint(hint);
    }

    private void showEdit(boolean showIME) {
        showEdit(null, showIME);
    }

    public void showNone() {
        flContainer.removeAllViews();
    }

    private void sendText() {
        if (inputListener != null) {
            inputListener.onSend(emojiEdit.getText());
        }
    }

    public void hideAll() {
        showEdit(false);
    }

    public void setOnInputListner(OnInputListener inputListener) {
        this.inputListener = inputListener;
    }

    public void hideIME() {
        Utils.hideIME(this);
    }

    @Override
    protected void onAttachedToWindow() {
        // showEdit(true);
        super.onAttachedToWindow();
    }

    // public void setAlbum(AlbumEntity albumEntity) {
    // if (albumEntity == null) {
    // return;
    // }
    // LogUtil.d("TAG", "albumEntity" + albumEntity);
    // this.albumEntity = albumEntity;
    // }

    @Override
    protected void onDetachedFromWindow() {
        Utils.hideIME(this);
        super.onDetachedFromWindow();
    }

    ;

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.et_edit_indexer_input) {
            // this view is a EditText. whenever u click it, the ime will show
            inputListener.checkIsJoined(null, true, Consts.SHOW_NONE);
            showNone();
        } else if (id == R.id.btn_emoji_index_input) {
//			showEmojiView();
            inputListener.checkIsJoined(null, false, Consts.SHOW_EMOJI);
        } else if (id == R.id.btn_send_indexer_input) {
            sendText();
            Utils.hideIME(this);
            emojiEdit.setText("");
            emojiEdit.setHint(R.string.add_comment);
            flContainer.removeView(emojiViewer);
        }
    }

    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        if (hasFocus) {
            inputListener.checkIsJoined(null, true, Consts.SHOW_NONE);
        }

    }

    @Override
    public void OnEmojiSelected(String emoji) {
        emoji = EmojiParser.toEmojiTag(emoji);
        emojiEdit.insert(emoji);
    }

    @Override
    public void OnEmojiShowIME() {
        showEdit(true);
    }

    @Override
    public boolean onKey(View v, int keyCode, KeyEvent event) {
        int action = event.getAction();
        if (keyCode == KeyEvent.KEYCODE_ENTER && action == KeyEvent.ACTION_DOWN) {
            // sendText();
            // return true;
        }
        return false;
    }

}
