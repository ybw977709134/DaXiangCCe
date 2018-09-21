package com.daxiangce123.android.uil;

import android.content.Context;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.LogUtil;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ContentLengthInputStream;
import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.nostra13.universalimageloader.utils.IoUtils;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;

/**
 * @author ram
 * @project DaXiangCe
 * @time Jul 4, 2014
 */
public class ImageDownloader extends BaseImageDownloader {

    private final static String TAG = "ImageDownloader";

    public ImageDownloader(Context context) {
        super(context);
    }

    @Override
    protected HttpURLConnection createConnection(String url, Object extra) throws IOException {
        HashMap<String, String> auth = ConnectBuilder.getAuthentication();
        if (auth == null) {
            return super.createConnection(url, extra);
        }
        String token = auth.get(Consts.AUTHORIZATION);
        if (url.contains(Consts.ACCESS_TOKEN_TAG)) {
            int index = url.indexOf(Consts.ACCESS_TOKEN_TAG);
            token = url.substring(index + Consts.ACCESS_TOKEN_TAG.length());
            token = ConnectBuilder.corpBearer(token);
            url = url.substring(0, index);
        }

        HttpURLConnection conn = super.createConnection(url, extra);
        conn.setRequestMethod("GET");
        for (HashMap.Entry<String, String> entry : auth.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            conn.setRequestProperty(key, value);
        }
        conn.setRequestProperty(Consts.AUTHORIZATION, token);
        if (App.DEBUG) {
            LogUtil.d(TAG, "token=" + token + "	url=" + url);
        }
        return conn;
    }

    protected HttpURLConnection createConnectionWithPost(String url, Object extra) throws IOException {
        HashMap<String, String> auth = ConnectBuilder.getAuthentication();
        if (auth == null) {
            return super.createConnection(url, extra);
        }
        String token = auth.get(Consts.AUTHORIZATION);
        HttpURLConnection conn = super.createConnection(url, extra);
        conn.setRequestMethod("POST");
        for (HashMap.Entry<String, String> entry : auth.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            conn.setRequestProperty(key, value);
        }
        conn.setRequestProperty(Consts.AUTHORIZATION, token);
        if (App.DEBUG) {
            LogUtil.d(TAG, "token=" + token + "	url=" + url);
        }
        return conn;
    }

    protected HttpURLConnection createConnectionWithTempToken(String url, Object extra) throws IOException {
        HashMap<String, String> auth = ConnectBuilder.getAuthentication();
        if (auth == null || App.NET_STATE.equals(Consts.INTERNAL)) {
            return super.createConnection(url, extra);
        } else {

        }
        String token = ConnectBuilder.getTempHttpToken();
        if (token.isEmpty()) {
            token = requestTempToken();
        }
        ConnectBuilder.corpBearer(token);

        HttpURLConnection conn = super.createConnection(url, extra);
        conn.setRequestMethod("GET");
        for (HashMap.Entry<String, String> entry : auth.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            conn.setRequestProperty(key, value);
        }
        conn.setRequestProperty(Consts.AUTHORIZATION, token);
        if (App.DEBUG) {
            LogUtil.d(TAG, "createConnectionWithTempToken token=" + token + "	url=" + url);
        }
        return conn;
    }

    /**
     * Retrieves {@link InputStream} of image by URI (image is located in the
     * network).
     *
     * @param imageUri Image URI
     * @param extra    Auxiliary object which was passed to
     *                 {@link DisplayImageOptions.Builder#extraForDownloader(Object)
     *                 DisplayImageOptions.extraForDownloader(Object)}; can be null
     * @return {@link InputStream} of image
     * @throws IOException if some I/O error occurs during network request or if no
     *                     InputStream could be created for URL.
     */
    protected InputStream getStreamFromNetwork(String imageUri, Object extra) throws IOException {
        HttpURLConnection conn = null;
        long start = System.currentTimeMillis();
        int redirectCount = 0;
        if (imageUri.contains(Consts.METHOD_GET_FIEL_DWRUL)) {
            try {
                String dwUrl = getDWUrl(imageUri, extra);
                conn = super.createConnection(dwUrl, extra);
                if (conn.getResponseCode() != 200) {
                    imageUri.replace(Consts.METHOD_GET_FIEL_DWRUL, Consts.METHOD_DOWNLOAD_FILE);
                    conn = createConnection(imageUri, extra);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageUri.replace(Consts.METHOD_GET_FIEL_DWRUL, Consts.METHOD_DOWNLOAD_FILE);
                conn = createConnection(imageUri, extra);
            }

        } else {
            if (imageUri.contains(Consts.METHOD_DOWNLOAD_THUMB)) {

                // use temp http token
                conn = createConnectionWithTempToken(imageUri, extra);
            } else if (imageUri.contains(Consts.METHOD_DOWNLOAD_AVATAR)) {
                // use temp http token
                conn = createConnectionWithTempToken(imageUri, extra);
            } else {
                conn = createConnection(imageUri, extra);
            }
        }
        while (conn.getResponseCode() / 100 == 3 && redirectCount < MAX_REDIRECT_COUNT) {
            conn = createConnection(conn.getHeaderField("Location"), extra);
            redirectCount++;
        }
        if (conn.getResponseCode() == 401) {
            ConnectBuilder.setTempHttpToken("");
            conn = createConnectionWithTempToken(imageUri, extra);
        }

        InputStream imageStream;
        try {
            imageStream = conn.getInputStream();
        } catch (IOException e) {
            // Read all data to allow reuse connection (http://bit.ly/1ad35PY)
            IoUtils.readAndCloseStream(conn.getErrorStream());
            throw e;
        }
        // catAvgTime(System.currentTimeMillis() - start);
        return new ContentLengthInputStream(new BufferedInputStream(imageStream, BUFFER_SIZE), conn.getContentLength());
    }

    private String getDWUrl(String imageUri, Object extra) throws IOException {
        HttpURLConnection conn = createConnection(imageUri, extra);

        // LogUtil.e(TAG, conn.getResponseCode() + " res" + response.toString()
        // + "||\n " + imageUri);
        return Parser.parseDWUrl(readConnection(conn));
    }

    protected String requestTempToken() {
        String url = Consts.HOST_HTTPS + Consts.METHOD_GET_METHOD_TEMP_TOKEN;
        try {
            HttpURLConnection conn = createConnectionWithPost(url, null);
            String tempToken = Parser.parseTempHttpTokenContent(readConnection(conn));
            if (tempToken != null) {
                if (App.DEBUG) {
                    LogUtil.d(TAG, "getAuth() get temptoken and set:" + tempToken);
                }
                ConnectBuilder.setTempHttpToken(tempToken);
                return tempToken;
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return "";
    }

    private String readConnection(HttpURLConnection conn) {
        String inputLine = null;
        StringBuffer response = new StringBuffer();
        InputStream iStream = null;
        BufferedReader in = null;
        try {
            if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
                iStream = conn.getInputStream();
                in = new BufferedReader(new InputStreamReader(iStream));
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (iStream != null) {
                    iStream.close();
                }
                conn.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return response.toString();
    }

}
