/*
 * Copyright (C) 2010 ZXing authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.yourbay.barcoder.barcode;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import java.util.Map;

import me.yourbay.barcoder.CapActivity;

final class DecodeHandler extends Handler {

    // private static final String TAG = DecodeHandler.class.getSimpleName();

    private final CapActivity activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(CapActivity activity, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }
        switch (message.what) {
            case Hyb.MESSAGE_ID_DECODE:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case Hyb.MESSAGE_ID_QUIT:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it
     * took. For efficiency, reuse the same reader objects from one decode to
     * the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        /**
         * TO PORTRAIT START
         */
        byte[] rotatedData = new byte[data.length];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++)
                rotatedData[x * height + height - y - 1] = data[x + y * width];
        }
        int tmp = width; // Here we are swapping, that's the difference to #11
        width = height;
        height = tmp;
        PlanarYUVLuminanceSource source = activity.getCameraManager()
                .buildLuminanceSource(rotatedData, width, height);
        /**
         * TO PORTRAIT END
         */
        // .buildLuminanceSource(data, width, height);
        Result rawResult = null;
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
                // continue
            } finally {
                multiFormatReader.reset();
            }
        }

        Handler handler = activity.getHandler();
        if (rawResult != null) {
            if (handler != null) {
                Message message = Message.obtain(handler,
                        Hyb.MESSAGE_ID_DECODE_SUCCEEDED, rawResult);
                Bundle bundle = new Bundle();
                Bitmap grayscaleBitmap = toBitmap(source,
                        source.renderThumbnail());
                bundle.putParcelable(DecodeThread.BARCODE_BITMAP,
                        grayscaleBitmap);
                message.setData(bundle);
                message.sendToTarget();
            }
        } else {
            if (handler != null) {
                Message message = Message.obtain(handler,
                        Hyb.MESSAGE_ID_DECODE_FAILED);
                message.sendToTarget();
            }
        }
    }

    private static Bitmap toBitmap(PlanarYUVLuminanceSource source, int[] pixels) {
        int width = source.getThumbnailWidth();
        int height = source.getThumbnailHeight();
        Bitmap bitmap = Bitmap.createBitmap(width, height,
                Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }

}
