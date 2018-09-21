package com.daxiangce123.android.http;

import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.HotType;
import com.daxiangce123.android.Consts.HttpMethod;
import com.daxiangce123.android.Consts.Order;
import com.daxiangce123.android.Consts.Priority;
import com.daxiangce123.android.Consts.Sort;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.DownloadedExtra;
import com.daxiangce123.android.data.ImageSize;
import com.daxiangce123.android.data.Location;
import com.daxiangce123.android.data.json.Contacts;
import com.daxiangce123.android.manager.HttpDownloadManager;
import com.daxiangce123.android.manager.HttpUploadManager;
import com.daxiangce123.android.manager.RequestManager;
import com.daxiangce123.android.manager.UploadCancelManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.push.PushCallBack.Provider;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * TODO USE single instance!!!
 *
 * @project DaXiangCe
 * @time May 29, 2014
 */
public class ConnectBuilder {
    public static final String TAG = "ConnectBuilder";
    private static HashMap<String, String> header = null;
    private static String tempHttpToken = null;

    /**
     * @time May 29, 2014
     */
    public static void clear() {
        if (header != null) {
            header.clear();
        }
        tempHttpToken = null;
    }

    public static String corpBearer(String token) {
        StringBuffer sBuffer = new StringBuffer("Bearer ");
        sBuffer.append(token);
        return sBuffer.toString();// "Bearer " + token;
    }

    public static void init() {
        String token = AppData.getToken();
        tempHttpToken = AppData.getTempHttpToken();
        if (Utils.isEmpty(token)) {
            return;
        }
        String authorization = corpBearer(AppData.getToken());
        if (header == null) {
            header = new HashMap<String, String>();
        } else {
            header.clear();
        }
        header.put(Consts.AUTHORIZATION, authorization);
    }

    public final static HashMap<String, String> getAuthentication() {
        if (header == null || header.isEmpty()) {
            return null;
        }
        return header;
    }

    public final static String getTempHttpToken() {
        if (tempHttpToken == null) {
            tempHttpToken = AppData.getTempHttpToken();
        }
        return tempHttpToken;
    }

    public final synchronized static void setTempHttpToken(String httpToken) {
        tempHttpToken = httpToken;
        AppData.setTempHttpToken(tempHttpToken);
    }

    private static boolean checkParams(String... params) {
        for (String param : params) {
            if (Utils.isEmpty(param)) {
                LogUtil.e(TAG, "invalid parameter");
                return false;
            }
        }
        return true;
    }

    public final static boolean sso_bind(String params, String provider) {
        if (!checkParams(params)) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_BIND;
        HttpEntity entity;
        HashMap<String, String> auth = getAuthentication();
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.SSO_BIND);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setTag(params);
            info.setTag2(provider);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean sso_bind(String params) {
        return sso_bind(params, null);
    }

    public final static boolean unbindDevice() {
        return unbindDevice(Consts.UNBIND_DEVICE);
    }

