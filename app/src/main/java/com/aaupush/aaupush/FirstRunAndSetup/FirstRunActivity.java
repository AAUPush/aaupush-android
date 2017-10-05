package com.aaupush.aaupush.FirstRunAndSetup;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaupush.aaupush.R;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.tsengvn.typekit.TypekitContextWrapper;

public class FirstRunActivity extends AppCompatActivity {

    public RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);

        // Set up request queue for the whole activity
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.first_run_activity);
        if (fragment == null){
            fragment = FirstRunIntro.newInstance();
            fragmentManager.beginTransaction().add(R.id.first_run_activity, fragment).commit();
        }

    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }

    public RequestQueue getRequestQueue() {
        return this.requestQueue;
    }
}
