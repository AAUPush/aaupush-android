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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import java.util.ArrayList;


/**
 * A  {@link Fragment} that displays a list of materials and course folders.
 */
public class MaterialFragment extends Fragment {

    private static final String TAG = "MaterialFragment";

    // Fragment status values
    static final int FRAGMENT_STATUS_MAIN = 1;
    static final int FRAGMENT_STATUS_FOLDER_CONTENT = 2;

    // Status about the current state of the fragment
    // Can have two values
    // 1. Main
    //    This is the state where the recycler view contains the headers, the latest materials
    //    and the course folders
    // 2. Folder Content
    //    This is the state where the recycler view contains only materials of a certain
    //    course folder
    //
    // Default value is main
    static int FRAGMENT_STATUS = FRAGMENT_STATUS_MAIN;

    // When a Course Folder is selected, its ID is temporarily saved here
    // so that when the list has to be refreshed for different reasons, it
    // will remember which course folder it was in
    static int selectedCourse = -1;

    static String INTENT_EXTRA_COURSE_ID = "course_id";

    // Main RecyclerView
    RecyclerView recyclerView;

    // Empty folder layout
    LinearLayout emptyFolderLayout;

    // Swipe refresh layout
    SwipeRefreshLayout swipeRefreshLayout;

    static MaterialFragment newInstance() {
        return new MaterialFragment();
    }

    public MaterialFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view =  inflater.inflate(R.layout.fragment_material, container, false);

        // Link the recycler view
        recyclerView = (RecyclerView) view.findViewById(R.id.material_recycler_view);

