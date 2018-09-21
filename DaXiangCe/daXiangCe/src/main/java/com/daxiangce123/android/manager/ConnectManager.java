package com.daxiangce123.android.manager;

import android.content.Intent;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.http.Connector;
import com.daxiangce123.android.http.ProgressInfo;
import com.daxiangce123.android.http.ProgressListener;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

public abstract class ConnectManager {
    public static final String TAG = "ConnectManager";

    private boolean mRunning;
    protected LinkedList<ConnectInfo> mConnectList;
    protected LinkedList<ConnectRunner> mRunnerList;
    protected ArrayList<ConnectInfo> currentList;
    private int mPoolLimit;
    private int maxTaskSize = -1;

    protected ConnectManager() {
        mRunning = false;
        mConnectList = new LinkedList<ConnectInfo>();
        mRunnerList = new LinkedList<ConnectRunner>();
        currentList = new ArrayList<ConnectInfo>();
        setPoolLimit(initPoolSize());
    }

    protected ConnectRunner createRunner() {
        return new ConnectRunner();
    }

    protected abstract int initPoolSize();

    public synchronized void start() {
        if (mRunning) {
            // LogUtil.w(TAG, "transfer manager already started");
            return;
        }
        mRunning = true;

        startThreads();
    }

    public synchronized void stop() {
        if (!mRunning) {
            LogUtil.w(TAG, "transfer manager already stopped");
            return;
        }

        mRunning = false;
        mConnectList.clear();
        stopThreads();
    }

    public synchronized void addConnect(ConnectInfo info) {
        if (info == null || !info.valid()) {
            LogUtil.d(TAG, "invalid connect info: " + info);
            return;
        }
        if (App.DEBUG) {
            LogUtil.d(TAG, "------------------------------------------>addConnect() " + "	unique=" + info.getUnique() + "	" + info.getType().toUpperCase());
        }
        synchronized (currentList) {
            int curSize = currentList.size();
            for (int i = 0; i < curSize; i++) {
                ConnectInfo ingInfo = currentList.get(i);
                if (ingInfo == null) {
                    continue;
                }
                if (ingInfo.getUnique().equals(info.getUnique())) {
                    LogUtil.w(TAG, "request is running");
                    return;
                }
            }
        }
        start();
        //TODO need to fix that bug
//        at java.util.LinkedList$LinkIterator.next(LinkedList.java:124)
//        W/System.err(31254): 	at com.daxiangce123.android.manager.ConnectManager.addConnect(ConnectManager.java:89)
//        W/System.err(31254): 	at com.daxiangce123.android.http.ConnectBuilder.getAlbumCoverId(ConnectBuilder.java:932)
//        W/System.err(31254): 	at com.daxiangce123.android.http.ConnectBuilder.getAlbumCoverId(ConnectBuilder.java:912)
//        W/System.err(31254): 	at com.daxiangce123.android.EventService.onListAlbumEnd(EventService.java:802)
//        W/System.err(31254): 	at com.daxiangce123.android.EventService.access$2200(EventService.java:72)
//        W/System.err(31254): 	at com.daxiangce123.android.EventService$3.run(EventService.java:272)
//        W/System.err(31254): 	at android.os.Handler.handleCallback(Handler.java:733)
//        W/System.err(31254): 	at android.os.Handler.dispatchMessage(Handler.java:95)
        synchronized (mConnectList) {
            Iterator<ConnectInfo> it = mConnectList.iterator();
            while (it.hasNext()) {
                ConnectInfo taskInfo = it.next();
                if (taskInfo == null) {
                    continue;
                }
                if (taskInfo.getUnique().equals(info.getUnique())) {
                    LogUtil.w(TAG, "request is duplicated");
                    it.remove();
                    break;
                }
            }
            if (maxTaskSize > 0 && mConnectList.size() >= maxTaskSize) {
                mConnectList.removeFirst();
            }
            mConnectList.addLast(info);
            mConnectList.notify();
        }
    }

    public synchronized void cancelConnect(String type, String tag) {
        if (Utils.existsEmpty(type, tag)) {
            LogUtil.w(TAG, "invalid type or tag");
            return;
        }
        int size = mConnectList.size();
        for (int index = size - 1; index >= 0; --index) {
            ConnectInfo info = mConnectList.get(index);
            if (!tag.equals(info.getTag())) {
                continue;
            }
            if (!type.equals(info.getType())) {
                continue;
            }
            // remove transfer info
            synchronized (mConnectList) {
                mConnectList.remove(index);
            }
            Runnable runner = info.getRunner();
            if (runner == null || !(runner instanceof ConnectRunner)) {
                continue;
            }
            // cancel transfer connection
            ConnectRunner tr = (ConnectRunner) runner;
            tr.cancel();
        }
    }

