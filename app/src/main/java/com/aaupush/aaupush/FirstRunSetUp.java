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
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.security.Permission;
import java.util.ArrayList;
import java.util.Locale;
import java.util.ServiceConfigurationError;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstRunSetUp extends Fragment implements AdapterView.OnItemSelectedListener{

    RequestQueue requestQueue;

    static final String TAG = "FirstRunSetUpFragment";


    // UI Elements
    Spinner studyFieldSpinner;

    // Loading Layout
    LinearLayout loadingForeground;

    //SharedPreference
    SharedPreferences preferences;
    SharedPreferences.Editor editor;

    // Edit text layouts
    TextInputLayout sectionInputLayout;
    TextInputLayout yearInputLayout;

    // Main view
    View view;


    public FirstRunSetUp() {
        // Required empty public constructor
    }

    public static FirstRunSetUp newInstance(){
        return new FirstRunSetUp();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_set_up, container, false);

        // Init SharedPreferences and Editor
        preferences = getContext().getSharedPreferences(PushUtils.SP_KEY_NAME, Context.MODE_PRIVATE);
        editor = preferences.edit();

        // TextInputLayout
        sectionInputLayout = (TextInputLayout) view.findViewById(R.id.section_layout);
        yearInputLayout = (TextInputLayout) view.findViewById(R.id.year_layout);
        sectionInputLayout.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                view.findViewById(R.id.next_button).setVisibility(View.VISIBLE);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        // Loading layout
        loadingForeground = (LinearLayout) view.findViewById(R.id.loading_foreground);

        requestQueue = Volley.newRequestQueue(getContext());

        // Init Spinner for Study Fields
        studyFieldSpinner = (Spinner) view.findViewById(R.id.studyFieldSpinner);
        studyFieldSpinner.setOnItemSelectedListener(this);
        setStudyFieldAdapter();

        final Button nextBtn = (Button) view.findViewById(R.id.next_button);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View view) {

                // Show the loading layout/progress bar
                loadingForeground.setVisibility(View.VISIBLE);

                // TODO: Check if correct section and year are selected
                String enteredSection = sectionInputLayout.getEditText()
                        .getText().toString().replaceAll(" ", "");
                String enteredYear = yearInputLayout.getEditText()
                        .getText().toString().replaceAll(" ", "");

                Log.d(TAG, "Section " + enteredSection);
                Log.d(TAG, "Year " + enteredYear);


                if (enteredSection.equals("") || enteredYear.equals("")) {
                    if (enteredSection.equals("")) {
                        sectionInputLayout.setError("Enter a number.");
                    }

                    if (enteredYear.equals("")) {
                        yearInputLayout.setError("Enter a number.");
                    }

                    loadingForeground.setVisibility(View.GONE);
                    return;
                }

                try {
                    Integer.parseInt(enteredSection);
                    Integer.parseInt(enteredYear);
                } catch (Exception e) {
                    Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                    loadingForeground.setVisibility(View.GONE);
                    return;
                }

                // Get the list of course and add them to the db
                // Build the request URL
                String url = PushUtils.URL_SECTION_EXISTS;

                // Get the study field of the student
                String studyField = preferences.getString(PushUtils.SP_STUDY_FIELD_CODE, "CS");

                // Build Section code
                String sectionCode = String.format(Locale.ENGLISH, "%sY%sS%s", studyField.toUpperCase(), enteredYear, enteredSection);

                editor.putString(PushUtils.SP_SECTION_CODE, sectionCode);
                editor.apply();

                // Year of the student
                final int year = preferences.getInt(PushUtils.SP_SELECTED_YEAR, 1);

                // Append GET parameters
                url = PushUtils.appendGetParameter("section_code", sectionCode, url);

                // Build the json array request
                StringRequest request = new StringRequest(
                        url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                // Output the response to log
                                if (response.equals("true")) {
                                    // TODO: Go to course selection fragment
                                    Toast.makeText(getContext(), "Success", Toast.LENGTH_SHORT).show();
                                } else {
                                    // Output error about year section combination not existing
                                    Snackbar.make(view,
                                            "The Year-Section combination you chose does not exist",
                                            Snackbar.LENGTH_LONG).show();
                                    loadingForeground.setVisibility(View.GONE);
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
                                        jsonStudyField.getString("code")
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

        // Reveal Year and Section layout
        this.view.findViewById(R.id.year_layout).setVisibility(View.VISIBLE);
        this.view.findViewById(R.id.section_layout).setVisibility(View.VISIBLE);

    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

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

        public StudyField(int id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        @Override
        public String toString(){
            return name;
        }
    }

}
