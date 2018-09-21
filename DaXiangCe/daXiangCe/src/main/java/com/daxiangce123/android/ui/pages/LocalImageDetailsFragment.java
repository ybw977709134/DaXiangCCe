package com.daxiangce123.android.ui.pages;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.Folder;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.CViewPager;
import com.daxiangce123.android.ui.view.PhotoLocalPreview;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author ram
 * @project Pickup
 * @time Jan 27, 2014
 */
public class LocalImageDetailsFragment extends BaseFragment implements View.OnClickListener, ViewPager.OnPageChangeListener {

    protected final String TAG = "LocalImageDetailsFragment";

    private Folder mFolder;
    private int maxChoosen;
    private int currentPosition;
    private View contentView;
    private CViewPager cViewPager;
    private ImagePagerAdapter imagePagerAdapter;
    private TextView confirmView, fileSize;
    private CheckBox selected, original;
    private LinearLayout topBar;
    private RelativeLayout bottomBar;

    private ArrayList<PhotoLocalPreview> viewList = new ArrayList<PhotoLocalPreview>();
    private ArrayList<UploadImage> selectedPath = new ArrayList<UploadImage>();
    private HashMap<String, PhotoLocalPreview> viewMap = new HashMap<String, PhotoLocalPreview>();
    private VideoPlayerFragment playerFragment;

    @Override
    public String getFragmentName() {
        return "LocalImageDetailsFragment";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewList = new ArrayList<PhotoLocalPreview>();
        viewMap = new HashMap<String, PhotoLocalPreview>();
        contentView = inflater.inflate(R.layout.fragment_local_image_details, container, false);
        initUI();
        updateUI();
        return contentView;
    }

    private void initUI() {
        for (int i = 0; i < 3; i++) {
            float zoomRatio = 0.8f;
            if (App.SCREEN_WIDTH <= 720) {
                zoomRatio = 1.0f;
            }

            ImageSize pictureSize = new ImageSize((int) (App.SCREEN_WIDTH * zoomRatio), (int) (App.SCREEN_HEIGHT * zoomRatio));

            PhotoLocalPreview view = new PhotoLocalPreview(getActivity());
            ViewPager.LayoutParams vlp = new ViewPager.LayoutParams();
            vlp.width = pictureSize.getWidth();
            vlp.height = pictureSize.getHeight();
            view.setLayoutParams(vlp);

            viewList.add(view);
            view.setOnClickListener(this);
            view.setImageSize(pictureSize);
        }
        confirmView = (TextView) contentView.findViewById(R.id.confirm_upload);
        fileSize = (TextView) contentView.findViewById(R.id.tv_filesize_local_image_detail);
        selected = (CheckBox) contentView.findViewById(R.id.selected_local_image_detail);
        original = (CheckBox) contentView.findViewById(R.id.cb_original_local_image_detail);
        cViewPager = (CViewPager) contentView.findViewById(R.id.vp_container_picture);
        topBar = (LinearLayout) contentView.findViewById(R.id.ll_topbar_picture_viewer);
        bottomBar = (RelativeLayout) contentView.findViewById(R.id.ll_bottom_panel_picture);
        selected.setOnClickListener(this);
        original.setOnClickListener(this);
        confirmView.setOnClickListener(this);
        imagePagerAdapter = new ImagePagerAdapter();
        imagePagerAdapter.setFolder(mFolder);
        cViewPager.setAdapter(imagePagerAdapter);
        cViewPager.setOnPageChangeListener(this);
        cViewPager.setCurrentItem(currentPosition, false);
        if (selectedPath.size() > 0 && (!selectedPath.get(0).compress)) {
            original.setChecked(true);
        } else {
            original.setChecked(false);
        }
    }

