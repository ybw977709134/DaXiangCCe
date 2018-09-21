package com.daxiangce123.android.ui.pages;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.Folder;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.listener.CapturePictureListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.activities.LocalImageActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.util.ArrayList;

/**
 * @author ram
 * @project Pickup
 * @time Jan 27, 2014
 */
public class LocalImageGridFragment extends BaseFragment implements OnItemClickListener, OnClickListener, OnCheckedChangeListener {

    protected final String TAG = "LocalImageGridFragment";

    private View contentView;
    private int height;
    private int spacing = 3;
    private int numClomns = 3;
    private int maxChoosen = 100;
    private boolean disablePhotoPreview = false;
    private int type = LocalImageActivity.CHOOSE_TYPE_IMAGE;
    private Folder mFolder;
    private GridView gvFolders;
    private GridAdapter folderAdapter;
    private TextView confirmTextView, previewTextView;
    private CheckBox cbOrginImage;
    private TextView fileSize, title;

    private boolean orginBitmap = false;

    private ArrayList<UploadImage> selectedPath = new ArrayList<UploadImage>();
    private CapturePictureListener capturePictureListener;

    @Override
    public String getFragmentName() {
        return "LocalImageGridFragment";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        selectedPath = new ArrayList<UploadImage>();
        if (contentView == null) {
            contentView = inflater.inflate(R.layout.fragment_local_image_grid, container, false);
            initUI();
            // if(maxChoosen>1){
            // CToast.showToast(R.string.video_duration_limit);
            // }
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        updateUI();
        return contentView;
    }

    private void initUI() {
        contentView.findViewById(R.id.cancel_local_grid).setOnClickListener(this);
        confirmTextView = (TextView) contentView.findViewById(R.id.confirm_upload);
        previewTextView = (TextView) contentView.findViewById(R.id.tv_preview_local_image_detail);
        title = (TextView) contentView.findViewById(R.id.title);
        confirmTextView.setOnClickListener(this);
        previewTextView.setOnClickListener(this);
        if (disablePhotoPreview) {
            previewTextView.setVisibility(View.INVISIBLE);
        }
        /*----------init child views----------*/
        folderAdapter = new GridAdapter();
        gvFolders = (GridView) contentView.findViewById(R.id.gv_local_image_folder);
        gvFolders.setNumColumns(numClomns);
        gvFolders.setAdapter(folderAdapter);
        gvFolders.setOnItemClickListener(this);
        gvFolders.setCacheColorHint(0x00000000);
        /*----------init dimensons----------*/
        spacing = Utils.getDip(spacing);
        gvFolders.setPadding(spacing, 0, spacing, 0);
        int paddings = gvFolders.getPaddingLeft() + gvFolders.getPaddingRight();
        height = (App.SCREEN_WIDTH - numClomns * spacing - paddings) / numClomns;

        gvFolders.setVerticalSpacing(spacing);
        gvFolders.setHorizontalSpacing(spacing);
        cbOrginImage = (CheckBox) contentView.findViewById(R.id.cb_original_local_image_detail);
        fileSize = (TextView) contentView.findViewById(R.id.tv_filesize_local_image_detail);
        cbOrginImage.setOnCheckedChangeListener(this);
        if (type == LocalImageActivity.CHOOSE_TYPE_QRCODE) {
            cbOrginImage.setVisibility(View.GONE);
            confirmTextView.setText(R.string.scan);
            title.setText(R.string.choose_qrcode);
        } else {
            confirmTextView.setText(R.string.upload);
            title.setText(R.string.choose_photo);
        }
        ViewUtil.ajustMaximumVelocity(gvFolders, Consts.DEFAUTL_ABS_SCROLL_RATION);


    }

    public void setCapturePictureListener(CapturePictureListener capturePictureListener) {
        this.capturePictureListener = capturePictureListener;
    }

    private void updateUI() {
        /*-------------update adapter-------------*/
        folderAdapter.setData(mFolder);
        updateGridUi();
    }

    private boolean selected(String path) {
        if (Utils.isEmpty(path)) {
            return false;
        }
        if (Utils.isEmpty(selectedPath)) {
            return false;
        }
        return UploadImage.containFile(selectedPath, path);
    }

    /**
     * put Serializable list into intent。<br>
     *
     * @return LinkedList<String>
     * @time Mar 20, 2014
     */
    private boolean onResult() {
        Intent intent = getActivity().getIntent();
        selectedPath = UploadImage.setCompress(selectedPath, !orginBitmap);
        if (orginBitmap) {
            UMutils.instance().diyEvent(ID.EventUploadOrigin);
        }
        intent.putExtra(Consts.PATH_LIST, selectedPath);
        getActivity().setResult(Activity.RESULT_OK, intent);
        return true;
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
            updateGridUi();
            folderAdapter.notifyDataSetChanged();
        }
        if (jo.containsKey(Consts.DISABLE_PHOTO_PREVIEW)) {
            disablePhotoPreview = jo.getBooleanValue(Consts.DISABLE_PHOTO_PREVIEW);
        }
        if (jo.containsKey(Consts.ORGINIAL_IMAGE)) {
            orginBitmap = jo.getBooleanValue(Consts.ORGINIAL_IMAGE);
            if (orginBitmap && selectedPath != null && selectedPath.size() > 0) {
                cbOrginImage.setChecked(true);
            } else {
                cbOrginImage.setChecked(false);
            }
        }
        if (jo.containsKey(Consts.TYPE)) {
            type = jo.getInteger(Consts.TYPE);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (view instanceof ImageView) {
            if (capturePictureListener != null) {
                capturePictureListener.capturePicture();
            }
        } else if (view instanceof PictureView) {
            handleGridCheck((PictureView) view);
        }
    }

    public void showDetailsFragment() {
        if (disablePhotoPreview) {
            return;
        }
        // show LocalImageDetailsFragment
        JSONObject jo = new JSONObject();
        jo.put(Consts.FOLDER, mFolder);
        jo.put(Consts.MAX_CHOOSEN, maxChoosen);
        jo.put(Consts.CURRENT_SELECT, selectedPath);
        BaseFragment fragment = new LocalImageDetailsFragment();
        fragment.setData(jo);
        fragment.show(getBaseActivity());
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.confirm_upload) {
            onResult();
            getActivity().finish();
        }
        if (id == R.id.cancel_local_grid) {
            back();
        }
        if (id == R.id.tv_preview_local_image_detail) {
            showDetailsFragment();
        }
    }

