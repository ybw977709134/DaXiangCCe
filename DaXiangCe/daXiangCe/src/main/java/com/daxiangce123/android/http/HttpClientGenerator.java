package com.daxiangce123.android.http;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import com.yunio.httpclient.conn.ClientConnectionManager;
import com.yunio.httpclient.conn.scheme.PlainSocketFactory;
import com.yunio.httpclient.conn.scheme.Scheme;
import com.yunio.httpclient.conn.scheme.SchemeRegistry;
import com.yunio.httpclient.conn.ssl.SSLSocketFactory;
import com.yunio.httpclient.impl.client.DefaultHttpClient;
import com.yunio.httpclient.impl.conn.tsccm.ThreadSafeClientConnManager;
import com.yunio.httpclient.params.BasicHttpParams;
import com.yunio.httpclient.params.CoreConnectionPNames;
import com.yunio.httpclient.params.HttpParams;

public class HttpClientGenerator {
	// public static OkHttpClient getOKHttpClient(int timeout) {
	// OkHttpClient client = new OkHttpClient();
	// return client;
	// }

	@SuppressWarnings("deprecation")
	public static DefaultHttpClient getHttpClient(int timeout) {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null, null);
			SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
			sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			HttpParams params = new BasicHttpParams();
			params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, timeout);
			params.setParameter(CoreConnectionPNames.SO_TIMEOUT, timeout);

			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			registry.register(new Scheme("https", sf, 443));

			// ConnManagerParams.setMaxTotalConnections(params, 200);
			// ConnPerRouteBean connPerRoute = new ConnPerRouteBean(20);
			// HttpHost localhost = new HttpHost("locahost", 80);
			// connPerRoute.setMaxForRoute(new HttpRoute(localhost), 50);
			// ConnManagerParams.setMaxConnectionsPerRoute(params,
			// connPerRoute);

			ClientConnectionManager ccm = new ThreadSafeClientConnManager(params, registry);

			return new DefaultHttpClient(ccm, params);
		} catch (Exception e) {
			e.printStackTrace();
			return new DefaultHttpClient();
		}
	}

	static class SSLSocketFactoryEx extends SSLSocketFactory {

		SSLContext sslContext = SSLContext.getInstance("TLS");

		public SSLSocketFactoryEx(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}

				public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws java.security.cert.CertificateException {
				}
			};
			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}
}
