package com.daxiangce123.android.util;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;

import java.lang.reflect.Field;

/**
 * @author ram
 * @project listgrid
 * @time 2013-10-28
 */
public class ViewUtil {
    private final static String TAG = "ViewUtil";

    public static int seGridViewHeightBasedOnChildren(GridView gridView) {
        if (gridView == null) return -1;
        ListAdapter listAdapter = gridView.getAdapter();
        if (listAdapter == null) {
            return -1;
        }
        try {
            // int numColum = gridView.getNumColumns();
            int numColum = 2;
            View v = listAdapter.getView(0, null, gridView);
            v.measure(0, 0);
            int singleHeight = v.getMeasuredHeight();
            int count = listAdapter.getCount();
            int extra = count / numColum;
            int totalHeight = (count / numColum + ((extra == 0) ? 0 : 1)) * singleHeight;
            ViewGroup.LayoutParams params = gridView.getLayoutParams();
            params.height = totalHeight;
            gridView.setLayoutParams(params);
            return totalHeight;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean inScreen(final View view, Activity activity) {
        if (activity == null || view == null) {
            return false;
        }
        Rect screenRec = new Rect();
        Rect viewRect = new Rect();
        View v = activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT);
        v.getGlobalVisibleRect(screenRec);
        view.getGlobalVisibleRect(viewRect);
        if (screenRec.contains(viewRect)) {
            return true;
        }
        return false;
    }

    public static int indexOfViewInParent(View view, ViewGroup parent) {
        int index;
        for (index = 0; index < parent.getChildCount(); index++) {
            if (parent.getChildAt(index) == view) break;
        }
        return index;
    }

    /**
     * The v.isShown is true in onCreateView
     */
    public static boolean isShown(View v) {
        if (v == null) {
            return false;
        }
        int visiablity = v.getVisibility();
        if (visiablity == View.VISIBLE) {
            return true;
        }
        return false;
    }

    public static boolean removeFromParent(View v) {
        try {
            ViewParent vp = v.getParent();
            if (vp instanceof ViewGroup) {
                ((ViewGroup) vp).removeView(v);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * get the width of child in gridview
     *
     * @param numColums numColums of GRIDVIEW
     * @param spacing   Horizontal spacing of GRIDVIEW
     * @param width     WIDTH of GRIDVIEW (padding left & padding right is not
     *                  included of GRIDVIEW)
     * @return
     * @time Jan 13, 2014
     */
    public final static int getAdapterItemSize(int numColums, int spacing, int width) {
        if (width < 0) {
            return 0;
        }
        if (numColums < 1 || spacing < 0) {
            return width;
        }
        int exSpace = (numColums - 1) * spacing;
        int trueWidth = width - exSpace;
        return (int) trueWidth / numColums;
    }

    /**
     * <b><i>Before</b></i> call {@link #startFrameAnim(ImageView)}<br>
     * If it's playing it will <b>stop</b> playing, if not <b>start</b> playing<br>
     * <b><i>After</b></i> call frameAnim<br>
     * If it's <b><i>playing</b></i> return TRUE, if not return FALSE<br>
     *
     * @param iv
     * @return
     */
    public final static boolean startFrameAnim(ImageView iv) {
        if (iv == null) {
            return false;
        }
        Drawable drawable = iv.getDrawable();
        if (!(drawable instanceof AnimationDrawable)) {
            return false;
        }
        AnimationDrawable animDraw = (AnimationDrawable) drawable;
        if (animDraw.isRunning()) {
            animDraw.stop();
            animDraw.selectDrawable(0);
            return false;
        } else {
            animDraw.start();
        }
        return true;
    }

    /**
     * If AnimationDrawable is playing , it will be stopped, return <b>TRUE</b>
     *
     * @param iv
     */
    public final static boolean stopFrameAnim(ImageView iv) {
        if (iv == null) {
            return false;
        }
        Drawable drawable = iv.getDrawable();
        if (!(drawable instanceof AnimationDrawable)) {
            return false;
        }
        AnimationDrawable animDraw = (AnimationDrawable) drawable;
        if (animDraw.isRunning()) {
            animDraw.stop();
            animDraw.selectDrawable(0);
            return true;
        }
        return false;
    }

    public final static boolean hasParent(View view) {
        if (view == null) {
            return false;
        }
        ViewParent vp = view.getParent();
        if (vp instanceof ViewGroup) {
            return true;
        }
        return false;
    }

    public final static boolean setTextColor(TextView tv, int resId) {
        if (tv == null) {
            return false;
        }
        if (resId <= 0) {
            return false;
        }
        try {
            ColorStateList cst = tv.getResources().getColorStateList(resId);
            if (cst == null) {
                return false;
            }
            tv.setTextColor(cst);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean pressDelete(EditText editText) {
        if (editText == null) {
            return false;
        }
        try {
            int keyCode = KeyEvent.KEYCODE_DEL;
            KeyEvent keyEventDown = new KeyEvent(KeyEvent.ACTION_DOWN, keyCode);
            KeyEvent keyEventUp = new KeyEvent(KeyEvent.ACTION_UP, keyCode);
            editText.onKeyDown(keyCode, keyEventDown);
            editText.onKeyUp(keyCode, keyEventUp);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static void ajustMaximumVelocity(AbsListView abs, float ration) {
        if (abs == null) {
            return;
        }

        String fieldName = "mMaximumVelocity";
        try {
            final int max = ViewConfiguration.get(abs.getContext()).getScaledMaximumFlingVelocity();
            Field fild = AbsListView.class.getDeclaredField(fieldName);
            fild.setAccessible(true);
            fild.set(abs, (int) (max / ration));
            // fild.set(abs, 100);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (App.DEBUG) {
            try {
                Field fild = AbsListView.class.getDeclaredField(fieldName);
                fild.setAccessible(true);
                LogUtil.d(TAG, "ajustMaximumVelocity()	" + fild.get(abs));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void aleartMessage(int content, DialogInterface.OnClickListener listener, Context context) {
        AlertDialog.Builder clearCache = new AlertDialog.Builder(context);
        clearCache.setMessage(content);
        clearCache.setPositiveButton(R.string.confirm, listener);
        AlertDialog dialog = clearCache.show();
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
    }

    public static void aleartMessage(int content, int postiveContent, DialogInterface.OnClickListener postiveListener, Context context) {
        AlertDialog.Builder clearCache = new AlertDialog.Builder(context);
        clearCache.setMessage(content);
        clearCache.setPositiveButton(postiveContent, postiveListener);
        clearCache.setNegativeButton(R.string.cancel, null);
        AlertDialog dialog = clearCache.show();
        TextView msg = (TextView) dialog.findViewById(android.R.id.message);
        msg.setGravity(Gravity.CENTER);
    }

    public void toggleHideyBar(Activity activity) {

        // BEGIN_INCLUDE (get_current_ui_flags)
        // The UI options currently enabled are represented by a bitfield.
        // getSystemUiVisibility() gives us that bitfield.
        int uiOptions = activity.getWindow().getDecorView().getSystemUiVisibility();
        int newUiOptions = uiOptions;
        // END_INCLUDE (get_current_ui_flags)
        // BEGIN_INCLUDE (toggle_ui_flags)
        boolean isImmersiveModeEnabled =
                ((uiOptions | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY) == uiOptions);
        if (isImmersiveModeEnabled) {
            Log.i(TAG, "Turning immersive mode mode off. ");
        } else {
            Log.i(TAG, "Turning immersive mode mode on.");
        }

        // Navigation bar hiding:  Backwards compatible to ICS.
        if (Build.VERSION.SDK_INT >= 14) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        // Status bar hiding: Backwards compatible to Jellybean
        if (Build.VERSION.SDK_INT >= 16) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
        }

        // Immersive mode: Backward compatible to KitKat.
        // Note that this flag doesn't do anything by itself, it only augments the behavior
        // of HIDE_NAVIGATION and FLAG_FULLSCREEN.  For the purposes of this sample
        // all three flags are being toggled together.
        // Note that there are two immersive mode UI flags, one of which is referred to as "sticky".
        // Sticky immersive mode differs in that it makes the navigation and status bars
        // semi-transparent, and the UI flag does not get cleared when the user interacts with
        // the screen.
        if (Build.VERSION.SDK_INT >= 18) {
            newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        }

        activity.getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
        //END_INCLUDE (set_ui_flags)
    }
}
