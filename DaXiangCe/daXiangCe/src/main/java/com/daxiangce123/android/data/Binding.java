package com.daxiangce123.android.data;

import java.io.Serializable;

/**
 * Created by hansentian on 12/21/14.
 */
public class Binding implements Serializable {
    String user_name;
    String provider;
    String create_date;

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }
}
