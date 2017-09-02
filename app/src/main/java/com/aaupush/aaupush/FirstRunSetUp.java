package com.aaupush.aaupush;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.ServiceConfigurationError;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstRunSetUp extends Fragment implements AdapterView.OnItemSelectedListener,
        RadioGroup.OnCheckedChangeListener{

    RequestQueue requestQueue;

    static final String TAG = "FirstRunSetUpFragment";


    // UI Elements
    Spinner studyFieldSpinner;

    // Loading Layout
    LinearLayout loadingForeground;

    //SharedPreference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Main view
    View view;


    public FirstRunSetUp() {
        // Required empty public constructor
    }

    public static FirstRunSetUp newInstance(){
        return new FirstRunSetUp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_set_up, container, false);

        // Init SharedPreferences and Editor
        preferences = getContext().getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        // Loading layout
        loadingForeground = (LinearLayout) view.findViewById(R.id.loading_foreground);

        requestQueue = Volley.newRequestQueue(getContext());

        // Init Spinner for Study Fields
        studyFieldSpinner = (Spinner) view.findViewById(R.id.studyFieldSpinner);
        studyFieldSpinner.setOnItemSelectedListener(this);
        setStudyFieldAdapter();

        // Init RadioGroups and set OnChange Listeners
        RadioGroup yearRg = (RadioGroup) view.findViewById(R.id.year_rg);
        yearRg.setOnCheckedChangeListener(this);

        RadioGroup sectionRg = (RadioGroup) view.findViewById(R.id.section_rg);
        sectionRg.setOnCheckedChangeListener(this);

        final Button nextBtn = (Button) view.findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Show the loading layout/progress bar
                loadingForeground.setVisibility(View.VISIBLE);

                // TODO: Check if correct section and year are selected

                // Get the list of course and add them to the db
                // Build the request URL
                String url = PushUtils.URL_GET_COURSES;

                // Get the study field of the student
                String studyField = preferences.getString(PushUtils.SP_STUDY_FIELD_CODE, "CS");

                // Year of the student
                final int year = preferences.getInt(PushUtils.SP_SELECTED_YEAR, 1);

                // Append GET parameters
                url = PushUtils.appendGetParameter("year", year + "", url);
                url = PushUtils.appendGetParameter("study_field_code", studyField, url);

                // Build the json array request
                JsonArrayRequest request = new JsonArrayRequest(
                        url,
                        new Response.Listener<JSONArray>() {
                            @Override
                            public void onResponse(JSONArray response) {
                                // Output the response to log
                                Log.d(TAG, response.toString());

                                // Parse the JSON Array
                                try {
                                    // Check if the array is not empty
                                    if (response.length() < 1){
                                        Log.d(TAG, "CourseRequest returned empty JSON Array");
                                        // TODO: Show message about empty course list
                                        return; // Exit if array is empty
                                    }

                                    // Create Y(year)S(section) folder
                                    String yearFolder = "Y" + year;
                                    // Create aaupush directory
                                    File createFolder = new File(Environment.getExternalStorageDirectory(),
                                            PushUtils.ROOT_FOLDER + "/" + yearFolder);
                                    if (!createFolder.exists()) {
                                        createFolder.mkdirs();
                                    }

                                    // Init DB Object
                                    DBHelper dbHelper = new DBHelper(getContext().getApplicationContext());

                                    // Loop through the array and add each course to the db
                                    for (int i = 0; i < response.length(); i++) {
                                        // Get a JSON object from the array
                                        JSONObject json = (JSONObject) response.get(i);

                                        // Add the course to the db
                                        dbHelper.addCourse(
                                                json.getInt("id"),
                                                json.getString("name"),
                                                json.getInt("year")
                                        );

                                        new File(Environment.getExternalStorageDirectory(),
                                                PushUtils.ROOT_FOLDER + "/" +
                                                        "Y" + json.getInt("year") + "/" +
                                                        json.getString("name"))
                                                .mkdirs();

                                    }

                                    // Close the db object
                                    dbHelper.close();

                                    // If all went well proceed to MainActivity
                                    //Change is_first_run value in shared preferences
                                    editor.putBoolean(PushUtils.SP_IS_FIRST_RUN, false);
                                    editor.apply();

                                    //Start MainActivity
                                    startActivity(new Intent(getContext().getApplicationContext(),
                                            MainActivity.class));
                                    getActivity().finish();

                                    } catch (JSONException exception){
                                    exception.printStackTrace();
                                }
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {

                                String errorMessage = "Unknown Error!";

                                // Set the errorMessage based on the error type
                                if (error instanceof NoConnectionError) {
                                    errorMessage = "No Connection";
                                } else if (error instanceof TimeoutError) {
                                    errorMessage = "Server took too long to respond";
                                } else if (error instanceof ServerError) {
                                    errorMessage = "The was a problem with the server";
                                } else if (error instanceof NetworkError) {
                                    errorMessage = "Unknown error with the network";
                                } else if (error instanceof ParseError) {

                                }

                                //  Show error about connection
                                Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                                        .setAction("RETRY", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View view) {
                                                nextBtn.performClick();
                                            }
                                        }).show();

                                // Hide the progress dialog after showing an error message
                                loadingForeground.setVisibility(View.GONE);

                            }
                        }
                );

                // Set request retry policy
                request.setRetryPolicy(
                        new DefaultRetryPolicy(40000,
                                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

                // Disable Volley Cache
                request.setShouldCache(false);

                // Add the request to the request queue
                requestQueue.add(request);


            }
        });



        return view;
    }


    private void setStudyFieldAdapter(){
        final ArrayList<StudyField> studyFields = new ArrayList<>();

        // Show progress dialog
        loadingForeground.setVisibility(View.VISIBLE);


        // Build Request
        JsonArrayRequest request = new JsonArrayRequest(PushUtils.URL_GET_STUDY_FIELDS,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());

                        // Parse JSON Array
                        try {
                            for(int i = 0; i < response.length(); i++){
                                // Get a json object from the list/array
                                JSONObject jsonStudyField = (JSONObject) response.get(i);

                                // Map the json object to StudyField
                                // and add it to the array list
                                studyFields.add(new StudyField(
                                        jsonStudyField.getInt("id"),
                                        jsonStudyField.getString("name"),
                                        jsonStudyField.getString("code"),
                                        jsonStudyField.getInt("years"),
                                        jsonStudyField.getInt("sections")
                                ));
                            }
                        } catch (JSONException exception){
                            exception.printStackTrace();
                            // TODO: Remove toast
                            Toast.makeText(getContext(), "Error in onResponse", Toast.LENGTH_SHORT).show();
                        }

                        // Build Adapter for Spinner
                        ArrayAdapter<StudyField> studyFieldArrayAdapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item,
                                studyFields);
                        studyFieldArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Set adapter for StudyField Spinner
                        studyFieldSpinner.setAdapter(studyFieldArrayAdapter);

                        // Hide the progress layout
                        loadingForeground.setVisibility(View.GONE);

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                String errorMessage = "Unknown Error!";

                // Set the errorMessage based on the error type
                if (error instanceof NoConnectionError) {
                    errorMessage = "No Connection";
                } else if (error instanceof TimeoutError) {
                    errorMessage = "Server took too long to respond";
                } else if (error instanceof ServerError) {
                    errorMessage = "The was a problem with the server";
                } else if (error instanceof NetworkError) {
                    errorMessage = "Unknown error with the network";
                } else if (error instanceof ParseError) {

                }

                //  Show error about connection
                Snackbar.make(getView(), errorMessage, Snackbar.LENGTH_INDEFINITE)
                        .setAction("RETRY", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                setStudyFieldAdapter();
                            }
                        }).show();

                // Hide the progress bar/loading layout
                loadingForeground.setVisibility(View.GONE);
            }
        });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Disable Volley Cache
        request.setShouldCache(false);

        // Add request to request queue
        requestQueue.add(request);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {
        // Get the selected StudyField object
        StudyField studyField = (StudyField)adapterView.getItemAtPosition(pos);

        // Save the selected study field
        editor.putString(PushUtils.SP_STUDY_FIELD_CODE, studyField.code);
        editor.apply();

        // Reveal Year Year layout
        this.view.findViewById(R.id.year_layout).setVisibility(View.VISIBLE);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int id) {
        switch (radioGroup.getId()){
            case R.id.year_rg:
                int selectedYear = 0;
                // Get the selected year and save it to shared preferences
                if (id == R.id.year_option_1){
                    selectedYear = 1;
                } else if (id == R.id.year_option_2){
                    selectedYear = 2;
                } else if (id == R.id.year_option_3){
                    selectedYear = 3;
                } else if (id == R.id.year_option_4){
                    selectedYear = 4;
                }

                // Save
                editor.putInt(PushUtils.SP_SELECTED_YEAR, selectedYear);
                editor.commit();

                // Reveal Section Layout
                this.view.findViewById(R.id.section_layout).setVisibility(View.VISIBLE);

                break;
            case R.id.section_rg:
                int selectedSection = 0;
                // Get the selected section and save it to shared preferences
                if (id == R.id.section_toggle_1){
                    selectedSection = 1;
                } else if (id == R.id.section_toggle_2){
                    selectedSection = 2;
                } else if (id == R.id.section_toggle_3){
                    selectedSection = 3;
                }

                // Save
                editor.putInt(PushUtils.SP_SELECTED_SECTION, selectedSection);
                editor.commit();

                // Reveal next button
                this.view.findViewById(R.id.next_button).setVisibility(View.VISIBLE);

                break;
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {


        } else {
            Snackbar.make(view, "You need to accept the permissions",
                    Snackbar.LENGTH_INDEFINITE).
                    setAction("TRY AGAIN", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Request Permission
                    requestPermissions(
                            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            222
                    );
                }
            }).show();
        }
    }

    class StudyField{

        int id;
        String name;
        String code;
        int years;
        int sections;

        public StudyField(int id, String name, String code, int years, int sections) {
            this.id = id;
            this.name = name;
            this.code = code;
            this.years = years;
            this.sections = sections;
        }

        @Override
        public String toString(){
            return name;
        }
    }

}
