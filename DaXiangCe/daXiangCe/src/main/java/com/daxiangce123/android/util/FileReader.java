package com.daxiangce123.android.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FileReader {
	
	private List<String> list = null;
	public static FileReader instance = null;
	
	
	private FileReader() {
		list = new ArrayList<String>();
	}
	
	public static FileReader getInstance() {
		if (instance == null) {
			instance = new FileReader();
		}
		return instance;
	}
	
	
	public List<String> getFilePathList(String path) {
		File file = new File(path);
		if (file.isHidden()) {
			return null;
		}
		
		File[] filelist = file.listFiles();
		if(filelist == null) {
			return null;
		} 
		for (File f : filelist) {
			if (f.isDirectory() && !file.isHidden()) {
				getFilePathList(f.getPath());
			} else {
				String p = f.getPath();
				if (p.toLowerCase(Locale.ENGLISH).endsWith(".mp4") || p.toLowerCase(Locale.ENGLISH).endsWith(".jpg")
						|| p.toLowerCase(Locale.ENGLISH).endsWith(".png")) {
					list.add(p);
				}
			}		
		
		}
		if (list.size() > 0) {
			return list;
		}
		return null;
	}
	
	public void close() {
		list.clear();
		list = null;
	}
}
