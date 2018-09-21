package com.daxiangce123.android.data;

import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

public class AppData {

    public final static boolean setToken(String user) {
        if (Utils.isEmpty(user)) {
            return false;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.ACCESS_TOKEN, user).commit();
        return true;
    }

    public final static String getToken() {
        String current = App.getAppPrefs().getString(Consts.ACCESS_TOKEN, "");
        return current;
    }

    public final static boolean setTempHttpToken(String token) {
        if (Utils.isEmpty(token)) {
            return false;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.TEMP_ACCESS_TOKEN, token).commit();
        return true;
    }

    public final static String getTempHttpToken() {
        String current = App.getAppPrefs().getString(Consts.TEMP_ACCESS_TOKEN, "");
        return current;
    }

    public final static void setStatus(String status) {
        if (Utils.isEmpty(status)) {
            return;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.STATUS, status).commit();
    }

    public final static String getStatus() {
        String status = App.getAppPrefs().getString(Consts.STATUS, "");
        return status;
    }

    public final static void setBannerId(String bannerId) {
        if (Utils.isEmpty(bannerId)) {
            return;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.BANNER_ID + App.getUid(), bannerId).commit();
    }

    public final static String getBannerId() {
        String bannerId = App.getAppPrefs().getString(Consts.BANNER_ID + App.getUid(), "");
        return bannerId;
    }


    public final static void setAppLockTime(int time) {
        Editor editor = App.getAppPrefs().edit();
        editor.putInt(Consts.APP_LOCK_TIME, time).commit();
    }

    public final static int getAppLockTime() {
        int time = App.getAppPrefs().getInt(Consts.APP_LOCK_TIME, 0);
        return time;

    }

    public final static void setDownloadFileTime(int time) {
        Editor editor = App.getAppPrefs().edit();
        editor.putInt(Consts.DOWNLOAD_FILE_TIME, time).commit();
    }

    public final static int getDownloadFileTime() {
        int time = App.getAppPrefs().getInt(Consts.DOWNLOAD_FILE_TIME, 0);
        return time;
    }

