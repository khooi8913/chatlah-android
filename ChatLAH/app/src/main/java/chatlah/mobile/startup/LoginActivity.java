package chatlah.mobile.startup;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import chatlah.mobile.R;

public class LoginActivity extends AppCompatActivity {

    private Context mContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this.getApplicationContext();
    }
}
