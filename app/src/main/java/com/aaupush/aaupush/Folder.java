package com.aaupush.aaupush;


public class Folder {
    private String name;
    private int courseID;
    private int numberOfFiles;
    private int courseYear;

    public Folder(String name, int courseID, int numberOfFiles) {
        this.name = name;
        this.courseID = courseID;
        this.numberOfFiles = numberOfFiles;
    }

    public Folder( int courseID, String name, int year) {
        this.name = name;
        this.courseID = courseID;
        this.courseYear = year;
    }

    public String getName() {
        return name;
    }

    public int getCourseID() {
        return courseID;
    }

    public int getNumberOfFiles() {
        return numberOfFiles;
    }

    public int getCourseYear() {
        return courseYear;
    }
}
