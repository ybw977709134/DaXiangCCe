package com.daxiangce123.android.data;

import com.daxiangce123.android.util.Utils;

public class ImageSize {
	public static final String TAG = "ImageSize";

	private int mWidth;
	private int mHeight;
	private boolean isThumb;
	private boolean isCircle;
	private boolean hasRotateOverlay;
	private boolean isRound;
	private boolean hasThumbFile = true;
	private ThumbSize thumbSize;

	public ImageSize(ImageSize size) {
		this.mWidth = size.mWidth;
		this.mHeight = size.mHeight;
		this.isThumb = size.isThumb;
		this.isCircle = size.isCircle;
		this.hasRotateOverlay = size.hasRotateOverlay;
	}

	/**
	 * if this is a thumb. fianlly the {@link #mWidth} will be the width of
	 * thumbSize.So is {@link #mHeight}
	 * 
	 * @time Apr 25, 2014
	 * 
	 * @param width
	 * @param height
	 */
	public ImageSize(int width, int height) {
		mWidth = width;
		mHeight = height;
		getThumbSize();
	}

	public void setCircle(boolean isCircle) {
		this.isCircle = isCircle;
	}

	public boolean isCircle() {
		return isCircle;
	}

	public void setRotateOverlay(boolean isOverlay) {
		this.hasRotateOverlay = isOverlay;
	}

	public boolean hasRotateOverlay() {
		return hasRotateOverlay;
	}

	public void setRound(boolean isRound) {
		this.isRound = isRound;
	}

	public boolean isRound() {
		return isRound;
	}

	public void setThumb(boolean isThumb) {
		this.isThumb = isThumb;
		if (!isThumb) {
			return;
		}
		getThumbSize();
	}

	public boolean isThumb() {
		return isThumb;
	}

	public void setWidth(int width) {
		this.mWidth = width;
	}

	public int getWidth() {
		return this.mWidth;
	}

	public void setHeight(int height) {
		this.mHeight = height;
	}

	public int getHeight() {
		return this.mHeight;
	}

	/**
	 * default is true
	 * 
	 * @time 2014-4-27
	 * 
	 * @return
	 */
	public boolean hasThumbFile() {
		return hasThumbFile;
	}

	public void setHasThumbFile(boolean hasThumbFile) {
		this.hasThumbFile = hasThumbFile;
	}

	public ThumbSize getThumbSize() {
		if (!isThumb) {
			return null;
		}
		if (thumbSize == null) {
			thumbSize = new ThumbSize(mWidth, mHeight);
		}
		return thumbSize;
	}

	/**
	 * return "{@link #mWidth}x{@link #mHeight}" or null
	 */
	public String toURI() {
		if (!isThumb) {
			return null;
		}
		String size = getThumbSize().getSizes();
		if (size == null) {
			return null;
		}
		return size;
	}

	public boolean valid() {
		return mWidth >= 0 && mHeight >= 0;
	}

	public String toString() {
		return TAG + " [width] " + mWidth + " [height] " + mHeight + " [isThumb] " + isThumb + " [isCircle] " + isCircle;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ImageSize) {
			ImageSize size = (ImageSize) o;
			boolean result = (mWidth == size.getWidth());
			if (!result) {
				return false;
			}
			if (mHeight != size.getHeight()) {
				return false;
			}
			result = (isThumb == size.isThumb());
			if (!result) {
				return false;
			}
			result = (isCircle == size.isCircle());
			if (!result) {
				return false;
			}
			result = (hasRotateOverlay == size.hasRotateOverlay());
			return result;
		}
		return super.equals(o);
	}

//	/**
//	 * change from "/sdcard/xxx/fileId_200x200" to "fileId"
//	 * 
//	 * @time Mar 19, 2014
//	 * 
//	 * @param path
//	 * @return
//	 */
//	public String removeThumbSize(String path) {
//		if (!isThumb) {
//			return path;
//		}
//		if (Utils.isEmpty(path)) {
//			return null;
//		}
//		String sizeSufix = "_" + toURI();
//		if (!path.endsWith(sizeSufix)) {
//			return path;
//		}
//		int index = path.lastIndexOf(sizeSufix);
//		if (index <= 0) {
//			return path;
//		}
//		return path.substring(0, index);
//	}

	public final static class ThumbSize {
		private int thumbWidth;
		private int thumbHeight;
		private final static int[][] SIZES = { { 200, 200 }, { 460, 460 }, { 660, 660 } };

		public ThumbSize(int width, int height) {
			size(width, height);
		}

		private final boolean size(int width, int height) {
			if (width > 0 && height > 0) {
				int max = ((width + height) + Math.abs(width - height)) / 2;
				int target = Integer.MAX_VALUE;
				for (int[] ints : SIZES) {
					if (Math.abs((max - ints[0])) < target) {
						thumbWidth = ints[0];
						thumbHeight = ints[1];
						target = Math.abs((max - ints[0]));
					}
				}
				return true;
			}
			thumbWidth = 660;
			thumbHeight = 660;
			return false;
		}

		private final String getSizes() {
			return getSizes(new int[] { thumbWidth, thumbHeight });
		}

		public int getWidth() {
			return thumbWidth;
		}

		public int getHeight() {
			return thumbHeight;
		}

		public final static String getSizes(int[] data) {
			if (data == null) {
				return null;
			}
			if (data.length != 2) {
				return null;
			}
			return data[0] + "x" + data[1];
		}

		public final static int[][] Sizes() {
			return SIZES;
		}

		@Override
		public boolean equals(Object o) {
			if (o instanceof ImageSize) {
				ImageSize is = (ImageSize) o;
				if (is.getWidth() != thumbWidth) {
					return false;
				}
				if (is.getHeight() != thumbHeight) {
					return false;
				}
				return true;
			}
			return super.equals(o);
		}

	}
}
