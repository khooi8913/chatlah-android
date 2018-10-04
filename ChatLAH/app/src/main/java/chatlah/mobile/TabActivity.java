package chatlah.mobile;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
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

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.firebase.auth.FirebaseAuth;

import chatlah.mobile.chat.ChatFragment;
import chatlah.mobile.info.InfoFragment;
import chatlah.mobile.startup.SplashActivity;

public class TabActivity extends AppCompatActivity {

    private String TAG = getClass().getSimpleName();
    private final int LOCATION_RC_CODE = 9002;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    private ViewPager mViewPager;
    private TabLayout mTabLayout;

    private FirebaseAuth mAuth;
    private GoogleApiClient mGoogleApiClient;
    private GoogleSignInClient mGoogleSignInClient;

    private BroadcastReceiver broadcastReceiver;
    private IntentFilter intentFilter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tab);

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

        final Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        mTabLayout = findViewById(R.id.tab_layout);
        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE) == null ||
                        SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE).equals("")) {
                    toolbar.setTitle("ChatLAH!");
                } else {
                    toolbar.setTitle("ChatLAH!@" + SharedPreferencesSingleton.getSharedPrefStringVal(SharedPreferencesSingleton.CONVERSATION_ZONE));
                }
            }
        };

        intentFilter = new IntentFilter();
        intentFilter.addAction("chatlah.mobile.LOCATION_CHANGED");
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Request location permissions
        // TODO: onRequestPermissionResult
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_RC_CODE);
        }
        registerReceiver(broadcastReceiver, intentFilter);
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
                    return new ChatFragment();
                case 1:
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
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(broadcastReceiver!=null) unregisterReceiver(broadcastReceiver);
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
