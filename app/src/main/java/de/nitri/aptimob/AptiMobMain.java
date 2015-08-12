package de.nitri.aptimob;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.widget.TabHost;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.jasypt.util.text.BasicTextEncryptor;

import java.util.HashMap;

public class AptiMobMain extends FragmentActivity implements AvailabilityFragment.AvailabilityFragmentCallbacks, MessagesFragment.MessagesFragmentCallbacks {

    private Gson gson = new Gson();
    private JsonParser parser = new JsonParser();

    private SharedPreferences pref;
    private boolean available;
    public static final String PREF_AVAILABLE = "available";

    private final String TAG = "AptiMob";
    private TabHost tabHost;

    private BasicTextEncryptor textEncryptor;
    private BroadcastReceiver mRegistrationBroadcastReceiver;

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    private ProgressDialog progressDialog;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        showProgress();
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                dismissProgress();
                // boolean sentToken = pref
                //        .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
                //if (sentToken) {
                // mInformationTextView.setText(getString(R.string.gcm_send_message));
                //} else {
                // mInformationTextView.setText(getString(R.string.token_error_message));
                //}
            }
        };
        //mInformationTextView = (TextView) findViewById(R.id.informationTextView);

        if (checkPlayServices()) {
            // Start IntentService to register this application with GCM.
            Intent intent = new Intent(this, RegistrationIntentService.class);
            startService(intent);
        }

        available = pref.getBoolean(PREF_AVAILABLE, false);

        tabHost = (TabHost) findViewById(android.R.id.tabhost);
        tabHost.setup();

        TabManager tabManager = new TabManager(this, tabHost, R.id.realtabcontent);

        tabManager.addTab(
                tabHost.newTabSpec("availability").setIndicator(
                        getString(R.string.Availability),
                        ContextCompat.getDrawable(this,
                                R.drawable.ic_tab_availability)),
                AvailabilityFragment.class, null);
        tabManager
                .addTab(tabHost.newTabSpec("messages").setIndicator(
                                getString(R.string.Messages),
                                ContextCompat.getDrawable(this, R.drawable.ic_tab_messages)),
                        MessagesFragment.class, null);

        if (getIntent().getBooleanExtra("newMessage", false)) {
            tabHost.setCurrentTab(1);
        }

        if (savedInstanceState != null) {
            tabHost.setCurrentTabByTag(savedInstanceState.getString("tab"));
        }

        textEncryptor = new BasicTextEncryptor();
        textEncryptor.setPassword("swordfish69");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tab", tabHost.getCurrentTabTag());
    }

	/*public void sendGcmRegId(String registrationId) {
        if (null != registrationId && !registrationId.equals("")) {
			String body = "";
			try {
				body = "username=" + URLEncoder.encode(username, "UTF-8");
				body += "&gcmRegId="
						+ URLEncoder.encode(registrationId, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Log.i("SENDING", body);
			username = pref.getString(AptiMob.PREF_USERNAME, "");
			password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
			RestTask restTask = new RestTask(this, "aptimob.gcmRegId",
					username, password);
			restTask.execute(pref.getString(AptiMob.PREF_REST_URL, getString(R.string.rest_url)) + "gcmRegId", body, "POST");
		}
	}*/

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(QuickstartPreferences.REGISTRATION_COMPLETE));
        IntentFilter filter = new IntentFilter("aptimob.userByUsername");
        filter.addAction("aptimob.available");
        registerReceiver(receiver, filter);
    }


    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        unregisterReceiver(receiver);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            // Clear progress indicator
            // if(progress != null) {
            // progress.dismiss();
            // }
            String action = intent.getAction();
            if (action.equals("aptimob.userByUsername")) {
                if (intent.getIntExtra(RestTask.HTTP_RESPONSE_CODE, 0) == 200) {
                    JsonObject jo = parser.parse(
                            intent.getStringExtra(RestTask.HTTP_RESPONSE))
                            .getAsJsonObject();
                    User user = gson.fromJson(jo, User.class);
                    available = user.isAvailable();
                    pref.edit().putBoolean(PREF_AVAILABLE, available).apply();
                    AvailabilityFragment availabilityFragment = (AvailabilityFragment) getSupportFragmentManager()
                            .findFragmentByTag("availability");
                    if (availabilityFragment != null) {
                        availabilityFragment.setAvailable(available);
                        availabilityFragment.updateScreen();
                        if (user.hasScenarios()) {
                            availabilityFragment.setScenarios(true);
                        }
                    }
                }
            }
            if (action.equals("aptimob.available")) {
                if (intent.getIntExtra(RestTask.HTTP_RESPONSE_CODE, 0) == 200) {
                    // Ok
                }
            }
            String response = intent.getStringExtra(RestTask.HTTP_RESPONSE);
            // Process the response data (here we just display it)

        }
    };

    @Override
    public BasicTextEncryptor getTextEncryptor() {
        return textEncryptor;
    }

    @Override
    public SharedPreferences getPref() {
        return pref;
    }

    /**
     * This is a helper class that implements a generic mechanism for
     * associating fragments with the tabs in a tab host. It relies on a trick.
     * Normally a tab host has a simple API for supplying a View or Intent that
     * each tab will show. This is not sufficient for switching between
     * fragments. So instead we make the content part of the tab host 0dp high
     * (it is not shown) and the TabManager supplies its own dummy view to show
     * as the tab content. It listens to changes in tabs, and takes care of
     * switch to the correct fragment shown in a separate content area whenever
     * the selected tab changes.
     */
    public static class TabManager implements TabHost.OnTabChangeListener {
        private final FragmentActivity mActivity;
        private final TabHost mTabHost;
        private final int mContainerId;
        private final HashMap<String, TabInfo> mTabs = new HashMap<>();
        TabInfo mLastTab;

        static final class TabInfo {
            private final String tag;
            private final Class<?> clss;
            private final Bundle args;
            private Fragment fragment;

            TabInfo(String _tag, Class<?> _class, Bundle _args) {
                tag = _tag;
                clss = _class;
                args = _args;
            }
        }

        static class DummyTabFactory implements TabHost.TabContentFactory {
            private final Context mContext;

            public DummyTabFactory(Context context) {
                mContext = context;
            }

            @Override
            public View createTabContent(String tag) {
                View v = new View(mContext);
                v.setMinimumWidth(0);
                v.setMinimumHeight(0);
                return v;
            }
        }

        public TabManager(FragmentActivity activity, TabHost tabHost,
                          int containerId) {
            mActivity = activity;
            mTabHost = tabHost;
            mContainerId = containerId;
            mTabHost.setOnTabChangedListener(this);
        }

        public void addTab(TabHost.TabSpec tabSpec, Class<?> clss, Bundle args) {
            tabSpec.setContent(new DummyTabFactory(mActivity));
            String tag = tabSpec.getTag();

            TabInfo info = new TabInfo(tag, clss, args);

            // Check to see if we already have a fragment for this tab, probably
            // from a previously saved state. If so, deactivate it, because our
            // initial state is that a tab isn't shown.
            info.fragment = mActivity.getSupportFragmentManager()
                    .findFragmentByTag(tag);
            if (info.fragment != null && !info.fragment.isDetached()) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager()
                        .beginTransaction();
                ft.detach(info.fragment);
                ft.commit();
            }

            mTabs.put(tag, info);
            mTabHost.addTab(tabSpec);
        }

        @Override
        public void onTabChanged(String tabId) {
            TabInfo newTab = mTabs.get(tabId);
            if (mLastTab != newTab) {
                FragmentTransaction ft = mActivity.getSupportFragmentManager()
                        .beginTransaction();
                if (mLastTab != null) {
                    if (mLastTab.fragment != null) {
                        ft.detach(mLastTab.fragment);
                    }
                }
                if (newTab != null) {
                    if (newTab.fragment == null) {
                        newTab.fragment = Fragment.instantiate(mActivity,
                                newTab.clss.getName(), newTab.args);
                        ft.add(mContainerId, newTab.fragment, newTab.tag);
                    } else {
                        ft.attach(newTab.fragment);
                    }
                }

                mLastTab = newTab;
                ft.commit();
                mActivity.getSupportFragmentManager()
                        .executePendingTransactions();
            }
        }
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }

    /**
     * http://pygmalion.nitri.de/an-indeterminate-progress-indicator-on-android-393.html
     */
    public void showProgress() {
        dismissProgress();
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setCancelable(false);
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        timerDelayDismissProgress(15000, progressDialog);
    }

    public void dismissProgress() {
        if (null != progressDialog)
            progressDialog.dismiss();
    }

    public void timerDelayDismissProgress(long time, final ProgressDialog d) {
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                d.dismiss();
            }
        }, time);
    }

}
