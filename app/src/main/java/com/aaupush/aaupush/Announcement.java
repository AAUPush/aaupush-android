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
    private boolean isUrgent;

    public Announcement(int id, String announcement, String lecturer, long postDate, long expDate,
                         boolean isUrgent) {
        this.id = id;
        this.announcement = announcement;
        this.lecturer = lecturer;
        this.postDate = postDate;
        this.expDate = expDate;
        this.isUrgent = isUrgent;
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

    public int isUrgentInt(){
        return isUrgent ? 1 : 0;
    }

    public boolean isUrgent(){
        return isUrgent;
    }

}
