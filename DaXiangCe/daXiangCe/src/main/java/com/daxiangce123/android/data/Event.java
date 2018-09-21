package com.daxiangce123.android.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.parser.Parser;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project DaXiangCe
 * @time Mar 28, 2014
 * @author ram
 */
public class Event extends Data implements Parcelable {

	public final static Event EMPTY = new Event();
	public final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + Consts.EVENTS // table
																							// name
			+ "( " // (
			+ "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , " // _id
			+ Consts.EVENT_ID + " TEXT NOT NULL UNIQUE,"// EVENT_ID
			+ Consts.CREATED_AT + " TEXT  ," // CREATED_AT
			+ Consts.USER_ID + " TEXT NOT NULL ," // USER_ID
			+ Consts.USER_NAME + " TEXT  ," // USER_NAME
			+ Consts.SRC_DEVICE + " TEXT ," // SRC_DEVICE
			+ Consts.OP_TYPE + " TEXT NOT NULL ," // OP_TYPE
			+ Consts.OBJECT + " TEXT ," // OBJECT
			+ Consts.STATUS + " INTEGER, " // OBJECT + Consts.STATUS_UNREAD
			+ Consts.TYPE + " TEXT, " // OBJECT
			+ Consts.OBJ_ID + " TEXT " // OBJECT
			+ " ) ";

	private final static String TAG = "Event";
	private String eventId;
	private String createdAt;
	private String userId;
	private String userName;
	private String srcDevice;
	private String opType;
	private Object object;
	private String objectStr;
	private boolean hasRead = false;
	private boolean needShown = false;

	public Event() {
		needShown = isNotification();
	}

	public Event(Parcel parcel) {
		if (parcel == null) {
			return;
		}
		LogUtil.d(TAG, "Event read from parcel");
		eventId = parcel.readString();
		createdAt = parcel.readString();
		userId = parcel.readString();
		userName = parcel.readString();
		srcDevice = parcel.readString();
		opType = parcel.readString();
		String objStr = parcel.readString();
		object = Parser.parserByOpType(opType, objStr);

		boolean[] bools = { hasRead };
		parcel.readBooleanArray(bools);
		hasRead = bools[0];
	}

	public Event(String eventId, String createdDate, String userId, String userName, String srcDevice, String opType, Object object, String objectStr) {
		this(eventId, createdDate, userId, userName, srcDevice, opType, object, objectStr, false);
	}

	public Event(String eventId, String createdDate, String userId, String userName, String srcDevice, String opType, Object object, String objectStr, boolean hasRead) {
		this.eventId = eventId;
		this.createdAt = createdDate;
		this.userId = userId;
		this.userName = userName;
		this.srcDevice = srcDevice;
		this.opType = opType;
		this.object = object;
		this.hasRead = hasRead;
		this.objectStr = objectStr;
		needShown = isNotification();
	}

	public String getEventId() {
		return eventId;
	}

	public void setEventId(String eventId) {
		this.eventId = eventId;
	}

	public String getCreatedDate() {
		return createdAt;
	}

