package com.daxiangce123.android.ui.view.emoji;

import java.util.LinkedList;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.EmojiParser;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

/**
 * @project Emoji
 * @time 2013-11-29
 * @author ram
 */
public class EmojiViewer extends FrameLayout implements OnItemClickListener,
		OnClickListener {

	public interface EmojiListner {
		public void OnEmojiSelected(String emoji);

		public void OnEmojiShowIME();
	}

	public static class EmojiIndex {
		public int imgResId;
		public String[] emoji;

		public EmojiIndex(int imgResId, String[] emoji) {
			this.imgResId = imgResId;
			this.emoji = emoji;
		}
	}

	private int horiSize = 7;
	private int vertSize = 3;
	private Context context;
	private EditText editText;
	private String[] curEmojis;
	private ViewPager vpViewer;
	private LinearLayout llIndex;
	private LinearLayout llIndicator;
	private EmojiListner mEmojiListner;
	private EmojiPagerAdapter emojiPagerAdapter;
	private LinkedList<GridView> gvList = new LinkedList<GridView>();
	private OnPageChangeListener onPageChangeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int position) {
			updateEmojiIndicator(position);
		}

		@Override
		public void onPageScrolled(int position, float positionOffset,
				int positionOffsetPixels) {
		}

		@Override
		public void onPageScrollStateChanged(int state) {
		}
	};

	private final String TAG = "EmojiViewer";
	private final String TAG_IME = "TAG_IME";
	private final String TAG_DEL = "TAG_DEL";

	public EmojiViewer(Context context) {
		this(context, null);
	}

	public EmojiViewer(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init();
	}

	@SuppressLint("NewApi")
	private void init() {
		LayoutInflater.from(getContext()).inflate(R.layout.view_emoji_viewer,
				this);

		emojiPagerAdapter = new EmojiPagerAdapter();
		vpViewer = (ViewPager) findViewById(R.id.vp_container_emoji_viewer);
		vpViewer.setOnPageChangeListener(onPageChangeListener);
		vpViewer.setAdapter(emojiPagerAdapter);

		/*---------------init llIndex/llIndicator-------------*/
		llIndex = (LinearLayout) findViewById(R.id.ll_index_emoji_viewer);
		llIndicator = (LinearLayout) findViewById(R.id.ll_indicator_emoji_viewer);
		/*---------------init gvList-------------*/
		for (int i = 0; i < 3; i++) {
			GridView gv = new GridView(getContext());
			EmojiAdapter adapter = new EmojiAdapter();
			gv.setNumColumns(horiSize);
			gv.setAdapter(adapter);
			gv.setOnItemClickListener(this);
			try {
				gv.setOverScrollMode(OVER_SCROLL_NEVER);
			} catch (Exception e) {
				e.printStackTrace();
			}
			gvList.add(gv);
		}
	}

	private GridView getPageChild(int position) {
		if (Utils.isEmpty(gvList)) {
			return null;
		}
		int size = gvList.size();
		int truePosition = position % size;
		return gvList.get(truePosition);
	}

	private int getPageSize() {
		if (curEmojis == null || curEmojis.length == 0) {
			return 0;
		}
		int pageSize = horiSize * vertSize;
		int len = curEmojis.length;
		int count = len / pageSize;
		int tmp = len % pageSize;
		if (tmp != 0) {
			count = count + 1;
		}
		return count;
	}

	private View updateEmojiPage(int position) {
		int count = curEmojis.length;
		int pageSize = horiSize * vertSize;
		int start = position * pageSize;
		int len = curEmojis.length;
		int pageCount = (position + 1 == count) ? len - pageSize * position
				: pageSize;

		GridView gridView = getPageChild(position);
		EmojiAdapter adapter = (EmojiAdapter) gridView.getAdapter();
		adapter.setData(curEmojis, start, pageCount);
		adapter.notifyDataSetChanged();
		return gridView;
	}

	@SuppressWarnings("deprecation")
	private void updateEmojiIndicator(int position) {
		int size = getPageSize();
		int childCount = llIndicator.getChildCount();

		int dia = 20;
		/*----------------update child NUM--------------*/
		if (size > childCount) {
			LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dia,
					dia);
			lp.leftMargin = dia / 4;
			lp.rightMargin = dia / 4;
			for (int i = 0; i < size - childCount; i++) {
				TextView child = new TextView(context);
				llIndicator.addView(child, lp);
			}
		} else if (size < childCount) {
			for (int i = childCount; i > size; i--) {
				llIndicator.removeViewAt(i - 1);
			}
		}

		/*----------------update child status--------------*/
		for (int i = 0; i < size; i++) {
			View child = llIndicator.getChildAt(i);
			if (i == position) {
				Bitmap b = BitmapUtil.getRound(0xff666666, dia);
				BitmapDrawable drawable = new BitmapDrawable(b);
				child.setBackgroundDrawable(drawable);
			} else {
				Bitmap b = BitmapUtil.getRound(0xffcccccc, dia);
				BitmapDrawable drawable = new BitmapDrawable(b);
				child.setBackgroundDrawable(drawable);
			}
		}
	}

	private void updateIndex(int index) {
		int size = llIndex.getChildCount();
		if (index < 0 || index >= size) {
			return;
		}
		for (int i = 0; i < size; i++) {
			View view = llIndex.getChildAt(i);
			if (i == index) {
				view.setSelected(true);
			} else {
				view.setSelected(false);
			}
		}
	}

	private void selectEmojis(String[] emojis) {
		if (Utils.isEmpty(emojis) || curEmojis == emojis) {
			return;
		}
		LogUtil.d(TAG, "selectEmojis " + emojis.length);
		curEmojis = emojis;
		int count = getPageSize();
		emojiPagerAdapter.setCount(count);
		emojiPagerAdapter.notifyDataSetChanged();
		vpViewer.setCurrentItem(0);
		updateEmojiIndicator(0);
	}

	public void setEmojis(List<EmojiIndex> emojis) {
		if (emojis == null || emojis.isEmpty()) {
			return;
		}
		llIndex.removeAllViews();
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0,
				LayoutParams.MATCH_PARENT, 1.0f);

		// input
		Button btn0 = new Button(context);
		llIndex.addView(btn0, lp);
		btn0.setTag(TAG_IME);
		btn0.setOnClickListener(this);
		btn0.setBackgroundResource(R.drawable.input_indexer_ime);
		// emoji indexer
		for (EmojiIndex ei : emojis) {
			if (ei == null || Utils.isEmpty(ei.emoji)) {
				continue;
			}
			String[] emo = ei.emoji;
			Button btn = new Button(context);
			llIndex.addView(btn, lp);
			btn.setTag(emo);
			btn.setOnClickListener(this);
			btn.setBackgroundResource(ei.imgResId);
			if (Utils.isEmpty(curEmojis)) {
				selectEmojis(emo);
				updateIndex(1);
			}
		}

		// back
		Button btn1 = new Button(context);
		btn1.setBackgroundResource(R.drawable.input_indexer_del);
		llIndex.addView(btn1, lp);
		btn1.setTag(TAG_DEL);
		btn1.setOnClickListener(this);
	}

	public void bindEditText(EditText editText) {
		this.editText = editText;
	}

	public void setEmojiListner(EmojiListner mEmojiListner) {
		this.mEmojiListner = mEmojiListner;
	}

	@Override
	public void onClick(View v) {
		Object tag = v.getTag();
		if (tag instanceof String[]) {
			selectEmojis((String[]) tag);
			int index = llIndex.indexOfChild(v);
			updateIndex(index);
		} else if (TAG_IME.equals(tag)) {
			if (mEmojiListner != null) {
				mEmojiListner.OnEmojiShowIME();
			}
		} else if (TAG_DEL.equals(tag)) {
			// Utils.pressKey(KeyEvent.KEYCODE_DEL);
			ViewUtil.pressDelete(editText);
		} else {

		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		if (mEmojiListner == null) {
			return;
		}
		if (view.getTag() instanceof String) {
			mEmojiListner.OnEmojiSelected(view.getTag().toString());
		}
	}

	class EmojiAdapter extends BaseAdapter {

		private String[] emojis;
		private int start;
		private int count;

		public void setData(String[] emojis, int start, int count) {
			this.emojis = emojis;
			this.start = start;
			this.count = count;
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public String getItem(int position) {
			try {
				return emojis[position + start];
			} catch (Exception e) {
			}
			return null;
		}

		@Override
		public long getItemId(int position) {
			return position + start;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ImageView iv = new ImageView(parent.getContext());
			String emojiSource = getItem(position);
			int id = EmojiParser.getEmojiRes(emojiSource);
			int size = (parent.getMeasuredHeight() / vertSize);
			AbsListView.LayoutParams lp = new AbsListView.LayoutParams(size,
					size);
			iv.setLayoutParams(lp);
			iv.setScaleType(ScaleType.CENTER);
			if (id > 0) {
				iv.setImageResource(id);
			}
			iv.setTag(emojiSource);
			return iv;
		}
	}

	class EmojiPagerAdapter extends PagerAdapter {
		private int count;

		public void setCount(int count) {
			this.count = count;
		}

		@Override
		public int getItemPosition(Object object) {
			if (count > 0) {
				return POSITION_NONE;
			}
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
		}

		@Override
		public int getCount() {
			return count;
		}

		@Override
		public Object instantiateItem(View arg0, int position) {
			try {
				View view = updateEmojiPage(position);
				ViewUtil.removeFromParent(view);
				((ViewPager) arg0).addView(view, 0);
				return view;
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

	}
}
