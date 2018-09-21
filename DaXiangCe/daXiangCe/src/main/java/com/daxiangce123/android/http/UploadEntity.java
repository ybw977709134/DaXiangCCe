package com.daxiangce123.android.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.yunio.httpclient.entity.AbstractHttpEntity;

import com.daxiangce123.android.Consts;
import com.daxiangce123.android.util.FileUtil;
import com.daxiangce123.android.util.LogUtil;

public class UploadEntity extends AbstractHttpEntity implements Cloneable {
	public final static String TAG = "UploadEntity";

	protected final File mFile;
	protected ProgressListener mListener;
	protected long mFileSize;
	protected long mOffset;
	protected InputStream mInputStream;
	private static boolean DEBUG = false;
	private String absPath;

	public UploadEntity(String absPath, long offset) throws FileNotFoundException {
		super();
		if (!FileUtil.exists(absPath)) {
			LogUtil.e(TAG, "file not found!");
			throw new FileNotFoundException();
		}
		File file = new File(absPath);
		this.absPath = absPath;
		this.mOffset = offset;
		this.mFile = file;
		this.mFileSize = mFile.length();
		reset();
	}

	public void reset() {
		String contentType = FileUtil.getMimeType(mFile);
		setContentType(contentType);
		try {
			mInputStream = new FileInputStream(mFile);
			mInputStream.skip(mOffset);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void writeTo(OutputStream outstream) throws IOException {
		if (outstream == null) {
			throw new IllegalArgumentException("Output stream may not be null");
		}
		UploadData ud = new UploadData(outstream);
		try {
			byte[] tmp = new byte[Consts.IO_BUFFER_SIZE];
			int length = 0;
			while ((length = mInputStream.read(tmp)) != -1) {
				ud.write(tmp, 0, length);
			}
			ud.flush();
		} finally {
			mInputStream.close();
			ud.close();
		}
	}

	@Override
	public InputStream getContent() {
		return mInputStream;
	}

	@Override
	public long getContentLength() {
		return mFile.length() - mOffset;
	}

	@Override
	public boolean isRepeatable() {
		return true;
	}

	@Override
	public boolean isStreaming() {
		return false;
	}

	public void setListener(ProgressListener listener) {
		this.mListener = listener;
	}

	public ProgressListener getListener() {
		return this.mListener;
	}

	class UploadData extends DataOutputStream {
		public static final String TAG = "UploadData";

		private long mLastTime;
		private long mTransferred;
		private long mStartTime;
		private int mProgress;

		public UploadData(OutputStream out) {
			super(out);
			mTransferred = mOffset;
			mProgress = 0;
			mLastTime = 0;
			mStartTime = System.currentTimeMillis();
		}

		@Override
		public void write(byte[] buffer, int offset, int count) throws IOException {
			super.write(buffer, offset, count);

			mTransferred += count;

			if (mFileSize == 0 || mListener == null) {
				return;
			}

			int curPro = (int) (mTransferred * 100 / mFileSize);
			long curTime = System.currentTimeMillis();
			long deltaTime = curTime - mLastTime;

			if (curPro <= mProgress || (deltaTime < Consts.PROGRESS_DELTA && mTransferred < mFileSize - 10)) {
				return;
			}

			mProgress = curPro;
			mLastTime = curTime;

			long deltaSize = mTransferred - mOffset;
			deltaTime = curTime - mStartTime;
			if (deltaTime <= 0) {
				return;
			}
			mListener.onProgress(mFile.getAbsolutePath(), mProgress, (deltaSize / deltaTime), mTransferred, mFileSize);
			if (DEBUG) {
				LogUtil.d(TAG, "upload speed " + deltaSize);
			}
		}
	}
}
