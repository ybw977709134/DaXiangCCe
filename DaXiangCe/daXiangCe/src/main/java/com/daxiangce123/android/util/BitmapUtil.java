package com.daxiangce123.android.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.provider.MediaStore.Images;
import android.view.Gravity;

import com.daxiangce123.android.App;
import com.daxiangce123.android.data.ImageSize;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BitmapUtil {
    public static final String TAG = "BitmapUtil";
    private static boolean DEBUG = false;

    static {
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    public final static Bitmap zoom(Bitmap sourceBitmap, int destWidth, int destHeight) {
        if (sourceBitmap == null) {
            return null;
        }
        if (destHeight <= 0 || destWidth <= 0) {
            if (DEBUG) {
                LogUtil.d(TAG, "invalid paraments destWidth = " + destWidth + ";  destHeight=" + destHeight);
            }
            return sourceBitmap;
        }
        final float srcWidth = sourceBitmap.getWidth();
        final float srcHeight = sourceBitmap.getHeight();
        final float widthRatio = destWidth / srcWidth;
        final float heightRatio = destHeight / srcHeight;
        if (DEBUG) {
            LogUtil.d(TAG, "zoom() dest:" + destWidth + "x" + destHeight + " src:" + sourceBitmap.getWidth() + "x" + sourceBitmap.getHeight() + "|" + widthRatio + "x" + heightRatio);
        }
        if (widthRatio >= 1 || heightRatio >= 1) {
            return sourceBitmap;
        }
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.postScale(widthRatio, heightRatio);
        Bitmap resizedBitmap = Bitmap.createBitmap(sourceBitmap, 0, 0, (int) srcWidth, (int) srcHeight, matrix, true);
        recycle(sourceBitmap);
        return resizedBitmap;
    }

    private static int sampleSize(int src, int target) {
        int result = 1;// src > target ? 2 : 1
        while (src > target * (result + 1)) {
            ++result;
        }
        return result;
    }

    public static ImageSize getImageSize(String absPath) {
        Options options = getOptions(absPath);
        int width = options.outWidth;
        int height = options.outHeight;
        ImageSize size = new ImageSize(width, height);
        return size;
    }

    public static Options getOptions(String absPath) {
        Options options = new Options();
        options.inPreferredConfig = Config.RGB_565;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(absPath, options);
        return options;
    }

    public static Options getOptions(String absPath, int desireWidth, int desireHeight) {
        Options options = getOptions(absPath);
        if (options == null) {
            return null;
        }
        if (desireHeight <= 0 && desireWidth <= 0) {
            desireHeight = options.outHeight / 2;
            desireWidth = options.outWidth / 2;
        } else if (desireHeight <= 0) {
            desireHeight = (options.outHeight * desireWidth / options.outWidth);
        } else if (desireWidth <= 0) {
            desireWidth = (options.outWidth * desireHeight / options.outHeight);
        }
        int scaleW = sampleSize(options.outWidth, desireWidth);
        int scaleH = sampleSize(options.outHeight, desireHeight);
        // int scaleW = (int) Math.ceil(options.outWidth / desireWidth) + 1;
        // int scaleH = (int) Math.ceil(options.outWidth / desireHeight) + 1;
        // options.inSampleSize = scaleH >= scaleW ? scaleH : scaleW;
        options.inSampleSize = Math.max(scaleW, scaleH);
        options.inJustDecodeBounds = false;
        return options;
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int rotation) {
        if (rotation <= 0) {
            return bitmap;
        }
        Matrix matrix = new Matrix();
        matrix.setRotate(rotation);
        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotated;
    }

    public static Bitmap getBitmap(String absPath, int width, int height) {
        if (width <= 0 && height <= 0 || !FileUtil.exists(absPath)) {
            if (DEBUG) {
                LogUtil.e(TAG, "invalid parameters absPath " + absPath + " width " + width + " height " + height);
            }
            return null;
        }

        if (DEBUG) {
            LogUtil.d(TAG, "decode bitmap " + absPath + " width " + width + " height " + height);
        }
        Bitmap bitmap = null;
        Options options = getOptions(absPath, width, height);
        if (DEBUG) {
            LogUtil.d(TAG, "decode bitmap inSampleSize=" + options.inSampleSize + " " + options.outWidth + "x" + options.outHeight);
        }
        bitmap = BitmapFactory.decodeFile(absPath, options);
        if (bitmap == null) {
            return null;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "decode bitmap size=" + bitmap.getWidth() + "x" + bitmap.getHeight());
        }

        float ratioW = ((float) width) / bitmap.getWidth();
        float ratioH = ((float) height) / bitmap.getHeight();
        float ratio = Math.min(ratioW, ratioH);
        if (ratio > 1 || ratio <= 0) {
            ratio = 1;
        }

        // width = (int) (bitmap.getWidth() * ratio);
        // height = (int) (bitmap.getHeight() * ratio);

        if (DEBUG) {
            LogUtil.d(TAG, "decode bitmap final=" + width + "x" + height + " ratio=" + ratio);
        }

        Matrix matrix = new Matrix();
        matrix.postScale(ratio, ratio);

        int rotate = getRotate(absPath);
        if (rotate > 0) {
            matrix.setRotate(rotate);
        }

        Bitmap rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

        if (rotated == null) {
            return bitmap;
        }
        if (DEBUG) {
            LogUtil.d(TAG, "decode bitmap final=" + rotated.getWidth() + "x" + rotated.getHeight());
        }
        return rotated;
    }

    public static boolean saveBitmap(Bitmap bitmap, String absPath, int quality) {
        File file = FileUtil.create(absPath);
        if (!FileUtil.exists(file)) {
            if (DEBUG) {
                LogUtil.w(TAG, "create file failed.");
            }
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            if (DEBUG) {
                LogUtil.e(TAG, "failed to write image content");
            }
            return false;
        }

        return true;
    }

    public static Bitmap squareBitmap(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();
        if (width == height) {
            return source;
        }
        int x = (height < width) ? ((width - height) / 2) : 0;
        int y = (width < height) ? ((height - width) / 2) : 0;
        int pixels = width < height ? width : height;
        Bitmap square = Bitmap.createBitmap(source, x, y, pixels, pixels);
        return square;
    }

    public static int getRotate(String absPath) {
        if (Utils.isEmpty(absPath) || !FileUtil.exists(absPath)) {
            return 0;
        }

        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(absPath);
        } catch (IOException e) {
            e.printStackTrace();
            return 0;
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);
        int rotate = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                rotate = 270;
                break;
            default:
                break;
        }
        if (DEBUG) {
            LogUtil.v(TAG, "image rotate " + rotate);
        }
        return rotate;
    }

    public static Bitmap resizeBitmap(Bitmap bitmap, int desireWidth, int desireHeight, boolean isEnlarge) {
        if (bitmap == null || desireHeight <= 0 || desireWidth <= 0) {
            return null;
        }

        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();

        if (width <= 0 || height <= 0) {
            return bitmap;
        }
        if (!isEnlarge && (width < desireWidth && height < desireHeight)) {
            return bitmap;
        }
        float scale;
        if (width < height) {
            scale = (float) desireHeight / (float) height;
            if (desireWidth < width * scale) {
                scale = (float) desireWidth / (float) width;
            }
        } else {
            scale = (float) desireWidth / (float) width;
        }

        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        return bitmap;
    }

    public static int getSize(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    public final static Bitmap getImageThumbnail(String imagePath, int width, int height) {
        BitmapFactory.Options options = getOptions(imagePath, width, height);
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, options);
        if (options != null) {
            if (height <= 0) {
                height = (options.outHeight * width / options.outWidth);
            } else if (width <= 0) {
                width = (options.outWidth * height / options.outHeight);
            }
        }
        Bitmap result = ThumbnailUtils.extractThumbnail(bitmap, width, height);
        int rotation = getRotate(imagePath);
        if (rotation > 0) {
            return rotateBitmap(bitmap, rotation);
        }
        // ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        return result;
    }

    public final static Bitmap getThumbnail(Bitmap bitmap, int desireWidth, int desireHeight) {
        if (bitmap == null || desireWidth <= 0 || desireHeight <= 0) {
            return bitmap;
        }
        return ThumbnailUtils.extractThumbnail(bitmap, desireWidth, desireHeight);
    }

    public final static Bitmap getVideoThumb(String absPath, int width, int height) {
        if (App.DEBUG) {
            LogUtil.v(TAG, "width:" + width);
        }
        Bitmap bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(absPath, Images.Thumbnails.MINI_KIND);
        bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height);
        // ThumbnailUtils.OPTIONS_RECYCLE_INPUT
        bitmap = waterMark(bitmap, App.getAppContext(), Gravity.CENTER);
        return bitmap;
    }

    public final static Bitmap waterMark(Bitmap bitmap, Context context, int gravity) {
        // if (bitmap == null) {
        // return null;
        // }
        // Drawable waterPrintDrawable =
        // context.getResources().getDrawable(R.drawable.video_overlay);
        // int waterPrintWidth = waterPrintDrawable.getIntrinsicWidth();
        // int waterPrintHeight = waterPrintDrawable.getIntrinsicHeight();
        // int w = bitmap.getWidth();
        // int h = bitmap.getHeight();
        // if (gravity == Gravity.CENTER) {
        // waterPrintDrawable.setBounds((w - waterPrintWidth) / 2, (h -
        // waterPrintHeight) / 2, (w + waterPrintWidth) / 2, (h +
        // waterPrintHeight) / 2);
        // } else {
        // waterPrintDrawable.setBounds((w - waterPrintWidth) / 2, h, (w +
        // waterPrintWidth) / 2, h + waterPrintHeight);
        // }
        // Canvas mCanvas = new Canvas(bitmap);
        // waterPrintDrawable.draw(mCanvas);
        return bitmap;
    }

    public final static Bitmap toRoundBitmap(Bitmap bitmap, int color, int borderDips, int desireWidth) {
        if (bitmap == null) {
            return null;
        }
        Bitmap bitmap0 = squareBitmap(bitmap);
        Bitmap bitmap1 = resizeBitmap(bitmap0, desireWidth, desireWidth, true);
        int toPX = Utils.dp2px(App.getAppContext(), borderDips);
        int maxBorder = (desireWidth / 2) / 5;
        maxBorder = maxBorder > 15 ? 15 : maxBorder;
        final int borderSizePx = toPX > maxBorder ? maxBorder : toPX;

        final int size = desireWidth;
        int center = (int) (size / 2);
        int left = (int) ((desireWidth - size) / 2);
        int top = (int) ((desireWidth - size) / 2);
        int right = left + size;
        int bottom = top + size;

        Bitmap output = Bitmap.createBitmap(size, size, Config.ARGB_8888);
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
        recycle(bitmap0, bitmap1, bitmap);
        return output;
    }

    public final static Bitmap toRoundBitmap(Bitmap bitmap, int desireWidth) {
        // try {
        // int width = bitmap.getWidth();
        // int height = bitmap.getHeight();
        // float roundPx;
        // float left, top, right, bottom, dst_left, dst_top, dst_right,
        // dst_bottom;
        // if (width <= height) {
        // roundPx = width / 2;
        // top = 0;
        // bottom = width;
        // left = 0;
        // right = width;
        // height = width;
        // dst_left = 0;
        // dst_top = 0;
        // dst_right = width;
        // dst_bottom = width;
        // } else {
        // roundPx = height / 2;
        // float clip = (width - height) / 2;
        // left = clip;
        // right = width - clip;
        // top = 0;
        // bottom = height;
        // width = height;
        // dst_left = 0;
        // dst_top = 0;
        // dst_right = height;
        // dst_bottom = height;
        // }
        // Bitmap output = Bitmap
        // .createBitmap(width, height, Config.ARGB_8888);
        // Canvas canvas = new Canvas(output);
        // final int color = 0xff424242;
        // final Paint paint = new Paint();
        // final Rect src = new Rect((int) left, (int) top, (int) right,
        // (int) bottom);
        // final Rect dst = new Rect((int) dst_left, (int) dst_top,
        // (int) dst_right, (int) dst_bottom);
        // final RectF rectF = new RectF(dst);
        // paint.setAntiAlias(true);
        // canvas.drawARGB(0, 0, 0, 0);
        // paint.setColor(color);
        // canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
        // paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        // canvas.drawBitmap(bitmap, src, dst, paint);
        // return output;
        // } catch (Exception e) {
        //
        // }
        return toRoundBitmap(bitmap, 0xFFFFFFFF, 3, desireWidth);
    }

    public static Bitmap toRoundCorner(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();

        final int delta = Utils.dp2px(App.getAppContext(), 3);
        final float roundPx = Utils.dp2px(App.getAppContext(), 8);
        final Paint paint = new Paint();
        paint.setAntiAlias(true);

		/* draw round forground */
        int foreW = w - 2 * delta;
        int foreH = h - 2 * delta;
        Bitmap foreBmp = Bitmap.createBitmap(foreW, foreH, Config.ARGB_8888);
        Rect rect = new Rect(0, 0, foreW, foreH);
        RectF rectF = new RectF(rect);
        Canvas canvas0 = new Canvas(foreBmp);
        canvas0.drawRoundRect(rectF, roundPx, roundPx, paint);
        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas0.drawBitmap(bitmap, null, rect, paint);

		/* draw round backgroud */
        Drawable drawable = DrawableUtils.getDrawbale(0xffffffff, (int) (delta + roundPx));
        drawable.setBounds(0, 0, w, h);
        Bitmap result = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.draw(canvas);
        canvas.drawBitmap(foreBmp, delta, delta, null);
        recycle(foreBmp, bitmap);

        return result;
    }

    public final static Bitmap drawRing(Bitmap bitmap, int deltaRadius, int color) {
        int w = bitmap.getWidth();
        int dia = deltaRadius * 2 + w;
        float radius = (float) dia / 2;
        Bitmap resultBitmap = Bitmap.createBitmap(dia, dia, Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(color);
        paint.setAlpha(128);
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawCircle(radius, radius, radius, paint);
        canvas.drawBitmap(bitmap, deltaRadius, deltaRadius, null);
        return resultBitmap;
    }

    public final static Bitmap getRound(int argb, int dia) {
        float radius = dia / 2;
        Bitmap resultBitmap = Bitmap.createBitmap(dia, dia, Config.ARGB_8888);
        Paint paint = new Paint();
        paint.setColor(argb);
        paint.setAntiAlias(true);
        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawCircle(radius, radius, radius, paint);
        return resultBitmap;
    }

    public final static Bitmap stretch(Bitmap bitmap, int height) {
        if (bitmap == null) {
            return null;
        }
        if (height <= bitmap.getHeight()) {
            return bitmap;
        }
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int tmpHeight = 1;
        Bitmap resultBitmap = Bitmap.createBitmap(w, height, Config.ARGB_8888);
        Canvas mCanvas = new Canvas(resultBitmap);
        mCanvas.drawBitmap(bitmap, 0, 0, null);
        Bitmap tmp = Bitmap.createBitmap(bitmap, 0, h - tmpHeight, w, tmpHeight);
        bitmap.recycle();
        for (int i = 0; i < (height - h) / tmpHeight; i++) {
            mCanvas.drawBitmap(tmp, 0, h + i * tmpHeight, null);
        }
        tmp.recycle();
        return resultBitmap;
    }

    public static Bitmap rotateOverlay(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        // bitmap = squareBitmap(bitmap);
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        if (w == 0 || h == 0) {
            return null;
        }
        if (w != h) {
            bitmap = squareBitmap(bitmap);
            w = h = bitmap.getWidth();
        }
        float rota = 345;
        double radians = Math.toRadians(rota);
        /* after rotate , the new width is the orginal width */
        int rotateWidth = (int) (w / (Math.abs(Math.sin(radians)) + Math.abs(Math.cos(radians))));
        int delta = (w - rotateWidth) / 2;

        // result
        Bitmap result = Bitmap.createBitmap(w, w, Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // forgroud
        Bitmap foreBmp = resizeBitmap(bitmap, rotateWidth, rotateWidth, true);

        // rotate
        Matrix matrix = new Matrix();
        matrix.postRotate(rota);

        Bitmap rotate = Bitmap.createBitmap(foreBmp, 0, 0, rotateWidth, rotateWidth, matrix, true);
        canvas.drawBitmap(rotate, 0, 0, null);
        canvas.drawBitmap(foreBmp, delta, delta, null);

        recycle(bitmap, rotate, foreBmp);
        return result;
    }

    public static Bitmap drawLayoutDropShadow(Bitmap bitmap) {
        BlurMaskFilter blurFilter = new BlurMaskFilter(2, BlurMaskFilter.Blur.OUTER);
        Paint shadowPaint = new Paint();
        shadowPaint.setMaskFilter(blurFilter);
        int[] offsetXY = {5, 5};
        Bitmap shadowBitmap = bitmap.extractAlpha(shadowPaint, offsetXY);
        Bitmap shadowImage32 = shadowBitmap.copy(Bitmap.Config.ARGB_8888, true);
        Canvas c = new Canvas(shadowImage32);
        c.drawBitmap(bitmap, 0, 0, null);
        return shadowImage32;
    }

    public static Bitmap blur(Bitmap bmp) {
        int iterations = 1;
        int radius = 8;
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] inPixels = new int[width * height];
        int[] outPixels = new int[width * height];
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.getPixels(inPixels, 0, width, 0, 0, width, height);
        for (int i = 0; i < iterations; i++) {
            blur(inPixels, outPixels, width, height, radius);
            blur(outPixels, inPixels, height, width, radius);
        }
        bitmap.setPixels(inPixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public final static boolean compress(String srcPath, String destPath, int maxMinSize, long maxFileSize, boolean override) {
        return compress(srcPath, destPath, maxMinSize, maxFileSize, override, false);
    }

    /**
     * <pre>
     *
     * @param srcPath     path of local file
     * @param destPath    dest path when is compressed
     * @param maxFileSize max size of dest file to compress
     * @param destWidth   max width of the compressed dest file.if is -1, height of dest
     *                    file will be scaled with the ratio of maxHeight and height
     * @param destHeight  max height of the compressed dest file. if is -1, height of
     *                    dest file will be scaled with the ratio of maxWidth and width
     * @param maxMinSize  max size of min from(destWidth, destHeight)
     * @param override    if destPath is already exists file, if override, it will
     *                    override it.if not do nothing
     * @throws it's invalid and do nothing when both maxWidth and maxHeight is -1
     *              </pre>
     * @time Apr 3, 2014
     */
    public final static boolean compress(String srcPath, String destPath, int maxMinSize, long maxFileSize, boolean override, boolean maxSizeIsMax) {
        if (maxMinSize <= 0 || maxFileSize <= 0) {
            return false;
        }
        if (!FileUtil.exists(srcPath)) {
            return false;
        }
        if (FileUtil.exists(destPath)) {
            if (!override) {
                return false;
            } else {
                FileUtil.delete(destPath);
            }
        }
        final long srcFileSize = FileUtil.size(srcPath);

        int rotation = getRotate(srcPath);
        // read image with and height
        Options options = getOptions(srcPath);
        if (rotation == 90 || rotation == 270) {
            int tmp = options.outHeight;
            options.outHeight = options.outWidth;
            options.outWidth = tmp;
        }
        int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;
        // calculate destWidth and destHeight
        // if (destWidth == -1) {
        // destWidth = destHeight >= srcHeight ? srcWidth : (srcWidth
        // * destHeight / srcHeight);
        // } else if (destHeight == -1) {
        // destHeight = destWidth >= srcWidth ? srcHeight : (srcHeight
        // * destWidth / srcWidth);
        // }

        // the min size is maxMin
        float destRatio = 1.0f;
        final float minSize = maxSizeIsMax ? Math.max(srcWidth, srcHeight) : Math.min(srcWidth, srcHeight);
        if (minSize > maxMinSize) {
            destRatio = ((float) maxMinSize) / minSize;
        }
        int destWidth = (int) (srcWidth * destRatio);
        int destHeight = (int) (srcHeight * destRatio);

        // read bitmap from sdcard
        Bitmap bitmap = getBitmap(srcPath, destWidth, destHeight);
        bitmap = zoom(bitmap, destWidth, destHeight);
        if (bitmap == null) {
            return false;
        }

        destWidth = bitmap.getWidth();
        destHeight = bitmap.getHeight();
        File outputFile = new File(destPath);
        FileOutputStream out = null;
        try {
            // calculate quality
            float ratio = ((float) (srcWidth * srcHeight)) / (destWidth * destHeight);
            long losssLessSize = (long) (srcFileSize / ratio);
            int quality = 0;
            if (maxFileSize >= losssLessSize) {
                quality = 100;
            } else {
                quality = (int) Math.floor(100.00 * maxFileSize / losssLessSize);
            }
            if (quality <= 0) {
                quality = 1;
            } else if (quality > 100) {
                quality = 100;
            }

            long start = System.currentTimeMillis();
            long curSize = 0;
            int ceilQuality = quality > 70 ? 100 : quality + 30;
            int floorQuality = quality < 30 ? 0 : quality - 30;
            boolean running = true;
            while (running) {
                if (out != null) {
                    out.close();
                }
                out = new FileOutputStream(outputFile);
                BufferedOutputStream stream = new BufferedOutputStream(out);
                bitmap.compress(CompressFormat.JPEG, quality, stream);
                stream.flush();
                stream.close();
                curSize = FileUtil.size(destPath);
                if (Math.abs(curSize - maxFileSize) <= 5 * 1024) {
                    running = false;
                } else if (Math.abs(ceilQuality - floorQuality) <= 1) {
                    running = false;
                } else {
                    if (curSize < maxFileSize) {
                        floorQuality = quality;
                    } else {
                        ceilQuality = quality;
                    }
                }
                quality = (ceilQuality + floorQuality) / 2;
            }
            long duration = System.currentTimeMillis() - start;
            if (DEBUG) {
                LogUtil.d(TAG, "compress-dest-file-size " + srcFileSize + "|" + maxFileSize + " ratio=" + ratio + " quality=" + quality + " duration=" + duration + " size:" + destWidth + "x"
                        + destHeight + "|" + srcWidth + "x" + srcHeight + " " + srcPath);
                LogUtil.d(TAG, "compress-dest-file-size is " + FileUtil.size(outputFile) + " " + destPath);
            }
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // catch (OutOfMemoryError e) {
        // e.printStackTrace();
        // }

        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;

    }

    private static void blur(int[] in, int[] out, int width, int height, int radius) {
        int widthMinus1 = width - 1;
        int tableSize = 2 * radius + 1;
        int divide[] = new int[256 * tableSize];

        for (int i = 0; i < 256 * tableSize; i++)
            divide[i] = i / tableSize;

        int inIndex = 0;

        for (int y = 0; y < height; y++) {
            int outIndex = y;
            int ta = 0, tr = 0, tg = 0, tb = 0;

            for (int i = -radius; i <= radius; i++) {
                int rgb = in[inIndex + clamp(i, 0, width - 1)];
                ta += (rgb >> 24) & 0xff;
                tr += (rgb >> 16) & 0xff;
                tg += (rgb >> 8) & 0xff;
                tb += rgb & 0xff;
            }

            for (int x = 0; x < width; x++) {
                out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16) | (divide[tg] << 8) | divide[tb];

                int i1 = x + radius + 1;
                if (i1 > widthMinus1) i1 = widthMinus1;
                int i2 = x - radius;
                if (i2 < 0) i2 = 0;
                int rgb1 = in[inIndex + i1];
                int rgb2 = in[inIndex + i2];

                ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
                tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
                tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
                tb += (rgb1 & 0xff) - (rgb2 & 0xff);
                outIndex += height;
            }
            inIndex += width;
        }
    }

    private static int clamp(int x, int a, int b) {
        return (x < a) ? a : (x > b) ? b : x;
    }

    public final static void recycle(Bitmap... bitmaps) {
        if (bitmaps == null || bitmaps.length == 0) {
            return;
        }
        try {
            for (Bitmap bitmap : bitmaps) {
                if (bitmap == null) {
                    continue;
                }
                if (!bitmap.isRecycled()) {
                    try {
                        bitmap.recycle();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                bitmap = null;
            }
        } catch (Exception e) {
        }
    }

    public static Bitmap getNinePatchDrawable(NinePatchDrawable np_drawable, int x, int y) {
        Bitmap output_bitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output_bitmap);
        np_drawable.draw(canvas);
        return output_bitmap;
    }


}
