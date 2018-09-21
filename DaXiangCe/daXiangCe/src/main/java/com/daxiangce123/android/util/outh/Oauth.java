package com.daxiangce123.android.util.outh;

import com.daxiangce123.android.util.Utils;

/**
 * @author ram
 * @project Pickup
 * @time 2013-11-13
 */
public class Oauth {
    public final static String TYPE_QQ = "qq";
    public final static String TYPE_FB = "facebook";
    public final static String TYPE_WEIBO = "weibo";
    public final static String TYPE_GOOGLE = "google";
    public final static String TYPE_TWITTER = "twitter";
    public final static String TYPE_WECHAT = "wechat";
    private String token;
    private String type;
    private String uid;
    private String union_id;

    public Oauth(String type, String token) {
        this.token = token;
        this.type = type;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUnion_id() {
        return union_id;
    }

    public void setUnion_id(String union_id) {
        this.union_id = union_id;
    }

    public boolean isValid() {
        if (!Utils.isEmpty(type) && !Utils.isEmpty(token)) {
            token = token.trim();
            type = type.trim();
            if (!Utils.isEmpty(token))
                if (type.equals(TYPE_QQ) || type.equals(TYPE_FB)
                        || type.equals(TYPE_WEIBO) || type.equals(TYPE_GOOGLE)
                        || type.equals(TYPE_TWITTER)
                        || type.equals(TYPE_WECHAT)) {
                    return true;
                }
        }
        return false;
    }

    @Override
    public String toString() {
        return "[type:" + type + "] [token:" + token + "]" + uid;
    }

}
