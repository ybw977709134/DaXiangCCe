package com.daxiangce123.android.data;

/**
 * Created by lillian on 15/4/2.
 */

/**
 * <pre>
 * {
 *      "user_id" : String,
 *      "user_name" : String,
 *      "status" : String,
 *      "create_date" : Timestamp,
 *      "expire_date" : Timestamp
 * }
 * </pre>
 */
public class UserSuspendedInfo {

    private String userId;
    private String userName;
    private String status;
    private String createDate;
    private String expireDate;

    public UserSuspendedInfo() {
    }

    public UserSuspendedInfo(String userId, String userName, String createDate, String status,
                             String expireDate) {
        this.userId = userId;
        this.userName = userName;
        this.createDate = createDate;
        this.status = status;
        this.expireDate = expireDate;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }


    public String getCreateDate() {
        return createDate;
    }

    public void setCreateDate(String createDate) {
        this.createDate = createDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }
}
