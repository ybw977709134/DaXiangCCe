package com.daxiangce123.android.ui.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.listener.OptionListener;
import com.daxiangce123.android.util.LogUtil;

import java.util.List;

public class OptionDialog extends CDialog implements OnItemClickListener, android.view.View.OnClickListener {
    public static final String TAG = "OptionDialog";
    private Context mContext;
    private View mView;
    private ListView mSortImageList;
    private OptionAdapter mAddAlbumPopupAdapter;
    private List<Integer> mDatas;
    protected OptionListener optionListener;
    private int selectedPosX;
    private TextView tvTitle;
    private LinearLayout ivCancle;
    private boolean lastGrey = true;

    public OptionDialog(Context context) {
        super();
        this.mContext = context;
        selectedPosX = -1;
        initDialog();
        initData();
        initView();
    }

    private void initDialog() {
        setCancelable(true);
        setCanceledOnTouchOutside(true);

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        window.setWindowAnimations(R.style.AnimBottom);
        window.setGravity(Gravity.BOTTOM);
    }

    private void initData() {

    }

    private void initView() {
        mView = (View) LayoutInflater.from(mContext).inflate(R.layout.sort_image_popup, null, false);
        mSortImageList = (ListView) mView.findViewById(R.id.lv_sort_image_popup);
        tvTitle = (TextView) mView.findViewById(R.id.tv_title);
        tvTitle.setVisibility(View.GONE);

        ivCancle = (LinearLayout) mView.findViewById(R.id.iv_cancel);
        ivCancle.setVisibility(View.GONE);
        ivCancle.setOnClickListener(this);

        mAddAlbumPopupAdapter = new OptionAdapter(mContext);
        mSortImageList.setAdapter(mAddAlbumPopupAdapter);
        mSortImageList.setOnItemClickListener(this);
        final int dialogWidth = App.SCREEN_WIDTH;
        final int padding = 20;
        LayoutParams lp = new LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT);
        mView.setPadding(padding, padding, padding, padding);
        setContentView(mView, lp);
    }

    public void setOptionListener(OptionListener optionListener) {
        this.optionListener = optionListener;
    }

    public void setSelected(int selected) {
        selectedPosX = selected;
        if (App.DEBUG) {
            LogUtil.d(TAG, "selected" + selectedPosX);
        }
        mAddAlbumPopupAdapter.setPos(selected);
    }

    public void setTitle(CharSequence title) {
        if (title == null || title.length() == 0) {
            tvTitle.setVisibility(View.GONE);
            return;
        }
        tvTitle.setVisibility(View.VISIBLE);
        tvTitle.setText(title);
    }

    public void showCancleView(boolean isShow) {
        if (isShow) {
            ivCancle.setVisibility(View.VISIBLE);
            mAddAlbumPopupAdapter.setItemBackground(isShow);
        } else {
            ivCancle.setVisibility(View.GONE);
        }
    }

    public void setData(List<Integer> options) {
        this.mDatas = options;
        mAddAlbumPopupAdapter.setData(mDatas);
        mSortImageList.setAdapter(mAddAlbumPopupAdapter);
    }

    public void notificationChanged() {
        mAddAlbumPopupAdapter.notifyDataSetChanged();
    }

    protected Object getOptionObj() {
        return this;
    }

    @Override
    public void onClick(View v) {
        if (v.equals(ivCancle)) {
            dismiss();
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (optionListener == null) {
            return;
        }
        if (mDatas == null) {
            return;
        }
        optionListener.OnOptionClick(position, mDatas.get(position), getOptionObj());

        dismiss();
    }

    class OptionAdapter extends BaseAdapter {
        private List<Integer> datas = null;
        private LayoutInflater mInflater = null;
        private int mPos;
        private boolean isAddAlbum;

        public OptionAdapter(Context context) {
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            mPos = -1;
        }

        public void setData(List<Integer> datas) {
            this.datas = datas;
        }

        public void setPos(int pos) {
            mPos = pos;
        }

        public void setItemBackground(boolean isAddAlbum) {
            this.isAddAlbum = isAddAlbum;
        }

        @Override
        public int getCount() {
            if (datas == null) {
                return 0;
            }
            return datas.size();
        }

        @Override
        public Object getItem(int position) {
            return datas.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            TextView btnPopupbutton = null;
//            RelativeLayout popupItem = null;
//            ImageView selectItem = null;
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.add_album_popup_item, null);
//                popupItem = (RelativeLayout) convertView.findViewById(R.id.rl_add_album_item);
//                selectItem = (ImageView) convertView.findViewById(R.id.item_select);
//                btnPopupbutton = (TextView) convertView.findViewById(R.id.bt_add_album_item);
//                btnPopupbutton.setText(datas.get(position));
                viewHolder = new ViewHolder(convertView);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.btnPopupbutton.setText(datas.get(position));
            ColorStateList stateList = null;
            if (isAddAlbum) {
//                btnPopupbutton.setBackgroundResource(R.drawable.add_album_option_blue);
                viewHolder.popupItem.setBackgroundResource(R.drawable.add_album_option_blue);
                stateList = parent.getResources().getColorStateList(R.color.clickable_blue_white);
            } else {
                if (position == mPos) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "position" + position + "selectedPos" + selectedPosX);
                    }
//                    btnPopupbutton.setBackgroundResource(R.drawable.option_blue);
                    viewHolder.selectItem.setVisibility(View.VISIBLE);
                    viewHolder.popupItem.setBackgroundResource(R.drawable.add_album_option_blue);
//                    stateList = parent.getResources().getColorStateList(R.color.clickable_white);
                    stateList = parent.getResources().getColorStateList(R.color.clickable_blue_white);
                } else if (position == getCount() - 1 && lastGrey) {
                    stateList = parent.getResources().getColorStateList(R.color.clickable_black_white);
//                    btnPopupbutton.setBackgroundResource(R.drawable.option_grey);
                    viewHolder.popupItem.setBackgroundResource(R.drawable.option_grey);
                    viewHolder.selectItem.setVisibility(View.GONE);
                } else {
                    stateList = parent.getResources().getColorStateList(R.color.clickable_blue_white);
//                    btnPopupbutton.setBackgroundResource(R.drawable.add_album_option_blue);
                    viewHolder.popupItem.setBackgroundResource(R.drawable.add_album_option_blue);
                    viewHolder.selectItem.setVisibility(View.GONE);
                }
            }

            viewHolder.btnPopupbutton.setTextColor(stateList);
            return convertView;
        }
    }

    public boolean isLastGrey() {
        return lastGrey;
    }

    public void setLastGrey(boolean lastGrey) {
        this.lastGrey = lastGrey;
    }

    private class ViewHolder {
        private TextView btnPopupbutton = null;
        private RelativeLayout popupItem = null;
        private ImageView selectItem = null;

        public ViewHolder(View parent) {
            popupItem = (RelativeLayout) parent.findViewById(R.id.rl_add_album_item);
            selectItem = (ImageView) parent.findViewById(R.id.item_select);
            btnPopupbutton = (TextView) parent.findViewById(R.id.bt_add_album_item);
        }

    }

}
