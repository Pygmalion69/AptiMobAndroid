package de.nitri.aptimob;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import javax.net.ssl.X509TrustManager;
import java.security.cert.X509Certificate;

public class RestTask extends AsyncTask<String, Void, String> {

	public static final String HTTP_RESPONSE = "httpResponse";
	public static final String HTTP_RESPONSE_CODE = "httpResponseCode";

	HttpsURLConnection con;
	int r;

	private Context mContext;
	private String mAction;

	private String mUser;
	private String mPassword;

	public RestTask(Context context, String action, String user, String password) {
		mContext = context;
		mAction = action;
		mUser = user;
		mPassword = password;
	}

	@Override
	protected String doInBackground(String... params) {
		try {

			// Create a trust manager that does not validate certificate chains
			final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				@Override
				public void checkClientTrusted(final X509Certificate[] chain,
						final String authType) {
				}

				@Override
				public void checkServerTrusted(final X509Certificate[] chain,
						final String authType) {
				}

				@Override
				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			} };

			// Install the all-trusting trust manager
			final SSLContext sslContext = SSLContext.getInstance("SSL");
			sslContext.init(null, trustAllCerts,
					new java.security.SecureRandom());
			// Create an ssl socket factory with our all-trusting manager
			final SSLSocketFactory sslSocketFactory = sslContext
					.getSocketFactory();

			URL url = new URL(params[0]);
			String body = params[1];
			String method = params[2];
			if (method == null)
				method = "POST";
			String authString = mUser + ":" + mPassword;
			con = (HttpsURLConnection) url.openConnection();
			con.setSSLSocketFactory(sslSocketFactory);

			if (null != body) {
				byte[] bodyBytes = body.getBytes();
				con.setRequestProperty("Content-Length",
						String.valueOf(bodyBytes.length));
			}
			con.setReadTimeout(30000 /* milliseconds */);
			con.setConnectTimeout(50000 /* milliseconds */);
			con.setRequestMethod(method);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty(
					"Authorization",
					"Basic "
							+ Base64.encodeToString(authString.getBytes(),
									Base64.NO_WRAP));
			con.setRequestProperty("Content-Type",
					"application/x-www-form-urlencoded; charset=utf-8");
			if (null != body) {
				OutputStreamWriter writer = new OutputStreamWriter(
						con.getOutputStream(), "UTF-8");
				writer.write(body);
				writer.flush();
			}

			// Start the query
			con.connect();
			r = con.getResponseCode();
			Log.d("body", body);
			Log.d("ResponseCode", Integer.toString(r));
			// Read results from the query
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					con.getInputStream(), "UTF-8"), 8 * 1024);
			String payload = reader.readLine();
			reader.close();
			if (null != payload)
				Log.d("PAYLOAD", payload);
			return payload;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		} finally {
			if (con != null) {
				con.disconnect();
			}
		}
	}

	@Override
	protected void onPostExecute(String result) {
		Intent intent = new Intent(mAction);
		intent.putExtra(HTTP_RESPONSE, result);
		intent.putExtra(HTTP_RESPONSE_CODE, r);

		// Broadcast the completion
		mContext.sendBroadcast(intent);
	}
}