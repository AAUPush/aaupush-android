package com.aaupush.aaupush.Setting;


import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.aaupush.aaupush.FirstRunAndSetup.CourseSelectionFragment;
import com.aaupush.aaupush.PushUtils;
import com.aaupush.aaupush.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class SettingsHomeFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener{

    // SharedPreferences
    SharedPreferences preferences;

    // Main View
    View view;


    public SettingsHomeFragment() {
        // Required empty public constructor
    }

    public static SettingsHomeFragment newInstance() { return new SettingsHomeFragment(); }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_settings_home, container, false);

        // Init SharedPreferences
        preferences = getContext().getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE);

        // Set OnClickListeners
        view.findViewById(R.id.follow_unfollow_courses_tv).setOnClickListener(this);
        view.findViewById(R.id.start_over_tv).setOnClickListener(this);

        // Init notification switches
        SwitchCompat mainNotificationSwitch = (SwitchCompat)view.findViewById(R.id.notification_switch);
        SwitchCompat announcementNotificationSwitch = (SwitchCompat)view.findViewById(R.id.announcements_notification_switch);
        SwitchCompat materialNotificationSwitch = (SwitchCompat)view.findViewById(R.id.materials_notification_switch);

        // Get values from SharedPreferences
        boolean mainNotificationEnabled = preferences.getBoolean(PushUtils.SP_NOTIFICATION_ENABLED, true);
        boolean announcementNotificationEnabled = preferences.getBoolean(PushUtils.SP_ANNOUNCEMENT_NOTIFICATION_ENABLED, true);
        boolean materialNotificationEnabled = preferences.getBoolean(PushUtils.SP_MATERIAL_NOTIFICATION_ENABLED, true);

        // Set the values to the Switch views
        mainNotificationSwitch.setChecked(mainNotificationEnabled);
        announcementNotificationSwitch.setChecked(announcementNotificationEnabled);
        materialNotificationSwitch.setChecked(materialNotificationEnabled);

        // Set OnCheckedChangeListeners
        mainNotificationSwitch.setOnCheckedChangeListener(this);
        announcementNotificationSwitch.setOnCheckedChangeListener(this);
        materialNotificationSwitch.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.follow_unfollow_courses_tv:
                // Go to follow course fragment
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager
                        .beginTransaction()
                        .replace(R.id.setting_activity, CourseSelectionFragment.newInstance())
                        .commit();

                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
        switch (compoundButton.getId()) {
            case R.id.notification_switch:
                preferences.edit().putBoolean(PushUtils.SP_NOTIFICATION_ENABLED, b).apply();
                if(!b) {
                    view.findViewById(R.id.announcements_notification_switch).setEnabled(false);
                    view.findViewById(R.id.materials_notification_switch).setEnabled(false);
                } else {
                    view.findViewById(R.id.announcements_notification_switch).setEnabled(true);
                    view.findViewById(R.id.materials_notification_switch).setEnabled(true);
                }
                break;
            case R.id.announcements_notification_switch:
                preferences.edit().putBoolean(PushUtils.SP_ANNOUNCEMENT_NOTIFICATION_ENABLED, b).apply();
                break;
            case R.id.materials_notification_switch:
                preferences.edit().putBoolean(PushUtils.SP_MATERIAL_NOTIFICATION_ENABLED, b).apply();
                break;
        }
    }
}
