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

public class FileEntity extends Data implements Parcelable {
    private final static String TAG = "FileEntity";
    private String id;
    private String name;
    private String note;
    private String createDate;
    private String modDate;
    private String album;
    private String creator;
    private String owner;
    private int size;
    private String digest;
    private String status;
    private String link;
    private String batch_id;
    private int seq_num;
    private int offset;
    private boolean has_thumb;
    private String mimeType;
    private String title;
    private int comments;
    private int shares;
    private int downloads;
    private int likes;
    private String filePath = "";
    private String fakeId;

    private boolean isNewUpload = false;

    public final static FileEntity EMPTY = new FileEntity();
    public final static String CREATE_SQL = "CREATE TABLE IF NOT EXISTS " + EMPTY.getTableName() // tablename
            + "( " // (
            + "_id INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT , " // _id
            + Consts.FILE_ID + " TEXT NOT NULL UNIQUE,"// FILE_ID
            + Consts.NAME + " TEXT  ," // NAME
            + Consts.NOTE + " TEXT ," // NOTE
            + Consts.CREATE_DATE + " TEXT  ," // CREATE_DATE
            + Consts.MOD_DATE + " TEXT ," // MOD_DATE
            + Consts.ALBUM + " TEXT NOT NULL ," // ALBUM
            + Consts.CREATOR + " TEXT  NOT NULL," // CREATOR
            + Consts.OWNER + " TEXT  NOT NULL," // OWNER
            + Consts.SIZE + " INTEGER, " // SIZE
            + Consts.DIGEST + " TEXT, " // DIGEST
            + Consts.STATUS + " TEXT, " // STATUS
            + Consts.LINK + " TEXT, " // LINK
            + Consts.BATCH_ID + " TEXT, " // BATCH_ID
            + Consts.SEQ_NUM + " INTEGER, " // SEQ_NUM
            + Consts.OFFSET + " INTEGER, " // OFFSET
            + Consts.HAS_THUMB + " INTEGER, " // HAS_THUMB
            + Consts.MIME_TYPE + " TEXT, " // MIME_TYPE
            + Consts.TITLE + " TEXT, " // TITLE
            + Consts.COMMENTS + " INTEGER, " // COMMENTS
            + Consts.SHARES + " INTEGER, " // SHARES
            + Consts.DOWNLOADS + " INTEGER, " // DOWNLOADS
            + Consts.LIKES + " INTEGER ," // LIKES
            + Consts.FILE_PATH + " TEXT " // LIKES
            + " ) ";

    public FileEntity(Parcel parcel) {
        boolean[] bools = {has_thumb};
        id = parcel.readString();
        name = parcel.readString();
        note = parcel.readString();
        createDate = parcel.readString();
        modDate = parcel.readString();
        album = parcel.readString();
        creator = parcel.readString();
        owner = parcel.readString();
        size = parcel.readInt();
        digest = parcel.readString();
        status = parcel.readString();
        link = parcel.readString();
        batch_id = parcel.readString();
        seq_num = parcel.readInt();
        offset = parcel.readInt();
        mimeType = parcel.readString();
        title = parcel.readString();
        filePath = parcel.readString();
        comments = parcel.readInt();
        shares = parcel.readInt();
        downloads = parcel.readInt();
        likes = parcel.readInt();
        parcel.readBooleanArray(bools);
        if (bools == null || bools.length < 1) {
            return;
        }
        has_thumb = bools[0];
    }

    public FileEntity() {

    }

    public FileEntity(String id, String title, String name, String note, String createDate, String modDate, String album, String creator, String owner, int size, String digest, String status,
                      String link, String batchId, int seqNum, int offset, boolean hasThumb, String mimeType, int comments, int shares, int downloads, int likes) {

        this.id = id;
        this.title = title;
        this.name = name;
        this.note = note;
        this.createDate = createDate;
        this.modDate = modDate;
        this.album = album;
        this.creator = creator;
        this.owner = owner;
        this.size = size;
        this.digest = digest;
        this.status = status;
        this.link = link;
        this.batch_id = batchId;
        this.seq_num = seqNum;
        this.offset = offset;
        this.has_thumb = hasThumb;
        this.mimeType = mimeType;
        this.comments = comments;
        this.shares = shares;
        this.downloads = downloads;
        this.likes = likes;
    }

    public boolean isUploading() {
        return this.getStatus().equals(Consts.FILEENTITY_STATUS_UNUPLOAD) || this.getStatus().equals(Consts.FILEENTITY_STATUS_UPLOADING);
    }

