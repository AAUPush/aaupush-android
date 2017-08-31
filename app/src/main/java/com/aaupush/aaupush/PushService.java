package com.aaupush.aaupush;

import android.app.AlarmManager;
import android.app.DownloadManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
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
import java.net.URI;
import java.util.ArrayList;

public class PushService extends Service {

    private static final String TAG = "PushService";

    // Interval the service should run on
    private static final long SERVICE_REFRESH_MS = 600000;

    // RequestQueue for the whole application
    public RequestQueue requestQueue;

    // SharedPreferences
    SharedPreferences preferences;


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                Log.d(TAG, "Download complete broadcast received");

                updateDownloadStatus();

            }
            else if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                // Broadcast received after change in connectivity
                Log.d(TAG, "Connection change broadcast received");

                // Get details about the changes
                ConnectivityManager connectivityManager =
                        (ConnectivityManager)getApplicationContext()
                                .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
                boolean isConnectedSuccess = networkInfo !=null && networkInfo.isConnected();
                boolean isOnWiFi = (networkInfo != null ? networkInfo.getType() : 0) == ConnectivityManager.TYPE_WIFI;
                boolean isOnData = (networkInfo != null ? networkInfo.getType() : 0) == ConnectivityManager.TYPE_MOBILE;

                // If the changes were positive(going from disconnected to connected)
                // update materials and announcements
                if (isConnectedSuccess && (isOnWiFi || isOnData)) {
                    refreshAnnouncements();
                    refreshMaterials();
                }

            }
        }
    };

    public PushService() {
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "PushService Created");

        // Init requestQueue instance
        requestQueue = Volley.newRequestQueue(getApplicationContext());

        // Init SharedPreferences
        preferences = getSharedPreferences(PushUtils.SP_KEY_NAME, MODE_PRIVATE);


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Refresh announcements
        refreshAnnouncements();

        // Refresh Materials
        refreshMaterials();

        // Schedule for the service to run again
        setNextServiceRunAlarm();

        // Update download status
        updateDownloadStatus();

        // Register broadcast receiver
        IntentFilter broadcastFilters = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        broadcastFilters.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(broadcastReceiver, broadcastFilters);

        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /**
     * When this is called the method makes a JSON request to the server for new announcements.
     * If there are new announcements it adds them to the local database and sends appropriate
     * broadcast to notify any one listening about new announcements.
     * </p>
     * An announcement is classified as new if its ID is greater than the one the app received on
     * the last request.
     */
    private void refreshAnnouncements(){
        Log.d(TAG, "Checking for new announcements...");

        // Get the last announcement id that the device received
        // so that we can request for new announcement with IDs
        // greater that this
        int lastAnnouncementID = preferences.getInt(PushUtils.SP_LAST_ANNOUNCEMENT_RECEIVED_ID, 0);
        Log.d(TAG, "Last Announcement ID - " + lastAnnouncementID);

        // Study field of the student, used for requesting
        // announcements
        String studyField = preferences.getString(PushUtils.SP_STUDY_FIELD_CODE, "CS");

        // Section of the student
        int section = preferences.getInt(PushUtils.SP_SELECTED_SECTION, 1);

        // Year of the student
        int year = preferences.getInt(PushUtils.SP_SELECTED_YEAR, 1);

        // Build the request url
        String url = PushUtils.URL_GET_ANNOUNCEMENTS;

        // append parameters
        url = PushUtils.appendGetParameter("study_field", studyField, url);
        url = PushUtils.appendGetParameter("id", String.valueOf(lastAnnouncementID), url);
        url = PushUtils.appendGetParameter("year", String.valueOf(year), url);
        url = PushUtils.appendGetParameter("section", String.valueOf(section), url);

        Log.d(TAG, "Ann Request URL - " + url);

        // Build the JSONArrayRequest
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
                                return; // Exit if array is empty
                            }

                            // Init DB object
                            DBHelper dbHelper = new DBHelper(getApplicationContext());

                            // Loop through every JSON object in the array
                            for (int i = 0; i < response.length(); i++){
                                // Get a JSON object from the array
                                JSONObject json = (JSONObject) response.get(i);

                                // Map the JSON object to an Announcement object

                                // Parse postDate and expDate to long values
                                long postDate = Long.parseLong(json.getString("post_date")
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll(":", ""));
                                long expDate = Long.parseLong(json.getString("exp_date")
                                        .replaceAll(" ", "")
                                        .replaceAll("-", "")
                                        .replaceAll(":", ""));

                                try {
                                    // Cut the second values from the dates
                                    postDate = Long.parseLong(String.valueOf(postDate).substring(0, 12));
                                    expDate = Long.parseLong(String.valueOf(expDate).substring(0, 12));
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e(TAG, "Date parsing exception");
                                }


                                String announcementText = json.getString("announcement");
                                String lecturerName = json.getString("lecturer_name");

                                Announcement announcement = new Announcement(
                                        json.getInt("id"),
                                        announcementText,
                                        lecturerName,
                                        postDate,
                                        expDate,
                                        json.getInt("section"),
                                        json.getInt("year"),
                                        json.getString("is_urgent").equals("1"),
                                        false
                                );

                                // Add the announcement to the database
                                dbHelper.addAnnouncement(announcement);

                                // Save the ID of the last announcement
                                if (i == response.length() - 1){
                                    SharedPreferences.Editor editor = preferences.edit();
                                    editor.putInt(PushUtils.SP_LAST_ANNOUNCEMENT_RECEIVED_ID,
                                            json.getInt("id"));
                                    editor.apply();
                                }

                                // Raise notification on each announcement
                                // if AnnouncementFragment is not running and
                                // the number of announcements is less that 3
                                if (!preferences.getBoolean(PushUtils.SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING, false)
                                        && response.length() < 3){
                                    raiseAnnouncementNotification(
                                            "Announcement From " +
                                                    lecturerName,
                                            announcementText);
                                }

                            }

                            // Send a broadcast to notify any listeners about
                            // a new announcement/s
                            Intent newAnnouncementBroadcast = new Intent(PushUtils.NEW_ANNOUNCEMENT_BROADCAST);
                            getApplicationContext().sendBroadcast(newAnnouncementBroadcast);

                            // Show a summary notification if announcement fragment not running
                            // and the number of new announcements is greater that 2
                            if (!preferences.getBoolean(PushUtils.SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING, false)
                                    && response.length() > 2){
                                raiseAnnouncementNotification(
                                        "AAU Push",
                                        response.length() + " new announcements");
                            }

                            // Close db object
                            dbHelper.close();



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
                            sendBroadcast(new Intent(PushUtils.NO_CONNECTION_BROADCAST));
                            errorMessage = "No Connection";
                        } else if (error instanceof TimeoutError) {
                            errorMessage = "Server took too long to respond";
                        } else if (error instanceof ServerError) {
                            errorMessage = "The was a problem with the server";
                        } else if (error instanceof NetworkError) {
                            errorMessage = "Unknown error with the network";
                        } else if (error instanceof ParseError) {

                        }

                        Log.e(TAG, "VolleyError in refreshAnnouncement: " + errorMessage);
                    }
                });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));


        // Add the request to the request queue
        requestQueue.add(request);
    }

    /**
     * When this is called the method makes a JSON request to the server for new materials.
     * If there are new materials it adds them to the local database and sends the appropriate
     * broadcast to notify any one listening about new materials.
     * </p>
     * A material is classified as new if its ID is greater than the one the app received on the
     * last request.
     */
    private void refreshMaterials() {
        Log.d(TAG, "Checking for new materials");

        // Get the last material id that was downloaded
        int lastMaterialID = preferences.getInt(PushUtils.SP_LAST_MATERIAL_RECEIVED_ID, 0);

        // Get the study field of the student
        String studyField = preferences.getString(PushUtils.SP_STUDY_FIELD_CODE, "CS");

        // Section of the student
        int section = preferences.getInt(PushUtils.SP_SELECTED_SECTION, 1);

        // Year of the student
        int year = preferences.getInt(PushUtils.SP_SELECTED_YEAR, 1);

        // Build the request url
        String url = PushUtils.URL_GET_MATERIALS;

        // Append GET Materials
        url = PushUtils.appendGetParameter("study_field", studyField, url);
        url = PushUtils.appendGetParameter("year", year + "", url);
        url = PushUtils.appendGetParameter("section", section + "", url);
        url = PushUtils.appendGetParameter("file_id", lastMaterialID + "", url);

        // Build the JSONArray request
        JsonArrayRequest request = new JsonArrayRequest(
            url,
            new Response.Listener<JSONArray>() {
                @Override
                public void onResponse(JSONArray response) {
                    // Output the response
                    Log.d(TAG, response.toString());

                    // Parse the JSON Array
                    try {
                        // Check if the array is not empty
                        if (response.length() < 1){
                            Log.d(TAG, "MaterialRequest returned empty JSON Array");
                            return; // Exit if array is empty
                        }

                        // Init DB object
                        DBHelper dbHelper = new DBHelper(getApplicationContext());

                        // Loop through every JSON object in the array
                        for (int i = 0; i < response.length(); i++) {
                            // Get a JSON object from the array
                            JSONObject json = (JSONObject) response.get(i);

                            // Map the JSON object to a Material object and add it to the db
                            // Get the published date
                            long publishedDate = Long.parseLong(json.getString("pub_date")
                                    .replaceAll(" ", "")
                                    .replaceAll("-", "")
                                    .replaceAll(":", ""));

                            dbHelper.addMaterial(new Material(
                                    json.getInt("id"),
                                    json.getString("name"),
                                    json.getString("description"),
                                    json.getString("file_format"),
                                    Material.MATERIAL_NOT_AVAILABLE,
                                    json.getInt("course_id"),
                                    publishedDate
                            ));

                            // Save the ID of the last material
                            if (i == response.length() - 1){
                                SharedPreferences.Editor editor = preferences.edit();
                                editor.putInt(PushUtils.SP_LAST_MATERIAL_RECEIVED_ID,
                                        json.getInt("id"));
                                editor.apply();
                            }


                            // TODO: Raise notification

                        }

                        // Send a broadcast to notify any listeners about
                        // a new material/s
                        Intent newMaterialBroadcast = new Intent(PushUtils.NEW_MATERIAL_BROADCAST);
                        getApplicationContext().sendBroadcast(newMaterialBroadcast);


                        // Raise notification if fragment is not running
                        if (!preferences.getBoolean(PushUtils.SP_IS_MATERIAL_FRAGMENT_RUNNING, false)){
                            raiseAnnouncementNotification(
                                    "New Files Uploaded",
                                    "A new course materials has just been uploaded. Check it out.");
                        }


                        // Close DB object
                        dbHelper.close();



                    } catch (JSONException exception) {
                        exception.printStackTrace();
                    }
                }
            }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();

                String errorMessage = "Unknown Error!";

                // Set the errorMessage based on the error type
                if (error instanceof NoConnectionError) {
                    sendBroadcast(new Intent(PushUtils.NO_CONNECTION_BROADCAST));
                    errorMessage = "No Connection";
                } else if (error instanceof TimeoutError) {
                    errorMessage = "Server took too long to respond";
                } else if (error instanceof ServerError) {
                    errorMessage = "The was a problem with the server";
                } else if (error instanceof NetworkError) {
                    errorMessage = "Unknown error with the network";
                } else if (error instanceof ParseError) {

                }

                Log.e(TAG, "VolleyError in refreshMaterials: " + errorMessage);
            }
        });

        // Set request retry policy
        request.setRetryPolicy(
                new DefaultRetryPolicy(60000,
                        DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                        DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));

        // Add the request to the request queue
        requestQueue.add(request);

    }

    /**
     * The service should run endlessly in a given interval. This method schedules the next time the
     * service should run
     */
    private void setNextServiceRunAlarm() {
        AlarmManager alarmManager = (AlarmManager) getApplicationContext()
                .getSystemService(Context.ALARM_SERVICE);
        Intent startServiceIntent = new Intent(this, PushService.class);
        PendingIntent pIntent = PendingIntent.getService(this, 1, startServiceIntent, 0);

        // Set the alarm // TODO: refresh time value to something greater
        alarmManager.set(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + SERVICE_REFRESH_MS,
                pIntent);
    }

    /**
     * Easily raises a notification when called. When the notification is clicked on, MainActivity
     * will be started.
     * @param title A text shown as the title of the notification
     * @param message A text shown as the message of the notification
     */
    private void raiseAnnouncementNotification(String title, String message){
        // Pending Intent
        Intent launchActivity = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, launchActivity, 0);

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext());
        builder.setAutoCancel(true).setDefaults(NotificationCompat.DEFAULT_ALL)
                .setContentTitle(title)
                .setContentText(message)
                .setTicker(title).setPriority(0)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setSmallIcon(R.drawable.ic_notification)
                .setContentIntent(pendingIntent);

        // Get notification counter for notification id
        int notificationID = preferences.getInt(PushUtils.SP_NOTIFICATION_COUNTER, 1);

        // Notify
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.notify(notificationID, builder.build());

        // Save a new incremented notification ID for next time
        notificationID++;
        preferences.edit().putInt(PushUtils.SP_NOTIFICATION_COUNTER, notificationID).apply();

    }

    /**
     * Starts the process of the downloading a given material using {@link DownloadManager}.
     * It is a static method and can be called from any place by passing a context.
     * It returns a modified Material object, with download ID and download status.
     * @param material The material to be downloaded
     * @param context Should be value of getApplicationContext
     * @return the modified {@link Material} object.
     */
    static public Material downloadMaterial(Material material, Context context) {
        // URI String only for testing
        String localUri = "http://10.0.2.2/~djang/base.apk";
        String onlineUri = "http://djang.tk/aaupush/api/sample.txt";


        // Get the download manager instance
        DownloadManager downloadManager = (DownloadManager)context.getSystemService(DOWNLOAD_SERVICE);

        // Build file uri
        DBHelper dbHelper = new DBHelper(context);
        Folder courseFolder = dbHelper.getCourseFolder(material.getParentCourseId());
        File file = new File(Environment.getExternalStorageDirectory() +
                "/" + PushUtils.ROOT_FOLDER + "/Y" +
                courseFolder.getCourseYear() + "/" +
                courseFolder.getName() + "/" +
                material.getTitle() + "." + material.getFileFormat()
        );


        DownloadManager.Request request = new DownloadManager.Request(
                Uri.parse(onlineUri));
        request = request.setTitle(material.getTitle())
                .setDescription(material.getDescription())
                .setDestinationUri(Uri.fromFile(file));
        long downloadID = downloadManager.enqueue(request);

        // Update material state
        dbHelper.setMaterialDownloadStatus(
                material.getMaterialId(),
                Material.MATERIAL_DOWNLOADING,
                downloadID, Uri.fromFile(file).toString()
        );

        material.setDownloadID(downloadID);
        material.setOfflineLocation(Uri.fromFile(file).toString());
        material.setAvailableOfflineStatus(Material.MATERIAL_DOWNLOADING);

        // Debug Log
        Log.d(TAG, "Download ID: " + downloadID);
        Log.d(TAG, "Download Location: " + Uri.fromFile(file).toString());

        // Send broadcast to notify about the start of the download
        Intent broadcastIntent = new Intent(PushUtils.MATERIAL_DOWNLOAD_STARTED_BROADCAST);
        context.sendBroadcast(broadcastIntent);

        // Start DownloadListener


        // Return the updated object
        return material;
    }

    /**
     * Queries the database for Materials marked as 'currently downloading' and checks
     * if each of these materials either finished downloading or failed to download.
     * Finally updates the database about their status.
     */
    void updateDownloadStatus() {
        int CHANGE_FLAG = 0;

        // Get our db object, to query it for active downloads
        DBHelper dbHelper = new DBHelper(getApplicationContext());

        // Get all the active downloads
        ArrayList<Material> materials = dbHelper.getDownloadingMaterials();

        // Exit if there are no actively downloading material
        if (materials == null) { return; }

        // Get the download manager service
        DownloadManager downloadManager = (DownloadManager)getSystemService(DOWNLOAD_SERVICE);

        // Loop through every actively downloading material and check its status
        for (Material material: materials) {
            Cursor cursor = downloadManager.query(new DownloadManager.Query().setFilterById(material.getDownloadID()));

            // Check if such download(download id of the material) exists in the downloads db
            if (cursor == null) { continue; }

            cursor.moveToFirst();

            // Get the status of the download
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            int reason = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_REASON));

            if (status == DownloadManager.STATUS_FAILED && reason != DownloadManager.ERROR_FILE_ALREADY_EXISTS) {
                dbHelper.setMaterialDownloadStatus(
                        material.getMaterialId(),
                        Material.MATERIAL_NOT_AVAILABLE,
                        material.getDownloadID(),
                        null
                );
                CHANGE_FLAG = 1;
            } else if (status == DownloadManager.STATUS_SUCCESSFUL) {
                dbHelper.setMaterialDownloadStatus(
                        material.getMaterialId(),
                        Material.MATERIAL_AVAILABLE_OFFLINE,
                        material.getDownloadID(),
                        null
                );
                CHANGE_FLAG = 1;
            } else if (status == DownloadManager.STATUS_FAILED && reason == DownloadManager.ERROR_FILE_ALREADY_EXISTS) {
                dbHelper.setMaterialDownloadStatus(
                        material.getMaterialId(),
                        Material.MATERIAL_AVAILABLE_OFFLINE,
                        material.getDownloadID(),
                        null
                );
                CHANGE_FLAG = 1;
            }

            // Close the cursor
            cursor.close();

        }

        // Close the db object
        dbHelper.close();

        if (CHANGE_FLAG == 1) {
            // Send a broadcast to notify any listeners about
            // a updated material/s
            Intent newMaterialBroadcast = new Intent(PushUtils.MATERIAL_DOWNLOAD_COMPLETED_BROADCAST);
            getApplicationContext().sendBroadcast(newMaterialBroadcast);
        }

    }
}
