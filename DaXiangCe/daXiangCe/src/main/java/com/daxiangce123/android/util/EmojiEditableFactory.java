package com.daxiangce123.android.util;

import android.content.Context;
import android.text.Editable;
import android.text.Editable.Factory;
import android.text.SpannableStringBuilder;

/**
 * @project Emoji
 * @time 2013-11-29
 * @author ram
 */
public class EmojiEditableFactory extends Factory {
	private Context mContext;

	public EmojiEditableFactory(Context context) {
		super();
		mContext = context;
	}

	@Override
	public Editable newEditable(CharSequence source) {
		return new EmojiSpannableStringBuilder(source);
	}

	class EmojiSpannableStringBuilder extends SpannableStringBuilder {
		public EmojiSpannableStringBuilder(CharSequence source) {
			super(source);
		}

		// @Override
		// public SpannableStringBuilder replace(int start, int end,
		// CharSequence tb, int tbstart, int tbend) {
		// CharSequence cs = tb;
		// return super.replace(start, end, cs, 0, cs.length());
		// }
	}
}
