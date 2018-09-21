package com.daxiangce123.android.core;

/**
 * 这个写的有问题 ，需要重新搞
 *
 * @author ram
 * @project DaXiangCe
 * @time Sep 3, 2014
 */
public class UiRuntime extends BaseRunner {

    private static UiRuntime instance;

    @Deprecated
    public final static UiRuntime instance() {
        if (instance == null) {
            instance = new UiRuntime();
        }
        return instance;
    }

    private UiRuntime() {
        init();
    }

    @Override
    public int getCorePoolSize() {
        return 4;
    }
}
