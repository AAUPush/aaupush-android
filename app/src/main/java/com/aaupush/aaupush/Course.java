package com.aaupush.aaupush;


public class Course {
    private String name;
    private int courseID;
    private int numberOfFiles;
    private String sectionCode;

    public Course(String name, int courseID, int numberOfFiles) {
        this.name = name;
        this.courseID = courseID;
        this.numberOfFiles = numberOfFiles;
    }

    public Course(int courseID, String name, String sectionCode) {
        this.name = name;
        this.courseID = courseID;
        this.sectionCode = sectionCode;
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

    public String getSectionCode() {
        return sectionCode;
    }
}
