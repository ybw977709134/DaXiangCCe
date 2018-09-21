package com.daxiangce123.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

//@DatabaseTable(tableName = "comment_data")  
public class CommentEntity extends Data implements Parcelable {
	// @DatabaseField
	private String id = null;
	// @DatabaseField
	private String obj_id = null;
	// @DatabaseField
	private String obj_type = null;
	// @DatabaseField
	private String user_id = null;
	// @DatabaseField
	private String user_nam = null;
	// @DatabaseField
	private String msg = null;
	// @DatabaseField
	private String create_date = null;
	// @DatabaseField
	private String reply_to_user = null;
	// @DatabaseField
	private String reply_to_user_name = null;

	public static final String TAG = "CommentEntitty";

	public final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + Consts.COMMENT // table
			// name
			+ "( " // (
			+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , " // _id
			+ Consts.ID + " TEXT NOT NULL UNIQUE,"// album id
			+ Consts.OBJ_ID + " TEXT NOT NULL ," // name
			+ Consts.OBJ_TYPE + " TEXT NOT NULL ," // note
			+ Consts.USER_ID + " TEXT NOT NULL ," // CREATE_DATE
			+ Consts.USER_NAME + " TEXT NOT NULL ," // MOD_DATE
			+ Consts.MSG + " TEXT NOT NULL ," // CREATOR
			+ Consts.CREATE_DATE + " TEXT NOT NULL ," // owner
			+ Consts.REPLY_TO_USER + " TEXT  ," // link
			+ Consts.REPLY_TO_USER_NAME + " TEXT )"; // size

	public final static CommentEntity EMPTY = new CommentEntity();

	// {
	// public boolean update(SQLiteDatabase db) {
	// setUpdateKey(null);
	// keepLocal(false);
	// boolean result = super.update(db);
	// return result;
	// };
	// };

	public CommentEntity(Parcel parcel) {
		id = parcel.readString();
		obj_id = parcel.readString();
		obj_type = parcel.readString();
		user_id = parcel.readString();
		create_date = parcel.readString();
		user_nam = parcel.readString();
		msg = parcel.readString();
		reply_to_user = parcel.readString();
		reply_to_user_name = parcel.readString();
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(id);
		dest.writeString(obj_id);
		dest.writeString(obj_type);
		dest.writeString(user_id);
		dest.writeString(create_date);
		dest.writeString(user_nam);
		dest.writeString(msg);
		dest.writeString(reply_to_user);
		dest.writeString(reply_to_user_name);
	}

	public CommentEntity() {
	}

