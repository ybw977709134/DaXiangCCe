package com.daxiangce123.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Log;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.ui.view.CToast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import me.yourbay.barcoder.Generator;

public class ShareImageCreateUtil {
    private static final String TAG = "ShareImageCreateUtil";
    public final static int QR_BRODER = Utils.getShareImageCreateUtilSize(12);
    public final static int LOGO_SIZE = Utils.getShareImageCreateUtilSize(30);
    public final static int MIN_CANVAS_WIDTH = Utils.getShareImageCreateUtilSize(530);
    public final static int SMALL_TEXT = Utils.getShareImageCreateUtilSize(20);
    public final static int NORMAL_TEXT = Utils.getShareImageCreateUtilSize(30);

    public final static int AVATAR_LENGTH = Utils.getShareImageCreateUtilSize(65);
//    private static

    private String creatorName;
    private Bitmap avatar;
    public static Bitmap targetBitmap;
    private AlbumEntity albumEntity;
    private Canvas canvas;
    private Paint painPaint, textPaint;
    private Context context;
    private String filePath;
    private int canvasWidth = 0;
    private int createrNameLength = 0;
    String create;
    String by;


    public void setAlbumEntity(AlbumEntity albumEntity) {
        this.albumEntity = albumEntity;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public Context getContext() {
        return context;
    }

    public void setCreatorName(String creatorName) {
        if (creatorName != null) {
            this.creatorName = "\"" + creatorName + "\"";
        } else {
            this.creatorName = "unKnown";
        }
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public boolean createShareImage() {
        if (existed()) {
//            CToast.showToast(context.getResources().getString(R.string.save_to_x_succeed, filePath));
            CToast.showToast(R.string.done_download_to_phone);
            return true;
        }
        try {
            initPaint();
            calculateSize();
            targetBitmap = Bitmap.createBitmap(canvasWidth, Utils.getShareImageCreateUtilSize(675), Bitmap.Config.RGB_565);
            canvas = new Canvas(targetBitmap);
            canvas.drawColor(context.getResources().getColor(R.color.blue_pressed));
            drawText(albumEntity.getName(), Utils.getShareImageCreateUtilSize(83), Utils.getShareImageCreateUtilSize(36));
            drawText(context.getString(R.string.daxiangce_niubi), Utils.getShareImageCreateUtilSize(583) + Utils.getShareImageCreateUtilSize(26), Utils.getShareImageCreateUtilSize(26));
            drawText(context.getString(R.string.url), Utils.getShareImageCreateUtilSize(620) + Utils.getShareImageCreateUtilSize(18), Utils.getShareImageCreateUtilSize(18));
            drawLine();
            drawQrCode();
            drawInviteCode();
            drawOwnerName();
        } catch (Exception e) {
            e.printStackTrace();
        }
        outputToFile();
        return false;
    }

    private void drawInviteCode() {
        String code = albumEntity.getInviteCode();
        String inviteCodeis = context.getResources().getString(R.string.invite_code);
        textPaint.setTextSize(SMALL_TEXT);
        int left = (int) textPaint.measureText(inviteCodeis);
        textPaint.setTextSize(NORMAL_TEXT);
        int right = (int) textPaint.measureText(code);
        int start = canvas.getWidth() / 2 - (right + left) / 2;
        int leftCenter = start + (left / 2);
        int rightCenter = start + left + (right / 2);
        canvas.drawText(code, rightCenter, Utils.getShareImageCreateUtilSize(130), textPaint);
        textPaint.setTextSize(SMALL_TEXT);
        canvas.drawText(inviteCodeis, leftCenter, Utils.getShareImageCreateUtilSize(130), textPaint);
    }

    private void calculateSize() {
        create = context.getResources().getString(R.string.create);
        by = context.getResources().getString(R.string.by);
        int maxWidth = 0;
        textPaint.setTextSize(Utils.getShareImageCreateUtilSize(36));
        int albumLength = (int) (textPaint.measureText(albumEntity.getName()) * 1.1f);
        textPaint.setTextSize(NORMAL_TEXT);
        int creatorLength = (int) textPaint.measureText(creatorName);
        textPaint.setTextSize(SMALL_TEXT);
        creatorLength += (int) textPaint.measureText(create + by);
        creatorLength += AVATAR_LENGTH;
        creatorLength += 10;
        createrNameLength = creatorLength;
        creatorLength += +textPaint.measureText("text");
        maxWidth = Math.max(albumLength, creatorLength);
        if (maxWidth < MIN_CANVAS_WIDTH) {
            canvasWidth = MIN_CANVAS_WIDTH;
        } else {
            canvasWidth = maxWidth;
        }
        if (App.DEBUG) {
            Log.v(TAG, "creatorLength=" + creatorLength + " al=" + albumLength);
        }
    }

    private void drawLine() {
        float start = canvas.getWidth() / 2f * 0.3f;
        float end = canvas.getWidth() / 2f * 1.7f;
        painPaint.setStrokeWidth(Utils.getShareImageCreateUtilSize(3));
        canvas.drawLine(start, Utils.getShareImageCreateUtilSize(240), end, Utils.getShareImageCreateUtilSize(240), painPaint);
    }

    private void drawQrCode() {
        Bitmap qrCode = null;
        String url = Consts.URL_ENTITY_VIEWER + albumEntity.getLink() + "&target=qrcode";
        qrCode = Generator.encode(url, Utils.getShareImageCreateUtilSize(348), 0);
        RectF outter = new RectF();
        outter.left = (canvas.getWidth() - qrCode.getWidth()) / 2 - QR_BRODER;
        outter.right = outter.left + QR_BRODER * 2 + qrCode.getWidth();
        outter.top = Utils.getShareImageCreateUtilSize(294) - QR_BRODER;
        outter.bottom = outter.top + QR_BRODER * 2 + qrCode.getHeight();

        canvas.drawRoundRect(outter, QR_BRODER / 2, QR_BRODER / 2, painPaint);
        canvas.drawBitmap(qrCode, (canvas.getWidth() - qrCode.getWidth()) / 2, Utils.getShareImageCreateUtilSize(294), painPaint);
        qrCode.recycle();
        Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_launcher);
        float centerX = outter.centerX();
        float centerY = outter.centerY();
        outter.top = centerY - LOGO_SIZE;
        outter.bottom = centerY + LOGO_SIZE;
        outter.left = centerX - LOGO_SIZE;
        outter.right = centerX + LOGO_SIZE;
        Rect src = new Rect();
        src.top = src.left = 0;
        src.bottom = logo.getHeight();
        src.right = logo.getWidth();
        canvas.drawBitmap(logo, src, outter, painPaint);
    }

    private void drawOwnerName() {
        drawOwnerAvatar();
    }

    public static Bitmap toRoundBitmap(Bitmap bitmap, int color, int borderDips, int desireWidth) {
        if (bitmap == null) {
            return null;
        }
        Bitmap bitmap0 = BitmapUtil.squareBitmap(bitmap);
        Bitmap bitmap1 = BitmapUtil.resizeBitmap(bitmap0, desireWidth, desireWidth, true);
        int maxBorder = (desireWidth / 2) / 5;
        maxBorder = maxBorder > Utils.getShareImageCreateUtilSize(15) ? Utils.getShareImageCreateUtilSize(15) : maxBorder;
        final int borderSizePx = borderDips > maxBorder ? maxBorder : borderDips;

        final int size = desireWidth;
        int center = (size / 2);
        int left = ((desireWidth - size) / 2);
        int top = ((desireWidth - size) / 2);
        int right = left + size;
        int bottom = top + size;

        Bitmap output = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        final Paint paint = new Paint();

        final Rect src = new Rect(left, top, right, bottom);
        final Rect dst = new Rect(0, 0, size, size);

        canvas.drawARGB(0, 0, 0, 0);
        // draw border
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        int radius = center - borderSizePx;
        canvas.drawCircle(center, center, radius, paint);
        // draw bitmap
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap1, src, dst, paint);
        paint.setXfermode(null);

        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth((float) borderSizePx);
        radius = center - borderSizePx / 2;
        canvas.drawCircle(center, center, radius, paint);
        canvas.save();
        BitmapUtil.recycle(bitmap1);
        return output;
    }

