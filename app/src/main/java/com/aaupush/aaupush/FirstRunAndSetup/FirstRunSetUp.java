package com.aaupush.aaupush.FirstRunAndSetup;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.Toast;

import com.aaupush.aaupush.DBHelper;
import com.aaupush.aaupush.MainActivity;
import com.aaupush.aaupush.PushUtils;
import com.aaupush.aaupush.R;
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

import java.util.ArrayList;
import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 */
public class FirstRunSetUp extends Fragment implements AdapterView.OnItemSelectedListener{

    RequestQueue requestQueue;

    static final String TAG = "FirstRunSetUpFragment";


    // UI Elements
    Spinner studyFieldSpinner;
    Spinner departmentSpinner;

    // Spinner Holding LinearLayout s
    LinearLayout studyFieldLayout;
    LinearLayout departmentLayout;

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

        // Init Spinner for Department
        departmentSpinner = (Spinner) view.findViewById(R.id.department_spinner);
        departmentSpinner.setOnItemSelectedListener(this);
        setDepartmentSpinnerAdapter();

        // Init Spinner for Study Fields
        studyFieldSpinner = (Spinner) view.findViewById(R.id.study_field_spinner);
        studyFieldSpinner.setOnItemSelectedListener(this);
        //setStudyFieldAdapter();

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

