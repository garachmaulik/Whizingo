package com.mg.socialmedia;

public class Comments
{
    private String UID,Comment,Date,Time,Username,ProfileImage;

    public Comments(){}

    public Comments(String UID, String comment, String date, String time, String username,String profileImage) {
        this.UID = UID;
        Comment = comment;
        Date = date;
        Time = time;
        Username = username;
        ProfileImage = profileImage;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        Comment = comment;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }
}
