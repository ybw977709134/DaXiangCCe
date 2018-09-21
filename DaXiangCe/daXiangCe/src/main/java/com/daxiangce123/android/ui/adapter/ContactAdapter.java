package com.daxiangce123.android.ui.adapter;

import android.content.Context;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.TextViewParserEmoji;
import com.daxiangce123.android.util.Utils;

import java.util.List;

public class ContactAdapter extends BaseAdapter {

    private static final String TAG = "ContactAdapter";
    private Context mContext = null;
    private List<Contact> noMatchList;
    private List<Contact> registedList;

    private ImageSize imageSize;
    private int showNewPosition = 0;


    private OnClickListener clickListener;
    private AbsListView parentView;
    private boolean DEBUG = true;


    public ContactAdapter(Context context) {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
        mContext = context;
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setImageSize(ImageSize size) {
        this.imageSize = size;
    }

    public ImageSize getImageSize() {
        return imageSize;
    }

    public int getCount() {
        if (Utils.isEmpty(noMatchList) && Utils.isEmpty(registedList)) {
            return 0;
        }
        int count = 2;
        if (noMatchList != null) {
            count += noMatchList.size();
        }
        if (registedList != null) {
            count += registedList.size();
        }

        return count;
    }

    public void setData(List<Contact> noMatchList, List<Contact> registedList) {
        if (noMatchList != null) {
            this.noMatchList = noMatchList;
        }
        if (registedList != null) {
            this.registedList = registedList;
        }

    }

    public Contact getItem(int position) {
        if (position == showNewPosition) {
            return null;
        }
        if (position > showNewPosition && position <= registedList.size()) {
            return registedList.get(position - 1);
        }
        if (position > registedList.size() + 1) {
            return noMatchList.get(position - registedList.size() - 2);
        }
        return null;
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (parentView == null && parent instanceof AbsListView) {
            parentView = (AbsListView) parent;
        }
        mContext = parent.getContext();
        if (position == showNewPosition) {
            // show matchCount
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_count_item, parent, false);
            TextView countTextView = (TextView) convertView.findViewById(R.id.tv_member_count);
            countTextView.setText(Html.fromHtml(mContext.getString(R.string.match_count, registedList.size())));
            return convertView;

        } else if (position == (1 + registedList.size())) {
            // show matchCount
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contact_count_item, parent, false);
            TextView countTextView = (TextView) convertView.findViewById(R.id.tv_member_count);
            countTextView.setText(Html.fromHtml(mContext.getString(R.string.no_match_count, noMatchList.size())));
            return convertView;

        } else {
            ViewHolder viewHolder = null;
            if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
                viewHolder = new ViewHolder();
                convertView = LayoutInflater.from(mContext).inflate(R.layout.member_item, parent, false);
                viewHolder.tvUserName = (TextViewParserEmoji) convertView.findViewById(R.id.tv_member_name);
                viewHolder.userIcon = (ImageViewEx) convertView.findViewById(R.id.member_icon);
                viewHolder.bottomRightIcon = (ImageView) convertView.findViewById(R.id.friend_view_invite);
                convertView.setTag(viewHolder);
                ViewGroup.LayoutParams lp = new RelativeLayout.LayoutParams((int) (imageSize.getWidth() / 1.5), (int) (imageSize.getWidth() / 1.5));
                viewHolder.userIcon.setLayoutParams(lp);
                viewHolder.bottomRightIcon.setOnClickListener(clickListener);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            Contact member = getItem(position);
            viewHolder.tvUserName.setEmojiText(member.getFriend_name());
            viewHolder.setAvatar(member);
            viewHolder.bottomRightIcon.setTag(member);
            convertView.setTag(convertView.getId(), member);
            viewHolder.bottomRightIcon.setVisibility(View.VISIBLE);

        }
        return convertView;
    }

    private class ViewHolder {
        ImageViewEx userIcon = null;
        TextViewParserEmoji tvUserName = null;
        ImageView bottomRightIcon = null;

        public void setAvatar(Contact contact) {
            if (userIcon == null) {
                return;
            }
            userIcon.setImageBitmap(null);
            if (contact == null) {
                return;
            }
            if (imageSize == null) {
                return;
            }
            if (contact.isRegister()) {
                bottomRightIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_friend_view));
            } else {
                bottomRightIcon.setImageDrawable(mContext.getResources().getDrawable(R.drawable.btn_friend_invite));
            }
            if (TextUtils.isEmpty(contact.getPhotoUri())) {
                userIcon.setImageDrawable(App.getAppContext().getResources().getDrawable(R.drawable.contact_avatar));
            } else {
                userIcon.setImageURI(Uri.parse(contact.getPhotoUri()));
            }
        }
    }

}
