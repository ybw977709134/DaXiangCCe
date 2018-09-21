package com.daxiangce123.android.ui.test;

import android.app.Dialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.daxiangce123.android.App;
import com.daxiangce123.android.ui.activities.base.BaseCliqActivity;
import com.daxiangce123.android.ui.view.CDialog;

/**
 * @project DaXiangCe
 * @time 2014-5-8
 * @author
 */
public class TestDialogAcitivity extends BaseCliqActivity implements
		OnClickListener {

	private Dialog curDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		LinearLayout ll = new LinearLayout(this);
		ll.setOrientation(LinearLayout.VERTICAL);
		setContentView(ll);
		Button button = new Button(this);
		button.setText("Show");
		button.setTag("show");
		button.setOnClickListener(this);
		ll.addView(button);

		Button lvbutton = new Button(this);
		lvbutton.setText("listView");
		lvbutton.setTag("listView");
		lvbutton.setOnClickListener(this);
		ll.addView(lvbutton);
	}

	private CDialog cDialog;
	private Button button;

	private void showDialog() {
		if (cDialog == null) {
			cDialog = new CDialog();
			button = new Button(this);
			button.setTag("dismiss");
			button.setOnClickListener(this);
			cDialog.setContentView(button);
			cDialog.setCancelable(true);
		}
		button.setText("" + System.currentTimeMillis());
		cDialog.show();
		curDialog = cDialog;
	}

	private CDialog lvDialog;
	private DiaAdapter diaAdapter;

	private void showLvDialog() {
		if (lvDialog == null) {
			lvDialog = new CDialog();
			lvDialog.setCancelable(true);
			ListView listView = new ListView(this);
			lvDialog.setContentView(listView);
			diaAdapter = new DiaAdapter();
			listView.setAdapter(diaAdapter);
		}
		diaAdapter.notifyDataSetChanged();
		lvDialog.show();
		curDialog = lvDialog;
	}

	@Override
	public void onClick(View v) {
		if (v.getTag().equals("show")) {
			showDialog();
		} else if (v.getTag().equals("listView")) {
			showLvDialog();
		} else if (v.getTag().equals("dismiss")) {
			if (curDialog != null) {
				curDialog.dismiss();
			}
		}
	}

	private class DiaAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return 3;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			Button button = new Button(parent.getContext());
			long mills = System.currentTimeMillis();
			button.setText("" + position + "-" + mills);
			button.setTextColor(mills % 2 == 0 ? Color.BLACK : Color.WHITE);
			button.setBackgroundColor(mills % 2 == 1 ? Color.BLACK
					: Color.WHITE);
			button.setLayoutParams(new AbsListView.LayoutParams(
					App.SCREEN_WIDTH, LayoutParams.WRAP_CONTENT));
			return button;
		}
	}
}
