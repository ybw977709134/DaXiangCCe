package com.daxiangce123.android.data;

/**
 * @project DaXiangCe
 * @time Jun 4, 2014
 * @author ram
 */
public class TempToken {

	private String token;
	private String objStr;
	private String objType;
	private Object object;
	public static final TempToken EMPTY = new TempToken();

	public TempToken() {
	}

	public TempToken(String token, String objStr, String objType, Object object) {
		this.token = token;
		this.objStr = objStr;
		this.objType = objType;
		this.object = object;
	}

	public String getObjStr() {
		return objStr;
	}

	public void setObjStr(String objStr) {
		this.objStr = objStr;
	}

	public String getObjType() {
		return objType;
	}

	public void setObjType(String objType) {
		this.objType = objType;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public Object getObject() {
		return object;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("[Token		" + token + "]\n");
		sb.append("[objStr		" + objStr + "]\n");
		sb.append("[objType		" + objType + "]");
		sb.append("[object]>>>>>>>>>>>>>>>>>>>>>>>>>>>\n" + object);
		return sb.toString();
	}

}
