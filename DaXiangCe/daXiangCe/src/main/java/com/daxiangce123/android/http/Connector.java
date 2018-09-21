package com.daxiangce123.android.http;

import android.content.Intent;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.HttpMethod;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.monitor.NetworkMonitor;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.AppUtil;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.HttpResponse;
import com.yunio.httpclient.HttpStatus;
import com.yunio.httpclient.client.HttpClient;
import com.yunio.httpclient.client.methods.HttpEntityEnclosingRequestBase;
import com.yunio.httpclient.client.methods.HttpGet;
import com.yunio.httpclient.client.methods.HttpPost;
import com.yunio.httpclient.client.methods.HttpPut;
import com.yunio.httpclient.client.methods.HttpRequestBase;
import com.yunio.httpclient.conn.ClientConnectionManager;
import com.yunio.httpclient.util.CharArrayBuffer;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.HashMap;

//import com.yunio.httpclient.client.methods.HttpRequestBase;

public class Connector {
    private static final String TAG = "Connector";

    private ConnectInfo mConnectInfo;
    private HttpResponse mResponse;
    private HttpClient mClient;
    private HttpRequestBase mRequest;
    private ProgressListener mListener;
    private Error error;

    private int mStatusCode = Consts.RESPONSE_STATUS_DEFAULT;
    private ErrorCode mErrCode;
    private String mContent;
    private static boolean DEBUG = true;

    public Connector() {
        mErrCode = ErrorCode.NONE;
        if (DEBUG) {
            DEBUG = App.DEBUG;
        }
    }

    ;

    public Connector(ConnectInfo info) {
        this();
        mConnectInfo = info;
    }

    public ConnectInfo getConnectInfo() {
        return mConnectInfo;
    }

    public void setConnectInfo(ConnectInfo info) {
        mConnectInfo = info;
    }

    public void setListener(ProgressListener listener) {
        this.mListener = listener;
    }

    public ProgressListener getListener() {
        return this.mListener;
    }

    public void connect() {
        try {
            connect(false);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (mStatusCode != 200) {
                error = Parser.parseErrors(mContent);
            }
            if (error != null) {
                mErrCode = error.toErrorCode();
            }
        }
    }