    public void handleGridCheck(PictureView pictureView) {
        if (pictureView == null) {
            return;
        }
        if (pictureView.getTag() instanceof String) {
            String path = (String) pictureView.getTag();
            if (!mFolder.contains(path)) {
                return;
            }
            int selected = selectedPath == null ? 0 : selectedPath.size();
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
                    pictureView.showSelector(false);
                    CToast.showToast(R.string.video_duration_limit);
                    return;
                }

            } else if (selected > 0) {
                String lastPath = selectedPath.get(selected - 1).filePath;
                if (FileUtil.isVideo(lastPath)) {
                    selectedPath.clear();
                }
            }
            if (selected(path)) {
                UploadImage.removeFile(selectedPath, path);
            } else {
                if (selected >= maxChoosen) {
                    CToast.showToast(getString(R.string.max_x_to_upload, maxChoosen));
                    pictureView.showSelector(false);
                    return;
                } else {
                    selectedPath.add(new UploadImage(path, true));
                }
            }
            folderAdapter.notifyDataSetChanged();
            updateGridUi();
        }
    }

    public void updateGridUi() {
        if (selectedPath.size() == 0) {
            confirmTextView.setEnabled(false);
            confirmTextView.setBackgroundColor(Color.WHITE);
            confirmTextView.setTextColor(Color.parseColor("#d7d7d7"));
            if (type == LocalImageActivity.CHOOSE_TYPE_IMAGE)
                confirmTextView.setText(getResources().getString(R.string.upload));
            fileSize.setText(getString(R.string.original_image));
            fileSize.setTextColor(getResources().getColor(R.color.gray));
            cbOrginImage.setEnabled(false);
            cbOrginImage.setChecked(false);
            previewTextView.setEnabled(false);
        } else {
            int fileLength = 0;
            for (UploadImage uploadImage : selectedPath) {
                fileLength += new File(uploadImage.getFilePath()).length();
            }
            confirmTextView.setEnabled(true);
            confirmTextView.setTextColor(getResources().getColor(R.color.clickable_white_blue));
            confirmTextView.setBackgroundColor(getResources().getColor(R.color.blue));
            if (type == LocalImageActivity.CHOOSE_TYPE_IMAGE)
                confirmTextView.setText(getResources().getString(R.string.upload) + "(" + selectedPath.size() + ")");
            cbOrginImage.setEnabled(true);
            previewTextView.setEnabled(true);
            if (cbOrginImage.isChecked()) {
                fileSize.setText(getString(R.string.original_photo, Utils.formatSize(fileLength)));
                fileSize.setTextColor(getResources().getColor(R.color.black_pressed));
            } else {
                fileSize.setText(getString(R.string.original_image));
                fileSize.setTextColor(getResources().getColor(R.color.gray));
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class GridAdapter extends BaseAdapter {
        private boolean loadImage = true;
        private Folder folder;

        public void setData(Folder folder) {
            this.folder = folder;
        }

        @Override
        public int getCount() {
            if (folder == null) {
                return 0;
            }
            if (type == LocalImageActivity.CHOOSE_TYPE_QRCODE) {
                return folder.getCount();
            } else {
                return folder.getCount() + 1;
            }
        }

        @Override
        public String getItem(int position) {
            if (folder == null) {
                return null;
            }
            if (type == LocalImageActivity.CHOOSE_TYPE_QRCODE) {
                return folder.get(position);
            } else {
                return folder.get(position - 1);
            }
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (type != LocalImageActivity.CHOOSE_TYPE_QRCODE) {

                if (position == 0) {
                    ImageView image = new ImageView(parent.getContext());
                    image.setImageDrawable(getResources().getDrawable(R.drawable.local_take_picture));
                    image.setLayoutParams(new AbsListView.LayoutParams(height, height, Gravity.CENTER));
                    image.setScaleType(ScaleType.CENTER_INSIDE);
                    convertView = image;
                    return convertView;
                }
            }
            if (!(convertView instanceof PictureView)) {
                convertView = new PictureView(parent.getContext());
            }
            String path = getItem(position);
            if (loadImage) {
                ((PictureView) convertView).setData(path);
            }
            ((PictureView) convertView).showSelector(selected(path));
            return convertView;
        }

    }

    private class PictureView extends FrameLayout {
        private Context context;
        private ImageViewEx ivIcon;
        private ImageViewEx ivOverlay;
        private ImageView ivSelector;
        private DisplayImageOptions options;
        private Bitmap defCover;

        public PictureView(Context context) {
            super(context);
            this.context = context;
            initView();
        }

        private void initView() {
            /*---------Icon--------*/
//            setBackgroundResource(R.drawable.default_image_small);
            if (defCover == null) {
                defCover = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image_normal);
            }
            Drawable drawable = new BitmapDrawable(context.getResources(), defCover);
            options = UILUtils.getDiaplayOption().showImageOnFail(drawable).showImageForEmptyUri(drawable).showImageOnLoading(new BitmapDrawable(defCover)).build();
            LayoutParams lp = new LayoutParams(height, height);
            ivIcon = new ImageViewEx(context);
            ivIcon.setScaleType(ScaleType.CENTER_CROP);
            ivIcon.setLayoutParams(lp);
            addView(ivIcon);

            ivOverlay = new ImageViewEx(context);
            ivOverlay.setLayoutParams(lp);
            ivOverlay.setScaleType(ScaleType.CENTER_INSIDE);
            ivOverlay.setImageResource(R.drawable.video_overlay);
            addView(ivOverlay);

            RelativeLayout rlSelector = new RelativeLayout(context);
            rlSelector.setLayoutParams(lp);

            RelativeLayout chLayout = new RelativeLayout(context);

            RelativeLayout.LayoutParams cL = new RelativeLayout.LayoutParams(height / 2, height / 2);
            cL.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            cL.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            cL.rightMargin = Utils.getDip(4);
            cL.bottomMargin = cL.rightMargin;
            chLayout.setLayoutParams(cL);

            RelativeLayout.LayoutParams rll = new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
            rll.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            rll.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
            // rll.rightMargin = Utils.getDip(4);
            rll.bottomMargin = rll.rightMargin;

            ivSelector = new ImageView(context);
            ivSelector.setLayoutParams(lp);
            ivSelector.setScaleType(ScaleType.CENTER);
            ivSelector.setBackgroundColor(0xb20794e1);
            ivSelector.setImageDrawable(getResources().getDrawable(R.drawable.tick));
            ivSelector.setVisibility(GONE);

            rlSelector.addView(chLayout);
            rlSelector.addView(ivSelector);
            addView(rlSelector);
        }

        public void setData(String path) {
            setTag(path);
            if (Utils.isEmpty(path)) {
                return;
            }
            setImage(path);
            if (FileUtil.isVideo(path)) {
                ivOverlay.setVisibility(View.VISIBLE);
            } else {
                ivOverlay.setVisibility(View.GONE);
            }
        }

        public void setImage(String path) {
            if (path == null) {
                return;
            }
            String old_path = (String) ivIcon.getTag();
            if (!path.equals(old_path)) {
                ivIcon.setImageBitmap(null);
                ivIcon.setTag(path);
                ImageManager.instance().loadLocal(ivIcon, path, options, null);
            }

        }

        public boolean showSelector(boolean selected) {
            if (selected) {
                ivSelector.setVisibility(VISIBLE);
            } else {
                ivSelector.setVisibility(GONE);
            }
            return false;
        }

    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            orginBitmap = true;
        } else {
            orginBitmap = false;
        }
        selectedPath = UploadImage.setCompress(selectedPath, !orginBitmap);
        updateGridUi();
    }

}
