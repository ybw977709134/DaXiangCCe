package com.daxiangce123.android.ui.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.WeakHashMap;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.listener.OnTimeLineHeaderActionListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.BulletManager;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.PhotoView;
import com.daxiangce123.android.ui.view.StickyHeaderListview.StickHeaderListener;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.SparseArray;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.Utils;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.TagConsts;

public class TimeLineAdapter extends BaseAdapter implements StickHeaderListener {

    private OnTimeLineHeaderActionListener onActionListenerra;
    private final static String TAG = "TimeLineAdapter";
    private Context mContext;

    private SparseArray<String> batchIndex;
    /**
     * batchId -> filelist
     */
    private HashMap<String, SparseArray<FileEntity>> batchData;
    /**
     * selected files
     */
    private HashSet<FileEntity> selectedData;

    /**
     * for Adapter timeInMills -> fileList (max length is {@link #numColums})
     */
    private ArrayList<SparseArray<FileEntity>> timeLineList;

    private OnClickListener clickListener;
    private OnLongClickListener longClicklistener;
    private ImageSize avaterSize;
    private ImageSize singleSize;
    private ImageSize doubleSize;
    private ImageSize triSize;
    private int leftWidth;
    private int rightWidth;
    private int spacing;
    private int headerSpacing;
    private WeakHashMap<Integer, View> firstViewMap = new WeakHashMap<Integer, View>();
    // private WeakHashMap<Integer, View> secondViewMap = new
    // WeakHashMap<Integer, View>();
    private LinkedList<PhotoView> photoViews = new LinkedList<PhotoView>();
    private boolean isJoined = true;
    private ViewHolder headerHolder;

    private int numColums = 3;

    public TimeLineAdapter(Context context) {
        this.mContext = context;
    }

    public ArrayList<SparseArray<FileEntity>> getTimeLineList() {
        return timeLineList;
    }

    public void initSizes(View parent) {
        if (avaterSize != null || parent == null) {
            return;
        }

		/* parentWidth = leftWidth + rightWidth */
        /* leftWidth = spacing + avaterWidth + padding */
		/* rightWidth = numColunms * (childWith + padding) */

        spacing = Utils.dp2px(mContext, 7);
        headerSpacing = mContext.getResources().getDimensionPixelSize(R.dimen.preference_padding_bt);
        final int totalWidth = App.SCREEN_WIDTH - (parent.getPaddingLeft() + parent.getPaddingRight());

        rightWidth = totalWidth * 4 / 5;
        leftWidth = totalWidth - rightWidth;

        int avaterWidth = leftWidth - 3 * spacing;

        avaterSize = new ImageSize(avaterWidth, avaterWidth);
        avaterSize.setThumb(true);
        avaterSize.setCircle(true);

        singleSize = getGridSize(1, spacing, rightWidth);
        doubleSize = getGridSize(2, spacing, rightWidth);
        triSize = getGridSize(3, spacing, rightWidth);
    }

    private ImageSize getGridSize(int numColum, int padding, int parentWidth) {
        int totalSpacing = numColum * padding;
        int itemWidth = (parentWidth - totalSpacing) / numColum;

        ImageSize size = new ImageSize(itemWidth, itemWidth);
        size.setThumb(true);
        return size;
    }

    /**
     * <p/>
     * If this is called. No NEED TO CALL {@link #updateTimeList()}
     *
     * @param memberData
     * @param selectedData
     * @param memberIndex
     * @time Apr 17, 2014
     */
    public void setData(HashMap<String, SparseArray<FileEntity>> memberData, HashSet<FileEntity> selectedData, SparseArray<String> memberIndex) {
        this.batchData = memberData;
        this.batchIndex = memberIndex;
        this.selectedData = selectedData;
        updateTimeList();
    }

    public void setClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public void setLongClickListener(OnLongClickListener longClickListener) {
        this.longClicklistener = longClickListener;
    }

    public void setIsJoined(boolean joined) {
        this.isJoined = joined;
    }

    public boolean containSize(ImageSize size) {
        if (size == null) {
            return false;
        }
        if (size == avaterSize || size == singleSize || size == doubleSize || size == triSize) {
            return true;
        }
        return false;
    }

    public ImageSize getAvaterSize() {
        return avaterSize;
    }

    private String getCreateDate(int position) {
        int count = getCount();
        if (position < 0 || position >= count) {
            return null;
        }
        SparseArray<FileEntity> sa = getItem(position);
        if (sa == null) {
            return null;
        }
        return sa.valueAntiAt(0).getCreateDate();
    }

    private String getBatchId(int position) {
        SparseArray<FileEntity> l = getItem(position);
        if (l == null || l.size() == 0) {
            return null;
        }
        return l.valueAt(0).getBatchId();
    }

