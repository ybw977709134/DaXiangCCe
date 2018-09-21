package com.daxiangce123.android.data;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;

import com.daxiangce123.android.App;
import com.daxiangce123.android.util.LogUtil;
import com.daxiangce123.android.util.Utils;

public class AlbumSamples implements Parcelable {
	private final static String TAG = "AlbumItems";
	private int limit;
	private boolean hasMore;
	private LinkedList<FileEntity> files;
	private int size;
	private boolean DEBUG = true;

	public AlbumSamples() {
		if (DEBUG) {
			DEBUG = App.DEBUG;
		}
	}

	public AlbumSamples(Parcel parcel) {
		files = new LinkedList<FileEntity>();
		parcel.readList(files, FileEntity.class.getClassLoader());
		limit = parcel.readInt();
		boolean[] bools = new boolean[1];
		parcel.readBooleanArray(bools);
		if (bools != null && bools.length > 1) {
			hasMore = bools[0];
		}
		size = parcel.readInt();
	}

	public AlbumSamples(int limit, boolean hasMore, LinkedList<FileEntity> files, int size) {
		this.limit = limit;
		this.hasMore = hasMore;
		this.files = files;
		this.size = size;
	}

	public boolean add(AlbumSamples items) {
		if (items == null) {
			return false;
		}
		this.limit = items.getLimit();
		this.hasMore = items.hasMore;
		this.size = items.getSize();
		return add(items.getFiles());
	}

	public boolean add(List<FileEntity> entities) {
		// if (DEBUG) {
		// LogUtil.d(
		// TAG,
		// "----------------------------------------------------------add(List<FileEntity>)"
		// + new Date()
		// + "----------------------------------------------------------");
		// }
		if (Utils.isEmpty(entities)) {
			if (DEBUG) {
				LogUtil.d(TAG, "entities is empty");
			}
			return false;
		}
		// if (DEBUG) {
		// LogUtil.d(TAG, "entities size=" + Utils.sizeOf(entities)
		// + " files=" + (files != null) + "	" + "" + new Date());
		// }
		if (files == null) {
			files = new LinkedList<FileEntity>();
			return files.addAll(entities);
		} else {
			if (DEBUG) {
				LogUtil.d(TAG, "extract entities");
			}
			boolean result = true;
			for (FileEntity entity : entities) {
				if (entity == null) {
					continue;
				}
				// if (DEBUG) {
				// LogUtil.d(TAG, "extract fileId	" + entity.getId());
				// }
				add(entity);
			}
			return result;
		}
	}

	public boolean add(FileEntity file) {
		if (file == null) {
			// if (DEBUG) {
			// LogUtil.d(TAG, "file is null");
			// }
			return false;
		}
		// if (DEBUG) {
		// LogUtil.d(TAG, "add(FileEntity)	fileid " + file.getId());
		// }
		try {
			if (files == null) {
				files = new LinkedList<FileEntity>();
			} else {
				for (FileEntity entity : files) {
					if (entity == null) {
						continue;
					}
					if (file.getId().equals(entity.getId())) {
						entity.setComments(file.getComments());
						entity.setLikes(file.getLikes());
						// if (DEBUG) {
						// LogUtil.d(TAG,
						// "has same!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! ");
						// }
						file = null;
						return false;
					}
				}
			}
			return files.add(file);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getLimit() {
		return limit;
	}

	public void setHasMore(boolean hasMore) {
		this.hasMore = hasMore;
	}

	public boolean hasMore() {
		return hasMore;
	}

	public void setAlbums(LinkedList<FileEntity> files) {
		if (this.files != null) {
			this.files.clear();
		}
		this.files = files;
	}

	public LinkedList<FileEntity> getFiles() {
		return files;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public void clear() {
		limit = 0;
		if (files == null) {
			return;
		}
		size = 0;
		files.clear();
	}

	public int size() {
		if (files == null) {
			return 0;
		}
		return files.size();
	}

	public void printFileIds() {
		if (!DEBUG) {
			return;
		}
		if (Utils.isEmpty(files)) {
			return;
		}
		StringBuilder builder = new StringBuilder();
		builder.append(">>>>>>>>>>>>>>>>>>>>> printFiles <<<<<<<<<<<<<<<<<<<<<\n");
		for (FileEntity file : files) {
			if (file == null) {
				continue;
			}
			builder.append("" + file.getId() + "	" + file.getTitle() + "\n");
		}
		builder.append(">>>>>>>>>>>>>>>>>>>>> size=" + files.size() + " hasMore=" + hasMore + " limit=" + limit + new Date() + " <<<<<<<<<<<<<<<<<<<<<");
		LogUtil.d(TAG, "" + builder);
	}

	// ////////////////////////////////////
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeList(files);
		dest.writeInt(limit);
		boolean[] bools = { hasMore };
		dest.writeBooleanArray(bools);
		dest.writeInt(size);
	}

	public static final Parcelable.Creator<AlbumSamples> CREATOR = new Parcelable.Creator<AlbumSamples>() {
		public AlbumSamples createFromParcel(Parcel parcel) {
			return new AlbumSamples(parcel);
		}

		public AlbumSamples[] newArray(int size) {
			return new AlbumSamples[size];
		}
	};
}
