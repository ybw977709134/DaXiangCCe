package com.daxiangce123.android.util;

import java.util.Locale;

/**
 * @project DaXiangCe
 * @time Jun 25, 2014
 * @author ram
 */
public class MimeTypeUtil {

	public enum Mime {
		VID, IMG, GIF;
	}

	public static final Mime getMime(String mimeType) {
		if (mimeType == null) {
			return null;
		}
		mimeType = mimeType.trim().toLowerCase(Locale.ENGLISH);
		if (mimeType.startsWith("video/")) {
			return Mime.VID;
		} else if (mimeType.startsWith("image/")) {
			if (mimeType.endsWith("gif")) {
				return Mime.GIF;
			}
			return Mime.IMG;
		}
		return null;
	}

}
