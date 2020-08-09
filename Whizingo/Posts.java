package com.mg.socialmedia;

public class Posts
{
    private String  Date,Description,FullName,PostImage,ProfileImage,Time,UID;

    Posts ()
    {

    }
    public Posts(String UID, String time, String date, String postimage, String description, String profileimage, String fullName) {
        this.UID = UID;
        Time = time;
        Date = date;
        PostImage = postimage;
        Description = description;
        ProfileImage = profileimage;
        FullName = fullName;
    }

    public String getUID() {
        return UID;
    }

    public void setUID(String UID) {
        this.UID = UID;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getPostImage() {
        return PostImage;
    }

    public void setPostImage(String postimage) {
        PostImage = postimage;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileimage) {
        ProfileImage = profileimage;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }
}