    /**
     * whenever data is changed. This must be called before
     * {@link #notifyDataSetChanged()}.
     * <p/>
     * If {@link #setData(HashMap, HashSet, SparseArray)} is called. No NEED TO
     * CALL THIS
     *
     * @time Apr 17, 2014
     */
    public void updateTimeList() {
        if (timeLineList == null) {
            timeLineList = new ArrayList<SparseArray<FileEntity>>();
        } else {
            timeLineList.clear();
        }
        if (batchData == null || batchIndex == null) {
            return;
        }
        int batchSize = batchIndex.size();
        for (int i = 0; i < batchSize; i++) {
            // DESC
            String batchId = batchIndex.valueAt(batchSize - 1 - i);
            SparseArray<FileEntity> sa = batchData.get(batchId);
            if (sa == null) {
                continue;
            }
            final int fileSize = sa.size();
            final int remainder = fileSize % numColums;
            for (int j = 0; j < fileSize; j++) {
                SparseArray<FileEntity> list = new SparseArray<FileEntity>();
                if (j < remainder) {
                    while (j < remainder && list.size() < remainder) {
                        FileEntity file = sa.valueAt(j);// ASC
                        final int seqNum = file.getSeqNum();
                        list.put(seqNum, file, false);
                        if (list.size() >= remainder) {
                            break;
                        }
                        j++;
                    }
                } else {
                    while (j < fileSize && list.size() < numColums) {
                        FileEntity file = sa.valueAt(j);// ASC
                        final int seqNum = file.getSeqNum();
                        list.put(seqNum, file, false);
                        if (list.size() >= numColums) {
                            break;
                        }
                        j++;
                    }
                }

                if (list.size() == 0) {
                    list = null;
                    continue;
                }
                timeLineList.add(list);

            }
        }
        // printTimelineList();
    }

    void printTimelineList() {
        if (!App.DEBUG) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        String batchId = "";
        int length = timeLineList == null ? 0 : timeLineList.size();
        builder.append("length is " + length);
        for (int i = 0; i < length; i++) {
            SparseArray<FileEntity> array = timeLineList.get(i);
            if (array == null) {
                continue;
            }
            String id = array.valueAt(0).getBatchId();
            if (!batchId.equals(id)) {
                batchId = id;
                builder.append("\n----------------------------------------------------------------------------");
            }
            for (int j = 0; j < array.size(); j++) {
                builder.append("\n	" + array.valueAt(j).getSeqNum() + "	" + array.valueAt(j).getCreateDate());
            }
        }
        LogUtil.d(TAG, "printTimelineList(): " + builder);
    }

    @Override
    public int getCount() {
        if (timeLineList == null) {
            return 0;
        }
        return timeLineList.size();
    }

