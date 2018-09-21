package com.daxiangce123.android.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.Consts;
import com.daxiangce123.android.Consts.HttpMethod;
import com.daxiangce123.android.http.DownloadEntity;
import com.daxiangce123.android.util.Utils;
import com.yunio.httpclient.HttpEntity;
import com.yunio.httpclient.entity.StringEntity;
import com.yunio.httpclient.util.EntityUtils;
import com.daxiangce123.android.Consts.Priority;

import java.util.HashMap;

public class ConnectInfo implements Parcelable {
    public static final String TAG = "ConnectInfo";

    private String mTag;
    private String mTag2;
    private String mTag3;
    private String mFakeId;
    private String mURL;
    private HttpMethod mMethod;
    private int mTimeout;
    private HttpEntity mEntity;
    private HashMap<String, String> mHeaders;
    private String mType;
    private DownloadEntity mWriter;
    private Runnable mRunner;
    private String uniqueId;
    private Object obj;
    private long createTime;
    private long uniqId;
    private int repectTime = 10;
    private Priority mPriority;

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public ConnectInfo(String type) {
        mType = type;
        mTimeout = Consts.TIMEOUT_SHORT;
        mEntity = null;
        mHeaders = new HashMap<String, String>();
        mMethod = HttpMethod.NONE;
        mPriority=Priority.LOW;
        createTime = System.currentTimeMillis();
        generateUniqueId();
    }

    public long getUniqId() {
        return uniqId;
    }

    public void setUniqId(long uniqId) {
        this.uniqId = uniqId;
    }

    private ConnectInfo(Parcel parcel) {
        this("");
    }

    public String getURL() {
        return mURL;
    }

    public ConnectInfo setURL(String url) {
        this.mURL = url;
        generateUniqueId();
        return this;
    }

    public String getTag2() {
        return mTag2;
    }

    public ConnectInfo setTag2(String tag2) {
        this.mTag2 = tag2;
        generateUniqueId();
        return this;
    }

    public String getTag3() {
        return mTag3;
    }

    public ConnectInfo setTag3(String tag3) {
        this.mTag3 = tag3;
        generateUniqueId();
        return this;
    }

    public String getTag() {
        return mTag;
    }

    public ConnectInfo setTag(String tag) {
        this.mTag = tag;
        generateUniqueId();
        return this;
    }

    public ConnectInfo setPriority(Priority priority) {
        this.mPriority = priority;
        return this;
    }

    public Priority getPriority() {
        return mPriority;
    }

    public String getType() {
        return mType;
    }

    public ConnectInfo setType(String type) {
        this.mType = type;
        generateUniqueId();
        return this;
    }

    public Runnable getRunner() {
        return this.mRunner;
    }

    public ConnectInfo setRunner(Runnable runner) {
        this.mRunner = runner;
        return this;
    }

    public HttpMethod getMethod() {
        return this.mMethod;
    }

    public int getTimeout() {
        return this.mTimeout;
    }

    public ConnectInfo setTimeout(int timeout) {
        this.mTimeout = timeout;
        return this;
    }

    public DownloadEntity getWriter() {
        return this.mWriter;
    }

    public ConnectInfo setWriter(DownloadEntity writer) {
        this.mWriter = writer;
        return this;
    }

    public ConnectInfo setMethod(HttpMethod method) {
        this.mMethod = method;
        return this;
    }

    public HttpEntity getEntity() {
        return this.mEntity;
    }

    public ConnectInfo setEntity(HttpEntity entity) {
        mEntity = entity;
        return this;
    }

    public HashMap<String, String> getHeaders() {
        return this.mHeaders;
    }

    public ConnectInfo addHeader(String name, String value) {
        if (!Utils.isEmpty(name) && !Utils.isEmpty(value)) {
            mHeaders.put(name, value);
        }
        return this;
    }

    public ConnectInfo addHeaders(HashMap<String, String> headers) {
        if (headers == null || headers.size() == 0) {
            return this;
        }
        for (HashMap.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (Utils.isEmpty(key) || Utils.isEmpty(value)) {
                continue;
            }
            mHeaders.put(key, value);
        }
        return this;
    }

    public Object getObj() {
        return obj;
    }

    public void setObj(Object obj) {
        this.obj = obj;
    }

    public boolean valid() {
        return !(Utils.existsEmpty(mType, mURL) || mMethod == HttpMethod.NONE || mTimeout <= 0);
    }

    private void generateUniqueId() {

        try {
            StringBuilder builder = new StringBuilder();
            builder.append("mURL-" + mURL);
            builder.append("-type-" + mType);
            builder.append("-tag-" + mTag);
            builder.append("-tag2-" + mTag2);
            builder.append("-priority-" + mPriority);
            builder.append("-httpMethod-" + mMethod);
            uniqueId = Utils.MD5(builder.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * finger print of one kind of ConnectInfo.Every kind(not every one) has a
     * unique value.
     */
    public String getUnique() {
        if (uniqueId == null) {
            generateUniqueId();
        }
        return uniqueId;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[tag] " + mTag + "\n");
        builder.append("[priority] " + mPriority + "\n");
        builder.append("[url] " + mURL + "\n");
        builder.append("[method] " + mMethod + "\n");
        builder.append("[timeout] " + mTimeout + "\n");
        builder.append("[type] " + mType + "\n");

        try {
            if (mEntity instanceof StringEntity) {
                builder.append("[entity] " + EntityUtils.toString(mEntity) + "\n");
            } else if (mEntity != null) {
                builder.append("[entity] " + mEntity.toString() + "\n");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (HashMap.Entry<String, String> entry : mHeaders.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            builder.append("[header] " + key + ":" + value + "\n");
        }
        return builder.toString();
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel dest, int flags) {
        //
    }

    public static final Parcelable.Creator<ConnectInfo> CREATOR = new Parcelable.Creator<ConnectInfo>() {
        public ConnectInfo createFromParcel(Parcel parcel) {
            return new ConnectInfo(parcel);
        }

        public ConnectInfo[] newArray(int size) {
            return new ConnectInfo[size];
        }
    };

    public String getFakeId() {
        if (mFakeId == null) {
            return "";
        }
        return mFakeId;
    }

    public void setFakeId(String mFakeId) {
        this.mFakeId = mFakeId;
    }

    public boolean needRepect() {
        return repectTime > 0;
    }

    public void decreaseRepectTime() {
        this.repectTime--;
    }
}
