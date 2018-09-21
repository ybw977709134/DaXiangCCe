package com.daxiangce123.android.ui.view;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.view.KeyEvent;

import com.daxiangce123.R;
import com.daxiangce123.android.App;

/**
 * @project Pickup
 * @time 2013-11-19
 * @author ram
 */
public class CDialog extends Dialog implements OnKeyListener {

	public interface PDialogBackListener {
		public void onPBackPressed(DialogInterface dialog);
	}

	private PDialogBackListener backListener;
	private boolean isBackable = false;

	// private int color;
	private OnDismissListener onDismissListener = new OnDismissListener() {

		@Override
		public void onDismiss(DialogInterface dialog) {
		}
	};

	public CDialog() {
		this(R.style.custom_dialog);
	}

	public CDialog(int style) {
		super(App.getActivity(), style);
		setCancelable(false);
		// setContentView(R.layout.view_yprocess_dialog);
		setOnKeyListener(this);
		setOnDismissListener(onDismissListener);
		// setBackground(color);
		// Window window = getWindow();
		// WindowManager.LayoutParams params = window.getAttributes();
		// float density = getDensity(getContext());
		// params.width = (int) (width * density);
		// params.height = (int) (height * density);
		// params.gravity = Gravity.CENTER;
		// window.setAttributes(params);
	}

	public void setBackListener(PDialogBackListener backListener) {
		this.backListener = backListener;
	}

	public void removeYBackListener() {
		this.backListener = null;
	}

	public void setBackable(boolean isBackable) {
		this.isBackable = isBackable;
	}

	@Override
	public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_UP
				&& keyCode == KeyEvent.KEYCODE_BACK) {
			if (isBackable) {
				dismiss();
			}
			if (backListener != null) {
				backListener.onPBackPressed(dialog);
			}
		}
		return false;
	}

	// public void setBackground(int color) {
	// this.color = color;
	// }

	public void onDismiss(DialogInterface dialog) {

	}

}
