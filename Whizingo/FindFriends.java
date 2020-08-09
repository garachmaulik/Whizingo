package com.mg.socialmedia;

public class FindFriends
{
    private String ProfileImage,FullName,Status;

    FindFriends()
    {

    }

    public FindFriends(String profileImage, String fullName, String status) {
        ProfileImage = profileImage;
        FullName = fullName;
        Status = status;
    }

    public String getProfileImage() {
        return ProfileImage;
    }

    public void setProfileImage(String profileImage) {
        ProfileImage = profileImage;
    }

    public String getFullName() {
        return FullName;
    }

    public void setFullName(String fullName) {
        FullName = fullName;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }
}