    private void drawOwnerAvatar() {
        avatar = toRoundBitmap(avatar, Color.WHITE, Utils.getShareImageCreateUtilSize(3), Utils.getShareImageCreateUtilSize(64));
        int startx = (canvas.getWidth() - createrNameLength) / 2;
        int startY = Utils.getShareImageCreateUtilSize(158);
        RectF rectF = new RectF();
        rectF.top = startY;
        rectF.bottom = rectF.top + avatar.getHeight();
        rectF.left = startx;
        rectF.right = startx + avatar.getWidth();
        canvas.drawBitmap(avatar, null, rectF, painPaint);
        int baseline = (int) (rectF.centerY() + SMALL_TEXT / 2);
        //draw 由
        textPaint.setTextSize(SMALL_TEXT);
        canvas.drawText(by, (int) rectF.right + Utils.getShareImageCreateUtilSize(20), baseline, textPaint);
        //draw 创建者名称
        int right2 = (int) (rectF.right + Utils.getShareImageCreateUtilSize(10) + textPaint.measureText(by));
        textPaint.setTextSize(NORMAL_TEXT);
        right2 += textPaint.measureText(creatorName) / 2;
        canvas.drawText(creatorName, right2, baseline, textPaint);
        //draw 创建
        right2 += textPaint.measureText(creatorName) / 2;
        textPaint.setTextSize(SMALL_TEXT);
        right2 += textPaint.measureText(create) / 2;
        canvas.drawText(create, right2, baseline, textPaint);

    }

    private void drawText(String content, int height, int textSize) {
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTextSize(textSize);
        canvas.drawText(content, canvas.getWidth() / 2, height, textPaint);
    }

    private void initPaint() {
        painPaint = new Paint();
        painPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        painPaint.setColor(Color.WHITE);
        textPaint = new Paint();
        textPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
    }

    private boolean existed() {
        filePath = MediaUtil.getDestSaveDir() + File.separator + albumEntity.getId() + ".png";
        return ((new File(filePath)).exists());
    }

    private void outputToFile() {
        File output = new File(filePath);
        if (targetBitmap != null && (!targetBitmap.isRecycled())) {
            try {
                FileOutputStream fos = new FileOutputStream(output);
                int qulaiaty = Utils.getShareImageCreateUtilSize(100);
                targetBitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
                fos.flush();
                fos.close();
                Utils.scanNewMedia(filePath);
            } catch (IOException e) {
                e.printStackTrace();
                if (App.DEBUG) {
                    LogUtil.e(TAG, "failed to write image content");
                }
                CToast.showToast(context.getResources().getString(R.string.save_failed));
                return;
            }
//            CToast.showToast(context.getResources().getString(R.string.save_to_x_succeed, filePath));
            CToast.showToast(R.string.done_download_to_phone);
        }

    }


}
