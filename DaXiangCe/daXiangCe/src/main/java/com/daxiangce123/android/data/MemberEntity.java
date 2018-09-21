package com.daxiangce123.android.data;

import com.daxiangce123.android.util.Utils;

//@DatabaseTable(tableName = "member_data")  
public class MemberEntity {
	// @DatabaseField
	private String userId = null;
	// @DatabaseField
	private String name = null;
	// @DatabaseField
	private String albumId = null;
	private String albumName = null;
	// @DatabaseField
	private String role = null;
	private int permissions;

	public MemberEntity() {

	}

	public MemberEntity(String userId, String name, String albumId, String role, String albumName, int permissions) {
		this.userId = userId;
		this.name = name;
		this.albumId = albumId;
		this.role = role;
		this.albumName = albumName;
		this.permissions = permissions;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserId() {
		return userId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumId() {
		return albumId;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getRole() {
		return role;
	}

	public void setPermissions(int permissions) {
		this.permissions = permissions;
	}

	public int getPermissions() {
		return permissions;
	}

	public boolean isValid() {
		if (Utils.isEmpty(albumId) || Utils.isEmpty(userId) || Utils.isEmpty(role)) {
			return false;
		}
		return true;
	}

}