    @Override
    public SparseArray<FileEntity> getItem(int position) {
        int count = getCount();
        if (position >= count || position < 0) {
            return null;
        }
        return timeLineList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        initSizes(parent);
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.image_timelin_list_view_item, parent, false);
            viewHolder = new ViewHolder(convertView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        SparseArray<FileEntity> data = getItem(position);
        if (viewHolder != null) {
            convertView.setTag(TagConsts.TAG_POSITION, position);
            viewHolder.setTime(getCreateDate(position));
            viewHolder.setData(data, getBatchId(position - 1));
            // viewHolder.tvDate.setTag(position);
            // secondViewMap.put(position, viewHolder.llLeft);
            // if (!viewHolder.showAvator) {
            // convertView.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_NONE);
            // }

            if (viewHolder.showDateIndexer(position)) {
                firstViewMap.put(position, viewHolder.llDateIndexer);
                convertView.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_WITH_DAY_INDCATOR);
            } else if (viewHolder.showAvator) {
                convertView.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_AVATOR_ONLY);
            } else {
                convertView.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_NONE);
            }

        }
        return convertView;
    }

    public void refresh(FileEntity newFile) {
        String fakeIdString = Utils.createEntityHashId(newFile);
        for (PhotoView photoView : photoViews) {
            String fileId = ((FileEntity) photoView.getTag()).getId();
            if (fileId.equals(fakeIdString) || fileId.equals(newFile.getId())) {
                photoView.dismissLoading();
                photoView.setTag(newFile);
                return;
            }
        }
    }

    private class ViewHolder extends HeaderHolder implements PullToRefreshListView.AlbumHeadTabController {
        /**
         * parent of {@link HeaderHolder#ivDay} and {@link HeaderHolder#tvDate}
         */
        private View llDateIndexer;
        /**
         * parent which contains all the photo
         */
        private LinearLayout llPhotos;
        /**
         * parent of {@link HeaderHolder#llLeft}
         */
        private View flLeftOuter;
        private View parent;
        public boolean showAvator;

        public ViewHolder(View parent) {
            super(parent);
            if (parent == null) {
                return;
            }
            this.parent = parent;
            llDateIndexer = parent.findViewById(R.id.ll_date_indexer);
            llPhotos = (LinearLayout) parent.findViewById(R.id.ll_album_photos);
            flLeftOuter = parent.findViewById(R.id.fl_time_avater_outer);

            LinearLayout.LayoutParams rlp = new LinearLayout.LayoutParams(leftWidth, LayoutParams.WRAP_CONTENT);
            flLeftOuter.setLayoutParams(rlp);

            LinearLayout.LayoutParams ivlp = new LinearLayout.LayoutParams(avaterSize.getWidth(), avaterSize.getHeight());
            ivAvater.setLayoutParams(ivlp);
            ivAvater.setOnClickListener(clickListener);

        }

        public void setOwner(String owner) {
            if (ivAvater == null) {
                return;
            }
            if (ivAvater.getVisibility() != View.VISIBLE) {
                ivAvater.setVisibility(View.VISIBLE);
            }
            UserInfo info = null;
            Object obj = ivAvater.getTag();
            if (obj instanceof UserInfo) {
                info = (UserInfo) obj;
            } else {
                info = new UserInfo();
            }
            info.setId(owner);
            ivAvater.setTag(info);
        }

        public void setData(SparseArray<FileEntity> data, String lastBatchId) {
            if (data == null) {
                return;
            }
            int size = data.size();
            if (size <= 0) {
                return;
            }

            FileEntity fileEntity = data.valueAt(0);
            setOwner(fileEntity.getOwner());
            flLeftOuter.setVisibility(View.VISIBLE);
            super.setData(fileEntity);

            int viewCount = llPhotos.getChildCount();
            if (viewCount > size) {
                for (int i = 0; i < viewCount - size; i++) {
                    View view = llPhotos.getChildAt(viewCount - i - 1);
                    view.setVisibility(View.GONE);
                }
            } else if (viewCount < size) {
                for (int i = 0; i < size - viewCount; i++) {
                    PhotoView photoView = new PhotoView(mContext);
                    photoViews.add(photoView);
                    llPhotos.addView(photoView);
                    LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    llp.rightMargin = spacing;
                    photoView.setLayoutParams(llp);
                }
            }

            ImageSize currentSize;
            if (fileEntity.getBatchId().equals(lastBatchId)) {
                llLeft.setVisibility(View.GONE);
                showAvator = false;
            } else {
                llLeft.setVisibility(View.VISIBLE);
                showAvator = true;
            }

            if (size == 1) {
                currentSize = singleSize;
            } else if (size == 2) {
                currentSize = doubleSize;
            } else {
                currentSize = triSize;
            }

            for (int i = 0; i < size; i++) {
                // llPhotos has a right padding which has a size gridSpacing
                FileEntity file = data.valueAt(i);
//                if (App.DEBUG) {
//                    LogUtil.d(TAG, " --setData-- + i " + i + " -- file-- " + file);
//                }
                BulletManager.instance().addFile(file);
                PhotoView photoView = (PhotoView) llPhotos.getChildAt(i);
                photoView.setOnClickListener(clickListener);
                photoView.setOnLongClickListener(longClicklistener);
                photoView.setData(currentSize, file);
                photoView.setTag(file);
                LinearLayout.LayoutParams llp = (LinearLayout.LayoutParams) photoView.getLayoutParams();
                llp.width = currentSize.getWidth();
                llp.height = currentSize.getHeight();
                photoView.resetLoadingSize(currentSize.getWidth(), currentSize.getHeight());

                if (photoView.getVisibility() != View.VISIBLE) {
                    photoView.setVisibility(View.VISIBLE);
                }
                if (isJoined) {

                    photoView.showPhoto();
                } else {
                    photoView.showSamplePhoto();
                }

                if (selectedData != null) {
                    boolean contained = selectedData.contains(file);
                    photoView.checked(contained);
                }
            }
        }

        private boolean showDateIndexer(int position) {
            if (position < 0) {
                return false;
            }
            // if (!showAvator) {
            // llDateIndexer.setVisibility(View.GONE);
            // return false;
            // }
            if (position == 0) {
                parent.setPadding(0, headerSpacing, 0, spacing);
            } else {
                parent.setPadding(0, 0, 0, spacing);
                long timeInDays = TimeUtil.toDay(getCreateDate(position), Consts.SERVER_UTC_FORMAT);
                long lastTimeInDays = TimeUtil.toDay(getCreateDate(position - 1), Consts.SERVER_UTC_FORMAT);
                // llDateIndexer.setVisibility(View.VISIBLE);
                if (lastTimeInDays != timeInDays) {
                    llDateIndexer.setVisibility(View.VISIBLE);
                } else {
                    llDateIndexer.setVisibility(View.GONE);
                    return false;
                }
            }
            return true;
        }

        @Override
        public void hideAvator() {
            parent.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_AVATOR_ONLY_HIDE);
            flLeftOuter.setVisibility(View.INVISIBLE);

        }

        @Override
        public void hideAvatorAndDayNightIndicator() {
            parent.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_WITH_DAY_INDCATOR_HIDE);
            llDateIndexer.setVisibility(View.INVISIBLE);
            flLeftOuter.setVisibility(View.INVISIBLE);
        }

        @Override
        public void showAvator() {
            parent.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_AVATOR_ONLY);
            flLeftOuter.setVisibility(View.VISIBLE);

        }

        @Override
        public void showAvatorAndDayNightIndicator() {
            parent.setTag(TagConsts.TAG_TYPE, TagConsts.TYPE_WITH_DAY_INDCATOR);
            llDateIndexer.setVisibility(View.VISIBLE);
            flLeftOuter.setVisibility(View.VISIBLE);
        }
    }

    // /////////////////////////////////////////////////////

    @Override
    public View getFirstStickyView(int position) {
        if (firstViewMap == null) {
            return null;
        }
        return firstViewMap.get(position);
    }

    @Override
    public View getSecondStickyView(int position) {
        return null;
    }

    public void setHeader(int position) {
        SparseArray<FileEntity> saFiles = getItem(position);
        if (saFiles != null && headerHolder != null) {
            FileEntity fileEntity = saFiles.valueAt(0);
            headerHolder.setTime(getCreateDate(position));
            headerHolder.setData(fileEntity);
            headerHolder.setOwner(fileEntity.getOwner());
        }
    }

    public void initTabHeader(View tabView, ViewGroup listview) {

        initSizes(listview);
        headerHolder = new ViewHolder(tabView);
    }

    @Override
    public View getHeader(int position, View convertView, ViewGroup parent) {

        return convertView;
    }

    private class HeaderHolder {

        public static final int STATE_DAY = 1;
        public static final int STATE_NIGHT = 2;
        public int dayNightState = 0;
        /**
         * show last updated date of current batch
         */
        TextView tvDate;
        /**
         * show if is day or night of current batch
         */
        ImageView ivDay;

        /**
         * parent of {@link #ivAvater} and {@link #tvTime}
         */
        View llLeft;
        ImageViewEx ivAvater;
        public TextView tvTime;

        public HeaderHolder(View parent) {
            if (parent == null) {
                return;
            }
            tvDate = (TextView) parent.findViewById(R.id.tv_date);
            ivDay = (ImageView) parent.findViewById(R.id.iv_day_indexer);
            LinearLayout.LayoutParams llp = new LinearLayout.LayoutParams(leftWidth, tvDate.getLayoutParams().height);
            tvDate.setLayoutParams(llp);
            llLeft = parent.findViewById(R.id.ll_time_avater);
            tvTime = (TextView) parent.findViewById(R.id.tv_timeline_time);
            ivAvater = parent.findViewById(R.id.iv_timeline_user_icon) == null ? null : (ImageViewEx) parent.findViewById(R.id.iv_timeline_user_icon);
            if (ivAvater != null) {
                LinearLayout.LayoutParams llpAvater = new LinearLayout.LayoutParams(avaterSize.getWidth(), avaterSize.getHeight());
                ivAvater.setLayoutParams(llpAvater);
                // ivAvater.setOnClickListener(clickListener);
            }

        }

        public void setTime(String date) {
            long mills = TimeUtil.toLong(date, Consts.SERVER_UTC_FORMAT);
            tvDate.setText(TimeUtil.humanizeDate(mills));
            if (tvTime != null) {
                String time = TimeUtil.formatTime(mills, "HH:mm");
                tvTime.setText(time);
            }
            boolean dayTime = TimeUtil.dayTime(mills);
            if (dayTime && (dayNightState != STATE_DAY)) {
                dayNightState = STATE_DAY;
                ivDay.setImageResource(R.drawable.sun_bg);
                ivDay.setBackgroundResource(R.drawable.day_corners);
            } else if (!dayTime && dayNightState != STATE_NIGHT) {
                dayNightState = STATE_NIGHT;
                ivDay.setBackgroundResource(R.drawable.night_corners);
                ivDay.setImageResource(R.drawable.moon_bg);

            }
        }

        void setData(FileEntity entity) {
            if (entity == null) {
                return;
            }
            ivAvater.setImageResource(R.drawable.default_image_small);
            ImageManager.instance().loadAvater(ivAvater, entity.getOwner());
        }
    }

    public void setOnActionListenerra(OnTimeLineHeaderActionListener onActionListenerra) {
        this.onActionListenerra = onActionListenerra;
    }

    public void clear() {
        // for (PhotoView photoView : photoViews) {
        // photoView = null;
        // }
        // photoViews.clear();
    }
}
