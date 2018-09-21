/*
 * Copyright (C) 2012 The Android Open Source Project
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

package com.daxiangce123.android.util;

//import com.example.android.bitmapfun.ui.ImageDetailActivity;
//import com.example.android.bitmapfun.ui.ImageGridActivity;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.graphics.Point;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.StrictMode;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.TextPaint;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.MimeTypeMap;
import android.widget.TextView;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.Binding;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.manager.ImageManager;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.HanyuPinyinVCharType;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Class containing some static utility methods.
 */
public class Utils {
    private static long lastClickTime;
    private static HashMap<String, String> nations = new HashMap<String, String>();

    static {
        initNationsCode();
    }

    private Utils() {
    }


    @TargetApi(11)
    public static void enableStrictMode() {
        if (Utils.hasGingerbread()) {
            StrictMode.ThreadPolicy.Builder threadPolicyBuilder = new StrictMode.ThreadPolicy.Builder().detectAll().penaltyLog();
            StrictMode.VmPolicy.Builder vmPolicyBuilder = new StrictMode.VmPolicy.Builder().detectAll().penaltyLog();

            if (Utils.hasHoneycomb()) {
                threadPolicyBuilder.penaltyFlashScreen();
                // vmPolicyBuilder
                // .setClassInstanceLimit(ImageGridActivity.class, 1)
                // .setClassInstanceLimit(ImageDetailActivity.class, 1);
            }
            StrictMode.setThreadPolicy(threadPolicyBuilder.build());
            StrictMode.setVmPolicy(vmPolicyBuilder.build());
        }
    }

    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static int dp2px(Context ctx, float dpValue) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float scale = metrics.density;

