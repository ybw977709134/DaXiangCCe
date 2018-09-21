package com.daxiangce123.android.business.event;

/**
 * Created by hansentian on 3/16/15.
 */
public class Signal {
    public static final int ALBUM_CONTROLLER_LOAD_MORE_OK = 2;
    public static final int ALBUM_CONTROLLER_LOAD_MORE_EMPTY = 3;
    public static final int ALBUM_DETAIL_ACTIVITY_LOAD_MORE_OK = 4;
    public static final int ALBUM_FINISH = 5;
    public int action;
    public String album;

    public Signal(int action) {
        this.action = action;
    }

    public Signal(int action, String album) {
        this.action = action;
        this.album = album;
    }
}
