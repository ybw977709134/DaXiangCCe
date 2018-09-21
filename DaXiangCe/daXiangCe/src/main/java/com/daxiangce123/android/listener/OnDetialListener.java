package com.daxiangce123.android.listener;

import java.util.Collection;
import java.util.LinkedList;

import com.daxiangce123.android.data.FileEntity;

/**
 * @project Cliq
 * @time Mar 20, 2014
 * @author ram
 */
public interface OnDetialListener {

	public boolean onFileLongClicked(FileEntity entity);

	public boolean onFileClicked(FileEntity entity);

	public boolean onFileClicked(int position);

//	/**
//	 * @param position
//	 *            of the list
//	 * @param list
//	 * @return
//	 */
//	public boolean onFileOpen(int position, LinkedList<FileEntity> list);

	public boolean onDelete(Collection<FileEntity> entities);

	public boolean onUpload();

	public boolean onDisplayAllSelected(boolean selected);

}
