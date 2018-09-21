package com.daxiangce123.android.data.base;

import java.util.LinkedList;

/**
 * Created by hansentian on 4/3/15.
 */
public class ObjectsWrapper<T> {
    private int limit;
    private boolean hasMore;
    private LinkedList<T> data;

    public ObjectsWrapper(int limit, boolean hasMore, LinkedList<T> data) {
        this.limit = limit;
        this.hasMore = hasMore;
        this.data = data;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public boolean isHasMore() {
        return hasMore;
    }

    public void setHasMore(boolean hasMore) {
        this.hasMore = hasMore;
    }

    public LinkedList<T> getData() {
        return data;
    }

    public void setData(LinkedList<T> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "ObjectsWrapper{" +
                "limit=" + limit +
                ", hasMore=" + hasMore +
                ", data=" + data +
                '}';
    }
}
