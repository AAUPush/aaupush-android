package com.aaupush.aaupush;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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

    // No announcement layout
    LinearLayout noAnnouncementLayout;

    // Swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;

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

        // Set up swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.announcement_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getContext()
                                .getApplicationContext()
                                .sendBroadcast(
                                new Intent(PushUtils.ANNOUNCEMENT_REFRESH_REQUEST_BROADCAST));
                    }
                }
        );

        noAnnouncementLayout = (LinearLayout) view.findViewById(R.id.no_announcement_layout);

        recyclerView = (RecyclerView) view.findViewById(R.id.announcement_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
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

                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);

            } else if (intent.getAction().equals(PushUtils.NO_CONNECTION_BROADCAST)
                    || intent.getAction().equals(PushUtils.CONNECTION_TIMEOUT_BROADCAST)) {
                Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();

                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);

            } else if (intent.getAction().equals(PushUtils.ANNOUNCEMENT_REFRESHED_BROADCAST)) {
                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };

    @Override
    public void onResume(){
        super.onResume();

        // Register BroadcastReceiver
        IntentFilter broadcastFilter = new IntentFilter(PushUtils.NEW_ANNOUNCEMENT_BROADCAST);
        broadcastFilter.addAction(PushUtils.NO_CONNECTION_BROADCAST);
        broadcastFilter.addAction(PushUtils.CONNECTION_TIMEOUT_BROADCAST);
        broadcastFilter.addAction(PushUtils.ANNOUNCEMENT_REFRESHED_BROADCAST);
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
            noAnnouncementLayout.setVisibility(View.VISIBLE);
        } else {
            noAnnouncementLayout.setVisibility(View.GONE);
        }

        AnnouncementAdapter announcementAdapter = new AnnouncementAdapter(sampleAnnouncements);

        recyclerView.setAdapter(announcementAdapter);
    }
}
