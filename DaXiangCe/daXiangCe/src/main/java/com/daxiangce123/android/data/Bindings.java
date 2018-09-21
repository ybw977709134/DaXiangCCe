package com.daxiangce123.android.data;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by hansentian on 12/22/14.
 */
public class Bindings {
    private int limit;
    private boolean hasMore;
    private ArrayList<Binding> bindings;

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

    public ArrayList<Binding> getBindings() {
        return bindings;
    }

    public void setBindings(ArrayList<Binding> bindings) {
        this.bindings = bindings;
    }

    public Bindings(int limit, boolean hasMore, ArrayList<Binding> bindings) {
        this.limit = limit;
        this.hasMore = hasMore;
        this.bindings = bindings;
    }
}