    public final static void setSplashId(String id){
        if(Utils.isEmpty(id)){return;}
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.ID, id).commit();
    }

    public final static String getSplashId(){
        String id = App.getAppPrefs().getString(Consts.ID, "");
        return id;
    }

    public final static void setApkName(String apkName){
        if(Utils.isEmpty(apkName)){return;}
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.NAME, apkName).commit();
    }

    public final static String getApkName(){
        String  apkName = App.getAppPrefs().getString(Consts.NAME, "");
        return apkName;
    }

    public final static void setSplashUrl(String Url){
        if(Utils.isEmpty(Url)){return;}
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.URL, Url).commit();
    }

    public final static String getSplashUrl(){
        String url = App.getAppPrefs().getString(Consts.URL, "");
        return url;
    }

    public final static void setSplashBgColor(String color){
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.SPLASH_BG_COLOR, color).commit();
    }

    public final static String getSplashBgColor(){
        String colors = App.getAppPrefs().getString(Consts.SPLASH_BG_COLOR, "");
        return colors;
    }

    public final static void setSplashStartDate(long startDate){
        Editor editor = App.getAppPrefs().edit();
        editor.putLong(Consts.START_DATE, startDate).commit();
    }

    public final static long getSplashStartDate(){
        long startDate = App.getAppPrefs().getLong(Consts.START_DATE, 0);
        return startDate;
    }

    public final static void setSplashEndDate(long endDate){
        Editor editor = App.getAppPrefs().edit();
        editor.putLong(Consts.END_DATE, endDate).commit();
    }

    public final static long getSplashEndDate(){
        long endDate = App.getAppPrefs().getLong(Consts.END_DATE, 0);
        return endDate;
    }

    public final static void setBindPhoneGuideTime(int time) {
        Editor editor = App.getAppPrefs().edit();
        editor.putInt(Consts.BIND_PHONE_GUIDE_TIME, time).commit();
    }

    public final static int getBindPhoneGuideTime() {
        int time = App.getAppPrefs().getInt(Consts.BIND_PHONE_GUIDE_TIME, 0);
        return time;
    }

    public final static int getReadContactPopupTime() {
        int time = App.getAppPrefs().getInt(Consts.READ_CONTACT_SHOW_TIME + getUid(), 1);
        return time;
    }

    public final static boolean hasUserAgreeToReadContact() {
        boolean time = App.getAppPrefs().getBoolean(Consts.READ_CONTACT + getUid(), false);
        return time;
    }

    public static void sethasUserAgreeToReadContact(boolean agree) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.READ_CONTACT + getUid(), agree).commit();
    }

    public static void setReadContactPopupTime(int time) {
        Editor editor = App.getAppPrefs().edit();
        editor.putInt(Consts.READ_CONTACT_SHOW_TIME + getUid(), time).commit();
    }


    public final static boolean hasShowUserContactGruid() {
        boolean time = App.getAppPrefs().getBoolean(Consts.SHOW_CONTACT_GRUID + getUid(), true);
        return time;
    }

    public static void setShowContactGruid(boolean agree) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.SHOW_CONTACT_GRUID + getUid(), agree).commit();
    }

    public final static void setCloseBannerDate(long closeBannerDate) {
        if (closeBannerDate == 0) {
            return;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putLong(Consts.CLOSE_DATE, closeBannerDate).commit();
    }

    public final static long getCloseBannerDate() {
        long closeBannerDate = App.getAppPrefs().getLong(Consts.CLOSE_DATE, 0);
        return closeBannerDate;
    }

    public final static void setQuitTime(long quitTime) {
        Editor editor = App.getAppPrefs().edit();
        editor.putLong(Consts.QUIT_APP_TIME, quitTime).commit();
    }

    public final static long getQuitTime() {
        long quitTime = App.getAppPrefs().getLong(Consts.QUIT_APP_TIME, 0);
        return quitTime;
    }

    public final static boolean hasShowUploadShareGuide(boolean hasShow) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.HAS_SHOW_UPLOAD_SHARE_GUIDE, hasShow);
        return editor.commit();
    }

    public final static boolean getHasShowUploadShareGuide() {
        boolean hasShow = App.getAppPrefs().getBoolean(Consts.HAS_SHOW_UPLOAD_SHARE_GUIDE, false);
        return hasShow;
    }

    public final static boolean hasShowShareGuide(boolean hasShow) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.HAS_SHOW_SHARE_GUIDE, hasShow);
        return editor.commit();
    }

    public final static boolean getHasShowShareGuide() {
        boolean hasShow = App.getAppPrefs().getBoolean(Consts.HAS_SHOW_SHARE_GUIDE, false);
        return hasShow;
    }

