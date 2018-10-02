package chatlah.mobile.startup;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import chatlah.mobile.R;
import chatlah.mobile.TabActivity;

public class SplashActivity extends AppCompatActivity {

    private final int SPLASH_DURATION = 1000;
    private String TAG = getClass().getSimpleName();

    private Thread mThread;
    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mContext = this.getApplicationContext();
        mThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    synchronized (this) {
                        wait(SPLASH_DURATION);
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, e.toString());
                } finally {
                    if (!isFinishing()) {
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if (firebaseUser != null) {  // User is logged in, proceed to verify identity
                            // TODO: Verify user
                            Log.d(TAG, firebaseUser.getUid());

                            Intent intent = new Intent(SplashActivity.this, TabActivity.class);
                            startActivity(intent);
                            finish();
                        } else {    // User is not logged in, proceed to login page
                            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }
                    mThread.interrupt();
                }
            }
        });

        mThread.start();
    }

}
