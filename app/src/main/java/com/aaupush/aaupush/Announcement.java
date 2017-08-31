package com.aaupush.aaupush;


import java.util.Date;

/**
 * A model for an announcement
 */

public class Announcement {

    private int id;
    private String announcement;
    private String lecturer;
    private long postDate;
    private long expDate;
    private int section;
    private int year;
    private boolean isUrgent;
    private boolean hasBeenNotified;

    public Announcement(int id, String announcement, String lecturer, long postDate, long expDate,
                        int section, int year, boolean isUrgent, boolean hasBeenNotified) {
        this.id = id;
        this.announcement = announcement;
        this.lecturer = lecturer;
        this.postDate = postDate;
        this.expDate = expDate;
        this.section = section;
        this.year = year;
        this.isUrgent = isUrgent;
        this.hasBeenNotified = hasBeenNotified;
    }

    public int getId() {
        return id;
    }

    public String getAnnouncement() {
        return announcement;
    }

    public String getLecturer() {
        return lecturer;
    }

    public long getPostDate() {
        return postDate;
    }

    public long getExpDate() {
        return expDate;
    }

    public int getSection() {
        return section;
    }

    public int getYear() {
        return year;
    }

    public int isUrgentInt(){
        return isUrgent ? 1 : 0;
    }

    public boolean isUrgent(){
        return isUrgent;
    }

    public int hasBeenNotifiedInt(){
        return hasBeenNotified ? 1: 0;
    }

    public boolean hasBeenNotified(){
        return hasBeenNotified;
    }

}
