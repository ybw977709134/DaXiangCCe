package com.daxiangce123.android.ui.view;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.Html;
import android.text.Spanned;
import android.text.TextPaint;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.util.BitmapUtil;
import com.daxiangce123.android.util.DrawableUtils;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.MimeTypeUtil;
import com.daxiangce123.android.util.MimeTypeUtil.Mime;
import com.daxiangce123.android.util.SparseArray;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.outh.OauthHelper;
import com.daxiangce123.android.util.outh.QQHelper;
import com.daxiangce123.android.util.outh.WBHelper;
import com.daxiangce123.android.util.outh.WXHelper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.yourbay.barcoder.Generator;

/**
 * @author ram
 * @project Cliq
 * @time Mar 21, 2014
 */
public class ShareDialog extends CDialog implements OnItemClickListener, OnClickListener {

    private String TAG = "ShareDialog";
    private GridView gridView;
    private ShareAdapter shareAdapter;
    private Object targetObj;
    private Bitmap targetBitmap;
    private Bitmap defaultBitmap;
    private ArrayList<Item> items;

    private int[] drawables = {R.drawable.weixin, R.drawable.share_weixin_friend, R.drawable.weibo, R.drawable.share_qqzone};

    private int[] strings = {R.string.weixin_friend, R.string.weixin_friends, R.string.sina_weibo, R.string.qq_space};

    private WXHelper wxHelper;
    private QQHelper qqHelper;
    private WBHelper wbHelper;
    private OauthHelper oauthHelper;
    private String title = null, summary = null, link = null, inviteCode = null, imageId = null;
    private String targetUrl;
    private String rawUrl;
    private TextView tvTitle;
    private String ownerName;
    private SparseArray<Intent> itentsArray = new SparseArray<Intent>();
    private ImageView albumCover = new ImageView(getContext());
    private ImageSize imageSize;
    private LinearLayout albumDetail;
    private ImageView ivQR;
    private TextView tvCode;
    private TextView copyCode;
    private View topLine;
    private View bottomLine;
    private boolean isGuide;
    private View.OnClickListener qrClickListener;


    protected ImageLoadingListener loadingListener = new ImageLoadingListener() {

        @Override
        public void onLoadingStarted(String imageUri, View view) {
        }

        @Override
        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
        }

        @Override
        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
            if (imageUri == null) {
                return;
            }

