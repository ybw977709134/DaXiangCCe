package com.daxiangce123.android.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * Created by hansentian on 1/23/15.
 */
public class Contact extends Data {

    public static final String CONTACT_STATE_UNUPLOAD = "unupload";
    public static final String CONTACT_STATE_UPLOADED = "uploaded";
    public static final String CONTACT_STATE_OLD = "old";

    public static String areacode = "+86";

    /**
     * that is id in db
     */
    private String id;
    private String user_id;
    private String create_date;
    private String mod_date;
    private String contact;
    private String type;
    private String friend_name;
    private String reg_user_id;
    private String reg_date;

    private String state;

    /**
     * store the contactID in db
     */
    private String contactID;

    /**
     * url of contact photo
     */
    private String photoUri;


    public static final String TAG = "ContactEntity";

    public final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + Consts.CONTACT // table
            // name
            + "( " // (
            + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , " // _id
            + Consts.ID + " TEXT,"//
            + Consts.FRIEND_NAME + " TEXT ," //
            + Consts.PHOTO_URI + " TEXT ," //
            + Consts.CONTACT + " TEXT ," //
            + Consts.CONTACT_ID + " TEXT ," //
            + Consts.REG_USER_ID + " TEXT  ," //
            + Consts.STATE + " TEXT )"; // size

    public final static Contact EMPTY = new Contact();


    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPhotoUri() {
        return photoUri;
    }

    public void setPhotoUri(String photoUri) {
        this.photoUri = photoUri;
    }

    public String getContactID() {
        return contactID;
    }

