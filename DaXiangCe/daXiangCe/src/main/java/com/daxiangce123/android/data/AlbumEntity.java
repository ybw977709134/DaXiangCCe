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

import java.util.HashSet;

/**
 * TODO remove COVER / UPDATE_COUNT from {@link AlbumEntity} into a new
 * table!!!!
 *
 * @project DaXiangCe
 */
// @DatabaseTable(tableName = "album_data")
public class AlbumEntity extends Data implements Parcelable {

    /**
     * {@link #updateKeys} & {@link #keepLocal} will be set to <b>default</b>
     * after {@link #update(SQLiteDatabase)} is called
     */
    public final static AlbumEntity EMPTY = new AlbumEntity() {
        public boolean update(SQLiteDatabase db) {
            boolean result = super.update(db);
            setUpdateKey(null);
            keepLocal(false);
            return result;
        }
    };
    // TODO thumb_file_id
    public final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + Consts.ALBUMS // table
            // name
            + "( " // (
            + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , " // _id
            + Consts.ALBUM_ID + " TEXT NOT NULL UNIQUE,"// album id
            + Consts.NAME + " TEXT NOT NULL ," // name
            + Consts.NOTE + " TEXT ," // note
            + Consts.CREATE_DATE + " TEXT NOT NULL ," // CREATE_DATE
            + Consts.MOD_DATE + " TEXT NOT NULL ," // MOD_DATE
            + Consts.CREATOR + " TEXT NOT NULL ," // CREATOR
            + Consts.OWNER + " TEXT NOT NULL ," // owner
            + Consts.LINK + " TEXT NOT NULL ," // link
            + Consts.SIZE + " INTEGER NOT NULL DEFAULT 0 ," // size
            + Consts.MEMBERS + " INTEGER NOT NULL DEFAULT 0," // members
            + Consts.COVER + " TEXT, " // COVER,
            + Consts.INVITE_CODE + " TEXT, " // COVER
            + Consts.UPDATE_COUNT + " INTEGER DEFAULT 0 ," // COVER
            + Consts.THUMB_FILE_ID + " TEXT ," + Consts.IS_PRIVATE + " INTEGER, " // IS_PRIVATE
            + Consts.IS_LOCKED + " INTEGER, " // IS_LOCKED
            + Consts.COMMENT_OFF + " INTEGER, " // COMMENT_OFF
            + Consts.LIKE_OFF + " INTEGER, " // LIKE_OFF
            + Consts.NO_PUSH + " INTEGER, " // LIKE_OFF
            + Consts.VIEWS + " INTEGER, " // views
            + Consts.SHARES + " INTEGER, " // shares
            + Consts.DOWNLOADS + " INTEGER, " // downloads
            + Consts.COMMENTS + " INTEGER, " // comments
            + Consts.LIKES + " INTEGER, " // likes
            + Consts.PERMISSIONS + " INTEGER," // PERMISSIONS
            + Consts.OLD_MEMBERS + " INTEGER" + " ) ";
    private final static String TAG = "AlbumEntity";
    // @DatabaseField
    private String id = null;
    // @DatabaseField
    private String name = null;
    // @DatabaseField
    private String note = null;
    // @DatabaseField
    private String create_date = null;
    // @DatabaseField
    private String mod_date = null;
    // @DatabaseField
    private String creator = null;
    // @DatabaseField
    private String owner = null;
    private String link = null;
    // @DatabaseField
    private int size;
    // @DatabaseField
    private int members;
    private int views;
    private int shares;
    private int downloads;
    private int permissions;
    private String inviteCode;
    /**
     * just for single client. nothing with server
     */
    private int oldMembers;
    /**
     * just for single client. nothing with server
     */
    private String cover;
    /**
     * just for single client. nothing with server
     */
    private int updateCount;

    private HashSet<String> updateKeys;

    /**
     * Cover of album that user has set, if haven't set, this is null
     */
    private String thumbFileId;

    private boolean isPrivate;
    private boolean isLocked;
    private boolean commentOff;
    private boolean likeOff;
    private boolean noPush;
    private boolean keepLocal = false;

