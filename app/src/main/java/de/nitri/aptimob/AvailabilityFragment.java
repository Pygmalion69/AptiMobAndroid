package de.nitri.aptimob;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import com.google.gson.Gson;
import com.google.gson.JsonParser;

import android.R.bool;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.jasypt.util.text.BasicTextEncryptor;

public class AvailabilityFragment extends Fragment implements LocationListener {

    private TextView tvStatus;
    private boolean available;

    private String restUrl;
    private SharedPreferences pref;
    private LocationManager locationManager;
    private static final String PREF_AVAILABLE = "available";
    private String best;
    private String username;
    private boolean scenarios;
    private Button btnScenarios;
    private Activity mActivity;
    private BasicTextEncryptor textEncryptor;
    private String password;

    public interface AvailabilityFragmentCallbacks {

        BasicTextEncryptor getTextEncryptor();

        SharedPreferences getPref();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View availabilityView = inflater.inflate(R.layout.availability,
                container, false);
        tvStatus = (TextView) availabilityView.findViewById(R.id.status);

        Button btnAvailable = (Button) availabilityView
                .findViewById(R.id.button_available);
        Button btnOccupied = (Button) availabilityView
                .findViewById(R.id.button_occupied);

        btnAvailable.getBackground().setColorFilter(Color.GREEN,
                PorterDuff.Mode.MULTIPLY);
        btnOccupied.getBackground().setColorFilter(Color.RED,
                PorterDuff.Mode.MULTIPLY);

        btnScenarios = (Button) availabilityView
                .findViewById(R.id.button_scenarios);
        // globalCache.setMainActivity(this);

        btnAvailable.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setAvailability(true);
                Location location = locationManager.getLastKnownLocation(best);
                sendLocation(location);
            }
        });

        btnOccupied.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                setAvailability(false);

            }
        });

        return availabilityView;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mActivity = activity;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        AvailabilityFragmentCallbacks callbacks = (AvailabilityFragmentCallbacks) mActivity;

        pref = callbacks.getPref();
        textEncryptor = callbacks.getTextEncryptor();

        restUrl = pref.getString(AptiMob.PREF_REST_URL, getString(R.string.rest_url));

        username = pref.getString(AptiMob.PREF_USERNAME, "");

        pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        available = pref.getBoolean(PREF_AVAILABLE, false);

        locationManager = (LocationManager) getActivity().getSystemService(
                Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        best = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(best);

        getUser();
        updateScreen();
        sendLocation(location);
    }

    public void updateScreen() {
        if (available) {
            tvStatus.setText(R.string.Available);
            tvStatus.setTextColor(Color.GREEN);
        } else {
            tvStatus.setText(R.string.Occupied);
            tvStatus.setTextColor(Color.RED);
        }
    }

    private void setAvailability(boolean avail) {
        available = avail;
        username = pref.getString(AptiMob.PREF_USERNAME, "");
        password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
        String body = "";
        try {
            body = "username="
                    + URLEncoder.encode(username, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        body += "&available=" + Boolean.toString(avail);
        RestTask restTask = new RestTask(getActivity(), "aptimob.available",
                username, password);
        restTask.execute(restUrl + "available", body, "POST");
        updateScreen();
    }

    private void sendLocation(Location location) {
        if (null != location && available) {
            String body = "";
            Double lon = location.getLongitude();
            Double lat = location.getLatitude();
            username = pref.getString(AptiMob.PREF_USERNAME, "");
            password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
            try {
                body = "username="
                        + URLEncoder.encode(username, "UTF-8");
                body += "&lon="
                        + URLEncoder.encode(Double.toString(lon), "UTF-8");
                body += "&lat="
                        + URLEncoder.encode(Double.toString(lat), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Log.i("SENDING", body);
            RestTask restTask = new RestTask(getActivity(), "aptimob.location",
                    username, password);
            restTask.execute(restUrl + "location", body, "POST");
        }
    }

    private void getUser() {
        username = pref.getString(AptiMob.PREF_USERNAME, "");
        password = textEncryptor.decrypt(pref.getString(AptiMob.PREF_PASSWORD, ""));
        String body = "";
        try {
            body = "username=" + URLEncoder.encode(username, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        RestTask restTask = new RestTask(getActivity(),
                "aptimob.userByUsername", username, password);
        restTask.execute(restUrl + "userByUsername", body, "POST");

    }

    @Override
    public void onResume() {
        super.onResume();
        locationManager.requestLocationUpdates(best, 600000, 1, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        sendLocation(location);

    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public boolean getScenarios() {
        return scenarios;
    }

    public void setScenarios(boolean scenarios) {
        this.scenarios = scenarios;
        if (scenarios) {
            btnScenarios.setVisibility(View.VISIBLE);
            btnScenarios.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {

                    Intent scenariosIntent = new Intent(getActivity(), ScenariosActivity.class);
                    scenariosIntent.putExtra("section", 0);
                    startActivity(scenariosIntent);

                }
            });
        }

    }

}
