package com.daxiangce123.android.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.google.gson.Gson;

public class UploadImage implements Serializable {
	private static final String TAG = "UploadingImage";

	private static final long serialVersionUID = 158252854485215L;

	public String fileId;
	public long size;
	public String albumeId;
	public String batchId;
	public String fileEntityContent;
	public int seqNum;
	public String filePath;
	public boolean compress;

	public String getFileId() {
		return fileId;
	}

	public void setFileId(String fileId) {
		this.fileId = fileId;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getFileEntityContent() {
		return fileEntityContent;
	}

	public void setFileEntityContent(FileEntity fileEntity) {
		if (fileEntity == null) return;
		Gson gson = new Gson();
		try {
			this.fileEntityContent = gson.toJson(fileEntity);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (fileEntityContent == null) {
			fileEntityContent = "";
		}
		if (App.DEBUG) {
			LogUtil.v(TAG, "fileEntityContent=" + fileEntityContent);
		}

	}

	public String getAlbumeId() {
		return albumeId;
	}

	public void setAlbumeId(String albumeId) {
		this.albumeId = albumeId;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

	public String getFileName() {
		return FileUtil.getFileName(filePath);
	}

	public String getBatchId() {
		return batchId;
	}

	public void setBatchId(String batchId) {
		this.batchId = batchId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public boolean isCompress() {
		return compress;
	}

	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public UploadImage(String filePath, boolean compress) {
		this.filePath = filePath;
		this.compress = compress;
	}

	public UploadImage() {

	}

	public static boolean containFile(ArrayList<UploadImage> uploadList, String path) {
		for (UploadImage item : uploadList) {
			if (item.filePath.equals(path)) {
				return true;
			}
		}
		return false;
	}

	public static UploadImage getFile(ArrayList<UploadImage> uploadList, String path) {
		for (UploadImage item : uploadList) {
			if (item.filePath.equals(path)) {
				return item;
			}
		}
		return null;
	}

	public static ArrayList<UploadImage> removeFile(ArrayList<UploadImage> uploadList, String path) {
		for (UploadImage item : uploadList) {
			if (item.filePath.equals(path)) {
				uploadList.remove(item);
				return uploadList;
			}
		}
		return uploadList;
	}

	public static ArrayList<UploadImage> setCompress(ArrayList<UploadImage> uploadList, boolean compress) {
		for (UploadImage item : uploadList) {
			item.setCompress(compress);
		}
		return uploadList;
	}

	public static void initUploadImage(List<UploadImage> images) {
		String batchId = UUID.randomUUID().toString();
		int seqNum = 1;
		for (UploadImage uploadImage : images) {
			uploadImage.setBatchId(batchId);
			uploadImage.setSeqNum(seqNum);
			seqNum++;
		}
	}
}
