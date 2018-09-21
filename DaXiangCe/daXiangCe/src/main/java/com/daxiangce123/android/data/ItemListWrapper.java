package com.daxiangce123.android.data;

import java.util.LinkedList;

/**
 * Created by hansentian on 1/27/15.
 */
public class ItemListWrapper<T> {
    private int limit;
    private boolean hasMore;
    private LinkedList<T> itemList;

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

    public LinkedList<T> getItemList() {
        return itemList;
    }

    public void setItemList(LinkedList<T> itemList) {
        this.itemList = itemList;
    }

    public ItemListWrapper(int limit, boolean hasMore, LinkedList<T> itemList) {
        this.limit = limit;
        this.hasMore = hasMore;
        this.itemList = itemList;
    }
}
