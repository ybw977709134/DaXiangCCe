package com.daxiangce123.android.data;

/**
 * Created by danty on 2015/4/23.
 */
public class SplashAdInfo {

    /*
    "id" : String,
    "name" : String,
    "click_num" : Number,
    "create_date" : Timestamp,
    "mod_date" : Timestamp,
    "start_date" : Timestamp,
    "end_date" : Timestamp,
    "url" : String,
    "os" : String,
    "status" : String,
    "background_color" : Number,
    "use_background_color" : Boolean
    */

    private String id;
    private String name;
    private int click_num;
    private String create_date;
    private String mod_date;
    private String start_date;
    private String end_date;
    private String url;
    private String os;
    private String status;
    private int background_color;
    private boolean use_background_color;

    public SplashAdInfo() {}

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getClick_num() {
        return click_num;
    }

    public void setClickNum(int click_num) {
        this.click_num = click_num;
    }

    public boolean isUse_background_color() {
        return use_background_color;
    }

    public void setUseBackgroundColor(boolean use_background_color) {
        this.use_background_color = use_background_color;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getEnd_date() {
        return end_date;
    }

    public void setEnd_date(String end_date) {
        this.end_date = end_date;
    }

    public String getStart_date() {
        return start_date;
    }

    public void setStart_date(String start_date) {
        this.start_date = start_date;
    }

    public String getMod_date() {
        return mod_date;
    }

    public void setMod_date(String mod_date) {
        this.mod_date = mod_date;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getBackground_color() {
        return background_color;
    }

    public void setBackground_color(int background_color) {
        this.background_color = background_color;
    }
}
