package com.daxiangce123.android.data;

/**
 * @author Hiccup
 */
public class RequestEntity {
	
	private int msgCode = 0;
	private int priority = 0;
	private String tag = null;
	private String params = null;
	
	public RequestEntity(int msgCode, int priority, String tag, String params) {
		this.msgCode = msgCode;
		this.priority = priority;
		this.tag = tag;
		this.params = params;
	
	}
	
	public int getMsgCode() {
		return msgCode;
	}
	
	public int getPriority() {
		return priority;
	}
	
	public String getTag() {
		return tag;
	}
	
	public String getParams() {
		return params;
	}
	
	
}