        return (int) (dpValue * scale + 0.5f);

    }

    public static int px2dp(Context ctx, float pxValue) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) ctx.getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(metrics);
        float scale = metrics.density;
        return (int) (pxValue / scale + 0.5f);
    }

    public static final String TAG = "Cliq_Utils";

    public static final String regEmail = "^([a-zA-Z0-9_\\-\\.]+)@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.)|(([a-zA-Z0-9\\-]+\\.)+))([a-zA-Z]{2,4}|[0-9]{1,3})(\\]?)$";
    public static final String regCnChar = "[\u4E00-\u9FFF]+";
    public static final String regUserName = "^[a-zA-Z0-9_-]{2,30}$";
    public static final String regPassword = "^[]{6,25}$";
    public static final String regIpAddress = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\.([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";

    public static boolean isSame(String str0, String str1) {
        if (str0 == str1) {
            // memory addr
            return true;
        }
        if (str0 == null || str1 == null) {
            return false;
        }
        if (str0.length() != str1.length()) {
            return false;
        }
        if (str0.hashCode() != str1.hashCode()) {
            // hashcode
            return false;
        }
        if (str0.equals(str1)) {
            return true;
        }
        return false;
    }

    public static boolean isUserName(String username) {
        if (username == null) {
            return false;
        }

        Pattern p = Pattern.compile(regUserName);
        Matcher m = p.matcher(username);
        return m.find();
    }

    public static boolean isPassword(String text) {
        if (isEmpty(text)) {
            return false;
        }

        int length = text.length();
        if (length < 6) {
            return false;
        }

        return true;
    }

    // public static boolean isEmail(String text) {
    // if (isEmpty(text)) {
    // return false;
    // }
    //
    // Pattern pattern = Pattern.compile(regEmail);
    // Matcher matcher = pattern.matcher(text);
    // return matcher.find();
    // }

    public static boolean isIp4Address(String ipAddress) {
        if (isEmpty(ipAddress)) {
            return false;
        }

        Pattern pattern = Pattern.compile(regIpAddress);
        Matcher macher = pattern.matcher(ipAddress);
        return macher.matches();
    }

    public static void sortByAlbumOwner(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<AlbumEntity>() {
            public int compare(AlbumEntity object1, AlbumEntity object2) {
                int result = object1.getOwner().compareToIgnoreCase(object2.getOwner());
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static List<AlbumEntity> sortByAlbumOwnerList(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return null;
        }
        LinkedList<AlbumEntity> ownedList = new LinkedList<AlbumEntity>();
        LinkedList<AlbumEntity> otherList = new LinkedList<AlbumEntity>();
        for (AlbumEntity albumEntity : list) {
            if (albumEntity.getOwner().equals(App.getUid())) {
                ownedList.add(albumEntity);
            } else {
                otherList.add(albumEntity);
            }
        }

        sortByUpdateTime(ownedList, isDesc);
        sortByUpdateTime(otherList, isDesc);
        for (AlbumEntity album : otherList) {
            ownedList.add(album);
        }
        return ownedList;
    }

    public static void sortByCreateTime(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<AlbumEntity>() {
            public int compare(AlbumEntity object1, AlbumEntity object2) {
                int result = object1.getCreateDate().compareToIgnoreCase(object2.getCreateDate());
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }


    public static void sortByUpdateTime(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<AlbumEntity>() {
            public int compare(AlbumEntity object1, AlbumEntity object2) {
                int result = object1.getModDate().compareToIgnoreCase(object2.getModDate());
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByItemCount(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<AlbumEntity>() {
            public int compare(AlbumEntity object1, AlbumEntity object2) {
                int count1 = object1.getSize();
                int count2 = object2.getSize();
                int result = count1 > count2 ? 1 : (count1 == count2 ? 0 : -1);
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByUpdateCount(List<AlbumEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<AlbumEntity>() {
            public int compare(AlbumEntity object1, AlbumEntity object2) {
                int count1 = object1.getUpdateCount();
                int count2 = object2.getUpdateCount();
                int result = count1 > count2 ? 1 : (count1 == count2 ? 0 : -1);
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByComments(List<FileEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<FileEntity>() {
            public int compare(FileEntity object1, FileEntity object2) {
                int count1 = object1.getComments();
                int count2 = object2.getComments();
                int result = count1 > count2 ? 1 : (count1 == count2 ? 0 : -1);
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByLikes(List<FileEntity> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<FileEntity>() {
            public int compare(FileEntity object1, FileEntity object2) {
                int count1 = object1.getLikes();
                int count2 = object2.getLikes();
                int result = count1 > count2 ? 1 : (count1 == count2 ? 0 : -1);
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByCreatedAt(List<Event> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<Event>() {
            public int compare(Event object1, Event object2) {
                int result = object1.getCreatedDate().compareToIgnoreCase(object2.getCreatedDate());
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }

    public static void sortByDistance(List<NearbyAlbum> list, final boolean isDesc) {
        if (isEmpty(list)) {
            return;
        }
        Collections.sort(list, new Comparator<NearbyAlbum>() {
            public int compare(NearbyAlbum object1, NearbyAlbum object2) {
                float dis1 = object1.getDistance();
                float dis2 = object2.getDistance();
                int result = dis1 > dis2 ? 1 : (dis1 == dis2 ? 0 : -1);
                if (isDesc) {
                    result = -result;
                }
                return result;
            }
        });
    }


    /**
     *
     */
    public static void sordContast(List<Contact> contacts) {
        Collections.sort(contacts, new Comparator<Contact>() {
            @Override
            public int compare(Contact object1, Contact object2) {
                String name1 = object1.getFriend_name();
                String name2 = object2.getFriend_name();
                String str1 = getPingYin(name1);
                String str2 = getPingYin(name2);
                int flag = str1.compareTo(str2);
                return flag;
            }
        });
    }

//    public static void sordContastName(List<String> list) {
//
//        Collections.sort(list, new Comparator<String>() {
//            @Override
//            public int compare(String object1, String object2) {
//                String str1 = getPingYin(object1);
//                String str2 = getPingYin(object2);
//                int flag = str1.compareTo(str2);
//                return flag;
//            }
//        });
//    }

    public static String getPingYin(String inputString) {
        HanyuPinyinOutputFormat format = new HanyuPinyinOutputFormat();
        format.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        format.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
        format.setVCharType(HanyuPinyinVCharType.WITH_V);

        char[] input = inputString.trim().toCharArray();// 把字符串转化成字符数组
        String output = "";

        try {
            for (int i = 0; i < input.length; i++) {
                // \\u4E00是unicode编码，判断是不是中文
                if (java.lang.Character.toString(input[i]).matches(
                        "[\\u4E00-\\u9FA5]+")) {
                    // 将汉语拼音的全拼存到temp数组
                    String[] temp = PinyinHelper.toHanyuPinyinStringArray(
                            input[i], format);
                    // 取拼音的第一个读音
                    if (output.equals("")) {
                        output += "\u0001";
                    }
                    output += temp[0];
                }
                // 大写字母转化成小写字母
                else if (input[i] > 'A' && input[i] < 'Z') {
                    if (output.equals("")) {
                        output += "\u0001";
                    }
                    output += java.lang.Character.toString(input[i]);
                    output = output.toLowerCase();
                } else if (input[i] > 'a' && input[i] < 'z') {
                    if (output.equals("")) {
                        output += "\u0001";
                    }
                    output += input[i];
                } else {
                    output += java.lang.Character.toString(input[i]);
                }
            }
        } catch (Exception e) {
        }
        return output;
    }


    public static String encodeURL(String url) {
        if (isEmpty(url)) {
            return url;
        }

        String result = "";
        String[] temp = url.split("/");
        int length = temp.length;
        for (int index = 0; index < length; index++) {
            try {
                temp[index] = URLEncoder.encode(temp[index], "UTF-8");
                temp[index] = temp[index].replace("+", "%20");
            } catch (Exception e) {
                e.printStackTrace();
                return url;
            }
            result += temp[index];
            if (index < (length - 1)) {
                result += "/";
            }
        }
        return result;
    }

    public static long valueOfLong(String text) {
        long value = -1;
        try {
            value = Long.valueOf(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static boolean eventInView(MotionEvent event, View view) {
        if (event == null || view == null) {
            return false;
        }

        int eventX = (int) event.getRawX();
        int eventY = (int) event.getRawY();

        int[] location = new int[2];
        view.getLocationOnScreen(location);

        int width = view.getWidth();
        int height = view.getHeight();
        int left = location[0];
        int top = location[1];
        int right = left + width;
        int bottom = top + height;

        Rect rect = new Rect(left, top, right, bottom);
        boolean contains = rect.contains(eventX, eventY);
        return contains;
    }

    public static Point getViewMiddlePoint(View view) {
        if (view == null) {
            return new Point();
        }

        int[] location = new int[2];
        view.getLocationOnScreen(location);
        return new Point(location[0] + view.getWidth() / 2, location[1] + view.getHeight() / 2);
    }

    public static int getDistance(MotionEvent event1, MotionEvent event2) {
        float x = event1.getX() - event2.getX();
        float y = event1.getY() - event2.getY();
        return (int) Math.sqrt(x * x + y * y);
    }

    public static boolean isEmpty(CharSequence text) {
        if (text == null || text.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean existsEmpty(String... text) {
        if (text == null || text.length == 0) {
            return true;
        } else {
            for (String str : text) {
                if (isEmpty(str)) {
                    return true;
                }
            }
            return false;
        }
    }

    public static int sizeOf(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return 0;
        } else {
            return collection.size();
        }
    }

    public static boolean isEmpty(Collection<?> collection) {
        if (collection == null || collection.isEmpty()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEmpty(String[] strings) {
        if (strings == null || strings.length == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isClick() {
        long time = System.currentTimeMillis();
        long timeD = time - lastClickTime;
        if (0 < timeD && timeD < 1200) {
            return true;
        }
        lastClickTime = time;
        return false;
    }

    public static final String getString(int resId) {
        if (resId <= 0) {
            return null;
        }
        try {
            return App.getAppContext().getString(resId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static final String getString(int resId, Object... formatArgs) {
        if (resId <= 0) {
            return null;
        }
        try {
            return App.getAppContext().getString(resId, formatArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 2013-12-16T16:42:00.344+08:00
     */
    @SuppressLint("SimpleDateFormat")
    public static String utcToLocal(String utcDate) {
        try {
            String fromFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";//
            String toFormat = "MM-dd HH:mm:ss";
            SimpleDateFormat sdf = new SimpleDateFormat(fromFormat);
            Date date = sdf.parse(utcDate);
            sdf.applyPattern(toFormat);
            return sdf.format(date);
        } catch (Exception e) {
            e.printStackTrace();
            // return e.getMessage() + "; " + utcDate
            // + "---------------------------------------";
        }
        return utcDate;
    }

    public static void printStackTrace() {
        // StackTraceElement[] traces = Thread.currentThread().getStackTrace();
        // for (StackTraceElement trace : traces) {
        // }
    }

    public static void printBundleKey(Bundle bundle) {
        if (bundle == null) {
            return;
        }

        // Set<String> set = bundle.keySet();
        // Iterator<String> iterator = set.iterator();
        // while (iterator.hasNext()) {
        // String key = iterator.next();
        // }
    }

    /**
     * get physic size of the device
     */
    public static float physicSize() {
        try {
            Resources resources = App.getAppContext().getResources();
            DisplayMetrics dm = resources.getDisplayMetrics();
            double x = Math.pow(dm.widthPixels / dm.xdpi, 2);
            double y = Math.pow(dm.heightPixels / dm.ydpi, 2);
            double screenInches = Math.sqrt(x + y);
            return (float) screenInches;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 1;
    }

    public final static int getDip(int d) {
        try {
            Resources resources = App.getAppContext().getResources();
            final float density = resources.getDisplayMetrics().density;
            return (int) (d * density);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public static void showIME() {
        InputMethodManager imm = (InputMethodManager) App.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
    }

    public static void hideIME(View v) {
        InputMethodManager imm = (InputMethodManager) App.getAppContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public final static String getMimeType2(String url) {
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return mimeType;
    }

    public final boolean openLocalFile(String localPath) {
        try {
            if (isEmpty(localPath)) {
                return false;
            }
            File file = new File(localPath);
            if (file.isDirectory() || !file.exists()) {
                return false;
            }
            String mime = FileUtil.getMimeType(file.getName());
            if (Utils.isEmpty(mime)) {
                return false;
            }
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            intent.addCategory(Intent.CATEGORY_DEFAULT);
            intent.setDataAndType(Uri.fromFile(file), mime);
            App.getAppContext().startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public final static void pressKey(int keyCode) {
        try {
            Runtime runtime = Runtime.getRuntime();
            runtime.exec("input keyevent " + keyCode);// KeyEvent.KEYCODE_BACK
            // Instrumentation inst = new Instrumentation();
            // inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final static int getResourceId(String name) {
        try {
            Field field = R.drawable.class.getField(name);
            return Integer.parseInt(field.get(null).toString());
        } catch (Exception e) {
        }
        return 0;
    }

    public final static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public final static int measureTextWidth(TextView view, String text) {
        if (view == null || isEmpty(text)) {
            return 0;
        }
        TextPaint paint = view.getPaint();
        int width = (int) paint.measureText(text);
        return width;
    }

    public static String byte2hex(byte[] bytes) {
        String hs = "";
        String stmp = "";
        for (int n = 0; n < bytes.length; n++) {
            stmp = (java.lang.Integer.toHexString(bytes[n] & 0XFF));
            if (stmp.length() == 1) hs += "0" + stmp;
            else hs += stmp;
        }
        return hs.toLowerCase(Locale.ENGLISH);
    }

    public static final String toMaxLength(String message, int max) {
        if (Utils.isEmpty(message)) {
            return null;
        }
        int length = message.length();
        if (length > max) {
            return message.substring(0, max);
        }
        return message;
    }

    /**
     * @param inStr
     * @return
     */
    public static String MD5(String inStr) {
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
            return "";
        }
        char[] charArray = inStr.toCharArray();
        byte[] byteArray = new byte[charArray.length];

        for (int i = 0; i < charArray.length; i++)
            byteArray[i] = (byte) charArray[i];

        byte[] md5Bytes = md5.digest(byteArray);

        StringBuffer hexValue = new StringBuffer();

        for (int i = 0; i < md5Bytes.length; i++) {
            int val = ((int) md5Bytes[i]) & 0xff;
            if (val < 16) {
                hexValue.append("0");
            }
            hexValue.append(Integer.toHexString(val));
        }

        return hexValue.toString();
    }

//    public static final String getHWUUID() {
//        try {
//            StringBuilder builder = new StringBuilder();
//            builder.append(getBtMac() + "-");
//            builder.append(getWlanMac() + "-");
//            builder.append(getAndroidId() + "-");
//            builder.append(getIMEI());
//            return MD5(builder.toString());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    public static final String getBtMac() {
//        BluetoothAdapter m_BluetoothAdapter = null;
//        m_BluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        String m_szBTMAC = m_BluetoothAdapter.getAddress();
//        return m_szBTMAC;
//    }
//
//    public static final String getWlanMac() {
//        WifiManager wm = (WifiManager) App.getAppContext().getSystemService(Context.WIFI_SERVICE);
//        String m_szWLANMAC = wm.getConnectionInfo().getMacAddress();
//        return m_szWLANMAC;
//    }

    public static final String getAndroidId() {
        String m_szAndroidID = Secure.getString(App.getAppContext().getContentResolver(), Secure.ANDROID_ID);
        return m_szAndroidID;
    }

    public static final String getIMEI() {
        TelephonyManager TelephonyMgr = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        String szImei = TelephonyMgr.getDeviceId();
        return szImei;
    }

    public static boolean isAppInstalled(Context context, String packageName) {
        boolean flag = false;
        try {
            if (isEmpty(packageName)) return flag;
            final PackageManager packageManager = context.getPackageManager();
            List<PackageInfo> mPackageInfo = packageManager.getInstalledPackages(PackageManager.PERMISSION_GRANTED);
            if (mPackageInfo != null) {
                String tempName = null;
                for (PackageInfo packageInfo : mPackageInfo) {
                    tempName = packageInfo.packageName;
                    if (tempName != null && tempName.equals(packageName)) {
                        flag = true;
                        break;
                    }
                }
            }
            return flag;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    public final static int seperate(List<FileEntity> lists, List<FileEntity> resultList, FileEntity fileEntity) {
        if (isEmpty(lists) || resultList == null) {
            LogUtil.d(TAG, "data is empty or resultlist is null");
            return -1;
        }
        if (fileEntity == null || !fileEntity.isValid()) {
            return -1;
        }
        try {
            int result = -1;
            for (int i = 0; i < lists.size(); i++) {
                FileEntity entity = lists.get(i);
                if (entity == null) {
                    continue;
                }
                if (entity == fileEntity) {
                    result = i;
                } else if (!fileEntity.getOwner().equals(entity.getOwner())) {
                    continue;
                }
                resultList.add(entity);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public final static LinkedList<FileEntity> seperate(List<FileEntity> lists, String owner) {
        if (isEmpty(lists)) {
            return null;
        }
        if (isEmpty(owner)) {
            return null;
        }
        try {
            LinkedList<FileEntity> resultList = null;
            for (int i = 0; i < lists.size(); i++) {
                FileEntity entity = lists.get(i);
                if (entity == null) {
                    continue;
                }
                if (!owner.equals(entity.getOwner())) {
                    continue;
                }
                if (resultList == null) {
                    resultList = new LinkedList<FileEntity>();
                }
                resultList.add(entity);
            }
            return resultList;
        } catch (Exception e) {
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    public static void copyToClipboard(Context context, String text) {
        Context appContext = context.getApplicationContext();
        ClipboardManager clipboardManager = (ClipboardManager) appContext.getSystemService(Context.CLIPBOARD_SERVICE);
        clipboardManager.setText(text);
    }

    public static void sendViaSMS(Context context, String text) {
        try {
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"));
            intent.putExtra("sms_body", text);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            sendViaEmail(context, "", text);
        }
        // if (isSimAvailable()) {
        // } else {
        // YToast.showToast(R.string.simunavailable);
        // }
    }

    public static void sendViaSMS(Context context, String text, String sendTo) {
        try {
            if (sendTo == null) {
                sendTo = "";
            }
            Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:" + sendTo));
            intent.putExtra("sms_body", text);
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            sendViaEmail(context, "", text);
        }
        // if (isSimAvailable()) {
        // } else {
        // YToast.showToast(R.string.simunavailable);
        // }
    }


    public static void sendViaEmail(Context context, String subject, String text) {
        try {
            Uri uri = Uri.parse("mailto:");
            Intent intent = new Intent(Intent.ACTION_SEND, uri);
            // intent.setType("text/plain");
            // intent.setType("plain/text");
            intent.setType("message/rfc822");
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});
            intent.putExtra(Intent.EXTRA_SUBJECT, subject);
            intent.putExtra(Intent.EXTRA_TEXT, text);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(Intent.createChooser(intent, subject));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * @param dest
     * @param format "#.#"
     * @return
     */
    public static final String formatNum(double dest, String format) {
        if (isEmpty(format)) {
            return dest + "";
        }
        DecimalFormat formatter = new DecimalFormat(format);
        return formatter.format(dest);
    }

    public static boolean isAppHidden(Context context) {
        if (context == null) {
            return true;
        }
        try {
            ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            List<RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
            if (appProcesses == null) {
                return false;
            }
            for (RunningAppProcessInfo runningAppProcessInfo : appProcesses) {
                if (runningAppProcessInfo.processName.equals(context.getPackageName())) {
                    int status = runningAppProcessInfo.importance;
                    if (status == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                        return true;
                    }
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    private static Pattern pattern = Pattern.compile("((?:(http|https|Http|Https):\\/\\/" + "(?:(?:[a-zA-Z0-9\\$\\-\\_\\.\\+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|"
            + "(?:\\%[a-fA-F0-9]{2})){1,64}(?:\\:(?:[a-zA-Z0-9\\$\\-\\_\\.\\" + "+\\!\\*\\'\\(\\)\\,\\;\\?\\&\\=]|(?:\\%[a-fA-F0-9]{2})){1,25})"
            + "?\\@)?)?((?:(?:[a-zA-Z0-9][a-zA-Z0-9\\-\\_]{0,64}\\.)" + "+(?:(?:aero|arpa|asia|a[cdefgilmnoqrstuwxz])" + "|(?:biz|b[abdefghijmnorstvwyz])|(?:cat|com|"
            + "coop|c[acdfghiklmnoruvxyz])|d[ejkmoz]|(?:edu|e[cegrstu])|f[ijkmor]" + "|(?:gov|g[abdefghilmnpqrstuwy])|h[kmnrtu]|(?:info|int|i[delmnoqrst])"
            + "|(?:jobs|j[emop])|k[eghimnrwyz]|l[abcikrstuvy]|" + "(?:mil|mobi|museum|m[acdeghklmnopqrstuvwxyz])|" + "(?:name|net|n[acefgilopruz])|(?:org|om)|(?:pro|p[aefghklmnrstwy])|"
            + "qa|r[eouw]|s[abcdeghijklmnortuvyz]|(?:tel|travel|t[cdfghjklmnoprtvwz])" + "|u[agkmsyz]|v[aceginu]|w[fs]|y[etu]|z[amw]))|(?:(?:25[0-5]|2[0-4][0-9]"
            + "|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}" + "|[1-9][0-9]|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]"
            + "|[1-9]|0)\\.(?:25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[0-9])))" + "(?:\\:\\d{1,5})?)(\\/(?:(?:[a-zA-Z0-9\\;\\/\\?\\:\\@\\&\\=\\#\\~%\\-\\."
            + "\\+\\!\\*\\'\\(\\)\\,\\_])|(?:\\%[a-fA-F0-9]{2}))*)?");

    public static List<String> extractUrls(String input) {
        List<String> result = new ArrayList<String>();

        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            result.add(matcher.group());
        }

        return result;
    }

    public static void hideInputMethod(Context context, View v) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static void scanNewMedia(String path) {
        if (path != null) {
            App.getAppContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(new File(path))));
        }
    }

    public static void createBatchId(List<UploadImage> images) {
        String batchId = UUID.randomUUID().toString();
        for (UploadImage uploadImage : images) {
            uploadImage.setBatchId(batchId);
        }
    }

    public static String createEntityHashId(FileEntity fileEntity) {
        if (fileEntity == null || (!fileEntity.isValid())) {
            return null;
        } else {
            StringBuilder fakeIdBuilder = new StringBuilder();
            fakeIdBuilder.append(fileEntity.getAlbum());
            fakeIdBuilder.append(fileEntity.getBatchId());
            fakeIdBuilder.append(fileEntity.getSeqNum());
            String hash = MD5(fakeIdBuilder.toString());
            return hash;
        }
    }

    public static String createHashId(UploadImage uploadImage, String albumId) {
        if (uploadImage == null) {
            return null;
        } else {
            String hash = MD5(albumId + uploadImage.getBatchId() + uploadImage.getSeqNum());
            // LogUtil.v(TAG, albumId + "-" + uploadImage.getBatchId() + "-" +
            // uploadImage.getSeqNum() + " |createHashId=" + hash);
            return hash;
        }
    }

    public static String createHashId(String albumId, String batchId, String seqNum) {
        if (albumId == null || batchId == null || seqNum == null) {
            return null;
        } else {
            String hash = MD5(albumId + batchId + seqNum);
            // LogUtil.v(TAG, albumId + "-" + uploadImage.getBatchId() + "-" +
            // uploadImage.getSeqNum() + " |createHashId=" + hash);
            return hash;
        }
    }

    /**
     * 生成假的 entity 用来填充UI
     *
     * @return
     */
    public static ArrayList<FileEntity> createFakeFileEntities(AlbumEntity albumEntity, List<UploadImage> images) {
        ArrayList<FileEntity> entityList = new ArrayList<FileEntity>();
        for (UploadImage uploadImage : images) {
            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(createHashId(uploadImage, albumEntity.getId()));
            fileEntity.setBatchId(uploadImage.batchId);
            fileEntity.setTitle(uploadImage.getFileName());
            fileEntity.setName(uploadImage.getFileName());
            fileEntity.setSeqNum(uploadImage.getSeqNum());
            fileEntity.setFilePath(uploadImage.getFilePath());
            fileEntity.setCreateDate(TimeUtil.getCurrentUTCTime());
            fileEntity.setModDate(TimeUtil.getCurrentUTCTime());
            fileEntity.setalbum(albumEntity.getId());
            fileEntity.setOwner(App.getUid());
            fileEntity.setCreator(App.getUid());
            fileEntity.setStatus(Consts.FILEENTITY_STATUS_UNUPLOAD);
            fileEntity.setMimeType(FileUtil.getMimeType(uploadImage.getFileName()));
            fileEntity.setNewUpload(true);
            entityList.add(fileEntity);
        }
        return entityList;
    }

    public static List<UploadImage> getUploadingImagesAndSync(DBHelper dbHelper) {
        List<FileEntity> fileEntities = null;
        List<UploadImage> resultsImages = new LinkedList<UploadImage>();
        List<String> deletes = new LinkedList<String>();
        if (App.getUid() == null) {
            return resultsImages;
        }
        // 1 query create file not success
        String WHERE = Consts.STATUS + " in (\"" + Consts.FILEENTITY_STATUS_UNUPLOAD + "\" , \"" + Consts.FILEENTITY_STATUS_UPLOADING + "\" )";
        fileEntities = dbHelper.getList(FileEntity.EMPTY, WHERE);
        if (fileEntities != null) {
            for (FileEntity file : fileEntities) {
                File orgin = new File(file.getFilePath());
                File cache = new File(ImageManager.instance().getImageCachePath(Utils.createHashId(file.getAlbum(), file.getBatchId(), String.valueOf(file.getSeqNum()))));
                if ((orgin.exists() || cache.exists()) && (file.getOwner().equals(App.getUid()))) {
                    // need upload again
                    UploadImage image = new UploadImage();
                    image.setBatchId(file.getBatchId());
                    image.setSeqNum(file.getSeqNum());
                    image.setAlbumeId(file.getAlbum());
                    image.setFilePath(file.getFilePath());
                    image.setCompress(true);
                    if (file.getStatus().equals(Consts.FILEENTITY_STATUS_UPLOADING)) {
                        image.setFileId(file.getId());
                        image.setSize(file.getSize());
                        image.setFileEntityContent(file);
                    }
                    resultsImages.add(image);
                } else {
                    deletes.add(file.getId());
                }
            }
            if (App.DEBUG) {
                LogUtil.d(TAG, "not create file:" + fileEntities.size());
            }
        }
        // 2 delete fileEntity that never used
        for (String fileID : deletes) {
            dbHelper.delete(FileEntity.EMPTY, fileID);
        }
        return resultsImages;
    }

    public static StringBuilder printInheritance(Object object) {
        StringBuilder builder = new StringBuilder();
        if (object == null) {
            builder.append("NULL");
            return builder;
        }
        builder.append("hash:" + object.hashCode() + "	");
        Class<?> cls = object.getClass();
        while (true) {
            String name = cls.getName();
            builder.append("name	" + name);
            builder.append("\n");
            if (Object.class.getName().equals(name)) {
                break;
            } else {
                builder.append(" -> ");
                cls = cls.getSuperclass();
            }
        }
        return builder;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static SQLiteDatabase openOrCreateDatabase(Context context, String name, int mode, CursorFactory factory, DatabaseErrorHandler errorHandler) {
        SQLiteDatabase db = null;
        int flags = SQLiteDatabase.CREATE_IF_NECESSARY | SQLiteDatabase.NO_LOCALIZED_COLLATORS;
        Class<?> cls = context.getClass();
        try {
            Method method = cls.getDeclaredMethod("validateFilePath", new Class[]{String.class, boolean.class});
            method.setAccessible(true);
            File f = (File) method.invoke(context, new Object[]{name, true});
            if ((mode & Context.MODE_ENABLE_WRITE_AHEAD_LOGGING) != 0) {
                flags |= SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING;
            }
            db = SQLiteDatabase.openDatabase(f.getPath(), factory, flags, errorHandler);
            if (db != null) {
                Method methodSFPF = cls.getDeclaredMethod("setFilePermissionsFromMode", new Class[]{String.class, int.class, int.class});
                methodSFPF.setAccessible(true);
                methodSFPF.invoke(context, new Object[]{db.getPath(), mode, 0});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return db;
    }

//    public static int getActionBarHeight(Context context) {
//        TypedValue tv = new TypedValue();
//        int height = 0;
//        int resId = android.R.attr.actionBarSize;
//        if (context.getTheme().resolveAttribute(resId, tv, true)) {
//            DisplayMetrics dm = context.getResources().getDisplayMetrics();
//            height = TypedValue.complexToDimensionPixelSize(tv.data, dm);
//        }
//        return height;
//    }
//
//    public static int getStatusBarHeight(Context context) {
//        int height = 0;
//        if (context == null) {
//            return height;
//        }
//        Resources resources = context.getResources();
//        int resId = resources.getIdentifier("status_bar_height", "dimen",
//                "android");
//        if (resId > 0) {
//            height = resources.getDimensionPixelSize(resId);
//        }
//        return height;
//    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void showStatusBar(Activity activity) {
        if (VersionUtil.hasJellyBean()) {
            View decorView = activity.getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void hideStatusBar(Activity activity) {
        if (VersionUtil.hasJellyBean()) {
            View decorView = activity.getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.INVISIBLE;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    public static void setStatusBarVisibility(Activity activity, boolean show) {
        if (show) {
            WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
            activity.getWindow().setAttributes(lp);
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        } else {
            WindowManager.LayoutParams attr = activity.getWindow().getAttributes();
            attr.flags &= (~WindowManager.LayoutParams.FLAG_FULLSCREEN);
            activity.getWindow().setAttributes(attr);
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        }
    }

    public static String formatSize(long length) {
        long kb = length / Consts.BYTE_PRE_KB;
        length = length > 0 ? length : 1;
        String result = "";
        if (kb > 0 && kb < Consts.KB_PRE_MB) {
            result = kb + "K";
        } else if (kb > Consts.KB_PRE_MB) {
            result = String.format("%.1f", kb / Double.valueOf(Consts.KB_PRE_MB)) + "M";
        }
        return result;
    }

    public static String formatFileSize(long fileS) {
        java.text.DecimalFormat df = new java.text.DecimalFormat("#.00");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / Consts.BYTE_PRE_KB) + "KB";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / Consts.BYTE_PRE_MB) + "MB";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static long getDirSize(File dir) {
        if (dir == null) {
            return 0;
        }
        if (!dir.isDirectory()) {
            return 0;
        }
        long dirSize = 0;
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isFile()) {
                dirSize += file.length();
            } else if (file.isDirectory()) {
                dirSize += file.length();
                dirSize += getDirSize(file);
            }
        }
        return dirSize;
    }

    public static boolean isPasswordVilied(String password) {
        int length = password.length();
        if (length >= 8 && length <= 30) {
            return true;
        } else {
            return false;
        }
    }

    public static String repleaseMobileNumber(String mobile) {
        return mobile.replaceAll("\\+(\\d{2})(\\d{3})\\d{4}(\\d{4})", "$2****$3");
    }

    public static String repleaseMobileNumberWithContact(String mobile) {
        return mobile.replaceAll("\\+(\\d{2})(\\d{3})\\d{4}(\\d{4})", "+$1$2****$3");
    }

//    public static boolean isMobileVilied(String area, String mobile) {
//        if (area != null && mobile != null) {
//            if (area.length() + mobile.length() > 11)
//                return true;
//        }
//        return false;
//    }

    public static boolean isMobileVilied(String area, String mobile) {
        if (area.equals("+86")) {

            if (mobile.length() == 11 && mobile.startsWith("1")) {
                return true;
            } else {
                return false;
            }
        } else {
            return true;
        }
//        else if (area.equals("+1")) {
//            if (mobile.length() > 9) {
//                return true;
//            } else {
//                return false;
//            }
//        }
    }


    /**
     * return nations code
     */


    private static final void initNationsCode() {

        nations.put("AF", "+93");
        nations.put("AR", "+54");
        nations.put("AU", "+61");
        nations.put("AT", "+43");
        nations.put("BD", "+880");
        nations.put("BY", "+375");
        nations.put("BE", "+32");
        nations.put("BJ", "+229");
        nations.put("BO", "+591");
        nations.put("BR", "+55");
        nations.put("BN", "+673");
        nations.put("BG", "+359");
        nations.put("MM", "+95");
        nations.put("CM", "+237");
        nations.put("CA", "+1");
        nations.put("CF", "+236");
        nations.put("CL", "+56");
        nations.put("CN", "+86");
        nations.put("CO", "+57");
        nations.put("CG", "+242");
        nations.put("CR", "+506");
        nations.put("CU", "+53");
        nations.put("CZ", "+420");
        nations.put("DK", "+45");
        nations.put("EC", "+593");
        nations.put("EG", "+20");
        nations.put("ET", "+251");
        nations.put("FI", "+358");
        nations.put("FR", "+33");
        nations.put("DE", "+49");
        nations.put("GH", "+233");
        nations.put("GR", "+30");
        nations.put("GN", "+224");
        nations.put("HT", "+509");
        nations.put("HN", "+504");
        nations.put("HK", "+852");
        nations.put("HU", "+36");
        nations.put("IS", "+354");
        nations.put("IN", "+91");
        nations.put("ID", "+62");
        nations.put("IR", "+98");
        nations.put("IQ", "+964");
        nations.put("IE", "+353");
        nations.put("IL", "+972");
        nations.put("IT", "+39");
        nations.put("JM", "+1876");
        nations.put("JP", "+81");
        nations.put("JO", "+962");
        nations.put("KH", "+855");
        nations.put("KZ", "+327");
        nations.put("KE", "+254");
        nations.put("KR", "+82");
        nations.put("KW", "+965");
        nations.put("KG", "+331");
        nations.put("LA", "+856");
        nations.put("LB", "+961");
        nations.put("LR", "+231");
        nations.put("LY", "+218");
        nations.put("LT", "+370");
        nations.put("LU", "+352");
        nations.put("MO", "+853");
        nations.put("MG", "+261");
        nations.put("MY", "+60");
        nations.put("MV", "+960");
        nations.put("MU", "+230");
        nations.put("MX", "+52");
        nations.put("MC", "+377");
        nations.put("MN", "+976");
        nations.put("MA", "+212");
        nations.put("NP", "+977");
        nations.put("NL", "+31");
        nations.put("NZ", "+64");
        nations.put("NE", "+227");
        nations.put("NG", "+234");
        nations.put("KP", "+850");
        nations.put("NO", "+47");
        nations.put("PK", "+92");
        nations.put("PA", "+507");
        nations.put("PY", "+595");
        nations.put("PE", "+51");
        nations.put("PH", "+63");
        nations.put("PL", "+48");
        nations.put("PT", "+351");
        nations.put("RO", "+40");
        nations.put("RU", "+7");
        nations.put("SA", "+966");
        nations.put("SG", "+65");
        nations.put("SK", "+421");
        nations.put("SB", "+677");
        nations.put("SO", "+252");
        nations.put("ZA", "+27");
        nations.put("ES", "+34");
        nations.put("LK", "+94");
        nations.put("SD", "+249");
        nations.put("SE", "+46");
        nations.put("CH", "+41");
        nations.put("SY", "+963");
        nations.put("TW", "+886");
        nations.put("TJ", "+992");
        nations.put("TH", "+66");
        nations.put("TO", "+676");
        nations.put("TR", "+90");
        nations.put("TM", "+993");
        nations.put("UA", "+380");
        nations.put("AE", "+971");
        nations.put("GB", "+44");
        nations.put("US", "+1");
        nations.put("UY", "+598");
        nations.put("UZ", "+233");
        nations.put("VE", "+58");
        nations.put("VN", "+84");
        nations.put("YE", "+967");
        nations.put("YU", "+381");
        nations.put("ZW", "+263");
        nations.put("ZM", "+260");
    }

    public static String getNationsPhoneCode(String areaCode) {
        areaCode = areaCode.toUpperCase();
        String phoneCode = nations.get(areaCode);
        return phoneCode;
    }


    public static int getShareImageCreateUtilSize(int originSize) {
        int size = (int) (originSize * 1.3f);
        return size;
    }

    /**
     * check p
     *
     * @param bindings
     * @param provider
     * @return
     */
    public static boolean checkBinded(ArrayList<Binding> bindings, Consts.PROVIDERS provider) {
        if (bindings == null) return false;
        for (Binding binding : bindings) {
            if (binding.getProvider().equals(provider.toString())) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkMessage(String msm) {
        if (!msm.contains("大象册")) {
            return false;
        }
        return true;
    }

    /**
     * @param isoCode
     * @return display name
     */
    public static String getCountryInCode(String isoCode) {
        if (App.DEBUG) {
            LogUtil.v(TAG, "getCountryInCode: " + isoCode);
        }
        Locale.setDefault(Locale.CHINA);
        Locale locale = new Locale("", isoCode, "");
        return locale.getDisplayCountry();
    }

    public static String getDeviceId() {
        String deviceId = null;
        try {
            final TelephonyManager tm = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
            final String tmDevice, androidId;
            tmDevice = "" + tm.getDeviceId();
            androidId = "" + android.provider.Settings.Secure.getString(App.getAppContext().getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
            StringBuffer sb = new StringBuffer();
            if (tmDevice != null) {
                sb.append(tmDevice);
            }
            if (androidId != null) {
                sb.append(androidId);
            }
            deviceId = sb.toString();
            if (App.DEBUG) {
                LogUtil.v("App_", tmDevice + "  " + androidId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(deviceId)) {
            return "";
        } else {
            return deviceId;
        }
    }

    public static String grepTextBlank(String text) {
        if (!TextUtils.isEmpty(text)) {
            return text.replace(" ", "");
        }
        return text;
    }
}
