package de.nitri.aptimob;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import org.jasypt.util.text.BasicTextEncryptor;

import java.io.OutputStreamWriter;
import java.net.URL;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Activity which displays a login screen to the user, offering registration as
 * well.
 */
public class AptiMob extends Activity {
	/**
	 * A dummy authentication store containing known user names and passwords.
	 * TODO: remove after connecting to a real authentication system.
	 */
	private static final String[] DUMMY_CREDENTIALS = new String[] {
			"foo@example.com:hello", "bar@example.com:world" };

	/**
	 * The default email to populate the email field with.
	 */
	public static final String EXTRA_EMAIL = "com.example.android.authenticatordemo.extra.EMAIL";

	/**
	 * Keep track of the login task to ensure we can cancel it if requested.
	 */
	private UserLoginTask mAuthTask = null;

	// Values for email and password at the time of the login attempt.
	private String mEmail;
	private String mPassword;

	// UI references.
	private EditText mEmailView;
	private EditText mPasswordView;
	private View mLoginFormView;
	private View mLoginStatusView;
	private TextView mLoginStatusMessageView;


	private SharedPreferences pref;

	protected static final String PREF_USER_ID = "user_id";
	protected static final String PREF_USERNAME = "username";
	protected static final String PREF_PASSWORD = "password";
	public static final String PREF_REST_URL = "rest_url";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			System.setProperty("http.keepAlive", "false");
		}

		Intent unregIntent = new Intent(
				"com.google.android.c2dm.intent.UNREGISTER");
		unregIntent.putExtra("app",
				PendingIntent.getBroadcast(this, 0, new Intent(), 0));
