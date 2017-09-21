package com.aaupush.aaupush;

import android.content.Context;
import android.util.Log;
import android.util.TypedValue;

import java.util.Calendar;
import java.util.Locale;

/**
 * Created by djang on 7/26/17.
 * A utility class for things that don't fit in any other class
 */

public class PushUtils {

    // SharedPreferences Constants
    public static final String SP_KEY_NAME = "aau_push_pref";   // SharedPreferences name
    public static final String SP_IS_FIRST_RUN = "is_first_run";
    public static final String SP_SELECTED_YEAR = "selected_year";
    public static final String SP_SELECTED_SECTION = "selected_section";
    public static final String SP_LAST_ANNOUNCEMENT_RECEIVED_ID = "last_announcement_id";
    public static final String SP_STUDY_FIELD_CODE = "study_field_code";
    public static final String SP_IS_ANNOUNCEMENT_FRAGMENT_RUNNING = "is_announcement_fragment_running";
    public static final String SP_IS_MATERIAL_FRAGMENT_RUNNING = "is_material_fragment_running";
    public static final String SP_NOTIFICATION_COUNTER = "notification_counter";
    public static final String SP_LAST_MATERIAL_RECEIVED_ID = "last_material_id";
    public static final String SP_MATERIAL_LAST_CHECKED = "material_last_checked";
    public static final String SP_ANNOUNCEMENT_LAST_CHECKED = "announcement_last_checked";
    public static final String SP_SECTION_CODE = "section_code";
    public static final String SP_STUDY_FIELD_ID = "study_field_id";
    public static final String SP_NOTIFICATION_ENABLED = "notification_enabled";
    public static final String SP_ANNOUNCEMENT_NOTIFICATION_ENABLED = "announcement_notification_enabled";
    public static final String SP_MATERIAL_NOTIFICATION_ENABLED = "material_notification_enabled";
    public static final String SP_BACKGROUND_REFRESH_INTERVAL = "background_refresh_interval";

    // BroadCast
    static final String NEW_ANNOUNCEMENT_BROADCAST = "com.aaupush.aaupush.NEW_ANNOUNCEMENT";
    static final String NEW_MATERIAL_BROADCAST = "com.aaupush.aaupush.NEW_MATERIAL";
    static final String ON_FOLDER_CLICK_BROADCAST = "com.aaupush.aaupush.ON_FOLDER_CLICK";
    static final String BACK_PRESSED_ON_FOLDER_VIEW_BROADCAST = "com.aaupush.aaupush.BACK_PRESSED_OFV";
    static final String MATERIAL_DOWNLOAD_STARTED_BROADCAST = "com.aaupush.aaupush.MATERIAL_DOWNLOAD_STARTED";
    static final String MATERIAL_DOWNLOAD_COMPLETED_BROADCAST = "com.aaupush.aaupush.MATERIAL_DOWNLOAD_COMPLETED";
    static final String NO_CONNECTION_BROADCAST = "com.aaupush.aaupush.NO_CONNECTION";
    static final String CONNECTION_TIMEOUT_BROADCAST = "com.aaupush.aaupush.CONNECTION_TIMEOUT";
    static final String ANNOUNCEMENT_REFRESHED_BROADCAST = "com.aaupush.aaupush.ANNOUNCEMENT_REFRESHED";
    static final String MATERIAL_REFRESHED_BROADCAST = "com.aaupush.aaupush.MATERIAL_REFRESHED";
    static final String ANNOUNCEMENT_REFRESH_REQUEST_BROADCAST = "com.aaupush.aaupush.ANNOUNCEMENT_REFRESH_REQUEST_BROADCAST";
    static final String MATERIAL_REFRESH_REQUEST_BROADCAST = "com.aaupush.aaupush.MATERIAL_REFRESH_REQUEST_BROADCAST";
    public static String CLICKED_ON_SECTION_BROADCAST = "com.aaupush.aaupush.CLICKED_ON_SECTION";
    public static final String COURSE_LIST_REFRESHED_BROADCAST = "com.aaupush.aaupush.COURSE_LIST_REFRESHED";

    // API Request URL Parameter Names
    public static final String API_PARAMS_COURSES_STUDY_FIELD = "study_field";
    public static final String API_PARAMS_COURSES_SECTION = "section";
    public static final String API_PARAMS_SECTIONS_STUDY_FIELD_ID = "study_field_id";
    public static final String API_PARAMS_ANNOUNCEMENTS_COURSE_SECTION = "course_section";
    public static final String API_PARAMS_MATERIALS_COURSE_SECTION = "course_section";
    public static final String API_PARAMS_MATERIALS_ID = "id";

