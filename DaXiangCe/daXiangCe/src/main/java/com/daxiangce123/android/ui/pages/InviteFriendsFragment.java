package com.daxiangce123.android.ui.pages;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.ui.pages.base.BaseFragment;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.ui.view.Preference;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;
import com.daxiangce123.android.util.outh.WXHelper;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import me.yourbay.barcoder.Generator;

public class InviteFriendsFragment extends BaseFragment implements OnClickListener {
    private static final String TAG = "InviteFriendsFragment";

    private View mRootView = null;
    private View vWechat;
    private View vSms;
    private TextView mBack;
    private Preference mQRCode;
    private TextView mInvitationCode;
    private Button mCopy;
    private Bitmap barCodeBmp;
    private WXHelper wxHelper;
    private String albumUrl;
    private AlbumEntity albumEntity;
    private String inviteCode;
    private String albumName;
    private String albumLink;
    private Bitmap bitmap;
    private String albumCoverId;
    private ImageView albumCover;
    private ImageSize imageSize;
    private Bitmap defaultBitmap;
    private String qrUrl;

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

            if (Utils.isEmpty(albumCoverId) || !imageUri.contains(albumCoverId)) {
                return;
            }
            // albumCover.setImageBitmap(loadedImage);
            bitmap = loadedImage;
        }

        @Override
        public void onLoadingCancelled(String imageUri, View view) {
        }
    };

    @Override
    public String getFragmentName() {
        return "InviteFriendsFragment";
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        if (mRootView == null) {
            mRootView = inflater.inflate(R.layout.invite_friends_fragment, container, false);
        } else {
            ViewUtil.removeFromParent(mRootView);
        }
        initComponent();
        intitData();
        return mRootView;
    }

    private void initComponent() {
        mBack = (TextView) mRootView.findViewById(R.id.tv_title_ivite_friend_back);
        mQRCode = (Preference) mRootView.findViewById(R.id.pf_album_qr_code);
        mInvitationCode = (TextView) mRootView.findViewById(R.id.tv_invitation_code);
        mCopy = (Button) mRootView.findViewById(R.id.bt_copy_invitation_code);
        mBack.setOnClickListener(this);
        mQRCode.setOnClickListener(this);
        mCopy.setOnClickListener(this);
        vSms = mRootView.findViewById(R.id.pf_invite_via_sms);
        vSms.setOnClickListener(this);
        vWechat = mRootView.findViewById(R.id.pf_invite_wechat_friend);
        vWechat.setOnClickListener(this);

        if (App.getAppContext().getPackageName().contains("com.cliq123")) {
            vWechat.setVisibility(View.GONE);
        }

        // Bundle bundle = getActivity().getIntent().getExtras();
        // LogUtil.d(TAG, "bundle is " + bundle);
        // if (bundle.containsKey(Consts.ALBUM_ID)) {
        // String albumId = bundle.getString(Consts.ALBUM_ID);
        // LogUtil.d(TAG, "albumId is " + albumId);
        // if (!Utils.isEmpty(albumId)) {
        // DBHelper dbHelper = App.getDBHelper();
        // if (dbHelper != null) {
        // albumEntity = dbHelper.getAlbum(albumId);
        // }
        // }
        // }

        // if (albumEntity != null) {
        // mInvitationCode.setText(albumEntity.getInviteCode());
        // }

    }

    private void intitData() {
        if (albumEntity != null) {
            inviteCode = albumEntity.getInviteCode();
            albumName = albumEntity.getName();
            albumLink = albumEntity.getLink();
            albumCoverId = albumEntity.getTrueCover();
        }

        if (inviteCode != null) {
            mInvitationCode.setText(inviteCode);
        }
        if (albumCoverId != null) {
            imageSize = new ImageSize(80, 80);
            imageSize.setThumb(true);
            albumCover = new ImageView(getActivity());
            RelativeLayout.LayoutParams rlp = new RelativeLayout.LayoutParams(imageSize.getWidth(), imageSize.getHeight());
            albumCover.setLayoutParams(rlp);
            if (bitmap == null && !Utils.isEmpty(albumCoverId)) {
                bitmap = ImageManager.instance().getBitmap(albumCoverId, imageSize, albumCover);
                if (bitmap == null) {
                    loadImage();
                }
            }
        }

        try {
            if (bitmap == null) {
                defaultBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher);
                bitmap = defaultBitmap;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (v.equals(mBack)) {
            finish();
        } else if (v.equals(mQRCode)) {
            showBarcode();
        } else if (v.equals(mCopy)) {
            copyToClip();
        } else if (id == R.id.pf_invite_wechat_friend) {
            inviteViaWechat();
        } else if (id == R.id.pf_invite_via_sms) {
            sendSMS();
        }
    }

    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    private void copyToClip() {

        if (inviteCode == null) {
            return;
        }
        try {
            Utils.copyToClipboard(getActivity(), inviteCode);
            CToast.showToast(R.string.copy_invitation_code_success);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendSMS() {

        if (albumName == null || albumLink == null) {
            return;
        }
        if (albumUrl == null) {
            albumUrl = Consts.URL_ENTITY_VIEWER + albumLink + "&target=invite";
        }
        // String msg = getString(R.string.invite_view_sms_x, albumUrl);
        String msg = getString(R.string.intive_friend, albumName, albumUrl);
        Utils.sendViaSMS(getActivity(), msg);
        UMutils.instance().diyEvent(ID.EventInviteMemberViaSMSSuccess);
//        UMutils.instance().diyEvent(ID.EventShareAlbumViaSms);
    }

    private void showBarcode() {

        if (inviteCode == null) {
            return;
        }

        AlertDialog.Builder qrDialog = new AlertDialog.Builder(getActivity());
        ImageView iv = new ImageView(this.getActivity());
        qrUrl = Consts.URL_ENTITY_VIEWER + albumLink + "&target=qrcode";
        barCodeBmp = Generator.encode(qrUrl, (int) (App.SCREEN_WIDTH * 0.8f));
        if (barCodeBmp != null) {
            iv.setImageBitmap(barCodeBmp);
        }
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == DialogInterface.BUTTON_POSITIVE) {
                    saveBarcode();
                }
                dialog.cancel();
            }
        };
        qrDialog.setTitle(R.string.share_qr_code);
        qrDialog.setMessage(R.string.share_qr_code_description);
        qrDialog.setView(iv);
        qrDialog.setPositiveButton(R.string.save_qr_code, listener);
        qrDialog.show();
    }

    private void inviteViaWechat() {
        if (!Utils.isAppInstalled(this.getActivity(), "com.tencent.mm")) {
            CToast.showToast(R.string.not_install_wx);
        }
        if (albumName == null || albumLink == null) {
            return;
        }
        if (wxHelper == null) {
            wxHelper = new WXHelper();
        }
        if (albumUrl == null) {
            albumUrl = Consts.URL_ENTITY_VIEWER + albumLink + "&target=inviteviawechat";
        }
        Log.d(TAG, "albumEntity is albumUrl=" + albumUrl);
        // Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
        // R.drawable.ic_launcher);

        // String description = getString(R.string.intive_via_wechat_x,
        // albumEntity.getName());

        if (!Utils.isEmpty(albumCoverId) && (bitmap == null || bitmap == defaultBitmap)) {
            CToast.showToast(R.string.load_album_cover);

        } else {

            String description = getString(R.string.intive_friend, albumName, albumUrl);
            wxHelper.sendWebPage(albumUrl, bitmap, getString(R.string.app_name), description, false);
            UMutils.instance().diyEvent(ID.EventInviteMemberViaWechatSuccess);
        }
    }

    protected void loadImage() {
        ImageManager.instance().load(albumCover, albumCoverId, imageSize, null, loadingListener, null);
    }

    private void saveBarcode() {

        if (inviteCode == null) {
            return;
        }

//        TaskRuntime.instance().run(new Task() {
//
//            @Override
//            public void run() {
//                Bitmap codeBmp;
//                // Bitmap barCodeBmp = Generator.encode(inviteCode, (int)
//                // (App.SCREEN_WIDTH * 0.8f), Color.BLACK, Color.WHITE);
//                if (AppData.getAlbumQRSwitch().equals("1")) {
//                    codeBmp = Generator.encode(qrUrl, (int) (App.SCREEN_WIDTH * 0.8f), Color.BLACK, Color.WHITE);
//                } else {
//                    codeBmp = Generator.encode(inviteCode, (int) (App.SCREEN_WIDTH * 0.8f), Color.BLACK, Color.WHITE);
//                }
//                final String path = MediaUtil.getDestSaveDir() + File.separator + inviteCode + ".jpg";
//                if (FileUtil.exists(path)) {
//                    FileUtil.delete(path);
//                }
//                if (codeBmp != null) {
//                    BitmapUtil.saveBitmap(codeBmp, path, 100);
//                }
//                Utils.scanNewMedia(path);
//                runOnUI(new Runnable() {
//
//                    @Override
//                    public void run() {
//                        if (Utils.isEmpty(path)) {
//                            CToast.showToast(R.string.save_failed);
//                        } else {
//                            CToast.showToast(getString(R.string.save_to_x_succeed, path));
//                        }
//                    }
//                });
//            }
//        });
    }

}