    public AlbumEntity(Parcel parcel) {
        boolean[] bools = {isPrivate, isLocked, commentOff, likeOff, noPush, keepLocal};

        thumbFileId = parcel.readString();
        id = parcel.readString();
        name = parcel.readString();
        note = parcel.readString();
        create_date = parcel.readString();
        mod_date = parcel.readString();
        creator = parcel.readString();
        owner = parcel.readString();
        link = parcel.readString();
        size = parcel.readInt();
        if (size < 0) {
            size = 0;
        }
        views = parcel.readInt();
        shares = parcel.readInt();
        downloads = parcel.readInt();
        members = parcel.readInt();
        permissions = parcel.readInt();
        inviteCode = parcel.readString();
        cover = parcel.readString();
        updateCount = parcel.readInt();
        oldMembers = parcel.readInt();
        parcel.readBooleanArray(bools);
        if (bools == null || bools.length < 1) {
            return;
        }
        isPrivate = bools[0];
        isLocked = bools[1];
        commentOff = bools[2];
        likeOff = bools[3];
        noPush = bools[4];
        keepLocal = bools[5];
    }

    public AlbumEntity() {
    }

    public AlbumEntity(String id, String name, String note, String createDate, String modDate, String creator, String owner, String link, int size, int views, int shares, int downloads, int members,
                       String inviteCode, boolean isPrivate, boolean isLocked, boolean commentOff, boolean likeOff, int permissions) {
        this(id, name, note, createDate, modDate, creator, owner, link, size, views, shares, downloads, members, inviteCode, isPrivate, isLocked, commentOff, likeOff, permissions, null);
    }

    public AlbumEntity(String id, String name, String note, String createDate, String modDate, String creator, String owner, String link, int size, int views, int shares, int downloads, int members,
                       String inviteCode, boolean isPrivate, boolean isLocked, boolean commentOff, boolean likeOff, int permissions, String cover) {

        this.id = id;
        this.name = name;
        this.note = note;
        this.create_date = createDate;
        this.mod_date = modDate;
        this.creator = creator;
        this.owner = owner;
        this.link = link;
        if (size < 0) {
            size = 0;
        }
        this.size = size;
        this.members = members;
        this.inviteCode = inviteCode;
        this.isPrivate = isPrivate;
        this.isLocked = isLocked;
        this.commentOff = commentOff;
        this.likeOff = likeOff;
        this.views = views;
        this.shares = shares;
        this.downloads = downloads;
        this.permissions = permissions;
    }

    /**
     * just update message in memory
     */
    public void updateFromRemote(AlbumEntity album) {
        if (album == null) {
            return;
        }
        setSize(album.getSize());
        setMembers(album.getMembers());
        setCommentOff(album.getCommentOff());
        setIsLocked(album.getIsLocked());
        setIsPrivate(album.getIsPrivate());
        setLikeOff(album.getLikeOff());
        setModDate(album.getModDate());
        setViews(album.getViews());
        setShares(album.getShares());
        setDownloads(album.getDownloads());
        setNote(album.getNote());
        setName(album.getName());
        setNoPush(album.isNoPush());
        setThumbFileId(album.getThumbFileId());
        setPermissions(album.getPermissions());
        setLocalCover(album.getLocalCover());
        if (App.DEBUG) {
            LogUtil.d(TAG, " ---- cover + album.getThumbFileId() ---- " + album.getThumbFileId());
        }

        if (App.DEBUG) {
            LogUtil.d(TAG, " ---- cover + album.getLocalCover() ---- " + album.getLocalCover());
        }

    }

