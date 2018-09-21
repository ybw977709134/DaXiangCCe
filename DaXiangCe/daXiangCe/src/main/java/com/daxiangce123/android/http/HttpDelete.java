package com.daxiangce123.android.http;

import java.net.URI;

import com.yunio.httpclient.client.methods.HttpPost;

public class HttpDelete extends HttpPost {

	public HttpDelete() {
		super();
	}

	public HttpDelete(String uri) {
		super(uri);
	}

	public HttpDelete(URI uri) {
		super(uri);
	}

	public String getMethod() {
		return "DELETE";
	}

}