    // API Request URLs
    /*// Local
    // Returns list of study fields
    public static final String URL_GET_STUDY_FIELDS = "http://10.0.2.2:8000/api/study_fields";
    // List of announcements
    public static final String URL_GET_ANNOUNCEMENTS = "http://10.0.2.2:8000/api/announcements/?";
    // List of materials
    public static final String URL_GET_MATERIALS = "http://10.0.2.2:8000/api/materials/?";
    // List of courses withing a given study field and section
    public static final String URL_GET_COURSES = "http://10.0.2.2:8000/api/courses/?";
    // Check if a given section exists
    public static final String URL_SECTION_EXISTS = "http://10.0.2.2:8000/api/section_exists/?";
    // Get a list of sections for a given study field
    public static final String URL_GET_SECTIONS = "http://10.0.2.2:8000/api/sections/?";
    // Download a file
    public static final String URL_DOWNLOAD = "http://10.0.2.2:8000/file/";*/

    // Online
    // Returns list of study fields
    public static final String URL_GET_STUDY_FIELDS = "http://aaupush.pythonanywhere.com/api/study_fields";
    // List of announcements
    public static final String URL_GET_ANNOUNCEMENTS = "http://aaupush.pythonanywhere.com/api/announcements/?";
    // List of materials
    public static final String URL_GET_MATERIALS = "http://aaupush.pythonanywhere.com/api/materials/?";
    // List of courses withing a given study field and section
    public static final String URL_GET_COURSES = "http://aaupush.pythonanywhere.com/api/courses/?";
    // Check if a given section exists
    public static final String URL_SECTION_EXISTS = "http://aaupush.pythonanywhere.com/api/section_exists/?";
    // Get a list of sections for a given study field
    public static final String URL_GET_SECTIONS = "http://aaupush.pythonanywhere.com/api/sections/?";
    // Download a file
    public static final String URL_DOWNLOAD = "http://aaupush.pythonanywhere.com/file/";

    // Folder name
    public static final String ROOT_FOLDER = "AAUPush";


    /**
     * Append GET parameters on URLs
     * @param name  parameter name to append
     * @param value parameter value to append
     * @param url the url on which the parameters are appended
     * @return url with parameter appended
     */
    public static String appendGetParameter(String name, String value, String url){
        return url + name + "=" + value + "&";
    }

    /**
     * Convert a {@link Calendar} instance into a String with the format
     * yyyymmdd or yyyymmddhhmm if time is included
     * @param calendar Calendar instance to be converted
     * @param withTime should the converted string contain the time as well
     * @return the formatted string either with the format of yyyymmdd or yyyymmddhhmm
     */
    static public String calendarToString(Calendar calendar, boolean withTime){
        String date;
        date =  Integer.toString(calendar.get(Calendar.YEAR));
        date += String.format(Locale.ENGLISH, "%02d", 1 +  calendar.get(Calendar.MONTH));
        date += String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.DAY_OF_MONTH));
        if ( !withTime) {
            return date;
        }
        date += String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.HOUR_OF_DAY));
        date += String.format(Locale.ENGLISH, "%02d", calendar.get(Calendar.MINUTE));
        return date;
    }

    /**
     * Converts a String which contains a date with the format
     * yyyymmddhhmm into a {@link Calendar} object
     * eg. 201712301230
     * @param date the date to be converted
     * @param hasTime indicate if the String include a time in addition to the date
     * @return Calendar instance of the converted string
     */
    static public Calendar stringToCalendar (String date, boolean hasTime ){
        try {
            Calendar calendar = Calendar.getInstance();
            int year = Integer.parseInt(date.substring(0, 4));
            int month = Integer.parseInt(date.substring(4, 6)) - 1;
            int day = Integer.parseInt(date.substring(6, 8));
            if (!hasTime) {
                calendar.set(year, month, day);
                return calendar;
            }
            int hour = Integer.parseInt(date.substring(8, 10));
            int minute = Integer.parseInt(date.substring(10, 12));
            calendar.set(year, month, day, hour, minute);
            return calendar;
        } catch (Exception e){
            Log.e("String To Calendar", "Error Parsing date " + date + " " + e.getMessage());
            return stringToCalendar(date, false);
        }
    }

    /**
     * Converts a given SP value to its corresponding pixels value based on the display metrics
     * @param sp the sp value to be converted
     * @param context Context instance to retrieve the display metrics
     * @return the converted value in pixels
     */
    public static int convertSpToPixels(float sp, Context context) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                sp, context.getResources().getDisplayMetrics());
    }
}
