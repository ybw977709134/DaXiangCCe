package com.daxiangce123.android.parser;

import android.annotation.SuppressLint;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.AlbumAct;
import com.daxiangce123.android.data.AlbumEntity;
import com.daxiangce123.android.data.AlbumMembers;
import com.daxiangce123.android.data.AlbumSamples;
import com.daxiangce123.android.data.AppData;
import com.daxiangce123.android.data.Banner;
import com.daxiangce123.android.data.Batch;
import com.daxiangce123.android.data.Batches;
import com.daxiangce123.android.data.Binding;
import com.daxiangce123.android.data.Bindings;
import com.daxiangce123.android.data.CommentEntity;
import com.daxiangce123.android.data.Contact;
import com.daxiangce123.android.data.Event;
import com.daxiangce123.android.data.FileComments;
import com.daxiangce123.android.data.FileEntity;
import com.daxiangce123.android.data.FileLike;
import com.daxiangce123.android.data.HotAlumList;
import com.daxiangce123.android.data.ItemListWrapper;
import com.daxiangce123.android.data.LikeEntity;
import com.daxiangce123.android.data.ListAllAlbums;
import com.daxiangce123.android.data.ListBanners;
import com.daxiangce123.android.data.MemberEntity;
import com.daxiangce123.android.data.NearbyAlbum;
import com.daxiangce123.android.data.NearbyAlumList;
import com.daxiangce123.android.data.RegionInfo;
import com.daxiangce123.android.data.SensitiveWords;
import com.daxiangce123.android.data.SimpleAlbumItems;
import com.daxiangce123.android.data.SplashAdInfo;
import com.daxiangce123.android.data.TempHttpToken;
import com.daxiangce123.android.data.TempToken;
import com.daxiangce123.android.data.Token;
import com.daxiangce123.android.data.UserInfo;
import com.daxiangce123.android.data.UserSuspendedInfo;
import com.daxiangce123.android.data.base.ObjectsWrapper;
import com.daxiangce123.android.http.Error;
import com.daxiangce123.android.util.JSONUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.MediaUtil;
import com.daxiangce123.android.util.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

/**
 * @author ram
 * @project Groubum
 * @time Feb 28, 2014
 */
public class Parser {

    private final static String TAG = "Parser";

    /**
     * <pre>
     * {
     *     "id" : "e8596838-78e2-11e3-816b-a30d01488505",
     *     "name" : "2013 Christmas Party",
     *     "note" : "Funny shit happened here",
     *     "create_date" : "2013-12-25T03:17:25Z",
     *     "mod_date" : "2013-12-25T03:17:25Z",
     *     "creator" : "7f6a089a-78e3-11e3-bb7a-4b4c0493315c",
     *     "owner" : "7f6a089a-78e3-11e3-bb7a-4b4c0493315c",
     *     "link" : "efsIj9",
     *     "size" : 45,
     *     "members" : 15,
     *     "invite_code":"10172"
     * }
     * </pre>
     */
    public final static AlbumEntity parseAlbum(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }

