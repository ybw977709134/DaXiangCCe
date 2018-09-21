package com.daxiangce123.android.data;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.daxiangce123.android.helper.DBHelper;

import java.util.HashSet;

public abstract class Data {

    public boolean isValid() {
        return true;
    }

    /**
     * delete this data from cursor<br>
     * after operation is completed.cursor will <i><b>NOT</b></i> be closed
     *
     * @param cursor
     * @return
     */
    public abstract Data create(Cursor cursor);

    /**
     * delete this data from database<br>
     * after operation is completed. db will <i><b>NOT</b></i> be closed
     *
     * @param db
     * @return
     */
    public abstract boolean delete(SQLiteDatabase db);

    /**
     * delete this data from database<br>
     * after operation is completed. db will <i><b>NOT</b></i> be closed
     * <p/>
     * <p/>
     * See More {@link DBHelper#delete(Data, String)}
     *
     * @param db
     * @return
     */
    public abstract boolean delete(SQLiteDatabase db, String uniqueId);

    /**
     * insert this data from database<br>
     * after operation is completed. db will <i><b>NOT</b></i> be closed
     *
     * @param db
     * @return
     */
    public abstract boolean insert(SQLiteDatabase db);

    /**
     * update this data from database<br>
     * after operation is completed. db will <i><b>NOT</b></i> be closed
     *
     * @param db
     * @return
     */
    public abstract boolean update(SQLiteDatabase db);

    /**
     * update this data from database<br>
     * after operation is completed. db will <i><b>NOT</b></i> be closed
     *
     * @param db
     * @return
     */
    public abstract Data get(SQLiteDatabase db, String uniqueId);

    /**
     * for client, we add some extra key in model which has nothing connected
     * with server
     * <p/>
     * if ture, <b>the local key</b> in db will be updated with new value.
     *
     * @param keepLocal
     * @time May 28, 2014
     */
    public void keepLocal(boolean keepLocal) {
    }

    /**
     * If keys is not null this will just {@link #update(SQLiteDatabase)} key in
     * this it;
     * <p/>
     * If null all keys in this data will be updated
     * <p/>
     * Default is <b>null</b>
     */
    public void setUpdateKey(HashSet<String> keys) {
    }

    public void close(Cursor cursor) {
        if (cursor == null) {
            return;
        }
        if (cursor.isClosed()) {
            return;
        }
        try {
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public abstract String getTableName();

}