    public InputStream connect(boolean justIPS) {
        if (DEBUG) {
            LogUtil.d(TAG, "-------------CONNECT------------>" + mConnectInfo.getType() + "  waitTime=" + (System.currentTimeMillis() - mConnectInfo.getCreateTime()) / 1000f);

        }
        boolean connected = NetworkMonitor.networkConnected();
        if (!connected) {
            mErrCode = ErrorCode.NETWORK_ERROR;
            LogUtil.e(TAG, "network not connected");
            return null;
        }

        if (AppUtil.isMainThread()) {
            LogUtil.e(TAG, "network on main thread");
            return null;
        }

        if (mConnectInfo == null) {
            mErrCode = ErrorCode.INVALID_PARAMS;
            LogUtil.e(TAG, "invalid request info");
            return null;
        }

        // instantiate request based on method parameter
        String url = mConnectInfo.getURL();
        if (Utils.isEmpty(url)) {
            mErrCode = ErrorCode.INVALID_PARAMS;
            LogUtil.e(TAG, "invalid url: " + url);
            return null;
        }
        HttpMethod method = mConnectInfo.getMethod();
        String extraToken = null;
        if (url.contains(Consts.ACCESS_TOKEN_TAG)) {
            int index = url.indexOf(Consts.ACCESS_TOKEN_TAG);
            String token = url.substring(index + Consts.ACCESS_TOKEN_TAG.length());
            extraToken = ConnectBuilder.corpBearer(token);
            url = url.substring(0, index);

            if (DEBUG) {
                LogUtil.d(TAG, "ACCESS_TOKEN_TAG	" + url + "	token=" + extraToken);
            }
        }
        try {
            if (method == HttpMethod.GET) {
                mRequest = new HttpGet(url);
            } else if (method == HttpMethod.POST) {
                mRequest = new HttpPost(url);
            } else if (method == HttpMethod.PUT) {
                mRequest = new HttpPut(url);
            } else if (method == HttpMethod.DELETE) {
                mRequest = new HttpDelete(url);
            } else {
                mErrCode = ErrorCode.METHOD_NOT_ALLOWED;
                LogUtil.e(TAG, "invalid method: " + method);
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        // fill up request headers
        HashMap<String, String> headers = (HashMap<String, String>) mConnectInfo.getHeaders();
        mRequest.addHeader(Consts.SOURCE, Consts.SOURCE_ANDROID);
        mRequest.addHeader(Consts.USER_AGENT, "cliq" + App.mobileInfo.VERSION);
        if (!Utils.isEmpty(extraToken)) {
            mRequest.addHeader(Consts.AUTHORIZATION, extraToken);
        }
        for (HashMap.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (!Utils.isEmpty(extraToken) && key.equals(Consts.AUTHORIZATION)) {
                continue;
            }
            String value = entry.getValue();
            mRequest.addHeader(key, value);
        }

        // set entity TODO 获得HttpEntity
        HttpEntity entity = mConnectInfo.getEntity();
        if (mListener != null && (entity instanceof UploadEntity)) {
            UploadEntity uEntity = (UploadEntity) entity;
            uEntity.setListener(mListener);
            uEntity.reset();
        }

        if (mRequest instanceof HttpEntityEnclosingRequestBase && entity != null) {
            ((HttpEntityEnclosingRequestBase) mRequest).setEntity(entity);
        }

        int timeout = mConnectInfo.getTimeout();
        if (timeout <= 0) {
            mErrCode = ErrorCode.INVALID_PARAMS;
            LogUtil.w(TAG, "invalid timeout: " + timeout);
            return null;
        }

        mClient = HttpClientGenerator.getHttpClient(timeout);
        // HttpClientGenerator.getOKHttpClient(timeout);
        // mClient = HttpClientGenerator.AsyncHttpClient(timeout);
        // debug request parameters

        // execute request
        try {
            mResponse = mClient.execute(mRequest);
        } catch (UnknownHostException e) {
            // TODO 实际是DNS挂了
            mErrCode = ErrorCode.NETWORK_ERROR;
            e.printStackTrace();
            if (DEBUG) {
                LogUtil.d(TAG, "UnknownHostException " + e.getMessage());
            }
        } catch (SocketTimeoutException e) {
            mErrCode = ErrorCode.TIME_OUT;
            e.printStackTrace();
            if (DEBUG) {
                LogUtil.d(TAG, "SocketTimeoutException	" + e.getMessage());
            }
        } catch (SocketException e) {
            if (mConnectInfo.getType().equals(Consts.UPLOAD_FILE) && e.getMessage().contains("ECONNRESET")) {
                mStatusCode = 200;
            } else {
                mErrCode = ErrorCode.UNKNOWN;
                e.printStackTrace();
                if (DEBUG) {
                    LogUtil.d(TAG, "Exception	" + e.getClass().getName() + "	Message:" + e.getMessage() + " ");
                }
            }
        } catch (Exception e) {
            mErrCode = ErrorCode.UNKNOWN;
            e.printStackTrace();
            if (DEBUG) {
                LogUtil.d(TAG, "Exception	" + e.getClass().getName() + "	Message:" + e.getMessage() + " ");
            }
        } finally {
            if (mResponse == null) {
                return null;
            }
        }

        mStatusCode = mResponse.getStatusLine().getStatusCode();
        if (DEBUG) {
            LogUtil.d(TAG, "mStatusCode " + mStatusCode);
        }
        // get entity input stream
        InputStream ips = null;
        HttpEntity responseEntity = mResponse.getEntity();
        if (responseEntity == null) {
            return null;
        }

        try {
            ips = responseEntity.getContent();
        } catch (Exception e) {
            // e.printStackTrace();
            return null;
        } finally {
            if (ips == null) {
                return null;
            }
        }
        if (justIPS) {
            return ips;
        }

        // check if a download file task.
        DownloadEntity dEntity = mConnectInfo.getWriter();
        if (method == HttpMethod.GET && dEntity != null && mStatusCode == HttpStatus.SC_OK) {
            if (mListener != null) {
                dEntity.setListener(mListener);
            }
            dEntity.input(ips);
            return null;
        } else {
            // save server response body as string
            int length = (int) responseEntity.getContentLength();
            if (length < 0) {
                length = Consts.IO_BUFFER_SIZE;
            }
            try {
                Reader reader = new InputStreamReader(ips, Consts.CHARSET_UTF_8);
                CharArrayBuffer buffer = new CharArrayBuffer(length);
                char[] tmp = new char[Consts.IO_BUFFER_SIZE];
                int readSize;
                while ((readSize = reader.read(tmp)) != -1) {
                    buffer.append(tmp, 0, readSize);
                }
                mContent = buffer.toString();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (mContent == null) {
                    return null;
                }
            }
            return null;
        }
    }

    public void clear() {
        mConnectInfo = null;
        mResponse = null;
        mErrCode = ErrorCode.NONE;
        mStatusCode = Consts.RESPONSE_STATUS_DEFAULT;
        mContent = null;
        error = null;
    }

    public boolean disconnect() {
        if (mRequest == null) {
            LogUtil.e(TAG, "invalid request");
            return false;
        }

        if (AppUtil.isMainThread()) {
            LogUtil.e(TAG, "network on main thread");
            return false;
        }

        mRequest.abort();
        mRequest = null;
        if (mClient == null) {
            LogUtil.e(TAG, "invalid client");
            return false;
        }
        ClientConnectionManager manager = mClient.getConnectionManager();
        manager.shutdown();

        return true;
    }

    public void response() {
        if (mConnectInfo == null || !mConnectInfo.valid()) {
            return;
        }
        Response response = new Response();
        response.setStatusCode(mStatusCode);
        response.setErrCode(mErrCode);
        response.setContent(mContent);
        response.setError(error);


        if (response.getStatusCode() == 401) {
            Intent forceCloseIntent = new Intent(Consts.SYSTEM_USER_DISABLED);
            forceCloseIntent.putExtra(Consts.REQUEST, mConnectInfo);
            forceCloseIntent.putExtra(Consts.RESPONSE, response);
            Broadcaster.sendBroadcast(forceCloseIntent);
        }
        Intent intent = new Intent(mConnectInfo.getType());
        intent.putExtra(Consts.REQUEST, mConnectInfo);
        intent.putExtra(Consts.RESPONSE, response);
        Broadcaster.sendBroadcast(intent);

        if (DEBUG) {
            // LogUtil.d(TAG, mConnectInfo.toString());
            LogUtil.d(TAG, "-- respone time--" + System.currentTimeMillis() + "   -- " + mConnectInfo.getType().toUpperCase());

            LogUtil.d(TAG, "------RESPONSE" + mConnectInfo.getType().toUpperCase() + "------ " + mErrCode + "/" + mStatusCode + "	" + mContent);
        }
    }

    public int getStatusCode() {
        return mStatusCode;
    }

    public ErrorCode getError() {
        return mErrCode;
    }

    public String getContent() {
        return mContent;
    }
}
