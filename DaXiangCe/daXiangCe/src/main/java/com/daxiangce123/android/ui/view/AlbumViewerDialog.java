package com.daxiangce123.android.ui.view;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Build;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.listener.NearyAlbumListener;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

/**
 * @project DaXiangCe
 * @time 2014-6-2
 * @author
 */
public class AlbumViewerDialog extends CDialog implements OnClickListener {

	public final static String TAG = "AlbumViewerDialog";

	private NearyAlbumListener albumListener;
	private List<?> albums;
	private ArrayList<AlbumView> viewList;

	private ViewPager viewPager;
	private View contentView;
	private AlbumViewerAdapter viewerAdapter;
	private int curPosition = 0;
	private int PositionOffSet = -1;

	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
			viewPager.removeAllViews();
		}
	};

	private OnPageChangeListener changeListener = new OnPageChangeListener() {

		@Override
		public void onPageSelected(int arg0) {
			if (albumListener != null) {
				albumListener.onAlbumSelected(arg0 - PositionOffSet);
			}
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			if (viewPager != null) {
				viewPager.invalidate();
			}
		}

		@Override
		public void onPageScrollStateChanged(int arg0) {
		}
	};

	public AlbumViewerDialog() {
		super();
		initDialog();
		initUI();
	}

	private void initDialog() {
		setCancelable(true);
		setCanceledOnTouchOutside(true);
		Window window = getWindow();
		window.setWindowAnimations(R.style.AnimBottom);
		window.setGravity(Gravity.BOTTOM);
		setOnDismissListener(onDismissListener);
	}

	private void initUI() {
		contentView = LayoutInflater.from(getContext()).inflate(
				R.layout.fragment_album_viewer, null);
		LayoutParams dlp = new LayoutParams(App.SCREEN_WIDTH,
				LayoutParams.WRAP_CONTENT);
		setContentView(contentView, dlp);

		viewPager = (ViewPager) contentView.findViewById(R.id.vp_container);
		viewPager.setOnPageChangeListener(changeListener);
		viewerAdapter = new AlbumViewerAdapter();
		viewPager.setAdapter(viewerAdapter);

		int margin = (int) (App.SCREEN_WIDTH / 15);
		LinearLayout.LayoutParams vlp = new LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		vlp.rightMargin = margin;
		vlp.leftMargin = margin;
		viewPager.setLayoutParams(vlp);
		// viewPager.setPageMargin(margin);
		// contentView.setPadding(margin, margin, margin, margin);

		try {
			Method setLayerTypeMethod = viewPager.getClass().getMethod(
					"setLayerType", new Class[] { int.class, Paint.class });
			setLayerTypeMethod.invoke(this, new Object[] { 1, null });// View.LAYER_TYPE_SOFTWARE
																		// = 1
		} catch (NoSuchMethodException e) {
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		}

		contentView.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				return viewPager.dispatchTouchEvent(event);
			}
		});

		int padding = Utils.getDip(20);
		int width = (int) (App.SCREEN_WIDTH - 2 * margin) - padding * 2;
		ImageSize imageSize = new ImageSize(width, width);
		imageSize.setHasThumbFile(true);
		imageSize.setThumb(true);
		viewList = new ArrayList<AlbumView>();
		for (int i = 0; i < 3; i++) {
			AlbumView v = new AlbumView(this.getContext());
			v.setJoinClickListener(this);
			v.setImageSize(imageSize);
			viewList.add(v);
		}
	}

	private int getAlbumSize() {
		if (albums == null) {
			return 0;
		}
		return albums.size();
	}

	@Override
	public void show() {
		super.show();
		viewPager.setCurrentItem(curPosition);
	}

	public void setCurrentPosition(int position) {
		this.curPosition = position + PositionOffSet;
	}

	public void setOnAlbumListener(NearyAlbumListener albumListener) {
		this.albumListener = albumListener;
	}

	public void setData(List<?> albums) {
		this.albums = albums;
		if (albums == null) {
			return;
		}
		// viewPager.setOffscreenPageLimit(viewPager.getChildCount());
		notifyDataSetChanged();
	}

	public void notifyDataSetChanged() {
		if (App.DEBUG) {
			LogUtil.d(TAG, "notifyDataSetChanged	" + Utils.sizeOf(albums));
		}
		viewerAdapter.notifyDataSetChanged();
	}

	private AlbumView getView(int position) {
		int size = viewList.size();
		position = position % size;
		return viewList.get(position);
	}

	private View oninstantiateItem(ViewGroup container, int position) {
		Object obj = albums.get(position);
		AlbumView v = getView(position);
		v.setData(obj);
		AlbumEntity album = null;
		if (obj instanceof AlbumEntity) {
			album = (AlbumEntity) obj;
		} else if (obj instanceof NearbyAlbum) {
			album = ((NearbyAlbum) obj).getAlbum();
		}
		if (album != null) {
			v.setSample(albumListener.getSamples(album.getId()));
			v.showPrivate(albumListener.needPasswd(album),
					albumListener.hasJoined(album.getId()));
			// v.setToken(albumListener.getToken(album));
		} else {
			v.setSample(null);
		}
		return v;
	}

	@SuppressLint("NewApi")
	private void joninAlbum(AlbumEntity album) {
		if (album == null) {
			return;
		}
		final String albumId = album.getId();
		final String inviteCode = album.getInviteCode();
		if (!albumListener.needPasswd(album)) {
			JSONObject jo = new JSONObject();
			jo.put(Consts.USER_ID, App.getUid());
			ConnectBuilder.joinAlbum(albumId, inviteCode, jo.toJSONString());
			return;
		}

		View v = LayoutInflater.from(getContext()).inflate(
				R.layout.dialog_input_passwd, null);
		final EditText etInput = (EditText) v.findViewById(R.id.editText1);

		AlertDialog.Builder joinDialog = null;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			joinDialog = new Builder(getContext());
		} else {
			joinDialog = new Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT);
		}
		joinDialog.setTitle(R.string.join_ablum);
		joinDialog.setMessage(R.string.please_input_password);
		DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				if (which == DialogInterface.BUTTON_POSITIVE) {
					String password = etInput.getText().toString();
					if (Utils.isEmpty(password)) {
						CToast.showToast(R.string.havet_input);
					} else {
						JSONObject jo = new JSONObject();
						jo.put(Consts.USER_ID, App.getUid());
						jo.put(Consts.PASSWORD, password);
						ConnectBuilder.joinAlbum(albumId, inviteCode,
								jo.toJSONString());
					}
				}
				dialog.cancel();
			}
		};
		joinDialog.setPositiveButton(R.string.confirm, listener);
		joinDialog.setNegativeButton(R.string.cancel, listener);
		joinDialog.setView(v);
		joinDialog.show();

	}

	@Override
	public void onClick(View v) {
		Object obj = v.getTag();
		if (obj instanceof AlbumEntity) {
			AlbumEntity album = (AlbumEntity) obj;
			if (albumListener.hasJoined(album.getId())) {
				albumListener.openAlbum(album);
			} else {
				joninAlbum(album);
			}
		}
	}

	private class AlbumViewerAdapter extends PagerAdapter {

		@Override
		public int getCount() {
			return getAlbumSize();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public void destroyItem(ViewGroup container, int position, Object object) {
		}

		@Override
		public int getItemPosition(Object object) {
			if (getCount() > 0) {
				return PagerAdapter.POSITION_NONE;
			}
			return super.getItemPosition(object);
		}

		@Override
		public Object instantiateItem(ViewGroup container, int position) {
			View v = oninstantiateItem(container, position);
			ViewUtil.removeFromParent(v);
			container.addView(v);
			return v;
		}
	}

}
