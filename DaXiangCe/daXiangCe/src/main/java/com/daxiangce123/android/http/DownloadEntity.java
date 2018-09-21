package com.daxiangce123.android.http;

import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

public class DownloadEntity extends RandomAccessFile {
	public static final String TAG = "DownloadEntity";

	private boolean mWritable;
	private int mProgress;
	private long mFileSize;
	private long mReceived;
	private long mLastTime;
	private long lastReceived;
	private long mStartTime;
	private long mCurTime;
	private long mOffset;
	private String mFilePath;
	private String tmpPath;
	private ProgressListener mListener;

	public DownloadEntity(String filePath, String tmpPath, long fileSize) throws FileNotFoundException {
		super(tmpPath, Consts.FILE_OPEN_MODE_RW);
		this.tmpPath = tmpPath;
		mStartTime = -1;
		mProgress = -1;
		mFilePath = filePath;
		mFileSize = fileSize;
		mWritable = true;
		mReceived = FileUtil.size(mFilePath);
		mReceived = (mReceived == -1) ? 0 : mReceived; // -1 : file not exist
	}

    public DownloadEntity(String filePath, String tmpPath ) throws FileNotFoundException {
        super(tmpPath, Consts.FILE_OPEN_MODE_RW);
        this.tmpPath = tmpPath;
        mStartTime = -1;
        mProgress = -1;
        mFilePath = filePath;
        mWritable = true;
        mReceived = FileUtil.size(mFilePath);
        mReceived = (mReceived == -1) ? 0 : mReceived; // -1 : file not exist
    }

	public DownloadEntity(File file, String mode) throws FileNotFoundException {
		super(file, mode);
	}

	public void setFileSize(long fileSize) {
		this.mFileSize = fileSize;
	}

	// stop write any data to file.
	public void setWritable(boolean writable) {
		mWritable = writable;
	}

	public boolean getWritable() {
		return mWritable;
	}

	public void setListener(ProgressListener listener) {
		this.mListener = listener;
	}

	public ProgressListener getListener() {
		return this.mListener;
	}

	@Override
	public void seek(long offset) {
		mOffset = offset;
		try {
			super.seek(offset);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void write(byte[] buffer, int offset, int count) throws IOException {
		if (!mWritable) {
			LogUtil.e(TAG, "can't write " + count + "  bytes");
			return;
		}
		super.write(buffer, offset, count);

		if (mStartTime == -1) {
			mStartTime = System.currentTimeMillis();
			mLastTime = mStartTime;
		}

		mReceived += count;
		if (mFileSize <= 0) {
			return;
		}
		int curPro = (int) (mReceived * 100 / mFileSize);
		if (curPro == 100) {
			if (mReceived != mFileSize) {
				curPro = 99;
			}
		}
		mCurTime = System.currentTimeMillis();
		long deltaTime = mCurTime - mLastTime;
		final int deltaProgress = curPro - mProgress;

		if ((deltaTime < Consts.PROGRESS_DELTA || deltaProgress < 1)
				&& curPro <= 99) {
			return;
		}
		mProgress = curPro;
		deltaTime = mCurTime - mLastTime;
		long deltaSize = mReceived - lastReceived;
		long speed = (deltaTime == 0) ? 0 : (deltaSize / deltaTime);
		lastReceived = mReceived;
		mLastTime = mCurTime;
		// if (App.DEBUG) {
		// LogUtil.d(TAG, "path=" + mFilePath + "	download deltaTime="
		// + deltaTime + " speed=" + speed + " downloaded="
		// + mReceived + " deltaProgress=" + deltaProgress);
		// }
		if (mListener == null) {
			return;
		}
		mListener.onProgress(mFilePath, mProgress, speed, mReceived, mFileSize);
	}

	public boolean input(InputStream ips) {
		if (ips == null) {
			return false;
		}
		try {
			int count = 0;
			byte[] buffer = new byte[Consts.IO_BUFFER_SIZE];
			while ((count = ips.read(buffer)) > 0 && getWritable()) {
				write(buffer, 0, count);
			}
			ips.close();
			close();
			FileUtil.delete(mFilePath);
			FileUtil.renameTo(tmpPath, mFilePath);
		} catch (Exception e) {
			e.printStackTrace();
			FileUtil.delete(tmpPath);
			return false;
		}
		return true;
	}

	public void close() {
		try {
			super.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