            if (Utils.isEmpty(imageId) || !imageUri.contains(imageId)) {
                return;
            }
            // albumCover.setImageBitmap(loadedImage);
            targetBitmap = loadedImage;
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
        }
    };

    public ShareDialog() {
        super();
        initDialog();
        initUI();
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
        if (items == null) {
            items = new ArrayList<ShareDialog.Item>();
        } else {
            items.clear();
        }

        boolean hasWX = false;
        if (App.getAppContext().getPackageName().equals("com.cliq123")) {
            int[] drawables0 = {R.drawable.ic_facebook, R.drawable.ic_twitter, R.drawable.ic_google_plus, R.drawable.ic_instagram};
            int[] strings0 = {R.string.facebook, R.string.twitter, R.string.google_plus, R.string.instagram};
            strings = strings0;
            drawables = drawables0;
            initIntents();
        } else {
            if (Utils.isAppInstalled(getContext(), "com.tencent.mm")) {
                hasWX = true;
            }
        }
        for (int i = 0; i < strings.length; i++) {
            int strInt = strings[i];
            if (!hasWX) {
                if (strInt == R.string.weixin_friend || strInt == R.string.weixin_friends) {
                    continue;
                }
            }
            items.add(new Item(strings[i], drawables[i]));
        }
    }

    private void initUI() {
        final int dialogWidth = App.SCREEN_WIDTH;
        final int numColums = 4;
        int widthRatio = 3;
        int padding = dialogWidth / (numColums * widthRatio + numColums + 1);
        int width = widthRatio * padding;

        LayoutParams lp = new LayoutParams(dialogWidth, LayoutParams.WRAP_CONTENT);
        View rootView = LayoutInflater.from(App.getActivity()).inflate(R.layout.dialog_share, null);
        // rootView.setPadding(padding, padding, padding, padding * 3 / 2);
        View shareButtonView = rootView.findViewById(R.id.ll_share_button_view);
        shareButtonView.setPadding(padding, padding, padding, padding * 3 / 2);
        setContentView(rootView, lp);
        tvTitle = (TextView) findViewById(R.id.tv_dialog_title);
        topLine = findViewById(R.id.top_line);
        bottomLine = findViewById(R.id.bottom_line);
        int coverwith = ((App.SCREEN_WIDTH - padding * 2) - (2 - 1) * padding) / 2;
        imageSize = new ImageSize(coverwith, coverwith);
        imageSize.setThumb(true);
        albumDetail = (LinearLayout) findViewById(R.id.ll_album_detail);

        ivQR = (ImageView) albumDetail.findViewById(R.id.iv_qr_album_detail);
        ivQR.setOnClickListener(this);

        tvCode = (TextView) albumDetail.findViewById(R.id.tv_invite_code_album_detail);
        tvCode.setOnClickListener(this);
        copyCode = (TextView) albumDetail.findViewById(R.id.tv_copy_code_album_detail);
        copyCode.setOnClickListener(this);

        RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(imageSize.getWidth(), imageSize.getHeight());

        albumCover.setLayoutParams(rlp);

        shareAdapter = new ShareAdapter();
        shareAdapter.setWidth(width);

        gridView = (GridView) rootView.findViewById(R.id.gv_dialog_share);
        gridView.setNumColumns(numColums);
        gridView.setAdapter(shareAdapter);
        gridView.setPadding(0, padding, 0, 0);
        gridView.setOnItemClickListener(this);
        gridView.setHorizontalSpacing(padding);
        gridView.setVerticalSpacing(padding / 2);
        gridView.setSelector(DrawableUtils.getDrawbale(0x00000000));
    }

    // public void show(Object object, Bitmap bitmap, String dialogTitle) {
    // show(object, bitmap, dialogTitle, null);
    // }

    public void setIsGuide(boolean isGuide) {
        this.isGuide = isGuide;
    }

    public void show(Object object, Bitmap bitmap, String dialogTitle, String ownerName) {
        initData();
        show();
        this.ownerName = ownerName;
        targetObj = object;
        targetBitmap = bitmap;
        tvTitle.setText(dialogTitle);
        title = null;
        summary = null;
        link = null;
        imageId = null;
        if (targetObj instanceof FileEntity) {
            FileEntity entity = (FileEntity) targetObj;
            title = entity.getName();
            summary = entity.getNote();
            link = entity.getLink();
            imageId = entity.getId();
            albumDetail.setVisibility(View.GONE);
            topLine.setVisibility(View.GONE);
            bottomLine.setVisibility(View.GONE);
        } else if (targetObj instanceof AlbumEntity) {
            AlbumEntity entity = (AlbumEntity) targetObj;
            title = entity.getName();
            summary = entity.getNote();
            link = entity.getLink();
            imageId = entity.getTrueCover();
            inviteCode = entity.getInviteCode();

            if (targetBitmap == null && !Utils.isEmpty(imageId)) {
                targetBitmap = ImageManager.instance().getBitmap(imageId, imageSize, albumCover);
                if (targetBitmap == null) {
                    loadImage();
                }
            }
            albumDetail.setVisibility(View.VISIBLE);
            topLine.setVisibility(View.VISIBLE);
            bottomLine.setVisibility(View.VISIBLE);
            String url = Consts.URL_ENTITY_VIEWER + link + "&target=qrcode";
            Bitmap bm;
            bm = Generator.encode(url, App.SCREEN_WIDTH / 5 * 2);
            if (bm != null) {
                ivQR.setImageBitmap(bm);
            }
            Spanned spanned = Html.fromHtml(getContext().getResources().getString(R.string.invite_code_is, entity.getInviteCode()));
            tvCode.setText(spanned);
        }
        if (!Utils.isEmpty(link)) {
            targetUrl = Consts.URL_ENTITY_VIEWER + link + "&target=share";
            rawUrl = Consts.URL_ENTITY_RAW + link;
        } else {
            targetUrl = rawUrl = null;
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, " shareDialog	[title = " + title + "]-[summary = " + summary + "]-[targetUrl = " + targetUrl + " ]");
        }
        try {
            if (targetBitmap == null) {
                defaultBitmap = BitmapFactory.decodeResource(App.getAppContext().getResources(), R.drawable.ic_launcher);
                targetBitmap = defaultBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void loadImage() {
        ImageManager.instance().load(albumCover, imageId, imageSize, null, loadingListener, null);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (targetObj == null) {
            return;
        }
        if (position >= strings.length) {
            return;
        }
        int stringId = items.get(position).strRes;
        if (App.DEBUG) {
            String dest = getContext().getResources().getString(stringId);
            LogUtil.d(TAG, "SHARE TO " + dest);
        }

        Consts.SharedMethod sharedMethod = null;

        boolean ifDismiss = true;
        if (stringId == R.string.share_link) {
            shareViaUrl();
            sharedMethod = Consts.SharedMethod.PUBLIC;
        } else if (stringId == R.string.weixin_friend) {
            if (!Utils.isEmpty(imageId) && (targetBitmap == null || targetBitmap == defaultBitmap)) {
                CToast.showToast(R.string.load_album_cover);
                ifDismiss = false;
            } else {
                shareToWeixin(false);
            }
            if (isGuide) {
                UMutils.instance().diyEvent(ID.EventTourShareAlbumToWechat);
            } else {
                UMutils.instance().diyEvent(ID.EventShareAlbumToWechat);
            }
            sharedMethod = Consts.SharedMethod.WECHAT_FRIEND;
        } else if (stringId == R.string.weixin_friends) {

            if (!Utils.isEmpty(imageId) && (targetBitmap == null || targetBitmap == defaultBitmap)) {
                CToast.showToast(R.string.load_album_cover);
                ifDismiss = false;

            } else {
                shareToWeixin(true);
            }
            if (isGuide) {
                UMutils.instance().diyEvent(ID.EventTourShareAlbumToMoments);
            } else {
                UMutils.instance().diyEvent(ID.EventShareAlbumToMoments);
            }

            sharedMethod = Consts.SharedMethod.WECHAR_MOMENT;
        } else if (stringId == R.string.sina_weibo) {
            if (!Utils.isEmpty(imageId) && (targetBitmap == null || targetBitmap == defaultBitmap)) {
                CToast.showToast(R.string.load_album_cover);
                ifDismiss = false;

            } else {
                shareToWeibo();
            }
            if (isGuide) {
                UMutils.instance().diyEvent(ID.EventTourShareAlbumToWeibo);
            } else {
                UMutils.instance().diyEvent(ID.EventShareAlbumToWeibo);
            }

            sharedMethod = Consts.SharedMethod.WEIBO;
        } else if (stringId == R.string.qq_space) {
            shareToQzone();
            if (isGuide) {
                UMutils.instance().diyEvent(ID.EventTourShareAlbumToQzone);
            } else {
                UMutils.instance().diyEvent(ID.EventShareAlbumToQzone);
            }
            sharedMethod = Consts.SharedMethod.QQ_SPACE;
        } else if (stringId == R.string.twitter) {
            shareToTwitter();
            sharedMethod = Consts.SharedMethod.OTHER;
        } else if (stringId == R.string.facebook) {
            shareToFaceBook();
            sharedMethod = Consts.SharedMethod.OTHER;
        }
//        else if (stringId == R.string.google_plus) {
//            shareToPlus();
//            sharedMethod = Consts.SharedMethod.OTHER;
//        } else if (stringId == R.string.instagram) {
//            shareToInstagram();
//            sharedMethod = Consts.SharedMethod.OTHER;
//        }
        if (targetObj != null) {
            if (targetObj instanceof FileEntity) {
                shareFile(sharedMethod, this.imageId);
            } else {
                shareAlbum(sharedMethod, ((AlbumEntity) targetObj).getId());
            }
        }
        if (ifDismiss) {
            dismiss();
        }

        // else if (stringId == R.string.twitter) {
        // } else if (stringId == R.string.facebook) {
        // }
    }

    private boolean isVideo() {
        if (!(targetObj instanceof FileEntity)) {
            return false;
        }
        String mineType = ((FileEntity) targetObj).getMimeType();
        if (Utils.isEmpty(mineType)) {
            return false;
        }
        if (mineType.startsWith("image/")) {
            return false;
        } else if (mineType.startsWith("video/")) {
            return true;
        }
        return false;
    }

    private boolean isImage() {
        if (!(targetObj instanceof FileEntity)) {
            return false;
        }
        String mineType = ((FileEntity) targetObj).getMimeType();
        if (Utils.isEmpty(mineType)) {
            return false;
        }
        if (mineType.startsWith("image/")) {
            return true;
        } else if (mineType.startsWith("video/")) {
            return false;
        }
        return false;
    }

    private void shareViaUrl() {
        if (targetObj == null) {
            return;
        }
        Utils.copyToClipboard(getContext(), targetUrl);
        if (targetObj instanceof AlbumEntity) {
            // AlbumEntity albumEntity = (AlbumEntity) targetObj;
            CToast.showToast(R.string.album_url_copied_succeed);
        } else if (targetObj instanceof FileEntity) {
            // FileEntity fileEntity = (FileEntity) targetObj;
            if (App.DEBUG) {
                LogUtil.d(TAG, "mime type is " + ((FileEntity) targetObj).getMimeType());
            }
            if (isImage()) {
                CToast.showToast(R.string.photo_url_copied_succeed);
            } else if (isVideo()) {
                CToast.showToast(R.string.video_url_copied_succeed);
            } else {
                CToast.showToast(R.string.url_copied_succeed);
            }
        }
    }

    private void shareAlbum(Consts.SharedMethod sharedMethod, String albumId) {
        JSONObject jo = new JSONObject();
        jo.put(Consts.SHARES, 1);
        jo.put(Consts.SHARED_TO, sharedMethod);
        ConnectBuilder.shareAlbum(jo.toString(), albumId);
    }

    private void shareFile(Consts.SharedMethod sharedMethod, String fileId) {
        JSONObject jo = new JSONObject();
        jo.put(Consts.SHARES, 1);
        jo.put(Consts.SHARED_TO, sharedMethod);
        ConnectBuilder.shareFile(jo.toString(), fileId);
    }

    private void shareToWeibo() {
        if (wbHelper == null) {
            wbHelper = new WBHelper();
        }
        targetUrl = targetUrl + "viaweibo";
        if (targetObj instanceof FileEntity) {
            // FileEntity fileEntity = (FileEntity) targetObj;
            if (isImage()) {
                String title = Utils.getString(R.string.share_image_to_webo);
                wbHelper.sendImg(targetBitmap, title);
            } else {
//				wbHelper.sendText(targetUrl);
                String title = Utils.getString(R.string.share_video_to_weibo, targetUrl);
                wbHelper.sendImg(targetBitmap, title);
            }
        } else if (targetObj instanceof AlbumEntity) {
            AlbumEntity albumEntity = (AlbumEntity) targetObj;
            String title = Utils.getString(R.string.share_album_to_weibo, albumEntity.getName(), targetUrl);
            wbHelper.sendImg(targetBitmap, title);
        }
        OauthHelper oauthHelper1 = oauthHelper = wbHelper;
    }

    private void shareToQzone() {
        if (qqHelper == null) {
            qqHelper = new QQHelper();
        }
        if (link == null) {
            return;
        }
        targetUrl = targetUrl + "viaqzone";
        // final String localPath = ImageManager.instance().getImageCachePath(
        // imageId);
        final String localPath = ImageManager.instance().getImageCachePath(imageId);
        String title = Utils.getString(R.string.app_name);
        String summary = null;
        String url = null;

        if (FileUtil.exists(localPath)) {
            url = localPath;
        } else {
            url = ConnectBuilder.getFileUrl(imageId, false);
        }
        if (targetObj instanceof FileEntity) {
            // FileEntity fileEntity = (FileEntity) targetObj;
            if (ownerName != null) {
                summary = Utils.getString(R.string.share_file_to_qq_summary, ownerName);
            } else {
                summary = Utils.getString(R.string.share_file_to_qq_summary_1);
            }

        } else if (targetObj instanceof AlbumEntity) {
            AlbumEntity albumEntity = (AlbumEntity) targetObj;
            summary = Utils.getString(R.string.share_album_to_qzone, albumEntity.getName(), targetUrl);
        }
        // String xxx =
        // (Environment.getExternalStorageDirectory().getAbsolutePath()+
        // "/DCIM/1410577133343.jpeg");
        qqHelper.shareToQQSpace(title, summary, targetUrl, url);
        oauthHelper = qqHelper;
    }

    private void shareToWeixin(boolean toMoments) {
        if (targetObj == null) {
            LogUtil.d(TAG, "targetObj is NULL!!!");
            return;
        }
        if (wxHelper == null) {
            wxHelper = new WXHelper();
        }
        if (toMoments) {
            targetUrl = targetUrl + "viawechat";
        } else {
            targetUrl = targetUrl + "viamoments";
        }
        App.shareToFriends = toMoments;
        App.shareType = Consts.WEI_XIN;
        String title = Utils.getString(R.string.app_name);
        Bitmap thumb = BitmapUtil.resizeBitmap(targetBitmap, 120, 120, false);
        if (targetObj instanceof AlbumEntity) {
            App.shareObject = Consts.ALBUM;
            AlbumEntity albumEntity = (AlbumEntity) targetObj;
            String summary = Utils.getString(R.string.share_album_to_weixin, albumEntity.getNote());
            title = albumEntity.getName();
            wxHelper.sendWebPage(targetUrl, thumb, title, summary, toMoments);
            return;
        }
        App.shareObject = Consts.FILE;
        FileEntity file = (FileEntity) targetObj;
        Mime mime = MimeTypeUtil.getMime(file.getMimeType());
        if (mime == Mime.VID) {
            String summary = Utils.getString(R.string.share_file_to_weixin_summary, ownerName);
            wxHelper.sendWebPage(targetUrl, thumb, summary, "", toMoments);
            return;
        }
        // String path = ImageManager.instance().getImageCachePath(file.getId(),
        // null);
        String path = ImageManager.instance().getImageCachePath(file.getId(), null);
        if (mime == Mime.GIF) {
            if (toMoments) {
                String summary = Utils.getString(R.string.share_file_to_weixin_summary, ownerName);
                wxHelper.sendWebPage(targetUrl, thumb, summary, "", true);
            } else {
                wxHelper.sendGif(path, thumb, false);
            }
        } else {
            wxHelper.sendImage(path, thumb, toMoments);
        }
    }

    private void shareToTwitter() {
        Intent intent = itentsArray.get(R.string.twitter);
        if (intent == null) {
            String msg = Utils.getString(R.string.havet_install_x, Utils.getString(R.string.twitter));
            CToast.showToast(msg);
            return;
        }
        // final String localPath = ImageManager.instance().getImageCachePath(
        // imageId);
        final String localPath = ImageManager.instance().getImageCachePath(imageId);
        if (isVideo()) {
            intent.putExtra(Intent.EXTRA_TEXT, title + " " + rawUrl);
        } else {
            intent.setType("image/*");
            Uri uri = Uri.fromFile(new File(localPath));
            intent.putExtra(Intent.EXTRA_STREAM, uri);
            if (targetObj instanceof AlbumEntity) {
                AlbumEntity albumEntity = (AlbumEntity) targetObj;
                String title = Utils.getString(R.string.share_album_to_twitter, ownerName, albumEntity.getName(), targetUrl);
                intent.putExtra(Intent.EXTRA_TEXT, title);
            } else {
                intent.putExtra(Intent.EXTRA_TEXT, title);
            }
        }
        App.getActivity().startActivity(intent);
    }

    private void shareToFaceBook() {
        final Intent intent = itentsArray.get(R.string.facebook);
        if (intent == null) {
            String msg = Utils.getString(R.string.havet_install_x, Utils.getString(R.string.facebook));
            CToast.showToast(msg);
            return;
        }
        // final String localPath = ImageManager.instance().getImageCachePath(
        // imageId);
        final String localPath = ImageManager.instance().getImageCachePath(imageId);
        if (isVideo()) {
            intent.putExtra(Intent.EXTRA_TEXT, title + " " + rawUrl);
            App.getActivity().startActivity(intent);
        } else {
            if (!FileUtil.exists(localPath)) {
                return;
            }
            TaskRuntime.instance().run(new Runnable() {
                @Override
                public void run() {
                    String destPath = localPath;
                    if (FileUtil.getSuffix(localPath) == null) {
                        String name = FileUtil.getFileName(localPath);
                        destPath = MediaUtil.getTempDir() + "/image" + name + ".png";
                        if (!FileUtil.exists(destPath)) {
                            FileUtil.copy(localPath, destPath);
                        }
                    }
                    intent.setType("image/*");
                    Uri uri = Uri.fromFile(new File(destPath));
                    intent.putExtra(Intent.EXTRA_STREAM, uri);
                    if (targetObj instanceof AlbumEntity) {
                        AlbumEntity albumEntity = (AlbumEntity) targetObj;
                        String title = Utils.getString(R.string.share_album_to_facebook, ownerName, albumEntity.getName(), targetUrl);
                        intent.putExtra(Intent.EXTRA_TEXT, title);
                    } else {
                        intent.putExtra(Intent.EXTRA_TEXT, title);
                    }
                    App.getActivity().startActivity(intent);
                }
            });

        }
    }
//
//    private void shareToInstagram() {
//        Intent intent = itentsArray.get(R.string.instagram);
//        if (intent == null) {
//            String msg = Utils.getString(R.string.havet_install_x, Utils.getString(R.string.instagram));
//            CToast.showToast(msg);
//            return;
//        }
//        // final String localPath = ImageManager.instance().getImageCachePath(
//        // imageId);
//        final String localPath = ImageManager.instance().getImageCachePath(imageId);
//        if (isVideo()) {
//            intent.putExtra(Intent.EXTRA_TEXT, title + " " + rawUrl);
//        } else {
//            intent.setType("image/*");
//            Uri uri = Uri.fromFile(new File(localPath));
//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            if (targetObj instanceof AlbumEntity) {
//                AlbumEntity albumEntity = (AlbumEntity) targetObj;
//                String title = Utils.getString(R.string.share_album_to_instagram, ownerName, albumEntity.getName(), targetUrl);
//                intent.putExtra(Intent.EXTRA_TEXT, title);
//            } else {
//                intent.putExtra(Intent.EXTRA_TEXT, title);
//            }
//        }
//        App.getActivity().startActivity(intent);
//    }
//
//    private void shareToPlus() {
//        Intent intent = itentsArray.get(R.string.google_plus);
//        if (intent == null) {
//            String msg = Utils.getString(R.string.havet_install_x, Utils.getString(R.string.google_plus));
//            CToast.showToast(msg);
//            return;
//        }
//        // final String localPath = ImageManager.instance().getImageCachePath(
//        // imageId);
//        final String localPath = ImageManager.instance().getImageCachePath(imageId);
//        if (isVideo()) {
//            intent.putExtra(Intent.EXTRA_TEXT, title + " " + rawUrl);
//        } else {
//            intent.setType("image/*");
//            Uri uri = Uri.fromFile(new File(localPath));
//            intent.putExtra(Intent.EXTRA_STREAM, uri);
//            if (targetObj instanceof AlbumEntity) {
//                AlbumEntity albumEntity = (AlbumEntity) targetObj;
//                String title = Utils.getString(R.string.share_album_to_google_plus, ownerName, albumEntity.getName(), targetUrl);
//                intent.putExtra(Intent.EXTRA_TEXT, title);
//            } else {
//                intent.putExtra(Intent.EXTRA_TEXT, title);
//            }
//        }
//        App.getActivity().startActivity(intent);
//    }

    private void initIntents() {
        Intent it = new Intent(Intent.ACTION_SEND);
        it.setType("image/*");
        List<ResolveInfo> resInfo = App.getAppContext().getPackageManager().queryIntentActivities(it, 0);
        if (!resInfo.isEmpty()) {
            // List<Intent> targetedShareIntents = new ArrayList<Intent>();
            for (ResolveInfo info : resInfo) {
                Intent targeted = new Intent(Intent.ACTION_SEND);
                targeted.setType("*/*");
                ActivityInfo activityInfo = info.activityInfo;
                String pkgName = activityInfo.packageName;
                if (pkgName.equals("com.twitter.android")) {
                    targeted.setPackage(activityInfo.packageName);
                    itentsArray.put(R.string.twitter, targeted);
                    // targetedShareIntents.add(targeted);
                } else if (pkgName.equals("com.facebook.katana")) {
                    targeted.setPackage(activityInfo.packageName);
                    itentsArray.put(R.string.facebook, targeted);
                } else if (pkgName.equals("com.instagram.android")) {
                    targeted.setPackage(activityInfo.packageName);
                    itentsArray.put(R.string.instagram, targeted);
                } else if (pkgName.equals("com.google.android.apps.plus")) {
                    targeted.setPackage(activityInfo.packageName);
                    itentsArray.put(R.string.google_plus, targeted);
                }
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (oauthHelper == null) {
            return;
        }
        oauthHelper.onActivityResult(requestCode, resultCode, data);
        oauthHelper = null;
        if (resultCode == Activity.RESULT_OK) {
            onShareSuccess();
        }
    }

    private void onShareSuccess() {
        if (targetObj instanceof AlbumEntity) {
            UMutils.instance().diyEvent(ID.EventSharedAlbumToSocialNetworkSuccess);
        } else if (targetObj instanceof FileEntity) {
            UMutils.instance().diyEvent(ID.EventSharedFileToSocialNetworkSuccess);
        }
    }

    private static class Item {
        int strRes;
        int imgRes;

        public Item(int strRes, int imgRes) {
            super();
            this.strRes = strRes;
            this.imgRes = imgRes;
        }
    }

    private class ShareAdapter extends BaseAdapter {
        private int width;

        @Override
        public int getCount() {
            if (items == null) {
                return 0;
            }
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            AbsListView.LayoutParams absLp = new AbsListView.LayoutParams(width, android.widget.AbsListView.LayoutParams.WRAP_CONTENT);

            int textSize = (int) getContext().getResources().getDimension(R.dimen.text_size_tiny);
            int paddingTop = Utils.dp2px(getContext(), 5);

            ColorStateList colors = getContext().getResources().getColorStateList(R.color.clickable_grey);
            Item item = items.get(position);
            LinearLayout ll = new LinearLayout(getContext());
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setGravity(Gravity.CENTER_HORIZONTAL);
            ll.setLayoutParams(absLp);

            ImageView iv = new ImageView(getContext());
            iv.setImageResource(item.imgRes);

            TextView tv = new TextView(getContext());
            TextPaint textPaint = tv.getPaint();
            tv.setSingleLine();
            tv.setText(item.strRes);
            tv.setTextColor(colors);
            textPaint.setTextSize(textSize);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, paddingTop, 0, 0);

            ll.addView(iv);
            ll.addView(tv);
            return ll;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.tv_copy_code_album_detail) {
            Utils.copyToClipboard(getContext(), inviteCode);
            CToast.showToast(R.string.copy_invitation_code_success);
        } else if (id == R.id.iv_qr_album_detail) {
            qrClickListener.onClick(v);
        }

    }

    public void setQrClickListener(View.OnClickListener qrClickListener) {
        this.qrClickListener = qrClickListener;
    }
}
