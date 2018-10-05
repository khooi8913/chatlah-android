package chatlah.mobile;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONException;
import org.json.JSONObject;

import chatlah.mobile.chat.ChatFragment;
import chatlah.mobile.info.InfoFragment;
import chatlah.mobile.startup.SplashActivity;

public class TabActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private Context mContext;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private Toolbar toolbar;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

//    public final int ZONE_NO_CHANGE = 0;
//    public final int CHANGE_NEW_ZONE = 1;
//    public final int OUT_OF_ZONE = 2;

    public final int REQUEST_CHECK_SETTINGS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);
        mContext = getApplicationContext();
        SharedPreferencesSingleton.getInstance(mContext);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mAuth = FirebaseAuth.getInstance();
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestId()
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(TabActivity.this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        Log.e(TAG, connectionResult.toString());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        setUpLocationCallback();
        createLocationRequest();

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
                boolean isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
                if(!isGpsEnabled || !isNetworkEnabled) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(mContext);

                    // Setting Dialog Title
                    alertDialog.setTitle("GPS is settings");

                    // Setting Dialog Message
                    alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

                    // On pressing Settings button
                    alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,int which) {
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            mContext.startActivity(intent);
                        }
                    });
                    // on pressing cancel button
                    alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    // Showing Alert Message
                    alertDialog.show();
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tab, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // Do we need anything here?
            return true;
        } else if (id == R.id.action_log_out) {
            logOut();
        } else if (id == R.id.action_app_info) {
            Intent intent = new Intent(TabActivity.this, AppInfoActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    Log.d(TAG, "ChatFragment Selected");
                    return new ChatFragment();
                case 1:
                    Log.d(TAG, "InfoFragment Selected");
                    return new InfoFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, intentFilter);
        startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
        if(broadcastReceiver!=null){
            unregisterReceiver(broadcastReceiver);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesSingleton.clearSharedPrefs();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_CHECK_SETTINGS) {
            Log.d(TAG, "Request Check Setting" + resultCode);
            if(resultCode == Activity.RESULT_OK) {
                // Do nothing
            } else {
                // Failed
            }
        }
    }

    private void startLocationUpdates() throws SecurityException {
        mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                mLocationCallback,
                null /* Looper */);
    }

    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    private void setUpLocationCallback () {
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                if (locationResult == null) {
                    Toast.makeText(mContext, "Location information not available...", Toast.LENGTH_SHORT).show();
                    return;
                }

                Location lastKnownLocation = locationResult.getLastLocation();
                double latitude = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();

                Log.d(TAG, latitude + "," + longitude);

                requestGeofenceInfo(latitude, longitude);
            }
        };
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(180000);
        mLocationRequest.setFastestInterval(180000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                // ...
            }
        });
        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(TabActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });
    }

    private void requestGeofenceInfo(double latitude, double longitude) {
        String geoFenceApiUrl = getString(R.string.geofence_api_url) + latitude + "," + longitude;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, geoFenceApiUrl, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    Log.d(TAG, response.toString());

                    if (response.getBoolean("in_fence")) {
                        String fence_id = response.getString("fence_id");

                        // Compare with the current one
                        String current_fence_id = SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE);

                        // Trying not to let it be null
                        if (current_fence_id == null) current_fence_id = "";

                        if (current_fence_id.equals(fence_id)) {
                            // Do nothing since location not changed
                            Intent broadcast = new Intent();
                            broadcast.setAction("chatlah.mobile.LOCATION_CHANGED");
                            broadcast.putExtra("GeofenceEvent", 0); // ZONE NO CHANGE
                            sendBroadcast(broadcast);
                        } else {
                            // Have to clear chat messages
                            SharedPreferencesSingleton.setSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE, fence_id);
                            SharedPreferencesSingleton.setSharedPrefStringVal(SharedPreferencesSingleton.CHAT_SESSION_START, System.currentTimeMillis() / 1000L + "");

                            toolbar.setTitle("ChatLAH!@" + SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE));

                            // Notify everyone that something changed
                            Intent broadcast = new Intent();
                            broadcast.setAction("chatlah.mobile.LOCATION_CHANGED");
                            broadcast.putExtra("GeofenceEvent", 1); // ENTERED SOME NEW ZONE
                            sendBroadcast(broadcast);
                        }
                    } else {
                        toolbar.setTitle("ChatLAH!");
                        // Out of zone
                        // Notify everyone that something changed
                        SharedPreferencesSingleton.clearSharedPrefs();

                        Intent broadcast = new Intent();
                        broadcast.setAction("chatlah.mobile.LOCATION_CHANGED");
                        broadcast.putExtra("GeofenceEvent", 2); // OUT OF ZONE
                        sendBroadcast(broadcast);
                    }
                } catch (JSONException e) {
                    Log.d(TAG, e.toString());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // What should we do here a?
                Log.e(TAG, error.toString());
            }
        });
        Volley.newRequestQueue(mContext).add(jsonObjectRequest);
    }

    private void logOut() {
        mAuth.signOut();
        Auth.GoogleSignInApi.signOut(mGoogleApiClient).setResultCallback(new ResultCallbacks<Status>() {
            @Override
            public void onSuccess(@NonNull Status status) {
                Log.d(TAG, status.toString());
            }

            @Override
            public void onFailure(@NonNull Status status) {
                Log.e(TAG, status.toString());
            }
        });
        Intent intent = new Intent(TabActivity.this, SplashActivity.class);
        startActivity(intent);
        finish();
    }
}