    public boolean isActive() {
        return this.getStatus().equals(Consts.FILEENTITY_STATUS_ACTIVE);
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getCreateDate() {
        return createDate;
    }

    public void setModDate(String modDate) {
        this.modDate = modDate;
    }

    public String getModDate() {
        return modDate;
    }

    public void setalbum(String album) {
        this.album = album;
    }

    public String getAlbum() {
        return album;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getCreator() {
        return creator;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSize() {
        return size;
    }

    public void setDigest(String digest) {
        this.digest = digest;
    }

    public String getDigest() {
        return digest;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getLink() {
        return link;
    }

    public void setBatchId(String batchId) {
        this.batch_id = batchId;
    }

    public String getBatchId() {
        return batch_id;
    }

    public void setSeqNum(int seqNum) {
        this.seq_num = seqNum;
    }

    public int getSeqNum() {
        return seq_num;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return offset;
    }

    public void setHasThumb(boolean hasThumb) {
        this.has_thumb = hasThumb;
    }

    public boolean getHasThumb() {
        return has_thumb;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public int getComments() {
        return comments;
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

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getLikes() {
        return likes;
    }

    public boolean isValid() {
        if (Utils.existsEmpty(id, creator, name, owner, album)) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        return super.equals(o);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("=====File======\n");
        builder.append("[id]		" + id + "\n");
//		builder.append("[name]		" + name + "\n");
//		builder.append("[note]		" + note + "\n");
//		builder.append("[createDate]	" + createDate + "\n");
//		builder.append("[modDate]		" + modDate + "\n");
//		builder.append("[album]		" + album + "\n");
//		builder.append("[creator]		" + creator + "\n");
//		builder.append("[owner]		" + owner + "\n");
//		builder.append("[size]		" + size + "\n");
//		builder.append("[digest]		" + digest + "\n");
//		builder.append("[status]		" + status + "\n");
//		builder.append("[link]		" + link + "\n");
//		builder.append("[batchId]		" + batch_id + "\n");
        builder.append("[seqNum]		" + seq_num + "\n");
//		builder.append("[offset]		" + offset + "\n");
//		builder.append("[hasThumb]		" + has_thumb + "\n");
//		builder.append("[mimeType]		" + mimeType + "\n");
//		builder.append("[title]		" + title + "\n");
        builder.append("[comments]		" + comments + "\n");
//		builder.append("[shares]		" + shares + "\n");
//		builder.append("[downloads]		" + downloads + "\n");
        builder.append("[likes]		" + likes + "\n ");
//		builder.append("[filePath]		" + filePath);
        return builder.toString();
    }

    private ContentValues getCV() {
        ContentValues cv = new ContentValues();
        cv.put(Consts.FILE_ID, id);
        cv.put(Consts.NAME, name);
        cv.put(Consts.NOTE, note);
        cv.put(Consts.CREATE_DATE, createDate);
        cv.put(Consts.MOD_DATE, modDate);
        cv.put(Consts.ALBUM, album);
        cv.put(Consts.CREATOR, creator);
        cv.put(Consts.OWNER, owner);
        cv.put(Consts.SIZE, size);
        cv.put(Consts.DIGEST, digest);
        cv.put(Consts.STATUS, status);
        cv.put(Consts.LINK, link);
        cv.put(Consts.BATCH_ID, batch_id);
        cv.put(Consts.SEQ_NUM, seq_num);
        cv.put(Consts.OFFSET, offset);
        cv.put(Consts.HAS_THUMB, has_thumb ? Consts.BOOLEAN_TRUE : Consts.BOOLEAN_FALSE);
        cv.put(Consts.MIME_TYPE, mimeType);
        cv.put(Consts.TITLE, title);
        cv.put(Consts.FILE_PATH, filePath);
        cv.put(Consts.COMMENTS, comments);
        cv.put(Consts.SHARES, shares);
        cv.put(Consts.DOWNLOADS, downloads);
        cv.put(Consts.LIKES, likes);
        return cv;
    }

    private ContentValues getCV(HashSet<String> unInsertKey) {
        ContentValues cv = new ContentValues();
        if (unInsertKey == null) {
            cv.put(Consts.FILE_ID, id);
            cv.put(Consts.NAME, name);
            cv.put(Consts.NOTE, note);
            cv.put(Consts.CREATE_DATE, createDate);
            cv.put(Consts.MOD_DATE, modDate);
            cv.put(Consts.ALBUM, album);
            cv.put(Consts.CREATOR, creator);
            cv.put(Consts.OWNER, owner);
            cv.put(Consts.SIZE, size);
            cv.put(Consts.DIGEST, digest);
            cv.put(Consts.STATUS, status);
            cv.put(Consts.LINK, link);
            cv.put(Consts.BATCH_ID, batch_id);
            cv.put(Consts.SEQ_NUM, seq_num);
            cv.put(Consts.OFFSET, offset);
            cv.put(Consts.HAS_THUMB, has_thumb ? Consts.BOOLEAN_TRUE : Consts.BOOLEAN_FALSE);
            cv.put(Consts.MIME_TYPE, mimeType);
            cv.put(Consts.TITLE, title);
            cv.put(Consts.FILE_PATH, filePath);
            cv.put(Consts.COMMENTS, comments);
            cv.put(Consts.SHARES, shares);
            cv.put(Consts.DOWNLOADS, downloads);
            cv.put(Consts.LIKES, likes);
        } else {
            cv.put(Consts.FILE_ID, id);
            cv.put(Consts.NAME, name);
            cv.put(Consts.NOTE, note);
            cv.put(Consts.CREATE_DATE, createDate);
            cv.put(Consts.MOD_DATE, modDate);
            cv.put(Consts.ALBUM, album);
            cv.put(Consts.CREATOR, creator);
            cv.put(Consts.OWNER, owner);
            cv.put(Consts.SIZE, size);
            cv.put(Consts.DIGEST, digest);
            cv.put(Consts.STATUS, status);
            cv.put(Consts.LINK, link);
            cv.put(Consts.BATCH_ID, batch_id);
            cv.put(Consts.SEQ_NUM, seq_num);
            cv.put(Consts.OFFSET, offset);
            cv.put(Consts.HAS_THUMB, has_thumb ? Consts.BOOLEAN_TRUE : Consts.BOOLEAN_FALSE);
            cv.put(Consts.MIME_TYPE, mimeType);
            cv.put(Consts.TITLE, title);
            if (!unInsertKey.contains(Consts.FILE_PATH)) {
                cv.put(Consts.FILE_PATH, filePath);
            }
            cv.put(Consts.COMMENTS, comments);
            cv.put(Consts.SHARES, shares);
            cv.put(Consts.DOWNLOADS, downloads);
            cv.put(Consts.LIKES, likes);
        }

        return cv;
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
            String sql = "UPDATE " + getTableName() + " SET " + fieldName + " = " + fieldName + ((increase) ? " + " : " - ") + " 1 " + " WHERE " + Consts.FILE_ID + "=\"" + id + "\"";
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean exists(SQLiteDatabase db) {
        Cursor cursor = null;
        boolean result = false;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.FILE_ID + " in ( \"" + id + "\" ,\"" + Utils.createEntityHashId(this) + "\" )";
            cursor = db.rawQuery(sql, null);
            if (cursor != null) {
                result = cursor.getCount() > 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(cursor);
        }
        return result;
    }

    @Override
    public String getTableName() {
        return Consts.FILES;
    }

    public void clone(FileEntity fileEntity) {
        this.id = fileEntity.getId();
        this.title = fileEntity.getTitle();
        this.name = fileEntity.getName();
        this.note = fileEntity.getNote();
        this.createDate = fileEntity.getCreateDate();
        this.modDate = fileEntity.getModDate();
        this.album = fileEntity.getAlbum();
        this.creator = fileEntity.getCreator();
        this.owner = fileEntity.getOwner();
        this.size = fileEntity.getSize();
        this.digest = fileEntity.getDigest();
        this.status = fileEntity.getStatus();
        this.link = fileEntity.getLink();
        this.batch_id = fileEntity.getBatchId();
        this.seq_num = fileEntity.getSeqNum();
        this.offset = fileEntity.getOffset();
        this.has_thumb = fileEntity.has_thumb;
        this.mimeType = fileEntity.getMimeType();
        this.comments = fileEntity.getComments();
        this.shares = fileEntity.getShares();
        this.downloads = fileEntity.getDownloads();
        this.likes = fileEntity.getLikes();
    }

    @Override
    public FileEntity create(Cursor cursor) {
        if (cursor == null || cursor.isClosed()) {
            LogUtil.e(TAG, "cursor is null");
            return null;
        }
        if (cursor.isClosed() || cursor.getCount() <= 0) {
            LogUtil.e(TAG, "cursor is Closed or count is 0");
            return null;
        }
        FileEntity entity = new FileEntity();
        try {
            int index = cursor.getColumnIndex(Consts.FILE_ID);
            if (index >= 0) {
                String id = cursor.getString(index);
                entity.setId(id);
            }

            index = cursor.getColumnIndex(Consts.NAME);
            if (index >= 0) {
                String name = cursor.getString(index);
                entity.setName(name);
            }

            index = cursor.getColumnIndex(Consts.NOTE);
            if (index >= 0) {
                String note = cursor.getString(index);
                entity.setNote(note);
            }

            index = cursor.getColumnIndex(Consts.CREATE_DATE);
            if (index >= 0) {
                String createDate = cursor.getString(index);
                entity.setCreateDate(createDate);
            }

            index = cursor.getColumnIndex(Consts.MOD_DATE);
            if (index >= 0) {
                String modDate = cursor.getString(index);
                entity.setModDate(modDate);
            }

            index = cursor.getColumnIndex(Consts.ALBUM);
            if (index >= 0) {
                String album = cursor.getString(index);
                entity.setalbum(album);
            }

            index = cursor.getColumnIndex(Consts.CREATOR);
            if (index >= 0) {
                String creator = cursor.getString(index);
                entity.setCreator(creator);
            }

            index = cursor.getColumnIndex(Consts.OWNER);
            if (index >= 0) {
                String owner = cursor.getString(index);
                entity.setOwner(owner);
            }

            index = cursor.getColumnIndex(Consts.SIZE);
            if (index >= 0) {
                int size = cursor.getInt(index);
                entity.setSize(size);
            }

            index = cursor.getColumnIndex(Consts.DIGEST);
            if (index >= 0) {
                String digest = cursor.getString(index);
                entity.setDigest(digest);
            }

            index = cursor.getColumnIndex(Consts.STATUS);
            if (index >= 0) {
                String status = cursor.getString(index);
                entity.setStatus(status);
            }

            index = cursor.getColumnIndex(Consts.LINK);
            if (index >= 0) {
                String link = cursor.getString(index);
                entity.setLink(link);
            }

            index = cursor.getColumnIndex(Consts.BATCH_ID);
            if (index >= 0) {
                String batchId = cursor.getString(index);
                entity.setBatchId(batchId);
            }

            index = cursor.getColumnIndex(Consts.SEQ_NUM);
            if (index >= 0) {
                int seqnum = cursor.getInt(index);
                entity.setSeqNum(seqnum);
            }

            index = cursor.getColumnIndex(Consts.OFFSET);
            if (index >= 0) {
                int offset = cursor.getInt(index);
                entity.setOffset(offset);
            }

            index = cursor.getColumnIndex(Consts.HAS_THUMB);
            if (index >= 0) {
                int thumb = cursor.getInt(index);
                entity.setHasThumb(thumb == Consts.BOOLEAN_TRUE);
            }

            index = cursor.getColumnIndex(Consts.MIME_TYPE);
            if (index >= 0) {
                String mimeType = cursor.getString(index);
                entity.setMimeType(mimeType);
            }

            index = cursor.getColumnIndex(Consts.TITLE);
            if (index >= 0) {
                String title = cursor.getString(index);
                entity.setTitle(title);
            }

            index = cursor.getColumnIndex(Consts.COMMENTS);
            if (index >= 0) {
                int comments = cursor.getInt(index);
                entity.setComments(comments);
            }

            index = cursor.getColumnIndex(Consts.SHARES);
            if (index >= 0) {
                int shares = cursor.getInt(index);
                entity.setShares(shares);
            }

            index = cursor.getColumnIndex(Consts.DOWNLOADS);
            if (index >= 0) {
                int downloads = cursor.getInt(index);
                entity.setDownloads(downloads);
            }

            index = cursor.getColumnIndex(Consts.LIKES);
            if (index >= 0) {
                int likes = cursor.getInt(index);
                entity.setLikes(likes);
            }

            index = cursor.getColumnIndex(Consts.FILE_PATH);
            if (index >= 0) {
                String filePath = cursor.getString(index);
                entity.setFilePath(filePath);
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
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.FILE_ID + " = \"" + uniqueId + "\"");
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean deleteFilesInAlbum(SQLiteDatabase db, String albumId) {
        if (db == null) {
            LogUtil.e(TAG, "ERROR: db is null");
            return false;
        }
        if (db.isReadOnly()) {
            LogUtil.e(TAG, "ERROR: db is read only");
            return false;
        }
        if (Utils.isEmpty(albumId)) {
            LogUtil.e(TAG, "ERROR: file id is empty");
            return false;
        }
        try {
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.ALBUM + " = \"" + albumId + "\"");
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
        if (Utils.isEmpty(id)) {
            LogUtil.e(TAG, "ERROR: file id is empty");
            return false;
        }
        try {
            db.execSQL("DELETE FROM " + getTableName() + " WHERE " + Consts.FILE_ID + " = \"" + id + "\"");
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
        ContentValues cv = getCV();
        try {
            if (exists(db)) {
                return update(db);
            }
            long row = db.insert(getTableName(), null, cv);
            if (row > -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean update(SQLiteDatabase db) {
        if (db == null || db.isReadOnly() || !isValid()) {
            return false;
        }
        try {
            ContentValues cv = getCV();
            String[] args = {id, Utils.createEntityHashId(this)};
            int row = db.update(getTableName(), cv, Consts.FILE_ID + " in (? , ?)", args);
            // if (row == 0) {
            // // update uploading file
            // args[0] = Utils.createEntityHashId(this);
            // row = db.update(getTableName(), cv, Consts.FILE_ID + "=?", args);
            // }
            if (row == -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean setFileCreateSuccess(SQLiteDatabase db) {
        HashSet<String> unInsertKey = new HashSet<String>();
        unInsertKey.add(Consts.FILE_PATH);
        return setUploaded(db, unInsertKey);
    }

    public boolean setUploaded(SQLiteDatabase db) {
        return setUploaded(db, null);
    }

    public boolean setUploaded(SQLiteDatabase db, HashSet<String> unInsertKey) {
        if (db == null || db.isReadOnly() || !isValid()) {
            return false;
        }
        try {
            if (App.DEBUG) {
                LogUtil.e(TAG, "<------------------->");
            }
            ContentValues cv = getCV(unInsertKey);
            String[] args = {Utils.createEntityHashId(this)};
            int row = db.update(getTableName(), cv, Consts.FILE_ID + "=?", args);
            if (row == 0) {
                args = new String[]{id};
                row = db.update(getTableName(), cv, Consts.FILE_ID + "=?", args);
                // if (App.DEBUG) {
                // LogUtil.e(TAG, "------>native id=" + id);
                // }
            }
            if (App.DEBUG) {
                LogUtil.e(TAG, "updateUploaded :" + row);
            }
            if (row > 0) {
                // update albumEntity size
                HashSet<String> keys = new HashSet<String>();
                keys.add(Consts.SIZE);
                AlbumEntity album = new AlbumEntity();
                album = album.get(db, getAlbum());
                if (album != null) {
                    album.setUpdateKey(keys);
                    album.setSize(album.getSize() + 1);
                    album.update(db, false);
                    if (App.DEBUG) {
                        LogUtil.e(TAG, "update albumEntity size :" + album.getSize());
                    }
                }

            }
            if (row == -1) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public FileEntity get(SQLiteDatabase db, String uniqueId) {
        if (db == null) {
            LogUtil.d(TAG, "db is null");
            return null;
        }
        if (Utils.isEmpty(uniqueId)) {
            LogUtil.d(TAG, "uniqueId is empty");
            return null;
        }
        Cursor cursor = null;
        FileEntity fileEntity = null;
        try {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + Consts.FILE_ID + " = \"" + uniqueId + "\"";
            cursor = db.rawQuery(sql, null);
            if (cursor != null && cursor.getCount() >= 0) {
                cursor.moveToFirst();
                fileEntity = create(cursor);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            close(cursor);
        }
        return fileEntity;
    }

    // ///////////////////////////////parcel///////////////////////////
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        boolean[] bools = {has_thumb};
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(note);
        dest.writeString(createDate);
        dest.writeString(modDate);
        dest.writeString(album);
        dest.writeString(creator);
        dest.writeString(owner);
        dest.writeInt(size);
        dest.writeString(digest);
        dest.writeString(status);
        dest.writeString(link);
        dest.writeString(batch_id);
        dest.writeInt(seq_num);
        dest.writeInt(offset);
        dest.writeString(mimeType);
        dest.writeString(title);
        dest.writeString(filePath);
        dest.writeInt(comments);
        dest.writeInt(shares);
        dest.writeInt(downloads);
        dest.writeInt(likes);
        dest.writeBooleanArray(bools);
    }

    public static final Parcelable.Creator<FileEntity> CREATOR = new Parcelable.Creator<FileEntity>() {
        public FileEntity createFromParcel(Parcel parcel) {
            return new FileEntity(parcel);
        }

        public FileEntity[] newArray(int size) {
            return new FileEntity[size];
        }
    };

    public String getFakeId() {
        return fakeId;
    }

    public void setFakeId(String fakeId) {
        this.fakeId = fakeId;
    }

    public void createFakeId() {

        this.fakeId = Utils.createEntityHashId(this);

    }

    public boolean isNewUpload() {
        return isNewUpload;
    }

    public void setNewUpload(boolean isNewUpload) {
        this.isNewUpload = isNewUpload;
    }

}
