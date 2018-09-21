package com.daxiangce123.android.ui.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;

import com.daxiangce123.R;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.core.TaskRuntime;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.UMutils;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.util.Hashtable;
import java.util.List;

import me.yourbay.barcoder.CapActivity;
import me.yourbay.barcoder.barcode.ViewfinderView;

/**
 * @author ram
 * @project Cliq
 * @time Mar 6, 2014
 */
public class ZXingActivity extends CapActivity implements View.OnClickListener {

    private final static String TAG = "ZXingActivity";
    private Button button;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void init() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.scanner);
        button = (Button) findViewById(R.id.choose_from_image);
        button.setOnClickListener(this);
    }

    @Override
    public ViewfinderView bindViewfinderView() {
        return (ViewfinderView) findViewById(R.id.viewfinder_view);
    }

    @Override
    public SurfaceView bindSurfaceView() {
        return (SurfaceView) findViewById(R.id.preview_view);
    }

    @Override
    public void handleResult(Result rawResult, Bitmap barcode) {
        if (barcode != null) {
            ((ImageView) findViewById(R.id.barcode_image_view))
                    .setImageBitmap(barcode);
        }
        if (rawResult != null) {
            Intent intent = getIntent();
            intent.putExtra(Consts.ZXING_RESULT, rawResult.getText());
            setResult(RESULT_OK, intent);
            finish();
        } else {
            CToast.showToast(R.string.qrcode_not_found);
        }
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.choose_from_image) {
            UMutils.instance().diyEvent(UMutils.ID.EventReadQrcode);
            Intent intent = new Intent();
            intent.setClass(this, LocalImageActivity.class);
            intent.putExtra(Consts.MAX_CHOOSEN, 1);
            intent.putExtra(Consts.NEED_VIDEO, false);
            intent.putExtra(Consts.DISABLE_PHOTO_PREVIEW, true);
            intent.putExtra(Consts.TYPE, LocalImageActivity.CHOOSE_TYPE_QRCODE);
            this.startActivityForResult(intent, Consts.REQUEST_CODE_CHOOSE_IMAGE);
        }
    }

    /**
     * call back after QRimage has benn chosen
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    protected void onActivityResult(int requestCode, int resultCode, android.content.Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LogUtil.d("onActivityResult", "req=" + requestCode + " res=" + resultCode);
        if (resultCode == RESULT_OK && requestCode == Consts.REQUEST_CODE_CHOOSE_IMAGE) {
            try {
                final List<UploadImage> paths = (List<UploadImage>) data.getExtras().get(Consts.PATH_LIST);
                if (paths != null) {
                    TaskRuntime.instance().run(new Runnable() {
                        @Override
                        public void run() {
                            final Result result = scanningImage(paths.get(0).getFilePath());
                            ZXingActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    handleResult(result, null);
                                }
                            });
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * scanned qrcode from image
     *
     * @param path file that to be scanned
     * @return
     */
    public Result scanningImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return null;
        }
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; // 先获取原大小
        Bitmap scanBitmap = BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false; // 获取新的大小
        int sampleSize = (int) (options.outHeight / (float) 1000);
        if (sampleSize <= 0)
            sampleSize = 1;
        options.inSampleSize = sampleSize;
        scanBitmap = BitmapFactory.decodeFile(path, options);
        int[] intArray = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(intArray, 0, scanBitmap.getWidth(), 0, 0, scanBitmap.getWidth(), scanBitmap.getHeight());
        LuminanceSource source = new RGBLuminanceSource(scanBitmap.getWidth(), scanBitmap.getHeight(), intArray);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        Result result = null;
        try {
            scanBitmap.recycle();
            Hashtable<DecodeHintType, String> hints = new Hashtable<>();
            hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
            result = reader.decode(binaryBitmap, hints);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}