	public CommentEntity(String id, String objId, String objType, String userId, String userName, String msg, String createDate, String replyWho, String replyUserName) {
		this.id = id;
		this.obj_id = objId;
		this.obj_type = objType;
		this.user_id = userId;
		this.user_nam = userName;
		this.msg = msg;
		this.create_date = createDate;
		this.reply_to_user = replyWho;
		this.reply_to_user_name = replyUserName;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId() {
		return id;
	}

	public void setObjId(String objId) {
		this.obj_id = objId;
	}

	public String getObjId() {
		return obj_id;
	}

	public void setObjType(String objType) {
		this.obj_type = objType;
	}

	public String getObjType() {
		return obj_type;
	}

	public void setUserId(String userId) {
		this.user_id = userId;
	}

	public String getUserId() {
		return user_id;
	}

	public void setUserName(String userName) {
		this.user_nam = userName;
	}

	public String getUserName() {
		return user_nam;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getMsg() {
		return msg;
	}

	public void setCreateDate(String createDate) {
		this.create_date = createDate;
	}

	public String getCreateDate() {
		return create_date;
	}

	public void setReplyToUser(String replayWho) {
		this.reply_to_user = replayWho;
	}

	public String getReplyToUser() {
		return reply_to_user;
	}

	public void setReplyToUserName(String replayUserName) {
		this.reply_to_user_name = replayUserName;
	}

	public String getReplayToUserName() {
		return reply_to_user_name;
	}

	public boolean isValid() {
		if (Utils.isEmpty(id) || Utils.isEmpty(create_date) || Utils.isEmpty(user_id)) {// ||
																						// Utils.isEmpty(user_nam)
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[id]	" + id + "\n");
		builder.append("[obj_id]	" + obj_id + "\n");
		builder.append("[obj_type]	" + obj_type + "\n");
		builder.append("[user_id]	" + user_id + "\n");
		builder.append("[user_nam]	" + user_nam + "\n");
		builder.append("[msg]	" + msg + "\n");
		builder.append("[create_date]	" + create_date + "\n");
		builder.append("[reply_to_user]	" + reply_to_user + "\n");
		builder.append("[reply_to_user_name]	" + reply_to_user_name);
		return builder.toString();
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String getTableName() {
		return Consts.COMMENT;
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
		CommentEntity entity = new CommentEntity();
		try {
			int index = cursor.getColumnIndex(Consts.ID);
			if (index >= 0) {
				String id = cursor.getString(index);
				entity.setId(id);
			}

			index = cursor.getColumnIndex(Consts.OBJ_ID);
			if (index >= 0) {
				String obj_id = cursor.getString(index);
				entity.setObjId(obj_id);
			}
			index = cursor.getColumnIndex(Consts.OBJ_TYPE);
			if (index >= 0) {
				String obj_type = cursor.getString(index);
				entity.setObjType(obj_type);
			}
			index = cursor.getColumnIndex(Consts.USER_ID);
			if (index >= 0) {
				String user_id = cursor.getString(index);
				entity.setUserId(user_id);
			}
			index = cursor.getColumnIndex(Consts.USER_NAME);
			if (index >= 0) {
				String user_nam = cursor.getString(index);
				entity.setUserName(user_nam);
			}
			index = cursor.getColumnIndex(Consts.MSG);
			if (index >= 0) {
				String msg = cursor.getString(index);
				entity.setMsg(msg);
			}
			index = cursor.getColumnIndex(Consts.CREATE_DATE);
			if (index >= 0) {
				String create_date = cursor.getString(index);
				entity.setCreateDate(create_date);
			}

			index = cursor.getColumnIndex(Consts.REPLY_TO_USER);
			if (index >= 0) {
				String reply_to_user = cursor.getString(index);
				entity.setReplyToUser(reply_to_user);
			}
			index = cursor.getColumnIndex(Consts.REPLY_TO_USER_NAME);
			if (index >= 0) {
				String reply_to_user_name = cursor.getString(index);
				entity.setReplyToUserName(reply_to_user_name);
			}

			if (entity.isValid()) {
				return entity;
			}
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
		if (Utils.isEmpty(id)) {
			LogUtil.e(TAG, "ERROR: file id is empty");
			return false;
		}
		try {
			db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.ID + " = \"" + id + "\"");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}


    public boolean deleteAllComment(SQLiteDatabase db) {
        if (db == null) {
            LogUtil.e(TAG, "ERROR: db is null");
            return false;
        }
        if (db.isReadOnly()) {
            LogUtil.e(TAG, "ERROR: db is read only");
            return false;
        }
        try {
            db.execSQL("DELETE FROM " + getTableName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }


    @Override
	public boolean delete(SQLiteDatabase db, String uniqueId) {
		if (db == null) {
			LogUtil.e(TAG, "ERROR: db is null");
			return false;
		}
		if (db.isReadOnly()) {
			LogUtil.e(TAG, "ERROR: db is read only");
			return false;
		}
		if (Utils.isEmpty(uniqueId)) {
			LogUtil.e(TAG, "ERROR: file id is empty");
			return false;
		}
		try {
			db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.ID + " = \"" + uniqueId + "\"");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	private ContentValues getCV() {
		// String objectStr = object == null ? "" : object.toString();
		ContentValues cv = new ContentValues();
		cv.put(Consts.ID, id);
		cv.put(Consts.OBJ_ID, obj_id);
		cv.put(Consts.OBJ_TYPE, obj_type);
		cv.put(Consts.USER_ID, user_id);
		cv.put(Consts.CREATE_DATE, create_date);
		cv.put(Consts.USER_NAME, user_nam);
		cv.put(Consts.MSG, msg);
		cv.put(Consts.CREATE_DATE, create_date);
		cv.put(Consts.REPLY_TO_USER, reply_to_user);
		cv.put(Consts.REPLY_TO_USER_NAME, reply_to_user);
		if (App.DEBUG) {
			LogUtil.d(TAG, "cv is :\n" + cv);
		}
		return cv;
	}

	public boolean exists(SQLiteDatabase db) {
		Cursor cursor = null;
		try {
			String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.ID + " = \"" + id + "\"";
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
			/*
			 * if (App.DEBUG) { LogUtil.d(TAG, "Event.insert() row is " + row +
			 * " >>>>>>>>>>>>>>>>>>>>>>> exists:" + exists(db)); }
			 */
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
			String[] args = { id };
			long row = db.update(getTableName(), cv, Consts.ID + "=?", args);
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
		if (db == null) {
			LogUtil.e(TAG, "db is null");
			return null;
		}
		if (Utils.isEmpty(uniqueId)) {
			LogUtil.e(TAG, "uniqueId is null");
			return null;
		}
		uniqueId = uniqueId.trim();
		Cursor cursor = null;
		CommentEntity event = null;
		try {
			String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.EVENT_ID + " = \"" + uniqueId + "\"";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() >= 0) {
				cursor.moveToFirst();
				event = (CommentEntity) create(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(cursor);
		}
		return event;
	}

}