//		startService(unregIntent);

		setContentView(R.layout.login);

		pref = PreferenceManager.getDefaultSharedPreferences(this);

		pref.edit().putString(AptiMob.PREF_REST_URL, getString(R.string.rest_url)).apply();

		// Set up the login form.
		mEmail = getIntent().getStringExtra(EXTRA_EMAIL);
		mEmailView = (EditText) findViewById(R.id.email);
		mEmailView.setText(mEmail);

		mPasswordView = (EditText) findViewById(R.id.password);
		mPasswordView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView textView, int id,
							KeyEvent keyEvent) {
						if (id == R.id.login || id == EditorInfo.IME_NULL) {
							attemptLogin();
							return true;
						}
						return false;
					}
				});

		mLoginFormView = findViewById(R.id.login_form);
		mLoginStatusView = findViewById(R.id.login_status);
		mLoginStatusMessageView = (TextView) findViewById(R.id.login_status_message);

		mEmail = pref.getString(PREF_USERNAME, null);

		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("swordfish69");

		//mPassword = textEncryptor.decrypt(pref.getString(PREF_PASSWORD, ""));
		if (mEmail != null) {
			mEmailView.setText(mEmail);
		}

		findViewById(R.id.sign_in_button).setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						attemptLogin();
					}
				});
		if (VERSION.SDK_INT < 16) {
			Account[] accounts = AccountManager.get(this).getAccountsByType(
					"com.google");
			if (accounts.length == 0) {
				gmailPopup();
			}
		}

		if (getIntent().getBooleanExtra("newMessage", false)) {
			if (mEmail != null
					&& mPassword != null && mAuthTask == null) {
				mAuthTask = new UserLoginTask();
				mAuthTask.execute(mEmail,
						mPassword);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getMenuInflater().inflate(R.menu.login, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_forgot_password:
			// startActivity(new Intent(this, About.class));
			return true;
		case R.id.menu_register_new:
			Intent registerIntent = new Intent(getBaseContext(),
					RegisterNewAccount.class);
			registerIntent.putExtra("user", mEmail);
			startActivity(registerIntent);
			return true;
		}
		return false;
	}

	/**
	 * Attempts to sign in or register the account specified by the login form.
	 * If there are form errors (invalid email, missing fields, etc.), the
	 * errors are presented and no actual login attempt is made.
	 */
	public void attemptLogin() {
		if (mAuthTask != null) {
			return;
		}

		// Reset errors.
		mEmailView.setError(null);
		mPasswordView.setError(null);

		// Store values at the time of the login attempt.
		mEmail = mEmailView.getText().toString();
		mPassword = mPasswordView.getText().toString();

		boolean cancel = false;
		View focusView = null;

		// Check for a valid password.
		if (TextUtils.isEmpty(mPassword)) {
			mPasswordView.setError(getString(R.string.error_field_required));
			focusView = mPasswordView;
			cancel = true;
		} else if (mPassword.length() < 4) {
			mPasswordView.setError(getString(R.string.error_invalid_password));
			focusView = mPasswordView;
			cancel = true;
		}

		// Check for a valid email address.
		if (TextUtils.isEmpty(mEmail)) {
			mEmailView.setError(getString(R.string.error_field_required));
			focusView = mEmailView;
			cancel = true;
		} else if (!mEmail.contains("@")) {
			mEmailView.setError(getString(R.string.error_invalid_email));
			focusView = mEmailView;
			cancel = true;
		}

		if (cancel) {
			// There was an error; don't attempt login and focus the first
			// form field with an error.
			focusView.requestFocus();
		} else {
			// Show a progress spinner, and kick off a background task to
			// perform the user login attempt.
			mLoginStatusMessageView.setText(R.string.login_progress_signing_in);
			showProgress(true);
			mAuthTask = new UserLoginTask();
			mAuthTask.execute(mEmail, mPassword);
		}
	}

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	private void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginStatusView.setVisibility(View.VISIBLE);
			mLoginStatusView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginStatusView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});

			mLoginFormView.setVisibility(View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mLoginStatusView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	/**
	 * Represents an asynchronous login/registration task used to authenticate
	 * the user.
	 */
	public class UserLoginTask extends AsyncTask<String, Void, Boolean> {

		HttpsURLConnection con;
		private int r;

		@Override
		protected Boolean doInBackground(String... params) {

			try {

				// Create a trust manager that does not validate certificate
				// chains
				final TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
					@Override
					public void checkClientTrusted(
							final X509Certificate[] chain, final String authType) {
					}

					@Override
					public void checkServerTrusted(
							final X509Certificate[] chain, final String authType) {
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

				URL url = new URL("https://www.nitri.de/aptimob/login.php");
				String body = "";
				byte[] bodyBytes = body.getBytes();
				con = (HttpsURLConnection) url.openConnection();

				con
						.setSSLSocketFactory(sslSocketFactory);

				con.setReadTimeout(30000 /* milliseconds */);
				con.setConnectTimeout(50000 /* milliseconds */);
				con.setRequestMethod("POST");
				con.setDoInput(true);
				con.setDoOutput(true);
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
					con.setRequestProperty("Connection", "close");
				}
				String authString = mEmail + ":" + mPassword;
				con.setRequestProperty(
						"Authorization",
						"Basic "
								+ Base64.encodeToString(authString.getBytes(),
										Base64.NO_WRAP));
				con.setRequestProperty("Content-Type", "application/json");
				con.setRequestProperty("Content-Length",
						String.valueOf(bodyBytes.length));

				OutputStreamWriter writer = new OutputStreamWriter(
						con.getOutputStream(), "UTF-8");
				writer.write(body);
				writer.flush();

				// Start the query
				con.connect();
				r = con.getResponseCode();
				Log.d("body", body);
				Log.d("ResponseCode", Integer.toString(r));
				// Read results from the query
				// BufferedReader reader = new BufferedReader(
				// new InputStreamReader(con.getInputStream(), "UTF-8"),
				// 8 * 1024);
				// String payload = reader.readLine();
				// reader.close();
				// return true;
				// return payload;

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return false;
			}

			for (String credential : DUMMY_CREDENTIALS) {
				String[] pieces = credential.split(":");
				if (pieces[0].equals(mEmail)) {
					// Account exists, return true if the password matches.
					return pieces[1].equals(mPassword);
				}
			}

			// TODO: register the new account here.
			// Intent registerIntent = new Intent(getBaseContext(),
			// RegisterNewAccount.class);
			// registerIntent.putExtra("user", mEmail);
			// startActivity(registerIntent);
			return (r == 200);
		}

		@Override
		protected void onPostExecute(final Boolean success) {
			mAuthTask = null;
			showProgress(false);

			if (success) {
				pref.edit().putString(PREF_USERNAME, mEmail).apply();

				BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
				textEncryptor.setPassword("swordfish69");

				pref.edit().putString(PREF_PASSWORD, textEncryptor.encrypt(mPassword)).apply();

				Intent mainIntent = new Intent(getBaseContext(),
						AptiMobMain.class);
				mainIntent.putExtra("newMessage",
						getIntent().getBooleanExtra("newMessage", false));
				// mainIntent.putExtra("user", mEmail);
				// mainIntent.putExtra("password", mPassword);
				startActivity(mainIntent);
			} else {
				mPasswordView
						.setError(getString(R.string.error_incorrect_password));
				mPasswordView.requestFocus();
			}
		}

		@Override
		protected void onCancelled() {
			mAuthTask = null;
			showProgress(false);
		}
	}

	private void gmailPopup() {
		AlertDialog.Builder helpBuilder = new AlertDialog.Builder(this);
		helpBuilder.setTitle(getString(R.string.AddGmailAccount));
		helpBuilder.setMessage(getString(R.string.QuestionConfigureGmail));

		helpBuilder.setPositiveButton(getString(R.string.Yes),
				new DialogInterface.OnClickListener() {
					// @Override
					public void onClick(DialogInterface dialog, int which) {
						// try/ catch block was here
						AccountManager acm = AccountManager
								.get(getApplicationContext());
						acm.addAccount("com.google", null, null, null,
								AptiMob.this, null, null);
					}
				});

		helpBuilder.setNegativeButton(getString(R.string.No),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// close the dialog, return to activity
					}
				});

		AlertDialog helpDialog = helpBuilder.create();
		helpDialog.show();
	}
}
