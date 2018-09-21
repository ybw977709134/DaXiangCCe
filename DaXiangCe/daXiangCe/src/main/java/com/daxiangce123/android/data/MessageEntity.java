package com.daxiangce123.android.data;


//@DatabaseTable(tableName = "message_data")  
public class MessageEntity {
//	@DatabaseField
	private int msgCode = 0;
//	@DatabaseField
	private int priority = 0;
//	@DatabaseField
	private String tag = null;
	
	public MessageEntity(int msgCode, int priority, String tag) {
		this.msgCode = msgCode;
		this.priority = priority;
		this.tag = tag;
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
	
}