    public ContentValues getCV() {
        ContentValues cv = new ContentValues();
        cv.put(Consts.ALBUM_ID, id);
        if (updateKeys != null) {
            if (updateKeys.contains(Consts.NAME)) {
                cv.put(Consts.NAME, name);
            }

            if (updateKeys.contains(Consts.NOTE)) {
                cv.put(Consts.NOTE, note);
            }

            if (updateKeys.contains(Consts.CREATE_DATE)) {
                cv.put(Consts.CREATE_DATE, create_date);
            }

            if (updateKeys.contains(Consts.MOD_DATE)) {
                cv.put(Consts.MOD_DATE, mod_date);
            }

            if (updateKeys.contains(Consts.CREATOR)) {
                cv.put(Consts.CREATOR, creator);
            }

            if (updateKeys.contains(Consts.OWNER)) {
                cv.put(Consts.OWNER, owner);
            }

            if (updateKeys.contains(Consts.LINK)) {
                cv.put(Consts.LINK, link);
            }

            if (updateKeys.contains(Consts.SIZE)) {
                cv.put(Consts.SIZE, size);
            }

            if (updateKeys.contains(Consts.MEMBERS)) {
                cv.put(Consts.MEMBERS, members);
            }

            if (updateKeys.contains(Consts.INVITE_CODE)) {
                cv.put(Consts.INVITE_CODE, inviteCode);
            }

            if (updateKeys.contains(Consts.IS_PRIVATE)) {
                cv.put(Consts.IS_PRIVATE, Consts.getBool(isPrivate));
            }

            if (updateKeys.contains(Consts.IS_LOCKED)) {
                cv.put(Consts.IS_LOCKED, Consts.getBool(isLocked));
            }

            if (updateKeys.contains(Consts.COMMENT_OFF)) {
                cv.put(Consts.COMMENT_OFF, Consts.getBool(commentOff));
            }

            if (updateKeys.contains(Consts.LIKE_OFF)) {
                cv.put(Consts.LIKE_OFF, Consts.getBool(likeOff));
            }

            if (updateKeys.contains(Consts.VIEWS)) {
                cv.put(Consts.VIEWS, views);
            }

            if (updateKeys.contains(Consts.SHARES)) {
                cv.put(Consts.SHARES, shares);
            }

            if (updateKeys.contains(Consts.DOWNLOADS)) {
                cv.put(Consts.DOWNLOADS, downloads);
            }
            if (updateKeys.contains(Consts.PERMISSIONS)) {
                cv.put(Consts.PERMISSIONS, permissions);
            }

            if (updateKeys.contains(Consts.COVER)) {
                cv.put(Consts.COVER, cover);
            }

            if (updateKeys.contains(Consts.UPDATE_COUNT)) {
                cv.put(Consts.UPDATE_COUNT, updateCount);
            }

            if (updateKeys.contains(Consts.OLD_MEMBERS)) {
                cv.put(Consts.OLD_MEMBERS, oldMembers);
            }
            if (updateKeys.contains(Consts.THUMB_FILE_ID)) {
                cv.put(Consts.THUMB_FILE_ID, thumbFileId);
            }

        } else {
            cv.put(Consts.ALBUM_ID, id);
            cv.put(Consts.NAME, name);
            cv.put(Consts.NOTE, note);
            cv.put(Consts.CREATE_DATE, create_date);
            cv.put(Consts.MOD_DATE, mod_date);
            cv.put(Consts.CREATOR, creator);
            cv.put(Consts.OWNER, owner);
            cv.put(Consts.LINK, link);
            cv.put(Consts.SIZE, size);
            cv.put(Consts.MEMBERS, members);
            cv.put(Consts.INVITE_CODE, inviteCode);
            cv.put(Consts.IS_PRIVATE, Consts.getBool(isPrivate));
            cv.put(Consts.IS_LOCKED, Consts.getBool(isLocked));
            cv.put(Consts.COMMENT_OFF, Consts.getBool(commentOff));
            cv.put(Consts.LIKE_OFF, Consts.getBool(likeOff));
            cv.put(Consts.VIEWS, views);
            cv.put(Consts.SHARES, shares);
            cv.put(Consts.DOWNLOADS, downloads);
            cv.put(Consts.PERMISSIONS, permissions);
            cv.put(Consts.THUMB_FILE_ID, thumbFileId);

            if (!keepLocal) {
                cv.put(Consts.OLD_MEMBERS, oldMembers);
                cv.put(Consts.COVER, cover);
                cv.put(Consts.UPDATE_COUNT, updateCount);
            }
        }
        return cv;
    }

    @Override
    public void keepLocal(boolean keepLocal) {
        this.keepLocal = keepLocal;
    }

