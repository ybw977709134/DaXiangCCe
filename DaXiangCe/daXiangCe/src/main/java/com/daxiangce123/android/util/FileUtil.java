package com.daxiangce123.android.util;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.daxiangce123.android.App;
import com.daxiangce123.android.Consts;
import com.daxiangce123.android.data.Folder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class FileUtil {
    private static final String TAG = "FileUtil";
    private static final int IO_BUFFER_SIZE = 16384; // 16 * 1024
    public static final String MIMETYPE_AUDIO = ".m4a.mp3.mid.xmf.ogg.wav.";
    public static final String MIMETYPE_VIDEO = ".3gp.mp4.rmvb.avi.wmv.mov.mkv.asf.flv.mpg.vob.";
    public static final String MIMETYPE_MP4 = ".mp4";
    public static final String MIMETYPE_IMAGE = ".jpg.jpeg.gif.png.bmp.";

    public static File create(String absPath, long modified) {
        File file = create(absPath);
        if (file != null) {
            file.setLastModified(modified);
        }
        return file;
    }

    public static File create(String absPath) {
        File file = new File(absPath);
        if (exists(absPath)) {
            return file;
        }

        if (Utils.isEmpty(absPath)) {
            return null;
        }
        String folderPath = parent(absPath);
        File folder = new File(folderPath);

        if (!exists(folderPath)) {
            folder.mkdirs();
        }

        try {
            file.createNewFile();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static File mkdir(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return null;
        }
        File file = new File(absPath);
        if (exists(absPath)) {
            if (!file.isDirectory()) {
                delete(file);
            } else {
                return file;
            }
        }
        file.mkdirs();
        return file;
    }

    public static boolean move(String oldPath, String newPath) {
        if (Utils.isEmpty(oldPath) || Utils.isEmpty(newPath)) {
            return false;
        }

        if (!exists(oldPath) || exists(newPath)) {
            return false;
        }

        try {
            File file = new File(oldPath);
            file.renameTo(new File(newPath));
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean delete(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return false;
        }

        File file = new File(absPath);
        return delete(file);
    }

    public static boolean delete(File file) {
        if (!exists(file)) {
            return true;
        }

        if (file.isFile()) {
            return file.delete();
        }
        File files[] = file.listFiles();
        if (files == null || files.length < 1) {
            return file.delete();
        }
        boolean result = true;
        for (int index = 0; index < files.length; index++) {
            result |= delete(files[index]);
        }
        result |= file.delete();

        return result;
    }

    public static boolean exists(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return false;
        }
        File file = new File(absPath);
        return exists(file);
    }

    public static boolean exists(File file) {
        if (file == null) {
            return false;
        }
        return file.exists();
    }

    public static String parent(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return null;
        }
        absPath = cleanPath(absPath);
        File file = new File(absPath);
        return parent(file);
    }

    public static String parent(File file) {
        if (file == null) {
            return null;
        } else {
            return file.getParent();
        }
    }

    public static boolean childOf(String childPath, String parentPath) {
        if (Utils.isEmpty(childPath) || Utils.isEmpty(parentPath)) {
            return false;
        }
        childPath = cleanPath(childPath);
        parentPath = cleanPath(parentPath);
        if (childPath.startsWith(parentPath + File.separator)) {
            return true;
        }
        return false;
    }

    public static String cleanPath(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return absPath;
        }
        while (absPath.contains("//")) {
            absPath = absPath.replace("//", "/");
        }
        if (absPath.endsWith("/")) {
            absPath = absPath.substring(0, absPath.length() - 1);
        }
        return absPath;
    }

    public static long size(String absPath) {
        if (absPath == null) {
            return 0;
        }
        File file = new File(absPath);
        return size(file);
    }

    public static long size(File file) {
        if (!exists(file)) {
            return 0;
        }

        long length = 0;
        if (file.isFile()) {
            length = file.length();
            return length;
        }

        File files[] = file.listFiles();
        if (files == null || files.length == 0) {
            return length;
        }

        int size = files.length;
        for (int index = 0; index < size; index++) {
            File child = files[index];
            length += size(child);
        }
        return length;
    }

    public static boolean copy(String srcPath, String dstPath) {
        if (Utils.isEmpty(srcPath) || Utils.isEmpty(dstPath)) {
            return false;
        }

        // check if copy source equals destination
        if (srcPath.equals(dstPath)) {
            return true;
        }

        // check if source file exists or is a directory
        File srcFile = new File(srcPath);
        if (!srcFile.exists()) {
            return false;
        }

        if (srcFile.isDirectory()) {
            return false;
        }

        // make destination file directory if necessary
        String folderPath = parent(dstPath);
        File dstFolder = new File(folderPath);
        if (!dstFolder.exists()) {
            dstFolder.mkdirs();
        }

        File dstFile = new File(dstPath);

        // if file exists, delete it.
        if (dstFile.exists()) {
            dstFile.delete();
            dstFile = new File(dstPath);
        }

        FileInputStream in = null;
        FileOutputStream out = null;

        // get streams
        try {
            in = new FileInputStream(srcFile);
            out = new FileOutputStream(dstFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        try {
            byte[] buffer = new byte[IO_BUFFER_SIZE];

            int len;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                in.close();
                out.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public static String fileName(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return null;
        }

        absPath = cleanPath(absPath);
        File file = new File(absPath);
        return file.getName();
    }

    public static String getExtension(String fileName) {
        if (Utils.isEmpty(fileName)) {
            return "";
        }

        int index = fileName.lastIndexOf('.');
        if (index < 0 || index >= fileName.length()) {
            return "";
        }
        return fileName.substring(index + 1).toLowerCase(Locale.getDefault());
    }

    public static String getMimeType(File file) {
        if (file == null) {
            return "*/*";
        }
        String fileName = file.getName();
        return getMimeType(fileName);
    }

    public static String getMimeType(String name) {
        if (Utils.isEmpty(name)) {
            return "*/*";
        }
        String extension = getExtension(name);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String type = map.getMimeTypeFromExtension(extension);
        if (Utils.isEmpty(type)) {
            if (name.endsWith(".webp")) {
                return "image/webp";
            } else {
                return "*/*";
            }
        } else {
            return type;
        }
    }

    public static String getMimeTypeFromPath(String path) {
        String mimeType = "";

        try {
            ContentResolver cR = App.getAppContext().getContentResolver();
            Uri uri = Uri.fromFile(new File(path));
            mimeType = cR.getType(uri);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (TextUtils.isEmpty(mimeType)) {
            mimeType = getMimeType(new File(path));
        }
        return mimeType;
    }

    public final static int getFileDeepth(String path, int deep) {
        File file = new File(path == null ? "" : path);
        return getFileDeepth(file, 0);
    }

    public final static int getFileDeepth(File file, int deep) {
        if (!file.exists()) return 0;
        else if (file.getParent() == null || file.getParent().equals("") || file.getParent().equals("/"))
            return ++deep;
        else return getFileDeepth(file.getParentFile(), ++deep);
    }

    public final static boolean isHide(String path) {
        File file = new File(path == null ? "" : path);
        if (!file.exists() || file.isHidden()) return true;
        else if (file.getName().startsWith(".")) {
            return true;
        } else if (file.getParent() != null && !file.getParent().equals("") && file.getParent().equals("/"))
            return isHide(file.getParent());
        return false;
    }

    public final static String getFileName(String path) {
        if (Utils.isEmpty(path)) {
            return null;
        }
        int indexSlash = path.lastIndexOf("/");
        if (indexSlash > 0) {
            path = path.substring(indexSlash + 1, path.length());
        }
        return path;
    }

    public final static String removeSuffix(String nameOrPath) {
        if (Utils.isEmpty(nameOrPath)) {
            return null;
        }
        nameOrPath = getFileName(nameOrPath);
        if (Utils.isEmpty(nameOrPath)) {
            return null;
        }
        try {
            int index = nameOrPath.lastIndexOf(".");
            if (index < 0) {
                return nameOrPath;
            }
            return nameOrPath.substring(0, index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public final static String getSuffix(String nameOrPath) {
        nameOrPath = getFileName(nameOrPath);
        if (Utils.isEmpty(nameOrPath)) {
            return null;
        }
        try {
            int index = nameOrPath.lastIndexOf(".");
            if (index < 0) {
                return null;
            }
            nameOrPath = nameOrPath.toLowerCase(Locale.ENGLISH);
            String suffix = nameOrPath.substring(index + 1, nameOrPath.length());
            return suffix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // public final static String getMimeType(String nameOrPath) {
    // try {
    // String suffix = getSuffix(nameOrPath);
    // if (Utils.isEmpty(suffix)) {
    // return null;
    // }
    // String mime2 = "." + suffix + ".";
    // if (MIMETYPE_AUDIO.contains(mime2)) {
    // return "audio/" + suffix;
    // } else if (MIMETYPE_VIDEO.contains(mime2)) {
    // return "video/" + suffix;
    // } else if (MIMETYPE_IMAGE.contains(mime2)) {
    // return "image/" + suffix;
    // } else if (suffix.equals("apk")) {
    // return "application/vnd.android.package-archive";
    // } else if (suffix.equals("pps") // pps
    // || suffix.equals("ppt")// ppt
    // || suffix.equals("pptx")// pptx
    // ) {
    // return "application/vnd.ms-powerpoint";
    // } else if (suffix.equals("vcf")) {
    // return "text/x-vcard";
    // } else if (suffix.equals("xls")// xls
    // || suffix.equals("xlsx")// xlsx
    // ) {
    // return "application/vnd.ms-excel";
    // } else if (suffix.equals("doc")// doc
    // || suffix.equals("docx")// docx
    // ) {
    // return "application/msword";
    // } else if (suffix.equals("pdf")) {
    // return "application/pdf";
    // } else if (suffix.equals("chm")) {
    // return "application/x-chm";
    // } else if (suffix.equals("txt")// txt
    // || suffix.equals("lrc")// lrc
    // ) {
    // return "text/plain";
    // }
    // } catch (Exception e) {
    // e.printStackTrace();
    // }
    // return null;
    // }

    public final static boolean isFile(String absPath) {
        boolean exists = exists(absPath);
        if (!exists) {
            return false;
        }

        File file = new File(absPath);
        return file.isFile();
    }

    public final static boolean isGif(String path) {
        if (!exists(path)) {
            return false;
        }
        String sufix = getSuffix(path);
        if (sufix == null) {
            return false;
        }
        if (sufix.equalsIgnoreCase("gif")) {
            return true;
        }
        return false;
    }

    public final static boolean isImage(String path) {
        String suffix = getSuffix(path);
        if (Utils.isEmpty(suffix)) {
            return false;
        }
        String mime2 = "." + suffix + ".";
        return MIMETYPE_IMAGE.contains(mime2);
    }

    @SuppressLint("DefaultLocale")
    public final static boolean isVideoByMime(String mimeType) {
        if (mimeType == null) {
            return false;
        }
        mimeType = mimeType.toLowerCase();
        return mimeType.startsWith("video/");
    }

    public final static boolean isVideo(String path) {
        String suffix = getSuffix(path);
        if (Utils.isEmpty(suffix)) {
            return false;
        }
        String mime2 = "." + suffix + ".";
        return MIMETYPE_VIDEO.contains(mime2);
    }

    public final static boolean isMp4(String path) {
        return path.endsWith(MIMETYPE_MP4);
    }

    public final static String getParent(String path) {
        if (Utils.isEmpty(path)) {
            return null;
        }
        if (!exists(path)) {
            return null;
        }
        File file = new File(path);
        return file.getParent();
    }

    public static String getFileSHA1(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return null;
        }
        File file = new File(absPath);

        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        long fileSize = file.length();
        String fileHash = null;
        long startTime = System.currentTimeMillis();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
            byte[] buffer = new byte[Consts.IO_BUFFER_SIZE];
            int length = 0;
            while ((length = fis.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            fis.close();
            fileHash = Utils.byte2hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        long deltaTime = endTime - startTime;
        if (App.DEBUG) {
            LogUtil.d(TAG, "getFileSHA1 filePath " + absPath + " fileSize " + fileSize + " deltaTime " + deltaTime);
        }
        if (!Utils.isEmpty(fileHash)) {
            fileHash = fileHash.trim();
        }
        return fileHash;
    }

    public static String getFileMD5(String absPath) {
        if (Utils.isEmpty(absPath)) {
            return null;
        }
        File file = new File(absPath);
        if (!file.exists() || file.isDirectory()) {
            return null;
        }
        long fileSize = file.length();
        String fileHash = null;
        long startTime = System.currentTimeMillis();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        }
        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("MD5");
            byte[] buffer = new byte[Consts.IO_BUFFER_SIZE];
            int length = 0;
            while ((length = fis.read(buffer)) > 0) {
                messageDigest.update(buffer, 0, length);
            }
            fis.close();
            fileHash = Utils.byte2hex(messageDigest.digest());
        } catch (Exception e) {
            e.printStackTrace();
            try {
                fis.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        long endTime = System.currentTimeMillis();
        long deltaTime = endTime - startTime;
        if (App.DEBUG) {
            LogUtil.d(TAG, "getFileMD5 filePath " + absPath + " fileSize " + fileSize + " deltaTime " + deltaTime);
        }
        if (!Utils.isEmpty(fileHash)) {
            fileHash = fileHash.trim();
        }
        return fileHash;
    }

    public final static List<Folder> getImageFolders1(Context context, boolean needVideo) {
        ArrayList<Folder> folderList = new ArrayList<Folder>();
        HashMap<String, Folder> folderMap = new HashMap<String, Folder>();
        try {
            long start = System.currentTimeMillis();
            ContentResolver cr = context.getContentResolver();
            /************************ Images *******************/
            String[] imgProjection = {MediaStore.Video.Media.DATA};//

            Cursor cursor = cr.query(Images.Media.EXTERNAL_CONTENT_URI, imgProjection, Images.Media.SIZE + ">1 ", null, MediaStore.Images.Media.DATE_MODIFIED + " DESC");
            Log.e(TAG, "getImageFolders() query" + (System.currentTimeMillis() - start));
            resolveCursor1(cursor, folderList, folderMap, false);
            if (cursor != null) {
                cursor.close();
            }

            if (needVideo) {
                cursor = cr.query(Video.Media.EXTERNAL_CONTENT_URI, imgProjection, null, null, MediaStore.Video.Media.DATE_MODIFIED + " DESC");
                resolveCursor1(cursor, folderList, folderMap, true);
            }
            if (cursor != null) {
                cursor.close();
            }

            folderMap.clear();
            if (App.DEBUG) {
                Log.e(TAG, "getImageFolders()" + (System.currentTimeMillis() - start));
                LogUtil.d(TAG, "getImageFolders()  folderList.size : " + folderList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderList;
    }

    private final static void resolveCursor1(Cursor cursor, ArrayList<Folder> folderList, HashMap<String, Folder> folderMap, boolean isVideo) {
        if (cursor == null | cursor.getCount() <= 0) {
            LogUtil.d(TAG, "cursor is EMPTY");
        }
        // String DCIM = Environment.getExternalStoragePublicDirectory(
        // Environment.DIRECTORY_DCIM).getPath();
        // int DCIMDeepth = getFileDeepth(new File(DCIM), 0);
        boolean hasNext = cursor.moveToFirst();
        while (hasNext) {
            int i = cursor.getColumnIndex(ImageColumns.DATA);
            if (i != -1) {
                String path = cursor.getString(i);
                hasNext = cursor.moveToNext();
                File f = new File(path);
                if (f.exists() && !isHide(path)) {
                    String parent = f.getParent();
                    // int deepth = getFileDeepth(parent, 0);
                    // if (deepth > DCIMDeepth + 4) {
                    // continue;
                    // }
                    File file = new File(path);
                    if (!file.exists()) {
                        continue;
                    }
                    if (file.length() <= 0) {
                        continue;
                    }
                    if (isVideo) {
                        if (!path.endsWith(".mp4")) {
                            continue;
                        }
                        if (file.length() / 1024 / 1024 > Consts.FILE_UPLOAD_LIMIT) {
                            continue;
                        }
                        // int duration = getMediaDuration(path);
                        // if (App.DEBUG) {
                        // LogUtil.d(TAG, "duration: " + duration + " path: "
                        // + path);
                        // }
                        // if (duration <= 5 * 1000 || duration > 60 * 1000) {
                        // continue;
                        // }
                    }
                    Folder folder = folderMap.get(parent);
                    if (folder == null) {
                        folder = new Folder(parent);
                        folderMap.put(parent, folder);
                        folderList.add(folder);
                    }
                    folder.add(path);
                }
            }
        }
    }

    public final static List<Folder> getImageFolders2(Context context, boolean needVideo) {
        ArrayList<Folder> folderList = new ArrayList<Folder>();
        try {
            long start = System.currentTimeMillis();
            ContentResolver cr = context.getContentResolver();
            String[] imgProjection = {"_data"};// MediaStore.Images.Media.DATA,
            Uri uri = MediaStore.Files.getContentUri("external");
            Log.e("tag", "url=" + uri.getPath());
            String selection = "_id in ( select parent from files where media_type=1 or media_type=3 group by parent)";
            Cursor cursor = cr.query(uri, imgProjection, selection, null, null);
            resolveCursor2(cursor, folderList, needVideo);
            if (cursor != null) {
                cursor.close();
            }
            if (App.DEBUG) {
                Log.e(TAG, "getImageFolders()" + (System.currentTimeMillis() - start));
                LogUtil.d(TAG, "getImageFolders()  folderList.size : " + folderList.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return folderList;
    }

    private final static void resolveCursor2(Cursor cursor, ArrayList<Folder> folderList, final boolean isVideo) {
        if (cursor == null | cursor.getCount() <= 0) {
            LogUtil.d(TAG, "cursor is EMPTY");
            return;
        }
        cursor.moveToFirst();
        do {
            String folderPathString = cursor.getString(0);
            Folder folder = new Folder(folderPathString);
            File dirFile = new File(folderPathString);

            String[] files = dirFile.list(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String filename) {
                    if (isImage(filename)) return true;
                    // if (isVideo && isMp4(filename)){
                    // int duration =
                    // getMediaDuration(dir.getPath()+File.separator+filename);
                    // // if (duration <= 5 * 1000 || duration > 60 * 1000) {
                    // // return false;
                    // // }else {
                    // return true;
                    // // }
                    // }
                    return false;
                }
            });
            if (folder.add(files)) {
                folderList.add(folder);
            }
        } while (cursor.moveToNext());
    }

    public final static List<Folder> getImageFolders(Context context, boolean needVideo) {
        return getImageFolders1(context, needVideo);
    }

    public final static String mimeToSuffix(String mimeType) {
        if (Utils.isEmpty(mimeType)) {
            return null;
        }
        int index = mimeType.lastIndexOf(File.separator);
        if (index < 0 || index == mimeType.length()) {
            return null;
        }
        return mimeType.substring(index + 1);
    }

    public final static String rename(String absPath, String name) {
        if (Utils.isEmpty(name)) {
            return absPath;
        }
        if (!exists(absPath)) {
            return absPath;
        }
        File file = new File(absPath);
        String path = file.getParent() + File.separator + name;
        return renameTo(absPath, path);
    }

    public final static String renameTo(String absPath, String newPath) {
        if (exists(newPath)) {
            return absPath;
        }
        if (!exists(absPath)) {
            return absPath;
        }
        File file = new File(absPath);
        File destFile = new File(newPath);
        boolean result = file.renameTo(destFile);
        file = null;
        destFile = null;
        if (result) {
            return newPath;
        } else {
            return absPath;
        }
    }

    public final static int getMediaDuration(String localPath) {
        if (!exists(localPath)) {
            return -1;
        }
        MediaPlayer mMediaPlayer = new MediaPlayer();
        try {
            if (!isVideo(localPath)) {
                return -1;
            }
            mMediaPlayer.setDataSource(localPath);
            mMediaPlayer.prepare();
            return mMediaPlayer.getDuration();
        } catch (Exception e) {

            e.printStackTrace();
        } finally {
            mMediaPlayer.release();
        }
        return -1;
    }

}
