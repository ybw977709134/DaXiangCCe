package com.daxiangce123.android.http;

/**
 * @author
 * @project DaXiangCe
 * @time 2014-6-1
 */
public class Error {

    // // 200
    // public final static String SUCCESS = "success";
    //
    // // 201
    // public final static String CREATED = "created";
    //
    // // 202
    // public final static String ACCEPTED = "accepted";
    //
    // // 204
    // public final static String NO_CONTENT = "no_content";
    //
    // // 206
    // public final static String PARTIAL_CONTENT = "partial_content";
    //
    // // 302
    // public final static String REDIRECT = "redirect";
    //
    // // 304
    // public final static String NOT_MODIFIED = "not_modified";
    //
    // // 400
    // public final static String BAD_REQUEST = "bad_request";
    // public final static String INVALID_RESOURCE = "invalid_resource";
    // public final static String BAD_CONFIRMATION = "bad_confirmation";
    //
    // // 401
    // public final static String UNAUTHORIZED = "unauthorized";
    // public final static String INVALID_TOKEN = "invalid_token";
    // public final static String ILLEGAL_APP = "illegal_app";
    // public final static String ACCOUNT_SUSPENDED = "account_suspended";
    // public final static String INVALID_PASSWORD = "invalid_password";
    //
    // // 403
    // public final static String FORBIDDEN = "forbidden";
    // public final static String ALBUM_LOCKED = "album_locked";
    // public final static String USER_BLOCKED = "user_blocked";
    // public final static String NOT_ALLOWED = "not_allowed";
    // public final static String NO_SPACE = "no_space";
    //
    // // 404
    // public final static String NOT_FOUND = "not_found";
    //
    // // 405
    // public final static String METHOD_NOT_ALLOWED = "method_not_allowed";
    //
    // // 409
    // public final static String CONFLICT = "conflict";
    //
    // // 412
    // public final static String PRECONDITION_FAILED = "precondition_failed";
    //
    // // 416
    // public final static String REQUESTED_RANGE_NOT_SATISFIABLE = "requested_range_not_satisfiable";
    //
    // // 500
    // public final static String INTERNAL_SERVER_ERROR = "internal_server_error";
    //
    // // 503
    // public final static String SERVICE_UNAVAILABLE = "service_unavailable";
    // public final static String TOO_MANY_REQUESTS = "too_many_requests";

    private int status;
    private String code;
    private String message;
    private String req_id;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReq_id() {
        return req_id;
    }

    public void setReq_id(String req_id) {
        this.req_id = req_id;
    }

    public ErrorCode toErrorCode() {
        return ErrorCode.to(code);
    }

    public String toString() {
        return "{" + code + " message: " + message + "}";
    }
}