        // Set up swipe refresh layout
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.material_refresh_layout);
        swipeRefreshLayout.setRefreshing(true);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        getContext()
                                .getApplicationContext()
                                .sendBroadcast(
                                        new Intent(PushUtils.MATERIAL_REFRESH_REQUEST_BROADCAST));
                    }
                }
        );

        // Link the empty folder layout
        emptyFolderLayout = (LinearLayout) view.findViewById(R.id.no_file_folder_layout);

        // Set attributes for the recycler view
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(false);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


        // Set the adapter
        setAdapter();

        return view;
    }

    @Override
    public void onResume(){
        super.onResume();

        // Register BroadcastReceiver
        IntentFilter broadcastFilter = new IntentFilter(PushUtils.NEW_MATERIAL_BROADCAST);
        broadcastFilter.addAction(PushUtils.ON_FOLDER_CLICK_BROADCAST);
        broadcastFilter.addAction(PushUtils.BACK_PRESSED_ON_FOLDER_VIEW_BROADCAST);
        broadcastFilter.addAction(PushUtils.MATERIAL_DOWNLOAD_STARTED_BROADCAST);
        broadcastFilter.addAction(PushUtils.MATERIAL_DOWNLOAD_COMPLETED_BROADCAST);
        broadcastFilter.addAction(PushUtils.NO_CONNECTION_BROADCAST);
        broadcastFilter.addAction(PushUtils.CONNECTION_TIMEOUT_BROADCAST);
        broadcastFilter.addAction(PushUtils.MATERIAL_REFRESHED_BROADCAST);
        getActivity().registerReceiver(broadcastReceiver, broadcastFilter);

        // The SharedPreferences value is updated every time the fragment resumes
        // or detached. This will let the Service know if the fragment is running
        // or not. The service will use this information either to raise a notification
        // or not
        SharedPreferences.Editor editor = getActivity()
                .getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE)
                .edit();
        editor.putBoolean(PushUtils.SP_IS_MATERIAL_FRAGMENT_RUNNING, true);
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
        editor.putBoolean(PushUtils.SP_IS_MATERIAL_FRAGMENT_RUNNING, false);
        editor.apply();
    }


    /**
     * This methods set the default adapter to the main material recycler view.
     * The default adapter contains the latest 3 materials and the list of the
     * course folders, with list headers between each section(latest 3 and course
     * folders).
     */
    private void setAdapter() {
        ArrayList<Object> sampleList = new ArrayList<>();
        sampleList.add("LATEST");   // Add the header for the latest materials
        DBHelper dbHelper = new DBHelper(getContext());

        // Get materials
        ArrayList<Material> materials = dbHelper.getLatestMaterials();

        // Add them to the adapter if the result is not null
        if (materials != null) {
            sampleList.addAll(materials);
        }

        // Add a header to the list
        sampleList.add("COURSE FOLDER");

        // Get Course Folders
        ArrayList<Course> folders = dbHelper.getCourses();

        // Add them to the adapter if the result is not null
        if (folders != null){
            sampleList.addAll(folders);
            emptyFolderLayout.setVisibility(View.GONE);
        } else {
            emptyFolderLayout.setVisibility(View.VISIBLE);
        }

        // Close the DB object
        dbHelper.close();

        MaterialAdapter adapter = new MaterialAdapter(sampleList);
        recyclerView.setAdapter(adapter);

    }

    /**
     * When a course folder is clicked on, this method is called with the id of
     * the course as the parameter
     * It will query the database for materials with the course id
     * @param courseId The ID of the course clicked on
     */
    private void setCourseAdapter(int courseId) {
        ArrayList<Object> materialList = new ArrayList<>();
        DBHelper dbHelper = new DBHelper(getContext());

        // Get materials
        ArrayList<Material> materials = dbHelper.getMaterials(courseId);

        // Set the title of the folder
        materialList.add(dbHelper.getCourse(courseId).getName());

        // Add them to the adapter if the result is not null
        if (materials != null) {
            materialList.addAll(materials);
            emptyFolderLayout.setVisibility(View.GONE);
        } else {
            emptyFolderLayout.setVisibility(View.VISIBLE);
        }

        // Close the DB object
        dbHelper.close();

        MaterialAdapter adapter = new MaterialAdapter(materialList);
        recyclerView.setAdapter(adapter);

    }

    // BroadcastReceiver
    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, intent.getAction() + " Broadcast Received");
            if (intent.getAction().equals(PushUtils.NEW_MATERIAL_BROADCAST)){

                // refresh the adapter
                setAdapter();

                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);

            } else if (intent.getAction().equals(PushUtils.ON_FOLDER_CLICK_BROADCAST)) {
                int courseId = intent.getIntExtra(INTENT_EXTRA_COURSE_ID, 0);

                // Save the status of the courseID for later
                selectedCourse = courseId;

                // Change recycler adapter to folders
                setCourseAdapter(courseId);
                FRAGMENT_STATUS = FRAGMENT_STATUS_FOLDER_CONTENT;
            } else if (intent.getAction().equals(PushUtils.BACK_PRESSED_ON_FOLDER_VIEW_BROADCAST)) {
                // Go back to main
                setAdapter();
                FRAGMENT_STATUS = FRAGMENT_STATUS_MAIN;
            } else if (intent.getAction().equals(PushUtils.MATERIAL_DOWNLOAD_STARTED_BROADCAST)
                    || intent.getAction().equals(PushUtils.MATERIAL_DOWNLOAD_COMPLETED_BROADCAST)) {

                if (FRAGMENT_STATUS == FRAGMENT_STATUS_MAIN) {
                    setAdapter();
                } else if (FRAGMENT_STATUS == FRAGMENT_STATUS_FOLDER_CONTENT &&
                        selectedCourse != -1) {
                    setCourseAdapter(selectedCourse);
                }
            } else if (intent.getAction().equals(PushUtils.NO_CONNECTION_BROADCAST)
                    || intent.getAction().equals(PushUtils.CONNECTION_TIMEOUT_BROADCAST)) {
                Snackbar.make(getView(), "Connection Error", Snackbar.LENGTH_LONG).show();

                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);
            } else if (intent.getAction().equals(PushUtils.MATERIAL_REFRESHED_BROADCAST)) {
                // Stop refreshing layout
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    };

}