    /**
     * 更新界面 UI
     */
    private void updateUI() {
        String filepathString = mFolder.get(currentPosition);
        selected.setChecked(UploadImage.containFile(selectedPath, filepathString));
        File file = new File(filepathString);
        long fileLength = file.length();
        if (FileUtil.isImage(filepathString)) {
            // only image can compress
            fileSize.setText(getString(R.string.original_photo, Utils.formatSize(fileLength)));
            original.setEnabled(true);
        } else {
            // others disable original checkbox
            original.setChecked(false);
            original.setEnabled(false);
            fileSize.setText(getString(R.string.file_size, Utils.formatSize(fileLength)));
            return;
        }
        //
        if (selectedPath.size() == 0) {
            confirmView.setEnabled(false);
            confirmView.setTextColor(getResources().getColor(R.color.gray));
            confirmView.setText(getResources().getString(R.string.upload));
            confirmView.setBackgroundResource(R.drawable.photo_detail_enable);
            int rightPadding = Utils.getDip(13);
            int topPadding = Utils.getDip(3);
            confirmView.setPadding(rightPadding, topPadding, rightPadding, topPadding);
            confirmView.setTextColor(getResources().getColor(R.color.gray));
            original.setChecked(false);
            // original.setEnabled(false);
            fileSize.setTextColor(getResources().getColor(R.color.gray));
        } else {
            int rightPadding = Utils.getDip(13);
            int topPadding = Utils.getDip(3);

            confirmView.setEnabled(true);
            confirmView.setBackgroundResource(R.drawable.photo_detail_disable);
            confirmView.setText(getResources().getString(R.string.upload) + "(" + selectedPath.size() + ")");
            confirmView.setPadding(rightPadding, topPadding, rightPadding, topPadding);
            confirmView.setTextColor(getResources().getColor(R.color.clickable_white_blue));
            // original.setEnabled(true);
            fileSize.setTextColor(getResources().getColor(R.color.bg));
        }
    }

    private PhotoLocalPreview getView(int position) {
        if (viewList == null) {
            return null;
        }
        int size = viewList.size();
        int truePosition = position % size;
        PhotoLocalPreview view = null;
        if (truePosition < size) {
            view = (PhotoLocalPreview) viewList.get(truePosition);
        }
        return view;
    }

    @Override
    public void setData(JSONObject jo) {
        if (jo == null) {
            return;
        }
        if (jo.containsKey(Consts.FOLDER)) {
            Object obj = jo.get(Consts.FOLDER);
            if (obj instanceof Folder) {
                mFolder = (Folder) obj;
            }
        }
        if (jo.containsKey(Consts.MAX_CHOOSEN)) {
            maxChoosen = jo.getInteger(Consts.MAX_CHOOSEN);
        }
        if (jo.containsKey(Consts.CURRENT_SELECT)) {
            selectedPath = (ArrayList<UploadImage>) jo.getObject(Consts.CURRENT_SELECT, ArrayList.class);
        }
        for (int i = 0; i < mFolder.getCount(); i++) {
            if (mFolder.get(i).equals(selectedPath.get(0).filePath)) {
                currentPosition = i;
                break;
            }
        }

    }

    private class ImagePagerAdapter extends PagerAdapter {
        public Folder mFolder;

        public void setFolder(Folder mFolder) {
            this.mFolder = mFolder;
        }

        @Override
        public int getCount() {
            return mFolder.getCount();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            String file = mFolder.get(position);

            PhotoLocalPreview photoPreview = getView(position);

            if (photoPreview.getTag() instanceof String) {
                String preFile = (String) photoPreview.getTag();
                photoPreview.setImageBitmap(null);
                container.removeView(photoPreview);
            }
            photoPreview.setTag(file);
            viewMap.put(file, photoPreview);
            showMedia(file);
            container.addView(photoPreview);
            photoPreview.setOnVideoClickListener(LocalImageDetailsFragment.this);
            photoPreview.setOnClickListener(LocalImageDetailsFragment.this);
            return photoPreview;
        }

        private Bitmap showMedia(String filePath) {
            if (filePath == null) {
                return null;
            }
            PhotoLocalPreview preview = viewMap.get(filePath);
            preview.setFile(filePath);
            return preview.showFile(filePath == mFolder.get(currentPosition));
        }

        @Override
        public int getItemPosition(Object object) {
            if (getCount() > 0) {
                return PagerAdapter.POSITION_NONE;
            }
            return super.getItemPosition(object);
        }

    }

    @Override
    public void onClick(View v) {
        if (v instanceof PhotoLocalPreview) {
//            toggleBarHideShow();
            onFileClicked();
            return;
        }
        switch (v.getId()) {
            case R.id.cb_original_local_image_detail:
                if (original.isChecked()) {
                    handleChecked(false);
                }
                break;
            case R.id.selected_local_image_detail:
                handleChecked(true);
                break;
            case R.id.confirm_upload:
                onResult();
                getActivity().finish();
                break;
            default:
                // others is viewpager click
//                String currentPath = mFolder.get(currentPosition);
//                if (FileUtil.isVideo(currentPath)) {
//                    playVideo(currentPath);
//                }
                break;
        }

    }

