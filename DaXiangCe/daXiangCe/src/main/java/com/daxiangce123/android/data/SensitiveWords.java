package com.daxiangce123.android.data;

/**
 * Created by hansentian on 4/2/15.
 */
public class SensitiveWords {
    public String type;
    public String word;
    public String level;
    public String create_date;
    public String mod_date;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCreate_date() {
        return create_date;
    }

    public void setCreate_date(String create_date) {
        this.create_date = create_date;
    }

    public String getMod_date() {
        return mod_date;
    }

    public void setMod_date(String mod_date) {
        this.mod_date = mod_date;
    }


    public SensitiveWords(String type, String word, String level) {
        this.type = type;
        this.word = word;
        this.level = level;
    }

    @Override
    public String toString() {
        return "SensitiveWords{" +
                "type='" + type + '\'' +
                ", word='" + word + '\'' +
                '}';
    }
}
