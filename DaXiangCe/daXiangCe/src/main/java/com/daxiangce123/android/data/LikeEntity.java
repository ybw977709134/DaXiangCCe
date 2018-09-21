package com.daxiangce123.android.data;

import com.daxiangce123.android.util.Utils;

//@DatabaseTable(tableName = "like_data")  
public class LikeEntity {
	// @DatabaseField
	private String obj_id = null;
	// @DatabaseField
	private String obj_type = null;
	// @DatabaseField
	private String user_id = null;
	// @DatabaseField
	private String user_name = null;
	// @DatabaseField
	private String create_date = null;

	public LikeEntity() {
	}

	public LikeEntity(String objId, String objType, String userId,
			String userName, String createDate) {
		this.obj_id = objId;
		this.obj_type = objType;
		this.user_id = userId;
		this.user_name = userName;
		this.create_date = createDate;
	}

	public void setObjId(String objId) {
		this.obj_id = objId;
	}

	public String getObjId() {
		return obj_id;
	}

	public void setObjType(String objType) {
		this.obj_type = objType;
	}

	public String getObjType() {
		return obj_type;
	}

	public void setUserId(String userId) {
		this.user_id = userId;
	}

	public String getUserId() {
		return user_id;
	}

	public void setUserName(String userName) {
		this.user_name = userName;
	}

	public String getUserName() {
		return user_name;
	}

	public void setCreateDate(String createDate) {
		this.create_date = createDate;
	}

	public String getCreateDate() {
		return create_date;
	}

	public boolean isValid() {
		if (Utils.isEmpty(create_date) || Utils.isEmpty(user_id)
				|| Utils.isEmpty(user_name)) {
			return false;
		}
		return true;
	}

}
