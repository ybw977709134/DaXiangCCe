package com.daxiangce123.android.push;

/**
 * @author ram
 * @project DaXiangCe
 * @time Jul 11, 2014
 */
public interface PushCallBack {

    public enum Provider {
        BAIDU("baidu"), GOOGLE("google");
        private String name;

        private Provider(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

    }

    public void onNewMessage(String msg, Provider provider);

}
