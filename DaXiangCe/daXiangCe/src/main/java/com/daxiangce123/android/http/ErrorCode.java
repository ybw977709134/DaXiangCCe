package com.daxiangce123.android.http;


/**
 * @project DaXiangCe
 * @time 2014-10-15
 * @author ram
 */
public enum ErrorCode {
	NONE, //
	UNKNOWN, //
	INVALID_PARAMS, //
	NETWORK_ERROR, //
	TIME_OUT, //

	// 200
	SUCCESS, //

	// 201
	CREATED, //
	// 202
	ACCEPTED, //

	// 204
	NO_CONTENT, //

	// 206,//
	PARTIAL_CONTENT, //

	// 302,//
	REDIRECT, //

	// 304
	NOT_MODIFIED, //

	// 400,//
	BAD_REQUEST, //
	INVALID_RESOURCE, //
	BAD_CONFIRMATION, //

	// 401
	UNAUTHORIZED, //
	INVALID_TOKEN, //
	ILLEGAL_APP, //
	ACCOUNT_SUSPENDED, //
	INVALID_PASSWORD, //

	// 403
	FORBIDDEN, //
	ALBUM_LOCKED, //
	// INVALID_PASSWORD, //
	USER_BLOCKED, //
	NOT_ALLOWED, //
	NO_SPACE, //

	// 404
	NOT_FOUND, //

	// 405
	METHOD_NOT_ALLOWED, //

	// 409
	CONFLICT, //

	// 412
	PRECONDITION_FAILED, //

	// 416
	REQUESTED_RANGE_NOT_SATISFIABLE, //

	// 500
	INTERNAL_SERVER_ERROR, //

	// 503
	SERVICE_UNAVAILABLE, //
	TOO_MANY_REQUESTS, //

	;
	public static ErrorCode to(String errorCode) {
		if (errorCode == null) {
			return ErrorCode.UNKNOWN;
		}
		try {
			errorCode = errorCode.trim();
			errorCode = hackCode(errorCode);
			return valueOf(errorCode.toUpperCase());
		} catch (Exception e) {
		}
		return ErrorCode.UNKNOWN;
	}

	private static String hackCode(String errorCode) {
		if (errorCode == null) {
			return null;
		}
		errorCode = errorCode.trim();
		// server error: "invalid password.", the correct is "invalid_password"
		if (errorCode.equalsIgnoreCase("invalid password.")) {
			return INVALID_PASSWORD.toString();
		}
		return errorCode;
	}

}
