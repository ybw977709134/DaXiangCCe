package me.yourbay.barcoder;

import android.graphics.Bitmap;
import android.util.Log;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;

public class Generator {

    public static final String TAG = "Generator";

    /**
     * @param content
     * @param width   this is original matrix width !! output may smaller than that!!
     * @return
     */
    public static Bitmap encode(String content, int width) {
        try {
            final int FOREGROUND = 0xFF000000;
            final int BACKGROUND = 0x00000000;
            return encodeAsBitmap(content, BarcodeFormat.QR_CODE, width, width,
                    FOREGROUND, BACKGROUND, 0.05f);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param content
     * @param width   this is original matrix width !! output may smaller than that!!
     * @return
     */
    public static Bitmap encode(String content, int width, float percentage) {
        try {
            final int FOREGROUND = 0xFF000000;
            final int BACKGROUND = 0x00000000;
            return encodeAsBitmap(content, BarcodeFormat.QR_CODE, width, width,
                    FOREGROUND, BACKGROUND, percentage);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Bitmap encode(String content, int width, int foreground,
                                int background, float percentage) {
        try {
            return encodeAsBitmap(content, BarcodeFormat.QR_CODE, width, width,
                    foreground, background, percentage);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        return null;
    }

    // private static Bitmap encodeAsBitmap(String contents, BarcodeFormat
    // format,
    // int desiredWidth, int desiredHeight) throws WriterException {
    // final int BACKGROUND = 0x00000000;
    // final int FOREGROUND = 0xFF000000;
    // return encodeAsBitmap(contents, format, desiredWidth, desiredHeight,
    // BACKGROUND, FOREGROUND);
    // }

    private static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
                                         int desiredWidth, int desiredHeight, int foreground, int background, float percentage)
            throws WriterException {

        if (contents == null || contents.length() == 0) {
            return null;
        }

        HashMap<EncodeHintType, Object> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new HashMap<EncodeHintType, Object>();
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
            hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = deleteWhite(writer.encode(contents, format, desiredWidth,
                desiredHeight, hints), percentage);
        int width = result.getWidth();
        int height = result.getHeight();
        Log.d("zxing", " encode width =" + width + " desiredWidth="
                + desiredWidth);
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? foreground : background;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

    public static BitMatrix deleteWhite(BitMatrix matrix, float percentage) {
        BitMatrix resMatrix = matrix;
        try {
            int[] rec = matrix.getEnclosingRectangle();
            int offset = (int) (rec[2] * percentage);
            if (offset % 2 == 1) {
                offset--;
            }
            int resWidth = rec[2] + offset;
            int resHeight = rec[3] + offset;
            resMatrix = new BitMatrix(resWidth, resHeight);
            resMatrix.clear();
            for (int i = 0; i < resWidth + offset; i++) {
                for (int j = 0; j < resHeight + offset; j++) {
                    if (matrix.get(i + rec[0], j + rec[1]))
                        resMatrix.set(i + offset / 2, j + offset / 2);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            resMatrix = matrix;
        }

        return resMatrix;
    }

    private static String guessAppropriateEncoding(CharSequence contents) {
        for (int i = 0; i < contents.length(); i++) {
            if (contents.charAt(i) > 0xFF) {
                return "UTF-8";
            }
        }
        return "gbk";
    }

    public static boolean save(Bitmap bitmap, String destPath, int quality,
                               Bitmap.CompressFormat format) {
        if (bitmap == null) {
            return false;
        }
        if (format == null) {
            return false;
        }
        if (quality > 100) {
            quality = 100;
        } else if (quality <= 0) {
            quality = 100;
        }
        File file = new File(destPath);
        try {
            return bitmap.compress(format, quality, new FileOutputStream(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static Bitmap encodeAsBitmap(String contents, BarcodeFormat format,
                                         int desiredWidth, int desiredHeight, int foreground, int background, Bitmap centerBitmpa)
            throws WriterException {
        if (contents == null || contents.length() == 0) {
            return null;
        }
        HashMap<EncodeHintType, String> hints = null;
        String encoding = guessAppropriateEncoding(contents);
        if (encoding != null) {
            hints = new HashMap<EncodeHintType, String>(2);
            hints.put(EncodeHintType.CHARACTER_SET, encoding);
        }
        MultiFormatWriter writer = new MultiFormatWriter();
        BitMatrix result = writer.encode(contents, format, desiredWidth,
                desiredHeight, hints);
        int width = result.getWidth();
        int height = result.getHeight();
        Log.d("zxing", " encode width =" + width + " desiredWidth="
                + desiredWidth);
        int[] pixels = new int[width * height];
        // All are 0, or black, by default
        for (int y = 0; y < height; y++) {
            int offset = y * width;
            for (int x = 0; x < width; x++) {
                pixels[offset + x] = result.get(x, y) ? foreground : background;
            }
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
