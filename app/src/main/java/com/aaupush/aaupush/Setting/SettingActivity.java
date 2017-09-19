package com.aaupush.aaupush.Setting;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaupush.aaupush.FirstRunAndSetup.FirstRunIntro;
import com.aaupush.aaupush.R;

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.setting_activity);
        if (fragment == null){
            fragment = SettingsHomeFragment.newInstance();
            fragmentManager.beginTransaction().add(R.id.setting_activity, fragment).commit();
        }
    }
}