//    public final static boolean setFirstShare(boolean firstShare) {
//        Editor editor = App.getAppPrefs().edit();
//        editor.putBoolean(Consts.FIRST_UPLOAD, firstShare);
//        return editor.commit();
//    }
//
//    public final static boolean getFirstShare() {
//        boolean firstShare = App.getAppPrefs().getBoolean(Consts.FIRST_UPLOAD, false);
//        return firstShare;
//    }

    public final static boolean setFirstUpload(boolean firstUpload) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.FIRST_UPLOAD, firstUpload);
        return editor.commit();
    }

    public final static boolean getFirstUpload() {
        boolean firstUpload = App.getAppPrefs().getBoolean(Consts.FIRST_UPLOAD, false);
        return firstUpload;
    }

    public final static boolean setFirstOpenAlbum(boolean firstOpen) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.FIRST_OPEN_ALBUM, firstOpen);
        return editor.commit();
    }

    public final static boolean getFirstOpenAlbum() {
        boolean firstOpen = App.getAppPrefs().getBoolean(Consts.FIRST_OPEN_ALBUM, false);
        return firstOpen;
    }


    public final static boolean setAlbumSort(int sort) {
        Editor editor = App.getAppPrefs().edit();
        editor.putInt(Consts.ALBUM_SORT, sort);
        return editor.commit();
    }

    public final static int getAlbumSort() {
        int sort = App.getAppPrefs().getInt(Consts.ALBUM_SORT, 0);
        return sort;
    }

    public static void setFirstBindPhone(boolean isFirstBind) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.IS_FIRST_BIND, isFirstBind);
        editor.commit();
    }

    public static boolean getFirstBindPhone() {
        boolean isFirstBind = App.getAppPrefs().getBoolean(Consts.IS_FIRST_BIND, false);
        return isFirstBind;
    }

    public static void showNewUserGuide(boolean isShow) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.SHOW_NEW_USER_GUIDE, isShow);
        editor.commit();
    }

    public static boolean getShowNewUserGuide() {
        boolean showNewUserGuide = App.getAppPrefs().getBoolean(Consts.SHOW_NEW_USER_GUIDE, false);
        return showNewUserGuide;
    }

    public final static void setFirstShowActTime(long firstShowTime) {
        Editor editor = App.getAppPrefs().edit();
        editor.putLong(Consts.FIRST_UPLOAD_TIME, firstShowTime).commit();
    }

    public final static long getFirstShowActTime() {
        long firtShowTime = App.getAppPrefs().getLong(Consts.FIRST_UPLOAD_TIME, 0);
        return firtShowTime;
    }

    public final static boolean setFirstShowAct(boolean firstShow) {
        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.FIRST_UPLOAD, firstShow);
        return editor.commit();
    }

    public final static boolean getFirstShowAct() {
        boolean firstShow = App.getAppPrefs().getBoolean(Consts.FIRST_UPLOAD, false);
        return firstShow;
    }

    // public final static boolean setMaskShow(boolean hasShow) {
    // Editor editor = App.getAppPrefs().edit();
    // editor.putBoolean(Consts.MASK_HAS_SHOW + App.getUid(), hasShow);
    // return editor.commit();
    // }
    //
    // public final static boolean getMaskShow() {
    // boolean hasShow = App.getAppPrefs().getBoolean(Consts.MASK_HAS_SHOW +
    // App.getUid(), false);
    // return hasShow;
    // }

    public final static boolean setUid(String user) {
        if (Utils.isEmpty(user)) {
            return false;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.CURRENT_USER, user).commit();
        return true;
    }

    public final static String getUid() {
        String current = App.getAppPrefs().getString(Consts.CURRENT_USER, "");
        return current;
    }

    // public final static boolean setLogined(boolean logined) {
    // Editor editor = App.getAppPrefs().edit();
    // editor.putBoolean(Consts.HAS_LOGINED, logined);
    // return editor.commit();
    // }

    // public final static boolean hasLogined() {
    // return App.getAppPrefs().getBoolean(Consts.HAS_LOGINED, false);
    // }

    public final static boolean getNotFirstLaunch() {

        boolean notFirst = App.getAppPrefs().getBoolean(Consts.FIRST_LAUNCH, false);
        return notFirst;
    }

    public final static boolean setNotFirstLaunch(boolean notFirstLaunch) {

        Editor editor = App.getAppPrefs().edit();
        editor.putBoolean(Consts.FIRST_LAUNCH, notFirstLaunch);
        return editor.commit();
    }

    public final static boolean setRegisterId(String regId) {
        if (Utils.isEmpty(regId)) {
            return false;
        }
        Editor editor = App.getAppPrefs().edit();
        editor.putString(Consts.REGISTRATION_ID, regId).commit();
        return true;
    }

    public final static String getRegisterId() {
        String current = App.getAppPrefs().getString(Consts.REGISTRATION_ID, null);
        return current;
    }



    public final static boolean clear() {
        try {
            Editor editor = App.getAppPrefs().edit();
            editor.remove(Consts.CURRENT_USER);
            editor.remove(Consts.ACCESS_TOKEN);
            editor.remove(Consts.TWITTER_TOKEN);
            editor.remove(Consts.TWITTER_SECRET);
            editor.remove(Consts.REGISTRATION_ID);
            return editor.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static ArrayList<Binding> getBindings() {
        Context context = App.getAppContext();
        ArrayList<Binding> bindings = null;
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = context.openFileInput(Consts.BINDINGS);
            ois = new ObjectInputStream(fis);
            bindings = (ArrayList<Binding>) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bindings == null) {
            bindings = new ArrayList<Binding>();
        }
        return bindings;
    }


    public static void setBindings(ArrayList<Binding> bindings) {
        Context context = App.getAppContext();
        FileOutputStream fis = null;
        ObjectOutputStream ois = null;
        try {
            fis = context.openFileOutput(Consts.BINDINGS, Context.MODE_PRIVATE);
            ois = new ObjectOutputStream(fis);
            ois.writeObject(bindings);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (ois != null) {
                    ois.close();
                }
            } catch (IOException e) {
                e.printStackTrace();

            }
        }
    }

    /**
     * @return T if login
     */
    public static boolean isLogin() {
        String session = AppData.getToken();
        return !TextUtils.isEmpty(session);
    }
}
