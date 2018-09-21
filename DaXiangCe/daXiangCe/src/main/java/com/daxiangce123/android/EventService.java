package com.daxiangce123.android;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.R;
import com.daxiangce123.android.Consts.HttpMethod;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.DownloadedExtra;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileComments;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.SplashAdInfo;
import com.daxiangce123.android.data.UploadImage;
import com.daxiangce123.android.data.UserSuspendedInfo;
import com.daxiangce123.android.helper.DBHelper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Connector;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.manager.FileUploadManager;
import com.daxiangce123.android.manager.HttpTimeOutManger;
import com.daxiangce123.android.manager.ImageManager;
import com.daxiangce123.android.manager.LocateManager;
import com.daxiangce123.android.manager.NotifyManager;
import com.daxiangce123.android.manager.UploadCancelManager;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.ui.view.CToast;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.DialogUtils;
import com.daxiangce123.android.util.InitAlarm;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.TimeUtil;
import com.daxiangce123.android.util.UMutils;
import com.daxiangce123.android.util.UMutils.ID;
import com.daxiangce123.android.util.UnbindDevice;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.HttpStatus;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

/**
 * @author ram
 * @project DaXiangCe
 * @time Mar 28, 2014
 */
public class EventService extends Service {

    private final static String TAG = "EventService";
    private static boolean serviceRunning = false;
    private static boolean isDBRunning = false;
    private static boolean isEventRunning = false;
    private Connector mConnector;
    private ConnectInfo mConnectInfo;
    private Handler dbHandler;
    private Handler eventHandler;
    private HandlerThread dbThread;
    private HandlerThread eventThread;
    private LinkedList<Object> mTaskList;
    private DBHelper dbHelper;
    private LinkedList<AlbumEntity> allAlbumList;
    // private Location lastLocation;
    // /**
    // * GEO updated to server
    // */
    // private long lastGeoUpdateTimeInMill;
    // /**
    // * check if need to update GEO
    // */
    // private long lastGeoLoopTimeInMill;
    // private final int USER_GEO_ACCURACY = 2000;// 5minutes
    // private final int USER_GEO_LOOP_DURATION = 60 * 1000;// 1minutes
    // private final int USER_GEO_UPDATE_DURATION = 10 * 60 * 1000;// 10minutes
    private final int EVENT_SLEEP_DURATION = 15 * 1000;
    /**
     * eventTime+fileId -> event
     */

    private SimpleDateFormat sdf = null;
    private DateFormat df = null;
    private String currentDate = null;
    private int NOTIfID = 0x123;

