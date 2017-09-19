package com.aaupush.aaupush;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;

/**
 * This class handles all local database interactions
 */

public class DBHelper extends SQLiteOpenHelper {
    // Database name and version
    private static final String DATABASE_NAME = "aaupush.db";
    private static final int VERSION = 1;

    // Table: announcement
    static final String ANNOUNCEMENT_TABLE_NAME = "announcement";
    static final String ANNOUNCEMENT_ID = "id";
    static final String ANNOUNCEMENT_ANNOUNCEMENT = "announcement";
    static final String ANNOUNCEMENT_PUB_DATE = "pub_date";
    static final String ANNOUNCEMENT_EXP_DATE = "exp_date";
    static final String ANNOUNCEMENT_LECTURER_NAME = "lecturer_name";
    static final String ANNOUNCEMENT_IS_URGENT = "is_urgent";
    static final String ANNOUNCEMENT_NOTIFIED = "notified";
    static final String ANNOUNCEMENT_SECTION = "section";
    static final String ANNOUNCEMENT_YEAR = "year";

    // Table: course
    static final String COURSE_TABLE_NAME = "Course";
    static final String COURSE_ID = "id";
    static final String COURSE_NAME = "name";
    static final String COURSE_SECTION = "section";

    // Table: Material
    static final String MATERIAL_TABLE_NAME = "Material";
    static final String MATERIAL_ID = "id";
    static final String MATERIAL_NAME = "name";
    static final String MATERIAL_DESC = "desc";
    static final String MATERIAL_PUB_DATE = "pub_date";
    static final String MATERIAL_COURSE_ID = "course_id";
    static final String MATERIAL_FILE_FORMAT = "file_format";
    static final String MATERIAL_AVAILABLE_OFFLINE = "available_offline";
    static final String MATERIAL_OFFLINE_LOCATION = "offline_location";
    static final String MATERIAL_DOWNLOAD_ID = "download_id";

    private static final String TAG = "DBHelper";


    public DBHelper(Context context){
        super(context, DATABASE_NAME, null, VERSION);
    }

    /**
     * Called when the app runs for the first time
     * @param sqLiteDatabase the db to act on
     */
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        // Create our tables here

        // construct the sql statement for creating announcement table
        String createAnnouncementTable = "CREATE TABLE " + ANNOUNCEMENT_TABLE_NAME + "( " +
                ANNOUNCEMENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                ANNOUNCEMENT_ANNOUNCEMENT + " TEXT, " +
                ANNOUNCEMENT_PUB_DATE + " LONG, " +
                ANNOUNCEMENT_EXP_DATE + " LONG, " +
                ANNOUNCEMENT_LECTURER_NAME + " TEXT, " +
                ANNOUNCEMENT_IS_URGENT + " INTEGER);";

        // exec the sql statement
        sqLiteDatabase.execSQL(createAnnouncementTable);

        // sql statement for Course table
        String createCourseTable = "CREATE TABLE " + COURSE_TABLE_NAME + " ( " +
                COURSE_ID + " INTEGER, " +
                COURSE_NAME + " TEXT, " +
                COURSE_SECTION + " TEXT, " +
                "PRIMARY KEY(" + COURSE_ID + ", " + COURSE_SECTION + ")" +
                ");";

        // exec the sql statement
        sqLiteDatabase.execSQL(createCourseTable);


        // SQL Statement for Material table
        String createMaterialTable = "CREATE TABLE " + MATERIAL_TABLE_NAME + "( " +
                MATERIAL_ID + " INTEGER PRIMARY KEY, " +
                MATERIAL_NAME + " TEXT, " +
                MATERIAL_DESC + " TEXT, " +
                MATERIAL_PUB_DATE + " LONG, " +
                MATERIAL_COURSE_ID + " INTEGER, " +
                MATERIAL_FILE_FORMAT + " TEXT, " +
                MATERIAL_AVAILABLE_OFFLINE + " INTEGER, " +
                MATERIAL_OFFLINE_LOCATION + " TEXT, " +
                MATERIAL_DOWNLOAD_ID + " LONG);";

        // exec the SQL statement
        sqLiteDatabase.execSQL(createMaterialTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }


    /**
     * Adds an announcement to the database
     * @param announcement the announcement to be added
     */
    public void addAnnouncement(Announcement announcement){
        // Get a writable database instance
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //
        contentValues.put(ANNOUNCEMENT_ID, announcement.getId());
        contentValues.put(ANNOUNCEMENT_ANNOUNCEMENT, announcement.getAnnouncement());
        contentValues.put(ANNOUNCEMENT_PUB_DATE, announcement.getPostDate());
        contentValues.put(ANNOUNCEMENT_EXP_DATE, announcement.getExpDate());
        contentValues.put(ANNOUNCEMENT_LECTURER_NAME, announcement.getLecturer());
        contentValues.put(ANNOUNCEMENT_IS_URGENT, announcement.isUrgentInt());

        sqLiteDatabase.insert(ANNOUNCEMENT_TABLE_NAME, ANNOUNCEMENT_ID, contentValues);

        // Close the sqLiteDatabase object to release resources
        sqLiteDatabase.close();
    }

    /**
     * Adds a material to the database
     * @param material the material to be added
     */
    public void addMaterial(Material material) {
        // Get a writable database instance
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //
        contentValues.put(MATERIAL_ID, material.getMaterialId());
        contentValues.put(MATERIAL_NAME, material.getTitle());
        contentValues.put(MATERIAL_DESC, material.getDescription());
        contentValues.put(MATERIAL_AVAILABLE_OFFLINE, material.getAvailableOfflineStatus());
        contentValues.put(MATERIAL_COURSE_ID, material.getParentCourseId());
        contentValues.put(MATERIAL_FILE_FORMAT, material.getFileFormat());
        contentValues.put(MATERIAL_PUB_DATE, material.getPublishedDate());

        // Add Offline location if material is offline available
        if (material.getAvailableOfflineStatus() == Material.MATERIAL_AVAILABLE_OFFLINE){
            contentValues.put(MATERIAL_OFFLINE_LOCATION, material.getOfflineLocation());
        }

        sqLiteDatabase.insert(MATERIAL_TABLE_NAME, MATERIAL_ID, contentValues);

        // Close the sqLiteDatabase
        sqLiteDatabase.close();

    }

    /**
     * Retrieves an ArrayList of materials in descending order of published date.
     * @param courseID if courseID's value is
     *                 <ul>
     *                     <ol>
     *                         -1, every material in the db is returned
     *                     </ol>
     *                     <ol>
     *                         any other positive number, indicates material from a course with
     *                         the given ID
     *                     </ol>
     *                 </ul>
     * @return ArrayList of materials
     */
    public ArrayList<Material> getMaterials(int courseID) {

        // Get a readable database instance
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // Query String
        String query;

        // If course id is not set get all the materials else get materials only from that course
        if (courseID == -1){
            query = "SELECT * FROM " + MATERIAL_TABLE_NAME + " ORDER BY " + MATERIAL_PUB_DATE + " DESC";
        } else {
            query = "SELECT * FROM " + MATERIAL_TABLE_NAME +
                    " WHERE " +MATERIAL_COURSE_ID + " = " + courseID +
                    " ORDER BY " + MATERIAL_PUB_DATE + " DESC";
        }

        // Cursor get the cursor after the querying the db
        Cursor cursor = sqLiteDatabase.rawQuery(
                query,
                null
        );

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return null;
        }

        // ArrayList to return
        ArrayList<Material> materials = new ArrayList<>();

        // Iterate through the cursor and each material to the array
        while (cursor.moveToNext()) {
            // Get the data out of each row
            // and construct a Material object
            Material material = new Material(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(4),
                    cursor.getLong(3)
            );

            if (Material.MATERIAL_AVAILABLE_OFFLINE == cursor.getInt(6)){
                material.setOfflineLocation(cursor.getString(7));
            }

            // Add the Material object to the ArrayList
            materials.add(material);
        }

        // Close DB and Cursor object to release resources
        sqLiteDatabase.close();
        cursor.close();

