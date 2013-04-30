package com.example.draw;

import java.io.IOException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import android.annotation.SuppressLint;
import android.os.StrictMode;

/**Utility class that can disable certificate checks for SSL connections.
 * 
 * @author Chris Allen on Mar 21, 2013
 */
public final class DisableSSLCertificateCheckUtil {
	/**
	 * Prevent instantiation of utility class.
	 */
	private DisableSSLCertificateCheckUtil() {

	}

	/**
	 * Trust manager that does not perform nay checks.
	 */
	private static class NullX509TrustManager implements X509TrustManager {
		public void checkClientTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			System.out.println();
		}

		public void checkServerTrusted(X509Certificate[] chain, String authType)
				throws CertificateException {
			System.out.println();
		}

		public X509Certificate[] getAcceptedIssuers() {
			return new X509Certificate[0];
		}

	}

	/**
	 * Host name verifier that does not perform nay checks.
	 */
	private static class NullHostnameVerifier implements HostnameVerifier {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	}

	/**
	 * Disable trust checks for SSL connections.
	 */
	@SuppressLint("NewApi")
	public static void disableChecks() throws NoSuchAlgorithmException,
	KeyManagementException {

		try {
			if(android.os.Build.VERSION.SDK_INT >= 9) {
				StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
				StrictMode.setThreadPolicy(policy);
			}
			
			new URL("https://0.0.0.0/").getContent();
		} catch (IOException e) {
			// This invocation will always fail, but it will register the
			// default SSL provider to the URL class.
		}

		try{
			SSLContext sslc;

			sslc = SSLContext.getInstance("TLS");

			TrustManager[] trustManagerArray = { new NullX509TrustManager() };
			sslc.init(null, trustManagerArray, null);

			HttpsURLConnection.setDefaultSSLSocketFactory(sslc.getSocketFactory());
			HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());

		}catch(Exception e){
			e.printStackTrace();
		}


	}
}