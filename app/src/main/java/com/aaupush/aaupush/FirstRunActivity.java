package com.aaupush.aaupush;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;

public class FirstRunActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_first_run);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.first_run_activity);
        if (fragment == null){
            fragment = FirstRunIntro.newInstance();
            fragmentManager.beginTransaction().add(R.id.first_run_activity, fragment).commit();
        }

    }
}