    @Override
    public void setUpdateKey(HashSet<String> keys) {
        this.updateKeys = keys;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getCreateDate() {
        return create_date;
    }

    public void setCreateDate(String create_date) {
        this.create_date = create_date;
    }

    public String getModDate() {
        return mod_date;
    }

    public void setModDate(String mod_date) {
        this.mod_date = mod_date;
    }

    public String getCreator() {
        return creator;
    }

    public int getOldMembers() {
        return oldMembers;
    }

    public void setOldMembers(int oldMembers) {
        this.oldMembers = oldMembers;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getThumbFileId() {
        return thumbFileId;
    }

    public void setThumbFileId(String thumbFileId) {
        this.thumbFileId = thumbFileId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if (size < 0) {
            size = 0;
        }
        this.size = size;
    }

    public int getMembers() {
        return members;
    }

    public void setMembers(int members) {
        if (members < 0) {
            members = 0;
        }
        this.members = members;
    }

    public void setInviteCode(String inviteCode) {
        this.inviteCode = inviteCode;
    }

    public String getInviteCode() {
        return inviteCode;
    }

    public boolean getIsPrivate() {
        return isPrivate;
    }

    public void setIsPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    public boolean getIsLocked() {
        return isLocked;
    }

    public void setIsLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    public boolean getCommentOff() {
        return commentOff;
    }

    public void setCommentOff(boolean commentOff) {
        this.commentOff = commentOff;
    }

    public boolean getLikeOff() {
        return likeOff;
    }

    public void setLikeOff(boolean likeOff) {
        this.likeOff = likeOff;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public int getShares() {
        return shares;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public int getDownloads() {
        return downloads;
    }

    public int getPermissions() {
        return permissions;
    }

    public void setPermissions(int permissions) {
        this.permissions = permissions;
    }

    /**
     * just for single client. nothing with server
     */
    public String getLocalCover() {
        return cover;
    }

    public void setLocalCover(String cover) {
        this.cover = cover;
    }

    public String getTrueCover() {
        if (Utils.isEmpty(thumbFileId)) {
            return cover;
        }
        return thumbFileId;
    }

    public int getUpdateCount() {
        return updateCount;
    }

    public void setUpdateCount(int updateCount) {
        this.updateCount = updateCount;
    }

    public boolean isNoPush() {
        return noPush;
    }

    public void setNoPush(boolean noPush) {
        this.noPush = noPush;
    }

    @Override
    public boolean isValid() {
        if (Utils.isEmpty(id) || Utils.isEmpty(creator) || Utils.isEmpty(name) || Utils.isEmpty(owner)) {
            if (App.DEBUG) {
                LogUtil.d(TAG, "isValid() id=" + id + " creator=" + creator + " name=" + name + " owner=" + owner);
            }
            return false;
        }
        return true;
    }

    public boolean changeSize(SQLiteDatabase db, String fieldName, boolean increase) {
        if (this == EMPTY) {
            return false;
        }
        return changeSize(db, id, fieldName, increase);
    }

    public boolean changeSize(SQLiteDatabase db, String id, String fieldName, boolean increase) {
        if (db == null) {
            LogUtil.e(TAG, "ERROR: db is null");
            return false;
        }
        if (db.isReadOnly()) {
            LogUtil.e(TAG, "ERROR: db is read only");
            return false;
        }
        if (Utils.existsEmpty(id)) {
            LogUtil.e(TAG, "ERROR: file id is empty");
            return false;
        }
        try {
            String sql = "UPDATE " + getTableName() + " SET " + fieldName + " = " + fieldName + ((increase) ? " + " : " - ") + " 1 " + " WHERE " + Consts.ALBUM_ID + "=\"" + id + "\"";
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public String getTableName() {
        return Consts.ALBUMS;
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
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.ALBUM_ID + " = \"" + uniqueId + "\"");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(SQLiteDatabase db) {
        if (db == null) {
            return false;
        }
        if (Utils.isEmpty(id)) {
            return false;
        }
        try {
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.ALBUM_ID + " = \"" + id + "\"");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(SQLiteDatabase db) {
        return update(db, true);
    }

    public boolean update(SQLiteDatabase db, boolean refreshUpdateCount) {
        if (db == null || db.isReadOnly()) {
            return false;
        }
        try {
            ContentValues cv = getCV();
            if (refreshUpdateCount) {
                refreshUpdateCount(db, cv);
            }
            String[] args = {id};
            long row = db.update(getTableName(), cv, Consts.ALBUM_ID + "=?", args);
            if (row == -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean insert(SQLiteDatabase db) {
        if (db == null || db.isReadOnly() || !isValid()) {
            return false;
        }
        ContentValues cv = getCV();
        try {
            if (exists(db)) {
                return update(db);
            }
            long row = db.insert(getTableName(), null, cv);
            if (row != -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void refreshUpdateCount(SQLiteDatabase db, ContentValues cv) {
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.ALBUM_ID + " = \"" + id + "\"";
            cursor = db.rawQuery(sql, null);
            cursor.moveToFirst();
            int oldIndex = cursor.getColumnIndex(Consts.UPDATE_COUNT);
            int oldUpdateCount = cursor.getInt(oldIndex);
            int oldSize = cursor.getInt(cursor.getColumnIndex(Consts.SIZE));
            updateCount = (size - oldSize < 0) ? 0 : (size - oldSize);
            if (updateCount != 0) {
                updateCount += oldUpdateCount;
                cv.put(Consts.UPDATE_COUNT, updateCount);
            }
            if (App.DEBUG) {
                LogUtil.v(TAG, id + String.format("updateCount = %1$d	oldUpdateCount = %2$d	oldSize = %3$d ", updateCount, oldUpdateCount, oldSize));
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(cursor);
        }

    }

    private boolean exists(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.ALBUM_ID + " = \"" + id + "\"";
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
    public AlbumEntity get(SQLiteDatabase db, String uniqueId) {
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
        AlbumEntity albumEntity = null;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.ALBUM_ID + " = \"" + uniqueId + "\"";
            cursor = db.rawQuery(sql, null);
            if (App.DEBUG) {
                LogUtil.d(TAG, "get() " + "\"" + sql + "\"");
            }
            if (cursor != null && cursor.getCount() >= 0) {
                cursor.moveToFirst();
                albumEntity = create(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(cursor);
        }
        return albumEntity;
    }

    @Override
    public AlbumEntity create(Cursor cursor) {
        if (cursor == null || cursor.getCount() <= 0 || cursor.getColumnCount() <= 0) {
            return null;
        }
        try {
            AlbumEntity albumEntity = new AlbumEntity();
            int index = cursor.getColumnIndex(Consts.ALBUM_ID);
            if (index >= 0) {
                String id = cursor.getString(index);
                albumEntity.setId(id);
            }

            index = cursor.getColumnIndex(Consts.NAME);
            if (index >= 0) {
                String name = cursor.getString(index);
                albumEntity.setName(name);
            }
            index = cursor.getColumnIndex(Consts.NOTE);
            if (index >= 0) {
                String note = cursor.getString(index);
                albumEntity.setNote(note);
            }
            index = cursor.getColumnIndex(Consts.CREATE_DATE);
            if (index >= 0) {
                String createDate = cursor.getString(index);
                albumEntity.setCreateDate(createDate);
            }
            index = cursor.getColumnIndex(Consts.MOD_DATE);
            if (index >= 0) {
                String modDate = cursor.getString(index);
                albumEntity.setModDate(modDate);
            }
            index = cursor.getColumnIndex(Consts.CREATOR);
            if (index >= 0) {
                String creator = cursor.getString(index);
                albumEntity.setCreator(creator);
            }
            index = cursor.getColumnIndex(Consts.OWNER);
            if (index >= 0) {
                String owner = cursor.getString(index);
                albumEntity.setOwner(owner);
            }
            index = cursor.getColumnIndex(Consts.LINK);
            if (index >= 0) {
                String link = cursor.getString(index);
                albumEntity.setLink(link);
            }
            index = cursor.getColumnIndex(Consts.SIZE);
            if (index >= 0) {
                int size = cursor.getInt(index);
                albumEntity.setSize(size);
            }
            index = cursor.getColumnIndex(Consts.MEMBERS);
            if (index >= 0) {
                int members = cursor.getInt(index);
                albumEntity.setMembers(members);
            }
            index = cursor.getColumnIndex(Consts.COVER);
            if (index >= 0) {
                String cover = cursor.getString(index);
                albumEntity.setLocalCover(cover);
            }

            index = cursor.getColumnIndex(Consts.INVITE_CODE);
            if (index >= 0) {
                String inviteCode = cursor.getString(index);
                albumEntity.setInviteCode(inviteCode);
            }

            index = cursor.getColumnIndex(Consts.THUMB_FILE_ID);
            if (index >= 0) {
                String id = cursor.getString(index);
                albumEntity.setThumbFileId(id);
            }

            index = cursor.getColumnIndex(Consts.UPDATE_COUNT);
            if (index >= 0) {
                int updateCount = cursor.getInt(index);
                albumEntity.setUpdateCount(updateCount);
            }

            index = cursor.getColumnIndex(Consts.OLD_MEMBERS);
            if (index >= 0) {
                int old_members = cursor.getInt(index);
                albumEntity.setOldMembers(old_members);
            }

            index = cursor.getColumnIndex(Consts.IS_PRIVATE);
            if (index >= 0) {
                int isPrivate = cursor.getInt(index);
                albumEntity.setIsPrivate(isPrivate == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.IS_LOCKED);
            if (index >= 0) {
                int isLocked = cursor.getInt(index);
                albumEntity.setIsLocked(isLocked == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.COMMENT_OFF);
            if (index >= 0) {
                int commentOff = cursor.getInt(index);
                albumEntity.setCommentOff(commentOff == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.LIKE_OFF);
            if (index >= 0) {
                int likeOff = cursor.getInt(index);
                albumEntity.setLikeOff(likeOff == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.VIEWS);
            if (index >= 0) {
                int views = cursor.getInt(index);
                albumEntity.setViews(views);
            }

            index = cursor.getColumnIndex(Consts.SHARES);
            if (index >= 0) {
                int shares = cursor.getInt(index);
                albumEntity.setShares(shares);
            }

            index = cursor.getColumnIndex(Consts.DOWNLOADS);
            if (index >= 0) {
                int downloads = cursor.getInt(index);
                albumEntity.setDownloads(downloads);
            }

            index = cursor.getColumnIndex(Consts.NO_PUSH);
            if (index >= 0) {
                int likeOff = cursor.getInt(index);
                albumEntity.setNoPush(likeOff == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.PERMISSIONS);
            if (index >= 0) {
                int permission = cursor.getInt(index);
                albumEntity.setPermissions(permission);
            }

            if (albumEntity.isValid()) {
                return albumEntity;
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "clone Exception : " + e.getLocalizedMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("[id]		" + id);
        builder.append("\n[name]		" + name);
        builder.append("\n[create]		" + create_date);
        builder.append("\n[modify]		" + mod_date);
        builder.append("\n[creator]		" + creator);
        builder.append("\n[inviteCode]	" + inviteCode);
        builder.append("\n[link]		" + link);
        builder.append("\n[members]		" + members);
        builder.append("\n[note]		" + note);
        builder.append("\n[permissions]	" + permissions);
        builder.append("\n[size]		" + size);
        builder.append("\n[views]		" + views);
        builder.append("\n[shares]		" + shares);
        builder.append("\n[downloads]		" + downloads);
        builder.append("\n[cover]		" + cover);
        builder.append("\n[updateCount]	" + updateCount);
        builder.append("\n[oldMembers]	" + oldMembers);
        builder.append("\n[thumbFileId]	" + thumbFileId);
        return builder.toString();

    }

    // //////////////////////////////
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] bools = {isPrivate, isLocked, commentOff, likeOff, noPush, keepLocal};
        dest.writeString(thumbFileId);
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(note);
        dest.writeString(create_date);
        dest.writeString(mod_date);
        dest.writeString(creator);
        dest.writeString(owner);
        dest.writeString(link);
        dest.writeInt(size);
        dest.writeInt(views);
        dest.writeInt(shares);
        dest.writeInt(downloads);
        dest.writeInt(members);
        dest.writeInt(permissions);
        dest.writeString(inviteCode);
        dest.writeString(cover);
        dest.writeInt(updateCount);
        dest.writeInt(oldMembers);
        dest.writeBooleanArray(bools);
    }

    public final static Creator<AlbumEntity> CREATOR = new Creator<AlbumEntity>() {

        @Override
        public AlbumEntity createFromParcel(Parcel source) {
            // TODO Auto-generated method stub
            return new AlbumEntity(source);
        }

        @Override
        public AlbumEntity[] newArray(int size) {
            // TODO Auto-generated method stub
            return new AlbumEntity[size];
        }

    };
}
