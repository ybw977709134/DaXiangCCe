package com.daxiangce123.android;

import android.database.sqlite.SQLiteDatabase;

import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.util.Utils;

/**
 * @author ram
 * @project DaXiangCe
 * @time Oct 8, 2014
 */
public class EventManger {

    public final static boolean needShow(Event event, SQLiteDatabase db) {
        if (!event.isNotification()) {
            event.setNeedShown(false);
            return false;
        }
        boolean needNotify = false;
        if (!isMe(event.getUserId())) {
            final String type = event.getOpType();
            if (Consts.ALBUM_DELETED.equals(type)) {
                needNotify = true;
            } else if (Consts.SYSTEM_ALBUM_DELETED.equals(type)) {
                needNotify = true;
            } else if (Consts.MEMBER_LEFT.equals(type)) {
                MemberEntity memberEntity = (MemberEntity) event.getObject();
                needNotify = App.getUid().equals(memberEntity.getUserId());
            } else if (Consts.FILE_DELETED.equals(type)) {
                needNotify = isMyFile((FileEntity) event.getObject());
            } else if (Consts.SYSTEM_FILE_DELETED.equals(type)) {
                needNotify = isMyFile((FileEntity) event.getObject());
            } else if (Consts.ALBUM_SHARED.equals(type)) {
                needNotify = isMyAlbum((AlbumEntity) event.getObject());
            } else if (Consts.FILE_SHARED.equals(type)) {
                needNotify = isMyFile((FileEntity) event.getObject());
            } else if (Consts.FILE_DOWNLOADED.equals(type)) {
                needNotify = isMyFile((FileEntity) event.getObject());
            } else if (Consts.COMMENT_CREATED.equals(type) || Consts.LIKE_CREATED.equals(type)) {
                Object obj = event.getObject();
                String objId = null;
                String objType = null;
                if (obj instanceof CommentEntity) {
                    CommentEntity commentEntity = (CommentEntity) obj;
                    if (App.getUid().equals(commentEntity.getReplyToUser())) {
                        return true;
                    }
                    objId = ((CommentEntity) obj).getObjId();
                    objType = ((CommentEntity) obj).getObjType();
                } else {
                    objId = ((LikeEntity) obj).getObjId();
                    objType = ((LikeEntity) obj).getObjType();
                }
                needNotify = isMyEvent(objType, objId, db);
            }
        }
        event.setNeedShown(needNotify);
        return needNotify;
    }

    // if (isMe(event.getUserId())) {
    // event.setNeedShown(false);
    // }
    // // just save notification event
    // if (event.isNotification() && event.isNeedShown()) {
    // event.insert(db);
    // }
    private final static boolean isMyEvent(String objType, String objId, SQLiteDatabase db) {
        if (Utils.isEmpty(App.getUid())) {
            return false;
        }
        boolean result = false;
        if (Consts.ALBUM.equals(objType)) {
            AlbumEntity albumEntity = AlbumEntity.EMPTY.get(db, objId);
            String owner = albumEntity != null ? albumEntity.getOwner() : null;
            return isMe(owner);
        }
        if (Consts.FILE.equals(objType)) {
            FileEntity fileEntity = FileEntity.EMPTY.get(db, objId);
            result = isMyFile(fileEntity);
            if (result) {
                // If has joined its album
                // result = isInMyAlbum(fileEntity.getAlbum(), db);
            }
        }
        return result;
    }

    private final static boolean isMyFile(FileEntity fileEntity) {
        String owner = fileEntity != null ? fileEntity.getOwner() : null;
        return isMe(owner);
    }

    private final static boolean isMyAlbum(AlbumEntity album) {
        String owner = album != null ? album.getOwner() : null;
        return isMe(owner);
    }

    // private final static boolean isInMyAlbum(String albumId, SQLiteDatabase
    // db) {
    // return AlbumEntity.EMPTY.get(db, albumId) != null;
    // }

    private final static boolean isMe(String owner) {
        if (App.getUid().equals(owner)) {
            return true;
        }
        return false;
    }

}