        JSONObject jo = JSONObject.parseObject(jsonstr);
        try {
            String id = jo.getString(Consts.ID);
            String name = jo.getString(Consts.NAME);
            String note = jo.getString(Consts.NOTE);
            String cdate = jo.getString(Consts.CREATE_DATE);
            String mdata = jo.getString(Consts.MOD_DATE);
            String creator = jo.getString(Consts.CREATOR);
            String owner = jo.getString(Consts.OWNER);
            String link = jo.getString(Consts.LINK);
            int size = jo.getInteger(Consts.SIZE);
            int members = jo.getInteger(Consts.MEMBERS);
            String inviteCode = jo.getString(Consts.INVITE_CODE);
            String thumbFileId = jo.getString(Consts.THUMB_FILE_ID);
            boolean isPrivate = jo.getBooleanValue(Consts.IS_PRIVATE);
            boolean isLocked = jo.getBooleanValue(Consts.IS_LOCKED);
            boolean commentOff = jo.getBooleanValue(Consts.COMMENT_OFF);
            boolean likeOff = jo.getBooleanValue(Consts.LIKE_OFF);
            int views = jo.getIntValue(Consts.VIEWS);
            int shares = jo.getIntValue(Consts.SHARES);
            int downloads = jo.getIntValue(Consts.DOWNLOADS);
            JSONArray ja = jo.getJSONArray(Consts.PERMISSIONS);
            int permissions = 0;
            for (int i = 0; i < ja.size(); i++) {
                String per = ja.getString(i);
                if (Utils.isEmpty(per)) {
                    continue;
                }
                per = per.trim().toLowerCase(Locale.ENGLISH);
                if ("read".equals(per)) {
                    permissions = permissions | Consts.IO_PERMISSION_R;
                } else if ("write".equals(per)) {
                    permissions = permissions | Consts.IO_PERMISSION_W;
                }
            }
            AlbumEntity albumEntity = new AlbumEntity(id, name, note, cdate, mdata, creator, owner, link, size, views, shares, downloads, members, inviteCode, isPrivate, isLocked, commentOff,
                    likeOff, permissions);
            albumEntity.setThumbFileId(thumbFileId);
            // AlbumEntity albumEntity = new AlbumEntity(id, name, note, cdate,
            // mdata, creator, owner, link, size, members, inviteCode,
            // isPrivate, isLocked, commentOff, likeOff, permissions);
            if (albumEntity.isValid()) {
                return albumEntity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String parseFileFakeId(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        String fakeId = null;
        try {
            JSONObject jo = JSONObject.parseObject(jsonstr);
            String albumdId = jo.getString(Consts.ALBUM);
            String batchId = jo.getString(Consts.BATCH_ID);
            int seqNum = jo.getInteger(Consts.SEQ_NUM);
            fakeId = Utils.createHashId(albumdId, batchId, String.valueOf(seqNum));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fakeId;
    }

    public static String parseAlbumThumId(String jonstr) {
        if (!JSONUtil.isJSONObject(jonstr)) {
            return null;
        }
        String fileId = null;
        try {
            JSONObject jo = JSONObject.parseObject(jonstr);
            fileId = jo.getString(Consts.THUMB_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileId;
    }

    public static String parseFileFakeIdFromFileEntity(String jsonString) {
        FileEntity fileEntity = parseFile(jsonString);
        return Utils.createEntityHashId(fileEntity);

    }

    public static String parseAlbumId(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        String albumdId = null;
        try {
            JSONObject jo = JSONObject.parseObject(jsonstr);
            albumdId = jo.getString(Consts.ALBUM);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return albumdId;
    }

    /**
     * <pre>
     * {
     *     "id" : "7d657bd4-79be-11e3-a3e4-e3159f61335d",
     *     "title" : "Rick naked.png",
     *     "name" : "Rick naked.png",
     *     "note" : "Rick seen naked at the party",
     *     "create_date" : "2013-12-24T23:17:25Z",
     *     "mod_date" : "2013-12-24T23:17:25Z",
     *     "album" : "e8596838-78e2-11e3-816b-a30d01488505",
     *     "creator" : "008de6d6-79bf-11e3-a80d-df9c0ff87f82",
     *     "owner" : "008de6d6-79bf-11e3-a80d-df9c0ff87f82",
     *     "size" : 74838,
     *     "digest" : "db63c0473683b067375b2adc186aeafc51975e4a",
     *     "status" : "active",
     *     "link" : "fe2Ifl",
     *     "batch_id" : "34e52342-7f44-11e3-a1e5-6f0818200885",
     *     "seq_num": 1,
     *     "offset": ,
     *     "has_thumb" : true,
     *     "mime_type" : "image/png"
     *     "comments" : 1,
     *     "shares" : 1,
     *     "downloads" : 1,
     *     "likes" : 1
     * }
     * </pre>
     */

    public static FileEntity parseFile(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonstr);
        try {
            String id = jo.getString(Consts.ID);
            String title = jo.getString(Consts.TITLE);
            String name = jo.getString(Consts.NAME);
            String note = jo.getString(Consts.NOTE);
            String cdate = jo.getString(Consts.CREATE_DATE);
            String mdata = jo.getString(Consts.MOD_DATE);
            String album = jo.getString(Consts.ALBUM);
            String creator = jo.getString(Consts.CREATOR);
            String owner = jo.getString(Consts.OWNER);
            int size = jo.getInteger(Consts.SIZE);
            String digest = jo.getString(Consts.DIGEST);
            String status = jo.getString(Consts.STATUS);
            String link = jo.getString(Consts.LINK);
            String batchId = jo.getString(Consts.BATCH_ID);
            int seqNum = jo.getInteger(Consts.SEQ_NUM);
            int offset = jo.getInteger(Consts.OFFSET);
            boolean hasThumb = jo.getBoolean(Consts.HAS_THUMB);
            String mimeType = jo.getString(Consts.MIME_TYPE);
            int comments = 0, shares = 0, downloads = 0, likes = 0;
            try {
                comments = jo.getInteger(Consts.COMMENTS);
                shares = jo.getInteger(Consts.SHARES);
                downloads = jo.getInteger(Consts.DOWNLOADS);
                likes = jo.getInteger(Consts.LIKES);
            } catch (Exception e) {
                comments = jo.getInteger("num_comment");
                shares = jo.getInteger("num_share");
                downloads = jo.getInteger("num_download");
                likes = jo.getInteger("num_like");
            }

            FileEntity fileEntity = new FileEntity();
            fileEntity.setId(id);
            fileEntity.setTitle(title);
            fileEntity.setName(name);
            fileEntity.setNote(note);
            fileEntity.setCreateDate(cdate);
            fileEntity.setModDate(mdata);
            fileEntity.setalbum(album);
            fileEntity.setCreator(creator);
            fileEntity.setOwner(owner);
            fileEntity.setSize(size);
            fileEntity.setDigest(digest);
            fileEntity.setStatus(status);
            fileEntity.setLink(link);
            fileEntity.setBatchId(batchId);
            fileEntity.setSeqNum(seqNum);
            fileEntity.setOffset(offset);
            fileEntity.setHasThumb(hasThumb);
            fileEntity.setMimeType(mimeType);
            fileEntity.setComments(comments);
            fileEntity.setShares(shares);
            fileEntity.setDownloads(downloads);
            fileEntity.setLikes(likes);
            if (fileEntity.isValid()) {
                return fileEntity;
            }
        } catch (Exception e) {
            LogUtil.d(TAG, "Exception--->parseFile():" + jsonstr);
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     *   "user_id" : "String,
     *     "name" : String,
     *     "album_id" : String,
     *     "role" : String
     * }
     * </pre>
     */

    @SuppressLint("DefaultLocale")
    public final static MemberEntity parseMember(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonstr);
        try {
            String userId = jo.getString(Consts.USER_ID);
            String name = jo.getString(Consts.NAME);
            String albumId = jo.getString(Consts.ALBUM_ID);
            String albumName = jo.getString(Consts.ALBUM_NAME);
            String role = jo.getString(Consts.ROLE);
            JSONArray ja = jo.getJSONArray(Consts.PERMISSIONS);
            int permissions = 0;
            for (int i = 0; i < ja.size(); i++) {
                String per = ja.getString(i);
                if (Utils.isEmpty(per)) {
                    continue;
                }
                per = per.trim().toLowerCase();
                if ("read".equals(per)) {
                    permissions = permissions | Consts.IO_PERMISSION_R;
                } else if ("write".equals(per)) {
                    permissions = permissions | Consts.IO_PERMISSION_W;
                }
            }

            MemberEntity memberEntity = new MemberEntity();
            memberEntity.setUserId(userId);
            memberEntity.setName(name);
            memberEntity.setAlbumId(albumId);
            memberEntity.setAlbumName(albumName);
            memberEntity.setRole(role);
            memberEntity.setPermissions(permissions);
            if (memberEntity.isValid()) {
                return memberEntity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * @param albumEntity
     * @return
     * @see #parseAlbum
     */
    public final static String albumToJson(AlbumEntity albumEntity) {
        if (albumEntity == null) {
            return null;
        }
        try {
            JSONObject jo = new JSONObject();
            jo.put(Consts.ID, albumEntity.getId());
            jo.put(Consts.NAME, albumEntity.getName());
            jo.put(Consts.NOTE, albumEntity.getNote());
            jo.put(Consts.CREATE_DATE, albumEntity.getCreateDate());
            jo.put(Consts.MOD_DATE, albumEntity.getModDate());
            jo.put(Consts.CREATOR, albumEntity.getCreator());
            jo.put(Consts.OWNER, albumEntity.getOwner());
            jo.put(Consts.LINK, albumEntity.getLink());
            jo.put(Consts.SIZE, albumEntity.getSize());
            jo.put(Consts.MEMBERS, albumEntity.getMembers());
            return jo.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static UserSuspendedInfo parseUserSupended(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject jsonObject = JSONObject.parseObject(jsonStr);
        try {
            String userId = jsonObject.getString(Consts.USER_ID);
            String userName = jsonObject.getString(Consts.USER_NAME);
            String status = jsonObject.getString(Consts.STATUS);
            String createDate = jsonObject.getString(Consts.CREATE_DATE);
            String expireDate = jsonObject.getString(Consts.EXPIRE_DATE);
            UserSuspendedInfo userSuspendedInfo = new UserSuspendedInfo();
            userSuspendedInfo.setUserId(userId);
            userSuspendedInfo.setUserName(userName);
            userSuspendedInfo.setStatus(status);
            userSuspendedInfo.setCreateDate(createDate);
            userSuspendedInfo.setExpireDate(expireDate);
            return userSuspendedInfo;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static Banner parseBanner(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject joObject = JSONObject.parseObject(jsonStr);
        try {
            String bannerId = joObject.getString(Consts.BANNER_ID);
            String name = joObject.getString(Consts.NAME);
            String createDate = joObject.getString(Consts.CREATE_DATE);
            String modDate = joObject.getString(Consts.MOD_DATE);
            String startDate = joObject.getString(Consts.START_DATE);
            String endDate = joObject.getString(Consts.END_DATE);
            int seqNum = joObject.getInteger(Consts.SEQ_NUM);
            AlbumEntity album = parseAlbum(joObject.getString(Consts.ALBUM));
            Banner banner = new Banner();
            banner.setBannerId(bannerId);
            banner.setName(name);
            banner.setCreateDate(createDate);
            banner.setModDate(modDate);
            banner.setStartDate(startDate);
            banner.setEndDate(endDate);
            banner.setSeqNum(seqNum);
            banner.setAlbum(album);

            return banner;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public final static ListBanners parseBannerList(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject joObject = JSONObject.parseObject(jsonStr);
        try {
            JSONArray ja = joObject.getJSONArray(Consts.BANNERS);
            LinkedList<Banner> banners = null;
            Iterator<Object> lit = ja.iterator();
            if (lit != null && ja.size() > 0) {
                banners = new LinkedList<Banner>();
                while (lit.hasNext()) {
                    JSONObject jo = (JSONObject) lit.next();
                    Banner banner = parseBanner(jo.toString());
                    if (banner != null) {
                        banners.add(banner);
                    }
                }
            }

            return new ListBanners(banners);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    /**
     * <pre>
     * {
     *     "limit" : 3,
     *     "has_more" : true,
     *     "albums" :
     *     [
     *          {Album},
     *          {Album},
     *          {Album}
     *     ]
     * }
     * </pre>
     *
     * @param jsonstr
     * @return
     * @time Feb 28, 2014
     */

    public final static ListAllAlbums parseAlbumList(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject joOuter = JSONObject.parseObject(jsonstr);
        try {
            int limit = joOuter.getInteger(Consts.LIMIT);
            boolean hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            JSONArray ja = joOuter.getJSONArray(Consts.ALBUMS);
            LinkedList<AlbumEntity> albums = null;

            Iterator<Object> lit = ja.iterator();
            if (lit != null && ja.size() > 0) {
                albums = new LinkedList<AlbumEntity>();
                while (lit.hasNext()) {
                    JSONObject jo = (JSONObject) lit.next();
                    AlbumEntity entity = parseAlbum(jo.toString());
                    if (entity != null) {
                        albums.add(entity);
                    }
                }
            }
            return new ListAllAlbums(limit, hasMore, albums);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     * 	    "limit" : 3,
     * 	    "has_more" : false,
     * 	     "comments" :
     * 	[
     *         {Like},
     *         {Like},
     *         {Like}
     *     ]	 *
     *    }
     * </pre>
     *
     * @param  jsonStr
     * @return
     * @time Mar 21, 2014
     */

    public final static FileLike parseFileLike(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject joOuter = JSONObject.parseObject(jsonStr);
        try {
            int limit = joOuter.getInteger(Consts.LIMIT);
            boolean hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            JSONArray ja = joOuter.getJSONArray(Consts.LIKE);
            LinkedList<LikeEntity> like = null;

            Iterator<Object> lit = ja.iterator();
            if (lit != null && ja.size() > 0) {
                like = new LinkedList<LikeEntity>();
                while (lit.hasNext()) {
                    JSONObject jo = (JSONObject) lit.next();
                    LikeEntity entity = parseLike(jo.toString());
                    if (entity != null) {
                        like.add(entity);
                    }
                }
            }
            return new FileLike(limit, hasMore, like);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;

    }

    public static LikeEntity parseLike(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonStr);
        try {
            String objId = jo.getString(Consts.OBJ_ID);
            String objType = jo.getString(Consts.OBJ_TYPE);
            String userId = jo.getString(Consts.USER_ID);
            String userName = jo.getString(Consts.USER_NAME);
            String createDate = jo.getString(Consts.CREATE_DATE);

            LikeEntity likeEntity = new LikeEntity();
            likeEntity.setObjId(objId);
            likeEntity.setObjType(objType);
            likeEntity.setUserId(userId);
            likeEntity.setUserName(userName);
            likeEntity.setCreateDate(createDate);
            if (likeEntity.isValid()) {
                return likeEntity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static boolean parseHasPasswd(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return false;
        }
        JSONObject jo = JSONObject.parseObject(jsonStr);
        try {
            boolean needPassword = jo.getBoolean(Consts.NEED_PASSWORD);
            return needPassword;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * <pre>
     * {
     * 	    "limit" : 3,
     * 	    "has_more" : false,
     * 	     "comments" :
     *     [
     *         {Comment},
     *         {Comment},
     *         {Comment}
     *     ]
     *
     *    }
     * </pre>
     *
     * @param jsonStr
     * @return
     * @time Mar 21, 2014
     */
    public final static FileComments parseFileComments(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject joOuter = JSONObject.parseObject(jsonStr);
        try {
            int limit = joOuter.getInteger(Consts.LIMIT);
            boolean hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            JSONArray ja = joOuter.getJSONArray(Consts.COMMENTS);
            LinkedList<CommentEntity> comments = null;

            Iterator<Object> lit = ja.iterator();
            if (lit != null && ja.size() > 0) {
                comments = new LinkedList<CommentEntity>();
                while (lit.hasNext()) {
                    JSONObject jo = (JSONObject) lit.next();
                    CommentEntity entity = parseComment(jo.toString());
                    if (entity != null) {
                        comments.add(entity);
                    }
                }
            }
            return new FileComments(limit, hasMore, comments);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public final static CommentEntity parseComment(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonStr);
        try {
            String id = jo.getString(Consts.ID);
            String objId = jo.getString(Consts.OBJ_ID);
            String objType = jo.getString(Consts.OBJ_TYPE);
            String userId = jo.getString(Consts.USER_ID);
            String userName = jo.getString(Consts.USER_NAME);
            String msg = jo.getString(Consts.MSG);
            String createDate = jo.getString(Consts.CREATE_DATE);
            String replyWho = null;
            if (jo.containsKey(Consts.REPLY_TO_USER)) {
                replyWho = jo.getString(Consts.REPLY_TO_USER);
            }
            String replyUserName = null;
            if (jo.containsKey(Consts.REPLY_TO_USER_NAME)) {
                replyUserName = jo.getString(Consts.REPLY_TO_USER_NAME);
            }

            CommentEntity commentEntity = new CommentEntity();
            commentEntity.setId(id);
            commentEntity.setObjId(objId);
            commentEntity.setObjType(objType);
            commentEntity.setUserId(userId);
            commentEntity.setUserName(userName);
            commentEntity.setMsg(msg);
            commentEntity.setCreateDate(createDate);
            commentEntity.setReplyToUser(replyWho);
            commentEntity.setReplyToUserName(replyUserName);
            if (App.DEBUG) {
                LogUtil.d(TAG, "commentEntity is -> \n" + commentEntity);
            }
            if (commentEntity.isValid()) {
                return commentEntity;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    /**
     * <pre>
     * {
     * 	    "limit" : 3,
     * 	    "has_more" : false,
     * 	    "members" :
     * 	   [
     *         {Member},
     *         {Member},
     *         {Member}
     *     ]
     *    }
     * </pre>
     *
     * @param jsonstr
     * @return
     * @time Mar 13, 2014
     */

    public final static AlbumMembers parseAlbumMembers(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject joOuter = JSONObject.parseObject(jsonstr);
        try {
            int limit = joOuter.getInteger(Consts.LIMIT);
            boolean hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            JSONArray ja = joOuter.getJSONArray(Consts.MEMBERS);
            LinkedList<MemberEntity> members = null;

            Iterator<Object> lit = ja.iterator();
            if (lit != null && ja.size() > 0) {
                members = new LinkedList<MemberEntity>();
                while (lit.hasNext()) {
                    JSONObject jo = (JSONObject) lit.next();
                    MemberEntity entity = parseMember(jo.toString());
                    if (entity != null) {
                        members.add(entity);
                    }
                }
            }
            return new AlbumMembers(limit, hasMore, members);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     * 	    "limit" : 3,
     * 	    "has_more" : false,
     * 	    "files" :
     * 	    [
     *            {{@link FileEntity}},
     *            {{@link FileEntity}},
     *            {{@link FileEntity}}
     * 	    ]
     *    }
     * </pre>
     *
     * @param jsonstr
     * @return
     * @time Feb 28, 2014
     */
    public final static AlbumSamples parseAlbumSamples(String jsonstr, boolean onlyPareseHasMore) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }

        JSONObject joOuter = JSONObject.parseObject(jsonstr);
        try {
            boolean hasMore;
            int limit = 0;
            LinkedList<FileEntity> files = null;
            hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            limit = joOuter.getInteger(Consts.LIMIT);
            JSONArray ja = joOuter.getJSONArray(Consts.FILES);
            Iterator<Object> list = ja.iterator();
            int size = ja.size();
            if (!onlyPareseHasMore) {
                if (list != null && ja.size() > 0) {
                    files = new LinkedList<FileEntity>();
                    while (list.hasNext()) {
                        JSONObject jo = (JSONObject) list.next();
                        FileEntity entity = parseFile(jo.toString());
                        if (entity != null) {
                            files.add(entity);
                        }
                    }
                }
            }

            return new AlbumSamples(limit, hasMore, files, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static SimpleAlbumItems parseSimpleAlbumItems(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject joOuter = JSONObject.parseObject(jsonstr);
        try {
            int limit = joOuter.getInteger(Consts.LIMIT);
            boolean hasMore = joOuter.getBoolean(Consts.HAS_MORE);
            JSONArray ja = joOuter.getJSONArray(Consts.FILES);
            int size = ja.size();
            return new SimpleAlbumItems(limit, hasMore, size);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static AlbumAct parseAlbumAct(String jsonStr) {
        if (!JSONUtil.isJSON(jsonStr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonStr);
        try {
            String albumId = jo.getString(Consts.ALBUM_ID);
            String userId = jo.getString(Consts.USER_ID);
            int pictureNum = jo.getInteger(Consts.PICTURES);
            int videoNum = jo.getInteger(Consts.VIDEO);
            int commentNum = jo.getInteger(Consts.COMMENTS);
            int likeNum = jo.getInteger(Consts.LIKES);
            AlbumAct albumActivity = new AlbumAct();
            albumActivity.setAlbumId(albumId);
            albumActivity.setUserId(userId);
            albumActivity.setPictureNum(pictureNum);
            albumActivity.setVideoNum(videoNum);
            albumActivity.setCommentNum(commentNum);
            albumActivity.setLikeNum(likeNum);
            return albumActivity;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     *     "id" : "6041ba74-7dd1-11e3-8525-effe403f413e",
     *     "name": "xp",
     *     "email" : "xp@test.com"
     *     "email_verified" : false,
     *     "mobile": "",
     *     "mobile_verified": false,
     *     "create_date" : "2013-12-25T03:17:25Z",
     *     "mod_date" : "2013-12-25T03:17:25Z",
     *     "level" : "basic",
     *     "status" : "normal",
     *     "gender" : "",
     *     "lang": "",
     *     "quota": {
     *         "max_num_albums" : 15
     *      }
     *  }
     *
     *
     * </pre>
     *
     * @param jsonstr
     * @return
     */

    public final static UserInfo parseUserInfo(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        JSONObject jo = JSONObject.parseObject(jsonstr);
        try {
            String id = jo.getString(Consts.ID);
            String name = jo.getString(Consts.NAME);
            if (!jo.containsKey(Consts.EMAIL)) {
                return new UserInfo(id, name, null, false, null, false, null, null, null, null, null, null, null);
            }
            String email = jo.getString(Consts.EMAIL);
            boolean emailVerified = jo.getBoolean(Consts.EMAIL_VERIFIED);
            String mobile = jo.getString(Consts.MOBILE);
            boolean mobileVerified = jo.getBoolean(Consts.MOBILE_VERIFIED);
            String cdate = jo.getString(Consts.CREATE_DATE);
            String mdata = jo.getString(Consts.MOD_DATE);
            String level = jo.getString(Consts.LEVEL);
            String status = jo.getString(Consts.STATUS);
            String gender = jo.getString(Consts.GENDER);
            String lang = jo.getString(Consts.LANG);
            JSONObject quato = jo.getJSONObject(Consts.QUATA);
            UserInfo info = new UserInfo(id, name, email, emailVerified, mobile, mobileVerified, cdate, mdata, level, status, gender, lang, quato == null ? "" : quato.toString());
            return info;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     *     "access_token": String,
     *     "token_type": String,
     *     "expires_in": Number,
     *     "scope": [String String ...],
     *     "state": String,
     *      "status": String,
     * }
     * </pre>
     *
     * @param jsonStr
     * @return
     * @time Mar 1, 2014
     */
    public final static Token parseToken(String jsonStr) {
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        Token token = null;
        try {
            JSONObject jo = JSONObject.parseObject(jsonStr);
            String accessToken = jo.getString(Consts.ACCESS_TOKEN);
            String tokenType = jo.getString(Consts.TOKEN_TYPE);
            int expiresIn = jo.getInteger(Consts.EXPIRES_IN);
            String scope = jo.getString(Consts.SCOPE);
            String state = jo.getString(Consts.STATE);
            String status = jo.getString(Consts.STATUS);
            token = new Token(accessToken, tokenType, expiresIn, scope, state, status);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return token;
    }

    public final static String parseToken(Token token) {
        return null;
    }

    /**
     * <pre>
     * {
     *     "event_id" : String,
     *     "created_at" : Timestamp,
     *     "user_id" : String,
     *     "user_name" : String,
     *     "src_device" : String,
     *     "op_type" : String,
     *     "object" : Object
     * }
     * </pre>
     * <p/>
     * <pre>
     * 	album_created	=> {Album}
     * 	album_deleted	=> {Album}
     * 	album_updated	=> {Album}
     * 	album_shared	=> {Album}
     * 	file_created	=> {File}
     * 	file_deleted	=> {File}
     * 	file_updated	=> {File}
     *  file_shared		=> {File}
     *  file_downloaded	=> {File}
     * 	comment_created	=> {comment}
     * 	comment_deleted	=> {comment}
     * 	like_created	=> {like}
     * 	like_deleted	=> {like}
     * 	member_joined	=> {member}
     * 	member_left	=> {member}
     * 	member_updated	=> {member}
     * 	user_updated
     * 	avatar_updated
     * </pre>
     *
     * @param jsonStr
     * @return
     * @time Mar 28, 2014
     */
    public final static Event parseEvent(String jsonStr) {
        if (Utils.isEmpty(jsonStr)) {
            return null;
        }
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(jsonStr);
            Event event = new Event();
            if (jo.containsKey(Consts.EVENT_ID)) {
                event.setEventId(jo.getString(Consts.EVENT_ID));
            }

            if (jo.containsKey(Consts.CREATED_AT)) {
                event.setCreatedDate(jo.getString(Consts.CREATED_AT));
            }

            if (jo.containsKey(Consts.USER_ID)) {
                event.setUserId(jo.getString(Consts.USER_ID));
            }

            if (jo.containsKey(Consts.USER_NAME)) {
                event.setUserName(jo.getString(Consts.USER_NAME));
            }

            if (jo.containsKey(Consts.SRC_DEVICE)) {
                event.setSrcDevice(jo.getString(Consts.SRC_DEVICE));
            }

            if (!jo.containsKey(Consts.OP_TYPE)) {
                return event = null;
            }

            String optionType = jo.getString(Consts.OP_TYPE);
            event.setOpType(optionType);

            String objectStr = null;
            if (jo.containsKey(Consts.OBJECT)) {
                objectStr = jo.getString(Consts.OBJECT);
            }

            Object object = null;
            if (Consts.USER_UPDATED.equals(optionType)// user_updated
                    // || Consts.SYSTEM_USER_DISABLED.equals(optionType)//
                    // system_user_disabled
                    || Consts.AVATAR_UPDATED.equals(optionType)// avatar_updated
                    ) {
                return event;
            } else {
                object = parserByOpType(optionType, objectStr);
            }
            if (object == null) {
                return event = null;
            }
            event.setObject(object);
            event.setObjectStr(objectStr);
            return event;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * <pre>
     * {
     *     "access_token" : String,
     *     "type" : String, //file or album
     *     "object": object //{file} or {Album}
     * }
     * </pre>
     *
     * @param content
     * @return
     * @time Jun 4, 2014
     */
    public final static TempToken parseTempToken(String content) {
        if (Utils.existsEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            String accessToken = jo.getString(Consts.ACCESS_TOKEN);
            String type = jo.getString(Consts.TYPE);
            String objStr = jo.getString(Consts.OBJECT);
            Object object = null;
            if (type.equals(Consts.ALBUM)) {
                object = parseAlbum(objStr);
            } else if (type.equals(Consts.FILE)) {
                object = parseFile(objStr);
            }
            return new TempToken(accessToken, objStr, type, object);
        } catch (Exception e) {
            e.printStackTrace();
            if (App.DEBUG) {
                LogUtil.d(TAG, "parseTempToken()	" + e.getMessage());
            }
        }
        return null;
    }

    public final static Object parserByOpType(String opType, String objectStr) {
        if (Utils.isEmpty(objectStr) || Utils.isEmpty(opType)) {
            return null;
        }
        Object object = null;
        if (Consts.ALBUM_CREATED.equals(opType)// album_created
                || Consts.ALBUM_DELETED.equals(opType)// album_deleted
                || Consts.SYSTEM_ALBUM_DELETED.equals(opType)// system_album_delete
                || Consts.ALBUM_UPDATED.equals(opType)// album_shared
                || Consts.ALBUM_SHARED.equals(opType)// album_updated
                ) {
            object = parseAlbum(objectStr);
        } else if (Consts.FILE_CREATED.equals(opType)// file_created
                || Consts.FILE_DELETED.equals(opType)// file_deleted
                || Consts.SYSTEM_FILE_DELETED.equals(opType)// system_file_deleted
                || Consts.FILE_SHARED.equals(opType)// file_shared
                || Consts.FILE_DOWNLOADED.equals(opType)// file_downloaded
                || Consts.FILE_UPDATED.equals(opType)// file_updated
                ) {
            object = parseFile(objectStr);
        } else if (Consts.COMMENT_CREATED.equals(opType)// comment_created
                || Consts.COMMENT_DELETED.equals(opType)// comment_deleted
                ) {
            object = parseComment(objectStr);
        } else if (Consts.LIKE_CREATED.equals(opType)// LIKE_CREATED
                || Consts.LIKE_DELETED.equals(opType)// comment_deleted
                ) {
            object = parseLike(objectStr);
        } else if (Consts.MEMBER_JOINED.equals(opType)// member_joined
                || Consts.MEMBER_LEFT.equals(opType)// member_left
                || Consts.MEMBER_UPDATED.equals(opType)// member_updated
                ) {
            object = parseMember(objectStr);
        }
        return object;

    }

    /**
     * <pre>
     * {
     *     "number": 3,
     *     "events" :
     *     [
     *         {Event},
     *         {Event},
     *         {Event}
     *     ]
     * }
     * </pre>
     *
     * @param jsonStr
     * @return
     * @time Mar 28, 2014
     */
    public final static ArrayList<Event> parseEventList(String jsonStr) {
        if (Utils.isEmpty(jsonStr)) {
            return null;
        }
        if (!JSONUtil.isJSONObject(jsonStr)) {
            return null;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(jsonStr);
            int number = jsonObject.getInteger(Consts.NUMBER);
            if (number <= 0) {
                return null;
            }
            ArrayList<Event> events = new ArrayList<Event>();
            JSONArray ja = jsonObject.getJSONArray(Consts.EVENTS);
            for (int i = 0; i < ja.size(); i++) {
                String content = ja.getString(i);
                if (Utils.isEmpty(content)) {
                    continue;
                }
                Event event = parseEvent(content);
                if (event == null) {
                    continue;
                }
                events.add(event);
            }
            return events;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static NearbyAlbum parseNearbyAlbum(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            // int distance = jo.getInteger(Consts.DISTANCE);
            float distance = jo.getFloat(Consts.DISTANCE);
            String albumContent = jo.getString(Consts.ALBUM);
            AlbumEntity album = parseAlbum(albumContent);
            if (album == null) {
                return null;
            }
            return new NearbyAlbum(distance, album);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * {
     * <p/>
     * <pre>
     *     "limit" : Number,
     *     "has_more" : Boolean,
     *     "albums" :
     *     [
     *         {"distance":Number, "album": {Album}},
     *         {"distance":Number, "album": {Album}},
     *         {"distance":Number, "album": {Album}}
     *     ]
     * }
     * </pre>
     *
     * @param content
     * @return
     * @time 2014-6-1
     */
    public final static NearbyAlumList parseNearbyAlbumList(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            int limit = jo.getInteger(Consts.LIMIT);
            boolean hasMore = jo.getBooleanValue(Consts.HAS_MORE);
            JSONArray ja = jo.getJSONArray(Consts.ALBUMS);
            ArrayList<NearbyAlbum> list = new ArrayList<NearbyAlbum>();
            if (ja != null && !ja.isEmpty()) {
                Iterator<Object> lit = ja.iterator();
                while (lit.hasNext()) {
                    JSONObject json = (JSONObject) lit.next();
                    NearbyAlbum entity = parseNearbyAlbum(json.toString());
                    if (entity != null) {
                        list.add(entity);
                    }
                }
            }
            return new NearbyAlumList(limit, hasMore, list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static HotAlumList parseHotAlbumList(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            JSONArray ja = jo.getJSONArray(Consts.ALBUMS);
            ArrayList<AlbumEntity> list = new ArrayList<AlbumEntity>();
            if (ja != null && !ja.isEmpty()) {
                Iterator<Object> lit = ja.iterator();
                while (lit.hasNext()) {
                    JSONObject json = (JSONObject) lit.next();
                    AlbumEntity entity = parseAlbum(json.toString());
                    if (entity != null) {
                        list.add(entity);
                    }
                }
            }
            return new HotAlumList(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static Error parseErrors(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            Error errors = new Error();
            errors.setCode(jo.getString(Consts.CODE));
            errors.setMessage(jo.getString(Consts.MESSAGE));
            // errors.setReq_id(jo.getString(Consts.REQ_ID));
            errors.setStatus(jo.getInteger(Consts.STATUS));
            return errors;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static String parseDWUrl(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            if (App.DEBUG) {
                if (content == null) {
                    LogUtil.v(TAG, "parseDWUrl content==null");
                } else {
                    LogUtil.v(TAG, "parseDWUrl :" + content);
                }
            }
            return jo.getString(Consts.URL);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static TempHttpToken parseTempHttpToken(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            TempHttpToken token = new TempHttpToken();
            JSONObject jo = JSONObject.parseObject(content);
            token.token = jo.getString(Consts.TOKEN);
            token.create_date = jo.getString(Consts.CREATE_DATE);
            token.expires_in = jo.getInteger(Consts.EXPIRES_IN);
            return token;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public final static String parseTempHttpTokenContent(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        try {
            JSONObject jo = JSONObject.parseObject(content);
            return jo.getString(Consts.TOKEN);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;

    }

    public final static Batch parseBatch(String content) {
        try {
            Batch batch = new Batch();
            JSONObject jo = JSONObject.parseObject(content);
            batch.setAlbum_id(jo.getString(Consts.ALBUM_ID));
            batch.setBatch_id(jo.getString(Consts.BATCH_ID));
            batch.setSize(jo.getInteger(Consts.SIZE));
            return batch;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static Batches parseBatches(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        boolean hasMore;
        int limit = 0;
        LinkedList<Batch> files = null;
        JSONObject joOuter = JSONObject.parseObject(content);
        hasMore = joOuter.getBoolean(Consts.HAS_MORE);
        limit = joOuter.getInteger(Consts.LIMIT);
        JSONArray ja = joOuter.getJSONArray(Consts.BATCHES);
        Iterator<Object> list = null;
        if (ja != null) {
            list = ja.iterator();
        }
        int size = ja.size();
        if (list != null && size > 0) {
            files = new LinkedList<Batch>();
            while (list.hasNext()) {
                JSONObject jo = (JSONObject) list.next();
                Batch entity = parseBatch(jo.toString());
                if (entity != null) {
                    files.add(entity);
                }
            }
        }
        return new Batches(limit, hasMore, files);
    }

    public static Bindings parseBingdings(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        boolean hasMore = false;
        int limit = 0;
        ArrayList<Binding> bindings = null;
        JSONObject joOuter = JSONObject.parseObject(content);
        JSONArray ja = joOuter.getJSONArray(Consts.BINDINGS);
        Iterator<Object> list = ja.iterator();
        int size = ja.size();
        if (list != null && size > 0) {
            bindings = new ArrayList<Binding>();
            while (list.hasNext()) {
                JSONObject jo = (JSONObject) list.next();
                Binding entity = parseBinding(jo.toString());
                if (entity != null) {
                    bindings.add(entity);
                }
            }
        }
        return new Bindings(limit, hasMore, bindings);
    }

    public final static Binding parseBinding(String content) {
        try {
            Binding binding = new Binding();
            JSONObject jo = JSONObject.parseObject(content);
            binding.setCreate_date(jo.getString(Consts.CREATE_DATE));
            binding.setProvider(jo.getString(Consts.PROVIDER));
            binding.setUser_name(jo.getString(Consts.USER_NAME));
            return binding;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * TODO can't handle more than 100 countries
     *
     * @param content
     * @return
     */
    public static ArrayList<RegionInfo> parseRegions(String content) {
        if (Utils.isEmpty(content)) {
            return null;
        }
        boolean hasMore = false;
        int limit = 0;
        ArrayList<RegionInfo> bindings = null;
        JSONObject joOuter = JSONObject.parseObject(content);
        JSONArray ja = joOuter.getJSONArray(Consts.REGIONS);
        Iterator<Object> list = ja.iterator();
        int size = ja.size();
        if (list != null && size > 0) {
            bindings = new ArrayList<RegionInfo>();
            while (list.hasNext()) {
                JSONObject jo = (JSONObject) list.next();
                RegionInfo regionInfo = new RegionInfo();
                regionInfo.setRegion(jo.getString(Consts.REGION));
                regionInfo.setCode(jo.getString(Consts.CODE));
                bindings.add(regionInfo);
            }
        }
        return bindings;
    }

    public static Contact parseContact(String jsonstr) {
        if (!JSONUtil.isJSONObject(jsonstr)) {
            return null;
        }
        Contact item = null;
        try {
            item = new Contact();
            JSONObject jo = JSONObject.parseObject(jsonstr);
            String id = jo.getString(Consts.ID);
            String user_id = jo.getString(Consts.USER_ID);
            String contact = jo.getString(Consts.CONTACT);
            String type = jo.getString(Consts.TYPE);
            String friend_name = jo.getString(Consts.FRIEND_NAME);
            String reg_user_id = jo.getString(Consts.REG_USER_ID);
            item.setId(id);
            item.setUser_id(user_id);
            item.setContact(contact);
            item.setType(type);
            item.setFriend_name(friend_name);
            item.setReg_user_id(reg_user_id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return item;
    }

    public static ItemListWrapper<Contact> parseContactList(String content) {
        ItemListWrapper<Contact> contacts = null;
        JSONObject jo = JSONObject.parseObject(content);
        int limit = 0;
        boolean hasMore = false;
        if (jo.containsValue(Consts.LIMIT)) {
            limit = jo.getInteger(Consts.LIMIT);
        }
        if (jo.containsKey(Consts.HAS_MORE)) {
            hasMore = jo.getBooleanValue(Consts.HAS_MORE);
        }
        JSONArray ja = jo.getJSONArray(Consts.CONTACTS);
        LinkedList<Contact> contactList = new LinkedList<>();
        Iterator<Object> list = ja.iterator();
        int size = ja.size();
        if (list != null && size > 0) {
            while (list.hasNext()) {
                JSONObject jsonObject = (JSONObject) list.next();
                Contact item = parseContact(jsonObject.toString());
                if (item != null) {
                    contactList.add(item);
                }
            }
        }
        contacts = new ItemListWrapper<>(limit, hasMore, contactList);
        return contacts;
    }

    public static SensitiveWords parseSensitivieWord(String content) {
        if (!JSONUtil.isJSONObject(content)) {
            return null;
        }
        SensitiveWords words = null;
        try {
            JSONObject jo = JSONObject.parseObject(content);
            String type = jo.getString(Consts.TYPE);
            String word = jo.getString(Consts.WORD);
            String level = jo.getString(Consts.LEVEL);
            words = new SensitiveWords(type, word, level);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return words;
    }

    public static ObjectsWrapper<SensitiveWords> parseSensitiveWords(String content) {
        if (content == null) {
            return null;
        }
        ObjectsWrapper<SensitiveWords> objectsWrapper = null;
        try {
            JSONObject jo = JSONObject.parseObject(content);
            int limit = 0;
            boolean hasMore = false;
            if (jo.containsValue(Consts.LIMIT)) {
                limit = jo.getInteger(Consts.LIMIT);
            }
            if (jo.containsKey(Consts.HAS_MORE)) {
                hasMore = jo.getBooleanValue(Consts.HAS_MORE);
            }
            JSONArray ja = jo.getJSONArray(Consts.WORDS);
            LinkedList<SensitiveWords> objectList = new LinkedList<>();
            Iterator<Object> list = ja.iterator();
            int size = ja.size();
            if (list != null && size > 0) {
                while (list.hasNext()) {
                    JSONObject jsonObject = (JSONObject) list.next();
                    SensitiveWords item = parseSensitivieWord(jsonObject.toString());
                    if (item != null) {
                        objectList.add(item);
                    }
                }
            }
            objectsWrapper = new ObjectsWrapper<>(limit, hasMore, objectList);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return objectsWrapper;
    }



    public static List<SplashAdInfo> getSplashData(String content){
        File f = new File(MediaUtil.getDestSaveDir() + AppData.getSplashId() + ".png");
        List<SplashAdInfo> li = new ArrayList<SplashAdInfo>();
        JSONObject jo = JSONObject.parseObject(content);
        int limit = jo.getIntValue(Consts.LIMIT);
        if(limit == 0){if(f.exists()){f.delete();};}
        boolean has_more = jo.getBoolean("has_more");
        JSONArray ja = jo.getJSONArray(Consts.Cartoons);
        int size = ja.size();
        for (int i = 0; i < size; i++) {
            JSONObject jo01 = ja.getJSONObject(i);
            SplashAdInfo sa = new SplashAdInfo();
            /*
            "id" : String,
            "name" : String,
            "click_num" : Number,
            "create_date" : Timestamp,
            "mod_date" : Timestamp,
            "start_date" : Timestamp,
            "end_date" : Timestamp,
            "url" : String,
            "os" : String,
            "status" : String,
            "background_color" : Number,
            "use_background_color" : Boolean
            */
            sa.setId(jo01.getString(Consts.ID));
            sa.setName(jo01.getString(Consts.NAME));
            sa.setClickNum(jo01.getIntValue(Consts.CLICK_NUM));
            sa.setCreate_date(jo01.getString(Consts.CREATE_DATE));
            sa.setMod_date(jo01.getString(Consts.MOD_DATE));
            sa.setStart_date(jo01.getString(Consts.START_DATE));
            sa.setEnd_date(jo01.getString(Consts.END_DATE));
            sa.setUrl(jo01.getString(Consts.URL));
            sa.setOs(jo01.getString(Consts.OS));
            sa.setStatus(jo01.getString(Consts.STATUS));
            sa.setBackground_color(jo01.getIntValue(Consts.BACKGROUND_COLOR));
            sa.setUseBackgroundColor(jo01.getBoolean(Consts.USE_BACKGROUND_COLOR));
            li.add(sa);
        }
        return li;
    }


}
