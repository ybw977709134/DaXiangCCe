package com.daxiangce123.android.data;

/**
 * <pre>
 * {
 *     "banner_id" : String,
 *     "name" : String,
 *     "album_id" : String,
 *     "created_date" : Timestamp,
 *     "mod_date" : Timestamp,
 *     "start_date" : Timestamp,
 *     "end_date" : Timestamp,
 *     "seq_num" : Number
 * }
 * </pre>
 * 
 * @project DaXiangCe
 * @time Jun 18, 2014
 * @author ram
 */
public class Banner {

	private String bannerId;
	private String name;
	// private String albumId;
	private String createDate;
	private String modDate;
	private String startDate;
	private String endDate;
	private int seqNum;
	private AlbumEntity album;

	public Banner() {
	}

	public Banner(String bannerId, String name, String createDate,
			String modDate, String startDate, String endDate, int seqNum,
			AlbumEntity album) {
		this.bannerId = bannerId;
		this.name = name;
		// this.albumId = albumId;
		this.createDate = createDate;
		this.modDate = modDate;
		this.startDate = startDate;
		this.endDate = endDate;
		this.seqNum = seqNum;
		this.album = album;
	}

	public String getBannerId() {
		return bannerId;
	}

	public void setBannerId(String bannerId) {
		this.bannerId = bannerId;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	// public String getAlbumId() {
	// return albumId;
	// }
	public AlbumEntity getAlbum() {
		return album;
	}

	public void setAlbum(AlbumEntity album) {
		this.album = album;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public String getModDate() {
		return modDate;
	}

	public void setModDate(String modDate) {
		this.modDate = modDate;
	}

	public String getStartDate() {
		return startDate;
	}

	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	public String getEndDate() {
		return endDate;
	}

	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public int getSeqNum() {
		return seqNum;
	}

	public void setSeqNum(int seqNum) {
		this.seqNum = seqNum;
	}

}
