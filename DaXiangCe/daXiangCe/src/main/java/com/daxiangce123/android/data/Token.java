package com.daxiangce123.android.data;

import com.daxiangce123.android.util.Utils;

/**
 * @project Groubum
 * @time Mar 1, 2014
 * @author ram
 */
public class Token {

	private String accessToken;
	private String tokenType;
	private int expiresIn;
	private String scope;
	private String state;
	private String status;

	public Token() {
	}

	public Token(String accessToken, String tokenType, int expiresIn, String scope, String state, String status) {
		this.accessToken = accessToken;
		this.tokenType = tokenType;
		this.expiresIn = expiresIn;
		this.scope = scope;
		this.state = state;
		this.status = status;
	}

	public boolean isValid() {
		return !(Utils.isEmpty(accessToken));
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public String getTokenType() {
		return tokenType;
	}

	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}

	public int getExpiresIn() {
		return expiresIn;
	}

	public void setExpiresIn(int expiresIn) {
		this.expiresIn = expiresIn;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;

	}

}