    public void setContactID(String contactID) {
        this.contactID = contactID;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
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

    public String getContact() {
        return contact;
    }

    public void setContact(String contact) {
        this.contact = grepPhoneNumber(contact);
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFriend_name() {
        return friend_name;
    }

    public void setFriend_name(String friend_name) {
        this.friend_name = friend_name;
    }

    public String getReg_user_id() {
        return reg_user_id;
    }

    public void setReg_user_id(String reg_user_id) {
        this.reg_user_id = reg_user_id;
    }

    public String getReg_date() {
        return reg_date;
    }

    public void setReg_date(String reg_date) {
        this.reg_date = reg_date;
    }

    public Contact(String friend_name, String contact) {
        this.friend_name = friend_name;
        setContact(contact);
    }

    public Contact() {
    }

    public String grepPhoneNumber(String mobile) {
        if (TextUtils.isEmpty(mobile)) {
            return "";
        }
        mobile = mobile.replace(" ", "");
        mobile = mobile.replace("-", "");
        if (!mobile.startsWith("+")) {
            if (mobile.startsWith(areacode.substring(1))) {
                mobile = "+" + mobile;
            }
            mobile = areacode + mobile;
        }
        return mobile;
    }

    public boolean isRegister() {
        return !TextUtils.isEmpty(reg_user_id);
    }

    static {
        TelephonyManager tm = (TelephonyManager) App.getAppContext().getSystemService(Context.TELEPHONY_SERVICE);
        if ((!TextUtils.isEmpty(tm.getSimCountryIso())) && (!TextUtils.isEmpty(Utils.getNationsPhoneCode(tm.getSimCountryIso())))) {
            areacode = Utils.getNationsPhoneCode(tm.getSimCountryIso());
        }
    }

//    Consts.ID + " TEXT NOT NULL UNIQUE,"//
//            + Consts.FRIEND_NAME + " TEXT NOT NULL ," //
//            + Consts.PHOTO_URI + " TEXT NOT NULL ," //
//            + Consts.CONTACT_ID + " TEXT NOT NULL ," //
//            + Consts.REG_USER_ID + " TEXT NOT NULL ," //
//            + Consts.STATE + " TEXT )"; // size

    private ContentValues getCV() {
        ContentValues cv = new ContentValues();
        cv.put(Consts.ID, id);
        cv.put(Consts.FRIEND_NAME, friend_name);
        cv.put(Consts.PHOTO_URI, photoUri);
        cv.put(Consts.CONTACT_ID, contactID);
        cv.put(Consts.REG_USER_ID, reg_user_id);
        cv.put(Consts.STATE, state);
        cv.put(Consts.CONTACT, contact);
        return cv;
    }


    @Override
    public Data create(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            LogUtil.e(TAG, "cursor is null");
            return null;
        }
        if (cursor.isClosed() || cursor.getCount() <= 0) {
            LogUtil.e(TAG, "cursor is Closed or count is 0");
            return null;
        }
        Contact entity = new Contact();
        try {
            int index = cursor.getColumnIndex(Consts.REG_USER_ID);
            if (index >= 0) {
                String id = cursor.getString(index);
                entity.setReg_user_id(id);
            }
            index = cursor.getColumnIndex(Consts.ID);
            if (index >= 0) {
                String obj_id = cursor.getString(index);
                entity.setId(obj_id);
            }
            index = cursor.getColumnIndex(Consts.FRIEND_NAME);
            if (index >= 0) {
                String obj_type = cursor.getString(index);
                entity.setFriend_name(obj_type);
            }
            index = cursor.getColumnIndex(Consts.PHOTO_URI);
            if (index >= 0) {
                String user_id = cursor.getString(index);
                entity.setPhotoUri(user_id);
            }
            index = cursor.getColumnIndex(Consts.CONTACT_ID);
            if (index >= 0) {
                String user_nam = cursor.getString(index);
                entity.setContactID(user_nam);
            }
            index = cursor.getColumnIndex(Consts.CONTACT);
            if (index >= 0) {
                String user_nam = cursor.getString(index);
                entity.setContact(user_nam);
            }
            index = cursor.getColumnIndex(Consts.STATE);
            if (index >= 0) {
                String obj_id = cursor.getString(index);
                entity.setState(obj_id);
            }
            return entity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return entity;
    }

    @Override
    public boolean delete(SQLiteDatabase db) {
        if (db == null) {
            LogUtil.e(TAG, "ERROR: db is null");
            return false;
        }
        if (db.isReadOnly()) {
            LogUtil.e(TAG, "ERROR: db is read only");
            return false;
        }
        try {
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.CONTACT + " = \"" + contact + "\"");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(SQLiteDatabase db, String uniqueId) {
        return false;
    }

    public boolean exists(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.CONTACT + " = \"" + contact + "\"";
            cursor = db.rawQuery(sql, null);
            if (cursor == null) {
                return false;
            }
            return cursor.getCount() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(cursor);
        }
        return false;
    }

    @Override
    public boolean insert(SQLiteDatabase db) {
        if (db == null) {
            LogUtil.e(TAG, "ERROR: db is null");
            return false;
        }
        if (db.isReadOnly()) {
            LogUtil.e(TAG, "ERROR: db is read only");
            return false;
        }
        if (!isValid()) {
            LogUtil.e(TAG, "ERROR: fileEntify is invalid");
            return false;
        }
        ContentValues cv = getCV();
        try {
            if (exists(db)) {
                return update(db);
            }
            long row = db.insert(getTableName(), null, cv);
            cv = null;
            if (row > -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cv = null;
        return false;
    }

    @Override
    public boolean update(SQLiteDatabase db) {
        if (db == null || db.isReadOnly() || !isValid()) {
            return false;
        }
        try {
            ContentValues cv = getCV();
            String[] args = {contact};
            long row = db.update(getTableName(), cv, Consts.CONTACT + "=?", args);
            if (row == -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Data get(SQLiteDatabase db, String uniqueId) {
        return null;
    }

    @Override
    public String getTableName() {
        return "Contact";
    }

    @Override
    public String toString() {
        return "Contact{" +
                "contact='" + contact + '\'' +
                ", friend_name='" + friend_name + '\'' +
                ", reg_user_id='" + reg_user_id + '\'' +
                ", contactID='" + contactID + '\'' +
                ", photoUri='" + photoUri + '\'' +
                '}';
    }
}