    private HashMap<String, Event> tmpEventMap;
    private HomeKeyBroadcastReceiver homeKeyReceiver = new HomeKeyBroadcastReceiver();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Response response = intent.getParcelableExtra(Consts.RESPONSE);
            try {
                if (App.DEBUG) {
                    LogUtil.d(TAG, "--------------->onReceive	" + action);
                }
                if (Consts.GET_DWURL.equals(action)) {
                    onGetDwrul(intent);
                } else if (Consts.STOP_EVENT_SERVICE.equals(action)) {
                    stopWork();
                } else if (Consts.START_EVENT_SERVICE.equals(action)) {
                    startWork();
                } else if (Consts.ACTION_LOCATED.equals(action)) {
                    updateUserGeo();
                } else if (Consts.STOP_FETCH_EVENT_SERVICE.equals(action)) {
                    stopEvent();
                } else if (Consts.SYSTEM_USER_DISABLED.equals(action)) {
                    userDisabled(intent);
                } else if (Consts.GET_USER_ID_BY_AUTHENTIC.equals(action) || Consts.GET_USER_ID_BY_OAUTH.equals(action)) {
                    String content = response.getContent();
                    JSONObject jsonObject = JSONObject.parseObject(content);
                    String userId = jsonObject.getString(Consts.USER_ID);
                    ConnectBuilder.getUsetSuspendedInfo(userId);
                } else if (Consts.GET_USER_SUSPENDED_INFO.equals(action)) {
                    onGetUserSuspend(intent);
                }else if(Consts.LIST_SPLASH_AD.equals(action)){
                    getRequestData(intent);
                }else if(Consts.APK_DOWNLOAD.equals(action)){
                    if(response.getStatusCode() == 200){
                        installThirdApk();
                        if(App.DEBUG){LogUtil.d(TAG,"StatusCode()/"+response.getStatusCode());}
                    }
                }else {
                    addTask(intent);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void installThirdApk(){
        if(App.DEBUG){LogUtil.i(TAG,"installApk");}
        File apkFile = new File(MediaUtil.getThirdPartyApkPath());
        if (!apkFile.exists()) {return;}
        Intent insIntent = new Intent(Intent.ACTION_VIEW);
        insIntent.setDataAndType(Uri.parse("file://" + apkFile.toString()), Consts.APLICATION_ARCHIVE);
        String title = Utils.getString(R.string.noti_flag);
        String content = Utils.getString(R.string.download_complete);
        NotifyManager.instance().showNotification(title, AppData.getApkName() + content, NOTIfID, insIntent, false);
    }

    /**
     * Parsing the json data from server request parsing the json data from server requests
     */
    private void getRequestData(Intent intent) {
        if(App.DEBUG){LogUtil.i(TAG, "enter into getRequestData method");}
        File file = new File(MediaUtil.getDestSaveDir() + AppData.getSplashId() + ".png");
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        String content = response.getContent();
        long bCurDate = System.currentTimeMillis();
        if (info == null || response == null) {
            return;
        }
        SplashAdInfo splashAdInfo = null;
        List<SplashAdInfo> list = Parser.getSplashData(content);
        for (int i = 0; i < list.size(); i++) {
            SplashAdInfo sa = list.get(i);
            long bStartDate = TimeUtil.formatTime( sa.getStart_date(), Consts.SERVER_UTC_FORMAT);
            long bEndDate = TimeUtil.formatTime(sa.getEnd_date(), Consts.SERVER_UTC_FORMAT);
            if(App.DEBUG){
                LogUtil.i(TAG, "bStartDate/" + bStartDate);
                LogUtil.i(TAG, "bEndDate/" + bEndDate);
                LogUtil.i(TAG, "bCurDate/" + bCurDate);
            }
//            if((bCurDate > bStartDate && bCurDate > bEndDate) || (bCurDate < bStartDate && bCurDate < bEndDate)){
//                if(file.exists()){file.delete();}
//                AppData.setSplashId("");
//                continue;
//            }
            if (bStartDate < bCurDate && bCurDate < bEndDate) {
                splashAdInfo = sa;
                break;
            }
        }
        if(splashAdInfo == null){return;}
        Log.i(TAG, "AppData.getSplashId()/" + AppData.getSplashId());
        Log.i(TAG, "splashAdInfo.getId()/" + splashAdInfo.getId());
        if(!AppData.getSplashId().equals(splashAdInfo.getId())){
            if(file.exists()){file.delete();}
        }
        AppData.setSplashId(splashAdInfo.getId());
        AppData.setApkName(splashAdInfo.getName());
        AppData.setSplashUrl(splashAdInfo.getUrl());
        /*
        if(splashAdInfo.getBackground_color() == 0){
            AppData.setSplashBgColor("0794e1");
        }else{
            AppData.setSplashBgColor(Integer.toHexString(splashAdInfo.getBackground_color()));
        }
        */
        AppData.setSplashStartDate(TimeUtil.formatTime( splashAdInfo.getStart_date(), Consts.SERVER_UTC_FORMAT));
        AppData.setSplashEndDate(TimeUtil.formatTime( splashAdInfo.getEnd_date(), Consts.SERVER_UTC_FORMAT));
        if(App.DEBUG){
            LogUtil.i(TAG, "sp_id/" + AppData.getSplashId());
            LogUtil.i(TAG, "sp_name/" + AppData.getApkName());
            LogUtil.i(TAG, "sp_start_date/" + AppData.getSplashStartDate());
            LogUtil.i(TAG, "sp_end_date/" + AppData.getSplashEndDate());
            LogUtil.i(TAG, "sp_url/" + AppData.getSplashUrl());
            LogUtil.i(TAG, "sp_bgcolor/" + AppData.getSplashBgColor());
        }
    }


    private void addTask(Object task) {
        if (task == null) {
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "--------------->addTask	" + task.getClass().getName());
        }

        synchronized (mTaskList) {
            if (task instanceof Intent) {
                ConnectInfo info = ((Intent) task).getParcelableExtra(Consts.REQUEST);
                if (info != null) {
                    if (info.getPriority() == (Consts.Priority.HIGH)) {
                        mTaskList.addFirst(task);
                    } else {
                        mTaskList.addLast(task);
                    }
                }

            } else {
                mTaskList.addLast(task);
            }
        }
        startDbHelper();
    }

    private Runnable eventRunnable = new Runnable() {
        @Override
        public void run() {
            while (isEventRunning) {
                try {
                    if (Utils.isAppHidden(App.getActivity())) {
                        stopRecycling();
                    }
                    int statusCode = execute();
                    if (!isEventRunning) {
                        continue;
                    }
                    if (statusCode == 403 || statusCode == 500 || statusCode == 503) {
                        Thread.sleep(EVENT_SLEEP_DURATION * 3);
                    } else {
                        Thread.sleep(EVENT_SLEEP_DURATION);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    };

    private Runnable dbRunnable = new Runnable() {
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            try {
                while (!mTaskList.isEmpty()) {
                    verifyDbHelper();
                    if (dbHelper == null) {
                        try {
                            Thread.sleep(500);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        continue;
                    }
                    Object data = null;
                    synchronized (mTaskList) {
                        data = mTaskList.removeFirst();
                    }
                    if (data == null) {
                        continue;
                    }
                    if (data instanceof ArrayList<?>) {
                        ArrayList<?> l = (ArrayList<?>) data;
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "dbRunnable	EventList size 	" + Utils.sizeOf(l));
                        }
                        if (Utils.isEmpty(l)) {
                            continue;
                        }
                        if (l.get(0) instanceof Event) {
                            handleEvent((ArrayList<Event>) l);
                        }
                    } else if (data instanceof Intent) {
                        Intent task = (Intent) data;
                        String action = task.getAction();
                        ConnectInfo info = task.getParcelableExtra(Consts.REQUEST);
                        Response response = task.getParcelableExtra(Consts.RESPONSE);
                        if (App.DEBUG) {
                            LogUtil.d(TAG, "dbRunnable	request 	" + info);
                            LogUtil.d(TAG, "dbRunnable	response 	" + response);
                        }
                        if (Consts.START_UPLOADING_CHECKING.equals(action)) {
                            checkUnloadedFile();
                            continue;
                        }
                        if (response == null || info == null) {
                            continue;
                        }
                        int sCode = response.getStatusCode();
                        if (Consts.GET_ALBUM_ITEMS.equals(action) || Consts.GET_ALBUM_UPDATE_ITEMS.equals(action) || Consts.GET_USER_OF_ALBUM_ITEMS.equals(action)) {
                            String content = response.getContent();
                            onGetAlbumItems(content, info);
                        } else if (Consts.CREATE_FILE.equals(action)) {
                            if (sCode == 200) {
                                onFileCreate(task);
                            } else if (sCode == 401 || sCode == 403) {
                                handleUnAuthFile(info, null);
                            }
                        } else if (Consts.DELETE_FILE.equals(action)) {
                            String id = info.getTag();
                            if (id == null && task.hasExtra(Consts.FILE_ID)) {
                                id = task.getStringExtra(Consts.FILE_ID);
                            }
                            if (sCode == 200 || sCode == 404) {
                                onDeleteFile(id);// boolean result =
                            }
                        } else if (Consts.DOWNLOAD_FILE.equals(action)) {
                            if (sCode == 404) {
                                onDeleteFile(info.getTag2());
                            }
                        } else if (Consts.UPDATE_ALBUM.equals(action)) {
                            if (sCode == 200) {
                                updateAlbum(info);
                            }
                        } else if (Consts.SET_MEMBER_ROLE.equals(action)) {
                            if (sCode == 200) {
                                onRoleChanged(info);
                            }
                        } else if (Consts.GET_ALBUM_INFO.equals(action)) {
                            if (sCode == 200) {
                                onAlbumInfoGet(response, info);
                            }
                        } else if (Consts.UPLOAD_FILE.equals(action)) {
                            if (sCode == 200) {
                                onFileUpload(response, info);
                            } else if (sCode == 401 || sCode == 403) {
                                FileEntity fileEntity = Parser.parseFile(info.getTag3());
                                handleUnAuthFile(info, fileEntity);
                            }
                        } else if (Consts.UPLOADING_FILE.equals(action)) {
                            onFileUploading(task);
                        } else if (Consts.LIST_ALBUM.equals(action)) {

                            if (App.DEBUG) {
                                LogUtil.d(TAG, "-- LIST_ALBUM -- " + response + " -- " + info);
                            }
                            if (sCode == 200) {
                                ListAllAlbums listAllAlbums = Parser.parseAlbumList(response.getContent());
                                onListAlbumEnd(listAllAlbums, info);
                            }
                        } else if (Consts.GET_ALBUM_COVER.equals(action)) {
                            if (sCode == 200) {
                                onGetAlbum(response, info);
                            } else if (sCode == 404) {
                                onDeleteAlbum(response, info);
                            }
                        } else if (Consts.DELETE_ALBUM.equals(action) || Consts.LEAVE_ALBUM.equals(action)) {
                            if (sCode == 200 || sCode == 404) {
                                onDeleteAlbum(response, info);
                            }
                        } else if (Consts.GET_FILE_INFO.equals(action)) {
                            if (sCode == 200) {
                                onGetFileInfo(response);
                            } else if (sCode == 404) {
                                onDeleteFile(info.getTag());
                            }
                        } else if (Consts.CREATE_ALBUM.equals(action) || Consts.JOIN_ALBUM.equals(action)) {
                            if (sCode == 200) {
                                AlbumEntity album = Parser.parseAlbum(response.getContent());
                                if (dbHelper != null) {
                                    dbHelper.insert(album);
                                }
                            }
                        } else if (Consts.NO_PUSH.equals(action)) {
                            if (sCode == 200) {
                                onPushChanged(response, info);
                            }
                        } else if (Consts.SET_ALBUM_THUMB.equals(action)) {
                            if (sCode == 200) {
                                setAlbumCover(response, info);
                            }
                        } else if (Consts.LIKE_FILE.equals(action)) {

                            if (sCode == 200) {
                                onLikeFile(info.getTag());
                            }
                        } else if (Consts.DISLIKE_FILE.equals(action)) {

                            if (sCode == 200) {
                                onDisikeFile(info.getTag());
                            }

                        } else if (Consts.SHARED_FILE.equals(action)) {
                            if (sCode == 200) {
                                onShareFile(info.getTag());
                            }
                        } else if (Consts.DOWNLOADED_FILE.equals(action)) {
                            if (sCode == 200) {
                                onDownloadedFile(info.getTag());
                            }
                        } else if (Consts.SHARED_ALBUM.equals(action)) {
                            if (sCode == 200) {
                                onShareAlbum(info.getTag());
                            }
                        } else if (Consts.GET_COMMENTS.equals(action)) {
                            if (sCode == 200) {
                                onGetCommentItems(response.getContent(), info);
                            }
                        } else if (Consts.DELETE_COMMENT.equals(action)) {
                            if (sCode == 200 || sCode == 404) {
                                onDeleteComment(info.getTag());
                            }
                        } else if (Consts.POST_COMMENT.equals(action)) {
                            if (sCode == 200) {
                                onPostComment(response.getContent(), info);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                LogUtil.d(TAG, "dbRunnable	Exception	" + e);
            }
            isDBRunning = false;
        }
    };


    private void userDisabled(Intent intent) {
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        HttpEntity entity = info.getEntity();
        JSONObject jo = null;
        if (entity != null) {
            try {
                if (entity instanceof StringEntity) {
                    String entityStr = EntityUtils.toString(entity);
                    jo = JSONObject.parseObject(entityStr);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Consts.MOBILE_LOGIN.equals(info.getType())) {
            if (jo == null) {
                return;
            }
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Consts.MOBILE, jo.getString(Consts.MOBILE));
            jsonObject.put(Consts.PASSWORD, jo.getString(Consts.PASSWORD));
            jsonObject.put(Consts.DEVICE, jo.getString(Consts.DEVICE));
            ConnectBuilder.getUserIdByAuthentic(jo.toString());
        } else if (Consts.SSO_BIND.equals(info.getType())) {
            if (jo == null) {
                return;
            }

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(Consts.SSO_PROVIDER, jo.getString(Consts.SSO_PROVIDER));
            jsonObject.put(Consts.TOKEN, jo.getString(Consts.TOKEN));
            jsonObject.put(Consts.DEVICE, jo.getString(Consts.DEVICE));
            ConnectBuilder.getUserIdByOAuth(jo.toString());
        } else {
            ConnectBuilder.getUsetSuspendedInfo(App.getUid());
        }

    }


    private void onGetUserSuspend(Intent intent) {
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        String content = response.getContent();
        UserSuspendedInfo userSuspendedInfo = Parser.parseUserSupended(content);
        onDeleteCommentTable();

        if (Utils.isEmpty(App.getUid())) {
            return;
        }
        if (userSuspendedInfo == null) {
            return;
        }
        if (!userSuspendedInfo.getUserId().equals(info.getTag())) {
            return;
        }

        if (userSuspendedInfo.getStatus().equals(Consts.DISABLED_PERMANENTLY)) {
            UnbindDevice.unbindDevice(true);
            DialogUtils.dialog(R.string.disabled_permanently);
            App.getActivity().finish();
        }

        if (userSuspendedInfo.getStatus().equals(Consts.DISABLED_TEMPORARILY)) {
            UnbindDevice.unbindDevice(true);
            DialogUtils.dialog(R.string.disabled_temporarily);
            App.getActivity().finish();
        }


    }

    private void onGetDwrul(Intent intent) {
        ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
        Response response = intent.getParcelableExtra(Consts.RESPONSE);
        if (info == null || response == null) {
            return;
        }
        String fileId = info.getTag();
        String url = Parser.parseDWUrl(response.getContent());
        /** DownloadedExtra-javabean */
        DownloadedExtra extra = (DownloadedExtra) info.getObj();
        if (fileId != null && extra != null && url != null) {
            String params = extra.getParams();
            if (App.DEBUG) {
                LogUtil.d(TAG, "{" + fileId + "}:onGetDwrul " + url);
            }
//            if (params != null) {
////                ConnectBuilder.downloadFileCount(params, fileId);
//            } else {
            ConnectBuilder.downloadFile(url, fileId, extra.getLocalPath(), extra.getToken(), extra.getFileSize());
        }
//        }
    }

    private void onGetFileInfo(Response response) {
        FileEntity fileEntity = Parser.parseFile(response.getContent());
        if (dbHelper == null) {
            return;
        }
        dbHelper.insert(fileEntity);
    }

    private void onGetAlbumItems(String content, ConnectInfo info) {
        if (Utils.isEmpty(content) || info == null) {
            return;
        }
        AlbumSamples albumItems = Parser.parseAlbumSamples(content, false);
        if (albumItems == null) {
            return;
        }
        dbHelper.insert(albumItems.getFiles());
        if (App.DEBUG) {
            LogUtil.d(TAG, "onGetAlbumItems()	" + content);
        }
    }

    private void onDeleteComment(String commentId) {
        if (Utils.isEmpty(commentId)) {
            return;
        }
        boolean result = dbHelper.delete(CommentEntity.EMPTY, commentId);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onDeleteComment()	" + result);
        }
    }

    private void onDeleteCommentTable() {
//        boolean result = dbHelper.delete(CommentEntity.EMPTY);
        CommentEntity.EMPTY.deleteAllComment(dbHelper.getWritableDatabase());

        if (App.DEBUG) {
            LogUtil.d(TAG, "onDeleteCommentTable()	" + CommentEntity.EMPTY.deleteAllComment(dbHelper.getWritableDatabase()));
        }
    }

    private void onPostComment(String content, ConnectInfo info) {
        if (Utils.isEmpty(content) || info == null) {
            return;
        }
        CommentEntity commentEntity = Parser.parseComment(content);
        if (dbHelper == null) {
            return;
        }
        // update albumEntity
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        boolean result = commentEntity.insert(db);
        if (App.DEBUG) {
            LogUtil.d(TAG, "onPostComment()	" + result);
        }
    }

    /**
     * on get items save to db
     *
     * @param content
     * @param info
     */
    private void onGetCommentItems(String content, ConnectInfo info) {
        if (Utils.isEmpty(content) || info == null) {
            return;
        }
        FileComments albumItems = Parser.parseFileComments(content);
        if (albumItems == null) {
            return;
        }
        dbHelper.insert(albumItems.getcomments());
        if (App.DEBUG) {
            LogUtil.d(TAG, "onGetAlbumItems()	" + content);
        }
    }

    /**
     * delete file
     *
     * @param fileId
     * @return
     */
    private boolean onDeleteFile(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return false;
        }
        boolean result = dbHelper.delete(FileEntity.EMPTY, fileId);
        return result;
    }

    @SuppressLint("DefaultLocale")
    public void updateAlbum(ConnectInfo connectInfo) {
        HttpEntity entity = connectInfo.getEntity();
        try {
            if (entity instanceof StringEntity) {
                String albumId = connectInfo.getTag();
                if (Utils.isEmpty(albumId) || dbHelper == null) {
                    return;
                }
                String entityStr = EntityUtils.toString(entity);
                JSONObject jo = JSONObject.parseObject(entityStr);
                HashSet<String> keys = new HashSet<String>();
                keys.add(Consts.NAME);
                keys.add(Consts.NOTE);
                keys.add(Consts.IS_PRIVATE);
                keys.add(Consts.COMMENT_OFF);
                keys.add(Consts.LIKE_OFF);
                keys.add(Consts.IS_LOCKED);
                String name = jo.getString(Consts.NAME);
                String note = jo.getString(Consts.NOTE);
                boolean isPrivate = jo.getBoolean(Consts.IS_PRIVATE);
                boolean commentOff = jo.getBooleanValue(Consts.COMMENT_OFF);
                boolean likeOff = jo.getBoolean(Consts.LIKE_OFF);
                boolean isLocked = jo.getBoolean(Consts.IS_LOCKED);
                AlbumEntity albumEntity = new AlbumEntity();
                albumEntity.setUpdateKey(keys);
                albumEntity.setId(albumId);
                albumEntity.setName(name);
                albumEntity.setNote(note);
                albumEntity.setIsPrivate(isPrivate);
                albumEntity.setCommentOff(commentOff);
                albumEntity.setLikeOff(likeOff);
                albumEntity.setIsLocked(isLocked);
                if (jo.containsKey(Consts.PERMISSIONS)) {
                    ConnectBuilder.resetMemberPermission(albumId);

                    int permissions = -1;
                    JSONArray ja = jo.getJSONArray(Consts.PERMISSIONS);
                    for (int i = 0; i < ja.size(); i++) {
                        String per = ja.getString(i);
                        if (Utils.isEmpty(per)) {
                            continue;
                        }
                        per = per.trim().toLowerCase(Locale.ENGLISH);
                        if ("read".equals(per)) {
                            permissions = permissions == -1 ? 0 : permissions;
                            permissions = permissions | Consts.IO_PERMISSION_R;
                        } else if ("write".equals(per)) {
                            permissions = permissions == -1 ? 0 : permissions;
                            permissions = permissions | Consts.IO_PERMISSION_W;
                        }
                    }
                    if (permissions > -1) {
                        albumEntity.setPermissions(permissions);
                        keys.add(Consts.PERMISSIONS);
                    }
                }
                dbHelper.update(albumEntity);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void onRoleChanged(ConnectInfo info) {
        if (info == null) {
            return;
        }
        String jsonStr = info.getTag();
        JSONObject jo = JSONObject.parseObject(jsonStr);
        if (jo.containsKey(Consts.PERMISSIONS)) {
            return;
        }
        UMutils.instance().diyEvent(ID.EventTransferAlbum);
        String albumId = jo.getString(Consts.ALBUM_ID);
        String owner = jo.getString(Consts.USER_ID);
        HashSet<String> keys = new HashSet<String>();
        keys.add(Consts.OWNER);
        AlbumEntity album = new AlbumEntity();
        album.setId(albumId);
        album.setOwner(owner);
        album.setUpdateKey(keys);

        if (dbHelper != null) {
            dbHelper.update(album);
        }
    }

    private void onLikeFile(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return;
        }
        UMutils.instance().diyEvent(ID.EventLike);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.update(db);
        fileEntity.changeSize(db, fileId, Consts.LIKES, true);
        db.close();
    }

    private void onDisikeFile(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return;
        }
        UMutils.instance().diyEvent(ID.EventLike);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.update(db);
        fileEntity.changeSize(db, fileId, Consts.LIKES, false);
        db.close();
    }

    private void onShareFile(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return;
        }
        // UMutils.instance().diyEvent(ID.EventLike);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.update(db);
        fileEntity.changeSize(db, fileId, Consts.SHARES, true);
        db.close();
    }

    private void onDownloadedFile(String fileId) {
        if (Utils.isEmpty(fileId)) {
            return;
        }
        // UMutils.instance().diyEvent(ID.EventLike);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        FileEntity fileEntity = new FileEntity();
        fileEntity.setId(fileId);
        fileEntity.update(db);
        fileEntity.changeSize(db, fileId, Consts.DOWNLOADS, true);
        db.close();
    }

    private void onShareAlbum(String albumId) {
        if (Utils.isEmpty(albumId)) {
            return;
        }
        // UMutils.instance().diyEvent(ID.EventLike);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        AlbumEntity albumEntity = new AlbumEntity();
        albumEntity.setId(albumId);
        albumEntity.update(db, false);
        albumEntity.changeSize(db, albumId, Consts.SHARES, true);
        db.close();
    }

    private void onFileUploading(Intent task) {
        if (App.DEBUG) {
            LogUtil.e(TAG, "onFIleUploading" + task);
        }
        ArrayList<FileEntity> entities = (ArrayList<FileEntity>) task.getSerializableExtra(Consts.UPLOADING_FILE);
        if (dbHelper == null) {
            return;
        }
        // update albumEntity
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        // HashSet<String> keys = new HashSet<String>();
        // keys.add(Consts.SIZE);
        // AlbumEntity album = new AlbumEntity();
        // album = album.get(db, entities.get(0).getAlbum());
        // if (album != null) {
        // album.setUpdateKey(keys);
        // album.setSize(album.getSize() + entities.size());
        // album.update(db, false);
        // }
        // insert fileEntity into albumEntity
        for (FileEntity fileEntity : entities) {
            fileEntity.insert(db);
        }
        db.close();
    }

    private void onAlbumInfoGet(Response response, ConnectInfo info) {
        AlbumEntity album = (AlbumEntity) Parser.parseAlbum(response.getContent());
        if (album == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        HashSet<String> keys = new HashSet<String>();
        keys.add(Consts.SIZE);
        album.setUpdateKey(keys);
        album.update(db, false);
        db.close();
    }

    private void onFileUpload(Response response, ConnectInfo info) {
        if (App.DEBUG) {
            LogUtil.e(TAG, "onFileUpload " + response);
        }
        if (response == null) {
            return;
        }
        if (response.getStatusCode() != 200) {
            return;
        }
        if (Utils.isEmpty(response.getContent())) {
            return;
        }
        FileEntity file = Parser.parseFile(info.getTag3());
        if (file == null) {
            return;
        }
        if (UploadCancelManager.sharedInstance().checkFileEntity(file)) {
            UploadCancelManager.sharedInstance().deleteFileFromServer(file);
            return;
        }
        UMutils.instance().diyEvent(ID.EventUploadFileSuccess);
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        file.setStatus(Consts.FILEENTITY_STATUS_ACTIVE);
        file.setUploaded(db);
        db.close();
    }


    private void onListAlbumEnd(ListAllAlbums listAllAlbums, ConnectInfo info) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "onListAlbumEnd	size " + (listAllAlbums == null ? "is null" : Utils.sizeOf(listAllAlbums.getAlbums())));
        }
        long start = System.currentTimeMillis();
        if (App.DEBUG) {
            LogUtil.d(TAG, " --onListAlbumEnd--   -- listAllAlbums : " + listAllAlbums);
        }

        if (listAllAlbums == null) {
            return;
        }
        LinkedList<AlbumEntity> list = listAllAlbums.getAlbums();
        if (App.DEBUG) {
            LogUtil.d(TAG, " --onListAlbumEnd--   -- list : " + list);
        }

        if (list == null) {
            Intent intent = new Intent(Consts.ON_HANLDE_ALBUM_LIST);
            intent.putExtra(Consts.LIST_ALL_ALBUMS, listAllAlbums);
            Broadcaster.sendBroadcast(intent);
            return;
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, " --onListAlbumEnd--   -- dbHelper : " + dbHelper);
        }

        if (dbHelper == null) {
            return;
        }

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        dbHelper.insert(list, db, true);
        LinkedList<AlbumEntity> dbList = dbHelper.getList(AlbumEntity.EMPTY, db);
        if (allAlbumList == null) {
            allAlbumList = new LinkedList<AlbumEntity>();
        }
        String tag = info.getTag2();
        if (Consts.FROM_STARTER.equals(tag)) {
            allAlbumList.clear();
            allAlbumList.addAll(list);
        } else {
            allAlbumList.addAll(list);
        }
        if (!listAllAlbums.hasMore()) {
            for (int index = dbList.size() - 1; index >= 0; --index) {
                AlbumEntity albumEntity = dbList.get(index);
                boolean inList = false;
                for (int i = 0; i < allAlbumList.size(); i++) {
                    AlbumEntity album = allAlbumList.get(i);
                    if (albumEntity.getId().equals(album.getId())) {
                        inList = true;
                        break;
                    }
                }
                if (!inList) {
                    dbList.remove(albumEntity);
                    AlbumEntity.EMPTY.delete(db, albumEntity.getId());
                }
            }
            allAlbumList.clear();
        } else {
        }

        List<AlbumEntity> dbCloneList = (List<AlbumEntity>) dbList.clone();

//		listAllAlbums.setAlbums(list);
        listAllAlbums.setAlbums(dbList);
        Intent intent = new Intent(Consts.ON_HANLDE_ALBUM_LIST);
        intent.putExtra(Consts.LIST_ALL_ALBUMS, listAllAlbums);
        Broadcaster.sendBroadcast(intent);

        if (App.DEBUG) {
            LogUtil.d(TAG, "onListAlbumEnd	MID	duration=" + (System.currentTimeMillis() - start));
        }

        db.beginTransaction();
        try {
            for (AlbumEntity album : dbCloneList) {
                if (album == null) {
                    continue;
                }
                if (album.getSize() == 0 && album.getLocalCover() != null) {
                    HashSet<String> keys = new HashSet<String>();
                    keys.add(Consts.COVER);
                    album.setUpdateKey(keys);
                    album.setLocalCover("");
                    album.update(db);
                    String sql = "DELETE FROM " + FileEntity.EMPTY.getTableName() + " WHERE " + Consts.ALBUM + "=\"" + album.getId() + "\"";
                    dbHelper.execute(sql, db);
                }
                if (Utils.isEmpty(album.getThumbFileId())) {
                    // ConnectBuilder.getAlbumCover(album.getId());
                    ConnectBuilder.getAlbumCoverId(album.getId());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.setTransactionSuccessful();
        db.endTransaction();
        db.close();
        dbCloneList.clear();
        if (App.DEBUG) {
            LogUtil.d(TAG, "onListAlbumEnd	END	duration=" + (System.currentTimeMillis() - start));
        }
    }

    private void onDeleteAlbum(Response response, ConnectInfo info) {
        if (info == null || response == null) {
            return;
        }
        String albumId = info.getTag();
        if (dbHelper != null) {
            dbHelper.delete(AlbumEntity.EMPTY, albumId);
            FileEntity.EMPTY.deleteFilesInAlbum(dbHelper.getWritableDatabase(), albumId);
        }
        //TODO delete file in album


    }

    private void onGetAlbum(Response response, ConnectInfo info) {
        if (info == null || response == null) {
            return;
        }
        String albumId = info.getTag();
        // String fileId = null;
        // AlbumSamples albumItems =
        // Parser.parseAlbumSamples(response.getContent(), false);
        String fileId = Parser.parseAlbumThumId(response.getContent());
        // if (albumItems != null && !Utils.isEmpty(albumItems.getFiles())) {
        // FileEntity file = albumItems.getFiles().getFirst();
        // if (file != null) {
        // fileId = file.getId();
        // }
        // }

        if (dbHelper == null) {
            return;
        }
        if (Utils.isEmpty(fileId)) {
            fileId = "";
        }
        String sql = "UPDATE " + Consts.ALBUMS + " SET " + Consts.COVER + "=\"" + fileId + "\" WHERE " + Consts.ALBUM_ID + "=\"" + albumId + "\"";
        dbHelper.execute(sql);
    }

    private void onPushChanged(Response response, ConnectInfo info) {
        if (info == null) {
            return;
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, "onPushChanged	" + response + "\n------------>connectInfo:\n" + info);
        }

        JSONObject jo = JSONObject.parseObject(info.getTag2());
        boolean noPush = jo.getBooleanValue(Consts.NO_PUSH);
        if (dbHelper == null) {
            return;
        }
        String albumId = info.getTag();
        int bool = noPush ? Consts.BOOLEAN_TRUE : Consts.BOOLEAN_FALSE;
        dbHelper.execute("UPDATE " + Consts.ALBUMS + " SET " + Consts.NO_PUSH + "=\"" + bool + "\" WHERE " + Consts.ALBUM_ID + "=\"" + albumId + "\"");
    }

    public void setAlbumCover(Response response, ConnectInfo connectInfo) {
        if (response == null || connectInfo == null) {
            return;
        }
        if (response.getStatusCode() == 200) {
            CToast.showToast(R.string.set_album_cover_succeed);
            String albumId = connectInfo.getTag();
            String fileId = connectInfo.getTag2();
            dbHelper.execute("UPDATE " + Consts.ALBUMS + " SET " + Consts.THUMB_FILE_ID + "=\"" + fileId + "\" WHERE " + Consts.ALBUM_ID + "=\"" + albumId + "\"");
        } else {
            CToast.showToast(R.string.set_album_cover_failed);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCreate() {
        super.onCreate();
        if (App.DEBUG) {
            LogUtil.d(TAG, "EventService onCreate	" + System.currentTimeMillis());
        }
        serviceRunning = true;
        initBroadcast();
        mTaskList = new LinkedList<Object>();
        startWork();
        if (App.DEBUG) {
            LogUtil.d(TAG, "TEMPP onCreate	" + System.currentTimeMillis());
        }

		/*----start TimeOutManger---*/
        HttpTimeOutManger.instance().start();

        IntentFilter ift = new IntentFilter();
        ift.addAction(Intent.ACTION_SCREEN_OFF);
        ift.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        registerReceiver(homeKeyReceiver, ift);
        // checkUnloadedFile();
    }

    private void startWork() {
        verifyDbHelper();
        startEvent();
    }

    private void stopWork() {
        stopRecycling();
        stopDbHelper();
        dbHelper = null;
        synchronized (mTaskList) {
            clearTask();
        }
    }

    private void clearTask() {
        if (mTaskList == null) {
            return;
        }
        mTaskList.clear();
    }

    private void initBroadcast() {
        IntentFilter ift = new IntentFilter();
        ift.addAction(Consts.CREATE_FILE);
        ift.addAction(Consts.STOP_FETCH_EVENT_SERVICE);
        ift.addAction(Consts.START_EVENT_SERVICE);
        ift.addAction(Consts.STOP_EVENT_SERVICE);
        ift.addAction(Consts.GET_ALBUM_ITEMS);
        ift.addAction(Consts.GET_ALBUM_COVER);
        ift.addAction(Consts.SET_MEMBER_ROLE);
        ift.addAction(Consts.DOWNLOAD_FILE);
        ift.addAction(Consts.GET_FILE_INFO);
        ift.addAction(Consts.UPDATE_ALBUM);
        ift.addAction(Consts.DELETE_ALBUM);
        ift.addAction(Consts.LEAVE_ALBUM);
        ift.addAction(Consts.UPLOAD_FILE);
        ift.addAction(Consts.UPLOADING_FILE);
        ift.addAction(Consts.DELETE_FILE);
        ift.addAction(Consts.LIST_ALBUM);
        ift.addAction(Consts.CREATE_ALBUM);
        ift.addAction(Consts.JOIN_ALBUM);
        ift.addAction(Consts.NO_PUSH);
        ift.addAction(Consts.SET_ALBUM_THUMB);
        ift.addAction(Consts.LIKE_FILE);
        ift.addAction(Consts.DISLIKE_FILE);
        ift.addAction(Consts.SHARED_FILE);
        ift.addAction(Consts.DOWNLOADED_FILE);
        ift.addAction(Consts.SHARED_ALBUM);

        ift.addAction(Consts.GET_USER_OF_ALBUM_ITEMS);
        ift.addAction(Consts.ACTION_LOCATED);
        ift.addAction(Consts.GET_ALBUM_UPDATE_ITEMS);
        ift.addAction(Consts.START_UPLOADING_CHECKING);
        ift.addAction(Consts.SYSTEM_USER_DISABLED);
        ift.addAction(Consts.LOGIN_TIMEOUT);
        ift.addAction(Consts.GET_ALBUM_INFO);

        ift.addAction(Consts.DELETE_COMMENT);
        ift.addAction(Consts.POST_COMMENT);
        ift.addAction(Consts.GET_COMMENTS);

        ift.addAction(Consts.GET_DWURL);
        ift.addAction(Consts.GET_USER_ID_BY_AUTHENTIC);
        ift.addAction(Consts.GET_USER_ID_BY_OAUTH);
        ift.addAction(Consts.GET_USER_SUSPENDED_INFO);

        ift.addAction(Consts.MOBILE_LOGIN_INTERRUPT);
        ift.addAction(Consts.OAUTH_INTERRUPT);

        ift.addAction(Consts.LIST_SPLASH_AD);
        ift.addAction(Consts.APK_DOWNLOAD);

        // ift.addAction(Consts.FORCE_UNBIND_DEVICE);
        Broadcaster.registerReceiver(mReceiver, ift);
    }

    private void startDbHelper() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "startDbHelper	isDBRunning=" + isDBRunning);
        }
        if (isDBRunning) {
            return;
        }
        isDBRunning = true;
        if (dbThread == null) {
            dbThread = new HandlerThread("DBHELPER" + TAG);
            dbThread.start();
        }
        if (dbHandler == null) {
            dbHandler = new Handler(dbThread.getLooper());
        }
        dbHandler.post(dbRunnable);
    }

    private void stopDbHelper() {
        isDBRunning = false;
        if (dbHandler != null) {
            dbHandler.removeCallbacks(dbRunnable);
        }
    }

    private void startEvent() {
        if (isEventRunning) {
            return;
        }

        isEventRunning = true;
        if (eventThread == null) {
            eventThread = new HandlerThread(TAG);
            eventThread.start();
        }
        if (eventHandler == null) {
            eventHandler = new Handler(eventThread.getLooper());
        }
        eventHandler.post(eventRunnable);
        verifyDbHelper();

    }

    private void stopEvent() {
        LogUtil.d(TAG, "stopEvent	");
        isEventRunning = false;
        mConnectInfo = null;
        if (mConnector != null) {
            mConnector.disconnect();
            // mConnector = null;
        }
        eventHandler.removeCallbacks(eventRunnable);
    }

    private int execute() {
        if (mConnector == null) {
            mConnector = new Connector();
        } else {
            mConnector.clear();
        }
        if (mConnectInfo == null) {
            mConnectInfo = getlistEventInfo(100);
        }
        if (mConnectInfo == null) {
            LogUtil.e(TAG, "CANT GET AUTHORIZATION!!!");
            return -1;
        }
        mConnector.setConnectInfo(mConnectInfo);
        mConnector.connect();
        if (mConnector.getStatusCode() == 200) {
            parserEvent(mConnector.getContent());
        }
        mConnector.disconnect();
        return mConnector.getStatusCode();
    }

    int albumDeleteCount;
    int albumDeleteOK;

    /**
     * *********************************************** handleEvent *************************************************
     */

    private synchronized void parserEvent(String content) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "parserEvent	" + new Date() + "	" + content);
        }
        ArrayList<Event> events = Parser.parseEventList(content);
        if (Utils.isEmpty(events)) {
            events = null;
            return;
        }
        addTask(events);
    }

    /**
     * <pre>
     * FILE_CREATED is one hour ago will NOT be send to fragment!
     *
     * <pre>
     *
     * @param events
     * @time Jul 28, 2014
     */
    private synchronized void handleEvent(ArrayList<Event> events) {
        if (tmpEventMap == null) {
            tmpEventMap = new HashMap<String, Event>();
        }
        if (dbHelper != null) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            dimissDuplicatedCreate(events);
            if (App.DEBUG) {
                LogUtil.d(TAG, "handleEvent	True size:" + Utils.sizeOf(events));
            }
            for (int i = 0; i < events.size(); i++) {
                if (i < 0) {
                    i = 0;
                }
                Event event = events.get(i);
                if (event == null) {
                    continue;
                }
                String opType = event.getOpType();
                if (Utils.isEmpty(opType)) {
                    continue;
                }
                Object obj = event.getObject();
                if (obj instanceof AlbumEntity) {
                    // ALBUM
                    AlbumEntity album = (AlbumEntity) event.getObject();
                    if (opType.equals(Consts.ALBUM_DELETED) || opType.equals(Consts.SYSTEM_ALBUM_DELETED)) {
                        albumDeleteCount++;
                        boolean result = album.delete(db);
                        if (App.DEBUG) {
                            if (result) {
                                albumDeleteOK++;
                            }
                            LogUtil.d("DELETE_ALBUM", "DELETE_ALBUM	name=" + album.getName() + "	albumDeleteCount=" + albumDeleteCount + "	OK=" + albumDeleteOK);
                        }
                        FileEntity.EMPTY.deleteFilesInAlbum(db, album.getId());
                    } else if (opType.equals(Consts.ALBUM_CREATED)) {
                        if (App.DEBUG) {
                            LogUtil.d("ALBUM_CREATED", "ALBUM_CREATED	name=" + album.getName());
                        }
                        album.insert(db);
                    } else if (opType.equals(Consts.ALBUM_UPDATED)) {
                        album.keepLocal(true);
                        album.update(db);
                        album.keepLocal(false);
                    }
                } else if (obj instanceof FileEntity) {
                    // FILE
                    FileEntity fileEntity = (FileEntity) obj;
                    if (opType.equals(Consts.FILE_CREATED) || opType.equals(Consts.FILE_DELETED) || opType.equals(Consts.SYSTEM_FILE_DELETED)) {
                        String key = event.getCreatedDate() + "_" + fileEntity.getId();
                        if (tmpEventMap.containsKey(key)) {
                            Event lastEvent = tmpEventMap.remove(key);
                            events.remove(i);
                            i--;
                            events.remove(lastEvent);
                            i--;
                        } else {
                            tmpEventMap.put(key, event);
                            if (opType.equals(Consts.FILE_DELETED) || opType.equals(Consts.SYSTEM_FILE_DELETED)) {
                                fileEntity.delete(db);
                                AlbumEntity.EMPTY.changeSize(db, fileEntity.getAlbum(), Consts.SIZE, false);
                            } else if (opType.equals(Consts.FILE_CREATED)) {
                                fileEntity.insert(db);
                                long timeInMills = TimeUtil.toLong(fileEntity.getCreateDate(), Consts.SERVER_UTC_FORMAT);
                                if (System.currentTimeMillis() - timeInMills >= Consts.MIN_IN_MILLS * 10) {
                                    events.remove(i);
                                    i--;
                                } else {
                                    AlbumEntity.EMPTY.changeSize(db, fileEntity.getAlbum(), Consts.SIZE, true);
                                    AlbumEntity.EMPTY.changeSize(db, fileEntity.getAlbum(), Consts.UPDATE_COUNT, true);
                                }
                            }
                            // else {
                            // fileEntity.delete(db);
                            // AlbumEntity.EMPTY.changeSize(db,
                            // fileEntity.getAlbum(), Consts.SIZE, false);
                            // }

                        }
                    } else if (opType.equals(Consts.FILE_SHARED)) {
                        FileEntity.EMPTY.changeSize(db, fileEntity.getId(), Consts.SHARES, true);
                    } else if (opType.equals(Consts.FILE_DOWNLOADED)) {
                        FileEntity.EMPTY.changeSize(db, fileEntity.getId(), Consts.DOWNLOADS, true);
                    }
                } else if (obj instanceof MemberEntity) {
                    // MEMBER
                    MemberEntity mb = (MemberEntity) obj;
                    if (opType.equals(Consts.MEMBER_LEFT)) {
                        // update album list
                        if (mb.getUserId().equals(App.getUid())) {
                            AlbumEntity.EMPTY.delete(db, mb.getAlbumId());
                        } else {
                            AlbumEntity.EMPTY.changeSize(db, mb.getAlbumId(), Consts.MEMBERS, false);
                        }
                    } else if (opType.equals(Consts.JOIN_ALBUM)) {
                        if (mb.getUserId().equals(App.getUid())) {// ???????
                            AlbumEntity.EMPTY.delete(db, mb.getAlbumId());
                        } else {
                            AlbumEntity.EMPTY.changeSize(db, mb.getAlbumId(), Consts.MEMBERS, true);
                        }
                    } else if (opType.equals(Consts.MEMBER_UPDATED)) {
                        onMemberUpdated(mb);
                        // events.remove(i);
                    }
                } else if (obj instanceof LikeEntity) {
                    LikeEntity like = (LikeEntity) obj;
                    String sql = "DELETE FROM " + event.getTableName() + " WHERE " + Consts.OBJ_ID + "=\"" + Event.getObjectWidthId(like) + "\"";
                    dbHelper.execute(sql, db);
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "LIKE	" + sql);
                    }
                    if (opType.equals(Consts.LIKE_CREATED)) {
                        FileEntity.EMPTY.changeSize(db, like.getObjId(), Consts.LIKES, true);
                    } else if (opType.equals(Consts.LIKE_DELETED)) {
                        FileEntity.EMPTY.changeSize(db, like.getObjId(), Consts.LIKES, false);
                    }
                } else if (obj instanceof CommentEntity) {
                    CommentEntity commentEntity = (CommentEntity) obj;
                    String sql = "DELETE FROM " + event.getTableName() + " WHERE " + Consts.OBJ_ID + "=\"" + Event.getObjectWidthId(commentEntity) + "\"";
                    dbHelper.execute(sql, db);
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "CommentEntity	" + sql);
                    }
                    if (opType.equals(Consts.COMMENT_CREATED)) {
                        FileEntity.EMPTY.changeSize(db, commentEntity.getObjId(), Consts.COMMENTS, true);
                    } else if (opType.equals(Consts.COMMENT_DELETED)) {
                        onDeleteComment(commentEntity.getId());
                        FileEntity.EMPTY.changeSize(db, commentEntity.getObjId(), Consts.COMMENTS, false);
                    }
//                    else if (opType.equals())
                } else {
                    // if (opType.equals(Consts.SYSTEM_USER_DISABLED)) {
                    // //
                    // ConnectBuilder.unbindDevice(Consts.FORCE_UNBIND_DEVICE);
                    // UnbindDevice.unbindDevice();
                    // DialogUtils.dialog(R.string.account_suspended);
                    // App.getActivity().finish();
                    // }

                }
                // just save notification event
                if (EventManger.needShow(event, db)) {
                    if (App.DEBUG) {
                        LogUtil.d(TAG, "handleEvent	needShow！	" + event.getUserName() + "	" + event.getOpType());
                    }
                    event.insert(db);
                }
            }
            db.close();
        }
        if (tmpEventMap != null) {
            tmpEventMap.clear();
        }
        Intent intent = new Intent(Consts.GET_EVENTS);
        intent.putExtra(Consts.EVENT_LIST, events);
        Broadcaster.sendBroadcast(intent);
        if (App.DEBUG) {
            LogUtil.d(TAG, "handleEvent	EVENT_LIST Broadcast	" + Utils.sizeOf(events));
        }
    }

    /**
     * event is invalid when a file is deleted after it's been created
     */
    private void dimissDuplicatedCreate(List<Event> events) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "dimissDuplicatedCreate	" + Utils.sizeOf(events));
        }
        HashMap<String, Event> tmpMap = new HashMap<String, Event>();
        for (int i = 0; i < Utils.sizeOf(events); i++) {
            Event event = events.get(i);
            if (event == null) {
                continue;
            }
            Object obj = event.getObject();
            if (!(obj instanceof FileEntity)) {
                continue;
            }

            FileEntity entity = (FileEntity) obj;
            String fileId = entity.getId();
            if (TextUtils.isEmpty(fileId)) {
                continue;
            }
            if (tmpMap.containsKey(fileId)) {
                Event oldEvent = tmpMap.get(fileId);
                if (oldEvent == null) {
                    continue;
                }
                String oldType = oldEvent.getOpType();
                String curType = event.getOpType();
                if (oldType.equals(curType)) {
                    Log.d(TAG, "!!! got duplicated event " + fileId);
                    continue;
                } else if ((curType.equals(Consts.FILE_CREATED) && oldType.equals(Consts.FILE_DELETED))//
                        || (curType.equals(Consts.FILE_DELETED) && oldType.equals(Consts.FILE_CREATED))//
                        ) {
                    // remove duplicated event
                    tmpMap.remove(fileId);
                    // remove current event;
                    if (events.remove(oldEvent)) {
                        i--;
                    }
                    if (events.remove(i) != null) {
                        i--;
                    }
                    if (App.DEBUG) {
                        Log.d(TAG, "!!! dimiss opposite event " + fileId);
                    }
                    continue;
                }
            }
            tmpMap.put(fileId, event);
        }
        tmpMap.clear();
    }

    private boolean onMemberUpdated(MemberEntity member) {
        if (member == null) {
            return false;
        }
        if (!Consts.OWNER.equals(member.getRole())) {
            return false;
        }
        if (dbHelper == null) {
            return false;
        }
        dbHelper.execute("UPDATE " + AlbumEntity.EMPTY.getTableName() + " SET " + Consts.OWNER + "=\"" + member.getUserId() + "\"" + " WHERE " + Consts.ALBUM_ID + "=\"" + member.getAlbumId() + "\"");
        return false;
    }

    private final ConnectInfo getlistEventInfo(int limit) {
        if (limit < 0) {
            limit = 100;
        } else if (limit > 100) {
            limit = 100;
        }
        HashMap<String, String> auth = ConnectBuilder.getAuthentication();
        if (auth == null) {
            return null;
        }
        StringBuilder url = new StringBuilder(Consts.HOST_HTTPS + Consts.METHOD_GET_EVENTS);
        url.append("?limit=" + limit);
        ConnectInfo info = new ConnectInfo(Consts.GET_EVENTS);
        info.addHeaders(auth);
        info.setURL(url.toString());
        info.setMethod(HttpMethod.GET);
        return info;
    }

    /**
     * {@link #stopWork()} and stopEvent
     */
    private void stopRecycling() {
        LocateManager.instance().stop();
        stopEvent();
    }

    // //////updateUserLocation
    private void updateUserGeo() {
        // final long timeMills = System.currentTimeMillis();
        // if (App.DEBUG) {
        // LogUtil.d(TAG, "updateUserGeo()	loop=" + (timeMills -
        // lastGeoLoopTimeInMill) + "	update=" + (timeMills -
        // lastGeoUpdateTimeInMill));
        // }
        // if (timeMills - lastGeoLoopTimeInMill <= USER_GEO_LOOP_DURATION) {
        // return;
        // }
        // lastGeoLoopTimeInMill = timeMills;
        // if (timeMills - lastGeoUpdateTimeInMill <= USER_GEO_UPDATE_DURATION)
        // {
        // return;
        // }
        // lastGeoUpdateTimeInMill = timeMills;
        // final Location location = LocateManager.instance().getLocation();
        // if (location == null) {
        // return;
        // }
        // LocateManager.instance().stop();
        // final long distance = location.distanceTo(lastLocation);
        // if (distance <= USER_GEO_ACCURACY) {
        // return;
        // }
        // ConnectBuilder.updateUserGeo(location);
        // lastLocation = location;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (App.DEBUG) {
            LogUtil.d(TAG, "EventService onStartCommand	" + System.currentTimeMillis());
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        if (App.DEBUG) {
            LogUtil.d(TAG, "EventService onDestroy	" + System.currentTimeMillis());
        }
        serviceRunning = false;
        stopRecycling();
        stopEvent();
        Broadcaster.unregisterReceiver(mReceiver);
        if (eventThread != null) {
            eventThread.quit();
        }
        ImageManager.instance().clearMemory();
        super.onDestroy();
    }

    public final static boolean running() {
        return serviceRunning;
    }

    private void verifyDbHelper() {
        if (Utils.isEmpty(App.getUid())) {
            dbHandler = null;
            return;
        }
        if (dbHelper == null) {
            dbHelper = new DBHelper(App.getUid());
        }
    }

    boolean newPhoto = false;

    class HomeKeyBroadcastReceiver extends BroadcastReceiver {
        final String SYSTEM_DIALOG_REASON = "reason";
        final String HOME_KEY = "homekey";
        final String RECENT_APPS = "recentapps";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON);
                if (reason != null) {
                    if (reason.equals(HOME_KEY)) {
                        // TODO stop event
                        stopRecycling();
                        // TODO  setAlarm to wakeup send loacl push
                        AppData.setQuitTime(System.currentTimeMillis());
                        InitAlarm.initAlarm();
                        // TODO new user not bind phone will logout App
                        if (AppData.getFirstBindPhone()) {
                            App.getActivity().finish();
                            UnbindDevice.unbindDevice(false);
                            AppData.setFirstBindPhone(false);

                        }
                    }
                }
            } else if (action.equals(Intent.ACTION_SCREEN_OFF)) {
                stopRecycling();
            }
        }
    }

    /**
     * on file create intent receive
     *
     * @param intent
     */
    private void onFileCreate(Intent intent) {
        verifyDbHelper();
        String action = intent.getAction();
        if (Utils.isEmpty(action)) {
            return;
        }
        if (dbHelper == null) {
            return;
        }
        SQLiteDatabase dbDatabase = dbHelper.getWritableDatabase();
        try {
            Response response = intent.getParcelableExtra(Consts.RESPONSE);
            if (response.getStatusCode() != HttpStatus.SC_OK) {
                return;
            }
            String content = response.getContent();
            FileEntity fileEntity = Parser.parseFile(content);
            if (fileEntity == null) {
                return;
            }
            if (UploadCancelManager.sharedInstance().checkFileEntity(fileEntity)) {
                if (fileEntity.isActive()) {
                    UploadCancelManager.sharedInstance().deleteFileFromServer(fileEntity);
                }
                return;
            }
            if (fileEntity.isActive()) {
                fileEntity.setUploaded(dbDatabase);
            } else {
                fileEntity.setStatus(Consts.FILEENTITY_STATUS_UPLOADING);
                fileEntity.setFileCreateSuccess(dbDatabase);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dbDatabase.close();
        }
    }

    private void handleUnAuthFile(ConnectInfo info, FileEntity fileEntity) {
        String fileId = null;
        if (fileEntity != null) {
            fileId = Utils.createEntityHashId(fileEntity);
        } else {
            fileId = Parser.parseFileFakeId(info.getTag());
        }
        if (Utils.isEmpty(fileId)) {
            return;
        }
        if (Utils.isEmpty(fileId)) {
            return;
        }
        dbHelper.delete(FileEntity.EMPTY, fileId);
    }

    private void checkUnloadedFile() {
        if (App.DEBUG) {
            LogUtil.v(TAG, "checkUnloadedFile");
        }
        if (FileUploadManager.instance().hasTaskRunning()) {
            return;
        }
        try {
            verifyDbHelper();
            if (dbHelper != null) {
                HashMap<String, List<UploadImage>> uploadMap = new HashMap<String, List<UploadImage>>();
                List<UploadImage> needUploads = Utils.getUploadingImagesAndSync(dbHelper);
                // set up uploadMap
                for (UploadImage uploadImage : needUploads) {
                    if (uploadMap.get(uploadImage.getBatchId()) == null) {
                        uploadMap.put(uploadImage.getBatchId(), new ArrayList<UploadImage>());
                    }
                    uploadMap.get(uploadImage.getBatchId()).add(uploadImage);
                }
                if (App.DEBUG) {
                    LogUtil.v(TAG, "uploadImage");
                }
                // put each batch into ImageManager
                for (Entry<String, List<UploadImage>> entry : uploadMap.entrySet()) {
                    ImageManager.instance().uploadImage(entry.getValue(), entry.getKey());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