    public final static boolean unbindDevice(String action) {

        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_UNBIND_DEVICE;
        try {
            ConnectInfo info = new ConnectInfo(action);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;
    }

    public final static boolean createAlbum(String params) {
        if (!checkParams(params)) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_CREATE_ALBUM;
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.CREATE_ALBUM);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setTag(params);
            // info.addHeader(name, value);
            info.addHeaders(getAuthentication());
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <pre>
     * {
     * "obj_type": String,
     * "obj_id": String,
     * "reason": String
     * }
     * </pre>
     */
    public final static boolean postReport(String params) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_POST_REPORT);
        try {
            HttpEntity entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.POST_REPORT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.addHeaders(auth);
            info.setEntity(entity);
            info.setTag(params);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean shareFile(String params, String fileId) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_SHARED_FILE);
        url.append("/" + fileId);
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.SHARED_FILE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(fileId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean shareAlbum(String params, String albumId) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_SHARED_ALBUM);
        url.append("/" + albumId);
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.SHARED_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean updateAlbum(String params, String albumId) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_UPDATE_ALBUM);
        url.append("/" + albumId);
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.UPDATE_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean setMemberRole(String params) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_SET_MEMBER_ROLE;
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.SET_MEMBER_ROLE);
            info.setURL(url);
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(params);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getMemberRole(String albumId, String userId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_MEMBER_ROLE);
        url.append("/");
        url.append(albumId);
        url.append("?uid=" + userId);

        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_MEMBER_ROLE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean joinAlbum(String inviteCode, String param) {
        return joinAlbum(null, inviteCode, param);
    }

    // public final static boolean joinAlbum(String albumId, String inviteCode,
    // String param) {
    // return joinAlbum(albumId, inviteCode, param, Consts.JOIN_ALBUM);
    // }

    public final static boolean joinAlbum(String albumId, String inviteCode, String param) {

        HashMap<String, String> auth = getAuthentication();

        if (auth == null) {
            return false;
        }

        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_JOIN_ALBUM);
        url.append("/");
        url.append(inviteCode);
        try {
            HttpEntity entity = new StringEntity(param, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.JOIN_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(inviteCode);
            info.setTag2(albumId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;

    }

    public final static boolean leaveAlbum(String albumId, String userId) {
        return leaveAlbum(albumId, userId, false);
    }

    public final static boolean leaveAlbum(String albumId, String userId, boolean block) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LEAVE_ALBUM);
        url.append("/");
        url.append(albumId);
        url.append("?uid=" + userId);
        url.append("&block=" + (block ? "y" : "n"));
        try {
            ConnectInfo info = new ConnectInfo(Consts.LEAVE_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            info.setTag(block ? "" : albumId);
            info.setTag2(userId);
            info.setTag3(albumId);
            info.setPriority(Priority.HIGH);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean deleteMember(String albumId, String userId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LEAVE_ALBUM);
        url.append("/");
        url.append(albumId);
        url.append("?uid=" + userId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.DELETE_MEMBER);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setTag2(userId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean deleteAlbum(String albumId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DELETE_ALBUM);
        url.append("/");
        url.append(albumId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.DELETE_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            info.setTag(albumId);
            // String name = "Authorization";
            // String value = "Bearer f8a4c479-1952-4f42-a49a-227792e3b85b";
            // info.addHeader(name, value);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;

    }

    public final static boolean getFileInfo(String fileId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_FILE_INFO);
        url.append("/" + fileId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_FILE_INFO);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getActivtyPage(String currentDate, String versionName) {
        StringBuilder url = new StringBuilder(Consts.URL_GET_ACTIVITY_PAGE);
        url.append("&date=" + currentDate);
        url.append("&v_name=" + versionName);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_ACTIVITY_PAGE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "url--getActivtyPage : " + url + " -- info " + info.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    public final static boolean getRecommendedApps(String type, String versionName) {
        StringBuilder url = new StringBuilder(Consts.URL_RECOMMEND_APPS_STAUTS);
        url.append("?type=" + type);
        url.append("&v_name=" + versionName);
        if (App.DEBUG) {
            LogUtil.d(TAG, "url--getRecommandApps : " + url);
        }
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_RECOMMEND_APP_STATUS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "url--getRecommandApps : " + url + " -- info " + info.toString());
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getPinToWEB(String pinCode) {
        StringBuilder url = new StringBuilder("http://vd.ppickup.com/send.php");
        url.append("?k=" + pinCode);
        if (App.DEBUG) {
            LogUtil.d(TAG, "url  --- getPinToWEB: " + url);
        }
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_PIN_TO_WEB);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(pinCode);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getAlbumAct(String albumId, String userId) {

        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_ACTIVITY);
        url.append("/");
        url.append(albumId);
        url.append("?uid=" + userId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_ALBUM_ACT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(userId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean checkAlbumAccessControl(String albumId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_CHECK_ALBUN_ACCESS_CONTROL);
        url.append("/" + albumId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.CHECK_ALBUM_ACCESS_CONTROL);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // public final static boolean checkAlbumAccessControl(String albumId,
    // String accessToken) {
    // if (Utils.existsEmpty(albumId, accessToken)) {
    // return false;
    // }
    // String bearer = "Bearer " + accessToken;
    // HashMap<String, String> auth = new HashMap<String, String>();
    // auth.put(Consts.AUTHORIZATION, bearer);
    //
    // StringBuilder url = new StringBuilder(Consts.HOST
    // + Consts.METHOD_CHECK_ALBUN_ACCESS_CONTROL);
    // url.append("/" + albumId);
    // try {
    // ConnectInfo info = new ConnectInfo(
    // Consts.CHECK_ALBUM_TEMP_ACCESS_CONTROL);
    // info.setURL(url.toString());
    // info.setMethod(HttpMethod.GET);
    // info.addHeaders(auth);
    // info.setTag(albumId);
    // RequestManager.sharedInstance().addConnect(info);
    // return true;
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return false;
    // }

    public final static boolean resetMemberPermission(String albumId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_RESET_MEMBER_PERMISSION);
        url.append("/" + albumId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.RESET_MEMBER_PERMISSION);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getUserInfo(String userId) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        if (App.DEBUG) {
            LogUtil.d(TAG, "get_user_info userId=" + userId);
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_USER_INFO);
        url.append("/" + userId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_USER_INFO);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getMineInfo() {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_MINE_INFO;
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_MIME_INFO);
            info.setURL(url);
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean updateMineInfo(String params) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_MINE_INFO;
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);

            ConnectInfo info = new ConnectInfo(Consts.UPDATE_MIME_INFO);
            info.setURL(url);
            info.setMethod(HttpMethod.PUT);
            info.setEntity(entity);
            info.setTag(params);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * Consts.LIST_ALBUM
     *
     * @return
     */
    public final static boolean listAlbum() {
        return listAlbum(0, 100, Sort.BY_MOD_DATE, Order.DESC);
    }

    public final static boolean listAlbum(int startPos, int limit, Sort sort, Order order) {
        return listAlbum(startPos, 100, Sort.BY_MOD_DATE, Order.DESC, Consts.LIST_ALBUM);
    }

    /**
     * Consts.LIST_ALBUM
     *
     * @param startPos
     * @param limit
     * @param sort
     * @param order
     * @return
     */
    public final static boolean listAlbum(int startPos, int limit, Sort sort, Order order, String action) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "TEMPP listAlbum	" + System.currentTimeMillis());
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_ALBUM);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        if (sort == null) {
            sort = Sort.BY_MOD_DATE;
        }
        url.append("&sort=" + sort.toString().toLowerCase(Locale.US));
        if (order == null) {
            order = Order.DESC;
        }
        url.append("&order=" + order.toString().toLowerCase(Locale.US));
        try {
            ConnectInfo info = new ConnectInfo(action);
            if (App.DEBUG) {
                LogUtil.d(TAG, "--list album -- " + action);
            }
            if (startPos == 0) {
                info.setTag2(Consts.FROM_STARTER);
            }
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean listAlbum(String uid, int startPos, int limit, Sort sort, Order order) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_ALBUM);
        url.append("?uid=" + uid);

        if (startPos < 0) {
            startPos = 0;
        }
        url.append("&pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        if (sort == null) {
            sort = Sort.BY_MOD_DATE;
        }
        url.append("&sort=" + sort.toString().toLowerCase(Locale.US));
        if (order == null) {
            order = Order.DESC;
        }
        url.append("&order=" + order.toString().toLowerCase(Locale.US));
        try {
            ConnectInfo info = new ConnectInfo(Consts.LIST_OTHER_USER_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(uid);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean getBanner(String bannerId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_BANNER);
        url.append("/");
        url.append(bannerId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_BANNER);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean listBanner() {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_BANNER);

        try {
            ConnectInfo info = new ConnectInfo(Consts.LIST_BANNER);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;
    }

    public final static boolean getAlbumCover(List<AlbumEntity> albumList) {
        if (Utils.isEmpty(albumList)) {
            return false;
        }
        try {
            for (AlbumEntity albumEntity : albumList) {
                if (albumEntity == null) {
                    continue;
                }
                String id = albumEntity.getId();
                if (Utils.isEmpty(id)) {
                    continue;
                }
                // getAlbumCover(id);
                getAlbumCoverId(id);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean getAlbumCoverId(String albumId) {
        return getAlbumCoverId(albumId, Consts.GET_ALBUM_COVER);
    }

    public final static boolean getAlbumCoverId(String albumId, String action) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_THUM_ID);
        url.append("/");
        url.append(albumId);
        try {
            ConnectInfo info = new ConnectInfo(action);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
//            if (App.DEBUG) {
//                LogUtil.d(TAG, " getAlbumCoverId -- url " + url);
//            }
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean getAlbumSampleCover(String albumId) {
        return getAlbumSample(albumId, Consts.GET_ALBUM_SAMPLE_COVER);
    }

    public final static boolean getAlbumCover(String albumId) {
        return getAlbumItems(albumId, 0, 1, null, Order.DESC, Consts.GET_ALBUM_COVER);
    }

    public final static boolean getAlbumCover(String albumId, String action) {
        return getAlbumItems(albumId, 0, 1, null, Order.DESC, action);
    }

    // public final static boolean getNonMemberAlbumItems(String albumId) {
    // return getNonMemberAlbumItems(albumId, 0, 100, Sort.BY_MOD_DATE,
    // Order.DESC, Consts.GET_NON_MEMBER_ALBUM_ITEMS);
    // }
    //
    // public final static boolean getNonMemberAlbumItems(String albumId,
    // int startPos, int limit, Sort sort, Order order) {
    // return getNonMemberAlbumItems(albumId, 0, 100, Sort.BY_MOD_DATE,
    // Order.DESC, Consts.GET_NON_MEMBER_ALBUM_ITEMS);
    // }
    //
    // public final static boolean getNonMemberAlbumItems(String albumId,
    // int startPos, int limit, Sort sort, Order order, String action) {
    //
    // HashMap<String, String> auth = getAuthentication();
    // if (auth == null) {
    // return false;
    // }
    // if (App.DEBUG) {
    // LogUtil.d(TAG, "albumid " + albumId + " token" + auth);
    // }
    // StringBuilder url = new StringBuilder(Consts.HOST
    // + Consts.METHOD_GET_NON_MEMBER_ALBUM_ITEMS);
    // url.append("/");
    // url.append(albumId);
    //
    // if (startPos < 0) {
    // startPos = 0;
    // }
    // url.append("?pos=" + startPos);
    // if (limit <= 0 || limit > 100) {
    // limit = 100;
    // }
    // url.append("&limit=" + limit);
    // if (sort == null) {
    // sort = Sort.BY_MOD_DATE;
    // }
    // url.append("&sort=" + sort.toString().toLowerCase(Locale.US));
    // if (order == null) {
    // order = Order.DESC;
    // }
    // url.append("&order=" + order.toString().toLowerCase(Locale.US));
    // try {
    // ConnectInfo info = new ConnectInfo(action);
    // info.setURL(url.toString());
    // info.setMethod(HttpMethod.GET);
    // info.addHeaders(auth);
    // info.setTag(albumId);
    // info.setObj(startPos);
    // // String name = "Authorization";
    // // String value = "Bearer f8a4c479-1952-4f42-a49a-227792e3b85b";
    // // info.addHeader(name, value);
    // RequestManager.sharedInstance().addConnect(info);
    // return true;
    // } catch (Exception e) {
    // e.printStackTrace();
    // LogUtil.d(TAG, "" + e.toString());
    // }
    // return false;
    // }

    /**
     * 0-100
     *
     * @param albumId
     * @return
     * @time May 9, 2014
     */
    public final static boolean getAlbumItems(String albumId) {
        return getAlbumItems(albumId, 0, 100, Consts.FileSort.TIMELINE_SORT.getServer_sort(), Order.DESC, Consts.GET_ALBUM_ITEMS);
    }

    public final static boolean getAlbumItems(String albumId, int startPos, int limit, Consts.FileSort sort, Order order) {
        return getAlbumItems(albumId, startPos, limit, sort.getServer_sort(), order, Consts.GET_ALBUM_ITEMS);
    }

    public final static boolean getAlbumUpdateItems(String albumId, int startPos, int limit, Consts.FileSort sort, Order order) {
        return getAlbumItems(albumId, startPos, limit, sort.getServer_sort(), order, Consts.GET_ALBUM_UPDATE_ITEMS);
    }

    public final static boolean getAlbumItems(String albumId, int startPos, int limit, String sort, Order order, String action) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_ITEMS);
        url.append("/");
        url.append(albumId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        if (sort == null) {
            sort = Consts.FileSort.TIMELINE_SORT.getServer_sort();
        }
        url.append("&sort=" + sort.toString().toLowerCase(Locale.US));
        if (order == null) {
            order = Order.DESC;
        }
        url.append("&order=" + order.toString().toLowerCase(Locale.US));
        if (App.DEBUG) {
            LogUtil.v(TAG, url.toString());
        }
        try {
            ConnectInfo info = new ConnectInfo(action);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            // info.setObj(Integer.valueOf(startPos));
            info.setObj(Integer.valueOf(startPos));
            // String name = "Authorization";
            // String value = "Bearer f8a4c479-1952-4f42-a49a-227792e3b85b";
            // info.addHeader(name, value);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean getUserOfAlbumItems(String albumId, String uid, int startPos, int limit, Sort sort, Order order) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_ITEMS);
        url.append("/");
        url.append(albumId);
        url.append("?uid=" + uid);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("&pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        if (sort == null) {
            sort = Sort.BY_MOD_DATE;
        }
        url.append("&sort=" + sort.toString().toLowerCase(Locale.US));
        if (order == null) {
            order = Order.DESC;
        }
        url.append("&order=" + order.toString().toLowerCase(Locale.US));
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_USER_OF_ALBUM_ITEMS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setTag2(uid);
            info.setObj(startPos);
            // String name = "Authorization";
            // String value = "Bearer f8a4c479-1952-4f42-a49a-227792e3b85b";
            // info.addHeader(name, value);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean getLikeList(String fileId) {
        return getLikeList(fileId, 0, 5);
    }

    public final static boolean getLikeList(String fileId, int startPos, int limit) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_LIKE);
        url.append("/");
        url.append(fileId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_LIKE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * <pre>
     * {
     * "msg" : String
     * "reply_to_user" : String
     * }
     * </pre>
     */

    public final static boolean postComment(String fileId, String param) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_POST_COMMENTS);
        url.append("/");
        url.append(fileId);
        try {
            HttpEntity entity = new StringEntity(param, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.POST_COMMENT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.addHeaders(auth);
            info.setTag(fileId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * DELETE /file/like/{file_id}
     */
    public final static boolean dislikeFile(String fileId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DISLIKE_FILE);
        url.append("/");
        url.append(fileId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.DISLIKE_FILE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * POST /file/like/{file_id}
     */
    public final static boolean likeFile(String fileId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIKE_FILE);
        url.append("/");
        url.append(fileId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.LIKE_FILE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.addHeaders(auth);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /file/like/{file_id}?uid={user_id}
     */
    public final static boolean hasLiked(String uid, String fileId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIKE_FILE);
        url.append("/");
        url.append(fileId);
        url.append("?uid=");
        url.append(uid);
        try {
            ConnectInfo info = new ConnectInfo(Consts.HAS_LIKED);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean deleteComment(String commentId) {

        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DELETE_COMMENTS);
        url.append("/");
        url.append(commentId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.DELETE_COMMENT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            info.setTag(commentId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean getComments(String fileId) {
        return getComments(fileId, 0, 30, null);
    }

    public final static boolean getComments(String fileId, int startPos, int limit, String tag2) {
        HashMap<String, String> auth = getAuthentication();
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_COMMENTS);
        url.append("/");
        url.append(fileId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        url.append("&sort=by_create_date&order=desc");
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_COMMENTS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(fileId);
            info.setTag2(tag2);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public final static boolean getAlbumMembersDESC(String albumId, int startPos, int limit) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_MEMBERS);
        url.append("/");
        url.append(albumId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        // url.append("&sort=by_create_date");
        url.append("&order=" + Order.DESC.toString().toLowerCase(Locale.US));
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_ALBUM_MEMBERS_DESC);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static boolean getAlbumMembers(String albumId) {
        return getAlbumMembers(albumId, 0, 30, Order.ASC);
    }

    public final static boolean getAlbumMembers(String albumId, Order order) {
        return getAlbumMembers(albumId, 0, 30, order);
    }

    public final static boolean getAlbumMembers(String albumId, int startPos, int limit) {
        return getAlbumMembers(albumId, startPos, limit, Order.ASC);
    }

    public final static boolean getAlbumMembers(String albumId, int startPos, int limit, Order order) {

        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_MEMBERS);
        url.append("/");
        url.append(albumId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        // url.append("&sort=by_create_date");
        url.append("&order=" + order.toString().toLowerCase(Locale.US));
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_ALBUM_MEMBERS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;

    }

    public final static String getAvaterUrl(String userId) {
        if (Utils.isEmpty(userId)) {
            return null;
        }
        StringBuilder url = null;
        if (App.NO_HTTPS) {
            url = new StringBuilder(Consts.HOST_HTTP + Consts.METHOD_DOWNLOAD_AVATAR);
        } else {
            url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DOWNLOAD_AVATAR);
        }
        url.append("/");
        url.append(userId);
        return url.toString();
    }

    public final static boolean getAvatar(String localPath, String userId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        try {
            String tmpPath = localPath + "-" + System.currentTimeMillis() + ".tmp";
            DownloadEntity downloadEntity = new DownloadEntity(localPath, tmpPath, 0);
            ConnectInfo info = new ConnectInfo(Consts.DOWNLOAD_FILE);
            info.setURL(getAvaterUrl(userId));
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(localPath);
            info.setWriter(downloadEntity);
            HttpDownloadManager.instance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

        return false;
    }

    public final static boolean getAlbumInfo(String albumId) {
        // HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_INFO);
        url.append("/" + albumId);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_ALBUM_INFO);
            info.setURL(url.toString());
            if (App.DEBUG) {
                LogUtil.d(TAG, "getAlbumInfo  -- url  " + url);
            }
            info.setMethod(HttpMethod.GET);
            // info.addHeaders(auth);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "getAlbumInfo  -- ConnectInfo  " + info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <pre>
     * {
     *     "album": String,
     *     "title": String,
     *     "name": String,
     *     "size": Number,
     *     "digest" : String, //hash
     *     "mime_type": String, //NO
     *     "batch_id" : String,
     *     "seq_num" : Number
     * }
     * </pre>
     *
     * @return
     * @time Mar 3, 2014
     */
    public static final boolean createFile(String params, String localPath) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "createFile:\n" + params);
        }
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        ConnectInfo info = new ConnectInfo(Consts.CREATE_FILE);
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_CREATE_NEW_FILE);
        try {
            HttpEntity entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            info.setURL(url.toString());
            info.setEntity(entity);
            info.setMethod(HttpMethod.POST);
            info.addHeaders(auth);
            info.setTag(params);
            info.setTag2(localPath);
            info.setFakeId(Parser.parseFileFakeId(params));
            if (!UploadCancelManager.sharedInstance().checkJosnParams(params)) {
                RequestManager.sharedInstance().addConnect(info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * action: {@link Consts#UPLOAD_FILE}
     *
     * @param localPath
     * @param fileId
     * @return
     * @time Mar 4, 2014
     */
    public static final boolean uploadFile(String localPath, String fileId, String batchId, long size, String fileEntity) {
        if (!FileUtil.exists(localPath)) {
            return false;
        }
        if (!checkParams(fileId)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        try {
            HttpEntity entity = new UploadEntity(localPath, 0);
            ConnectInfo info = new ConnectInfo(Consts.UPLOAD_FILE);
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_UPLOAD_CONTENT);
            url.append("/" + fileId);
            info.addHeaders(auth);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.setTag(localPath);
            info.setTag2(batchId);
            info.setTag3(fileEntity);
            info.setObj(fileId);
            info.setEntity(entity);
            info.setFakeId(Parser.parseFileFakeIdFromFileEntity(fileEntity));
            info.addHeader(Consts.CONTENT_RANGE, "bytes 0-" + (size - 1) + "/" + size);
            if (!UploadCancelManager.sharedInstance().checkFileEntity(fileEntity)) {
                HttpUploadManager.instance().addConnect(info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * action: {@link Consts#SET_ALBUM_THUMB}
     *
     * @param albumId
     * @param fileId
     * @return
     * @time jul 25, 2014
     */

    public static final boolean setAlbumThum(String albumId, String fileId) {
        if (!checkParams(albumId)) {
            return false;
        }

        if (!checkParams(fileId)) {
            return false;
        }

        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }

        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_SET_ALBUM_THUM);
        url.append("/");
        url.append(albumId);
        url.append("?file=" + fileId);

        try {
            ConnectInfo info = new ConnectInfo(Consts.SET_ALBUM_THUMB);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setTag2(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static final boolean setAvatar(String localPath) {
        if (!FileUtil.exists(localPath)) {
            return false;
        }

        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        try {
            long size = FileUtil.size(localPath);
            HttpEntity entity = new UploadEntity(localPath, 0);
            ConnectInfo info = new ConnectInfo(Consts.SET_AVATAR);
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_SET_AVATAR);
            info.addHeaders(auth);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.setTag(localPath);
            info.setEntity(entity);
            info.addHeader(Consts.CONTENT_RANGE, "bytes 0-" + (size - 1) + "/" + size);
            HttpUploadManager.instance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "SET_AVATER " + info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static final String getFileUrl(String fileId, boolean formCOS) {
        if (Utils.isEmpty(fileId)) {
            return null;
        }
        StringBuilder url;
        if (formCOS) {
            url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_FIEL_DWRUL);
        } else {
            url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DOWNLOAD_FILE);
        }
        url.append("/" + fileId);
        return url.toString();
    }

    /**
     * use in PhotoViewerFragment::save()
     *
     * @param params
     * @param fileId
     * @return
     */
    public final static boolean downloadFileCount(String params, String fileId) {
        if (!checkParams(params)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DOWNLOAD_FILE_COUNT);
        url.append("/" + fileId);
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.DOWNLOADED_FILE);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.PUT);
            info.addHeaders(auth);
            info.setTag(fileId);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static final boolean downloadFile(String fileId, String token, String localPath, long fileSize, String params) {
        return getDwurl(fileId, new DownloadedExtra(token, fileSize, localPath, params));
    }


    /**
     * use in download gif
     *
     * @param fileId
     * @param localPath
     * @param token
     * @param fileSize
     * @return
     */
    public static final boolean downloadFile(String url, String fileId, String localPath, String token, long fileSize) {
        if (!checkParams(url, fileId)) {
            return false;
        }
        /* if (auth == null) {return false;}*/
        try {
            String tmpPath = localPath + "-" + System.currentTimeMillis() + ".tmp";
            DownloadEntity writer = new DownloadEntity(localPath, tmpPath, fileSize);

            // TODO 需要修改这个
            ConnectInfo info = new ConnectInfo(Consts.DOWNLOAD_FILE);
            info.setWriter(writer);
            info.setURL(url);
            info.setMethod(HttpMethod.GET);
            info.setTag(localPath);
            info.setTag2(fileId);
            HttpDownloadManager.instance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "info	" + info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <pre>
     *  	GET /thumb/{file_id}?dim={width_x_height}
     * </pre>
     */
    public static final String getThumbUrl(String fileId, ImageSize size) {
        if (Utils.existsEmpty(fileId) || size == null) {
            return null;
        }
        StringBuilder url;
        if (App.NO_HTTPS) {
            url = new StringBuilder(Consts.HOST_HTTP + Consts.METHOD_DOWNLOAD_THUMB);
        } else {
            url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DOWNLOAD_THUMB);
        }

        url.append("/" + fileId);
        url.append("?dim=" + size.toURI());
//        if (App.DEBUG) {
//            LogUtil.d(TAG, " -- getThumbUrl -- url " + url);
//        }
        return url.toString();
    }

    private static final String appendToken(String token) {
        if (Utils.isEmpty(token)) {
            return "";
        }
        StringBuilder url = new StringBuilder(Consts.ACCESS_TOKEN_TAG);
        url.append(token);
        return url.toString();
    }

    /**
     * <pre>
     * DELETE /file/{file_id}
     * </pre>
     */
    public static final boolean deleteFile(String fileId) {
        if (Utils.existsEmpty(fileId)) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DELETE_FILE);
            url.append("/" + fileId);
            ConnectInfo info = new ConnectInfo(Consts.DELETE_FILE);
            info.addHeaders(auth);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.setTag(fileId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <pre>
     * GET /event?limit={n}
     * </pre>
     * <p/>
     * Action is {@linkplain Consts#GET_EVENTS}
     *
     * @param limit
     * @return
     * @time Mar 28, 2014
     */
    public static final ConnectInfo getlistEventInfo(int limit) {
        if (limit < 0) {
            return null;
        }
        if (limit > 20) {
            limit = 20;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return null;
        }
        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_EVENTS);
            url.append("/?limit=" + limit);
            ConnectInfo info = new ConnectInfo(Consts.GET_EVENTS);
            info.addHeaders(auth);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * curl https://api.cliq.com/1.0/user/geo \ -H
     * "Authorization: Bearer {access_token}" \ -X POST -d
     * '{"latitude":31.23513, "longitude":121.52759,"accuracy":100}'
     *
     * @param location
     * @return
     * @time 2014-6-1
     */
    public static final boolean updateUserGeo(Location location) {
        if (location == null) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_UPDATE_USER_GEO);
        JSONObject jo = new JSONObject();
        jo.put(Consts.LATITUDE, location.getLat());
        jo.put(Consts.LONGITUDE, location.getLon());
        // jo.put(Consts.ACCURACY, location.getAccuracy());
        ConnectInfo info = new ConnectInfo(Consts.UPDATE_USER_GEO);
        try {
            StringEntity entity = new StringEntity(jo.toString(), Consts.CHARSET_UTF_8);
            info.setEntity(entity);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.addHeaders(getAuthentication());
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "updateUserGeo()	" + info);
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean listHotAlbum(HotType type, String action) {
        HashMap<String, String> auth = getAuthentication();
        if (App.DEBUG) {
            LogUtil.d(TAG, "type	" + type + "	" + auth);
        }
        // if (auth == null) {
        // return false;
        // }
        if (type == null) {
            return false;
        }
        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_HOT_ALBUM);

            url.append("?type=" + type.getType());

            ConnectInfo info = new ConnectInfo(action);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);

            if (App.DEBUG) {
                LogUtil.d(TAG, "listHotAlbum() " + info);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * default limit 40
     */
    public static boolean listNearByAlbum(int limit) {
        return listNearByAlbum(0, limit, Sort.BY_MOD_DATE, Order.DESC);
    }

    /**
     * GET /album/nearby?pos={x}&limit={n}&sort={sort_by}&order={order}
     *
     * @return
     * @time 2014-6-1
     */
    public static boolean listNearByAlbum(int startPos, int limit, Sort sort, Order order) {
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_NEARBY);
            if (startPos < 0) {
                startPos = 0;
            }
            url.append("?pos=" + startPos);
            if (limit <= 0 || limit > 100) {
                limit = 100;
            }
            url.append("&limit=" + limit);
            if (sort == null) {
                sort = Sort.BY_MOD_DATE;
            }
            url.append("&sort=" + sort.toString().toLowerCase(Locale.ENGLISH));
            if (order == null) {
                order = Order.DESC;
            }
            url.append("&order=" + order.toString().toLowerCase(Locale.US));
            ConnectInfo info = new ConnectInfo(Consts.LIST_NEARBY_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            if (startPos == 0) {
                info.setTag2(Consts.FROM_STARTER);
            }
            RequestManager.sharedInstance().addConnect(info);

            if (App.DEBUG) {
                LogUtil.d(TAG, "---------------------------listNearByAlbum\n " + info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /search/album?q={query_string}&pos={x}&limit={n}
     */

    public final static boolean searchAlbum(String query, int startPos, int limit) {
        if (Utils.isEmpty(query)) {
            return false;
        }
        // if (getAuthentication() == null) {
        // return false;
        // }
        // String tag =
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_SEARCH_ALBUM);
        url.append("?q=" + Utils.encodeURL(query));
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("&pos=" + startPos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        try {
            url.append("&limit=" + limit);
            ConnectInfo info = new ConnectInfo(Consts.SEARCH_ALBUM);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            // info.addHeaders(getAuthentication());
            info.setTag(query);
            if (startPos == 0) {
                info.setTag2(Consts.FROM_STARTER);
            }
            RequestManager.sharedInstance().addConnect(info);

            if (App.DEBUG) {
                LogUtil.d(TAG, "---------------------------searchAlbum\n " + info);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /album/thumb/{album_id}?dim={width_x_height}
     */
    public final static boolean getAlbumThumb(String albumId, String localPath, ImageSize size) {
        if (Utils.existsEmpty(albumId, localPath) || size == null) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        try {
            String tmpPath = localPath + "-" + System.currentTimeMillis() + ".tmp";
            DownloadEntity writer = new DownloadEntity(localPath, tmpPath, 0);
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_ALBUM_THUMB);
            url.append("/" + albumId);
            url.append("?dim=" + size.toURI());
            ConnectInfo info = new ConnectInfo(Consts.DOWNLOAD_FILE);
            info.addHeaders(auth);
            info.setWriter(writer);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(localPath);
            info.setTag2(albumId);
            HttpDownloadManager.instance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /album/samples/{album_id}
     */
    public final static boolean getAlbumSample(String albumId) {
        return getAlbumSample(albumId, Consts.GET_ALBUM_SAMPLES);
    }

    public final static boolean getAlbumSample(String albumId, String action) {
        if (Utils.isEmpty(albumId)) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_ALBUM_SAMPLES);
            url.append("/" + albumId);
            ConnectInfo info = new ConnectInfo(action);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(getAuthentication());
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.d(TAG, "---------------------------getAlbumSample " + albumId);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * GET /link/{link}
     */
    public final static boolean getTempTokenByLink(String link, String objId) {
        if (Utils.isEmpty(link)) {
            return false;
        }
        // if (getAuthentication() == null) {
        // return false;
        // }
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_TEMP_TOKEN_BY_LINK);
            info.setURL(Consts.HOST_HTTPS + Consts.METHOD_ACCESS_BY_LINK + "/" + link);
            // info.addHeaders(getAuthentication());
            info.setTag(link);
            info.setTag2(objId);
            info.setMethod(HttpMethod.GET);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * GET /banner/content/{banner_id}
     *
     * @param bannerId
     */
    public final static String getBannerUrl(String bannerId) {
        if (Utils.isEmpty(bannerId)) {
            return null;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_DOWN_BANNER);
        url.append("/" + bannerId);
        return url.toString();
    }

    public final static boolean finishBatch(String albumId, String batchId) {
        if (Utils.existsEmpty(albumId, batchId)) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder(Consts.HOST_HTTPS);
        sb.append(Consts.METHOD_FINISH_BATCH);

        JSONObject jo = new JSONObject();
        jo.put(Consts.ALBUM, albumId);
        jo.put(Consts.BATCH_ID, batchId);
        try {
            StringEntity entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.METHOD_FINISH_BATCH);
            info.addHeaders(getAuthentication());
            info.setMethod(HttpMethod.PUT);
            info.setURL(sb.toString());
            info.setEntity(entity);
            info.setTag2(batchId);
            info.setTag(albumId);
            RequestManager.sharedInstance().addConnect(info);

            if (App.DEBUG) {
                LogUtil.d(TAG, "-----------------------finishBatch\n	" + info);
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean registerNotificationId(String regId, String channelId, Provider provider) {
        if (Utils.existsEmpty(regId) || provider == null) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder(Consts.HOST_HTTPS);
        sb.append(Consts.METHOD_REGI_PUSH);

        JSONObject jo = new JSONObject();
        jo.put(Consts.REGISTRATION_ID, regId);
        jo.put(Consts.PROVIDER, provider.getName());
        jo.put(Consts.OS_TYPE, "android");
        if (provider == Provider.BAIDU) {
            jo.put(Consts.CHANNEL_ID, channelId);
        }
        try {
            StringEntity entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.REGI_PUSH);
            info.addHeaders(getAuthentication());
            info.setMethod(HttpMethod.POST);
            info.setURL(sb.toString());
            info.setEntity(entity);
            info.setTag(regId);
            info.setTag2(provider.getName());
            if (App.DEBUG) {
                LogUtil.e(TAG, "registerNotificationId	" + info);
            }

            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * DELETE /user/push/{registration_id}
     */
    public final static boolean unregisterNotificationId(String regId) {
        if (Utils.existsEmpty(regId)) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder(Consts.HOST_HTTPS);
        sb.append(Consts.METHOD_REGI_PUSH);
        sb.append("/");
        sb.append(regId);
        try {
            HashMap<String, String> map = new HashMap<String, String>();
            map.putAll(getAuthentication());
            ConnectInfo info = new ConnectInfo(Consts.UNREGI_PUSH);
            info.addHeaders(map);
            info.setMethod(HttpMethod.DELETE);
            info.setURL(sb.toString());
            info.setTag2(regId);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /*
     * DELETE /user/push/{registration_id}
     */
    public final static boolean noPush(String albumId, boolean close) {
        if (Utils.existsEmpty(albumId)) {
            return false;
        }
        if (getAuthentication() == null) {
            return false;
        }
        StringBuilder sb = new StringBuilder(Consts.HOST_HTTPS);
        sb.append(Consts.METHOD_NO_PUSH);
        sb.append("/");
        sb.append(albumId);
        try {
            JSONObject jo = new JSONObject();
            jo.put(Consts.NO_PUSH, close);
            ConnectInfo info = new ConnectInfo(Consts.NO_PUSH);
            info.addHeaders(getAuthentication());
            info.setMethod(HttpMethod.PUT);
            info.setURL(sb.toString());
            info.setEntity(new StringEntity(jo.toString(), Consts.CHARSET_UTF_8));
            info.setTag(albumId);
            info.setTag2(jo.toJSONString());
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public final static boolean wxAccessToken(String url) {
        try {
            ConnectInfo info = new ConnectInfo(Consts.WX_ACCESS_TOEKN);
            info.setMethod(HttpMethod.GET);
            info.setURL(url);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean wxUnionId(String url) {
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_WX_UNION_ID);
            info.setMethod(HttpMethod.GET);
            info.setURL(url);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * request a temp token instead of http
     */
    public final static void requestAccessToken() {
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_METHOD_TEMP_TOKEN;
        ConnectInfo info = new ConnectInfo(Consts.TEMP_ACCESS_TOKEN);
        info.addHeaders(getAuthentication());
        info.setMethod(HttpMethod.PUT);
        info.setURL(url);
        RequestManager.sharedInstance().addConnect(info);
    }

    public final static boolean getDwurl(String fileID, Object obj) {
        if (fileID == null) {
            return false;
        }
        HashMap<String, String> auth = getAuthentication();
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_FIEL_DWRUL);
        url.append("/");
        url.append(fileID);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_DWURL);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(fileID);
            info.setObj(obj);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public final static void getBatcheList(String albumId, int startPos, int limit) {
        if (albumId == null) {
            return;
        }
        HashMap<String, String> auth = getAuthentication();
        // if (auth == null) {
        // return false;
        // }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_BATCHES);
        url.append("/");
        url.append(albumId);
        if (startPos < 0) {
            startPos = 0;
        }
        url.append("?pos=" + startPos);
        url.append("&limit=" + limit);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_BATCHES);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(albumId);
            info.setObj(Integer.valueOf(startPos));
            RequestManager.sharedInstance().addConnect(info);

        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
    }

    /**
     * getBindList for settings
     *
     * @return false if uesr not login
     */
    public static boolean getBindList(String userId) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return false;
        }
        StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_LIST_BINDINGS);
        try {
            ConnectInfo info = new ConnectInfo(Consts.LIST_BINDINGS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(userId);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.v(TAG, "getBindList error");
        }
        return true;

    }

    /**
     * use for request region list for server
     *
     * @param postion
     * @param limit
     */
    public static void getRegionInfo(int postion, int limit) {
        StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_LIST_REGION);
        try {
            ConnectInfo info = new ConnectInfo(Consts.LIST_REGION);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.v(TAG, "getRegionInfo error!");
        }
    }

    /**
     * reset password
     *
     * @param mobile
     * @param email        this is not used for now ,so it is null normally
     * @param confirmation
     * @param password
     */
    public static void resetPassword(String mobile, String email, String confirmation, String password) {
        if (confirmation == null || password == null) {
            return;
        }
        StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_RESET_PASSWORD);
        HttpEntity entity;
        JSONObject jo = new JSONObject();
        if (mobile != null) {
            jo.put(Consts.MOBILE, mobile);
        }
        if (email != null) {
            jo.put(Consts.EMAIL, email);
        }
        jo.put(Consts.CONFIRMATION, confirmation);
        jo.put(Consts.PASSWORD, password);
        if (App.DEBUG) {
            LogUtil.v(TAG, "resetPassword=" + jo.toJSONString());
        }
        try {
            ConnectInfo info = new ConnectInfo(Consts.RESET_PASSWORD);
            entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            info.setURL(url.toString());
            info.setEntity(entity);
            info.setMethod(HttpMethod.POST);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.v(TAG, "resetPassword error!");
        }
    }

    public static void sso_unbind(Consts.PROVIDERS provider) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null || provider == null) {
            return;
        }
        try {
            StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_UNBIND_SSO + "/" + provider.toString());
            ConnectInfo info = new ConnectInfo(Consts.UNBIND_SSO);
            info.setURL(url.toString());
            info.addHeaders(auth);
            info.setMethod(HttpMethod.DELETE);
            info.setTag(provider.toString());
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param mobile   new mobile
     * @param password new password
     */
    public static void changeMobile(String mobile, String password, String confirmation) {
        createNewUser(mobile, password, confirmation, null);

    }


    /**
     * use for
     * 1 create new mobile account from server
     * 2 change mobile number,every time change mobile must change password
     * 3 bind mobile
     *
     * @param mobile
     * @param password
     * @param confirmation
     * @param nikeName
     */
    public static void createNewUser(String mobile, String password, String confirmation, String nikeName) {
        if (mobile == null || confirmation == null) {
            return;
        }
        HashMap<String, String> auth = getAuthentication();
        StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_CREATE_NEW_USER);
        HttpEntity entity;
        JSONObject jo = new JSONObject();
        try {
            if (nikeName != null) {
                jo.put(Consts.NAME, nikeName);
            }
            jo.put(Consts.MOBILE, mobile);
            jo.put(Consts.CONFIRMATION, confirmation);
            if (password != null) {
                jo.put(Consts.PASSWORD, password);
            }
            if (App.DEBUG) {
                LogUtil.v(TAG, jo.toString());
//                LogUtil.v(TAG, auth.toString());
            }
            ConnectInfo info = new ConnectInfo(Consts.CREATE_NEW_USER);
            entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            info.addHeaders(auth);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * request server to send a confirmation code to mobile
     * use for
     * 1. new user register
     * 2. user recovery password(reset password)
     * 3. bind new mobile
     * 4. change mobile
     * if PURPOZE set to auth existed
     *
     * @param PURPOZE
     * @param mobile
     * @param uniqId
     */
    public static void requestConfirmationCode(Consts.PURPOZE PURPOZE, String mobile, long uniqId) {
        if (mobile == null || PURPOZE == null) {
            return;
        }
        HashMap<String, String> auth = getAuthentication();
        StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHDO_REQUEST_CONFIRMATION);
        HttpEntity entity;
        JSONObject jo = new JSONObject();
        jo.put(Consts.MOBILE, mobile);
        jo.put(Consts.PURPOSE, PURPOZE);
        try {
            ConnectInfo info = new ConnectInfo(Consts.REQUEST_CONFIRMATION);
            entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            info.setURL(url.toString());
            info.setEntity(entity);
            info.setMethod(HttpMethod.POST);
            info.setUniqId(uniqId);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.v(TAG, info.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.v(TAG, "requestConfirmationCode");
        }
    }


    //TODO Mobile login interrupt by error 401 to get user id
    public static boolean getUserIdByAuthentic(String params) {
        if (!checkParams(params)) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_USER_ID_BY_AUTHENTIC;
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.GET_USER_ID_BY_AUTHENTIC);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setTag(params);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    //TODO third-party account interrupt by error 401 to get user id
    public static boolean getUserIdByOAuth(String params) {
        if (!checkParams(params)) {
            return false;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_USER_ID_BY_OAUTH;
        HttpEntity entity;
        try {
            entity = new StringEntity(params, Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.GET_USER_ID_BY_OAUTH);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setTag(params);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }


    public static boolean getUsetSuspendedInfo(String uid) {
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_USER_SUSPENDED_INFO);
        url.append("/");
        url.append(uid);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_USER_SUSPENDED_INFO);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(uid);
            RequestManager.sharedInstance().addConnect(info);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
        return false;
    }

    public static void mobileUserLogine(String mobile, String password, long uniqId) {
        if (password == null || mobile == null) {
            return;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_MOBILE_LOGIN;
        HttpEntity entity;
        try {
            JSONObject jo = new JSONObject();
            jo.put(Consts.MOBILE, mobile);
            jo.put(Consts.PASSWORD, password);
            jo.put(Consts.DEVICE, Utils.getDeviceId());
            entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.MOBILE_LOGIN);
            info.setURL(url);
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setUniqId(uniqId);
            RequestManager.sharedInstance().addConnect(info);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            LogUtil.d(TAG, "mobileUserLogine ERROR");
        }
    }

    public static void changePassword(String old_password, String new_password, long uniqId) {
        if (old_password == null || new_password == null) {
            return;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return;
        }
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_MINE_INFO;
        HttpEntity entity;
        try {
            JSONObject jo = new JSONObject();
            jo.put(Consts.OLD_PASSWORD, old_password);
            jo.put(Consts.NEW_PASSWORD, new_password);
            entity = new StringEntity(jo.toJSONString(), Consts.CHARSET_UTF_8);
            ConnectInfo info = new ConnectInfo(Consts.CHANGE_PASSWORD);
            info.setURL(url);
            info.setMethod(HttpMethod.PUT);
            info.setEntity(entity);
            info.setUniqId(uniqId);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
            return;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return;
    }

    public static void checkConfirmation(String mobile, String confirmation, Consts.PURPOZE purpoze) {
        try {
            mobile = URLEncoder.encode(mobile, "gb2312");
            StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_CHECK_CONFIRMATION + "/" + confirmation + "?purpose=" + purpoze + "&mobile=" + mobile);
            ConnectInfo info = new ConnectInfo(Consts.CHECK_CONFIRMATION);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(confirmation);
            RequestManager.sharedInstance().addConnect(info);
            if (App.DEBUG) {
                LogUtil.v(TAG, url.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void createContacts(List<Contact> contacts) {
        if (contacts == null) return;
        for (int start = 0; start < contacts.size(); start += 50) {
            int end = start + 50 > contacts.size() ? contacts.size() : start + 50;
            List subList = contacts.subList(start, end);
            createContact(subList);
        }
    }

    /*
     *
     * @param contacts
     */
    public static void createContact(List<Contact> contacts) {
        HashMap<String, String> auth = getAuthentication();
        if (contacts == null || auth == null) {
            return;
        }
        Contacts jsonContacts = new Contacts(contacts);
        String jsonString = ((JSONObject) JSONObject.toJSON(jsonContacts)).toJSONString();
        try {
            StringEntity entity = new StringEntity(jsonString, Consts.CHARSET_UTF_8);
            StringBuffer url = new StringBuffer(Consts.HOST_HTTPS + Consts.METHOD_CONTACT);
            ConnectInfo info = new ConnectInfo(Consts.CREATE_CONTACT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            info.setEntity(entity);
            info.setTag(jsonString);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void deleteContact(Contact contact) {
        if (contact == null || TextUtils.isEmpty(contact.getId())) {
            return;
        }
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_CONTACT);
        url.append("/");
        url.append(contact.getContactID());
        try {
            ConnectInfo info = new ConnectInfo(Consts.DELETE_CONTACT);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.DELETE);
            info.addHeaders(auth);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void updateContact() {

    }

    public static void getContactList(int postion, int limit) {
        getContactList(postion, limit, null, null);
    }

    public static void getContactList() {
        getContactList(0, 1000);
    }

    /**
     * lsit contact
     * GET /contact/list?did={device_id}&type={t}&pos={x}&limit={n}&sort={sort}&order={order}
     * @param postion
     * @param limit
     * @param sort
     * @param order
     */
    public static void getContactList(int postion, int limit, String sort, String order) {
        HashMap<String, String> auth = getAuthentication();
        if (auth == null) {
            return;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_CONTACT);
        url.append("?did=");
        url.append(Utils.getDeviceId());
        if (postion < 0) {
            postion = 0;
        }
        url.append("&pos=" + postion);
        if (limit <= 0) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        url.append("&type=" + Consts.MOBILE);
        if (App.DEBUG) {
            Log.v(TAG, url.toString() + "\n" + auth.toString());
        }
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_CONTACTS);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.addHeaders(auth);
            info.setTag(String.valueOf(postion));
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
    }

    /**
     * GET /sensitive_word/list?type={type}&pos={x}&limit={n}&sort={sort_by}&order={order}
     */
    public static void listSenstiveWords(int pos) {
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_LIST_SENTIVE_WORD);
        url.append("/list");
        url.append("?pos=" + pos);
        try {
            ConnectInfo info = new ConnectInfo(Consts.GET_SENSITIVE_WORD);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            info.setTag(String.valueOf(pos));
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * Third party advertising images are downloading
     * @param pos
     * @param limit
     * @return
     */
    public static final void requestSplashData(int pos, int limit){
        if(App.DEBUG){LogUtil.i(TAG, "enter into requestSplashData method" );}
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.SPLASH_LIST);
        url.append("?os=android");
        if (pos < 0) {
            pos = 0;
        }
        url.append("&pos=" + pos);
        if (limit <= 0 || limit > 100) {
            limit = 100;
        }
        url.append("&limit=" + limit);
        try {
            ConnectInfo info = new ConnectInfo(Consts.LIST_SPLASH_AD);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            RequestManager.sharedInstance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
    }

    /**
     * The third party images are downloaded and stored in the specified location
     * @param spId
     */
    public static final void splashDownload(String spId){

        try {
        String tmpPath = MediaUtil.getImageDir() + ".tmp";
        DownloadEntity dle = new DownloadEntity(MediaUtil.getDestSaveDir() + spId + ".png" , tmpPath);
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.SPLASH + spId + Consts.FILECONTENT);
            ConnectInfo info = new ConnectInfo(Consts.DOWNLOAD_SPLASH);
            info.setWriter(dle);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            HttpDownloadManager.instance().addConnect(info);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
    }

    /**
     * splash clicked
     * @param spId
     */
    public static final void splashClick(String spId, String apkUrl){

        try {
            StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.SPLASH + spId + Consts.CLICK);
            ConnectInfo info = new ConnectInfo(Consts.SPLASH_CLICK);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.POST);
            RequestManager.sharedInstance().addConnect(info);
            if(App.DEBUG){
                LogUtil.i(TAG, "splashClick");
                LogUtil.i(TAG, "url"+url.toString());
            }
            downLoadApk(apkUrl);
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }
    }

    public  static final void downLoadApk(String akpUrl){

        try {
            String tmpPath = MediaUtil.getImageDir() + ".tmp";
            DownloadEntity dle = new DownloadEntity(MediaUtil.getThirdPartyApkPath() , tmpPath);
            StringBuilder url = new StringBuilder(akpUrl);
            ConnectInfo info = new ConnectInfo(Consts.APK_DOWNLOAD);
            info.setWriter(dle);
            info.setURL(url.toString());
            info.setMethod(HttpMethod.GET);
            HttpDownloadManager.instance().addConnect(info);
            CToast.showToast(R.string.start_download);
            if(App.DEBUG){
                LogUtil.i(TAG, "downLoadApk");
                LogUtil.i(TAG, "url"+url.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LogUtil.d(TAG, "" + e.toString());
        }

    }


}