	public void setCreatedDate(String createdDate) {
		this.createdAt = createdDate;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getSrcDevice() {
		return srcDevice;
	}

	public void setSrcDevice(String srcDevice) {
		this.srcDevice = srcDevice;
	}

	public String getOpType() {
		return opType;
	}

	public void setOpType(String opType) {
		this.opType = opType;
		needShown = isNotification();
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public boolean hasRead() {
		return hasRead;
	}

	public void setRead(boolean hasRead) {
		this.hasRead = hasRead;
	}

	public String getObjectStr() {
		return objectStr;
	}

	public void setObjectStr(String objectStr) {
		this.objectStr = objectStr;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("------------EVENT------------\n");
		builder.append("[eventId]	" + eventId + "\n");
		builder.append("[cDate]	" + createdAt + "\n");
		builder.append("[userId]	" + userId + "\n");
		builder.append("[userName]	" + userName + "\n");
		builder.append("[srcDevice]	" + srcDevice + "\n");
		builder.append("[opType]	" + opType + "\n");
		builder.append("[hasRead]	" + hasRead + "\n");
		builder.append("[object]>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + object);
		builder.append("\n------------EVENT------------\n");
		return builder.toString();
	}

	/**
	 * message that will show in notificationCenter
	 *
	 * @time Jul 31, 2014
	 *
	 * @return
	 */
	public boolean isNotification() {
		if (Consts.ALBUM_DELETED.equals(opType)// album_deleted
                || Consts.SYSTEM_ALBUM_DELETED.equals(opType)// system_album_delete
				|| Consts.ALBUM_SHARED.equals(opType)// album_shared
				|| Consts.COMMENT_CREATED.equals(opType)// comment_created
				|| Consts.LIKE_CREATED.equals(opType)// comment_created
				|| Consts.MEMBER_LEFT.equals(opType)// member_left
				|| Consts.FILE_SHARED.equals(opType)// file_shared
				|| Consts.FILE_DOWNLOADED.equals(opType)// file_downloaded
				|| Consts.SYSTEM_FILE_DELETED.equals(opType) // system_file_delete
				|| Consts.FILE_DELETED.equals(opType)) {
			return true;
		}
		return false;
	}

	public boolean isNeedShown() {
		return needShown;
	}

	public void setNeedShown(boolean needShown) {
		this.needShown = needShown;
	}

	private String getType() {
		if (isNotification()) {
			return Consts.NOTIFICATION;
		}
		return null;
	}

	@Override
	public boolean isValid() {
		if (Utils.existsEmpty(eventId, createdAt, userId, opType)) {
			return false;
		}
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////

	private ContentValues getCV() {
		// String objectStr = object == null ? "" : object.toString();
		ContentValues cv = new ContentValues();
		cv.put(Consts.EVENT_ID, eventId);
		cv.put(Consts.CREATED_AT, createdAt);
		cv.put(Consts.USER_ID, userId);
		cv.put(Consts.USER_NAME, userName);
		cv.put(Consts.SRC_DEVICE, srcDevice);
		cv.put(Consts.OP_TYPE, opType);
		cv.put(Consts.OBJECT, objectStr);
		cv.put(Consts.STATUS, hasRead ? Consts.BOOLEAN_TRUE : Consts.BOOLEAN_FALSE);
		cv.put(Consts.TYPE, getType());
		cv.put(Consts.OBJ_ID, getObjectWidthId(object));
		if (App.DEBUG) {
			LogUtil.d(TAG, "cv is :\n" + cv);
		}
		return cv;
	}

	@Override
	public String getTableName() {
		return Consts.EVENTS;
	}

	public boolean exists(SQLiteDatabase db) {
		Cursor cursor = null;
		try {
			String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.EVENT_ID + " = \"" + eventId + "\"";
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
			db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.EVENT_ID + " = \"" + uniqueId + "\"");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
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
		if (Utils.isEmpty(eventId)) {
			LogUtil.e(TAG, "ERROR: file id is empty");
			return false;
		}
		try {
			db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.EVENT_ID + " = \"" + eventId + "\"");
			return true;
		} catch (Exception e) {
			e.printStackTrace();
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
		if (!isNotification()) {
			LogUtil.d(TAG, "ERROR: event is not a notification");
			return false;
		}
		ContentValues cv = getCV();
		try {
			long row = db.insert(getTableName(), null, cv);
			if (App.DEBUG) {
				LogUtil.d(TAG, "Event.insert() row is " + row + " >>>>>>>>>>>>>>>>>>>>>>> exists:" + exists(db));
			}
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
			String[] args = { eventId };
			long row = db.update(getTableName(), cv, Consts.EVENT_ID + "=?", args);
			if (row == -1) {
				return true;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public Event get(SQLiteDatabase db, String uniqueId) {
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
		Event event = null;
		try {
			String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.EVENT_ID + " = \"" + uniqueId + "\"";
			cursor = db.rawQuery(sql, null);
			if (cursor != null && cursor.getCount() >= 0) {
				cursor.moveToFirst();
				event = create(cursor);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			close(cursor);
		}
		return event;
	}

	@Override
	public Event create(Cursor cursor) {
		if (cursor == null || cursor.getCount() <= 0 || cursor.getColumnCount() <= 0) {
			return null;
		}
		try {
			Event event = new Event();

			int index = cursor.getColumnIndex(Consts.EVENT_ID);
			if (index >= 0) {
				String id = cursor.getString(index);
				event.setEventId(id);
			}

			index = cursor.getColumnIndex(Consts.STATUS);
			if (index >= 0) {
				int sta = cursor.getInt(index);
				event.setRead(sta == Consts.BOOLEAN_TRUE);
			}

			index = cursor.getColumnIndex(Consts.CREATED_AT);
			if (index >= 0) {
				String createAt = cursor.getString(index);
				event.setCreatedDate(createAt);
			}

			index = cursor.getColumnIndex(Consts.USER_ID);
			if (index >= 0) {
				String userId = cursor.getString(index);
				event.setUserId(userId);
			}

			index = cursor.getColumnIndex(Consts.USER_NAME);
			if (index >= 0) {
				String userName = cursor.getString(index);
				event.setUserName(userName);
			}

			index = cursor.getColumnIndex(Consts.SRC_DEVICE);
			if (index >= 0) {
				String srcDevice = cursor.getString(index);
				event.setSrcDevice(srcDevice);
			}

			index = cursor.getColumnIndex(Consts.OP_TYPE);
			String opType = null;
			if (index >= 0) {
				opType = cursor.getString(index);
				event.setOpType(opType);
			}

			index = cursor.getColumnIndex(Consts.OBJECT);
			if (index >= 0) {
				String objectStr = cursor.getString(index);
				Object object = Parser.parserByOpType(opType, objectStr);
				event.setObject(object);
			}

			if (event.isValid()) {
				return event;
			}
		} catch (Exception e) {
			LogUtil.d(TAG, "clone Exception : " + e.getLocalizedMessage());
			e.printStackTrace();
		}
		return null;
	}

	// ////////////////////////////////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		boolean[] bools = { hasRead };
		String objStr = object == null ? null : object.toString();
		dest.writeString(eventId);
		dest.writeString(createdAt);
		dest.writeString(userId);
		dest.writeString(userName);
		dest.writeString(srcDevice);
		dest.writeString(opType);
		dest.writeString(objStr);
		dest.writeBooleanArray(bools);
	}

	public final static Parcelable.Creator<Event> CREATOR = new Creator<Event>() {

		@Override
		public Event[] newArray(int size) {
			return new Event[size];
		}

		@Override
		public Event createFromParcel(Parcel source) {
			return new Event(source);
		}
	};

	public final String getObjectWidthId() {
		return getObjectWidthId(object);
	}

	public final static String getObjectWidthId(Object object) {
		if (object == null) {
			return null;
		}
		if (object instanceof AlbumEntity) {
			return getObjectWidthId(Consts.ALBUM, ((AlbumEntity) object).getId());
		} else if (object instanceof FileEntity) {
			return getObjectWidthId(Consts.FILE, ((FileEntity) object).getAlbum());// album
		} else if (object instanceof CommentEntity) {
			return getObjectWidthId(Consts.COMMENT, ((CommentEntity) object).getId());
		} else if (object instanceof LikeEntity) {
			LikeEntity like = (LikeEntity) object;
			String id = like.getUserId() + "-" + like.getObjId();
			return getObjectWidthId(Consts.LIKE, id);
		}
		return null;
	}

	public final static String getObjectWidthId(String prefix, String id) {
		if (Utils.existsEmpty(prefix, id)) {
			return null;
		}
		return prefix + "-" + id;
	}
}
