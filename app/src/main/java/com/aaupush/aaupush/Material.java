package com.aaupush.aaupush;

public class Material {

    // Available Online Status
    static final int MATERIAL_AVAILABLE_OFFLINE = 1;
    static final int MATERIAL_DOWNLOADING = 2;
    static final int MATERIAL_NOT_AVAILABLE = 3;

    private int materialId;
    private String title;
    private String description;
    private String fileFormat;
    private int availableOfflineStatus;
    private int parentCourseId;
    private long downloadID;
    private long publishedDate;
    private float fileSize; // in KB
    private String offlineLocation; // May have to change this to a URI

    public Material(int materialId, String title, String description, String fileFormat,
                    int availableOfflineStatus, int parentCourseId, long publishedDate,
                    float fileSize) {
        this.materialId = materialId;
        this.title = title;
        this.description = description;
        this.fileFormat = fileFormat;
        this.availableOfflineStatus = availableOfflineStatus;
        this.parentCourseId = parentCourseId;
        this.publishedDate = publishedDate;
        this.fileSize = fileSize;
    }

    public int getMaterialId() {
        return materialId;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getFileFormat() {
        return fileFormat;
    }

    public int getAvailableOfflineStatus() {
        return availableOfflineStatus;
    }

    public long getDownloadID() {
        return downloadID;
    }

    public void setDownloadID(long downloadID) {
        this.downloadID = downloadID;
    }

    public int getParentCourseId() {
        return parentCourseId;
    }

    public long getPublishedDate() {
        return publishedDate;
    }

    public String getOfflineLocation() {
        return offlineLocation;
    }

    public float getFileSize() {
        return fileSize;
    }

    public void setOfflineLocation(String offlineLocation) {
        this.offlineLocation = offlineLocation;
    }

    public void setAvailableOfflineStatus(int availableOfflineStatus) {
        this.availableOfflineStatus = availableOfflineStatus;
    }
}
