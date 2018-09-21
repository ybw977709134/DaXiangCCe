package com.daxiangce123.android.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

/**
 * @project Pickup
 * @time Jan 24, 2014
 * @author ram
 */
public class Folder implements Comparator<Folder> {

	private final String TAG = "Folder";

	private String path;
	private String childPath;
	private List<String> childList;

	public Folder(String path) {
		this.path = path;
	}

//	public void setThumbPath(String path) {
//		this.childPath = path;
//	}

	public boolean contains(String path) {
		if (Utils.isEmpty(path)) {
			return false;
		}
		if (Utils.isEmpty(childList)) {
			return false;
		}
		return childList.contains(path);
	}
	
	public void add(String path,int position){
		if (Utils.isEmpty(path)) {
			return;
		}
		if (childList == null) {
			childList = new ArrayList<String>();
		}
		if (childPath==null&&Utils.isEmpty(childPath)) {
			childPath = path;
		}
		childList.add(position,path);
	}

	public boolean add(String path) {
		if (Utils.isEmpty(path)) {
			return false;
		}
		if (childList == null) {
			childList = new ArrayList<String>();
		}
		if (childPath==null&&Utils.isEmpty(childPath)) {
			childPath = path;
		}
		return childList.add(path);
	}

	public boolean add(List<String> list) {
		if (Utils.isEmpty(list)) {
			return false;
		}
		if (childList == null) {
			childList = new ArrayList<String>();
		}
		return childList.addAll(list);
	}
	
	public boolean add(String[] list) {
		if (Utils.isEmpty(list)||list.length==0) {
			return false;
		}
		childList = new ArrayList<String>();
		for (String string : list) {
			childList.add(path+File.separator+string);
		}
		childPath = childList.get(0);
	    return childList.size()!=0;
	}

	public String get(int position) {
		if (position >= getCount()) {
			return null;
		}
		return childList.get(position);
	}

	public String getName() {
		if (Utils.isEmpty(path)) {
			return null;
		}
		return FileUtil.getFileName(path);
	}

	public String getPath() {
		return path;
	}

	public String getChild() {
		return childPath;
	}

	public int getCount() {
		if (Utils.isEmpty(childList)) {
			return 0;
		}
		return childList.size();
	}

	public boolean valid() {
		if (Utils.isEmpty(path) || Utils.isEmpty(childPath)) {
			return false;
		}
		try {
			File curFile = new File(path);
			if (curFile == null || curFile.isHidden() || curFile.isFile()) {
				LogUtil.d(TAG,
						"folder path is invalid	" + path + curFile.isHidden()
								+ curFile.isFile());
				return false;
			}
			File childFile = new File(childPath);
			if (childFile == null || childFile.isHidden()
					|| childFile.isDirectory()) {
				LogUtil.d(TAG, "childPath path is invalid	" + childPath);
				return false;
			}
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public int compare(Folder lhs, Folder rhs) {
		try {
			return lhs.path.compareToIgnoreCase(rhs.path);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
