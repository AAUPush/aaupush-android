package com.aaupush.aaupush.Setting;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.aaupush.aaupush.FirstRunAndSetup.CourseSelectionFragment;
import com.aaupush.aaupush.FirstRunAndSetup.FirstRunIntro;
import com.aaupush.aaupush.R;
import com.tsengvn.typekit.TypekitContextWrapper;

public class SettingActivity extends AppCompatActivity {

    public final static int MODE_ADD_DROP_COURSE = 1;
    public final static int MODE_DEFAULT = 2;
    public final static String FRAGMENT_MODE_INTENT_EXTRA_KEY = "mode";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.setting_activity);
        if (fragment == null){
            if (getIntent().getIntExtra(FRAGMENT_MODE_INTENT_EXTRA_KEY, MODE_DEFAULT) == MODE_DEFAULT) {
                fragment = SettingsHomeFragment.newInstance();
            } else {
                fragment = CourseSelectionFragment.newInstance();
            }

            fragmentManager.beginTransaction().add(R.id.setting_activity, fragment).commit();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(TypekitContextWrapper.wrap(newBase));
    }
}
