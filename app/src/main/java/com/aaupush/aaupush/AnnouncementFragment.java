package com.aaupush.aaupush;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.awt.font.TextAttribute;
import java.util.ArrayList;
import java.util.Date;


/**
 * A {@link Fragment} that displays a list of {@link Announcement}s.
 */
public class AnnouncementFragment extends Fragment {

    private static final String TAG = "AnnouncementFragment";

    // The main list/recycler view
    RecyclerView recyclerView;

    public AnnouncementFragment() {
        // Required empty public constructor
    }

    static AnnouncementFragment newInstance(){
        return new AnnouncementFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_announcement, container, false);


        recyclerView = (RecyclerView) view.findViewById(R.id.announcement_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                new LinearLayoutManager(getContext()).getOrientation()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        // Set adapter for the recycler view
        setAdapter();

        return view;
    }

    // BroadcastReceiver
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getAction() + " Broadcast Received");
            if (intent.getAction().equals(PushUtils.NEW_ANNOUNCEMENT_BROADCAST)){
                setAdapter();
            } else if (intent.getAction().equals(PushUtils.NO_CONNECTION_BROADCAST)) {
                Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();

        // Register BroadcastReceiver
        IntentFilter broadcastFilter = new IntentFilter(PushUtils.NEW_ANNOUNCEMENT_BROADCAST);
        broadcastFilter.addAction(PushUtils.NO_CONNECTION_BROADCAST);
        getActivity().registerReceiver(broadcastReceiver, broadcastFilter);

        // The SharedPreferences value is updated every time the fragment resumes
        // or detached. This will let the Service know if the fragment is running
        // or not. The service will use this information either to raise a notification
        // or not
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.putBoolean(PushUtils.SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING, true);
        editor.apply();

    }

    @Override
    public void onDetach(){
        super.onDetach();

        // Unregister BroadcastReceiver
        getActivity().unregisterReceiver(broadcastReceiver);

        // The SharedPreferences value is updated every time the fragment resumes
        // or detached. This will let the Service know if the fragment is running
        // or not. The service will use this information either to raise a notification
        // or not
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.putBoolean(PushUtils.SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING, false);
        editor.apply();
    }

    /**
     * Queries the database for the announcements and adds them to the list.
     * Also called when there is a new announcement.
     */
    private void setAdapter(){
        ArrayList<Announcement> sampleAnnouncements;
        DBHelper dbHelper = new DBHelper(getContext().getApplicationContext());
        sampleAnnouncements = dbHelper.getAnnouncements();
        dbHelper.close();
        if (sampleAnnouncements == null){
            sampleAnnouncements = new ArrayList<>();
        }

        AnnouncementAdapter announcementAdapter = new AnnouncementAdapter(sampleAnnouncements);

        recyclerView.setAdapter(announcementAdapter);
    }
}
