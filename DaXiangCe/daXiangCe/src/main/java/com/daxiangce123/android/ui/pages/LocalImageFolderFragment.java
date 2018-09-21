package com.daxiangce123.android.ui.pages;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.Task;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.Folder;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.listener.CapturePictureListener;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.activities.LocalImageActivity;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.ImageViewEx;
import com.daxiangce123.android.ui.view.LoadingDialog;
import com.daxiangce123.android.uil.UILUtils;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author ram
 * @project Pickup
 * @time Jan 24, 2014
 */
public class LocalImageFolderFragment extends BaseFragment implements
        OnClickListener {

    protected final String TAG = "LocalImageFolderFragment";

    private ImageSize size;
    private View contentView;
    private int height = 100;
    private int padding = 5;
    private ListView lvFolders;
    private FolderAdapter folderAdapter;
    private List<Folder> imageFolders;
    private boolean isLoading = false;
    private boolean needVideo = true;
    private boolean disablePhotoPreview = false;
    private int maxChoosen = 100;
    private int type = LocalImageActivity.CHOOSE_TYPE_IMAGE;
    private BaseFragment fragment;
    private HashMap<String, Integer> positionMap = new HashMap<String, Integer>();

    private boolean orginBitmap = false;
    private ImageView mShoot;
    private ArrayList<UploadImage> destPaths;

    private CapturePictureListener capturePictureListener;

    // private String albumId;

    @Override
    public String getFragmentName() {
        return "LocalImageFolderFragment";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (contentView == null) {
            contentView = inflater.inflate(
                    R.layout.fragment_local_image_folder, container, false);
            initUI();
        } else {
            ViewUtil.removeFromParent(contentView);
        }
        loadImage();
        return contentView;
    }

    private void initUI() {
        height = Utils.getDip(height);
        size = new ImageSize(height, height);
        size.setThumb(true);
        padding = Utils.getDip(padding);

        folderAdapter = new FolderAdapter();
        lvFolders = (ListView) contentView
                .findViewById(R.id.lv_local_image_folder);
        lvFolders.setCacheColorHint(0x00000000);
        lvFolders.setAdapter(folderAdapter);
        lvFolders.setDivider(null);
        lvFolders.setPadding(padding, padding, padding, padding);

        mShoot = (ImageView) contentView.findViewById(R.id.iv_shoot);
        mShoot.setOnClickListener(this);

        if (type == LocalImageActivity.CHOOSE_TYPE_QRCODE) {
            mShoot.setVisibility(View.GONE);
        } else {
            mShoot.setVisibility(View.VISIBLE);
        }

        // showShootBtn();
    }

    // private void showShootBtn() {
    // if (albumId == null) {
    // mShoot.setVisibility(View.GONE);
    //
    // } else {
    // mShoot.setVisibility(View.VISIBLE);
    // }
    // }

    private void loadImage() {
        if (isLoading) {
            return;
        }
        LoadingDialog.show(R.string.loading);
        TaskRuntime.instance().run(new Task() {

            @Override
            public void run() {
                isLoading = true;
                imageFolders = FileUtil.getImageFolders(getActivity(),
                        needVideo);
                runOnUI(new Runnable() {

                    @Override
                    public void run() {
                        LoadingDialog.dismiss();
                        if (!Utils.isEmpty(imageFolders)) {
                            folderAdapter.setData(imageFolders);
                            folderAdapter.notifyDataSetChanged();
                        }
                        isLoading = false;
                    }
                });
            }
        });
    }

    public void setMaxChoosen(int maxChoosen, boolean needVideo) {
        this.maxChoosen = maxChoosen;
        this.needVideo = needVideo;
    }

    public void setMaxChoosen(int maxChoosen, boolean needVideo,
                              boolean disablePhotoPreview, int type) {
        this.maxChoosen = maxChoosen;
        this.needVideo = needVideo;
        this.disablePhotoPreview = disablePhotoPreview;
        this.type = type;
    }


    public void setCapturePictureListener(
            CapturePictureListener capturePictureListener) {
        this.capturePictureListener = capturePictureListener;
    }

    @Override
    public void onClick(View view) {
        if (view == null) {
            return;
        }
        if (view.getTag() instanceof Folder) {
            Folder folder = (Folder) view.getTag();
            JSONObject jo = new JSONObject();
            jo.put(Consts.FOLDER, folder);
            jo.put(Consts.MAX_CHOOSEN, maxChoosen);
            jo.put(Consts.DISABLE_PHOTO_PREVIEW, disablePhotoPreview);
            jo.put(Consts.CAPTURE_PICTURE_LISTENER, capturePictureListener);
            jo.put(Consts.TYPE, type);
            if (fragment == null) {
                LocalImageGridFragment gridFragment = new LocalImageGridFragment();
                gridFragment.setCapturePictureListener(capturePictureListener);
                fragment = gridFragment;

            }
            fragment.setData(jo);
            fragment.show(getBaseActivity());
        } else if (view.equals(mShoot)) {
            capturePicture();
        }
    }

    private void capturePicture() {
        if (capturePictureListener != null) {
            capturePictureListener.capturePicture();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private class FolderAdapter extends BaseAdapter {

        private List<Folder> imageFolders;

        public void setData(List<Folder> imageFolders) {
            this.imageFolders = imageFolders;
        }

        @Override
        public int getCount() {
            if (imageFolders == null) {
                return 0;
            }
            return imageFolders.size();
        }

        @Override
        public Folder getItem(int position) {
            if (Utils.isEmpty(imageFolders)) {
                return null;
            }
            if (position >= getCount()) {
                return null;
            }
            return imageFolders.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (!(convertView instanceof FolderView)) {
                convertView = new FolderView(parent.getContext());
                convertView.setOnClickListener(LocalImageFolderFragment.this);
            }
            Folder folder = getItem(position);
            ((FolderView) convertView).setData(folder);
            positionMap.put(folder.getChild(), position);
            return convertView;
        }

    }

    private class FolderView extends LinearLayout {

        private Context context;
        private ImageViewEx ivIcon;
        private TextView tvName;
        private TextView tvNum;
        private DisplayImageOptions options;
        private Bitmap defCover;

        public FolderView(Context context) {
            super(context);
            this.context = context;
            initView();
        }

        private void initView() {
            setBackgroundResource(R.drawable.clickable_white);
            setPadding(padding, padding, padding, padding);
            setOrientation(HORIZONTAL);
            setClickable(true);

			/*---------Icon--------*/
            int iconSize = height - padding;
            LayoutParams lp = new LayoutParams(iconSize, iconSize);
            ivIcon = new ImageViewEx(context);
            ivIcon.setScaleType(ScaleType.CENTER_CROP);
            ivIcon.setLayoutParams(lp);

            FrameLayout flIcon = new FrameLayout(context);
//            flIcon.setBackgroundResource(R.drawable.default_image_small);
            if (defCover == null) {
                defCover = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_image_normal);
            }
            Drawable drawable = new BitmapDrawable(context.getResources(), defCover);
            options = UILUtils.getDiaplayOption().showImageOnFail(drawable).showImageForEmptyUri(drawable).showImageOnLoading(new BitmapDrawable(defCover)).build();
            flIcon.setLayoutParams(lp);
            flIcon.addView(ivIcon);
            addView(flIcon);

			/*---------parent of NAME/NUM--------*/
            LinearLayout ll = new LinearLayout(context);
            ll.setOrientation(VERTICAL);
            ll.setGravity(Gravity.CENTER_VERTICAL);
            ll.setPadding(padding, 0, 0, 0);
            LayoutParams lpMM = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.MATCH_PARENT);
            addView(ll, lpMM);

			/*---------TextView--------*/
            tvName = new TextView(context);
            ll.addView(tvName);
            try {
                ColorStateList cst = getResources().getColorStateList(
                        R.color.clickable_black);
                tvName.setTextColor(cst);
            } catch (Exception e) {
                e.printStackTrace();
            }

            tvNum = new TextView(context);
            ll.addView(tvNum);
            try {
                ColorStateList cst = getResources().getColorStateList(
                        R.color.clickable_grey);
                tvNum.setTextColor(cst);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setData(Folder folder) {
            setTag(folder);
            if (folder == null || !folder.valid()) {
                ivIcon.setImageResource(R.drawable.ic_launcher);
                return;
            }
            tvName.setText(folder.getName());
            String nums = context.getString(R.string.total_x_photo,
                    folder.getCount());
            tvNum.setText(nums);

            String childPath = folder.getChild();
            setIcon(childPath);
        }

        public void setIcon(String path) {
            // ivIcon.setImageResource(R.drawable.default_image_small);
            ivIcon.setImageBitmap(null);
            ImageManager.instance().loadLocal(ivIcon, path, options, null);
        }

    }

}