    @Override
    public boolean onBackPressed() {
        back(null);
        return true;
    }

    @Override
    public void back(JSONObject jo) {
        jo = new JSONObject();
        jo.put(Consts.FOLDER, mFolder);
        jo.put(Consts.MAX_CHOOSEN, maxChoosen);
        // jo.put(Consts.POSITION, position);
        jo.put(Consts.CURRENT_SELECT, UploadImage.setCompress(selectedPath, !original.isChecked()));
        jo.put(Consts.ORGINIAL_IMAGE, original.isChecked());
        super.back(jo);
    }

    public void onPageSelected(int position) {
        currentPosition = position;
        updateUI();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }

//    //    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        if (isChecked) {
//            handleChecked(false);
//        }
//    }

    private boolean selected(String path) {
        if (Utils.isEmpty(path)) {
            return false;
        }
        if (Utils.isEmpty(selectedPath)) {
            return false;
        }
        return UploadImage.containFile(selectedPath, path);
    }


    private void onFileClicked() {
        String currentPath = mFolder.get(currentPosition);
        if (FileUtil.isVideo(currentPath)) {
            playVideo(currentPath);
        } else {
            toggleBarHideShow();
        }
    }

    private void toggleBarHideShow() {
        if (topBar.getVisibility() == View.GONE || bottomBar.getVisibility() == View.GONE) {
            topBar.setVisibility(View.VISIBLE);
            bottomBar.setVisibility(View.VISIBLE);
            Utils.setStatusBarVisibility(getActivity(), false);
        } else {
            topBar.setVisibility(View.GONE);
            bottomBar.setVisibility(View.GONE);
            Utils.setStatusBarVisibility(getActivity(), true);
        }
    }

    public void handleChecked(boolean removeIfSelected) {
        String path = mFolder.get(currentPosition);

        int length = selectedPath == null ? 0 : selectedPath.size();
        if (selectedPath == null) {
            selectedPath = new ArrayList<UploadImage>();
        }
        if (FileUtil.isVideo(path)) {
            int videoDuration = FileUtil.getMediaDuration(path);
            if (videoDuration < Consts.VIDEO_DURATION_MAX && videoDuration > Consts.VIDEO_DURATION_MIN) {
                if (!selected(path)) {
                    selectedPath.clear();
                }
            } else {
                selected.setChecked(false);
                CToast.showToast(R.string.video_duration_limit);
                return;
            }
        } else if (length > 0) {
            String lastPath = selectedPath.get(length - 1).filePath;
            if (FileUtil.isVideo(lastPath)) {
                selectedPath.clear();
            }
        }
        if (selected(path) && removeIfSelected) {
            UploadImage.removeFile(selectedPath, path);
        } else {
            if (length >= maxChoosen) {
                CToast.showToast(getString(R.string.max_x_to_upload, maxChoosen));
                selected.setChecked(false);
                original.setChecked(false);
                return;
            } else {
                boolean add = true;
                for (UploadImage uploadImage : selectedPath) {
                    if (uploadImage.getFilePath().equals(path)) {
                        add = false;
                    }
                }
                if (add)
                    selectedPath.add(new UploadImage(path, true));

            }
        }
        updateUI();
    }

    /**
     * put Serializable list into intent。<br>
     *
     * @return LinkedList<String>
     * @time Mar 20, 2014
     */
    private boolean onResult() {
        Intent intent = getActivity().getIntent();
        intent.putExtra(Consts.PATH_LIST, UploadImage.setCompress(selectedPath, !original.isChecked()));
        if (original.isChecked()) {
            UMutils.instance().diyEvent(ID.EventUploadOrigin);
        }
        getActivity().setResult(Activity.RESULT_OK, intent);
        return true;
    }

    private boolean playVideo(String videopath) {
        if (videopath == null) {
            return false;
        }

        if (playerFragment == null) {
            playerFragment = new VideoPlayerFragment();
        }
        playerFragment.setFilePath(videopath);
        playerFragment.show(getBaseActivity());
        return true;
    }
}
