package com.daxiangce123.android.manager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;

import com.daxiangce123.R;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.ConnectInfo;
import com.daxiangce123.android.data.SensitiveWords;
import com.daxiangce123.android.data.base.ObjectsWrapper;
import com.daxiangce123.android.http.ConnectBuilder;
import com.daxiangce123.android.http.Response;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.Broadcaster;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;
import com.daxiangce123.android.util.ViewUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by hansentian on 4/3/15.
 */
public class SensitiveWordGrepManager {

    public enum Type {
        search, comment, album_name, album_note, user_name
    }

    public static class WordsWrapper {
        private String word;
        private Type type;

        public WordsWrapper(String word, Type type) {
            this.word = word;
            this.type = type;
        }

        public String getWord() {
            return word;
        }

        public Type getType() {
            return type;
        }
    }

    public static final String TAG = "SensitiveWordManager";

    private static SensitiveWordGrepManager instance = new SensitiveWordGrepManager();
    private LinkedList<SensitiveWords> sensitiveWordses = new LinkedList<>();
    private long lastModify;


    BroadcastReceiver broadcaster = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Consts.GET_SENSITIVE_WORD.equals(intent.getAction())) {
                ConnectInfo info = intent.getParcelableExtra(Consts.REQUEST);
                Response response = intent.getParcelableExtra(Consts.RESPONSE);
                if (info != null && response != null) {
                    handleSensitiveWordResponse(info, response);
                }
            }
        }
    };

    private SensitiveWordGrepManager() {

    }

    public static SensitiveWordGrepManager getInstance() {
        return instance;
    }

    public void handleSensitiveWordResponse(ConnectInfo info, Response response) {
        if (response.getContent() == null || info.getTag() == null) {
            return;
        }
        int startPos = Integer.valueOf(info.getTag());
        ObjectsWrapper<SensitiveWords> objectsWrapper = Parser.parseSensitiveWords(response.getContent());
        if (objectsWrapper == null) {
            return;
        }
        if (startPos == 0) {
            sensitiveWordses.clear();
        }
        if (!Utils.isEmpty(objectsWrapper.getData())) {
            sensitiveWordses.addAll(objectsWrapper.getData());
        }
        if (objectsWrapper.isHasMore()) {
            ConnectBuilder.listSenstiveWords(sensitiveWordses.size());
        } else {
            Broadcaster.unregisterReceiver(broadcaster);
            if (App.DEBUG) {
                LogUtil.v(TAG, sensitiveWordses.toString());
            }
        }
    }

    /**
     * check sensitive words
     *
     * @param activity
     * @param words
     * @return T word is ok
     */
    public boolean doSensitiveGrep(Activity activity, WordsWrapper... words) {
        if (words != null) {
            for (int i = 0; i < words.length; i++) {
                WordsWrapper word = words[i];
                if (containWords(word)) {
                    showAlert(activity);
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * test word if contains sensitive word
     *
     * @param word
     * @return if contains illegal words return true
     */
    private boolean containWords(WordsWrapper word) {
        String words = word.getWord();
        Type type = word.getType();
        if (words == null || type == null) {
            return false;
        }
        Pattern p = Pattern.compile(Consts.regEx);
        Matcher m = p.matcher(words);
        words = (m.replaceAll("").trim());
        Iterator<SensitiveWords> iterator = sensitiveWordses.iterator();
        while (iterator.hasNext()) {
            SensitiveWords next = iterator.next();
            if (next.getType().equals(type.toString())) {
                if (words.contains(next.getWord())) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void showAlert(Activity activity) {
        ViewUtil.aleartMessage(R.string.submit_failed_due_word, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }, activity);
    }

    public void refreshSensitiveWord() {
        if (System.currentTimeMillis() - lastModify > Consts.DAY_IN_MILLS) {
            Broadcaster.registerReceiver(broadcaster, new IntentFilter(Consts.GET_SENSITIVE_WORD));
            ConnectBuilder.listSenstiveWords(0);
            lastModify = System.currentTimeMillis();
        }

    }


}
