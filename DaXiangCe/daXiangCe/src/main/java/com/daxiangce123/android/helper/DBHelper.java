package com.daxiangce123.android.helper;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.Data;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {
    private final static String TAG = "DBHelper";
    private final static int version = 9;
    private String dbName;

    public DBHelper(String dbName) {
        super(App.getAppContext(), dbName, null, version);
        this.dbName = dbName;
        if (App.DEBUG) {
            LogUtil.d(TAG, "dbName = " + dbName);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Event.CREATE_SQL);
        db.execSQL(FileEntity.CREATE_SQL);
        db.execSQL(AlbumEntity.CREATE_SQL);
        db.execSQL(CommentEntity.CREATE_SQL);
        db.execSQL(Contact.CREATE_SQL);

        if (App.DEBUG) {
            LogUtil.d(TAG, "Event.CREATE_SQL" + Event.CREATE_SQL);
            LogUtil.d(TAG, "FileEntity.CREATE_SQL = " + FileEntity.CREATE_SQL);
            LogUtil.d(TAG, "AlbumEntity.CREATE_SQL = " + AlbumEntity.CREATE_SQL);
            LogUtil.d(TAG, "CommentEntity.CREATE_SQL = " + CommentEntity.CREATE_SQL);
            LogUtil.d(TAG, "CONTACT.CREATE_SQL = " + Contact.CREATE_SQL);
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + Event.EMPTY.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + FileEntity.EMPTY.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + AlbumEntity.EMPTY.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + CommentEntity.EMPTY.getTableName());
        db.execSQL("DROP TABLE IF EXISTS " + Contact.EMPTY.getTableName());
        onCreate(db);
    }

    public final String getDbName() {
        return dbName;
    }

    public final <E extends Data> LinkedList<E> getList(E data) {
        SQLiteDatabase db = getReadableDatabase();
        LinkedList<E> result = getList(data, db);
        db.close();
        return result;
    }

    /**
     * db will <b><i> NOT </b></i> be closed
     */
    public final <E extends Data> LinkedList<E> getList(E data, SQLiteDatabase db) {
        return getList(data, null, db);
    }

    public final <E extends Data> LinkedList<E> getList(E data, String selection) {
        SQLiteDatabase db = getReadableDatabase();
        LinkedList<E> result = getList(data, selection, db);
        db.close();
        return result;
    }

    /**
     * db will <b><i> NOT </b></i> be closed
     */
    public final <E extends Data> LinkedList<E> getList(E data, String selection, SQLiteDatabase db) {
        return getList(data, null, selection, null, null, null, null, db);
    }

    /**
     * db will <b><i> NOT </b></i> be closed
     *
     * @param data
     * @param columns
     * @param selection
     * @param selectionArgs
     * @param groupBy
     * @param having
     * @param orderBy
     * @return
     * @time Apr 15, 2014
     */
    @SuppressWarnings("unchecked")
    public final <E extends Data> LinkedList<E> getList(E data, String[] columns, String selection, String[] selectionArgs, String groupBy, String having, String orderBy, SQLiteDatabase db) {
        if (data == null) {
            return null;
        }
        Cursor cursor = null;
        LinkedList<E> list = null;
        try {
            cursor = db.query(data.getTableName(), columns, selection, selectionArgs, groupBy, having, orderBy);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                list = new LinkedList<E>();
                while (!cursor.isAfterLast()) {
                    Data d = data.create(cursor);
                    cursor.moveToNext();
                    if (d == null) {
                        continue;
                    }
                    list.add((E) d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }


    public final ArrayList<Event> getAllEvent() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = null;
        ArrayList<Event> list = null;
        try {
            String sql = "SELECT * FROM " + Event.EMPTY.getTableName() + " ORDER BY " + Consts.CREATED_AT + " DESC";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.getCount() > 0) {
                cursor.moveToFirst();
                list = new ArrayList<Event>();
                while (!cursor.isAfterLast()) {
                    Event d = Event.EMPTY.create(cursor);
                    cursor.moveToNext();
                    if (d == null) {
                        continue;
                    }
                    list.add(d);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            if (cursor != null) {
                cursor.close();
            }
            db.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public final boolean readAllEvent(String type) {
        SQLiteDatabase db = getReadableDatabase();
        boolean result = false;
        try {
            String sql = "UPDATE " + Event.EMPTY.getTableName() + " SET " + Consts.STATUS + " = \"" + Consts.BOOLEAN_TRUE + "\" WHERE " + Consts.TYPE + " = \"" + type + "\"";
            db.execSQL(sql);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        db.close();
        return result;
    }

    public final boolean delete(Data data) {
        if (data == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        boolean result = data.delete(db);
        db.close();
        return result;
    }

    /**
     * @param data     EMPTY static Data {@link FileEntity#EMPTY} or
     *                 {@link AlbumEntity#EMPTY} or {@link Event#EMPTY}
     * @param uniqueId the uniqueId of the Data
     * @return
     * @time 2014-4-16
     */
    public final boolean delete(Data data, String uniqueId) {
        if (data == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        boolean result = data.delete(db, uniqueId);
        db.close();
        return result;
    }

    public final void delete(List<? extends Data> list) {
        if (Utils.isEmpty(list)) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        try {
            for (Data data : list) {
                data.delete(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    public final void emptyAlbum(String albumId) {
        if (Utils.isEmpty(albumId)) {
            return;
        }
        String sql = "DELETE FROM " + FileEntity.EMPTY.getTableName() + " WHERE " + Consts.ALBUM + "=\"" + albumId + "\"";
        SQLiteDatabase db = getWritableDatabase();
        execute(sql, db);
        db.close();
    }

    public final boolean insert(Data data) {
        if (data == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        boolean result = data.insert(db);
        db.close();
        return result;
    }

    public final void insert(List<? extends Data> list) {
        if (Utils.isEmpty(list)) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        insert(list, db);
        db.close();
    }

    public final void insert(List<? extends Data> list, SQLiteDatabase db) {
        insert(list, db, false);
    }

    /**
     * @param list
     * @param db   will not be closed after called
     * @time May 28, 2014
     */
    public final void insert(List<? extends Data> list, SQLiteDatabase db, boolean keepLocal) {
        if (Utils.isEmpty(list)) {
            return;
        }
        if (db == null) {
            return;
        }
        if (db.isReadOnly()) {
            return;
        }
        try {
            db.beginTransaction();
            for (Data data : list) {
                data.keepLocal(keepLocal);
                data.insert(db);
            }
            db.setTransactionSuccessful();
            db.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public final boolean update(Data data) {
        if (data == null) {
            return false;
        }
        SQLiteDatabase db = getWritableDatabase();
        boolean result = data.update(db);
        db.close();
        return result;
    }

    public final void update(List<? extends Data> list) {
        if (Utils.isEmpty(list)) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        try {
            for (Data data : list) {
                data.update(db);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();
        }
    }

    @SuppressWarnings("unchecked")
    public final <T extends Data> T getData(T t, String uniqueId) {
        SQLiteDatabase db = getReadableDatabase();
        T result = null;
        try {
            result = (T) t.get(db, uniqueId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (db.isOpen()) {
            db.close();
        }
        return result;
    }

    public final AlbumEntity getAlbum(String albumId) {
        return getData(AlbumEntity.EMPTY, albumId);
    }

    /**
     * Avoid to use this to read DB(by using {@link #getWritableDatabase()})
     */
    public final void execute(String sql) {
        if (Utils.isEmpty(sql)) {
            return;
        }
        SQLiteDatabase db = getWritableDatabase();
        execute(sql, db);
        db.close();
    }

    /**
     * db will <b> NOT </b> be closed
     */
    public final void execute(String sql, SQLiteDatabase db) {
        if (Utils.isEmpty(sql)) {
            return;
        }
        if (db == null) {
            return;
        }
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