    /**
     * cancel upload progress
     *
     * @param fakeId
     */
    public synchronized void cancelUploadConnect(String fakeId) {
        int size = mConnectList.size();
        for (int index = size - 1; index >= 0; --index) {
            ConnectInfo info = mConnectList.get(index);
            if (!fakeId.equals(info.getFakeId())) {
                continue;
            }
            // remove transfer info
            synchronized (mConnectList) {
                mConnectList.remove(index);
            }
            Runnable runner = info.getRunner();
            if (runner == null || !(runner instanceof ConnectRunner)) {
                continue;
            }
            // cancel transfer connection
            ConnectRunner tr = (ConnectRunner) runner;
            tr.cancel();
        }
    }

    public synchronized boolean hasConnect(String tag) {
        if (Utils.isEmpty(tag)) {
            LogUtil.w(TAG, "invalid tag");
            return false;
        }

        int size = mConnectList.size();
        for (int index = 0; index < size; index++) {
            ConnectInfo info = mConnectList.get(index);
            if (tag.equals(info.getTag())) {
                return true;
            }
        }
        return false;
    }

    public synchronized ConnectInfo getConnect(String tag) {
        if (Utils.isEmpty(tag)) {
            LogUtil.w(TAG, "invalid tag");
            return null;
        }

        int size = mConnectList.size();
        for (int index = 0; index < size; index++) {
            ConnectInfo info = mConnectList.get(index);
            if (tag.equals(info.getTag())) {
                return info;
            }
        }

        return null;
    }

    public void setPoolLimit(int limit) {
        if (limit < 0) {
            return;
        }
        mPoolLimit = limit;
    }

    public int getPoolLimit() {
        return mPoolLimit;
    }

    public void setMaxTaskLimit(int limit) {
        this.maxTaskSize = limit;
    }

    public int getMaxTaskLimit() {
        return maxTaskSize;
    }

    private void startThreads() {
        for (int index = 0; index < mPoolLimit; ++index) {
            ConnectRunner runner = createRunner();
            runner.setName("connect_runner");
            runner.start();
            mRunnerList.add(runner);
        }
    }

    private void stopThreads() {
        int runnerSize = mRunnerList.size();
        for (int index = 0; index < runnerSize; ++index) {
            ConnectRunner runner = mRunnerList.get(index);
            runner.cancel();
            try {
                runner.interrupt();
            } catch (Exception e) {
            }
            try {
                runner.join(200);
            } catch (Exception e) {
                e.printStackTrace();
            }
            runner = null;
        }
    }

    private ConnectInfo getConnect() {
        if (mConnectList.isEmpty()) {
            try {
                synchronized (mConnectList) {
                    mConnectList.wait();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        ConnectInfo info = null;
        synchronized (mConnectList) {
            if (!mConnectList.isEmpty()) {
                info = mConnectList.removeLast();
            }
        }
        return info;
    }

    class ConnectRunner extends Thread implements ProgressListener {
        private static final String TAG = "ConnectRunner";

        private boolean mRunning;
        protected ConnectInfo mInfo;
        private Connector mConnector;

        public ConnectRunner() {
            mRunning = false;
        }

        public void start() {
            mRunning = true;
            super.start();
        }

        public boolean cancel() {
            if (mConnector == null) {
                return false;
            }
            mConnector.disconnect();
            return true;
        }

        public final void run() {
            while (mRunning) {
                mInfo = getConnect();
                if (mInfo == null || !mRunning) {
                    LogUtil.d(TAG, "failed to get connect info!");
                    continue;
                }
                synchronized (currentList) {
                    currentList.add(mInfo);
                }
                // set runner info
                mInfo.setRunner(this);

                // XXX new connector every time????
                if (mConnector == null) {
                    mConnector = new Connector();
                } else {
                    mConnector.clear();
                }
                if (needProgress()) {
                    mConnector.setListener(this);
                } else {
                    mConnector.setListener(null);
                }
                mConnector.setConnectInfo(mInfo);
                mConnector.connect();
                synchronized (currentList) {
                    currentList.remove(mInfo);
                }
                // reset runner info
                mInfo.setRunner(null);
                mConnector.response();
                // XXX disconnect after connect
                mConnector.disconnect();
                mInfo = null;
                // takeBreak();
            }
        }

        protected boolean needProgress() {
            return false;
        }

        @Override
        public void onProgress(String localPath, int progress, long speed, long offset, long totalSize) {
            if (mInfo == null) {
                return;
            }

            String tag = mInfo.getTag();
            String type = mInfo.getType();

            Intent intent = new Intent(Consts.TRANSFER_PROGRESS);
            ProgressInfo pi = new ProgressInfo(tag, type, progress);
            pi.setReceived(offset).setSpeed(speed);
            intent.putExtra(Consts.PROGRESS_INFO, pi);
            intent.putExtra(Consts.REQUEST, mInfo);
            Broadcaster.sendBroadcast(intent);
        }

    }
}