                // Check if entered section exists
                checkIfSectionExists(enteredYear, enteredSection);

            }
        });



        return view;
    }


    /**
     * Set an adapter for the study field spinner with a list of study fields from a given ID.
     * @param departmentId the department from which the study fields come from.
     */
    private void setStudyFieldAdapter(final int departmentId){
        final ArrayList<StudyField> studyFields = new ArrayList<>();

        // Show progress dialog
        loadingForeground.setVisibility(View.VISIBLE);


        // Build Request
        // Build Request URL
        String url = PushUtils.URL_GET_STUDY_FIELDS;
        url = PushUtils.appendGetParameter(PushUtils.API_PARAMS_SECTIONS_ID, "" + departmentId, url);
        JsonArrayRequest request = new JsonArrayRequest(url,
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
                                setStudyFieldAdapter(departmentId);
                            }
                        }).show();

                // Hide the progress bar/loading layout
                loadingForeground.setVisibility(View.GONE);
            }
        });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Disable Volley Cache
        request.setShouldCache(false);

        // Add request to request queue
        requestQueue.add(request);
    }

    /**
     * Set an adapter for the departments spinner with a list of available departments.
     */
    private void setDepartmentSpinnerAdapter() {
        final ArrayList<Department> departments = new ArrayList<>();

        // Show progress dialog
        loadingForeground.setVisibility(View.VISIBLE);

        // Build the json request
        JsonArrayRequest request = new JsonArrayRequest(PushUtils.URL_GET_DEPARTMENTS,
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
                                departments.add(new Department(
                                        Integer.valueOf(jsonStudyField.getString("id")),
                                        jsonStudyField.getString("name"),
                                        jsonStudyField.getString("code")
                                ));
                            }
                        } catch (JSONException exception){
                            exception.printStackTrace();
                        }

                        // Build Adapter for Spinner
                        ArrayAdapter<Department> studyFieldArrayAdapter = new ArrayAdapter<>(getContext(),
                                android.R.layout.simple_spinner_item,
                                departments);
                        studyFieldArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        // Set adapter for StudyField Spinner
                        departmentSpinner.setAdapter(studyFieldArrayAdapter);

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
                                setDepartmentSpinnerAdapter();
                            }
                        }).show();

                // Hide the progress bar/loading layout
                loadingForeground.setVisibility(View.GONE);
            }
        });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Disable Volley Cache
        request.setShouldCache(false);

        // Add request to request queue
        requestQueue.add(request);
    }

    /**
     * Add the default courses to the db
     */
    private void addDefaultCourses() {
        // Show progress bar
        loadingForeground.setVisibility(View.VISIBLE);

        // Make JSONArrayRequest and get the list of courses
        // Base request URL
        String url = PushUtils.URL_GET_COURSES;
        final int sectionId = preferences.getInt(PushUtils.SP_SECTION_ID, 1);

        // Append GET parameters
        url = PushUtils.appendGetParameter(PushUtils.API_PARAMS_COURSES_SECTIONS, sectionId + "", url);
        //url = PushUtils.appendGetParameter(PushUtils.API_PARAMS_COURSES_STUDY_FIELD, studyFieldID + "", url);

        // Output get course url to log
        Log.d(TAG, "Url: " + url);

        // Build the request
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Output the response to the log
                        Log.d(TAG, response.toString());

                        // Parse the JSON Array
                        try {
                            // Check if the array is not empty
                            if (response.length() < 1){
                                Log.d(TAG, "Request returned empty JSON Array");

                                // Show Error Message
                                Snackbar.make(view,
                                        "This section does not contain any courses. Go back and choose a different section",
                                        Snackbar.LENGTH_INDEFINITE).show();

                                return; // Exit if array is empty
                            }

                            // Init DBHelper Object
                            DBHelper dbHelper = new DBHelper(getContext().getApplicationContext());

                            // Loop through every JSON object in the array
                            for (int i = 0; i < response.length(); i++){
                                // Get a JSON object from the array
                                JSONObject json = (JSONObject) response.get(i);

                                // Add the course to the fragment
                                dbHelper.addCourse(json.getInt("id"), json.getString("name"), String.valueOf(sectionId));
                            }

                            // Save state about finishing FirstRun and Setup
                            preferences.edit().putBoolean(PushUtils.SP_IS_FIRST_RUN, false).apply();

                            // Start MainActivity
                            startActivity(new Intent(getContext(), MainActivity.class));


                            // Hide progress bar
                            loadingForeground.setVisibility(View.GONE);



                        } catch (JSONException exception){
                            exception.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
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
                            errorMessage = "There was a problem with the server";
                        } else if (error instanceof NetworkError) {
                            errorMessage = "Unknown error with the network";
                        } else if (error instanceof ParseError) {

                        }

                        Log.e(TAG, "VolleyError in setCourseAdapter: " + errorMessage);

                        Snackbar.make(view, errorMessage, Snackbar.LENGTH_INDEFINITE)
                                .setAction("RETRY", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        addDefaultCourses();
                                    }
                                }).show();

                        // Hide progress bar
                        loadingForeground.setVisibility(View.GONE);
                    }
                });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Disable Volley Cache
        request.setShouldCache(false);

        // Add to the request queue
        requestQueue.add(request);
    }

    private void checkIfSectionExists(final String year, final String section) {
        // Get saved department id
        int departmentId = preferences.getInt(PushUtils.SP_DEPARTMENT_ID, -1);

        // Get the list of sections within the given department
        // and check if the inputted section exists

        // Build the request url
        String url = PushUtils.URL_GET_SECTIONS;
        url = PushUtils.appendGetParameter(PushUtils.API_PARAMS_DEPARTMENT, String.valueOf(departmentId), url);

        // Build the Volley Request
        JsonArrayRequest request = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        Log.d(TAG, response.toString());
                        // Section found flag
                        boolean isSectionFound = false;

                        // Parse JSON Array
                        try {
                            // Output the response to the log
                            Log.d(TAG, "Get Sections API Response: \n" + response);

                            for(int i = 0; i < response.length(); i++){

                                // Get a json object from the list/array
                                JSONObject jsonSection = (JSONObject) response.get(i);

                                if (jsonSection.getString("code").contains("Y"+year) &&
                                        jsonSection.getString("code").contains("S"+section)) {
                                    // Output the selected section to the log
                                    Log.d(TAG, "Selected Section: " + jsonSection.getString("code"));

                                    // Save the section code and id
                                    editor.putString(PushUtils.SP_SECTION_CODE, jsonSection.getString("code"));
                                    editor.putInt(PushUtils.SP_SECTION_ID, jsonSection.getInt("id"));
                                    editor.apply();

                                    // Set the section found flag to true
                                    isSectionFound = true;
                                }

                            }
                        } catch (JSONException exception){
                            exception.printStackTrace();
                        }

                        // Check the sectionFound flag
                        if (isSectionFound) {
                            // Add the default courses in this section
                            addDefaultCourses();
                        } else {
                            // Selected section was not found
                            //TODO : Change error message from Toast to Snackbar
                            Toast.makeText(
                                    getContext(),
                                    "The Year-Section combination entered is wrong.",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }

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
                                setDepartmentSpinnerAdapter();
                            }
                        }).show();

                // Hide the progress bar/loading layout
                loadingForeground.setVisibility(View.GONE);
            }
        });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(10000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Disable Volley Cache
        request.setShouldCache(false);

        // Add request to request queue
        requestQueue.add(request);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int pos, long l) {

        if (adapterView.getId() == R.id.department_spinner) {
            // Get the selected department from the spinner
            Department department = (Department)adapterView.getItemAtPosition(pos);

            // Save the department code of the selected department to the shared preferences
            editor.putString(PushUtils.SP_DEPARTMENT_CODE, department.getCode());
            editor.putInt(PushUtils.SP_DEPARTMENT_ID, department.getId());
            editor.apply();

            // Reveal Year and Section layout
            this.view.findViewById(R.id.year_layout).setVisibility(View.VISIBLE);
            this.view.findViewById(R.id.section_layout).setVisibility(View.VISIBLE);

            //setStudyFieldAdapter(((Department)adapterView.getItemAtPosition(pos)).getId());
            //this.view.findViewById(R.id.study_field_layout).setVisibility(View.VISIBLE);
        } else {
            // Get the selected StudyField object
            StudyField studyField = (StudyField)adapterView.getItemAtPosition(pos);

            // Save the selected study field
            editor.putInt(PushUtils.SP_STUDY_FIELD_ID, studyField.id);
            editor.putString(PushUtils.SP_STUDY_FIELD_CODE, studyField.code);
            editor.apply();

            // Reveal Year and Section layout
            this.view.findViewById(R.id.year_layout).setVisibility(View.VISIBLE);
            this.view.findViewById(R.id.section_layout).setVisibility(View.VISIBLE);
        }

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

    class Department {
        int id;
        String name;
        String code;

        public Department(int id, String name, String code) {
            this.id = id;
            this.name = name;
            this.code = code;
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getCode() { return code; }

        @Override
        public String toString() {
            return name;
        }
    }

}