        return materials;
    }

    /**
     * Retrieves the latest 3 materials from the database
     * @return ArrayList containing the latest 3 materials
     */
    public ArrayList<Material> getLatestMaterials() {
        // Get a readable database instance
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // Cursor get the cursor after the querying the db
        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MATERIAL_TABLE_NAME + " ORDER BY " + MATERIAL_PUB_DATE + " DESC LIMIT 3",
                null
        );

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return null;
        }

        // ArrayList to return
        ArrayList<Material> materials = new ArrayList<>();

        // Iterate through the cursor and each material to the array
        while (cursor.moveToNext()) {
            // Get the data out of each row
            // and construct a Material object
            Material material = new Material(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(4),
                    cursor.getLong(3)
            );

            if (Material.MATERIAL_AVAILABLE_OFFLINE == cursor.getInt(6)){
                material.setOfflineLocation(cursor.getString(7));
            }

            // Add the Material object to the ArrayList
            materials.add(material);
        }

        // Close DB and Cursor object to release resources
        sqLiteDatabase.close();
        cursor.close();

        return materials;
    }

    /**
     * Updates the download status of a material
     * @param materialID The ID of the material to be updated
     * @param materialStatus The status value to be updated.
     *                       Can be one of three values: {@link Material#MATERIAL_AVAILABLE_OFFLINE},
     *                       {@link Material#MATERIAL_DOWNLOADING} or
     *                       {@link Material#MATERIAL_NOT_AVAILABLE}
     * @param downloadID If download has already started, the ID that {@link android.app.DownloadManager} provided
     * @param downloadLocation If download has already started, the local location of the download
     */
    public void setMaterialDownloadStatus(int materialID, int materialStatus, long downloadID, @Nullable String downloadLocation) {
        // Get writable database
        SQLiteDatabase database = getWritableDatabase();

        // Values to update
        ContentValues contentValues = new ContentValues();
        contentValues.put(MATERIAL_DOWNLOAD_ID, downloadID);
        contentValues.put(MATERIAL_AVAILABLE_OFFLINE, materialStatus);
        if (downloadLocation != null) {
            contentValues.put(MATERIAL_OFFLINE_LOCATION, downloadLocation);
        }

        // WHERE clause
        String where = MATERIAL_ID + " = ?";
        String[] whereArgs = {"" + materialID};

        // Update the table
        database.update(MATERIAL_TABLE_NAME, contentValues, where, whereArgs);

        // Close the db
        database.close();
    }

    /**
     * Retrieves materials marked as {@link Material#MATERIAL_DOWNLOADING}, which means that they
     * currently in the process of downloading
     * @return ArrayList of Material currently downloading
     */
    public ArrayList<Material> getDownloadingMaterials() {

        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + MATERIAL_TABLE_NAME +
                        " WHERE " + MATERIAL_AVAILABLE_OFFLINE + " = " + Material.MATERIAL_DOWNLOADING,
                null);

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return null;
        }

        // ArrayList to return
        ArrayList<Material> materials = new ArrayList<>();

        // Iterate through the cursor and each material to the array
        while (cursor.moveToNext()) {
            // Get the data out of each row
            // and construct a Material object
            Material material = new Material(
                    cursor.getInt(0),
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(5),
                    cursor.getInt(6),
                    cursor.getInt(4),
                    cursor.getLong(3)
            );

            material.setDownloadID(cursor.getLong(8));

            if (Material.MATERIAL_AVAILABLE_OFFLINE == cursor.getInt(6)){
                material.setOfflineLocation(cursor.getString(7));
            }

            // Add the Material object to the ArrayList
            materials.add(material);
        }

        // Close DB and Cursor object to release resources
        sqLiteDatabase.close();
        cursor.close();

        return materials;
    }

    /**
     * Retrieves a list of announcements that hasn't expired yet in descending order of published
     * date
     * @return ArrayList of Announcement
     */
    public ArrayList<Announcement> getAnnouncements(){

        // Get a readable database instance
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // Date selection clause
        long now = Long.parseLong(PushUtils.calendarToString(Calendar.getInstance(), true));

        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + ANNOUNCEMENT_TABLE_NAME +
                        " WHERE " + ANNOUNCEMENT_EXP_DATE + " > " + now +
                        " ORDER BY " + ANNOUNCEMENT_PUB_DATE + " DESC",
                null);

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return null;
        }

        // ArrayList to return
        ArrayList<Announcement> announcements = new ArrayList<>();

        // Loop through every row and add each announcement to list
        while (cursor.moveToNext()){

            // Get the integer values for isUrgent and hasBeenNotified and change them to bool
            // this is because SQLite doesn't support boolean data types
            boolean isUrgent = cursor.getInt(5) == 1;

            // Map the returned rows to announcement objects and add them to the list
            announcements.add(
                    new Announcement(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(4),
                            cursor.getLong(2),
                            cursor.getLong(3),
                            isUrgent
                    )
            );
        }

        // Close DB and Cursor object to release resources
        sqLiteDatabase.close();
        cursor.close();

        return announcements;
    }

    /**
     * Add a new course to the database
     * @param id the ID of the course
     * @param name The name of the course
     * @param section The section where the course is given to
     */
    public long addCourse(int id, String name, String section) {
        // Get a writable database instance
        SQLiteDatabase sqLiteDatabase = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        //
        contentValues.put(COURSE_ID, id);
        contentValues.put(COURSE_NAME, name);
        contentValues.put(COURSE_SECTION, section);


        long insertedRowID;
        try {
            insertedRowID = sqLiteDatabase.insert(COURSE_TABLE_NAME, COURSE_ID, contentValues);
        } catch (SQLiteConstraintException exception) {
            insertedRowID = -1L;
            Log.e(TAG, "Course " + name + " already exists.");
        }

        // Close the sqLiteDatabase
        sqLiteDatabase.close();

        return insertedRowID;
    }

    /**
     * Removes a course from the db based on the ID AND section of the course
     * @param courseID the ID of the course to be removed
     * @param sectionCode the section code of the course to be removed
     */
    public void removeCourse(int courseID, String sectionCode) {
        SQLiteDatabase database = getWritableDatabase();
        database.delete(COURSE_TABLE_NAME,
                COURSE_ID + " = ? AND " + COURSE_SECTION + " = ?",
                new String[] {courseID + "", sectionCode});
        database.close();
    }

    /**
     * Retrieves a list of Courses from the database and maps them as Course Folders
     * @return ArrayList of {@link Course}
     */
    public ArrayList<Course> getCourses() {
        // Get a readable database instance
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // Cursor get the cursor after the querying the db
        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT * FROM " + COURSE_TABLE_NAME,
                null
        );

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return null;
        }

        // The array to return
        ArrayList<Course> courses = new ArrayList<>();

        // Loop through the cursor and add the course to the array
        while (cursor.moveToNext()){
            // Folder object constructed from this row
            Course course;

            // Course ID
            int id = cursor.getInt(0);
            String name = cursor.getString(1);
            String sectionCode = cursor.getString(2);
            int noOfFiles;

            // Query the db for number of files the course has
            Cursor noOfFilesCursor = sqLiteDatabase.rawQuery(
                    "SELECT COUNT(*) FROM " + MATERIAL_TABLE_NAME +
                            " WHERE " + MATERIAL_COURSE_ID + " = " + id,
                    null
            );
            noOfFilesCursor.moveToFirst();
            noOfFiles = noOfFilesCursor.getInt(0);

            // Close noOfFiles cursor
            noOfFilesCursor.close();

            // Construct the Course object
            course = new Course(name, id, noOfFiles);
            course.setSectionCode(sectionCode);

            // Add the course to the array
            courses.add(course);
        }

        // Close the cursor and the db
        cursor.close();
        sqLiteDatabase.close();

        // Return the list of Courses
        return courses;
    }

    /**
     * Returns a single course mapped as a Folder
     * @param courseID the ID of the course
     * @return a single course mapped as a Folder
     */
    public Course getCourse(int courseID){
        // Get a readable database instance
        SQLiteDatabase sqLiteDatabase = this.getReadableDatabase();

        // Cursor get the cursor after the querying the db
        Cursor cursor = sqLiteDatabase.rawQuery(
                "SELECT " + COURSE_NAME + ", " + COURSE_SECTION + " FROM " + COURSE_TABLE_NAME +
                        " WHERE " + COURSE_ID + " = " + courseID,
                null
        );

        // Exit if there are no rows to process
        if (cursor.getCount() < 1){
            sqLiteDatabase.close();
            cursor.close();
            return new Course("Unknown", courseID, -1);
        }
        cursor.moveToFirst();
        String courseName = cursor.getString(0);
        String sectionCode = cursor.getString(1);

        // Close Cursor and DB object
        cursor.close();
        sqLiteDatabase.close();

        // Return course name
        return new Course(courseID, courseName, sectionCode);
    }
}
